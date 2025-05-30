package org.self.system.brains;

import org.self.database.SelfDB;
import org.self.objects.ValidationMessage;
import org.self.objects.SELFData;
import org.self.objects.SELFNumber;
import org.self.objects.SELFSystem;
import org.self.utils.SelfLogger;

public class WitnessSelector extends MessageProcessor {
    
    /**
     * Minimum number of witnesses required
     */
    public static final int MIN_WITNESSES = 3;
    
    /**
     * Maximum number of witnesses allowed
     */
    public static final int MAX_WITNESSES = 10;
    
    /**
     * Minimum reputation required to be a witness
     */
    public static final SELFNumber MIN_WITNESS_REPUTATION = new SELFNumber("0.6");
    
    /**
     * Minimum time between witness selections
     */
    public static final SELFNumber MIN_WITNESS_INTERVAL = new SELFNumber("300000"); // 5 minutes
    
    public WitnessSelector() {
        super("WITNESS_SELECTOR");
    }
    
    /**
     * Select witnesses for a validation
     */
    public ArrayList<SELFData> selectWitnesses(ValidationMessage validation) {
        try {
            // Get all eligible validators
            ArrayList<SELFData> validators = getEligibleValidators();
            
            if(validators.isEmpty()) {
                SelfLogger.log("No eligible validators found for witnesses");
                return new ArrayList<>();
            }
            
            // Sort validators by reputation
            validators.sort((v1, v2) -> {
                SELFNumber rep1 = SelfDB.getDB().getValidatorReputation(v1);
                SELFNumber rep2 = SelfDB.getDB().getValidatorReputation(v2);
                return rep2.compareTo(rep1);
            });
            
            // Select witnesses based on reputation and availability
            ArrayList<SELFData> witnesses = new ArrayList<>();
            int numWitnesses = Math.min(MAX_WITNESSES, Math.max(MIN_WITNESSES, validators.size() / 2));
            
            for(int i = 0; i < numWitnesses; i++) {
                SELFData validator = validators.get(i);
                
                // Check if validator is eligible
                if(isValidatorEligible(validator, validation)) {
                    witnesses.add(validator);
                }
            }
            
            // If we don't have enough witnesses, add more from the pool
            if(witnesses.size() < MIN_WITNESSES) {
                addAdditionalWitnesses(validators, witnesses, validation);
            }
            
            return witnesses;
            
        } catch (Exception e) {
            SelfLogger.log("Error selecting witnesses: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get list of eligible validators
     */
    private ArrayList<SELFData> getEligibleValidators() {
        try {
            // Get active validators
            ArrayList<SELFData> validators = SelfDB.getDB().getActiveValidators();
            
            // Filter based on reputation
            ArrayList<SELFData> eligible = new ArrayList<>();
            for(SELFData validator : validators) {
                SELFNumber reputation = SelfDB.getDB().getValidatorReputation(validator);
                if(reputation.compareTo(MIN_WITNESS_REPUTATION) >= 0) {
                    eligible.add(validator);
                }
            }
            
            return eligible;
            
        } catch (Exception e) {
            SelfLogger.log("Error getting eligible validators: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Check if validator is eligible to be a witness
     */
    private boolean isValidatorEligible(SELFData validator, ValidationMessage validation) {
        try {
            // Check reputation
            SELFNumber reputation = SelfDB.getDB().getValidatorReputation(validator);
            if(reputation.compareTo(MIN_WITNESS_REPUTATION) < 0) {
                return false;
            }
            
            // Check last validation time
            SELFNumber lastValidation = SelfDB.getDB().getValidatorLastValidation(validator);
            SELFNumber currentTime = SELFNumber.fromLong(SELFSystem.getInstance().getSystemTime());
            
            if(currentTime.subtract(lastValidation).compareTo(MIN_WITNESS_INTERVAL) < 0) {
                return false;
            }
            
            // Check if validator is not the original validator
            if(validator.isEqual(validation.getValidatorID())) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            SelfLogger.log("Error checking validator eligibility: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Add additional witnesses if needed
     */
    private void addAdditionalWitnesses(ArrayList<SELFData> validators, 
                                       ArrayList<SELFData> witnesses, 
                                       ValidationMessage validation) {
        try {
            // Add witnesses from remaining validators
            for(SELFData validator : validators) {
                if(witnesses.size() >= MIN_WITNESSES) {
                    break;
                }
                
                if(!witnesses.contains(validator) && 
                   isValidatorEligible(validator, validation)) {
                    witnesses.add(validator);
                }
            }
            
        } catch (Exception e) {
            SelfLogger.log("Error adding additional witnesses: " + e.getMessage());
        }
    }
    
    /**
     * Verify witness signatures
     */
    public boolean verifyWitnesses(ValidationMessage validation) {
        try {
            // Get all witnesses
            ArrayList<SELFData> witnesses = validation.getBody().getWitnesses();
            
            if(witnesses.size() < MIN_WITNESSES) {
                SelfLogger.log("Not enough witnesses: " + witnesses.size());
                return false;
            }
            
            // Verify each witness signature
            for(SELFData witness : witnesses) {
                if(!verifyWitnessSignature(validation, witness)) {
                    SelfLogger.log("Invalid witness signature from: " + witness);
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            SelfLogger.log("Error verifying witnesses: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verify single witness signature
     */
    private boolean verifyWitnessSignature(ValidationMessage validation, SELFData witness) {
        try {
            // Get witness signature
            String signature = validation.getBody().getWitnessSignature(witness);
            
            // Verify signature
            return Crypto.verifySignature(
                witness,
                validation.getValidationID() + validation.getValidationScore().toString(),
                signature
            );
            
        } catch (Exception e) {
            SelfLogger.log("Error verifying witness signature: " + e.getMessage());
            return false;
        }
    }
}
