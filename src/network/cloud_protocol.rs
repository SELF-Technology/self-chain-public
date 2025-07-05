use crate::blockchain::{Block, Transaction};
use crate::core::config::StorageConfig;
use crate::network::reputation::ReputationManager;
use libp2p::PeerId;
use serde::{Deserialize, Serialize};
use std::collections::{HashMap, HashSet};
use std::sync::Arc;
use std::time::{Duration, Instant};
use tokio::sync::RwLock;
use tracing::{debug, info, warn};

/// Cloud Node Communication Protocol for SELF Chain
///
/// This module implements the communication protocol for cloud nodes,
/// enabling secure, reliable communication between nodes in the network.

const CLOUD_PROTOCOL_VERSION: &str = "1.0.0";
#[allow(dead_code)]
const HEARTBEAT_INTERVAL_SEC: u64 = 30;
const DEFAULT_TTL_SEC: u64 = 300; // 5 minutes

/// Message types for the cloud communication protocol
#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum NodeMessage {
    Block(Block),
    Transaction(Transaction),
    GetBlocks,
    BlockResponse(Vec<Block>),

    Heartbeat,
    JoinNetwork,
    LeaveNetwork,
    PeerList(Vec<String>),

    ValidationRequest(String),
    ValidationResponse(bool),

    StorageRequest(StorageRequestType),
    StorageResponse(StorageResponseType),

    Error(NetworkError),
    HealthReport(HealthStatus),
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum StorageRequestType {
    Get(String),
    Put(String, Vec<u8>),
    Delete(String),
    List(String),
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum StorageResponseType {
    Value(String, Vec<u8>),
    Success(String),
    Keys(Vec<String>),
    Error(String),
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum NetworkError {
    ConnectionFailed,
    Timeout,
    ValidationFailed,
    StorageError(String),
    ProtocolError(String),
    SecurityError,
    InternalError(String),
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum HealthStatus {
    Healthy,
    Warning,
    Critical,
    Offline,
}

/// Circuit breaker pattern implementation for error handling
#[derive(Debug, Clone)]
pub struct CircuitBreaker {
    failures: u32,
    threshold: u32,
    reset_after: Duration,
    last_failure: Option<Instant>,
    open: bool,
}

impl CircuitBreaker {
    pub fn new(threshold: u32, reset_after: Duration) -> Self {
        Self {
            failures: 0,
            threshold,
            reset_after,
            last_failure: None,
            open: false,
        }
    }

    pub fn record_failure(&mut self) {
        self.failures += 1;
        self.last_failure = Some(Instant::now());

        if self.failures >= self.threshold {
            self.open = true;
        }
    }

    pub fn is_open(&self) -> bool {
        if !self.open {
            return false;
        }

        if let Some(last_failure) = self.last_failure {
            if last_failure.elapsed() > self.reset_after {
                return false;
            }
        }

        self.open
    }

    pub fn reset(&mut self) {
        self.failures = 0;
        self.open = false;
    }
}

/// Message priority levels for the cloud communication protocol
#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Serialize, Deserialize)]
pub enum MessagePriority {
    Low = 0,
    Medium = 1,
    High = 2,
    Critical = 3,
}

/// Message envelope containing metadata for routing and handling
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MessageEnvelope {
    pub message_id: String,
    pub source_node_id: String,
    pub target_node_id: Option<String>, // None for broadcast
    pub timestamp: u64,
    pub ttl: u64,
    pub priority: MessagePriority,
    pub is_encrypted: bool,
    pub payload: NodeMessage,
    pub signature: Option<Vec<u8>>,
}

impl MessageEnvelope {
    pub fn new(
        source_node_id: String,
        target_node_id: Option<String>,
        priority: MessagePriority,
        payload: NodeMessage,
    ) -> Self {
        let message_id = uuid::Uuid::new_v4().to_string();
        let now = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_secs();

        Self {
            message_id,
            source_node_id,
            target_node_id,
            timestamp: now,
            ttl: DEFAULT_TTL_SEC,
            priority,
            is_encrypted: false,
            payload,
            signature: None,
        }
    }

    pub fn is_expired(&self) -> bool {
        let now = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_secs();

        now > self.timestamp + self.ttl
    }
}

/// Stats for cloud communication protocol metrics
#[derive(Debug, Default, Clone)]
pub struct CloudCommunicationStats {
    pub messages_sent: u64,
    pub messages_received: u64,
    pub messages_dropped: u64,
    pub messages_expired: u64,
    pub connection_errors: u64,
    pub retry_attempts: u64,
    pub successful_retries: u64,
    pub last_heartbeat: Option<Instant>,
    pub average_latency_ms: f64,
    pub pending_requests: u64,
}

/// Information about connected cloud peers
#[derive(Debug, Clone)]
pub struct CloudPeerInfo {
    pub peer_id: PeerId,
    pub node_id: String,
    pub addresses: Vec<String>,
    pub connected_since: Instant,
    pub last_seen: Instant,
    pub health_status: HealthStatus,
    pub latency_ms: Vec<u64>,
    pub success_count: u64,
    pub failure_count: u64,
    pub capabilities: HashSet<String>,
    pub version: String,
}

/// Cloud node information
#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct CloudNode {
    pub id: String,
    pub address: String,
    pub last_seen: u64,
    pub capacity: u64,
    pub used: u64,
}

/// The main cloud node communicator
#[derive(Debug, Clone)]
pub struct CloudNodeCommunicator {
    node_id: String,
    #[allow(dead_code)]
    storage_config: StorageConfig,
    #[allow(dead_code)]
    reputation_manager: Arc<ReputationManager>,
    connected_peers: Arc<RwLock<HashMap<PeerId, CloudPeerInfo>>>,
    message_queue: Arc<RwLock<Vec<MessageEnvelope>>>,
    circuit_breaker: Arc<RwLock<CircuitBreaker>>,
    stats: Arc<RwLock<CloudCommunicationStats>>,
}

impl CloudNodeCommunicator {
    /// Create a new cloud node communicator
    pub fn new(
        node_id: String,
        storage_config: StorageConfig,
        reputation_manager: Arc<ReputationManager>,
    ) -> Self {
        Self {
            node_id,
            storage_config,
            reputation_manager,
            connected_peers: Arc::new(RwLock::new(HashMap::new())),
            message_queue: Arc::new(RwLock::new(Vec::new())),
            circuit_breaker: Arc::new(RwLock::new(CircuitBreaker::new(5, Duration::from_secs(60)))),
            stats: Arc::new(RwLock::new(CloudCommunicationStats::default())),
        }
    }

    /// Add a new peer to the network
    pub async fn add_peer(&self, peer_id: PeerId, node_id: String) {
        let mut peers = self.connected_peers.write().await;

        // Skip if peer already exists
        if peers.contains_key(&peer_id) {
            return;
        }

        // Add new peer
        peers.insert(
            peer_id,
            CloudPeerInfo {
                peer_id,
                node_id,
                addresses: Vec::new(),
                connected_since: Instant::now(),
                last_seen: Instant::now(),
                health_status: HealthStatus::Healthy,
                latency_ms: Vec::new(),
                success_count: 0,
                failure_count: 0,
                capabilities: HashSet::new(),
                version: CLOUD_PROTOCOL_VERSION.to_string(),
            },
        );

        debug!("Added new peer: {}", peer_id);
    }

    /// Remove a peer from the network
    pub async fn remove_peer(&self, peer_id: &PeerId) {
        let mut peers = self.connected_peers.write().await;
        if peers.remove(peer_id).is_some() {
            info!("Removed peer: {}", peer_id);
        }
    }

    /// Update a peer's health status
    pub async fn update_peer_health(&self, peer_id: &PeerId, status: HealthStatus) {
        let mut peers = self.connected_peers.write().await;
        if let Some(peer) = peers.get_mut(peer_id) {
            peer.health_status = status;
            peer.last_seen = Instant::now();
        }
    }

    /// Record a successful message for a peer
    pub async fn record_success(&self, peer_id: &PeerId) {
        let mut peers = self.connected_peers.write().await;
        if let Some(peer) = peers.get_mut(peer_id) {
            peer.success_count += 1;
            peer.last_seen = Instant::now();
        }
    }

    /// Record a failed message for a peer
    pub async fn record_failure(&self, peer_id: &PeerId) {
        let mut peers = self.connected_peers.write().await;
        if let Some(peer) = peers.get_mut(peer_id) {
            peer.failure_count += 1;
            peer.last_seen = Instant::now();
        }
    }

    /// Send a message to a peer
    pub async fn send_message(
        &self,
        target_node_id: Option<String>,
        priority: MessagePriority,
        payload: NodeMessage,
    ) -> Result<(), String> {
        // Create the message envelope
        let envelope =
            MessageEnvelope::new(self.node_id.clone(), target_node_id, priority, payload);

        // Add to message queue
        let mut queue = self.message_queue.write().await;
        queue.push(envelope);

        // Update stats
        let mut stats = self.stats.write().await;
        stats.messages_sent += 1;

        Ok(())
    }

    /// Broadcast a message to all peers
    pub async fn broadcast(
        &self,
        priority: MessagePriority,
        payload: NodeMessage,
    ) -> Result<(), String> {
        self.send_message(None, priority, payload).await
    }

    /// Send a direct message to a specific peer
    pub async fn send_direct(
        &self,
        target_node_id: String,
        priority: MessagePriority,
        payload: NodeMessage,
    ) -> Result<(), String> {
        self.send_message(Some(target_node_id), priority, payload)
            .await
    }

    /// Process incoming messages
    pub async fn process_messages(&self) -> Result<(), String> {
        // Process the message queue
        let mut queue = self.message_queue.write().await;
        let messages = std::mem::take(&mut *queue);

        for message in messages {
            if message.is_expired() {
                let mut stats = self.stats.write().await;
                stats.messages_expired += 1;
                continue;
            }

            // Process based on target
            if let Some(target) = &message.target_node_id {
                if target == &self.node_id {
                    self.handle_message(message).await?;
                } else {
                    // Not for us, re-queue for forwarding
                    queue.push(message);
                }
            } else {
                // Broadcast message, process it
                self.handle_message(message).await?;
            }
        }

        Ok(())
    }

    /// Handle a specific message
    async fn handle_message(&self, envelope: MessageEnvelope) -> Result<(), String> {
        debug!("Handling message: {:?}", envelope.payload);

        // Update stats
        let mut stats = self.stats.write().await;
        stats.messages_received += 1;
        drop(stats);

        // Process based on message type
        match envelope.payload {
            NodeMessage::Heartbeat => {
                // Process heartbeat
                if let Some(source_peer_id) =
                    self.find_peer_by_node_id(&envelope.source_node_id).await
                {
                    self.update_peer_health(&source_peer_id, HealthStatus::Healthy)
                        .await;
                }
            }
            NodeMessage::JoinNetwork => {
                // Process join request
                info!("Node {} joined the network", envelope.source_node_id);
                // Would add logic to share network state with the new node
            }
            NodeMessage::LeaveNetwork => {
                // Process leave notification
                if let Some(peer_id) = self.find_peer_by_node_id(&envelope.source_node_id).await {
                    self.remove_peer(&peer_id).await;
                }
            }
            NodeMessage::StorageRequest(request) => {
                // Process storage request
                self.handle_storage_request(envelope.source_node_id, request)
                    .await?;
            }
            NodeMessage::Error(error) => {
                // Process error message
                warn!(
                    "Received error from {}: {:?}",
                    envelope.source_node_id, error
                );
                if let Some(peer_id) = self.find_peer_by_node_id(&envelope.source_node_id).await {
                    self.record_failure(&peer_id).await;
                }
            }
            // Handle other message types as needed
            _ => {
                debug!("Unhandled message type: {:?}", envelope.payload);
            }
        }

        Ok(())
    }

    /// Handle storage requests
    async fn handle_storage_request(
        &self,
        source_node_id: String,
        request: StorageRequestType,
    ) -> Result<(), String> {
        // In a real implementation, this would interact with the storage layer
        match request {
            StorageRequestType::Get(key) => {
                debug!("Storage GET request for key: {}", key);
                // Would retrieve data and send response back
            }
            StorageRequestType::Put(key, _value) => {
                debug!("Storage PUT request for key: {}", key);
                // Would store data and send confirmation
            }
            StorageRequestType::Delete(key) => {
                debug!("Storage DELETE request for key: {}", key);
                // Would delete data and send confirmation
            }
            StorageRequestType::List(prefix) => {
                debug!("Storage LIST request with prefix: {}", prefix);
                // Would list keys and send back
            }
        }

        // For now, just record a success
        if let Some(peer_id) = self.find_peer_by_node_id(&source_node_id).await {
            self.record_success(&peer_id).await;
        }

        Ok(())
    }

    /// Find a peer_id by node_id
    async fn find_peer_by_node_id(&self, node_id: &str) -> Option<PeerId> {
        let peers = self.connected_peers.read().await;
        peers
            .iter()
            .find(|(_, info)| info.node_id == node_id)
            .map(|(peer_id, _)| *peer_id)
    }

    /// Get all connected peers
    pub async fn get_connected_peers(&self) -> Vec<CloudPeerInfo> {
        let peers = self.connected_peers.read().await;
        peers.values().cloned().collect()
    }

    /// Get communication statistics
    pub async fn get_stats(&self) -> CloudCommunicationStats {
        self.stats.read().await.clone()
    }

    /// Clean up expired messages and update peer status
    pub async fn maintain(&self) {
        // Clean up expired messages
        let mut queue = self.message_queue.write().await;
        let before_len = queue.len();
        queue.retain(|msg| !msg.is_expired());
        let expired = before_len - queue.len();

        if expired > 0 {
            let mut stats = self.stats.write().await;
            stats.messages_expired += expired as u64;
        }

        // Update peer status based on last seen time
        let mut peers = self.connected_peers.write().await;
        let mut peers_to_remove = Vec::new();

        for (peer_id, info) in peers.iter_mut() {
            let last_seen_duration = info.last_seen.elapsed();

            // Mark as offline if not seen for 5 minutes
            if last_seen_duration > Duration::from_secs(300) {
                info.health_status = HealthStatus::Offline;

                // Add to removal list if not seen for 15 minutes
                if last_seen_duration > Duration::from_secs(900) {
                    peers_to_remove.push(*peer_id);
                }
            } else if last_seen_duration > Duration::from_secs(120) {
                // Mark as warning if not seen for 2 minutes
                info.health_status = HealthStatus::Warning;
            }
        }

        // Remove offline peers
        for peer_id in &peers_to_remove {
            peers.remove(peer_id);
        }

        if !peers_to_remove.is_empty() {
            debug!("Removed {} offline peers", peers_to_remove.len());
        }
    }

    /// Recover from network error
    pub async fn recover_from_error(&self, error: NetworkError) -> Result<(), String> {
        let mut circuit_breaker = self.circuit_breaker.write().await;

        // Record failure
        circuit_breaker.record_failure();

        // Check if circuit breaker is open
        if circuit_breaker.is_open() {
            warn!("Circuit breaker is open, entering recovery mode");

            match error {
                NetworkError::ConnectionFailed => {
                    // Clear peer list and reconnect
                    let mut peers = self.connected_peers.write().await;
                    peers.clear();
                }
                NetworkError::Timeout => {
                    // No specific recovery needed
                }
                NetworkError::ValidationFailed => {
                    // Reset validation state
                }
                NetworkError::StorageError(_) => {
                    // Repair storage connections
                }
                NetworkError::ProtocolError(_) => {
                    // Reset protocol state
                }
                NetworkError::SecurityError => {
                    // Reset security state
                }
                NetworkError::InternalError(_) => {
                    // Log and continue
                }
            }

            // Reset circuit breaker after recovery
            circuit_breaker.reset();
        }

        Ok(())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_message_envelope() {
        let envelope = MessageEnvelope::new(
            "node1".to_string(),
            Some("node2".to_string()),
            MessagePriority::High,
            NodeMessage::Heartbeat,
        );

        assert_eq!(envelope.source_node_id, "node1");
        assert_eq!(envelope.target_node_id, Some("node2".to_string()));
        assert_eq!(envelope.priority, MessagePriority::High);
        assert!(!envelope.is_expired());
    }

    #[test]
    fn test_circuit_breaker() {
        let mut cb = CircuitBreaker::new(3, Duration::from_secs(60));

        assert!(!cb.is_open());

        cb.record_failure();
        cb.record_failure();
        assert!(!cb.is_open());

        cb.record_failure();
        assert!(cb.is_open());

        cb.reset();
        assert!(!cb.is_open());
    }
}
