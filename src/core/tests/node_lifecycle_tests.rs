use self_chain_core::blockchain::{Block, Transaction};
use self_chain_core::config::node_config::{NodeConfig, NetworkConfig};
use self_chain_core::core::Node;
use std::time::Duration;
use tokio::test;
use tokio::time::sleep;
use anyhow::Result;
use std::sync::Arc;

// Test node initialization with different configurations
#[test]
async fn test_node_initialization() -> Result<()> {
    // Test node with no peers
    let config1 = NodeConfig {
        id: "test-init-node-1".to_string(),
        network: NetworkConfig {
            listen_addr: "127.0.0.1:9090".to_string(),
            peers: vec![],
        },
        difficulty: 2,
    };

    // Create and initialize the node
    let node1 = Node::new(config1).await?;
    
    // Initialize storage and AI components
    node1.initialize_storage().await?;
    node1.initialize_ai_components().await?;
    node1.initialize_network().await?;
    
    // Test node with one peer (that doesn't exist yet, so we expect connection to fail gracefully)
    let config2 = NodeConfig {
        id: "test-init-node-2".to_string(),
        network: NetworkConfig {
            listen_addr: "127.0.0.1:9091".to_string(),
            peers: vec!["127.0.0.1:9092".parse()?], // This peer doesn't exist
        },
        difficulty: 2,
    };

    // Create and initialize the node - should still succeed even if peer is unreachable
    let node2 = Node::new(config2).await?;
    node2.initialize_storage().await?;
    node2.initialize_ai_components().await?;
    
    // This will try to connect to a non-existent peer, but should handle it gracefully
    node2.initialize_network().await?;
    
    Ok(())
}

// Test network connectivity between two nodes
#[test]
async fn test_node_connectivity() -> Result<()> {
    // Create first node that will listen
    let config1 = NodeConfig {
        id: "test-connect-node-1".to_string(),
        network: NetworkConfig {
            listen_addr: "127.0.0.1:9095".to_string(),
            peers: vec![],
        },
        difficulty: 2,
    };

    // Create and initialize the first node
    let node1 = Node::new(config1).await?;
    node1.initialize_storage().await?;
    node1.initialize_ai_components().await?;
    node1.initialize_network().await?;
    
    // Brief pause to ensure node1 is fully initialized
    sleep(Duration::from_millis(100)).await;
    
    // Create second node that will connect to the first
    let config2 = NodeConfig {
        id: "test-connect-node-2".to_string(),
        network: NetworkConfig {
            listen_addr: "127.0.0.1:9096".to_string(),
            peers: vec!["127.0.0.1:9095".parse()?], // Connect to node1
        },
        difficulty: 2,
    };

    // Create and initialize the second node
    let node2 = Node::new(config2).await?;
    node2.initialize_storage().await?;
    node2.initialize_ai_components().await?;
    node2.initialize_network().await?;
    
    // Test connectivity from node2 to node1
    let connection_result = node2.test_connection("127.0.0.1:9095").await?;
    assert!(connection_result, "Connection test from node2 to node1 failed");
    
    Ok(())
}

// Test a full startup and graceful shutdown cycle
#[test]
#[ignore] // Add ignore attribute to avoid running in CI environments
async fn test_node_lifecycle() -> Result<()> {
    // This test demonstrates the full node lifecycle but is ignored by default
    // to avoid resource conflicts in CI environments
    
    let config = NodeConfig {
        id: "test-lifecycle-node".to_string(),
        network: NetworkConfig {
            listen_addr: "127.0.0.1:9099".to_string(),
            peers: vec![],
        },
        difficulty: 1, // Low difficulty for faster test
    };

    // Create node with the test configuration
    let node = Node::new(config).await?;
    
    // Start a task to run the node for a short period, then shut it down
    let node_clone = Arc::new(node);
    let node_arc = node_clone.clone();
    
    let handle = tokio::spawn(async move {
        // Run the node (this would normally block)
        tokio::select! {
            result = node_arc.run() => {
                result?; // Propagate any errors
            },
            _ = sleep(Duration::from_secs(2)) => {
                // After 2 seconds, the select will complete and execution will continue
            }
        }
        
        Ok::<(), anyhow::Error>(())
    });
    
    // Wait a moment for the node to initialize
    sleep(Duration::from_secs(1)).await;
    
    // In a real environment, the user would press Ctrl+C here
    // Since we can't do that in a test, we'll just let the timeout
    // in the select! statement above handle it
    
    // Wait for the spawned task to complete
    handle.await??;
    
    Ok(())
}

// Simulate a pair of nodes exchanging messages
#[test]
async fn test_node_message_exchange() -> Result<()> {
    // Create first message exchange test node
    let config1 = NodeConfig {
        id: "test-msg-node-1".to_string(),
        network: NetworkConfig {
            listen_addr: "127.0.0.1:9085".to_string(),
            peers: vec![],
        },
        difficulty: 2,
    };

    // Create and initialize the first node
    let node1 = Node::new(config1).await?;
    node1.initialize_storage().await?;
    node1.initialize_ai_components().await?;
    node1.initialize_network().await?;
    
    // Brief pause to ensure node1 is fully initialized
    sleep(Duration::from_millis(100)).await;
    
    // Create second node that will connect to the first
    let config2 = NodeConfig {
        id: "test-msg-node-2".to_string(),
        network: NetworkConfig {
            listen_addr: "127.0.0.1:9086".to_string(),
            peers: vec!["127.0.0.1:9085".parse()?], // Connect to node1
        },
        difficulty: 2,
    };

    // Create and initialize the second node
    let node2 = Node::new(config2).await?;
    node2.initialize_storage().await?;
    node2.initialize_ai_components().await?;
    node2.initialize_network().await?;
    
    // Start network services for both nodes
    let service_handle1 = node1.start_network_services();
    let service_handle2 = node2.start_network_services();
    
    // Allow time for nodes to establish connections
    sleep(Duration::from_millis(500)).await;
    
    // Create a transaction at node2 that should propagate to node1
    // This is done through the message handler which we're not directly accessing here
    // but which should start processing as part of the network services
    
    // For now, we're just verifying the nodes can connect and start services
    assert!(service_handle1.is_some());
    assert!(service_handle2.is_some());
    
    // Clean up by shutting down both nodes
    node1.shutdown().await?;
    node2.shutdown().await?;
    
    Ok(())
}
