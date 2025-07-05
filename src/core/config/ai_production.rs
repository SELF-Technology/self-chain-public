//! Production-ready AI configuration for SELF Chain PoAI consensus
//!
//! This module provides environment-aware configuration that automatically
//! switches between development (localhost) and production (cloud) endpoints.

use serde::{Deserialize, Serialize};
use std::env;
use std::time::Duration;

/// Environment types for AI configuration
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub enum Environment {
    /// Development environment (localhost)
    Development,
    /// Staging environment (cloud testing)
    Staging,
    /// Production environment (cloud production)
    Production,
}

impl Environment {
    /// Detect environment from environment variables
    pub fn detect() -> Self {
        match env::var("SELF_CHAIN_ENV").as_deref() {
            Ok("production") | Ok("prod") => Environment::Production,
            Ok("staging") | Ok("stage") => Environment::Staging,
            _ => Environment::Development,
        }
    }

    /// Check if this is a production environment
    pub fn is_production(&self) -> bool {
        matches!(self, Environment::Production)
    }

    /// Check if this is a development environment
    pub fn is_development(&self) -> bool {
        matches!(self, Environment::Development)
    }
}

/// Production-ready AI configuration with failover support
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ProductionAIConfig {
    /// Current environment
    pub environment: Environment,

    /// Primary AI endpoint
    pub primary_endpoint: String,

    /// Backup AI endpoints for failover
    pub backup_endpoints: Vec<String>,

    /// AI model name
    pub model: String,

    /// Maximum tokens per request
    pub max_tokens: u32,

    /// Temperature for AI responses
    pub temperature: f32,

    /// API key for authentication
    pub api_key: Option<String>,

    /// Request timeout in milliseconds
    pub timeout_ms: u64,

    /// Number of retry attempts
    pub retry_attempts: u32,

    /// Health check interval in seconds
    pub health_check_interval_secs: u64,

    /// Circuit breaker configuration
    pub circuit_breaker: CircuitBreakerConfig,

    /// Rate limiting configuration
    pub rate_limit: RateLimitConfig,

    /// TLS configuration
    pub tls: TLSConfig,
}

/// Circuit breaker configuration for AI endpoints
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CircuitBreakerConfig {
    /// Failure threshold before opening circuit
    pub failure_threshold: u32,

    /// Time to wait before attempting to close circuit
    pub recovery_timeout_secs: u64,

    /// Success threshold to close circuit
    pub success_threshold: u32,

    /// Whether circuit breaker is enabled
    pub enabled: bool,
}

/// Rate limiting configuration
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct RateLimitConfig {
    /// Maximum requests per minute
    pub requests_per_minute: u32,

    /// Burst capacity
    pub burst_capacity: u32,

    /// Whether rate limiting is enabled
    pub enabled: bool,
}

/// TLS configuration for secure connections
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TLSConfig {
    /// Whether TLS is enabled
    pub enabled: bool,

    /// Whether to verify SSL certificates
    pub verify_ssl: bool,

    /// Custom CA certificate path
    pub ca_cert_path: Option<String>,

    /// Client certificate path for mutual TLS
    pub client_cert_path: Option<String>,

    /// Client key path for mutual TLS
    pub client_key_path: Option<String>,
}

impl Default for CircuitBreakerConfig {
    fn default() -> Self {
        Self {
            failure_threshold: 5,
            recovery_timeout_secs: 60,
            success_threshold: 3,
            enabled: true,
        }
    }
}

impl Default for RateLimitConfig {
    fn default() -> Self {
        Self {
            requests_per_minute: 1000,
            burst_capacity: 100,
            enabled: true,
        }
    }
}

impl Default for TLSConfig {
    fn default() -> Self {
        Self {
            enabled: true,
            verify_ssl: true,
            ca_cert_path: None,
            client_cert_path: None,
            client_key_path: None,
        }
    }
}

impl ProductionAIConfig {
    /// Create configuration based on environment detection
    pub fn new() -> Self {
        let environment = Environment::detect();
        Self::for_environment(environment)
    }

