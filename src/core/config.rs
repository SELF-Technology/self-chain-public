use crate::core::types::*;
use chrono::Utc;
use serde::{Deserialize, Serialize};
use std::env;
use std::path::PathBuf;
use uuid::Uuid;
use rand::Rng;

// Import the production AI configuration
mod ai_production;
pub use ai_production::{AIConfigBuilder, Environment, ProductionAIConfig};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UrlWrapper(pub String);

impl Default for UrlWrapper {
    fn default() -> Self {
        UrlWrapper("http://localhost:3000/".to_string())
    }
}

impl UrlWrapper {
    pub fn as_str(&self) -> &str {
        &self.0
    }
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct GenesisConfig {
    pub timestamp: u64,
    pub difficulty: u64,
    pub reward: u64,
}

impl Default for GenesisConfig {
    fn default() -> Self {
        Self {
            timestamp: Utc::now().timestamp() as u64,
            difficulty: 1000,
            reward: 100,
        }
    }
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct ConsensusConfig {
    pub algorithm: String,
    pub target_time: u64,
    pub difficulty_adjustment: u64,
}

impl Default for ConsensusConfig {
    fn default() -> Self {
        Self {
            algorithm: "pow".to_string(),
            target_time: 60, // 60 seconds
            difficulty_adjustment: 1000,
        }
    }
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct BlockchainConfig {
    pub genesis_block: GenesisConfig,
    pub consensus: ConsensusConfig,
    pub block_size_limit: u64,
    pub transaction_size_limit: u64,
    pub storage_path: PathBuf,
}

impl Default for BlockchainConfig {
    fn default() -> Self {
        Self {
            genesis_block: GenesisConfig::default(),
            consensus: ConsensusConfig::default(),
            block_size_limit: 1024 * 1024,       // 1MB
            transaction_size_limit: 1024 * 1024, // 1MB
            storage_path: PathBuf::from("./blocks"),
        }
    }
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct NodeConfig {
    pub node_id: String,
    pub network: NetworkConfig,
    pub blockchain: BlockchainConfig,
    pub ai: AIConfig,
    pub security: SecurityConfig,
    pub storage: StorageConfig,
}

impl Default for NodeConfig {
    fn default() -> Self {
        Self {
            node_id: Uuid::parse_str("00000000-0000-0000-0000-000000000000")
                .unwrap_or_else(|_| Uuid::nil())
                .to_string(),
            network: NetworkConfig {
                listen_addr: "127.0.0.1:8000".to_string(),
                initial_peers: Vec::new(),
                max_connections: 50,
                connection_timeout_secs: 30,
                message_timeout_secs: 10,
                discovery_interval_secs: 60,
                peer_timeout_secs: 300,
                max_peer_age_secs: 3600,
            },
            blockchain: BlockchainConfig::default(),
            ai: AIConfig::default(),
            security: SecurityConfig::with_env_jwt_secret(),
            storage: StorageConfig::default(),
        }
    }
}

impl NodeConfig {
    pub fn get_api_url(&self) -> &str {
        self.ai.api_endpoint.as_str()
    }

    pub fn get_listen_address(&self) -> String {
        self.network.listen_addr.clone()
    }

    pub fn get_initial_peers(&self) -> &[String] {
        &self.network.initial_peers
    }

    pub fn validate(&self) -> Result<(), String> {
        if self.network.max_connections < 1 {
            return Err("Max connections must be at least 1".to_string());
        }

        if self.blockchain.genesis_block.difficulty < 1 {
            return Err("Difficulty must be at least 1".to_string());
        }

        if self.security.password_salt_rounds < 4 {
            return Err("Salt rounds must be at least 4".to_string());
        }

        if self.security.api_key_length < 32 {
            return Err("API key length must be at least 32".to_string());
        }

        if self.ai.api_endpoint.as_str().is_empty() {
            return Err("API endpoint must be provided".to_string());
        }

        Ok(())
    }
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct AIConfig {
    pub api_endpoint: UrlWrapper,
    pub model: String,
    pub max_tokens: u32,
    pub temperature: f32,

    // Production configuration
    pub production_config: Option<ProductionAIConfig>,
}

impl Default for AIConfig {
    fn default() -> Self {
        // Detect environment and create appropriate configuration
        let production_config = ProductionAIConfig::new();

        Self {
            api_endpoint: UrlWrapper(production_config.get_active_endpoint().to_string()),
            model: production_config.model.clone(),
            max_tokens: production_config.max_tokens,
            temperature: production_config.temperature,
            production_config: Some(production_config),
        }
    }
}

impl AIConfig {
    pub fn get_api_endpoint(&self) -> &str {
        // Use production config if available, otherwise fallback to basic config
        if let Some(prod_config) = &self.production_config {
            prod_config.get_active_endpoint()
        } else {
            &self.api_endpoint.0
        }
    }

    /// Create AI config for specific environment
    pub fn for_environment(env: Environment) -> Self {
        let production_config = ProductionAIConfig::for_environment(env);

        Self {
            api_endpoint: UrlWrapper(production_config.get_active_endpoint().to_string()),
            model: production_config.model.clone(),
            max_tokens: production_config.max_tokens,
            temperature: production_config.temperature,
            production_config: Some(production_config),
        }
    }

    /// Create AI config optimized for consensus
    pub fn for_consensus() -> Self {
        let production_config = ProductionAIConfig::for_consensus();

        Self {
            api_endpoint: UrlWrapper(production_config.get_active_endpoint().to_string()),
            model: production_config.model.clone(),
            max_tokens: production_config.max_tokens,
            temperature: production_config.temperature,
            production_config: Some(production_config),
        }
    }

