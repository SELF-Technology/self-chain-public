//! Adapter to bridge between the new NetworkAdapter trait and the legacy network implementation
//!
//! This adapter allows the existing network code to be used through the new
//! abstraction layer, enabling a gradual migration to the new architecture.

use std::collections::HashMap;
use std::net::SocketAddr;
use std::sync::Arc;

use anyhow::Result as AnyhowResult;
use async_trait::async_trait;
use tokio::sync::{mpsc, RwLock};
use tracing::{debug, error, info};

use crate::core::message_handler as legacy_message;
use crate::core::message_handler::NetworkMessage;
use crate::core::network_node::NetworkNode as LegacyNetwork;
use crate::network::p2p::{
    ConnectionStatus, MessagePayload, MessageType, NetworkAdapter, NetworkConfig, NetworkError,
    NetworkStats, PeerInfo,
};

/// Adapter implementing NetworkAdapter trait using the legacy network implementation
pub struct LegacyNetworkAdapter {
    /// Legacy network node
    network: Arc<RwLock<LegacyNetwork>>,

    /// Legacy message handler
    message_handler: Arc<RwLock<legacy_message::MessageHandler>>,

    /// Network configuration
    config: NetworkConfig,

    /// Connected peers
    peers: Arc<RwLock<HashMap<String, PeerInfo>>>,

    /// Message handlers
    message_handlers: Arc<
        RwLock<
            Vec<
                Box<
                    dyn Fn(PeerInfo, MessagePayload) -> Result<(), NetworkError>
                        + Send
                        + Sync
                        + 'static,
                >,
            >,
        >,
    >,

    /// Network statistics
    stats: Arc<RwLock<NetworkStats>>,

    /// Message channel for incoming messages
    message_receiver:
        Arc<RwLock<Option<mpsc::Receiver<(legacy_message::NetworkMessage, SocketAddr)>>>>,

    /// Message channel for outgoing messages
    message_sender: Arc<RwLock<Option<mpsc::Sender<(legacy_message::NetworkMessage, SocketAddr)>>>>,

    /// Initialized flag
    initialized: Arc<RwLock<bool>>,

    /// Running flag
    running: Arc<RwLock<bool>>,
}

impl LegacyNetworkAdapter {
    /// Create a new legacy network adapter
    pub fn new(config: NetworkConfig) -> AnyhowResult<Self> {
        // Create a new legacy network node
        let network_node = LegacyNetwork::new(config.listen_address.clone())?;
        let network = Arc::new(RwLock::new(network_node));

        // Create a new message handler
        let message_handler = legacy_message::MessageHandler::new(network.clone());

        // Create message channels
        let (tx, rx) = mpsc::channel(100);

        // Create initial stats
        let stats = NetworkStats {
            connected_peers: 0,
            pending_connections: 0,
            messages_sent: 0,
            messages_received: 0,
            bytes_sent: 0,
            bytes_received: 0,
            average_latency_ms: 0.0,
        };

        Ok(Self {
            network,
            message_handler: Arc::new(RwLock::new(message_handler)),
            config,
            peers: Arc::new(RwLock::new(HashMap::new())),
            message_handlers: Arc::new(RwLock::new(Vec::new())),
            stats: Arc::new(RwLock::new(stats)),
            message_receiver: Arc::new(RwLock::new(Some(rx))),
            message_sender: Arc::new(RwLock::new(Some(tx))),
            initialized: Arc::new(RwLock::new(false)),
            running: Arc::new(RwLock::new(false)),
        })
    }

