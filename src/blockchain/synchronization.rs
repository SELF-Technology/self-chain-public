use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::sync::Arc;
use tokio::sync::RwLock;
use tracing::{debug, error, info, warn};

use crate::blockchain::{Block, Blockchain, Transaction};
use crate::network::message_handler::{MessageHandler, NetworkMessage};
// Alias for clarity
#[allow(dead_code)]
type CoreNetworkMessage = crate::network::NetworkMessage;
use crate::ai::context_manager::ContextManager;
use crate::ai::pattern_analysis::{PatternAnalysisRequest, PatternAnalyzer, PatternType};
use crate::ai::validator::AIValidator;

// Message type for synchronization
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum MessageType {
    SyncHeightRequest,
    SyncHeightResponse,
    SyncBlocksRequest,
    SyncBlocksResponse,
    SyncError,
}

/// Response for a block synchronization request
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SyncResponse {
    pub blocks: Vec<Block>,
    pub current_height: u64,
    pub status: SyncStatus,
}

/// Status of the synchronization process
#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum SyncStatus {
    Success,
    PartialSuccess,
    Failed(String),
}

/// Request for block synchronization
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SyncRequest {
    pub from_height: u64,
    pub to_height: Option<u64>,
    pub max_blocks: Option<u64>,
    pub requesting_node_id: String,
}

/// Block Synchronization Service for PoAI-compatible block synchronization
#[derive(Debug)]
pub struct BlockSynchronizer {
    blockchain: Arc<RwLock<Blockchain>>,
    message_handler: Arc<RwLock<MessageHandler>>,
    ai_validator: Arc<RwLock<AIValidator>>,
    context_manager: Arc<RwLock<ContextManager>>,
    pattern_analyzer: Arc<RwLock<PatternAnalyzer>>,
    sync_state: Arc<RwLock<HashMap<String, SyncStatus>>>,
    last_sync_height: Arc<RwLock<u64>>,
    max_blocks_per_sync: u64,
}

impl BlockSynchronizer {
    /// Create a new BlockSynchronizer
    pub fn new(
        blockchain: Arc<RwLock<Blockchain>>,
        message_handler: Arc<RwLock<MessageHandler>>,
        ai_validator: Arc<RwLock<AIValidator>>,
        context_manager: Arc<RwLock<ContextManager>>,
        pattern_analyzer: Arc<RwLock<PatternAnalyzer>>,
    ) -> Self {
        Self {
            blockchain,
            message_handler,
            ai_validator,
            context_manager,
            pattern_analyzer,
            sync_state: Arc::new(RwLock::new(HashMap::new())),
            last_sync_height: Arc::new(RwLock::new(0)),
            max_blocks_per_sync: 100, // Default value, can be configured
        }
    }

    /// Start the synchronization process with the network
    pub async fn start_sync(&self) -> Result<(), String> {
        // Check if sync is already in progress
        let mut sync_lock = self.sync_state.write().await;
        if sync_lock.contains_key("self") {
            return Err("Synchronization already in progress".to_string());
        }

        // Mark sync as in progress
        sync_lock.insert("self".to_string(), SyncStatus::Success);
        drop(sync_lock); // Release lock before async operations

        // Get local blockchain height
        let local_height = self.get_blockchain_height().await?;

        info!(
            "Starting blockchain synchronization from height {}",
            local_height
        );

        // Get heights from peers and identify the highest one
        let peer_data = self.message_handler.read().await.get_peers().await;
        let peers = peer_data
            .iter()
            .map(|(id, _addr)| id.clone())
            .collect::<Vec<String>>();
        if peers.is_empty() {
            return Ok(());
        }

        // Create sync request
        let _request = SyncRequest {
            from_height: local_height + 1,
            to_height: None, // Request all available blocks
            max_blocks: Some(self.max_blocks_per_sync),
            requesting_node_id: "self".to_string(), // Replace with actual node ID
        };

        // Broadcast GetBlocks message
        match self
            .message_handler
            .read()
            .await
            .broadcast_message(NetworkMessage::GetBlocks)
            .await
        {
            Ok(_) => debug!("GetBlocks message broadcasted successfully"),
            Err(e) => {
                let mut new_sync_lock = self.sync_state.write().await;
                new_sync_lock.remove("self"); // Reset lock
                return Err(format!("Failed to broadcast GetBlocks message: {}", e));
            }
        }

        // In a real implementation, we would wait for responses asynchronously
        // This is just a placeholder for the synchronization logic

        // Reset sync lock
        let mut sync_lock = self.sync_state.write().await;
        sync_lock.remove("self");
        Ok(())
    }

