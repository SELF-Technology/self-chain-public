use crate::network::message_handler::MessageHandler;
use crate::network::message_handler::NetworkMessage;
use crate::network::p2p::NetworkError;
use crate::network::tls::TLSConfig;
use crate::serialization::SerializationService;
use anyhow::Result;

use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::net::SocketAddr;
use std::sync::Arc;
use tokio::io::{AsyncReadExt, AsyncWriteExt};
use tokio::net::{TcpListener, TcpStream};
use tokio::sync::mpsc;
use tokio::sync::{Mutex, RwLock, Semaphore};
use tokio_rustls::rustls::Certificate;
use tokio_rustls::TlsAcceptor;
use tracing::{error, info, warn};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct NetworkConfig {
    pub listen_addr: String,
    pub max_connections: usize,
    pub timeout: u64,
    pub max_message_size: usize,
    pub enable_tls: bool,
    pub cert_path: Option<String>,
    pub key_path: Option<String>,
}

pub struct NetworkTransport {
    listener: TcpListener,
    tls_acceptor: TlsAcceptor,
    config: NetworkConfig,
    peers: Arc<RwLock<HashMap<String, SocketAddr>>>,
    connection_pool: Arc<RwLock<HashMap<String, Arc<Semaphore>>>>,
    message_handler: Arc<MessageHandler>,
    serialization: Arc<SerializationService>,
    max_connections_per_peer: usize,
    shutdown_tx: mpsc::Sender<()>,
    shutdown_rx: Arc<Mutex<mpsc::Receiver<()>>>,
}

impl std::fmt::Debug for NetworkTransport {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.debug_struct("NetworkTransport")
            .field("config", &self.config)
            .field("max_connections_per_peer", &self.max_connections_per_peer)
            .finish()
    }
}

impl NetworkTransport {
    pub async fn new(
        config: NetworkConfig,
        message_handler: Arc<MessageHandler>,
        tls_config: Arc<TLSConfig>,
    ) -> Result<Arc<Self>> {
        let listener = TcpListener::bind(&config.listen_addr).await?;
        let (shutdown_tx, shutdown_rx) = mpsc::channel(1);
        let server_config = tls_config.get_server_config();
        let tls_acceptor = tokio_rustls::TlsAcceptor::from(server_config);

        let transport = Arc::new(Self {
            listener,
            tls_acceptor,
            config,
            peers: Arc::new(RwLock::new(HashMap::new())),
            connection_pool: Arc::new(RwLock::new(HashMap::new())),
            message_handler,
            serialization: Arc::new(SerializationService::new()),
            max_connections_per_peer: 10, // Default max connections per peer
            shutdown_tx,
            shutdown_rx: Arc::new(Mutex::new(shutdown_rx)),
        });

        // Start connection handler
        let transport_clone = transport.clone();
        tokio::spawn(async move {
            if let Err(e) = transport_clone.handle_connections().await {
                error!("Error handling connections: {}", e);
            }
        });

        Ok(transport)
    }

    pub async fn broadcast_message(&self, message: NetworkMessage) -> Result<()> {
        let peers = self.peers.read().await;
        let serialized = self
            .serialization
            .serialize(&message)
            .map_err(|e| anyhow::anyhow!("Serialization error: {}", e))?;
        let serialized_bytes = serialized.as_bytes();

        // Collect peer IDs and addresses to avoid holding multiple locks
        let peer_list: Vec<(String, SocketAddr)> =
            peers.iter().map(|(id, addr)| (id.clone(), *addr)).collect();
        drop(peers); // Release peers lock early

        for (peer_id, addr) in peer_list {
            let stream = TcpStream::connect(addr).await?;
            let mut tls_stream = self.tls_acceptor.accept(stream).await?;
            let serialized_bytes = serialized_bytes.to_vec(); // Clone bytes for each connection

            // Get semaphore reference and clone it to avoid lifetime issues
            let semaphore = {
                let connection_pool_guard = self.connection_pool.read().await;
                connection_pool_guard.get(&peer_id).cloned()
            };

            let semaphore = semaphore.ok_or_else(|| {
                NetworkError::ConnectionFailed("No connection pool for peer".to_string())
            })?;

            let permit = semaphore.acquire().await.map_err(|e| {
                NetworkError::ResourceError(format!("Failed to acquire semaphore: {}", e))
            })?;

            // Send message directly
            let mut buf = [0; 4];
            let message_len = serialized_bytes.len() as u32;

            // Send message length
            buf.copy_from_slice(&message_len.to_be_bytes());
            tls_stream.write_all(&buf).await?;

            // Send message
            tls_stream.write_all(&serialized_bytes).await?;

            drop(permit); // Release permit
        }

        Ok(())
    }

    pub async fn send_message(
        &self,
        stream: &mut tokio_rustls::server::TlsStream<TcpStream>,
        message: &[u8],
    ) -> Result<()> {
        let mut buf = [0; 4];
        let message_len = message.len() as u32;

        // Send message length
        buf.copy_from_slice(&message_len.to_be_bytes());
        stream.write_all(&buf).await?;

        // Send message
        stream.write_all(message).await?;

        Ok(())
    }

