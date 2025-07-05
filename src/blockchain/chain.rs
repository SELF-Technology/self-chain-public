use std::sync::Arc;
use serde::{Serialize, Deserialize};
use crate::blockchain::block::{Block, Transaction};
use crate::network::message::NetworkMessage;
use crate::storage::orbitdb::OrbitDBStore;
use crate::network::message_handler::MessageHandler;
use crate::ai::service::AIService;
use crate::consensus::poai::PoAI;
use anyhow::Result;
use bitcoin_hashes::sha256;
use hex; // For hex color calculations

pub struct Blockchain {
    store: Arc<OrbitDBStore>,
    difficulty: u64,
    message_handler: MessageHandler,
    ai_service: AIService,
    poai: PoAI,
    point_price: u64, // Points required per block
    current_points: u64, // Current points accumulated
    block_timer: u64, // Timer for block assembly
}

impl Blockchain {
    pub fn new(store: Arc<OrbitDBStore>, difficulty: u64, ai_service: AIService) -> Self {
        let ai_validator = AIValidator::new();
        Self {
            store,
            difficulty,
            message_handler: MessageHandler::new(),
            ai_service,
            ai_validator,
            point_price: 10000, // Initial point price (equivalent to 10 coins)
            current_points: 0,
            block_timer: 60, // 60 seconds block timer
        }
    }

    pub async fn add_block(&self, block: &Block) -> Result<(), String> {
        // 1. Validate block with PoAI
        if !self.poai.validate_block(block).await? {
            return Err("Block validation failed".to_string());
        }
        
        // 2. Wait for voting result
        let voting_result = self.poai.get_voting_result(&block.hash()).await?;
        
        if let Some(result) = voting_result {
            if result.winning_block.is_none() || result.participation_rate < 0.6 {
                return Err("Voting failed or insufficient participation".to_string());
            }
        } else {
            return Err("No voting result".to_string());
        }
        
        // 3. Update points and reward system
        self.current_points += block.transactions.len() as u64 * self.point_price;
        
        // 4. Store block in OrbitDB
        self.store.add_block(block).await?;
        
        // 5. Broadcast block to peers
        self.message_handler.broadcast_message(NetworkMessage::NewBlock(block.clone())).await?;
        
        Ok(())
    }

    pub async fn add_transaction(&self, tx: &Transaction) -> Result<(), String> {
        // 1. Validate transaction with AI service
        self.ai_service.validate_transaction(tx).await?;
        
        // 2. Validate transaction structure
        if !self.validate_transaction_structure(tx)? {
            return Err("Invalid transaction structure".to_string());
        }
        
        // 3. Store transaction in OrbitDB
        self.store.add_transaction(tx).await?;
        
        // 4. Broadcast transaction to peers
        self.message_handler.broadcast_message(NetworkMessage::Transaction(tx.clone())).await?;
        
        Ok(())
    }

    pub async fn get_latest_block(&self) -> Option<Block> {
        self.store.get_latest_block().await
    }

    fn calculate_block_efficiency(&self, block: &Block) -> Result<f64, String> {
        // Calculate efficiency based on:
        // 1. Point usage
        // 2. Transaction volume
        // 3. Useful information content
        
        let total_points = block.transactions.len() as u64 * self.point_price;
        let useful_info = self.calculate_useful_information(block)?;
        
        Ok((useful_info as f64) / (total_points as f64))
    }

    fn calculate_useful_information(&self, block: &Block) -> Result<u64, String> {
        // Calculate based on:
        // 1. Transaction complexity
        // 2. Data value
        // 3. AI validation score
        
        let mut score = 0;
        for tx in &block.transactions {
            score += self.ai_validator.validate_transaction(tx)?;
        }
        
        Ok(score)
    }

    fn validate_transaction_with_color(&self, tx: &Transaction) -> Result<(), String> {
        // 1. Calculate HEX transaction
        let hex_tx = self.calculate_hex_transaction(tx)?;
        
        // 2. Get current wallet color
        let current_color = self.ai_validator.get_wallet_color(&tx.sender)?;
        
        // 3. Calculate new color
        let new_color = self.calculate_new_color(&current_color, &hex_tx)?;
        
        // 4. Validate color transition
        if !self.ai_validator.validate_color_transition(&current_color, &new_color)? {
            return Err("Invalid color transition".to_string());
        }
        
        // 5. Update wallet color
        self.ai_validator.update_wallet_color(&tx.sender, &new_color)?;
        
        Ok(())
    }

    fn calculate_hex_transaction(&self, tx: &Transaction) -> Result<String, String> {
        // Split transaction hash into 6 parts
        let hash = tx.hash();
        let mut hex_parts = Vec::new();
        
        for i in 0..6 {
            let part = &hash[i*4..(i+1)*4];
            let hex = hex::encode(part);
            hex_parts.push(hex);
        }
        
        // Combine parts
        Ok(hex_parts.join(""))
    }

    fn calculate_new_color(&self, current_color: &str, hex_tx: &str) -> Result<String, String> {
        // Add hex transaction to current color
        let mut new_color = current_color.to_string();
        new_color.push_str(hex_tx);
        
        // Reduce to 6 characters
        new_color.truncate(6);
        
        Ok(new_color)
    }
}
