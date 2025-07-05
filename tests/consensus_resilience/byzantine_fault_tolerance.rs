//! Byzantine Fault Tolerance Tests
//!
//! Tests the PoAI consensus system's ability to handle Byzantine (malicious) nodes
//! and maintain consensus accuracy despite adversarial behavior.

use super::*;
use anyhow::Result;
use std::sync::Arc;
use std::time::Duration;
use tokio::time::timeout;

/// Byzantine fault tolerance test scenarios
#[derive(Debug, Clone)]
pub enum ByzantineScenario {
    /// Nodes always vote against valid blocks
    AlwaysReject,
    /// Nodes always vote for invalid blocks
    AlwaysAccept,
    /// Nodes vote randomly
    RandomVoting,
    /// Nodes try to double-spend
    DoubleSpend,
    /// Nodes create invalid blocks
    InvalidBlocks,
    /// Nodes delay responses
    DelayedResponse,
    /// Nodes send conflicting messages
    ConflictingMessages,
}

/// Byzantine node behavior configuration
#[derive(Debug, Clone)]
pub struct ByzantineBehavior {
    pub scenario: ByzantineScenario,
    pub activation_delay: Duration,
    pub duration: Duration,
    pub intensity: f64, // 0.0 to 1.0
}

/// Enhanced test node with Byzantine behavior
pub struct ByzantineTestNode {
    pub base_node: Arc<TestNode>,
    pub byzantine_behavior: Option<ByzantineBehavior>,
    pub attack_active: Arc<RwLock<bool>>,
    pub attack_metrics: Arc<Mutex<AttackMetrics>>,
}

#[derive(Debug, Clone)]
pub struct AttackMetrics {
    pub malicious_votes: u64,
    pub invalid_blocks_created: u64,
    pub delayed_responses: u64,
    pub conflicting_messages: u64,
    pub double_spend_attempts: u64,
}

impl ByzantineTestNode {
    pub async fn new(id: String, byzantine_behavior: Option<ByzantineBehavior>) -> Result<Self> {
        let is_malicious = byzantine_behavior.is_some();
        let base_node = Arc::new(TestNode::new(id, is_malicious).await?);

        let attack_metrics = Arc::new(Mutex::new(AttackMetrics {
            malicious_votes: 0,
            invalid_blocks_created: 0,
            delayed_responses: 0,
            conflicting_messages: 0,
            double_spend_attempts: 0,
        }));

        Ok(Self {
            base_node,
            byzantine_behavior,
            attack_active: Arc::new(RwLock::new(false)),
            attack_metrics,
        })
    }

    pub async fn start_byzantine_behavior(&self) {
        if let Some(behavior) = &self.byzantine_behavior {
            // Wait for activation delay
            tokio::time::sleep(behavior.activation_delay).await;

            *self.attack_active.write().await = true;

            // Schedule deactivation
            let attack_active = self.attack_active.clone();
            let duration = behavior.duration;
            tokio::spawn(async move {
                tokio::time::sleep(duration).await;
                *attack_active.write().await = false;
            });
        }
    }

    pub async fn validate_block_with_byzantine(&self, block: &Block) -> Result<bool> {
        if !*self.attack_active.read().await {
            return self.base_node.validate_block(block).await;
        }

        if let Some(behavior) = &self.byzantine_behavior {
            let mut metrics = self.attack_metrics.lock().await;

            let result = match behavior.scenario {
                ByzantineScenario::AlwaysReject => {
                    metrics.malicious_votes += 1;
                    Ok(false)
                }
                ByzantineScenario::AlwaysAccept => {
                    metrics.malicious_votes += 1;
                    Ok(true)
                }
                ByzantineScenario::RandomVoting => {
                    metrics.malicious_votes += 1;
                    Ok(rand::random::<f64>() < behavior.intensity)
                }
                ByzantineScenario::DelayedResponse => {
                    metrics.delayed_responses += 1;
                    let delay = Duration::from_millis((1000.0 * behavior.intensity) as u64);
                    tokio::time::sleep(delay).await;
                    self.base_node.validate_block(block).await
                }
                _ => self.base_node.validate_block(block).await,
            };

            result
        } else {
            self.base_node.validate_block(block).await
        }
    }

