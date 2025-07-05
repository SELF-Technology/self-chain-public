//! Common type definitions for the network abstraction layer

use serde::{Deserialize, Serialize};
use std::net::SocketAddr;
use std::time::SystemTime;

/// Message payload type for all network communications
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MessagePayload {
    /// Message type identifier
    pub message_type: MessageType,

    /// Binary payload data
    pub data: Vec<u8>,

    /// Optional message ID for correlating requests and responses
    pub message_id: Option<String>,

    /// Optional TTL (time-to-live) for messages that should expire
    pub ttl: Option<u32>,

    /// Timestamp when the message was created
    pub timestamp: u64,
}

impl MessagePayload {
    /// Create a new message payload
    pub fn new(message_type: MessageType, data: Vec<u8>) -> Self {
        let now = SystemTime::now()
            .duration_since(SystemTime::UNIX_EPOCH)
            .unwrap_or_default()
            .as_secs();

        Self {
            message_type,
            data,
            message_id: None,
            ttl: None,
            timestamp: now,
        }
    }

    /// Set message ID
    pub fn with_id(mut self, id: impl Into<String>) -> Self {
        self.message_id = Some(id.into());
        self
    }

    /// Set time-to-live
    pub fn with_ttl(mut self, ttl: u32) -> Self {
        self.ttl = Some(ttl);
        self
    }

    /// Check if the message has expired based on its TTL
    pub fn is_expired(&self) -> bool {
        if let Some(ttl) = self.ttl {
            let now = SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap_or_default()
                .as_secs();

            // If more than TTL seconds have passed since message creation
            now > self.timestamp + ttl as u64
        } else {
            false
        }
    }
}

/// Message types for different kinds of network communications
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum MessageType {
    /// Block-related messages
    Block,

    /// Transaction-related messages
    Transaction,

    /// Consensus-related messages
    Consensus,

    /// Peer discovery and maintenance
    Discovery,

    /// Validation requests and responses
    Validation,

    /// Synchronization messages
    Sync,

    /// Peer connection management
    PeerConnection,

    /// Heartbeat/ping messages
    Heartbeat,

    /// Custom application-specific message
    Custom(u16),
}

/// Information about a peer in the network
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PeerInfo {
    /// Unique identifier for the peer
    pub peer_id: String,

    /// Network address of the peer
    pub address: SocketAddr,

    /// Current connection status
    pub status: ConnectionStatus,

    /// When the peer was last seen (UNIX timestamp)
    pub last_seen: u64,

    /// Protocol version the peer is using
    pub protocol_version: Option<String>,

    /// User agent or client identifier
    pub user_agent: Option<String>,

    /// Additional metadata about the peer
    pub metadata: std::collections::HashMap<String, String>,
}

impl PeerInfo {
    /// Create a new peer info
    pub fn new(peer_id: impl Into<String>, address: SocketAddr) -> Self {
        let now = SystemTime::now()
            .duration_since(SystemTime::UNIX_EPOCH)
            .unwrap_or_default()
            .as_secs();

        Self {
            peer_id: peer_id.into(),
            address,
            status: ConnectionStatus::Disconnected,
            last_seen: now,
            protocol_version: None,
            user_agent: None,
            metadata: std::collections::HashMap::new(),
        }
    }

    /// Update the last seen timestamp to now
    pub fn update_last_seen(&mut self) {
        self.last_seen = SystemTime::now()
            .duration_since(SystemTime::UNIX_EPOCH)
            .unwrap_or_default()
            .as_secs();
    }

    /// Add metadata to the peer info
    pub fn add_metadata(&mut self, key: impl Into<String>, value: impl Into<String>) {
        self.metadata.insert(key.into(), value.into());
    }
}

/// Connection status for peers
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum ConnectionStatus {
    /// Connected and ready for communication
    Connected,

    /// Not connected
    Disconnected,

    /// Connection in progress
    Connecting,

    /// Disconnection in progress
    Disconnecting,

    /// Connection failed
    Failed,

    /// Connection rejected
    Rejected,

    /// Connection banned
    Banned,
}
