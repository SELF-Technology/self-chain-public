use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::RwLock;
use serde::{Deserialize, Serialize};
use rand::Rng;
use crate::blockchain::block::{Block, Transaction};
use crate::network::communication::NodeCommunicator;
use crate::grid::compute::GridCompute;
use crate::monitoring::performance::PerformanceMonitor;
use crate::benchmark_scenarios::{BenchmarkScenario, TransactionProfile};
use crate::benchmark_metrics::{ScenarioMetrics, BenchmarkError};

#[derive(Debug, Serialize, Deserialize)]
pub struct BenchmarkConfig {
    pub num_transactions: u64,
    pub transaction_profiles: Vec<TransactionProfile>,
    pub scenarios: Vec<BenchmarkScenario>,
    pub node_count: u64,
    pub transaction_size: usize,
    pub validation_delay: u64, // in ms
    pub test_duration: u64, // in seconds
    pub load_pattern: LoadPattern,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum LoadPattern {
    Constant,
    RampUp,
    Spike,
    Random,
}

#[derive(Debug, Serialize, Deserialize, Clone, Default)]
pub struct BenchmarkMetrics {
    pub tps: u64,
    pub latency: u64,
    pub resource_usage: ResourceUtilization,
    pub error_rate: f64,
    pub scalability_score: f64,
    pub stability_score: f64,
    pub bottlenecks: Vec<Bottleneck>,
    pub transaction_count: u64,
    pub successful_transactions: u64,
    pub failed_transactions: u64,
    pub avg_tps: u64,
    pub peak_tps: u64,
    pub avg_latency: u64,
    pub max_latency: u64,
    pub min_latency: u64,
    pub validation_time: u64,
    pub block_time: u64,
    pub cache_hits: u64,
    pub cache_misses: u64,
}

impl BenchmarkMetrics {
    pub fn update_from_scenario(&mut self, scenario_metrics: &ScenarioMetrics) {
        self.tps = scenario_metrics.tps;
        self.latency = scenario_metrics.latency;
        self.resource_usage = scenario_metrics.resource_usage.clone();
        self.error_rate = scenario_metrics.error_rate;
        self.scalability_score = scenario_metrics.scalability_score;
        self.stability_score = scenario_metrics.stability_score;
        self.bottlenecks.extend_from_slice(&scenario_metrics.bottlenecks);
        self.transaction_count += 1;
        if scenario_metrics.error_rate == 0.0 {
            self.successful_transactions += 1;
        } else {
            self.failed_transactions += 1;
        }
        self.avg_tps = (self.avg_tps + scenario_metrics.tps) / 2;
        self.peak_tps = self.peak_tps.max(scenario_metrics.tps);
        self.avg_latency = (self.avg_latency + scenario_metrics.latency) / 2;
        self.max_latency = self.max_latency.max(scenario_metrics.latency);
        self.min_latency = self.min_latency.min(scenario_metrics.latency);
        self.validation_time += scenario_metrics.latency;
        self.block_time += scenario_metrics.latency;
        self.cache_hits += 1;
        self.cache_misses += (scenario_metrics.error_rate * 100.0) as u64;
    }
}

pub struct BenchmarkSuite {
    config: BenchmarkConfig,
    metrics: Arc<RwLock<BenchmarkMetrics>>,
    grid_compute: Arc<GridCompute>,
    performance_monitor: Arc<PerformanceMonitor>,
    communicator: Arc<NodeCommunicator>,
    rng: rand::rngs::ThreadRng,
}

impl BenchmarkSuite {
    pub fn new(
        config: BenchmarkConfig,
        grid_compute: Arc<GridCompute>,
        performance_monitor: Arc<PerformanceMonitor>,
        communicator: Arc<NodeCommunicator>,
    ) -> Self {
        Self {
            config: BenchmarkConfig {
                num_transactions: 10000,
                batch_size: 100,
                num_shards: 16,
                transaction_size: 1024,
                validation_delay: 100,
                test_duration: 60,
                load_pattern: LoadPattern::Constant,
                node_count: 100,
                scenarios: vec![
                    BenchmarkScenario::Surge {
                        baseline_tps: 1000,
                        peak_tps: 10000,
                        duration: 60,
                        surge_duration: 10
                    }
                ]
            },
            metrics: Arc::new(RwLock::new(BenchmarkMetrics {
                tps: 0,
                latency: 0,
                resource_usage: ResourceUtilization::default(),
                error_rate: 0.0,
                scalability_score: 0.0,
                stability_score: 0.0,
                bottlenecks: Vec::new(),
                transaction_count: 0,
                successful_transactions: 0,
                failed_transactions: 0,
                avg_tps: 0,
                peak_tps: 0,
                avg_latency: 0,
                max_latency: 0,
                min_latency: 0,
                validation_time: 0,
                block_time: 0,
                cache_hits: 0,
                cache_misses: 0,
            })),
            grid_compute,
            performance_monitor,
            communicator,
            rng: rand::thread_rng(),
        }
    }

