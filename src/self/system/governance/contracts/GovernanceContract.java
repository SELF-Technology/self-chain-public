package org.self.system.governance.contracts;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.ai.AIVotingSystem;
import org.self.system.governance.ai.PointBasedVoting;
import org.self.system.governance.ai.ProposalEvaluator;
import org.self.system.governance.monitor.GovernanceMonitor;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class GovernanceContract {
    private static GovernanceContract instance;
    private Map<MiniData, Proposal> proposals;
    private Map<MiniData, Vote> votes;
    private AIVotingSystem votingSystem;
    private ProposalEvaluator evaluator;
    private GovernanceMonitor monitor;
    private Timer proposalTimer;
    private long proposalPeriod;
    
    private GovernanceContract() {
        proposals = new HashMap<>();
        votes = new HashMap<>();
        votingSystem = new AIVotingSystem();
        evaluator = new ProposalEvaluator();
        monitor = GovernanceMonitor.getInstance();
        proposalPeriod = SELFParams.PROPOSAL_PERIOD.toLong();
        initializeProposalSystem();
    }
    
    public static GovernanceContract getInstance() {
        if (instance == null) {
            instance = new GovernanceContract();
        }
        return instance;
    }
    
    private void initializeProposalSystem() {
        proposalTimer = new Timer();
        proposalTimer.schedule(new ProposalTask(), 0, proposalPeriod);
    }
    
    public void submitProposal(Proposal zProposal) {
        MiniData proposalID = zProposal.getProposalID();
        
        // Validate proposal
        if (!validateProposal(zProposal)) {
            throw new IllegalArgumentException("Invalid proposal");
        }
        
        // Evaluate proposal
        double score = evaluator.evaluateProposal(zProposal);
        zProposal.setScore(score);
        
        // Add to proposals
        proposals.put(proposalID, zProposal);
        
        // Log proposal
        monitor.log(String.format(
            "Proposal submitted: id=%s, title=%s, score=%.2f",
            proposalID.toString(),
            zProposal.getTitle(),
            score
        ));
    }
    
    public void submitVote(Vote zVote) {
        MiniData voteID = zVote.getVoteID();
        
        // Validate vote
        if (!validateVote(zVote)) {
            throw new IllegalArgumentException("Invalid vote");
        }
        
        // Add to votes
        votes.put(voteID, zVote);
        
        // Update proposal status
        updateProposalStatus(zVote.getProposalID());
        
        // Log vote
        monitor.log(String.format(
            "Vote submitted: id=%s, proposal=%s, validator=%s",
            voteID.toString(),
            zVote.getProposalID().toString(),
            zVote.getValidatorID().toString()
        ));
    }
    
    private boolean validateProposal(Proposal zProposal) {
        // Validate proposal requirements
        return zProposal != null &&
               zProposal.getProposalID() != null &&
               zProposal.getTitle() != null &&
               zProposal.getDescription() != null &&
               zProposal.getType() != null;
    }
    
    private boolean validateVote(Vote zVote) {
        // Validate vote requirements
        return zVote != null &&
               zVote.getVoteID() != null &&
               zVote.getProposalID() != null &&
               zVote.getValidatorID() != null &&
               zVote.getVoteValue() != null;
    }
    
    private void updateProposalStatus(MiniData zProposalID) {
        Proposal proposal = proposals.get(zProposalID);
        if (proposal != null) {
            // Calculate total votes
            MiniNumber totalVotes = MiniNumber.ZERO;
            for (Vote vote : votes.values()) {
                if (vote.getProposalID().equals(zProposalID)) {
                    totalVotes = totalVotes.add(vote.getVoteValue());
                }
            }
            
            // Update proposal status
            proposal.setTotalVotes(totalVotes);
            
            // Check if proposal is complete
            if (isProposalComplete(proposal)) {
                processProposal(proposal);
            }
        }
    }
    
    private boolean isProposalComplete(Proposal zProposal) {
        // Check if proposal has enough votes
        MiniNumber requiredVotes = SELFParams.REQUIRED_VOTES;
        return zProposal.getTotalVotes().compareTo(requiredVotes) >= 0;
    }
    
    private void processProposal(Proposal zProposal) {
        // Process proposal based on type
        switch (zProposal.getType()) {
            case "upgrade":
                processUpgradeProposal(zProposal);
                break;
            case "parameter":
                processParameterProposal(zProposal);
                break;
            case "reward":
                processRewardProposal(zProposal);
                break;
            default:
                throw new IllegalArgumentException("Unknown proposal type");
        }
    }
    
    private void processUpgradeProposal(Proposal zProposal) {
        // Process upgrade proposal
        UpgradeManager upgradeManager = UpgradeManager.getInstance();
        upgradeManager.processUpgrade(zProposal.getParameters());
    }
    
    private void processParameterProposal(Proposal zProposal) {
        // Process parameter proposal
        SELFParams.updateParameters(zProposal.getParameters());
    }
    
    private void processRewardProposal(Proposal zProposal) {
        // Process reward proposal
        RewardManager rewardManager = RewardManager.getInstance();
        rewardManager.processRewardProposal(zProposal);
    }
    
    private class ProposalTask extends TimerTask {
        @Override
        public void run() {
            // Process pending proposals
            for (Map.Entry<MiniData, Proposal> entry : proposals.entrySet()) {
                Proposal proposal = entry.getValue();
                if (isProposalComplete(proposal)) {
                    processProposal(proposal);
                }
            }
            
            // Generate proposal report
            generateProposalReport();
        }
    }
    
    private void generateProposalReport() {
        StringBuilder report = new StringBuilder("\n=== PROPOSAL REPORT ===\n");
        
        for (Map.Entry<MiniData, Proposal> entry : proposals.entrySet()) {
            Proposal proposal = entry.getValue();
            report.append(String.format(
                "\nProposal: %s\n" +
                "Title: %s\n" +
                "Status: %s\n" +
                "Votes: %s\n" +
                "Score: %.2f\n",
                proposal.getProposalID().toString(),
                proposal.getTitle(),
                proposal.getStatus(),
                proposal.getTotalVotes().toString(),
                proposal.getScore()
            ));
        }
        
        monitor.log(report.toString());
    }
    
    public Proposal getProposal(MiniData zProposalID) {
        return proposals.get(zProposalID);
    }
    
    public Vote getVote(MiniData zVoteID) {
        return votes.get(zVoteID);
    }
    
    public Map<MiniData, Proposal> getProposals() {
        return new HashMap<>(proposals);
    }
    
    public Map<MiniData, Vote> getVotes() {
        return new HashMap<>(votes);
    }
    
    public void resetContract() {
        proposals.clear();
        votes.clear();
        if (proposalTimer != null) {
            proposalTimer.cancel();
            proposalTimer = null;
        }
    }
}
