use crate::core::config::StorageConfig;
use crate::network::cloud_protocol::{
    CloudNodeCommunicator, MessagePriority, NetworkError, NodeMessage, StorageRequestType,
};
use crate::network::reputation::ReputationManager;
use std::sync::Arc;
use tokio::time::Duration;
use tracing::{error, info};

/// Example demonstrating how to use the Cloud Node Communication Protocol
///
/// This example shows:
/// 1. How to create cloud node communicators
/// 2. How to send messages between nodes
/// 3. How to handle message processing
/// 4. How to manage peers
/// 5. How to recover from network errors
pub async fn cloud_protocol_example() -> Result<(), String> {
    // Initialize the reputation manager
    let reputation_manager = Arc::new(ReputationManager::new());

    // Initialize cloud storage
    let storage_config = StorageConfig {
        node_id: Some("example-node-001".to_string()),
        max_storage_gb: Some(100),
        max_size: Some(1_000_000_000), // 1GB
        cloud_enabled: true,
        orbitdb_directory: Some("/tmp/self-chain-storage/orbitdb".into()),
        ipfs_swarm_key: None,
        recovery_mode: false,
        ipfs_api: "http://localhost:5001/api/v0".to_string(),
        ipfs_gateway: "https://ipfs.io/ipfs/".to_string(),
        data_dir: "/tmp/self-chain-storage".into(),
    };
    // Create two cloud node communicators for demonstration
    let node1 = CloudNodeCommunicator::new(
        "node1".to_string(),
        storage_config.clone(),
        reputation_manager.clone(),
    );

    let node2 = CloudNodeCommunicator::new(
        "node2".to_string(),
        storage_config.clone(),
        reputation_manager.clone(),
    );

    // Add each node as a peer to the other
    let node1_peer_id = libp2p::PeerId::random();
    let node2_peer_id = libp2p::PeerId::random();

    node1.add_peer(node2_peer_id, "node2".to_string()).await;
    node2.add_peer(node1_peer_id, "node1".to_string()).await;

    info!("Cloud node communicators created and peers connected");

    // Example 1: Broadcasting a message
    info!("Example 1: Broadcasting a message");
    node1
        .broadcast(MessagePriority::High, NodeMessage::JoinNetwork)
        .await?;

    // Process messages
    node2.process_messages().await?;

    // Wait for the message to be processed
    tokio::time::sleep(Duration::from_secs(1)).await;

    // Example 2: Direct message to a specific node
    info!("Example 2: Sending direct message");
    node1
        .send_direct(
            "node2".to_string(),
            MessagePriority::Medium,
            NodeMessage::Heartbeat,
        )
        .await?;

    // Process messages
    node2.process_messages().await?;

    // Wait for the message to be processed
    tokio::time::sleep(Duration::from_secs(1)).await;

    // Example 3: Error recovery
    info!("Example 3: Error recovery demonstration");
    node1
        .recover_from_error(NetworkError::ConnectionFailed)
        .await?;

    // Wait for recovery to complete
    tokio::time::sleep(Duration::from_secs(1)).await;

    // Example 4: Storage operations
    info!("Example 4: Storage operations");
    node1
        .send_direct(
            "node2".to_string(),
            MessagePriority::High,
            NodeMessage::StorageRequest(StorageRequestType::Get("test-key".to_string())),
        )
        .await?;

    // Process messages
    node2.process_messages().await?;

    // Wait for storage operation to complete
    tokio::time::sleep(Duration::from_secs(1)).await;

    // Example 5: Maintenance operations
    info!("Example 5: Maintenance operations");
    node1.maintain().await;
    node2.maintain().await;

    // Wait for maintenance to complete
    tokio::time::sleep(Duration::from_secs(1)).await;

    // Get stats
    let node1_stats = node1.get_stats().await;
    let node2_stats = node2.get_stats().await;

    info!("Node 1 stats: {:?}", node1_stats);
    info!("Node 2 stats: {:?}", node2_stats);

    // Get connected peers
    let node1_peers = node1.get_connected_peers().await;
    let node2_peers = node2.get_connected_peers().await;

    info!("Node 1 connected peers: {}", node1_peers.len());
    info!("Node 2 connected peers: {}", node2_peers.len());

    info!("Cloud protocol example completed successfully");

    Ok(())
}

/// Example showing how to use the cloud protocol in a real node implementation
pub async fn cloud_protocol_integration_example() -> Result<(), String> {
    // Initialize components
    let reputation_manager = Arc::new(ReputationManager::new());
    let storage_config = StorageConfig {
        node_id: Some("example-node-001".to_string()),
        max_storage_gb: Some(100),
        max_size: Some(1_000_000_000), // 1GB
        cloud_enabled: true,
        orbitdb_directory: Some("/tmp/self-chain-storage/orbitdb".into()),
        ipfs_swarm_key: None,
        recovery_mode: false,
        ipfs_api: "http://localhost:5001/api/v0".to_string(),
        ipfs_gateway: "https://ipfs.io/ipfs/".to_string(),
        data_dir: "/tmp/self-chain-storage".into(),
    };
    // Create the cloud node communicator
    let communicator =
        CloudNodeCommunicator::new("main-node".to_string(), storage_config, reputation_manager);

    // In a real implementation, we would:
    // 1. Start a background task that periodically processes messages
    tokio::spawn({
        let comm = communicator.clone();
        async move {
            loop {
                if let Err(e) = comm.process_messages().await {
                    error!("Error processing messages: {}", e);
                }

                // Perform maintenance
                comm.maintain().await;

                tokio::time::sleep(Duration::from_millis(100)).await;
            }
        }
    });

    // 2. Expose an API for other components to send messages

    // 3. Handle incoming messages and route them to appropriate components

    Ok(())
}