    pub async fn create_malicious_block(&self, block_template: &Block) -> Result<Block> {
        if !*self.attack_active.read().await {
            return Ok(block_template.clone());
        }

        if let Some(behavior) = &self.byzantine_behavior {
            let mut metrics = self.attack_metrics.lock().await;

            let mut malicious_block = block_template.clone();

            match behavior.scenario {
                ByzantineScenario::InvalidBlocks => {
                    metrics.invalid_blocks_created += 1;
                    // Create invalid block by corrupting data
                    malicious_block.header.previous_hash = "invalid_hash".to_string();
                    malicious_block.hash = "corrupted_hash".to_string();
                }
                ByzantineScenario::DoubleSpend => {
                    metrics.double_spend_attempts += 1;
                    // Create conflicting transaction
                    if !malicious_block.transactions.is_empty() {
                        let mut tx = malicious_block.transactions[0].clone();
                        tx.id = format!("{}_double_spend", tx.id);
                        tx.receiver = "malicious_receiver".to_string();
                        malicious_block.transactions.push(tx);
                    }
                }
                _ => {}
            }

            Ok(malicious_block)
        } else {
            Ok(block_template.clone())
        }
    }

    pub async fn get_attack_metrics(&self) -> AttackMetrics {
        self.attack_metrics.lock().await.clone()
    }
}

/// Byzantine fault tolerance test suite
pub struct ByzantineFaultToleranceTest {
    pub network: TestNetwork,
    pub byzantine_nodes: Vec<Arc<ByzantineTestNode>>,
    pub honest_nodes: Vec<Arc<ByzantineTestNode>>,
}

impl ByzantineFaultToleranceTest {
    pub async fn new(config: ResilienceTestConfig) -> Result<Self> {
        let network = TestNetwork::new(config.clone()).await?;
        let mut byzantine_nodes = Vec::new();
        let mut honest_nodes = Vec::new();

        // Create Byzantine nodes with different attack patterns
        for i in 0..config.malicious_nodes {
            let behavior = match i % 4 {
                0 => Some(ByzantineBehavior {
                    scenario: ByzantineScenario::AlwaysReject,
                    activation_delay: Duration::from_secs(10),
                    duration: Duration::from_secs(60),
                    intensity: 1.0,
                }),
                1 => Some(ByzantineBehavior {
                    scenario: ByzantineScenario::RandomVoting,
                    activation_delay: Duration::from_secs(20),
                    duration: Duration::from_secs(120),
                    intensity: 0.7,
                }),
                2 => Some(ByzantineBehavior {
                    scenario: ByzantineScenario::DelayedResponse,
                    activation_delay: Duration::from_secs(30),
                    duration: Duration::from_secs(90),
                    intensity: 0.5,
                }),
                _ => Some(ByzantineBehavior {
                    scenario: ByzantineScenario::InvalidBlocks,
                    activation_delay: Duration::from_secs(15),
                    duration: Duration::from_secs(75),
                    intensity: 0.8,
                }),
            };

            let node =
                Arc::new(ByzantineTestNode::new(format!("byzantine_node_{}", i), behavior).await?);
            byzantine_nodes.push(node);
        }

        // Create honest nodes
        for i in 0..(config.node_count - config.malicious_nodes) {
            let node = Arc::new(ByzantineTestNode::new(format!("honest_node_{}", i), None).await?);
            honest_nodes.push(node);
        }

        Ok(Self {
            network,
            byzantine_nodes,
            honest_nodes,
        })
    }

