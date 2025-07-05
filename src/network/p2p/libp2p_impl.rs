//! libp2p implementation of the network adapter

use crate::network::p2p::{
    ConnectionStatus, MessagePayload, MessageType, NetworkAdapter, NetworkConfig, NetworkError,
    NetworkStats, PeerInfo,
};
use anyhow::Result as AnyhowResult;
use async_trait::async_trait;
use futures::{channel::mpsc, prelude::*};
use libp2p::{
    core::upgrade,
    gossipsub, identity, mdns, noise,
    request_response::{self, Codec},
    swarm::{Config as SwarmConfig, NetworkBehaviour, Swarm, SwarmEvent},
    tcp, yamux, Multiaddr, PeerId, Transport,
};
use log::{debug, error, info, warn};
use serde::{Deserialize, Serialize};
use std::{
    collections::HashMap,
    str::FromStr,
    sync::Arc,
    time::{Duration, SystemTime},
};
use tokio::sync::{Mutex, RwLock};

// Protocol name for direct request/response
#[derive(Debug, Clone, Default)]
struct P2PProtocol();

impl AsRef<str> for P2PProtocol {
    fn as_ref(&self) -> &str {
        "/self-chain/p2p/1.0.0"
    }
}

impl Codec for P2PProtocol {
    type Protocol = P2PProtocol;
    type Request = MessagePayload;
    type Response = MessagePayload;

    fn read_request<'life0, 'life1, 'life2, 'async_trait, T>(
        &'life0 mut self,
        _protocol: &'life1 Self::Protocol,
        io: &'life2 mut T,
    ) -> std::pin::Pin<
        Box<
            dyn std::future::Future<Output = Result<Self::Request, std::io::Error>>
                + Send
                + 'async_trait,
        >,
    >
    where
        'life0: 'async_trait,
        'life1: 'async_trait,
        'life2: 'async_trait,
        T: futures::AsyncRead + Unpin + Send + 'async_trait,
        Self: 'async_trait,
    {
        Box::pin(async move {
            // Simple implementation - in practice you'd use proper serialization
            let mut buf = Vec::new();
            let _ = futures::AsyncReadExt::read_to_end(io, &mut buf).await?;

            // Create a properly formatted MessagePayload
            let now = std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .map_err(|e| std::io::Error::new(std::io::ErrorKind::Other, e))?;

            Ok(MessagePayload {
                message_type: MessageType::PeerConnection,
                data: buf,
                message_id: Some(format!("msg_{}", now.as_nanos())),
                ttl: Some(300), // 5 minutes default TTL
                timestamp: now.as_secs(),
            })
        })
    }

    fn read_response<'life0, 'life1, 'life2, 'async_trait, T>(
        &'life0 mut self,
        _protocol: &'life1 Self::Protocol,
        io: &'life2 mut T,
    ) -> std::pin::Pin<
        Box<
            dyn std::future::Future<Output = Result<Self::Response, std::io::Error>>
                + Send
                + 'async_trait,
        >,
    >
    where
        'life0: 'async_trait,
        'life1: 'async_trait,
        'life2: 'async_trait,
        T: futures::AsyncRead + Unpin + Send + 'async_trait,
        Self: 'async_trait,
    {
        Box::pin(async move {
            // Read the message data
            let mut buf = Vec::new();
            let _ = futures::AsyncReadExt::read_to_end(io, &mut buf).await?;

            // Create a properly formatted MessagePayload
            let now = std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .map_err(|e| std::io::Error::new(std::io::ErrorKind::Other, e))?;

            Ok(MessagePayload {
                message_type: MessageType::PeerConnection,
                data: buf,
                message_id: Some(format!("msg_{}", now.as_nanos())),
                ttl: Some(300), // 5 minutes default TTL
                timestamp: now.as_secs(),
            })
        })
    }

    fn write_request<'life0, 'life1, 'life2, 'async_trait, T>(
        &'life0 mut self,
        _protocol: &'life1 Self::Protocol,
        io: &'life2 mut T,
        req: Self::Request,
    ) -> std::pin::Pin<
        Box<dyn std::future::Future<Output = Result<(), std::io::Error>> + Send + 'async_trait>,
    >
    where
        'life0: 'async_trait,
        'life1: 'async_trait,
        'life2: 'async_trait,
        T: futures::AsyncWrite + Unpin + Send + 'async_trait,
        Self: 'async_trait,
    {
        Box::pin(async move {
            // Serialize the message using bincode
            let data = bincode::serialize(&req)
                .map_err(|e| std::io::Error::new(std::io::ErrorKind::InvalidData, e))?;

            // Write the length prefix (4 bytes, big-endian)
            let len = data.len() as u32;
            let len_bytes = len.to_be_bytes();

            // Write the length and then the data
            futures::AsyncWriteExt::write_all(io, &len_bytes).await?;
            futures::AsyncWriteExt::write_all(io, &data).await?;

            Ok(())
        })
    }

