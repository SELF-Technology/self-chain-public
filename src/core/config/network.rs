use std::net::SocketAddr;
use serde::{Deserialize, Serialize};

#[derive(Debug, Deserialize, Serialize)]
pub struct NetworkConfig {
    pub listen_address: SocketAddr,
    pub peers: Vec<SocketAddr>,
    pub max_peers: u32,
    pub ping_interval: u64,
    pub timeout: u64,
}

impl Default for NetworkConfig {
    fn default() -> Self {
        Self {
            listen_address: "127.0.0.1:8080".parse().unwrap(),
            peers: vec![],
            max_peers: 100,
            ping_interval: 30,  // seconds
            timeout: 10,       // seconds
        }
    }
}
