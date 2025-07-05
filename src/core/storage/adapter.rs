use anyhow::Result;
use async_trait::async_trait;
use serde_json::Value;
use std::sync::Arc;
use tokio::sync::RwLock;

use crate::core::storage::StorageStats;

/// StorageAdapter trait defines the interface for OrbitDB and IPFS storage
/// This provides a unified interface for the node to interact with distributed storage
#[async_trait]
pub trait StorageAdapter: Send + Sync {
    /// Initialize the storage system with OrbitDB and IPFS
    /// This should connect to IPFS and initialize OrbitDB with the node's identity
    async fn initialize(&mut self, node_id: &str) -> Result<()>;
    
    /// Shutdown the storage system gracefully
    /// This should close all database connections and save any pending changes
    async fn shutdown(&mut self) -> Result<()>;
    
    /// Store document data with the given key using OrbitDB
    /// Returns the unique ID of the stored document
    async fn store_document(&self, collection: &str, document: &Value) -> Result<String>;
    
    /// Retrieve document data with the given ID from OrbitDB
    /// Returns None if document with the given ID doesn't exist
    async fn get_document(&self, collection: &str, id: &str) -> Result<Option<Value>>;
    
    /// Query documents based on criteria
    /// Returns a list of documents matching the query
    async fn query_documents(&self, collection: &str, query: &Value) -> Result<Vec<Value>>;
    
    /// Store large binary data in IPFS
    /// Returns the Content Identifier (CID) for the stored data
    async fn store_to_ipfs(&self, data: &[u8]) -> Result<String>;
    
    /// Retrieve data from IPFS by CID
    /// Returns the binary data associated with the given CID
    async fn retrieve_from_ipfs(&self, cid: &str) -> Result<Vec<u8>>;
    
    /// Get storage statistics including total documents, IPFS objects, and size
    async fn get_stats(&self) -> Result<StorageStats>;
}

/// Factory trait for creating storage adapters
/// This allows different storage adapter implementations to be created based on configuration
pub trait StorageAdapterFactory {
    /// Create a new storage adapter instance based on the provided configuration
    fn create_adapter(&self, config: &Value) -> Result<Box<dyn StorageAdapter>>;
}
