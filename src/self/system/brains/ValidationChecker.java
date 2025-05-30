package org.self.system.brains;

import org.self.objects.SELFData;
import org.self.objects.SELFNumber;
import org.self.objects.Transaction;
import org.self.objects.ValidationMessage;
import org.self.objects.TxPoWTreeNode;
import org.self.objects.TxHeader;
import org.self.objects.SELFData;
import java.util.ArrayList;

public class ValidationChecker {
    
    /**
     * The current network chain ID
     */
    public static final SELFData CURRENT_NETWORK = new SELFData("SELF_CHAIN");
    
    /**
     * The minimum validation score required
     */
    public static final SELFNumber MIN_VALIDATION_SCORE = new SELFNumber("0.5");
    
    /**
     * The maximum state store size
     */
    public static final SELFNumber MAX_STATE_STORE_SIZE = new SELFNumber("1000000");
    
    /**
     * The minimum validator reputation required
     */
    public static final SELFNumber MIN_VALIDATOR_REPUTATION = new SELFNumber("0.7");
    
    /**
     * The maximum validation time window
     */
    public static final SELFNumber MAX_VALIDATION_TIME = new SELFNumber("300000"); // 5 minutes
    
    /**
     * Check validator rewards
     */
    public static boolean checkValidatorRewards(ValidationMessage validation) {
        // Get the transaction
        Transaction tx = validation.getTransaction();
        
        // Must be a validator reward transaction
        if (!(tx instanceof ValidatorRewardTx)) {
            return false;
        }
        return true;
    }
    
    /**
     * Check validation basic requirements
     */
    public static boolean checkValidationBasic(ValidationMessage validation) {
        // Check chain ID
        if(!validation.getHeader().getChainID().isEqual(CURRENT_NETWORK)) {
            return false;
        }
        
        // Check validation score
        if(validation.getValidationScore().compareTo(MIN_VALIDATION_SCORE) < 0) {
            return false;
        }
        
        // Check state store size
        if(validation.getBody().getStateStoreSize().compareTo(MAX_STATE_STORE_SIZE) > 0) {
            return false;
        }
        
        // Check validation time window
        SELFNumber currentTime = SELFNumber.fromLong(SELFSystem.getInstance().getSystemTime());
        SELFNumber validationTime = validation.getValidationTime();
        if(currentTime.subtract(validationTime).compareTo(MAX_VALIDATION_TIME) > 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check validation signatures
     */
    public static boolean checkValidationSignatures(ValidationMessage validation) {
        // Check validator signature
        if(!Crypto.verifySignature(
                validation.getValidatorID(),
                validation.getValidationScore().toString(),
                validation.getHeader().getSignature())) {
            return false;
        }
        
        // Check witness signatures
        for(Witness witness : validation.getBody().getWitnesses()) {
            if(!Crypto.verifySignature(
                    witness.getWitnessID(),
                    validation.getValidationScore().toString(),
                    witness.getSignature())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check validation difficulty
     */
    public static boolean checkValidationDifficulty(ValidationMessage validation) {
        // Check if validation meets current difficulty requirement
        SELFNumber currentDifficulty = SELFParams.CURRENT_VALIDATION_DIFFICULTY;
        
        // Calculate validation difficulty score
        SELFNumber difficultyScore = SELFNumber.ONE;
        
        // Add bonus for quick validation
        SELFNumber currentTime = SELFNumber.fromLong(SELFSystem.getInstance().getSystemTime());
        SELFNumber validationTime = validation.getValidationTime();
        SELFNumber timeDiff = currentTime.subtract(validationTime);
        if(timeDiff.compareTo(SELFNumber.fromLong(60000)) <= 0) { // Within 1 minute
            difficultyScore = difficultyScore.add(difficultyScore.multiply(SELFNumber.fromDouble(0.2)));
        }
        
        // Add bonus for validator reputation
        SELFNumber validatorReputation = SelfDB.getDB().getValidatorAverageScore(validation.getValidatorID());
        if(validatorReputation.compareTo(MIN_VALIDATOR_REPUTATION) >= 0) {
            difficultyScore = difficultyScore.add(difficultyScore.multiply(SELFNumber.fromDouble(0.1)));
        }
        
        // Check if difficulty score meets requirement
        if(difficultyScore.compareTo(currentDifficulty) < 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * MAX Time block checker..
     */
    public static boolean checkValidationBlockTimed(TxPoWTreeNode zParentNode, ValidationMessage validation, ArrayList<ValidationMessage> validations) {
        
        // Create new checker..
        TimedChecker tc = new TimedChecker();
        
        // Run it in a timed environment
        return tc.checkValidationBlock(zParentNode, validation, validations);
    }
}
