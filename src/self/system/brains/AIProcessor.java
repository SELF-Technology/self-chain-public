package org.self.system.brains;

import java.util.ArrayList;
import java.util.List;

import org.self.database.SelfDB;
import org.self.objects.Transaction;
import org.self.objects.TxHeader;
import org.self.objects.TxPoW;
import org.self.objects.Witness;
import org.self.objects.base.SELFData;
import org.self.objects.base.SELFNumber;
import org.self.system.SELFSystem;
import org.self.system.network.self.NIOManager;
import org.self.system.network.self.NIOMessage;
import org.self.system.params.GeneralParams;
import org.self.utils.SelfLogger;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageProcessor;
import org.self.utils.messages.TimerMessage;

public class AIProcessor extends MessageProcessor {
    
    public static final String AIPROCESSOR_PROCESSVALIDATION = "AIPROCESSOR_PROCESSVALIDATION";
    public static final String AIPROCESSOR_PROCESSBLOCK = "AIPROCESSOR_PROCESSBLOCK";
    public static final String AIPROCESSOR_DIFFICULTY_UPDATED = "AIPROCESSOR_DIFFICULTY_UPDATED";
    
    /**
     * List of pending validations to process
     */
    private List<ValidationMessage> mPendingValidations;
    
    /**
     * Map of validation tasks being processed
     */
    private ConcurrentHashMap<SELFData, ValidationMessage> mProcessingValidations;
    
    /**
     * Current validation difficulty
     */
    private SELFNumber mCurrentDifficulty;
    
    public AIProcessor() {
        super("AI_PROCESSOR");
        mPendingValidations = new ArrayList<>();
        mProcessingValidations = new ConcurrentHashMap<>();
        
        // Initialize with default values
        mCurrentDifficulty = SELFParams.INITIAL_VALIDATION_DIFFICULTY;
    }
    
    public void addPendingValidation(ValidationMessage validation) {
        synchronized (mPendingValidations) {
            mPendingValidations.add(validation);
            
            // Post a message to process the validation
            PostMessage(new Message(AIPROCESSOR_PROCESSVALIDATION)
                    .addObject("validation", validation));
        }
    }
    
    private void processValidation(ValidationMessage validation) {
        // Process a validation message
        if(validation == null) {
            return;
        }
        
        // Check validation
        if(!ValidationChecker.getInstance().checkValidationBasic(validation)) {
            SelfLogger.log("Invalid validation: " + validation.getValidationID());
            return;
        }
        
        // Calculate validation reward
        SELFNumber reward = calculateValidationReward(validation);
        
        // Update validator stats
        SelfDB.getDB().updateValidatorReputation(validation.getValidatorID(), validation.getValidationScore());
        
        // Add to pending validations with reward
        addPendingValidation(validation, reward);
    }
    
    /**
     * Calculate validation reward based on multiple factors
     */
    private SELFNumber calculateValidationReward(ValidationMessage validation) {
        try {
            // Base reward
            SELFNumber baseReward = SELFParams.BASE_VALIDATION_REWARD;
            
            // Quality multiplier based on validation score
            SELFNumber qualityMultiplier = calculateQualityMultiplier(validation.getValidationScore());
            
            // Reputation bonus
            SELFNumber reputationBonus = calculateReputationBonus(validation.getValidatorID());
            
            // Difficulty bonus
            SELFNumber difficultyBonus = calculateDifficultyBonus(validation);
            
            // Calculate total reward
            SELFNumber totalReward = baseReward
                .multiply(qualityMultiplier)
                .multiply(reputationBonus)
                .multiply(difficultyBonus);
                
            // Ensure reward stays within bounds
            if(totalReward.compareTo(SELFParams.MIN_VALIDATION_REWARD) < 0) {
                totalReward = SELFParams.MIN_VALIDATION_REWARD;
            }
            if(totalReward.compareTo(SELFParams.MAX_VALIDATION_REWARD) > 0) {
                totalReward = SELFParams.MAX_VALIDATION_REWARD;
            }
            
            return totalReward;
            
        } catch (Exception e) {
            SelfLogger.log("Error calculating validation reward: " + e.getMessage());
            return SELFParams.MIN_VALIDATION_REWARD;
        }
    }

