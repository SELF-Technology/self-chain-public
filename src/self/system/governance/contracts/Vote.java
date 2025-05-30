package org.self.system.governance.contracts;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;

public class Vote {
    private AIData voteID;
    private AIData proposalID;
    private AIData validatorID;
    private AICapacityNumber voteValue;
    private String reason;
    private long timestamp;
    
    public Vote(AIData zVoteID, AIData zProposalID, AIData zValidatorID, AICapacityNumber zVoteValue) {
        voteID = zVoteID;
        proposalID = zProposalID;
        validatorID = zValidatorID;
        voteValue = zVoteValue;
        reason = "";
        timestamp = System.currentTimeMillis();
    }
    
    public void setReason(String zReason) {
        reason = zReason;
    }
    
    public AIData getVoteID() {
        return voteID;
    }
    
    public AIData getProposalID() {
        return proposalID;
    }
    
    public AIData getValidatorID() {
        return validatorID;
    }
    
    public AICapacityNumber getVoteValue() {
        return voteValue;
    }
    
    public String getReason() {
        return reason;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String toString() {
        return String.format(
            "Vote[" +
            "id=%s, " +
            "proposal=%s, " +
            "validator=%s, " +
            "value=%s, " +
            "reason=%s]",
            voteID.toString(),
            proposalID.toString(),
            validatorID.toString(),
            voteValue.toString(),
            reason
        );
    }
}
