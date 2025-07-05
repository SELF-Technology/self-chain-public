use anyhow::Result;
use tokio::test;
use std::sync::Arc;
use serde::{Serialize, Deserialize};
use std::path::PathBuf;
use self_chain_core::storage::hybrid_storage::{HybridStorage, StorageConfig};
use self_chain_core::blockchain::{Block, Transaction};
use self_chain_core::storage::{ValidatorState, AIContext};

// Mock data generation
fn create_mock_block(index: u64) -> Block {
    Block {
        index,
        timestamp: chrono::Utc::now().timestamp(),
        prev_hash: format!("prev_hash_{}", index),
        hash: format!("hash_{}", index),
        transactions: vec![
            create_mock_transaction(format!("tx_{}_1", index)),
            create_mock_transaction(format!("tx_{}_2", index)),
        ],
        validator: format!("validator_{}", index),
        difficulty: 2,
        nonce: 12345,
    }
}

fn create_mock_transaction(id: String) -> Transaction {
    Transaction {
        id,
        sender: "mock_sender".to_string(),
        recipient: "mock_recipient".to_string(),
        amount: 100.0,
        signature: "mock_signature".to_string(),
        timestamp: chrono::Utc::now().timestamp(),
        data: serde_json::json!({"test": "data"}),
    }
}

fn create_mock_validator_state(id: &str) -> ValidatorState {
    ValidatorState {
        id: id.to_string(),
        public_key: "mock_public_key".to_string(),
        reputation: 95.5,
        last_active: chrono::Utc::now().timestamp(),
        verified_blocks: 100,
        status: "active".to_string(),
    }
}

fn create_mock_ai_context(validator_id: &str) -> AIContext {
    AIContext {
        validator_id: validator_id.to_string(),
        context: "mock AI context data".to_string(),
        timestamp: chrono::Utc::now().timestamp(),
        performance_metrics: serde_json::json!({"accuracy": 0.95, "latency_ms": 150}),
    }
}

// Helper to create a test storage instance
async fn create_test_storage() -> Result<HybridStorage> {
    let config = StorageConfig {
        ipfs_url: "http://localhost:5001".to_string(),
        orbitdb_timeout_secs: 30,
        max_retries: 3,
        retry_delay_ms: 1000,
    };
    
    HybridStorage::new(config)
}

// Basic connection and initialization test
#[test]
async fn test_hybrid_storage_init() -> Result<()> {
    let storage = create_test_storage().await?;
    
    // Simply creating the instance without errors is a success
    // We can add additional verification if the class exposes status methods
    
    Ok(())
}

// Test block storage and retrieval
#[test]
async fn test_block_storage() -> Result<()> {
    let storage = create_test_storage().await?;
    let test_block = create_mock_block(1);
    
    // Store the block
    let cid = storage.add_block(&test_block).await?;
    assert!(!cid.is_empty(), "Block storage should return a valid CID");
    
    // Retrieve the block
    let retrieved_block = storage.get_block(&test_block.hash).await?;
    assert!(retrieved_block.is_some(), "Should retrieve the stored block");
    
    if let Some(block) = retrieved_block {
        assert_eq!(block.hash, test_block.hash, "Retrieved block hash should match");
        assert_eq!(block.index, test_block.index, "Retrieved block index should match");
        assert_eq!(block.transactions.len(), test_block.transactions.len(), 
                  "Retrieved block should have the same number of transactions");
    }
    
    Ok(())
}

// Test transaction storage and retrieval
#[test]
async fn test_transaction_storage() -> Result<()> {
    let storage = create_test_storage().await?;
    let test_tx = create_mock_transaction("test_tx_1".to_string());
    
    // Store the transaction
    let cid = storage.add_transaction(&test_tx).await?;
    assert!(!cid.is_empty(), "Transaction storage should return a valid CID");
    
    // Retrieve the transaction
    let retrieved_tx = storage.get_transaction(&test_tx.id).await?;
    assert!(retrieved_tx.is_some(), "Should retrieve the stored transaction");
    
    if let Some(tx) = retrieved_tx {
        assert_eq!(tx.id, test_tx.id, "Retrieved transaction ID should match");
        assert_eq!(tx.sender, test_tx.sender, "Retrieved transaction sender should match");
        assert_eq!(tx.recipient, test_tx.recipient, "Retrieved transaction recipient should match");
    }
    
    Ok(())
}

