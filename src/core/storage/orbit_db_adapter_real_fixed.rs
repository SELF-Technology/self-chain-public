//! Real implementation of OrbitDBAdapter using actual OrbitDB functionality
//! This version fixes the Send trait issues with IPFS client

use anyhow::{Result, anyhow, Context};
use async_trait::async_trait;
use serde_json::Value;
use std::path::PathBuf;
use std::sync::Arc;
use tokio::sync::RwLock;
use chrono::Utc;
use tracing::{info, debug, warn};

use crate::core::storage::{StorageAdapter, StorageStats};
use crate::storage::orbitdb_real::{OrbitDBManager, DbType};

/// Real OrbitDBAdapter implementation (Send-safe version)
pub struct RealOrbitDBAdapter {
    node_id: String,
    db_path: PathBuf,
    initialized: bool,
    manager: Option<Arc<OrbitDBManager>>,
    stats: Arc<RwLock<StorageStats>>,
    ipfs_api_url: String,
}

impl RealOrbitDBAdapter {
    /// Create a new RealOrbitDBAdapter instance
    pub fn new() -> Self {
        Self {
            node_id: String::new(),
            db_path: PathBuf::from("./data/orbitdb"),
            initialized: false,
            manager: None,
            stats: Arc::new(RwLock::new(StorageStats {
                blocks: 0,
                transactions: 0,
                peers: 0,
                size: 0,
                last_update: Utc::now().timestamp(),
                ipfs_objects: 0,
                orbit_db_documents: 0,
            })),
            ipfs_api_url: "http://localhost:5001".to_string(),
        }
    }
    
    /// Configure the path where OrbitDB data will be stored
    pub fn with_path(mut self, path: PathBuf) -> Self {
        self.db_path = path;
        self
    }
    
    /// Configure IPFS API endpoint
    pub fn with_ipfs_api(mut self, ipfs_api: &str) -> Self {
        self.ipfs_api_url = ipfs_api.to_string();
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
        
        // Get IPFS API URL
        let ipfs_api = std::env::var("IPFS_API")
            .unwrap_or_else(|_| self.ipfs_api_url.clone());
        
        // Test IPFS connection using HTTP client
        let client = reqwest::Client::new();
        match client.get(&format!("{}/api/v0/version", ipfs_api)).send().await {
            Ok(response) => {
                if response.status().is_success() {
                    info!("Connected to IPFS at {}", ipfs_api);
                } else {
                    warn!("IPFS returned error status: {}", response.status());
                }
            }
            Err(e) => {
                warn!("IPFS not available: {}. Running in offline mode.", e);
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
                ("state", DbType::KeyValue),
            ];
            
            for (name, db_type) in databases {
                let db_name = format!("{}-{}", node_id, name);
                manager.create_or_open(&db_name, db_type).await?;
                info!("Created OrbitDB database: {}", db_name);
            }
            
            // Update stats
            let mut stats = self.stats.write().await;
            stats.orbit_db_documents = 4; // We created 4 databases
            stats.last_update = Utc::now().timestamp();
        }
        
        self.initialized = true;
        info!("RealOrbitDBAdapter initialized successfully");
        Ok(())
    }
    
    /// Shutdown the storage adapter
    async fn shutdown(&mut self) -> Result<()> {
        if !self.initialized {
            return Ok(());
        }
        
        info!("Shutting down RealOrbitDBAdapter for node {}", self.node_id);
        
        // Skip sync on shutdown to avoid Send trait issues
        // In production, this would be handled by a separate sync service
        
        self.initialized = false;
        self.manager = None;
        
        info!("RealOrbitDBAdapter shutdown complete");
        Ok(())
    }
    
    /// Get storage statistics
    async fn get_stats(&self) -> Result<StorageStats> {
        let stats = self.stats.read().await;
        Ok(stats.clone())
    }
    
