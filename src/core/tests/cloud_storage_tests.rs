use anyhow::Result;
use std::path::PathBuf;
use std::sync::Arc;
use tokio::sync::RwLock;

use self_chain_core::config::node_config::NodeConfig;
use self_chain_core::core::storage::HybridStorage;
use self_chain_core::core::storage::OrbitDBAdapter;
use self_chain_core::core::storage::StorageStats;
use self_chain_core::core::node::Node;

#[tokio::test]
async fn test_storage_adapter_initialization() -> Result<()> {
    // Create a basic storage adapter directly
    let node_id = format!("test-node-{}", chrono::Utc::now().timestamp());
    let adapter = OrbitDBAdapter::new();
    
    // Initialize the adapter with the node ID
    adapter.initialize(&node_id).await?;
    
    // Test basic operations
    let doc = serde_json::json!({
        "id": "test-doc-1",
        "content": "Test document for cloud storage initialization",
        "timestamp": chrono::Utc::now().timestamp()
    });
    
    // Store a document
    let id = adapter.store_document("test_collection", &doc).await?;
    println!("Stored document with ID: {}", id);
    
    // Retrieve the document
    let retrieved_doc = adapter.get_document("test_collection", &id).await?;
    assert!(retrieved_doc.is_some(), "Document should be retrieved successfully");
    
    // Clean up
    adapter.shutdown().await?;
    
    Ok(())
}

#[tokio::test]
async fn test_hybrid_storage_with_orbit_db() -> Result<()> {
    // Create a hybrid storage instance with a node ID
    let node_id = format!("test-node-{}", chrono::Utc::now().timestamp());
    let max_size = 1000000000; // 1GB
    
    let mut storage = HybridStorage::new(max_size)
        .with_node_id(node_id);
    
    // Initialize storage
    storage.initialize().await?;
    
    // Test storing and retrieving a document
    let doc = serde_json::json!({
        "id": "test-doc-1",
        "content": "Test document for hybrid storage",
        "timestamp": chrono::Utc::now().timestamp()
    });
    
    // Store a document
    let id = storage.store_document("test_collection", &doc).await?;
    println!("Stored document with ID: {}", id);
    
    // Retrieve the document
    let retrieved_doc = storage.get_document("test_collection", &id).await?;
    assert!(retrieved_doc.is_some(), "Document should be retrieved successfully");
    
    // Get storage stats
    let stats = storage.get_stats().await;
    println!("Storage stats: {:?}", stats);
    
    // Clean up
    storage.shutdown().await?;
    
    Ok(())
}

#[tokio::test]
async fn test_node_storage_initialization() -> Result<()> {
    // Create a node configuration with storage options
    let node_id = format!("test-node-{}", chrono::Utc::now().timestamp());
    
    let mut config = NodeConfig::default();
    config.id = node_id.clone();
    
    // Configure storage options
    config.storage.max_size = Some(1000000000); // 1GB
    config.storage.cloud_enabled = true;
    config.storage.orbitdb_directory = Some(PathBuf::from("./test_data/orbitdb"));
    config.storage.recovery_mode = false;
    
    // Create a node with the config
    let node = Node::new(config).await?;
    
    // Initialize the node (which will initialize storage)
    node.initialize().await?;
    
    // Store a test document through the node
    let test_doc = serde_json::json!({
        "node_id": node_id,
        "timestamp": chrono::Utc::now().timestamp(),
        "test_data": "This is a test document for node storage initialization",
        "tags": ["test", "initialization", "cloud-storage"]
    });
    
    let doc_id = node.store_document("test_nodes", &test_doc).await?;
    println!("Successfully stored document with ID: {}", doc_id);
    
    // Retrieve the document
    let retrieved_doc = node.get_document("test_nodes", &doc_id).await?;
    assert!(retrieved_doc.is_some(), "Document should be retrievable");
    println!("Retrieved document: {:?}", retrieved_doc);
    
    // Get storage stats
    let stats = node.get_storage_stats().await;
    println!("Node storage stats: {:?}", stats);
    assert!(stats.orbit_db_documents > 0, "Should have at least one OrbitDB document");
    
    // Store binary data in IPFS
    let test_data = b"This is some binary test data for IPFS storage";
    let cid = node.store_to_ipfs(test_data).await?;
    println!("Stored binary data in IPFS with CID: {}", cid);
    
    // Retrieve the binary data
    let retrieved_data = node.retrieve_from_ipfs(&cid).await?;
    assert_eq!(retrieved_data, test_data, "Binary data should match");
    
    // Shutdown the node
    node.shutdown().await?;
    
    Ok(())
}
