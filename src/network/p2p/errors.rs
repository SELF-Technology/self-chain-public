//! Error types for the network abstraction layer

use std::io;
use thiserror::Error;

/// Network-related errors
#[derive(Error, Debug)]
pub enum NetworkError {
    /// Failed to connect to peer
    #[error("Connection failed: {0}")]
    ConnectionFailed(String),

    /// Connection timed out
    #[error("Connection timed out: {0}")]
    ConnectionTimeout(String),

    /// Failed to send message
    #[error("Failed to send message: {0}")]
    SendFailed(String),

    /// Failed to receive message
    #[error("Failed to receive message: {0}")]
    ReceiveFailed(String),

    /// Message too large
    #[error("Message exceeds maximum size: {size} > {max_size}")]
    MessageTooLarge { size: usize, max_size: usize },

    /// Failed to serialize message
    #[error("Failed to serialize message: {0}")]
    SerializationError(String),

    /// Failed to deserialize message
    #[error("Failed to deserialize message: {0}")]
    DeserializationError(String),

    /// Peer not found
    #[error("Peer not found: {0}")]
    PeerNotFound(String),

    /// Invalid peer ID
    #[error("Invalid peer ID: {0}")]
    InvalidPeerId(String),

    /// TLS error
    #[error("TLS error: {0}")]
    TlsError(String),

    /// Certificate error
    #[error("Certificate error: {0}")]
    CertificateError(String),

    /// Transport error
    #[error("Transport error: {0}")]
    TransportError(String),

    /// Too many connections
    #[error("Too many connections: {current} >= {max}")]
    TooManyConnections { current: usize, max: usize },

    /// Resource error
    #[error("Resource error: {0}")]
    ResourceError(String),

    /// Not initialized
    #[error("Network adapter not initialized")]
    NotInitialized,

    /// Already initialized
    #[error("Network adapter already initialized")]
    AlreadyInitialized,

    /// I/O error
    #[error("I/O error: {0}")]
    IoError(#[from] io::Error),

    /// Underlying libp2p error
    #[error("libp2p error: {0}")]
    LibP2pError(String),

    /// Unknown error
    #[error("Unknown error: {0}")]
    Unknown(String),
}

impl From<String> for NetworkError {
    fn from(error: String) -> Self {
        NetworkError::Unknown(error)
    }
}

impl From<&str> for NetworkError {
    fn from(error: &str) -> Self {
        NetworkError::Unknown(error.to_string())
    }
}

impl From<NetworkError> for std::io::Error {
    fn from(error: NetworkError) -> Self {
        std::io::Error::new(std::io::ErrorKind::Other, error.to_string())
    }
}

/// Utility result type for network operations
#[allow(dead_code)]
pub type NetworkResult<T> = Result<T, NetworkError>;