    /// Store document in OrbitDB
    async fn store_document(&self, collection: &str, document: &Value) -> Result<String> {
        if !self.initialized {
            return Err(anyhow!("Storage not initialized"));
        }
        
        let db_name = format!("{}-{}", self.node_id, collection);
        
        if let Some(manager) = &self.manager {
            if let Some(db) = manager.get(&db_name).await {
                let id = db.add(document.clone()).await?;
                
                // Update stats
                let mut stats = self.stats.write().await;
                match collection {
                    "blocks" => stats.blocks += 1,
                    "transactions" => stats.transactions += 1,
                    _ => {}
                }
                stats.last_update = Utc::now().timestamp();
                
                Ok(id)
            } else {
                Err(anyhow!("Database {} not found", db_name))
            }
        } else {
            Err(anyhow!("OrbitDB manager not initialized"))
        }
    }
    
    /// Retrieve document from OrbitDB
    async fn get_document(&self, collection: &str, id: &str) -> Result<Option<Value>> {
        if !self.initialized {
            return Err(anyhow!("Storage not initialized"));
        }
        
        let db_name = format!("{}-{}", self.node_id, collection);
        
        if let Some(manager) = &self.manager {
            if let Some(db) = manager.get(&db_name).await {
                Ok(db.get(id).await?)
            } else {
                Err(anyhow!("Database {} not found", db_name))
            }
        } else {
            Err(anyhow!("OrbitDB manager not initialized"))
        }
    }
    
    /// Query documents based on criteria
    async fn query_documents(&self, collection: &str, query: &Value) -> Result<Vec<Value>> {
        if !self.initialized {
            return Err(anyhow!("Storage not initialized"));
        }
        
        let db_name = format!("{}-{}", self.node_id, collection);
        
        if let Some(manager) = &self.manager {
            if let Some(db) = manager.get(&db_name).await {
                db.query(query).await
            } else {
                Err(anyhow!("Database {} not found", db_name))
            }
        } else {
            Err(anyhow!("OrbitDB manager not initialized"))
        }
    }
    
    /// Store data in IPFS
    async fn store_to_ipfs(&self, data: &[u8]) -> Result<String> {
        if !self.initialized {
            return Err(anyhow!("Storage not initialized"));
        }
        
        // Use HTTP API directly to avoid Send issues
        let client = reqwest::Client::new();
        let response = client
            .post(&format!("{}/api/v0/add", self.ipfs_api_url))
            .body(data.to_vec())
            .send()
            .await
            .context("Failed to store in IPFS")?;
        
        if response.status().is_success() {
            let result: serde_json::Value = response.json().await?;
            if let Some(hash) = result.get("Hash").and_then(|h| h.as_str()) {
                // Update stats
                let mut stats = self.stats.write().await;
                stats.ipfs_objects += 1;
                stats.size += data.len() as u64;
                stats.last_update = Utc::now().timestamp();
                
                Ok(hash.to_string())
            } else {
                Err(anyhow!("Invalid response from IPFS"))
            }
        } else {
            Err(anyhow!("IPFS returned error: {}", response.status()))
        }
    }
    
    /// Retrieve data from IPFS
    async fn retrieve_from_ipfs(&self, cid: &str) -> Result<Vec<u8>> {
        if !self.initialized {
            return Err(anyhow!("Storage not initialized"));
        }
        
        // Use HTTP API directly to avoid Send issues
        let client = reqwest::Client::new();
        let response = client
            .get(&format!("{}/api/v0/cat?arg={}", self.ipfs_api_url, cid))
            .send()
            .await
            .context("Failed to retrieve from IPFS")?;
        
        if response.status().is_success() {
            Ok(response.bytes().await?.to_vec())
        } else {
            Err(anyhow!("IPFS returned error: {}", response.status()))
        }
    }
}

impl Default for RealOrbitDBAdapter {
    fn default() -> Self {
        Self::new()
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    
    #[tokio::test]
    async fn test_adapter_creation() {
        let adapter = RealOrbitDBAdapter::new();
        assert!(!adapter.initialized);
        assert_eq!(adapter.ipfs_api_url, "http://localhost:5001");
    }
}