    /// Create configuration for specific environment
    pub fn for_environment(environment: Environment) -> Self {
        match environment {
            Environment::Development => Self::development_config(),
            Environment::Staging => Self::staging_config(),
            Environment::Production => Self::production_config(),
        }
    }

    /// Development configuration (localhost)
    fn development_config() -> Self {
        Self {
            environment: Environment::Development,
            primary_endpoint: "http://localhost:3000/v1/".to_string(),
            backup_endpoints: vec![
                "http://localhost:3001/v1/".to_string(),
                "http://localhost:3002/v1/".to_string(),
            ],
            model: "mistral:7b-instruct".to_string(),
            max_tokens: 2048,
            temperature: 0.7,
            api_key: None,     // No auth for localhost
            timeout_ms: 10000, // 10 seconds for development
            retry_attempts: 2,
            health_check_interval_secs: 30,
            circuit_breaker: CircuitBreakerConfig {
                enabled: false, // Disabled for development
                ..Default::default()
            },
            rate_limit: RateLimitConfig {
                enabled: false, // Disabled for development
                ..Default::default()
            },
            tls: TLSConfig {
                enabled: false, // No TLS for localhost
                verify_ssl: false,
                ..Default::default()
            },
        }
    }

    /// Staging configuration (cloud testing)
    fn staging_config() -> Self {
        Self {
            environment: Environment::Staging,
            primary_endpoint: env::var("AI_STAGING_ENDPOINT").unwrap_or_else(|_| {
                "https://ai-consensus-staging.selfchain.aws.com/v1/".to_string()
            }),
            backup_endpoints: vec![env::var("AI_STAGING_BACKUP_1").unwrap_or_else(|_| {
                "https://ai-consensus-staging-us-west.selfchain.aws.com/v1/".to_string()
            })],
            model: env::var("AI_STAGING_MODEL")
                .unwrap_or_else(|_| "mistral:7b-instruct".to_string()),
            max_tokens: 2048,
            temperature: 0.7,
            api_key: env::var("AI_STAGING_API_KEY").ok(),
            timeout_ms: 5000, // 5 seconds for staging
            retry_attempts: 3,
            health_check_interval_secs: 60,
            circuit_breaker: CircuitBreakerConfig {
                failure_threshold: 3,
                recovery_timeout_secs: 30,
                success_threshold: 2,
                enabled: true,
            },
            rate_limit: RateLimitConfig {
                requests_per_minute: 500,
                burst_capacity: 50,
                enabled: true,
            },
            tls: TLSConfig {
                enabled: true,
                verify_ssl: true,
                ..Default::default()
            },
        }
    }

    /// Production configuration (cloud production)
    fn production_config() -> Self {
        Self {
            environment: Environment::Production,
            primary_endpoint: env::var("AI_PRODUCTION_ENDPOINT")
                .unwrap_or_else(|_| "https://ai-consensus.selfchain.aws.com/v1/".to_string()),
            backup_endpoints: env::var("AI_BACKUP_ENDPOINTS")
                .map(|endpoints| endpoints.split(',').map(|s| s.trim().to_string()).collect())
                .unwrap_or_else(|_| {
                    vec![
                        "https://ai-consensus-us-west.selfchain.aws.com/v1/".to_string(),
                        "https://ai-consensus-eu-west.selfchain.aws.com/v1/".to_string(),
                        "https://ai-consensus-ap-southeast.selfchain.aws.com/v1/".to_string(),
                    ]
                }),
            model: env::var("AI_PRODUCTION_MODEL")
                .unwrap_or_else(|_| "mistral:7b-instruct".to_string()),
            max_tokens: env::var("AI_MAX_TOKENS")
                .map(|v| v.parse().unwrap_or(2048))
                .unwrap_or(2048),
            temperature: env::var("AI_TEMPERATURE")
                .map(|v| v.parse().unwrap_or(0.7))
                .unwrap_or(0.7),
            api_key: env::var("AI_PRODUCTION_API_KEY")
                .or_else(|_| env::var("AWS_AI_API_KEY"))
                .ok(),
            timeout_ms: env::var("AI_TIMEOUT_MS")
                .map(|v| v.parse().unwrap_or(5000))
                .unwrap_or(5000),
            retry_attempts: env::var("AI_RETRY_ATTEMPTS")
                .map(|v| v.parse().unwrap_or(3))
                .unwrap_or(3),
            health_check_interval_secs: 30,
            circuit_breaker: CircuitBreakerConfig::default(),
            rate_limit: RateLimitConfig::default(),
            tls: TLSConfig {
                enabled: true,
                verify_ssl: true,
                ca_cert_path: env::var("AI_CA_CERT_PATH").ok(),
                client_cert_path: env::var("AI_CLIENT_CERT_PATH").ok(),
                client_key_path: env::var("AI_CLIENT_KEY_PATH").ok(),
            },
        }
    }

