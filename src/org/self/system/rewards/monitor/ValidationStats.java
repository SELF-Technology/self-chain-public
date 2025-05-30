package org.self.system.rewards.monitor;

import org.self.objects.MiniNumber;

public class ValidationStats {
    private double totalScore;
    private int validationCount;
    private double averageScore;
    private double maxScore;
    private double minScore;
    private MiniNumber totalAmount;
    private MiniNumber validatedAmount;
    private int successCount;
    
    public ValidationStats() {
        totalScore = 0.0;
        validationCount = 0;
        averageScore = 0.0;
        maxScore = 0.0;
        minScore = Double.MAX_VALUE;
        totalAmount = MiniNumber.ZERO;
        validatedAmount = MiniNumber.ZERO;
        successCount = 0;
    }
    
    public void updateScore(double zScore) {
        totalScore += zScore;
        validationCount++;
        
        // Update min/max
        if (zScore > maxScore) {
            maxScore = zScore;
        }
        if (zScore < minScore) {
            minScore = zScore;
        }
        
        // Update average
        averageScore = totalScore / validationCount;
    }
    
    public void updateAmount(MiniNumber zAmount, boolean zValidated) {
        totalAmount = totalAmount.add(zAmount);
        if (zValidated) {
            validatedAmount = validatedAmount.add(zAmount);
            successCount++;
        }
    }
    
    public void incrementValidationCount() {
        validationCount++;
    }
    
    public double getTotalScore() {
        return totalScore;
    }
    
    public int getValidationCount() {
        return validationCount;
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
    
    public MiniNumber getTotalAmount() {
        return totalAmount;
    }
    
    public MiniNumber getValidatedAmount() {
        return validatedAmount;
    }
    
    public double getSuccessRate() {
        return validationCount > 0 ? 
            (successCount / (double) validationCount) * 100 : 0;
    }
    
    public double getValidationRate() {
        return totalAmount.compareTo(MiniNumber.ZERO) > 0 ? 
            (validatedAmount.toDouble() / totalAmount.toDouble()) * 100 : 0;
    }
    
    public String toString() {
        return String.format(
            "ValidationStats[totalScore=%.2f, count=%d, avg=%.2f, max=%.2f, min=%.2f, " +
            "totalAmount=%s, validatedAmount=%s, success=%.2f%%, rate=%.2f%%]",
            totalScore,
            validationCount,
            averageScore,
            maxScore,
            minScore,
            totalAmount.toString(),
            validatedAmount.toString(),
            getSuccessRate(),
            getValidationRate()
        );
    }
}
