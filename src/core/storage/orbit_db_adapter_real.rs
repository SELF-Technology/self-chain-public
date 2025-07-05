//! Real implementation of OrbitDBAdapter using actual OrbitDB functionality

use anyhow::{Result, anyhow, Context};
use async_trait::async_trait;
use serde_json::Value;
use std::path::PathBuf;
use std::sync::Arc;
use tokio::sync::RwLock;
use chrono::Utc;
use tracing::{info, debug, warn};
use ipfs_api_backend_hyper::{IpfsClient, IpfsApi};
use std::sync::Mutex;

use crate::core::storage::{StorageAdapter, StorageStats};
use crate::storage::orbitdb_real::{OrbitDBManager, DbType, OrbitEntry};

/// Real OrbitDBAdapter implementation
pub struct RealOrbitDBAdapter {
    node_id: String,
    db_path: PathBuf,
    initialized: bool,
    manager: Option<Arc<OrbitDBManager>>,
    ipfs_client: Option<Arc<Mutex<IpfsClient>>>,
    stats: Arc<RwLock<StorageStats>>,
}

impl RealOrbitDBAdapter {
    /// Create a new RealOrbitDBAdapter instance
    pub fn new() -> Self {
        Self {
            node_id: String::new(),
            db_path: PathBuf::from("./data/orbitdb"),
            initialized: false,
            manager: None,
            ipfs_client: None,
            stats: Arc::new(RwLock::new(StorageStats {
                blocks: 0,
                transactions: 0,
                peers: 0,
                size: 0,
                last_update: Utc::now().timestamp(),
                ipfs_objects: 0,
                orbit_db_documents: 0,
            })),
        }
    }
    
    /// Configure the path where OrbitDB data will be stored
    pub fn with_path(mut self, path: PathBuf) -> Self {
        self.db_path = path;
        self
    }
    
    /// Configure IPFS API endpoint
    pub fn with_ipfs_api(self, _ipfs_api: &str) -> Self {
        // IPFS API is configured in initialize()
        self
    }
}

#[async_trait]
impl StorageAdapter for RealOrbitDBAdapter {
    /// Initialize the OrbitDB adapter
    async fn initialize(&mut self, node_id: &str) -> Result<()> {
        info!("Initializing RealOrbitDBAdapter for node {}", node_id);
        
        self.node_id = node_id.to_string();
        
        // Create directory if it doesn't exist
        tokio::fs::create_dir_all(&self.db_path).await
            .context("Failed to create OrbitDB directory")?;
        
        // Initialize IPFS client
        let ipfs_api = std::env::var("IPFS_API")
            .unwrap_or_else(|_| "http://localhost:5001".to_string());
        
        self.ipfs_client = Some(IpfsClient::from_str(&ipfs_api)
            .context("Failed to create IPFS client")?);
        
        // Test IPFS connection
        if let Some(client) = &self.ipfs_client {
            match client.version().await {
                Ok(version) => {
                    info!("Connected to IPFS version: {}", version.version);
                }
                Err(e) => {
                    warn!("IPFS not available: {}. Running in offline mode.", e);
                }
            }
        }
        
        // Initialize OrbitDB manager
        self.manager = Some(Arc::new(OrbitDBManager::new(&ipfs_api)?));
        
        // Create standard databases
        if let Some(manager) = &self.manager {
            let databases = vec![
                ("blocks", DbType::DocStore),
                ("transactions", DbType::DocStore),
                ("peers", DbType::KeyValue),
                ("system", DbType::EventLog),
            ];
            
            for (name, db_type) in databases {
                manager.create_or_open(name, db_type).await
                    .context(format!("Failed to create {} database", name))?;
                debug!("Initialized OrbitDB database: {}", name);
            }
        }
        
        self.initialized = true;
        
        // Update stats
        {
            let mut stats = self.stats.write().await;
            stats.orbit_db_documents = 4; // 4 standard databases
            stats.last_update = Utc::now().timestamp();
        }
        
        info!("RealOrbitDBAdapter initialized successfully");
        Ok(())
    }
    
    /// Shutdown the storage adapter
    async fn shutdown(&mut self) -> Result<()> {
        if !self.initialized {
            return Ok(());
        }
        
        info!("Shutting down RealOrbitDBAdapter for node {}", self.node_id);
        
        // Sync all databases before shutdown
        if let Some(manager) = &self.manager {
            manager.sync_all().await?;
        }
        
        self.initialized = false;
        self.manager = None;
        self.ipfs_client = None;
        
        Ok(())
    }
    
    /// Get storage statistics
    async fn get_stats(&self) -> Result<StorageStats> {
        let mut stats = self.stats.write().await;
        
        // Update peer count from IPFS
        if let Some(client) = &self.ipfs_client {
            if let Ok(peers) = client.peers().await {
                stats.peers = peers.peers.len() as u64;
            }
        }
        
        stats.last_update = Utc::now().timestamp();
        Ok(stats.clone())
    }
    