    fn write_response<'life0, 'life1, 'life2, 'async_trait, T>(
        &'life0 mut self,
        _protocol: &'life1 Self::Protocol,
        io: &'life2 mut T,
        res: Self::Response,
    ) -> std::pin::Pin<
        Box<dyn std::future::Future<Output = Result<(), std::io::Error>> + Send + 'async_trait>,
    >
    where
        'life0: 'async_trait,
        'life1: 'async_trait,
        'life2: 'async_trait,
        T: futures::AsyncWrite + Unpin + Send + 'async_trait,
        Self: 'async_trait,
    {
        // Reuse the same implementation as write_request since they're identical
        self.write_request(_protocol, io, res)
    }
}

// Combined network behavior for our swarm
#[derive(NetworkBehaviour)]
struct P2PBehaviour {
    gossipsub: gossipsub::Behaviour,
    request_response: request_response::Behaviour<P2PProtocol>,
    // mdns: mdns::Behaviour<libp2p_mdns::tokio::Provider>,
}

// Message handler type
type MessageHandlerFn =
    Box<dyn Fn(PeerInfo, MessagePayload) -> Result<(), NetworkError> + Send + Sync + 'static>;

// Request/response types
#[derive(Debug, Clone, Serialize, Deserialize)]
struct P2PRequest {
    message_type: MessageType,
    data: Vec<u8>,
    message_id: Option<String>,
}

