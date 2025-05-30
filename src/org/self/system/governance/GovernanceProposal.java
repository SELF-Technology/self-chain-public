package org.self.system.governance;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class GovernanceProposal {
    private AIData proposalID;
    private String description;
    private AIData creator;
    private Date creationTime;
    private Map<AIData, Boolean> votes;
    private Map<AIData, AICapacityNumber> voteStakes;
    private AICapacityNumber totalStake;
    
    public GovernanceProposal(AIData zProposalID, String zDescription, AIData zCreator) {
        proposalID = zProposalID;
        description = zDescription;
        creator = zCreator;
        creationTime = new Date();
        votes = new HashMap<>();
        voteStakes = new HashMap<>();
        totalStake = AICapacityNumber.ZERO;
        
        // Creator automatically gets a yes vote
        votes.put(creator, true);
        voteStakes.put(creator, getCreatorStake());
        totalStake = getCreatorStake();
    }
    
    private AICapacityNumber getCreatorStake() {
        return GovernanceContract.getInstance().getStake(creator);
    }
    
    public void addVote(AIData zVoter, boolean zVote, AICapacityNumber zStake) {
        if (!votes.containsKey(zVoter)) {
            votes.put(zVoter, zVote);
            voteStakes.put(zVoter, zStake);
            if (zVote) {
                totalStake = totalStake.add(zStake);
            }
        }
    }
    
    public AICapacityNumber getApprovalPercentage() {
        AICapacityNumber totalVoterStake = AICapacityNumber.ZERO;
        for (AICapacityNumber stake : voteStakes.values()) {
            totalVoterStake = totalVoterStake.add(stake);
        }
        
        if (totalVoterStake.compareTo(AICapacityNumber.ZERO) <= 0) {
            return AICapacityNumber.ZERO;
        }
        
        return totalStake.divide(totalVoterStake).multiply(new AICapacityNumber(100));
    }
    
    public boolean isExpired() {
        long creationTimeSeconds = creationTime.getTime() / 1000;
        long currentTimeSeconds = new Date().getTime() / 1000;
        
        return (currentTimeSeconds - creationTimeSeconds) > SELFParams.UPGRADE_GRACE_PERIOD.toLong();
    }
    
    public AIData getProposalID() {
        return proposalID;
    }
    
    public String getDescription() {
        return description;
    }
    
    public AIData getCreator() {
        return creator;
    }
    
    public Date getCreationTime() {
        return creationTime;
    }
    
    public Map<AIData, Boolean> getVotes() {
        return new HashMap<>(votes);
    }
    
    public Map<AIData, AICapacityNumber> getVoteStakes() {
        return new HashMap<>(voteStakes);
    }
    
    public int getTotalVotes() {
        return votes.size();
    }
    
    public AICapacityNumber getTotalStake() {
        return totalStake;
    }
}
