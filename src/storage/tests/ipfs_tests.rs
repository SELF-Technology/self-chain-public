use std::path::PathBuf;
use anyhow::Result;
use tokio::fs;
use tokio::test;
use std::collections::HashMap;
use self_chain_core::storage::{StorageInterface, StorageStatus, IpfsStorage};

// Test IPFS connection establishment
#[test]
async fn test_ipfs_connection() -> Result<()> {
    let ipfs = IpfsStorage::new("http://localhost:5001".to_string());
    let connection_result = ipfs.test_connection().await;
    
    assert!(connection_result.is_ok(), 
        "IPFS connection failed: {:?}", connection_result.err());
    
    Ok(())
}

// Test file upload functionality
#[test]
async fn test_file_upload() -> Result<()> {
    let ipfs = IpfsStorage::new("http://localhost:5001".to_string());
    let test_file_path = PathBuf::from("/tmp/ipfs_test_file.txt");
    
    // Create a test file with random content
    let content = format!("Test content generated at {}", chrono::Utc::now());
    fs::write(&test_file_path, content).await?;
    
    // Upload the file
    let cid = ipfs.upload_file(test_file_path.clone()).await?;
    assert!(!cid.is_empty(), "CID should not be empty");
    
    // Check the status
    let status = ipfs.get_status(&cid).await?;
    assert!(status.ipfs_status, "File should be available in IPFS");
    
    // Clean up
    fs::remove_file(test_file_path).await?;
    
    Ok(())
}

// Test retrieving non-existent content
#[test]
async fn test_missing_content() -> Result<()> {
    let ipfs = IpfsStorage::new("http://localhost:5001".to_string());
    let fake_cid = "QmNonExistentCIDxxxxxxxxxxxxxxxxxxxxx";
    
    let status_result = ipfs.get_status(fake_cid).await;
    
    // It should either return an error or a status with ipfs_status=false
    if let Ok(status) = status_result {
        assert!(!status.ipfs_status, "Non-existent CID should have status=false");
    } else {
        // Error is also an acceptable result for non-existent content
        assert!(status_result.is_err());
    }
    
    Ok(())
}

// Test IPFS configuration
#[test]
async fn test_ipfs_configuration() -> Result<()> {
    let ipfs = IpfsStorage::new("http://localhost:5001".to_string());
    let mut settings = HashMap::new();
    
    settings.insert("timeout_seconds".to_string(), "30".to_string());
    settings.insert("max_retries".to_string(), "3".to_string());
    
    let config_result = ipfs.configure(&settings).await;
    assert!(config_result.is_ok(), 
        "Configuration should succeed: {:?}", config_result.err());
    
    Ok(())
}

// Test content verification - this will check the hash verification
#[test]
async fn test_content_verification() -> Result<()> {
    let ipfs = IpfsStorage::new("http://localhost:5001".to_string());
    let test_file_path = PathBuf::from("/tmp/ipfs_verify_test.txt");
    
    // Create test content
    let content = "Content for verification testing";
    fs::write(&test_file_path, content).await?;
    
    // Upload and get CID
    let cid = ipfs.upload_file(test_file_path.clone()).await?;
    
    // Verify the content
    let verification = ipfs.verify_content(&cid).await?;
    assert!(!verification.is_empty(), "Verification string should not be empty");
    
    // Clean up
    fs::remove_file(test_file_path).await?;
    
    Ok(())
}

// Test parallel uploads
#[test]
async fn test_parallel_uploads() -> Result<()> {
    let ipfs = IpfsStorage::new("http://localhost:5001".to_string());
    let file_count = 5;
    let mut file_paths = Vec::new();
    let mut tasks = Vec::new();
    
    // Create test files
    for i in 0..file_count {
        let path = PathBuf::from(format!("/tmp/ipfs_parallel_test_{}.txt", i));
        let content = format!("Parallel test content {}", i);
        fs::write(&path, content).await?;
        file_paths.push(path);
    }
    
    // Upload files concurrently
    for path in file_paths.clone() {
        let ipfs_clone = ipfs.clone(); // Requires Clone implementation
        let task = tokio::spawn(async move {
            ipfs_clone.upload_file(path).await
        });
        tasks.push(task);
    }
    
    // Collect results
    let mut cids = Vec::new();
    for task in tasks {
        let result = task.await??; // Unwrap JoinResult, then Result<String>
        cids.push(result);
    }
    
    // Verify all uploads succeeded
    assert_eq!(cids.len(), file_count, "All uploads should succeed");
    
    // Clean up
    for path in file_paths {
        fs::remove_file(path).await?;
    }
    
    Ok(())
}
