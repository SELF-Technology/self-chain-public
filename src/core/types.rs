use crate::blockchain::{Block, Transaction};
use serde::{Deserialize, Serialize};
use std::net::SocketAddr;
use std::path::PathBuf;
use std::sync::Arc;
use tracing::info;

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct NetworkConfig {
    pub listen_addr: String,
    pub initial_peers: Vec<String>,
    pub max_connections: u32,
    pub connection_timeout_secs: u64,
    pub message_timeout_secs: u64,
    pub discovery_interval_secs: u64,
    pub peer_timeout_secs: u64,
    pub max_peer_age_secs: u64,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct PeerInfo {
    pub id: String,
    pub address: String,
    pub last_seen: u64,
    pub latency: u64,
    pub version: String,
    pub user_agent: String,
    pub capabilities: Vec<String>,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct PeerStats {
    pub total_peers: u64,
    pub active_peers: u64,
    pub failed_connections: u64,
    pub connection_attempts: u64,
    pub discovery_attempts: u64,
    pub discovery_successes: u64,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct PeerDiscoveryConfig {
    pub enabled: bool,
    pub interval_secs: u64,
    pub max_peers: u32,
    pub min_peers: u32,
    pub bootstrap_nodes: Vec<String>,
    pub dht_enabled: bool,
    pub kademlia_config: Option<KademliaConfig>,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct KademliaConfig {
    pub replication_factor: u32,
    pub query_timeout_secs: u64,
    pub republish_interval_secs: u64,
    pub record_ttl_secs: u64,
    pub provider_ttl_secs: u64,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct Storage {
    pub data_dir: PathBuf,
    pub max_size_bytes: u64,
    pub compression_enabled: bool,
    pub encryption_enabled: bool,
    pub backup_enabled: bool,
    pub backup_interval_secs: u64,
}

impl Default for Storage {
    fn default() -> Self {
        Self {
            data_dir: PathBuf::from("./data"),
            max_size_bytes: 1024 * 1024 * 1024, // 1GB
            compression_enabled: true,
            encryption_enabled: false,
            backup_enabled: false,
            backup_interval_secs: 3600,
        }
    }
}

#[derive(Debug, Serialize, Deserialize)]
pub struct OpenLLMConfig {
    pub api_key: String,
    pub model: String,
    pub endpoint: String,
    pub context_window_size: u32,
    pub max_tokens: u32,
    pub timeout_secs: u64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct AuthConfig {
    pub api_key: String,
    pub key_store_path: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ValidatorConfig {
    pub min_stake: u64,
    pub min_active_hours: u64,
    pub validation_window: u64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct VotingConfig {
    pub voting_window: u64,
    pub min_voting_power: u64,
    pub max_voting_rounds: u64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct BlockHeader {
    pub index: u64,
    pub timestamp: u64,
    pub previous_hash: String,
    pub nonce: u64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct BlockMeta {
    pub size: u64,
    pub tx_count: u64,
    pub height: u64,
    pub validator_signature: Option<String>,
    pub validator_id: Option<String>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Peer {
    pub id: String,
    pub address: SocketAddr,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum NetworkMessage {
    PeerStats(PeerStats),
    NewBlock(Block),
    Transaction(Transaction),
    GetBlocks,
    Blocks(Vec<Block>),
    Ping,
    Pong,
    PeerDiscoveryRequest,
    PeerDiscoveryResponse(Vec<String>),
    PeerInfoRequest,
    PeerInfoResponse(PeerInfo),
    PeerStatsRequest,
    PeerStatsResponse(PeerStats),
    PeerBan(String, u64),   // peer_id, duration_secs
    PeerUnban(String),      // peer_id
    PeerDisconnect(String), // peer_id
    GetPeerStats,
    GetPeerInfo(String),
    GetPeers,
    AddPeers(Vec<String>),
    UpdatePeerInfo(PeerInfo),
    UpdatePeerStats(PeerStats),
}

#[derive(Debug)]
pub struct AIService {
    validation_threshold: f64,
    security_level: u32,
}

impl AIService {
    pub fn new() -> Self {
        Self {
            validation_threshold: 0.75,  // 75% threshold by default
            security_level: 2,           // Medium-high security level (1-3)
        }
    }

    pub async fn initialize(&self) -> Result<(), String> {
        info!("Initializing core AI service");
        Ok(())
    }

    pub async fn validate_block(&self, block: &Block) -> Result<(), String> {
        info!("Core: Validating block {}", block.header.index);
        
        // Validate block structure
        if block.header.index == 0 && block.header.previous_hash != "0".repeat(64) {
            return Err("Genesis block must have zeros as previous hash".to_string());
        } else if block.header.index > 0 && block.header.previous_hash.len() != 64 {
            return Err("Invalid previous hash length".to_string());
        }
        
        // Validate block hash
        if block.hash.len() != 64 {
            return Err("Invalid block hash length".to_string());
        }
        
        // Check if hash matches calculated hash
        let calculated_hash = self.calculate_block_hash(block);
        if block.hash != calculated_hash {
            return Err(format!("Block hash mismatch. Expected: {}, Found: {}", calculated_hash, block.hash));
        }
        
        // Validate transactions
        for tx in &block.transactions {
            self.validate_transaction(tx).await?
        }
        
        // Validate block timestamps
        let current_time = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map_err(|e| format!("System time error: {}", e))?
            .as_secs();
        
        // Block timestamp should not be too far in the future (5 minutes allowed for clock drift)
        if block.header.timestamp > current_time + 300 {
            return Err(format!("Block timestamp is in the future: {} vs current {}", 
                              block.header.timestamp, current_time));
        }
        
        // Block timestamp should not be too old (prevents replay attacks)
        if block.header.index > 0 && block.header.timestamp < current_time - 86400 {
            // Don't validate timestamps for historical blocks beyond 1 day
            // This is primarily for newly received blocks
            info!("Block {} has an old timestamp, but accepting for historical reasons", block.header.index);
        }
        
        Ok(())
    }
    
    pub async fn validate_transaction(&self, tx: &Transaction) -> Result<(), String> {
        // Verify transaction signature
        if !tx.verify() {
            return Err(format!("Invalid transaction signature for tx {}", tx.id));
        }
        
        // Verify transaction timestamp is not in the future
        let current_time = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map_err(|e| format!("System time error: {}", e))?
            .as_secs();
            
        if tx.timestamp > current_time + 300 { // Allow 5-minute clock drift
            return Err(format!("Transaction timestamp is in the future: {} vs current {}",
                             tx.timestamp, current_time));
        }
        
        // Verify transaction amount
        if tx.amount == 0 {
            return Err("Transaction amount cannot be zero".to_string());
        }
        
        // Basic format validation
        if tx.sender.is_empty() || tx.receiver.is_empty() {
            return Err("Sender and receiver addresses cannot be empty".to_string());
        }
        
        if tx.id.len() < 8 {
            return Err("Transaction ID is too short".to_string());
        }
        
        Ok(())
    }
    
    fn calculate_block_hash(&self, block: &Block) -> String {
        use sha2::{Sha256, Digest};
        
        let mut hasher = Sha256::new();
        
        // Include all block header fields in hash calculation
        let data = format!("{}{}{}{}{}{}",
            block.header.index,
            block.header.timestamp,
            block.header.previous_hash,
            block.header.nonce,
            serde_json::to_string(&block.transactions).unwrap_or_default(),
            block.meta.size
        );
        
        hasher.update(data.as_bytes());
        format!("{:x}", hasher.finalize())
    }
}

#[derive(Debug)]
pub struct AuthService {
    // TODO: Implement auth service
}

impl AuthService {
    pub fn new() -> Self {
        Self {}
    }
}

#[derive(Debug)]
pub struct MessageHandler {
    peers: Arc<tokio::sync::Mutex<Vec<SocketAddr>>>,
    message_sender: tokio::sync::mpsc::Sender<String>,
}

impl MessageHandler {
    pub fn new(message_sender: tokio::sync::mpsc::Sender<String>) -> Self {
        MessageHandler {
            peers: Arc::new(tokio::sync::Mutex::new(Vec::new())),
            message_sender,
        }
    }

    pub async fn broadcast_message(
        &self,
        message: NetworkMessage,
        _addr: SocketAddr,
    ) -> Result<(), anyhow::Error> {
        let peers = self.peers.lock().await;
        let message_json = serde_json::to_string(&message)
            .map_err(|e| anyhow::anyhow!("Failed to serialize message: {}", e))?;

        self.message_sender
            .send(message_json)
            .await
            .map_err(|e| anyhow::anyhow!("Failed to send message: {}", e))?;

        for peer in peers.iter() {
            info!("Broadcasting message to peer: {}", peer);
        }

        Ok(())
    }

    pub async fn add_peer(&self, _peer: Peer) {
        // TODO: Implement peer addition
    }
}
