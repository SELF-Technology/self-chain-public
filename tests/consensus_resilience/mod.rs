//! Consensus Resilience Testing Framework
//!
//! This module provides comprehensive testing for the PoAI consensus system
//! under various stress conditions and adversarial scenarios.

use anyhow::{anyhow, Result};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::sync::Arc;
use std::time::{Duration, Instant, SystemTime};
use tokio::sync::{Mutex, RwLock};
use tokio::time::timeout;

use self_chain_core::ai::service::AIService;
use self_chain_core::blockchain::{Block, BlockHeader, BlockMeta, Blockchain, Transaction};
use self_chain_core::consensus::PoAI;
use self_chain_core::core::config::AIConfig;
use self_chain_core::network::message_handler::MessageHandler;

pub mod ai_consensus_accuracy;
pub mod byzantine_fault_tolerance;
pub mod load_testing;
pub mod network_partition;
pub mod performance_benchmarks;

/// Test configuration for resilience testing
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ResilienceTestConfig {
    /// Number of nodes in the test network
    pub node_count: usize,
    /// Number of malicious nodes (for Byzantine testing)
    pub malicious_nodes: usize,
    /// Test duration in seconds
    pub test_duration_secs: u64,
    /// Transaction rate per second
    pub tx_rate_per_sec: u32,
    /// Block time in seconds
    pub block_time_secs: u64,
    /// Network latency simulation (ms)
    pub network_latency_ms: u64,
    /// Packet loss percentage (0-100)
    pub packet_loss_percent: u8,
    /// Memory limit per node (MB)
    pub memory_limit_mb: usize,
    /// CPU limit per node (percentage)
    pub cpu_limit_percent: u8,
}

impl Default for ResilienceTestConfig {
    fn default() -> Self {
        Self {
            node_count: 5,
            malicious_nodes: 1,
            test_duration_secs: 300, // 5 minutes
            tx_rate_per_sec: 10,
            block_time_secs: 10,
            network_latency_ms: 100,
            packet_loss_percent: 5,
            memory_limit_mb: 512,
            cpu_limit_percent: 80,
        }
    }
}