    /// Get production configuration if available
    pub fn get_production_config(&self) -> Option<&ProductionAIConfig> {
        self.production_config.as_ref()
    }

    /// Check if this is a production environment
    pub fn is_production(&self) -> bool {
        self.production_config
            .as_ref()
            .map(|config| config.environment.is_production())
            .unwrap_or(false)
    }

    /// Get authentication header if available
    pub fn get_auth_header(&self) -> Option<(String, String)> {
        self.production_config
            .as_ref()
            .and_then(|config| config.get_auth_header())
    }

    /// Get timeout duration
    pub fn get_timeout(&self) -> std::time::Duration {
        self.production_config
            .as_ref()
            .map(|config| config.get_timeout())
            .unwrap_or(std::time::Duration::from_secs(10))
    }

    /// Get backup endpoints for failover
    pub fn get_backup_endpoints(&self) -> Vec<&str> {
        self.production_config
            .as_ref()
            .map(|config| config.backup_endpoints.iter().map(|s| s.as_str()).collect())
            .unwrap_or_default()
    }
}

#[derive(Debug, Clone)]
pub struct SecurityConfig {
    pub key_path: PathBuf,
    pub cert_path: PathBuf,
    pub jwt_secret: String,
    pub password_salt_rounds: u32,
    pub api_key_length: u32,
}

// Custom Serialize implementation that doesn't serialize jwt_secret
impl Serialize for SecurityConfig {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::Serializer,
    {
        use serde::ser::SerializeStruct;
        let mut state = serializer.serialize_struct("SecurityConfig", 5)?;
        state.serialize_field("key_path", &self.key_path)?;
        state.serialize_field("cert_path", &self.cert_path)?;
        state.serialize_field("jwt_secret", "<redacted>")?;
        state.serialize_field("password_salt_rounds", &self.password_salt_rounds)?;
        state.serialize_field("api_key_length", &self.api_key_length)?;
        state.end()
    }
}

// Custom Deserialize implementation that generates jwt_secret if not provided
impl<'de> Deserialize<'de> for SecurityConfig {
    fn deserialize<D>(deserializer: D) -> Result<Self, D::Error>
    where
        D: serde::Deserializer<'de>,
    {
        #[derive(Deserialize)]
        struct SecurityConfigHelper {
            key_path: PathBuf,
            cert_path: PathBuf,
            jwt_secret: Option<String>,
            password_salt_rounds: u32,
            api_key_length: u32,
        }
        
        let helper = SecurityConfigHelper::deserialize(deserializer)?;
        
        Ok(SecurityConfig {
            key_path: helper.key_path,
            cert_path: helper.cert_path,
            jwt_secret: helper.jwt_secret.unwrap_or_else(SecurityConfig::generate_jwt_secret),
            password_salt_rounds: helper.password_salt_rounds,
            api_key_length: helper.api_key_length,
        })
    }
}

impl Default for SecurityConfig {
    fn default() -> Self {
        Self {
            key_path: PathBuf::from("./key"),
            cert_path: PathBuf::from("./cert"),
            jwt_secret: Self::generate_jwt_secret(),
            password_salt_rounds: 10,
            api_key_length: 32,
        }
    }
}

impl SecurityConfig {
    /// Generate a cryptographically secure JWT secret
    fn generate_jwt_secret() -> String {
        // Generate 32 random bytes for a strong secret
        let mut rng = rand::thread_rng();
        let bytes: [u8; 32] = rng.gen();
        
        // Encode as base64 for a string representation
        use base64::Engine;
        base64::engine::general_purpose::STANDARD.encode(bytes)
    }
    
    /// Load JWT secret from environment variable or generate a new one
    pub fn with_env_jwt_secret() -> Self {
        let mut config = Self::default();
        
        // Check for JWT_SECRET environment variable
        if let Ok(secret) = env::var("JWT_SECRET") {
            if !secret.is_empty() && secret.len() >= 32 {
                config.jwt_secret = secret;
            } else {
                eprintln!("Warning: JWT_SECRET environment variable is too short (minimum 32 characters). Generating a new secret.");
            }
        } else {
            eprintln!("Warning: JWT_SECRET environment variable not set. Generated a random secret. Set JWT_SECRET for production use.");
        }
        
        config
    }
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct StorageConfig {
    pub ipfs_api: String,
    pub ipfs_gateway: String,
    pub data_dir: PathBuf,
    pub max_size: Option<u64>,
    pub cloud_enabled: bool,
    pub orbitdb_directory: Option<PathBuf>,
    pub ipfs_swarm_key: Option<String>,
    pub recovery_mode: bool,
    pub node_id: Option<String>,
    pub max_storage_gb: Option<u64>,
}

impl Default for StorageConfig {
    fn default() -> Self {
        Self {
            ipfs_api: "http://localhost:5001/api/v0".to_string(),
            ipfs_gateway: "https://ipfs.io/ipfs/".to_string(),
            data_dir: PathBuf::from("./data"),
            max_size: Some(1000000000), // 1GB default max size
            cloud_enabled: true,        // Enable cloud storage by default
            orbitdb_directory: Some(PathBuf::from("./data/orbitdb")),
            ipfs_swarm_key: None, // Will generate a new swarm key if not provided
            recovery_mode: false, // Default to normal mode (not recovery)
            node_id: None,
            max_storage_gb: Some(100), // Default 100GB storage
        }
    }
}
