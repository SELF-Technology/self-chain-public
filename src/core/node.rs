use std::sync::Arc;
// SocketAddr is not used directly in this file
// use std::net::SocketAddr;
use crate::blockchain::{Blockchain, Transaction};
use crate::core::message_handler::MessageHandler;
use anyhow::{anyhow, Result};
use log::error;
use tokio::sync::RwLock; // Add missing RwLock import
use tracing::info; // only using info logging currently
                   // Storage trait import not directly used
                   // use crate::core::Storage;
                   // Use the ValidationResult enum from ai/validation for voting
use crate::ai::validation::ValidationResult;

use crate::ai::{
    AICapacityManager, AIValidator, ContextManager, ValidatorReputation, VotingSystem,
};
use crate::core::network_node::NetworkNode;
use crate::network::p2p::{LegacyNetworkAdapter, NetworkAdapter}; // Import NetworkAdapter trait

// Timestamp functions not currently used
// use chrono::Utc;
use crate::config::node_config::NodeConfig;
use crate::core::storage::{HybridStorage, StorageAdapter, StorageStats};

pub struct Node {
    config: NodeConfig,
    blockchain: Arc<RwLock<Blockchain>>,
    network: Arc<dyn NetworkAdapter>,
    _legacy_network: Arc<RwLock<NetworkNode>>, // Keep for backward compatibility during transition
    message_handler: Arc<RwLock<MessageHandler>>,
    validator: Arc<RwLock<AIValidator>>,
    voting_system: Arc<RwLock<VotingSystem>>,
    storage: Arc<RwLock<HybridStorage>>,

    ai_context: Arc<RwLock<ContextManager>>,
    validator_reputation: Arc<RwLock<ValidatorReputation>>,
    capacity_manager: Arc<RwLock<AICapacityManager>>,
}

impl Node {
    pub async fn new(config: NodeConfig) -> Result<Self> {
        info!("Initializing PoAI node with ID: {}", config.id);

        let blockchain = Arc::new(RwLock::new(Blockchain::new(config.ai_validation_threshold)));

        // Create legacy network first
        let legacy_network = Arc::new(RwLock::new(NetworkNode::new(
            config.network.listen_addr.parse()?,
        )?));

        // Create P2P network adapter using legacy adapter
        let p2p_config = crate::network::p2p::NetworkConfig {
            listen_address: config.network.listen_addr.clone(),
            bootstrap_peers: vec![],
            max_connections: 50,
            connection_timeout_ms: 30000,  // 30 seconds in milliseconds
            max_message_size: 1024 * 1024, // 1MB max message size
            tls_config: None,              // Will be set up separately
            adapter_type: Some("legacy".to_string()),
        };
        let network: Arc<dyn NetworkAdapter> = Arc::new(LegacyNetworkAdapter::new(p2p_config)?);

        let message_handler = Arc::new(RwLock::new(MessageHandler::new(legacy_network.clone())));
        let validator = Arc::new(RwLock::new(AIValidator::new()));
        let voting_system = Arc::new(RwLock::new(VotingSystem::new(0.5)));
        
        // Initialize storage with proper configuration
        let node_id = format!("node-{}", config.id);
        let max_size = config.storage.max_size.unwrap_or(1000000000);
        let storage = Arc::new(RwLock::new(
            HybridStorage::new(max_size).with_node_id(node_id)
        ));
        let ai_context = Arc::new(RwLock::new(ContextManager::new(1000)));
        let validator_reputation = Arc::new(RwLock::new(ValidatorReputation::new(0.0, 1.0, 0.99)));
        let capacity_manager = Arc::new(RwLock::new(AICapacityManager::new(
            1000, // total capacity
            800,  // max_reservation
            200,  // min_available
        )));

        Ok(Self {
            config,
            blockchain,
            network,
            _legacy_network: legacy_network,
            message_handler,
            validator,
            voting_system,
            storage,
            ai_context,
            validator_reputation,
            capacity_manager,
        })
    }

