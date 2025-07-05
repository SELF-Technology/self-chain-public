//! Comprehensive security tests for SELF Chain
//! 
//! This test suite validates all security features including:
//! - JWT secret generation
//! - TLS certificate verification
//! - Input validation
//! - Sensitive data handling
//! - AI validation security

use self_chain_core::security::validation::{InputValidator, ValidationError};
use self_chain_core::security::auth::AuthService;
use self_chain_core::blockchain::{Block, Transaction};
use self_chain_core::network::message::NetworkMessage;
use self_chain_core::core::config::SecurityConfig;
use self_chain_core::ai::context_manager::{ContextManager, ContextType};
use self_chain_core::ai::rate_limiter::AIRateLimiter;
use self_chain_core::ai::security_limits;
use std::collections::HashMap;
use chrono::Utc;
use tokio::time::{sleep, Duration};

#[cfg(test)]
mod jwt_security_tests {
    use super::*;

    #[test]
    fn test_no_hardcoded_jwt_secrets() {
        // Test that default config doesn't use hardcoded secrets
        let config1 = SecurityConfig::default();
        let config2 = SecurityConfig::default();
        
        // Each instance should have a different secret
        assert_ne!(config1.jwt_secret, config2.jwt_secret);
        
        // Should not be the old hardcoded value
        assert_ne!(config1.jwt_secret, "default_jwt_secret");
        
        // Should be sufficiently long
        assert!(config1.jwt_secret.len() >= 32);
    }
    
    #[test]
    fn test_jwt_secret_from_environment() {
        std::env::set_var("JWT_SECRET", "test_secret_that_is_long_enough_for_security");
        let config = SecurityConfig::with_env_jwt_secret();
        assert_eq!(config.jwt_secret, "test_secret_that_is_long_enough_for_security");
        std::env::remove_var("JWT_SECRET");
    }
    
    #[tokio::test]
    async fn test_auth_service_with_secure_jwt() {
        let config = SecurityConfig::default();
        let auth_service = AuthService::new(config);
        
        // Create a user
        let user = auth_service.create_user("testuser", "password123", "user").await.unwrap();
        assert!(!user.jwt_secret.is_empty());
        
        // Generate and validate token
        let token = auth_service.generate_token("testuser").await.unwrap();
        let claims = auth_service.validate_token(&token).unwrap();
        assert_eq!(claims.sub, "testuser");
    }
}

#[cfg(test)]
mod input_validation_tests {
    use super::*;

    #[test]
    fn test_block_validation() {
        let mut validator = InputValidator::new();
        
        // Create a valid block
        let mut block = Block {
            hash: "a".repeat(64),
            header: Default::default(),
            transactions: vec![],
            meta: Default::default(),
        };
        block.header.previous_hash = "b".repeat(64);
        block.header.timestamp = Utc::now().timestamp() as u64;
        
        // Should pass validation
        assert!(validator.validate_block(&block).is_ok());
        
        // Test invalid hash
        block.hash = "invalid".to_string();
        assert!(validator.validate_block(&block).is_err());
        
        // Reset for next test
        block.hash = "a".repeat(64);
        
        // Test too many transactions
        for i in 0..1001 {
            block.transactions.push(Transaction {
                id: format!("tx_{}", i),
                sender: "f".repeat(40),
                receiver: "e".repeat(40),
                amount: 100,
                signature: "d".repeat(128),
                timestamp: Utc::now().timestamp() as u64,
            });
        }
        assert!(validator.validate_block(&block).is_err());
    }
    
    #[test]
    fn test_transaction_validation() {
        let mut validator = InputValidator::new();
        
        // Valid transaction
        let tx = Transaction {
            id: "test_tx".to_string(),
            sender: "a".repeat(40),
            receiver: "b".repeat(40),
            amount: 100,
            signature: "c".repeat(128),
            timestamp: Utc::now().timestamp() as u64,
        };
        
        assert!(validator.validate_transaction(&tx).is_ok());
        
        // Invalid sender address
        let mut invalid_tx = tx.clone();
        invalid_tx.sender = "not_hex!".to_string();
        assert!(validator.validate_transaction(&invalid_tx).is_err());
        
        // Negative amount
        let mut negative_tx = tx.clone();
        negative_tx.amount = -100;
        assert!(validator.validate_transaction(&negative_tx).is_err());
        
        // Future timestamp
        let mut future_tx = tx.clone();
        future_tx.timestamp = (Utc::now().timestamp() + 600) as i64;
        assert!(validator.validate_transaction(&future_tx).is_err());
    }
    
