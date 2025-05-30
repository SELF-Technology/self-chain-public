package org.self.system.rewards;

import org.self.objects.ai.AICapacityNumber;

public class RewardDistribution {
    private AICapacityNumber totalAmount;
    private int distributionCount;
    private double averageAmount;
    private AICapacityNumber maxAmount;
    private AICapacityNumber minAmount;
    
    public RewardDistribution() {
        totalAmount = AICapacityNumber.ZERO;
        distributionCount = 0;
        averageAmount = 0.0;
        maxAmount = AICapacityNumber.ZERO;
        minAmount = AICapacityNumber.ZERO;
    }
    
    public void updateTotalAmount(AICapacityNumber zAmount) {
        totalAmount = totalAmount.add(zAmount);
        
        // Update min/max
        if (zAmount.compareTo(maxAmount) > 0) {
            maxAmount = zAmount;
        }
        if (zAmount.compareTo(minAmount) < 0 || minAmount.equals(AICapacityNumber.ZERO)) {
            minAmount = zAmount;
        }
        
        // Update average
        averageAmount = totalAmount.getAsDouble() / distributionCount;
    }
    
    public void incrementDistributionCount() {
        distributionCount++;
    }
    
    public AICapacityNumber getTotalAmount() {
        return totalAmount;
    }
    
    public int getDistributionCount() {
        return distributionCount;
    }
    
    public double getAverageAmount() {
        return averageAmount;
    }
    
    public AICapacityNumber getMaxAmount() {
        return maxAmount;
    }
    
    public AICapacityNumber getMinAmount() {
        return minAmount;
    }
    
    public String toString() {
        return String.format(
            "Distribution[total=%s, count=%d, avg=%.2f, max=%s, min=%s]",
            totalAmount.toString(),
            distributionCount,
            averageAmount,
            maxAmount.toString(),
            minAmount.toString()
        );
    }
}
