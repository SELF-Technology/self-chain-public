package org.self.system.governance;

import java.util.List;
import java.util.Map;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.cloud.CloudNodeRegistry;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class GovernanceManager {
    private static GovernanceManager instance;
    private GovernanceContract contract;
    
    private GovernanceManager() {
        contract = GovernanceContract.getInstance();
    }
    
    public static GovernanceManager getInstance() {
        if (instance == null) {
            instance = new GovernanceManager();
        }
        return instance;
    }
    
    // Resource Allocation Management
    public void allocateResources(AIData zNodeID, AICapacityNumber zAmount) {
        AICapacityNumber stake = contract.getStake(zNodeID);
        if (stake.compareTo(AICapacityNumber.ZERO) > 0) {
            CloudNodeRegistry.getInstance().updateNodeResources(zNodeID, zAmount);
            SelfLogger.log("Resources allocated to node: " + zNodeID + " - " + zAmount);
        }
    }
    
    // Bridge Parameter Management
    public void updateBridgeParameters(Map<String, Object> zParams) {
        // Create proposal for bridge parameter update
        AIData proposalID = AIData.getRandomData();
        String description = "Bridge parameter update proposal";
        
        contract.createProposal(proposalID, description, getProposalCreator());
        
        // Add parameters to proposal
        GovernanceProposal proposal = contract.getProposal(proposalID);
        if (proposal != null) {
            proposal.setParameters(zParams);
        }
    }
    
    // Upgrade Management
    public void proposeUpgrade(String zDescription, AICapacityNumber zTargetVersion) {
        AIData proposalID = AIData.getRandomData();
        
        contract.createProposal(proposalID, zDescription, getProposalCreator());
        
        // Add upgrade details to proposal
        GovernanceProposal proposal = contract.getProposal(proposalID);
        if (proposal != null) {
            proposal.setTargetVersion(zTargetVersion);
        }
    }
    
    // Resource Management
    public void manageNodeResources(AIData zNodeID, AICapacityNumber zAmount) {
        AICapacityNumber stake = contract.getStake(zNodeID);
        if (stake.compareTo(AICapacityNumber.ZERO) > 0) {
            CloudNodeRegistry.getInstance().updateNodeResources(zNodeID, zAmount);
            SelfLogger.log("Node resources updated: " + zNodeID + " - " + zAmount);
        }
    }
    
    // Proposal Voting
    public void voteOnProposal(AIData zProposalID, boolean zVote) {
        AIData voter = getVoter();
        AICapacityNumber stake = contract.getStake(voter);
        if (stake.compareTo(AICapacityNumber.ZERO) > 0) {
            contract.voteOnProposal(zProposalID, voter, zVote);
        }
    }
    
    // Helper Methods
    private AIData getProposalCreator() {
        // Get the node with highest stake
        Map<AIData, AICapacityNumber> stakes = contract.getStakeDistribution();
        AIData creator = null;
        AICapacityNumber maxStake = AICapacityNumber.ZERO;
        
        for (Map.Entry<AIData, AICapacityNumber> entry : stakes.entrySet()) {
            if (entry.getValue().compareTo(maxStake) > 0) {
                creator = entry.getKey();
                maxStake = entry.getValue();
            }
        }
        
        return creator;
    }
    
    private MiniData getVoter() {
        // Get a random node with sufficient stake
        Map<MiniData, MiniNumber> stakes = contract.getStakeDistribution();
        List<MiniData> voters = new ArrayList<>();
        
        for (Map.Entry<SELFData, SELFNumber> entry : stakes.entrySet()) {
            voters.add(entry.getKey());
        }
        
        if (voters.isEmpty()) {
            return null;
        }
        
        return voters.get(new Random().nextInt(voters.size()));
    }
    
    // Monitoring
    public List<GovernanceProposal> getActiveProposals() {
        return contract.getActiveProposals();
    }
    
    public Map<SELFData, SELFNumber> getStakeDistribution() {
        return contract.getStakeDistribution();
    }
    
    public SELFNumber getTotalStake() {
        return contract.getTotalStake();
    }
}
