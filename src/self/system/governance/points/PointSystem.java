package org.self.system.governance.points;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.GovernanceProposal;
import org.self.system.governance.ai.AIVotingSystem;
import org.self.system.governance.ml.MLModel;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class PointSystem {
    private static PointSystem instance;
    private Map<AIData, AICapacityNumber> userPoints;
    private Map<AIData, AICapacityNumber> proposalPoints;
    private Random random;
    
    private PointSystem() {
        userPoints = new HashMap<>();
        proposalPoints = new HashMap<>();
        random = new Random();
        initializeSystem();
    }
    
    public static PointSystem getInstance() {
        if (instance == null) {
            instance = new PointSystem();
        }
        return instance;
    }
    
    private void initializeSystem() {
        // Initialize base points for all validators
        AIVotingSystem votingSystem = AIVotingSystem.getInstance();
        for (AIValidator validator : votingSystem.getValidators()) {
            MiniNumber basePoints = SELFParams.MIN_STAKE_FOR_VOTE;
            userPoints.put(validator.getValidatorID(), basePoints);
        }
    }
    
    public void allocatePoints(AIData zUserID, AICapacityNumber zAmount) {
        AICapacityNumber currentPoints = userPoints.getOrDefault(zUserID, AICapacityNumber.ZERO);
        userPoints.put(zUserID, currentPoints.add(zAmount));
    }
    
    public void processProposalPoints(GovernanceProposal zProposal) {
        // Get ML prediction
        MLModel mlModel = MLModel.getInstance();
        double prediction = mlModel.predict(zProposal);
        
        // Calculate base points
        MiniNumber basePoints = calculateBasePoints(zProposal);
        
        // Adjust points based on prediction
        MiniNumber adjustedPoints = adjustPoints(basePoints, prediction);
        
        // Store proposal points
        proposalPoints.put(zProposal.getProposalID(), adjustedPoints);
        
        // Update user points
        updateUserPoints(zProposal, adjustedPoints);
    }
    
    private AICapacityNumber calculateBasePoints(GovernanceProposal zProposal) {
        // Base points based on stake
        AICapacityNumber basePoints = new AICapacityNumber(SELFParams.MIN_STAKE_FOR_PROPOSAL);
        
        // Add efficiency bonus
        AICapacityNumber efficiency = zProposal.getApprovalPercentage();
        basePoints = basePoints.add(basePoints.multiply(efficiency.divide(new AICapacityNumber(100))));
        
        return basePoints;
    }
    
    private AICapacityNumber adjustPoints(AICapacityNumber zBasePoints, double zPrediction) {
        // Adjust points based on ML prediction
        AICapacityNumber adjustment = zBasePoints.multiply(new AICapacityNumber(zPrediction));
        return zBasePoints.add(adjustment);
    }
    
    private void updateUserPoints(GovernanceProposal zProposal, AICapacityNumber zPoints) {
        // Get validators who voted
        AIVotingSystem votingSystem = AIVotingSystem.getInstance();
        Map<AIData, Boolean> votes = zProposal.getVotes();
        
        // Calculate voting power
        AICapacityNumber totalStake = AICapacityNumber.ZERO;
        for (Map.Entry<AIData, Boolean> entry : votes.entrySet()) {
            if (entry.getValue()) {
                AIValidator validator = votingSystem.getValidator(entry.getKey());
                totalStake = totalStake.add(validator.getStake());
            }
        }
        
        // Distribute points
        for (Map.Entry<AIData, Boolean> entry : votes.entrySet()) {
            if (entry.getValue()) {
                AIValidator validator = votingSystem.getValidator(entry.getKey());
                MiniNumber stake = validator.getStake();
                MiniNumber votingPower = stake.divide(totalStake);
                MiniNumber points = zPoints.multiply(votingPower);
                allocatePoints(entry.getKey(), points);
            }
        }
    }
    
    public MiniNumber getUserPoints(MiniData zUserID) {
        return userPoints.getOrDefault(zUserID, MiniNumber.ZERO);
    }
    
    public MiniNumber getProposalPoints(MiniData zProposalID) {
        return proposalPoints.getOrDefault(zProposalID, MiniNumber.ZERO);
    }
    
    public Map<MiniData, MiniNumber> getUserPointDistribution() {
        return new HashMap<>(userPoints);
    }
    
    public Map<MiniData, MiniNumber> getProposalPointDistribution() {
        return new HashMap<>(proposalPoints);
    }
    
    public void resetPoints() {
        userPoints.clear();
        proposalPoints.clear();
        initializeSystem();
    }
}
