use crate::blockchain::block::Transaction;
use crate::monitoring::performance::SystemInfo;
use rand::Rng;
use std::sync::Arc;

pub struct TransactionPatternGenerator {
    system_info: Arc<SystemInfo>,
}

impl TransactionPatternGenerator {
    pub fn new(system_info: Arc<SystemInfo>) -> Self {
        Self { system_info }
    }

    pub fn generate_transaction(&self, profile: &TransactionProfile) -> Transaction {
        let mut rng = rand::thread_rng();
        
        Transaction::new(
            format!("tx_{}", rng.gen::<u64>()),
            profile.size,
            chrono::Utc::now().timestamp() as u64,
            format!("sender_{}", rng.gen::<u64>()),
            format!("recipient_{}", rng.gen::<u64>()),
            rng.gen::<u64>() % 1000000,
            format!("sig_{}", rng.gen::<u64>()),
            rng.gen::<u64>(),
        )
    }

    pub fn generate_transactions(&self, profile: &TransactionProfile, count: u64) -> Vec<Transaction> {
        (0..count).map(|_| self.generate_transaction(profile)).collect()
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TransactionProfile {
    pub size: usize,
    pub complexity: u64,
    pub frequency: u64,
    pub value_range: (u64, u64),
    pub timestamp: u64,
}
