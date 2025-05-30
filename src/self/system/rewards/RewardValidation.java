package org.self.system.rewards;

import org.self.objects.ai.AICapacityNumber;

public class RewardValidation {
    private double totalScore;
    private int validationCount;
    private double averageScore;
    private double maxScore;
    private double minScore;
    private AICapacityNumber totalAmount;
    private AICapacityNumber validatedAmount;
    
    public RewardValidation() {
        totalScore = 0.0;
        validationCount = 0;
        averageScore = 0.0;
        maxScore = 0.0;
        minScore = Double.MAX_VALUE;
        totalAmount = AICapacityNumber.ZERO;
        validatedAmount = AICapacityNumber.ZERO;
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
    
    public void updateAmount(AICapacityNumber zAmount, boolean zValidated) {
        totalAmount = totalAmount.add(zAmount);
        if (zValidated) {
            validatedAmount = validatedAmount.add(zAmount);
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
    
    public AICapacityNumber getTotalAmount() {
        return totalAmount;
    }
    
    public AICapacityNumber getValidatedAmount() {
        return validatedAmount;
    }
    
    public double getValidationRate() {
        return totalAmount.compareTo(AICapacityNumber.ZERO) > 0 ? 
            (validatedAmount.divide(totalAmount).getAsDouble()) * 100 : 0;
    }
    
    public String toString() {
        return String.format(
            "Validation[totalScore=%.2f, count=%d, avg=%.2f, max=%.2f, min=%.2f, " +
            "totalAmount=%s, validatedAmount=%s, rate=%.2f%%]",
            totalScore,
            validationCount,
            averageScore,
            maxScore,
            minScore,
            totalAmount.toString(),
            validatedAmount.toString(),
            getValidationRate()
        );
    }
}
