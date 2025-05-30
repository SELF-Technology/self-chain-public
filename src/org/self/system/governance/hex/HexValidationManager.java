package org.self.system.governance.hex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.system.governance.GovernanceProposal;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.points.PointSystem;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class HexValidationManager {
    private static HexValidationManager instance;
    private Map<MiniData, List<MiniData>> proposalValidators;
    private Map<MiniData, MiniNumber> validationScores;
    private Random random;
    
    private HexValidationManager() {
        proposalValidators = new HashMap<>();
        validationScores = new HashMap<>();
        random = new Random();
    }
    
    public static HexValidationManager getInstance() {
        if (instance == null) {
            instance = new HexValidationManager();
        }
        return instance;
    }
    
    public List<MiniData> selectProposalValidators(GovernanceProposal zProposal) {
        // Calculate number of validators needed
        int numValidators = Math.min(
            SELFParams.MAX_PROPOSALS_PER_NODE.toLong(),
            AIVotingSystem.getInstance().getValidators().size()
        );
        
        // Select validators using hex-based algorithm
        List<MiniData> validators = HexValidator.getInstance().selectValidators(zProposal, numValidators);
        
        // Store validators for proposal
        proposalValidators.put(zProposal.getProposalID(), validators);
        
        return validators;
    }
    
    public boolean validateProposal(MiniData zValidatorID, GovernanceProposal zProposal, boolean zVote) {
        // Check if validator is eligible
        List<MiniData> validators = proposalValidators.get(zProposal.getProposalID());
        if (validators == null || !validators.contains(zValidatorID)) {
            return false;
        }
        
        // Get validator color
        String validatorColor = HexValidator.getInstance().getValidatorColor(zValidatorID);
        
        // Convert proposal ID to hex
        String hexProposal = convertToHex(zProposal.getProposalID());
        
        // Calculate validation score
        double score = calculateValidationScore(validatorColor, hexProposal);
        
        // Update validation score
        MiniNumber currentScore = validationScores.getOrDefault(zProposal.getProposalID(), MiniNumber.ZERO);
        validationScores.put(zProposal.getProposalID(), currentScore.add(new MiniNumber(score)));
        
        // Update points based on validation
        PointSystem pointSystem = PointSystem.getInstance();
        MiniNumber points = calculatePoints(zValidatorID, zProposal, score);
        pointSystem.allocatePoints(zValidatorID, points);
        
        return true;
    }
    
    private double calculateValidationScore(String zValidatorColor, String zHexProposal) {
        double score = 0.0;
        for (int i = 1; i < 7; i++) {
            char validatorChar = zValidatorColor.charAt(i);
            char proposalChar = zHexProposal.charAt(i);
            
            // Calculate difference (0-15)
            int diff = Math.abs(validatorChar - proposalChar);
            
            // Normalize to 0-1
            score += 1.0 - (diff / 15.0);
        }
        
        // Average score
        return score / 6.0;
    }
    
    private MiniNumber calculatePoints(MiniData zValidatorID, 
                                      GovernanceProposal zProposal, 
                                      double zScore) {
        // Base points based on stake
        MiniNumber basePoints = SELFParams.MIN_STAKE_FOR_VOTE;
        
        // Add validation bonus
        MiniNumber bonus = basePoints.multiply(new MiniNumber(zScore));
        
        // Add reputation bonus
        AIValidator validator = AIVotingSystem.getInstance().getValidator(zValidatorID);
        MiniNumber reputationBonus = basePoints.multiply(validator.getReputation().divide(SELFParams.MAX_REPUTATION));
        
        return basePoints.add(bonus).add(reputationBonus);
    }
    
    private String convertToHex(MiniData zData) {
        byte[] bytes = zData.getBytes();
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
    
    public Map<MiniData, List<MiniData>> getProposalValidators() {
        return new HashMap<>(proposalValidators);
    }
    
    public Map<MiniData, MiniNumber> getValidationScores() {
        return new HashMap<>(validationScores);
    }
    
    public void reset() {
        proposalValidators.clear();
        validationScores.clear();
        HexValidator.getInstance().reset();
    }
}
