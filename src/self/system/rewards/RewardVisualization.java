package org.self.system.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.self.objects.MiniNumber;
import org.self.system.governance.monitor.GovernanceMonitor;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class RewardVisualization {
    private static RewardVisualization instance;
    private Map<String, VisualizationData> visualizations;
    private Timer visualizationTimer;
    private long refreshPeriod;
    private GovernanceMonitor governanceMonitor;
    private RewardMetrics metrics;
    private RewardTrendTracker trendTracker;
    
    private RewardVisualization() {
        visualizations = new HashMap<>();
        refreshPeriod = SELFParams.SELF_REWARD_VISUALIZATION_REFRESH.toLong();
        governanceMonitor = GovernanceMonitor.getInstance();
        metrics = RewardMetrics.getInstance();
        trendTracker = RewardTrendTracker.getInstance();
        initializeVisualizations();
    }
    
    public static RewardVisualization getInstance() {
        if (instance == null) {
            instance = new RewardVisualization();
        }
        return instance;
    }
    
    private void initializeVisualizations() {
        visualizationTimer = new Timer();
        visualizationTimer.schedule(new VisualizationTask(), 0, refreshPeriod);
        
        // Initialize visualizations
        initializeVisualization("validator_distribution");
        initializeVisualization("user_distribution");
        initializeVisualization("validation_rate");
    }
    
    private void initializeVisualization(String zMetric) {
        VisualizationData data = new VisualizationData(zMetric);
        visualizations.put(zMetric, data);
    }
    
    private class VisualizationTask extends TimerTask {
        @Override
        public void run() {
            updateVisualizations();
            generateVisualizationReports();
        }
    }
    
    private void updateVisualizations() {
        // Update validator visualization
        updateMetricVisualization("validator_distribution", metrics.getDistributions().get("validator"));
        updateMetricVisualization("validator_performance", metrics.getPerformance().get("validator"));
        
        // Update user visualization
        updateMetricVisualization("user_distribution", metrics.getDistributions().get("user"));
        updateMetricVisualization("user_performance", metrics.getPerformance().get("user"));
        
        // Update validation visualization
        updateMetricVisualization("validation_rate", metrics.getValidations().get("hex"));
        updateMetricVisualization("validation_amount", metrics.getValidations().get("hex"));
    }
    
    private void updateMetricVisualization(String zMetric, Object zData) {
        VisualizationData data = visualizations.get(zMetric);
        if (data != null && zData != null) {
            // Get current value
            double value = getMetricValue(zData);
            
            // Update trend data
            data.addTrendPoint(value);
            
            // Update visualization data
            data.updateVisualization(value);
        }
    }
    
    private double getMetricValue(Object zData) {
        if (zData instanceof RewardDistribution) {
            RewardDistribution dist = (RewardDistribution) zData;
            return dist.getAverageAmount();
        } else if (zData instanceof RewardPerformance) {
            RewardPerformance perf = (RewardPerformance) zData;
            return perf.getAverageScore();
        } else if (zData instanceof RewardValidation) {
            RewardValidation val = (RewardValidation) zData;
            return val.getValidationRate();
        }
        return 0.0;
    }
    
    private void generateVisualizationReports() {
        StringBuilder report = new StringBuilder("=== REWARD VISUALIZATION REPORT ===\n\n");
        
        // Add validator visualizations
        report.append("=== Validator Visualizations ===\n");
        addVisualizationToReport("validator_distribution");
        addVisualizationToReport("validator_performance");
        
        // Add user visualizations
        report.append("\n=== User Visualizations ===\n");
        addVisualizationToReport("user_distribution");
        addVisualizationToReport("user_performance");
        
        // Add validation visualizations
        report.append("\n=== Validation Visualizations ===\n");
        addVisualizationToReport("validation_rate");
        addVisualizationToReport("validation_amount");
        
        // Generate trend charts
        generateTrendCharts();
        
        // Log the report
        SelfLogger.log(report.toString());
        
        // Send to governance monitor
        governanceMonitor.updateVisualization(report.toString());
    }
    
    private void addVisualizationToReport(String zMetric) {
        VisualizationData data = visualizations.get(zMetric);
        if (data != null) {
            StringBuilder line = new StringBuilder();
            line.append(String.format("%s: ", zMetric));
            
            // Add bar chart representation
            double value = data.getCurrentValue();
            int bars = (int)(value / 10); // Scale for visualization
            String barChart = new String(new char[Math.max(0, bars)]).replace("\0", "█");
            
            line.append(String.format("%s (%.2f)", barChart, value));
            
            // Add trend indicator
            if (data.isTrendUp()) {
                line.append(" ↑");
            } else if (data.isTrendDown()) {
                line.append(" ↓");
            }
            
            line.append("\n");
            SelfLogger.log(line.toString());
        }
    }
    
    private void generateTrendCharts() {
        for (Map.Entry<String, VisualizationData> entry : visualizations.entrySet()) {
            String metric = entry.getKey();
            VisualizationData data = entry.getValue();
            
            // Generate line chart
            StringBuilder chart = new StringBuilder();
            List<Double> trendPoints = data.getTrendPoints();
            
            if (!trendPoints.isEmpty()) {
                double maxValue = Collections.max(trendPoints);
                double minValue = Collections.min(trendPoints);
                
                chart.append(String.format("\n=== %s Trend Chart ===\n", metric));
                
                // Generate chart rows
                for (int row = 0; row < 5; row++) {
                    double rowValue = minValue + ((maxValue - minValue) * (row / 4.0));
                    chart.append(String.format("%.2f: ", rowValue));
                    
                    for (double point : trendPoints) {
                        if (point >= rowValue) {
                            chart.append("█");
                        } else {
                            chart.append(" ");
                        }
                    }
                    chart.append("\n");
                }
                
                // Add time axis
                chart.append("Time: ");
                for (int i = 0; i < trendPoints.size(); i++) {
                    chart.append("-" + (i % 2 == 0 ? " " : ""));
                }
                
                SelfLogger.log(chart.toString());
            }
        }
    }
    
    public void resetVisualizations() {
        if (visualizationTimer != null) {
            visualizationTimer.cancel();
            visualizationTimer = null;
        }
        visualizations.clear();
        initializeVisualizations();
    }

    public Map<String, VisualizationData> getVisualizations() {
        return new HashMap<>(visualizations);
    }

    public VisualizationData getVisualization(String zMetric) {
        return visualizations.get(zMetric);
    }
}
