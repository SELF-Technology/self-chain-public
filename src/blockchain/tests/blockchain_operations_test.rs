use crate::blockchain::{Block, BlockHeader, BlockMeta, Blockchain, Transaction};
use crate::blockchain::tests::blockchain_arc_ext::BlockchainArcExt;
use std::sync::Arc;
use std::time::SystemTime;

use sha2::{Digest, Sha256};

#[tokio::test]
async fn test_transaction_processing() {
    // Create a new blockchain with difficulty 2 wrapped in Arc for trait extensions
    let blockchain = Arc::new(Blockchain::new(2));

    // Create a new transaction
    let transaction = Transaction {
        id: "test_tx_1".to_string(),
        sender: "sender1".to_string(),
        receiver: "receiver1".to_string(),
        amount: 100,
        signature: "test_signature".to_string(),
        timestamp: SystemTime::now()
            .duration_since(SystemTime::UNIX_EPOCH)
            .unwrap()
            .as_secs(),
    };

    // Get a mutable reference to add the transaction
    let blockchain_ref = unsafe { &mut *(Arc::as_ptr(&blockchain) as *mut Blockchain) };
    
    // Add transaction to blockchain
    let result = blockchain_ref.add_transaction(&transaction).await;
    assert!(result.is_ok(), "Failed to add transaction to blockchain");

    // Check if transaction is in pending transactions
    let pending = blockchain_ref.get_pending_transactions().await;
    assert!(!pending.is_empty(), "No pending transactions found");
    assert_eq!(pending[0].id, "test_tx_1", "Transaction ID mismatch");
}

#[tokio::test]
async fn test_block_validation() {
    // Create a new blockchain with difficulty 2 wrapped in Arc for trait extensions
    let blockchain = Arc::new(Blockchain::new(2));
    
    // We need to temporarily get a mutable reference to create the genesis block
    let blockchain_ref = unsafe { &mut *(Arc::as_ptr(&blockchain) as *mut Blockchain) };

    // Create genesis block
    let genesis_block = blockchain_ref.create_genesis_block(vec![]).await;
    assert!(genesis_block.is_ok(), "Failed to create genesis block");

    // Check if block is in the chain using the extension trait
    let blocks = blockchain.get_blocks().await;
    assert!(blocks.is_ok(), "Failed to get blocks");
    assert_eq!(blocks.unwrap().len(), 1, "Block count mismatch");

    // Validate the last block matches what we added using the extension trait
    let last_block = blockchain.get_last_block().await;
    assert!(last_block.is_ok(), "Failed to get last block");
    assert_eq!(
        last_block.unwrap().header.index,
        0,
        "Genesis block index mismatch"
    );
}

