package org.self.system.cloud;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.params.SELFParams;

public class CloudNodeRegistry {
    private static CloudNodeRegistry instance;
    private Map<AIData, CloudNodeManager> nodeMap;
    
    private CloudNodeRegistry() {
        nodeMap = new HashMap<>();
    }
    
    public static CloudNodeRegistry getInstance() {
        if (instance == null) {
            instance = new CloudNodeRegistry();
        }
        return instance;
    }
    
    public void registerNode(AIData zNodeID) {
        if (!nodeMap.containsKey(zNodeID)) {
            nodeMap.put(zNodeID, new CloudNodeManager(zNodeID));
        }
    }
    
    public void unregisterNode(AIData zNodeID) {
        nodeMap.remove(zNodeID);
    }
    
    public void updateNodeResources(AIData zNodeID, AICapacityNumber zResources) {
        CloudNodeManager manager = nodeMap.get(zNodeID);
        if (manager != null) {
            manager.updateResourceAllocation(zResources);
        }
    }
    
    public void updateNodeUptime(AIData zNodeID, AICapacityNumber zUptime) {
        CloudNodeManager manager = nodeMap.get(zNodeID);
        if (manager != null) {
            manager.updateUptime(zUptime);
        }
    }
    
    public void updateNodeReputation(AIData zNodeID, AICapacityNumber zScore) {
        CloudNodeManager manager = nodeMap.get(zNodeID);
        if (manager != null) {
            manager.updateReputation(zScore);
        }
    }
    
    public AICapacityNumber calculateNodeReward(AIData zNodeID) {
        CloudNodeManager manager = nodeMap.get(zNodeID);
        return manager != null ? manager.calculateNodeReward() : AICapacityNumber.ZERO;
    }
    
    public CloudNodeManager getNodeManager(AIData zNodeID) {
        return nodeMap.get(zNodeID);
    }
    
    public int getNodeCount() {
        return nodeMap.size();
    }
    
    public AICapacityNumber getTotalResourceAllocation() {
        AICapacityNumber total = AICapacityNumber.ZERO;
        for (CloudNodeManager manager : nodeMap.values()) {
            total = total.add(manager.getResourceAllocation());
        }
        return total;
    }
    
    public AICapacityNumber getAverageReputation() {
        if (nodeMap.isEmpty()) {
            return new AICapacityNumber(SELFParams.MIN_REPUTATION.toDouble());
        }
        
        AICapacityNumber total = AICapacityNumber.ZERO;
        for (CloudNodeManager manager : nodeMap.values()) {
            total = total.add(manager.getReputationScore());
        }
        
        return total.divide(new AICapacityNumber(nodeMap.size()));
    }
}
