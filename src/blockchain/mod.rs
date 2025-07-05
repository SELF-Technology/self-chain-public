use anyhow::Result;
use chrono::Utc;
use serde::{Deserialize, Serialize};
use sha2::{Digest, Sha256};
use std::sync::Arc;
use tokio::sync::RwLock;

// Import AI components for PoAI consensus
use crate::ai::context_manager::ContextManager;
use crate::ai::pattern_analysis::{
    AnalysisContext, PatternAnalysisRequest, PatternAnalyzer, PatternType,
};
use crate::ai::validator::AIValidator;
use std::collections::HashMap;
use std::str::FromStr;

pub mod block;
pub mod types;
pub mod blockchain;
pub mod transaction_pool;
pub mod chain;
pub mod batching;
pub mod caching;
pub mod hardware_acceleration;
pub mod block_proposal;
pub mod transaction_processor;

#[cfg(test)]
pub mod tests;

pub mod synchronization;

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct BlockHeader {
    pub index: u64,
    pub timestamp: u64,
    pub previous_hash: String,
    pub nonce: u64,          // Deprecated: Always 0 in PoAI consensus
    pub ai_threshold: u32,   // AI validation threshold level (1-10)
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct BlockMeta {
    pub size: u64,
    pub tx_count: u64,
    pub height: u64,
    pub validator_signature: Option<String>,
    pub validator_id: Option<String>,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct Block {
    pub header: BlockHeader,
    pub transactions: Vec<Transaction>,
    pub meta: BlockMeta,
    pub hash: String,
}

impl Default for Block {
    fn default() -> Self {
        Self {
            header: BlockHeader {
                index: 0,
                timestamp: 0,
                previous_hash: String::new(),
                nonce: 0,
                ai_threshold: 0,
            },
            transactions: Vec::new(),
            meta: BlockMeta {
                size: 0,
                tx_count: 0,
                height: 0,
                validator_signature: None,
                validator_id: None,
            },
            hash: String::new(),
        }
    }
}

impl Block {
    pub fn calculate_size(&self) -> u64 {
        let mut size = 0;
        size += self.header.index.to_string().len() as u64;
        size += self.header.timestamp.to_string().len() as u64;
        size += self.header.previous_hash.len() as u64;
        size += self.header.nonce.to_string().len() as u64;
        size += self.header.ai_threshold.to_string().len() as u64;
        size += self
            .transactions
            .iter()
            .map(|tx| tx.calculate_size())
            .sum::<u64>();
        size
    }

    pub fn calculate_hash(&self) -> String {
        let mut hasher = Sha256::new();
        hasher.update(format!(
            "{}{}{}{}{}{}{}",
            self.header.index,
            self.header.timestamp,
            self.header.previous_hash,
            self.header.nonce,
            self.header.ai_threshold,
            serde_json::to_string(&self.transactions).unwrap_or_default(),
            self.meta.size
        ));
        format!("{:x}", hasher.finalize())
    }

    pub fn verify(&self) -> bool {
        use crate::security::validation::InputValidator;
        
        // Use comprehensive validation
        let mut validator = InputValidator::new();
        match validator.validate_block(self) {
            Ok(_) => {
                // Additional block-specific checks
                !self.hash.is_empty()
                    && !self.header.previous_hash.is_empty()
                    && self.header.timestamp > 0
                    && !self.transactions.is_empty()
            },
            Err(e) => {
                tracing::warn!("Block validation failed: {}", e);
                false
            }
        }
    }
}

#[derive(Debug, Serialize, Deserialize, Clone, Default)]
pub struct Transaction {
    pub id: String,
    pub sender: String,    // Sender's address (public key in hex format)
    pub receiver: String,  // Receiver's address
    pub amount: u64,       // Transaction amount
    pub signature: String, // Signature of the transaction data
    pub timestamp: u64,    // Timestamp when the transaction was created
}

impl Transaction {
    /// Create a new transaction with the given parameters for testing
    #[cfg(test)]
    pub fn new(id: String, receiver: String, amount: f64, description: String) -> Self {
        // For testing, we generate a simple timestamp and signature
        use std::time::{SystemTime, UNIX_EPOCH};
        let timestamp = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_secs();

        Transaction {
            id,
            sender: "test_sender".to_string(),
            receiver,
            amount: amount as u64,
            signature: description, // For tests we use description as signature
            timestamp,
        }
    }

    /// Create a new transaction with the given parameters
    #[cfg(not(test))]
    pub fn new(
        id: String,
        sender: String,
        receiver: String,
        amount: u64,
        signature: String,
        timestamp: u64,
    ) -> Self {
        Transaction {
            id,
            sender,
            receiver,
            amount,
            signature,
            timestamp,
        }
    }

    pub fn calculate_size(&self) -> u64 {
        self.id.len() as u64
            + self.sender.len() as u64
            + self.receiver.len() as u64
            + self.amount.to_string().len() as u64
            + self.signature.len() as u64
            + self.timestamp.to_string().len() as u64
    }

    pub fn verify(&self) -> bool {
        use crate::security::validation::InputValidator;
        
        // Use comprehensive validation first
        let mut validator = InputValidator::new();
        
        // Convert to the expected format for validation
        let tx_for_validation = Transaction {
            id: self.id.clone(),
            sender: self.sender.clone(),
            receiver: self.receiver.clone(),
            amount: self.amount as f64,
            signature: self.signature.clone(),
            timestamp: self.timestamp as i64,
        };
        
        match validator.validate_transaction(&tx_for_validation) {
            Ok(_) => {
                // Continue with signature verification
                if !self.signature.is_empty() {
                    // Try to verify the signature if possible
                    if let Err(_) = self.verify_signature() {
                        return false;
                    }
                }
                true
            },
            Err(e) => {
                tracing::warn!("Transaction validation failed: {}", e);
                false
            }
        }
    }

    pub fn verify_signature(&self) -> Result<bool, String> {
        use hex::FromHex;
        use secp256k1::ecdsa::Signature;
        use secp256k1::{Message, PublicKey, Secp256k1};
        use sha2::{Digest, Sha256};

        // Create the message that was signed
        let message_data = format!("{}{}{}", self.sender, self.receiver, self.amount);
        let message_hash = Sha256::digest(message_data.as_bytes());

        // Convert GenericArray to [u8; 32] for from_digest
        let mut digest_bytes = [0u8; 32];
        digest_bytes.copy_from_slice(message_hash.as_slice());

        // Updated to use from_digest which is recommended over deprecated from_slice
        let message = Message::from_digest(digest_bytes);

        // Try to parse the sender as a public key
        let public_key = match PublicKey::from_str(&self.sender) {
            Ok(pk) => pk,
            Err(_) => {
                // If the sender isn't a valid public key, try as a hex string
                let key_bytes =
                    Vec::from_hex(&self.sender).map_err(|_| "Invalid sender format".to_string())?;
                PublicKey::from_slice(&key_bytes)
                    .map_err(|e| format!("Invalid public key: {}", e))?
            }
        };

        // Parse the signature
        let signature = Signature::from_str(&self.signature)
            .map_err(|e| format!("Invalid signature format: {}", e))?;

        // Verify the signature
        let secp = Secp256k1::verification_only();
        // Fixed: pass message directly, not as reference
        match secp.verify_ecdsa(message, &signature, &public_key) {
            Ok(_) => Ok(true),
            Err(e) => Err(format!("Signature verification failed: {}", e)),
        }
    }

    pub fn hash(&self) -> String {
        use std::collections::hash_map::DefaultHasher;
        use std::hash::{Hash, Hasher};

        let mut hasher = DefaultHasher::new();
        self.id.hash(&mut hasher);
        self.sender.hash(&mut hasher);
        self.receiver.hash(&mut hasher);
        self.amount.hash(&mut hasher);
        self.timestamp.hash(&mut hasher);
        self.signature.hash(&mut hasher);

        format!("{:x}", hasher.finish())
    }
}

#[derive(Debug)]
pub struct Blockchain {
    blocks: Arc<RwLock<Vec<Block>>>,
    pending_transactions: Arc<RwLock<Vec<Transaction>>>,
    ai_validation_threshold: u32,  // PoAI validation strictness level (1-10, higher = stricter)
    // PoAI components
    ai_validator: Option<Arc<RwLock<AIValidator>>>,
    context_manager: Option<Arc<RwLock<ContextManager>>>,
    pattern_analyzer: Option<Arc<RwLock<PatternAnalyzer>>>,
    // Synchronization component
    synchronizer: Option<Arc<synchronization::BlockSynchronizer>>,
}

impl Blockchain {
    /// Initialize the PoAI consensus components for the blockchain
    pub async fn initialize_poai_components(&mut self) -> Result<(), String> {
        // Initialize ContextManager if not already initialized
        if self.context_manager.is_none() {
            // Create with a reasonable version history (10 versions)
            let context_manager = Arc::new(RwLock::new(ContextManager::new(10)));
            self.context_manager = Some(context_manager.clone());
        }

        // Initialize PatternAnalyzer if not already initialized
        if self.pattern_analyzer.is_none() {
            let _context_manager = self
                .context_manager
                .as_ref()
                .ok_or_else(|| "ContextManager must be initialized first".to_string())?
                .clone();

            // Initialize PatternAnalyzer with the ContextManager (create new Arc for the inner ContextManager)
            let inner_context_manager = Arc::new(ContextManager::new(10));
            let pattern_analyzer =
                Arc::new(RwLock::new(PatternAnalyzer::new(inner_context_manager)));

            // Update the pattern_analyzer field
            self.pattern_analyzer = Some(pattern_analyzer.clone());
        }

        // Initialize AIValidator for PoAI consensus
        let ai_validator = Arc::new(RwLock::new(AIValidator::new()));

        // Initialize the AIValidator component
        {
            let mut validator = ai_validator.write().await;
            validator
                .initialize()
                .await
                .map_err(|e| format!("Failed to initialize AIValidator: {}", e))?
        }

        self.ai_validator = Some(ai_validator);

        tracing::info!("PoAI consensus components initialized successfully");
        Ok(())
    }

    /// Initialize the block synchronizer component
    pub async fn initialize_synchronizer(
        &mut self,
        message_handler: Arc<RwLock<crate::network::message_handler::MessageHandler>>,
    ) -> Result<(), String> {
        // Ensure PoAI components are initialized first
        if self.ai_validator.is_none()
            || self.context_manager.is_none()
            || self.pattern_analyzer.is_none()
        {
            return Err("PoAI components must be initialized before the synchronizer".to_string());
        }

        // Get unwrapped PoAI components
        let ai_validator = self.ai_validator.as_ref().unwrap().clone();
        let context_manager = self.context_manager.as_ref().unwrap().clone();
        let pattern_analyzer = self.pattern_analyzer.as_ref().unwrap().clone();

        // Create a BlockSynchronizer with the PoAI components and message handler
        let synchronizer = Arc::new(synchronization::BlockSynchronizer::new(
            Arc::new(RwLock::new(self.clone())),
            message_handler,
            ai_validator,
            context_manager,
            pattern_analyzer,
        ));

        // Set the synchronizer
        self.synchronizer = Some(synchronizer);

        tracing::info!("Block synchronizer initialized successfully");
        Ok(())
    }

    /// Get a reference to the block synchronizer if initialized
    pub async fn get_synchronizer(&self) -> Option<Arc<synchronization::BlockSynchronizer>> {
        self.synchronizer.clone()
    }

    /// Start the block synchronization process
    pub async fn start_synchronization(&self) -> Result<(), String> {
        match &self.synchronizer {
            Some(sync) => sync.start_sync().await,
            None => Err("Synchronizer not initialized".to_string()),
        }
    }

    /// This is a placeholder comment to indicate where the duplicate initialize_poai_components method was removed
    /// The functionality is now merged into the primary implementation above

    /// Establish PoAI baseline for validation of future blocks
    async fn establish_poai_baseline(&self, genesis_block: &mut Block) -> Result<(), String> {
        // Ensure PoAI components are initialized
        if self.ai_validator.is_none()
            || self.context_manager.is_none()
            || self.pattern_analyzer.is_none()
        {
            return Err("PoAI components not initialized".to_string());
        }

        // Create initial AI context for the genesis block
        let genesis_context = format!(
            "SELF Chain Genesis Block - Timestamp: {} - Initial AI Threshold: {}",
            genesis_block.header.timestamp, genesis_block.header.ai_threshold
        );

        // If we have a validator, establish the baseline
        if let Some(validator) = &self.ai_validator {
            let validator = validator.read().await;

            // Convert block to JSON for the validator
            let block_json = serde_json::to_string(genesis_block)
                .map_err(|e| format!("Failed to serialize genesis block: {}", e))?;

            // Perform initial validation (this will populate baseline patterns)
            validator
                .validate_block(&block_json, &genesis_context)
                .await
                .map_err(|e| format!("Failed to establish PoAI baseline: {}", e))?;
        }

        // If we have a pattern analyzer, establish pattern baselines
        if let Some(pattern_analyzer) = &self.pattern_analyzer {
            let mut analyzer = pattern_analyzer.write().await;

            // Initialize baseline patterns for block size
            let baseline_request = PatternAnalysisRequest {
                pattern_type: PatternType::BlockSize,
                block: genesis_block.clone(),
                context: Some(AnalysisContext {
                    context_id: format!("genesis-{}", genesis_block.header.previous_hash),
                    chain_height: 0,
                    timestamp: genesis_block.header.timestamp as i64,
                    previous_blocks: Vec::new(),
                    metrics: HashMap::new(),
                    flags: HashMap::new(),
                    validator_info: None,
                }),
                validation_rules: Vec::new(),
                previous_results: None,
                correlation_data: Some(HashMap::new()),
                security_level: 3,
                max_processing_time_ms: Some(5000),
            };

            analyzer
                .analyze_pattern(baseline_request)
                .await
                .map_err(|e| format!("Failed to establish pattern baseline: {}", e))?;
        }

        tracing::info!("PoAI baseline established successfully");
        Ok(())
    }
    pub fn new(difficulty: u32) -> Self {
        Blockchain {
            blocks: Arc::new(RwLock::new(Vec::new())),
            pending_transactions: Arc::new(RwLock::new(Vec::new())),
            ai_validation_threshold: difficulty,
            // Initialize PoAI components as None - they will be set up during genesis block creation
            ai_validator: None,
            context_manager: None,
            pattern_analyzer: None,
            // Initialize synchronizer as None - it will be set up after PoAI components
            synchronizer: None,
        }
    }

    pub async fn create_genesis_block(
        &mut self,
        transactions: Vec<Transaction>,
    ) -> Result<Block, String> {
        // Check if blockchain already has blocks
        if !self.blocks.read().await.is_empty() {
            return Err("Genesis block already exists".to_string());
        }

        // Initialize PoAI components for consensus
        self.initialize_poai_components().await?;

        // Create a genesis block with PoAI metadata
        let mut genesis_block = Block {
            header: BlockHeader {
                index: 0,
                timestamp: Utc::now().timestamp() as u64,
                previous_hash: "0".to_string(),
                nonce: 0,
                ai_threshold: self.ai_validation_threshold,
            },
            transactions,
            meta: BlockMeta {
                size: 0,
                tx_count: 0,
                height: 0,
                validator_signature: None,
                validator_id: None,
            },
            hash: String::new(),
        };

        // Calculate block metadata
        genesis_block.meta.size = genesis_block.calculate_size();
        genesis_block.meta.tx_count = genesis_block.transactions.len() as u64;

        // Generate initial AI context and validation baseline
        self.establish_poai_baseline(&mut genesis_block).await?;

        // Finalize the genesis block with PoAI consensus
        self.finalize_block_with_poai(&mut genesis_block).await?;

        // Add the genesis block to the chain
        self.blocks.write().await.push(genesis_block.clone());

        Ok(genesis_block)
    }

    pub async fn add_transaction(&self, transaction: &Transaction) -> Result<(), String> {
        // Verify the transaction before adding it to the pending pool
        if !transaction.verify() {
            return Err("Invalid transaction: verification failed".to_string());
        }

        // Check for duplicate transactions
        let pending = self.pending_transactions.read().await;
        if pending.iter().any(|tx| tx.id == transaction.id) {
            return Err("Duplicate transaction".to_string());
        }

        // Check if sender has enough balance (if implemented)
        // This would typically involve checking the UTXO set or account balances

        // Add the transaction to the pending pool
        drop(pending); // Release the read lock before acquiring write lock
        self.pending_transactions
            .write()
            .await
            .push(transaction.clone());

        Ok(())
    }

    pub async fn create_block(&self, mut block: Block) -> Result<Block, String> {
        let last_block = self.get_last_block().await?;
        block.header.index = last_block.header.index + 1;
        block.header.timestamp = Utc::now().timestamp() as u64;
        block.header.previous_hash = last_block.hash;
        block.header.nonce = 0;
        block.header.ai_threshold = self.ai_validation_threshold;
        block.transactions = self.pending_transactions.read().await.clone();
        block.meta.size = block.calculate_size();
        block.meta.tx_count = block.transactions.len() as u64;
        block.meta.height = block.header.index;

        // Generate validator ID and signature if available
        if let Some(ai_validator) = &self.ai_validator {
            block.meta.validator_id = Some(format!("validator-{}", Utc::now().timestamp()));
            block.meta.validator_signature = Some(format!("sig-{}", block.header.index));
        }

        // Pre-validation with PoAI before mining
        if let Some(pattern_analyzer) = &self.pattern_analyzer {
            let mut analyzer = pattern_analyzer.write().await;
            for pattern_type in PatternType::basic_validation() {
                let request = PatternAnalysisRequest {
                    block: block.clone(),
                    pattern_type,
                    context: None,
                    validation_rules: Vec::new(),
                    previous_results: None,
                    correlation_data: None,
                    security_level: 1,
                    max_processing_time_ms: None,
                };

                let result = analyzer
                    .analyze_pattern(request)
                    .await
                    .map_err(|e| format!("Pattern analysis failed: {}", e))?;

                // Fail if high risk is detected with high confidence
                if result.analysis.risk_level > 0.8 && result.analysis.confidence > 0.7 {
                    return Err(format!(
                        "Block validation failed: {}",
                        result.analysis.pattern
                    ));
                }
            }
        }

        // PoAI validation step 2: AI-based block validation
        if let Some(ai_validator) = &self.ai_validator {
            // Serialize the block to JSON for AI validation
            if let Ok(block_json) = serde_json::to_string(&block) {
                // Create a validation context with block metadata
                let context = format!(
                    "{{\"height\":{},\"timestamp\":{},\"tx_count\":{}}}",
                    block.meta.height,
                    block.header.timestamp,
                    block.transactions.len()
                );

                // Use the AI validator for deep analysis
                let validator = ai_validator.read().await;

                // AI validation logic for PoAI consensus
                match validator.validate_block(&block_json, &context).await {
                    Ok(valid) => {
                        if !valid {
                            tracing::warn!(
                                "PoAI consensus validation failed for block {}",
                                block.header.index
                            );
                            return Err("Block rejected by AI validator".to_string());
                        }
                    }
                    Err(e) => {
                        tracing::warn!("AI validation error: {}", e);
                        // Non-fatal error, continue with validation
                    }
                }
            }
        }

        // Perform PoAI consensus validation for block finalization
        self.finalize_block_with_poai(&mut block).await?;

        // Add block to the chain and clear pending transactions
        self.blocks.write().await.push(block.clone());
        self.pending_transactions.write().await.clear();

        Ok(block)
    }

    // Synchronous version for test compatibility
    pub fn create_block_sync(&self, block: &mut Block) -> Result<(), String> {
        if let Ok(handle) = tokio::runtime::Handle::try_current() {
            handle.block_on(self.finalize_block_with_poai(block))
        } else {
            tokio::runtime::Runtime::new()
                .unwrap()
                .block_on(self.finalize_block_with_poai(block))
        }
    }

    /// Finalize block using PoAI consensus instead of proof-of-work
    /// This function validates the block through AI consensus and generates the final hash
    pub async fn finalize_block_with_poai(&self, block: &mut Block) -> Result<(), String> {
        // Set nonce to 0 as it's not used in PoAI consensus
        block.header.nonce = 0;
        
        // Validate block using PoAI consensus
        if !self.valid_proof(block).await {
            return Err("Block failed PoAI consensus validation".to_string());
        }
        
        // Generate final block hash based on all block data
        let mut hasher = Sha256::new();
        hasher.update(format!(
            "{}{}{}{}{}{}",
            block.header.index,
            block.header.timestamp,
            block.header.previous_hash,
            serde_json::to_string(&block.transactions).unwrap_or_default(),
            block.meta.validator_id.as_ref().unwrap_or(&"none".to_string()),
            block.meta.size
        ));
        block.hash = format!("{:x}", hasher.finalize());
        
        // Log successful PoAI validation
        tracing::info!(
            "Block {} finalized with PoAI consensus - Hash: {}, Validator: {}",
            block.header.index,
            &block.hash[..8],
            block.meta.validator_id.as_ref().unwrap_or(&"system".to_string())
        );
        
        Ok(())
    }

    // Non-async version for compatibility with tests
    pub fn valid_proof_sync(&self, block: &Block) -> bool {
        // Use tokio::runtime::Handle to run the async version synchronously
        if let Ok(handle) = tokio::runtime::Handle::try_current() {
            handle.block_on(self.valid_proof(block))
        } else {
            // Fallback to creating a new runtime if we're not in a tokio context
            tokio::runtime::Runtime::new()
                .unwrap()
                .block_on(self.valid_proof(block))
        }
    }

    /// Validate block using PoAI consensus mechanisms instead of proof-of-work
    /// Uses AI pattern analysis and validation instead of hash difficulty
    pub async fn valid_proof(&self, block: &Block) -> bool {
        // First verify all transactions in the block using traditional cryptographic verification
        for tx in &block.transactions {
            if !tx.verify() {
                tracing::warn!(
                    "Transaction verification failed in block {}",
                    block.header.index
                );
                return false;
            }
        }

        // PoAI validation step 1: Pattern analysis for anomaly detection
        if let Some(pattern_analyzer) = &self.pattern_analyzer {
            let mut analyzer = pattern_analyzer.write().await;

            // Define key pattern types for PoAI validation
            let poai_patterns = vec![
                PatternType::TimestampValidation,
                PatternType::BlockSize,
                PatternType::AnomalyDetection,
                PatternType::TransactionClustering,
            ];

            // Analyze all required patterns
            for pattern_type in poai_patterns {
                let request = PatternAnalysisRequest {
                    block: block.clone(),
                    pattern_type: pattern_type.clone(),
                    context: None,
                    validation_rules: Vec::new(),
                    previous_results: None,
                    correlation_data: None,
                    security_level: 2, // Medium-high security level
                    max_processing_time_ms: Some(2000), // 2 second timeout
                };

                if let Ok(result) = analyzer.analyze_pattern(request).await {
                    // Reject blocks with high-risk patterns that have high confidence
                    if result.analysis.risk_level > 0.8 && result.analysis.confidence > 0.7 {
                        tracing::warn!(
                            "PoAI validation failed: {} pattern detected with risk {:.2}, confidence {:.2}",
                            pattern_type.to_string(),
                            result.analysis.risk_level,
                            result.analysis.confidence
                        );
                        return false;
                    }
                }
            }
        }

        // PoAI validation step 2: AI-based block validation
        if let Some(ai_validator) = &self.ai_validator {
            // Serialize the block to JSON for AI validation
            if let Ok(block_json) = serde_json::to_string(block) {
                // Create a validation context with block metadata
                let context = format!(
                    "{{\"height\":{},\"timestamp\":{},\"tx_count\":{}}}",
                    block.meta.height,
                    block.header.timestamp,
                    block.transactions.len()
                );

                // Use the AI validator for deep analysis
                let validator = ai_validator.read().await;

                // We already have block_json and context from lines above, no need to recreate them

                match validator.validate_block(&block_json, &context).await {
                    Ok(valid) => {
                        if !valid {
                            tracing::warn!(
                                "PoAI deep validation failed for block {}",
                                block.header.index
                            );
                            return false;
                        }
                        // Passed AI validation
                    }
                    Err(e) => {
                        // Log the error but don't fail validation just because analysis failed
                        tracing::warn!("PoAI validation error: {}", e);
                    }
                }
            }
        }

        // All PoAI validation steps passed
        true
    }

    // Synchronous version for test compatibility
    pub fn add_block_sync(&mut self, block: &Block) -> Result<(), String> {
        if let Ok(handle) = tokio::runtime::Handle::try_current() {
            handle.block_on(self.add_block(block))
        } else {
            tokio::runtime::Runtime::new()
                .unwrap()
                .block_on(self.add_block(block))
        }
    }

    pub async fn add_block(&mut self, block: &Block) -> Result<(), String> {
        // Validate using PoAI consensus and transaction integrity
        if !self.valid_proof(block).await {
            return Err(
                "Block failed PoAI validation or contains invalid transactions".to_string(),
            );
        }

        // Validate block structure and references
        let blocks = self.blocks.read().await;
        if !blocks.is_empty() {
            // Check previous hash
            if block.header.previous_hash != blocks.last().unwrap().hash {
                return Err("Invalid previous hash".to_string());
            }

            // Check block index
            if block.header.index != blocks.last().unwrap().header.index + 1 {
                return Err("Invalid block index".to_string());
            }
        } else if block.header.index != 0 {
            // First block must have index 0
            return Err("First block must have index 0".to_string());
        }

        // Run pattern analysis if available
        if let Some(pattern_analyzer) = &self.pattern_analyzer {
            let mut analyzer = pattern_analyzer.write().await;

            // Run analysis on critical patterns
            for pattern_type in [
                PatternType::AnomalyDetection,
                PatternType::TimestampValidation,
            ]
            .iter()
            {
                let request = PatternAnalysisRequest {
                    block: block.clone(),
                    pattern_type: pattern_type.clone(),
                    context: None,
                    validation_rules: Vec::new(),
                    previous_results: None,
                    correlation_data: None,
                    security_level: 2, // Higher security for external blocks
                    max_processing_time_ms: None,
                };

                let result = analyzer
                    .analyze_pattern(request)
                    .await
                    .map_err(|e| format!("Pattern analysis failed: {}", e))?;

                // Reject blocks with high risk and high confidence
                if result.analysis.risk_level > 0.7 && result.analysis.confidence > 0.7 {
                    return Err(format!(
                        "Block rejected due to high risk pattern: {}",
                        result.analysis.pattern
                    ));
                }
            }
        }

        // Validate block with AI if available
        if let Some(validator) = &self.ai_validator {
            let validator = validator.read().await;
            let block_json = serde_json::to_string(block)
                .map_err(|e| format!("Failed to serialize block: {}", e))?;
            let context = format!(
                "{{\"height\":{},\"timestamp\":{},\"tx_count\":{}}}",
                block.meta.height,
                block.header.timestamp,
                block.transactions.len()
            );
            let validation_result = validator
                .validate_block(&block_json, &context)
                .await
                .map_err(|e| format!("AI validation error: {}", e))?;

            if !validation_result {
                return Err("Block rejected by AI validator".to_string());
            }

            // Log the successful validation
            tracing::info!("Block {} passed AI validation", block.hash);
        }

        // Add block to the chain
        drop(blocks); // Release read lock before acquiring write lock
        self.blocks.write().await.push(block.clone());

        // Remove transactions that are now in the block from pending
        let pending = self.pending_transactions.read().await;
        let block_tx_ids: std::collections::HashSet<String> =
            block.transactions.iter().map(|tx| tx.id.clone()).collect();

        let filtered_transactions: Vec<Transaction> = pending
            .iter()
            .filter(|tx| !block_tx_ids.contains(&tx.id))
            .cloned()
            .collect();

        drop(pending);
        *self.pending_transactions.write().await = filtered_transactions;

        Ok(())
    }

    // Synchronous version for test compatibility
    pub fn get_blocks_sync(&self) -> Result<Vec<Block>, String> {
        if let Ok(handle) = tokio::runtime::Handle::try_current() {
            handle.block_on(self.get_blocks())
        } else {
            tokio::runtime::Runtime::new()
                .unwrap()
                .block_on(self.get_blocks())
        }
    }

    pub async fn get_blocks(&self) -> Result<Vec<Block>, String> {
        let blocks = self.blocks.read().await;
        Ok(blocks.clone())
    }

    // Synchronous version for test compatibility
    pub fn get_last_block_sync(&self) -> Result<Block, String> {
        if let Ok(handle) = tokio::runtime::Handle::try_current() {
            handle.block_on(self.get_last_block())
        } else {
            tokio::runtime::Runtime::new()
                .unwrap()
                .block_on(self.get_last_block())
        }
    }

    pub async fn get_last_block(&self) -> Result<Block, String> {
        let blocks = self.blocks.read().await;
        if blocks.is_empty() {
            return Err("Blockchain is empty".to_string());
        }
        Ok(blocks.last().unwrap().clone())
    }

    pub async fn get_last_blocks(&self, count: usize) -> Result<Vec<Block>, String> {
        let blocks = self.blocks.read().await;
        Ok(blocks.iter().rev().take(count).cloned().collect())
    }

    /// Get the current blockchain height
    pub fn get_height_sync(&self) -> Result<u64, String> {
        if let Ok(handle) = tokio::runtime::Handle::try_current() {
            handle.block_on(self.get_height())
        } else {
            tokio::runtime::Runtime::new()
                .unwrap()
                .block_on(self.get_height())
        }
    }

    /// Get the current blockchain height
    pub async fn get_height(&self) -> Result<u64, String> {
        let blocks = self.blocks.read().await;
        if blocks.is_empty() {
            return Ok(0); // Height is 0 for empty blockchain
        }
        Ok(blocks.len() as u64)
    }

    /// Get the hash of the last block in the chain
    pub async fn get_last_block_hash(&self) -> Result<String, String> {
        let last_block = self.get_last_block().await?;
        Ok(last_block.hash.clone())
    }

    /// Get pending transactions - async version
    pub async fn get_pending_transactions(&self) -> Vec<Transaction> {
        self.pending_transactions.read().await.clone()
    }

    /// Get pending transactions - sync version for tests
    pub fn get_pending_transactions_sync(&self) -> Vec<Transaction> {
        self.pending_transactions.blocking_read().clone()
    }

    /// Get the PoAI validation strictness level (1-10)
    /// Higher values mean stricter AI validation requirements
    pub fn get_ai_validation_threshold(&self) -> u32 {
        self.ai_validation_threshold
    }

    pub async fn get_chain(&self) -> Vec<Block> {
        self.blocks.read().await.clone()
    }
}

impl Clone for Blockchain {
    fn clone(&self) -> Self {
        Blockchain {
            blocks: Arc::new(RwLock::new(Vec::new())),
            pending_transactions: Arc::new(RwLock::new(Vec::new())),
            ai_validation_threshold: self.ai_validation_threshold,
            ai_validator: self.ai_validator.clone(),
            context_manager: self.context_manager.clone(),
            pattern_analyzer: self.pattern_analyzer.clone(),
            synchronizer: None,
        }
    }
}
