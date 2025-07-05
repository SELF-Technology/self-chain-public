use std::sync::Arc;
use tokio::sync::RwLock;
use serde::{Serialize, Deserialize};
use anyhow::Result;
use tracing::{info, warn};
use crate::network::message_handler::{NetworkMessage, MessageHandler};
use crate::blockchain::block::{Block, Transaction};
use crate::security::auth::AuthService;

#[derive(Debug, Serialize, Deserialize, Clone)]
pub enum ValidationResult {
    Valid,
    Invalid(String),
    Pending,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub enum ValidationRequest {
    BlockValidation(Block),
    TransactionValidation(Transaction),
    MessageValidation(NetworkMessage),
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub enum ValidationResponse {
    BlockValidationResult(Block, ValidationResult),
    TransactionValidationResult(Transaction, ValidationResult),
    MessageValidationResult(NetworkMessage, ValidationResult),
}

pub struct PeerValidator {
    message_handler: Arc<MessageHandler>,
    auth_service: Arc<AuthService>,
    validation_peers: Arc<RwLock<Vec<String>>>,
}

impl PeerValidator {
    pub fn new(
        message_handler: Arc<MessageHandler>,
        auth_service: Arc<AuthService>,
    ) -> Self {
        Self {
            message_handler,
            auth_service,
            validation_peers: Arc::new(RwLock::new(Vec::new())),
        }
    }

    pub async fn add_validation_peer(&self, peer_id: String) {
        let mut peers = self.validation_peers.write().await;
        if !peers.contains(&peer_id) {
            peers.push(peer_id);
            info!("Added validation peer: {}", peer_id);
        }
    }

    pub async fn remove_validation_peer(&self, peer_id: &str) {
        let mut peers = self.validation_peers.write().await;
        peers.retain(|p| p != peer_id);
        info!("Removed validation peer: {}", peer_id);
    }

    pub async fn validate_message(&self, message: &NetworkMessage) -> Result<ValidationResult> {
        // First validate locally
        let local_result = self.local_validate(message).await?;
        
        if let ValidationResult::Valid = local_result {
            // If locally valid, send to peers for validation
            let request = ValidationRequest::MessageValidation(message.clone());
            let serialized_request = serde_json::to_string(&request)?;
            
            let peers = self.validation_peers.read().await;
            let mut peer_responses = Vec::new();
            
            // Send validation request to peers
            for peer_id in peers.iter() {
                if let Ok(response) = self.send_validation_request(peer_id, &serialized_request).await {
                    peer_responses.push(response);
                }
            }
            
            // Aggregate peer responses
            let result = self.aggregate_peer_responses(peer_responses).await;
            Ok(result)
        } else {
            Ok(local_result)
        }
    }

    async fn local_validate(&self, message: &NetworkMessage) -> Result<ValidationResult> {
        match message {
            NetworkMessage::NewBlock(block) => {
                // Validate block signature and structure
                if let Ok(is_valid) = self.auth_service.validate_block(block).await {
                    if is_valid {
                        Ok(ValidationResult::Valid)
                    } else {
                        Ok(ValidationResult::Invalid("Invalid block signature".to_string()))
                    }
                } else {
                    Ok(ValidationResult::Pending)
                }
            }
            NetworkMessage::Transaction(tx) => {
                // Validate transaction signature
                if let Ok(is_valid) = self.auth_service.validate_transaction(tx).await {
                    if is_valid {
                        Ok(ValidationResult::Valid)
                    } else {
                        Ok(ValidationResult::Invalid("Invalid transaction signature".to_string()))
                    }
                } else {
                    Ok(ValidationResult::Pending)
                }
            }
            _ => Ok(ValidationResult::Valid), // Other message types are always valid
        }
    }

    async fn send_validation_request(
        &self,
        peer_id: &str,
        request: &str,
    ) -> Result<ValidationResult> {
        // Send request to peer and wait for response
        // This would use the network layer to send the request
        // For now, we'll simulate it
        Ok(ValidationResult::Valid) // TODO: Implement actual peer communication
    }

    async fn aggregate_peer_responses(
        &self,
        responses: Vec<ValidationResult>,
    ) -> ValidationResult {
        let mut valid_count = 0;
        let mut invalid_count = 0;
        let mut invalid_reasons = Vec::new();
        
        for response in responses {
            match response {
                ValidationResult::Valid => valid_count += 1,
                ValidationResult::Invalid(reason) => {
                    invalid_count += 1;
                    invalid_reasons.push(reason);
                }
                ValidationResult::Pending => {} // Ignore pending responses
            }
        }
        
        // Use majority voting
        if valid_count > invalid_count {
            ValidationResult::Valid
        } else if invalid_count > valid_count {
            ValidationResult::Invalid(format!(
                "Majority of peers rejected: {}",
                invalid_reasons.join(", ")
            ))
        } else {
            ValidationResult::Pending // In case of tie
        }
    }
}
