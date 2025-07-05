use std::collections::HashMap;
use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::{RwLock, mpsc};
use libp2p::PeerId;
use anyhow::Result;
use prometheus::{Histogram, HistogramOpts, register_histogram};
use crate::network::validation_types::ValidationType;
use crate::network::validation_worker::{ValidationWorker, WorkerType};

#[derive(Debug, Clone)]
pub struct ValidationWorkerStats {
    pub worker_id: String,
    pub worker_type: WorkerType,
    pub last_heartbeat: SystemTime,
    pub success_count: u64,
    pub error_count: u64,
    pub latency: Duration,
    pub load: f64,
    pub health_score: f64,
    pub validation_type: ValidationType,
    pub ai_score: Option<f64>,
    pub reputation_score: Option<f64>,
}

#[derive(Debug, Clone)]
pub struct ValidationWorkerHealth {
    pub worker_id: String,
    pub worker_type: WorkerType,
    pub status: WorkerStatus,
    pub last_error: Option<String>,
    pub error_type: Option<ErrorType>,
    pub error_count: u32,
    pub consecutive_errors: u32,
    pub last_success: Option<SystemTime>,
    pub validation_type: ValidationType,
}

#[derive(Debug, Clone, Copy)]
pub enum WorkerStatus {
    Healthy,
    Warning,
    Unhealthy,
    Offline,
}

#[derive(Debug, Clone, Copy)]
pub enum ErrorType {
    Validation,
    Network,
    Timeout,
    Resource,
    Other,
}

pub struct ValidationWorkerMonitor {
    workers: Arc<RwLock<HashMap<String, ValidationWorkerStats>>>,
    health: Arc<RwLock<HashMap<String, ValidationWorkerHealth>>>,
    config: MonitorConfig,
    metrics: Arc<MonitorMetrics>,
    heartbeat_queue: mpsc::Sender<ValidationWorkerStats>,
}

struct MonitorConfig {
    pub heartbeat_interval: Duration,
    pub max_consecutive_errors: u32,
    pub health_threshold: f64,
    pub warning_threshold: f64,
    pub offline_threshold: Duration,
    pub max_workers: usize,
}

struct MonitorMetrics {
    worker_count: Histogram,
    success_rate: Histogram,
    error_rate: Histogram,
    latency: Histogram,
    health_score: Histogram,
    offline_workers: Histogram,
}

impl MonitorMetrics {
    pub fn new() -> Result<Arc<Self>> {
        let opts = HistogramOpts::new("worker_count", "Number of active workers");
        let worker_count = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("success_rate", "Worker success rate");
        let success_rate = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("error_rate", "Worker error rate");
        let error_rate = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("latency", "Worker processing latency");
        let latency = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("health_score", "Worker health score");
        let health_score = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("offline_workers", "Number of offline workers");
        let offline_workers = register_histogram!(opts.clone())?;
        
        Ok(Arc::new(Self {
            worker_count,
            success_rate,
            error_rate,
            latency,
            health_score,
            offline_workers,
        }))
    }
}

impl ValidationWorkerMonitor {
    pub fn new(config: MonitorConfig) -> Result<Self> {
        let (tx, rx) = mpsc::channel(config.max_workers);
        let metrics = MonitorMetrics::new()?;
        
        let monitor = Self {
            workers: Arc::new(RwLock::new(HashMap::new())),
            health: Arc::new(RwLock::new(HashMap::new())),
            config,
            metrics,
            heartbeat_queue: tx,
        };

        // Start monitoring tasks
        monitor.start_monitoring(rx);
        monitor.start_health_check();

        Ok(monitor)
    }

