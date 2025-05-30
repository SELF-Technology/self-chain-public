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

public class RewardTrendTracker {
    private static RewardTrendTracker instance;
    private Map<String, List<TrendDataPoint>> trendData;
    private Timer trendTimer;
    private long trackingPeriod;
    private GovernanceMonitor governanceMonitor;
    private RewardMetrics metrics;
    
    private RewardTrendTracker() {
        trendData = new HashMap<>();
        trackingPeriod = SELFParams.SELF_REWARD_TREND_TRACKING_PERIOD.toLong();
        governanceMonitor = GovernanceMonitor.getInstance();
        metrics = RewardMetrics.getInstance();
        initializeTrendTracking();
    }
    
    public static RewardTrendTracker getInstance() {
        if (instance == null) {
            instance = new RewardTrendTracker();
        }
        return instance;
    }
    
    private void initializeTrendTracking() {
        trendTimer = new Timer();
        trendTimer.schedule(new TrendTrackingTask(), 0, trackingPeriod);
        
        // Initialize trend data for all metrics
        initializeTrendData();
    }
    
    private void initializeTrendData() {
        // Validator metrics
        initializeTrendData("validator_distribution");
        initializeTrendData("validator_performance");
        
        // User metrics
        initializeTrendData("user_distribution");
        initializeTrendData("user_performance");
        
        // Validation metrics
        initializeTrendData("validation_rate");
        initializeTrendData("validation_amount");
    }
    
    private void initializeTrendData(String zMetric) {
        List<TrendDataPoint> dataPoints = new ArrayList<>();
        trendData.put(zMetric, dataPoints);
    }
    
    private class TrendTrackingTask extends TimerTask {
        @Override
        public void run() {
            trackTrends();
            analyzeTrends();
            generateTrendReport();
        }
    }
    
    private void trackTrends() {
        // Track validator trends
        trackMetricTrend("validator_distribution", metrics.getDistributions().get("validator"));
        trackMetricTrend("validator_performance", metrics.getPerformance().get("validator"));
        
        // Track user trends
        trackMetricTrend("user_distribution", metrics.getDistributions().get("user"));
        trackMetricTrend("user_performance", metrics.getPerformance().get("user"));
        
        // Track validation trends
        trackMetricTrend("validation_rate", metrics.getValidations().get("hex"));
        trackMetricTrend("validation_amount", metrics.getValidations().get("hex"));
    }
    
    private void trackMetricTrend(String zMetric, Object zData) {
        List<TrendDataPoint> dataPoints = trendData.get(zMetric);
        if (dataPoints != null && zData != null) {
            double value = getMetricValue(zData);
            dataPoints.add(new TrendDataPoint(value));
            
            // Keep only last N data points
            int maxPoints = SELFParams.SELF_REWARD_TREND_MAX_POINTS.toInt();
            if (dataPoints.size() > maxPoints) {
                dataPoints.remove(0);
            }
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
    
    private void analyzeTrends() {
        for (Map.Entry<String, List<TrendDataPoint>> entry : trendData.entrySet()) {
            String metric = entry.getKey();
            List<TrendDataPoint> dataPoints = entry.getValue();
            
            if (dataPoints.size() > 1) {
                TrendAnalysis analysis = analyzeTrend(dataPoints);
                
                // Check for anomalies
                if (analysis.isAnomalyDetected()) {
                    String alert = String.format(
                        "TREND ANOMALY: %s showing unusual pattern", 
                        metric
                    );
                    
                    // Log the alert
                    SelfLogger.log(alert);
                    
                    // Notify governance system
                    governanceMonitor.notifyAlert(alert);
                }
            }
        }
    }
    
    private TrendAnalysis analyzeTrend(List<TrendDataPoint> zDataPoints) {
        TrendAnalysis analysis = new TrendAnalysis();
        
        // Calculate basic statistics
        double[] values = new double[zDataPoints.size()];
        for (int i = 0; i < zDataPoints.size(); i++) {
            values[i] = zDataPoints.get(i).getValue();
        }
        
        // Calculate mean and standard deviation
        double mean = calculateMean(values);
        double stdDev = calculateStandardDeviation(values, mean);
        
        // Check for anomalies
        boolean anomalyDetected = false;
        for (double value : values) {
            if (Math.abs(value - mean) > (2 * stdDev)) {
                anomalyDetected = true;
                break;
            }
        }
        
        analysis.setMean(mean);
        analysis.setStandardDeviation(stdDev);
        analysis.setAnomalyDetected(anomalyDetected);
        
        return analysis;
    }
    
    private double calculateMean(double[] zValues) {
        double sum = 0.0;
        for (double value : zValues) {
            sum += value;
        }
        return sum / zValues.length;
    }
    
    private double calculateStandardDeviation(double[] zValues, double zMean) {
        double sum = 0.0;
        for (double value : zValues) {
            sum += Math.pow(value - zMean, 2);
        }
        return Math.sqrt(sum / zValues.length);
    }
    
    private void generateTrendReport() {
        StringBuilder report = new StringBuilder("=== REWARD TREND REPORT ===\n\n");
        
        // Add validator trends
        report.append("=== Validator Trends ===\n");
        addTrendToReport("validator_distribution");
        addTrendToReport("validator_performance");
        
        // Add user trends
        report.append("\n=== User Trends ===\n");
        addTrendToReport("user_distribution");
        addTrendToReport("user_performance");
        
        // Add validation trends
        report.append("\n=== Validation Trends ===\n");
        addTrendToReport("validation_rate");
        addTrendToReport("validation_amount");
        
        // Log the report
        SelfLogger.log(report.toString());
        
        // Send to governance monitor
        governanceMonitor.updateTrendReport(report.toString());
    }
    
    private void addTrendToReport(String zMetric) {
        List<TrendDataPoint> dataPoints = trendData.get(zMetric);
        if (dataPoints != null && !dataPoints.isEmpty()) {
            TrendAnalysis analysis = analyzeTrend(dataPoints);
            StringBuilder line = new StringBuilder();
            line.append(String.format("%s: ", zMetric));
            
            // Add latest value
            line.append(String.format("Latest: %.2f", dataPoints.get(dataPoints.size() - 1).getValue()));
            
            // Add trend analysis
            line.append(String.format(" (Mean: %.2f, StdDev: %.2f)", 
                analysis.getMean(), 
                analysis.getStandardDeviation()
            ));
            
            // Add anomaly status
            if (analysis.isAnomalyDetected()) {
                line.append(" (ANOMALY DETECTED)");
            }
            
            line.append("\n");
            SelfLogger.log(line.toString());
        }
    }
    
    public void resetTrendTracking() {
        if (trendTimer != null) {
            trendTimer.cancel();
            trendTimer = null;
        }
        trendData.clear();
        initializeTrendTracking();
    }

    public Map<String, List<TrendDataPoint>> getTrendData() {
        return new HashMap<>(trendData);
    }

    public List<TrendDataPoint> getTrendData(String zMetric) {
        return trendData.get(zMetric);
    }
}
