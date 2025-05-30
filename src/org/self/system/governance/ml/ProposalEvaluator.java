package org.self.system.governance.ml;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.GovernanceProposal;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class ProposalEvaluator {
    private static ProposalEvaluator instance;
    private Map<String, Double> proposalScores;
    private Map<String, Double> featureWeights;
    private Random random;
    private static final AICapacityNumber MAX_STAKE = new AICapacityNumber(SELFParams.MAX_STAKE_FOR_VOTE.toDouble());
    
    private ProposalEvaluator() {
        proposalScores = new HashMap<>();
        initializeFeatureWeights();
        random = new Random();
    }
    
    public static ProposalEvaluator getInstance() {
        if (instance == null) {
            instance = new ProposalEvaluator();
        }
        return instance;
    }
    
    private void initializeFeatureWeights() {
        featureWeights = new HashMap<>();
        featureWeights.put("stake", 0.3);
        featureWeights.put("reputation", 0.2);
        featureWeights.put("resource_efficiency", 0.2);
        featureWeights.put("network_impact", 0.15);
        featureWeights.put("consensus_score", 0.15);
    }
    
    public double evaluateProposal(GovernanceProposal zProposal) {
        String proposalID = zProposal.getProposalID().toString();
        
        // Check if we already have a score
        if (proposalScores.containsKey(proposalID)) {
            return proposalScores.get(proposalID);
        }
        
        // Calculate features
        Map<String, Double> features = calculateFeatures(zProposal);
        
        // Calculate weighted score
        double score = 0.0;
        for (Map.Entry<String, Double> entry : features.entrySet()) {
            String feature = entry.getKey();
            double value = entry.getValue();
            double weight = featureWeights.get(feature);
            score += value * weight;
        }
        
        // Normalize score
        score = Math.max(0.0, Math.min(1.0, score));
        
        // Store and return score
        proposalScores.put(proposalID, score);
        return score;
    }
    
    private Map<String, Double> calculateFeatures(GovernanceProposal zProposal) {
        Map<String, Double> features = new HashMap<>();
        
        // Calculate stake feature
        MiniNumber totalStake = calculateTotalStake(zProposal);
        double stakeScore = totalStake.toDouble() / SELFParams.MAX_STAKE_FOR_VOTE.toDouble();
        features.put("stake", stakeScore);
        
        // Calculate reputation feature
        double reputationScore = calculateReputationScore(zProposal);
        features.put("reputation", reputationScore);
        
        // Calculate resource efficiency
        double resourceScore = calculateResourceEfficiency(zProposal);
        features.put("resource_efficiency", resourceScore);
        
        // Calculate network impact
        double impactScore = calculateNetworkImpact(zProposal);
        features.put("network_impact", impactScore);
        
        // Calculate consensus score
        double consensusScore = calculateConsensusScore(zProposal);
        features.put("consensus_score", consensusScore);
        
        return features;
    }
    
    private AICapacityNumber calculateTotalStake(GovernanceProposal zProposal) {
        AICapacityNumber total = AICapacityNumber.ZERO;
        for (Map.Entry<AIData, Boolean> entry : zProposal.getVotes().entrySet()) {
            if (entry.getValue()) {
                AICapacityNumber stake = AIVotingSystem.getInstance().getValidator(entry.getKey()).getStake();
                total = total.add(stake);
            }
        }
        return total;
    }
    
    private double calculateReputationScore(GovernanceProposal zProposal) {
        double totalReputation = 0.0;
        int totalValidators = 0;
        
        for (Map.Entry<AIData, Boolean> entry : zProposal.getVotes().entrySet()) {
            if (entry.getValue()) {
                AIValidator validator = AIVotingSystem.getInstance().getValidator(entry.getKey());
                totalReputation += validator.getReputation().getAsDouble();
                totalValidators++;
            }
        }
        
        return totalValidators > 0 ? totalReputation / totalValidators : 0.0;
        return score;
    }
    
    private double calculateResourceEfficiency(GovernanceProposal zProposal) {
        // Calculate resource usage
        double resources = zProposal.getResourcesUsed().toDouble();
        double maxResources = SELFParams.MAX_RESOURCE_ALLOCATION.toDouble();
        
        // Calculate efficiency score (1 - (resources/maxResources))
        return Math.max(0.0, 1.0 - (resources / maxResources));
    }
    
    private double calculateNetworkImpact(GovernanceProposal zProposal) {
        // Calculate impact based on affected nodes
        int affectedNodes = zProposal.getAffectedNodes().size();
        int totalNodes = CloudNodeRegistry.getInstance().getTotalNodes();
        
        // Impact score = 1 - (affected/total)
        return Math.max(0.0, 1.0 - ((double) affectedNodes / totalNodes));
    }
    
    private double calculateConsensusScore(GovernanceProposal zProposal) {
        return zProposal.getApprovalPercentage().toDouble() / 100.0;
    }
    
    public double getScore(GovernanceProposal zProposal) {
        String proposalID = zProposal.getProposalID().toString();
        return proposalScores.getOrDefault(proposalID, 0.0);
    }
    
    public void updateFeatureWeights(Map<String, Double> zNewWeights) {
        featureWeights.putAll(zNewWeights);
    }
    
    public Map<String, Double> getFeatureWeights() {
        return new HashMap<>(featureWeights);
    }
}