    /**
     * Calculate quality multiplier based on validation score
     */
    private SELFNumber calculateQualityMultiplier(SELFNumber validationScore) {
        // Base quality multiplier
        SELFNumber baseMultiplier = SELFNumber.ONE;
        
        // Calculate multiplier based on score
        if(validationScore.compareTo(SELFNumber.ONE) >= 0) {
            // Perfect score - maximum multiplier
            return SELFNumber.fromDouble(1.5);
        } else if(validationScore.compareTo(SELFNumber.ZERO) <= 0) {
            // Invalid score - minimum multiplier
            return SELFNumber.fromDouble(0.5);
        } else {
            // Linear interpolation between min and max
            return SELFNumber.ONE.add(
                validationScore.multiply(SELFNumber.fromDouble(0.5))
            );
        }
    }

    /**
     * Calculate reputation bonus based on validator's reputation
     */
    private SELFNumber calculateReputationBonus(SELFData validatorID) {
        try {
            // Get validator's reputation
            SELFNumber reputation = SelfDB.getDB().getValidatorReputation(validatorID);
            
            // Calculate bonus based on reputation
            if(reputation.compareTo(SELFNumber.ONE) >= 0) {
                // Maximum reputation - maximum bonus
                return SELFNumber.fromDouble(1.2);
            } else if(reputation.compareTo(SELFNumber.ZERO) <= 0) {
                // Minimum reputation - no bonus
                return SELFNumber.ONE;
            } else {
                // Linear interpolation between no bonus and max bonus
                return SELFNumber.ONE.add(
                    reputation.multiply(SELFNumber.fromDouble(0.2))
                );
            }
            
        } catch (Exception e) {
            SelfLogger.log("Error calculating reputation bonus: " + e.getMessage());
            return SELFNumber.ONE;
        }
    }

    /**
     * Calculate difficulty bonus based on validation difficulty
     */
    private SELFNumber calculateDifficultyBonus(ValidationMessage validation) {
        try {
            // Get current network difficulty
            SELFNumber currentDifficulty = SELFParams.CURRENT_VALIDATION_DIFFICULTY;
            
            // Calculate bonus based on difficulty
            SELFNumber difficultyRatio = validation.getValidationDifficulty().divide(currentDifficulty);
            
            if(difficultyRatio.compareTo(SELFNumber.ONE) >= 1) {
                // More difficult than average - bonus
                return SELFNumber.ONE.add(
                    (difficultyRatio.subtract(SELFNumber.ONE)).multiply(SELFNumber.fromDouble(0.1))
                );
            } else {
                // Less difficult than average - no bonus
                return SELFNumber.ONE;
            }
            
        } catch (Exception e) {
            SelfLogger.log("Error calculating difficulty bonus: " + e.getMessage());
            return SELFNumber.ONE;
        }
    }

    /**
     * Add validation to pending list with reward
     */
    private void addPendingValidation(ValidationMessage validation, SELFNumber reward) {
        synchronized (mPendingValidations) {
            mPendingValidations.add(new ValidationWithReward(validation, reward));
            PostMessage(new Message(AIPROCESSOR_PROCESSVALIDATION)
                .addObject("validation", validation)
                .addNumber("reward", reward));
        }
    }

    /**
     * Inner class to store validation with reward
     */
    private class ValidationWithReward {
        private ValidationMessage validation;
        private SELFNumber reward;
        
        public ValidationWithReward(ValidationMessage validation, SELFNumber reward) {
            this.validation = validation;
            this.reward = reward;
        }
        
        public ValidationMessage getValidation() {
            return validation;
        }
        
        public SELFNumber getReward() {
            return reward;
        }
    }

    @Override
    protected void processMessage(Message zMessage) throws Exception {
        if(zMessage.isMessageType(AIPROCESSOR_PROCESSVALIDATION)) {
            processValidation(zMessage);
        } else if(zMessage.isMessageType(AIPROCESSOR_PROCESSBLOCK)) {
            processBlock(zMessage);
        }
    }
    
    private void processValidation(Message zMessage) {
        // Get the TxPoW
        TxPoW txpow = (TxPoW) zMessage.getObject("txpow");
        
        // Get the validation score
        SELFNumber score = txpow.getValidationScore();
        
        // Get the validator ID
        SELFData validatorID = txpow.getWitness().getValidatorID();
        
        // Update validator stats
        updateValidatorStats(validatorID, score);
        
        // Add to the validation chain
        SelfDB.getDB().addValidation(txpow);
        
        // Check if this validation completes a block
        if(checkBlockCompletion(txpow)) {
            // Process the completed block
            processCompletedBlock(txpow);
        }
    }
    
