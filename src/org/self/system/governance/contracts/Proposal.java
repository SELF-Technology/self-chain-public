package org.self.system.governance.contracts;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;

public class Proposal {
    private AIData proposalID;
    private String title;
    private String description;
    private String type;
    private Map<String, String> parameters;
    private AICapacityNumber totalVotes;
    private AICapacityNumber score;
    private String status;
    private long timestamp;
    
    public Proposal(AIData zProposalID, String zTitle, String zDescription, String zType) {
        proposalID = zProposalID;
        title = zTitle;
        description = zDescription;
        type = zType;
        parameters = new HashMap<>();
        totalVotes = AICapacityNumber.ZERO;
        score = new AICapacityNumber(0.0);
        status = "pending";
        timestamp = System.currentTimeMillis();
    }
    
    public void setParameter(String zKey, String zValue) {
        parameters.put(zKey, zValue);
    }
    
    public void setTotalVotes(AICapacityNumber zVotes) {
        totalVotes = zVotes;
    }
    
    public void setScore(AICapacityNumber zScore) {
        score = zScore;
    }
    
    public void setStatus(String zStatus) {
        status = zStatus;
    }
    
    public AIData getProposalID() {
        return proposalID;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getType() {
        return type;
    }
    
    public Map<String, String> getParameters() {
        return new HashMap<>(parameters);
    }
    
    public AICapacityNumber getTotalVotes() {
        return totalVotes;
    }
    
    public AICapacityNumber getScore() {
        return score;
    }
    
    public String getStatus() {
        return status;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String toString() {
        return String.format(
            "Proposal[" +
            "id=%s, " +
            "title=%s, " +
            "type=%s, " +
            "status=%s, " +
            "votes=%s, " +
            "score=%.2f]",
            proposalID.toString(),
            title,
            type,
            status,
            totalVotes.toString(),
            score.toDouble()
        );
    }
}