/// Test metrics collected during resilience testing
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TestMetrics {
    /// Test start time
    pub start_time: SystemTime,
    /// Test end time
    pub end_time: Option<SystemTime>,
    /// Total transactions processed
    pub transactions_processed: u64,
    /// Total blocks created
    pub blocks_created: u64,
    /// Consensus accuracy percentage
    pub consensus_accuracy: f64,
    /// Average block time
    pub avg_block_time_ms: u64,
    /// Average transaction throughput
    pub avg_tx_throughput: f64,
    /// Network partition events
    pub partition_events: u32,
    /// Byzantine attack attempts
    pub byzantine_attacks: u32,
    /// Failed consensus attempts
    pub failed_consensus: u32,
    /// Node failures
    pub node_failures: u32,
    /// Recovery time from failures (ms)
    pub avg_recovery_time_ms: u64,
    /// Memory usage statistics
    pub memory_stats: ResourceStats,
    /// CPU usage statistics
    pub cpu_stats: ResourceStats,
    /// Network statistics
    pub network_stats: NetworkStats,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ResourceStats {
    pub min: f64,
    pub max: f64,
    pub avg: f64,
    pub p95: f64,
    pub p99: f64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct NetworkStats {
    pub messages_sent: u64,
    pub messages_received: u64,
    pub bytes_sent: u64,
    pub bytes_received: u64,
    pub avg_latency_ms: f64,
    pub packet_loss_rate: f64,
}

/// Mock node for resilience testing
pub struct TestNode {
    pub id: String,
    pub blockchain: Arc<Blockchain>,
    pub consensus: Arc<PoAI>,
    pub is_malicious: bool,
    pub is_online: Arc<RwLock<bool>>,
    pub metrics: Arc<Mutex<NodeMetrics>>,
    pub message_handler: Arc<MessageHandler>,
}

#[derive(Debug, Clone)]
pub struct NodeMetrics {
    pub blocks_validated: u64,
    pub transactions_processed: u64,
    pub consensus_decisions: u64,
    pub correct_decisions: u64,
    pub message_count: u64,
    pub uptime_ms: u64,
    pub last_heartbeat: SystemTime,
}

impl TestNode {
    pub async fn new(id: String, is_malicious: bool) -> Result<Self> {
        // Create blockchain
        let blockchain = Arc::new(Blockchain::new(2));

        // Create message handler
        let message_handler = Arc::new(MessageHandler::new());

        // Create AI service with test configuration
        let ai_config = AIConfig::default();
        let ai_service = Arc::new(AIService::new(ai_config));

        // Create consensus system
        let consensus = Arc::new(PoAI::new(ai_service, message_handler.clone()));

        // Initialize metrics
        let metrics = Arc::new(Mutex::new(NodeMetrics {
            blocks_validated: 0,
            transactions_processed: 0,
            consensus_decisions: 0,
            correct_decisions: 0,
            message_count: 0,
            uptime_ms: 0,
            last_heartbeat: SystemTime::now(),
        }));

        Ok(Self {
            id,
            blockchain,
            consensus,
            is_malicious,
            is_online: Arc::new(RwLock::new(true)),
            metrics,
            message_handler,
        })
    }

    pub async fn validate_block(&self, block: &Block) -> Result<bool> {
        let mut metrics = self.metrics.lock().await;
        metrics.blocks_validated += 1;

        if self.is_malicious {
            // Malicious node behavior - random validation
            Ok(rand::random::<bool>())
        } else {
            // Normal validation
            self.consensus.validate_block(block).await
        }
    }

    pub async fn is_online(&self) -> bool {
        *self.is_online.read().await
    }

    pub async fn set_online(&self, online: bool) {
        *self.is_online.write().await = online;
    }

    pub async fn get_metrics(&self) -> NodeMetrics {
        self.metrics.lock().await.clone()
    }

    pub async fn update_heartbeat(&self) {
        let mut metrics = self.metrics.lock().await;
        metrics.last_heartbeat = SystemTime::now();
    }
}

/// Test network simulator
pub struct TestNetwork {
    pub nodes: Vec<Arc<TestNode>>,
    pub config: ResilienceTestConfig,
    pub metrics: Arc<Mutex<TestMetrics>>,
    pub running: Arc<RwLock<bool>>,
}

impl TestNetwork {
    pub async fn new(config: ResilienceTestConfig) -> Result<Self> {
        let mut nodes = Vec::new();

        // Create nodes
        for i in 0..config.node_count {
            let is_malicious = i < config.malicious_nodes;
            let node = Arc::new(TestNode::new(format!("node_{}", i), is_malicious).await?);
            nodes.push(node);
        }

        let metrics = Arc::new(Mutex::new(TestMetrics {
            start_time: SystemTime::now(),
            end_time: None,
            transactions_processed: 0,
            blocks_created: 0,
            consensus_accuracy: 0.0,
            avg_block_time_ms: 0,
            avg_tx_throughput: 0.0,
            partition_events: 0,
            byzantine_attacks: 0,
            failed_consensus: 0,
            node_failures: 0,
            avg_recovery_time_ms: 0,
            memory_stats: ResourceStats {
                min: 0.0,
                max: 0.0,
                avg: 0.0,
                p95: 0.0,
                p99: 0.0,
            },
            cpu_stats: ResourceStats {
                min: 0.0,
                max: 0.0,
                avg: 0.0,
                p95: 0.0,
                p99: 0.0,
            },
            network_stats: NetworkStats {
                messages_sent: 0,
                messages_received: 0,
                bytes_sent: 0,
                bytes_received: 0,
                avg_latency_ms: 0.0,
                packet_loss_rate: 0.0,
            },
        }));

        Ok(Self {
            nodes,
            config,
            metrics,
            running: Arc::new(RwLock::new(false)),
        })
    }

    pub async fn start_test(&self) -> Result<()> {
        *self.running.write().await = true;

        // Start heartbeat monitoring
        let heartbeat_task = self.start_heartbeat_monitoring();

        // Start block production
        let block_production_task = self.start_block_production();

        // Start transaction generation
        let tx_generation_task = self.start_transaction_generation();

        // Start metrics collection
        let metrics_task = self.start_metrics_collection();

        // Run test for specified duration
        let test_duration = Duration::from_secs(self.config.test_duration_secs);
        timeout(test_duration, async {
            tokio::join!(
                heartbeat_task,
                block_production_task,
                tx_generation_task,
                metrics_task
            );
        })
        .await
        .map_err(|_| anyhow!("Test timed out"))?;

        *self.running.write().await = false;

        // Finalize metrics
        let mut metrics = self.metrics.lock().await;
        metrics.end_time = Some(SystemTime::now());

        Ok(())
    }

    async fn start_heartbeat_monitoring(&self) {
        let nodes = self.nodes.clone();
        let running = self.running.clone();

        tokio::spawn(async move {
            while *running.read().await {
                for node in &nodes {
                    if node.is_online().await {
                        node.update_heartbeat().await;
                    }
                }
                tokio::time::sleep(Duration::from_secs(1)).await;
            }
        });
    }

    async fn start_block_production(&self) {
        let nodes = self.nodes.clone();
        let running = self.running.clone();
        let metrics = self.metrics.clone();
        let block_time = Duration::from_secs(self.config.block_time_secs);

        tokio::spawn(async move {
            while *running.read().await {
                // Select a random online node to produce block
                let online_nodes: Vec<_> = nodes
                    .iter()
                    .filter(|n| futures::executor::block_on(n.is_online()))
                    .collect();

                if !online_nodes.is_empty() {
                    let producer = &online_nodes[rand::random::<usize>() % online_nodes.len()];

                    // Create test block
                    if let Ok(block) = create_test_block(producer.id.clone()).await {
                        // Validate with all online nodes
                        let mut validations = Vec::new();
                        for node in &online_nodes {
                            if let Ok(is_valid) = node.validate_block(&block).await {
                                validations.push(is_valid);
                            }
                        }

                        // Count consensus
                        let valid_count = validations.iter().filter(|&&v| v).count();
                        let consensus_reached = valid_count > online_nodes.len() / 2;

                        if consensus_reached {
                            let mut metrics_guard = metrics.lock().await;
                            metrics_guard.blocks_created += 1;
                        }
                    }
                }

                tokio::time::sleep(block_time).await;
            }
        });
    }

    async fn start_transaction_generation(&self) {
        let running = self.running.clone();
        let metrics = self.metrics.clone();
        let tx_interval = Duration::from_millis(1000 / self.config.tx_rate_per_sec as u64);

        tokio::spawn(async move {
            let mut tx_counter = 0u64;

            while *running.read().await {
                // Generate test transaction
                let _transaction = Transaction {
                    id: format!("tx_{}", tx_counter),
                    sender: format!("sender_{}", tx_counter % 100),
                    receiver: format!("receiver_{}", (tx_counter + 1) % 100),
                    amount: 100 + (tx_counter % 1000),
                    signature: format!("sig_{}", tx_counter),
                    timestamp: SystemTime::now()
                        .duration_since(SystemTime::UNIX_EPOCH)
                        .unwrap()
                        .as_secs(),
                };

                tx_counter += 1;

                let mut metrics_guard = metrics.lock().await;
                metrics_guard.transactions_processed += 1;
                drop(metrics_guard);

                tokio::time::sleep(tx_interval).await;
            }
        });
    }

    async fn start_metrics_collection(&self) {
        let nodes = self.nodes.clone();
        let running = self.running.clone();
        let metrics = self.metrics.clone();

        tokio::spawn(async move {
            while *running.read().await {
                // Collect node metrics
                let mut total_blocks = 0u64;
                let mut total_txs = 0u64;
                let mut total_decisions = 0u64;
                let mut correct_decisions = 0u64;

                for node in &nodes {
                    let node_metrics = node.get_metrics().await;
                    total_blocks += node_metrics.blocks_validated;
                    total_txs += node_metrics.transactions_processed;
                    total_decisions += node_metrics.consensus_decisions;
                    correct_decisions += node_metrics.correct_decisions;
                }

                // Update global metrics
                let mut metrics_guard = metrics.lock().await;
                if total_decisions > 0 {
                    metrics_guard.consensus_accuracy =
                        (correct_decisions as f64 / total_decisions as f64) * 100.0;
                }
                drop(metrics_guard);

                tokio::time::sleep(Duration::from_secs(5)).await;
            }
        });
    }

    pub async fn simulate_network_partition(&self, partition_duration: Duration) -> Result<()> {
        let partition_size = self.nodes.len() / 2;

        // Take half the nodes offline
        for i in 0..partition_size {
            self.nodes[i].set_online(false).await;
        }

        let mut metrics = self.metrics.lock().await;
        metrics.partition_events += 1;
        drop(metrics);

        tokio::time::sleep(partition_duration).await;

        // Bring nodes back online
        for i in 0..partition_size {
            self.nodes[i].set_online(true).await;
        }

        Ok(())
    }

    pub async fn simulate_byzantine_attack(&self, attack_duration: Duration) -> Result<()> {
        // Mark additional nodes as malicious temporarily
        let additional_malicious = self.config.node_count / 3;

        for i in self.config.malicious_nodes..self.config.malicious_nodes + additional_malicious {
            if i < self.nodes.len() {
                let node = &self.nodes[i];
                // This would require modifying the node to support dynamic malicious behavior
                // For now, we'll just count it as an attack attempt
            }
        }

        let mut metrics = self.metrics.lock().await;
        metrics.byzantine_attacks += 1;
        drop(metrics);

        tokio::time::sleep(attack_duration).await;

        Ok(())
    }

    pub async fn get_final_metrics(&self) -> TestMetrics {
        self.metrics.lock().await.clone()
    }
}

/// Helper function to create test blocks
pub async fn create_test_block(producer_id: String) -> Result<Block> {
    let timestamp = SystemTime::now()
        .duration_since(SystemTime::UNIX_EPOCH)
        .unwrap()
        .as_secs();

    let transaction = Transaction {
        id: format!("tx_{}_{}", producer_id, timestamp),
        sender: "test_sender".to_string(),
        receiver: "test_receiver".to_string(),
        amount: 100,
        signature: "test_signature".to_string(),
        timestamp,
    };

    let mut block = Block {
        header: BlockHeader {
            index: 1,
            timestamp,
            previous_hash: "genesis_hash".to_string(),
            nonce: 0,
            difficulty: 2,
        },
        transactions: vec![transaction],
        meta: BlockMeta {
            size: 0,
            tx_count: 1,
            height: 1,
            validator_id: Some(producer_id),
            validator_signature: Some("test_signature".to_string()),
        },
        hash: String::new(),
    };

    block.meta.size = block.calculate_size();
    block.hash = block.calculate_hash();

    Ok(block)
}

/// Test result summary
#[derive(Debug, Serialize, Deserialize)]
pub struct TestSummary {
    pub test_name: String,
    pub config: ResilienceTestConfig,
    pub metrics: TestMetrics,
    pub success: bool,
    pub error_message: Option<String>,
    pub recommendations: Vec<String>,
}

impl TestSummary {
    pub fn generate_report(&self) -> String {
        format!(
            r#"
=== CONSENSUS RESILIENCE TEST REPORT ===

Test: {}
Duration: {:.2} minutes
Success: {}

PERFORMANCE METRICS:
- Transactions Processed: {}
- Blocks Created: {}
- Consensus Accuracy: {:.2}%
- Average Block Time: {} ms
- Average TX Throughput: {:.2} tx/s

RESILIENCE METRICS:
- Network Partitions: {}
- Byzantine Attacks: {}
- Failed Consensus: {}
- Node Failures: {}
- Average Recovery Time: {} ms

RESOURCE USAGE:
- Memory (Avg/Max): {:.1}MB / {:.1}MB
- CPU (Avg/Max): {:.1}% / {:.1}%

NETWORK STATS:
- Messages Sent/Received: {} / {}
- Average Latency: {:.2} ms
- Packet Loss Rate: {:.2}%

RECOMMENDATIONS:
{}

"#,
            self.test_name,
            self.config.test_duration_secs as f64 / 60.0,
            if self.success { "PASSED" } else { "FAILED" },
            self.metrics.transactions_processed,
            self.metrics.blocks_created,
            self.metrics.consensus_accuracy,
            self.metrics.avg_block_time_ms,
            self.metrics.avg_tx_throughput,
            self.metrics.partition_events,
            self.metrics.byzantine_attacks,
            self.metrics.failed_consensus,
            self.metrics.node_failures,
            self.metrics.avg_recovery_time_ms,
            self.metrics.memory_stats.avg,
            self.metrics.memory_stats.max,
            self.metrics.cpu_stats.avg,
            self.metrics.cpu_stats.max,
            self.metrics.network_stats.messages_sent,
            self.metrics.network_stats.messages_received,
            self.metrics.network_stats.avg_latency_ms,
            self.metrics.network_stats.packet_loss_rate,
            self.recommendations.join("\n- ")
        )
    }
}
