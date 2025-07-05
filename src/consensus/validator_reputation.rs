use std::sync::Arc;
use tokio::sync::RwLock;
use anyhow::{Result, anyhow};
use serde::{Serialize, Deserialize};
use tracing::{info, error, debug};
use std::collections::HashMap;
use std::time::{SystemTime, UNIX_EPOCH};
use crate::storage::hybrid_storage::ValidatorState;

#[derive(Debug, Serialize, Deserialize)]
pub struct ValidatorReputation {
    config: ValidatorConfig,
    validators: Arc<RwLock<HashMap<String, ValidatorState>>>,
    last_update: Arc<RwLock<u64>>, // Last update timestamp
}

impl ValidatorReputation {
    pub fn new(min_stake: u64, min_active_hours: u64) -> Result<Self> {
        Ok(Self {
            config: ValidatorConfig {
                min_stake,
                min_active_hours,
                validation_window: 3600,
                max_pending_validations: 100,
            },
            validators: Arc::new(RwLock::new(HashMap::new())),
            last_update: Arc::new(RwLock::new(0)),
        })
    }

    pub async fn is_eligible(&self, validator_id: &str) -> Result<bool> {
        let validators = self.validators.read().await;
        if let Some(state) = validators.get(validator_id) {
            return Ok(self.check_eligibility(state).await?);
        }
        Ok(false)
    }

    async fn check_eligibility(&self, state: &ValidatorState) -> Result<bool> {
        let current_time = SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs();
        
        // Check stake requirement
        if state.stake < self.config.min_stake {
            return Ok(false);
        }
        
        // Check active hours
        if current_time - state.last_update > self.config.min_active_hours * 3600 {
            return Ok(false);
        }
        
        // Check validation score
        if state.validation_score < 100 { // Minimum score requirement
            return Ok(false);
        }
        
        Ok(true)
    }

    pub async fn update_reputation(&self, validator_id: &str) -> Result<()> {
        let mut validators = self.validators.write().await;
        let current_time = SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs();
        
        if let Some(state) = validators.get_mut(validator_id) {
            // Update last update time
            state.last_update = current_time;
            
            // Update validation score based on recent validations
            let stats = self.get_validator_stats(validator_id).await?;
            state.validation_score = self.calculate_validation_score(&stats);
            
            // Update AI context
            self.update_ai_context(validator_id, state).await?;
        }
        
        Ok(())
    }

    fn calculate_validation_score(&self, stats: &UsageStats) -> u64 {
        if stats.total_validations == 0 {
            return 100; // Default score for new validators
        }
        
        let success_rate = (stats.successful_validations as f64 / stats.total_validations as f64) * 100.0;
        let score = (success_rate * 0.7) + // 70% weight on success rate
                     ((1000.0 / stats.avg_response_time as f64) * 0.3); // 30% weight on response time
        
        score as u64
    }

    async fn get_validator_stats(&self, validator_id: &str) -> Result<UsageStats> {
        let validators = self.validators.read().await;
        if let Some(state) = validators.get(validator_id) {
            Ok(state.usage_stats.clone())
        } else {
            Err(anyhow!("Validator not found"))
        }
    }

    async fn update_ai_context(&self, validator_id: &str, state: &ValidatorState) -> Result<()> {
        // TODO: Implement AI context update logic
        Ok(())
    }

    pub async fn add_validator(&self, validator: ValidatorState) -> Result<()> {
        let mut validators = self.validators.write().await;
        validators.insert(validator.id.clone(), validator);
        Ok(())
    }

    pub async fn get_validator(&self, validator_id: &str) -> Result<Option<ValidatorState>> {
        let validators = self.validators.read().await;
        Ok(validators.get(validator_id).cloned())
    }

    pub async fn remove_validator(&self, validator_id: &str) -> Result<()> {
        let mut validators = self.validators.write().await;
        validators.remove(validator_id);
        Ok(())
    }

    pub async fn get_all_validators(&self) -> Result<Vec<ValidatorState>> {
        let validators = self.validators.read().await;
        Ok(validators.values().cloned().collect())
    }

    pub async fn get_eligible_validators(&self) -> Result<Vec<ValidatorState>> {
        let validators = self.validators.read().await;
        let mut eligible = Vec::new();
        
        for validator in validators.values() {
            if self.check_eligibility(validator).await? {
                eligible.push(validator.clone());
            }
        }
        
        Ok(eligible)
    }

    pub async fn update_validator_stake(&self, validator_id: &str, new_stake: u64) -> Result<()> {
        let mut validators = self.validators.write().await;
        if let Some(validator) = validators.get_mut(validator_id) {
            validator.stake = new_stake;
            validator.last_update = SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs();
        }
        Ok(())
    }
}
