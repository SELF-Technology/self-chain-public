use crate::benchmark_suite::BenchmarkMetrics;
use plotters::prelude::*;
use std::path::Path;

pub struct BenchmarkVisualizer {
    metrics: BenchmarkMetrics,
    output_dir: String,
}

impl BenchmarkVisualizer {
    pub fn new(metrics: BenchmarkMetrics, output_dir: String) -> Self {
        Self { metrics, output_dir }
    }

    pub fn generate_tps_chart(&self) -> Result<(), Box<dyn std::error::Error>> {
        let tps_path = format!("{}/tps.svg", self.output_dir);
        let root = BitMapBackend::new(&tps_path, (800, 600))
            .into_drawing_area();
        
        root.fill(&WHITE)?;
        
        let mut chart = ChartBuilder::on(&root)
            .caption("Transactions Per Second", ("sans-serif", 40))
            .x_label_area_size(40)
            .y_label_area_size(40)
            .build_cartesian_2d(0..100, 0..100000)?;
            
        chart.configure_mesh().draw()?;
        
        let tps = vec![self.metrics.tps];
        let x = vec![0];
        
        chart.draw_series(LineSeries::new(x.into_iter().zip(tps.iter().map(|&t| t as i32)), &RED))?;
        
        Ok(())
    }

    pub fn generate_latency_chart(&self) -> Result<(), Box<dyn std::error::Error>> {
        let latency_path = format!("{}/latency.svg", self.output_dir);
        let root = BitMapBackend::new(&latency_path, (800, 600))
            .into_drawing_area();
        
        root.fill(&WHITE)?;
        
        let mut chart = ChartBuilder::on(&root)
            .caption("Latency (ms)", ("sans-serif", 40))
            .x_label_area_size(40)
            .y_label_area_size(40)
            .build_cartesian_2d(0..100, 0..1000)?;
            
        chart.configure_mesh().draw()?;
        
        let latency = vec![self.metrics.latency];
        let x = vec![0];
        
        chart.draw_series(LineSeries::new(x.into_iter().zip(latency.iter().map(|&l| l as i32)), &BLUE))?;
        
        Ok(())
    }

    pub fn generate_resource_usage_chart(&self) -> Result<(), Box<dyn std::error::Error>> {
        let root = BitMapBackend::new(&format!("{}/resource_usage.svg", self.output_dir), (800, 600))
            .into_drawing_area();
        
        root.fill(&WHITE)?;
        
        let mut chart = ChartBuilder::on(&root)
            .caption("Resource Usage", ("sans-serif", 40))
            .x_label_area_size(40)
            .y_label_area_size(40)
            .build_cartesian_2d(0..100, 0..100)?;
            
        chart.configure_mesh().draw()?;
        
        let cpu = vec![self.metrics.resource_usage.cpu];
        let memory = vec![self.metrics.resource_usage.memory];
        let network = vec![self.metrics.resource_usage.network];
        let x = vec![0];
        
        chart.draw_series(LineSeries::new(x.into_iter().zip(cpu.into_iter()), &GREEN))?;
        chart.draw_series(LineSeries::new(x.into_iter().zip(memory.into_iter()), &BLUE))?;
        chart.draw_series(LineSeries::new(x.into_iter().zip(network.into_iter()), &RED))?;
        
        Ok(())
    }
}
