use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::RwLock;
use serde::{Deserialize, Serialize};
use rand::Rng;
use crate::blockchain::block::{Block, Transaction};
use crate::network::communication::NodeCommunicator;
use crate::grid::compute::GridCompute;
use crate::monitoring::performance::PerformanceMonitor;

#[derive(Debug, Serialize, Deserialize)]
pub struct BenchmarkConfig {
    pub num_transactions: u64,
    pub batch_size: usize,
    pub num_shards: u64,
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

#[derive(Debug, Serialize, Deserialize)]
pub struct BenchmarkMetrics {
    pub total_transactions: u64,
    pub successful_transactions: u64,
    pub failed_transactions: u64,
    pub avg_tps: u64,
    pub peak_tps: u64,
    pub avg_latency: u64, // in ms
    pub peak_latency: u64, // in ms
    pub memory_usage: u64,
    pub cpu_usage: f64,
    pub network_bandwidth: u64, // in bytes/s
    pub validation_time: u64, // in ns
    pub block_time: u64, // in ns
    pub cache_hits: u64,
    pub cache_misses: u64,
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
            config,
            metrics: Arc::new(RwLock::new(BenchmarkMetrics {
                total_transactions: 0,
                successful_transactions: 0,
                failed_transactions: 0,
                avg_tps: 0,
                peak_tps: 0,
                avg_latency: 0,
                peak_latency: 0,
                memory_usage: 0,
                cpu_usage: 0.0,
                network_bandwidth: 0,
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

    pub async fn run(&self) -> BenchmarkMetrics {
        let start_time = SystemTime::now();
        let mut total_transactions = 0;
        let mut successful_transactions = 0;
        let mut failed_transactions = 0;
        let mut latencies = Vec::new();
        let mut tps_values = Vec::new();

        // Start performance monitoring
        self.performance_monitor.start().await.unwrap();

        // Run benchmark based on load pattern
        match self.config.load_pattern {
            LoadPattern::Constant => {
                self.run_constant_load().await;
            }
            LoadPattern::RampUp => {
                self.run_ramp_up_load().await;
            }
            LoadPattern::Spike => {
                self.run_spike_load().await;
            }
            LoadPattern::Random => {
                self.run_random_load().await;
            }
        }

        // Calculate metrics
        let duration = start_time.elapsed().unwrap().as_secs();
        let avg_tps = total_transactions / duration;
        let peak_tps = tps_values.iter().max().cloned().unwrap_or(0);
        let avg_latency = latencies.iter().sum::<u64>() / latencies.len() as u64;
        let peak_latency = latencies.iter().max().cloned().unwrap_or(0);

        // Get final metrics
        let metrics = self.performance_monitor.get_metrics().await;

        BenchmarkMetrics {
            total_transactions,
            successful_transactions,
            failed_transactions,
            avg_tps,
            peak_tps,
            avg_latency,
            peak_latency,
            memory_usage: metrics.memory_usage,
            cpu_usage: metrics.cpu_usage,
            network_bandwidth: metrics.network_bandwidth,
            validation_time: metrics.validation_time,
            block_time: metrics.block_time,
            cache_hits: metrics.cache_hits,
            cache_misses: metrics.cache_misses,
        }
    }

    async fn run_constant_load(&self) {
        let interval = Duration::from_secs(1) / self.config.num_transactions;
        let mut interval = tokio::time::interval(interval);
        
        for _ in 0..self.config.num_transactions {
            let tx = self.create_transaction();
            self.process_transaction(&tx).await;
            interval.tick().await;
        }
    }

    async fn run_ramp_up_load(&self) {
        let mut current_rate = 1;
        let max_rate = self.config.num_transactions;
        let step = max_rate / 10; // Ramp up in 10 steps
        
        for _ in 0..10 {
            for _ in 0..current_rate {
                let tx = self.create_transaction();
                self.process_transaction(&tx).await;
            }
            current_rate += step;
            tokio::time::sleep(Duration::from_secs(1)).await;
        }
    }

    async fn run_spike_load(&self) {
        let mut current_rate = 1;
        let max_rate = self.config.num_transactions;
        
        // Ramp up
        for i in 0..10 {
            current_rate = (max_rate as f64 * (i as f64 / 10.0)) as u64;
            for _ in 0..current_rate {
                let tx = self.create_transaction();
                self.process_transaction(&tx).await;
            }
            tokio::time::sleep(Duration::from_millis(100)).await;
        }

        // Spike
        for _ in 0..max_rate {
            let tx = self.create_transaction();
            self.process_transaction(&tx).await;
        }

        // Ramp down
        for i in (0..10).rev() {
            current_rate = (max_rate as f64 * (i as f64 / 10.0)) as u64;
            for _ in 0..current_rate {
                let tx = self.create_transaction();
                self.process_transaction(&tx).await;
            }
            tokio::time::sleep(Duration::from_millis(100)).await;
        }
    }

    async fn run_random_load(&self) {
        for _ in 0..self.config.num_transactions {
            let tx = self.create_transaction();
            self.process_transaction(&tx).await;
            let sleep_time = self.rng.gen_range(0..100);
            tokio::time::sleep(Duration::from_millis(sleep_time)).await;
        }
    }

    fn create_transaction(&self) -> Transaction {
        Transaction {
            sender: format!("sender_{}", self.rng.gen::<u64>()),
            recipient: format!("recipient_{}", self.rng.gen::<u64>()),
            amount: self.rng.gen::<u64>() % 1000000,
            signature: format!("signature_{}", self.rng.gen::<u64>()),
            timestamp: SystemTime::now().duration_since(SystemTime::UNIX_EPOCH).unwrap().as_secs(),
            nonce: self.rng.gen::<u64>(),
        }
    }

    async fn process_transaction(&self, tx: &Transaction) {
        let start = SystemTime::now();
        
        match self.grid_compute.add_transaction(tx.clone()).await {
            Ok(_) => {
                let latency = start.elapsed().unwrap().as_millis();
                self.metrics.write().await.successful_transactions += 1;
                self.metrics.write().await.latencies.push(latency);
            }
            Err(_) => {
                self.metrics.write().await.failed_transactions += 1;
            }
        }
    }

    pub async fn get_metrics(&self) -> BenchmarkMetrics {
        self.metrics.read().await.clone()
    }
}
