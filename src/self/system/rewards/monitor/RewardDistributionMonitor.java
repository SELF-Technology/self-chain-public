package org.self.system.rewards.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.monitor.GovernanceMonitor;
import org.self.system.params.SELFParams;
import org.self.system.rewards.RewardManager;
import org.self.system.rewards.RewardMetrics;
import org.self.utils.SelfLogger;

public class RewardDistributionMonitor {
    private static RewardDistributionMonitor instance;
    private Map<String, DistributionStats> distributionStats;
    private Map<String, ValidationStats> validationStats;
    private Timer monitoringTimer;
    private long monitoringInterval;
    private RewardMetrics metrics;
    private GovernanceMonitor governanceMonitor;
    
    private RewardDistributionMonitor() {
        distributionStats = new HashMap<>();
        validationStats = new HashMap<>();
        monitoringInterval = SELFParams.SELF_REWARD_PERIOD.toLong();
        metrics = RewardMetrics.getInstance();
        governanceMonitor = GovernanceMonitor.getInstance();
        initializeMonitoring();
    }
    
    public static RewardDistributionMonitor getInstance() {
        if (instance == null) {
            instance = new RewardDistributionMonitor();
        }
        return instance;
    }
    
    private void initializeMonitoring() {
        monitoringTimer = new Timer();
        monitoringTimer.schedule(new MonitoringTask(), 0, monitoringInterval);
        
        // Initialize default stats
        addDistributionStats("validator");
        addDistributionStats("user");
        addValidationStats("hex");
    }
    
    private void addDistributionStats(String zType) {
        distributionStats.put(zType, new DistributionStats());
    }
    
    private void addValidationStats(String zType) {
        validationStats.put(zType, new ValidationStats());
    }
    
    public void trackRewardDistribution(MiniData zRecipientID, MiniNumber zAmount, String zType) {
        DistributionStats stats = distributionStats.get(zType);
        if (stats != null) {
            stats.updateTotalAmount(zAmount);
            stats.incrementDistributionCount();
            
            // Update metrics
            metrics.updateRewardDistribution(zType, zAmount);
            
            // Log distribution
            governanceMonitor.log(String.format(
                "Reward distribution: recipient=%s, amount=%s, type=%s",
                zRecipientID.toString(), zAmount.toString(), zType
            ));
        }
    }
    
    public void trackRewardValidation(MiniData zValidatorID, double zScore, String zType) {
        ValidationStats stats = validationStats.get(zType);
        if (stats != null) {
            stats.updateScore(zScore);
            stats.incrementValidationCount();
            
            // Update metrics
            metrics.updateRewardValidation(zType, zScore);
            
            // Log validation
            governanceMonitor.log(String.format(
                "Reward validation: validator=%s, score=%.2f, type=%s",
                zValidatorID.toString(), zScore, zType
            ));
        }
    }
    
    private class MonitoringTask extends TimerTask {
        @Override
        public void run() {
            // Update distribution stats
            updateDistributionStats();
            
            // Update validation stats
            updateValidationStats();
            
            // Generate reports
            generateDistributionReport();
            generateValidationReport();
        }
    }
    
    private void updateDistributionStats() {
        for (Map.Entry<String, DistributionStats> entry : distributionStats.entrySet()) {
            String type = entry.getKey();
            DistributionStats stats = entry.getValue();
            
            // Update metrics
            metrics.updateRewardDistribution(type, stats.getTotalAmount());
        }
    }
    
    private void updateValidationStats() {
        for (Map.Entry<String, ValidationStats> entry : validationStats.entrySet()) {
            String type = entry.getKey();
            ValidationStats stats = entry.getValue();
            
            // Update metrics
            metrics.updateRewardValidation(type, stats.getAverageScore());
        }
    }
    
    private void generateDistributionReport() {
        StringBuilder report = new StringBuilder("\n=== REWARD DISTRIBUTION REPORT ===\n");
        
        for (Map.Entry<String, DistributionStats> entry : distributionStats.entrySet()) {
            String type = entry.getKey();
            DistributionStats stats = entry.getValue();
            
            report.append(String.format(
                "\nType: %s\n" +
                "Total Amount: %s\n" +
                "Distributions: %d\n" +
                "Average Amount: %.2f\n" +
                "Success Rate: %.2f%%\n",
                type,
                stats.getTotalAmount().toString(),
                stats.getDistributionCount(),
                stats.getAverageAmount(),
                stats.getSuccessRate()
            ));
        }
        
        governanceMonitor.log(report.toString());
    }
    
    private void generateValidationReport() {
        StringBuilder report = new StringBuilder("\n=== REWARD VALIDATION REPORT ===\n");
        
        for (Map.Entry<String, ValidationStats> entry : validationStats.entrySet()) {
            String type = entry.getKey();
            ValidationStats stats = entry.getValue();
            
            report.append(String.format(
                "\nType: %s\n" +
                "Total Validations: %d\n" +
                "Average Score: %.2f\n" +
                "Success Rate: %.2f%%\n" +
                "Validation Rate: %.2f%%\n",
                type,
                stats.getValidationCount(),
                stats.getAverageScore(),
                stats.getSuccessRate(),
                stats.getValidationRate()
            ));
        }
        
        governanceMonitor.log(report.toString());
    }
    
    public Map<String, DistributionStats> getDistributionStats() {
        return new HashMap<>(distributionStats);
    }
    
    public Map<String, ValidationStats> getValidationStats() {
        return new HashMap<>(validationStats);
    }
    
    public void resetMonitoring() {
        distributionStats.clear();
        validationStats.clear();
        if (monitoringTimer != null) {
            monitoringTimer.cancel();
            monitoringTimer = null;
        }
    }
}
