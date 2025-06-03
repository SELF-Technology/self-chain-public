use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::RwLock;
use serde::{Deserialize, Serialize};
use crate::benchmark_report::{BenchmarkReport, ScenarioReport};
use crate::monitoring::performance::PerformanceMetrics;

#[derive(Debug, Serialize, Deserialize)]
pub struct AlertConfig {
    pub tps_threshold: u64,
    pub latency_threshold: u64, // in ms
    pub memory_threshold: f64,
    pub cpu_threshold: f64,
    pub network_threshold: u64, // in bytes/s
    pub alert_channels: Vec<AlertChannel>,
    pub alert_interval: u64, // in seconds
    pub severity_levels: Vec<AlertSeverity>,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum AlertChannel {
    Email,
    Slack,
    Discord,
    Webhook,
    SMS,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct AlertSeverity {
    pub name: String,
    pub threshold: f64,
    pub color: String,
    pub channels: Vec<AlertChannel>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct PerformanceAlert {
    pub timestamp: u64,
    pub metric: String,
    pub value: f64,
    pub threshold: f64,
    pub severity: AlertSeverity,
    pub description: String,
    pub recommendations: Vec<String>,
}

pub struct PerformanceAlerter {
    config: AlertConfig,
    metrics: Arc<RwLock<PerformanceMetrics>>,
    alerts: Arc<RwLock<Vec<PerformanceAlert>>>,
    last_alert: Arc<RwLock<SystemTime>>,
    alert_channels: Vec<Box<dyn AlertChannelTrait>>,
}

pub trait AlertChannelTrait {
    fn send_alert(&self, alert: &PerformanceAlert) -> Result<(), String>;
}

impl PerformanceAlerter {
    pub fn new(config: AlertConfig, metrics: Arc<RwLock<PerformanceMetrics>>) -> Self {
        let mut alert_channels = Vec::new();
        
        // Initialize alert channels based on config
        for channel in &config.alert_channels {
            match channel {
                AlertChannel::Email => alert_channels.push(Box::new(EmailChannel::new())),
                AlertChannel::Slack => alert_channels.push(Box::new(SlackChannel::new())),
                AlertChannel::Discord => alert_channels.push(Box::new(DiscordChannel::new())),
                AlertChannel::Webhook => alert_channels.push(Box::new(WebhookChannel::new())),
                AlertChannel::SMS => alert_channels.push(Box::new(SMSChannel::new())),
            }
        }

        Self {
            config,
            metrics,
            alerts: Arc::new(RwLock::new(Vec::new())),
            last_alert: Arc::new(RwLock::new(SystemTime::now())),
            alert_channels,
        }
    }

    pub async fn start(&self) -> Result<(), String> {
        let alerter = self.clone();
        tokio::spawn(async move {
            alerter.monitor_performance().await;
        });

        Ok(())
    }

    async fn monitor_performance(&self) {
        let interval = tokio::time::interval(Duration::from_secs(
            self.config.alert_interval,
        ));

        loop {
            interval.tick().await;
            
            let metrics = self.metrics.read().await;
            self.check_thresholds(&metrics).await;
        }
    }

    async fn check_thresholds(&self, metrics: &PerformanceMetrics) {
        // Check TPS
        if metrics.tps < self.config.tps_threshold {
            self.send_alert(
                "tps",
                metrics.tps as f64,
                self.config.tps_threshold as f64,
                "TPS below threshold".to_string(),
            ).await;
        }

        // Check latency
        if metrics.latency > self.config.latency_threshold {
            self.send_alert(
                "latency",
                metrics.latency as f64,
                self.config.latency_threshold as f64,
                "Latency above threshold".to_string(),
            ).await;
        }

        // Check memory
        if metrics.memory_usage > self.config.memory_threshold {
            self.send_alert(
                "memory",
                metrics.memory_usage as f64,
                self.config.memory_threshold,
                "Memory usage above threshold".to_string(),
            ).await;
        }

        // Check CPU
        if metrics.cpu_usage > self.config.cpu_threshold {
            self.send_alert(
                "cpu",
                metrics.cpu_usage,
                self.config.cpu_threshold,
                "CPU usage above threshold".to_string(),
            ).await;
        }

        // Check network
        if metrics.network_bandwidth > self.config.network_threshold {
            self.send_alert(
                "network",
                metrics.network_bandwidth as f64,
                self.config.network_threshold as f64,
                "Network bandwidth above threshold".to_string(),
            ).await;
        }
    }

    async fn send_alert(
        &self,
        metric: &str,
        value: f64,
        threshold: f64,
        description: String,
    ) {
        // Get current time
        let now = SystemTime::now();
        
        // Check if we should send alert based on interval
        let last_alert = self.last_alert.read().await;
        if now.duration_since(*last_alert).unwrap().as_secs() < self.config.alert_interval {
            return;
        }

        // Determine severity
        let severity = self.get_severity(value, threshold);
        
        // Create alert
        let alert = PerformanceAlert {
            timestamp: now.duration_since(SystemTime::UNIX_EPOCH).unwrap().as_secs(),
            metric: metric.to_string(),
            value,
            threshold,
            severity: severity.clone(),
            description,
            recommendations: self.get_recommendations(metric, value, threshold),
        };

        // Send to all configured channels
        for channel in &self.alert_channels {
            channel.send_alert(&alert).unwrap_or_else(|e| {
                eprintln!("Failed to send alert: {}", e);
            });
        }

        // Update last alert time
        *self.last_alert.write().await = now;
    }

    fn get_severity(&self, value: f64, threshold: f64) -> AlertSeverity {
        let mut severity = self.config.severity_levels[0].clone();
        
        for s in &self.config.severity_levels {
            if value > s.threshold {
                severity = s.clone();
            }
        }

        severity
    }

    fn get_recommendations(&self, metric: &str, value: f64, threshold: f64) -> Vec<String> {
        match metric {
            "tps" => vec![
                format!("Check node load distribution"),
                format!("Verify network connectivity"),
                format!("Consider adding more nodes"),
            ],
            "latency" => vec![
                format!("Investigate network issues"),
                format!("Check node hardware performance"),
                format!("Verify shard distribution"),
            ],
            "memory" => vec![
                format!("Optimize memory usage"),
                format!("Consider memory upgrade"),
                format!("Implement more aggressive caching"),
            ],
            "cpu" => vec![
                format!("Implement CPU optimization"),
                format!("Consider CPU upgrade"),
                format!("Optimize transaction processing"),
            ],
            "network" => vec![
                format!("Check network bandwidth"),
                format!("Optimize message sizes"),
                format!("Verify network configuration"),
            ],
            _ => vec![],
        }
    }
}
