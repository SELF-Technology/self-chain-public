use std::sync::Arc;
use std::time::{SystemTime, Duration};
use libp2p::PeerId;
use serde::{Serialize, Deserialize};
use chrono::{DateTime, Utc};
use log::{info, warn, error};
use crate::network::peer_validation_config::{ErrorType, ValidationType};
use crate::network::peer_validation_metrics::PeerValidationMetrics;

#[derive(Debug, Serialize, Deserialize, Clone)]
pub enum ValidationLogLevel {
    Info,
    Warning,
    Error,
    Critical,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct ValidationLogEntry {
    pub timestamp: DateTime<Utc>,
    pub peer_id: PeerId,
    pub validation_type: ValidationType,
    pub log_level: ValidationLogLevel,
    pub message: String,
    pub duration: Option<Duration>,
    pub score: Option<f64>,
    pub error_type: Option<ErrorType>,
    pub recovery_strategy: Option<String>,
}

pub struct ValidationLogger {
    logs: Arc<RwLock<Vec<ValidationLogEntry>>>,
    metrics: Arc<PeerValidationMetrics>,
    config: LogConfig,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct LogConfig {
    pub max_logs: usize,
    pub log_retention: Duration,
    pub log_interval: Duration,
    pub log_level: ValidationLogLevel,
    pub log_to_file: bool,
    pub log_file_path: String,
}

impl ValidationLogger {
    pub fn new(
        metrics: Arc<PeerValidationMetrics>,
        config: LogConfig,
    ) -> Self {
        Self {
            logs: Arc::new(RwLock::new(Vec::new())),
            metrics,
            config,
        }
    }

    pub async fn log_validation_start(
        &self,
        peer_id: &PeerId,
        validation_type: ValidationType,
    ) {
        let entry = ValidationLogEntry {
            timestamp: Utc::now(),
            peer_id: peer_id.clone(),
            validation_type,
            log_level: ValidationLogLevel::Info,
            message: format!("Starting validation for peer {}", peer_id),
            duration: None,
            score: None,
            error_type: None,
            recovery_strategy: None,
        };
        
        self.logs.write().await.push(entry.clone());
        info!("{}", serde_json::to_string(&entry).unwrap());
    }

    pub async fn log_validation_success(
        &self,
        peer_id: &PeerId,
        validation_type: ValidationType,
        duration: Duration,
        score: f64,
    ) {
        let entry = ValidationLogEntry {
            timestamp: Utc::now(),
            peer_id: peer_id.clone(),
            validation_type,
            log_level: ValidationLogLevel::Info,
            message: format!("Validation successful for peer {}", peer_id),
            duration: Some(duration),
            score: Some(score),
            error_type: None,
            recovery_strategy: None,
        };
        
        self.logs.write().await.push(entry.clone());
        info!("{}", serde_json::to_string(&entry).unwrap());
    }

    pub async fn log_validation_error(
        &self,
        peer_id: &PeerId,
        validation_type: ValidationType,
        error_type: ErrorType,
        duration: Duration,
        recovery_strategy: Option<&str>,
        error_message: &str,
    ) {
        let entry = ValidationLogEntry {
            timestamp: Utc::now(),
            peer_id: peer_id.clone(),
            validation_type,
            log_level: ValidationLogLevel::Error,
            message: error_message.to_string(),
            duration: Some(duration),
            score: None,
            error_type: Some(error_type),
            recovery_strategy: recovery_strategy.map(|s| s.to_string()),
        };
        
        self.logs.write().await.push(entry.clone());
        error!("{}", serde_json::to_string(&entry).unwrap());
    }

    pub async fn log_validation_warning(
        &self,
        peer_id: &PeerId,
        validation_type: ValidationType,
        message: &str,
    ) {
        let entry = ValidationLogEntry {
            timestamp: Utc::now(),
            peer_id: peer_id.clone(),
            validation_type,
            log_level: ValidationLogLevel::Warning,
            message: message.to_string(),
            duration: None,
            score: None,
            error_type: None,
            recovery_strategy: None,
        };
        
        self.logs.write().await.push(entry.clone());
        warn!("{}", serde_json::to_string(&entry).unwrap());
    }

    pub async fn cleanup_logs(&self) {
        let current_time = Utc::now();
        let mut logs = self.logs.write().await;
        
        // Remove old logs
        logs.retain(|entry| {
            current_time - entry.timestamp <= self.config.log_retention
        });
        
        // Keep only the most recent logs if we exceed max_logs
        if logs.len() > self.config.max_logs {
            logs.sort_by(|a, b| b.timestamp.cmp(&a.timestamp));
            logs.truncate(self.config.max_logs);
        }
    }

    pub async fn get_logs(&self, limit: usize) -> Vec<ValidationLogEntry> {
        let logs = self.logs.read().await;
        logs.iter()
            .rev()
            .take(limit)
            .cloned()
            .collect()
    }

    pub async fn get_stats(&self) -> ValidationLogStats {
        let logs = self.logs.read().await;
        
        ValidationLogStats {
            total_logs: logs.len(),
            log_levels: logs.iter()
                .map(|e| e.log_level)
                .fold(HashMap::new(), |mut acc, ll| {
                    *acc.entry(ll).or_insert(0) += 1;
                    acc
                }),
            validation_types: logs.iter()
                .map(|e| e.validation_type)
                .fold(HashMap::new(), |mut acc, vt| {
                    *acc.entry(vt).or_insert(0) += 1;
                    acc
                }),
            error_types: logs.iter()
                .filter_map(|e| e.error_type)
                .fold(HashMap::new(), |mut acc, et| {
                    *acc.entry(et).or_insert(0) += 1;
                    acc
                }),
        }
    }
}

#[derive(Debug, Clone)]
pub struct ValidationLogStats {
    pub total_logs: usize,
    pub log_levels: HashMap<ValidationLogLevel, usize>,
    pub validation_types: HashMap<ValidationType, usize>,
    pub error_types: HashMap<ErrorType, usize>,
}
