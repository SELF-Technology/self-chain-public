use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::RwLock;
use libp2p::PeerId;
use prometheus::{Histogram, HistogramOpts, register_histogram};
use anyhow::Result;

#[derive(Debug, Clone)]
pub struct RateLimitConfig {
    pub max_requests_per_second: u32,
    pub max_burst: u32,
    pub peer_window: Duration,
    pub global_window: Duration,
    pub peer_limit: u32,
    pub global_limit: u32,
    pub retry_after: Duration,
}

pub struct PeerValidationRateLimiter {
    peer_limits: Arc<RwLock<HashMap<PeerId, PeerRateLimit>>>,
    global_limit: Arc<GlobalRateLimit>,
    config: RateLimitConfig,
    metrics: Arc<RateLimitMetrics>,
}

struct PeerRateLimit {
    last_request: SystemTime,
    request_count: u32,
    window_start: SystemTime,
    window_duration: Duration,
}

struct GlobalRateLimit {
    last_request: SystemTime,
    request_count: u32,
    window_start: SystemTime,
    window_duration: Duration,
}

struct RateLimitMetrics {
    peer_requests: Histogram,
    global_requests: Histogram,
    peer_rejections: Histogram,
    global_rejections: Histogram,
}

impl RateLimitMetrics {
    pub fn new() -> Result<Arc<Self>> {
        let opts = HistogramOpts::new("peer_validation_peer_requests", "Peer validation request distribution");
        let peer_requests = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("peer_validation_global_requests", "Global validation request distribution");
        let global_requests = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("peer_validation_peer_rejections", "Peer validation request rejection distribution");
        let peer_rejections = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("peer_validation_global_rejections", "Global validation request rejection distribution");
        let global_rejections = register_histogram!(opts.clone())?;
        
        Ok(Arc::new(Self {
            peer_requests,
            global_requests,
            peer_rejections,
            global_rejections,
        }))
    }
}

impl PeerValidationRateLimiter {
    pub fn new(config: RateLimitConfig) -> Result<Self> {
        let metrics = RateLimitMetrics::new()?;
        Ok(Self {
            peer_limits: Arc::new(RwLock::new(HashMap::new())),
            global_limit: Arc::new(GlobalRateLimit {
                last_request: SystemTime::now(),
                request_count: 0,
                window_start: SystemTime::now(),
                window_duration: config.global_window,
            }),
            config,
            metrics,
        })
    }

    pub async fn check_rate_limit(&self, peer_id: &PeerId) -> Result<(), RateLimitError> {
        // Check global rate limit
        if !self.check_global_limit().await {
            self.metrics.global_rejections.observe(1.0);
            return Err(RateLimitError::GlobalRateLimitExceeded {
                retry_after: self.config.retry_after,
            });
        }

        // Check peer-specific rate limit
        let mut peer_limits = self.peer_limits.write().await;
        let now = SystemTime::now();

        let peer_limit = peer_limits.entry(peer_id.clone()).or_insert(PeerRateLimit {
            last_request: now,
            request_count: 0,
            window_start: now,
            window_duration: self.config.peer_window,
        });

        // Check if we need to reset the window
        if now.duration_since(peer_limit.window_start)? > peer_limit.window_duration {
            peer_limit.window_start = now;
            peer_limit.request_count = 0;
        }

        // Check if we exceed the peer limit
        if peer_limit.request_count >= self.config.peer_limit {
            self.metrics.peer_rejections.observe(1.0);
            return Err(RateLimitError::PeerRateLimitExceeded {
                peer_id: peer_id.clone(),
                retry_after: self.config.retry_after,
            });
        }

        // Update metrics
        self.metrics.peer_requests.observe(1.0);
        self.metrics.global_requests.observe(1.0);

        // Update request counts
        peer_limit.request_count += 1;
        peer_limit.last_request = now;

        Ok(())
    }

    async fn check_global_limit(&self) -> bool {
        let now = SystemTime::now();
        let mut global_limit = self.global_limit.clone();

        // Check if we need to reset the window
        if now.duration_since(global_limit.window_start).unwrap_or(Duration::from_secs(0)) > global_limit.window_duration {
            global_limit.window_start = now;
            global_limit.request_count = 0;
        }

        // Check if we exceed the global limit
        if global_limit.request_count >= self.config.global_limit {
            return false;
        }

        // Update request count
        global_limit.request_count += 1;
        global_limit.last_request = now;
        true
    }

    pub async fn get_stats(&self) -> RateLimitStats {
        let peer_limits = self.peer_limits.read().await;
        let global_limit = self.global_limit.clone();

        RateLimitStats {
            peer_count: peer_limits.len(),
            active_peers: peer_limits.iter()
                .filter(|(_, limit)| limit.request_count > 0)
                .count(),
            peer_request_counts: peer_limits.iter()
                .map(|(_, limit)| limit.request_count)
                .collect(),
            global_request_count: global_limit.request_count,
            global_window_start: global_limit.window_start,
            global_window_duration: global_limit.window_duration,
        }
    }
}

#[derive(Debug, thiserror::Error)]
pub enum RateLimitError {
    #[error("Global rate limit exceeded. Retry after {retry_after:?}")]
    GlobalRateLimitExceeded {
        retry_after: Duration,
    },
    #[error("Peer rate limit exceeded for peer {peer_id}. Retry after {retry_after:?}")]
    PeerRateLimitExceeded {
        peer_id: PeerId,
        retry_after: Duration,
    },
}

#[derive(Debug, Clone)]
pub struct RateLimitStats {
    pub peer_count: usize,
    pub active_peers: usize,
    pub peer_request_counts: Vec<u32>,
    pub global_request_count: u32,
    pub global_window_start: SystemTime,
    pub global_window_duration: Duration,
}