    /// Handle incoming blocks from the network
    pub async fn handle_blocks(&self, blocks: Vec<Block>) -> Result<(), String> {
        let _transaction_count = blocks.iter().map(|b| b.transactions.len()).sum::<usize>();
        let mut _suspicious_count = 0;
        let mut _total_risk_score = 0.0;

        // Validate each transaction in the block
        for block in &blocks {
            for transaction in &block.transactions {
                // Validate the transaction using PoAI consensus
                let _is_valid = match self.validate_transaction_with_poai(transaction).await {
                    Ok(is_valid) => {
                        if !is_valid {
                            _suspicious_count += 1;
                            _total_risk_score += 1.0;
                        }
                    }
                    Err(e) => return Err(format!("Transaction validation error: {}", e)),
                };
            }
        }

        // Process each block
        for block in &blocks {
            // Validate the block using PoAI consensus
            let _is_valid = match self.validate_block_with_poai(block).await {
                Ok(is_valid) => {
                    if is_valid {
                        // Block is valid according to PoAI consensus
                        match self.blockchain.write().await.add_block(block).await {
                            Ok(_) => {
                                info!(
                                    "Successfully added block {} to the chain",
                                    block.header.index
                                );
                                // Update last sync height
                                let mut height = self.last_sync_height.write().await;
                                *height = block.header.index;
                            }
                            Err(e) => {
                                warn!(
                                    "Failed to add block {} to the chain: {}",
                                    block.header.index, e
                                );
                                // We might want to handle this failure differently depending on the error
                                // For example, if it's a "block already exists" error, we can continue
                                // But if it's an integrity error, we might want to stop
                                if e.contains("already exists") {
                                    continue;
                                } else {
                                    return Err(format!("Failed to add block to chain: {}", e));
                                }
                            }
                        }
                    } else {
                        warn!(
                            "Block {} failed PoAI consensus validation",
                            block.header.index
                        );
                        // We might want to request this block from another peer or handle this differently
                        return Err(format!(
                            "Block {} failed PoAI validation",
                            block.header.index
                        ));
                    }
                }
                Err(e) => {
                    error!("Error validating block with PoAI: {}", e);
                    return Err(format!("Block validation error: {}", e));
                }
            };
        }

        info!(
            "Block synchronization successfully processed {} blocks",
            blocks.len()
        );
        Ok(())
    } // Close the handle_blocks function

    /// Validate a transaction using PoAI consensus
    async fn validate_transaction_with_poai(
        &self,
        transaction: &Transaction,
    ) -> Result<bool, String> {
        // Step 1: Basic validation
        // We'll use simple validation before proceeding to AI validation
        // Basic checks like previous hash are correct, etc.
        // Skip for now and proceed to AI validation

        // Get transaction context for pattern analysis
        let _transaction_context = self
            .context_manager
            .read()
            .await
            .get_transaction_context(&[transaction.clone()])
            .await
            .map_err(|e| format!("Failed to get transaction context: {}", e))?;

        // Step 2: AI validation using AIValidator
        // Convert transaction to JSON for AI validation
        let transaction_json = serde_json::to_string(transaction)
            .map_err(|e| format!("Failed to serialize transaction: {}", e))?;
        let context = "blockchain_sync"; // Simple context for validation

        match self
            .ai_validator
            .read()
            .await
            .validate_transaction(&transaction_json, context)
            .await
        {
            Ok(validation_result) => {
                if !validation_result {
                    warn!("Transaction failed PoAI validation");
                    return Err("Transaction failed PoAI validation".to_string());
                }
            }
            Err(e) => return Err(format!("AI validation error: {}", e)),
        };

        // Step 3: Make validation decision based on AI and pattern analysis
        let pattern_request = PatternAnalysisRequest {
            pattern_type: PatternType::Transaction,
            block: Block::default(), // Use an empty block as placeholder
            context: None,
            validation_rules: Vec::new(),
            previous_results: None,
            correlation_data: None,
            security_level: 2,                  // Medium security level (u8)
            max_processing_time_ms: Some(5000), // 5 seconds max processing time
        };

        let pattern_result = self
            .pattern_analyzer
            .write()
            .await
            .analyze_pattern(pattern_request)
            .await
            .map_err(|e| format!("Pattern analysis failed for transaction: {}", e))?;

        if pattern_result.analysis.risk_level > 0.7 && pattern_result.analysis.confidence > 0.6 {
            // High risk with good confidence means we should reject
            debug!(
                "Transaction {} rejected by pattern analysis: {} (risk: {}, confidence: {})",
                transaction.id,
                pattern_result.score_adjustment.reasoning,
                pattern_result.analysis.risk_level,
                pattern_result.analysis.confidence
            );
            return Ok(false);
        }

        // All validation passed
        Ok(true)
    }