    pub async fn run(&self) -> Result<BenchmarkMetrics, BenchmarkError> {
        let mut metrics = BenchmarkMetrics::default();
        let mut rng = rand::thread_rng();

        // Run scenarios
        for scenario in &self.config.scenarios { // Note: This will still error until we add scenarios to BenchmarkConfig
            let scenario_metrics = self.run_scenario(scenario).await?;
            metrics.update_from_scenario(&scenario_metrics);
        }

        Ok(metrics)
    }

    async fn run_scenario(&self, scenario: &BenchmarkScenario) -> Result<ScenarioMetrics, BenchmarkError> {
        let mut metrics = ScenarioMetrics::default();
        let start_time = SystemTime::now();

        match scenario {
            BenchmarkScenario::Surge { baseline_tps, peak_tps, duration, surge_duration } => {
                self.run_surge_scenario(baseline_tps, peak_tps, duration, surge_duration).await?
            }
            BenchmarkScenario::RampUp { start_tps, end_tps, duration } => {
                self.run_ramp_up_scenario(start_tps, end_tps, duration).await?
            }
            BenchmarkScenario::RealWorld { transaction_profiles, duration } => {
                self.run_real_world_scenario(transaction_profiles, duration).await?
            }
            BenchmarkScenario::NetworkPartition { partition_duration, partition_size, recovery_time } => {
                self.run_network_partition_scenario(partition_duration, partition_size, recovery_time).await?
            }
            BenchmarkScenario::HardwareFailure { failure_rate, recovery_time } => {
                self.run_hardware_failure_scenario(failure_rate, recovery_time).await?
            }
        }

        let end_time = SystemTime::now();
        let duration = end_time.duration_since(start_time)?;

        metrics.tps = (metrics.transaction_count as f64 / duration.as_secs_f64()) as u64;
        metrics.latency = metrics.validation_time / metrics.transaction_count;

        Ok(metrics)
    }

    async fn run_surge_scenario(&self, baseline_tps: &u64, peak_tps: &u64, duration: &u64, surge_duration: &u64) -> Result<ScenarioMetrics, BenchmarkError> {
        let mut metrics = ScenarioMetrics::default();
        let start_time = SystemTime::now();

        // Generate and process transactions at baseline rate
        let baseline_transactions = self.generate_transactions(*baseline_tps, *duration - *surge_duration).await?;
        metrics.transaction_count += baseline_transactions.len() as u64;
        metrics.validation_time += self.process_transactions(&baseline_transactions).await;

        // Generate and process transactions at peak rate
        let peak_transactions = self.generate_transactions(*peak_tps, *surge_duration).await?;
        metrics.transaction_count += peak_transactions.len() as u64;
        metrics.validation_time += self.process_transactions(&peak_transactions).await;

        let end_time = SystemTime::now();
        let duration = end_time.duration_since(start_time)?;

        metrics.tps = (metrics.transaction_count as f64 / duration.as_secs_f64()) as u64;
        metrics.latency = metrics.validation_time / metrics.transaction_count;

        Ok(metrics)
    }

