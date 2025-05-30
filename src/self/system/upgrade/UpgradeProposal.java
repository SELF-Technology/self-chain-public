package org.self.system.upgrade;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.system.cloud.CloudNodeRegistry;

public class UpgradeProposal {
    private MiniData proposalID;
    private String description;
    private MiniNumber targetVersion;
    private Map<MiniData, Boolean> votes;
    
    public UpgradeProposal(MiniData zProposalID, String zDescription, MiniNumber zTargetVersion) {
        proposalID = zProposalID;
        description = zDescription;
        targetVersion = zTargetVersion;
        votes = new HashMap<>();
    }
    
    public void addVote(MiniData zVoterID, boolean zVote) {
        votes.put(zVoterID, zVote);
    }
    
    public boolean isApproved() {
        return getApprovalPercentage().compareTo(CloudNodeRegistry.getInstance().getAverageReputation()) >= 0;
    }
    
    public MiniNumber getApprovalPercentage() {
        int totalVotes = votes.size();
        int yesVotes = 0;
        
        for (Boolean vote : votes.values()) {
            if (vote) {
                yesVotes++;
            }
        }
        
        if (totalVotes == 0) {
            return MiniNumber.ZERO;
        }
        
        return new MiniNumber(yesVotes).divide(new MiniNumber(totalVotes)).multiply(new MiniNumber(100));
    }
    
    public MiniData getProposalID() {
        return proposalID;
    }
    
    public String getDescription() {
        return description;
    }
    
    public MiniNumber getTargetVersion() {
        return targetVersion;
    }
    
    public Map<MiniData, Boolean> getVotes() {
        return new HashMap<>(votes);
    }
    
    public int getTotalVotes() {
        return votes.size();
    }
    
    public int getYesVotes() {
        int count = 0;
        for (Boolean vote : votes.values()) {
            if (vote) {
                count++;
            }
        }
        return count;
    }
    
    public int getNoVotes() {
        int count = 0;
        for (Boolean vote : votes.values()) {
            if (!vote) {
                count++;
            }
        }
        return count;
    }
}
