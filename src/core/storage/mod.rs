mod adapter;
mod orbit_db_adapter;
// mod orbit_db_adapter_real; // Old version with Send issues
mod orbit_db_adapter_real_fixed;

use anyhow::Result;
use serde::{Deserialize, Serialize};
use std::sync::Arc;
use tokio::sync::RwLock;

pub use adapter::{StorageAdapter, StorageAdapterFactory};
pub use orbit_db_adapter::OrbitDBAdapter;
pub use orbit_db_adapter_real_fixed::RealOrbitDBAdapter;

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct StorageStats {
    pub blocks: u64,
    pub transactions: u64,
    pub peers: u64,
    pub size: u64,
    pub last_update: i64,
    pub ipfs_objects: u64,
    pub orbit_db_documents: u64,
}

/// The HybridStorage class provides a hybrid storage solution combining
/// local database with distributed OrbitDB/IPFS storage
pub struct HybridStorage {
    adapter: Box<dyn StorageAdapter>,
    stats: Arc<RwLock<StorageStats>>,
    max_size: u64,
    node_id: String,
}

impl HybridStorage {
    /// Create a new HybridStorage instance with the specified size limit
    pub fn new(max_size: u64) -> Self {
        // Create default storage stats
        let stats = StorageStats {
            blocks: 0,
            transactions: 0,
            peers: 0,
            size: 0,
            last_update: chrono::Utc::now().timestamp(),
            ipfs_objects: 0,
            orbit_db_documents: 0,
        };
        
        // Use real OrbitDB adapter if IPFS is available, otherwise use mock
        let use_real_orbitdb = std::env::var("USE_REAL_ORBITDB")
            .unwrap_or_else(|_| "false".to_string()) == "true";
        
        let adapter: Box<dyn StorageAdapter> = if use_real_orbitdb {
            Box::new(RealOrbitDBAdapter::new())
        } else {
            Box::new(OrbitDBAdapter::new())
        };
        
        Self {
            adapter,
            stats: Arc::new(RwLock::new(stats)),
            max_size,
            node_id: String::new(),
        }
    }
    
    /// Create a new HybridStorage with real OrbitDB implementation
    pub fn new_with_real_orbitdb(max_size: u64) -> Self {
        let stats = StorageStats {
            blocks: 0,
            transactions: 0,
            peers: 0,
            size: 0,
            last_update: chrono::Utc::now().timestamp(),
            ipfs_objects: 0,
            orbit_db_documents: 0,
        };
        
        let adapter: Box<dyn StorageAdapter> = Box::new(RealOrbitDBAdapter::new());
        
        Self {
            adapter,
            stats: Arc::new(RwLock::new(stats)),
            max_size,
            node_id: String::new(),
        }
    }
    
    /// Initialize the storage system with the specified node ID
    pub async fn initialize(&mut self) -> Result<()> {
        // Generate a node ID if not set
        if self.node_id.is_empty() {
            self.node_id = format!("node-{}", chrono::Utc::now().timestamp());
        }
        
        // Initialize the storage adapter
        self.adapter.initialize(&self.node_id).await?;
        
        // Update statistics
        let adapter_stats = self.adapter.get_stats().await?;
        let mut stats = self.stats.write().await;
        *stats = adapter_stats;
        
        Ok(())    
    }
    
    /// Set the node ID for this storage instance
    pub fn with_node_id(mut self, node_id: String) -> Self {
        self.node_id = node_id;
        self
    }
    
    /// Get storage statistics
    pub async fn get_stats(&self) -> StorageStats {
        match self.adapter.get_stats().await {
            Ok(stats) => stats,
            Err(_) => {
                // Fall back to local stats if adapter fails
                let stats = self.stats.read().await;
                stats.clone()
            }
        }
    }
    
    /// Store document data in OrbitDB
    pub async fn store_document(&self, collection: &str, document: &serde_json::Value) -> Result<String> {
        self.adapter.store_document(collection, document).await
    }
    
    /// Retrieve document data from OrbitDB
    pub async fn get_document(&self, collection: &str, id: &str) -> Result<Option<serde_json::Value>> {
        self.adapter.get_document(collection, id).await
    }
    
    /// Query documents based on criteria
    pub async fn query_documents(&self, collection: &str, query: &serde_json::Value) -> Result<Vec<serde_json::Value>> {
        self.adapter.query_documents(collection, query).await
    }
    
    /// Store large binary data in IPFS
    pub async fn store_to_ipfs(&self, data: &[u8]) -> Result<String> {
        self.adapter.store_to_ipfs(data).await
    }
    
    /// Retrieve data from IPFS by CID
    pub async fn retrieve_from_ipfs(&self, cid: &str) -> Result<Vec<u8>> {
        self.adapter.retrieve_from_ipfs(cid).await
    }
    
    /// Shutdown the storage system gracefully
    pub async fn shutdown(&mut self) -> Result<()> {
        self.adapter.shutdown().await
    }
    
    /// Store a block in the storage system
    pub async fn store_block(&self, block: &crate::blockchain::Block) -> Result<String> {
        let block_json = serde_json::to_value(block)?;
        self.store_document("blocks", &block_json).await
    }
    
    /// Retrieve a block by its hash
    pub async fn get_block(&self, hash: &str) -> Result<Option<crate::blockchain::Block>> {
        if let Some(doc) = self.get_document("blocks", hash).await? {
            let block = serde_json::from_value(doc)?;
            Ok(Some(block))
        } else {
            Ok(None)
        }
    }
    
    /// Store a transaction in the storage system
    pub async fn store_transaction(&self, tx: &crate::blockchain::Transaction) -> Result<String> {
        let tx_json = serde_json::to_value(tx)?;
        self.store_document("transactions", &tx_json).await
    }
    
    /// Retrieve a transaction by its ID
    pub async fn get_transaction(&self, id: &str) -> Result<Option<crate::blockchain::Transaction>> {
        if let Some(doc) = self.get_document("transactions", id).await? {
            let tx = serde_json::from_value(doc)?;
            Ok(Some(tx))
        } else {
            Ok(None)
        }
    }
}