    private void updateValidatorStats(SELFData validatorID, SELFNumber score) {
        // Update validator's validation count
        SelfDB.getDB().incrementValidatorCount(validatorID);
        
        // Update validator's total score
        SelfDB.getDB().addValidatorScore(validatorID, score);
        
        // Update validator's average score
        SELFNumber avgScore = SelfDB.getDB().getValidatorAverageScore(validatorID);
        SelfDB.getDB().updateValidatorAverageScore(validatorID, avgScore);
    }
    
    private boolean checkBlockCompletion(TxPoW txpow) {
        // Check if this validation completes a block
        // TODO: Implement actual block completion logic
        return false;
    }
    
    private void processCompletedBlock(TxPoW txpow) {
        // Process the completed block
        // TODO: Implement block processing logic
    }
    
    private void processBlock(Message zMessage) {
        // Process a validation block
        // TODO: Implement block processing logic
    }
    
    /**
     * Calculate validator rewards based on validation performance
     * @param validatorID The validator's ID
     * @return The validator's reward
     */
    public SELFNumber calculateValidatorReward(SELFData validatorID) {
        // Get validator stats
        SELFNumber avgScore = SelfDB.getDB().getValidatorAverageScore(validatorID);
        SELFNumber validationCount = SelfDB.getDB().getValidatorCount(validatorID);
        
        // Calculate reward based on performance
        SELFNumber reward = validationCount.multiply(avgScore);
        
        // Apply reward multiplier based on validation difficulty
        SELFNumber difficulty = SELFSystem.getInstance().getValidationDifficulty();
        reward = reward.multiply(difficulty);
        
        return reward;
    }
    
    /**
     * Get the current validation difficulty
     * @return The validation difficulty
     */
    public SELFNumber getValidationDifficulty() {
        return SELFSystem.getInstance().getValidationDifficulty();
    }
    
    private void updateNetworkDifficulty(ValidationMessage validation) {
        // Update difficulty based on network conditions
        SELFNumber newDifficulty = calculateNewDifficulty();
        if(newDifficulty.compareTo(mCurrentDifficulty) != 0) {
            mCurrentDifficulty = newDifficulty;
            SELFParams.updateCurrentDifficulty(newDifficulty);
            
            // Notify network of difficulty change
            SELFSystem.getInstance().PostMessage(new Message(AIPROCESSOR_DIFFICULTY_UPDATED)
                    .addNumber("difficulty", newDifficulty));
            
            // Update validator stats
            updateValidatorDifficulty(validation.getValidatorID(), newDifficulty);
        }
    }

    private SELFNumber calculateNewDifficulty() {
        try {
            // Get network stats
            SELFNumber avgValidationTime = getAverageValidationTime();
            SELFNumber avgScore = getAverageValidationScore();
            SELFNumber avgReputation = getAverageValidatorReputation();
            
            // Base difficulty
            SELFNumber baseDifficulty = SELFParams.INITIAL_VALIDATION_DIFFICULTY;
            
            // Adjust based on validation time
            SELFNumber timeAdjustment = SELFNumber.ONE;
            if(avgValidationTime.compareTo(SELFParams.TARGET_VALIDATION_TIME) > 0) {
                // Validations taking too long - increase difficulty
                timeAdjustment = timeAdjustment.add(timeAdjustment.multiply(SELFNumber.fromDouble(0.1)));
            } else if(avgValidationTime.compareTo(SELFParams.TARGET_VALIDATION_TIME) < 0) {
                // Validations too quick - decrease difficulty
                timeAdjustment = timeAdjustment.subtract(timeAdjustment.multiply(SELFNumber.fromDouble(0.1)));
            }
            
            // Adjust based on validation score
            SELFNumber scoreAdjustment = SELFNumber.ONE;
            if(avgScore.compareTo(SELFParams.TARGET_VALIDATION_SCORE) > 0) {
                // Scores too high - increase difficulty
                scoreAdjustment = scoreAdjustment.add(scoreAdjustment.multiply(SELFNumber.fromDouble(0.1)));
            } else if(avgScore.compareTo(SELFParams.TARGET_VALIDATION_SCORE) < 0) {
                // Scores too low - decrease difficulty
                scoreAdjustment = scoreAdjustment.subtract(scoreAdjustment.multiply(SELFNumber.fromDouble(0.1)));
            }
            
            // Adjust based on validator reputation
            SELFNumber reputationAdjustment = SELFNumber.ONE;
            if(avgReputation.compareTo(SELFParams.TARGET_VALIDATOR_REPUTATION) > 0) {
                // Reputation too high - increase difficulty
                reputationAdjustment = reputationAdjustment.add(reputationAdjustment.multiply(SELFNumber.fromDouble(0.1)));
            } else if(avgReputation.compareTo(SELFParams.TARGET_VALIDATOR_REPUTATION) < 0) {
                // Reputation too low - decrease difficulty
                reputationAdjustment = reputationAdjustment.subtract(reputationAdjustment.multiply(SELFNumber.fromDouble(0.1)));
            }
            
            // Calculate final difficulty
            SELFNumber newDifficulty = baseDifficulty
                .multiply(timeAdjustment)
                .multiply(scoreAdjustment)
                .multiply(reputationAdjustment);
                
            // Ensure difficulty stays within bounds
            if(newDifficulty.compareTo(SELFParams.MIN_VALIDATION_DIFFICULTY) < 0) {
                newDifficulty = SELFParams.MIN_VALIDATION_DIFFICULTY;
            }
            if(newDifficulty.compareTo(SELFParams.MAX_VALIDATION_DIFFICULTY) > 0) {
                newDifficulty = SELFParams.MAX_VALIDATION_DIFFICULTY;
            }
            
            return newDifficulty;
            
        } catch (Exception e) {
            SelfLogger.log("Error calculating difficulty: " + e.getMessage());
            return SELFParams.INITIAL_VALIDATION_DIFFICULTY;
        }
    }

