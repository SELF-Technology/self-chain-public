use std::time::Duration;
use serde::{Serialize, Deserialize};
use anyhow::Result;
use std::path::PathBuf;
use std::fs::File;
use std::io::Read;

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct NetworkConfig {
    pub listen_port: u16,
    pub max_connections: usize,
    pub discovery: DiscoveryConfig,
    pub routing: RoutingConfig,
    pub security: SecurityConfig,
    pub validation: PeerValidationConfig,
    pub connection_pool: ConnectionPoolConfig,
    pub metrics: MetricsConfig,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct DiscoveryConfig {
    pub interval: Duration,
    pub bootstrap_peers: Vec<(String, String)>, // (peer_id, multiaddr)
    pub max_peers: usize,
    pub discovery_timeout: Duration,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct RoutingConfig {
    pub max_hops: u32,
    pub routing_table_size: usize,
    pub message_ttl: Duration,
    pub flood_threshold: usize,
    pub routing_timeout: Duration,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct SecurityConfig {
    pub tls_config: TlsConfig,
    pub auth_config: AuthConfig,
    pub rate_limits: RateLimits,
    pub circuit_breakers: CircuitBreakers,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct TlsConfig {
    pub cert_path: PathBuf,
    pub key_path: PathBuf,
    pub ca_path: PathBuf,
    pub require_client_auth: bool,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct AuthConfig {
    pub token_lifetime: Duration,
    pub refresh_interval: Duration,
    pub max_tokens: usize,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct RateLimits {
    pub max_messages_per_second: usize,
    pub max_connections_per_second: usize,
    pub max_bandwidth: u64, // bytes per second
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct CircuitBreakers {
    pub error_threshold: f64,
    pub reset_timeout: Duration,
    pub max_concurrent_requests: usize,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct PeerValidationConfig {
    pub validation_interval: Duration,
    pub max_validation_errors: usize,
    pub minimum_reputation: f64,
    pub validation_timeout: Duration,
    pub validation_window: Duration,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct ConnectionPoolConfig {
    pub max_connections_per_peer: usize,
    pub connection_timeout: Duration,
    pub cleanup_interval: Duration,
    pub max_pool_size: usize,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct MetricsConfig {
    pub collection_interval: Duration,
    pub retention_period: Duration,
    pub export_interval: Duration,
    pub export_target: String,
}

impl NetworkConfig {
    pub fn load_from_file(path: &str) -> Result<Self> {
        let mut file = File::open(path)?;
        let mut contents = String::new();
        file.read_to_string(&mut contents)?;
        
        let config: Self = serde_yaml::from_str(&contents)?;
        Ok(config)
    }

    pub fn default() -> Self {
        Self {
            listen_port: 30333,
            max_connections: 100,
            discovery: DiscoveryConfig {
                interval: Duration::from_secs(30),
                bootstrap_peers: Vec::new(),
                max_peers: 100,
                discovery_timeout: Duration::from_secs(10),
            },
            routing: RoutingConfig {
                max_hops: 10,
                routing_table_size: 1000,
                message_ttl: Duration::from_secs(300),
                flood_threshold: 100,
                routing_timeout: Duration::from_secs(10),
            },
            security: SecurityConfig {
                tls_config: TlsConfig {
                    cert_path: PathBuf::from("cert.pem"),
                    key_path: PathBuf::from("key.pem"),
                    ca_path: PathBuf::from("ca.pem"),
                    require_client_auth: true,
                },
                auth_config: AuthConfig {
                    token_lifetime: Duration::from_secs(3600),
                    refresh_interval: Duration::from_secs(1800),
                    max_tokens: 1000,
                },
                rate_limits: RateLimits {
                    max_messages_per_second: 1000,
                    max_connections_per_second: 100,
                    max_bandwidth: 10485760, // 10MB/s
                },
                circuit_breakers: CircuitBreakers {
                    error_threshold: 0.1,
                    reset_timeout: Duration::from_secs(60),
                    max_concurrent_requests: 1000,
                },
            },
            validation: PeerValidationConfig {
                validation_interval: Duration::from_secs(300),
                max_validation_errors: 3,
                minimum_reputation: 0.7,
                validation_timeout: Duration::from_secs(5),
                validation_window: Duration::from_secs(3600),
            },
            connection_pool: ConnectionPoolConfig {
                max_connections_per_peer: 5,
                connection_timeout: Duration::from_secs(30),
                cleanup_interval: Duration::from_secs(60),
                max_pool_size: 1000,
            },
            metrics: MetricsConfig {
                collection_interval: Duration::from_secs(1),
                retention_period: Duration::from_secs(3600),
                export_interval: Duration::from_secs(60),
                export_target: "prometheus".to_string(),
            },
        }
    }
}
