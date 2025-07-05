use std::sync::Arc;
use tokio::sync::RwLock;
use anyhow::{Result, anyhow};
use serde::{Serialize, Deserialize};
use tracing::{info, error, debug};
use std::collections::HashMap;
use std::time::{SystemTime, UNIX_EPOCH};
use crate::blockchain::{Block, Transaction, Blockchain};
use crate::consensus::{AIValidator, VotingSystem, ValidatorReputation};
use crate::storage::hybrid_storage::{BlockProposal, ProposalStatus};
use crate::network::NetworkNode;

#[derive(Debug, Serialize, Deserialize)]
pub struct BlockProposalConfig {
    pub proposal_window: u64,         // Duration of proposal window in seconds
    pub min_stake: u64,              // Minimum stake required to propose
    pub max_tx_per_block: u32,       // Maximum transactions per block
    pub block_reward: u64,           // Block reward for proposer
    pub proposal_timeout: u64,       // Timeout for proposal acceptance
}

#[derive(Debug)]
pub struct BlockProposalSystem {
    config: BlockProposalConfig,
    blockchain: Arc<RwLock<Blockchain>>,
    validator: Arc<RwLock<AIValidator>>,
    voting: Arc<RwLock<VotingSystem>>,
    network: Arc<RwLock<NetworkNode>>,
    proposals: Arc<RwLock<HashMap<u64, BlockProposal>>>,
    current_proposal: Arc<RwLock<Option<u64>>>,
    metrics: Arc<BlockProposalMetrics>,
}

#[derive(Debug)]
pub struct BlockProposalMetrics {
    pub total_proposals: u64,
    pub successful_proposals: u64,
    pub failed_proposals: u64,
    pub avg_proposal_time: u64,
    pub avg_tx_count: u64,
}

impl BlockProposalSystem {
    pub fn new(
        blockchain: Arc<RwLock<Blockchain>>,
        validator: Arc<RwLock<AIValidator>>,
        voting: Arc<RwLock<VotingSystem>>,
        network: Arc<RwLock<NetworkNode>>,
        proposal_window: u64,
    ) -> Result<Self> {
        let config = BlockProposalConfig {
            proposal_window,
            min_stake: 1000000, // 1000 SELF tokens
            max_tx_per_block: 100,
            block_reward: 500000, // 500 SELF tokens
            proposal_timeout: 30,
        };

        let metrics = Arc::new(BlockProposalMetrics {
            total_proposals: 0,
            successful_proposals: 0,
            failed_proposals: 0,
            avg_proposal_time: 0,
            avg_tx_count: 0,
        });

        Ok(Self {
            config,
            blockchain,
            validator,
            voting,
            network,
            proposals: Arc::new(RwLock::new(HashMap::new())),
            current_proposal: Arc::new(RwLock::new(None)),
            metrics,
        })
    }

    pub async fn create_block_proposal(&self, txs: Vec<Transaction>) -> Result<BlockProposal> {
        let validator_id = self.validator.read().await.get_validator_id().await?;
        let current_time = SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs();
        
        // Check if validator is eligible
        if !self.validator.read().await.is_eligible(&validator_id).await? {
            return Err(anyhow!("Validator not eligible to create proposal"));
        }

        // Get current chain state
        let blockchain = self.blockchain.read().await;
        let last_block = blockchain.get_last_block().await?;
        
        // Create new block
        let block = Block::new(
            last_block.index + 1,
            current_time,
            last_block.hash.clone(),
            txs,
            validator_id.clone(),
        )?;

        // Validate block
        self.validator.read().await.validate_block(&block).await?;

        // Create proposal
        let proposal = BlockProposal {
            id: self.get_next_proposal_id().await,
            block,
            proposer_id: validator_id,
            timestamp: current_time,
            status: ProposalStatus::Pending,
            votes: HashMap::new(),
        };

        // Store proposal
        self.proposals.write().await.insert(proposal.id, proposal.clone());
        *self.current_proposal.write().await = Some(proposal.id);

        // Start proposal timer
        self.start_proposal_timer(proposal.id).await;

        // Broadcast proposal
        self.network.read().await.broadcast_block_proposal(&proposal).await?;

        info!("Created block proposal {} with {} transactions", proposal.id, txs.len());
        Ok(proposal)
    }

