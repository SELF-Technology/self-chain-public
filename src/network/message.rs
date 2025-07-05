use serde::{Deserialize, Serialize};
use std::net::SocketAddr;

use crate::blockchain::Block;
use crate::consensus::peer_validator::ValidationRequest;
use crate::consensus::vote::{Vote, VotingResult};

#[derive(Debug, Serialize, Deserialize, Clone)]
pub enum NetworkMessage {
    NewBlock(String),
    Transaction(String),
    GetBlocks,
    Blocks(Vec<String>),
    Ping,
    Pong,
    PeerUpdate(String, SocketAddr), // (peer_id, address)
    VotingStart(Block),
    Vote(Vote),
    VotingResult(VotingResult),
    ValidationRequest(ValidationRequest),
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct Peer {
    pub id: String,
    pub address: SocketAddr,
}

#[derive(Debug)]
pub struct MessageHandler {
    peers: Vec<Peer>,
}

impl MessageHandler {
    pub fn new() -> Self {
        Self { peers: Vec::new() }
    }

    pub fn add_peer(&mut self, peer: Peer) {
        self.peers.push(peer);
    }

    pub fn get_peers(&self) -> Vec<Peer> {
        self.peers.clone()
    }

    pub async fn broadcast_message(&self, _message: NetworkMessage) -> Result<(), String> {
        for _peer in &self.peers {
            // TODO: Implement actual broadcasting
            // This is a placeholder
        }
        Ok(())
    }
}
