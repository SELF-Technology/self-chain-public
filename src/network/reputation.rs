use crate::consensus::metrics::ConsensusMetrics;
use anyhow::Result;
use libp2p::PeerId;
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::net::SocketAddr;
use std::sync::Arc;
use std::time::{SystemTime, UNIX_EPOCH};
use tokio::sync::RwLock;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PeerStats {
    pub address: SocketAddr,
    pub last_seen: u64,
    pub uptime: u64,
    pub validation_score: u64,
    pub message_latency: u64,    // Average message latency in ms
    pub validation_latency: u64, // Average validation latency in ms
    pub error_count: u64,
    pub validation_count: u64,
    pub success_rate: f64,
    pub reputation_score: f64,
}

impl PeerStats {
    pub fn error_rate(&self) -> f64 {
        if self.validation_count + self.error_count == 0 {
            0.0
        } else {
            self.error_count as f64 / (self.validation_count + self.error_count) as f64
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ReputationConfig {
    pub min_reputation: f64,
    pub max_error_rate: f64,
    pub min_success_rate: f64,
    pub min_uptime: u64,
    pub validation_window: u64,
}

#[derive(Debug)]
pub struct PeerReputation {
    peers: Arc<RwLock<HashMap<String, PeerStats>>>,
    config: ReputationConfig,
    metrics: Arc<ConsensusMetrics>,
}

impl PeerReputation {
    pub fn new(metrics: Arc<ConsensusMetrics>) -> Self {
        Self {
            peers: Arc::new(RwLock::new(HashMap::new())),
            config: ReputationConfig {
                min_reputation: 0.7,     // Minimum reputation score
                max_error_rate: 0.1,     // Maximum error rate
                min_success_rate: 0.9,   // Minimum success rate
                min_uptime: 3600,        // Minimum 1 hour uptime
                validation_window: 3600, // 1 hour window
            },
            metrics,
        }
    }

    pub async fn add_peer(&self, peer_id: &str, address: SocketAddr) -> Result<()> {
        let mut peers = self.peers.write().await;
        let current_time = SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs();

        peers.insert(
            peer_id.to_string(),
            PeerStats {
                address,
                last_seen: current_time,
                uptime: 0,
                validation_score: 0,
                message_latency: 0,
                validation_latency: 0,
                error_count: 0,
                validation_count: 0,
                success_rate: 1.0,
                reputation_score: 1.0,
            },
        );

        Ok(())
    }

    pub async fn update_peer_stats(&self, peer_id: &str, stats: PeerStats) -> Result<()> {
        let mut peers = self.peers.write().await;
        if let Some(peer) = peers.get_mut(peer_id) {
            // Extract values before moving stats
            let validation_score = stats.validation_score;
            let uptime = stats.uptime;

            *peer = stats;
            self.metrics
                .set_validator_efficiency(validation_score as i64);
            self.metrics.set_validator_uptime(uptime as i64);
        }
        Ok(())
    }

    pub async fn get_peer_stats(&self, peer_id: &str) -> Result<Option<PeerStats>> {
        let peers = self.peers.read().await;
        Ok(peers.get(peer_id).cloned())
    }

    pub async fn update_latency(
        &self,
        peer_id: &str,
        latency: u64,
        is_validation: bool,
    ) -> Result<()> {
        let mut peers = self.peers.write().await;
        if let Some(peer) = peers.get_mut(peer_id) {
            if is_validation {
                peer.validation_latency = (peer.validation_latency + latency) / 2;
            } else {
                peer.message_latency = (peer.message_latency + latency) / 2;
            }
        }
        Ok(())
    }

    pub async fn record_error(&self, peer_id: &str) -> Result<()> {
        let mut peers = self.peers.write().await;
        if let Some(peer) = peers.get_mut(peer_id) {
            peer.error_count += 1;
            peer.success_rate =
                peer.validation_count as f64 / (peer.validation_count + peer.error_count) as f64;
            self.update_reputation(peer_id).await?;
        }
        Ok(())
    }

    pub async fn record_success(&self, peer_id: &str) -> Result<()> {
        let mut peers = self.peers.write().await;
        if let Some(peer) = peers.get_mut(peer_id) {
            peer.validation_count += 1;
            peer.success_rate =
                peer.validation_count as f64 / (peer.validation_count + peer.error_count) as f64;
            self.update_reputation(peer_id).await?;
        }
        Ok(())
    }

    pub async fn record_activity(&self, peer_addr: SocketAddr) -> Result<()> {
        let peer_id = format!("{}", peer_addr);
        let mut peers = self.peers.write().await;
        if let Some(peer) = peers.get_mut(&peer_id) {
            peer.last_seen = SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs();
        } else {
            // Add new peer if not exists
            let new_peer = PeerStats {
                address: peer_addr,
                last_seen: SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs(),
                uptime: 0,
                validation_score: 0,
                message_latency: 0,
                validation_latency: 0,
                error_count: 0,
                validation_count: 0,
                success_rate: 1.0,
                reputation_score: 1.0,
            };
            peers.insert(peer_id, new_peer);
        }
        Ok(())
    }

    async fn update_reputation(&self, peer_id: &str) -> Result<()> {
        let peers = self.peers.read().await;
        if let Some(peer) = peers.get(peer_id) {
            let uptime_score = peer.uptime as f64 / self.config.min_uptime as f64;
            let success_score = peer.success_rate;
            let error_score = 1.0 - (peer.error_count as f64 / peer.validation_count as f64);

            let new_score = (uptime_score + success_score + error_score) / 3.0;

            let mut peers = self.peers.write().await;
            if let Some(peer) = peers.get_mut(peer_id) {
                peer.reputation_score = new_score;
                self.metrics.observe_voting_participation_rate(new_score);
            }
        }
        Ok(())
    }

    pub async fn is_peer_reliable(&self, peer_id: &str) -> Result<bool> {
        let peers = self.peers.read().await;
        if let Some(peer) = peers.get(peer_id) {
            return Ok(peer.reputation_score >= self.config.min_reputation
                && peer.error_rate() <= self.config.max_error_rate
                && peer.success_rate >= self.config.min_success_rate
                && peer.uptime >= self.config.min_uptime);
        }
        Ok(false)
    }

    pub async fn get_reliable_peers(&self) -> Result<Vec<(String, PeerStats)>> {
        let peers = self.peers.read().await;
        let mut reliable_peers = Vec::new();

        for (id, stats) in peers.iter() {
            if self.is_peer_reliable(id).await.unwrap_or(false) {
                reliable_peers.push((id.clone(), stats.clone()));
            }
        }

        Ok(reliable_peers)
    }

    pub async fn cleanup_peers(&self) -> Result<()> {
        let current_time = SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs();
        let mut peers = self.peers.write().await;

        peers.retain(|_, stats| stats.last_seen + self.config.validation_window >= current_time);

        Ok(())
    }
}

/// ReputationManager manages the reputation of peers in the cloud communication protocol
#[derive(Debug)]
pub struct ReputationManager {
    peer_data: Arc<RwLock<HashMap<PeerId, f64>>>,
}

impl ReputationManager {
    /// Create a new ReputationManager
    pub fn new() -> Self {
        Self {
            peer_data: Arc::new(RwLock::new(HashMap::new())),
        }
    }

    /// Get the reputation score for a peer
    pub async fn get_reputation(&self, peer_id: PeerId) -> f64 {
        let peer_data = self.peer_data.read().await;
        *peer_data.get(&peer_id).unwrap_or(&1.0)
    }

    /// Update the reputation score for a peer
    pub async fn update_reputation(&self, peer_id: PeerId, delta: i32) {
        let mut peer_data = self.peer_data.write().await;
        let current = peer_data.entry(peer_id).or_insert(1.0);

        // Apply delta (positive or negative)
        *current += delta as f64 * 0.1;

        // Clamp to range [0, 2.0]
        *current = current.max(0.0).min(2.0);
    }

    /// Check if a peer is trustworthy (reputation > threshold)
    pub async fn is_trustworthy(&self, peer_id: PeerId) -> bool {
        self.get_reputation(peer_id).await >= 0.5
    }

    /// Get all peer reputation data
    pub async fn get_all_reputations(&self) -> HashMap<PeerId, f64> {
        self.peer_data.read().await.clone()
    }

    /// Reset reputation for a peer
    pub async fn reset_reputation(&self, peer_id: PeerId) {
        let mut peer_data = self.peer_data.write().await;
        peer_data.insert(peer_id, 1.0);
    }

    /// Clean up old peer data
    pub async fn cleanup(&self) {
        // Remove peers with very low reputation
        let mut peer_data = self.peer_data.write().await;
        peer_data.retain(|_, reputation| *reputation > 0.1);
    }
}