    /// Store document in OrbitDB
    async fn store_document(&self, collection: &str, document: &Value) -> Result<String> {
        if !self.initialized {
            return Err(anyhow!("RealOrbitDBAdapter not initialized"));
        }
        
        let manager = self.manager.as_ref()
            .ok_or_else(|| anyhow!("OrbitDB manager not available"))?;
        
        let db = manager.get(collection).await
            .ok_or_else(|| anyhow!("Database {} not found", collection))?;
        
        // Add document to OrbitDB
        let cid = db.add_entry(document).await
            .context("Failed to add document to OrbitDB")?;
        
        // Update stats
        {
            let mut stats = self.stats.write().await;
            stats.orbit_db_documents += 1;
            
            match collection {
                "blocks" => stats.blocks += 1,
                "transactions" => stats.transactions += 1,
                _ => {}
            }
            
            stats.last_update = Utc::now().timestamp();
        }
        
        debug!("Stored document in {} with CID: {}", collection, cid);
        Ok(cid)
    }
    
    /// Retrieve document from OrbitDB
    async fn get_document(&self, collection: &str, id: &str) -> Result<Option<Value>> {
        if !self.initialized {
            return Err(anyhow!("RealOrbitDBAdapter not initialized"));
        }
        
        let manager = self.manager.as_ref()
            .ok_or_else(|| anyhow!("OrbitDB manager not available"))?;
        
        let db = manager.get(collection).await
            .ok_or_else(|| anyhow!("Database {} not found", collection))?;
        
        // Try to get by entry ID first
        if let Some(entry) = db.get_entry(id).await? {
            return Ok(Some(entry.data));
        }
        
        // If not found by ID, search through all entries
        let entries = db.get_all_entries().await?;
        for entry in entries {
            if let Some(doc_id) = entry.data.get("id").and_then(|v| v.as_str()) {
                if doc_id == id {
                    return Ok(Some(entry.data));
                }
            }
        }
        
        Ok(None)
    }
    
    /// Query documents in OrbitDB
    async fn query_documents(&self, collection: &str, query: &Value) -> Result<Vec<Value>> {
        if !self.initialized {
            return Err(anyhow!("RealOrbitDBAdapter not initialized"));
        }
        
        let manager = self.manager.as_ref()
            .ok_or_else(|| anyhow!("OrbitDB manager not available"))?;
        
        let db = manager.get(collection).await
            .ok_or_else(|| anyhow!("Database {} not found", collection))?;
        
        // Simple query implementation - match fields in query with document fields
        let entries = db.get_all_entries().await?;
        let mut results = Vec::new();
        
        for entry in entries {
            let mut matches = true;
            
            // Check if all query fields match
            if let Some(query_obj) = query.as_object() {
                for (key, value) in query_obj {
                    if entry.data.get(key) != Some(value) {
                        matches = false;
                        break;
                    }
                }
            }
            
            if matches {
                results.push(entry.data);
            }
        }
        
        Ok(results)
    }
    
    /// Store data in IPFS
    async fn store_to_ipfs(&self, data: &[u8]) -> Result<String> {
        let client = self.ipfs_client.as_ref()
            .ok_or_else(|| anyhow!("IPFS client not available"))?;
        
        let res = client.add(data.to_vec().into()).await
            .context("Failed to add data to IPFS")?;
        
        // Update stats
        {
            let mut stats = self.stats.write().await;
            stats.ipfs_objects += 1;
            stats.size += data.len() as u64;
            stats.last_update = Utc::now().timestamp();
        }
        
        debug!("Stored {} bytes in IPFS with CID: {}", data.len(), res.hash);
        Ok(res.hash)
    }
    
    /// Retrieve data from IPFS
    async fn retrieve_from_ipfs(&self, cid: &str) -> Result<Vec<u8>> {
        let client = self.ipfs_client.as_ref()
            .ok_or_else(|| anyhow!("IPFS client not available"))?;
        
        let data = client.cat(cid)
            .map(|chunk| chunk.unwrap_or_default())
            .collect::<Vec<_>>()
            .await
            .into_iter()
            .flatten()
            .collect::<Vec<u8>>();
        
        if data.is_empty() {
            return Err(anyhow!("No data found for CID: {}", cid));
        }
        
        debug!("Retrieved {} bytes from IPFS CID: {}", data.len(), cid);
        Ok(data)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    
    #[tokio::test]
    async fn test_adapter_initialization() {
        let mut adapter = RealOrbitDBAdapter::new();
        
        // This will fail if IPFS is not running
        // let result = adapter.initialize("test-node").await;
        // assert!(result.is_ok());
        // assert!(adapter.initialized);
    }
}