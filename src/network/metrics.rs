use prometheus::{register_int_counter, register_int_gauge, register_histogram, IntCounter, IntGauge, Histogram, HistogramOpts};
use std::sync::Arc;
use std::time::Duration;

pub struct PeerDiscoveryMetrics {
    // Peer Discovery Metrics
    pub peers_discovered: IntCounter,
    pub peers_discovered_total: IntCounter,
    pub discovery_attempts: IntCounter,
    pub discovery_attempts_total: IntCounter,
    pub discovery_success_rate: Histogram,
    pub discovery_latency: Histogram,
    pub discovery_latency_total: Histogram,
    
    // Peer Connection Metrics
    pub peer_connections: IntGauge,
    pub peer_connections_total: IntGauge,
    pub peer_response_rate: Histogram,
    pub peer_response_rate_total: Histogram,
    pub peer_latency: Histogram,
    pub peer_latency_total: Histogram,
    
    // Peer Health Metrics
    pub reliable_peers: IntGauge,
    pub reliable_peers_total: IntGauge,
    pub peer_uptime: Histogram,
    pub peer_uptime_total: Histogram,
    pub peer_reputation_score: Histogram,
    pub peer_reputation_score_total: Histogram,
    
    // Discovery Errors
    pub discovery_errors: IntCounter,
    pub discovery_errors_total: IntCounter,
    pub peer_connection_errors: IntCounter,
    pub peer_connection_errors_total: IntCounter,
    
    // Network Topology Metrics
    pub network_degree: IntGauge,
    pub network_degree_total: IntGauge,
    pub network_diameter: IntGauge,
    pub network_diameter_total: IntGauge,
    pub network_clustering: Histogram,
    pub network_clustering_total: Histogram,
}

impl PeerDiscoveryMetrics {
    pub fn new() -> Self {
        let discovery_latency = register_histogram!(
            "peer_discovery_latency_seconds",
            "Time taken for peer discovery operations",
            vec![0.1, 0.5, 1.0, 2.0, 5.0, 10.0]
        ).unwrap();

        let peer_latency = register_histogram!(
            "peer_latency_seconds",
            "Latency to communicate with peers",
            vec![0.01, 0.05, 0.1, 0.5, 1.0]
        ).unwrap();

        let peer_response_rate = register_histogram!(
            "peer_response_rate",
            "Peer response rate (0-1)",
            vec![0.0, 0.2, 0.4, 0.6, 0.8, 1.0]
        ).unwrap();

        let peer_reputation_score = register_histogram!(
            "peer_reputation_score",
            "Peer reputation score (0-1)",
            vec![0.0, 0.2, 0.4, 0.6, 0.8, 1.0]
        ).unwrap();

        let network_clustering = register_histogram!(
            "network_clustering",
            "Network clustering coefficient (0-1)",
            vec![0.0, 0.2, 0.4, 0.6, 0.8, 1.0]
        ).unwrap();

        Self {
            peers_discovered: register_int_counter!(
                "peers_discovered",
                "Number of peers discovered"
            ).unwrap(),
            peers_discovered_total: register_int_counter!(
                "peers_discovered_total",
                "Total number of peers discovered"
            ).unwrap(),
            discovery_attempts: register_int_counter!(
                "discovery_attempts",
                "Number of discovery attempts"
            ).unwrap(),
            discovery_attempts_total: register_int_counter!(
                "discovery_attempts_total",
                "Total number of discovery attempts"
            ).unwrap(),
            discovery_success_rate: register_histogram!(
                "discovery_success_rate",
                "Discovery success rate (0-1)",
                vec![0.0, 0.2, 0.4, 0.6, 0.8, 1.0]
            ).unwrap(),
            discovery_latency,
            discovery_latency_total: discovery_latency.clone(),
            peer_connections: register_int_gauge!(
                "peer_connections",
                "Number of active peer connections"
            ).unwrap(),
            peer_connections_total: register_int_gauge!(
                "peer_connections_total",
                "Total peer connections"
            ).unwrap(),
            peer_response_rate,
            peer_response_rate_total: peer_response_rate.clone(),
            peer_latency,
            peer_latency_total: peer_latency.clone(),
            reliable_peers: register_int_gauge!(
                "reliable_peers",
                "Number of reliable peers"
            ).unwrap(),
            reliable_peers_total: register_int_gauge!(
                "reliable_peers_total",
                "Total reliable peers"
            ).unwrap(),
            peer_uptime: register_histogram!(
                "peer_uptime_seconds",
                "Peer uptime in seconds",
                vec![3600.0, 86400.0, 604800.0, 2678400.0] // 1h, 24h, 7d, 31d
            ).unwrap(),
            peer_uptime_total: peer_uptime.clone(),
            peer_reputation_score,
            peer_reputation_score_total: peer_reputation_score.clone(),
            discovery_errors: register_int_counter!(
                "discovery_errors",
                "Number of discovery errors"
            ).unwrap(),
            discovery_errors_total: register_int_counter!(
                "discovery_errors_total",
                "Total number of discovery errors"
            ).unwrap(),
            peer_connection_errors: register_int_counter!(
                "peer_connection_errors",
                "Number of peer connection errors"
            ).unwrap(),
            peer_connection_errors_total: register_int_counter!(
                "peer_connection_errors_total",
                "Total number of peer connection errors"
            ).unwrap(),
            network_degree: register_int_gauge!(
                "network_degree",
                "Average network degree"
            ).unwrap(),
            network_degree_total: register_int_gauge!(
                "network_degree_total",
                "Total network degree"
            ).unwrap(),
            network_diameter: register_int_gauge!(
                "network_diameter",
                "Network diameter"
            ).unwrap(),
            network_diameter_total: register_int_gauge!(
                "network_diameter_total",
                "Total network diameter"
            ).unwrap(),
            network_clustering,
            network_clustering_total: network_clustering.clone(),
        }
    }

