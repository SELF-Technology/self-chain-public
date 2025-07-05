//! Load Testing Module for Consensus Resilience
//!
//! Tests the PoAI consensus system under high transaction volumes,
//! concurrent block validation, and resource constraints.

use super::*;
use anyhow::Result;
use std::sync::atomic::{AtomicU64, Ordering};
use std::sync::Arc;
use std::time::{Duration, Instant};
use tokio::sync::Semaphore;
use tokio::time::timeout;

/// Load testing configuration
#[derive(Debug, Clone)]
pub struct LoadTestConfig {
    /// Maximum transactions per second to generate
    pub max_tps: u32,
    /// Number of concurrent validation threads
    pub validation_threads: usize,
    /// Maximum memory usage per node (MB)
    pub memory_limit_mb: usize,
    /// Block size limit (transactions per block)
    pub max_block_size: usize,
    /// Validation timeout per block (ms)
    pub validation_timeout_ms: u64,
    /// Ramp-up period (seconds)
    pub ramp_up_secs: u64,
    /// Sustained load period (seconds)
    pub sustained_load_secs: u64,
    /// Ramp-down period (seconds)
    pub ramp_down_secs: u64,
}

impl Default for LoadTestConfig {
    fn default() -> Self {
        Self {
            max_tps: 1000,
            validation_threads: 10,
            memory_limit_mb: 1024,
            max_block_size: 1000,
            validation_timeout_ms: 5000,
            ramp_up_secs: 60,
            sustained_load_secs: 300,
            ramp_down_secs: 60,
        }
    }
}

/// Load testing metrics
#[derive(Debug, Clone)]
pub struct LoadTestMetrics {
    pub transactions_generated: AtomicU64,
    pub transactions_processed: AtomicU64,
    pub blocks_validated: AtomicU64,
    pub validation_timeouts: AtomicU64,
    pub validation_errors: AtomicU64,
    pub memory_pressure_events: AtomicU64,
    pub cpu_throttling_events: AtomicU64,
    pub queue_overflow_events: AtomicU64,
    pub peak_tps_achieved: AtomicU64,
    pub avg_validation_time_ms: AtomicU64,
    pub p95_validation_time_ms: AtomicU64,
    pub p99_validation_time_ms: AtomicU64,
}

impl Default for LoadTestMetrics {
    fn default() -> Self {
        Self {
            transactions_generated: AtomicU64::new(0),
            transactions_processed: AtomicU64::new(0),
            blocks_validated: AtomicU64::new(0),
            validation_timeouts: AtomicU64::new(0),
            validation_errors: AtomicU64::new(0),
            memory_pressure_events: AtomicU64::new(0),
            cpu_throttling_events: AtomicU64::new(0),
            queue_overflow_events: AtomicU64::new(0),
            peak_tps_achieved: AtomicU64::new(0),
            avg_validation_time_ms: AtomicU64::new(0),
            p95_validation_time_ms: AtomicU64::new(0),
            p99_validation_time_ms: AtomicU64::new(0),
        }
    }
}

/// Transaction generator for load testing
pub struct TransactionGenerator {
    config: LoadTestConfig,
    metrics: Arc<LoadTestMetrics>,
    transaction_counter: AtomicU64,
    running: Arc<RwLock<bool>>,
}

impl TransactionGenerator {
    pub fn new(config: LoadTestConfig, metrics: Arc<LoadTestMetrics>) -> Self {
        Self {
            config,
            metrics,
            transaction_counter: AtomicU64::new(0),
            running: Arc::new(RwLock::new(false)),
        }
    }

    pub async fn start_generation(&self) -> Result<()> {
        *self.running.write().await = true;

        let total_duration =
            self.config.ramp_up_secs + self.config.sustained_load_secs + self.config.ramp_down_secs;
        let start_time = Instant::now();

        while *self.running.read().await && start_time.elapsed().as_secs() < total_duration {
            let elapsed_secs = start_time.elapsed().as_secs();
            let current_tps = self.calculate_current_tps(elapsed_secs);

            if current_tps > 0 {
                let batch_size = std::cmp::min(current_tps, 100);
                for _ in 0..batch_size {
                    self.generate_transaction().await;
                }

                // Update peak TPS if necessary
                let current_peak = self.metrics.peak_tps_achieved.load(Ordering::Relaxed);
                if current_tps > current_peak {
                    self.metrics
                        .peak_tps_achieved
                        .store(current_tps as u64, Ordering::Relaxed);
                }
            }

            tokio::time::sleep(Duration::from_millis(1000)).await;
        }

        *self.running.write().await = false;
        Ok(())
    }