    /// Validate a block using PoAI consensus
    async fn validate_block_with_poai(&self, block: &Block) -> Result<bool, String> {
        // Step 1: Basic validation
        // We'll use simple validation before proceeding to AI validation
        // Basic checks like previous hash are correct, etc.
        // Skip for now and proceed to AI validation

        // Get the block's transactions
        let transactions = &block.transactions;

        // Get transaction context for pattern analysis
        let transaction_context = self
            .context_manager
            .read()
            .await
            .get_transaction_context(transactions)
            .await
            .map_err(|e| format!("Failed to get transaction context: {}", e))?;

        // Step 2: AI validation using AIValidator
        // Convert block to JSON for AI validation
        let block_json = serde_json::to_string(block)
            .map_err(|e| format!("Failed to serialize block: {}", e))?;
        let context = "blockchain_sync"; // Simple context for validation

        match self
            .ai_validator
            .read()
            .await
            .validate_block(&block_json, context)
            .await
        {
            Ok(validation_result) => {
                if !validation_result {
                    warn!("Block failed PoAI validation");
                    return Err("Block failed PoAI validation".to_string());
                }
            }
            Err(e) => {
                return Err(format!("AI validation error: {}", e));
            }
        };

        // Step 3: Make validation decision based on AI and pattern analysis
        let pattern_request = PatternAnalysisRequest {
            pattern_type: PatternType::Block,
            block: block.clone(),
            context: Some(transaction_context),
            validation_rules: Vec::new(),
            previous_results: None,
            correlation_data: None,
            security_level: 2,                  // Medium security level (u8)
            max_processing_time_ms: Some(5000), // 5 seconds max processing time
        };

        let pattern_result = self
            .pattern_analyzer
            .write()
            .await
            .analyze_pattern(pattern_request)
            .await
            .map_err(|e| format!("Pattern analysis failed for transaction: {}", e))?;

        if pattern_result.analysis.risk_level > 0.7 && pattern_result.analysis.confidence > 0.6 {
            // High risk with good confidence means we should reject
            debug!(
                "Block {} rejected by pattern analysis: {} (risk: {}, confidence: {})",
                block.header.index,
                pattern_result.score_adjustment.reasoning,
                pattern_result.analysis.risk_level,
                pattern_result.analysis.confidence
            );
            return Ok(false);
        }

        // All validation passed
        Ok(true)
    }

    /// Request specific blocks from peers
    pub async fn request_blocks_range(
        &self,
        start_height: u64,
        end_height: Option<u64>,
    ) -> Result<(), String> {
        let request = SyncRequest {
            from_height: start_height,
            to_height: end_height,
            max_blocks: Some(self.max_blocks_per_sync),
            requesting_node_id: "self".to_string(), // Replace with actual node ID
        };

        // Serialize the request
        let _request_json = serde_json::to_string(&request)
            .map_err(|e| format!("Failed to serialize sync request: {}", e))?;

        // Broadcast the request
        self.message_handler
            .read()
            .await
            .broadcast_message(
                NetworkMessage::GetBlocks, // This is a simplification, ideally we'd include the request_json
            )
            .await
            .map_err(|e| format!("Failed to broadcast message: {}", e))
    }

    /// Handle a new block announcement from the network
    pub async fn handle_new_block_announcement(&self, block: &Block) -> Result<(), String> {
        // Check if we already have this block
        let blocks = self
            .blockchain
            .read()
            .await
            .get_blocks()
            .await
            .map_err(|e| format!("Failed to get blocks: {}", e))?;
        let current_height = blocks.len() as u64;
        if block.header.index <= current_height {
            // We already have this block or it's too old
            return Ok(());
        }

        // If the block is exactly the next one we need
        if block.header.index == current_height + 1 {
            // Validate and add the block directly
            return self.handle_blocks(vec![block.clone()]).await;
        } else if block.header.index > current_height + 1 {
            // We're missing blocks, request a range
            info!(
                "Received block at height {}, but we're at {}. Requesting missing blocks.",
                block.header.index, current_height
            );
            return self
                .request_blocks_range(current_height + 1, Some(block.header.index))
                .await;
        }

        Ok(())
    }

    /// Process network messages related to synchronization
    pub async fn process_sync_message(&self, message: &NetworkMessage) -> Result<(), String> {
        match message {
            NetworkMessage::NewBlock(block) => self.handle_new_block_announcement(block).await,
            NetworkMessage::GetBlocks => {
                // Handle request for blocks from a peer
                // In a real implementation, we would extract information from the message
                // to determine which blocks to send

                // For now, just a placeholder response
                let current_height = self.get_blockchain_height().await?;
                if current_height == 0 {
                    return Ok(()); // Nothing to send
                }

                // Get the last 10 blocks or all blocks if fewer
                let start_height = if current_height > 10 {
                    current_height - 10
                } else {
                    0
                };
                let mut blocks = Vec::new();

                for i in start_height..=current_height {
                    match self.get_blockchain_block_by_height(i).await {
                        Ok(block) => blocks.push(block),
                        Err(e) => warn!("Failed to get block at height {}: {}", i, e),
                    }
                }

                // Send blocks back directly
                self.message_handler
                    .read()
                    .await
                    .broadcast_message(NetworkMessage::Blocks(blocks))
                    .await
                    .map_err(|e| format!("Failed to broadcast blocks: {}", e))
            }
            NetworkMessage::Blocks(blocks) => {
                // Process received blocks directly
                self.handle_blocks(blocks.clone()).await
            }
            _ => {
                // Ignore other message types
                Ok(())
            }
        }
    }

