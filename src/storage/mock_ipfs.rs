/// Mock IPFS implementation for development without IPFS infrastructure
use anyhow::Result;
use std::collections::HashMap;
use std::sync::Arc;
use tokio::sync::RwLock;
use tracing::info;

#[derive(Debug, Clone)]
pub struct MockIpfsClient {
    storage: Arc<RwLock<HashMap<String, Vec<u8>>>>,
    base_url: String,
}

impl MockIpfsClient {
    pub fn new(url: &str) -> Result<Self> {
        info!("Creating mock IPFS client for URL: {}", url);
        Ok(Self {
            storage: Arc::new(RwLock::new(HashMap::new())),
            base_url: url.to_string(),
        })
    }
    
    pub fn from_url(url: &str) -> Result<Self> {
        Self::new(url)
    }
    
    pub async fn add_bytes(&self, data: &[u8]) -> Result<String> {
        // Generate a fake CID
        let cid = format!("mock-cid-{}", uuid::Uuid::new_v4());
        
        // Store in memory
        self.storage.write().await.insert(cid.clone(), data.to_vec());
        
        info!("Mock IPFS: Added {} bytes with CID: {}", data.len(), cid);
        Ok(cid)
    }
    
    pub async fn cat(&self, cid: &str) -> Result<Vec<u8>> {
        let storage = self.storage.read().await;
        if let Some(data) = storage.get(cid) {
            info!("Mock IPFS: Retrieved {} bytes for CID: {}", data.len(), cid);
            Ok(data.clone())
        } else {
            Err(anyhow::anyhow!("CID not found in mock storage: {}", cid))
        }
    }
    
    pub async fn pin_add(&self, cid: &str) -> Result<()> {
        info!("Mock IPFS: Pinned CID: {} (no-op)", cid);
        Ok(())
    }
}

// Re-export as IpfsClient for compatibility
pub type IpfsClient = MockIpfsClient;