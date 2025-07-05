use crate::blockchain::{Blockchain, Block, Transaction, BlockHeader, BlockMeta};
use crate::ai::pattern_analysis::{PatternAnalyzer, PatternType, PatternAnalysisRequest, PatternAnalysisResult, AnalysisContext};
use crate::ai::validator::AIValidator;
use crate::ai::context_manager::ContextManager;
use std::sync::Arc;
use tokio::sync::RwLock;
use std::time::SystemTime;
use anyhow::Result;

// Import the blockchain Arc extension trait
use crate::blockchain::tests::blockchain_arc_ext::BlockchainArcExt;

// Helper function to create a test block
async fn create_test_block(blockchain: &Arc<Blockchain>, is_valid: bool) -> Block {
    let timestamp = SystemTime::now()
        .duration_since(SystemTime::UNIX_EPOCH)
        .unwrap()
        .as_secs();
    
    // Create a transaction
    let transaction = Transaction {
        id: format!("tx_{}", timestamp),
        sender: "test_sender".to_string(),
        receiver: "test_receiver".to_string(),
        amount: 100,
        signature: "valid_signature".to_string(),
        timestamp,
    };
    
    // Get the last block using the extension trait
    let last_block = blockchain.get_last_block().await.unwrap();
    
    // Create a new block
    let mut block = Block {
        header: BlockHeader {
            index: last_block.header.index + 1,
            timestamp,
            previous_hash: last_block.hash.clone(),
            nonce: 0,
            difficulty: blockchain.get_difficulty(), // Using the extension trait
        },
        transactions: vec![transaction],
        meta: BlockMeta {
            size: 0, // Will be calculated
            tx_count: 1,
            height: last_block.header.index + 1,
            validator_id: Some("test_validator".to_string()),
            validator_signature: Some("test_signature".to_string()),
        },
        hash: String::new(), // Will be calculated
    };
    
    // Calculate size and hash
    block.meta.size = block.calculate_size();
    block.hash = block.calculate_hash();
    
    // If we're creating an invalid block, manipulate it to fail validation
    if !is_valid {
        // Set timestamp in the far future to trigger pattern analysis validation failure
        block.header.timestamp = timestamp + 10000000; // Far future timestamp
    }
    
    block
}

#[tokio::test]
async fn test_poai_valid_block() {
    // Create a new blockchain with difficulty 2 wrapped in Arc for trait extensions
    let blockchain = Arc::new(Blockchain::new(2));
    
    // We need to temporarily get a mutable reference to create the genesis block
    let blockchain_ref = unsafe { &mut *(Arc::as_ptr(&blockchain) as *mut Blockchain) };
    
    // Initialize the blockchain with genesis block
    let genesis_result = blockchain_ref.create_genesis_block(vec![]).await;
    assert!(genesis_result.is_ok(), "Failed to create genesis block");
    
    // Initialize PoAI components
    let init_result = blockchain_ref.initialize_poai_components().await;
    assert!(init_result.is_ok(), "Failed to initialize PoAI components");
    
    // Create a valid block using our helper that now accepts Arc<Blockchain>
    let valid_block = create_test_block(&blockchain, true).await;
    
    // Test the valid_proof method using sync version via extension trait
    let is_valid = blockchain.valid_proof_sync(&valid_block);
    assert!(is_valid, "Valid block should pass PoAI validation");
    
    // Test the add_block method using sync version via extension trait
    let add_result = blockchain.add_block_sync(&valid_block);
    assert!(add_result.is_ok(), "Failed to add valid block");
}