    /// Convert a legacy NetworkMessage to MessagePayload
    fn convert_legacy_message_to_payload(
        message: &legacy_message::NetworkMessage,
    ) -> AnyhowResult<MessagePayload> {
        // Serialize the message to binary data
        let data = bincode::serialize(message)?;

        // Determine message type based on the variant
        let message_type = match message {
            legacy_message::NetworkMessage::NewBlock(_) => MessageType::Block,
            legacy_message::NetworkMessage::Transaction(_) => MessageType::Transaction,
            legacy_message::NetworkMessage::GetBlocks
            | legacy_message::NetworkMessage::Blocks(_) => MessageType::Sync,
            legacy_message::NetworkMessage::Ping | legacy_message::NetworkMessage::Pong => {
                MessageType::Heartbeat
            }
            legacy_message::NetworkMessage::PeerDiscoveryRequest
            | legacy_message::NetworkMessage::PeerDiscoveryResponse(_) => MessageType::Discovery,
            legacy_message::NetworkMessage::PeerInfoRequest
            | legacy_message::NetworkMessage::PeerInfoResponse(_) => MessageType::Discovery,
            legacy_message::NetworkMessage::PeerStatsRequest
            | legacy_message::NetworkMessage::PeerStatsResponse(_) => MessageType::Heartbeat,
        };

        // Create payload
        Ok(MessagePayload::new(message_type, data))
    }

    /// Convert a MessagePayload to legacy NetworkMessage
    fn convert_payload_to_legacy_message(
        payload: &MessagePayload,
    ) -> AnyhowResult<legacy_message::NetworkMessage> {
        // Deserialize the data to a NetworkMessage
        let message = bincode::deserialize(&payload.data)?;
        Ok(message)
    }

    /// Start message processing loop
    async fn start_message_processing(self: Arc<Self>) -> Result<(), NetworkError> {
        let mut receiver = {
            let mut rx_lock = self.message_receiver.write().await;
            rx_lock.take().ok_or(NetworkError::NotInitialized)?
        };

        // Spawn a task to process incoming messages
        tokio::spawn(async move {
            while let Some((message, addr)) = receiver.recv().await {
                // Convert the legacy message to our new payload type
                match Self::convert_legacy_message_to_payload(&message) {
                    Ok(payload) => {
                        // Find peer info
                        let peer_info = {
                            let peers = self.peers.read().await;
                            let peer_id = addr.to_string();

                            peers.get(&peer_id).cloned().unwrap_or_else(|| {
                                // Create a new peer info if not found
                                PeerInfo::new(peer_id, addr)
                            })
                        };

                        // Update stats
                        {
                            let mut stats = self.stats.write().await;
                            stats.messages_received += 1;
                            stats.bytes_received += payload.data.len() as u64;
                        }

                        // Call message handlers
                        let handlers = self.message_handlers.read().await;
                        for handler in handlers.iter() {
                            if let Err(e) = handler(peer_info.clone(), payload.clone()) {
                                error!("Error handling message: {}", e);
                            }
                        }
                    }
                    Err(e) => {
                        error!("Error converting legacy message: {}", e);
                    }
                }
            }

            info!("Message processing stopped");
        });

        Ok(())
    }
}

#[async_trait]
impl NetworkAdapter for LegacyNetworkAdapter {
    async fn initialize(&self) -> Result<(), NetworkError> {
        // Check if already initialized
        {
            let initialized = self.initialized.read().await;
            if *initialized {
                return Err(NetworkError::AlreadyInitialized);
            }
        }

        // Initialize the legacy network
        {
            let mut network = self.network.write().await;
            network.initialize().await.map_err(|e| {
                NetworkError::TransportError(format!("Failed to initialize legacy network: {}", e))
            })?;
        }

        // Mark as initialized
        {
            let mut initialized = self.initialized.write().await;
            *initialized = true;
        }

        info!("Legacy network adapter initialized");
        Ok(())
    }

    async fn start(&self) -> Result<(), NetworkError> {
        // Check if initialized
        {
            let initialized = self.initialized.read().await;
            if !*initialized {
                return Err(NetworkError::NotInitialized);
            }
        }

        // Check if already running
        {
            let running = self.running.read().await;
            if *running {
                return Err(NetworkError::AlreadyInitialized);
            }
        }

        // Mark as running
        {
            let mut running = self.running.write().await;
            *running = true;
        }

        // Start message processing
        let self_arc = Arc::new(self.clone());
        self_arc.start_message_processing().await?;

        info!("Legacy network adapter started");
        Ok(())
    }