    pub async fn register_validation_worker(
        &self,
        worker_id: String,
        worker_type: WorkerType,
        validation_type: ValidationType,
    ) -> Result<()> {
        let mut workers = self.workers.write().await;
        let mut health = self.health.write().await;

        if workers.len() >= self.config.max_workers {
            return Err(anyhow::anyhow!("Maximum validation workers reached"));
        }

        workers.insert(worker_id.clone(), ValidationWorkerStats {
            worker_id: worker_id.clone(),
            worker_type,
            last_heartbeat: SystemTime::now(),
            success_count: 0,
            error_count: 0,
            latency: Duration::from_secs(0),
            load: 0.0,
            health_score: 1.0,
            validation_type,
            ai_score: None,
            reputation_score: None,
        });

        health.insert(worker_id, ValidationWorkerHealth {
            worker_id: worker_id.clone(),
            worker_type,
            status: WorkerStatus::Healthy,
            last_error: None,
            error_type: None,
            error_count: 0,
            consecutive_errors: 0,
            last_success: Some(SystemTime::now()),
            validation_type,
        });

        self.metrics.worker_count.observe(workers.len() as f64);
        Ok(())
    }

    pub async fn report_validation_heartbeat(&self, stats: ValidationWorkerStats) -> Result<()> {
        self.heartbeat_queue.send(stats).await?;
        Ok(())
    }

    async fn update_worker_health(&self, stats: &ValidationWorkerStats) {
        let mut health = self.health.write().await;

        if let Some(health) = health.get_mut(&stats.worker_id) {
            let error_rate = stats.error_count as f64 / (stats.success_count + stats.error_count) as f64;
            let success_rate = stats.success_count as f64 / (stats.success_count + stats.error_count) as f64;
            
            // Calculate health score with AI and reputation factors
            let health_score = success_rate * 0.4 + 
                              (1.0 - error_rate) * 0.3 +
                              (1.0 - stats.latency.as_secs_f64() / 60.0) * 0.2 +
                              stats.ai_score.unwrap_or(0.0) * 0.1 +
                              stats.reputation_score.unwrap_or(0.0) * 0.1;

            // Update status based on thresholds
            if health_score < self.config.health_threshold {
                health.status = WorkerStatus::Unhealthy;
            } else if health_score < self.config.warning_threshold {
                health.status = WorkerStatus::Warning;
            } else {
                health.status = WorkerStatus::Healthy;
            }

            // Update metrics
            self.metrics.health_score.observe(health_score);
            self.metrics.success_rate.observe(success_rate);
            self.metrics.error_rate.observe(error_rate);
            self.metrics.ai_score.observe(stats.ai_score.unwrap_or(0.0));
            self.metrics.reputation_score.observe(stats.reputation_score.unwrap_or(0.0));
        }
    }

    pub async fn register_worker(&self, worker_id: String) -> Result<()> {
        let mut workers = self.workers.write().await;
        let mut health = self.health.write().await;

        if workers.len() >= self.config.max_workers {
            return Err(anyhow::anyhow!("Maximum workers reached"));
        }

        workers.insert(worker_id.clone(), WorkerStats {
            worker_id: worker_id.clone(),
            last_heartbeat: SystemTime::now(),
            success_count: 0,
            error_count: 0,
            latency: Duration::from_secs(0),
            load: 0.0,
            health_score: 1.0,
        });

        health.insert(worker_id, WorkerHealth {
            worker_id: worker_id.clone(),
            status: WorkerStatus::Healthy,
            last_error: None,
            error_type: None,
            error_count: 0,
            consecutive_errors: 0,
            last_success: Some(SystemTime::now()),
        });

        self.metrics.worker_count.observe(workers.len() as f64);
        Ok(())
    }

    pub async fn report_heartbeat(&self, stats: WorkerStats) -> Result<()> {
        self.heartbeat_queue.send(stats).await?;
        Ok(())
    }

    async fn start_monitoring(&self, mut rx: mpsc::Receiver<WorkerStats>) {
        tokio::spawn(async move {
            let mut workers = self.workers.clone();
            let mut health = self.health.clone();
            
            while let Some(stats) = rx.recv().await {
                self.update_worker_stats(&stats).await;
                self.update_worker_health(&stats).await;
            }
        });
    }

    async fn start_health_check(&self) {
        tokio::spawn(async move {
            let mut interval = tokio::time::interval(self.config.heartbeat_interval);
            let workers = self.workers.clone();
            let health = self.health.clone();
            
            loop {
                interval.tick().await;
                self.check_worker_health().await;
            }
        });
    }

