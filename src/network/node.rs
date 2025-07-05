use std::net::SocketAddr;
use std::sync::{Arc};
use tokio::net::TcpListener;
use tokio::io::{AsyncReadExt};
use serde::{Deserialize, Serialize};
use crate::core::config::NetworkConfig;
use crate::blockchain::block::{Block, Transaction};
use crate::network::message::{NetworkMessage, MessageHandler};
use tracing::{info, error};

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
    message_handler: MessageHandler,
    listener: Option<TcpListener>,
}

impl NetworkNode {
    pub fn new() -> Self {
        Self {
            message_handler: MessageHandler::new(),
            listener: None,
        }
    }

    pub async fn start(&mut self, listen_addr: SocketAddr) {
        let listener = TcpListener::bind(listen_addr).await.unwrap();
        self.listener = Some(listener);
        
        info!("Network node listening on {}", listen_addr);
        
        loop {
            let (stream, addr) = listener.accept().await.unwrap();
            let message_handler = self.message_handler.clone();
            
            tokio::spawn(async move {
                handle_connection(stream, addr, message_handler).await;
            });
        }
    }

    async fn handle_connection(
        stream: TcpStream,
        addr: SocketAddr,
        message_handler: MessageHandler
    ) -> Result<(), String> {
        let mut stream = stream;
        let mut buffer = [0; 1024];
        
        loop {
            match stream.read(&mut buffer).await {
                Ok(0) => break, // Connection closed
                Ok(n) => {
                    let message_str = String::from_utf8_lossy(&buffer[..n]);
                    let message: NetworkMessage = serde_json::from_str(&message_str).unwrap_or_else(|e| {
                        error!("Failed to parse message: {}", e);
                        NetworkMessage::Ping
                    });
                    
                    message_handler.handle_message(message, addr).await?;
                }
                Err(e) => {
                    error!("Error reading from connection: {}", e);
                    break;
                }
            }
        }
        Ok(())
    }

    pub async fn broadcast_message(&self, message: NetworkMessage) -> Result<(), String> {
        self.message_handler.broadcast_message(message).await
    }

    pub async fn connect_to_peer(&self, addr: SocketAddr) -> Result<(), String> {
        self.message_handler.add_peer(addr).await
    }

    pub async fn get_message(&self) -> Option<NetworkMessage> {
        // TODO: Implement message retrieval
        None
    }
}
