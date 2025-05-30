package org.self.system.governance.ai.training;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.ai.PointBasedVoting;
import org.self.system.governance.ai.ProposalEvaluator;
import org.self.system.params.SELFParams;

public class AIModel {
    private static long modelVersion = 0;
    private long version;
    private Map<String, Double> weights;
    private Map<String, Double> biases;
    private Map<String, Double> activationFunctions;
    private AICapacityNumber accuracy;
    private long trainingTime;
    private Random random;
    
    public AIModel() {
        version = ++modelVersion;
        weights = new HashMap<>();
        biases = new HashMap<>();
        activationFunctions = new HashMap<>();
        random = new Random();
        initializeModel();
    }
    
    private void initializeModel() {
        // Initialize weights
        weights.put("validation", random.nextDouble());
        weights.put("voting", random.nextDouble());
        weights.put("reputation", random.nextDouble());
        
        // Initialize biases
        biases.put("validation", random.nextDouble());
        biases.put("voting", random.nextDouble());
        biases.put("reputation", random.nextDouble());
        
        // Initialize activation functions
        activationFunctions.put("validation", 0.9);
        activationFunctions.put("voting", 0.8);
        activationFunctions.put("reputation", 0.7);
        
        // Initialize accuracy
        accuracy = new AICapacityNumber(0);
    }
    
    public void train(List<TrainingData> zData) {
        long startTime = System.currentTimeMillis();
        
        // Update weights and biases
        for (TrainingData data : zData) {
            String type = data.getType();
            Object input = data.getData();
            boolean label = data.getLabel();
            
            // Calculate prediction
            double prediction = predict(type, input);
            
            // Calculate error
            double error = label ? 1.0 - prediction : prediction;
            
            // Update weights and biases
            updateWeights(type, error);
            updateBiases(type, error);
        }
        
        // Calculate accuracy
        accuracy = calculateAccuracy(zData);
        
        // Update training time
        trainingTime = System.currentTimeMillis() - startTime;
    }
    
    private double predict(String zType, Object zInput) {
        double weight = weights.get(zType);
        double bias = biases.get(zType);
        double activation = activationFunctions.get(zType);
        
        // Convert input to numeric value
        double input = getInputValue(zType, zInput);
        
        // Calculate weighted sum
        double weightedSum = weight * input + bias;
        
        // Apply activation function
        return activationFunction(weightedSum, activation);
    }
    
    private double getInputValue(String zType, Object zInput) {
        switch (zType) {
            case "proposal_validation":
                return validateProposal((MiniData) zInput);
            case "voting_pattern":
                return evaluateVote((MiniData) zInput);
            case "reputation_update":
                return calculateReputation((String) zInput);
            default:
                return 0.0;
        }
    }
    
    private double validateProposal(MiniData zProposalID) {
        PointBasedVoting voting = PointBasedVoting.getInstance();
        MiniNumber points = voting.getTotalProposalPoints().getOrDefault(zProposalID, MiniNumber.ZERO);
        return points.toDouble() / SELFParams.MAX_PROPOSAL_POINTS.toDouble();
    }
    
    private double evaluateVote(MiniData zProposalID) {
        ProposalEvaluator evaluator = ProposalEvaluator.getInstance();
        double score = evaluator.calculateReputationScore(zProposalID);
        return score;
    }
    
    private double calculateReputation(String zReason) {
        AIValidator validator = AIValidator.getInstance();
        double reputation = validator.getReputation().toDouble();
        return reputation;
    }
    
    private double activationFunction(double zValue, double zActivation) {
        return 1.0 / (1.0 + Math.exp(-zActivation * zValue));
    }
    
    private void updateWeights(String zType, double zError) {
        double weight = weights.get(zType);
        weights.put(zType, weight - zError * SELFParams.LEARNING_RATE.toDouble());
    }
    
    private void updateBiases(String zType, double zError) {
        double bias = biases.get(zType);
        biases.put(zType, bias - zError * SELFParams.LEARNING_RATE.toDouble());
    }
    
    private AICapacityNumber calculateAccuracy(List<TrainingData> zData) {
        int correct = 0;
        for (TrainingData data : zData) {
            String type = data.getType();
            Object input = data.getData();
            boolean label = data.getLabel();
            
            double prediction = predict(type, input);
            boolean predicted = prediction >= 0.5;
            
            if (predicted == label) {
                correct++;
            }
        }
        return new AICapacityNumber(correct).divide(new AICapacityNumber(zData.size()));
    }
    
    public long getVersion() {
        return version;
    }
    
    public AICapacityNumber getAccuracy() {
    public AICapacityNumber getAccuracy() {
        return accuracy;
    }
    
    public long getTrainingTime() {
        return trainingTime;
    }
    
    public Map<String, Double> getWeights() {
        return new HashMap<>(weights);
    }
    
    public Map<String, Double> getBiases() {
        return new HashMap<>(biases);
    }
    
    public Map<String, Double> getActivationFunctions() {
        return new HashMap<>(activationFunctions);
    }
    
    public void setWeights(Map<String, Double> zWeights) {
        weights = new HashMap<>(zWeights);
    }
    
    public void setBiases(Map<String, Double> zBiases) {
        biases = new HashMap<>(zBiases);
    }
    
    public void setActivationFunctions(Map<String, Double> zFunctions) {
        activationFunctions = new HashMap<>(zFunctions);
    }
    
    public void reset() {
        initializeModel();
    }
}
