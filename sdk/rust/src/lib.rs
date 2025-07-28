//! SELF Chain Rust SDK
//!
//! Build AI-native applications on SELF Chain using Rust.
//! 
//! **Coming Q4 2025** - This SDK is currently in development.
//!
//! # Example
//! 
//! ```rust,no_run
//! // Coming Q4 2025
//! use self_sdk::SelfClient;
//! 
//! #[tokio::main]
//! async fn main() -> Result<(), Box<dyn std::error::Error>> {
//!     let client = SelfClient::new("your-api-key")?;
//!     let status = client.get_status().await?;
//!     println!("Blockchain height: {}", status.height);
//!     Ok(())
//! }
//! ```

/// SELF Chain client for Rust applications
/// 
/// **Note**: This is a placeholder implementation. Full SDK coming Q4 2025.
pub struct SelfClient {
    _api_key: String,
}

impl SelfClient {
    /// Create a new SELF client
    /// 
    /// **Coming Q4 2025** - This method is not yet implemented.
    pub fn new(_api_key: impl Into<String>) -> Result<Self, SelfError> {
        Err(SelfError::NotImplemented)
    }
}

/// SELF SDK error types
#[derive(Debug, thiserror::Error)]
pub enum SelfError {
    #[error("SDK not yet implemented - coming Q4 2025")]
    NotImplemented,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_placeholder() {
        // Placeholder test to satisfy CodeQL
        let result = SelfClient::new("test");
        assert!(result.is_err());
    }
}