    async fn stop(&self) -> Result<(), NetworkError> {
        // Check if running
        {
            let running = self.running.read().await;
            if !*running {
                return Ok(());
            }
        }

        // Shutdown the legacy network
        {
            let mut network = self.network.write().await;
            network.shutdown().await.map_err(|e| {
                NetworkError::TransportError(format!("Failed to shutdown legacy network: {}", e))
            })?;
        }

        // Mark as not running
        {
            let mut running = self.running.write().await;
            *running = false;
        }

        info!("Legacy network adapter stopped");
        Ok(())
    }

    async fn connect_to_peer(&self, peer_id: &str, addr: &str) -> Result<(), NetworkError> {
        // Check if initialized
        {
            let initialized = self.initialized.read().await;
            if !*initialized {
                return Err(NetworkError::NotInitialized);
            }
        }

        // Connect to the peer
        let network = self.network.read().await;
        let _peer_info = network.connect(addr).await.map_err(|e| {
            NetworkError::ConnectionFailed(format!("Failed to connect to peer {}: {}", addr, e))
        })?;

        // Add to peers map
        {
            let mut peers = self.peers.write().await;
            let socket_addr = addr
                .parse::<SocketAddr>()
                .map_err(|e| NetworkError::InvalidPeerId(format!("Invalid peer address: {}", e)))?;

            let new_peer_info = PeerInfo::new(peer_id.to_string(), socket_addr);
            peers.insert(peer_id.to_string(), new_peer_info);
        }

        // Update stats
        {
            let mut stats = self.stats.write().await;
            stats.connected_peers += 1;
        }

        info!("Connected to peer {} at {}", peer_id, addr);
        Ok(())
    }

    async fn disconnect_peer(&self, peer_id: &str) -> Result<(), NetworkError> {
        info!("Disconnecting from peer: {}", peer_id);

        // Remove peer from connected peers
        let mut peers = self.peers.write().await;
        peers.remove(peer_id);

        Ok(())
    }

    async fn ping_peer(&self, peer_addr: &str) -> Result<bool, NetworkError> {
        info!("Pinging peer: {}", peer_addr);

        // Basic ping implementation
        // In a real implementation, you'd:
        // 1. Check if peer is connected
        // 2. Send a ping message
        // 3. Wait for pong response with timeout
        // For now, just check if we have the peer in our connected peers
        let peers = self.peers.read().await;
        let is_connected = peers
            .values()
            .any(|peer| peer.address.to_string() == peer_addr);

        Ok(is_connected)
    }

    async fn shutdown(&self) -> Result<(), NetworkError> {
        info!("Shutting down legacy network adapter");

        // Clear all peer connections
        let mut peers = self.peers.write().await;
        peers.clear();

        // Clear message channels
        *self.message_sender.write().await = None;
        *self.message_receiver.write().await = None;

        // Update running state
        *self.running.write().await = false;

        info!("Legacy network adapter shutdown complete");
        Ok(())
    }

    async fn broadcast_message(&self, payload: MessagePayload) -> Result<(), NetworkError> {
        // Check if initialized
        {
            let initialized = self.initialized.read().await;
            if !*initialized {
                return Err(NetworkError::NotInitialized);
            }
        }

        // Convert payload to legacy message
        let legacy_message = Self::convert_payload_to_legacy_message(&payload).map_err(|e| {
            NetworkError::SerializationError(format!(
                "Failed to convert payload to legacy message: {}",
                e
            ))
        })?;

        // Get all connected peers
        let peers = self.peers.read().await;

        // Broadcast to all peers
        for (_, peer_info) in peers.iter() {
            let network = self.network.read().await;
            network
                .send_message(legacy_message.clone(), peer_info.address)
                .await
                .map_err(|e| {
                    NetworkError::SendFailed(format!(
                        "Failed to broadcast message to {}: {}",
                        peer_info.peer_id, e
                    ))
                })?;
        }

        // Update stats
        {
            let mut stats = self.stats.write().await;
            stats.messages_sent += 1;
            stats.bytes_sent += payload.data.len() as u64;
        }

        debug!(
            "Broadcast message of type {:?} to {} peers",
            payload.message_type,
            peers.len()
        );
        Ok(())
    }

