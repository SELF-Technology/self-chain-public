pub mod ipfs;
pub mod hybrid_storage;
pub mod orbitdb;
pub mod orbitdb_real;
pub mod cloud;
pub mod cloud_storage_init;
pub mod ai;
pub mod storage;
pub mod mock_ipfs;
pub mod user_ipfs;
pub mod user_storage;
// pub mod ipfs_client; // TODO: Re-enable when we fix IPFS integration

// #[cfg(test)]
// mod ipfs_test;

pub use hybrid_storage::{HybridStorage, StorageConfig};
pub use cloud::CloudStorage;
pub use cloud_storage_init::CloudStorageInit;
// Storage struct is not available yet
// pub use storage::Storage;