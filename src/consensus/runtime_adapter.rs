use crate::runtime::ConsensusEngine;
use crate::consensus::PoAI;
use crate::network::message::NetworkMessage;
use crate::blockchain::Block;
use anyhow::Result;
use std::sync::Arc;
use tokio::sync::RwLock;
use tracing::{info, warn, error};

/// Adapter to integrate PoAI consensus with the runtime framework
pub struct PoAIConsensusAdapter {
    consensus: Arc<PoAI>,
    pending_blocks: Arc<RwLock<Vec<Block>>>,
    node_id: String,
}

impl PoAIConsensusAdapter {
    pub fn new(consensus: Arc<PoAI>, node_id: String) -> Self {
        Self {
            consensus,
            pending_blocks: Arc::new(RwLock::new(Vec::new())),
            node_id,
        }
    }
}

#[async_trait::async_trait]
impl ConsensusEngine for PoAIConsensusAdapter {
    type NetworkMsg = NetworkMessage;

    async fn tick(&mut self) -> Result<()> {
        // Process any pending blocks
        let pending = self.pending_blocks.read().await;
        if !pending.is_empty() {
            info!("Processing {} pending blocks", pending.len());
            
            for block in pending.iter() {
                match self.consensus.validate_block(block).await {
                    Ok(is_valid) => {
                        if is_valid {
                            info!("Block {} validated successfully", block.hash);
                            // Cast vote for valid blocks
                            if let Err(e) = self.consensus.cast_vote(&self.node_id, block).await {
                                warn!("Failed to cast vote for block {}: {}", block.hash, e);
                            }
                        } else {
                            warn!("Block {} validation failed", block.hash);
                        }
                    }
                    Err(e) => {
                        error!("Error validating block {}: {}", block.hash, e);
                    }
                }
            }
            
            // Clear processed blocks
            drop(pending);
            self.pending_blocks.write().await.clear();
        }
        
        Ok(())
    }

    async fn handle_network_msg(&mut self, msg: Self::NetworkMsg) -> Result<()> {
        match msg {
            NetworkMessage::NewBlock(block_data) => {
                info!("Received new block data from network");
                // TODO: Deserialize block from string and add to pending
                // self.pending_blocks.write().await.push(block);
            }
            NetworkMessage::Transaction(tx_data) => {
                // Transactions are handled separately by the transaction pool
                info!("Received transaction data from network");
            }
            NetworkMessage::Vote(vote) => {
                info!("Received vote from {} for block {}", vote.validator_id, vote.block_hash);
                // Votes are handled by the voting system through message handler
            }
            NetworkMessage::VotingStart(block) => {
                info!("Received voting start for block {}", block.hash);
                // Add to pending blocks for validation
                self.pending_blocks.write().await.push(block);
            }
            _ => {
                // Other message types are handled by the network layer
            }
        }
        
        Ok(())
    }
}