#[tokio::test]
async fn test_mining_operations() {
    // Create a new blockchain with difficulty 2
    let mut blockchain = Blockchain::new(2);

    // First create genesis block
    let genesis_result = blockchain.create_genesis_block(vec![]).await;
    assert!(genesis_result.is_ok(), "Failed to create genesis block");

    // Create a transaction
    let transaction = Transaction {
        id: "test_tx_1".to_string(),
        sender: "sender1".to_string(),
        receiver: "receiver1".to_string(),
        amount: 100,
        signature: "test_signature".to_string(),
        timestamp: SystemTime::now()
            .duration_since(SystemTime::UNIX_EPOCH)
            .unwrap()
            .as_secs(),
    };

    blockchain.add_transaction(&transaction).await.unwrap();

    // Get the AI validation threshold
    let ai_threshold = blockchain.get_ai_validation_threshold();
    assert_eq!(ai_threshold, 2, "AI validation threshold should be 2");

    // Create a second block with an empty template
    let empty_block = Block {
        header: BlockHeader {
            index: 0,                     // Will be set by create_block
            timestamp: 0,                 // Will be set by create_block
            previous_hash: String::new(), // Will be set by create_block
            nonce: 0,
            ai_threshold: ai_threshold,
        },
        transactions: vec![], // Will be set by create_block
        meta: BlockMeta {
            size: 0,
            tx_count: 0,
            height: 0,
            validator_id: Some("test_validator".to_string()),
            validator_signature: Some("test_signature".to_string()),
        },
        hash: String::new(),
    };

    // Create the block
    let created_block = blockchain.create_block(empty_block).await;
    assert!(
        created_block.is_ok(),
        "Failed to create block: {}",
        created_block.err().unwrap_or_default()
    );

    // Check that the created block has proper hash
    let created_block = created_block.unwrap();
    // In PoAI consensus, we don't check for leading zeros in hash
    assert!(
        !created_block.hash.is_empty(),
        "Block hash should not be empty: {}",
        created_block.hash
    );

    // Note: create_block already adds the block to the blockchain, no need to call add_block separately

    // Verify the transaction was moved from pending to the block
    let new_pending = blockchain.get_pending_transactions().await;
    assert!(
        new_pending.is_empty(),
        "Pending transactions not cleared after block creation"
    );

    // Verify the transaction is in the last block (index 1, since index 0 is genesis)
    let blocks = blockchain.get_blocks().await.unwrap();
    assert_eq!(blocks.len(), 2, "Should have 2 blocks (genesis + mined)");
    let last_block = &blocks[blocks.len() - 1];
    assert_eq!(
        last_block.transactions.len(),
        1,
        "Transaction not added to block"
    );
    assert_eq!(
        last_block.transactions[0].id, transaction.id,
        "Transaction ID mismatch"
    );
}

#[tokio::test]
async fn test_blockchain_consistency() {
    // Create a new blockchain with difficulty 1 (easier for testing)
    let mut blockchain = Blockchain::new(1);

    // First, create the genesis block
    let genesis_result = blockchain.create_genesis_block(vec![]).await;
    assert!(genesis_result.is_ok(), "Failed to create genesis block");

    // Mine and add 4 more blocks (total of 5 with genesis)
    for i in 1..5 {
        // Create a transaction for this block
        let transaction = Transaction {
            id: format!("test_tx_{}", i),
            sender: "sender1".to_string(),
            receiver: "receiver1".to_string(),
            amount: 100 * i as u64,
            signature: "test_signature".to_string(),
            timestamp: SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap()
                .as_secs(),
        };

        blockchain.add_transaction(&transaction).await.unwrap();

        // Create an empty block and let the mine_block method fill in the details properly
        let empty_block = Block {
            header: BlockHeader {
                index: 0,                     // Will be set by mine_block
                timestamp: 0,                 // Will be set by mine_block
                previous_hash: String::new(), // Will be set by mine_block
                nonce: 0,
                difficulty: blockchain.get_difficulty(),
            },
            transactions: vec![], // Will be set by mine_block
            meta: BlockMeta {
                size: 0,
                tx_count: 0,
                height: 0,
                validator_id: Some("test_validator".to_string()),
                validator_signature: Some("test_signature".to_string()),
            },
            hash: String::new(),
        };

        // Mine the block - this will populate all necessary fields and add it to the chain
        let mined_block = blockchain.mine_block(empty_block).await;
        assert!(
            mined_block.is_ok(),
            "Failed to mine block {}: {}",
            i,
            mined_block.err().unwrap_or_default()
        );
        let _mined_block = mined_block.unwrap(); // mine_block already adds the block to the chain
    }

    // Verify the blockchain has 5 blocks
    let blocks = blockchain.get_blocks_sync().unwrap();
    assert_eq!(blocks.len(), 5, "Expected 5 blocks, got {}", blocks.len());

    // Verify blockchain consistency - each block references the hash of the previous block
    for i in 1..blocks.len() {
        let current_block = &blocks[i];
        let previous_block = &blocks[i - 1];

        assert_eq!(
            current_block.header.previous_hash, previous_block.hash,
            "Block chain broken at index {}",
            i
        );

        assert_eq!(
            current_block.header.index,
            previous_block.header.index + 1,
            "Block index inconsistency at index {}",
            i
        );
    }
}

