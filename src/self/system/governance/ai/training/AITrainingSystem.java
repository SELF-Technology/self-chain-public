package org.self.system.governance.ai.training;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.ai.AIVotingSystem;
import org.self.system.governance.ai.PointBasedVoting;
import org.self.system.governance.ai.ProposalEvaluator;
import org.self.system.governance.ai.ReputationSystem;
import org.self.system.governance.monitor.GovernanceMonitor;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class AITrainingSystem {
    private static AITrainingSystem instance;
    private Map<AIData, AIModel> validatorModels;
    private Map<String, TrainingData> trainingData;
    private GovernanceMonitor monitor;
    private Timer trainingTimer;
    private long trainingInterval;
    private Random random;
    
    private AITrainingSystem() {
        validatorModels = new HashMap<>();
        trainingData = new HashMap<>();
        monitor = GovernanceMonitor.getInstance();
        trainingInterval = SELFParams.AI_TRAINING_INTERVAL.toLong();
        random = new Random();
        initializeTrainingSystem();
    }
    
    public static AITrainingSystem getInstance() {
        if (instance == null) {
            instance = new AITrainingSystem();
        }
        return instance;
    }
    
    private void initializeTrainingSystem() {
        trainingTimer = new Timer();
        trainingTimer.schedule(new TrainingTask(), 0, trainingInterval);
        
        // Initialize training data types
        addTrainingDataType("proposal_validation");
        addTrainingDataType("voting_pattern");
        addTrainingDataType("reputation_update");
    }
    
    private void addTrainingDataType(String zType) {
        trainingData.put(zType, new TrainingData());
    }
    
    public void trainAIValidator(AIData zValidatorID) {
        AIValidator validator = AIVotingSystem.getInstance().getValidator(zValidatorID);
        if (validator != null) {
            // Get validator model
            AIModel model = validatorModels.get(zValidatorID);
            if (model == null) {
                model = new AIModel();
                validatorModels.put(zValidatorID, model);
            }
            
            // Collect training data
            List<TrainingData> data = collectTrainingData(validator);
            
            // Train model
            model.train(data);
            
            // Calculate reward
            AICapacityNumber reward = new AICapacityNumber(100).multiply(model.getAccuracy());
            
            // Update validator's stake
            AICapacityNumber newStake = validator.getStake().add(reward);
            validator.updateStake(newStake);
            
            // Update validator with new model
            validator.setModel(model);
            
            // Log training
            monitor.log(String.format(
                "AI Validator trained: id=%s, model_version=%d",
                zValidatorID.toString(),
                model.getVersion()
            ));
        }
    }
    
    private List<TrainingData> collectTrainingData(AIValidator zValidator) {
        List<TrainingData> data = new ArrayList<>();
        
        // Collect validation data
        data.addAll(collectValidationData(zValidator));
        
        // Collect voting data
        data.addAll(collectVotingData(zValidator));
        
        // Collect reputation data
        data.addAll(collectReputationData(zValidator));
        
        return data;
    }
    
    private List<TrainingData> collectValidationData(AIValidator zValidator) {
        List<TrainingData> data = new ArrayList<>();
        
        // Get recent proposals
        List<GovernanceProposal> proposals = monitor.getRecentProposals();
        
        for (GovernanceProposal proposal : proposals) {
            // Get validator's vote
            AIData vote = zValidator.getVote(proposal);
            
            // Get proposal validation result
            boolean isValid = zValidator.validateProposal(proposal);
            
            // Add training data
            data.add(new TrainingData(
                "proposal_validation",
                vote,
                isValid
            ));
        }
        
        return data;
    }
    
    private List<TrainingData> collectVotingData(AIValidator zValidator) {
        List<TrainingData> data = new ArrayList<>();
        
        // Get recent votes
        List<AIData> votes = zValidator.getRecentVotes();
        
        for (AIData vote : votes) {
            TrainingData td = new TrainingData();
            td.setType("voting_pattern");
            td.setData(vote);
            td.setLabel(zValidator.validateVote(vote));
            data.add(td);
        }
        
        return data;
    }
    
    private List<TrainingData> collectReputationData(AIValidator zValidator) {
        List<TrainingData> data = new ArrayList<>();
        
        // Get recent reputation updates
        List<ReputationUpdate> updates = ReputationSystem.getInstance().getReputationHistory(zValidator.getValidatorID());
        
        for (ReputationUpdate update : updates) {
            TrainingData td = new TrainingData();
            td.setType("reputation_update");
            td.setData(update.getReason());
            td.setLabel(update.getChange() >= 0);
            data.add(td);
        }
        
        return data;
    }
    
    private class TrainingTask extends TimerTask {
        @Override
        public void run() {
            // Train all validators
            AIVotingSystem voting = AIVotingSystem.getInstance();
            for (AIValidator validator : voting.getValidators()) {
                trainAIValidator(validator.getValidatorID());
            }
            
            // Generate training report
            generateTrainingReport();
        }
    }
    
    private void generateTrainingReport() {
        StringBuilder report = new StringBuilder("\n=== AI TRAINING REPORT ===\n");
        
        // Add model statistics
        report.append("\nModel Statistics:\n");
        for (Map.Entry<MiniData, AIModel> entry : validatorModels.entrySet()) {
            AIModel model = entry.getValue();
            report.append(String.format(
                "\nValidator: %s\n" +
                "Model Version: %d\n" +
                "Accuracy: %.2f%%\n" +
                "Training Time: %dms\n",
                entry.getKey().toString(),
                model.getVersion(),
                model.getAccuracy() * 100,
                model.getTrainingTime()
            ));
        }
        
        // Add training data statistics
        report.append("\nTraining Data:\n");
        for (Map.Entry<String, TrainingData> entry : trainingData.entrySet()) {
            TrainingData data = entry.getValue();
            report.append(String.format(
                "\nType: %s\n" +
                "Samples: %d\n" +
                "Accuracy: %.2f%%\n",
                entry.getKey(),
                data.getSampleCount(),
                data.getAccuracy() * 100
            ));
        }
        
        monitor.log(report.toString());
    }
    
    public AIModel getModel(MiniData zValidatorID) {
        return validatorModels.get(zValidatorID);
    }
    
    public List<TrainingData> getTrainingData(String zType) {
        return trainingData.get(zType).getData();
    }
    
    public void resetTraining() {
        validatorModels.clear();
        trainingData.clear();
        if (trainingTimer != null) {
            trainingTimer.cancel();
            trainingTimer = null;
        }
    }
}
