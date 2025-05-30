package org.self.system.governance.ml;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.GovernanceProposal;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class MLModel {
    private static MLModel instance;
    private Map<String, Double> modelParameters;
    private Map<String, Double> learningRates;
    private Random random;
    
    private MLModel() {
        modelParameters = new HashMap<>();
        learningRates = new HashMap<>();
        initializeParameters();
        random = new Random();
    }
    
    public static MLModel getInstance() {
        if (instance == null) {
            instance = new MLModel();
        }
        return instance;
    }
    
    private void initializeParameters() {
        // Initialize model parameters
        modelParameters.put("bias", 0.0);
        modelParameters.put("stake_weight", 0.3);
        modelParameters.put("reputation_weight", 0.2);
        modelParameters.put("resource_weight", 0.2);
        modelParameters.put("network_weight", 0.15);
        modelParameters.put("consensus_weight", 0.15);
        
        // Initialize learning rates
        learningRates.put("bias", 0.01);
        learningRates.put("stake_weight", 0.01);
        learningRates.put("reputation_weight", 0.01);
        learningRates.put("resource_weight", 0.01);
        learningRates.put("network_weight", 0.01);
        learningRates.put("consensus_weight", 0.01);
    }
    
    public AICapacityNumber predict(GovernanceProposal zProposal) {
        ProposalEvaluator evaluator = ProposalEvaluator.getInstance();
        Map<String, Double> features = evaluator.calculateFeatures(zProposal);
        
        double prediction = modelParameters.get("bias");
        
        for (Map.Entry<String, Double> entry : features.entrySet()) {
            String feature = entry.getKey();
            double value = entry.getValue();
            double weight = modelParameters.get(feature + "_weight");
            prediction += value * weight;
        }
        
        return new AICapacityNumber(Math.max(0.0, Math.min(1.0, prediction)));
    }
    
    public void train(GovernanceProposal zProposal, AICapacityNumber zActualScore) {
        AICapacityNumber predictedScore = predict(zProposal);
        double error = zActualScore.getAsDouble() - predictedScore.getAsDouble();
        
        // Update parameters using gradient descent
        for (String param : modelParameters.keySet()) {
            if (!param.equals("bias")) {
                double gradient = error * ProposalEvaluator.getInstance().getFeatureWeights().get(param);
                double learningRate = learningRates.get(param);
                modelParameters.put(param, modelParameters.get(param) + gradient * learningRate);
            }
        }
        
        // Update bias
        modelParameters.put("bias", modelParameters.get("bias") + error * learningRates.get("bias"));
    }
    
    public void updateParameters(Map<String, Double> zNewParameters) {
        modelParameters.putAll(zNewParameters);
    }
    
    public Map<String, Double> getModelParameters() {
        return new HashMap<>(modelParameters);
    }
    
    public void updateLearningRates(Map<String, Double> zNewRates) {
        learningRates.putAll(zNewRates);
    }
    
    public Map<String, Double> getLearningRates() {
        return new HashMap<>(learningRates);
    }
}
