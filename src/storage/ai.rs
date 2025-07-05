use std::collections::HashMap;
use std::path::PathBuf;
use anyhow::{Result, anyhow};
use async_trait::async_trait;
use reqwest::Client;
use crate::storage::storage::{StorageInterface, StorageStatus};

pub struct AiStorage {
    client: Client,
    endpoint: String,
}

impl AiStorage {
    pub fn new(endpoint: String) -> Self {
        Self {
            client: Client::new(),
            endpoint,
        }
    }

    async fn test_connection(&self) -> Result<()> {
        let url = format!("{}/health", self.endpoint);
        self.client.get(&url)
            .send()
            .await
            .map_err(|e| anyhow!(e))?
            .error_for_status()
            .map_err(|e| anyhow!(e))?;
        Ok(())
    }
}

#[async_trait]
impl StorageInterface for AiStorage {
    async fn upload_file(&self, _file: PathBuf) -> Result<String> {
        Err(anyhow!("AI storage does not handle direct file uploads"))
    }

    async fn get_status(&self, cid: &str) -> Result<StorageStatus> {
        let url = format!("{}verify/{}", self.endpoint, cid);
        let response = self.client.get(&url)
            .send()
            .await
            .map_err(|e| anyhow!(e))?;
            
        Ok(StorageStatus {
            ipfs_status: false,
            ai_status: response.status().is_success(),
            content_cid: Some(cid.to_string()),
            ai_verification: Some(cid.to_string()),
        })
    }

    async fn verify_content(&self, cid: &str) -> Result<String> {
        let url = format!("{}verify/{}", self.endpoint, cid);
        let response = self.client.get(&url)
            .send()
            .await
            .map_err(|e| anyhow!(e))?;
            
        if response.status().is_success() {
            Ok(cid.to_string())
        } else {
            Err(anyhow!("Verification failed"))
        }
    }

    async fn configure(&self, settings: &HashMap<String, String>) -> Result<()> {
        if let Some(api_key) = settings.get("ai_api_key") {
            // TODO: Implement API key configuration
        }
        Ok(())
    }
}
