package org.self.system.governance.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.GovernanceProposal;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class AIValidator {
    private static AIValidator instance;
    private AIData validatorID;
    private String hexColor;
    private AICapacityNumber stake;
    private List<AIData> votedProposals;
    private Map<String, Double> validationMetrics;
    private Random random;
    private ReputationSystem reputationSystem;
    
    public AIValidator(MiniData zValidatorID) {
        validatorID = new AIData(zValidatorID);
        hexColor = generateHexColor();
        stake = new AICapacityNumber(0);
        votedProposals = new ArrayList<>();
        validationMetrics = new HashMap<>();
        random = new Random();
        reputationSystem = ReputationSystem.getInstance();
        initializeMetrics();
    }
    
    private void initializeMetrics() {
        validationMetrics.put("pattern_match", 0.4);
        validationMetrics.put("consensus", 0.3);
        validationMetrics.put("reputation", 0.2);
        validationMetrics.put("stake", 0.1);
    }
    
    private String generateHexColor() {
        StringBuilder hex = new StringBuilder("#000000");
        for (int i = 0; i < 6; i++) {
            hex.setCharAt(i + 1, "0123456789ABCDEF".charAt(random.nextInt(16)));
        }
        return hex.toString();
    }
    
    public boolean validateProposal(GovernanceProposal zProposal) {
        // Convert proposal ID to hex
        String hexProposal = convertToHex(zProposal.getProposalID());
        
        // Calculate new hex color
        String newColor = calculateNewColor(hexProposal);
        
        // Calculate validation score
        double score = calculateValidationScore(zProposal);
        
        // Update reputation
        double reputationChange = score * SELFParams.REPUTATION_CHANGE_FACTOR.toDouble();
        reputationSystem.updateReputation(validatorID, "validation", reputationChange);
        
        // Store the vote
        votedProposals.add(zProposal.getProposalID());
        
        // Return validation result
        return newColor.equals(hexColor) && score >= SELFParams.MIN_VALIDATION_SCORE.toDouble();
    }
    
    private double calculateValidationScore(GovernanceProposal zProposal) {
        double score = 0.0;
        
        // Pattern matching score
        String hexProposal = convertToHex(zProposal.getProposalID());
        String newColor = calculateNewColor(hexProposal);
        score += (newColor.equals(hexColor) ? 1.0 : 0.0) * validationMetrics.get("pattern_match");
        
        // Consensus score
        double consensus = zProposal.getApprovalPercentage().toDouble() / 100.0;
        score += consensus * validationMetrics.get("consensus");
        
        // Reputation score
        double reputation = reputationSystem.getReputation(validatorID);
        score += reputation * validationMetrics.get("reputation");
        
        // Stake score
        double stakeScore = stake.toDouble() / SELFParams.MAX_STAKE.toDouble();
        score += stakeScore * validationMetrics.get("stake");
        
        return score;
    }
    
    private String convertToHex(AIData zData) {
        return zData.toString();
    }
    
    private String calculateNewColor(String zHexProposal) {
        // Split hex into parts
        String[] parts = zHexProposal.split("(");
        
        // Convert each part to decimal and sum
        int sum = 0;
        for (String part : parts) {
            sum += Integer.parseInt(part, 16);
        }
        
        // Create new hex color
        StringBuilder newColor = new StringBuilder("#000000");
        for (int i = 0; i < 6; i++) {
            int value = (sum + hexColor.charAt(i + 1)) % 16;
            newColor.setCharAt(i + 1, "0123456789ABCDEF".charAt(value));
        }
        
        return newColor.toString();
    }
    
    public void updateStake(MiniNumber zAmount) {
        stake = stake.add(zAmount);
        
        // Update reputation based on stake change
        double stakeScore = stake.toDouble() / SELFParams.MAX_STAKE.toDouble();
        double reputationChange = stakeScore * SELFParams.STAKE_REPUTATION_FACTOR.toDouble();
        reputationSystem.updateReputation(validatorID, "stake_update", reputationChange);
    }
    
    public MiniData getValidatorID() {
        return validatorID;
    }
    
    public String getHexColor() {
        return hexColor;
    }
    
    public MiniNumber getStake() {
        return stake;
    }
    
    public List<MiniData> getVotedProposals() {
        return new ArrayList<>(votedProposals);
    }
}
