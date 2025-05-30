package org.self.system.governance.monitor;

import java.util.Map;
import java.util.HashMap;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.GovernanceProposal;
import org.self.system.governance.points.PointSystem;
import org.self.system.params.SELFParams;

public class GovernanceMetrics {
    private GovernanceProposal proposal;
    private double approvalRate;
    private double predictionAccuracy;
    private double resourceUsage;
    private Map<String, Integer> voteDistribution;
    
    public GovernanceMetrics(GovernanceProposal zProposal) {
        proposal = zProposal;
        approvalRate = 0.0;
        predictionAccuracy = 0.0;
        resourceUsage = 0.0;
        voteDistribution = new HashMap<>();
        initializeVoteDistribution();
    }
    
    private void initializeVoteDistribution() {
        voteDistribution.put("yes", 0);
        voteDistribution.put("no", 0);
        voteDistribution.put("abstain", 0);
    }
    
    public void updateApprovalRate() {
        PointBasedVoting voting = PointBasedVoting.getInstance();
        AIData proposalID = proposal.getProposalID();
        
        AICapacityNumber points = voting.getProposalPoints(proposalID);
        AICapacityNumber totalPoints = PointSystem.getInstance().getUserPoints(proposalID);
        
        if (totalPoints.compareTo(AICapacityNumber.ZERO) > 0) {
            approvalRate = points.divide(totalPoints).getAsDouble() * 100;
        }
    }
    
    public void updateVoteDistribution() {
        PointBasedVoting voting = PointBasedVoting.getInstance();
        Map<AIData, AICapacityNumber> votes = voting.getUserVotes(proposal.getProposalID());
        
        int yes = 0;
        int no = 0;
        int abstain = 0;
        
        for (Map.Entry<AIData, AICapacityNumber> entry : votes.entrySet()) {
            AICapacityNumber points = entry.getValue();
            if (points.compareTo(AICapacityNumber.ZERO) > 0) {
                yes++;
            } else if (points.compareTo(AICapacityNumber.ZERO) < 0) {
                no++;
            } else {
                abstain++;
            }
        }
        
        voteDistribution.put("yes", yes);
        voteDistribution.put("no", no);
        voteDistribution.put("abstain", abstain);
    }
    
    public void updateResourceUsage() {
        AICapacityNumber resources = proposal.getResourcesUsed();
        resourceUsage = resources.divide(new AICapacityNumber(SELFParams.MAX_RESOURCE_ALLOCATION)).getAsDouble() * 100;
    }
    
    public void updatePredictionAccuracy(double zPrediction, double zActualScore) {
        double diff = Math.abs(zPrediction - zActualScore);
        predictionAccuracy = 100.0 - (diff * 100.0);
    }
    
    public double getApprovalRate() {
        return approvalRate;
    }
    
    public double getPredictionAccuracy() {
        return predictionAccuracy;
    }
    
    public double getResourceUsage() {
        return resourceUsage;
    }
    
    public Map<String, Integer> getVoteDistribution() {
        return new HashMap<>(voteDistribution);
    }
    
    public GovernanceProposal getProposal() {
        return proposal;
    }
}
