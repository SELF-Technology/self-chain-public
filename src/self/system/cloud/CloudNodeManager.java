package org.self.system.cloud;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.main.NodeParticipation;
import org.self.system.params.SELFParams;

public class CloudNodeManager {
    private AIData nodeID;
    private AICapacityNumber cloudProvider;
    private AICapacityNumber resourceAllocation;
    private AICapacityNumber reputationScore;
    private AICapacityNumber uptime;
    private NodeParticipation participation;
    
    public CloudNodeManager(AIData zNodeID) {
        nodeID = zNodeID;
        cloudProvider = AICapacityNumber.ZERO;
        resourceAllocation = new AICapacityNumber(SELFParams.MIN_RESOURCE_ALLOCATION.toDouble());
        reputationScore = new AICapacityNumber(SELFParams.MIN_REPUTATION.toDouble());
        uptime = AICapacityNumber.ZERO;
        participation = new NodeParticipation(zNodeID);
    }
    
    public void updateResourceAllocation(AICapacityNumber zResources) {
        if (zResources.compareTo(new AICapacityNumber(SELFParams.MIN_RESOURCE_ALLOCATION.toDouble())) >= 0 && 
            zResources.compareTo(new AICapacityNumber(SELFParams.MAX_RESOURCE_ALLOCATION.toDouble())) <= 0) {
            resourceAllocation = zResources;
            participation.updateResources(zResources);
        }
    }
    
    public void updateUptime(AICapacityNumber zUptime) {
        uptime = zUptime;
        participation.updateUptime(zUptime);
    }
    
    public void updateReputation(AICapacityNumber zScore) {
        if (zScore.compareTo(new AICapacityNumber(SELFParams.MIN_REPUTATION.toDouble())) >= 0 && 
            zScore.compareTo(new AICapacityNumber(SELFParams.MAX_REPUTATION.toDouble())) <= 0) {
            reputationScore = zScore;
            participation.updateReputation(zScore);
        }
    }
    
    public AICapacityNumber calculateNodeReward() {
        return participation.calculateReward();
    }
    
    public AIData getNodeID() {
        return nodeID;
    }
    
    public AICapacityNumber getResourceAllocation() {
        return resourceAllocation;
    }
    
    public AICapacityNumber getReputationScore() {
        return reputationScore;
    }
    
    public AICapacityNumber getUptime() {
        return uptime;
    }
    
    public NodeParticipation getNodeParticipation() {
        return participation;
    }
}
