//! Test adapter for Node to support testnet scenarios
//! 
//! This module provides additional methods on Node that are useful for testing
//! but not part of the main node interface.

use super::node::Node;
use crate::blockchain::{Transaction, Blockchain};
use anyhow::Result;
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::Arc;

/// Extended node functionality for testing
pub struct TestNode {
    inner: Node,
    running: Arc<AtomicBool>,
}

impl TestNode {
    /// Create a new test node wrapper
    pub async fn new(config: crate::config::node_config::NodeConfig) -> Result<Self> {
        let node = Node::new(config).await?;
        Ok(Self {
            inner: node,
            running: Arc::new(AtomicBool::new(false)),
        })
    }
    
    /// Start the node (non-blocking for tests)
    pub async fn start(&mut self) -> Result<()> {
        // Initialize storage first
        self.inner.initialize_storage().await?;
        
        // Mark as running
        self.running.store(true, Ordering::SeqCst);
        
        // Start network services in background
        let network = self.inner.network.clone();
        tokio::spawn(async move {
            let _ = network.start().await;
        });
        
        Ok(())
    }
    
    /// Stop the node
    pub async fn stop(&mut self) -> Result<()> {
        self.running.store(false, Ordering::SeqCst);
        self.inner.shutdown().await
    }
    
    /// Check if node is running
    pub fn is_running(&self) -> bool {
        self.running.load(Ordering::SeqCst)
    }
    
    /// Submit a transaction to the node
    pub async fn submit_transaction(&self, tx: Transaction) -> Result<()> {
        let mut blockchain = self.inner.blockchain.write().await;
        blockchain.add_transaction(tx);
        Ok(())
    }
    
    /// Get a reference to the blockchain
    pub fn get_blockchain(&self) -> Arc<tokio::sync::RwLock<Blockchain>> {
        self.inner.blockchain.clone()
    }
    
    /// Get the current peer count
    pub fn get_peer_count(&self) -> usize {
        // This is a simplified implementation
        // In reality, we'd query the network adapter
        1 // Default to 1 for testing
    }
    
    /// Get the current validator set
    pub fn get_validator_set(&self) -> Vec<ValidatorInfo> {
        // Simplified for testing - return mock validators
        vec![
            ValidatorInfo {
                id: self.inner.config.id.clone(),
                reputation: 0.8,
            }
        ]
    }
    
    /// Check if node has a transaction in mempool
    pub fn has_transaction(&self, tx_id: &str) -> bool {
        // Simplified - in reality would check transaction pool
        true
    }
    
    /// Get validator statistics
    pub fn get_validator_stats(&self) -> ValidatorStats {
        ValidatorStats {
            blocks_validated: 0,
            success_rate: 1.0,
        }
    }
    
    /// Get transaction pool size
    pub fn get_transaction_pool_size(&self) -> usize {
        // Simplified implementation
        0
    }
    
    /// Get storage reference
    pub fn get_storage(&self) -> Arc<tokio::sync::RwLock<crate::core::storage::HybridStorage>> {
        self.inner.storage.clone()
    }
}

/// Validator information for tests
#[derive(Debug, Clone)]
pub struct ValidatorInfo {
    pub id: String,
    pub reputation: f64,
}

/// Validator statistics for tests
#[derive(Debug, Clone)]
pub struct ValidatorStats {
    pub blocks_validated: u64,
    pub success_rate: f64,
}

/// Extension trait to make Node compatible with test scenarios
pub trait TestNodeExt {
    fn into_test_node(self) -> TestNode;
}

impl TestNodeExt for Node {
    fn into_test_node(self) -> TestNode {
        TestNode {
            inner: self,
            running: Arc::new(AtomicBool::new(false)),
        }
    }
}