    /// Get the current active endpoint (primary or backup)
    pub fn get_active_endpoint(&self) -> &str {
        // In a real implementation, this would check endpoint health
        // and return the first healthy endpoint
        &self.primary_endpoint
    }

    /// Get all available endpoints
    pub fn get_all_endpoints(&self) -> Vec<&str> {
        let mut endpoints = vec![self.primary_endpoint.as_str()];
        endpoints.extend(self.backup_endpoints.iter().map(|s| s.as_str()));
        endpoints
    }

    /// Get request timeout as Duration
    pub fn get_timeout(&self) -> Duration {
        Duration::from_millis(self.timeout_ms)
    }

    /// Check if authentication is required
    pub fn requires_auth(&self) -> bool {
        self.api_key.is_some()
    }

    /// Get authentication header if available
    pub fn get_auth_header(&self) -> Option<(String, String)> {
        self.api_key
            .as_ref()
            .map(|key| ("Authorization".to_string(), format!("Bearer {}", key)))
    }

    /// Validate configuration
    pub fn validate(&self) -> Result<(), String> {
        if self.primary_endpoint.is_empty() {
            return Err("Primary endpoint cannot be empty".to_string());
        }

        if !self.primary_endpoint.starts_with("http://")
            && !self.primary_endpoint.starts_with("https://")
        {
            return Err("Primary endpoint must be a valid HTTP/HTTPS URL".to_string());
        }

        if self.environment.is_production() && self.api_key.is_none() {
            return Err("API key is required for production environment".to_string());
        }

        if self.environment.is_production() && !self.tls.enabled {
            return Err("TLS must be enabled for production environment".to_string());
        }

        if self.max_tokens == 0 {
            return Err("Max tokens must be greater than 0".to_string());
        }

        if self.temperature < 0.0 || self.temperature > 2.0 {
            return Err("Temperature must be between 0.0 and 2.0".to_string());
        }

        if self.timeout_ms == 0 {
            return Err("Timeout must be greater than 0".to_string());
        }

        Ok(())
    }

    /// Create a configuration optimized for consensus workloads
    pub fn for_consensus() -> Self {
        let mut config = Self::new();

        // Optimize for consensus requirements
        config.temperature = 0.1; // Lower temperature for more deterministic responses
        config.max_tokens = 1024; // Shorter responses for faster processing
        config.timeout_ms = 3000; // Shorter timeout for real-time consensus

        config
    }

    /// Create a configuration optimized for pattern analysis
    pub fn for_pattern_analysis() -> Self {
        let mut config = Self::new();

        // Optimize for pattern analysis requirements
        config.temperature = 0.5; // Balanced temperature
        config.max_tokens = 4096; // Longer responses for detailed analysis
        config.timeout_ms = 10000; // Longer timeout for complex analysis

        config
    }
}

impl Default for ProductionAIConfig {
    fn default() -> Self {
        Self::new()
    }
}

/// AI configuration builder for custom setups
pub struct AIConfigBuilder {
    config: ProductionAIConfig,
}

impl AIConfigBuilder {
    /// Create a new builder
    pub fn new() -> Self {
        Self {
            config: ProductionAIConfig::development_config(),
        }
    }

    /// Set the environment
    pub fn environment(mut self, env: Environment) -> Self {
        self.config = ProductionAIConfig::for_environment(env);
        self
    }

