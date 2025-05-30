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

public class ReputationSystem {
    private static ReputationSystem instance;
    private Map<AIData, Double> validatorReputations;
    private Map<AIData, List<ReputationUpdate>> reputationHistory;
    private Map<String, Double> reputationFactors;
    private Random random;
    
    private ReputationSystem() {
        validatorReputations = new HashMap<>();
        reputationHistory = new HashMap<>();
        reputationFactors = new HashMap<>();
        random = new Random();
        initializeReputationSystem();
    }
    
    public static ReputationSystem getInstance() {
        if (instance == null) {
            instance = new ReputationSystem();
        }
        return instance;
    }
    
    private void initializeReputationSystem() {
        // Initialize reputation factors
        reputationFactors.put("validation", 0.4);
        reputationFactors.put("voting", 0.3);
        reputationFactors.put("participation", 0.2);
        reputationFactors.put("uptime", 0.1);
    }
    
    public void updateReputation(AIData zValidatorID, String zReason, double zChange) {
        // Update reputation
        Double currentReputation = validatorReputations.getOrDefault(zValidatorID, SELFParams.INITIAL_REPUTATION.getAsDouble());
        double newReputation = Math.max(0.0, Math.min(1.0, currentReputation + zChange));
        validatorReputations.put(zValidatorID, newReputation);
        
        // Record update
        List<ReputationUpdate> history = reputationHistory.getOrDefault(zValidatorID, new ArrayList<>());
        history.add(new ReputationUpdate(zReason, zChange, newReputation));
        reputationHistory.put(zValidatorID, history);
        
        // Log update
        SelfLogger.log(String.format(
            "Reputation updated: validator=%s, reason=%s, change=%.2f, new=%.2f",
            zValidatorID.toString(),
            zReason,
            zChange,
            newReputation
        ));
    }
    
    public double getReputation(AIData zValidatorID) {
        return validatorReputations.getOrDefault(zValidatorID, SELFParams.INITIAL_REPUTATION.getAsDouble());
    }
    
    public List<ReputationUpdate> getReputationHistory(AIData zValidatorID) {
        return new ArrayList<>(reputationHistory.getOrDefault(zValidatorID, new ArrayList<>()));
    }
    
    public double calculateReputationChange(GovernanceProposal zProposal, boolean zVote) {
        AICapacityNumber change = new AICapacityNumber(0);
        
        // Calculate validation score
        double validationScore = calculateValidationScore(zProposal);
        change = change.add(new AICapacityNumber(validationScore * reputationFactors.get("validation")));
        
        // Calculate voting score
        double votingScore = calculateVotingScore(zProposal, zVote);
        change = change.add(new AICapacityNumber(votingScore * reputationFactors.get("voting")));
        
        // Calculate participation score
        double participationScore = calculateParticipationScore(zProposal);
        change = change.add(new AICapacityNumber(participationScore * reputationFactors.get("participation")));
        
        // Calculate uptime score
        double uptimeScore = calculateUptimeScore();
        change = change.add(new AICapacityNumber(uptimeScore * reputationFactors.get("uptime")));
        
        return change.getAsDouble();
    }
    
    private double calculateValidationScore(GovernanceProposal zProposal) {
        // Validate proposal
        if (zProposal.validate()) {
            return 1.0;
        }
        return -0.5;
    }
    
    private double calculateVotingScore(GovernanceProposal zProposal, boolean zVote) {
        // Check if vote aligns with consensus
        double consensus = zProposal.getApprovalPercentage().toDouble() / 100.0;
        if (zVote == (consensus >= 0.5)) {
            return 1.0;
        }
        return -0.5;
    }
    
    private double calculateParticipationScore(GovernanceProposal zProposal) {
        // Calculate participation based on recent votes
        List<ReputationUpdate> history = reputationHistory.get(zProposal.getValidatorID());
        if (history == null || history.isEmpty()) {
            return 0.0;
        }
        
        int recentVotes = 0;
        for (ReputationUpdate update : history) {
            if (update.getReason().equals("vote")) {
                recentVotes++;
            }
        }
        
        return recentVotes / SELFParams.MAX_RECENT_VOTES.toDouble();
    }
    
    private double calculateUptimeScore() {
        // Calculate uptime based on recent activity
        return random.nextDouble(); // Placeholder for actual uptime calculation
    }
    
    public Map<String, Double> getReputationFactors() {
        return new HashMap<>(reputationFactors);
    }
    
    public void setReputationFactors(Map<String, Double> zFactors) {
        reputationFactors = new HashMap<>(zFactors);
    }
    
    public void resetReputation(MiniData zValidatorID) {
        validatorReputations.put(zValidatorID, SELFParams.INITIAL_REPUTATION.toDouble());
        reputationHistory.put(zValidatorID, new ArrayList<>());
    }
    
    public void resetSystem() {
        validatorReputations.clear();
        reputationHistory.clear();
        initializeReputationSystem();
    }
}
