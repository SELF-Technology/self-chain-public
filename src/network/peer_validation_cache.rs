use std::collections::HashMap;
use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::RwLock;
use libp2p::PeerId;
use lru::LruCache;
use crate::network::peer_validation_metrics::PeerValidationMetrics;

pub struct PeerValidationCache {
    cache: Arc<RwLock<LruCache<PeerId, CachedValidation>>>,
    metrics: Arc<PeerValidationMetrics>,
    config: CacheConfig,
}

#[derive(Debug, Clone)]
pub struct CacheConfig {
    pub max_size: usize,
    pub validation_ttl: Duration,
    pub cleanup_interval: Duration,
}

#[derive(Debug, Clone)]
struct CachedValidation {
    pub peer_id: PeerId,
    pub validation_result: ValidationResult,
    pub timestamp: SystemTime,
    pub score: f64,
    pub validation_type: ValidationType,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub enum ValidationResult {
    Success,
    Failure,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub enum ValidationType {
    Certificate,
    Reputation,
    ResponseTime,
    Full,
}

impl PeerValidationCache {
    pub fn new(
        metrics: Arc<PeerValidationMetrics>,
        config: CacheConfig,
    ) -> Self {
        Self {
            cache: Arc::new(RwLock::new(LruCache::new(config.max_size))),
            metrics,
            config,
        }
    }

    pub async fn get(&self, peer_id: &PeerId, validation_type: ValidationType) -> Option<CachedValidation> {
        let cache = self.cache.read().await;
        if let Some(cached) = cache.get(peer_id) {
            if cached.validation_type == validation_type &&
                cached.timestamp + self.config.validation_ttl > SystemTime::now() {
                self.metrics.increment_cache_hits();
                return Some(cached.clone());
            }
        }
        self.metrics.increment_cache_misses();
        None
    }

    pub async fn insert(&self, peer_id: &PeerId, result: ValidationResult, score: f64, validation_type: ValidationType) {
        let mut cache = self.cache.write().await;
        cache.put(
            peer_id.clone(),
            CachedValidation {
                peer_id: peer_id.clone(),
                validation_result: result,
                timestamp: SystemTime::now(),
                score,
                validation_type,
            },
        );
        self.metrics.set_cache_size(cache.len());
    }

    pub async fn cleanup(&self) {
        let current_time = SystemTime::now();
        let mut cache = self.cache.write().await;
        cache.retain(|_, cached| {
            cached.timestamp + self.config.validation_ttl > current_time
        });
        self.metrics.set_cache_size(cache.len());
    }

    pub async fn get_cache_stats(&self) -> CacheStats {
        let cache = self.cache.read().await;
        CacheStats {
            size: cache.len(),
            hit_rate: self.metrics.cache_hits.get() as f64 /
                (self.metrics.cache_hits.get() + self.metrics.cache_misses.get()) as f64,
            average_score: cache.iter()
                .map(|(_, v)| v.score)
                .sum::<f64>() / cache.len() as f64,
            validation_types: cache.iter()
                .map(|(_, v)| v.validation_type)
                .fold(HashMap::new(), |mut acc, vt| {
                    *acc.entry(vt).or_insert(0) += 1;
                    acc
                }),
        }
    }
}

#[derive(Debug, Clone)]
pub struct CacheStats {
    pub size: usize,
    pub hit_rate: f64,
    pub average_score: f64,
    pub validation_types: HashMap<ValidationType, usize>,
}
