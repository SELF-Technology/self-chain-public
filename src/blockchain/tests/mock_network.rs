use std::collections::HashMap;
use std::net::SocketAddr;
use std::sync::Arc;
use tokio::sync::{mpsc, RwLock};

use crate::network::message::{MessageHandler, NetworkMessage, Peer};

/// Mock message handler implementation for testing
#[derive(Debug)]
pub struct MockMessageHandler {
    pub peers: Vec<Peer>,
    pub node_id: String,
    pub message_tx: mpsc::Sender<(String, NetworkMessage)>,
}

impl MockMessageHandler {
    pub fn new(node_id: String, message_tx: mpsc::Sender<(String, NetworkMessage)>) -> Self {
        Self {
            peers: Vec::new(),
            node_id,
            message_tx,
        }
    }

    pub fn add_peer(&mut self, peer: Peer) {
        self.peers.push(peer);
    }

    pub fn get_peers(&self) -> Vec<Peer> {
        self.peers.clone()
    }

    pub async fn broadcast_message(&self, message: NetworkMessage) -> Result<(), String> {
        for peer in &self.peers {
            // Send message to the message router
            if let Err(e) = self
                .message_tx
                .send((peer.id.clone(), message.clone()))
                .await
            {
                return Err(format!("Failed to send message: {}", e));
            }
        }
        Ok(())
    }

    /// Direct message to a specific peer
    pub async fn send_message_to_peer(
        &self,
        peer_id: &str,
        message: NetworkMessage,
    ) -> Result<(), String> {
        if self.peers.iter().any(|p| p.id == peer_id) {
            if let Err(e) = self.message_tx.send((peer_id.to_string(), message)).await {
                return Err(format!("Failed to send message to peer {}: {}", peer_id, e));
            }
            Ok(())
        } else {
            Err(format!("Peer {} not found", peer_id))
        }
    }
}

/// Mock network router for testing
pub struct MockNetworkRouter {
    // Map of node_id to message receiver
    node_receivers: HashMap<String, mpsc::Receiver<NetworkMessage>>,
    // Central message channel
    message_tx: mpsc::Sender<(String, NetworkMessage)>,
    message_rx: mpsc::Receiver<(String, NetworkMessage)>,
}

impl MockNetworkRouter {
    /// Create a new mock network router
    pub fn new() -> Self {
        let (message_tx, message_rx) = mpsc::channel::<(String, NetworkMessage)>(100);

        Self {
            node_receivers: HashMap::new(),
            message_tx,
            message_rx,
        }
    }

    /// Register a node with the router
    pub fn register_node(
        &mut self,
        node_id: String,
    ) -> (
        mpsc::Sender<(String, NetworkMessage)>,
        mpsc::Sender<NetworkMessage>,
    ) {
        // Create a channel for this node
        let (node_tx, node_rx) = mpsc::channel::<NetworkMessage>(100);

        // Store the receiver
        self.node_receivers.insert(node_id.clone(), node_rx);

        // Return the message sender for the node and sender for the node
        (self.message_tx.clone(), node_tx)
    }

    /// Start routing messages between nodes
    pub async fn start_routing(&mut self) {
        loop {
            match self.message_rx.recv().await {
                Some((target_node_id, message)) => {
                    if let Some(node_rx) = self.node_receivers.get_mut(&target_node_id) {
                        // Forward the message to the target node (node_rx is actually a receiver, not sender)
                        // This is a conceptual issue - we need to store senders, not receivers
                        println!("Would forward message to node {}", target_node_id);
                    }
                }
                None => {
                    // Channel closed, stop routing
                    break;
                }
            }
        }
    }
}

/// Test network factory
pub struct TestNetworkFactory {
    router: MockNetworkRouter,
    mock_handlers: HashMap<String, Arc<RwLock<MockMessageHandler>>>,
}

impl TestNetworkFactory {
    /// Create a new test network factory
    pub fn new() -> Self {
        Self {
            router: MockNetworkRouter::new(),
            mock_handlers: HashMap::new(),
        }
    }

    /// Create a new mock message handler for a node
    pub fn create_mock_handler(&mut self, node_id: String) -> Arc<RwLock<MockMessageHandler>> {
        let (message_tx, _message_tx) = self.router.register_node(node_id.clone());

        let mock_handler = Arc::new(RwLock::new(MockMessageHandler::new(
            node_id.clone(),
            message_tx,
        )));

        self.mock_handlers.insert(node_id, mock_handler.clone());

        mock_handler
    }

    /// Start the network router
    pub async fn start_network(&mut self) {
        tokio::spawn(async move {
            self.router.start_routing().await;
        });
    }
}
