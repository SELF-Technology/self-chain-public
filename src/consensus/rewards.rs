use anyhow::Result;
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use crate::blockchain::Address;

/// Reward distribution according to PoAI specification
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct RewardDistribution {
    /// 90% to block builder
    pub block_builder: u64,
    /// 8% to AI validators who voted correctly
    pub ai_validators: u64,
    /// 1% to color scheme checker
    pub color_checker: u64,
    /// 1% to PoAI reserve fund
    pub reserve_fund: u64,
}

/// Tracks rewards for a specific block
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct BlockRewards {
    pub block_height: u64,
    pub total_reward: u64,
    pub builder_address: Address,
    pub validator_rewards: HashMap<Address, u64>,
    pub color_checker_address: Address,
    pub distribution: RewardDistribution,
}

/// Manages reward distribution for the PoAI consensus
pub struct RewardManager {
    /// Reserve fund address
    reserve_fund_address: Address,
    /// Base block reward amount
    base_block_reward: u64,
}

impl RewardManager {
    pub fn new(reserve_fund_address: Address, base_block_reward: u64) -> Self {
        Self {
            reserve_fund_address,
            base_block_reward,
        }
    }

    /// Calculate reward distribution for a block
    pub fn calculate_rewards(
        &self,
        block_height: u64,
        builder_address: Address,
        validators: Vec<Address>,
        color_checker: Address,
    ) -> Result<BlockRewards> {
        let total_reward = self.base_block_reward;
        
        // Calculate individual portions according to PoAI specification
        let builder_reward = (total_reward * 90) / 100;
        let validators_total = (total_reward * 8) / 100;
        let color_checker_reward = (total_reward * 1) / 100;
        let reserve_reward = (total_reward * 1) / 100;
        
        // Ensure no rounding loss
        let accounted = builder_reward + validators_total + color_checker_reward + reserve_reward;
        let remainder = total_reward - accounted;
        let final_builder_reward = builder_reward + remainder;
        
        // Distribute validator rewards equally among all participating validators
        let mut validator_rewards = HashMap::new();
        if !validators.is_empty() {
            let per_validator = validators_total / validators.len() as u64;
            let validator_remainder = validators_total % validators.len() as u64;
            
            for (i, validator) in validators.iter().enumerate() {
                let reward = if i == 0 {
                    per_validator + validator_remainder
                } else {
                    per_validator
                };
                validator_rewards.insert(validator.clone(), reward);
            }
        }
        
        Ok(BlockRewards {
            block_height,
            total_reward,
            builder_address: builder_address.clone(),
            validator_rewards,
            color_checker_address: color_checker.clone(),
            distribution: RewardDistribution {
                block_builder: final_builder_reward,
                ai_validators: validators_total,
                color_checker: color_checker_reward,
                reserve_fund: reserve_reward,
            },
        })
    }
    
    /// Apply rewards to the blockchain state
    pub async fn apply_rewards(&self, rewards: &BlockRewards) -> Result<()> {
        // In a real implementation, this would update account balances
        // For now, we'll log the distribution
        
        log::info!(
            "Block {} rewards distributed: Builder: {} SELF, Validators: {} SELF, Color Checker: {} SELF, Reserve: {} SELF",
            rewards.block_height,
            rewards.distribution.block_builder,
            rewards.distribution.ai_validators,
            rewards.distribution.color_checker,
            rewards.distribution.reserve_fund
        );
        
        Ok(())
    }
    
    /// Get the reserve fund address
    pub fn reserve_fund_address(&self) -> &Address {
        &self.reserve_fund_address
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    
    #[test]
    fn test_reward_distribution() {
        let reserve_addr = Address::from([0u8; 32]);
        let manager = RewardManager::new(reserve_addr, 1000);
        
        let builder = Address::from([1u8; 32]);
        let validators = vec![
            Address::from([2u8; 32]),
            Address::from([3u8; 32]),
            Address::from([4u8; 32]),
        ];
        let color_checker = Address::from([5u8; 32]);
        
        let rewards = manager.calculate_rewards(100, builder, validators.clone(), color_checker)
            .expect("Failed to calculate rewards");
        
        // Verify distribution percentages
        assert_eq!(rewards.distribution.block_builder, 900); // 90%
        assert_eq!(rewards.distribution.ai_validators, 80);  // 8%
        assert_eq!(rewards.distribution.color_checker, 10);  // 1%
        assert_eq!(rewards.distribution.reserve_fund, 10);   // 1%
        
        // Verify total equals base reward
        let total = rewards.distribution.block_builder 
            + rewards.distribution.ai_validators
            + rewards.distribution.color_checker
            + rewards.distribution.reserve_fund;
        assert_eq!(total, 1000);
        
        // Verify validator distribution
        let validator_total: u64 = rewards.validator_rewards.values().sum();
        assert_eq!(validator_total, 80);
        
        // Each validator should get approximately equal share
        for validator in &validators {
            assert!(rewards.validator_rewards.contains_key(validator));
            let reward = rewards.validator_rewards[validator];
            assert!(reward >= 26 && reward <= 28); // 80/3 = 26.67
        }
    }
    
    #[test]
    fn test_single_validator_reward() {
        let reserve_addr = Address::from([0u8; 32]);
        let manager = RewardManager::new(reserve_addr, 1000);
        
        let builder = Address::from([1u8; 32]);
        let validators = vec![Address::from([2u8; 32])];
        let color_checker = Address::from([3u8; 32]);
        
        let rewards = manager.calculate_rewards(100, builder, validators, color_checker)
            .expect("Failed to calculate rewards");
        
        // Single validator gets all 8%
        assert_eq!(rewards.validator_rewards.len(), 1);
        let validator_reward: u64 = rewards.validator_rewards.values().sum();
        assert_eq!(validator_reward, 80);
    }
}