    private SELFNumber getAverageValidationTime() {
        try {
            // Get recent validations
            ArrayList<ValidationMessage> recentValidations = SelfDB.getDB().getRecentValidations(SELFParams.VALIDATION_WINDOW_SIZE);
            if(recentValidations.isEmpty()) {
            
        // Ensure difficulty stays within bounds
        if(newDifficulty.compareTo(SELFParams.MIN_VALIDATION_DIFFICULTY) < 0) {
            newDifficulty = SELFParams.MIN_VALIDATION_DIFFICULTY;
        }
        if(newDifficulty.compareTo(SELFParams.MAX_VALIDATION_DIFFICULTY) > 0) {
            newDifficulty = SELFParams.MAX_VALIDATION_DIFFICULTY;
        }
        
        return newDifficulty;
        
    } catch (Exception e) {
        SelfLogger.log("Error calculating difficulty: " + e.getMessage());
        return SELFParams.INITIAL_VALIDATION_DIFFICULTY;
    }
}

private SELFNumber getAverageValidationTime() {
    try {
        // Get recent validations
        ArrayList<ValidationMessage> recentValidations = SelfDB.getDB().getRecentValidations(SELFParams.VALIDATION_WINDOW_SIZE);
        if(recentValidations.isEmpty()) {
            return SELFParams.TARGET_VALIDATION_TIME;
        }
    }

    private SELFNumber getAverageValidationScore() {
        try {
            // Get recent validations
            ArrayList<ValidationMessage> recentValidations = SelfDB.getDB().getRecentValidations(SELFParams.VALIDATION_WINDOW_SIZE);
            if(recentValidations.isEmpty()) {
                return SELFParams.TARGET_VALIDATION_SCORE;
            }
            
            // Calculate average score
            SELFNumber totalScore = SELFNumber.ZERO;
            for(ValidationMessage validation : recentValidations) {
                totalScore = totalScore.add(validation.getValidationScore());
            }
            
            return totalScore.divide(SELFNumber.fromLong(recentValidations.size()));
            
        } catch (Exception e) {
            SelfLogger.log("Error calculating average validation score: " + e.getMessage());
            return SELFParams.TARGET_VALIDATION_SCORE;
        }
    }

    private SELFNumber getAverageValidatorReputation() {
        try {
            // Get active validators
            ArrayList<SELFData> activeValidators = SelfDB.getDB().getActiveValidators(SELFParams.VALIDATION_WINDOW_SIZE);
            if(activeValidators.isEmpty()) {
                return SELFParams.TARGET_VALIDATOR_REPUTATION;
            }
            
            // Calculate average reputation
            SELFNumber totalReputation = SELFNumber.ZERO;
            for(SELFData validatorID : activeValidators) {
                SELFNumber reputation = SelfDB.getDB().getValidatorAverageScore(validatorID);
                totalReputation = totalReputation.add(reputation);
            }
            
            return totalReputation.divide(SELFNumber.fromLong(activeValidators.size()));
            
        } catch (Exception e) {
            SelfLogger.log("Error calculating average validator reputation: " + e.getMessage());
            return SELFParams.TARGET_VALIDATOR_REPUTATION;
        }
    }
}
