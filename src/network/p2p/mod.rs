//! # Network Abstraction Layer
//!
//! This module provides an abstraction over the peer-to-peer networking implementation,
//! allowing for easier upgrades of the underlying transport layer (currently libp2p)
//! without affecting the rest of the application.
//!
//! ## Security Rationale
//!
//! This abstraction was introduced specifically to address security vulnerabilities
//! in transitive dependencies (particularly `ring` via libp2p). By isolating the
//! p2p implementation details, we can:
//!
//! 1. More easily upgrade libp2p in the future to fix vulnerabilities
//! 2. Contain the impact of API changes to a smaller part of the codebase
//! 3. Potentially switch to different networking implementations if needed
//!
//! The specific vulnerabilities addressed include:
//! - RUSTSEC-2025-0009: ring 0.16.20 - AES functions may panic when overflow checking is enabled
//! - RUSTSEC-2025-0010: ring 0.16.20 - Unmaintained
//! - RUSTSEC-2024-0436: paste 1.0.15 - Unmaintained (used by libp2p's network stack)

mod errors;
mod legacy_adapter;
mod libp2p_impl;
mod types;

pub use errors::NetworkError;
pub use legacy_adapter::LegacyNetworkAdapter;
pub use libp2p_impl::LibP2PAdapter;
pub use types::{ConnectionStatus, MessagePayload, MessageType, PeerInfo};

/// Main network adapter trait that abstracts away implementation details.
/// This trait defines the core networking functionality required by the application.
#[async_trait::async_trait]
pub trait NetworkAdapter: Send + Sync + 'static {
    /// Initialize the network adapter
    async fn initialize(&self) -> Result<(), NetworkError>;

    /// Start listening for incoming connections
    async fn start(&self) -> Result<(), NetworkError>;

    /// Stop the network adapter and clean up resources
    async fn stop(&self) -> Result<(), NetworkError>;

    /// Connect to a specific peer
    async fn connect_to_peer(&self, peer_id: &str, addr: &str) -> Result<(), NetworkError>;

    /// Disconnect from a specific peer
    async fn disconnect_peer(&self, peer_id: &str) -> Result<(), NetworkError>;

    /// Send a message to a specific peer
    async fn send_message(
        &self,
        peer_id: &str,
        payload: MessagePayload,
    ) -> Result<(), NetworkError>;

    /// Get pending messages from the network
    async fn get_pending_messages(&self) -> Result<Vec<(String, MessagePayload)>, NetworkError>;

    /// Check the health of all connected peers
    async fn check_peer_health(&self) -> Result<(), NetworkError>;

    /// Broadcast a message to all connected peers
    async fn broadcast_message(&self, payload: MessagePayload) -> Result<(), NetworkError>;

    /// Get a list of all connected peers
    async fn get_connected_peers(&self) -> Result<Vec<PeerInfo>, NetworkError>;

    /// Check if a peer is connected
    async fn is_peer_connected(&self, peer_id: &str) -> Result<bool, NetworkError>;

    /// Get statistics about the network
    async fn get_network_stats(&self) -> Result<NetworkStats, NetworkError>;

    /// Test connectivity to a specific peer
    async fn ping_peer(&self, peer_addr: &str) -> Result<bool, NetworkError>;

    /// Shutdown the network adapter
    async fn shutdown(&self) -> Result<(), NetworkError>;
}

/// Network statistics
#[derive(Debug, Clone)]
pub struct NetworkStats {
    /// Number of connected peers
    pub connected_peers: usize,

    /// Number of pending connections
    pub pending_connections: usize,

    /// Total messages sent
    pub messages_sent: u64,

    /// Total messages received
    pub messages_received: u64,

    /// Total bytes sent
    pub bytes_sent: u64,

    /// Total bytes received
    pub bytes_received: u64,

    /// Average latency to peers in milliseconds
    pub average_latency_ms: f64,
}

/// Factory function to create a new network adapter
pub fn create_network_adapter(config: NetworkConfig) -> Box<dyn NetworkAdapter> {
    if let Some(adapter_type) = &config.adapter_type {
        match adapter_type.as_str() {
            "legacy" => match LegacyNetworkAdapter::new(config.clone()) {
                Ok(adapter) => return Box::new(adapter),
                Err(e) => {
                    tracing::error!(
                        "Failed to create legacy network adapter: {}, falling back to libp2p",
                        e
                    );
                }
            },
            _ => {}
        }
    }

    // Default to libp2p adapter
    Box::new(LibP2PAdapter::new(config))
}

/// Network configuration
#[derive(Debug, Clone)]
pub struct NetworkConfig {
    /// Local listen address
    pub listen_address: String,

    /// Maximum number of concurrent connections
    pub max_connections: usize,

    /// Connection timeout in milliseconds
    pub connection_timeout_ms: u64,

    /// Maximum message size in bytes
    pub max_message_size: usize,

    /// TLS configuration (if applicable)
    pub tls_config: Option<TlsConfig>,

    /// Bootstrap peers to connect to on startup
    pub bootstrap_peers: Vec<String>,

    /// Network adapter type to use ("libp2p" or "legacy")
    pub adapter_type: Option<String>,
}

/// TLS configuration
#[derive(Debug, Clone)]
pub struct TlsConfig {
    /// Path to the certificate file
    pub cert_path: String,

    /// Path to the private key file
    pub key_path: String,

    /// Whether to verify peer certificates
    pub verify_peers: bool,
}
