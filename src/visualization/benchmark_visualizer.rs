use std::sync::Arc;
use std::path::Path;
use serde::{Deserialize, Serialize};
use crate::benchmark_report::{BenchmarkReport, ScenarioReport, ScenarioMetrics};
use plotters::prelude::*;

#[derive(Debug, Serialize, Deserialize)]
pub struct VisualizationConfig {
    pub output_dir: String,
    pub width: u32,
    pub height: u32,
    pub theme: Theme,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum Theme {
    Light,
    Dark,
    Monochrome,
}

pub struct BenchmarkVisualizer {
    config: VisualizationConfig,
    report: Arc<BenchmarkReport>,
    backend: SVGBackend,
}

impl BenchmarkVisualizer {
    pub fn new(
        config: VisualizationConfig,
        report: Arc<BenchmarkReport>,
    ) -> Self {
        let backend = SVGBackend::new(
            &format!("{}/benchmark.svg", config.output_dir),
            (config.width, config.height),
        );

        Self {
            config,
            report,
            backend,
        }
    }

    pub fn generate_all_visualizations(&self) -> Result<(), String> {
        self.generate_tps_chart()?;
        self.generate_latency_chart()?;
        self.generate_resource_usage_chart()?;
        self.generate_scalability_chart()?;
        self.generate_bottleneck_chart()?;

        Ok(())
    }

    fn generate_tps_chart(&self) -> Result<(), String> {
        let root = self.backend
            .root_area()
            .titled("Transactions Per Second", ("sans-serif", 20))
            .map_err(|e| e.to_string())?;

        let mut chart = ChartBuilder::on(&root)
            .caption("TPS Comparison", ("sans-serif", 40))
            .x_label_area_size(40)
            .y_label_area_size(40)
            .build_cartesian_2d(0..100, 0..100000)
            .map_err(|e| e.to_string())?;

        chart
            .configure_mesh()
            .draw()
            .map_err(|e| e.to_string())?;

        for scenario in &self.report.scenarios {
            let tps = scenario.metrics.tps.iter().cloned().collect::<Vec<_>>();
            let x = (0..tps.len()).collect::<Vec<_>>();

            chart
                .draw_series(LineSeries::new(
                    x.into_iter().zip(tps.into_iter()),
                    &RED,
                ))
                .map_err(|e| e.to_string())?;
        }

        Ok(())
    }

    fn generate_latency_chart(&self) -> Result<(), String> {
        let root = self.backend
            .root_area()
            .titled("Latency", ("sans-serif", 20))
            .map_err(|e| e.to_string())?;

        let mut chart = ChartBuilder::on(&root)
            .caption("Latency Comparison", ("sans-serif", 40))
            .x_label_area_size(40)
            .y_label_area_size(40)
            .build_cartesian_2d(0..100, 0..1000)
            .map_err(|e| e.to_string())?;

        chart
            .configure_mesh()
            .draw()
            .map_err(|e| e.to_string())?;

        for scenario in &self.report.scenarios {
            let latency = scenario.metrics.latency.iter().cloned().collect::<Vec<_>>();
            let x = (0..latency.len()).collect::<Vec<_>>();

            chart
                .draw_series(LineSeries::new(
                    x.into_iter().zip(latency.into_iter()),
                    &BLUE,
                ))
                .map_err(|e| e.to_string())?;
        }

        Ok(())
    }

    fn generate_resource_usage_chart(&self) -> Result<(), String> {
        let root = self.backend
            .root_area()
            .titled("Resource Usage", ("sans-serif", 20))
            .map_err(|e| e.to_string())?;

        let mut chart = ChartBuilder::on(&root)
            .caption("Resource Usage Comparison", ("sans-serif", 40))
            .x_label_area_size(40)
            .y_label_area_size(40)
            .build_cartesian_2d(0..100, 0..100)
            .map_err(|e| e.to_string())?;

        chart
            .configure_mesh()
            .draw()
            .map_err(|e| e.to_string())?;

        for scenario in &self.report.scenarios {
            let cpu = scenario.metrics.cpu_usage.iter().cloned().collect::<Vec<_>>();
            let memory = scenario.metrics.memory_usage.iter().cloned().collect::<Vec<_>>();
            let network = scenario.metrics.network_bandwidth.iter().cloned().collect::<Vec<_>>();
            let x = (0..cpu.len()).collect::<Vec<_>>();

            chart
                .draw_series(LineSeries::new(
                    x.iter().zip(cpu.iter()).map(|(x, y)| (*x, *y)),
                    &GREEN,
                ))
                .map_err(|e| e.to_string())?;

            chart
                .draw_series(LineSeries::new(
                    x.iter().zip(memory.iter()).map(|(x, y)| (*x, *y)),
                    &BLUE,
                ))
                .map_err(|e| e.to_string())?;

            chart
                .draw_series(LineSeries::new(
                    x.iter().zip(network.iter()).map(|(x, y)| (*x, *y)),
                    &RED,
                ))
                .map_err(|e| e.to_string())?;
        }

        Ok(())
    }

    fn generate_scalability_chart(&self) -> Result<(), String> {
        let root = self.backend
            .root_area()
            .titled("Scalability", ("sans-serif", 20))
            .map_err(|e| e.to_string())?;

        let mut chart = ChartBuilder::on(&root)
            .caption("Scalability Comparison", ("sans-serif", 40))
            .x_label_area_size(40)
            .y_label_area_size(40)
            .build_cartesian_2d(0..100, 0..100000)
            .map_err(|e| e.to_string())?;

        chart
            .configure_mesh()
            .draw()
            .map_err(|e| e.to_string())?;

        for scenario in &self.report.scenarios {
            let tps = scenario.metrics.tps.iter().cloned().collect::<Vec<_>>();
            let latency = scenario.metrics.latency.iter().cloned().collect::<Vec<_>>();
            let x = (0..tps.len()).collect::<Vec<_>>();

            chart
                .draw_series(LineSeries::new(
                    x.iter().zip(tps.iter().zip(latency.iter()))
                        .map(|(x, (tps, latency))| (*x, tps / latency)),
                    &PURPLE,
                ))
                .map_err(|e| e.to_string())?;
        }

        Ok(())
    }

    fn generate_bottleneck_chart(&self) -> Result<(), String> {
        let root = self.backend
            .root_area()
            .titled("Bottlenecks", ("sans-serif", 20))
            .map_err(|e| e.to_string())?;

        let mut chart = ChartBuilder::on(&root)
            .caption("Bottleneck Analysis", ("sans-serif", 40))
            .x_label_area_size(40)
            .y_label_area_size(40)
            .build_cartesian_2d(0..100, 0..100)
            .map_err(|e| e.to_string())?;

        chart
            .configure_mesh()
            .draw()
            .map_err(|e| e.to_string())?;

        for scenario in &self.report.scenarios {
            for bottleneck in &scenario.bottlenecks {
                let impact = bottleneck.impact;
                let x = scenario.metrics.tps.len() as i32;

                chart
                    .draw_series(LineSeries::new(
                        vec![(x, impact)],
                        &BLACK,
                    ))
                    .map_err(|e| e.to_string())?;
            }
        }

        Ok(())
    }
}