    async fn send_message(
        &self,
        peer_id: &str,
        payload: MessagePayload,
    ) -> Result<(), NetworkError> {
        // Convert payload to legacy format and send
        // Convert the payload to a NetworkMessage
        let message = match payload.message_type {
            MessageType::Block => {
                // Try to deserialize as Block, fallback to NewBlock with string
                if let Ok(block) = bincode::deserialize::<crate::blockchain::Block>(&payload.data) {
                    NetworkMessage::NewBlock(block)
                } else {
                    // If deserialization fails, we can't create a proper message
                    return Err(NetworkError::DeserializationError(
                        "Failed to deserialize block".to_string(),
                    ));
                }
            }
            MessageType::Transaction => {
                if let Ok(tx) =
                    bincode::deserialize::<crate::blockchain::Transaction>(&payload.data)
                {
                    NetworkMessage::Transaction(tx)
                } else {
                    return Err(NetworkError::DeserializationError(
                        "Failed to deserialize transaction".to_string(),
                    ));
                }
            }
            MessageType::Sync => NetworkMessage::GetBlocks,
            MessageType::Heartbeat => NetworkMessage::Ping,
            MessageType::Discovery => NetworkMessage::PeerDiscoveryRequest,
            _ => {
                return Err(NetworkError::DeserializationError(
                    "Unsupported message type for legacy adapter".to_string(),
                ));
            }
        };
        let socket_addr = peer_id
            .parse::<std::net::SocketAddr>()
            .map_err(|e| NetworkError::ConnectionFailed(format!("Invalid peer address: {}", e)))?;

        self.network
            .write()
            .await
            .send_message(message, socket_addr)
            .await
            .map_err(|e| NetworkError::ConnectionFailed(e.to_string()))
    }

    async fn get_pending_messages(&self) -> Result<Vec<(String, MessagePayload)>, NetworkError> {
        // Get messages from legacy network and convert format
        // For now, return empty - this would be implemented based on legacy network API
        Ok(Vec::new())
    }

    async fn check_peer_health(&self) -> Result<(), NetworkError> {
        // Perform health checks on all connected peers
        // For now, this is a no-op - would be implemented based on legacy network capabilities
        Ok(())
    }

    async fn get_connected_peers(&self) -> Result<Vec<PeerInfo>, NetworkError> {
        // Check if initialized
        {
            let initialized = self.initialized.read().await;
            if !*initialized {
                return Err(NetworkError::NotInitialized);
            }
        }

        // Get peers from legacy network
        let network = self.network.read().await;
        let legacy_peers = network.get_all_peer_info().await;

        // Convert to our PeerInfo type
        let mut peers = Vec::new();
        for legacy_peer in legacy_peers {
            let socket_addr = legacy_peer.address;
            let mut peer_info = PeerInfo::new(legacy_peer.id.clone(), socket_addr);
            peer_info.status = ConnectionStatus::Connected;

            // Add any additional metadata
            peer_info.add_metadata("version", legacy_peer.version);
            for capability in legacy_peer.capabilities {
                peer_info.add_metadata("capability", capability);
            }

            peers.push(peer_info);
        }

        Ok(peers)
    }

    async fn is_peer_connected(&self, peer_id: &str) -> Result<bool, NetworkError> {
        // Check if initialized
        {
            let initialized = self.initialized.read().await;
            if !*initialized {
                return Err(NetworkError::NotInitialized);
            }
        }

        // Check if peer is connected
        let network = self.network.read().await;
        let is_connected = network.is_connected(peer_id).await.map_err(|e| {
            NetworkError::Unknown(format!("Failed to check if peer is connected: {}", e))
        })?;

        Ok(is_connected)
    }

    async fn get_network_stats(&self) -> Result<NetworkStats, NetworkError> {
        let stats = self.stats.read().await;
        Ok(stats.clone())
    }
}

// Allow cloning the adapter
impl Clone for LegacyNetworkAdapter {
    fn clone(&self) -> Self {
        Self {
            network: self.network.clone(),
            message_handler: self.message_handler.clone(),
            config: self.config.clone(),
            peers: self.peers.clone(),
            message_handlers: self.message_handlers.clone(),
            stats: self.stats.clone(),
            message_receiver: self.message_receiver.clone(),
            message_sender: self.message_sender.clone(),
            initialized: self.initialized.clone(),
            running: self.running.clone(),
        }
    }
}
