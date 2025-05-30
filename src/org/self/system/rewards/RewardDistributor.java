package org.self.system.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.monitor.GovernanceMonitor;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class RewardDistributor {
    private static RewardDistributor instance;
    private StakeCalculator stakeCalculator;
    private ParticipationRateCalculator participationCalculator;
    private RewardMetrics metrics;
    private GovernanceMonitor monitor;
    private Timer distributionTimer;
    private long distributionInterval;
    
    private RewardDistributor() {
        stakeCalculator = StakeCalculator.getInstance();
        participationCalculator = ParticipationRateCalculator.getInstance();
        metrics = RewardMetrics.getInstance();
        monitor = GovernanceMonitor.getInstance();
        distributionInterval = SELFParams.REWARD_DISTRIBUTION_INTERVAL.toLong();
        initializeDistributor();
    }
    
    public static RewardDistributor getInstance() {
        if (instance == null) {
            instance = new RewardDistributor();
        }
        return instance;
    }
    
    private void initializeDistributor() {
        distributionTimer = new Timer();
        distributionTimer.schedule(new DistributionTask(), 0, distributionInterval);
    }
    
    public void distributeRewards() {
        try {
            // Get all active users and validators
            List<MiniData> activeUsers = getActiveUsers();
            List<MiniData> activeValidators = getActiveValidators();
            
            // Calculate total rewards
            MiniNumber totalRewards = calculateTotalRewards();
            MiniNumber userRewards = totalRewards.multiply(SELFParams.USER_REWARD_PERCENTAGE);
            MiniNumber validatorRewards = totalRewards.subtract(userRewards);
            
            // Distribute user rewards
            Map<MiniData, MiniNumber> userRewardsMap = distributeUserRewards(activeUsers, userRewards);
            
            // Distribute validator rewards
            Map<MiniData, MiniNumber> validatorRewardsMap = distributeValidatorRewards(activeValidators, validatorRewards);
            
            // Record rewards
            recordRewards(userRewardsMap, "user");
            recordRewards(validatorRewardsMap, "validator");
            
            // Update metrics
            metrics.updateRewardDistribution("user", userRewards);
            metrics.updateRewardDistribution("validator", validatorRewards);
            
            // Log distribution
            SelfLogger.log(String.format(
                "Distributed rewards - Users: %.2f, Validators: %.2f", 
                userRewards.toDouble(), validatorRewards.toDouble()));
            
        } catch (Exception e) {
            monitor.alert("REWARD_DISTRIBUTION_ERROR", e.getMessage());
            SelfLogger.error("Error distributing rewards: " + e.getMessage());
        }
    }
    
    private List<MiniData> getActiveUsers() {
        List<MiniData> activeUsers = new ArrayList<>();
        PointSystem pointSystem = PointSystem.getInstance();
        
        for (Map.Entry<MiniData, MiniNumber> entry : pointSystem.getUserPoints().entrySet()) {
            MiniNumber points = entry.getValue();
            if (points.toDouble() >= SELFParams.MIN_ACTIVE_POINTS.toDouble()) {
                activeUsers.add(entry.getKey());
            }
        }
        return activeUsers;
    }
    
    private List<MiniData> getActiveValidators() {
        List<MiniData> activeValidators = new ArrayList<>();
        
        for (AIValidator validator : AIValidator.getValidators()) {
            if (validator.isActive()) {
                activeValidators.add(validator.getValidatorID());
            }
        }
        return activeValidators;
    }
    
    private MiniNumber calculateTotalRewards() {
        // Calculate total rewards based on network parameters
        MiniNumber baseReward = SELFParams.BASE_REWARD;
        MiniNumber networkSize = MiniNumber.valueOf(AIValidator.getValidators().size());
        
        // Apply network size modifier
        MiniNumber sizeModifier = networkSize.divide(SELFParams.TARGET_NETWORK_SIZE);
        
        // Apply participation modifier
        double avgParticipation = participationCalculator.calculateAverageParticipationRate();
        MiniNumber participationModifier = MiniNumber.valueOf(avgParticipation);
        
        return baseReward.multiply(sizeModifier).multiply(participationModifier);
    }
    
    private Map<MiniData, MiniNumber> distributeUserRewards(List<MiniData> zUsers, MiniNumber zTotalRewards) {
        Map<MiniData, MiniNumber> rewards = new HashMap<>();
        MiniNumber totalStake = MiniNumber.ZERO;
        
        // Calculate total stake
        for (MiniData userID : zUsers) {
            MiniNumber stake = stakeCalculator.calculateUserStake(userID);
            totalStake = totalStake.add(stake);
        }
        
        // Calculate individual rewards
        for (MiniData userID : zUsers) {
            MiniNumber stake = stakeCalculator.calculateUserStake(userID);
            MiniNumber participationRate = participationCalculator.calculateParticipationRate(userID);
            
            // Calculate reward based on stake and participation
            MiniNumber reward = zTotalRewards
                .multiply(stake.divide(totalStake))
                .multiply(participationRate.divide(MiniNumber.valueOf(100)));
            
            rewards.put(userID, reward);
        }
        
        return rewards;
    }
    
    private Map<MiniData, MiniNumber> distributeValidatorRewards(List<MiniData> zValidators, MiniNumber zTotalRewards) {
        Map<MiniData, MiniNumber> rewards = new HashMap<>();
        MiniNumber totalStake = MiniNumber.ZERO;
        
        // Calculate total stake
        for (MiniData validatorID : zValidators) {
            MiniNumber stake = stakeCalculator.calculateValidatorStake(validatorID);
            totalStake = totalStake.add(stake);
        }
        
        // Calculate individual rewards
        for (MiniData validatorID : zValidators) {
            AIValidator validator = AIValidator.getInstance(validatorID);
            MiniNumber stake = stakeCalculator.calculateValidatorStake(validatorID);
            double reputation = validator.getReputation();
            
            // Calculate reward based on stake, reputation, and hex validation
            MiniNumber reward = zTotalRewards
                .multiply(stake.divide(totalStake))
                .multiply(MiniNumber.valueOf(reputation))
                .multiply(MiniNumber.valueOf(validator.getHexValidationScore()));
            
            rewards.put(validatorID, reward);
        }
        
        return rewards;
    }
    
    private void recordRewards(Map<MiniData, MiniNumber> zRewards, String zType) {
        long timestamp = System.currentTimeMillis();
        
        for (Map.Entry<MiniData, MiniNumber> entry : zRewards.entrySet()) {
            MiniData recipient = entry.getKey();
            MiniNumber amount = entry.getValue();
            
            // Create reward record
            RewardRecord record = new RewardRecord(
                recipient,
                amount,
                zType,
                timestamp
            );
            
            // Add record
            RewardRecordManager.getInstance().addRewardRecord(record);
        }
    }
    
    private class DistributionTask extends TimerTask {
        @Override
        public void run() {
            try {
                distributeRewards();
            } catch (Exception e) {
                monitor.alert("REWARD_DISTRIBUTION_ERROR", e.getMessage());
            }
        }
    }
    
    public void stopDistribution() {
        if (distributionTimer != null) {
            distributionTimer.cancel();
        }
    }
    
    public void resetDistributor() {
        stopDistribution();
        instance = null;
        initializeDistributor();
    }
}
