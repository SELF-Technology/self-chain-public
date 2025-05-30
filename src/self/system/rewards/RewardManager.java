package org.self.system.rewards;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.hex.HexValidator;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;
import org.self.system.rewards.StakeCalculator;
import org.self.system.rewards.ParticipationRateCalculator;
import org.self.system.rewards.RewardMetrics;

public class RewardManager {
    private static RewardManager instance;
    private Map<MiniData, RewardRecord> rewardRecords;
    private Map<MiniData, MiniNumber> validatorRewards;
    private Map<MiniData, MiniNumber> userRewards;
    private Timer rewardTimer;
    private long rewardPeriod;
    private StakeCalculator stakeCalculator;
    private ParticipationRateCalculator participationRateCalculator;
    
    private RewardManager() {
        rewardRecords = new HashMap<>();
        validatorRewards = new HashMap<>();
        userRewards = new HashMap<>();
        rewardPeriod = SELFParams.SELF_REWARD_PERIOD.toLong();
        stakeCalculator = StakeCalculator.getInstance();
        participationRateCalculator = ParticipationRateCalculator.getInstance();
        initializeRewardSystem();
    }
    
    public static RewardManager getInstance() {
        if (instance == null) {
            instance = new RewardManager();
        }
        return instance;
    }
    
    private void initializeRewardSystem() {
        rewardTimer = new Timer();
        rewardTimer.schedule(new RewardTask(), 0, rewardPeriod);
    }
    
    public void addValidatorReward(AIValidator zValidator, MiniNumber zAmount) {
        MiniData validatorID = zValidator.getValidatorID();
        MiniNumber currentReward = validatorRewards.getOrDefault(validatorID, MiniNumber.ZERO);
        validatorRewards.put(validatorID, currentReward.add(zAmount));
        
        // Create reward record
        RewardRecord record = new RewardRecord(
            validatorID,
            zAmount,
            "validator_reward",
            System.currentTimeMillis()
        );
        
        rewardRecords.put(validatorID, record);
    }
    
    public void addUserReward(MiniData zUserID, MiniNumber zAmount) {
        MiniNumber currentReward = userRewards.getOrDefault(zUserID, MiniNumber.ZERO);
        userRewards.put(zUserID, currentReward.add(zAmount));
        
        // Create reward record
        RewardRecord record = new RewardRecord(
            zUserID,
            zAmount,
            "user_reward",
            System.currentTimeMillis()
        );
        
        rewardRecords.put(zUserID, record);
    }
    
    public MiniNumber getValidatorReward(MiniData zValidatorID) {
        return validatorRewards.getOrDefault(zValidatorID, MiniNumber.ZERO);
    }
    
    public MiniNumber getUserReward(MiniData zUserID) {
        return userRewards.getOrDefault(zUserID, MiniNumber.ZERO);
    }
    
    private class RewardTask extends TimerTask {
        @Override
        public void run() {
            // Calculate rewards
            calculateValidatorRewards();
            calculateUserRewards();
            
            // Process rewards
            processRewards();
            
            // Update metrics
            updateRewardMetrics();
        }
    }
    
    private void calculateValidatorRewards() {
        HexValidator hexValidator = HexValidator.getInstance();
        for (Map.Entry<MiniData, MiniNumber> entry : validatorRewards.entrySet()) {
            MiniData validatorID = entry.getKey();
            MiniNumber baseReward = entry.getValue();
            
            // Calculate reputation bonus
            AIValidator validator = AIValidator.getInstance(validatorID);
            MiniNumber reputationBonus = baseReward.multiply(validator.getReputation());
            
            // Calculate hex validation bonus
            MiniNumber hexBonus = baseReward.multiply(hexValidator.getValidatorColorScore(validatorID));
            
            // Update total reward
            MiniNumber totalReward = baseReward.add(reputationBonus).add(hexBonus);
            validatorRewards.put(validatorID, totalReward);
        }
    }
    
    private void calculateUserRewards() {
        for (Map.Entry<MiniData, MiniNumber> entry : userRewards.entrySet()) {
            MiniData userID = entry.getKey();
            MiniNumber baseReward = entry.getValue();
            
            // Calculate stake bonus
            MiniNumber stakeBonus = baseReward.multiply(getUserStake(userID));
            
            // Calculate participation bonus
            MiniNumber participationBonus = baseReward.multiply(getUserParticipationRate(userID));
            
            // Update total reward
            MiniNumber totalReward = baseReward.add(stakeBonus).add(participationBonus);
            userRewards.put(userID, totalReward);
        }
    }
    
    private void processRewards() {
        // Process validator rewards
        for (Map.Entry<MiniData, MiniNumber> entry : validatorRewards.entrySet()) {
            MiniData validatorID = entry.getKey();
            MiniNumber reward = entry.getValue();
            
            // Process through bridge service
            if (reward.compareTo(MiniNumber.ZERO) > 0) {
                BridgeService.getInstance().processBridgeTransaction(
                    "self",
                    validatorID,
                    reward
                );
            }
        }
        
        // Process user rewards
        for (Map.Entry<MiniData, MiniNumber> entry : userRewards.entrySet()) {
            MiniData userID = entry.getKey();
            MiniNumber reward = entry.getValue();
            
            // Process through bridge service
            if (reward.compareTo(MiniNumber.ZERO) > 0) {
                BridgeService.getInstance().processBridgeTransaction(
                    "self",
                    userID,
                    reward
                );
            }
        }
    }
    
    private void updateRewardMetrics() {
        // Update validator metrics
        for (Map.Entry<MiniData, MiniNumber> entry : validatorRewards.entrySet()) {
            MiniData validatorID = entry.getKey();
            MiniNumber reward = entry.getValue();
            
            // Update validator metrics
            ValidatorMetrics metrics = ValidatorMetrics.getInstance(validatorID);
            metrics.updateReward(reward);
        }
        
        // Update user metrics
        for (Map.Entry<MiniData, MiniNumber> entry : userRewards.entrySet()) {
            MiniData userID = entry.getKey();
            MiniNumber reward = entry.getValue();
            
            // Update user metrics
            UserMetrics metrics = UserMetrics.getInstance(userID);
            metrics.updateReward(reward);
        }
    }
    

    
    private MiniNumber getUserStake(MiniData zUserID) {
        return stakeCalculator.calculateUserStake(zUserID);
    }
    
    private MiniNumber getUserParticipationRate(MiniData zUserID) {
        return participationRateCalculator.calculateParticipationRate(zUserID);
    }
    
    public Map<MiniData, RewardRecord> getRewardRecords() {
        return new HashMap<>(rewardRecords);
    }
    
    public void resetRewards() {
        rewardRecords.clear();
        validatorRewards.clear();
        userRewards.clear();
        stakeCalculator.resetStakes();
        participationRateCalculator.resetParticipationStats();
        rewardMetrics.resetMetrics();
        if (rewardTimer != null) {
            rewardTimer.cancel();
            rewardTimer = null;
        }
    }
}
