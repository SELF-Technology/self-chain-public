package org.self.system.main;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.params.SELFParams;

public class NodeParticipation {
    private AIData nodeID;
    private AICapacityNumber uptime;
    private AICapacityNumber resources;
    private AICapacityNumber reputation;
    
    public NodeParticipation(AIData zNodeID) {
        nodeID = zNodeID;
        uptime = AICapacityNumber.ZERO;
        resources = new AICapacityNumber(SELFParams.MIN_RESOURCE_ALLOCATION.toDouble());
        reputation = new AICapacityNumber(SELFParams.MIN_REPUTATION.toDouble());
    }
    
    public AICapacityNumber calculateReward() {
        // Calculate weighted scores
        AICapacityNumber resourceScore = resources.multiply(new AICapacityNumber(SELFParams.RESOURCE_WEIGHT.toDouble()));
        AICapacityNumber uptimeScore = uptime.multiply(new AICapacityNumber(SELFParams.UPTIME_WEIGHT.toDouble()));
        AICapacityNumber reputationScore = this.reputation.multiply(new AICapacityNumber(SELFParams.REPUTATION_WEIGHT.toDouble()));
        
        // Calculate total reward
        return new AICapacityNumber(SELFParams.REWARD_BASE.toDouble())
                .multiply(resourceScore.add(uptimeScore).add(reputationScore));
    }
    
    public void updateUptime(AICapacityNumber zUptime) {
        uptime = zUptime;
    }
    
    public void updateResources(AICapacityNumber zResources) {
        if (zResources.compareTo(new AICapacityNumber(SELFParams.MIN_RESOURCE_ALLOCATION.toDouble())) >= 0 && 
            zResources.compareTo(new AICapacityNumber(SELFParams.MAX_RESOURCE_ALLOCATION.toDouble())) <= 0) {
            resources = zResources;
        }
    }
    
    public void updateReputation(AICapacityNumber zReputation) {
        if (zReputation.compareTo(new AICapacityNumber(SELFParams.MIN_REPUTATION.toDouble())) >= 0 && 
            zReputation.compareTo(new AICapacityNumber(SELFParams.MAX_REPUTATION.toDouble())) <= 0) {
            reputation = zReputation;
        }
    }
    
    public AIData getNodeID() {
        return nodeID;
    }
    
    public AICapacityNumber getUptime() {
        return uptime;
    }
    
    public AICapacityNumber getResources() {
        return resources;
    }
    
    public AICapacityNumber getReputation() {
        return reputation;
    }
}