    async fn run_ramp_up_scenario(&self, start_tps: &u64, end_tps: &u64, duration: &u64) -> Result<ScenarioMetrics, BenchmarkError> {
        let mut metrics = ScenarioMetrics::default();
        let start_time = SystemTime::now();
        let total_transactions = (start_tps + end_tps) * duration / 2;

        // Generate and process transactions with increasing rate
        for i in 0..*duration {
            let current_tps = start_tps + (end_tps - start_tps) * i / duration;
            let transactions = self.generate_transactions(current_tps, 1).await?;
            metrics.transaction_count += transactions.len() as u64;
            metrics.validation_time += self.process_transactions(&transactions).await;
        }

        let end_time = SystemTime::now();
        let duration = end_time.duration_since(start_time)?;

        metrics.tps = (metrics.transaction_count as f64 / duration.as_secs_f64()) as u64;
        metrics.latency = metrics.validation_time / metrics.transaction_count;

        Ok(metrics)
    }

    async fn run_real_world_scenario(&self, transaction_profiles: &[TransactionProfile], duration: &u64) -> Result<ScenarioMetrics, BenchmarkError> {
        let mut metrics = ScenarioMetrics::default();
        let start_time = SystemTime::now();

        // Generate and process transactions based on profiles
        for profile in transaction_profiles {
            let transactions = self.generate_profile_transactions(profile, *duration).await?;
            metrics.transaction_count += transactions.len() as u64;
            metrics.validation_time += self.process_transactions(&transactions).await;
        }

        let end_time = SystemTime::now();
        let duration = end_time.duration_since(start_time)?;

        metrics.tps = (metrics.transaction_count as f64 / duration.as_secs_f64()) as u64;
        metrics.latency = metrics.validation_time / metrics.transaction_count;

        Ok(metrics)
    }

    async fn run_network_partition_scenario(&self, partition_duration: &u64, partition_size: &f64, recovery_time: &u64) -> Result<ScenarioMetrics, BenchmarkError> {
        let mut metrics = ScenarioMetrics::default();
        let start_time = SystemTime::now();

        // Simulate network partition
        let partition_size = (self.config.node_count as f64 * partition_size) as u64;
        let partition_nodes = self.select_partition_nodes(partition_size);

        // Generate and process transactions in partitioned network
        let transactions = self.generate_transactions(self.config.num_transactions, *partition_duration).await?;
        metrics.transaction_count += transactions.len() as u64;
        metrics.validation_time += self.process_transactions_in_partition(&partition_nodes, &transactions).await;

        // Simulate recovery
        tokio::time::sleep(Duration::from_secs(*recovery_time)).await;

        let end_time = SystemTime::now();
        let duration = end_time.duration_since(start_time)?;

        metrics.tps = (metrics.transaction_count as f64 / duration.as_secs_f64()) as u64;
        metrics.latency = metrics.validation_time / metrics.transaction_count;

        Ok(metrics)
    }

    async fn run_hardware_failure_scenario(&self, failure_rate: &f64, recovery_time: &u64) -> Result<ScenarioMetrics, BenchmarkError> {
        let mut metrics = ScenarioMetrics::default();
        let start_time = SystemTime::now();

        // Simulate hardware failures
        let failed_nodes = self.simulate_hardware_failures(*failure_rate);

        // Generate and process transactions with failed nodes
        let transactions = self.generate_transactions(self.config.num_transactions, *recovery_time).await?;
        metrics.transaction_count += transactions.len() as u64;
        metrics.validation_time += self.process_transactions_with_failures(&failed_nodes, &transactions).await;

        let end_time = SystemTime::now();
        let duration = end_time.duration_since(start_time)?;

        metrics.tps = (metrics.transaction_count as f64 / duration.as_secs_f64()) as u64;
        metrics.latency = metrics.validation_time / metrics.transaction_count;

        Ok(metrics)
    }

