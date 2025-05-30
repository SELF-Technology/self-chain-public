package org.self.system.rewards;

import java.util.HashMap;
import java.util.Map;

public class RewardPerformance {
    private Map<String, Double> metrics;
    private double totalScore;
    private int metricCount;
    private double averageScore;
    private double maxScore;
    private double minScore;
    
    public RewardPerformance() {
        metrics = new HashMap<>();
        totalScore = 0.0;
        metricCount = 0;
        averageScore = 0.0;
        maxScore = 0.0;
        minScore = Double.MAX_VALUE;
    }
    
    public void updateMetric(String zMetric, double zValue) {
        metrics.put(zMetric, zValue);
        updateStats(zValue);
    }
    
    public void updateMetric(double zValue) {
        updateStats(zValue);
    }
    
    private void updateStats(double zValue) {
        totalScore += zValue;
        metricCount++;
        
        // Update min/max
        if (zValue > maxScore) {
            maxScore = zValue;
        }
        if (zValue < minScore) {
            minScore = zValue;
        }
        
        // Update average
        averageScore = totalScore / metricCount;
    }
    
    public Map<String, Double> getMetrics() {
        return new HashMap<>(metrics);
    }
    
    public double getTotalScore() {
        return totalScore;
    }
    
    public int getMetricCount() {
        return metricCount;
    }
    
    public double getAverageScore() {
        return averageScore;
    }
    
    public double getMaxScore() {
        return maxScore;
    }
    
    public double getMinScore() {
        return minScore;
    }
    
    public String toString() {
        return String.format(
            "Performance[total=%.2f, count=%d, avg=%.2f, max=%.2f, min=%.2f]",
            totalScore,
            metricCount,
            averageScore,
            maxScore,
            minScore
        );
    }
}
