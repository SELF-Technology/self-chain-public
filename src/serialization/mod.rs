// Serialization module
// Handles serialization and deserialization of blockchain data

use serde::{Serialize, Deserialize};
use anyhow::Result;

pub struct SerializationService;

impl SerializationService {
    pub fn new() -> Self {
        Self
    }
    
    pub fn serialize<T: Serialize>(&self, data: &T) -> Result<Vec<u8>> {
        Ok(bincode::serialize(data)?)
    }
    
    pub fn deserialize<T: for<'de> Deserialize<'de>>(&self, data: &[u8]) -> Result<T> {
        Ok(bincode::deserialize(data)?)
    }
}

pub fn serialize<T: Serialize>(data: &T) -> Result<Vec<u8>> {
    Ok(bincode::serialize(data)?)
}

pub fn deserialize<T: for<'de> Deserialize<'de>>(data: &[u8]) -> Result<T> {
    Ok(bincode::deserialize(data)?)
}