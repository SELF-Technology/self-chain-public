use std::time::SystemTime;
use serde::{Deserialize, Serialize, de::DeserializeOwned, ser::SerializeStruct};
use bitcoin_hashes::{sha256, Hash};
use secp256k1::{Message, Secp256k1, SecretKey};
use std::str::FromStr;
use anyhow::anyhow;
use secp256k1::ecdsa::Signature;
use bitcoin_hashes::hex::ToHex;
use crate::serialization::{SerializableBlock, SerializableTransaction};

#[derive(Debug, Default, Serialize, Deserialize, Clone)]
pub struct Block {
    pub index: u64,
    pub timestamp: u64,
    pub transactions: Vec<Transaction>,
    pub previous_hash: String,
    pub nonce: u64,
    pub validator_signature: String,
    pub hash: String,
    pub difficulty: u64,
}

impl Block {
    pub fn new(index: u64, previous_hash: String, validator: String, difficulty: u64) -> Self {
        Self {
            index,
            timestamp: SystemTime::now().duration_since(SystemTime::UNIX_EPOCH).unwrap().as_secs(),
            transactions: Vec::new(),
            previous_hash,
            nonce: 0,
            validator_signature: validator,
            hash: String::new(),
            difficulty,
        }
    }

    pub fn add_transaction(&mut self, tx: Transaction) {
        self.transactions.push(tx);
    }

    pub fn to_string(&self) -> Result<String, serde_json::Error> {
        serde_json::to_string(self)
    }

    pub fn from_string(s: &str) -> Result<Self, serde_json::Error> {
        serde_json::from_str(s)
    }
}

#[derive(Debug, Default)]
pub struct Transaction {
    pub id: String,
    pub sender: String,
    pub recipient: String,
    pub amount: u64,
    pub timestamp: u64,
    pub signature: String,
}

impl Clone for Transaction {
    fn clone(&self) -> Self {
        Self {
            id: self.id.clone(),
            sender: self.sender.clone(),
            recipient: self.recipient.clone(),
            amount: self.amount,
            timestamp: self.timestamp,
            signature: self.signature.clone(),
        }
    }
}

impl Serialize for Transaction {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::Serializer,
    {
        let mut state = serializer.serialize_struct("Transaction", 6)?;
        state.serialize_field("id", &self.id)?;
        state.serialize_field("sender", &self.sender)?;
        state.serialize_field("recipient", &self.recipient)?;
        state.serialize_field("amount", &self.amount)?;
        state.serialize_field("timestamp", &self.timestamp)?;
        state.serialize_field("signature", &self.signature)?;
        state.end()
    }
}

impl<'de> Deserialize<'de> for Transaction {
    fn deserialize<D>(deserializer: D) -> Result<Self, D::Error>
    where
        D: serde::Deserializer<'de>,
    {
        #[derive(Deserialize)]
        struct Helper {
            id: String,
            sender: String,
            recipient: String,
            amount: u64,
            timestamp: u64,
            signature: String,
        }

        let helper = Helper::deserialize(deserializer)?;
        Ok(Self {
            id: helper.id,
            sender: helper.sender,
            recipient: helper.recipient,
            amount: helper.amount,
            timestamp: helper.timestamp,
            signature: helper.signature,
        })
    }
}

impl Transaction {
    pub fn new(id: String, sender: String, receiver: String, amount: u64) -> Self {
        Self {
            id,
            sender,
            recipient: receiver,
            amount,
            timestamp: SystemTime::now().duration_since(SystemTime::UNIX_EPOCH).unwrap().as_secs(),
            signature: String::new(),
        }
    }

    pub fn to_string(&self) -> Result<String, serde_json::Error> {
        let serializable = SerializableTransaction {
            id: self.id.clone(),
            sender: self.sender.clone(),
            receiver: self.recipient.clone(),
            amount: self.amount,
            timestamp: self.timestamp,
            signature: self.signature.clone(),
        };
        serde_json::to_string(&serializable)
    }

    pub fn verify(&self, sender_pubkey: &secp256k1::PublicKey) -> Result<bool, anyhow::Error> {
        let secp = Secp256k1::new();
        let data = format!("{}{}{}{}", self.sender, self.recipient, self.amount, self.timestamp);
        let digest = sha256::digest(data.as_bytes());
        let message = Message::from_slice(&digest.to_hex())
            .map_err(|_| anyhow!("Invalid message format"))?;
        let signature = secp256k1::Signature::from_str(&self.signature)?;
        Ok(secp.verify_ecdsa(message, &signature, sender_pubkey).is_ok())
    }

    pub fn hash(&self) -> String {
        use std::collections::hash_map::DefaultHasher;
        use std::hash::{Hash, Hasher};
        
        let mut hasher = DefaultHasher::new();
        self.id.hash(&mut hasher);
        self.sender.hash(&mut hasher);
        self.recipient.hash(&mut hasher);
        self.amount.hash(&mut hasher);
        self.timestamp.hash(&mut hasher);
        self.signature.hash(&mut hasher);
        
        format!("{:x}", hasher.finish())
    }

    pub fn verify(&self) -> bool {
        // Basic verification - check if required fields are not empty
        !self.id.is_empty() && 
        !self.sender.is_empty() && 
        !self.recipient.is_empty() && 
        !self.signature.is_empty() &&
        self.amount > 0
    }
}