    pub fn observe_discovery_latency(&self, duration: f64) {
        self.discovery_latency.observe(duration);
        self.discovery_latency_total.observe(duration);
    }

    pub fn observe_peer_latency(&self, duration: f64) {
        self.peer_latency.observe(duration);
        self.peer_latency_total.observe(duration);
    }

    pub fn observe_peer_response_rate(&self, rate: f64) {
        self.peer_response_rate.observe(rate);
        self.peer_response_rate_total.observe(rate);
    }

    pub fn observe_peer_reputation_score(&self, score: f64) {
        self.peer_reputation_score.observe(score);
        self.peer_reputation_score_total.observe(score);
    }

    pub fn observe_network_clustering(&self, coefficient: f64) {
        self.network_clustering.observe(coefficient);
        self.network_clustering_total.observe(coefficient);
    }

    pub fn increment_peers_discovered(&self) {
        self.peers_discovered.inc();
        self.peers_discovered_total.inc();
    }

    pub fn increment_discovery_attempts(&self) {
        self.discovery_attempts.inc();
        self.discovery_attempts_total.inc();
    }

    pub fn increment_discovery_errors(&self) {
        self.discovery_errors.inc();
        self.discovery_errors_total.inc();
    }

    pub fn increment_peer_connection_errors(&self) {
        self.peer_connection_errors.inc();
        self.peer_connection_errors_total.inc();
    }

    pub fn set_peer_connections(&self, count: u64) {
        self.peer_connections.set(count as i64);
        self.peer_connections_total.set(count as i64);
    }

    pub fn set_reliable_peers(&self, count: u64) {
        self.reliable_peers.set(count as i64);
        self.reliable_peers_total.set(count as i64);
    }

    pub fn set_network_degree(&self, degree: u64) {
        self.network_degree.set(degree as i64);
        self.network_degree_total.set(degree as i64);
    }

    pub fn set_network_diameter(&self, diameter: u64) {
        self.network_diameter.set(diameter as i64);
        self.network_diameter_total.set(diameter as i64);
    }
}
