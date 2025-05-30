package org.self.system.rewards;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.monitor.GovernanceMonitor;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class RewardMonitor {
    private static RewardMonitor instance;
    private Map<String, AlertThreshold> thresholds;
    private Timer monitoringTimer;
    private long monitoringPeriod;
    private GovernanceMonitor governanceMonitor;
    private RewardMetrics rewardMetrics;
    
    private RewardMonitor() {
        thresholds = new HashMap<>();
        monitoringPeriod = SELFParams.SELF_REWARD_MONITOR_PERIOD.toLong();
        governanceMonitor = GovernanceMonitor.getInstance();
        rewardMetrics = RewardMetrics.getInstance();
        initializeThresholds();
        initializeMonitoring();
    }
    
    public static RewardMonitor getInstance() {
        if (instance == null) {
            instance = new RewardMonitor();
        }
        return instance;
    }
    
    private void initializeThresholds() {
        // Validator reward thresholds
        thresholds.put("validator_distribution", new AlertThreshold(
            SELFParams.VALIDATOR_REWARD_THRESHOLD_LOW,
            SELFParams.VALIDATOR_REWARD_THRESHOLD_HIGH
        ));
        
        // User reward thresholds
        thresholds.put("user_distribution", new AlertThreshold(
            SELFParams.USER_REWARD_THRESHOLD_LOW,
            SELFParams.USER_REWARD_THRESHOLD_HIGH
        ));
        
        // Performance thresholds
        thresholds.put("performance", new AlertThreshold(
            SELFParams.PERFORMANCE_THRESHOLD_LOW,
            SELFParams.PERFORMANCE_THRESHOLD_HIGH
        ));
        
        // Validation thresholds
        thresholds.put("validation_rate", new AlertThreshold(
            SELFParams.VALIDATION_RATE_THRESHOLD_LOW,
            SELFParams.VALIDATION_RATE_THRESHOLD_HIGH
        ));
    }
    
    private void initializeMonitoring() {
        monitoringTimer = new Timer();
        monitoringTimer.schedule(new MonitoringTask(), 0, monitoringPeriod);
    }
    
    private class MonitoringTask extends TimerTask {
        @Override
        public void run() {
            checkRewardDistribution();
            checkPerformance();
            checkValidation();
        }
    }
    
    private void checkRewardDistribution() {
        Map<String, RewardDistribution> distributions = rewardMetrics.getDistributions();
        
        for (Map.Entry<String, RewardDistribution> entry : distributions.entrySet()) {
            String type = entry.getKey();
            RewardDistribution dist = entry.getValue();
            
            // Check total amount
            checkThreshold(type + "_total", dist.getTotalAmount(), "Total reward amount");
            
            // Check average amount
            checkThreshold(type + "_avg", new AICapacityNumber(dist.getAverageAmount()), "Average reward amount");
            
            // Check max amount
            checkThreshold(type + "_max", dist.getMaxAmount(), "Maximum reward amount");
            
            // Check min amount
            checkThreshold(type + "_min", dist.getMinAmount(), "Minimum reward amount");
        }
    }
    
    private void checkPerformance() {
        Map<String, RewardPerformance> performance = rewardMetrics.getPerformance();
        
        for (Map.Entry<String, RewardPerformance> entry : performance.entrySet()) {
            String metric = entry.getKey();
            RewardPerformance perf = entry.getValue();
            
            // Check total score
            checkThreshold(metric + "_total", MiniNumber.valueOf(perf.getTotalScore()), "Total performance score");
            
            // Check average score
            checkThreshold(metric + "_avg", MiniNumber.valueOf(perf.getAverageScore()), "Average performance score");
            
            // Check max score
            checkThreshold(metric + "_max", MiniNumber.valueOf(perf.getMaxScore()), "Maximum performance score");
            
            // Check min score
            checkThreshold(metric + "_min", MiniNumber.valueOf(perf.getMinScore()), "Minimum performance score");
        }
    }
    
    private void checkValidation() {
        Map<String, RewardValidation> validations = rewardMetrics.getValidations();
        
        for (Map.Entry<String, RewardValidation> entry : validations.entrySet()) {
            String type = entry.getKey();
            RewardValidation val = entry.getValue();
            
            // Check validation rate
            checkThreshold(type + "_rate", new AICapacityNumber(val.getValidationRate()), "Validation rate");
            
            // Check total amount
            checkThreshold(type + "_total", val.getTotalAmount(), "Total validation amount");
            
            // Check validated amount
            checkThreshold(type + "_validated", val.getValidatedAmount(), "Validated amount");
        }
    }
    
    private void checkThreshold(String zMetric, MiniNumber zValue, String zDescription) {
        AlertThreshold threshold = thresholds.get(zMetric);
        if (threshold != null) {
            MiniNumber low = MiniNumber.valueOf(threshold.getLowThreshold());
            MiniNumber high = MiniNumber.valueOf(threshold.getHighThreshold());
            
            if (zValue.compareTo(low) < 0) {
                triggerAlert(zMetric, zValue, zDescription, "below threshold");
            } else if (zValue.compareTo(high) > 0) {
                triggerAlert(zMetric, zValue, zDescription, "above threshold");
            }
        }
    }
    
    private void triggerAlert(String zMetric, MiniNumber zValue, String zDescription, String zCondition) {
        String alert = String.format(
            "ALERT: %s %s %s (value: %s)",
            zDescription,
            zCondition,
            "threshold",
            zValue.toString()
        );
        
        // Log the alert
        SelfLogger.log(alert);
        
        // Notify governance system
        governanceMonitor.notifyAlert(alert);
    }
    
    public void resetMonitoring() {
        if (monitoringTimer != null) {
            monitoringTimer.cancel();
            monitoringTimer = null;
        }
        initializeMonitoring();
    }
}
