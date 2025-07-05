use std::collections::BinaryHeap;
use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::{RwLock, mpsc};
use libp2p::PeerId;
use anyhow::Result;
use priority_queue::PriorityQueue;
use prometheus::{Histogram, HistogramOpts, register_histogram};

#[derive(Debug, Clone)]
pub struct ValidationTask {
    pub peer_id: PeerId,
    pub priority: f64,
    pub timestamp: SystemTime,
    pub validation_type: ValidationType,
    pub context: ValidationContext,
}

#[derive(Debug, Clone, Copy)]
pub enum ValidationType {
    Certificate,
    Reputation,
    ResponseTime,
    Full,
}

#[derive(Debug, Clone)]
pub struct ValidationContext {
    pub retries: u32,
    pub duration: Duration,
    pub error_type: Option<ErrorType>,
    pub batch_size: usize,
    pub priority_score: f64,
}

#[derive(Debug, Clone, Copy)]
pub enum ErrorType {
    Certificate,
    Reputation,
    ResponseTime,
    Network,
    Timeout,
    Other,
}

pub struct ValidationPriorityQueue {
    queue: Arc<RwLock<PriorityQueue<ValidationTask, f64>>>,
    task_queue: mpsc::Sender<ValidationTask>,
    config: QueueConfig,
    metrics: Arc<QueueMetrics>,
    active_tasks: Arc<RwLock<HashMap<PeerId, ValidationTask>>>,
}

struct QueueConfig {
    pub max_queue_size: usize,
    pub priority_threshold: f64,
    pub timeout: Duration,
    pub cleanup_interval: Duration,
    pub max_retries: u32,
}

struct QueueMetrics {
    queue_size: Histogram,
    task_latency: Histogram,
    priority_distribution: Histogram,
    error_rate: Histogram,
    retry_count: Histogram,
}

impl QueueMetrics {
    pub fn new() -> Result<Arc<Self>> {
        let opts = HistogramOpts::new("queue_size", "Number of tasks in queue");
        let queue_size = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("task_latency", "Task processing latency");
        let task_latency = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("priority_distribution", "Task priority distribution");
        let priority_distribution = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("error_rate", "Task error rate");
        let error_rate = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("retry_count", "Task retry count");
        let retry_count = register_histogram!(opts.clone())?;
        
        Ok(Arc::new(Self {
            queue_size,
            task_latency,
            priority_distribution,
            error_rate,
            retry_count,
        }))
    }
}

impl ValidationPriorityQueue {
    pub fn new(config: QueueConfig) -> Result<Self> {
        let (tx, rx) = mpsc::channel(config.max_queue_size);
        let metrics = QueueMetrics::new()?;
        
        let queue = Self {
            queue: Arc::new(RwLock::new(PriorityQueue::new())),
            task_queue: tx,
            config,
            metrics,
            active_tasks: Arc::new(RwLock::new(HashMap::new())),
        };

        // Start queue management tasks
        queue.start_queue_manager(rx);
        queue.start_cleanup_task();

        Ok(queue)
    }

    pub async fn submit_task(&self, task: ValidationTask) -> Result<()> {
        self.task_queue.send(task).await?;
        self.metrics.queue_size.observe(self.queue.read().await.len() as f64);
        self.metrics.priority_distribution.observe(task.priority);
        Ok(())
    }

    async fn start_queue_manager(&self, mut rx: mpsc::Receiver<ValidationTask>) {
        tokio::spawn(async move {
            let mut queue = self.queue.clone();
            let mut active_tasks = self.active_tasks.clone();
            
            while let Some(task) = rx.recv().await {
                // Check if task exists and should be retried
                if let Some(existing) = active_tasks.read().await.get(&task.peer_id) {
                    if existing.context.retries >= self.config.max_retries {
                        continue;
                    }
                }

                // Add task to queue
                queue.write().await.push(task.clone(), task.priority);
                active_tasks.write().await.insert(task.peer_id.clone(), task);
            }
        });
    }

    async fn start_cleanup_task(&self) {
        tokio::spawn(async move {
            let mut interval = tokio::time::interval(self.config.cleanup_interval);
            let queue = self.queue.clone();
            let active_tasks = self.active_tasks.clone();
            
            loop {
                interval.tick().await;
                self.cleanup_old_tasks().await;
            }
        });
    }

    async fn cleanup_old_tasks(&self) {
        let now = SystemTime::now();
        let mut active_tasks = self.active_tasks.write().await;
        let mut queue = self.queue.write().await;
        
        // Clean up expired tasks
        active_tasks.retain(|_, task| {
            now.duration_since(task.timestamp).unwrap_or(Duration::from_secs(0)) <= self.config.timeout
        });

        // Clean up expired queue items
        queue.retain(|_, priority| {
            if let Some(task) = active_tasks.get(&task.peer_id) {
                now.duration_since(task.timestamp).unwrap_or(Duration::from_secs(0)) <= self.config.timeout
            } else {
                false
            }
        });
    }

    pub async fn get_highest_priority_task(&self) -> Option<ValidationTask> {
        let queue = self.queue.read().await;
        queue.peek().map(|(task, _)| task.clone())
    }

    pub async fn remove_task(&self, peer_id: &PeerId) -> Option<ValidationTask> {
        let mut queue = self.queue.write().await;
        let mut active_tasks = self.active_tasks.write().await;
        
        if let Some(task) = active_tasks.remove(peer_id) {
            queue.remove(&task);
            Some(task)
        } else {
            None
        }
    }

    pub async fn get_stats(&self) -> QueueStats {
        let queue = self.queue.read().await;
        let active_tasks = self.active_tasks.read().await;
        
        QueueStats {
            queue_size: queue.len(),
            active_tasks: active_tasks.len(),
            max_queue_size: self.config.max_queue_size,
            priority_threshold: self.config.priority_threshold,
            timeout: self.config.timeout.as_secs(),
            cleanup_interval: self.config.cleanup_interval.as_secs(),
            max_retries: self.config.max_retries,
            priority_distribution: queue
                .iter()
                .map(|(_, priority)| priority)
                .collect(),
        }
    }

    pub async fn retry_task(&self, task: &ValidationTask) -> Result<()> {
        if task.context.retries >= self.config.max_retries {
            return Err(anyhow::anyhow!("Maximum retries exceeded"));
        }

        let new_task = ValidationTask {
            peer_id: task.peer_id.clone(),
            priority: task.priority * 0.9, // Reduce priority for retries
            timestamp: SystemTime::now(),
            validation_type: task.validation_type,
            context: ValidationContext {
                retries: task.context.retries + 1,
                duration: Duration::from_secs(0),
                error_type: task.context.error_type,
                batch_size: task.context.batch_size,
                priority_score: task.priority * 0.9,
            },
        };

        self.submit_task(new_task).await
    }
}

#[derive(Debug, Clone)]
pub struct QueueStats {
    pub queue_size: usize,
    pub active_tasks: usize,
    pub max_queue_size: usize,
    pub priority_threshold: f64,
    pub timeout: u64,
    pub cleanup_interval: u64,
    pub max_retries: u32,
    pub priority_distribution: Vec<f64>,
}
