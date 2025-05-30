package org.self.system.brains;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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

public class AIValidator extends MessageProcessor {

    public static final String AIVALIDATOR_VALIDATE = "AIVALIDATOR_VALIDATE";
    public static final String AIVALIDATOR_VALIDATEPULSE = "AIVALIDATOR_VALIDATEPULSE";
    public static final String AIVALIDATOR_TXBLOCKVALIDATOR = "AIVALIDATOR_TXBLOCKVALIDATOR";
    
    /**
     * A list of tasks currently being validated
     */
    private ArrayList<String> mValidatingTasks;
    
    /**
     * Map of validator scores
     */
    private ConcurrentHashMap<String, SELFNumber> mValidatorScores;
    
    /**
     * Current validation task
     */
    private ValidationMessage mCurrentTask;
    
    /**
     * Validation score
     */
    private SELFNumber mValidationScore;
    
    /**
     * Validation difficulty
     */
    private SELFNumber mDifficulty;
    
    /**
     * Validator stats
     */
    private ValidatorStats mStats;
    
    /**
     * Current validation difficulty
     */
    private SELFNumber mValidationDifficulty;
    
    public AIValidator() {
        super("AI_VALIDATOR");
        
        mValidatingTasks = new ArrayList<>();
        mValidatorScores = new ConcurrentHashMap<>();
        mValidationDifficulty = new SELFNumber("0.95"); // 95% accuracy required
        
        // Initialize with default values
        mValidationScore = SELFNumber.ZERO;
        mDifficulty = SELFNumber.ZERO;
        mStats = new ValidatorStats();
    }
    
    public void validateAITaskAsync(TxPoW zTxPoW) {
        // Add this task to our validating list
        addValidatingTask(zTxPoW);
        
        // Post a validation message
        PostMessage(new Message(AIVALIDATOR_VALIDATE)
                .addObject("txpow", zTxPoW)
                .addBoolean("automine", false));
    }
    
    public void handleValidationRequest(ValidationMessage request) {
        // Check if we're already processing a task
        if(mCurrentTask != null) {
            SelfLogger.log("Already processing validation task: " + mCurrentTask.getTxPoWID());
            return;
        }
        
        // Set current task
        mCurrentTask = request;
        
        // Start validation process
        PostMessage(new Message(AIVALIDATOR_STARTVALIDATION)
                .addObject("task", request));
    }
    
    @Override
    protected void processMessage(Message zMessage) throws Exception {
        if(zMessage.isMessageType(AIVALIDATOR_VALIDATE)) {
            validateAITask(zMessage);
        } else if(zMessage.isMessageType(AIVALIDATOR_VALIDATEPULSE)) {
            processValidationPulse();
        } else if(zMessage.isMessageType(AIVALIDATOR_TXBLOCKVALIDATOR)) {
            processTxBlockValidation();
        }
    }
    
    private void validateAITask(Message zMessage) {
        // Start time
        long startTime = System.currentTimeMillis();
        
        // Get the TxPoW
        TxPoW txpow = (TxPoW) zMessage.getObject("txpow");
        
        // Get the AI validation task
        Transaction task = txpow.getTransaction();
        
        // Get the validation witness
        Witness witness = txpow.getWitness();
        
        // Calculate the validation score
        SELFNumber score = calculateValidationScore(task, witness);
        
        // Check if validation is successful
        if(score.compareTo(mValidationDifficulty) >= 0) {
            // Validation successful
            txpow.setValidationScore(score);
            
            // Update validator score
            updateValidatorScore(witness.getValidatorID(), score);
            
            // Post validation success message
            SELFSystem.getInstance().PostMessage(new Message(SELFSystem.SELF_TXPOWMINED)
                    .addObject("txpow", txpow));
        }
        
        // Remove the task from validating list
        removeValidatingTask(txpow);
        
        // Log validation time
        if(GeneralParams.VALIDATION_LOGS) {
            long timeTaken = System.currentTimeMillis() - startTime;
            SelfLogger.log("AI VALIDATION FINISHED time:" + timeTaken);
        }
    }
    
    private SELFNumber calculateValidationScore(Transaction task, Witness witness) {
        // TODO: Implement AI validation scoring logic
        // This should use actual AI validation metrics
        // For now, return a random score for testing
        return new SELFNumber(Math.random());
    }
    
    private void updateValidatorScore(SELFData validatorID, SELFNumber score) {
        // Update the validator's score
        SELFNumber currentScore = mValidatorScores.getOrDefault(validatorID.toString(), SELFNumber.ZERO);
        SELFNumber newScore = currentScore.add(score);
        mValidatorScores.put(validatorID.toString(), newScore);
        
        // Update validator score in database
        SelfDB.getDB().updateValidatorScore(validatorID, newScore);
    }
    
    private void processValidationPulse() {
        // This is not where you get your validation task from
        if(GeneralParams.TXBLOCK_NODE) {
            return;
        }
        
        // Do we have any blocks yet
        if(SelfDB.getDB().getTxPoWTree().getTip() != null) {
            // Generate a new validation task
            TxPoW txpow = AIGenerator.generateValidationTask();
            
            // Validate the task
            PostMessage(new Message(AIVALIDATOR_VALIDATE)
                    .addObject("txpow", txpow)
                    .addBoolean("automine", true));
        } else {
            // Check again in 30 seconds
            PostTimerMessage(new TimerMessage(30000, AIVALIDATOR_VALIDATEPULSE));
        }
    }
    
    private void processTxBlockValidation() {
        // This is not where you get your validation block from
        if(GeneralParams.TXBLOCK_NODE) {
            return;
        }
        
        // TODO: Implement block validation logic
    }
    
    // Add/remove tasks from validating list
    private void addValidatingTask(TxPoW zTxPoW) {
        mValidatingTasks.add(zTxPoW.getTxPoWID().toString());
    }
    
    private void removeValidatingTask(TxPoW zTxPoW) {
        mValidatingTasks.remove(zTxPoW.getTxPoWID().toString());
    }
    
    // Check if a task is being validated
    public boolean isTaskBeingValidated(String zTaskID) {
        return mValidatingTasks.contains(zTaskID);
    }
    
    // Get validator score
    public SELFNumber getValidatorScore(SELFData validatorID) {
        return mValidatorScores.getOrDefault(validatorID.toString(), SELFNumber.ZERO);
    }
}
