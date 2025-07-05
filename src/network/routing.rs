use std::collections::HashMap;
use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::RwLock;
use libp2p::{PeerId, Multiaddr, identity::Keypair};
use libp2p::gossipsub::{Gossipsub, GossipsubConfig, MessageAuthenticity};
use libp2p::kad::{Kademlia, KademliaConfig};
use libp2p::swarm::{NetworkBehaviour, SwarmBuilder, Swarm};
use libp2p::tcp::TokioTcpConfig;
use libp2p::dns::DnsConfig;
use libp2p::noise::{NoiseConfig, X25519Spec};
use libp2p::yamux::YamuxConfig;
use libp2p::core::transport::Boxed;
use libp2p::core::upgrade::Version;
use libp2p::core::identity::Keypair as Libp2pKeypair;
use anyhow::Result;
use crate::network::metrics::PeerDiscoveryMetrics;
use crate::network::message::NetworkMessage;
use crate::serialization::SerializationService;

pub struct MessageRouter {
    swarm: Swarm<RouterBehaviour>,
    metrics: Arc<PeerDiscoveryMetrics>,
    serialization: Arc<SerializationService>,
    routing_table: Arc<RwLock<HashMap<PeerId, RoutingStats>>>,
    routing_config: RoutingConfig,
}

#[derive(Debug, Clone)]
pub struct RoutingConfig {
    pub max_hops: u32,
    pub routing_table_size: usize,
    pub message_ttl: Duration,
    pub flood_threshold: usize,
    pub routing_timeout: Duration,
}

#[derive(Debug, Clone)]
struct RoutingStats {
    last_seen: SystemTime,
    message_count: u64,
    success_rate: f64,
    latency: Duration,
    routing_score: f64,
}

#[derive(NetworkBehaviour)]
struct RouterBehaviour {
    gossipsub: Gossipsub,
    kademlia: Kademlia,
    #[behaviour(ignore)]
    metrics: Arc<PeerDiscoveryMetrics>,
}

impl MessageRouter {
    pub async fn new(
        keypair: Libp2pKeypair,
        listen_port: u16,
        metrics: Arc<PeerDiscoveryMetrics>,
        serialization: Arc<SerializationService>,
    ) -> Result<Self> {
        let transport = self::build_transport(keypair.clone())?;
        
        let config = RoutingConfig {
            max_hops: 10,
            routing_table_size: 1000,
            message_ttl: Duration::from_secs(300), // 5 minutes
            flood_threshold: 100,
            routing_timeout: Duration::from_secs(10),
        };
        
        let routing_table = Arc::new(RwLock::new(HashMap::new()));
        
        let router = Self {
            swarm: self::build_swarm(transport)?,
            metrics,
            serialization,
            routing_table,
            routing_config: config,
        };
        
        // Start listening
        router.swarm.listen_on(format!("/ip4/0.0.0.0/tcp/{}", listen_port).parse()?)?;
        
        Ok(router)
    }

    pub async fn start(&self) -> Result<()> {
        let mut swarm = self.swarm.clone();
        tokio::spawn(async move {
            loop {
                match swarm.next_event().await {
                    Ok(event) => {
                        self.handle_event(event).await;
                    }
                    Err(e) => {
                        error!("Swarm event error: {}", e);
                        self.metrics.increment_peer_connection_errors();
                    }
                }
            }
        });
        
        Ok(())
    }

    async fn handle_event(&self, event: SwarmEvent<RouterBehaviourEvent, anyhow::Error>) {
        match event {
            SwarmEvent::NewListenAddr { address, .. } => {
                info!("Listening on: {}", address);
            }
            SwarmEvent::ConnectionEstablished { peer_id, .. } => {
                self.metrics.increment_peers_discovered();
                self.update_routing_table(&peer_id, true).await;
            }
            SwarmEvent::ConnectionClosed { peer_id, .. } => {
                self.update_routing_table(&peer_id, false).await;
            }
            SwarmEvent::Behaviour(event) => {
                self.handle_behaviour_event(event).await;
            }
            _ => {}
        }
    }

    async fn handle_behaviour_event(&self, event: RouterBehaviourEvent) {
        match event {
            RouterBehaviourEvent::Gossipsub(event) => {
                self.handle_gossipsub_event(event).await;
            }
            RouterBehaviourEvent::Kademlia(event) => {
                self.handle_kademlia_event(event).await;
            }
        }
    }