    pub async fn process_block_proposal(&self, proposal: BlockProposal) -> Result<()> {
        // Validate proposal
        if !self.is_valid_proposal(&proposal)? {
            return Err(anyhow!("Invalid proposal"));
        }

        // Store proposal
        self.proposals.write().await.insert(proposal.id, proposal.clone());
        *self.current_proposal.write().await = Some(proposal.id);

        // Start voting round
        self.voting.read().await.start_voting_round(&proposal.block).await?;

        Ok(())
    }

    async fn start_proposal_timer(&self, proposal_id: u64) {
        let system = self.clone();
        tokio::spawn(async move {
            tokio::time::sleep(std::time::Duration::from_secs(system.config.proposal_timeout)).await;
            if let Err(e) = system.complete_proposal(proposal_id).await {
                error!("Failed to complete proposal {}: {}", proposal_id, e);
            }
        });
    }

    async fn complete_proposal(&self, proposal_id: u64) -> Result<()> {
        let mut proposals = self.proposals.write().await;
        if let Some(proposal) = proposals.get_mut(&proposal_id) {
            // Check voting result
            let voting = self.voting.read().await;
            let round = voting.get_current_round().await?;
            
            if let Some(round) = round {
                let result = voting.calculate_result(&round)?;

                // Update proposal status
                proposal.status = if result {
                    self.metrics.successful_proposals += 1;
                    ProposalStatus::Accepted
                } else {
                    self.metrics.failed_proposals += 1;
                    ProposalStatus::Rejected
                };

                // Add block to chain if accepted
                if result {
                    self.blockchain.write().await.add_block(&proposal.block).await?;
                    self.distribute_block_reward(&proposal).await?;
                }

                // Broadcast result
                self.network.read().await.broadcast_proposal_result(&proposal, result).await?;
            }
        }

        Ok(())
    }

    async fn distribute_block_reward(&self, proposal: &BlockProposal) -> Result<()> {
        let validator = self.validator.read().await;
        let validator_id = proposal.proposer_id.clone();
        
        // Get validator state
        let validator_state = validator.get_validator(&validator_id).await?;
        
        // Update validator state with reward
        validator.update_validator_stake(&validator_id, validator_state.stake + self.config.block_reward).await?;
        
        Ok(())
    }

    fn is_valid_proposal(&self, proposal: &BlockProposal) -> Result<bool> {
        // Check if proposer is eligible
        if !self.validator.read().await.is_eligible(&proposal.proposer_id).await? {
            return Ok(false);
        }

        // Check transaction count
        if proposal.block.transactions.len() > self.config.max_tx_per_block as usize {
            return Ok(false);
        }

        // Check block validity
        if !proposal.block.verify()? {
            return Ok(false);
        }

        Ok(true)
    }

    async fn get_next_proposal_id(&self) -> u64 {
        let proposals = self.proposals.read().await;
        proposals.keys().max().unwrap_or(&0) + 1
    }

    pub async fn get_block_proposal(&self, proposal_id: u64) -> Result<Option<BlockProposal>> {
        let proposals = self.proposals.read().await;
        Ok(proposals.get(&proposal_id).cloned())
    }

    pub async fn get_current_proposal(&self) -> Result<Option<BlockProposal>> {
        let current_proposal = *self.current_proposal.read().await.unwrap_or(0);
        if current_proposal == 0 {
            return Ok(None);
        }

        let proposals = self.proposals.read().await;
        Ok(proposals.get(&current_proposal).cloned())
    }

    pub async fn get_proposal_metrics(&self) -> BlockProposalMetrics {
        self.metrics.clone()
    }
}