    async fn generate_transactions(&self, tps: u64, duration: u64) -> Result<Vec<Transaction>, BenchmarkError> {
        let mut transactions = Vec::new();
        let mut rng = rand::thread_rng();

        for i in 0..(tps * duration) {
            let tx = Transaction {
                sender: format!("sender_{}", rng.gen::<u64>() % self.config.node_count),
                recipient: format!("recipient_{}", rng.gen::<u64>() % self.config.node_count),
                amount: rng.gen::<u64>() % 1000000,
                signature: format!("signature_{}", rng.gen::<u64>()),
                nonce: rng.gen::<u64>(),
            };
            transactions.push(tx);
        }

        Ok(transactions)
    }

    async fn generate_profile_transactions(&self, profile: &TransactionProfile, duration: u64) -> Result<Vec<Transaction>, BenchmarkError> {
        let mut transactions = Vec::new();
        let mut rng = rand::thread_rng();

        for i in 0..(profile.frequency * duration) {
            let tx = Transaction {
                sender: format!("sender_{}", rng.gen::<u64>() % self.config.node_count),
                recipient: format!("recipient_{}", rng.gen::<u64>() % self.config.node_count),
                amount: rng.gen::<u64>() % 1000000,
                signature: format!("signature_{}", rng.gen::<u64>()),
                nonce: rng.gen::<u64>(),
            };
            transactions.push(tx);
        }

        Ok(transactions)
    }

    async fn process_transactions(&self, transactions: &[Transaction]) -> u64 {
        let mut total_time = 0;
        for tx in transactions {
            let start = SystemTime::now();
            self.process_transaction(tx).await;
            let end = SystemTime::now();
            total_time += end.duration_since(start).unwrap().as_nanos() as u64;
        }
        total_time
    }

    async fn process_transactions_in_partition(&self, partition_nodes: &[u64], transactions: &[Transaction]) -> u64 {
        let mut total_time = 0;
        for tx in transactions {
            let start = SystemTime::now();
            self.process_transaction_in_partition(partition_nodes, tx).await;
            let end = SystemTime::now();
            total_time += end.duration_since(start).unwrap().as_nanos() as u64;
        }
        total_time
    }

    async fn process_transactions_with_failures(&self, failed_nodes: &[u64], transactions: &[Transaction]) -> u64 {
        let mut total_time = 0;
        for tx in transactions {
            let start = SystemTime::now();
            self.process_transaction_with_failures(failed_nodes, tx).await;
            let end = SystemTime::now();
            total_time += end.duration_since(start).unwrap().as_nanos() as u64;
        }
        total_time
    }

    pub async fn process_transaction(&self, tx: &Transaction) {
        // Simulate transaction processing
        tokio::time::sleep(Duration::from_millis(10)).await;
    }

    pub async fn process_transaction_in_partition(&self, partition_nodes: &[u64], tx: &Transaction) {
        // Simulate transaction processing in partitioned network
        tokio::time::sleep(Duration::from_millis(100)).await;
    }

    pub async fn process_transaction_with_failures(&self, failed_nodes: &[u64], tx: &Transaction) {
        // Simulate transaction processing with failed nodes
        tokio::time::sleep(Duration::from_millis(50)).await;
    }

    fn select_partition_nodes(&self, partition_size: u64) -> Vec<u64> {
        let mut nodes = Vec::new();
        let mut rng = rand::thread_rng();
        for _ in 0..partition_size {
            nodes.push(rng.gen::<u64>() % self.config.node_count);
        }
        nodes
    }

    fn simulate_hardware_failures(&self, failure_rate: f64) -> Vec<u64> {
        let mut failed_nodes = Vec::new();
        let mut rng = rand::thread_rng();
        for i in 0..self.config.node_count {
            if rng.gen::<f64>() < failure_rate {
                failed_nodes.push(i);
            }
        }
        failed_nodes
    }

    pub async fn get_metrics(&self) -> BenchmarkMetrics {
        self.metrics.read().await.clone()
    }
}
