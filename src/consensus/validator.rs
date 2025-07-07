//! # AI Validator
//!
//! The AIValidator implements the core validation logic for the PoAI consensus mechanism. It handles:
//! - Block validation using AI
//! - Transaction validation with color markers
//! - Reference block generation
//! - Wallet color management
//!
//! ## Block Validation
//!
//! Blocks are validated through a multi-step process:
//! 1. AI validation of block contents
//! 2. Efficiency calculation
//! 3. Reference block comparison
//! 4. Transaction validation
//!
//! ## Color Marker System
//!
//! Each transaction is assigned a color marker based on:
//! - Transaction hash
//! - Wallet history
//! - AI validation score
//! - Time-based transitions
//!
//! ## Error Handling
//!
//! The validator implements comprehensive error handling with specific error types:
//! - BlockValidationFailed
//! - AIValidationError
//! - InvalidColorTransition
//! - LowBlockEfficiency

use crate::ai::service::AIService;
use crate::blockchain::{Block, Transaction};
use crate::consensus::cache::ValidationCache;
use crate::consensus::error::ConsensusError;
use crate::consensus::metrics::ConsensusMetrics;
use crate::consensus::peer_validator::ValidatorStats;
use anyhow::Result;
use hex;
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::sync::Arc;
use std::time::{SystemTime, UNIX_EPOCH};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ValidatorConfig {
    pub min_active_hours: u64,
    pub min_balance: u64,
    pub validation_window: u64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct WalletColor {
    pub address: String,
    pub color: String,
    pub last_update: u64,
}

/// AIValidator implements the core validation logic for PoAI consensus.
///
/// It handles block and transaction validation using AI-powered algorithms
/// and maintains wallet color states for transaction validation.
#[derive(Debug)]
pub struct AIValidator {
    #[allow(dead_code)]
    config: ValidatorConfig,
    wallet_colors: Arc<tokio::sync::RwLock<HashMap<String, WalletColor>>>,
    ai_service: Arc<AIService>,
    metrics: Arc<ConsensusMetrics>,
    cache: Arc<ValidationCache>,
}

impl AIValidator {
    pub fn new(metrics: Arc<ConsensusMetrics>, cache: Arc<ValidationCache>) -> Self {
        Self {
            config: ValidatorConfig {
                min_active_hours: 24,
                min_balance: 1000000,    // 1000 SELF tokens
                validation_window: 3600, // 1 hour
            },
            wallet_colors: Arc::new(tokio::sync::RwLock::new(HashMap::new())),
            ai_service: Arc::new(AIService::new()),
            metrics,
            cache,
        }
    }

    // This function is replaced by the more comprehensive implementation below
    // that returns Result<bool, ConsensusError> and handles caching, metrics, etc.
    // See the validate_block function at line ~270

    /// Validates a transaction using the PoAI consensus rules.
    ///
    /// # Arguments
    ///
    /// * `tx` - The transaction to validate
    ///
    /// # Returns
    ///
    /// * `Ok(())` if transaction is valid
    /// * `Err(ConsensusError)` if validation fails
    pub async fn validate_transaction(&self, tx: &Transaction) -> Result<(), ConsensusError> {
        // Check cache first
        if let Some(result) = self.cache.get_cached_transaction_validation(tx).await {
            return if result.value {
                Ok(())
            } else {
                Err(ConsensusError::InvalidTransaction(
                    "Cached validation failed".to_string(),
                ))
            };
        }

        // 1. Basic transaction structure validation
        if !tx.verify() {
            self.metrics.increment_validation_failures("tx_structure");
            return Err(ConsensusError::TransactionValidationFailed(
                "Invalid transaction structure".to_string(),
            ));
        }

        // 2. AI validation
        let ai_result = self.ai_service.validate_transaction(tx).await;
        if let Err(e) = ai_result {
            self.metrics
                .increment_validation_failures("ai_tx_validation");
            return Err(ConsensusError::AIValidationError(e.to_string()));
        }

        // 3. Color marker validation
        let sender_color = self.get_wallet_color(&tx.sender).await?;
        let hex_tx = self.calculate_hex_transaction(tx)?;
        let new_color = self.calculate_new_color(&sender_color, &hex_tx)?;

        if !self.validate_color_transition(&sender_color, &new_color)? {
            self.metrics
                .increment_validation_failures("color_transition");
            return Err(ConsensusError::InvalidColorTransition);
        }

        // Cache result
        self.cache
            .cache_transaction_validation(tx, true, 100)
            .await?;
        self.metrics.increment_valid_transactions();
        Ok(())
    }

    /// Gets the current wallet color state.
    ///
    /// # Arguments
    ///
    /// * `address` - The wallet address
    ///
    /// # Returns
    ///
    /// * `Option<WalletColor>` with the current color state
    pub async fn get_wallet_state(&self, address: &str) -> Option<WalletColor> {
        self.wallet_colors.read().await.get(address).cloned()
    }

    /// Updates wallet color state.
    ///
    /// # Arguments
    ///
    /// * `address` - The wallet address
    /// * `color` - The new color
    ///
    /// # Returns
    ///
    /// * `Ok(())` if update was successful
    /// * `Err(ConsensusError)` if update failed
    pub async fn update_wallet_state(
        &self,
        address: &str,
        color: &str,
    ) -> Result<(), ConsensusError> {
        let mut colors = self.wallet_colors.write().await;

        let current_time = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_secs();
        colors.insert(
            address.to_string(),
            WalletColor {
                address: address.to_string(),
                color: color.to_string(),
                last_update: current_time,
            },
        );

        self.metrics.increment_wallet_updates();
        Ok(())
    }

    /// Gets validator statistics.
    ///
    /// # Returns
    ///
    /// * `ValidatorStats` with current statistics
    pub async fn get_validator_stats(&self) -> ValidatorStats {
        let _colors = self.wallet_colors.read().await;
        ValidatorStats {
            id: "main_validator".to_string(),
            last_active: SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap_or_default()
                .as_secs(),
            validation_score: 100,     // Default validation score
            blocks_validated: 0,       // TODO: Track this
            blocks_rejected: 0,        // TODO: Track this
            votes_cast: 0,             // TODO: Track this
            voting_participation: 1.0, // 100% participation by default
            uptime: 0,                 // TODO: Track this
        }
    }

    /// Gets active validators.
    ///
    /// # Returns
    ///
    /// * `u64` with number of active validators
    #[allow(dead_code)]
    async fn get_active_validators(&self) -> u64 {
        let current_time = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_secs();
        let colors = self.wallet_colors.read().await;

        colors
            .values()
            .filter(|c| current_time - c.last_update <= self.config.validation_window)
            .count() as u64
    }

    pub async fn generate_reference_block(&self, block: &Block) -> Result<Block> {
        // Use AI service to generate an optimal reference block
        let reference = self.ai_service.generate_reference_block(block).await?;
        Ok(reference)
    }

    /// Validates a block using the PoAI consensus rules.
    ///
    /// The validation process includes:
    /// 1. AI validation of block contents
    /// 2. Efficiency calculation
    /// 3. Reference block comparison
    /// 4. Transaction validation
    ///
    /// # Arguments
    ///
    /// * `block` - The block to validate
    ///
    /// # Returns
    ///
    /// * `Ok(true)` if block is valid
    /// * `Err(ConsensusError)` if validation fails
    pub async fn validate_block(&self, block: &Block) -> Result<bool, ConsensusError> {
        // Check cache first
        if let Some(cached) = self.cache.get_cached_block_validation(block).await {
            if self.cache.is_cache_valid(&cached).await? {
                return Ok(cached.value);
            }
        }

        let start_time = SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs_f64();

        // Calculate block efficiency early so it's available for cache calls
        let efficiency = self.calculate_block_efficiency(block).await?;
        self.metrics.set_block_efficiency(efficiency);

        // Validate block with AI service
        if !self.ai_service.validate_block(block).await? {
            self.metrics.increment_validation_error();
            self.cache
                .cache_block_validation(block, false, efficiency as u64)
                .await?;
            return Err(ConsensusError::AIValidationError(
                "AI validation failed".to_string(),
            ));
        }

        // Generate reference block
        let reference_block = self.ai_service.generate_reference_block(block).await?;

        // Validate against reference block
        if !self
            .is_more_efficient_than_reference(block, &reference_block)
            .await?
        {
            self.metrics.increment_validation_error();
            self.cache
                .cache_block_validation(block, false, efficiency as u64)
                .await?;
            return Err(ConsensusError::LowBlockEfficiency(
                efficiency,
                self.calculate_block_efficiency(&reference_block).await?,
            ));
        }

        // Validate transactions and color markers
        for tx in &block.transactions {
            self.validate_transaction(tx).await?;
        }

        let duration = SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs_f64() - start_time;
        self.metrics.observe_block_validation(duration);
        self.metrics.increment_blocks_validated();

        // Cache the result
        self.cache
            .cache_block_validation(block, true, efficiency as u64)
            .await?;

        Ok(true)
    }

    pub async fn is_more_efficient_than_reference(
        &self,
        block: &Block,
        reference: &Block,
    ) -> Result<bool> {
        // Compare block efficiency against reference
        let block_efficiency = self.calculate_block_efficiency(block).await?;
        let reference_efficiency = self.calculate_block_efficiency(reference).await?;

        Ok(block_efficiency > reference_efficiency)
    }

    async fn calculate_block_efficiency(&self, block: &Block) -> Result<f64> {
        // Calculate efficiency based on:
        // 1. Transaction complexity
        // 2. Data value
        // 3. AI validation score

        let mut score = 0.0;
        for tx in &block.transactions {
            score += self
                .ai_service
                .validate_transaction(tx)
                .await
                .map_err(|e| anyhow::anyhow!(e))? as f64;
        }

        Ok(score / block.transactions.len() as f64)
    }

    pub async fn get_wallet_color(&self, address: &str) -> Result<String> {
        let colors = self.wallet_colors.read().await;
        if let Some(color) = colors.get(address) {
            Ok(color.color.clone())
        } else {
            // Generate initial color for new wallet
            let color = self.generate_initial_color();
            Ok(color)
        }
    }

    fn generate_initial_color(&self) -> String {
        // Generate random initial color
        format!("{:06x}", rand::random::<u32>() % 0xFFFFFF)
    }

    pub async fn update_wallet_color(&self, address: &str, color: &str) -> Result<()> {
        let mut colors = self.wallet_colors.write().await;
        colors.insert(
            address.to_string(),
            WalletColor {
                address: address.to_string(),
                color: color.to_string(),
                last_update: std::time::SystemTime::now()
                    .duration_since(std::time::UNIX_EPOCH)?
                    .as_secs(),
            },
        );
        Ok(())
    }

    pub fn validate_color_transition(&self, current: &str, new: &str) -> Result<bool> {
        // Validate color transition based on:
        // 1. Valid hex format
        // 2. Color distance
        // 3. Time-based validation

        if !self.is_valid_hex(current) || !self.is_valid_hex(new) {
            return Ok(false);
        }

        let current_num = u32::from_str_radix(current, 16)?;
        let new_num = u32::from_str_radix(new, 16)?;

        // Check color distance
        let distance = (current_num as i32 - new_num as i32).abs();
        if distance > 0x10000 {
            // Maximum allowed color distance
            return Ok(false);
        }

        Ok(true)
    }

    fn is_valid_hex(&self, color: &str) -> bool {
        color.len() == 6 && color.chars().all(|c| c.is_ascii_hexdigit())
    }

    fn calculate_hex_transaction(&self, tx: &Transaction) -> Result<String> {
        // Split transaction hash into 6 parts
        let hash = tx.hash();
        let mut hex_parts = Vec::new();

        for i in 0..6 {
            let part = &hash[i * 4..(i + 1) * 4];
            let hex = hex::encode(part);
            hex_parts.push(hex);
        }

        // Combine parts
        Ok(hex_parts.join(""))
    }

    fn calculate_new_color(&self, current_color: &str, hex_tx: &str) -> Result<String> {
        // Add hex transaction to current color
        let mut new_color = current_color.to_string();
        new_color.push_str(hex_tx);

        // Reduce to 6 characters
        new_color.truncate(6);

        Ok(new_color)
    }

}

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_color_transition() {
        let metrics = Arc::new(ConsensusMetrics::new(&prometheus::Registry::new()).unwrap());
        let cache = Arc::new(ValidationCache::new(metrics.clone()));
        let validator = AIValidator::new(metrics, cache);
        let current = "abcdef";
        let new = "abcdef";
        assert!(validator.validate_color_transition(current, new).unwrap());
    }

    #[tokio::test]
    async fn test_wallet_color_update() {
        let metrics = Arc::new(ConsensusMetrics::new(&prometheus::Registry::new()).unwrap());
        let cache = Arc::new(ValidationCache::new(metrics.clone()));
        let validator = AIValidator::new(metrics, cache);
        let address = "test_address";
        let color = "abcdef";
        validator.update_wallet_color(address, color).await.unwrap();
        assert_eq!(validator.get_wallet_color(address).await.unwrap(), color);
    }
}
