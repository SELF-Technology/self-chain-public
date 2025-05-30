package org.self.objects;

import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.main.NodeParticipation;
import org.self.system.params.SELFParams;

public class NodeRewardTx extends Transaction {
    private MiniData nodeID;
    private MiniNumber rewardAmount;
    private MiniNumber period;
    private MiniNumber uptime;
    private MiniNumber resources;
    private MiniNumber reputation;
    
    public NodeRewardTx(MiniData zNodeID, MiniNumber zRewardAmount, MiniNumber zPeriod) {
        super();
        nodeID = zNodeID;
        rewardAmount = zRewardAmount;
        period = zPeriod;
    }
    
    public boolean validate() {
        // Validate node participation
        NodeParticipation participation = NodeParticipation.getNodeParticipation(nodeID);
        if (participation == null) {
            return false;
        }
        
        // Check reward calculation
        MiniNumber calculatedReward = participation.calculateReward();
        if (!calculatedReward.equals(rewardAmount)) {
            return false;
        }
        
        // Verify node reputation
        if (participation.getReputation().compareTo(SELFParams.MIN_REPUTATION) < 0 || 
            participation.getReputation().compareTo(SELFParams.MAX_REPUTATION) > 0) {
            return false;
        }
        
        // Store participation metrics
        uptime = participation.getUptime();
        resources = participation.getResources();
        reputation = participation.getReputation();
        
        return true;
    }
    
    public MiniData getNodeID() {
        return nodeID;
    }
    
    public MiniNumber getRewardAmount() {
        return rewardAmount;
    }
    
    public MiniNumber getPeriod() {
        return period;
    }
    
    public MiniNumber getUptime() {
        return uptime;
    }
    
    public MiniNumber getResources() {
        return resources;
    }
    
    public MiniNumber getReputation() {
        return reputation;
    }
}
