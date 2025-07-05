use std::sync::{Arc, RwLock};
use std::collections::HashMap;
use std::path::PathBuf;
use sled::Db;
use bitcoin_hashes::sha256;
use serde::{Deserialize, Serialize};
use crate::blockchain::block::{Block, Transaction};
use crate::core::config::BlockchainConfig;

#[derive(Debug, Serialize, Deserialize)]
pub struct Blockchain {
    chain: Vec<Block>,
    current_transactions: Vec<Transaction>,
    pending_transactions: Vec<Transaction>,
    db: Arc<RwLock<Db>>,
    config: BlockchainConfig,
    utxo_set: HashMap<String, u64>,
}

impl Blockchain {
    pub fn new(config: BlockchainConfig) -> Self {
        let db = Arc::new(RwLock::new(
            sled::open(&config.storage_path).expect("Failed to open database")
        ));
        
        let genesis_block = Self::create_genesis_block(&config.genesis_block);
        
        Self {
            chain: vec![genesis_block],
            current_transactions: Vec::new(),
            pending_transactions: Vec::new(),
            db,
            config,
            utxo_set: HashMap::new(),
        }
    }

    fn create_genesis_block(genesis: &GenesisConfig) -> Block {
        Block::new(
            0,
            String::from("0"),
            Vec::new(),
            genesis.difficulty,
            &SecretKey::from_slice(&[0; 32]).expect("32 bytes"), // Temporary genesis key
        )
    }

    pub fn add_transaction(&mut self, tx: Transaction) -> bool {
        if !tx.verify() {
            return false;
        }

        // Check if sender has enough balance
        if !self.has_sufficient_balance(&tx.sender, tx.amount) {
            return false;
        }

        self.current_transactions.push(tx);
        true
    }

    fn has_sufficient_balance(&self, address: &str, amount: u64) -> bool {
        self.utxo_set.get(address).unwrap_or(&0) >= &amount
    }

    pub async fn mine_block(&mut self, validator_key: &SecretKey) -> Result<Block, String> {
        let block = Block::new(
            self.chain.len() as u64,
            self.chain.last().unwrap().hash.clone(),
            self.current_transactions.clone(),
            self.config.consensus.difficulty,
            validator_key,
        );

        // Clear current transactions
        self.current_transactions.clear();

        // Update UTXO set
        self.update_utxo_set(&block);

        // Add block to chain
        self.chain.push(block.clone());

        // Save to database
        self.save_block(&block).await;

        Ok(block)
    }

    fn update_utxo_set(&mut self, block: &Block) {
        for tx in &block.transactions {
            // Subtract from sender's balance
            *self.utxo_set.entry(tx.sender.clone()).or_insert(0) -= tx.amount;
            
            // Add to recipient's balance
            *self.utxo_set.entry(tx.recipient.clone()).or_insert(0) += tx.amount;
        }
    }

    async fn save_block(&self, block: &Block) {
        let mut db = self.db.write().unwrap();
        let key = format!("block:{}", block.index);
        let value = serde_json::to_string(block).expect("Failed to serialize block");
        db.insert(key, value.as_bytes()).expect("Failed to save block");
    }

    pub fn validate_chain(&self) -> bool {
        for (i, block) in self.chain.iter().enumerate() {
            if i == 0 {
                continue; // Skip genesis block
            }

            if !block.validate() {
                return false;
            }

            if block.previous_hash != self.chain[i - 1].hash {
                return false;
            }
        }

        true
    }

    pub async fn get_balance(&self, address: &str) -> u64 {
        self.utxo_set.get(address).unwrap_or(&0).clone()
    }

    pub fn add_block(&mut self, block: Block) -> Result<(), String> {
        // Verify block
        if !block.verify() {
            return Err("Invalid block".to_string());
        }

        // Verify block index
        if block.index != self.chain.len() as u64 {
            return Err("Invalid block index".to_string());
        }

        // Verify previous hash
        if block.index > 0 && block.previous_hash != self.chain.last().unwrap().hash {
            return Err("Invalid previous hash".to_string());
        }

        // Update UTXO set
        self.update_utxo_set(&block);

        // Add to chain
        self.chain.push(block);

        Ok(())
    }

    pub fn add_pending_transaction(&mut self, tx: Transaction) -> Result<(), String> {
        // Verify transaction
        if !tx.verify() {
            return Err("Invalid transaction".to_string());
        }

        // Check balance
        if !self.has_sufficient_balance(&tx.sender, tx.amount) {
            return Err("Insufficient balance".to_string());
        }

        // Add to pending transactions
        self.pending_transactions.push(tx);
        Ok(())
    }

    pub async fn get_last_blocks(&self, count: usize) -> Result<Vec<Block>, String> {
        // Get last N blocks
        let start = self.chain.len().saturating_sub(count);
        Ok(self.chain[start..].to_vec())
    }

    pub async fn process_pending_transactions(&mut self, validator_key: &SecretKey) -> Result<Block, String> {
        // Process pending transactions
        let mut transactions = Vec::new();
        while !self.pending_transactions.is_empty() {
            let tx = self.pending_transactions.remove(0);
            if self.add_transaction(tx) {
                transactions.push(tx);
            }
        }

        // Mine new block with processed transactions
        self.mine_block(validator_key).await
    }

    pub async fn validate_block(&self, block: &Block) -> Result<bool, String> {
        // Verify block
        if !block.verify() {
            return Ok(false);
        }

        // Verify previous hash
        if block.index > 0 && block.previous_hash != self.chain.last().unwrap().hash {
            return Ok(false);
        }

        // Verify transactions
        for tx in &block.transactions {
            if !tx.verify() {
                return Ok(false);
            }
            if !self.has_sufficient_balance(&tx.sender, tx.amount) {
                return Ok(false);
            }
        }

        Ok(true)
    }

    pub async fn validate_transaction(&self, tx: &Transaction) -> Result<bool, String> {
        // Verify transaction
        if !tx.verify() {
            return Ok(false);
        }

        // Check balance
        if !self.has_sufficient_balance(&tx.sender, tx.amount) {
            return Ok(false);
        }

        Ok(true)
    }

    pub async fn get_block_by_hash(&self, hash: &str) -> Option<Block> {
        self.chain.iter().find(|b| b.hash == hash).cloned()
    }

    pub async fn get_block_by_index(&self, index: u64) -> Option<Block> {
        self.chain.get(index as usize).cloned()
    }

    pub async fn get_transaction_by_id(&self, tx_id: &str) -> Option<Transaction> {
        for block in &self.chain {
            for tx in &block.transactions {
                if tx.id == tx_id {
                    return Some(tx.clone());
                }
            }
        }
        None
    }

    pub async fn get_chain(&self) -> &Vec<Block> {
        &self.chain
    }
}