#[tokio::test]
async fn test_poai_invalid_block() {
    // Create a new blockchain with difficulty 2 wrapped in Arc for trait extensions
    let blockchain = Arc::new(Blockchain::new(2));
    
    // We need to temporarily get a mutable reference to create the genesis block
    let blockchain_ref = unsafe { &mut *(Arc::as_ptr(&blockchain) as *mut Blockchain) };
    
    // Initialize the blockchain with genesis block
    let genesis_result = blockchain_ref.create_genesis_block(vec![]).await;
    assert!(genesis_result.is_ok(), "Failed to create genesis block");
    
    // Initialize PoAI components
    let init_result = blockchain_ref.initialize_poai_components().await;
    assert!(init_result.is_ok(), "Failed to initialize PoAI components");
    
    // Create an invalid block (with future timestamp) using our helper that now accepts Arc<Blockchain>
    let invalid_block = create_test_block(&blockchain, false).await;
    
    // Test the valid_proof method using sync version via extension trait
    let is_valid = blockchain.valid_proof_sync(&invalid_block);
    assert!(!is_valid, "Invalid block should fail PoAI validation");
    
    // Test the add_block method using sync version via extension trait
    let add_result = blockchain.add_block_sync(&invalid_block);
    assert!(add_result.is_err(), "Adding invalid block should fail");
    assert!(
        add_result.unwrap_err().contains("PoAI validation"), 
        "Error message should reference PoAI validation"
    );
}

#[tokio::test]
async fn test_poai_pattern_analysis() {
    // Create a new blockchain with difficulty 2 wrapped in Arc for trait extensions
    let blockchain = Arc::new(Blockchain::new(2));
    
    // We need to temporarily get a mutable reference to create the genesis block
    let blockchain_ref = unsafe { &mut *(Arc::as_ptr(&blockchain) as *mut Blockchain) };
    
    // Initialize the blockchain with genesis block
    let genesis_result = blockchain_ref.create_genesis_block(vec![]).await;
    assert!(genesis_result.is_ok(), "Failed to create genesis block");
    
    // Initialize PoAI components
    let init_result = blockchain_ref.initialize_poai_components().await;
    assert!(init_result.is_ok(), "Failed to initialize PoAI components");
    
    // Get the pattern analyzer
    assert!(blockchain_ref.pattern_analyzer.is_some(), "Pattern analyzer should be initialized");
    
    // Create a test block using Arc<Blockchain>
    let block = create_test_block(&blockchain, true).await;
    
    // Test pattern analysis with different pattern types
    if let Some(pattern_analyzer) = &blockchain_ref.pattern_analyzer {
        let mut analyzer = pattern_analyzer.write().await;
        
        // Test timestamp validation
        let request = PatternAnalysisRequest {
            block: block.clone(),
            pattern_type: PatternType::TimestampValidation,
            context: None,
            validation_rules: Vec::new(),
            previous_results: None,
            correlation_data: None,
            security_level: 2,
            max_processing_time_ms: Some(2000),
        };
        
        let result = analyzer.analyze_pattern(request).await;
        assert!(result.is_ok(), "Pattern analysis should succeed");
        
        let analysis_result = result.unwrap();
        assert!(analysis_result.analysis.risk_level >= 0.0, "Risk level should be calculated");
    }
}

#[tokio::test]
async fn test_poai_ai_validation() {
    // Create a new blockchain with difficulty 2 wrapped in Arc for trait extensions
    let blockchain = Arc::new(Blockchain::new(2));
    
    // We need to temporarily get a mutable reference to create the genesis block
    let blockchain_ref = unsafe { &mut *(Arc::as_ptr(&blockchain) as *mut Blockchain) };
    
    // Initialize the blockchain with genesis block
    let genesis_result = blockchain_ref.create_genesis_block(vec![]).await;
    assert!(genesis_result.is_ok(), "Failed to create genesis block");
    
    // Initialize PoAI components
    let init_result = blockchain_ref.initialize_poai_components().await;
    assert!(init_result.is_ok(), "Failed to initialize PoAI components");
    
    // Get the AI validator
    assert!(blockchain_ref.ai_validator.is_some(), "AI validator should be initialized");
    
    // Create a test block using Arc<Blockchain>
    let block = create_test_block(&blockchain, true).await;
    
    // Test AI validation
    if let Some(ai_validator) = &blockchain_ref.ai_validator {
        let validator = ai_validator.read().await;
        
        // Serialize the block to JSON
        let block_json = serde_json::to_string(&block).unwrap();
        
        // Create context
        let context = format!(
            "{{\"height\":{},\"timestamp\":{},\"tx_count\":{}}}",
            block.meta.height,
            block.header.timestamp,
            block.transactions.len()
        );
        
        // Test validation
        let result = validator.validate_block(&block_json, &context).await;
        assert!(result.is_ok(), "AI validation should return a result");
    }
}
