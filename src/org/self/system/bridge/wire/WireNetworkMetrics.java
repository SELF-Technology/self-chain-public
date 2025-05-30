package org.self.system.bridge.wire;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.self.SELFNumber;
import org.self.system.bridge.BridgeStatus;
import org.self.system.params.SELFParams;

public class WireNetworkMetrics {
    private int totalTransactions;
    private int successfulTransactions;
    private SELFNumber totalAmount;
    private SELFNumber totalFees;
    private Map<String, WireNodeStatus> nodeStatus;
    private Map<String, WireConnectionStatus> connectionStatus;
    
    public WireNetworkMetrics() {
        totalTransactions = 0;
        successfulTransactions = 0;
        totalAmount = SELFNumber.ZERO;
        totalFees = SELFNumber.ZERO;
        nodeStatus = new HashMap<>();
        connectionStatus = new HashMap<>();
    }
    
    public void incrementTotalTransactions() {
        totalTransactions++;
    }
    
    public void incrementSuccessfulTransactions() {
        successfulTransactions++;
    }
    
    public void addTotalAmount(SELFNumber zAmount) {
        totalAmount = totalAmount.add(zAmount);
    }
    
    public void addTotalFees(SELFNumber zFee) {
        totalFees = totalFees.add(zFee);
    }
    
    public void updateNodeStatus(String zNodeID, boolean zIsOnline) {
        WireNodeStatus status = nodeStatus.getOrDefault(zNodeID, new WireNodeStatus());
        status.setOnline(zIsOnline);
        status.incrementUptime(zIsOnline);
        nodeStatus.put(zNodeID, status);
    }
    
    public void updateConnectionStatus(String zConnectionID, boolean zIsActive) {
        WireConnectionStatus status = connectionStatus.getOrDefault(zConnectionID, new WireConnectionStatus());
        status.setActive(zIsActive);
        status.incrementActiveTime(zIsActive);
        connectionStatus.put(zConnectionID, status);
    }
    
    public int getTotalTransactions() {
        return totalTransactions;
    }
    
    public int getSuccessfulTransactions() {
        return successfulTransactions;
    }
    
    public MiniNumber getTotalAmount() {
        return totalAmount;
    }
    
    public MiniNumber getTotalFees() {
        return totalFees;
    }
    
    public Map<String, WireNodeStatus> getNodeStatus() {
        return new HashMap<>(nodeStatus);
    }
    
    public Map<String, WireConnectionStatus> getConnectionStatus() {
        return new HashMap<>(connectionStatus);
    }
    
    public double getSuccessRate() {
        return totalTransactions > 0 ? 
            ((double) successfulTransactions / totalTransactions) * 100 : 0;
    }
    
    public double getAverageTransactionAmount() {
        return totalTransactions > 0 ? 
            totalAmount.toDouble() / totalTransactions : 0;
    }
    
    public double getAverageFee() {
        return totalTransactions > 0 ? 
            totalFees.toDouble() / totalTransactions : 0;
    }
    
    public void resetMetrics() {
        totalTransactions = 0;
        successfulTransactions = 0;
        totalAmount = MiniNumber.ZERO;
        totalFees = MiniNumber.ZERO;
        nodeStatus.clear();
        connectionStatus.clear();
    }
}
