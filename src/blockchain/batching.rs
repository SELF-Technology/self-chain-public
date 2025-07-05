use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::RwLock;
use serde::{Deserialize, Serialize};
use crate::blockchain::block::{Block, Transaction};
use crate::consensus::poai::PoAIValidator;
use crate::network::communication::NodeCommunicator;

#[derive(Debug, Serialize, Deserialize)]
pub struct BatchConfig {
    pub max_size: usize,
    pub min_size: usize,
    pub timeout: u64, // in ms
    pub validation_parallelism: usize,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct BatchMetrics {
    pub avg_batch_size: usize,
    pub max_batch_size: usize,
    pub min_batch_size: usize,
    pub batches_processed: u64,
    pub avg_processing_time: u64, // in ns
    pub peak_tps: u64,
}

pub struct TransactionBatcher {
    config: BatchConfig,
    metrics: Arc<RwLock<BatchMetrics>>,
    validator: Arc<PoAIValidator>,
    communicator: Arc<NodeCommunicator>,
    current_batch: Arc<RwLock<Vec<Arc<Transaction>>>>,
    last_batch_time: SystemTime,
}

impl TransactionBatcher {
    pub fn new(
        config: BatchConfig,
        validator: Arc<PoAIValidator>,
        communicator: Arc<NodeCommunicator>,
    ) -> Self {
        Self {
            config,
            metrics: Arc::new(RwLock::new(BatchMetrics {
                avg_batch_size: 0,
                max_batch_size: 0,
                min_batch_size: usize::MAX,
                batches_processed: 0,
                avg_processing_time: 0,
                peak_tps: 0,
            })),
            validator,
            communicator,
            current_batch: Arc::new(RwLock::new(Vec::new())),
            last_batch_time: SystemTime::now(),
        }
    }

    pub async fn add_transaction(&self, tx: Transaction) -> Result<(), String> {
        let tx = Arc::new(tx);
        self.current_batch.write().await.push(tx);
        
        // Check if we should process the batch
        if self.should_process_batch().await {
            self.process_batch().await;
        }
        
        Ok(())
    }

    async fn should_process_batch(&self) -> bool {
        let batch = self.current_batch.read().await;
        
        // Check batch size
        if batch.len() >= self.config.max_size {
            return true;
        }

        // Check timeout
        let elapsed = self.last_batch_time.elapsed().unwrap().as_millis();
        if elapsed >= self.config.timeout {
            return true;
        }

        false
    }

    async fn process_batch(&self) {
        let mut batch = self.current_batch.write().await;
        
        if batch.is_empty() {
            return;
        }

        let start_time = SystemTime::now();
        
        // Split batch for parallel validation
        let chunks: Vec<_> = batch.chunks(self.config.validation_parallelism)
            .map(|chunk| chunk.to_vec())
            .collect();

        // Validate chunks in parallel
        let validation_futures = chunks.into_iter()
            .map(|chunk| self.validate_chunk(chunk));

        let validation_results = tokio::join_all(validation_futures).await;

        // Process valid transactions
        for (chunk, result) in batch.chunks(self.config.validation_parallelism).zip(validation_results) {
            if result {
                let block = Block::new(
                    /* index */ 0,
                    /* previous_hash */ String::new(),
                    chunk.iter().map(|tx| tx.clone()).collect(),
                    /* difficulty */ 1000000,
                    /* validator_key */ &Default::default(),
                );

                if let Err(e) = self.communicator.broadcast_message(NodeMessage::Block(block)).await {
                    eprintln!("Failed to broadcast block: {}", e);
                }
            }
        }

        // Update metrics
        self.update_metrics(batch.len(), start_time.elapsed().unwrap().as_nanos() as u64).await;
        
        // Clear batch
        batch.clear();
        self.last_batch_time = SystemTime::now();
    }

    async fn validate_chunk(&self, chunk: Vec<Arc<Transaction>>) -> bool {
        // Validate chunk using PoAI
        self.validator.validate_transactions(&chunk).await
    }

    async fn update_metrics(&self, batch_size: usize, processing_time: u64) {
        let mut metrics = self.metrics.write().await;
        metrics.batches_processed += 1;
        metrics.avg_batch_size = (metrics.avg_batch_size + batch_size) / 2;
        metrics.max_batch_size = metrics.max_batch_size.max(batch_size);
        metrics.min_batch_size = metrics.min_batch_size.min(batch_size);
        metrics.avg_processing_time = (metrics.avg_processing_time + processing_time) / 2;
        
        // Calculate TPS
        let current_tps = (1_000_000_000 / processing_time) as u64;
        metrics.peak_tps = metrics.peak_tps.max(current_tps);
    }

    pub async fn get_metrics(&self) -> BatchMetrics {
        self.metrics.read().await.clone()
    }
}