// Test validator state storage and retrieval
#[test]
async fn test_validator_state_storage() -> Result<()> {
    let storage = create_test_storage().await?;
    let validator_id = "test_validator_1";
    let test_state = create_mock_validator_state(validator_id);
    
    // Store the validator state
    let cid = storage.add_validator_state(&test_state).await?;
    assert!(!cid.is_empty(), "Validator state storage should return a valid CID");
    
    // Retrieve the validator state
    let retrieved_state = storage.get_validator_state(validator_id).await?;
    assert!(retrieved_state.is_some(), "Should retrieve the stored validator state");
    
    if let Some(state) = retrieved_state {
        assert_eq!(state.id, test_state.id, "Retrieved validator ID should match");
        assert_eq!(state.reputation, test_state.reputation, "Retrieved validator reputation should match");
        assert_eq!(state.status, test_state.status, "Retrieved validator status should match");
    }
    
    Ok(())
}

// Test AI context storage and retrieval
#[test]
async fn test_ai_context_storage() -> Result<()> {
    let storage = create_test_storage().await?;
    let validator_id = "test_validator_2";
    let test_context = create_mock_ai_context(validator_id);
    
    // Store the AI context
    let cid = storage.add_ai_context(&test_context).await?;
    assert!(!cid.is_empty(), "AI context storage should return a valid CID");
    
    // Retrieve the AI context
    let retrieved_context = storage.get_ai_context(validator_id).await?;
    assert!(retrieved_context.is_some(), "Should retrieve the stored AI context");
    
    if let Some(context) = retrieved_context {
        assert_eq!(context.validator_id, test_context.validator_id, 
                  "Retrieved context validator ID should match");
        assert_eq!(context.timestamp, test_context.timestamp, 
                  "Retrieved context timestamp should match");
    }
    
    Ok(())
}

// Test network synchronization
#[test]
async fn test_network_sync() -> Result<()> {
    let storage = create_test_storage().await?;
    
    // Sync should run without errors
    let sync_result = storage.sync_with_network().await;
    assert!(sync_result.is_ok(), "Network sync should complete without errors");
    
    Ok(())
}

// Test multiple storage operations in sequence
#[test]
async fn test_mixed_storage_operations() -> Result<()> {
    let storage = create_test_storage().await?;
    
    // Store a block
    let block = create_mock_block(999);
    let block_cid = storage.add_block(&block).await?;
    
    // Store related transaction
    let tx = block.transactions[0].clone();
    let tx_cid = storage.add_transaction(&tx).await?;
    
    // Store validator state
    let validator = create_mock_validator_state(&block.validator);
    let validator_cid = storage.add_validator_state(&validator).await?;
    
    // Store AI context
    let context = create_mock_ai_context(&block.validator);
    let context_cid = storage.add_ai_context(&context).await?;
    
    // Verify we can retrieve each item
    let retrieved_block = storage.get_block(&block.hash).await?;
    let retrieved_tx = storage.get_transaction(&tx.id).await?;
    let retrieved_validator = storage.get_validator_state(&validator.id).await?;
    let retrieved_context = storage.get_ai_context(&context.validator_id).await?;
    
    assert!(retrieved_block.is_some());
    assert!(retrieved_tx.is_some());
    assert!(retrieved_validator.is_some());
    assert!(retrieved_context.is_some());
    
    Ok(())
}

// Test storage under concurrent operations
#[test]
async fn test_concurrent_storage_operations() -> Result<()> {
    let storage = Arc::new(create_test_storage().await?);
    let block_count = 5;
    let mut tasks = Vec::new();
    
    // Spawn concurrent block addition tasks
    for i in 0..block_count {
        let storage_clone = storage.clone();
        let block = create_mock_block(i as u64);
        
        let task = tokio::spawn(async move {
            storage_clone.add_block(&block).await
        });
        
        tasks.push(task);
    }
    
    // Wait for all tasks and check results
    let mut results = Vec::new();
    for task in tasks {
        let result = task.await??;
        results.push(result);
    }
    
    // Verify all operations completed successfully
    assert_eq!(results.len(), block_count, "All concurrent operations should succeed");
    for cid in results {
        assert!(!cid.is_empty(), "Each operation should return a valid CID");
    }
    
    Ok(())
}