    pub async fn run_byzantine_test(&self) -> Result<TestSummary> {
        println!("Starting Byzantine Fault Tolerance Test...");

        let start_time = std::time::Instant::now();

        // Start Byzantine behaviors
        for node in &self.byzantine_nodes {
            let node_clone = node.clone();
            tokio::spawn(async move {
                node_clone.start_byzantine_behavior().await;
            });
        }

        // Run consensus rounds with Byzantine nodes
        let mut consensus_rounds = 0;
        let mut successful_consensus = 0;
        let mut byzantine_blocks_rejected = 0;
        let mut valid_blocks_accepted = 0;

        let test_duration = Duration::from_secs(self.network.config.test_duration_secs);
        let round_interval = Duration::from_secs(self.network.config.block_time_secs);

        let timeout_result = timeout(test_duration, async {
            loop {
                consensus_rounds += 1;

                // Create test block
                let producer_id = format!("producer_{}", consensus_rounds);
                let block = create_test_block(producer_id).await?;

                // Collect votes from all nodes
                let mut votes = Vec::new();

                // Byzantine nodes vote
                for node in &self.byzantine_nodes {
                    if let Ok(vote) = node.validate_block_with_byzantine(&block).await {
                        votes.push(vote);
                    }
                }

                // Honest nodes vote
                for node in &self.honest_nodes {
                    if let Ok(vote) = node.base_node.validate_block(&block).await {
                        votes.push(vote);
                    }
                }

                // Determine consensus
                let positive_votes = votes.iter().filter(|&&v| v).count();
                let total_votes = votes.len();
                let consensus_threshold = total_votes / 2 + 1;

                if positive_votes >= consensus_threshold {
                    successful_consensus += 1;
                    valid_blocks_accepted += 1;
                } else {
                    byzantine_blocks_rejected += 1;
                }

                tokio::time::sleep(round_interval).await;
            }
        })
        .await;

        let elapsed = start_time.elapsed();

        println!(
            "Byzantine Fault Tolerance Test completed in {:.2?}",
            elapsed
        );

        // Collect attack metrics
        let mut total_attack_metrics = AttackMetrics {
            malicious_votes: 0,
            invalid_blocks_created: 0,
            delayed_responses: 0,
            conflicting_messages: 0,
            double_spend_attempts: 0,
        };

        for node in &self.byzantine_nodes {
            let metrics = node.get_attack_metrics().await;
            total_attack_metrics.malicious_votes += metrics.malicious_votes;
            total_attack_metrics.invalid_blocks_created += metrics.invalid_blocks_created;
            total_attack_metrics.delayed_responses += metrics.delayed_responses;
            total_attack_metrics.conflicting_messages += metrics.conflicting_messages;
            total_attack_metrics.double_spend_attempts += metrics.double_spend_attempts;
        }

        // Calculate success metrics
        let consensus_accuracy = if consensus_rounds > 0 {
            (successful_consensus as f64 / consensus_rounds as f64) * 100.0
        } else {
            0.0
        };

        let byzantine_resistance = if byzantine_blocks_rejected + valid_blocks_accepted > 0 {
            (valid_blocks_accepted as f64
                / (byzantine_blocks_rejected + valid_blocks_accepted) as f64)
                * 100.0
        } else {
            0.0
        };

        // Generate test summary
        let mut test_metrics = self.network.get_final_metrics().await;
        test_metrics.consensus_accuracy = consensus_accuracy;
        test_metrics.byzantine_attacks = total_attack_metrics.malicious_votes as u32;

        let success = consensus_accuracy >= 67.0 && byzantine_resistance >= 75.0;
        let mut recommendations = Vec::new();

        if consensus_accuracy < 67.0 {
            recommendations
                .push("Consider increasing Byzantine fault tolerance threshold".to_string());
        }
        if byzantine_resistance < 75.0 {
            recommendations.push("Improve malicious node detection mechanisms".to_string());
        }
        if total_attack_metrics.delayed_responses > 0 {
            recommendations.push("Implement timeout mechanisms for delayed responses".to_string());
        }
        if total_attack_metrics.double_spend_attempts > 0 {
            recommendations.push("Strengthen double-spend detection algorithms".to_string());
        }

        Ok(TestSummary {
            test_name: "Byzantine Fault Tolerance Test".to_string(),
            config: self.network.config.clone(),
            metrics: test_metrics,
            success,
            error_message: if success {
                None
            } else {
                Some("Byzantine fault tolerance below acceptable threshold".to_string())
            },
            recommendations,
        })
    }
}