    /// Run the node - initializes all components and starts services
    /// This is the main entry point for node operation
    pub async fn run(&self) -> Result<()> {
        info!("Starting SELF Chain node {}...", self.config.id);

        // Log the node configuration
        info!(
            "Node network configuration: {}",
            self.config.network.listen_addr
        );
        info!("Node AI validation threshold: {}", self.config.ai_validation_threshold);

        // Initialize components in proper sequence with error handling
        if let Err(e) = self.initialize_storage().await {
            info!("Failed to initialize storage: {}", e);
            return Err(e);
        }

        if let Err(e) = self.initialize_ai_components().await {
            info!("Failed to initialize AI components: {}", e);
            return Err(e);
        }

        if let Err(e) = self.initialize_network().await {
            info!("Failed to initialize network: {}", e);
            return Err(e);
        }

        // Start consensus and main processing tasks
        let consensus_handle = self.start_consensus();
        let network_handle = self.start_network_services();

        info!("SELF Chain node {} started successfully", self.config.id);

        // Set up a more robust shutdown signal handler
        let shutdown_signal = async {
            // Wait for SIGINT (Ctrl+C)
            match tokio::signal::ctrl_c().await {
                Ok(()) => {
                    info!("Ctrl+C received, initiating graceful shutdown");
                }
                Err(err) => {
                    info!(
                        "Error setting up signal handler: {}, initiating shutdown anyway",
                        err
                    );
                }
            }
        };

        // Keep the node running until shutdown signal
        info!("Node is running. Press Ctrl+C to shut down");

        // Wait for shutdown signal
        shutdown_signal.await;

        // Perform graceful shutdown with error handling
        if let Err(e) = self.shutdown().await {
            info!("Error during shutdown: {}", e);
        }

        // Wait for tasks to complete with timeout
        if let Some(handle) = consensus_handle {
            match tokio::time::timeout(tokio::time::Duration::from_secs(10), handle).await {
                Ok(result) => {
                    if let Err(e) = result {
                        info!("Consensus task error during shutdown: {}", e);
                    }
                }
                Err(_) => {
                    info!("Consensus task did not complete within timeout period");
                }
            }
        }

        if let Some(handle) = network_handle {
            match tokio::time::timeout(tokio::time::Duration::from_secs(10), handle).await {
                Ok(result) => {
                    if let Err(e) = result {
                        info!("Network task error during shutdown: {}", e);
                    }
                }
                Err(_) => {
                    info!("Network task did not complete within timeout period");
                }
            }
        }

        info!("Node {} shutdown complete", self.config.id);
        Ok(())
    }

    /// Initialize storage systems
    pub async fn initialize_storage(&self) -> Result<()> {
        info!("Initializing storage system with node ID: {}", self.config.id);
        
        // Set up storage with the node's identity
        let mut storage = self.storage.write().await;
        
        // Configure storage with node identity before initialization
        let node_id = format!("node-{}", self.config.id);
        *storage = HybridStorage::new(self.config.storage.max_size.unwrap_or(1000000000))
            .with_node_id(node_id);
            
        // Initialize storage adapter (which will set up OrbitDB/IPFS)
        storage.initialize().await?;
        
        // Get stats after initialization
        let stats = storage.get_stats().await;
        info!("Storage system ready. Blocks: {}, Transactions: {}, IPFS objects: {}, OrbitDB docs: {}",
            stats.blocks, stats.transactions, stats.ipfs_objects, stats.orbit_db_documents);
            
        Ok(())
    }

    /// Initialize AI components for validation
    async fn initialize_ai_components(&self) -> Result<()> {
        info!("Initializing AI validation components...");

        // Initialize AI validator
        let mut validator = self.validator.write().await;
        validator.initialize().await?;

        // Initialize context manager
        let mut context = self.ai_context.write().await;
        context.initialize().await?;

        // Initialize reputation system
        let mut reputation = self.validator_reputation.write().await;
        reputation.initialize().await?;

        // Initialize capacity manager
        let mut capacity = self.capacity_manager.write().await;
        capacity.initialize().await?;

        info!("AI validation components initialized");
        Ok(())
    }

