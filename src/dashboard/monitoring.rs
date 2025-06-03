use std::sync::Arc;
use std::time::{SystemTime, Duration};
use tokio::sync::RwLock;
use serde::{Deserialize, Serialize};
use crate::benchmark_report::{BenchmarkReport, ScenarioReport};
use crate::monitoring::performance::PerformanceMetrics;
use crate::alerting::performance_alerts::PerformanceAlert;

#[derive(Debug, Serialize, Deserialize)]
pub struct DashboardConfig {
    pub update_interval: u64, // in seconds
    pub metrics_retention: u64, // in seconds
    pub alert_config: AlertConfig,
    pub visualization_config: VisualizationConfig,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct AlertConfig {
    pub enabled: bool,
    pub channels: Vec<AlertChannel>,
    pub severity_levels: Vec<AlertSeverity>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct VisualizationConfig {
    pub theme: Theme,
    pub refresh_rate: u64, // in ms
    pub chart_types: Vec<ChartType>,
    pub metrics_to_display: Vec<String>,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum Theme {
    Light,
    Dark,
    Monochrome,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum ChartType {
    Line,
    Bar,
    Pie,
    Scatter,
    Heatmap,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct DashboardMetrics {
    pub tps: Vec<ChartPoint>,
    pub latency: Vec<ChartPoint>,
    pub memory: Vec<ChartPoint>,
    pub cpu: Vec<ChartPoint>,
    pub network: Vec<ChartPoint>,
    pub alerts: Vec<PerformanceAlert>,
    pub recommendations: Vec<String>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ChartPoint {
    pub timestamp: u64,
    pub value: f64,
    pub label: String,
}

pub struct PerformanceDashboard {
    config: DashboardConfig,
    metrics: Arc<RwLock<DashboardMetrics>>,
    performance_metrics: Arc<RwLock<PerformanceMetrics>>,
    alerter: Arc<PerformanceAlerter>,
    update_interval: tokio::time::Interval,
}

impl PerformanceDashboard {
    pub fn new(
        config: DashboardConfig,
        performance_metrics: Arc<RwLock<PerformanceMetrics>>,
        alerter: Arc<PerformanceAlerter>,
    ) -> Self {
        Self {
            config,
            metrics: Arc::new(RwLock::new(DashboardMetrics {
                tps: Vec::new(),
                latency: Vec::new(),
                memory: Vec::new(),
                cpu: Vec::new(),
                network: Vec::new(),
                alerts: Vec::new(),
                recommendations: Vec::new(),
            })),
            performance_metrics,
            alerter,
            update_interval: tokio::time::interval(Duration::from_secs(
                config.update_interval,
            )),
        }
    }

    pub async fn start(&self) -> Result<(), String> {
        let dashboard = self.clone();
        tokio::spawn(async move {
            dashboard.update_dashboard().await;
        });

        Ok(())
    }

    async fn update_dashboard(&self) {
        loop {
            self.update_interval.tick().await;
            
            // Get latest metrics
            let perf_metrics = self.performance_metrics.read().await;
            let alerts = self.alerter.get_alerts().await;
            
            // Update dashboard metrics
            self.update_metrics(&perf_metrics, &alerts).await;
            
            // Clean up old metrics
            self.cleanup_old_metrics().await;
        }
    }

    async fn update_metrics(
        &self,
        perf_metrics: &PerformanceMetrics,
        alerts: &[PerformanceAlert],
    ) {
        let mut metrics = self.metrics.write().await;
        
        // Update TPS
        metrics.tps.push(ChartPoint {
            timestamp: SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap()
                .as_secs(),
            value: perf_metrics.tps as f64,
            label: "TPS".to_string(),
        });

        // Update latency
        metrics.latency.push(ChartPoint {
            timestamp: SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap()
                .as_secs(),
            value: perf_metrics.latency as f64,
            label: "Latency".to_string(),
        });

        // Update memory
        metrics.memory.push(ChartPoint {
            timestamp: SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap()
                .as_secs(),
            value: perf_metrics.memory_usage as f64,
            label: "Memory".to_string(),
        });

        // Update CPU
        metrics.cpu.push(ChartPoint {
            timestamp: SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap()
                .as_secs(),
            value: perf_metrics.cpu_usage,
            label: "CPU".to_string(),
        });

        // Update network
        metrics.network.push(ChartPoint {
            timestamp: SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap()
                .as_secs(),
            value: perf_metrics.network_bandwidth as f64,
            label: "Network".to_string(),
        });

        // Update alerts
        metrics.alerts.extend_from_slice(alerts);
        
        // Update recommendations
        metrics.recommendations = self.generate_recommendations(&metrics).await;
    }

    async fn cleanup_old_metrics(&self) {
        let now = SystemTime::now()
            .duration_since(SystemTime::UNIX_EPOCH)
            .unwrap()
            .as_secs();

        let mut metrics = self.metrics.write().await;
        
        // Clean up old points
        metrics.tps.retain(|p| now - p.timestamp <= self.config.metrics_retention);
        metrics.latency.retain(|p| now - p.timestamp <= self.config.metrics_retention);
        metrics.memory.retain(|p| now - p.timestamp <= self.config.metrics_retention);
        metrics.cpu.retain(|p| now - p.timestamp <= self.config.metrics_retention);
        metrics.network.retain(|p| now - p.timestamp <= self.config.metrics_retention);
    }

    async fn generate_recommendations(&self, metrics: &DashboardMetrics) -> Vec<String> {
        let mut recommendations = Vec::new();
        
        // Analyze metrics and generate recommendations
        if let Some(last_tps) = metrics.tps.last() {
            if last_tps.value < self.config.alert_config.tps_threshold as f64 {
                recommendations.push("Consider adding more nodes to the network".to_string());
            }
        }

        if let Some(last_latency) = metrics.latency.last() {
            if last_latency.value > self.config.alert_config.latency_threshold as f64 {
                recommendations.push("Investigate potential network congestion".to_string());
            }
        }

        // Add more recommendations based on other metrics...
        
        recommendations
    }

    pub async fn get_metrics(&self) -> DashboardMetrics {
        self.metrics.read().await.clone()
    }

    pub async fn get_alerts(&self) -> Vec<PerformanceAlert> {
        self.metrics.read().await.alerts.clone()
    }

    pub async fn get_recommendations(&self) -> Vec<String> {
        self.metrics.read().await.recommendations.clone()
    }
}
