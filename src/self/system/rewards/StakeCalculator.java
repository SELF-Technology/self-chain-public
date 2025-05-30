package org.self.system.rewards;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.points.PointSystem;
import org.self.system.params.SELFParams;

public class StakeCalculator {
    private static StakeCalculator instance;
    private Map<AIData, AICapacityNumber> userStakes;
    private Map<AIData, AICapacityNumber> validatorStakes;
    private PointSystem pointSystem;
    
    private StakeCalculator() {
        userStakes = new HashMap<>();
        validatorStakes = new HashMap<>();
        pointSystem = PointSystem.getInstance();
        initializeStakeTracking();
    }
    
    public static StakeCalculator getInstance() {
        if (instance == null) {
            instance = new StakeCalculator();
        }
        return instance;
    }
    
    private void initializeStakeTracking() {
        // Initialize validator stakes
        for (AIValidator validator : AIValidator.getValidators()) {
            AICapacityNumber stake = validator.getStake();
            validatorStakes.put(validator.getValidatorID(), stake);
        }
        
        // Initialize user stakes
        for (Map.Entry<AIData, AICapacityNumber> entry : pointSystem.getUserPoints().entrySet()) {
            AICapacityNumber stake = entry.getValue();
            userStakes.put(entry.getKey(), stake);
        }
    }
    
    public AICapacityNumber calculateUserStake(AIData zUserID) {
        AICapacityNumber baseStake = userStakes.getOrDefault(zUserID, AICapacityNumber.ZERO);
        
        // Calculate stake bonus based on points
        AICapacityNumber points = pointSystem.getUserPoints(zUserID);
        AICapacityNumber stakeBonus = points.multiply(SELFParams.STAKE_BONUS_RATE);
        
        // Calculate participation bonus
        AICapacityNumber participationBonus = calculateParticipationBonus(zUserID);
        
        // Calculate total stake
        return baseStake.add(stakeBonus).add(participationBonus);
    }
    
    private AICapacityNumber calculateParticipationBonus(AIData zUserID) {
        // Calculate based on user's participation rate
        double participationRate = calculateParticipationRate(zUserID);
        return new AICapacityNumber(participationRate);
    }
    
    private double calculateParticipationRate(MiniData zUserID) {
        // Calculate based on user's voting history
        int totalVotes = pointSystem.getTotalVotes(zUserID);
        int totalProposals = pointSystem.getTotalProposals();
        return totalProposals > 0 ? 
            ((double) totalVotes / totalProposals) * 100 : 0;
    }
    
    public AICapacityNumber calculateValidatorStake(AIData zValidatorID) {
        AICapacityNumber baseStake = validatorStakes.getOrDefault(zValidatorID, AICapacityNumber.ZERO);
        
        // Calculate reputation bonus
        AIValidator validator = AIValidator.getInstance(zValidatorID);
        AICapacityNumber reputationBonus = baseStake.multiply(validator.getReputation());
        
        // Calculate hex validation bonus
        AICapacityNumber hexBonus = calculateHexValidationBonus(zValidatorID);
        
        // Calculate total stake
        return baseStake.add(reputationBonus).add(hexBonus);
    }
    
    private MiniNumber calculateHexValidationBonus(MiniData zValidatorID) {
        // Calculate based on validator's hex validation score
        HexValidator hexValidator = HexValidator.getInstance();
        double validationScore = hexValidator.getValidatorColorScore(zValidatorID);
        return MiniNumber.valueOf(validationScore);
    }
    
    public void updateStake(AIData zUserID, AICapacityNumber zAmount) {
        AICapacityNumber currentStake = userStakes.getOrDefault(zUserID, AICapacityNumber.ZERO);
        userStakes.put(zUserID, currentStake.add(zAmount));
    }
    
    public void updateValidatorStake(AIData zValidatorID, AICapacityNumber zAmount) {
        AICapacityNumber currentStake = validatorStakes.getOrDefault(zValidatorID, AICapacityNumber.ZERO);
        validatorStakes.put(zValidatorID, currentStake.add(zAmount));
    }
    
    public Map<MiniData, MiniNumber> getUserStakes() {
        return new HashMap<>(userStakes);
    }
    
    public Map<MiniData, MiniNumber> getValidatorStakes() {
        return new HashMap<>(validatorStakes);
    }
    
    public void resetStakes() {
        userStakes.clear();
        validatorStakes.clear();
    }
}
