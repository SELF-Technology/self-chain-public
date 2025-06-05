use serde::{Deserialize, Serialize};
use std::sync::Arc;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ScenarioResult {
    pub metrics: ScenarioMetrics,
    pub errors: Vec<BenchmarkError>,
    pub duration: u64,
    pub transactions: Vec<Transaction>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ScenarioMetrics {
    pub tps: u64,
    pub latency: u64,
    pub error_rate: f64,
    pub resource_usage: ResourceUtilization,
}

impl Default for ScenarioMetrics {
    fn default() -> Self {
        Self {
            tps: 0,
            latency: 0,
            error_rate: 0.0,
            resource_usage: ResourceUtilization::default(),
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
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

impl Default for BenchmarkMetrics {
    fn default() -> Self {
        Self {
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
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ResourceUtilization {
    pub cpu: f64,
    pub memory: f64,
    pub storage: f64,
    pub gpu: Option<f64>,
}

impl Default for ResourceUtilization {
    fn default() -> Self {
        Self {
            cpu: 0.0,
            memory: 0.0,
            storage: 0.0,
            gpu: None,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Bottleneck {
    pub component: String,
    pub impact: f64,
    pub description: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct BenchmarkError {
    pub message: String,
    pub error_type: String,
    pub timestamp: u64,
}
