use std::sync::Arc;
use std::collections::BinaryHeap;
use std::cmp::Reverse;
use tokio::sync::RwLock;
use serde::{Deserialize, Serialize};
use crate::blockchain::block::Transaction;
use crate::core::config::BlockchainConfig;

#[derive(Debug, Serialize, Deserialize)]
pub struct TransactionPool {
    config: BlockchainConfig,
    pool: Arc<RwLock<TransactionHeap>>,
    pending: Arc<RwLock<Vec<Transaction>>>,
    processed: Arc<RwLock<Vec<Transaction>>>,
    metrics: Arc<RwLock<TransactionMetrics>>,
}

#[derive(Debug)]
struct TransactionHeap(BinaryHeap<Reverse<Arc<Transaction>>>);

impl TransactionHeap {
    fn new() -> Self {
        Self(BinaryHeap::new())
    }

    fn push(&mut self, tx: Arc<Transaction>) {
        self.0.push(Reverse(tx));
    }

    fn pop(&mut self) -> Option<Arc<Transaction>> {
        self.0.pop().map(|r| r.0)
    }

    fn len(&self) -> usize {
        self.0.len()
    }
}

#[derive(Debug, Serialize, Deserialize)]
pub struct TransactionMetrics {
    pub pending_count: u64,
    pub processed_count: u64,
    pub avg_processing_time: u64,
    pub peak_tps: u64,
    pub current_tps: u64,
}

impl TransactionPool {
    pub fn new(config: BlockchainConfig) -> Self {
        Self {
            config,
            pool: Arc::new(RwLock::new(TransactionHeap::new())),
            pending: Arc::new(RwLock::new(Vec::new())),
            processed: Arc::new(RwLock::new(Vec::new())),
            metrics: Arc::new(RwLock::new(TransactionMetrics {
                pending_count: 0,
                processed_count: 0,
                avg_processing_time: 0,
                peak_tps: 0,
                current_tps: 0,
            })),
        }
    }

    pub async fn add_transaction(&self, tx: Transaction) -> Result<(), String> {
        let tx = Arc::new(tx);
        
        // Validate transaction
        if !self.validate_transaction(&tx).await {
            return Err("Invalid transaction".to_string());
        }

        // Add to pool
        self.pool.write().await.push(tx.clone());
        
        // Update metrics
        self.update_metrics().await;
        
        Ok(())
    }

    async fn validate_transaction(&self, tx: &Transaction) -> bool {
        // TODO: Implement transaction validation
        // 1. Check signature
        // 2. Check balance
        // 3. Check nonce
        // 4. Check gas
        true
    }

    pub async fn get_batch(&self, size: usize) -> Vec<Arc<Transaction>> {
        let mut pool = self.pool.write().await;
        let mut batch = Vec::with_capacity(size);
        
        for _ in 0..size {
            if let Some(tx) = pool.pop() {
                batch.push(tx);
            } else {
                break;
            }
        }
        
        batch
    }

    pub async fn mark_processed(&self, tx: Arc<Transaction>, time: u64) {
        self.processed.write().await.push(tx);
        self.update_metrics_with_time(time).await;
    }

    async fn update_metrics(&self) {
        let mut metrics = self.metrics.write().await;
        metrics.pending_count = self.pool.read().await.len() as u64;
    }

    async fn update_metrics_with_time(&self, processing_time: u64) {
        let mut metrics = self.metrics.write().await;
        metrics.processed_count += 1;
        metrics.avg_processing_time = (metrics.avg_processing_time + processing_time) / 2;
        
        // Calculate TPS
        let current_tps = (1_000_000_000 / processing_time) as u64;
        metrics.current_tps = current_tps;
        if current_tps > metrics.peak_tps {
            metrics.peak_tps = current_tps;
        }
    }

    pub async fn get_metrics(&self) -> TransactionMetrics {
        self.metrics.read().await.clone()
    }
}
