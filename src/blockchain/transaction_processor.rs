use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::RwLock;
use tokio::time::interval;
use crate::blockchain::{block::Block, transaction_pool::TransactionPool};
use crate::consensus::poai::PoAIValidator;
use crate::network::communication::NodeCommunicator;

pub struct TransactionProcessor {
    pool: Arc<TransactionPool>,
    validator: Arc<PoAIValidator>,
    communicator: Arc<NodeCommunicator>,
    batch_size: usize,
    processing_interval: Duration,
    last_batch_time: SystemTime,
}

impl TransactionProcessor {
    pub fn new(
        pool: Arc<TransactionPool>,
        validator: Arc<PoAIValidator>,
        communicator: Arc<NodeCommunicator>,
        batch_size: usize,
    ) -> Self {
        Self {
            pool,
            validator,
            communicator,
            batch_size,
            processing_interval: Duration::from_millis(100), // 10ms interval for high TPS
            last_batch_time: SystemTime::now(),
        }
    }

    pub async fn start(&self) -> Result<(), String> {
        let pool = self.pool.clone();
        let validator = self.validator.clone();
        let communicator = self.communicator.clone();
        let batch_size = self.batch_size;

        tokio::spawn(async move {
            let mut interval = interval(Duration::from_millis(10)); // 10ms interval
            
            loop {
                interval.tick().await;
                
                // Get current time
                let now = SystemTime::now();
                
                // Process batch
                let batch = pool.get_batch(batch_size).await;
                
                for tx in batch {
                    // Validate transaction
                    if validator.validate_transaction(&tx).await.unwrap_or(false) {
                        // Create block
                        let block = Block::new(
                            /* index */ 0,
                            /* previous_hash */ String::new(),
                            vec![tx.clone()],
                            /* difficulty */ 1000000,
                            /* validator_key */ &Default::default(),
                        );

                        // Broadcast block
                        if let Err(e) = communicator.broadcast_message(NodeMessage::Block(block)).await {
                            eprintln!("Failed to broadcast block: {}", e);
                        }

                        // Mark as processed
                        let processing_time = now.elapsed().unwrap().as_nanos() as u64;
                        pool.mark_processed(tx, processing_time).await;
                    }
                }
            }
        });

        Ok(())
    }

    pub async fn get_tps(&self) -> u64 {
        let metrics = self.pool.get_metrics().await;
        metrics.current_tps
    }

    pub async fn get_peak_tps(&self) -> u64 {
        let metrics = self.pool.get_metrics().await;
        metrics.peak_tps
    }

    pub async fn get_pending_count(&self) -> u64 {
        let metrics = self.pool.get_metrics().await;
        metrics.pending_count
    }
}
