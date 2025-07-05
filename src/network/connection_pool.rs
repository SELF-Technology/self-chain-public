use std::collections::HashMap;
use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::{RwLock, Semaphore};
use tokio::time::Instant;
use libp2p::{PeerId, Multiaddr};
use anyhow::Result;
use crate::network::metrics::PeerDiscoveryMetrics;

pub struct ConnectionPool {
    pools: Arc<RwLock<HashMap<PeerId, ConnectionPoolEntry>>>,
    metrics: Arc<PeerDiscoveryMetrics>,
    max_connections_per_peer: usize,
    connection_timeout: Duration,
}

#[derive(Debug)]
struct ConnectionPoolEntry {
    peer_id: PeerId,
    address: Multiaddr,
    semaphore: Semaphore,
    active_connections: usize,
    last_used: Instant,
    connection_stats: ConnectionStats,
}

#[derive(Debug, Default)]
struct ConnectionStats {
    successful_connections: usize,
    failed_connections: usize,
    total_latency: Duration,
    last_error: Option<anyhow::Error>,
}

impl ConnectionPool {
    pub fn new(
        metrics: Arc<PeerDiscoveryMetrics>,
        max_connections_per_peer: usize,
        connection_timeout: Duration,
    ) -> Self {
        Self {
            pools: Arc::new(RwLock::new(HashMap::new())),
            metrics,
            max_connections_per_peer,
            connection_timeout,
        }
    }

    pub async fn acquire_connection(&self, peer_id: &PeerId, address: &Multiaddr) -> Result<ConnectionPermit> {
        let mut pools = self.pools.write().await;
        
        let entry = pools.entry(peer_id.clone())
            .or_insert_with(|| ConnectionPoolEntry {
                peer_id: peer_id.clone(),
                address: address.clone(),
                semaphore: Semaphore::new(self.max_connections_per_peer),
                active_connections: 0,
                last_used: Instant::now(),
                connection_stats: Default::default(),
            });

        let permit = entry.semaphore.acquire().await?;
        entry.active_connections += 1;
        entry.last_used = Instant::now();

        Ok(ConnectionPermit {
            permit,
            pool: self.clone(),
            peer_id: peer_id.clone(),
        })
    }

    pub async fn release_connection(&self, peer_id: &PeerId, success: bool, latency: Duration) {
        let mut pools = self.pools.write().await;
        if let Some(entry) = pools.get_mut(peer_id) {
            entry.active_connections = entry.active_connections.saturating_sub(1);
            
            if success {
                entry.connection_stats.successful_connections += 1;
                entry.connection_stats.total_latency += latency;
            } else {
                entry.connection_stats.failed_connections += 1;
                entry.connection_stats.last_error = None; // Clear last error on failure
            }
            
            self.metrics.observe_peer_latency(latency.as_secs_f64());
            self.metrics.observe_peer_response_rate(
                entry.connection_stats.successful_connections as f64 /
                (entry.connection_stats.successful_connections + entry.connection_stats.failed_connections) as f64
            );
        }
    }

    pub async fn cleanup_inactive_connections(&self) {
        let mut pools = self.pools.write().await;
        let current_time = Instant::now();
        
        pools.retain(|_, entry| {
            if current_time.duration_since(entry.last_used) > self.connection_timeout {
                self.metrics.increment_peer_connection_errors();
                false
            } else {
                true
            }
        });
    }

    pub async fn get_peer_stats(&self, peer_id: &PeerId) -> Option<ConnectionStats> {
        let pools = self.pools.read().await;
        pools.get(peer_id).map(|entry| entry.connection_stats.clone())
    }
}

pub struct ConnectionPermit {
    permit: tokio::sync::OwnedSemaphorePermit,
    pool: Arc<ConnectionPool>,
    peer_id: PeerId,
}

impl ConnectionPermit {
    pub fn new(pool: Arc<ConnectionPool>, permit: tokio::sync::OwnedSemaphorePermit, peer_id: PeerId) -> Self {
        Self {
            permit,
            pool,
            peer_id,
        }
    }

    pub async fn release(self, success: bool, latency: Duration) {
        self.pool.release_connection(&self.peer_id, success, latency).await;
    }
}