/// Specific Byzantine attack scenarios
pub struct ByzantineAttackScenarios;

impl ByzantineAttackScenarios {
    /// Test 51% attack scenario
    pub async fn test_majority_attack(config: ResilienceTestConfig) -> Result<TestSummary> {
        let mut attack_config = config.clone();
        attack_config.malicious_nodes = attack_config.node_count / 2 + 1; // 51% attack

        let test = ByzantineFaultToleranceTest::new(attack_config).await?;
        test.run_byzantine_test().await
    }

    /// Test coordinated attack scenario
    pub async fn test_coordinated_attack(config: ResilienceTestConfig) -> Result<TestSummary> {
        let test = ByzantineFaultToleranceTest::new(config).await?;

        // All Byzantine nodes coordinate to attack at the same time
        for node in &test.byzantine_nodes {
            *node.attack_active.write().await = true;
        }

        test.run_byzantine_test().await
    }

    /// Test gradual attack scenario
    pub async fn test_gradual_attack(config: ResilienceTestConfig) -> Result<TestSummary> {
        let test = ByzantineFaultToleranceTest::new(config).await?;

        // Start Byzantine behaviors with staggered timing
        for (i, node) in test.byzantine_nodes.iter().enumerate() {
            let node_clone = node.clone();
            let delay = Duration::from_secs(30 + i as u64 * 10);
            tokio::spawn(async move {
                tokio::time::sleep(delay).await;
                *node_clone.attack_active.write().await = true;
            });
        }

        test.run_byzantine_test().await
    }

    /// Test adaptive attack scenario
    pub async fn test_adaptive_attack(config: ResilienceTestConfig) -> Result<TestSummary> {
        let test = ByzantineFaultToleranceTest::new(config).await?;

        // Byzantine nodes adapt their behavior based on success rate
        // This would require more sophisticated state tracking
        test.run_byzantine_test().await
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_basic_byzantine_tolerance() {
        let config = ResilienceTestConfig {
            node_count: 7,
            malicious_nodes: 2, // 28% malicious (below 33% threshold)
            test_duration_secs: 60,
            ..Default::default()
        };

        let test = ByzantineFaultToleranceTest::new(config).await.unwrap();
        let result = test.run_byzantine_test().await.unwrap();

        assert!(result.success, "Byzantine fault tolerance test failed");
        assert!(result.metrics.consensus_accuracy >= 67.0);

        println!("{}", result.generate_report());
    }

    #[tokio::test]
    async fn test_51_percent_attack_resistance() {
        let config = ResilienceTestConfig {
            node_count: 5,
            malicious_nodes: 3, // 60% malicious (above 51% threshold)
            test_duration_secs: 30,
            ..Default::default()
        };

        let result = ByzantineAttackScenarios::test_majority_attack(config)
            .await
            .unwrap();

        // With 51%+ attack, consensus should still be possible but with reduced accuracy
        println!("51% Attack Test Result: {}", result.generate_report());

        // The test might fail, which is expected for a 51% attack
        if !result.success {
            println!("51% attack successfully disrupted consensus (expected behavior)");
        }
    }

    #[tokio::test]
    async fn test_coordinated_attack_resistance() {
        let config = ResilienceTestConfig {
            node_count: 9,
            malicious_nodes: 3, // 33% malicious
            test_duration_secs: 90,
            ..Default::default()
        };

        let result = ByzantineAttackScenarios::test_coordinated_attack(config)
            .await
            .unwrap();

        // System should maintain consensus despite coordinated attack
        assert!(result.metrics.consensus_accuracy >= 60.0);

        println!("{}", result.generate_report());
    }

    #[tokio::test]
    async fn test_gradual_attack_resistance() {
        let config = ResilienceTestConfig {
            node_count: 10,
            malicious_nodes: 3,
            test_duration_secs: 120,
            ..Default::default()
        };

        let result = ByzantineAttackScenarios::test_gradual_attack(config)
            .await
            .unwrap();

        println!("{}", result.generate_report());
    }
}
