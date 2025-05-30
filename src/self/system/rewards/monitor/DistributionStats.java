package org.self.system.rewards.monitor;

import org.self.objects.MiniNumber;

public class DistributionStats {
    private MiniNumber totalAmount;
    private int distributionCount;
    private double averageAmount;
    private MiniNumber maxAmount;
    private MiniNumber minAmount;
    private int successCount;
    
    public DistributionStats() {
        totalAmount = MiniNumber.ZERO;
        distributionCount = 0;
        averageAmount = 0.0;
        maxAmount = MiniNumber.ZERO;
        minAmount = MiniNumber.ZERO;
        successCount = 0;
    }
    
    public void updateTotalAmount(MiniNumber zAmount) {
        totalAmount = totalAmount.add(zAmount);
        
        // Update min/max
        if (zAmount.compareTo(maxAmount) > 0) {
            maxAmount = zAmount;
        }
        if (zAmount.compareTo(minAmount) < 0 || minAmount.equals(MiniNumber.ZERO)) {
            minAmount = zAmount;
        }
        
        // Update average
        averageAmount = totalAmount.toDouble() / distributionCount;
    }
    
    public void incrementDistributionCount() {
        distributionCount++;
    }
    
    public void incrementSuccessCount() {
        successCount++;
    }
    
    public MiniNumber getTotalAmount() {
        return totalAmount;
    }
    
    public int getDistributionCount() {
        return distributionCount;
    }
    
    public double getAverageAmount() {
        return averageAmount;
    }
    
    public MiniNumber getMaxAmount() {
        return maxAmount;
    }
    
    public MiniNumber getMinAmount() {
        return minAmount;
    }
    
    public double getSuccessRate() {
        return distributionCount > 0 ? 
            (successCount / (double) distributionCount) * 100 : 0;
    }
    
    public String toString() {
        return String.format(
            "DistributionStats[total=%s, count=%d, avg=%.2f, max=%s, min=%s, success=%.2f%%]",
            totalAmount.toString(),
            distributionCount,
            averageAmount,
            maxAmount.toString(),
            minAmount.toString(),
            getSuccessRate()
        );
    }
}
