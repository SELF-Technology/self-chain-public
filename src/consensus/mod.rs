pub mod builder_rotation;
pub mod cache;
pub mod efficiency;
pub mod error;
pub mod metrics;
pub mod peer_validator;
pub mod rewards;
pub mod runtime_adapter;
pub mod validator;
pub mod vote;
pub mod voting;

// Re-export key types
pub use builder_rotation::{BuilderRotation, BuilderSelector, RotationStats};
pub use efficiency::{EfficiencyCalculator, EfficiencyCoefficient};
pub use error::ConsensusError;
pub use metrics::ConsensusMetrics;
pub use peer_validator::PeerValidator;
pub use rewards::{RewardManager, RewardDistribution, BlockRewards};
pub use runtime_adapter::PoAIConsensusAdapter;
pub use validator::AIValidator;
pub use vote::{Vote, VotingResult};
pub use voting::VotingSystem;

use crate::ai::service::AIService;
use crate::blockchain::Block;
use crate::consensus::cache::ValidationCache;
use crate::network::message_handler::MessageHandler;
use anyhow::Result;
use std::sync::Arc;

pub struct PoAI {
    validator: Arc<AIValidator>,
    voting_system: Arc<VotingSystem>,
    peer_validator: Arc<PeerValidator>,
}

impl PoAI {
    pub fn new(_ai_service: Arc<AIService>, message_handler: Arc<MessageHandler>) -> Self {
        // Create metrics registry for consensus
        let registry = prometheus::Registry::new();
        let metrics = Arc::new(ConsensusMetrics::new(&registry).unwrap_or_else(|_| {
            // Fallback to a simple metrics implementation if registry fails
            panic!("Failed to create consensus metrics")
        }));

        // Create validation cache for improved performance
        let cache = Arc::new(ValidationCache::new(metrics.clone()));
        let validator = Arc::new(AIValidator::new(metrics.clone(), cache));
        let voting_system = Arc::new(VotingSystem::new(
            validator.clone(),
            message_handler.clone(),
            metrics.clone(),
        ));
        let peer_validator = Arc::new(PeerValidator::new(message_handler, metrics.clone()));

        Self {
            validator,
            voting_system,
            peer_validator,
        }
    }

    pub async fn validate_block(&self, block: &Block) -> Result<bool> {
        // Validate block with AI validator
        let is_valid = self.validator.validate_block(block).await?;

        // Validate with peer validators
        if is_valid {
            let peer_valid = self.peer_validator.validate_block_with_peers(block).await?;
            if !peer_valid {
                return Ok(false);
            }
        }

        // Start voting round if block is valid
        if is_valid {
            self.voting_system.start_voting_round(block).await?;
        }

        Ok(is_valid)
    }

    pub async fn cast_vote(&self, validator_id: &str, block: &Block) -> Result<()> {
        self.voting_system
            .cast_vote(validator_id, block)
            .await
            .map_err(|e| anyhow::anyhow!("Voting error: {}", e))
    }

    pub async fn get_voting_result(
        &self,
        block_hash: &str,
    ) -> Result<Option<bool>, ConsensusError> {
        self.voting_system
            .get_voting_result(block_hash)
            .await
            .map_err(|e| ConsensusError::VotingError(e.to_string()))
    }
}