    /// Initialize network communication
    async fn initialize_network(&self) -> Result<()> {
        info!("Initializing network communication...");

        // Initialize the network adapter
        self.network
            .initialize()
            .await
            .map_err(|e| anyhow!("Failed to initialize network adapter: {}", e))?;

        // Start the network adapter
        self.network
            .start()
            .await
            .map_err(|e| anyhow!("Failed to start network adapter: {}", e))?;

        // Only connect to peers if we have any configured
        if !self.config.network.peers.is_empty() {
            for peer_addr in &self.config.network.peers {
                // In a real implementation, we would resolve peer ID from the address
                // For now, use the address string as the peer ID
                let peer_id = peer_addr.to_string();
                let addr_str = peer_addr.to_string();

                // Connect to the peer
                if let Err(e) = self.network.connect_to_peer(&peer_id, &addr_str).await {
                    error!("Failed to connect to peer {}: {}", addr_str, e);
                }
            }

            info!(
                "Connected to {} peer nodes",
                self.config.network.peers.len()
            );
        }

        info!(
            "Network communication ready on {}",
            self.config.network.listen_addr
        );
        Ok(())
    }

    /// Start the consensus mechanism (PoAI)
    fn start_consensus(&self) -> Option<tokio::task::JoinHandle<Result<()>>> {
        info!("Starting PoAI consensus mechanism...");

        // Clone the required Arc components
        let blockchain = self.blockchain.clone();
        let validator = self.validator.clone();
        let voting = self.voting_system.clone();
        let reputation = self.validator_reputation.clone();
        let capacity = self.capacity_manager.clone();
        let node_id = self.config.id.clone();

        // Spawn the consensus task
        let handle = tokio::spawn(async move {
            info!("PoAI consensus started for node {}", node_id);

            loop {
                // Check for shutdown signal
                if tokio::signal::ctrl_c().await.is_ok() {
                    info!("Consensus task received shutdown signal");
                    break;
                }

                // Run one consensus iteration
                let validator_lock = validator.read().await;
                let blockchain_lock = blockchain.read().await;
                let mut voting_lock = voting.write().await;
                let reputation_lock = reputation.read().await;
                let capacity_lock = capacity.read().await;

                // Perform one round of PoAI validation
                info!("Running PoAI consensus round");

                // Step 1: Get pending transactions from the network
                // Note: Using get_pending_transactions instead of collect_pending_transactions
                let pending_transactions = {
                    // In a real implementation, we would get transactions from the mempool
                    // For now, just create some test transactions
                    let mut test_transactions = Vec::new();

                    // Only occasionally create test transactions (1 in 5 chance) to avoid spam
                    if rand::random::<u8>() % 5 == 0 {
                        test_transactions.push(Transaction {
                            id: format!("tx-{}", rand::random::<u64>()),
                            sender: format!("sender-{}", rand::random::<u16>()),
                            receiver: format!("receiver-{}", rand::random::<u16>()),
                            amount: rand::random::<u64>() % 1000,
                            timestamp: std::time::SystemTime::now()
                                .duration_since(std::time::UNIX_EPOCH)
                                .unwrap_or_default()
                                .as_secs(),
                            signature: "test-signature".to_string(),
                        });
                    }

                    test_transactions
                };

                // Step 2: AI validation of transactions
                let validated_transactions = {
                    if !pending_transactions.is_empty() {
                        info!(
                            "Validating {} transactions with AI",
                            pending_transactions.len()
                        );

                        // For simplicity, validate each transaction individually
                        let mut valid_transactions = Vec::new();

                        for tx in pending_transactions {
                            // Convert Transaction to JSON for validator
                            match serde_json::to_string(&tx) {
                                Ok(tx_json) => {
                                    // Add context parameter required by validate_transaction
                                    let context = "transaction validation context";
                                    match validator_lock
                                        .validate_transaction(&tx_json, context)
                                        .await
                                    {
                                        Ok(is_valid) => {
                                            if is_valid {
                                                valid_transactions.push(tx);
                                            }
                                        }
                                        Err(e) => {
                                            info!("Error validating transaction: {}", e);
                                        }
                                    }
                                }
                                Err(e) => {
                                    info!("Error serializing transaction: {}", e);
                                }
                            }
                        }

                        valid_transactions
                    } else {
                        Vec::new()
                    }
                };

                // Step 3: Create a new block if we have valid transactions
                if !validated_transactions.is_empty() {
                    info!(
                        "Creating new block with {} validated transactions",
                        validated_transactions.len()
                    );

                    // Use reputation score - using default value as the method is missing
                    // In a real implementation, we would use reputation_lock.get_reputation_score(&node_id)
                    let _validator_weight = 1.0; // Default weight

                    // Check AI capacity before block creation
                    // Using get_capacity instead of has_capacity
                    // Need to await the async result and handle the Result type
                    if let Ok(capacity) = capacity_lock.get_capacity().await {
                        // Calculate available capacity instead of direct comparison
                        let available = capacity.total.saturating_sub(capacity.used);
                        if available > 0 {
                            let blockchain_write = blockchain.write().await;

                            // Create an empty block to pass to create_block
                            let empty_block = crate::blockchain::Block {
                                header: crate::blockchain::BlockHeader {
                                    index: 0,                      // will be updated in create_block
                                    timestamp: 0,                  // will be updated in create_block
                                    previous_hash: "".to_string(), // will be updated in create_block
                                    nonce: 0,
                                    ai_threshold: 0, // will be updated in create_block
                                },
                                transactions: vec![], // will be populated in create_block
                                meta: crate::blockchain::BlockMeta {
                                    size: 0, // will be calculated in create_block
                                    tx_count: 0,
                                    height: 0,
                                    validator_signature: None,
                                    validator_id: None,
                                },
                                hash: "".to_string(), // will be calculated in create_block
                            };

                            // Use create_block with empty block (it will set the necessary fields)
                            match blockchain_write.create_block(empty_block).await {
                                Ok(new_block) => {
                                    info!("Block {} created successfully", new_block.hash);

                                    // Cast a vote for the new block
                                    // Using add_vote with correct parameter types: validator_id (node_id) and ValidationResult enum
                                    // Use ValidationResult::Valid from the validation module
                                    match voting_lock
                                        .add_vote(node_id.clone(), ValidationResult::Valid)
                                        .await
                                    {
                                        Ok(()) => info!("Vote cast for block {}", new_block.hash),
                                        Err(e) => info!("Failed to cast vote: {}", e),
                                    }

                                    // Update reputation based on successful block creation
                                    // Handle the string error type properly
                                    match reputation_lock.update_score(&node_id, true).await {
                                        Ok(_) => {}
                                        Err(e) => info!("Failed to update reputation: {}", e),
                                    }
                                }
                                Err(e) => {
                                    info!("Failed to create block: {}", e);
                                    // Reduce reputation for failed block creation
                                    // Handle the string error type properly
                                    match reputation_lock.update_score(&node_id, false).await {
                                        Ok(_) => {}
                                        Err(e) => info!("Failed to update reputation: {}", e),
                                    }
                                }
                            }
                        } else {
                            info!("AI capacity limit reached, skipping block creation");
                        }
                    } else {
                        info!("Failed to get AI capacity, skipping block creation");
                    }
                } else {
                    info!("No valid transactions to create a block");
                }

                // Release locks
                drop(validator_lock);
                drop(blockchain_lock);
                drop(voting_lock);
                drop(reputation_lock);
                drop(capacity_lock);

                // Sleep to prevent CPU spinning
                tokio::time::sleep(tokio::time::Duration::from_secs(5)).await;
            }

            info!("PoAI consensus stopped");
            Ok(())
        });

        Some(handle)
    }

