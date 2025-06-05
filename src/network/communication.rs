use std::sync::Arc;
use std::time::Duration;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct NodeCommunicator {
    pub node_id: String,
    pub network_latency: u64, // in ms
    pub bandwidth: u64, // in bytes per second
}

impl NodeCommunicator {
    pub fn new(node_id: String, network_latency: u64, bandwidth: u64) -> Self {
        Self {
            node_id,
            network_latency,
            bandwidth,
        }
    }

    pub async fn send_transaction(&self, transaction: &Transaction) -> Result<(), String> {
        // Simulate network delay
        tokio::time::sleep(Duration::from_millis(self.network_latency)).await;
        Ok(())
    }
}
