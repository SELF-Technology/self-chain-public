use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::RwLock;
use tokio::time::interval;
use libp2p::{
    identity,
    PeerId,
    Swarm,
    NetworkBehaviour,
    swarm::SwarmEvent,
    mdns::{Mdns, MdnsEvent},
    gossipsub::{Gossipsub, GossipsubEvent, GossipsubConfig},
    noise::{Keypair, NoiseConfig},
    tcp::TokioTcpConfig,
    yamux::YamuxConfig,
    Multiaddr,
    Transport,
};
use serde::{Deserialize, Serialize};
use std::collections::HashSet;
use std::collections::HashMap;
use crate::storage::cloud::CloudNode;
use crate::consensus::metrics::ConsensusMetrics;
use crate::network::metrics::PeerDiscoveryMetrics;
use anyhow::Result;

#[derive(Debug, Serialize, Deserialize)]
pub struct NodeInfo {
    pub peer_id: PeerId,
    pub addresses: Vec<Multiaddr>,
    pub capacity: u64,
    pub used: u64,
    pub last_seen: u64,
}

#[derive(NetworkBehaviour)]
#[behaviour(event_process = false)]
pub struct NodeDiscovery {
    mdns: Mdns,
    gossipsub: Gossipsub,
}

pub struct PeerDiscovery {
    known_peers: Arc<RwLock<HashSet<PeerId>>>,
    bootstrap_peers: Arc<RwLock<Vec<(PeerId, Multiaddr)>>>,
    peer_cache: Arc<RwLock<HashMap<PeerId, PeerStats>>>,
    metrics: Arc<PeerDiscoveryMetrics>,
    discovery_interval: Duration,
    max_peers: usize,
}

impl PeerDiscovery {
    pub fn new(metrics: Arc<PeerDiscoveryMetrics>) -> Self {
        Self {
            known_peers: Arc::new(RwLock::new(HashSet::new())),
            bootstrap_peers: Arc::new(RwLock::new(Vec::new())),
            peer_cache: Arc::new(RwLock::new(HashMap::new())),
            metrics: metrics,
            discovery_interval: Duration::from_secs(30),
            max_peers: 100,
        }
    }
}

pub struct CloudNodeManager {
    local_key: identity::Keypair,
    local_peer_id: PeerId,
    swarm: Swarm<NodeDiscovery>,
    nodes: Arc<RwLock<Vec<NodeInfo>>>,
    heartbeat_interval: tokio::time::Interval,
    peer_discovery: PeerDiscovery,
}

impl CloudNodeManager {
    pub async fn new(listen_port: u16, capacity: u64, metrics: Arc<PeerDiscoveryMetrics>) -> Self {
        // Create a random keypair for this node
        let local_key = identity::Keypair::generate_ed25519();
        let local_peer_id = PeerId::from(local_key.public());

        // Create a transport
        let noise_keys = Keypair::<noise::X25519>::new()
            .into_authentic(&local_key)
            .expect("Signing libp2p-noise static DH keypair failed.");

        let transport = TokioTcpConfig::new()
            .upgrade(yamux::YamuxConfig::default())
            .authenticate(NoiseConfig::xx(noise_keys).into_authenticated())
            .multiplex(YamuxConfig::default())
            .boxed();

        // Create a Gossipsub topic
        let topic = "self-chain".to_string();

        // Create a Gossipsub config
        let mut gossipsub_config = GossipsubConfig::default();
        gossipsub_config.max_transmit_size = 1_048_576; // 1MB

        // Create a Gossipsub network behaviour
        let mut gossipsub = Gossipsub::new(
            GossipsubConfig::default(),
            local_peer_id.clone(),
        ).expect("Valid gossipsub configuration");

        // Subscribe to our topic
        gossipsub
            .subscribe(&topic)
            .expect("Subscribed to topic");

        // Create an mDNS behaviour
        let mdns = Mdns::new().await.expect("Failed to create mDNS");

        // Create the swarm
        let mut swarm = Swarm::new(
            transport,
            NodeDiscovery {
                mdns,
                gossipsub,
            },
            local_peer_id.clone(),
        );

        // Listen on all interfaces and the given port
        Swarm::listen_on(
            &mut swarm,
            "/ip4/0.0.0.0/tcp/".to_string() + &listen_port.to_string(),
        ).expect("Failed to listen");

        // Create heartbeat interval
        let heartbeat_interval = interval(Duration::from_secs(30));

        let peer_discovery = PeerDiscovery::new(metrics);

        Self {
            local_key,
            local_peer_id,
            swarm,
            nodes: Arc::new(RwLock::new(Vec::new())),
            heartbeat_interval,
            peer_discovery,
        }
    }

    pub async fn start(&self) -> Result<(), String> {
        // Start the heartbeat task
        let nodes = self.nodes.clone();
        let local_peer_id = self.local_peer_id.clone();

        tokio::spawn(async move {
            loop {
                tokio::select! {
                    _ = self.heartbeat_interval.tick() => {
                        // Send heartbeat
                        let nodes = nodes.read().await;
                        for node in nodes.iter() {
                            // TODO: Send heartbeat to node
                        }
                    }
                    event = self.swarm.select_next_some() => {
                        match event {
                            SwarmEvent::NewListenAddr { address, .. } => {
                                println!("Listening on: {}", address);
                            }
                            SwarmEvent::Behaviour(event) => {
                                match event {
                                    NodeDiscoveryEvent::Mdns(MdnsEvent::Discovered(list)) => {
                                        for (peer_id, multiaddr) in list {
                                            // TODO: Add discovered node
                                        }
                                    }
                                    NodeDiscoveryEvent::Mdns(MdnsEvent::Expired(list)) => {
                                        for (peer_id, multiaddr) in list {
                                            // TODO: Remove expired node
                                        }
                                    }
                                    NodeDiscoveryEvent::Gossipsub(GossipsubEvent::Message { message, .. }) => {
                                        // TODO: Handle gossipsub message
                                    }
                                    _ => {}
                                }
                            }
                            _ => {}
                        }
                    }
                }
            }
        });

        Ok(())
    }

    pub async fn get_nodes(&self) -> Vec<NodeInfo> {
        self.nodes.read().await.clone()
    }

    pub async fn get_local_info(&self) -> NodeInfo {
        NodeInfo {
            peer_id: self.local_peer_id.clone(),
            addresses: self.swarm.listeners().cloned().collect(),
            capacity: 1_073_741_824, // 1GB default
            used: 0,
            last_seen: SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap()
                .as_secs(),
        }
    }
}
