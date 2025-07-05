/// Real IPFS client implementation using ipfs-api-backend-hyper
use anyhow::{Result, Context};
use ipfs_api_backend_hyper::{IpfsClient as HyperIpfsClient, IpfsApi};
use futures::TryStreamExt;
use std::io::Cursor;
use tracing::{info, debug};

#[derive(Debug, Clone)]
pub struct IpfsClient {
    client: HyperIpfsClient,
}

impl IpfsClient {
    pub fn new(url: &str) -> Result<Self> {
        // Parse URL to extract host and port
        let url = url.trim_start_matches("http://");
        let parts: Vec<&str> = url.split(':').collect();
        
        let (host, port) = if parts.len() == 2 {
            (parts[0], parts[1].parse::<u16>().unwrap_or(5001))
        } else {
            (parts[0], 5001)
        };
        
        info!("Creating IPFS client for {}:{}", host, port);
        
        // Use the from_host_and_port method for creating the client
        let client = HyperIpfsClient::from_host_and_port(
            http::uri::Scheme::HTTP,
            host,
            port
        ).map_err(|e| anyhow::anyhow!("Failed to create IPFS client: {}", e))?;
        
        Ok(Self { client })
    }
    
    pub fn from_url(url: &str) -> Result<Self> {
        Self::new(url)
    }
    
    pub async fn add_bytes(&self, data: &[u8]) -> Result<String> {
        debug!("Adding {} bytes to IPFS", data.len());
        
        // Clone the data to avoid lifetime issues
        let data_vec = data.to_vec();
        let cursor = Cursor::new(data_vec);
        let response = self.client
            .add(cursor)
            .await
            .context("Failed to add data to IPFS")?;
        
        info!("Added data to IPFS with hash: {}", response.hash);
        Ok(response.hash)
    }
    
    pub async fn cat(&self, cid: &str) -> Result<Vec<u8>> {
        debug!("Retrieving {} from IPFS", cid);
        
        use futures::TryStreamExt;
        
        // cat returns a stream directly, not a Result<Stream>
        let data = self.client
            .cat(cid)
            .map_ok(|chunk| chunk.to_vec())
            .try_concat()
            .await
            .context("Failed to retrieve data from IPFS")?;
        
        info!("Retrieved {} bytes from IPFS for CID: {}", data.len(), cid);
        Ok(data)
    }
    
    pub async fn pin_add(&self, cid: &str) -> Result<()> {
        debug!("Pinning {} to IPFS", cid);
        
        self.client
            .pin_add(cid, false)
            .await
            .context("Failed to pin CID")?;
        
        info!("Successfully pinned {} to IPFS", cid);
        Ok(())
    }
    
    pub async fn version(&self) -> Result<String> {
        let version = self.client
            .version()
            .await
            .context("Failed to get IPFS version")?;
        
        Ok(format!("{} {}", version.version, version.commit))
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    
    #[tokio::test]
    async fn test_ipfs_connection() -> Result<()> {
        let client = IpfsClient::new("http://localhost:5001")?;
        let version = client.version().await?;
        println!("IPFS version: {}", version);
        assert!(!version.is_empty());
        Ok(())
    }
    
    #[tokio::test]
    async fn test_add_and_cat() -> Result<()> {
        let client = IpfsClient::new("http://localhost:5001")?;
        
        let test_data = b"Hello from SELF Chain!";
        let cid = client.add_bytes(test_data).await?;
        println!("Added data with CID: {}", cid);
        
        let retrieved = client.cat(&cid).await?;
        assert_eq!(test_data, &retrieved[..]);
        
        Ok(())
    }
}