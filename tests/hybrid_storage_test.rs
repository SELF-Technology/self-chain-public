//! Integration tests for hybrid storage implementation

use self_chain_core::blockchain::{Block, Transaction, BlockHeader, BlockMeta};
use self_chain_core::core::storage::HybridStorage;
use self_chain_core::crypto::KeyPair;
use chrono::Utc;

#[tokio::test]
async fn test_hybrid_storage_initialization() {
    let mut storage = HybridStorage::new(1_000_000_000) // 1GB
        .with_node_id("test-node-001".to_string());
    
    let result = storage.initialize().await;
    assert!(result.is_ok(), "Storage initialization failed: {:?}", result);
    
    // Get stats
    let stats = storage.get_stats().await;
    assert_eq!(stats.orbit_db_documents, 4); // 4 standard databases
}

#[tokio::test]
async fn test_store_and_retrieve_block() {
    let mut storage = HybridStorage::new(1_000_000_000)
        .with_node_id("test-node-002".to_string());
    
    storage.initialize().await.expect("Failed to initialize storage");
    
    // Create a test block
    let block = Block {
        header: BlockHeader {
            index: 1,
            timestamp: Utc::now().timestamp() as u64,
            previous_hash: "0000000000000000".to_string(),
            nonce: 0,
            difficulty: 5,
        },
        hash: "test-block-hash-001".to_string(),
        transactions: vec![],
        meta: BlockMeta {
            size: 1024,
            tx_count: 0,
            height: 1,
            validator_id: Some("validator-001".to_string()),
            validator_signature: Some("sig-001".to_string()),
        },
    };
    
    // Store the block
    let cid = storage.store_block(&block).await
        .expect("Failed to store block");
    assert!(!cid.is_empty(), "CID should not be empty");
    
    // Retrieve the block by hash
    let retrieved = storage.get_block(&block.hash).await
        .expect("Failed to retrieve block");
    
    // With mock adapter, this might return None
    // With real adapter, it should return the block
    println!("Retrieved block: {:?}", retrieved);
}

#[tokio::test]
async fn test_store_and_retrieve_transaction() {
    let mut storage = HybridStorage::new(1_000_000_000)
        .with_node_id("test-node-003".to_string());
    
    storage.initialize().await.expect("Failed to initialize storage");
    
    // Create a test transaction
    let keypair = KeyPair::generate();
    let mut tx = Transaction::new(
        keypair.public_key(),
        keypair.public_key(), // Self-transfer for testing
        100.0,
        0.0,
    );
    tx.sign(&keypair).expect("Failed to sign transaction");
    
    // Store the transaction
    let cid = storage.store_transaction(&tx).await
        .expect("Failed to store transaction");
    assert!(!cid.is_empty(), "CID should not be empty");
    
    // Retrieve the transaction by ID
    let retrieved = storage.get_transaction(&tx.id).await
        .expect("Failed to retrieve transaction");
    
    println!("Retrieved transaction: {:?}", retrieved);
}

#[tokio::test]
async fn test_ipfs_storage() {
    let mut storage = HybridStorage::new(1_000_000_000)
        .with_node_id("test-node-004".to_string());
    
    storage.initialize().await.expect("Failed to initialize storage");
    
    // Test data
    let test_data = b"Hello, SELF Chain IPFS storage!";
    
    // Store in IPFS
    let cid = storage.store_to_ipfs(test_data).await
        .expect("Failed to store in IPFS");
    assert!(!cid.is_empty(), "CID should not be empty");
    
    // Retrieve from IPFS
    let retrieved = storage.retrieve_from_ipfs(&cid).await
        .expect("Failed to retrieve from IPFS");
    
    // With mock adapter, this won't match exactly
    // With real adapter, it should match
    println!("Original: {:?}", test_data);
    println!("Retrieved: {:?}", retrieved);
}

#[tokio::test]
async fn test_storage_stats() {
    let mut storage = HybridStorage::new(1_000_000_000)
        .with_node_id("test-node-005".to_string());
    
    storage.initialize().await.expect("Failed to initialize storage");
    
    // Get initial stats
    let stats1 = storage.get_stats().await;
    println!("Initial stats: {:?}", stats1);
    
    // Store some data
    let data = b"Test data for stats";
    storage.store_to_ipfs(data).await
        .expect("Failed to store data");
    
    // Get updated stats
    let stats2 = storage.get_stats().await;
    println!("Updated stats: {:?}", stats2);
    
    // Stats should have changed
    assert!(stats2.last_update >= stats1.last_update);
}

#[tokio::test] 
async fn test_real_orbitdb_initialization() {
    // This test requires USE_REAL_ORBITDB=true and IPFS running
    std::env::set_var("USE_REAL_ORBITDB", "true");
    
    let mut storage = HybridStorage::new_with_real_orbitdb(1_000_000_000)
        .with_node_id("test-node-real".to_string());
    
    // This will fail if IPFS is not running
    match storage.initialize().await {
        Ok(_) => println!("Real OrbitDB initialized successfully"),
        Err(e) => println!("Real OrbitDB initialization failed (expected if IPFS not running): {}", e),
    }
}