package org.self.system.governance.points;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.GovernanceProposal;
import org.self.system.governance.ai.AIVotingSystem;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class PointBasedVoting {
    private static PointBasedVoting instance;
    private Map<AIData, Map<AIData, AICapacityNumber>> userVotes;
    private Map<AIData, AICapacityNumber> totalProposalPoints;
    
    private PointBasedVoting() {
        userVotes = new HashMap<>();
        totalProposalPoints = new HashMap<>();
    }
    
    public static PointBasedVoting getInstance() {
        if (instance == null) {
            instance = new PointBasedVoting();
        }
        return instance;
    }
    
    public boolean castVote(AIData zUserID, GovernanceProposal zProposal, boolean zVote) {
        // Check if user has enough points
        PointSystem pointSystem = PointSystem.getInstance();
        AICapacityNumber userPoints = pointSystem.getUserPoints(zUserID);
        
        if (userPoints.compareTo(new AICapacityNumber(SELFParams.MIN_STAKE_FOR_VOTE.toDouble())) < 0) {
            return false;
        }
        
        // Check if user has already voted
        if (userVotes.containsKey(zUserID) && 
            userVotes.get(zUserID).containsKey(zProposal.getProposalID())) {
            return false;
        }
        
        // Calculate voting power
        AICapacityNumber votingPower = calculateVotingPower(zUserID, zProposal);
        
        // Record vote
        recordVote(zUserID, zProposal.getProposalID(), votingPower, zVote);
        
        // Update proposal points
        updateProposalPoints(zProposal.getProposalID(), votingPower, zVote);
        
        return true;
    }
    
    private AICapacityNumber calculateVotingPower(AIData zUserID, GovernanceProposal zProposal) {
        PointSystem pointSystem = PointSystem.getInstance();
        AICapacityNumber userPoints = pointSystem.getUserPoints(zUserID);
        
        // Calculate reputation bonus
        AIValidator validator = AIVotingSystem.getInstance().getValidator(zUserID);
        AICapacityNumber reputation = validator.getReputation();
        AICapacityNumber reputationBonus = userPoints.multiply(reputation.divide(new AICapacityNumber(SELFParams.MAX_REPUTATION.toDouble())));
        
        return userPoints.add(reputationBonus);
    }
    
    private void recordVote(AIData zUserID, AIData zProposalID, 
                          AICapacityNumber zVotingPower, boolean zVote) {
        Map<AIData, AICapacityNumber> userVotesMap = userVotes.getOrDefault(zUserID, new HashMap<>());
        if (zVote) {
            userVotesMap.put(zProposalID, zVotingPower);
        } else {
            userVotesMap.put(zProposalID, AICapacityNumber.ZERO);
        }
        userVotes.put(zUserID, userVotesMap);
    }
    
    private void updateProposalPoints(AIData zProposalID, AICapacityNumber zVotingPower, boolean zVote) {
        AICapacityNumber currentPoints = totalProposalPoints.getOrDefault(zProposalID, AICapacityNumber.ZERO);
        if (zVote) {
            totalProposalPoints.put(zProposalID, currentPoints.add(zVotingPower));
        } else {
            totalProposalPoints.put(zProposalID, currentPoints.subtract(zVotingPower));
        }
    }
    
    public boolean isProposalApproved(AIData zProposalID) {
        AICapacityNumber points = totalProposalPoints.getOrDefault(zProposalID, AICapacityNumber.ZERO);
        PointSystem pointSystem = PointSystem.getInstance();
        AICapacityNumber totalPoints = pointSystem.getUserPoints(zProposalID);
        
        if (totalPoints.compareTo(AICapacityNumber.ZERO) <= 0) {
            return false;
        }
        
        AICapacityNumber percentage = points.divide(totalPoints).multiply(new AICapacityNumber(100));
        return percentage.compareTo(new AICapacityNumber(SELFParams.MIN_VOTE_THRESHOLD.toDouble())) >= 0;
    }
    
    public Map<MiniData, MiniNumber> getUserVotes(MiniData zUserID) {
        return new HashMap<>(userVotes.getOrDefault(zUserID, new HashMap<>()));
    }
    
    public Map<MiniData, MiniNumber> getTotalProposalPoints() {
        return new HashMap<>(totalProposalPoints);
    }
    
    public void resetVotes() {
        userVotes.clear();
        totalProposalPoints.clear();
    }
}
