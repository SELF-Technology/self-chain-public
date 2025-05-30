package org.self.system.governance.hex;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.GovernanceProposal;
import org.self.system.governance.ai.AIValidator;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class HexValidator {
    private static HexValidator instance;
    private Map<AIData, String> validatorColors;
    private Random random;
    
    private HexValidator() {
        validatorColors = new HashMap<>();
        random = new Random();
    }
    
    public static HexValidator getInstance() {
        if (instance == null) {
            instance = new HexValidator();
        }
        return instance;
    }
    
    public void registerValidator(AIData zValidatorID) {
        if (!validatorColors.containsKey(zValidatorID)) {
            String color = generateHexColor();
            validatorColors.put(zValidatorID, color);
            SelfLogger.log("Hex validator registered: " + zValidatorID + " - " + color);
        }
    }
    
    private String generateHexColor() {
        StringBuilder hex = new StringBuilder("#000000");
        for (int i = 0; i < 6; i++) {
            hex.setCharAt(i + 1, "0123456789ABCDEF".charAt(random.nextInt(16)));
        }
        return hex.toString();
    }
    
    public List<AIData> selectValidators(GovernanceProposal zProposal, int zNumValidators) {
        List<AIData> eligibleValidators = new ArrayList<>();
        AIVotingSystem votingSystem = AIVotingSystem.getInstance();
        
        // Get all validators
        for (AIValidator validator : votingSystem.getValidators()) {
            if (validator.canVote(zProposal)) {
                eligibleValidators.add(validator.getValidatorID());
            }
        }
        
        // Select validators using hex-based algorithm
        return selectHexValidators(eligibleValidators, zNumValidators);
    }
    
    private List<AIData> selectHexValidators(List<AIData> zEligibleValidators, int zNumValidators) {
        List<AIData> selected = new ArrayList<>();
        
        // Convert proposal ID to hex
        String hexProposal = convertToHex(zEligibleValidators.get(0));
        
        // Calculate validator scores
        Map<AIData, Double> validatorScores = new HashMap<>();
        for (AIData validatorID : zEligibleValidators) {
            double score = calculateHexScore(validatorID, hexProposal);
            validatorScores.put(validatorID, score);
        }
        
        // Select top validators
        while (selected.size() < zNumValidators && !validatorScores.isEmpty()) {
            AIData bestValidator = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            
            for (Map.Entry<AIData, Double> entry : validatorScores.entrySet()) {
                if (entry.getValue() > bestScore) {
                    bestScore = entry.getValue();
                    bestValidator = entry.getKey();
                }
            }
            
            if (bestValidator != null) {
                selected.add(bestValidator);
                validatorScores.remove(bestValidator);
            }
        }
        
        return selected;
    }
    
    private double calculateHexScore(AIData zValidatorID, String zHexProposal) {
        String validatorColor = validatorColors.get(zValidatorID);
        
        // Calculate similarity score
        double score = 0.0;
        for (int i = 1; i < 7; i++) {
            char validatorChar = validatorColor.charAt(i);
            char proposalChar = zHexProposal.charAt(i);
            
            // Calculate difference (0-15)
            int diff = Math.abs(validatorChar - proposalChar);
            
            // Normalize to 0-1
            score += 1.0 - (diff / 15.0);
        }
        
        // Average score
        return score / 6.0;
    }
    
    private String convertToHex(AIData zData) {
        byte[] bytes = zData.getBytes();
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
    
    public String getValidatorColor(AIData zValidatorID) {
        return validatorColors.get(zValidatorID);
    }
    
    public void updateValidatorColor(AIData zValidatorID) {
        validatorColors.put(zValidatorID, generateHexColor());
    }
    
    public Map<MiniData, String> getValidatorColors() {
        return new HashMap<>(validatorColors);
    }
}
