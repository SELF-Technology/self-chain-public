use std::sync::Arc;
use tokio::sync::RwLock;
use anyhow::{Result, anyhow};
use serde::{Serialize, Deserialize};
use tracing::{info, error, debug};
use std::collections::HashMap;
use std::time::{SystemTime, UNIX_EPOCH};
use crate::blockchain::{Block, Transaction};
use crate::consensus::{AIValidator, ValidatorReputation};
use crate::network::NetworkNode;
use crate::storage::hybrid_storage::{ValidatorState, VotingRound};

#[derive(Debug, Serialize, Deserialize)]
pub struct VotingConfig {
    pub voting_window: u64,         // Duration of voting round in seconds
    pub min_voting_power: u64,     // Minimum stake required to vote
    pub max_voting_rounds: u64,    // Maximum number of voting rounds
    pub quorum_percentage: f64,    // Percentage of validators needed for quorum
    pub vote_timeout_secs: u64,    // Timeout for individual votes
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Vote {
    pub validator_id: String,
    pub block_hash: String,
    pub timestamp: u64,
    pub vote: bool,
    pub signature: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct VotingRound {
    pub round_id: u64,
    pub block_hash: String,
    pub start_time: u64,
    pub end_time: u64,
    pub votes: HashMap<String, Vote>, // validator_id -> Vote
    pub status: VotingStatus,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum VotingStatus {
    Pending,
    InProgress,
    Completed,
    Failed,
}

#[derive(Debug)]
pub struct VotingSystem {
    config: VotingConfig,
    validator: Arc<RwLock<AIValidator>>,
    network: Arc<RwLock<NetworkNode>>,
    voting_rounds: Arc<RwLock<HashMap<u64, VotingRound>>>,
    current_round: Arc<RwLock<Option<u64>>>,
    metrics: Arc<VotingMetrics>,
}

#[derive(Debug)]
pub struct VotingMetrics {
    pub total_rounds: u64,
    pub successful_rounds: u64,
    pub failed_rounds: u64,
    pub avg_participation: f64,
    pub avg_duration: u64,
}

impl VotingSystem {
    pub fn new(
        validator: Arc<RwLock<AIValidator>>,
        network: Arc<RwLock<NetworkNode>>,
        voting_window: u64,
    ) -> Result<Self> {
        let config = VotingConfig {
            voting_window,
            min_voting_power: 1000000, // 1000 SELF tokens
            max_voting_rounds: 10,
            quorum_percentage: 0.67,    // 67% quorum required
            vote_timeout_secs: 30,
        };

        let metrics = Arc::new(VotingMetrics {
            total_rounds: 0,
            successful_rounds: 0,
            failed_rounds: 0,
            avg_participation: 0.0,
            avg_duration: 0,
        });

        Ok(Self {
            config,
            validator,
            network,
            voting_rounds: Arc::new(RwLock::new(HashMap::new())),
            current_round: Arc::new(RwLock::new(None)),
            metrics,
        })
    }

    pub async fn start_voting_round(&self, block: &Block) -> Result<()> {
        let validator_id = self.validator.read().await.get_validator_id().await?;
        let current_time = SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs();
        
        // Check if validator is eligible
        if !self.validator.read().await.is_eligible(&validator_id).await? {
            return Err(anyhow!("Validator not eligible to start voting round"));
        }

        // Create new voting round
        let round_id = self.get_next_round_id().await;
        let end_time = current_time + self.config.voting_window;
        
        let round = VotingRound {
            round_id,
            block_hash: block.hash.clone(),
            start_time: current_time,
            end_time,
            votes: HashMap::new(),
            status: VotingStatus::InProgress,
        };

        // Store round
        self.voting_rounds.write().await.insert(round_id, round.clone());
        *self.current_round.write().await = Some(round_id);

        // Start round timer
        self.start_round_timer(round_id).await;

        // Broadcast voting start
        self.network.read().await.broadcast_voting_start(&round).await?;

        info!("Started voting round {} for block {}", round_id, block.hash);
        Ok(())
    }

    pub async fn process_vote(&self, vote: Vote) -> Result<()> {
        let round_id = *self.current_round.read().await.unwrap_or(0);
        if round_id == 0 {
            return Err(anyhow!("No active voting round"));
        }

        let mut rounds = self.voting_rounds.write().await;
        if let Some(round) = rounds.get_mut(&round_id) {
            // Check if vote is valid
            if !self.is_valid_vote(&vote, round)? {
                return Err(anyhow!("Invalid vote"));
            }

            // Add vote
            round.votes.insert(vote.validator_id.clone(), vote);
            info!("Added vote from {} in round {}", vote.validator_id, round_id);

            // Check if round is complete
            if self.is_quorum_reached(round)? {
                self.complete_round(round_id).await?;
            }
        }

        Ok(())
    }

    async fn start_round_timer(&self, round_id: u64) {
        let system = self.clone();
        tokio::spawn(async move {
            tokio::time::sleep(std::time::Duration::from_secs(system.config.voting_window)).await;
            if let Err(e) = system.complete_round(round_id).await {
                error!("Failed to complete round {}: {}", round_id, e);
            }
        });
    }

    async fn complete_round(&self, round_id: u64) -> Result<()> {
        let mut rounds = self.voting_rounds.write().await;
        if let Some(round) = rounds.get_mut(&round_id) {
            // Calculate result
            let result = self.calculate_result(round)?;
            
            // Update metrics
            self.metrics.total_rounds += 1;
            self.metrics.avg_participation = self.calculate_avg_participation(round);
            self.metrics.avg_duration = (SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs() - round.start_time) / 2;

            // Update round status
            round.status = match result {
                true => {
                    self.metrics.successful_rounds += 1;
                    VotingStatus::Completed
                }
                false => {
                    self.metrics.failed_rounds += 1;
                    VotingStatus::Failed
                }
            };

            // Broadcast result
            self.network.read().await.broadcast_voting_result(round, result).await?;
        }

        Ok(())
    }

    fn calculate_result(&self, round: &VotingRound) -> Result<bool> {
        let mut yes_votes = 0;
        let mut total_votes = 0;
        
        for vote in round.votes.values() {
            if vote.vote {
                yes_votes += 1;
            }
            total_votes += 1;
        }

        Ok((yes_votes as f64 / total_votes as f64) >= self.config.quorum_percentage)
    }

    fn calculate_avg_participation(&self, round: &VotingRound) -> f64 {
        let validators = self.validator.read().await.get_eligible_validators().await.unwrap_or_default();
        let total_validators = validators.len() as f64;
        let total_votes = round.votes.len() as f64;
        
        total_votes / total_validators
    }

    fn is_valid_vote(&self, vote: &Vote, round: &VotingRound) -> Result<bool> {
        // Check if validator is eligible
        if !self.validator.read().await.is_eligible(&vote.validator_id).await? {
            return Ok(false);
        }

        // Check if vote is within voting window
        let current_time = SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs();
        if current_time > round.end_time {
            return Ok(false);
        }

        // Check if validator has already voted
        if round.votes.contains_key(&vote.validator_id) {
            return Ok(false);
        }

        // Check if validator has sufficient stake
        let validator = self.validator.read().await.get_validator(&vote.validator_id).await?;
        if validator.stake < self.config.min_voting_power {
            return Ok(false);
        }

        Ok(true)
    }

    fn is_quorum_reached(&self, round: &VotingRound) -> Result<bool> {
        let validators = self.validator.read().await.get_eligible_validators().await.unwrap_or_default();
        let total_validators = validators.len() as f64;
        let total_votes = round.votes.len() as f64;
        
        Ok(total_votes / total_validators >= self.config.quorum_percentage)
    }

    async fn get_next_round_id(&self) -> u64 {
        let rounds = self.voting_rounds.read().await;
        rounds.keys().max().unwrap_or(&0) + 1
    }

    pub async fn get_voting_status(&self) -> Result<VotingStatus> {
        let current_round = *self.current_round.read().await.unwrap_or(0);
        if current_round == 0 {
            return Ok(VotingStatus::Pending);
        }

        let rounds = self.voting_rounds.read().await;
        if let Some(round) = rounds.get(&current_round) {
            Ok(round.status.clone())
        } else {
            Ok(VotingStatus::Pending)
        }
    }

    pub async fn get_voting_round(&self, round_id: u64) -> Result<Option<VotingRound>> {
        let rounds = self.voting_rounds.read().await;
        Ok(rounds.get(&round_id).cloned())
    }

    pub async fn get_current_round(&self) -> Result<Option<VotingRound>> {
        let current_round = *self.current_round.read().await.unwrap_or(0);
        if current_round == 0 {
            return Ok(None);
        }

        let rounds = self.voting_rounds.read().await;
        Ok(rounds.get(&current_round).cloned())
    }
}
