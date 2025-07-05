use crate::blockchain::{Block, Transaction};
use serde::{Deserialize, Serialize};
use std::net::SocketAddr;
use std::sync::Arc;
use tokio::io::{AsyncReadExt, AsyncWriteExt};
use tokio::net::{TcpListener, TcpStream};
use tokio::sync::Mutex;
use tracing::{error, info};

pub mod cloud_protocol;
pub mod examples;
pub mod message;
pub mod message_handler;
pub mod p2p;
pub mod reputation;
pub mod tls;
pub mod transport;
// Re-export MessageHandler for backward compatibility
pub use message_handler::MessageHandler;
// Re-export NetworkAdapter trait and implementations
pub use cloud_protocol::{CloudNodeCommunicator, CloudPeerInfo, MessagePriority};
pub use examples::cloud_protocol_example;
pub use p2p::{
    create_network_adapter, MessagePayload, MessageType, NetworkAdapter, NetworkConfig,
    NetworkError,
};

#[derive(Debug, Serialize, Deserialize, Clone)]
pub enum NetworkMessage {
    NewBlock(Block),
    Transaction(Transaction),
    GetBlocks,
    Blocks(Vec<Block>),
    Ping,
    Pong,
}

pub struct NetworkNode {
    peers: Arc<Mutex<Vec<SocketAddr>>>,
}

impl NetworkNode {
    pub fn new() -> Self {
        Self {
            peers: Arc::new(Mutex::new(Vec::new())),
        }
    }

    pub async fn connect(&self, addr: SocketAddr) -> Result<TcpStream, String> {
        TcpStream::connect(addr)
            .await
            .map_err(|e| format!("Failed to connect: {}", e))
    }

    pub async fn send_message(
        &self,
        addr: &SocketAddr,
        message: NetworkMessage,
    ) -> Result<(), String> {
        let mut stream = self.connect(*addr).await?;
        let message_str = serde_json::to_string(&message)
            .map_err(|e| format!("Failed to serialize message: {}", e))?;

        stream
            .write_all(message_str.as_bytes())
            .await
            .map_err(|e| format!("Failed to send message: {}", e))?;

        Ok(())
    }

    pub async fn start(&self, listen_addr: SocketAddr) -> Result<(), String> {
        let listener = TcpListener::bind(listen_addr)
            .await
            .map_err(|e| format!("Failed to bind listener: {}", e))?;
        info!("Network node listening on {}", listen_addr);

        loop {
            let (stream, addr) = listener
                .accept()
                .await
                .map_err(|e| format!("Failed to accept connection: {}", e))?;
            let peers = Arc::clone(&self.peers);
            tokio::spawn(async move {
                if let Err(e) = handle_connection(stream, addr, peers).await {
                    error!("Connection handling failed: {}", e);
                }
            });
        }
    }
}

async fn handle_connection(
    mut stream: TcpStream,
    addr: SocketAddr,
    peers: Arc<tokio::sync::Mutex<Vec<SocketAddr>>>,
) -> Result<(), String> {
    let mut buffer = [0; 1024];

    // Add peer to list
    {
        let mut peers_write = peers.lock().await;
        peers_write.push(addr);
    }

    loop {
        let n = match stream.read(&mut buffer).await {
            Ok(0) => break,
            Ok(n) => n,
            Err(e) => {
                error!("Failed to read from stream: {}", e);
                break;
            }
        };

        let message: NetworkMessage = match serde_json::from_slice(&buffer[..n]) {
            Ok(msg) => msg,
            Err(e) => {
                error!("Failed to deserialize message: {}", e);
                continue;
            }
        };

        // Handle message
        match message {
            NetworkMessage::NewBlock(block) => {
                // TODO: Handle new block
                info!("Received new block: {:?}", block);
            }
            NetworkMessage::Transaction(tx) => {
                // TODO: Handle transaction
                info!("Received transaction: {:?}", tx);
            }
            NetworkMessage::GetBlocks => {
                // TODO: Handle get blocks
                info!("Received get blocks request");
            }
            NetworkMessage::Blocks(blocks) => {
                // TODO: Handle blocks
                info!("Received {} blocks", blocks.len());
            }
            NetworkMessage::Ping => {
                // TODO: Handle ping
                info!("Received ping");
            }
            NetworkMessage::Pong => {
                // TODO: Handle pong
                info!("Received pong");
            }
        }
    }

    // Remove peer
    let mut peers_write = peers.lock().await;
    peers_write.retain(|&x| x != addr);
    drop(peers_write);

    Ok(())
}
