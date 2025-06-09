use std::sync::Arc;
use std::sync::Mutex;
use anyhow::Result;
use reqwest::Client;
use serde::{Deserialize, Serialize};
use crate::core::config::AIConfig;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AIResponse {
    pub content: String,
    pub usage: UsageStats,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UsageStats {
    pub prompt_tokens: u32,
    pub completion_tokens: u32,
}

/// AIValidatorClient provides a client interface to call AI validation services.
/// It automatically tracks cumulative token usage statistics from responses.
pub struct AIValidatorClient {
    client: Client,
    config: AIConfig,
    usage_stats: Mutex<UsageStats>,
}

impl AIValidatorClient {
    pub fn new(config: AIConfig) -> Self {
        Self {
            client: Client::new(),
            config,
            usage_stats: Mutex::new(UsageStats {
                prompt_tokens: 0,
                completion_tokens: 0,
            }),
        }
    }

    pub async fn call_ai(&self, prompt: &str) -> Result<AIResponse> {
        let request = serde_json::json!({
            "model": self.config.model,
            "messages": [{
                "role": "user",
                "content": prompt
            }],
            "temperature": self.config.temperature,
            "max_tokens": self.config.max_tokens,
        });

        let response = self.client
            .post(&self.config.api_endpoint.0)
            .json(&request)
            .send()
            .await?;

        let response: AIResponse = response.json().await?;
        
        // Update usage statistics
        if let Ok(mut stats) = self.usage_stats.lock() {
            stats.prompt_tokens += response.usage.prompt_tokens;
            stats.completion_tokens += response.usage.completion_tokens;
        }
        
        Ok(response)
    }

    /// Returns a copy of the current cumulative usage statistics
    pub fn get_usage_stats(&self) -> Result<UsageStats> {
        match self.usage_stats.lock() {
            Ok(stats) => Ok(stats.clone()),
            Err(_) => Err(anyhow::anyhow!("Failed to acquire lock on usage statistics"))
        }
    }
    
    /// Resets the cumulative usage statistics to zero
    pub fn reset_usage_stats(&self) -> Result<()> {
        match self.usage_stats.lock() {
            Ok(mut stats) => {
                stats.prompt_tokens = 0;
                stats.completion_tokens = 0;
                Ok(())
            },
            Err(_) => Err(anyhow::anyhow!("Failed to acquire lock on usage statistics"))
        }
    }
}
