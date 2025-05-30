package org.self.system.rewards;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.monitor.GovernanceMonitor;
import org.self.system.params.SELFParams;

public class RewardMetrics {
    private static RewardMetrics instance;
    private Map<String, RewardDistribution> distributions;
    private Map<String, RewardPerformance> performance;
    private Map<String, RewardValidation> validations;
    private GovernanceMonitor monitor;
    
    private RewardMetrics() {
        distributions = new HashMap<>();
        performance = new HashMap<>();
        validations = new HashMap<>();
        monitor = GovernanceMonitor.getInstance();
        initializeMetrics();
    }
    
    public static RewardMetrics getInstance() {
        if (instance == null) {
            instance = new RewardMetrics();
        }
        return instance;
    }
    
    private void initializeMetrics() {
        // Initialize default metrics
        addDistribution("validator", new RewardDistribution());
        addDistribution("user", new RewardDistribution());
        addPerformance("overall", new RewardPerformance());
        addValidation("hex", new RewardValidation());
    }
    
    public void addDistribution(String zType, RewardDistribution zDistribution) {
        distributions.put(zType, zDistribution);
    }
    
    public void addPerformance(String zMetric, RewardPerformance zPerformance) {
        performance.put(zMetric, zPerformance);
    }
    
    public void addValidation(String zType, RewardValidation zValidation) {
        validations.put(zType, zValidation);
    }
    
    public void updateRewardDistribution(String zType, AICapacityNumber zAmount) {
        RewardDistribution dist = distributions.get(zType);
        if (dist != null) {
            dist.updateTotalAmount(zAmount);
            dist.incrementDistributionCount();
            
            // Update overall performance
            RewardPerformance perf = performance.get("overall");
            if (perf != null) {
                perf.updateTotalAmount(zAmount);
                perf.incrementDistributionCount();
            }
        }
    }
    
    public void updateRewardValidation(String zType, AICapacityNumber zScore) {
        RewardValidation val = validations.get(zType);
        if (val != null) {
            val.updateScore(zScore);
            val.incrementValidationCount();
            
            // Update overall validation
            RewardValidation overall = validations.get("overall");
            if (overall != null) {
                overall.updateScore(zScore);
                overall.incrementValidationCount();
            }
        }
    }
    
    public void updateRewardPerformance(String zMetric, AICapacityNumber zValue) {
        RewardPerformance perf = performance.get(zMetric);
        if (perf != null) {
            perf.updateMetric(zValue);
            
            // Update overall performance
            RewardPerformance overall = performance.get("overall");
            if (overall != null) {
                overall.updateMetric(zValue);
            }
        }
    }
    
    public Map<String, RewardDistribution> getDistributions() {
        return new HashMap<>(distributions);
    }
    
    public Map<String, RewardPerformance> getPerformance() {
        return new HashMap<>(performance);
    }
    
    public Map<String, RewardValidation> getValidations() {
        return new HashMap<>(validations);
    }
    
    public List<RewardRecord> getRewardRecords() {
        return rewardRecords.values().stream()
            .collect(Collectors.toList());
    }
    
    public List<RewardRecord> getRewardRecordsByType(String zType) {
        return rewardRecords.values().stream()
            .filter(record -> record.getRewardType().equals(zType))
            .collect(Collectors.toList());
    }
    
    public void generateRewardReport() {
        StringBuilder report = new StringBuilder("\n=== REWARD DISTRIBUTION REPORT ===\n");
        
        // Distribution metrics
        report.append("\nDistribution Metrics:\n");
        for (Map.Entry<String, RewardDistribution> entry : distributions.entrySet()) {
            report.append(String.format("%s: %s\n", 
                entry.getKey(), entry.getValue().toString()));
        }
        
        // Performance metrics
        report.append("\nPerformance Metrics:\n");
        for (Map.Entry<String, RewardPerformance> entry : performance.entrySet()) {
            report.append(String.format("%s: %s\n", 
                entry.getKey(), entry.getValue().toString()));
        }
        
        // Validation metrics
        report.append("\nValidation Metrics:\n");
        for (Map.Entry<String, RewardValidation> entry : validations.entrySet()) {
            report.append(String.format("%s: %s\n", 
                entry.getKey(), entry.getValue().toString()));
        }
        
        // Log report
        monitor.log(report.toString());
    }
    
    public void resetMetrics() {
        distributions.clear();
        performance.clear();
        validations.clear();
        initializeMetrics();
    }
}