    #[test]
    fn test_network_message_validation() {
        let mut validator = InputValidator::new();
        
        // Valid ping message
        assert!(validator.validate_network_message(&NetworkMessage::Ping).is_ok());
        
        // Test message size limits
        let large_block_str = "x".repeat(2_000_000); // 2MB
        let result = validator.validate_network_message(&NetworkMessage::NewBlock(large_block_str));
        assert!(result.is_err());
        
        // Test vector size limits
        let mut large_vector = vec![];
        for i in 0..10001 {
            large_vector.push(format!("block_{}", i));
        }
        let result = validator.validate_network_message(&NetworkMessage::Blocks(large_vector));
        assert!(result.is_err());
    }
    
    #[test]
    fn test_string_sanitization() {
        use self_chain_core::security::validation::sanitize_string;
        
        // Normal string
        let result = sanitize_string("hello world", 100).unwrap();
        assert_eq!(result, "hello world");
        
        // String with null bytes
        let result = sanitize_string("hello\0world", 100).unwrap();
        assert_eq!(result, "helloworld");
        
        // String too long
        let result = sanitize_string(&"a".repeat(101), 100);
        assert!(result.is_err());
    }
}

#[cfg(test)]
mod sensitive_data_tests {
    use super::*;

    #[tokio::test]
    async fn test_context_manager_redaction() {
        let manager = ContextManager::new(10);
        
        // Add context with sensitive data
        let mut data = HashMap::new();
        data.insert("username".to_string(), "public_user".to_string());
        data.insert("api_key".to_string(), "super_secret_key".to_string());
        data.insert("private_key".to_string(), "very_private_key".to_string());
        
        let context_id = manager.add_context(ContextType::Security, data).await.unwrap();
        let context = manager.get_context(&context_id).await.unwrap().unwrap();
        
        // Serialize and check redaction
        let serialized = serde_json::to_string(&context).unwrap();
        assert!(serialized.contains(r#""api_key":"<redacted>""#));
        assert!(serialized.contains(r#""private_key":"<redacted>""#));
        assert!(serialized.contains(r#""username":"public_user""#));
    }
    
    #[tokio::test]
    async fn test_context_manager_encryption() {
        let encryption_key = vec![0u8; 32];
        let manager = ContextManager::with_encryption(10, encryption_key);
        
        // Add sensitive data
        let mut data = HashMap::new();
        data.insert("secret_token".to_string(), "my_token_value".to_string());
        data.insert("normal_field".to_string(), "normal_value".to_string());
        
        let context_id = manager.add_context(ContextType::Security, data).await.unwrap();
        
        // Get encrypted context
        let context = manager.get_context(&context_id).await.unwrap().unwrap();
        if let Some(serde_json::Value::String(encrypted)) = context.data.get("secret_token") {
            assert!(encrypted.starts_with("encrypted:"));
            assert_ne!(encrypted, "my_token_value");
        }
        
        // Get decrypted context
        let decrypted = manager.get_context_decrypted(&context_id).await.unwrap().unwrap();
        if let Some(serde_json::Value::String(value)) = decrypted.data.get("secret_token") {
            assert_eq!(value, "my_token_value");
        }
    }
    
    #[tokio::test]
    async fn test_context_manager_cleanup() {
        let mut manager = ContextManager::new(10);
        manager.initialize().await.unwrap();
        
        // Add sensitive data
        let mut data = HashMap::new();
        data.insert("password".to_string(), "secret123".to_string());
        data.insert("token".to_string(), "auth_token".to_string());
        data.insert("safe_data".to_string(), "keep_this".to_string());
        
        let context_id = manager.add_context(ContextType::Security, data).await.unwrap();
        
        // Shutdown should clear sensitive data
        manager.shutdown().await.unwrap();
        
        let context = manager.get_context(&context_id).await.unwrap().unwrap();
        assert!(!context.data.contains_key("password"));
        assert!(!context.data.contains_key("token"));
        assert!(context.data.contains_key("safe_data"));
    }
}

#[cfg(test)]
mod ai_security_tests {
    use super::*;

    #[tokio::test]
    async fn test_rate_limiter() {
        let limiter = AIRateLimiter::new();
        let identifier = "test_user";
        
        // Should allow initial requests
        for _ in 0..10 {
            assert!(limiter.check_rate_limit(identifier).await.is_ok());
        }
        
        // Eventually should hit rate limit
        let mut hit_limit = false;
        for _ in 0..200 {
            if limiter.check_rate_limit(identifier).await.is_err() {
                hit_limit = true;
                break;
            }
        }
        assert!(hit_limit, "Should hit rate limit after many requests");
    }
    
    #[tokio::test]
    async fn test_concurrent_request_limits() {
        let limiter = AIRateLimiter::new();
        let mut slots = vec![];
        
        // Acquire slots up to limit
        for _ in 0..security_limits::MAX_CONCURRENT_AI_REQUESTS {
            let slot = limiter.acquire_concurrent_slot().await.unwrap();
            slots.push(slot);
        }
        
        // Next should fail
        assert!(limiter.acquire_concurrent_slot().await.is_err());
        
        // Drop a slot
        slots.pop();
        sleep(Duration::from_millis(10)).await;
        
        // Should succeed now
        assert!(limiter.acquire_concurrent_slot().await.is_ok());
    }
    
    #[test]
    fn test_ai_processing_limits() {
        // Test block size limits
        assert!(security_limits::is_block_within_ai_limits(100, 1_000_000));
        assert!(!security_limits::is_block_within_ai_limits(10_000, 1_000_000));
        assert!(!security_limits::is_block_within_ai_limits(100, 10_000_000));
        
        // Test pattern request validation
        assert!(security_limits::is_pattern_request_valid(100, 50_000).is_ok());
        assert!(security_limits::is_pattern_request_valid(2000, 50_000).is_err());
        assert!(security_limits::is_pattern_request_valid(100, 200_000).is_err());
    }
}

#[cfg(test)]
mod tls_security_tests {
    use super::*;
    use self_chain_core::network::tls::TLSConfig;
    use tempfile::NamedTempFile;
    use std::io::Write;

    fn create_test_files() -> (NamedTempFile, NamedTempFile, NamedTempFile) {
        // Test certificate (self-signed for testing)
        let cert_pem = br#"-----BEGIN CERTIFICATE-----
MIIBkTCB+wIJAKHHIgKwL4bMA0GCSqGSIb3DQEBCwUAMBAxDjAMBgNVBAMMBVNF
TEYxMB4XDTI0MDEwMTAwMDAwMFoXDTI1MDEwMTAwMDAwMFowEDEOMAwGA1UEAwwF
U0VMRjEwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBANtb6Z7+M7Y4lHQc6tqe
mQc6dgDG9G1b/95xKzGTMHCFCQvxGJ5h+kLwvFsFiGoGaSMc3kHH2fPaGxYruTQV
x2RyZY0D6wKZEQ5GPEJRKSnpMH8U7VAU3UbZRvhvk7w6h5TKEdhXvDH8NKkfImUM
WUb38nUwOm1gF5K6jJR7hORTAgMBAAEwDQYJKoZIhvcNAQELBQADgYEAZXjgQXtQ
EdVBvF5MgCoF8n1HNq7bDqKb5q3xVk8j3rG9Mz7K9QV7A7M8OZ7bJbwDMpi+2hcY
mpWBkN7XF8KpqgfUKDhB8OqPO8BhJEJ8z0qx+Ijq3dv3cXF5fEI7pvTVF2mfDJuV
nQ3+V8Y3tJn3fQZ7kQKJwCMQzVAkMqQMFVY=
-----END CERTIFICATE-----"#;

        let key_pem = br#"-----BEGIN PRIVATE KEY-----
MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANtb6Z7+M7Y4lHQc
6tqemQc6dgDG9G1b/95xKzGTMHCFCQvxGJ5h+kLwvFsFiGoGaSMc3kHH2fPaGxYr
uTQVx2RyZY0D6wKZEQ5GPEJRKSnpMH8U7VAU3UbZRvhvk7w6h5TKEdhXvDH8NKkf
ImUMWUb38nUwOm1gF5K6jJR7hORTAgMBAAECgYEAyJI0s/UJq2BhAQBgcGTdGKXh
CWuPAL3p2V3r7SgxqzBR6Q5Pxn7fmJb3xM5VYG3F4YGz6f0jVHJQZ6y8Q5Wlqvtt
T8F2eMA8h2H4TOaJJZBQ1gbGFRUJN6V5gQpUxyKFHGpDZjMYdCh8nK0EJaKeZVGE
HwEGg8pHkJPnmhJ4AAECQQDvQq2hfXtqHRSZXDqVQ0nY8vKRLZRtD5F9WYLsQlpB
zz3F0IqSlIyFuRq7Hv5lkGvFTKmFQwPB2YhVMOAl0cIxAkEA6qGHx3uLpqmpb2nA
KwBj1Z2KYivgB06kjFk3fGSJ1C+FmQ5gcD5qJhGUqYJfBTMthFJpYwJsLQxFtqvS
T5NOAwJBALUpWnckq0hOXNpQoqP8pUDKfH7MqNQ9uL3C7O0jQ6HfXQA2i0lQD4hf
87SmVlQfTMQFovvQqH5+gyqB4VeNpYECQGFMDRBPxQfiN8H7F4ynrk2jziC8s2Lh
zA+TFCMGSlo8K4PhoTFl3Iev//xF8n5VfCH5fBL/8bHq3QpbcR0j8GECQH8VH8fI
42gEU6EaGT1OFY1QiVYaFvCPg8mNGLWV9oq2RVNJGmH1wqNLpEGcGJabO6kP8BQP
R32TCzHVlIhqKvU=
-----END PRIVATE KEY-----"#;

        let mut cert_file = NamedTempFile::new().unwrap();
        let mut key_file = NamedTempFile::new().unwrap();
        let mut ca_file = NamedTempFile::new().unwrap();

        cert_file.write_all(cert_pem).unwrap();
        key_file.write_all(key_pem).unwrap();
        ca_file.write_all(cert_pem).unwrap(); // Self-signed

        (cert_file, key_file, ca_file)
    }

    #[test]
    fn test_tls_config_creation() {
        let (cert_file, key_file, ca_file) = create_test_files();
        
        let tls_config = TLSConfig::new(
            cert_file.path().to_str().unwrap(),
            key_file.path().to_str().unwrap(),
            ca_file.path().to_str().unwrap(),
        );
        
        assert!(tls_config.is_ok());
        let config = tls_config.unwrap();
        assert!(config.verify_peer);
        assert_eq!(config.ca_certs.len(), 1);
    }
    
    #[tokio::test]
    async fn test_peer_certificate_verification() {
        let (cert_file, key_file, ca_file) = create_test_files();
        
        let tls_config = TLSConfig::new(
            cert_file.path().to_str().unwrap(),
            key_file.path().to_str().unwrap(),
            ca_file.path().to_str().unwrap(),
        ).unwrap();
        
        // Test with a certificate from our CA
        let cert = &tls_config.ca_certs[0];
        let result = tls_config.verify_peer(cert, "test.example.com").await;
        
        // Should succeed since it's a trusted CA cert
        assert!(result.is_ok());
    }
}

#[cfg(test)]
mod integration_tests {
    use super::*;

    #[tokio::test]
    async fn test_secure_message_handling() {
        use self_chain_core::network::message_handler::MessageHandler;
        
        let mut handler = MessageHandler::new();
        
        // Create a valid transaction message
        let tx = Transaction {
            id: "test_tx".to_string(),
            sender: "a".repeat(40),
            receiver: "b".repeat(40),
            amount: 100,
            signature: "c".repeat(128),
            timestamp: Utc::now().timestamp() as u64,
        };
        
        let message = NetworkMessage::Transaction(serde_json::to_string(&tx).unwrap());
        
        // Should handle valid message
        assert!(handler.handle_message(
            message,
            "127.0.0.1:8080".parse().unwrap()
        ).is_ok());
        
        // Create invalid transaction (bad address)
        let invalid_tx = Transaction {
            id: "bad_tx".to_string(),
            sender: "not_a_valid_address".to_string(),
            receiver: "b".repeat(40),
            amount: 100,
            signature: "c".repeat(128),
            timestamp: Utc::now().timestamp() as u64,
        };
        
        let invalid_message = NetworkMessage::Transaction(
            serde_json::to_string(&invalid_tx).unwrap()
        );
        
        // Message handler should accept it (doesn't validate transaction contents)
        // but blockchain validation will reject it
        assert!(handler.handle_message(
            invalid_message,
            "127.0.0.1:8080".parse().unwrap()
        ).is_ok());
    }
    
    #[test]
    fn test_secure_block_verification() {
        let mut block = Block {
            hash: "a".repeat(64),
            header: Default::default(),
            transactions: vec![],
            meta: Default::default(),
        };
        block.header.previous_hash = "b".repeat(64);
        block.header.timestamp = Utc::now().timestamp() as u64;
        
        // Add a valid transaction
        block.transactions.push(Transaction {
            id: "tx1".to_string(),
            sender: "f".repeat(40),
            receiver: "e".repeat(40),
            amount: 100,
            signature: "d".repeat(128),
            timestamp: Utc::now().timestamp() as u64,
        });
        
        // Should pass verification
        assert!(block.verify());
        
        // Add invalid transaction
        block.transactions.push(Transaction {
            id: "tx2".to_string(),
            sender: "invalid!".to_string(),
            receiver: "e".repeat(40),
            amount: 100,
            signature: "d".repeat(128),
            timestamp: Utc::now().timestamp() as u64,
        });
        
        // Should fail verification
        assert!(!block.verify());
    }
}