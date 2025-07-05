use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::{RwLock, mpsc};
use libp2p::PeerId;
use prometheus::{Histogram, HistogramOpts, register_histogram};
use anyhow::Result;

#[derive(Debug, Clone)]
pub struct ValidationResult {
    pub peer_id: PeerId,
    pub score: f64,
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

pub struct ValidationResultAggregator {
    results: Arc<RwLock<HashMap<PeerId, AggregatedResult>>>,
    validation_queue: mpsc::Sender<ValidationResult>,
    config: AggregatorConfig,
    metrics: Arc<AggregatorMetrics>,
}

struct AggregatedResult {
    peer_id: PeerId,
    scores: Vec<f64>,
    timestamps: Vec<SystemTime>,
    validation_types: Vec<ValidationType>,
    contexts: Vec<ValidationContext>,
    aggregated_score: f64,
    last_update: SystemTime,
}

struct AggregatorConfig {
    pub max_results: usize,
    pub aggregation_window: Duration,
    pub batch_size: usize,
    pub aggregation_interval: Duration,
    pub score_threshold: f64,
}

struct AggregatorMetrics {
    result_count: Histogram,
    aggregation_latency: Histogram,
    score_distribution: Histogram,
    error_rate: Histogram,
}

impl AggregatorMetrics {
    pub fn new() -> Result<Arc<Self>> {
        let opts = HistogramOpts::new("validation_result_count", "Number of validation results");
        let result_count = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("aggregation_latency", "Aggregation latency distribution");
        let aggregation_latency = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("score_distribution", "Validation score distribution");
        let score_distribution = register_histogram!(opts.clone())?;
        
        let opts = HistogramOpts::new("error_rate", "Validation error rate");
        let error_rate = register_histogram!(opts.clone())?;
        
        Ok(Arc::new(Self {
            result_count,
            aggregation_latency,
            score_distribution,
            error_rate,
        }))
    }
}

impl ValidationResultAggregator {
    pub fn new(config: AggregatorConfig) -> Result<Self> {
        let (tx, rx) = mpsc::channel(config.batch_size);
        let metrics = AggregatorMetrics::new()?;
        
        let aggregator = Self {
            results: Arc::new(RwLock::new(HashMap::new())),
            validation_queue: tx,
            config,
            metrics,
        };

        // Start aggregation task
        aggregator.start_aggregation_task(rx);

        Ok(aggregator)
    }

    pub async fn submit_result(&self, result: ValidationResult) -> Result<()> {
        self.validation_queue.send(result).await?;
        self.metrics.result_count.observe(1.0);
        Ok(())
    }

    async fn start_aggregation_task(&self, mut rx: mpsc::Receiver<ValidationResult>) {
        tokio::spawn(async move {
            while let Some(result) = rx.recv().await {
                self.aggregate_result(result).await;
            }
        });
    }

    async fn aggregate_result(&self, result: ValidationResult) {
        let mut results = self.results.write().await;
        let now = SystemTime::now();

        // Check if we need to clean up old results
        results.retain(|_, agg| {
            now.duration_since(agg.last_update).unwrap_or(Duration::from_secs(0)) <= self.config.aggregation_window
        });

        // Update or create aggregated result
        let agg = results.entry(result.peer_id.clone()).or_insert(AggregatedResult {
            peer_id: result.peer_id.clone(),
            scores: Vec::new(),
            timestamps: Vec::new(),
            validation_types: Vec::new(),
            contexts: Vec::new(),
            aggregated_score: 0.0,
            last_update: now,
        });

        // Add new result
        agg.scores.push(result.score);
        agg.timestamps.push(result.timestamp);
        agg.validation_types.push(result.validation_type);
        agg.contexts.push(result.context);
        agg.last_update = now;

        // Recalculate aggregated score
        agg.aggregated_score = self.calculate_aggregated_score(agg).await;
        
        // Update metrics
        self.metrics.score_distribution.observe(agg.aggregated_score);
        if agg.aggregated_score < self.config.score_threshold {
            self.metrics.error_rate.observe(1.0);
        }
    }

    async fn calculate_aggregated_score(&self, agg: &AggregatedResult) -> f64 {
        if agg.scores.is_empty() {
            return 0.0;
        }

        // Calculate weighted average based on validation type
        let mut total_score = 0.0;
        let mut total_weight = 0.0;

        for (score, validation_type) in agg.scores.iter().zip(agg.validation_types.iter()) {
            let weight = match validation_type {
                ValidationType::Certificate => 0.4,
                ValidationType::Reputation => 0.3,
                ValidationType::ResponseTime => 0.2,
                ValidationType::Full => 0.1,
            };
            
            total_score += score * weight;
            total_weight += weight;
        }

        total_score / total_weight
    }

    pub async fn get_aggregated_result(&self, peer_id: &PeerId) -> Option<AggregatedResult> {
        let results = self.results.read().await;
        results.get(peer_id).cloned()
    }

    pub async fn get_stats(&self) -> AggregatorStats {
        let results = self.results.read().await;
        AggregatorStats {
            total_results: results.len(),
            active_peers: results.iter()
                .filter(|(_, agg)| agg.scores.len() > 0)
                .count(),
            average_score: results.values()
                .map(|agg| agg.aggregated_score)
                .sum::<f64>() / results.len() as f64,
            score_distribution: results.values()
                .map(|agg| agg.aggregated_score)
                .collect(),
            validation_types: results.values()
                .flat_map(|agg| agg.validation_types.iter().cloned())
                .fold(HashMap::new(), |mut acc, vt| {
                    *acc.entry(vt).or_insert(0) += 1;
                    acc
                }),
        }
    }
}

#[derive(Debug, Clone)]
pub struct AggregatorStats {
    pub total_results: usize,
    pub active_peers: usize,
    pub average_score: f64,
    pub score_distribution: Vec<f64>,
    pub validation_types: HashMap<ValidationType, usize>,
}
