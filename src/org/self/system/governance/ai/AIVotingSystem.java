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

public class AIVotingSystem {
    private static AIVotingSystem instance;
    private Map<AIData, AIValidator> validators;
    private Map<AIData, AICapacityNumber> proposalPoints;
    private Random random;
    
    private AIVotingSystem() {
        validators = new HashMap<>();
        proposalPoints = new HashMap<>();
        random = new Random();
    }
    
    public static AIVotingSystem getInstance() {
        if (instance == null) {
            instance = new AIVotingSystem();
        }
        return instance;
    }
    
    public void registerValidator(AIData zValidatorID, AICapacityNumber zStake) {
        if (!validators.containsKey(zValidatorID)) {
            AIValidator validator = new AIValidator(zValidatorID);
            validator.updateStake(zStake);
            validators.put(zValidatorID, validator);
            SelfLogger.log("Validator registered: " + zValidatorID);
        }
    }
    
    public void updateValidatorStake(AIData zValidatorID, AICapacityNumber zAmount) {
        AIValidator validator = validators.get(zValidatorID);
        if (validator != null) {
            validator.updateStake(zAmount);
        }
    }
    
    public List<AIValidator> selectValidators(GovernanceProposal zProposal) {
        List<AIValidator> eligibleValidators = new ArrayList<>();
        
        // Find eligible validators
        for (AIValidator validator : validators.values()) {
            if (validator.canVote(zProposal)) {
                eligibleValidators.add(validator);
            }
        }
        
        // Select random subset
        int numToSelect = Math.min(eligibleValidators.size(), SELFParams.MAX_PROPOSALS_PER_NODE.toLong());
        List<AIValidator> selected = new ArrayList<>();
        
        while (selected.size() < numToSelect && !eligibleValidators.isEmpty()) {
            int index = random.nextInt(eligibleValidators.size());
            selected.add(eligibleValidators.remove(index));
        }
        
        return selected;
    }
    
    public boolean processVote(AIData zValidatorID, GovernanceProposal zProposal, boolean zVote) {
        AIValidator validator = validators.get(zValidatorID);
        if (validator == null) {
            return false;
        }
        
        // Validate proposal
        if (!validator.validateProposal(zProposal)) {
            return false;
        }
        
        // Calculate points
        AICapacityNumber points = calculatePoints(validator, zProposal);
        
        // Update proposal points
        AICapacityNumber currentPoints = proposalPoints.getOrDefault(zProposal.getProposalID(), new AICapacityNumber(0));
        if (zVote) {
            proposalPoints.put(zProposal.getProposalID(), currentPoints.add(points));
        } else {
            proposalPoints.put(zProposal.getProposalID(), currentPoints.subtract(points));
        }
        
        return true;
    }
    
    private AICapacityNumber calculatePoints(AIValidator zValidator, GovernanceProposal zProposal) {
        // Base points based on stake
        AICapacityNumber points = zValidator.getStake().divide(new AICapacityNumber(SELFParams.MIN_STAKE_FOR_VOTE));
        
        // Add efficiency bonus
        double efficiency = zValidator.calculateEfficiency(zProposal);
        points = points.multiply(new AICapacityNumber(efficiency));
        
        return points;
    }
    
    public boolean isProposalApproved(GovernanceProposal zProposal) {
        AICapacityNumber points = proposalPoints.getOrDefault(zProposal.getProposalID(), new AICapacityNumber(0));
        AICapacityNumber totalStake = calculateTotalStake();
        
        if (totalStake.compareTo(new AICapacityNumber(0)) <= 0) {
            return false;
        }
        
        MiniNumber percentage = points.divide(totalStake).multiply(new MiniNumber(100));
        return percentage.compareTo(SELFParams.MIN_VOTE_THRESHOLD) >= 0;
    }
    
    private MiniNumber calculateTotalStake() {
        MiniNumber total = MiniNumber.ZERO;
        for (AIValidator validator : validators.values()) {
            total = total.add(validator.getStake());
        }
        return total;
    }
    
    public List<AIValidator> getValidators() {
        return new ArrayList<>(validators.values());
    }
    
    public Map<MiniData, MiniNumber> getProposalPoints() {
        return new HashMap<>(proposalPoints);
    }
}
