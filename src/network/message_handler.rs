use crate::blockchain::{Block, Blockchain, Transaction};
use crate::consensus::vote::{Vote, VotingResult};
use crate::consensus::{
    peer_validator::{ValidationRequest, ValidationResponse},
    ConsensusMetrics, PeerValidator,
};
use crate::network::reputation::PeerReputation;
use crate::network::transport::NetworkTransport;
use crate::serialization::SerializationService;
use crate::security::validation::InputValidator;
use anyhow::Result;
use serde::{Deserialize, Serialize};
use serde_json;
use std::collections::HashMap;
use std::net::SocketAddr;
use std::sync::Arc;
use std::time::SystemTime;
use tokio::sync::{mpsc, RwLock};
use tokio_stream::wrappers::UnboundedReceiverStream;
use tracing::{error, info, warn};
// Import removed: tokio_stream::StreamExt not used

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct Peer {
    pub id: String,
    pub address: SocketAddr,
    pub last_seen: u64,
    pub status: PeerStatus,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub enum PeerStatus {
    Connected,
    Disconnected,
    Pending,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum NetworkMessage {
    NewBlock(Block),
    Transaction(Transaction),
    GetBlocks,
    Blocks(Vec<Block>),
    Ping,
    Pong,
    PeerUpdate(String, String), // (peer_id, address)
    VotingStart(Block),
    Vote(Vote),
    VotingResult(VotingResult),
    ValidationRequest(ValidationRequest),
    ValidationResponse(ValidationResponse),
    SyncHeightRequest(String), // peer_id
}

#[derive(Debug)]
pub struct MessageHandler {
    peers: Arc<RwLock<HashMap<String, SocketAddr>>>,
    peer_validator: Option<Arc<PeerValidator>>,
    validation_requests: Arc<RwLock<HashMap<String, Vec<ValidationRequest>>>>,
    validation_responses: Arc<RwLock<HashMap<String, Vec<ValidationResponse>>>>,
    transport: Option<Arc<NetworkTransport>>,
    reputation: Option<Arc<PeerReputation>>,
    consensus_metrics: Option<Arc<ConsensusMetrics>>, // Add this line
    #[allow(dead_code)]
    serialization: Arc<SerializationService>,
    message_sender: Option<mpsc::UnboundedSender<(NetworkMessage, SocketAddr)>>,
    message_receiver: Option<mpsc::UnboundedReceiver<(NetworkMessage, SocketAddr)>>,
    blockchain: Option<Arc<RwLock<Blockchain>>>,
    input_validator: Arc<RwLock<InputValidator>>,
}

impl MessageHandler {
    pub fn new() -> Self {
        let (tx, rx) = mpsc::unbounded_channel();
        Self {
            peers: Arc::new(RwLock::new(HashMap::new())),
            peer_validator: None,
            validation_requests: Arc::new(RwLock::new(HashMap::new())),
            validation_responses: Arc::new(RwLock::new(HashMap::new())),
            transport: None,
            reputation: None,
            consensus_metrics: None, // Add this line
            serialization: Arc::new(SerializationService::new()),
            blockchain: None,
            message_sender: Some(tx),
            message_receiver: Some(rx),
            input_validator: Arc::new(RwLock::new(InputValidator::new())),
        }
    }

    pub async fn add_peer(&self, peer_id: String, address: SocketAddr) {
        let mut peers = self.peers.write().await;
        peers.insert(peer_id, address);
    }

    pub fn set_blockchain(&mut self, blockchain: Arc<RwLock<Blockchain>>) {
        self.blockchain = Some(blockchain);
    }

    pub async fn get_peers(&self) -> Vec<(String, SocketAddr)> {
        self.peers
            .read()
            .await
            .iter()
            .map(|(id, addr)| (id.clone(), addr.clone()))
            .collect()
    }

    pub async fn has_peer(&self, peer_id: &str) -> bool {
        self.peers.read().await.contains_key(peer_id)
    }

    pub async fn serialize_message(&self, message: &NetworkMessage) -> Result<String> {
        serde_json::to_string(message).map_err(|e| e.into())
    }

    pub async fn deserialize_message(&self, message_str: &str) -> Result<NetworkMessage> {
        serde_json::from_str(message_str).map_err(|e| e.into())
    }

    pub async fn broadcast_message(&self, message: NetworkMessage) -> Result<()> {
        if let Some(transport) = &self.transport {
            transport.broadcast_message(message.clone()).await?;
        }

        match message {
            NetworkMessage::ValidationRequest(request) => {
                if let Some(peer_validator) = &self.peer_validator {
                    peer_validator.add_validator(&request.validator_id).await?;

                    let mut requests = self.validation_requests.write().await;
                    requests
                        .entry(request.block_hash.clone())
                        .or_default()
                        .push(request);
                }
            }
            NetworkMessage::ValidationResponse(response) => {
                let mut responses = self.validation_responses.write().await;
                responses
                    .entry(response.validator_id.clone())
                    .or_default()
                    .push(response.clone()); // Fix borrow of moved value by cloning response before pushing to vector so original can be used later
            }
            _ => {}
        }

        Ok(())
    }

    pub fn handle_message(&self, message: NetworkMessage, addr: SocketAddr) -> Result<()> {
        if let Some(sender) = &self.message_sender {
            sender
                .send((message, addr))
                .map_err(|e| anyhow::anyhow!("Failed to send message to handler: {}", e))
        } else {
            Err(anyhow::anyhow!("Message handler not initialized"))
        }
    }

    pub async fn start_message_processor(&mut self) -> Result<()> {
        if let Some(rx) = self.message_receiver.take() {
            let reputation_clone = self.reputation.clone();
            let validator_clone = self.peer_validator.clone();
            let blockchain_clone = self.blockchain.clone();
            let consensus_metrics_clone = self.consensus_metrics.clone(); // Add this line

            tokio::spawn(async move {
                use tokio_stream::StreamExt; // Make sure we're using the right StreamExt

                // Create the stream with proper type for futures::StreamExt
                let mut stream = UnboundedReceiverStream::new(rx);

                // Process messages with proper StreamExt usage
                while let Some((message, addr)) = stream.next().await {
                    // Create a temporary handler for processing
                    let handler = MessageHandler {
                        peers: Arc::new(RwLock::new(HashMap::new())),
                        peer_validator: validator_clone.clone(),
                        validation_requests: Arc::new(RwLock::new(HashMap::new())),
                        validation_responses: Arc::new(RwLock::new(HashMap::new())),
                        transport: None,
                        reputation: reputation_clone.clone(),
                        consensus_metrics: consensus_metrics_clone.clone(),
                        serialization: Arc::new(SerializationService::new()),
                        message_sender: None,
                        message_receiver: None,
                        blockchain: blockchain_clone.clone(),
                        input_validator: Arc::new(RwLock::new(InputValidator::new())),
                    };

                    if let Err(e) = handler.process_message(message, addr).await {
                        error!("Failed to process message: {}", e);
                    }
                }
            });
        }
        Ok(())
    }

    async fn process_message(&self, message: NetworkMessage, addr: SocketAddr) -> Result<()> {
        // Validate the message first
        {
            let mut validator = self.input_validator.write().await;
            if let Err(e) = validator.validate_network_message(&message) {
                warn!("Invalid message from {}: {}", addr, e);
                // Update peer reputation for sending invalid messages
                if let Some(reputation) = &self.reputation {
                    reputation.record_error(&addr.to_string()).await?;
                }
                return Err(e);
            }
        }
        
        // Check peer reputation
        if let Some(reputation) = &self.reputation {
            if !reputation.is_peer_reliable(&addr.to_string()).await? {
                warn!("Message from unreliable peer: {}", addr);
                return Ok(());
            }
        }

        // Measure message processing time
        let start_time = SystemTime::now();

        let result = match message {
            NetworkMessage::NewBlock(block) => {
                info!("Received new block from {}", addr);
                self.handle_new_block(block).await
            }
            NetworkMessage::Transaction(tx) => {
                info!("Received transaction from {}", addr);
                self.handle_transaction(tx).await
            }
            NetworkMessage::GetBlocks => {
                info!("Blocks requested from {}", addr);
                self.handle_block_request(addr).await
            }
            NetworkMessage::Blocks(blocks) => {
                info!("Received blocks from {}", addr);
                self.handle_blocks_response(blocks).await
            }
            NetworkMessage::Ping => {
                info!("Ping received from {}", addr);
                self.broadcast_message(NetworkMessage::Pong).await
            }
            NetworkMessage::Pong => {
                info!("Pong received from {}", addr);
                self.update_peer_stats(&addr).await
            }
            NetworkMessage::PeerUpdate(peer_id, address) => {
                info!("Peer update received from {}", addr);
                self.handle_peer_update(peer_id, address, addr).await
            }
            NetworkMessage::VotingStart(block) => {
                info!("Voting start received from {}", addr);
                self.handle_voting_start(block).await
            }
            NetworkMessage::Vote(vote) => {
                info!("Vote received from {}", addr);
                self.handle_vote(vote).await
            }
            NetworkMessage::VotingResult(result) => {
                info!("Voting result received from {}", addr);
                self.handle_voting_result(result).await
            }
            NetworkMessage::ValidationRequest(request) => {
                info!("Processing validation request: {}", request.validator_id);
                self.handle_validation_request(request).await
            }
            NetworkMessage::ValidationResponse(response) => {
                info!("Processing validation response: {}", response.validator_id);

                // Store the response in our local mapping
                {
                    let mut validation_responses = self.validation_responses.write().await;
                    let responses = validation_responses
                        .entry(response.validator_id.clone())
                        .or_insert_with(Vec::new);
                    responses.push(response.clone()); // Fix borrow of moved value by cloning response before pushing to vector so original can be used later
                }

                // If we have a peer validator, process the validation response
                if let Some(validator) = &self.peer_validator {
                    // Update validation status based on response
                    validator.process_validation_response(&response).await?;

                    return Ok(());
                }

                Err(anyhow::anyhow!("Peer validator not initialized"))
            }
            NetworkMessage::SyncHeightRequest(peer_id) => {
                info!("Sync height request received from {}", addr);
                self.handle_sync_height_request(peer_id, addr).await
            }
        };

        // Update peer stats
        if let Some(reputation) = &self.reputation {
            let duration = start_time.elapsed().unwrap().as_millis();
            reputation
                .update_latency(&addr.to_string(), duration as u64, false)
                .await?;

            if result.is_err() {
                reputation.record_error(&addr.to_string()).await?;
            } else {
                reputation.record_success(&addr.to_string()).await?;
            }
        }

        result
    }

    async fn handle_new_block(&self, block: Block) -> Result<()> {
        // Verify block
        if !block.verify() {
            return Err(anyhow::anyhow!("Invalid block received"));
        }

        // Check if block is already in chain
        if self.has_block(&block.hash) {
            return Ok(());
        }

        // Add block to chain
        self.add_block(&block).await?;

        // Broadcast block to other peers
        self.broadcast_message(NetworkMessage::NewBlock(block.clone()))
            .await?;

        // Update peer reputation for successful block validation
        if let Some(reputation) = &self.reputation {
            if let Some(validator_sig) = &block.meta.validator_signature {
                reputation.record_success(validator_sig).await?;
            }
        }

        Ok(())
    }

    async fn handle_transaction(&self, tx: Transaction) -> Result<()> {
        // Verify transaction
        if !tx.verify() {
            return Err(anyhow::anyhow!("Invalid transaction received"));
        }

        // Add to pending transactions
        self.add_pending_transaction(&tx).await?;

        // Broadcast transaction to other peers
        self.broadcast_message(NetworkMessage::Transaction(tx.clone()))
            .await?;

        Ok(())
    }

    async fn handle_block_request(&self, addr: SocketAddr) -> Result<()> {
        // Get last N blocks
        let blocks = self.get_last_blocks(10).await?;

        // Send blocks to requesting peer
        self.send_message(NetworkMessage::Blocks(blocks), addr)
            .await?;

        Ok(())
    }

    async fn handle_blocks_response(&self, blocks: Vec<Block>) -> Result<()> {
        // Process blocks in order
        for block in blocks {
            self.handle_new_block(block).await?;
        }

        Ok(())
    }

    async fn handle_peer_update(
        &self,
        peer_id: String,
        address: String,
        _addr: SocketAddr,
    ) -> Result<()> {
        let peer_addr = address.parse::<SocketAddr>()?;

        // Update peer list
        let mut peers = self.peers.write().await;
        peers.insert(peer_id.clone(), peer_addr);

        // Update reputation system
        if let Some(reputation) = &self.reputation {
            reputation.add_peer(&peer_id, peer_addr).await?;
        }

        // Broadcast peer update
        self.broadcast_message(NetworkMessage::PeerUpdate(peer_id, address))
            .await?;

        Ok(())
    }

    async fn handle_voting_start(&self, block: Block) -> Result<()> {
        // Start voting process
        self.start_voting_process(block).await?;

        Ok(())
    }

    async fn handle_vote(&self, vote: Vote) -> Result<()> {
        // Process vote
        self.process_vote(vote).await?;

        Ok(())
    }

    async fn handle_voting_result(&self, result: VotingResult) -> Result<()> {
        // Process voting result
        self.process_voting_result(result).await?;

        Ok(())
    }

    async fn handle_validation_request(&self, request: ValidationRequest) -> Result<()> {
        info!("Processing validation request: {}", request.validator_id);

        // Store the request in our local mapping for tracking
        {
            let mut validation_requests = self.validation_requests.write().await;
            let requests = validation_requests
                .entry(request.block_hash.clone())
                .or_insert_with(Vec::new);
            requests.push(request.clone());
        }

        // If we have a peer validator, process the request
        if let Some(validator) = &self.peer_validator {
            // Process the validation request
            let response = validator.validate(&request).await?;

            // Find the peer address
            let peer_addr = {
                let peers = self.peers.read().await;
                match peers.get(&request.validator_id) {
                    Some(addr) => *addr,
                    None => {
                        return Err(anyhow::anyhow!("Peer not found: {}", request.validator_id))
                    }
                }
            };

            // Send the validation response to the peer
            self.send_message(NetworkMessage::ValidationResponse(response), peer_addr)
                .await?;

            return Ok(());
        }

        Err(anyhow::anyhow!("Peer validator not initialized"))
    }

    #[allow(dead_code)]
    async fn handle_validation_response(&self, response: ValidationResponse) -> Result<()> {
        info!("Processing validation response: {}", response.validator_id);

        // Store the response in our local mapping
        {
            let mut validation_responses = self.validation_responses.write().await;
            let responses = validation_responses
                .entry(response.validator_id.clone())
                .or_insert_with(Vec::new);
            responses.push(response.clone()); // Fix borrow of moved value by cloning response before pushing to vector so original can be used later
        }

        // If we have a peer validator, process the validation response
        if let Some(validator) = &self.peer_validator {
            // Update validation status based on response
            validator.process_validation_response(&response).await?;

            return Ok(());
        }

        Err(anyhow::anyhow!("Peer validator not initialized"))
    }

    async fn handle_sync_height_request(&self, _peer_id: String, _addr: SocketAddr) -> Result<()> {
        // Handle sync height request
        // This is a placeholder for the actual implementation
        Ok(())
    }

    // Consensus and Validation Process methods
    async fn start_voting_process(&self, block: Block) -> Result<()> {
        info!("Starting voting process for block: {}", block.hash);

        // Verify the block is valid before starting voting
        if !block.verify() {
            return Err(anyhow::anyhow!("Cannot start voting for invalid block"));
        }

        // If we have a peer validator, use it to initiate voting
        if let Some(validator) = &self.peer_validator {
            validator.initiate_voting(block.clone()).await?;

            // Broadcast voting start to all peers
            self.broadcast_message(NetworkMessage::VotingStart(block))
                .await?;

            return Ok(());
        }

        Err(anyhow::anyhow!("Peer validator not initialized"))
    }

    async fn process_vote(&self, vote: Vote) -> Result<()> {
        info!("Processing vote for block: {}", vote.block_hash);

        // If we have a peer validator, process the vote
        if let Some(validator) = &self.peer_validator {
            // Add the vote to the validator
            validator.add_vote(vote.clone()).await?;

            // Broadcast vote to other peers
            self.broadcast_message(NetworkMessage::Vote(vote)).await?;

            return Ok(());
        }

        Err(anyhow::anyhow!("Peer validator not initialized"))
    }

    async fn process_voting_result(&self, result: VotingResult) -> Result<()> {
        info!("Processing voting result for block: {}", result.block_hash);

        // If we have a peer validator and blockchain, process the result
        if let Some(validator) = &self.peer_validator {
            validator.process_voting_result(result.clone()).await?;

            // If voting was successful, add block to blockchain
            if result.approved {
                if let Some(blockchain) = &self.blockchain {
                    // Fetch the block from validator (assuming it has the block cached)
                    if let Some(block) = validator.get_block(&result.block_hash).await? {
                        // Add block to blockchain
                        match blockchain.write().await.add_block(&block).await {
                            Ok(_) => {}
                            Err(e) => return Err(anyhow::anyhow!("{}", e)),
                        };
                    } else {
                        warn!(
                            "Voted to accept block but block not found in validator: {}",
                            result.block_hash
                        );
                    }
                }
            }

            // Broadcast voting result to other peers
            self.broadcast_message(NetworkMessage::VotingResult(result))
                .await?;

            return Ok(());
        }

        Err(anyhow::anyhow!("Peer validator not initialized"))
    }

    #[allow(dead_code)]
    async fn process_validation_request(&self, request: ValidationRequest) -> Result<()> {
        info!("Processing validation request: {}", request.validator_id);

        // Store the request in our local mapping for tracking
        {
            let mut validation_requests = self.validation_requests.write().await;
            let requests = validation_requests
                .entry(request.block_hash.clone())
                .or_insert_with(Vec::new);
            requests.push(request.clone());
        }

        // If we have a peer validator, process the request
        if let Some(validator) = &self.peer_validator {
            // Process the validation request
            let response = validator.validate(&request).await?;

            // Find the peer address
            let peer_addr = {
                let peers = self.peers.read().await;
                match peers.get(&request.validator_id) {
                    Some(addr) => *addr,
                    None => {
                        return Err(anyhow::anyhow!("Peer not found: {}", request.validator_id))
                    }
                }
            };

            // Send the validation response to the peer
            self.send_message(NetworkMessage::ValidationResponse(response), peer_addr)
                .await?;

            return Ok(());
        }

        Err(anyhow::anyhow!("Peer validator not initialized"))
    }

    #[allow(dead_code)]
    async fn process_validation_response(&self, response: ValidationResponse) -> Result<()> {
        info!("Processing validation response: {}", response.validator_id);

        // Store the response in our local mapping
        {
            let mut validation_responses = self.validation_responses.write().await;
            let responses = validation_responses
                .entry(response.validator_id.clone())
                .or_insert_with(Vec::new);
            responses.push(response.clone()); // Fix borrow of moved value by cloning response before pushing to vector so original can be used later
        }

        // If we have a peer validator, process the validation response
        if let Some(validator) = &self.peer_validator {
            // Update validation status based on response
            validator.process_validation_response(&response).await?;

            return Ok(());
        }

        Err(anyhow::anyhow!("Peer validator not initialized"))
    }

    // Helper methods
    async fn update_peer_stats(&self, peer_addr: &SocketAddr) -> Result<()> {
        // Update peer reputation and statistics
        if let Some(reputation) = &self.reputation {
            reputation.record_activity(*peer_addr).await?;
        }
        Ok(())
    }

    fn has_block(&self, hash: &str) -> bool {
        match &self.blockchain {
            Some(blockchain) => {
                // Use a blocking call here since this is a synchronous function
                // In a real implementation, this would be better as an async function
                let blockchain_guard = futures::executor::block_on(blockchain.read());
                let chain = futures::executor::block_on((*blockchain_guard).get_chain());
                chain.iter().any(|b| b.hash == hash)
            }
            None => {
                warn!("Blockchain not initialized");
                false
            }
        }
    }

    async fn add_block(&self, block: &Block) -> Result<()> {
        // Add block to blockchain
        match &self.blockchain {
            Some(blockchain) => {
                match blockchain.write().await.add_block(block).await {
                    Ok(_) => {}
                    Err(e) => return Err(anyhow::anyhow!("{}", e)),
                };
                Ok(())
            }
            None => Err(anyhow::anyhow!("Blockchain not initialized")),
        }
    }

    async fn add_pending_transaction(&self, tx: &Transaction) -> Result<()> {
        // Add transaction to pending list
        match &self.blockchain {
            Some(blockchain) => {
                match blockchain.write().await.add_transaction(tx).await {
                    Ok(_) => {}
                    Err(e) => return Err(anyhow::anyhow!("{}", e)),
                };
                Ok(())
            }
            None => Err(anyhow::anyhow!("Blockchain not initialized")),
        }
    }

    async fn get_last_blocks(&self, count: usize) -> Result<Vec<Block>> {
        // Get last N blocks from blockchain
        match &self.blockchain {
            Some(blockchain) => Ok(blockchain
                .read()
                .await
                .get_last_blocks(count)
                .await
                .map_err(|e| anyhow::anyhow!(e))?),
            None => Err(anyhow::anyhow!("Blockchain not initialized")),
        }
    }

    async fn send_message(&self, message: NetworkMessage, _addr: SocketAddr) -> Result<()> {
        // For now, use broadcast_message since we don't have peer-specific sending implemented
        // TODO: Implement peer-specific message sending with proper connection management
        if let Some(transport) = &self.transport {
            transport.broadcast_message(message).await?
        }
        Ok(())
    }

    pub fn get_validation_responses(
        &self,
    ) -> &Arc<RwLock<HashMap<String, Vec<ValidationResponse>>>> {
        &self.validation_responses
    }
}
