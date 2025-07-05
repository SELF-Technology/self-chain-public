use std::sync::Arc;
use std::time::Duration;
use tokio::sync::RwLock;
use serde::{Deserialize, Serialize};
use lru::LruCache;
use crate::blockchain::block::{Block, Transaction};
use crate::consensus::poai::PoAIValidator;

#[derive(Debug, Serialize, Deserialize)]
pub struct CacheConfig {
    pub transaction_capacity: usize,
    pub block_capacity: usize,
    pub network_state_capacity: usize,
    pub validation_result_capacity: usize,
    pub ttl: u64, // in seconds
}

#[derive(Debug, Serialize, Deserialize)]
pub struct CacheMetrics {
    pub transaction_hits: u64,
    pub transaction_misses: u64,
    pub block_hits: u64,
    pub block_misses: u64,
    pub network_state_hits: u64,
    pub network_state_misses: u64,
    pub validation_hits: u64,
    pub validation_misses: u64,
}

pub struct BlockchainCache {
    config: CacheConfig,
    metrics: Arc<RwLock<CacheMetrics>>,
    transaction_cache: RwLock<LruCache<String, Arc<Transaction>>>,
    block_cache: RwLock<LruCache<String, Arc<Block>>>,
    network_state_cache: RwLock<LruCache<String, NetworkState>>,
    validation_cache: RwLock<LruCache<String, ValidationResult>>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct NetworkState {
    pub active_nodes: u32,
    pub avg_block_time: u64,
    pub pending_transactions: u32,
    pub network_load: f64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ValidationResult {
    pub valid: bool,
    pub timestamp: u64,
}

impl BlockchainCache {
    pub fn new(config: CacheConfig) -> Self {
        Self {
            config,
            metrics: Arc::new(RwLock::new(CacheMetrics {
                transaction_hits: 0,
                transaction_misses: 0,
                block_hits: 0,
                block_misses: 0,
                network_state_hits: 0,
                network_state_misses: 0,
                validation_hits: 0,
                validation_misses: 0,
            })),
            transaction_cache: RwLock::new(LruCache::new(config.transaction_capacity)),
            block_cache: RwLock::new(LruCache::new(config.block_capacity)),
            network_state_cache: RwLock::new(LruCache::new(config.network_state_capacity)),
            validation_cache: RwLock::new(LruCache::new(config.validation_result_capacity)),
        }
    }

    pub async fn cache_transaction(&self, tx: Arc<Transaction>) {
        let key = format!("tx_{}", tx.id);
        self.transaction_cache.write().await.put(key, tx);
    }

    pub async fn get_transaction(&self, id: &str) -> Option<Arc<Transaction>> {
        let key = format!("tx_{}", id);
        if let Some(tx) = self.transaction_cache.write().await.get(&key) {
            self.metrics.write().await.transaction_hits += 1;
            Some(tx.clone())
        } else {
            self.metrics.write().await.transaction_misses += 1;
            None
        }
    }

    pub async fn cache_block(&self, block: Arc<Block>) {
        let key = format!("block_{}", block.hash);
        self.block_cache.write().await.put(key, block);
    }

    pub async fn get_block(&self, hash: &str) -> Option<Arc<Block>> {
        let key = format!("block_{}", hash);
        if let Some(block) = self.block_cache.write().await.get(&key) {
            self.metrics.write().await.block_hits += 1;
            Some(block.clone())
        } else {
            self.metrics.write().await.block_misses += 1;
            None
        }
    }

    pub async fn cache_network_state(&self, state: NetworkState) {
        let key = "network_state".to_string();
        self.network_state_cache.write().await.put(key, state);
    }

    pub async fn get_network_state(&self) -> Option<NetworkState> {
        let key = "network_state".to_string();
        if let Some(state) = self.network_state_cache.write().await.get(&key) {
            self.metrics.write().await.network_state_hits += 1;
            Some(state.clone())
        } else {
            self.metrics.write().await.network_state_misses += 1;
            None
        }
    }

    pub async fn cache_validation_result(&self, tx_id: &str, result: ValidationResult) {
        let key = format!("validation_{}", tx_id);
        self.validation_cache.write().await.put(key, result);
    }

    pub async fn get_validation_result(&self, tx_id: &str) -> Option<ValidationResult> {
        let key = format!("validation_{}", tx_id);
        if let Some(result) = self.validation_cache.write().await.get(&key) {
            self.metrics.write().await.validation_hits += 1;
            Some(result.clone())
        } else {
            self.metrics.write().await.validation_misses += 1;
            None
        }
    }

    pub async fn get_metrics(&self) -> CacheMetrics {
        self.metrics.read().await.clone()
    }

    pub async fn cleanup(&self) {
        // Remove expired items
        let now = SystemTime::now().duration_since(SystemTime::UNIX_EPOCH).unwrap().as_secs();
        
        let mut validation_cache = self.validation_cache.write().await;
        validation_cache.retain(|_, v| now - v.timestamp < self.config.ttl);
    }
}
