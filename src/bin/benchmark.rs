use std::sync::Arc;
use std::path::Path;
use std::fs;
use std::env;
use serde::Deserialize;
use crate::visualization::BenchmarkVisualizer;
use crate::benchmark_report::BenchmarkReporter;
use crate::benchmark_scenarios::BenchmarkScenarioRunner;
use crate::benchmark_scenarios::BenchmarkScenario;
use crate::benchmark_scenarios::ScenarioResult;
use crate::benchmark_scenarios::ScenarioMetrics;
use crate::benchmark_scenarios::TransactionProfile;
use crate::benchmark_suite::BenchmarkSuite;
use crate::grid::advanced_sharding::ShardingManager;
use crate::monitoring::performance::PerformanceMonitor;

#[derive(Debug, Deserialize)]
pub struct BenchmarkConfig {
    pub visualization: VisualizationConfig,
    pub benchmark: BenchmarkConfigInner,
}

#[derive(Debug, Deserialize)]
pub struct BenchmarkConfigInner {
    pub scenarios: Vec<ScenarioConfig>,
}

#[derive(Debug, Deserialize)]
pub struct ScenarioConfig {
    pub type: String,
    pub baseline_tps: Option<u64>,
    pub peak_tps: Option<u64>,
    pub duration: Option<u64>,
    pub surge_duration: Option<u64>,
    pub start_tps: Option<u64>,
    pub end_tps: Option<u64>,
    pub transaction_profiles: Option<Vec<TransactionProfileConfig>>,
    pub partition_duration: Option<u64>,
    pub partition_size: Option<f64>,
    pub recovery_time: Option<u64>,
    pub failure_rate: Option<f64>,
}

#[derive(Debug, Deserialize)]
pub struct TransactionProfileConfig {
    pub frequency: f64,
    pub size: usize,
    pub complexity: u8,
    pub priority: u8,
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Initialize components
    let sharding_manager = Arc::new(ShardingManager::new());
    let performance_monitor = Arc::new(PerformanceMonitor::new());
    let suite = Arc::new(BenchmarkSuite::new(
        sharding_manager.clone(),
        performance_monitor.clone(),
    ));

    // Load configuration
    let config_path = Path::new("config/benchmark_config.yaml");
    let config_str = fs::read_to_string(config_path)?;
    let config: BenchmarkConfig = serde_yaml::from_str(&config_str)?;

    // Create scenario runner
    let runner = BenchmarkScenarioRunner::new(
        suite.clone(),
        sharding_manager.clone(),
        performance_monitor.clone(),
    );

    // Run scenarios
    let mut results = Vec::new();
    for scenario_config in config.benchmark.scenarios {
        let scenario = match scenario_config.type.as_str() {
            "Surge" => BenchmarkScenario::Surge {
                baseline_tps: scenario_config.baseline_tps.unwrap(),
                peak_tps: scenario_config.peak_tps.unwrap(),
                duration: scenario_config.duration.unwrap(),
                surge_duration: scenario_config.surge_duration.unwrap(),
            },
            "RampUp" => BenchmarkScenario::RampUp {
                start_tps: scenario_config.start_tps.unwrap(),
                end_tps: scenario_config.end_tps.unwrap(),
                duration: scenario_config.duration.unwrap(),
            },
            "RealWorld" => BenchmarkScenario::RealWorld {
                transaction_profiles: scenario_config.transaction_profiles
                    .unwrap()
                    .into_iter()
                    .map(|p| TransactionProfile {
                        frequency: p.frequency,
                        size: p.size,
                        complexity: p.complexity,
                        priority: p.priority,
                    })
                    .collect(),
                duration: scenario_config.duration.unwrap(),
            },
            "NetworkPartition" => BenchmarkScenario::NetworkPartition {
                partition_duration: scenario_config.partition_duration.unwrap(),
                partition_size: scenario_config.partition_size.unwrap(),
                recovery_time: scenario_config.recovery_time.unwrap(),
            },
            "HardwareFailure" => BenchmarkScenario::HardwareFailure {
                failure_rate: scenario_config.failure_rate.unwrap(),
                recovery_time: scenario_config.recovery_time.unwrap(),
            },
            _ => panic!("Unknown scenario type"),
        };

        let result = runner.run_scenario(&scenario).await;
        results.push(result);
    }

    // Generate report
    let reporter = BenchmarkReporter::new(
        Arc::new(SystemInfo::default()),
        config.benchmark.scenarios,
        results,
    );
    let report = reporter.generate_report();

    // Generate visualizations
    let visualizer = BenchmarkVisualizer::new(
        config.visualization,
        Arc::new(report.clone()),
    );
    visualizer.generate_all_visualizations()?;

    println!("Benchmark completed! Results saved to {}", config.visualization.output_dir);
    Ok(())
}