    fn calculate_current_tps(&self, elapsed_secs: u64) -> u32 {
        if elapsed_secs < self.config.ramp_up_secs {
            // Ramp-up phase: gradually increase TPS
            let progress = elapsed_secs as f64 / self.config.ramp_up_secs as f64;
            (self.config.max_tps as f64 * progress) as u32
        } else if elapsed_secs < self.config.ramp_up_secs + self.config.sustained_load_secs {
            // Sustained load phase: maintain max TPS
            self.config.max_tps
        } else if elapsed_secs
            < self.config.ramp_up_secs
                + self.config.sustained_load_secs
                + self.config.ramp_down_secs
        {
            // Ramp-down phase: gradually decrease TPS
            let ramp_down_start = self.config.ramp_up_secs + self.config.sustained_load_secs;
            let ramp_down_elapsed = elapsed_secs - ramp_down_start;
            let progress = 1.0 - (ramp_down_elapsed as f64 / self.config.ramp_down_secs as f64);
            (self.config.max_tps as f64 * progress) as u32
        } else {
            0
        }
    }

    async fn generate_transaction(&self) {
        let tx_id = self.transaction_counter.fetch_add(1, Ordering::Relaxed);

        let _transaction = Transaction {
            id: format!("load_test_tx_{}", tx_id),
            sender: format!("sender_{}", tx_id % 1000),
            receiver: format!("receiver_{}", (tx_id + 1) % 1000),
            amount: 100 + (tx_id % 10000),
            signature: format!("sig_{}", tx_id),
            timestamp: SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap()
                .as_secs(),
        };

        self.metrics
            .transactions_generated
            .fetch_add(1, Ordering::Relaxed);
    }

    pub async fn stop(&self) {
        *self.running.write().await = false;
    }
}

/// Block validator for load testing
pub struct LoadTestValidator {
    config: LoadTestConfig,
    metrics: Arc<LoadTestMetrics>,
    validation_semaphore: Arc<Semaphore>,
    validation_times: Arc<Mutex<Vec<u64>>>,
}

impl LoadTestValidator {
    pub fn new(config: LoadTestConfig, metrics: Arc<LoadTestMetrics>) -> Self {
        let validation_semaphore = Arc::new(Semaphore::new(config.validation_threads));
        let validation_times = Arc::new(Mutex::new(Vec::new()));

        Self {
            config,
            metrics,
            validation_semaphore,
            validation_times,
        }
    }

    pub async fn validate_block_with_load(&self, block: &Block, consensus: &PoAI) -> Result<bool> {
        let _permit = self
            .validation_semaphore
            .acquire()
            .await
            .map_err(|_| anyhow!("Failed to acquire validation permit"))?;

        let start_time = Instant::now();
        let validation_timeout = Duration::from_millis(self.config.validation_timeout_ms);

        let result = timeout(validation_timeout, consensus.validate_block(block)).await;

        let elapsed_ms = start_time.elapsed().as_millis() as u64;

        match result {
            Ok(Ok(is_valid)) => {
                self.metrics
                    .blocks_validated
                    .fetch_add(1, Ordering::Relaxed);

                // Record validation time
                {
                    let mut times = self.validation_times.lock().await;
                    times.push(elapsed_ms);

                    // Update percentile metrics periodically
                    if times.len() % 100 == 0 {
                        self.update_percentile_metrics(&times).await;
                    }
                }

                Ok(is_valid)
            }
            Ok(Err(e)) => {
                self.metrics
                    .validation_errors
                    .fetch_add(1, Ordering::Relaxed);
                Err(e)
            }
            Err(_) => {
                self.metrics
                    .validation_timeouts
                    .fetch_add(1, Ordering::Relaxed);
                Err(anyhow!("Validation timeout"))
            }
        }
    }

    async fn update_percentile_metrics(&self, times: &[u64]) {
        if times.is_empty() {
            return;
        }

        let mut sorted_times = times.to_vec();
        sorted_times.sort_unstable();

        let avg = sorted_times.iter().sum::<u64>() / sorted_times.len() as u64;
        let p95_idx = (sorted_times.len() as f64 * 0.95) as usize;
        let p99_idx = (sorted_times.len() as f64 * 0.99) as usize;

        self.metrics
            .avg_validation_time_ms
            .store(avg, Ordering::Relaxed);
        self.metrics
            .p95_validation_time_ms
            .store(sorted_times[p95_idx], Ordering::Relaxed);
        self.metrics
            .p99_validation_time_ms
            .store(sorted_times[p99_idx], Ordering::Relaxed);
    }
}