#[tokio::test]
async fn test_invalid_block_rejection() {
    // Create a new blockchain wrapped in Arc for trait extensions
    let blockchain = Arc::new(Blockchain::new(2));

    // First create a proper genesis block
    // We need to temporarily get a mutable reference to create the genesis block
    let blockchain_ref = unsafe { &mut *(Arc::as_ptr(&blockchain) as *mut Blockchain) };
    let genesis_result = blockchain_ref.create_genesis_block(vec![]).await;
    assert!(genesis_result.is_ok(), "Failed to create genesis block");

    // Get the genesis block to reference it using the extension trait
    let genesis_block = blockchain.get_last_block_sync().unwrap();

    // Try to add an invalid block with wrong index
    let mut invalid_block = Block {
        header: BlockHeader {
            index: 5, // Wrong index, should be 1
            timestamp: SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap()
                .as_secs(),
            previous_hash: genesis_block.hash.clone(),
            nonce: 0,
            difficulty: 2,
        },
        transactions: vec![],
        meta: BlockMeta {
            size: 0,
            tx_count: 0,
            height: 1,
            validator_id: Some("test_validator".to_string()),
            validator_signature: Some("test_signature".to_string()),
        },
        hash: String::new(),
    };

    // Calculate the block size and hash
    invalid_block.meta.size = invalid_block.calculate_size();
    let mut hasher = Sha256::new();
    hasher.update(format!(
        "{}{}{}{}{}{}",
        invalid_block.header.index,
        invalid_block.header.timestamp,
        invalid_block.header.previous_hash,
        invalid_block.header.nonce,
        invalid_block.header.difficulty,
        invalid_block.meta.size
    ));
    invalid_block.hash = format!("{:x}", hasher.finalize());

    // Attempt to add the invalid block directly (bypassing mining) using sync version via extension trait
    let result = blockchain.add_block_sync(&invalid_block);
    assert!(
        result.is_err(),
        "Expected error when adding block with incorrect index: {}",
        result.unwrap_err()
    );

    // Create a block with invalid previous hash
    let mut invalid_previous_hash_block = Block {
        header: BlockHeader {
            index: 1, // Correct index
            timestamp: SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap()
                .as_secs(),
            previous_hash: "invalid_hash".to_string(), // Invalid hash
            nonce: 0,
            difficulty: 2,
        },
        transactions: vec![],
        meta: BlockMeta {
            size: 0,
            tx_count: 1,
            height: 1,
            validator_id: Some("test_validator".to_string()),
            validator_signature: Some("test_signature".to_string()),
        },
        hash: String::new(),
    };

    // Calculate size and hash for the invalid previous hash block
    invalid_previous_hash_block.meta.size = invalid_previous_hash_block.calculate_size();
    let mut hasher = Sha256::new();
    hasher.update(format!(
        "{}{}{}{}{}{}",
        invalid_previous_hash_block.header.index,
        invalid_previous_hash_block.header.timestamp,
        invalid_previous_hash_block.header.previous_hash,
        invalid_previous_hash_block.header.nonce,
        invalid_previous_hash_block.header.difficulty,
        invalid_previous_hash_block.meta.size
    ));
    invalid_previous_hash_block.hash = format!("{:x}", hasher.finalize());

    // Try to add the invalid previous hash block to the blockchain using sync version via extension trait
    let result = blockchain.add_block_sync(&invalid_previous_hash_block);
    assert!(
        result.is_err(),
        "Expected error when adding block with invalid previous hash: {}",
        result.unwrap_err()
    );

    // Make sure we still only have one block (the genesis block) using sync version via extension trait
    let blocks = blockchain.get_blocks_sync().unwrap();
    assert_eq!(
        blocks.len(),
        1,
        "Blockchain should only have the genesis block"
    );

    // Verify the block in the blockchain is indeed the genesis block
    assert_eq!(
        blocks[0].header.index, 0,
        "The first block should be the genesis block"
    );
}