    async fn handle_gossipsub_event(&self, event: GossipsubEvent) {
        match event {
            GossipsubEvent::Message { message, .. } => {
                if let Ok(msg) = self.serialization.deserialize::<NetworkMessage>(&message.data) {
                    self.metrics.observe_peer_latency(message.received - message.sent);
                    self.metrics.observe_peer_response_rate(1.0);
                    self.metrics.increment_peers_discovered();
                    
                    // Forward message if needed
                    if !self.is_local_message(&msg) {
                        self.forward_message(msg).await;
                    }
                }
            }
            _ => {}
        }
    }

    async fn handle_kademlia_event(&self, event: KademliaEvent) {
        match event {
            KademliaEvent::RoutingUpdated { peer, .. } => {
                self.metrics.observe_peer_latency(SystemTime::now().duration_since(peer.last_seen)?);
                self.metrics.observe_peer_response_rate(peer.response_rate);
                self.metrics.observe_peer_reputation_score(peer.reputation_score);
            }
            _ => {}
        }
    }

    async fn forward_message(&self, msg: NetworkMessage) {
        let mut routing_table = self.routing_table.write().await;
        let peers = routing_table.iter()
            .filter(|(_, stats)| stats.routing_score >= 0.5)
            .map(|(id, _)| id)
            .take(self.routing_config.flood_threshold)
            .cloned()
            .collect::<Vec<_>>();
            
        for peer in peers {
            if let Err(e) = self.swarm.send_message(&peer, msg.clone()).await {
                error!("Failed to forward message to {}: {}", peer, e);
                self.metrics.increment_peer_connection_errors();
            }
        }
    }

    async fn update_routing_table(&self, peer_id: &PeerId, is_connected: bool) {
        let mut routing_table = self.routing_table.write().await;
        
        let stats = routing_table.entry(peer_id.clone())
            .or_insert_with(|| RoutingStats {
                last_seen: SystemTime::now(),
                message_count: 0,
                success_rate: 1.0,
                latency: Duration::from_secs(0),
                routing_score: 1.0,
            });
            
        if is_connected {
            stats.last_seen = SystemTime::now();
            stats.message_count += 1;
            stats.routing_score = self.calculate_routing_score(stats);
            
            self.metrics.observe_peer_latency(stats.latency.as_secs_f64());
            self.metrics.observe_peer_response_rate(stats.success_rate);
            self.metrics.observe_peer_reputation_score(stats.routing_score);
        } else {
            routing_table.remove(peer_id);
        }
    }

    fn calculate_routing_score(&self, stats: &RoutingStats) -> f64 {
        let uptime = SystemTime::now().duration_since(stats.last_seen)
            .unwrap_or(Duration::from_secs(0))
            .as_secs_f64();
            
        let score = (
            stats.success_rate * 0.4 +
            uptime.min(86400.0) / 86400.0 * 0.3 +
            stats.routing_score * 0.3
        ).min(1.0);
        
        score
    }

    fn is_local_message(&self, msg: &NetworkMessage) -> bool {
        // TODO: Implement local message detection
        false
    }
}

fn build_transport(keypair: Libp2pKeypair) -> Result<Boxed<(PeerId, Multiaddr)>> {
    let noise_keys = noise::Keypair::<X25519Spec>::new()
        .into_authentic(&keypair)
        .expect("Signing can't fail.");

    Ok(TokioTcpConfig::new()
        .nodelay(true)
        .upgrade(Version::V1)
        .authenticate(NoiseConfig::xx(noise_keys).into_authenticated())
        .multiplex(YamuxConfig::default())
        .timeout(Duration::from_secs(20))
        .boxed())
}

fn build_swarm(transport: Boxed<(PeerId, Multiaddr)>) -> Result<Swarm<RouterBehaviour>> {
    let local_peer_id = transport.local_peer_id();
    
    // Create Gossipsub behaviour
    let gossipsub_config = GossipsubConfig::default();
    let gossipsub = Gossipsub::new(
        MessageAuthenticity::Signed(keypair.clone()),
        gossipsub_config,
    )?;
    
    // Create Kademlia behaviour
    let kademlia_config = KademliaConfig::default();
    let kademlia = Kademlia::new(local_peer_id, kademlia_config);
    
    // Create router behaviour
    let behaviour = RouterBehaviour {
        gossipsub,
        kademlia,
        metrics: metrics.clone(),
    };
    
    // Create swarm
    let swarm = SwarmBuilder::new(transport, behaviour, local_peer_id)
        .executor(Box::new(|fut| {
            tokio::spawn(fut);
        }))
        .build();
    
    Ok(swarm)
}