impl P2PRequest {
    #[allow(dead_code)]
    fn len(&self) -> usize {
        self.data.len()
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
struct P2PResponse {
    success: bool,
    data: Vec<u8>,
    error: Option<String>,
}

/// LibP2P implementation of the NetworkAdapter trait
pub struct LibP2PAdapter {
    /// Network configuration
    config: NetworkConfig,

    /// libp2p swarm
    swarm: Arc<Mutex<Option<Swarm<P2PBehaviour>>>>,

    /// Connected peers
    peers: Arc<RwLock<HashMap<String, PeerInfo>>>,

    /// Message handlers
    message_handlers: Arc<RwLock<Vec<MessageHandlerFn>>>,

    /// Network statistics
    stats: Arc<Mutex<NetworkStats>>,

    /// Topics for different message types
    block_topic: gossipsub::TopicHash,
    transaction_topic: gossipsub::TopicHash,

    /// Outbound message channel
    message_sender: Option<mpsc::Sender<(MessagePayload, Option<String>)>>,

    /// Shutdown signal
    shutdown: Arc<RwLock<bool>>,

    /// Is initialized flag
    initialized: Arc<RwLock<bool>>,
}

// Allow cloning the adapter
impl Clone for LibP2PAdapter {
    fn clone(&self) -> Self {
        Self {
            config: self.config.clone(),
            swarm: self.swarm.clone(),
            peers: self.peers.clone(),
            message_handlers: self.message_handlers.clone(),
            stats: self.stats.clone(),
            block_topic: self.block_topic.clone(),
            transaction_topic: self.transaction_topic.clone(),
            message_sender: self.message_sender.clone(),
            shutdown: self.shutdown.clone(),
            initialized: self.initialized.clone(),
        }
    }
}

impl LibP2PAdapter {
    /// Create a new LibP2P adapter with the given configuration
    pub fn new(config: NetworkConfig) -> Self {
        let stats = NetworkStats {
            connected_peers: 0,
            pending_connections: 0,
            messages_sent: 0,
            messages_received: 0,
            bytes_sent: 0,
            bytes_received: 0,
            average_latency_ms: 0.0,
        };

        let block_topic = gossipsub::TopicHash::from_raw("self-chain/blocks");
        let transaction_topic = gossipsub::TopicHash::from_raw("self-chain/transactions");

        Self {
            config,
            swarm: Arc::new(Mutex::new(None)),
            peers: Arc::new(RwLock::new(HashMap::new())),
            message_handlers: Arc::new(RwLock::new(Vec::new())),
            stats: Arc::new(Mutex::new(stats)),
            block_topic,
            transaction_topic,
            message_sender: None,
            shutdown: Arc::new(RwLock::new(false)),
            initialized: Arc::new(RwLock::new(false)),
        }
    }

    // Convert our PeerId to libp2p PeerId
    fn peer_id_to_libp2p(&self, peer_id: &str) -> Result<PeerId, NetworkError> {
        PeerId::from_str(peer_id)
            .map_err(|e| NetworkError::InvalidPeerId(format!("Invalid peer ID format: {}", e)))
    }

    // Convert libp2p PeerId to our string format
    fn libp2p_to_peer_id(&self, peer_id: &PeerId) -> String {
        peer_id.to_string()
    }

    // Convert our MessagePayload to gossipsub message
    fn payload_to_gossipsub(&self, payload: &MessagePayload) -> Vec<u8> {
        bincode::serialize(payload).unwrap_or_else(|_| Vec::new())
    }

    // Convert gossipsub message to our MessagePayload
    #[allow(dead_code)]
    fn gossipsub_to_payload(
        &self,
        message: &gossipsub::Event,
    ) -> Result<MessagePayload, NetworkError> {
        match message {
            gossipsub::Event::Message { message, .. } => bincode::deserialize(&message.data)
                .map_err(|e| NetworkError::DeserializationError(e.to_string())),
            _ => Err(NetworkError::DeserializationError(
                "Invalid message type".to_string(),
            )),
        }
    }

    // Convert gossipsub Message directly to our MessagePayload
    fn gossipsub_message_to_payload(
        &self,
        message: &gossipsub::Message,
    ) -> Result<MessagePayload, NetworkError> {
        bincode::deserialize(&message.data)
            .map_err(|e| NetworkError::DeserializationError(e.to_string()))
    }

    // Create a new libp2p identity
    fn create_identity(&self) -> identity::Keypair {
        // For now, generate a random keypair
        // In production, this should be loaded from a file or other secure storage
        identity::Keypair::generate_ed25519()
    }

    // Setup the libp2p transport with appropriate security and multiplexing
    fn build_transport(
        &self,
        keypair: &identity::Keypair,
    ) -> AnyhowResult<libp2p::core::transport::Boxed<(PeerId, libp2p::core::muxing::StreamMuxerBox)>>
    {
        // Configure TCP transport with nodelay enabled
        let tcp_config = tcp::Config::new().nodelay(true);

        // Configure yamux
        let yamux_config = yamux::Config::default();

        let transport = tcp::tokio::Transport::new(tcp_config)
            .upgrade(upgrade::Version::V1)
            .authenticate(noise::Config::new(keypair).unwrap())
            .multiplex(yamux_config)
            .timeout(Duration::from_secs(
                self.config.connection_timeout_ms / 1000,
            ))
            .boxed();

        Ok(transport)
    }

    // Setup the gossipsub behavior
    fn build_gossipsub(&self, keypair: &identity::Keypair) -> AnyhowResult<gossipsub::Behaviour> {
        // Configure gossipsub
        let gossipsub_config = gossipsub::Config::default();

        // Create gossipsub with the configured settings
        let message_authenticity = gossipsub::MessageAuthenticity::Signed(keypair.clone());
        let mut gossipsub = gossipsub::Behaviour::new(message_authenticity, gossipsub_config)
            .map_err(|e| anyhow::anyhow!("Failed to create gossipsub behavior: {}", e))?;

        // Pre-subscribe to default topics
        let default_topics = [
            MessageType::Block,
            MessageType::Transaction,
            MessageType::Consensus,
            MessageType::Discovery,
        ];

        for msg_type in default_topics.iter() {
            let topic_name = match msg_type {
                MessageType::Block => "blocks",
                MessageType::Transaction => "transactions",
                MessageType::Consensus => "consensus",
                MessageType::Discovery => "discovery",
                MessageType::PeerConnection => "peer-connection",
                MessageType::Validation => "validation",
                MessageType::Sync => "sync",
                MessageType::Heartbeat => "heartbeat",
                MessageType::Custom(_id) => continue, // Skip custom topics for now
            };

            let topic = gossipsub::IdentTopic::new(format!("self-chain/{}", topic_name));
            if let Err(e) = gossipsub.subscribe(&topic) {
                warn!("Failed to subscribe to topic {}: {}", topic_name, e);
            }
        }

        Ok(gossipsub)
    }

    // Setup the request/response behavior
    fn build_request_response(&self) -> request_response::Behaviour<P2PProtocol> {
        request_response::Behaviour::new(
            std::iter::once((P2PProtocol {}, request_response::ProtocolSupport::Full)),
            request_response::Config::default(),
        )
    }

    // Setup the MDNS behavior for local peer discovery
    // fn build_mdns(&self) -> AnyhowResult<mdns::Behaviour<libp2p_mdns::tokio::Provider>> {
    //     mdns::Behaviour::new(mdns::Config::default(), self.create_identity().public())
    //         .map_err(|e| anyhow::anyhow!("Failed to create MDNS: {}", e))
    // }

    // Build the swarm with all behaviors
    async fn build_swarm(&self) -> AnyhowResult<Swarm<P2PBehaviour>> {
        let keypair = self.create_identity();
        let peer_id = PeerId::from(keypair.public());
        info!("Local peer id: {}", peer_id);

        let transport = self.build_transport(&keypair)?;
        let gossipsub = self.build_gossipsub(&keypair)?;
        let request_response = self.build_request_response();
        // let mdns = self.build_mdns()?;

        let behaviour = P2PBehaviour {
            gossipsub,
            request_response,
            // mdns,
        };

        let swarm = Swarm::new(
            transport,
            behaviour,
            peer_id,
            SwarmConfig::with_executor(Box::new(|fut| {
                tokio::spawn(fut);
            })),
        );

        Ok(swarm)
    }

    // Main event loop to process libp2p events
    async fn run_event_loop(self: Arc<Self>) -> Result<(), NetworkError> {
        let (_message_sender, mut message_receiver) = mpsc::channel(32);

        // Start listening on the configured address
        let listen_addr = self.config.listen_address.clone();
        let mut swarm = {
            let mut swarm_lock = self.swarm.lock().await;
            if swarm_lock.is_none() {
                *swarm_lock = Some(self.build_swarm().await.map_err(|e| {
                    NetworkError::TransportError(format!("Failed to build swarm: {}", e))
                })?);
            }
            swarm_lock.take().unwrap()
        };

        swarm
            .listen_on(listen_addr.parse::<Multiaddr>().map_err(|e| {
                NetworkError::TransportError(format!("Invalid listen address: {}", e))
            })?)
            .map_err(|e| {
                NetworkError::TransportError(format!("Failed to listen on {}: {}", listen_addr, e))
            })?;

        // Connect to bootstrap peers
        for peer_addr in &self.config.bootstrap_peers {
            match peer_addr.parse::<Multiaddr>() {
                Ok(addr) => {
                    debug!("Dialing bootstrap peer: {}", addr);
                    if let Err(e) = swarm.dial(addr.clone()) {
                        warn!("Failed to dial bootstrap peer {}: {}", addr, e);
                    }
                }
                Err(e) => {
                    warn!("Invalid bootstrap peer address {}: {}", peer_addr, e);
                }
            }
        }

        // Main event loop
        loop {
            // Check if we should shut down
            if *self.shutdown.read().await {
                info!("Network adapter shutting down");
                break;
            }

            tokio::select! {
                // Handle outbound messages from our application
                Some((payload, maybe_peer_id)) = message_receiver.next() => {
                    self.handle_outbound_message(&mut swarm, payload, maybe_peer_id).await?;
                }

                // Handle libp2p swarm events
                event = swarm.select_next_some() => {
                    self.handle_swarm_event(&mut swarm, event).await?;
                }
            }
        }

        // Update swarm
        {
            let mut swarm_lock = self.swarm.lock().await;
            *swarm_lock = Some(swarm);
        }

        Ok(())
    }

    // Handle outbound messages from our application
    async fn handle_outbound_message(
        &self,
        swarm: &mut Swarm<P2PBehaviour>,
        payload: MessagePayload,
        maybe_peer_id: Option<String>,
    ) -> Result<(), NetworkError> {
        if let Some(peer_id) = maybe_peer_id {
            // Direct message to a specific peer
            let libp2p_peer_id = self.peer_id_to_libp2p(&peer_id)?;
            let data_len = payload.data.len();
            swarm
                .behaviour_mut()
                .request_response
                .send_request(&libp2p_peer_id, payload);

            // Update stats
            let mut stats = self.stats.lock().await;
            stats.messages_sent += 1;
            stats.bytes_sent += data_len as u64;
        } else {
            // Broadcast to all peers via gossipsub
            let topic = match payload.message_type {
                MessageType::Block => self.block_topic.clone(),
                MessageType::Transaction => self.transaction_topic.clone(),
                MessageType::Consensus => gossipsub::TopicHash::from_raw("self-chain/consensus"),
                MessageType::Discovery => gossipsub::TopicHash::from_raw("self-chain/discovery"),
                MessageType::PeerConnection => {
                    gossipsub::TopicHash::from_raw("self-chain/peer-connection")
                }
                MessageType::Validation => gossipsub::TopicHash::from_raw("self-chain/validation"),
                MessageType::Sync => gossipsub::TopicHash::from_raw("self-chain/sync"),
                MessageType::Heartbeat => gossipsub::TopicHash::from_raw("self-chain/heartbeat"),
                MessageType::Custom(id) => {
                    gossipsub::TopicHash::from_raw(format!("self-chain/custom-{}", id))
                }
            };

            let message_data = self.payload_to_gossipsub(&payload);

            // Publish to the appropriate topic
            if let Err(e) = swarm
                .behaviour_mut()
                .gossipsub
                .publish(topic, message_data.clone())
            {
                return Err(NetworkError::SendFailed(format!(
                    "Failed to publish message: {}",
                    e
                )));
            }

            // Update stats
            let mut stats = self.stats.lock().await;
            stats.messages_sent += 1;
            stats.bytes_sent += message_data.len() as u64;
        }

        Ok(())
    }

    // Handle events from the libp2p swarm
    async fn handle_swarm_event(
        &self,
        swarm: &mut Swarm<P2PBehaviour>,
        event: SwarmEvent<P2PBehaviourEvent>,
    ) -> Result<(), NetworkError> {
        match event {
            // Connection events
            SwarmEvent::ConnectionEstablished {
                peer_id, endpoint, ..
            } => {
                let peer_id_str = self.libp2p_to_peer_id(&peer_id);
                debug!("Connected to peer: {}", peer_id_str);

                // Create or update peer info
                let mut peers = self.peers.write().await;
                let addr = endpoint
                    .get_remote_address()
                    .to_string()
                    .parse::<Multiaddr>()
                    .unwrap_or_else(|_| "0.0.0.0:0".parse().unwrap());

                let peer_info = peers.entry(peer_id_str.clone()).or_insert_with(|| {
                    // Convert Multiaddr to SocketAddr for PeerInfo::new()
                    let socket_addr = addr
                        .to_string()
                        .parse::<std::net::SocketAddr>()
                        .unwrap_or_else(|_| "0.0.0.0:0".parse().unwrap());
                    PeerInfo::new(peer_id_str, socket_addr)
                });

                peer_info.status = ConnectionStatus::Connected;
                peer_info.update_last_seen();

                // Update stats
                let mut stats = self.stats.lock().await;
                stats.connected_peers = peers.len();
            }

            SwarmEvent::ConnectionClosed { peer_id, .. } => {
                let peer_id_str = self.libp2p_to_peer_id(&peer_id);
                debug!("Disconnected from peer: {}", peer_id_str);

                // Update peer status
                let mut peers = self.peers.write().await;
                if let Some(peer_info) = peers.get_mut(&peer_id_str) {
                    peer_info.status = ConnectionStatus::Disconnected;
                    peer_info.update_last_seen();
                }

                // Update stats
                let mut stats = self.stats.lock().await;
                stats.connected_peers = peers
                    .values()
                    .filter(|p| p.status == ConnectionStatus::Connected)
                    .count();
            }

            // Incoming connections
            SwarmEvent::IncomingConnection { .. } => {
                // Just log for now
                debug!("Incoming connection");
            }

            // New listen addresses
            SwarmEvent::NewListenAddr { address, .. } => {
                info!("Listening on: {}", address);
            }

            // Dialing events
            SwarmEvent::OutgoingConnectionError { peer_id, error, .. } => {
                if let Some(peer_id) = peer_id {
                    let peer_id_str = self.libp2p_to_peer_id(&peer_id);
                    warn!("Failed to connect to peer {}: {}", peer_id_str, error);

                    // Update peer status
                    let mut peers = self.peers.write().await;
                    if let Some(peer_info) = peers.get_mut(&peer_id_str) {
                        peer_info.status = ConnectionStatus::Failed;
                        peer_info.update_last_seen();
                    }
                } else {
                    warn!("Failed to connect to unknown peer: {}", error);
                }
            }

            // Behavior-specific events
            SwarmEvent::Behaviour(event) => match event {
                P2PBehaviourEvent::Gossipsub(gossipsub_event) => {
                    self.handle_gossipsub_event(swarm, gossipsub_event).await?;
                }
                P2PBehaviourEvent::RequestResponse(req_resp_event) => {
                    self.handle_request_response_event(swarm, req_resp_event)
                        .await?;
                } // P2PBehaviourEvent::Mdns(mdns_event) => {
                  //     self.handle_mdns_event(swarm, mdns_event).await?;
                  // }
            },

            // Other events
            _ => {}
        }

        Ok(())
    }

    // Handle gossipsub events
    async fn handle_gossipsub_event(
        &self,
        _swarm: &mut Swarm<P2PBehaviour>,
        event: gossipsub::Event,
    ) -> Result<(), NetworkError> {
        match event {
            gossipsub::Event::Message {
                propagation_source,
                message_id,
                message,
            } => {
                let peer_id_str = self.libp2p_to_peer_id(&propagation_source);
                debug!(
                    "Received gossipsub message from {} (id: {})",
                    peer_id_str, message_id
                );

                // Convert to our message format
                let payload = self.gossipsub_message_to_payload(&message)?;

                // Update stats
                {
                    let mut stats = self.stats.lock().await;
                    stats.messages_received += 1;
                    stats.bytes_received += payload.data.len() as u64;
                }

                // Find peer info
                let peer_info = {
                    let peers = self.peers.read().await;
                    match peers.get(&peer_id_str) {
                        Some(info) => info.clone(),
                        None => {
                            // Create placeholder peer info
                            let addr = "0.0.0.0:0".parse().unwrap();
                            let mut info = PeerInfo::new(peer_id_str, addr);
                            info.status = ConnectionStatus::Connected;
                            info
                        }
                    }
                };

                // Call all message handlers
                let handlers = self.message_handlers.read().await;
                for handler in handlers.iter() {
                    if let Err(e) = handler(peer_info.clone(), payload.clone()) {
                        warn!("Message handler error: {}", e);
                    }
                }
            }
            gossipsub::Event::Subscribed { peer_id, topic } => {
                debug!("Peer {} subscribed to {}", peer_id, topic);
            }
            gossipsub::Event::Unsubscribed { peer_id, topic } => {
                debug!("Peer {} unsubscribed from {}", peer_id, topic);
            }
            // Handle other gossipsub events as needed
            _ => {}
        }

        Ok(())
    }

    // Handle request/response events
    async fn handle_request_response_event(
        &self,
        swarm: &mut Swarm<P2PBehaviour>,
        event: request_response::Event<MessagePayload, MessagePayload>,
    ) -> Result<(), NetworkError> {
        match event {
            request_response::Event::Message { peer, message, .. } => {
                match message {
                    request_response::Message::Request {
                        request_id,
                        request,
                        channel,
                    } => {
                        debug!("Received request from peer {}: {:?}", peer, request_id);

                        // Update stats
                        {
                            let mut stats = self.stats.lock().await;
                            stats.messages_received += 1;
                            stats.bytes_received += request.data.len() as u64;
                        }

                        // Convert to our message format
                        let payload = MessagePayload {
                            message_type: request.message_type,
                            data: request.data,
                            message_id: request.message_id,
                            ttl: None,
                            timestamp: SystemTime::now()
                                .duration_since(SystemTime::UNIX_EPOCH)
                                .unwrap()
                                .as_secs(),
                        };

                        // Find peer info
                        let peer_id_str = self.libp2p_to_peer_id(&peer);
                        let peer_info = {
                            let peers = self.peers.read().await;
                            match peers.get(&peer_id_str) {
                                Some(info) => info.clone(),
                                None => {
                                    // Create placeholder peer info
                                    let addr = "0.0.0.0:0".parse().unwrap();
                                    let mut info = PeerInfo::new(peer_id_str, addr);
                                    info.status = ConnectionStatus::Connected;
                                    info
                                }
                            }
                        };

                        // Call all message handlers
                        let handlers = self.message_handlers.read().await;
                        let mut success = true;
                        let mut error_msg = None;

                        for handler in handlers.iter() {
                            if let Err(e) = handler(peer_info.clone(), payload.clone()) {
                                warn!("Message handler error: {}", e);
                                success = false;
                                error_msg = Some(e.to_string());
                            }
                        }

                        // Send response
                        let response_payload = MessagePayload {
                            message_type: MessageType::Validation, // Response to validation request
                            data: if success {
                                vec![]
                            } else {
                                error_msg.clone().unwrap_or_default().into_bytes()
                            },
                            message_id: None,
                            ttl: None,
                            timestamp: SystemTime::now()
                                .duration_since(SystemTime::UNIX_EPOCH)
                                .unwrap()
                                .as_secs(),
                        };

                        swarm
                            .behaviour_mut()
                            .request_response
                            .send_response(channel, response_payload)
                            .map_err(|e| {
                                NetworkError::SendFailed(format!(
                                    "Failed to send response: {:?}",
                                    e
                                ))
                            })?;
                    }
                    request_response::Message::Response {
                        request_id,
                        response,
                    } => {
                        debug!("Received response from peer {}: {:?}", peer, request_id);

                        // Update stats
                        {
                            let mut stats = self.stats.lock().await;
                            stats.messages_received += 1;
                            stats.bytes_received += response.data.len() as u64;
                        }

                        // Since MessagePayload doesn't have success/error fields, interpret response
                        // Empty data typically indicates success, non-empty data indicates error message
                        if !response.data.is_empty() {
                            let error_msg = String::from_utf8(response.data.clone())
                                .unwrap_or_else(|_| "Unknown error".to_string());
                            warn!("Request {} failed: {}", request_id, error_msg);
                        }
                    }
                }
            }
            request_response::Event::OutboundFailure {
                peer,
                request_id,
                error,
            } => {
                warn!(
                    "Outbound request to {} failed: {:?} - {:?}",
                    peer, request_id, error
                );
            }
            request_response::Event::InboundFailure {
                peer,
                request_id,
                error,
            } => {
                warn!(
                    "Inbound request from {} failed: {:?} - {:?}",
                    peer, request_id, error
                );
            }
            request_response::Event::ResponseSent { peer, request_id } => {
                debug!("Response sent to {}: {:?}", peer, request_id);
            }
        }

        Ok(())
    }

    // Handle MDNS events for local peer discovery
    #[allow(dead_code)]
    async fn handle_mdns_event(
        &self,
        _swarm: &mut Swarm<P2PBehaviour>,
        event: mdns::Event,
    ) -> Result<(), NetworkError> {
        match event {
            mdns::Event::Discovered(list) => {
                for (peer_id, multiaddr) in list {
                    debug!("MDNS discovered peer: {} at {}", peer_id, multiaddr);

                    // Store peer info
                    let peer_id_str = self.libp2p_to_peer_id(&peer_id);
                    let mut peers = self.peers.write().await;

                    // Try to parse socket address from multiaddr
                    let addr_str = multiaddr.to_string();
                    let addr = addr_str
                        .parse::<Multiaddr>()
                        .unwrap_or_else(|_| "0.0.0.0:0".parse().unwrap());

                    let peer_info = peers.entry(peer_id_str.clone()).or_insert_with(|| {
                        // Convert Multiaddr to SocketAddr for PeerInfo::new()
                        let socket_addr = addr
                            .to_string()
                            .parse::<std::net::SocketAddr>()
                            .unwrap_or_else(|_| "0.0.0.0:0".parse().unwrap());
                        PeerInfo::new(peer_id_str, socket_addr)
                    });

                    peer_info.status = ConnectionStatus::Connected;
                    peer_info.update_last_seen();
                }
            }
            mdns::Event::Expired(list) => {
                for (peer_id, multiaddr) in list {
                    debug!("MDNS peer expired: {} at {}", peer_id, multiaddr);
                }
            }
        }

        Ok(())
    }
}

#[async_trait]
impl NetworkAdapter for LibP2PAdapter {
    async fn initialize(&self) -> Result<(), NetworkError> {
        let mut initialized = self.initialized.write().await;
        if *initialized {
            return Err(NetworkError::AlreadyInitialized);
        }

        // Create swarm
        let swarm = self
            .build_swarm()
            .await
            .map_err(|e| NetworkError::TransportError(format!("Failed to build swarm: {}", e)))?;

        // Store swarm
        let mut swarm_lock = self.swarm.lock().await;
        *swarm_lock = Some(swarm);

        // Mark as initialized
        *initialized = true;

        Ok(())
    }

    async fn start(&self) -> Result<(), NetworkError> {
        let initialized = self.initialized.read().await;
        if !*initialized {
            return Err(NetworkError::NotInitialized);
        }

        // Reset shutdown flag
        let mut shutdown = self.shutdown.write().await;
        *shutdown = false;
        drop(shutdown);

        // Clone self for the event loop
        let this = Arc::new(self.clone());

        // Start the event loop in a separate task
        tokio::spawn(async move {
            if let Err(e) = this.run_event_loop().await {
                error!("Network event loop failed: {}", e);
            }
        });

        info!("Network adapter started");
        Ok(())
    }

    async fn stop(&self) -> Result<(), NetworkError> {
        // Set shutdown flag
        let mut shutdown = self.shutdown.write().await;
        *shutdown = true;

        info!("Network adapter stopping");
        Ok(())
    }

    async fn connect_to_peer(&self, peer_id: &str, addr: &str) -> Result<(), NetworkError> {
        let initialized = self.initialized.read().await;
        if !*initialized {
            return Err(NetworkError::NotInitialized);
        }

        // Parse address
        let multiaddr = addr
            .parse::<Multiaddr>()
            .map_err(|e| NetworkError::InvalidPeerId(format!("Invalid address: {}", e)))?;

        // Parse peer ID
        let _libp2p_peer_id = self.peer_id_to_libp2p(peer_id)?;

        // Acquire swarm lock
        let mut swarm_lock = self.swarm.lock().await;
        if let Some(swarm) = swarm_lock.as_mut() {
            // Dial the peer
            swarm.dial(multiaddr.clone()).map_err(|e| {
                NetworkError::ConnectionFailed(format!("Failed to dial {}: {}", addr, e))
            })?;

            // Update peer info
            let mut peers = self.peers.write().await;
            let _socket_addr = addr
                .parse::<Multiaddr>()
                .unwrap_or_else(|_| "0.0.0.0:0".parse().unwrap());

            let peer_info = peers.entry(peer_id.to_string()).or_insert_with(|| {
                // Convert Multiaddr to SocketAddr for PeerInfo::new()
                let socket_addr = addr
                    .to_string()
                    .parse::<std::net::SocketAddr>()
                    .unwrap_or_else(|_| "0.0.0.0:0".parse().unwrap());
                PeerInfo::new(peer_id.to_string(), socket_addr)
            });

            peer_info.status = ConnectionStatus::Connecting;
            peer_info.update_last_seen();

            // Update stats
            let mut stats = self.stats.lock().await;
            stats.pending_connections += 1;

            debug!("Connecting to peer {} at {}", peer_id, addr);
            Ok(())
        } else {
            Err(NetworkError::NotInitialized)
        }
    }

    async fn disconnect_peer(&self, peer_id: &str) -> Result<(), NetworkError> {
        let initialized = self.initialized.read().await;
        if !*initialized {
            return Err(NetworkError::NotInitialized);
        }

        // Parse peer ID
        let _libp2p_peer_id = self.peer_id_to_libp2p(peer_id)?;

        // Acquire swarm lock
        let mut swarm_lock = self.swarm.lock().await;
        if let Some(_swarm) = swarm_lock.as_mut() {
            // Disconnect peer
            // Note: libp2p doesn't have a direct "disconnect" API, but we can ban the peer temporarily
            // Note: ban_peer_id not available in current libp2p version
            warn!("Would ban peer {} for 5 seconds", peer_id);

            // Update peer info
            let mut peers = self.peers.write().await;
            if let Some(peer_info) = peers.get_mut(peer_id) {
                peer_info.status = ConnectionStatus::Disconnecting;
                peer_info.update_last_seen();
            } else {
                return Err(NetworkError::PeerNotFound(peer_id.to_string()));
            }

            debug!("Disconnecting peer {}", peer_id);
            Ok(())
        } else {
            Err(NetworkError::NotInitialized)
        }
    }

    async fn broadcast_message(&self, payload: MessagePayload) -> Result<(), NetworkError> {
        let initialized = self.initialized.read().await;
        if !*initialized {
            return Err(NetworkError::NotInitialized);
        }

        // Check for message sender
        if let Some(sender) = &self.message_sender {
            // Send to message handler (None indicates broadcast)
            let mut sender = sender.clone();
            sender
                .send((payload, None))
                .await
                .map_err(|e| NetworkError::SendFailed(format!("Failed to queue message: {}", e)))?;

            Ok(())
        } else {
            Err(NetworkError::NotInitialized)
        }
    }

    async fn send_message(
        &self,
        peer_id: &str,
        payload: MessagePayload,
    ) -> Result<(), NetworkError> {
        let initialized = self.initialized.read().await;
        if !*initialized {
            return Err(NetworkError::NotInitialized);
        }

        // Check if peer exists
        {
            let peers = self.peers.read().await;
            if !peers.contains_key(peer_id) {
                return Err(NetworkError::PeerNotFound(peer_id.to_string()));
            }
        }

        // Check for message sender
        if let Some(sender) = &self.message_sender {
            // Send to message handler with specific peer
            let mut sender = sender.clone();
            sender
                .send((payload, Some(peer_id.to_string())))
                .await
                .map_err(|e| NetworkError::SendFailed(format!("Failed to queue message: {}", e)))?;

            Ok(())
        } else {
            Err(NetworkError::NotInitialized)
        }
    }

    async fn get_connected_peers(&self) -> Result<Vec<PeerInfo>, NetworkError> {
        let peers = self.peers.read().await;

        // Filter for connected peers only
        let connected_peers = peers
            .values()
            .filter(|p| p.status == ConnectionStatus::Connected)
            .cloned()
            .collect();

        Ok(connected_peers)
    }

    async fn is_peer_connected(&self, peer_id: &str) -> Result<bool, NetworkError> {
        let peers = self.peers.read().await;

        // Check if peer exists and is connected
        if let Some(peer) = peers.get(peer_id) {
            Ok(peer.status == ConnectionStatus::Connected)
        } else {
            Ok(false)
        }
    }

    async fn get_network_stats(&self) -> Result<NetworkStats, NetworkError> {
        let stats = self.stats.lock().await;
        Ok(stats.clone())
    }

    async fn ping_peer(&self, peer_addr: &str) -> Result<bool, NetworkError> {
        // Simple ping implementation - just check if peer is connected
        let peers = self.peers.read().await;
        let is_connected = peers.values().any(|peer| {
            peer.address.to_string() == peer_addr && peer.status == ConnectionStatus::Connected
        });
        Ok(is_connected)
    }

    async fn shutdown(&self) -> Result<(), NetworkError> {
        let mut shutdown = self.shutdown.write().await;
        *shutdown = true;

        // Clear peers
        let mut peers = self.peers.write().await;
        peers.clear();

        Ok(())
    }

    async fn get_pending_messages(&self) -> Result<Vec<(String, MessagePayload)>, NetworkError> {
        // Get messages from libp2p message queue
        // For now, return empty - this would be implemented based on libp2p event handling
        Ok(Vec::new())
    }

    async fn check_peer_health(&self) -> Result<(), NetworkError> {
        // Check health of all connected peers via libp2p
        // For now, this is a no-op - would be implemented using libp2p ping or other health checks
        Ok(())
    }
}
