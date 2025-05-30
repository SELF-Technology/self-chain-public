package org.self.system.governance.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.GovernanceProposal;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.hex.HexValidator;
import org.self.system.governance.points.PointSystem;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class GovernanceMonitor {
    private static GovernanceMonitor instance;
    private Map<String, GovernanceMetrics> proposalMetrics;
    private Map<String, ValidatorMetrics> validatorMetrics;
    private Timer monitoringTimer;
    private long monitoringInterval;
    
    private GovernanceMonitor() {
        proposalMetrics = new HashMap<>();
        validatorMetrics = new HashMap<>();
        monitoringInterval = SELFParams.SELF_REWARD_PERIOD.toLong();
        initializeMonitoring();
    }
    
    public static GovernanceMonitor getInstance() {
        if (instance == null) {
            instance = new GovernanceMonitor();
        }
        return instance;
    }
    
    private void initializeMonitoring() {
        monitoringTimer = new Timer();
        monitoringTimer.schedule(new MonitoringTask(), 0, monitoringInterval);
    }
    
    public void trackProposal(GovernanceProposal zProposal) {
        AIData proposalID = zProposal.getProposalID();
        if (!proposalMetrics.containsKey(proposalID.toString())) {
            proposalMetrics.put(proposalID.toString(), new GovernanceMetrics(zProposal));
        }
    }
    
    public void trackValidator(AIValidator zValidator) {
        AIData validatorID = zValidator.getValidatorID();
        if (!validatorMetrics.containsKey(validatorID.toString())) {
            validatorMetrics.put(validatorID.toString(), new ValidatorMetrics(zValidator));
        }
    }
    
    private class MonitoringTask extends TimerTask {
        @Override
        public void run() {
            updateProposalMetrics();
            updateValidatorMetrics();
            generateReports();
        }
    }
    
    private void updateProposalMetrics() {
        for (Map.Entry<String, GovernanceMetrics> entry : proposalMetrics.entrySet()) {
            GovernanceMetrics metrics = entry.getValue();
            
            // Update basic metrics
            metrics.updateApprovalRate();
            metrics.updateVoteDistribution();
            metrics.updateResourceUsage();
            
            // Update ML prediction accuracy
            MLModel mlModel = MLModel.getInstance();
            GovernanceProposal proposal = metrics.getProposal();
            double prediction = mlModel.predict(proposal);
            AICapacityNumber actualScore = PointBasedVoting.getInstance().getProposalPoints(proposal.getProposalID());
            metrics.updatePredictionAccuracy(prediction, actualScore.getAsDouble());
        }
    }
    
    private void updateValidatorMetrics() {
        for (Map.Entry<String, ValidatorMetrics> entry : validatorMetrics.entrySet()) {
            ValidatorMetrics metrics = entry.getValue();
            
            // Update basic metrics
            metrics.updateParticipationRate();
            metrics.updateValidationScore();
            metrics.updateReputation();
            
            // Update hex validation metrics
            HexValidator hexValidator = HexValidator.getInstance();
            String color = hexValidator.getValidatorColor(metrics.getValidator().getValidatorID());
            metrics.updateColorStability(color);
            
            // Update stake metrics
            AICapacityNumber stake = metrics.getValidator().getStake();
            metrics.updateStakeMetrics(stake);
        }
    }
    
    private void generateReports() {
        // Generate proposal report
        generateProposalReport();
        
        // Generate validator report
        generateValidatorReport();
        
        // Generate system health report
        generateSystemHealthReport();
    }
    
    private void generateProposalReport() {
        StringBuilder report = new StringBuilder("\n=== PROPOSAL REPORT ===\n");
        for (Map.Entry<String, GovernanceMetrics> entry : proposalMetrics.entrySet()) {
            GovernanceMetrics metrics = entry.getValue();
            report.append("Proposal ID: " + metrics.getProposal().getProposalID() + "\n");
            report.append("Approval Rate: " + metrics.getApprovalRate() + "%\n");
            report.append("Prediction Accuracy: " + metrics.getPredictionAccuracy() + "%\n");
            report.append("Resource Usage: " + metrics.getResourceUsage() + "\n");
            report.append("-------------------\n");
        }
        SelfLogger.log(report.toString());
    }
    
    private void generateValidatorReport() {
        StringBuilder report = new StringBuilder("\n=== VALIDATOR REPORT ===\n");
        for (Map.Entry<String, ValidatorMetrics> entry : validatorMetrics.entrySet()) {
            ValidatorMetrics metrics = entry.getValue();
            report.append("Validator ID: " + metrics.getValidator().getValidatorID() + "\n");
            report.append("Participation Rate: " + metrics.getParticipationRate() + "%\n");
            report.append("Validation Score: " + metrics.getValidationScore() + "\n");
            report.append("Reputation: " + metrics.getReputation() + "\n");
            report.append("Color Stability: " + metrics.getColorStability() + "%\n");
            report.append("-------------------\n");
        }
        SelfLogger.log(report.toString());
    }
    
    private void generateSystemHealthReport() {
        StringBuilder report = new StringBuilder("\n=== SYSTEM HEALTH REPORT ===\n");
        
        // Calculate system metrics
        double avgApprovalRate = calculateAverageApprovalRate();
        double avgPredictionAccuracy = calculateAveragePredictionAccuracy();
        double avgParticipationRate = calculateAverageParticipationRate();
        double avgResourceUsage = calculateAverageResourceUsage();
        
        // Add metrics to report
        report.append("Average Approval Rate: " + avgApprovalRate + "%\n");
        report.append("Average Prediction Accuracy: " + avgPredictionAccuracy + "%\n");
        report.append("Average Participation Rate: " + avgParticipationRate + "%\n");
        report.append("Average Resource Usage: " + avgResourceUsage + "\n");
        
        SelfLogger.log(report.toString());
    }
    
    private double calculateAverageApprovalRate() {
        double total = 0.0;
        int count = 0;
        for (GovernanceMetrics metrics : proposalMetrics.values()) {
            total += metrics.getApprovalRate();
            count++;
        }
        return count > 0 ? total / count : 0.0;
    }
    
    private double calculateAveragePredictionAccuracy() {
        double total = 0.0;
        int count = 0;
        for (GovernanceMetrics metrics : proposalMetrics.values()) {
            total += metrics.getPredictionAccuracy();
            count++;
        }
        return count > 0 ? total / count : 0.0;
    }
    
    private double calculateAverageParticipationRate() {
        double total = 0.0;
        int count = 0;
        for (ValidatorMetrics metrics : validatorMetrics.values()) {
            total += metrics.getParticipationRate();
            count++;
        }
        return count > 0 ? total / count : 0.0;
    }
    
    private double calculateAverageResourceUsage() {
        double total = 0.0;
        int count = 0;
        for (GovernanceMetrics metrics : proposalMetrics.values()) {
            total += metrics.getResourceUsage();
            count++;
        }
        return count > 0 ? total / count : 0.0;
    }
    
    public Map<String, GovernanceMetrics> getProposalMetrics() {
        return new HashMap<>(proposalMetrics);
    }
    
    public Map<String, ValidatorMetrics> getValidatorMetrics() {
        return new HashMap<>(validatorMetrics);
    }
    
    public void resetMetrics() {
        proposalMetrics.clear();
        validatorMetrics.clear();
        if (monitoringTimer != null) {
            monitoringTimer.cancel();
            monitoringTimer = null;
        }
    }
}
