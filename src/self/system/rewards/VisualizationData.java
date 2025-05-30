package org.self.system.rewards;

import java.util.ArrayList;
import java.util.List;

public class VisualizationData {
    private String metric;
    private double currentValue;
    private double previousValue;
    private List<Double> trendPoints;
    private int trendDirection;
    private int trendLength;
    private double trendThreshold;
    
    public VisualizationData(String zMetric) {
        metric = zMetric;
        currentValue = 0.0;
        previousValue = 0.0;
        trendPoints = new ArrayList<>();
        trendDirection = 0; // 0 = neutral, 1 = up, -1 = down
        trendLength = 0;
        trendThreshold = 0.1; // 10% change threshold
    }
    
    public void addTrendPoint(double zValue) {
        trendPoints.add(zValue);
        
        // Keep only last N points
        int maxPoints = SELFParams.SELF_REWARD_TREND_MAX_POINTS.toInt();
        if (trendPoints.size() > maxPoints) {
            trendPoints.remove(0);
        }
    }
    
    public void updateVisualization(double zValue) {
        previousValue = currentValue;
        currentValue = zValue;
        
        // Update trend direction
        updateTrendDirection();
    }
    
    private void updateTrendDirection() {
        if (currentValue == 0) return;
        
        double change = (currentValue - previousValue) / previousValue;
        
        if (Math.abs(change) >= trendThreshold) {
            if (change > 0) {
                if (trendDirection != 1) {
                    trendDirection = 1;
                    trendLength = 1;
                } else {
                    trendLength++;
                }
            } else {
                if (trendDirection != -1) {
                    trendDirection = -1;
                    trendLength = 1;
                } else {
                    trendLength++;
                }
            }
        } else {
            trendDirection = 0;
            trendLength = 0;
        }
    }
    
    public double getCurrentValue() {
        return currentValue;
    }
    
    public boolean isTrendUp() {
        return trendDirection == 1 && trendLength >= 2;
    }
    
    public boolean isTrendDown() {
        return trendDirection == -1 && trendLength >= 2;
    }
    
    public List<Double> getTrendPoints() {
        return new ArrayList<>(trendPoints);
    }
    
    public void reset() {
        currentValue = 0.0;
        previousValue = 0.0;
        trendPoints.clear();
        trendDirection = 0;
        trendLength = 0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "VisualizationData[metric=%s, value=%.2f, trend=%d, length=%d]",
            metric,
            currentValue,
            trendDirection,
            trendLength
        );
    }
}