    /// Start network services for message handling
    fn start_network_services(&self) -> Option<tokio::task::JoinHandle<Result<()>>> {
        info!("Starting network services...");

        // Clone the required Arc components
        let network = self.network.clone();
        let message_handler = self.message_handler.clone();
        let node_id = self.config.id.clone();

        // Spawn the network service task
        let handle = tokio::spawn(async move {
            info!("Network services started for node {}", node_id);

            loop {
                // Check for shutdown signal
                if tokio::signal::ctrl_c().await.is_ok() {
                    info!("Network service received shutdown signal");
                    break;
                }

                // Handle incoming messages
                let network_lock = network.clone();
                let handler_lock = message_handler.clone();

                // Process any pending messages
                // Use get_pending_messages instead of receive_pending_messages
                let messages = match network_lock.get_pending_messages().await {
                    Ok(msgs) => msgs,
                    Err(e) => {
                        info!("Error getting pending messages: {}", e);
                        Vec::new()
                    }
                };

                if !messages.is_empty() {
                    info!("Processing {} incoming network messages", messages.len());

                    for (peer_id, message_payload) in messages {
                        // Convert types: peer_id (String) to SocketAddr, message_payload to NetworkMessage
                        let sender_addr = match peer_id.parse::<std::net::SocketAddr>() {
                            Ok(addr) => addr,
                            Err(_) => {
                                // Use default addr if parsing fails
                                "0.0.0.0:0".parse().unwrap()
                            }
                        };

                        // Convert MessagePayload to NetworkMessage based on message type
                        let network_message = match message_payload.message_type {
                            crate::network::p2p::MessageType::Block => {
                                // Deserialize block data from JSON
                                match serde_json::from_slice::<crate::blockchain::Block>(
                                    &message_payload.data,
                                ) {
                                    Ok(block) => {
                                        crate::core::message_handler::NetworkMessage::NewBlock(
                                            block,
                                        )
                                    }
                                    Err(e) => {
                                        info!("Failed to deserialize block: {}", e);
                                        continue; // Skip invalid messages
                                    }
                                }
                            }
                            crate::network::p2p::MessageType::Transaction => {
                                // Deserialize transaction data from JSON
                                match serde_json::from_slice::<crate::blockchain::Transaction>(
                                    &message_payload.data,
                                ) {
                                    Ok(transaction) => {
                                        crate::core::message_handler::NetworkMessage::Transaction(
                                            transaction,
                                        )
                                    }
                                    Err(e) => {
                                        info!("Failed to deserialize transaction: {}", e);
                                        continue; // Skip invalid messages
                                    }
                                }
                            }
                            crate::network::p2p::MessageType::Heartbeat => {
                                crate::core::message_handler::NetworkMessage::Ping
                            }
                            crate::network::p2p::MessageType::Discovery => {
                                crate::core::message_handler::NetworkMessage::PeerDiscoveryRequest
                            }
                            _ => {
                                continue; // Skip unsupported message types
                            }
                        };

                        match handler_lock
                            .read()
                            .await
                            .handle_message(network_message, sender_addr)
                            .await
                        {
                            Ok(()) => {
                                // Successfully handled message, no response needed
                            }
                            Err(e) => {
                                info!("Error processing message: {}", e);
                            }
                        }
                    }
                }

                // Also process outgoing messages in the queue
                if let Err(e) = handler_lock.write().await.process_pending_messages().await {
                    info!("Error processing outgoing messages: {}", e);
                }

                // Check peer health using our newly added method
                if let Err(e) = network_lock.check_peer_health().await {
                    info!("Error checking peer health: {}", e);
                }

                // Release locks
                drop(handler_lock);

                // Short sleep to prevent CPU spinning
                tokio::time::sleep(tokio::time::Duration::from_millis(100)).await;
            }

            info!("Network services stopped");
            Ok(())
        });

        Some(handle)
    }

