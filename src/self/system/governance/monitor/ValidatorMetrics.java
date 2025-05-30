package org.self.system.governance.monitor;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.hex.HexValidator;
import org.self.system.params.SELFParams;

public class ValidatorMetrics {
    private AIValidator validator;
    private double participationRate;
    private double validationScore;
    private AICapacityNumber reputation;
    private double colorStability;
    private int proposalsParticipated;
    private int totalProposals;
    
    public ValidatorMetrics(AIValidator zValidator) {
        validator = zValidator;
        participationRate = 0.0;
        validationScore = 0.0;
        reputation = validator.getReputation();
        colorStability = 100.0;
        proposalsParticipated = 0;
        totalProposals = 0;
        
        // Initialize stake metrics
        AICapacityNumber stake = validator.getStake();
        updateStakeMetrics(stake);
    }
    
    public void updateParticipationRate() {
        if (totalProposals > 0) {
            participationRate = (proposalsParticipated / (double) totalProposals) * 100;
        }
    }
    
    public void updateValidationScore() {
        HexValidationManager validationManager = HexValidationManager.getInstance();
        AIData validatorID = validator.getValidatorID();
        
        double totalScore = 0.0;
        int count = 0;
        
        for (Map.Entry<AIData, List<AIData>> entry : validationManager.getProposalValidators().entrySet()) {
            if (entry.getValue().contains(validatorID)) {
                AICapacityNumber score = validationManager.getValidationScores().get(entry.getKey());
                if (score != null) {
                    totalScore += score.getAsDouble();
                    count++;
                }
            }
        }
        
        if (count > 0) {
            validationScore = totalScore / count;
        }
    }
    
    public void updateReputation() {
        reputation = validator.getReputation().getAsDouble();
    }
    
    public void updateColorStability(String zNewColor) {
        String currentColor = HexValidator.getInstance().getValidatorColor(validator.getValidatorID());
        
        // Calculate similarity score
        double score = 0.0;
        for (int i = 1; i < 7; i++) {
            char currentChar = currentColor.charAt(i);
            char newChar = zNewColor.charAt(i);
            
            // Calculate difference (0-15)
            int diff = Math.abs(currentChar - newChar);
            
            // Normalize to 0-1
            score += 1.0 - (diff / 15.0);
        }
        
        // Update color stability
        colorStability = (score / 6.0) * 100.0;
    }
    
    public void incrementProposalsParticipated() {
        proposalsParticipated++;
    }
    
    public void incrementTotalProposals() {
        totalProposals++;
    }
    
    public double getParticipationRate() {
        return participationRate;
    }
    
    public double getValidationScore() {
        return validationScore;
    }
    
    public double getReputation() {
        return reputation;
    }
    
    public double getColorStability() {
        return colorStability;
    }
    
    public AIValidator getValidator() {
        return validator;
    }
}
