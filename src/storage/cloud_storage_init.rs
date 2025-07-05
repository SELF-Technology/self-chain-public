//! Cloud storage initialization trait for node startup
//! This defines the interface that JMac will implement for Task #1

use anyhow::Result;

/// Trait for initializing node storage and network connectivity
/// 
/// This will be implemented by the CloudAdapter to enable nodes to:
/// 1. Initialize their storage (OrbitDB/IPFS)
/// 2. Join the network with peer discovery
pub trait CloudStorageInit {
    /// Initialize storage for a new node
    /// 
    /// This should:
    /// - Create necessary storage directories
    /// - Initialize OrbitDB instance
    /// - Set up IPFS node
    /// - Return any errors during initialization
    async fn initialize_node_storage(&self, node_id: &str) -> Result<()>;
    
    /// Join the network using bootstrap peers
    /// 
    /// This should:
    /// - Connect to bootstrap peers
    /// - Announce presence on the network
    /// - Begin peer discovery
    async fn join_network(&self, bootstrap_peers: Vec<String>) -> Result<()>;
}

// TODO(JMac): Implement this trait in cloud_adapter.rs