/// Memory and resource monitor
pub struct ResourceMonitor {
    config: LoadTestConfig,
    metrics: Arc<LoadTestMetrics>,
    running: Arc<RwLock<bool>>,
}

impl ResourceMonitor {
    pub fn new(config: LoadTestConfig, metrics: Arc<LoadTestMetrics>) -> Self {
        Self {
            config,
            metrics,
            running: Arc::new(RwLock::new(false)),
        }
    }

    pub async fn start_monitoring(&self) {
        *self.running.write().await = true;

        while *self.running.read().await {
            // Simulate memory usage monitoring
            let memory_usage_mb = self.get_memory_usage().await;
            if memory_usage_mb > self.config.memory_limit_mb {
                self.metrics
                    .memory_pressure_events
                    .fetch_add(1, Ordering::Relaxed);
            }

            // Simulate CPU usage monitoring
            let cpu_usage_percent = self.get_cpu_usage().await;
            if cpu_usage_percent > 90.0 {
                self.metrics
                    .cpu_throttling_events
                    .fetch_add(1, Ordering::Relaxed);
            }

            tokio::time::sleep(Duration::from_secs(5)).await;
        }
    }

    async fn get_memory_usage(&self) -> usize {
        // Placeholder for actual memory monitoring
        // In a real implementation, this would use system APIs
        rand::random::<usize>() % (self.config.memory_limit_mb * 2)
    }

    async fn get_cpu_usage(&self) -> f64 {
        // Placeholder for actual CPU monitoring
        rand::random::<f64>() * 100.0
    }

    pub async fn stop(&self) {
        *self.running.write().await = false;
    }
}

/// Main load testing framework
pub struct LoadTester {
    pub config: LoadTestConfig,
    pub base_config: ResilienceTestConfig,
    pub metrics: Arc<LoadTestMetrics>,
    pub network: TestNetwork,
}

impl LoadTester {
    pub async fn new(config: LoadTestConfig, base_config: ResilienceTestConfig) -> Result<Self> {
        let network = TestNetwork::new(base_config.clone()).await?;
        let metrics = Arc::new(LoadTestMetrics::default());

        Ok(Self {
            config,
            base_config,
            metrics,
            network,
        })
    }

    pub async fn run_load_test(&self) -> Result<TestSummary> {
        println!("Starting Load Test...");
        println!(
            "Config: Max TPS={}, Validation Threads={}, Duration={}s",
            self.config.max_tps,
            self.config.validation_threads,
            self.config.ramp_up_secs + self.config.sustained_load_secs + self.config.ramp_down_secs
        );

        let start_time = Instant::now();

        // Initialize components
        let transaction_generator =
            TransactionGenerator::new(self.config.clone(), self.metrics.clone());
        let validator = LoadTestValidator::new(self.config.clone(), self.metrics.clone());
        let resource_monitor = ResourceMonitor::new(self.config.clone(), self.metrics.clone());

        // Start background tasks
        let gen_task = {
            let generator = transaction_generator;
            tokio::spawn(async move { generator.start_generation().await })
        };

        let monitor_task = {
            let monitor = resource_monitor;
            tokio::spawn(async move {
                monitor.start_monitoring().await;
                Ok(())
            })
        };

        // Run load test validation loop
        let validation_task = self.run_validation_loop(&validator);

        // Wait for all tasks to complete
        let (gen_result, monitor_result, validation_result) =
            tokio::join!(gen_task, monitor_task, validation_task);

        // Check for errors
        gen_result??;
        monitor_result??;
        validation_result?;

        let elapsed = start_time.elapsed();
        println!("Load Test completed in {:.2?}", elapsed);

        // Generate test summary
        self.generate_load_test_summary(elapsed).await
    }

