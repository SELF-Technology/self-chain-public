// AI module
// This module contains AI-related functionality for the SELF chain
// Note: Core AI validation rules remain in the private repository

pub mod openllm;
pub mod context_manager;
pub mod pattern_analysis;
pub mod validator;
pub mod validation;
pub mod service;

// Re-export core types
pub use validator::AIValidator;
pub use context_manager::ContextManager;
pub use pattern_analysis::{PatternAnalyzer, PatternAnalysisRequest, PatternType};

// These are placeholders - actual implementations in private repo
pub struct AICapacityManager;
pub struct ValidatorReputation;
pub struct VotingSystem;

impl AICapacityManager {
    pub fn new() -> Self {
        Self
    }
}

impl ValidatorReputation {
    pub fn new() -> Self {
        Self
    }
}

impl VotingSystem {
    pub fn new() -> Self {
        Self
    }
}

// Re-export from storage module
pub use crate::storage::ai::*;