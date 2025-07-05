// AI Validation Results
// Structures for AI validation results

use serde::{Serialize, Deserialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ValidationResult {
    pub is_valid: bool,
    pub confidence: f64,
    pub reason: Option<String>,
}

impl ValidationResult {
    pub fn valid(confidence: f64) -> Self {
        Self {
            is_valid: true,
            confidence,
            reason: None,
        }
    }
    
    pub fn invalid(confidence: f64, reason: String) -> Self {
        Self {
            is_valid: false,
            confidence,
            reason: Some(reason),
        }
    }
}