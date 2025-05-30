package org.self.system.governance.ai;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;

public class ReputationUpdate {
    private AIData validatorID;
    private String reason;
    private AICapacityNumber change;
    private AICapacityNumber newReputation;
    private long timestamp;
    
    public ReputationUpdate(String zReason, AICapacityNumber zChange, AICapacityNumber zNewReputation) {
        validatorID = null; // Set by system
        reason = zReason;
        change = zChange;
        newReputation = zNewReputation;
        timestamp = System.currentTimeMillis();
    }
    
    public void setValidatorID(AIData zValidatorID) {
        validatorID = zValidatorID;
    }
    
    public AIData getValidatorID() {
        return validatorID;
    }
    
    public String getReason() {
        return reason;
    }
    
    public AICapacityNumber getChange() {
        return change;
    }
    
    public AICapacityNumber getNewReputation() {
        return newReputation;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String toString() {
        return String.format(
            "ReputationUpdate[" +
            "validator=%s, " +
            "reason=%s, " +
            "change=%.2f, " +
            "new=%.2f, " +
            "time=%d]",
            validatorID.toString(),
            reason,
            change.toDouble(),
            newReputation.toDouble(),
            timestamp
        );
    }
}
