use std::time::Duration;
use serde::{Serialize, Deserialize};
use anyhow::Result;
use std::path::PathBuf;
use std::fs::File;
use std::io::Read;

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct PeerValidationConfig {
    pub general: GeneralConfig,
    pub certificate: CertificateConfig,
    pub reputation: ReputationConfig,
    pub response_time: ResponseTimeConfig,
    pub cache: CacheConfig,
    pub error_handling: ErrorHandlingConfig,
    pub recovery: RecoveryConfig,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct GeneralConfig {
    pub validation_interval: Duration,
    pub max_validation_errors: usize,
    pub minimum_reputation: f64,
    pub validation_timeout: Duration,
    pub validation_window: Duration,
    pub parallel_validations: usize,
    pub validation_batch_size: usize,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct CertificateConfig {
    pub cert_path: PathBuf,
    pub key_path: PathBuf,
    pub ca_path: PathBuf,
    pub require_client_auth: bool,
    pub cert_validity_window: Duration,
    pub cert_refresh_interval: Duration,
    pub cert_cache_size: usize,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct ReputationConfig {
    pub score_threshold: f64,
    pub decay_factor: f64,
    pub update_interval: Duration,
    pub history_window: Duration,
    pub minimum_samples: usize,
    pub weight_factors: ReputationFactors,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct ReputationFactors {
    pub response_time: f64,
    pub message_delivery: f64,
    pub validation_success: f64,
    pub uptime: f64,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct ResponseTimeConfig {
    pub max_response_time: Duration,
    pub warning_threshold: Duration,
    pub error_threshold: Duration,
    pub measurement_window: Duration,
    pub minimum_samples: usize,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct CacheConfig {
    pub max_size: usize,
    pub validation_ttl: Duration,
    pub cleanup_interval: Duration,
    pub cache_types: Vec<CacheType>,
    pub cache_warmup: bool,
}

#[derive(Debug, Serialize, Deserialize, Clone, Copy, PartialEq, Eq)]
pub enum CacheType {
    Certificate,
    Reputation,
    ResponseTime,
    Full,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct ErrorHandlingConfig {
    pub retry_count: usize,
    pub retry_delay: Duration,
    pub circuit_breaker_threshold: f64,
    pub circuit_breaker_window: Duration,
    pub error_types: Vec<ErrorType>,
}

#[derive(Debug, Serialize, Deserialize, Clone, Copy, PartialEq, Eq)]
pub enum ErrorType {
    Certificate,
    Reputation,
    ResponseTime,
    Network,
    Timeout,
    Other,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct RecoveryConfig {
    pub recovery_interval: Duration,
    pub recovery_attempts: usize,
    pub recovery_strategies: Vec<RecoveryStrategy>,
    pub recovery_timeout: Duration,
}

#[derive(Debug, Serialize, Deserialize, Clone, Copy, PartialEq, Eq)]
pub enum RecoveryStrategy {
    Retry,
    AlternativePath,
    CircuitBreaker,
    Quarantine,
    Blacklist,
}

impl PeerValidationConfig {
    pub fn load_from_file(path: &str) -> Result<Self> {
        let mut file = File::open(path)?;
        let mut contents = String::new();
        file.read_to_string(&mut contents)?;
        
        let config: Self = serde_yaml::from_str(&contents)?;
        Ok(config)
    }

    pub fn default() -> Self {
        Self {
            general: GeneralConfig {
                validation_interval: Duration::from_secs(300),
                max_validation_errors: 3,
                minimum_reputation: 0.7,
                validation_timeout: Duration::from_secs(5),
                validation_window: Duration::from_secs(3600),
                parallel_validations: 10,
                validation_batch_size: 100,
            },
            certificate: CertificateConfig {
                cert_path: PathBuf::from("cert.pem"),
                key_path: PathBuf::from("key.pem"),
                ca_path: PathBuf::from("ca.pem"),
                require_client_auth: true,
                cert_validity_window: Duration::from_secs(86400),
                cert_refresh_interval: Duration::from_secs(3600),
                cert_cache_size: 1000,
            },
            reputation: ReputationConfig {
                score_threshold: 0.7,
                decay_factor: 0.95,
                update_interval: Duration::from_secs(60),
                history_window: Duration::from_secs(86400),
                minimum_samples: 10,
                weight_factors: ReputationFactors {
                    response_time: 0.3,
                    message_delivery: 0.3,
                    validation_success: 0.2,
                    uptime: 0.2,
                },
            },
            response_time: ResponseTimeConfig {
                max_response_time: Duration::from_secs(1),
                warning_threshold: Duration::from_secs(0.5),
                error_threshold: Duration::from_secs(2),
                measurement_window: Duration::from_secs(300),
                minimum_samples: 5,
            },
            cache: CacheConfig {
                max_size: 1000,
                validation_ttl: Duration::from_secs(3600),
                cleanup_interval: Duration::from_secs(60),
                cache_types: vec![
                    CacheType::Certificate,
                    CacheType::Reputation,
                    CacheType::ResponseTime,
                    CacheType::Full,
                ],
                cache_warmup: true,
            },
            error_handling: ErrorHandlingConfig {
                retry_count: 3,
                retry_delay: Duration::from_secs(1),
                circuit_breaker_threshold: 0.1,
                circuit_breaker_window: Duration::from_secs(60),
                error_types: vec![
                    ErrorType::Certificate,
                    ErrorType::Reputation,
                    ErrorType::ResponseTime,
                    ErrorType::Network,
                    ErrorType::Timeout,
                ],
            },
            recovery: RecoveryConfig {
                recovery_interval: Duration::from_secs(60),
                recovery_attempts: 3,
                recovery_strategies: vec![
                    RecoveryStrategy::Retry,
                    RecoveryStrategy::AlternativePath,
                    RecoveryStrategy::CircuitBreaker,
                ],
                recovery_timeout: Duration::from_secs(300),
            },
        }
    }
}
