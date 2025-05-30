package org.self.system.rewards;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.points.PointSystem;
import org.self.system.params.SELFParams;

public class RewardSystemTest {
    private static final MiniNumber TEST_AMOUNT = MiniNumber.valueOf(100);
    private static final MiniNumber TEST_STAKE = MiniNumber.valueOf(1000);
    private static final MiniNumber TEST_POINTS = MiniNumber.valueOf(500);
    private static final double TEST_PARTICIPATION_RATE = 0.8;
    
    private RewardRecordManager recordManager;
    private StakeCalculator stakeCalculator;
    private ParticipationRateCalculator participationCalculator;
    private RewardMetrics metrics;
    
    @Before
    public void setUp() {
        // Reset all instances
        RewardRecordManager.instance = null;
        StakeCalculator.instance = null;
        ParticipationRateCalculator.instance = null;
        RewardMetrics.instance = null;
        
        // Initialize test instances
        recordManager = RewardRecordManager.getInstance();
        stakeCalculator = StakeCalculator.getInstance();
        participationCalculator = ParticipationRateCalculator.getInstance();
        metrics = RewardMetrics.getInstance();
        
        // Reset all metrics and stats
        recordManager.clearAllRewardRecords();
        stakeCalculator.resetStakes();
        participationCalculator.resetParticipationStats();
        metrics.resetMetrics();
    }
    
    @Test
    public void testStakeCalculation() {
        // Test user stake calculation
        MiniData userID = new MiniData("test_user");
        stakeCalculator.updateStake(userID, TEST_STAKE);
        
        MiniNumber stake = stakeCalculator.calculateUserStake(userID);
        assertTrue(stake.toDouble() >= TEST_STAKE.toDouble());
        
        // Test validator stake calculation
        MiniData validatorID = new MiniData("test_validator");
        AIValidator validator = AIValidator.getInstance(validatorID);
        validator.setStake(TEST_STAKE);
        validator.setReputation(0.9);
        
        stakeCalculator.updateValidatorStake(validatorID, TEST_STAKE);
        MiniNumber validatorStake = stakeCalculator.calculateValidatorStake(validatorID);
        assertTrue(validatorStake.toDouble() > TEST_STAKE.toDouble());
    }
    
    @Test
    public void testParticipationRate() {
        MiniData userID = new MiniData("test_user");
        
        // Record activities
        for (int i = 0; i < 10; i++) {
            participationCalculator.recordActivity(userID);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        MiniNumber rate = participationCalculator.calculateParticipationRate(userID);
        assertTrue(rate.toDouble() > 0.5);
        
        // Test weighted rate
        MiniNumber weightedRate = participationCalculator.calculateWeightedParticipationRate(userID);
        assertTrue(weightedRate.toDouble() > 0.5);
    }
    
    @Test
    public void testRewardDistribution() {
        MiniData userID = new MiniData("test_user");
        MiniData validatorID = new MiniData("test_validator");
        
        // Setup stake and participation
        stakeCalculator.updateStake(userID, TEST_STAKE);
        stakeCalculator.updateValidatorStake(validatorID, TEST_STAKE);
        
        // Record participation
        participationCalculator.recordActivity(userID);
        
        // Calculate rewards
        MiniNumber userReward = stakeCalculator.calculateUserReward(userID, TEST_AMOUNT);
        MiniNumber validatorReward = stakeCalculator.calculateValidatorReward(validatorID, TEST_AMOUNT);
        
        // Create and add reward records
        RewardRecord userRecord = new RewardRecord(userID, userReward, "user", System.currentTimeMillis());
        RewardRecord validatorRecord = new RewardRecord(validatorID, validatorReward, "validator", System.currentTimeMillis());
        
        recordManager.addRewardRecord(userRecord);
        recordManager.addRewardRecord(validatorRecord);
        
        // Verify metrics
        Map<String, RewardDistribution> distributions = metrics.getDistributions();
        assertTrue(distributions.containsKey("user"));
        assertTrue(distributions.containsKey("validator"));
        
        // Verify total rewards
        MiniNumber totalUserRewards = metrics.getDistributions().get("user").getTotalAmount();
        MiniNumber totalValidatorRewards = metrics.getDistributions().get("validator").getTotalAmount();
        
        assertTrue(totalUserRewards.toDouble() > TEST_AMOUNT.toDouble());
        assertTrue(totalValidatorRewards.toDouble() > TEST_AMOUNT.toDouble());
    }
    
    @Test
    public void testBatchOperations() {
        List<MiniData> userIDs = new ArrayList<>();
        List<MiniData> validatorIDs = new ArrayList<>();
        
        // Create test users and validators
        for (int i = 0; i < 5; i++) {
            MiniData userID = new MiniData("user_" + i);
            MiniData validatorID = new MiniData("validator_" + i);
            
            userIDs.add(userID);
            validatorIDs.add(validatorID);
            
            stakeCalculator.updateStake(userID, TEST_STAKE);
            stakeCalculator.updateValidatorStake(validatorID, TEST_STAKE);
            
            participationCalculator.recordActivity(userID);
        }
        
        // Calculate batch rewards
        Map<MiniData, MiniNumber> userRewards = stakeCalculator.calculateBatchRewards(userIDs, TEST_AMOUNT);
        Map<MiniData, MiniNumber> validatorRewards = stakeCalculator.calculateBatchValidatorRewards(validatorIDs, TEST_AMOUNT);
        
        // Verify rewards
        for (MiniData userID : userIDs) {
            assertTrue(userRewards.get(userID).toDouble() > TEST_AMOUNT.toDouble());
        }
        
        for (MiniData validatorID : validatorIDs) {
            assertTrue(validatorRewards.get(validatorID).toDouble() > TEST_AMOUNT.toDouble());
        }
    }
    
    @Test
    public void testRewardMetrics() {
        MiniData userID = new MiniData("test_user");
        
        // Record activities and calculate participation
        participationCalculator.recordActivity(userID);
        MiniNumber participationRate = participationCalculator.calculateParticipationRate(userID);
        
        // Update metrics
        metrics.updateRewardPerformance("participation_rate", participationRate.toDouble());
        
        // Verify metrics
        Map<String, RewardPerformance> performance = metrics.getPerformance();
        assertTrue(performance.containsKey("participation_rate"));
        
        // Generate report
        metrics.generateRewardReport();
    }
    
    @Test
    public void testAnomalyDetection() {
        MiniData userID = new MiniData("test_user");
        
        // Create unusually high reward
        MiniNumber unusuallyHighReward = TEST_AMOUNT.multiply(MiniNumber.valueOf(1000));
        
        // Add reward record
        RewardRecord record = new RewardRecord(userID, unusuallyHighReward, "user", System.currentTimeMillis());
        recordManager.addRewardRecord(record);
        
        // Verify anomaly detection
        metrics.detectAnomalies();
        
        // Verify metrics
        Map<String, RewardDistribution> distributions = metrics.getDistributions();
        assertTrue(distributions.containsKey("user"));
        assertTrue(distributions.get("user").getTotalAmount().toDouble() > unusuallyHighReward.toDouble());
    }
}
