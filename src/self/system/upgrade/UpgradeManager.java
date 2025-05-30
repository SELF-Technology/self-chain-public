package org.self.system.upgrade;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.system.params.SELFParams;
import org.self.system.params.GeneralParams;
import org.self.system.params.GlobalParams;

public class UpgradeManager {
    private static UpgradeManager instance;
    private Map<MiniData, UpgradeProposal> proposals;
    private MiniNumber currentVersion;
    
    private UpgradeManager() {
        proposals = new HashMap<>();
        currentVersion = new MiniNumber(GlobalParams.SELF_VERSION);
    }
    
    public static UpgradeManager getInstance() {
        if (instance == null) {
            instance = new UpgradeManager();
        }
        return instance;
    }
    
    public void createProposal(MiniData zProposalID, String zDescription, MiniNumber zTargetVersion) {
        if (zTargetVersion.compareTo(currentVersion) > 0) {
            UpgradeProposal proposal = new UpgradeProposal(zProposalID, zDescription, zTargetVersion);
            proposals.put(zProposalID, proposal);
        }
    }
    
    public void voteOnProposal(MiniData zProposalID, MiniData zVoterID, boolean zVote) {
        UpgradeProposal proposal = proposals.get(zProposalID);
        if (proposal != null) {
            proposal.addVote(zVoterID, zVote);
        }
    }
    
    public boolean isProposalApproved(MiniData zProposalID) {
        UpgradeProposal proposal = proposals.get(zProposalID);
        if (proposal == null) {
            return false;
        }
        
        return proposal.getApprovalPercentage().compareTo(SELFParams.UPGRADE_VOTE_THRESHOLD) >= 0;
    }
    
    public void executeApprovedUpgrades() {
        for (UpgradeProposal proposal : proposals.values()) {
            if (proposal.isApproved() && 
                proposal.getTargetVersion().compareTo(currentVersion) > 0) {
                // Execute upgrade
                executeUpgrade(proposal.getTargetVersion());
                
                // Update current version
                currentVersion = proposal.getTargetVersion();
                
                // Remove proposal
                proposals.remove(proposal.getProposalID());
            }
        }
    }
    
    private void executeUpgrade(MiniNumber zNewVersion) {
        // TODO: Implement actual upgrade logic
        GeneralParams.SELF_VERSION = zNewVersion.toString();
        GlobalParams.SELF_BASE_VERSION = "SELF_" + zNewVersion;
    }
    
    public MiniNumber getCurrentVersion() {
        return currentVersion;
    }
    
    public Map<MiniData, UpgradeProposal> getProposals() {
        return new HashMap<>(proposals);
    }
}