    /// Test connection to a specific peer and verify network connectivity
    pub async fn test_connection(&self, peer_addr: &str) -> Result<bool> {
        info!("Testing connection to peer: {}", peer_addr);

        // Get network component
        let network = self.network.clone();

        // Attempt to ping the target peer
        match network.ping_peer(peer_addr).await {
            Ok(success) => {
                if success {
                    info!("Successfully pinged peer: {}", peer_addr);
                } else {
                    info!("Failed to ping peer: {}", peer_addr);
                }
                Ok(success)
            }
            Err(e) => {
                info!("Error pinging peer {}: {}", peer_addr, e);
                Err(anyhow::anyhow!("Failed to ping peer: {}", e))
            }
        }
    }

    /// Gracefully shutdown all node components
    pub async fn shutdown(&self) -> Result<()> {
        info!("Shutting down node {}", self.config.id);
        
        match self.network.shutdown().await {
            Ok(_) => info!("Network shutdown complete"),
            Err(e) => error!("Error shutting down network: {}", e),
        }

        let mut capacity_manager = self.capacity_manager.write().await;
        capacity_manager.shutdown().await?;
        
        // Shutdown storage system
        let mut storage = self.storage.write().await;
        match storage.shutdown().await {
            Ok(_) => info!("Storage system shutdown complete"),
            Err(e) => error!("Error shutting down storage: {}", e),
        }
        
        info!("Node shutdown complete");
        Ok(())
    }
    
