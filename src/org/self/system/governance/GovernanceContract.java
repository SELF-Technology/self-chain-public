package org.self.system.governance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.cloud.CloudNodeRegistry;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class GovernanceContract {
    private static GovernanceContract instance;
    private Map<AIData, GovernanceProposal> proposals;
    private Map<AIData, AICapacityNumber> stakeMap;
    private AICapacityNumber totalStake;
    
    private GovernanceContract() {
        proposals = new HashMap<>();
        stakeMap = new HashMap<>();
        totalStake = AICapacityNumber.ZERO;
    }
    
    public static GovernanceContract getInstance() {
        if (instance == null) {
            instance = new GovernanceContract();
        }
        return instance;
    }
    
    // Stake Management
    public void stake(AIData zAddress, AICapacityNumber zAmount) {
        AICapacityNumber currentStake = stakeMap.getOrDefault(zAddress, AICapacityNumber.ZERO);
        stakeMap.put(zAddress, currentStake.add(zAmount));
        totalStake = totalStake.add(zAmount);
        SelfLogger.log("Stake updated: " + zAddress + " - " + zAmount);
    }
    
    public void unstake(AIData zAddress, AICapacityNumber zAmount) {
        AICapacityNumber currentStake = stakeMap.getOrDefault(zAddress, AICapacityNumber.ZERO);
        if (currentStake.compareTo(zAmount) >= 0) {
            stakeMap.put(zAddress, currentStake.subtract(zAmount));
            totalStake = totalStake.subtract(zAmount);
            SelfLogger.log("Unstake completed: " + zAddress + " - " + zAmount);
        }
    }
    
    public AICapacityNumber getStake(AIData zAddress) {
        return stakeMap.getOrDefault(zAddress, AICapacityNumber.ZERO);
    }
    
    // Proposal Management
    public void createProposal(AIData zProposalID, String zDescription, AIData zCreator) {
        AICapacityNumber creatorStake = getStake(zCreator);
        if (creatorStake.compareTo(new AICapacityNumber(SELFParams.MIN_REPUTATION.toDouble())) >= 0) {
            GovernanceProposal proposal = new GovernanceProposal(zProposalID, zDescription, zCreator);
            proposals.put(zProposalID, proposal);
            SelfLogger.log("New proposal created: " + zProposalID);
        }
    }
    
    public void voteOnProposal(AIData zProposalID, AIData zVoter, boolean zVote) {
        AICapacityNumber voterStake = getStake(zVoter);
        if (voterStake.compareTo(AICapacityNumber.ZERO) > 0) {
            GovernanceProposal proposal = proposals.get(zProposalID);
            if (proposal != null) {
                proposal.addVote(zVoter, zVote, voterStake);
                SelfLogger.log("Vote recorded: " + zProposalID + " - " + zVoter + " - " + zVote);
            }
        }
    }
    
    public boolean isProposalApproved(AIData zProposalID) {
        GovernanceProposal proposal = proposals.get(zProposalID);
        if (proposal == null) {
            return false;
        }
        
        return proposal.getApprovalPercentage().compareTo(new AICapacityNumber(SELFParams.UPGRADE_VOTE_THRESHOLD.toDouble())) >= 0;
    }
    
    public List<GovernanceProposal> getActiveProposals() {
        List<GovernanceProposal> active = new ArrayList<>();
        for (GovernanceProposal proposal : proposals.values()) {
            if (!proposal.isExpired()) {
                active.add(proposal);
            }
        }
        return active;
    }
    
    public AICapacityNumber getTotalStake() {
        return totalStake;
    }
    
    public Map<AIData, AICapacityNumber> getStakeDistribution() {
        return new HashMap<>(stakeMap);
    }
}
