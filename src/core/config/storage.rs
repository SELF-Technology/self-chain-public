use serde::{Deserialize, Serialize};

#[derive(Debug, Deserialize, Serialize)]
pub struct StorageConfig {
    pub ipfs_url: String,
    pub orbitdb_url: String,
    pub db_path: String,
    pub cache_size: u64,
    pub sync_interval: u64,
}

impl Default for StorageConfig {
    fn default() -> Self {
        Self {
            ipfs_url: "http://localhost:5001".to_string(),
            orbitdb_url: "http://localhost:3000".to_string(),
            db_path: ".self-chain/db".to_string(),
            cache_size: 1024 * 1024 * 100,  // 100MB
            sync_interval: 60,  // seconds
        }
    }
}
