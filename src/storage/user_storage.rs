/// User-specific storage implementation for self-sovereign blockchain nodes
/// Each user has their own complete storage stack: IPFS + OrbitDB
use anyhow::{Result, Context};
use serde::{Serialize, Deserialize};
use crate::blockchain::{Block, Transaction};
use crate::storage::user_ipfs::{UserIPFS, UserIPFSConfig};
use tracing::{info, debug, error};
use std::sync::Arc;
use tokio::sync::RwLock;

/// Configuration for a user's complete storage stack
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserStorageConfig {
    pub user_id: String,
    pub instance_id: String,
    pub ipfs_config: UserIPFSConfig,
    pub orbitdb_path: String,
}

/// User's personal decentralized storage system
pub struct UserStorage {
    config: UserStorageConfig,
    ipfs: UserIPFS,
    /// In production, this would be a real OrbitDB instance
    block_db: Arc<RwLock<std::collections::HashMap<String, Block>>>,
    tx_db: Arc<RwLock<std::collections::HashMap<String, Transaction>>>,
}

impl UserStorage {
    /// Create a new storage system for a user
    pub fn new(config: UserStorageConfig, ipfs: UserIPFS) -> Self {
        info!("Creating storage system for user: {} on instance: {}", 
            config.user_id, config.instance_id);
        
        Self {
            config,
            ipfs,
            block_db: Arc::new(RwLock::new(std::collections::HashMap::new())),
            tx_db: Arc::new(RwLock::new(std::collections::HashMap::new())),
        }
    }
    
    /// Initialize OrbitDB for the user
    pub async fn initialize_orbitdb(&self) -> Result<()> {
        info!("Initializing OrbitDB for user {} at path: {}", 
            self.config.user_id, self.config.orbitdb_path);
        
        // In production AWS deployment:
        // 1. Create OrbitDB instance using user's IPFS
        // 2. Create databases for blocks, transactions, state
        // 3. Set up replication if user has multiple nodes
        
        Ok(())
    }
    
    /// Store a block in user's decentralized storage
    pub async fn store_block(&self, block: &Block) -> Result<String> {
        debug!("User {} storing block {}", self.config.user_id, block.hash);
        
        // Store block metadata in OrbitDB
        self.block_db.write().await.insert(block.hash.clone(), block.clone());
        
        // Store block data in IPFS
        let block_data = serde_json::to_vec(block)
            .context("Failed to serialize block")?;
        let cid = self.ipfs.add(&block_data).await?;
        
        // Pin important blocks
        self.ipfs.pin(&cid).await?;
        
        info!("User {} stored block {} with CID: {}", 
            self.config.user_id, block.hash, cid);
        
        Ok(cid)
    }
    
    /// Retrieve a block from user's storage
    pub async fn get_block(&self, hash: &str) -> Result<Option<Block>> {
        // First check OrbitDB cache
        if let Some(block) = self.block_db.read().await.get(hash) {
            return Ok(Some(block.clone()));
        }
        
        // If not in cache, this might be from another user's node
        // In production, we'd query the P2P network
        Ok(None)
    }
    
    /// Store a transaction in user's storage
    pub async fn store_transaction(&self, tx: &Transaction) -> Result<String> {
        debug!("User {} storing transaction {}", self.config.user_id, tx.id);
        
        // Store in OrbitDB
        self.tx_db.write().await.insert(tx.id.clone(), tx.clone());
        
        // Store in IPFS
        let tx_data = serde_json::to_vec(tx)
            .context("Failed to serialize transaction")?;
        let cid = self.ipfs.add(&tx_data).await?;
        
        Ok(cid)
    }
    
    /// Get storage statistics for the user
    pub async fn get_stats(&self) -> Result<UserStorageStats> {
        let ipfs_stats = self.ipfs.stats().await?;
        let blocks_count = self.block_db.read().await.len();
        let txs_count = self.tx_db.read().await.len();
        
        Ok(UserStorageStats {
            user_id: self.config.user_id.clone(),
            instance_id: self.config.instance_id.clone(),
            blocks_stored: blocks_count,
            transactions_stored: txs_count,
            ipfs_objects: ipfs_stats.total_objects,
            total_size_bytes: ipfs_stats.total_size,
        })
    }
}

#[derive(Debug, Serialize, Deserialize)]
pub struct UserStorageStats {
    pub user_id: String,
    pub instance_id: String,
    pub blocks_stored: usize,
    pub transactions_stored: usize,
    pub ipfs_objects: usize,
    pub total_size_bytes: usize,
}

/// Adapter to make UserStorage compatible with the runtime StorageAdapter trait
impl UserStorage {
    pub async fn store_block_bytes(&self, block_bytes: Vec<u8>) -> Result<()> {
        let block: Block = serde_json::from_slice(&block_bytes)
            .context("Failed to deserialize block")?;
        
        self.store_block(&block).await?;
        Ok(())
    }
    
    pub async fn fetch_block_by_index(&self, index: u64) -> Result<Option<Vec<u8>>> {
        // In a real implementation, we'd maintain an index-to-hash mapping
        // For now, search through blocks
        let blocks = self.block_db.read().await;
        
        for block in blocks.values() {
            if block.header.index == index {
                let block_bytes = serde_json::to_vec(block)?;
                return Ok(Some(block_bytes));
            }
        }
        
        Ok(None)
    }
}

/// Factory for provisioning user storage during AWS instance setup
pub struct UserStorageFactory;

impl UserStorageFactory {
    /// Provision complete storage stack for a new user
    pub async fn provision_for_user(
        user_id: &str, 
        instance_id: &str,
        ipfs: UserIPFS
    ) -> Result<UserStorage> {
        let config = UserStorageConfig {
            user_id: user_id.to_string(),
            instance_id: instance_id.to_string(),
            ipfs_config: ipfs.config.clone(),
            orbitdb_path: format!("/home/{}/orbitdb", user_id),
        };
        
        let storage = UserStorage::new(config, ipfs);
        storage.initialize_orbitdb().await?;
        
        info!("Provisioned complete storage stack for user {} on instance {}", 
            user_id, instance_id);
        
        Ok(storage)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::storage::user_ipfs::UserIPFSFactory;
    
    #[tokio::test]
    async fn test_user_storage_isolation() {
        // Create storage for two different users
        let alice_ipfs = UserIPFSFactory::provision_for_user("alice", "i-alice").await.unwrap();
        let alice_storage = UserStorageFactory::provision_for_user("alice", "i-alice", alice_ipfs).await.unwrap();
        
        let bob_ipfs = UserIPFSFactory::provision_for_user("bob", "i-bob").await.unwrap();
        let bob_storage = UserStorageFactory::provision_for_user("bob", "i-bob", bob_ipfs).await.unwrap();
        
        // Create test blocks
        let alice_block = Block {
            hash: "alice_block_1".to_string(),
            header: Default::default(),
            ..Default::default()
        };
        
        let bob_block = Block {
            hash: "bob_block_1".to_string(),
            header: Default::default(),
            ..Default::default()
        };
        
        // Store blocks in respective storages
        alice_storage.store_block(&alice_block).await.unwrap();
        bob_storage.store_block(&bob_block).await.unwrap();
        
        // Verify isolation - each user can only see their own blocks
        assert!(alice_storage.get_block("alice_block_1").await.unwrap().is_some());
        assert!(alice_storage.get_block("bob_block_1").await.unwrap().is_none());
        
        assert!(bob_storage.get_block("bob_block_1").await.unwrap().is_some());
        assert!(bob_storage.get_block("alice_block_1").await.unwrap().is_none());
    }
}