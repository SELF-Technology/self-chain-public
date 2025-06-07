use std::sync::Arc;
use anyhow::Result;
use reqwest::Client;
use serde::{Deserialize, Serialize};
use crate::core::config::AIConfig;

#[derive(Debug, Serialize, Deserialize)]
pub struct AIResponse {
    pub content: String,
    pub usage: UsageStats,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct UsageStats {
    pub prompt_tokens: u32,
    pub completion_tokens: u32,
}

pub struct AIValidatorClient {
    client: Client,
    config: AIConfig,
}

impl AIValidatorClient {
    pub fn new(config: AIConfig) -> Self {
        Self {
            client: Client::new(),
            config,
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
        Ok(response)
    }

    pub fn get_usage_stats(&self) -> UsageStats {
        UsageStats {
            prompt_tokens: 0,
            completion_tokens: 0,
        }
    }
}