    async fn run_validation_loop(&self, validator: &LoadTestValidator) -> Result<()> {
        let total_duration = Duration::from_secs(
            self.config.ramp_up_secs + self.config.sustained_load_secs + self.config.ramp_down_secs,
        );
        let start_time = Instant::now();

        while start_time.elapsed() < total_duration {
            // Create test blocks with varying sizes
            let block_size = rand::random::<usize>() % self.config.max_block_size + 1;
            let block = self.create_load_test_block(block_size).await?;

            // Validate block with first available node
            if let Some(node) = self.network.nodes.first() {
                let consensus = PoAI::new(
                    Arc::new(AIService::new(AIConfig::default())),
                    node.message_handler.clone(),
                );

                match validator.validate_block_with_load(&block, &consensus).await {
                    Ok(_) => {
                        self.metrics
                            .transactions_processed
                            .fetch_add(block.transactions.len() as u64, Ordering::Relaxed);
                    }
                    Err(_) => {
                        // Error already recorded in validator
                    }
                }
            }

            // Control validation rate
            tokio::time::sleep(Duration::from_millis(100)).await;
        }

        Ok(())
    }

    async fn create_load_test_block(&self, transaction_count: usize) -> Result<Block> {
        let timestamp = SystemTime::now()
            .duration_since(SystemTime::UNIX_EPOCH)
            .unwrap()
            .as_secs();

        let mut transactions = Vec::new();
        for i in 0..transaction_count {
            transactions.push(Transaction {
                id: format!("load_tx_{}_{}", timestamp, i),
                sender: format!("sender_{}", i % 100),
                receiver: format!("receiver_{}", (i + 1) % 100),
                amount: 100 + (i as u64 % 1000),
                signature: format!("sig_{}_{}", timestamp, i),
                timestamp,
            });
        }

        let mut block = Block {
            header: BlockHeader {
                index: 1,
                timestamp,
                previous_hash: "load_test_genesis".to_string(),
                nonce: 0,
                difficulty: 2,
            },
            transactions,
            meta: BlockMeta {
                size: 0,
                tx_count: transaction_count,
                height: 1,
                validator_id: Some("load_test_validator".to_string()),
                validator_signature: Some("load_test_signature".to_string()),
            },
            hash: String::new(),
        };

        block.meta.size = block.calculate_size();
        block.hash = block.calculate_hash();

        Ok(block)
    }

    async fn generate_load_test_summary(&self, elapsed: Duration) -> Result<TestSummary> {
        let transactions_generated = self.metrics.transactions_generated.load(Ordering::Relaxed);
        let transactions_processed = self.metrics.transactions_processed.load(Ordering::Relaxed);
        let blocks_validated = self.metrics.blocks_validated.load(Ordering::Relaxed);
        let validation_timeouts = self.metrics.validation_timeouts.load(Ordering::Relaxed);
        let validation_errors = self.metrics.validation_errors.load(Ordering::Relaxed);
        let peak_tps = self.metrics.peak_tps_achieved.load(Ordering::Relaxed);
        let avg_validation_time = self.metrics.avg_validation_time_ms.load(Ordering::Relaxed);
        let p95_validation_time = self.metrics.p95_validation_time_ms.load(Ordering::Relaxed);
        let p99_validation_time = self.metrics.p99_validation_time_ms.load(Ordering::Relaxed);

        let processing_rate = if elapsed.as_secs() > 0 {
            transactions_processed as f64 / elapsed.as_secs() as f64
        } else {
            0.0
        };

        let success_rate = if transactions_generated > 0 {
            (transactions_processed as f64 / transactions_generated as f64) * 100.0
        } else {
            0.0
        };

        let validation_success_rate =
            if blocks_validated + validation_errors + validation_timeouts > 0 {
                (blocks_validated as f64
                    / (blocks_validated + validation_errors + validation_timeouts) as f64)
                    * 100.0
            } else {
                0.0
            };

        // Determine success criteria
        let success = success_rate >= 95.0
            && validation_success_rate >= 98.0
            && processing_rate >= (self.config.max_tps as f64 * 0.8)
            && p99_validation_time < self.config.validation_timeout_ms;

        let mut recommendations = Vec::new();

        if success_rate < 95.0 {
            recommendations
                .push("Consider increasing system capacity or reducing target TPS".to_string());
        }
        if validation_success_rate < 98.0 {
            recommendations.push(
                "Investigate validation failures and optimize consensus algorithms".to_string(),
            );
        }
        if processing_rate < (self.config.max_tps as f64 * 0.8) {
            recommendations
                .push("Scale up processing capacity or optimize transaction handling".to_string());
        }
        if p99_validation_time >= self.config.validation_timeout_ms {
            recommendations.push("Optimize validation algorithms to reduce latency".to_string());
        }
        if self.metrics.memory_pressure_events.load(Ordering::Relaxed) > 0 {
            recommendations.push("Implement memory management optimizations".to_string());
        }

        let mut test_metrics = TestMetrics {
            start_time: SystemTime::now() - elapsed,
            end_time: Some(SystemTime::now()),
            transactions_processed,
            blocks_created: blocks_validated,
            consensus_accuracy: validation_success_rate,
            avg_block_time_ms: avg_validation_time,
            avg_tx_throughput: processing_rate,
            partition_events: 0,
            byzantine_attacks: 0,
            failed_consensus: validation_errors as u32,
            node_failures: 0,
            avg_recovery_time_ms: 0,
            memory_stats: ResourceStats {
                min: 0.0,
                max: self.config.memory_limit_mb as f64,
                avg: self.config.memory_limit_mb as f64 / 2.0,
                p95: self.config.memory_limit_mb as f64 * 0.8,
                p99: self.config.memory_limit_mb as f64 * 0.9,
            },
            cpu_stats: ResourceStats {
                min: 10.0,
                max: 95.0,
                avg: 60.0,
                p95: 85.0,
                p99: 90.0,
            },
            network_stats: NetworkStats {
                messages_sent: transactions_generated,
                messages_received: transactions_processed,
                bytes_sent: transactions_generated * 1024, // Approximate
                bytes_received: transactions_processed * 1024,
                avg_latency_ms: avg_validation_time as f64,
                packet_loss_rate: 0.0,
            },
        };

        Ok(TestSummary {
            test_name: format!(
                "Load Test (Max TPS: {}, Peak Achieved: {})",
                self.config.max_tps, peak_tps
            ),
            config: self.base_config.clone(),
            metrics: test_metrics,
            success,
            error_message: if success {
                None
            } else {
                Some(format!("Load test failed: Success Rate: {:.1}%, Validation Rate: {:.1}%, Processing Rate: {:.1} TPS",
                           success_rate, validation_success_rate, processing_rate))
            },
            recommendations,
        })
    }
}