    async fn update_worker_stats(&self, stats: &WorkerStats) {
        let mut workers = self.workers.write().await;
        let mut health = self.health.write().await;

        if let Some(worker) = workers.get_mut(&stats.worker_id) {
            worker.last_heartbeat = stats.last_heartbeat;
            worker.latency = stats.latency;
            worker.load = stats.load;
            
            // Update metrics
            self.metrics.latency.observe(stats.latency.as_secs_f64());
            self.metrics.load.observe(stats.load);
        }

        if let Some(health) = health.get_mut(&stats.worker_id) {
            health.last_success = Some(stats.last_heartbeat);
            health.consecutive_errors = 0;
            health.status = WorkerStatus::Healthy;
        }
    }

    async fn update_worker_health(&self, stats: &WorkerStats) {
        let mut health = self.health.write().await;

        if let Some(health) = health.get_mut(&stats.worker_id) {
            let error_rate = stats.error_count as f64 / (stats.success_count + stats.error_count) as f64;
            let success_rate = stats.success_count as f64 / (stats.success_count + stats.error_count) as f64;
            
            // Calculate health score
            let health_score = success_rate * 0.7 + 
                              (1.0 - error_rate) * 0.2 +
                              (1.0 - stats.latency.as_secs_f64() / 60.0) * 0.1;

            // Update status based on thresholds
            if health_score < self.config.health_threshold {
                health.status = WorkerStatus::Unhealthy;
            } else if health_score < self.config.warning_threshold {
                health.status = WorkerStatus::Warning;
            } else {
                health.status = WorkerStatus::Healthy;
            }

            // Update metrics
            self.metrics.health_score.observe(health_score);
            self.metrics.success_rate.observe(success_rate);
            self.metrics.error_rate.observe(error_rate);
        }
    }

    async fn check_worker_health(&self) {
        let now = SystemTime::now();
        let mut health = self.health.write().await;
        let mut workers = self.workers.write().await;
        let mut offline_count = 0;

        for (id, worker) in workers.iter_mut() {
            let duration = now.duration_since(worker.last_heartbeat).unwrap_or(Duration::from_secs(0));
            
            if duration > self.config.offline_threshold {
                if let Some(health) = health.get_mut(id) {
                    health.status = WorkerStatus::Offline;
                    offline_count += 1;
                }
            }
        }

        self.metrics.offline_workers.observe(offline_count as f64);
    }

    pub async fn get_worker_stats(&self) -> Vec<WorkerStats> {
        let workers = self.workers.read().await;
        workers.values().cloned().collect()
    }

    pub async fn get_worker_health(&self) -> Vec<WorkerHealth> {
        let health = self.health.read().await;
        health.values().cloned().collect()
    }

    pub async fn get_stats(&self) -> MonitorStats {
        let workers = self.workers.read().await;
        let health = self.health.read().await;
        
        let total_workers = workers.len();
        let healthy_workers = health.values()
            .filter(|h| matches!(h.status, WorkerStatus::Healthy))
            .count();
        
        let warning_workers = health.values()
            .filter(|h| matches!(h.status, WorkerStatus::Warning))
            .count();
        
        let unhealthy_workers = health.values()
            .filter(|h| matches!(h.status, WorkerStatus::Unhealthy))
            .count();
        
        let offline_workers = health.values()
            .filter(|h| matches!(h.status, WorkerStatus::Offline))
            .count();

        MonitorStats {
            total_workers,
            healthy_workers,
            warning_workers,
            unhealthy_workers,
            offline_workers,
            max_workers: self.config.max_workers,
            heartbeat_interval: self.config.heartbeat_interval.as_secs(),
            health_threshold: self.config.health_threshold,
            warning_threshold: self.config.warning_threshold,
            offline_threshold: self.config.offline_threshold.as_secs(),
        }
    }
}

#[derive(Debug, Clone)]
pub struct MonitorStats {
    pub total_workers: usize,
    pub healthy_workers: usize,
    pub warning_workers: usize,
    pub unhealthy_workers: usize,
    pub offline_workers: usize,
    pub max_workers: usize,
    pub heartbeat_interval: u64,
    pub health_threshold: f64,
    pub warning_threshold: f64,
    pub offline_threshold: u64,
}