    /// Set the primary endpoint
    pub fn primary_endpoint(mut self, endpoint: String) -> Self {
        self.config.primary_endpoint = endpoint;
        self
    }

    /// Add a backup endpoint
    pub fn backup_endpoint(mut self, endpoint: String) -> Self {
        self.config.backup_endpoints.push(endpoint);
        self
    }

    /// Set the model
    pub fn model(mut self, model: String) -> Self {
        self.config.model = model;
        self
    }

    /// Set the API key
    pub fn api_key(mut self, key: String) -> Self {
        self.config.api_key = Some(key);
        self
    }

    /// Set the timeout
    pub fn timeout_ms(mut self, timeout: u64) -> Self {
        self.config.timeout_ms = timeout;
        self
    }

    /// Enable/disable TLS
    pub fn tls_enabled(mut self, enabled: bool) -> Self {
        self.config.tls.enabled = enabled;
        self
    }

    /// Build the configuration
    pub fn build(self) -> Result<ProductionAIConfig, String> {
        self.config.validate()?;
        Ok(self.config)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_environment_detection() {
        // Test default (development)
        let env = Environment::detect();
        assert_eq!(env, Environment::Development);
    }

    #[test]
    fn test_development_config() {
        let config = ProductionAIConfig::development_config();
        assert_eq!(config.environment, Environment::Development);
        assert!(config.primary_endpoint.starts_with("http://localhost"));
        assert!(!config.tls.enabled);
        assert!(!config.circuit_breaker.enabled);
        assert!(config.api_key.is_none());
    }

    #[test]
    fn test_production_config() {
        let config = ProductionAIConfig::production_config();
        assert_eq!(config.environment, Environment::Production);
        assert!(config.primary_endpoint.starts_with("https://"));
        assert!(config.tls.enabled);
        assert!(config.circuit_breaker.enabled);
    }

    #[test]
    fn test_config_validation() {
        let mut config = ProductionAIConfig::development_config();
        assert!(config.validate().is_ok());

        // Test invalid endpoint
        config.primary_endpoint = "".to_string();
        assert!(config.validate().is_err());

        config.primary_endpoint = "invalid-url".to_string();
        assert!(config.validate().is_err());

        // Test invalid temperature
        config.primary_endpoint = "http://localhost:3000".to_string();
        config.temperature = -1.0;
        assert!(config.validate().is_err());

        config.temperature = 3.0;
        assert!(config.validate().is_err());
    }

    #[test]
    fn test_config_builder() {
        let config = AIConfigBuilder::new()
            .environment(Environment::Staging)
            .primary_endpoint("https://test.example.com/v1/".to_string())
            .model("test-model".to_string())
            .api_key("test-key".to_string())
            .timeout_ms(5000)
            .tls_enabled(true)
            .build()
            .unwrap();

        assert_eq!(config.environment, Environment::Staging);
        assert_eq!(config.primary_endpoint, "https://test.example.com/v1/");
        assert_eq!(config.model, "test-model");
        assert_eq!(config.api_key, Some("test-key".to_string()));
        assert_eq!(config.timeout_ms, 5000);
        assert!(config.tls.enabled);
    }

    #[test]
    fn test_consensus_optimization() {
        let config = ProductionAIConfig::for_consensus();
        assert_eq!(config.temperature, 0.1);
        assert_eq!(config.max_tokens, 1024);
        assert_eq!(config.timeout_ms, 3000);
    }

    #[test]
    fn test_pattern_analysis_optimization() {
        let config = ProductionAIConfig::for_pattern_analysis();
        assert_eq!(config.temperature, 0.5);
        assert_eq!(config.max_tokens, 4096);
        assert_eq!(config.timeout_ms, 10000);
    }

    #[test]
    fn test_auth_header_generation() {
        let mut config = ProductionAIConfig::development_config();
        assert!(config.get_auth_header().is_none());

        config.api_key = Some("test-key".to_string());
        let header = config.get_auth_header().unwrap();
        assert_eq!(header.0, "Authorization");
        assert_eq!(header.1, "Bearer test-key");
    }
}
