use serde::{Deserialize, Serialize};
use std::path::PathBuf;
use url::Url;

#[derive(Debug, Serialize, Deserialize)]
pub struct PublicConfig {
    pub server: ServerConfig,
    pub security: SecurityConfig,
    pub api: ApiConfig,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ServerConfig {
    pub address: String,
    pub port: u16,
    pub environment: String,
    pub log_level: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct SecurityConfig {
    pub jwt_secret: String,
    pub password_salt_rounds: u32,
    pub api_key_length: usize,
    pub token_expiration: u64, // in seconds
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ApiConfig {
    pub base_url: Url,
    pub version: String,
    pub rate_limit: RateLimitConfig,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct RateLimitConfig {
    pub limit: u32,
    pub window: u64, // in seconds
    pub burst: u32,
}

impl PublicConfig {
    pub fn from_file(path: &str) -> Self {
        let config_str = std::fs::read_to_string(path)
            .expect("Failed to read config file");
            
        serde_yaml::from_str(&config_str)
            .expect("Failed to parse config file")
    }
}
