use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Transaction {
    pub id: String,
    pub sender: String,
    pub recipient: String,
    pub amount: u64,
    pub signature: String,
    pub nonce: u64,
    pub timestamp: u64,
}

impl Transaction {
    pub fn new(
        id: String,
        sender: String,
        recipient: String,
        amount: u64,
        signature: String,
        nonce: u64,
        timestamp: u64,
    ) -> Self {
        Self {
            id,
            sender,
            recipient,
            amount,
            signature,
            nonce,
            timestamp,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Block {
    pub id: String,
    pub transactions: Vec<Transaction>,
    pub timestamp: u64,
}

impl Block {
    pub fn new(id: String, transactions: Vec<Transaction>, timestamp: u64) -> Self {
        Self {
            id,
            transactions,
            timestamp,
        }
    }
}