    async fn handle_connections(self: Arc<Self>) -> Result<(), NetworkError> {
        loop {
            // Create the shutdown future to avoid temporary value issues
            let shutdown_future = async { self.shutdown_rx.lock().await.recv().await };

            // Clone self for use in the accept match block
            let self_clone = self.clone();

            tokio::select! {
                _ = shutdown_future => {
                    info!("Shutdown signal received, stopping connection handler");
                    break;
                }
                result = self_clone.listener.accept() => {
                    match result {
                        Ok((stream, addr)) => {
                            info!("Accepted connection from: {}", addr);

                            // Clone self for handshake
                            let self_clone2 = self_clone.clone();

                            // Handle TLS handshake
                            match self_clone.tls_acceptor.accept(stream).await {
                                Ok(tls_stream) => {
                                    // Get TLS session info
                                    let tls_connection = tls_stream.get_ref();
                                    // Access the server_conn field, then the peer certificates
                                    if let Some(certs) = tls_connection.1.peer_certificates() {
                                        if let Some(peer_cert) = certs.first() {
                                            match self.extract_peer_id_from_cert(peer_cert) {
                                                Ok(peer_id) => {
                                                    // Check connection limits
                                                    let semaphore = {
                                                        let pool = self.connection_pool.read().await;
                                                        pool.get(&peer_id).cloned()
                                                    };
                                                    let semaphore = semaphore.ok_or_else(|| NetworkError::ConnectionFailed("No connection pool for peer".to_string()))?;

                                                    // Store peer connection info
                                                    let mut peers = self.peers.write().await;
                                                    peers.insert(peer_id.clone(), addr);
                                                    drop(peers);

                                                    // Spawn connection handler
                                                    // Clone self for the spawned task
                                                    let transport = self_clone2.clone();
                                                    let peer_id_clone = peer_id.clone();
                                                    let semaphore_clone = semaphore.clone();

                                                    tokio::spawn(async move {
                                                        // Acquire permit inside the spawned task
                                                        let permit = semaphore_clone.acquire().await
                                                            .map_err(|e| NetworkError::ResourceError(format!("Failed to acquire semaphore: {}", e)));

                                                        // If permit acquisition failed, log and return
                                                        let _permit = match permit {
                                                            Ok(p) => p,
                                                            Err(e) => {
                                                                error!("Failed to acquire connection permit: {:?}", e);
                                                                return;
                                                            }
                                                        };

                                                        // Keep both semaphore_clone and permit alive for the duration of this task
                                                        if let Err(e) = transport.handle_connection(tls_stream, peer_id_clone).await {
                                                            error!("Connection handler error: {}", e);
                                                        }
                                                    });
                                                }
                                                Err(e) => {
                                                    warn!("Failed to extract peer ID from certificate: {}", e);
                                                }
                                            }
                                        } else {
                                            warn!("No peer certificate found");
                                        }
                                    } else {
                                        warn!("No peer certificates available");
                                    }
                                }
                                Err(e) => {
                                    warn!("TLS handshake failed: {}", e);
                                }
                            }
                        }
                        Err(e) => {
                            error!("Failed to accept connection: {}", e);
                        }
                    }
                }
            }
        }
        Ok(())
    }

    async fn handle_connection(
        &self,
        mut tls_stream: tokio_rustls::server::TlsStream<TcpStream>,
        peer_id: String,
    ) -> Result<()> {
        // Get connection info to verify peer
        let (_, connection) = tls_stream.get_ref();
        let peer_certs = connection
            .peer_certificates()
            .ok_or_else(|| anyhow::anyhow!("No peer certificates"))?;
        let _peer_cert = peer_certs
            .first()
            .ok_or_else(|| anyhow::anyhow!("No peer certificate found"))?;

        // Check connection pool
        let mut connection_pool = self.connection_pool.write().await;
        let semaphore = connection_pool
            .entry(peer_id.clone())
            .or_insert_with(|| Arc::new(Semaphore::new(self.max_connections_per_peer)));

        // Acquire permit
        let _permit = semaphore.acquire().await?;

        // Add to peers map - store peer_id instead of trying to get peer_addr
        let mut peers = self.peers.write().await;
        peers.insert(peer_id.clone(), "0.0.0.0:0".parse().unwrap()); // Placeholder address

        // Handle messages
        loop {
            match self.receive_message(&mut tls_stream).await {
                Ok(message) => {
                    if let Err(e) = self
                        .message_handler
                        .handle_message(message, "0.0.0.0:0".parse().unwrap())
                    {
                        error!("Error handling message: {}", e);
                        break;
                    }
                }
                Err(e) => {
                    info!("Connection closed: {}", e);
                    break;
                }
            }
        }

        // Remove disconnected peer
        let mut peers = self.peers.write().await;
        peers.remove(&peer_id);

        info!("Peer disconnected: {}", peer_id);

        Ok(())
    }

    fn extract_peer_id_from_cert(&self, cert: &Certificate) -> Result<String, NetworkError> {
        // Simple peer ID extraction from certificate
        // In a real implementation, you'd parse the certificate properly
        let digest = md5::compute(&cert.0);
        let hash = format!("{:x}", digest);
        Ok(format!("peer_{}", &hash[..8])) // Use first 8 chars of hash as peer ID
    }

    async fn receive_message(
        &self,
        stream: &mut tokio_rustls::server::TlsStream<TcpStream>,
    ) -> Result<NetworkMessage> {
        let mut buf = [0; 4];
        stream.read_exact(&mut buf).await?;

        let message_len = u32::from_be_bytes(buf) as usize;
        if message_len > self.config.max_message_size {
            return Err(anyhow::anyhow!("Message too large"));
        }

        let mut message_buf = vec![0; message_len];
        stream.read_exact(&mut message_buf).await?;

        let message_str = String::from_utf8(message_buf)?;
        let message: NetworkMessage = serde_json::from_str(&message_str)?;

        Ok(message)
    }

    pub async fn shutdown(&self) {
        let _ = self.shutdown_tx.send(()).await;
    }
}
