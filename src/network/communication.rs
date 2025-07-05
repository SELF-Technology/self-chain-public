use std::sync::Arc;
use std::time::{Duration, SystemTime};
use std::collections::HashMap;
use tokio::sync::{RwLock, mpsc, oneshot};
use serde::{Deserialize, Serialize};
use anyhow::{Result, anyhow};
use tracing::{info, warn, error, debug};
use libp2p::{
    identity,
    PeerId,
    Swarm,
    NetworkBehaviour,
    swarm::SwarmEvent,
    gossipsub::{Gossipsub, GossipsubEvent, GossipsubConfig, MessageId, IdentTopic},
    noise::{X25519Spec, NoiseConfig},
    tcp::TokioTcpConfig,
    yamux::YamuxConfig,
    Multiaddr,
    Transport,
    request_response::{self, RequestResponse, ProtocolSupport, RequestResponseEvent, RequestResponseConfig, RequestResponseMessage},
    kad::{Kademlia, KademliaEvent, store::MemoryStore, KademliaConfig},
    identify::{Identify, IdentifyEvent, IdentifyConfig},
    core::upgrade,
};
use crate::blockchain::block::{Block, Transaction};
use crate::network::discovery::NodeInfo;
use crate::consensus::vote::{Vote, VotingResult};
use crate::consensus::peer_validator::{ValidationRequest, ValidationResponse, PeerValidator};
use uuid::Uuid;
use sha2::{Sha256, Digest};
use std::convert::TryFrom;
use tokio::time;
use futures::StreamExt;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum NodeMessage {
    // Basic blockchain messages
    Block(Block),
    Transaction(Transaction),
    GetBlocks(u64, u64),  // from_height, to_height
    BlockResponse(Vec<Block>),
    
    // Node status and discovery
    Heartbeat(NodeInfo),
    JoinNetwork(NodeInfo),
    LeaveNetwork(PeerId),
    PeerList(Vec<(PeerId, Multiaddr)>),
    
    // Validation and consensus
    ValidationRequest(ValidationRequest),
    ValidationResponse(ValidationResponse),
    VotingStart(Block),
    Vote(Vote),
    VotingResult(VotingResult),
    
    // Cloud storage operations
    StorageRequest(StorageRequestType),
    StorageResponse(StorageResponseType),
    
    // Error handling and recovery
    Error(NetworkError),
    RecoveryRequest(RecoveryType),
    RecoveryResponse(RecoveryData),
    
    // Health and metrics
    HealthCheck(HealthCheckType),
    HealthReport(HealthReport),
    MetricsRequest(MetricType),
    MetricsResponse(MetricsData),
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum StorageRequestType {
    Get(String),  // key
    Put(String, Vec<u8>),  // key, value
    Delete(String),  // key
    List(String),  // prefix
    Sync(String),  // collection_id
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum StorageResponseType {
    Value(Option<Vec<u8>>),  // value
    Success(bool),  // success
    Keys(Vec<String>),  // keys
    SyncResult(bool),  // success
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum NetworkError {
    ConnectionFailed(String),
    Timeout(String),
    ValidationFailed(String),
    StorageError(String),
    ProtocolError(String),
    SecurityError(String),
    InternalError(String),
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum RecoveryType {
    ResyncBlocks(u64),  // from_height
    ResyncState(String),  // state_id
    RejoinNetwork,
    ReconfigureNode,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct RecoveryData {
    pub success: bool,
    pub recovery_type: RecoveryType,
    pub message: String,
    pub data: Option<Vec<u8>>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum HealthCheckType {
    Full,
    Storage,
    Network,
    Consensus,
    Validation,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct HealthReport {
    pub node_id: PeerId,
    pub timestamp: u64,
    pub status: HealthStatus,
    pub details: HashMap<String, String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum HealthStatus {
    Healthy,
    Warning,
    Critical,
    Offline,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum MetricType {
    Performance,
    ResourceUsage,
    NetworkActivity,
    ValidationStats,
    ConsensusStats,
    StorageStats,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MetricsData {
    pub node_id: PeerId,
    pub timestamp: u64,
    pub metric_type: MetricType,
    pub data: HashMap<String, f64>,
}

// Protocol ID for direct request-response protocol
const PROTOCOL_VERSION: &str = "/self-chain/req-resp/1.0.0";

// Define custom protocol for request-response
#[derive(Debug, Clone)]
pub struct CloudProtocol;

type RequestResponseCodec = request_response::cbor::Cbor<NodeMessage, NodeMessage>;

#[derive(NetworkBehaviour)]
#[behaviour(event_process = false, out_event = "NodeCommunicationEvent")]
pub struct NodeCommunication {
    gossipsub: Gossipsub,
    request_response: RequestResponse<RequestResponseCodec>,
    kademlia: Kademlia<MemoryStore>,
    identify: Identify,
}

#[derive(Debug)]
pub enum NodeCommunicationEvent {
    Gossipsub(GossipsubEvent),
    RequestResponse(RequestResponseEvent<NodeMessage, NodeMessage>),
    Kademlia(KademliaEvent),
    Identify(IdentifyEvent),
}

impl From<GossipsubEvent> for NodeCommunicationEvent {
    fn from(event: GossipsubEvent) -> Self {
        NodeCommunicationEvent::Gossipsub(event)
    }
}

impl From<RequestResponseEvent<NodeMessage, NodeMessage>> for NodeCommunicationEvent {
    fn from(event: RequestResponseEvent<NodeMessage, NodeMessage>) -> Self {
        NodeCommunicationEvent::RequestResponse(event)
    }
}

impl From<KademliaEvent> for NodeCommunicationEvent {
    fn from(event: KademliaEvent) -> Self {
        NodeCommunicationEvent::Kademlia(event)
    }
}

impl From<IdentifyEvent> for NodeCommunicationEvent {
    fn from(event: IdentifyEvent) -> Self {
        NodeCommunicationEvent::Identify(event)
    }
}

pub struct NodeCommunicator {
    local_peer_id: PeerId,
    swarm: Option<Swarm<NodeCommunication>>,
    topics: HashMap<String, IdentTopic>,
    message_sender: tokio::sync::mpsc::Sender<NodeMessage>,
    message_receiver: tokio::sync::mpsc::Receiver<NodeMessage>,
    pending_requests: Arc<RwLock<HashMap<String, oneshot::Sender<Result<NodeMessage>>>>>,
    bootstrapped: Arc<RwLock<bool>>,
    connected_peers: Arc<RwLock<HashMap<PeerId, PeerStatus>>>,
    health_status: Arc<RwLock<HealthReport>>,
    circuit_breaker: Arc<RwLock<HashMap<String, CircuitBreaker>>>,
}

#[derive(Debug)]
pub struct CircuitBreaker {
    failures: usize,
    threshold: usize,
    reset_after: Duration,
    last_failure: SystemTime,
    open: bool,
}

impl CircuitBreaker {
    pub fn new(threshold: usize, reset_after: Duration) -> Self {
        Self {
            failures: 0,
            threshold,
            reset_after,
            last_failure: SystemTime::now(),
            open: false,
        }
    }

    pub fn record_failure(&mut self) -> bool {
        self.failures += 1;
        self.last_failure = SystemTime::now();
        
        if self.failures >= self.threshold {
            self.open = true;
        }
        
        self.open
    }
    
    pub fn is_open(&mut self) -> bool {
        // Auto-reset after specified duration
        if self.open && self.last_failure.elapsed().unwrap() > self.reset_after {
            self.reset();
        }
        
        self.open
    }
    
    pub fn reset(&mut self) {
        self.failures = 0;
        self.open = false;
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PeerStatus {
    addr: Multiaddr,
    connected_since: SystemTime,
    last_seen: SystemTime,
    success_count: usize,
    failure_count: usize,
    latency_ms: u64,
}

impl NodeCommunicator {
    pub async fn new(listen_port: u16) -> Self {
        // Create a random keypair for this node
        let local_key = identity::Keypair::generate_ed25519();
        let local_peer_id = PeerId::from(local_key.public());

        // Create message channel with larger buffer for cloud operations
        let (message_sender, message_receiver) = tokio::sync::mpsc::channel(1000);

        // Initialize with empty swarm, will be created in start() method
        Self {
            local_peer_id,
            swarm: None,
            topics: HashMap::new(),
            message_sender,
            message_receiver,
            pending_requests: Arc::new(RwLock::new(HashMap::new())),
            bootstrapped: Arc::new(RwLock::new(false)),
            connected_peers: Arc::new(RwLock::new(HashMap::new())),
            health_status: Arc::new(RwLock::new(HealthReport {
                node_id: local_peer_id,
                timestamp: SystemTime::now().duration_since(SystemTime::UNIX_EPOCH).unwrap().as_secs(),
                status: HealthStatus::Healthy,
                details: HashMap::new(),
            })),
            circuit_breaker: Arc::new(RwLock::new(HashMap::new())),
        }
    }

    pub async fn initialize(&mut self, bootstrap_nodes: Vec<Multiaddr>) -> Result<()> {
        // Create a transport with noise encryption and yamux multiplexing
        let local_key = identity::Keypair::generate_ed25519();
        let local_peer_id = self.local_peer_id.clone();

        // Define the transport with encryption and multiplexing
        let transport = TokioTcpConfig::new()
            .nodelay(true)
            .upgrade(upgrade::Version::V1)
            .authenticate(NoiseConfig::xx::<X25519Spec>::new(local_key.clone().into()))
            .multiplex(YamuxConfig::default())
            .boxed();

        // Create topics for different message categories
        let mut topics = HashMap::new();
        for topic_name in &["blocks", "transactions", "consensus", "validation", "storage", "health"] {
            let topic = IdentTopic::new(format!("self-chain/{}", topic_name));
            topics.insert(topic_name.to_string(), topic.clone());
        }
        self.topics = topics;

        // Setup gossipsub with message signing and validation
        let gossipsub_config = GossipsubConfig::default()
            .heartbeat_interval(Duration::from_secs(10))
            .validation_mode(libp2p::gossipsub::ValidationMode::Strict)
            .message_id_fn(|message: &GossipsubMessage| {
                let mut s = DefaultHasher::new();
                let data = &message.data;
                data.hash(&mut s);
                MessageId::from(s.finish().to_string())
            });

        let mut gossipsub = Gossipsub::new(MessageAuthenticity::Signed(local_key.clone()), gossipsub_config)
            .map_err(|e| anyhow!("Failed to create gossipsub: {}", e))?;
            
        // Subscribe to all topics
        for (_, topic) in &self.topics {
            gossipsub.subscribe(topic)
                .map_err(|e| anyhow!("Failed to subscribe to topic: {}", e))?;
        }

        // Setup kademlia for peer discovery
        let mut kademlia_config = KademliaConfig::default();
        kademlia_config.set_query_timeout(Duration::from_secs(30));
        
        let store = MemoryStore::new(local_peer_id);
        let mut kademlia = Kademlia::with_config(local_peer_id, store, kademlia_config);

        // Add bootstrap nodes to kademlia
        for addr in bootstrap_nodes {
            debug!("Adding bootstrap node: {:?}", addr);
            if let Some(peer_id) = extract_peer_id(&addr) {
                kademlia.add_address(&peer_id, addr.clone());
            } else {
                warn!("Could not extract peer id from: {:?}", addr);
            }
        }

        // Setup identify protocol
        let identify = Identify::new(IdentifyConfig::new(
            "/self-chain/1.0.0".to_string(),
            local_key.public(),
        ));

        // Setup request-response protocol
        let request_response_config = RequestResponseConfig::default();
        let request_response = RequestResponse::new(
            request_response::cbor::Codec::<NodeMessage, NodeMessage>::default(),
            vec![(CloudProtocol, ProtocolSupport::Full)],
            request_response_config
        );

        // Create the network behavior
        let behavior = NodeCommunication {
            gossipsub,
            request_response,
            kademlia,
            identify,
        };

        // Build the swarm
        let mut swarm = Swarm::new(transport, behavior, local_peer_id);

        // Listen on all interfaces at the specified port
        let listen_addr = format!("/ip4/0.0.0.0/tcp/0");
        swarm.listen_on(listen_addr.parse()?)
            .map_err(|e| anyhow!("Failed to listen: {}", e))?;

        // Store the swarm
        self.swarm = Some(swarm);

        Ok(())
    }
    
    fn extract_peer_id(addr: &Multiaddr) -> Option<PeerId> {
        addr.iter()
            .filter_map(|proto| {
                if let libp2p::multiaddr::Protocol::P2p(hash) = proto {
                    PeerId::from_multihash(hash).ok()
                } else {
                    None
                }
            }).next()
    }

    pub async fn start(&self) -> Result<()> {
        if self.swarm.is_none() {
            return Err(anyhow!("Swarm not initialized. Call initialize() first"));
        }

        let mut swarm = self.swarm.as_ref().unwrap().clone();
        let pending_requests = self.pending_requests.clone();
        let connected_peers = self.connected_peers.clone();
        let health_status = self.health_status.clone();
        let bootstrapped = self.bootstrapped.clone();
        let message_sender = self.message_sender.clone();
        let circuit_breaker = self.circuit_breaker.clone();

        // Start kademlia bootstrap process
        if let Some(NodeCommunication { kademlia, .. }) = swarm.behaviour_mut().get_mut() {
            info!("Starting kademlia bootstrap");
            match kademlia.bootstrap() {
                Ok(_) => info!("Bootstrap started"),
                Err(e) => warn!("Bootstrap error: {}", e),
            }
        }

        // Start periodic heartbeat and health check
        let health_status_clone = health_status.clone();
        let connected_peers_clone = connected_peers.clone();
        tokio::spawn(async move {
            let mut interval = time::interval(Duration::from_secs(60));
            loop {
                interval.tick().await;
                Self::send_heartbeat(&mut swarm, &health_status_clone, &connected_peers_clone).await;
                Self::check_peer_health(&connected_peers_clone).await;
            }
        });

        // Start the main event loop
        tokio::spawn(async move {
            loop {
                tokio::select! {
                    event = swarm.select_next_some() => {
                        match event {
                            SwarmEvent::NewListenAddr { address, .. } => {
                                info!("Node listening on: {}", address);
                            },
                            SwarmEvent::Behaviour(NodeCommunicationEvent::Gossipsub(GossipsubEvent::Message { 
                                propagation_source, message_id, message
                            })) => {
                                debug!("Received gossip message from {}: {}", propagation_source, message_id);
                                
                                match serde_json::from_slice::<NodeMessage>(&message.data) {
                                    Ok(msg) => {
                                        // Update peer status
                                        Self::update_peer_status(&connected_peers, &propagation_source, true).await;
                                        
                                        // Forward to message handler
                                        if let Err(e) = message_sender.send(msg.clone()).await {
                                            error!("Failed to send message to handler: {}", e);
                                        }
                                        
                                        // Handle specific message types directly if needed
                                        match &msg {
                                            NodeMessage::HealthCheck(check_type) => {
                                                let report = Self::generate_health_report(&health_status).await;
                                                let response = NodeMessage::HealthReport(report);
                                                
                                                // Send health report back via gossipsub
                                                if let Some(gossipsub) = swarm.behaviour_mut().gossipsub.as_mut() {
                                                    match serde_json::to_vec(&response) {
                                                        Ok(data) => {
                                                            if let Err(e) = gossipsub.publish(IdentTopic::new("self-chain/health"), data) {
                                                                error!("Failed to publish health report: {}", e);
                                                            }
                                                        }
                                                        Err(e) => error!("Failed to serialize health report: {}", e),
                                                    }
                                                }
                                            },
                                            NodeMessage::Error(err) => {
                                                warn!("Peer {} reported error: {:?}", propagation_source, err);
                                                
                                                // Update circuit breaker if error is from a specific peer
                                                Self::update_circuit_breaker(&circuit_breaker, propagation_source.to_string()).await;
                                            },
                                            _ => {}
                                        }
                                    },
                                    Err(e) => error!("Failed to deserialize gossip message: {}", e)
                                }
                            },
                            SwarmEvent::Behaviour(NodeCommunicationEvent::RequestResponse(event)) => {
                                match event {
                                    RequestResponseEvent::Message { peer, message, .. } => {
                                        match message {
                                            RequestResponseMessage::Request { request_id, request } => {
                                                debug!("Received request from {}: {:?}", peer, request);
                                                
                                                // Handle request
                                                let response = match Self::handle_request(request, &connected_peers, &health_status).await {
                                                    Ok(res) => res,
                                                    Err(e) => NodeMessage::Error(NetworkError::InternalError(e.to_string())),
                                                };
                                                
                                                // Send response
                                                if let Some(req_resp) = swarm.behaviour_mut().request_response.as_mut() {
                                                    if let Err(e) = req_resp.send_response(request_id, response) {
                                                        error!("Failed to send response: {}", e);
                                                    }
                                                }
                                                
                                                Self::update_peer_status(&connected_peers, &peer, true).await;
                                            },
                                            RequestResponseMessage::Response { request_id, response } => {
                                                debug!("Received response from {}: {:?}", peer, response);
                                                
                                                // Find and complete pending request
                                                let id = request_id.to_string();
                                                let mut requests = pending_requests.write().await;
                                                if let Some(sender) = requests.remove(&id) {
                                                    if let Err(_) = sender.send(Ok(response)) {
                                                        error!("Failed to complete pending request: receiver dropped");
                                                    }
                                                }
                                                
                                                Self::update_peer_status(&connected_peers, &peer, true).await;
                                            }
                                        }
                                    },
                                    RequestResponseEvent::OutboundFailure { peer, request_id, error, .. } => {
                                        error!("Outbound request to {} failed: {}", peer, error);
                                        
                                        // Complete pending request with error
                                        let id = request_id.to_string();
                                        let mut requests = pending_requests.write().await;
                                        if let Some(sender) = requests.remove(&id) {
                                            if let Err(_) = sender.send(Err(anyhow!("Request failed: {}", error))) {
                                                error!("Failed to complete pending request with error: receiver dropped");
                                            }
                                        }
                                        
                                        Self::update_peer_status(&connected_peers, &peer, false).await;
                                        Self::update_circuit_breaker(&circuit_breaker, peer.to_string()).await;
                                    },
                                    RequestResponseEvent::InboundFailure { peer, error, .. } => {
                                        error!("Inbound request from {} failed: {}", peer, error);
                                        Self::update_peer_status(&connected_peers, &peer, false).await;
                                    },
                                    _ => {}
                                }
                            },
                            SwarmEvent::Behaviour(NodeCommunicationEvent::Kademlia(KademliaEvent::BootstrapResult(res))) => {
                                match res {
                                    Ok(_) => {
                                        info!("Kademlia bootstrap succeeded");
                                        let mut bs = bootstrapped.write().await;
                                        *bs = true;
                                    },
                                    Err(e) => {
                                        warn!("Kademlia bootstrap failed: {}", e);
                                        // Retry bootstrap after delay
                                        let kad = &mut swarm.behaviour_mut().kademlia;
                                        tokio::spawn(async move {
                                            tokio::time::sleep(Duration::from_secs(30)).await;
                                            match kad.bootstrap() {
                                                Ok(_) => info!("Bootstrap retry started"),
                                                Err(e) => error!("Bootstrap retry failed: {}", e),
                                            }
                                        });
                                    }
                                }
                            },
                            SwarmEvent::Behaviour(NodeCommunicationEvent::Identify(IdentifyEvent::Received { peer_id, info })) => {
                                info!("Identified peer: {} with protocol version: {}", peer_id, info.protocol_version);
                                
                                // Add peer to kademlia routing table if it's running the same protocol
                                if info.protocol_version.starts_with("/self-chain") {
                                    for addr in info.listen_addrs {
                                        swarm.behaviour_mut().kademlia.add_address(&peer_id, addr);
                                    }
                                }
                            },
                            _ => {}
                        }
                    }
                }
            }
        });

        Ok(())
    }
    
    async fn send_heartbeat(
        swarm: &mut Swarm<NodeCommunication>,
        health_status: &Arc<RwLock<HealthReport>>,
        connected_peers: &Arc<RwLock<HashMap<PeerId, PeerStatus>>>
    ) {
        let report = Self::generate_health_report(health_status).await;
        
        let heartbeat = NodeMessage::Heartbeat(NodeInfo {
            peer_id: report.node_id.to_string(),
            timestamp: report.timestamp,
            health_status: match report.status {
                HealthStatus::Healthy => "healthy".to_string(),
                HealthStatus::Warning => "warning".to_string(),
                HealthStatus::Critical => "critical".to_string(),
                HealthStatus::Offline => "offline".to_string(),
            },
            connected_peers: connected_peers.read().await.len() as u32,
        });
        
        // Publish heartbeat
        if let Ok(data) = serde_json::to_vec(&heartbeat) {
            if let Some(gossipsub) = swarm.behaviour_mut().gossipsub.as_mut() {
                if let Err(e) = gossipsub.publish(IdentTopic::new("self-chain/health"), data) {
                    error!("Failed to publish heartbeat: {}", e);
                }
            }
        }
    }
    
    async fn generate_health_report(health_status: &Arc<RwLock<HealthReport>>) -> HealthReport {
        let mut report = health_status.read().await.clone();
        report.timestamp = SystemTime::now().duration_since(SystemTime::UNIX_EPOCH).unwrap().as_secs();
        report
    }
    
    async fn check_peer_health(connected_peers: &Arc<RwLock<HashMap<PeerId, PeerStatus>>>) {
        let now = SystemTime::now();
        let mut peers = connected_peers.write().await;
        
        // Remove peers that haven't been seen in 5 minutes
        peers.retain(|_, status| {
            match status.last_seen.elapsed() {
                Ok(elapsed) => elapsed < Duration::from_secs(300),
                Err(_) => false,
            }
        });
    }
    
    async fn update_peer_status(
        connected_peers: &Arc<RwLock<HashMap<PeerId, PeerStatus>>>,
        peer_id: &PeerId,
        success: bool
    ) {
        let now = SystemTime::now();
        let mut peers = connected_peers.write().await;
        
        let status = peers.entry(*peer_id).or_insert(PeerStatus {
            addr: "/ip4/0.0.0.0".parse().unwrap(), // Placeholder, will be updated when we get peer address
            connected_since: now,
            last_seen: now,
            success_count: 0,
            failure_count: 0,
            latency_ms: 0,
        });
        
        status.last_seen = now;
        
        if success {
            status.success_count += 1;
        } else {
            status.failure_count += 1;
        }
    }
    
    async fn update_circuit_breaker(
        circuit_breakers: &Arc<RwLock<HashMap<String, CircuitBreaker>>>,
        target: String
    ) {
        let mut breakers = circuit_breakers.write().await;
        
        let breaker = breakers.entry(target.clone()).or_insert(CircuitBreaker::new(
            5,  // Threshold: open after 5 failures
            Duration::from_secs(300)  // Reset after 5 minutes
        ));
        
        if breaker.record_failure() {
            warn!("Circuit breaker opened for target: {}", target);
        }
    }
    
    async fn handle_request(
        request: NodeMessage,
        connected_peers: &Arc<RwLock<HashMap<PeerId, PeerStatus>>>,
        health_status: &Arc<RwLock<HealthReport>>
    ) -> Result<NodeMessage> {
        match request {
            NodeMessage::HealthCheck(_) => {
                let report = Self::generate_health_report(health_status).await;
                Ok(NodeMessage::HealthReport(report))
            },
            NodeMessage::PeerList(_) => {
                let peers = connected_peers.read().await;
                let peer_list = peers.iter()
                    .map(|(id, status)| (*id, status.addr.clone()))
                    .collect();
                Ok(NodeMessage::PeerList(peer_list))
            },
            NodeMessage::RecoveryRequest(recovery_type) => {
                // Handle recovery request
                match recovery_type {
                    RecoveryType::ResyncBlocks(height) => {
                        // TODO: Implement block resync logic
                        Ok(NodeMessage::RecoveryResponse(RecoveryData {
                            success: true,
                            recovery_type: recovery_type,
                            message: "Resync initiated".to_string(),
                            data: None,
                        }))
                    },
                    RecoveryType::ResyncState(state_id) => {
                        // TODO: Implement state resync logic
                        Ok(NodeMessage::RecoveryResponse(RecoveryData {
                            success: true,
                            recovery_type: recovery_type,
                            message: "State resync initiated".to_string(),
                            data: None,
                        }))
                    },
                    RecoveryType::RejoinNetwork => {
                        // TODO: Implement network rejoin logic
                        Ok(NodeMessage::RecoveryResponse(RecoveryData {
                            success: true,
                            recovery_type: recovery_type,
                            message: "Network rejoin initiated".to_string(),
                            data: None,
                        }))
                    },
                    RecoveryType::ReconfigureNode => {
                        // TODO: Implement node reconfiguration logic
                        Ok(NodeMessage::RecoveryResponse(RecoveryData {
                            success: true,
                            recovery_type: recovery_type,
                            message: "Node reconfiguration initiated".to_string(),
                            data: None,
                        }))
                    },
                }
            },
            _ => {
                Err(anyhow!("Unhandled request type"))
            }
        }
    }

    pub async fn broadcast_message(&self, message: NodeMessage) -> Result<()> {
        if self.swarm.is_none() {
            return Err(anyhow!("Swarm not initialized. Call initialize() first"));
        }
        
        // Determine appropriate topic based on message type
        let topic_name = match &message {
            NodeMessage::Block(_) | NodeMessage::BlockRequest(_) | 
            NodeMessage::BlockResponse(_) | NodeMessage::BlocksRequest(_) | 
            NodeMessage::BlocksResponse(_) => "blocks",
            
            NodeMessage::Transaction(_) | NodeMessage::TransactionRequest(_) | 
            NodeMessage::TransactionResponse(_) => "transactions",
            
            NodeMessage::ConsensusMessage(_) | NodeMessage::ConsensusVote(_) | 
            NodeMessage::ConsensusProposal(_) | NodeMessage::ConsensusFinality(_) => "consensus",
            
            NodeMessage::ValidationRequest(_) | NodeMessage::ValidationResponse(_) => "validation",
            
            NodeMessage::HealthCheck(_) | NodeMessage::HealthReport(_) | 
            NodeMessage::Heartbeat(_) => "health",
            
            _ => "blocks"  // Default topic
        };
        
        let topic_key = format!("self-chain/{}", topic_name);
        let topic = IdentTopic::new(topic_key);

        // Serialize message
        let serialized = serde_json::to_vec(&message)
            .map_err(|e| anyhow!("Failed to serialize message: {}", e))?;

        // Get mutable reference to swarm and behavior
        let mut swarm = self.swarm.as_ref().unwrap().clone();
        
        if let Some(gossipsub) = swarm.behaviour_mut().gossipsub.as_mut() {
            gossipsub.publish(topic, serialized)
                .map_err(|e| anyhow!("Failed to publish message: {}", e))?;
        } else {
            return Err(anyhow!("Gossipsub not available"));
        }
        
        Ok(())
    pub async fn send_message(&self, peer_id: PeerId, message: NodeMessage) -> Result<NodeMessage> {
        if self.swarm.is_none() {
            return Err(anyhow!("Swarm not initialized. Call initialize() first"));
        }
        
        // Check circuit breaker for this peer
        let target = peer_id.to_string();
        let circuit_open = {
            let mut breakers = self.circuit_breaker.write().await;
            if let Some(breaker) = breakers.get_mut(&target) {
                if breaker.is_open() {
                    true 
                } else {
                    false
                }
            } else {
                false
            }
        };
        
        if circuit_open {
            return Err(anyhow!("Circuit breaker open for peer: {}", peer_id));
        }
        
        // Create oneshot channel for response
        let (sender, receiver) = oneshot::channel();
        
        // Generate unique request ID
        let request_id = Uuid::new_v4().to_string();
        
        // Store sender in pending requests
        {
            let mut requests = self.pending_requests.write().await;
            requests.insert(request_id.clone(), sender);
        }
        
        // Get mutable reference to swarm and behavior
        let mut swarm = self.swarm.as_ref().unwrap().clone();
        
        // Send request
        if let Some(req_resp) = swarm.behaviour_mut().request_response.as_mut() {
            req_resp.send_request(&peer_id, message);
        } else {
            return Err(anyhow!("Request-response protocol not available"));
        }
        
        // Wait for response with timeout
        let result = tokio::time::timeout(Duration::from_secs(30), receiver).await;
        
        // Clean up from pending requests if timed out
        if result.is_err() {
            let mut requests = self.pending_requests.write().await;
            requests.remove(&request_id);
            return Err(anyhow!("Request timed out"));
        }
        
        // Handle response
        match result.unwrap() {
            Ok(res) => res,
            Err(e) => Err(anyhow!("Failed to receive response: {}", e)),
        }
    }
    
    pub async fn connect_to_peer(&self, addr: Multiaddr) -> Result<()> {
        if self.swarm.is_none() {
            return Err(anyhow!("Swarm not initialized. Call initialize() first"));
        }
        
        // Extract peer ID if present in multiaddr
        let peer_id = Self::extract_peer_id(&addr);
        
        // Get mutable reference to swarm
        let mut swarm = self.swarm.as_ref().unwrap().clone();
        
        // Connect to the address
        if let Some(id) = peer_id {
            info!("Dialing peer {} at {}", id, addr);
            swarm.dial(addr.clone())
                .map_err(|e| anyhow!("Failed to dial peer: {}", e))?;
            
            // Add to Kademlia DHT
            swarm.behaviour_mut().kademlia.add_address(&id, addr);
        } else {
            info!("Dialing unknown peer at {}", addr);
            swarm.dial(addr)
                .map_err(|e| anyhow!("Failed to dial address: {}", e))?;
        }
        
        Ok(())
    }
    
    pub async fn get_connected_peers(&self) -> Result<Vec<(PeerId, Multiaddr)>> {
        if self.swarm.is_none() {
            return Err(anyhow!("Swarm not initialized. Call initialize() first"));
        }
        
        let peers = self.connected_peers.read().await;
        
        let result = peers.iter()
            .map(|(id, status)| (*id, status.addr.clone()))
            .collect();
            
        Ok(result)
    }
    
    pub async fn recover_from_network_error(&self, error_type: NetworkError) -> Result<RecoveryData> {
        let recovery_type = match error_type {
            NetworkError::BlockchainSyncError(_) => RecoveryType::ResyncBlocks(0),
            NetworkError::PeerConnectionError(_) => RecoveryType::RejoinNetwork,
            NetworkError::NetworkPartition(_) => RecoveryType::RejoinNetwork,
            NetworkError::StateCorruption(_) => RecoveryType::ResyncState(0),
            _ => RecoveryType::ReconfigureNode,
        };
        
        // Get all connected peers
        let peers = self.get_connected_peers().await?
            .into_iter()
            .map(|(id, _)| id)
            .collect::<Vec<_>>();
        
        if peers.is_empty() {
            return Err(anyhow!("No peers connected for recovery"));
        }
        
        // Try each peer until successful
        for peer_id in peers {
            match self.send_message(peer_id, NodeMessage::RecoveryRequest(recovery_type.clone())).await {
                Ok(NodeMessage::RecoveryResponse(data)) => {
                    return Ok(data);
                },
                _ => continue,
            }
        }
        
        Err(anyhow!("Failed to recover from network error"))
    }
    }

    pub async fn receive_message(&self) -> Option<NodeMessage> {
        self.message_receiver.recv().await
    }
}