    /// Store a document in the node's cloud storage
    pub async fn store_document(&self, collection: &str, document: &serde_json::Value) -> Result<String> {
        let storage = self.storage.read().await;
        storage.store_document(collection, document).await
    }
    
    /// Retrieve a document from the node's cloud storage by ID
    pub async fn get_document(&self, collection: &str, id: &str) -> Result<Option<serde_json::Value>> {
        let storage = self.storage.read().await;
        storage.get_document(collection, id).await
    }
    
    /// Query documents in the node's cloud storage based on criteria
    pub async fn query_documents(&self, collection: &str, query: &serde_json::Value) -> Result<Vec<serde_json::Value>> {
        let storage = self.storage.read().await;
        storage.query_documents(collection, query).await
    }
    
    /// Store binary data in IPFS and return the CID
    pub async fn store_to_ipfs(&self, data: &[u8]) -> Result<String> {
        let storage = self.storage.read().await;
        storage.store_to_ipfs(data).await
    }
    
    /// Retrieve binary data from IPFS by CID
    pub async fn retrieve_from_ipfs(&self, cid: &str) -> Result<Vec<u8>> {
        let storage = self.storage.read().await;
        storage.retrieve_from_ipfs(cid).await
    }
    
    /// Get storage statistics
    pub async fn get_storage_stats(&self) -> StorageStats {
        let storage = self.storage.read().await;
        storage.get_stats().await
    }

    /// Shutdown AI components
    async fn shutdown_ai_components(&self) -> Result<()> {
        info!("Shutting down AI components...");
        let mut validator = self.validator.write().await;
        validator.shutdown().await?;

        let mut context = self.ai_context.write().await;
        context.shutdown().await?;

        let mut voting_system = self.voting_system.write().await;
        voting_system.shutdown().await?;

        let mut reputation = self.validator_reputation.write().await;
        reputation.shutdown().await?;

        let mut capacity = self.capacity_manager.write().await;
        capacity.shutdown().await?;

        info!("AI components shutdown complete");

        // Shutdown storage
        info!("Shutting down storage system...");
        let mut storage = self.storage.write().await;
        storage.shutdown().await?;
        info!("Storage system shutdown complete");

        info!("Node shutdown sequence completed");
        Ok(())
    }
}