    pub async fn get_remote_height(&self, peer_id: &String) -> Result<u64, String> {
        let message = NetworkMessage::SyncHeightRequest(peer_id.clone());

        // Note: The MessageHandler doesn't have send_message_with_response or a public send_message method
        // so instead we'll use broadcast_message since it's the only available public method
        // In a real implementation, we'd need to set up a way to receive the response

        // First check if the peer exists
        let peer_exists = self
            .message_handler
            .read()
            .await
            .get_peers()
            .await
            .iter()
            .any(|(id, _addr)| id == peer_id);

        if !peer_exists {
            return Err(format!("Peer {} not found", peer_id));
        }

        // Since we can only broadcast, we'll have to rely on the peer to respond to our broadcast
        self.message_handler
            .read()
            .await
            .broadcast_message(message)
            .await
            .map_err(|e| format!("Failed to broadcast height request: {}", e))?;

        // This is a placeholder since we don't have a proper response handling mechanism
        // In a real implementation, we'd await for the response after sending the request
        let height = 0; // Default value for the prototype
        return Ok(height);

        // The rest of this function is temporarily commented out since we don't have proper response handling
        /*
        match MessageType::SyncHeightResponse {
            MessageType::SyncHeightResponse => {
                let height: u64 = serde_json::from_str(&response.data)
                    .map_err(|e| format!("Failed to parse height response: {}", e))?;
                Ok(height)
            },
            _ => Err(format!("Unexpected response type: {:?}", response.message_type))
        }
        */
    }

    /// Helper method to get blockchain height
    pub async fn get_blockchain_height(&self) -> Result<u64, String> {
        let blocks = self
            .blockchain
            .read()
            .await
            .get_blocks()
            .await
            .map_err(|e| format!("Failed to get blocks: {}", e))?;
        Ok(blocks.len() as u64)
    }

    /// Helper method to get blockchain block by height
    pub async fn get_blockchain_block_by_height(&self, height: u64) -> Result<Block, String> {
        let blocks = self
            .blockchain
            .read()
            .await
            .get_blocks()
            .await
            .map_err(|e| format!("Failed to get blocks: {}", e))?;

        if height >= blocks.len() as u64 {
            return Err(format!("Block at height {} not found", height));
        }

        Ok(blocks[height as usize].clone())
    }

    /// Helper method to request missing blocks
    // Helper method to request missing blocks
    pub async fn request_missing_blocks(&self) -> Result<(), String> {
        // Get local blockchain height
        let local_height = self.get_blockchain_height().await?;

        // Get heights from peers and identify the highest one
        let peer_data = self.message_handler.read().await.get_peers().await;
        let peers = peer_data
            .iter()
            .map(|(id, _addr)| id.clone())
            .collect::<Vec<String>>();
        if peers.is_empty() {
            return Ok(());
        }

        let mut max_height = local_height;
        let mut sync_peer: Option<String> = None;

        for peer_id in peers {
            match self.get_remote_height(&peer_id).await {
                Ok(height) => {
                    if height > max_height {
                        max_height = height;
                        sync_peer = Some(peer_id);
                    }
                }
                Err(e) => warn!("Failed to get height from peer {}: {}", peer_id, e),
            }
        }

        // If we found a peer with more blocks, request the blocks
        if let Some(_peer) = sync_peer {
            if max_height > local_height {
                // Request blocks range from local_height+1 to max_height
                self.request_blocks_range(local_height + 1, Some(max_height))
                    .await?;
            }
        }

        Ok(())
    }
}

#[cfg(test)]
mod tests {

    #[tokio::test]
    async fn test_sync_with_valid_blocks() {
        // This is a placeholder for a comprehensive test
        // In a real test we would:
        // 1. Create a blockchain with some blocks
        // 2. Create another blockchain that's behind
        // 3. Synchronize the second blockchain with the first
        // 4. Verify that both blockchains have the same blocks
    }

    #[tokio::test]
    async fn test_block_validation_during_sync() {
        // This is a placeholder for a test that verifies
        // that blocks are properly validated during synchronization
        // including PoAI consensus validation
    }
}