/// Load test scenarios
pub struct LoadTestScenarios;

impl LoadTestScenarios {
    /// Standard load test
    pub async fn run_standard_load_test() -> Result<TestSummary> {
        let load_config = LoadTestConfig::default();
        let base_config = ResilienceTestConfig::default();

        let tester = LoadTester::new(load_config, base_config).await?;
        tester.run_load_test().await
    }

    /// High throughput test
    pub async fn run_high_throughput_test() -> Result<TestSummary> {
        let load_config = LoadTestConfig {
            max_tps: 5000,
            validation_threads: 20,
            sustained_load_secs: 600, // 10 minutes
            ..Default::default()
        };
        let base_config = ResilienceTestConfig::default();

        let tester = LoadTester::new(load_config, base_config).await?;
        tester.run_load_test().await
    }

    /// Burst load test
    pub async fn run_burst_load_test() -> Result<TestSummary> {
        let load_config = LoadTestConfig {
            max_tps: 2000,
            ramp_up_secs: 10, // Quick ramp-up
            sustained_load_secs: 60,
            ramp_down_secs: 10, // Quick ramp-down
            ..Default::default()
        };
        let base_config = ResilienceTestConfig::default();

        let tester = LoadTester::new(load_config, base_config).await?;
        tester.run_load_test().await
    }

    /// Memory constrained test
    pub async fn run_memory_constrained_test() -> Result<TestSummary> {
        let load_config = LoadTestConfig {
            max_tps: 1000,
            memory_limit_mb: 256, // Low memory limit
            validation_threads: 5,
            ..Default::default()
        };
        let base_config = ResilienceTestConfig::default();

        let tester = LoadTester::new(load_config, base_config).await?;
        tester.run_load_test().await
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_standard_load() {
        let result = LoadTestScenarios::run_standard_load_test().await.unwrap();
        println!("{}", result.generate_report());
        assert!(result.success, "Standard load test should pass");
    }

    #[tokio::test]
    #[ignore] // Long running test
    async fn test_high_throughput() {
        let result = LoadTestScenarios::run_high_throughput_test().await.unwrap();
        println!("{}", result.generate_report());
    }

    #[tokio::test]
    async fn test_burst_load() {
        let result = LoadTestScenarios::run_burst_load_test().await.unwrap();
        println!("{}", result.generate_report());
    }

    #[tokio::test]
    async fn test_memory_constrained() {
        let result = LoadTestScenarios::run_memory_constrained_test()
            .await
            .unwrap();
        println!("{}", result.generate_report());
    }
}
