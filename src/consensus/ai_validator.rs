use std::sync::Arc;
use tokio::sync::RwLock;
use anyhow::{Result, anyhow};
use serde::{Serialize, Deserialize};
use tracing::{info, error, debug};
use std::time::{SystemTime, UNIX_EPOCH};
use crate::blockchain::{Block, Transaction};
use crate::ai::{AIService, AIContextManager};
use crate::storage::hybrid_storage::{ValidatorState, AIContext};
use crate::network::NetworkNode;

#[derive(Debug, Serialize, Deserialize)]
pub struct ValidatorConfig {
    pub min_stake: u64,
    pub min_active_hours: u64,
    pub validation_window: u64,
    pub max_pending_validations: u32,
}

#[derive(Debug)]
pub struct AIValidator {
    config: ValidatorConfig,
    ai_service: Arc<RwLock<AIService>>,
    validator_reputation: Arc<RwLock<ValidatorReputation>>,
    ai_context: Arc<RwLock<AIContextManager>>,
    pending_validations: Arc<RwLock<Vec<ValidationRequest>>>,
    metrics: Arc<ValidatorMetrics>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ValidationRequest {
    pub block_hash: String,
    pub validator_id: String,
    pub timestamp: u64,
    pub status: ValidationStatus,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum ValidationStatus {
    Pending,
    Valid,
    Invalid,
    Failed,
}

#[derive(Debug)]
pub struct ValidatorMetrics {
    pub total_validations: u64,
    pub successful_validations: u64,
    pub failed_validations: u64,
    pub avg_validation_time: u64,
    pub pending_validations: u64,
}

impl AIValidator {
    pub fn new_with_poai(
        ai_service: Arc<RwLock<AIService>>,
        validator_reputation: Arc<RwLock<ValidatorReputation>>,
        ai_context: Arc<RwLock<AIContextManager>>,
        validation_window: u64,
    ) -> Result<Self> {
        let config = ValidatorConfig {
            min_stake: 1000000, // 1000 SELF tokens
            min_active_hours: 24,
            validation_window,
            max_pending_validations: 100,
        };

        let metrics = Arc::new(ValidatorMetrics {
            total_validations: 0,
            successful_validations: 0,
            failed_validations: 0,
            avg_validation_time: 0,
            pending_validations: 0,
        });

        let validator = Self {
            config,
            ai_service,
            validator_reputation,
            ai_context,
            pending_validations: Arc::new(RwLock::new(Vec::new())),
            metrics,
        };

        validator.start_validation_daemon()?
            .start_reputation_daemon()?
            .start_metrics_daemon()?
            .start_context_update_daemon()?;

        Ok(validator)
    }

    pub async fn validate_block(&self, block: &Block) -> Result<()> {
        // Check validator eligibility
        self.check_validator_eligibility().await?;

        // Get AI context
        let context = self.get_validator_context().await?;

        // Validate using AI
        let start = SystemTime::now();
        let result = self.ai_service.read().await.validate_block(block).await;
        let duration = start.elapsed()?.as_millis() as u64;

        // Update metrics
        self.metrics.total_validations += 1;
        self.metrics.avg_validation_time = (self.metrics.avg_validation_time + duration) / 2;

        match result {
            Ok(_) => {
                self.metrics.successful_validations += 1;
                self.store_validation_result(block.hash.clone(), ValidationStatus::Valid).await?;
                Ok(())
            }
            Err(e) => {
                self.metrics.failed_validations += 1;
                self.store_validation_result(block.hash.clone(), ValidationStatus::Failed).await?;
                Err(e)
            }
        }
    }

    pub async fn validate_transaction(&self, tx: &Transaction) -> Result<()> {
        // Check validator eligibility
        self.check_validator_eligibility().await?;

        // Get AI context
        let context = self.get_validator_context().await?;

        // Validate using AI
        let start = SystemTime::now();
        let result = self.ai_service.read().await.validate_transaction(tx).await;
        let duration = start.elapsed()?.as_millis() as u64;

        // Update metrics
        self.metrics.total_validations += 1;
        self.metrics.avg_validation_time = (self.metrics.avg_validation_time + duration) / 2;

        match result {
            Ok(_) => {
                self.metrics.successful_validations += 1;
                Ok(())
            }
            Err(e) => {
                self.metrics.failed_validations += 1;
                Err(e)
            }
        }
    }

    async fn check_validator_eligibility(&self) -> Result<()> {
        let validator_id = self.get_validator_id().await?;
        let reputation = self.validator_reputation.read().await;
        
        if !reputation.is_eligible(&validator_id).await? {
            return Err(anyhow!("Validator not eligible for validation"));
        }
        
        Ok(())
    }

    async fn get_validator_context(&self) -> Result<String> {
        let validator_id = self.get_validator_id().await?;
        let context = self.ai_context.read().await.get_validator_context(&validator_id).await?;
        
        if let Some(ctx) = context {
            Ok(ctx.context)
        } else {
            Err(anyhow!("Validator context not found"))
        }
    }

    async fn get_validator_id(&self) -> Result<String> {
        // TODO: Implement proper validator ID generation
        Ok("validator_1".to_string())
    }

    async fn store_validation_result(&self, block_hash: String, status: ValidationStatus) -> Result<()> {
        let validator_id = self.get_validator_id().await?;
        let timestamp = SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs();
        
        let request = ValidationRequest {
            block_hash,
            validator_id,
            timestamp,
            status,
        };
        
        self.pending_validations.write().await.push(request);
        Ok(())
    }

    fn start_validation_daemon(&self) -> Result<&Self> {
        let validator = self.clone();
        tokio::spawn(async move {
            loop {
                validator.process_pending_validations().await;
                tokio::time::sleep(std::time::Duration::from_secs(1)).await;
            }
        });
        
        Ok(self)
    }

    fn start_reputation_daemon(&self) -> Result<&Self> {
        let validator = self.clone();
        tokio::spawn(async move {
            loop {
                validator.update_validator_reputation().await;
                tokio::time::sleep(std::time::Duration::from_secs(3600)).await;
            }
        });
        
        Ok(self)
    }

    fn start_metrics_daemon(&self) -> Result<&Self> {
        let validator = self.clone();
        tokio::spawn(async move {
            loop {
                validator.log_metrics().await;
                tokio::time::sleep(std::time::Duration::from_secs(60)).await;
            }
        });
        
        Ok(self)
    }

    fn start_context_update_daemon(&self) -> Result<&Self> {
        let validator = self.clone();
        tokio::spawn(async move {
            loop {
                validator.update_validator_context().await;
                tokio::time::sleep(std::time::Duration::from_secs(300)).await;
            }
        });
        
        Ok(self)
    }

    async fn process_pending_validations(&self) {
        let mut validations = self.pending_validations.write().await;
        if validations.len() > self.config.max_pending_validations as usize {
            validations.drain(0..(validations.len() - self.config.max_pending_validations as usize));
        }
    }

    async fn update_validator_reputation(&self) {
        let validator_id = self.get_validator_id().await.unwrap_or_default();
        let reputation = self.validator_reputation.read().await;
        reputation.update_reputation(&validator_id).await.unwrap_or_else(|e| {
            error!("Failed to update reputation: {}", e);
        });
    }

    async fn log_metrics(&self) {
        let metrics = self.metrics.clone();
        info!(
            "Validator Metrics: {} total validations, {} successful, {} failed, avg time: {}ms",
            metrics.total_validations,
            metrics.successful_validations,
            metrics.failed_validations,
            metrics.avg_validation_time
        );
    }

    async fn update_validator_context(&self) {
        let validator_id = self.get_validator_id().await.unwrap_or_default();
        let context = self.ai_context.read().await;
        context.update_validator_context(&validator_id).await.unwrap_or_else(|e| {
            error!("Failed to update context: {}", e);
        });
    }
}
