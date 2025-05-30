package org.self.system.bridge.erc20;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.self.SELFNumber;
import org.self.objects.base.RosettaNumber;
import org.self.system.bridge.BridgeStatus;
import org.self.system.params.SELFParams;
import org.self.system.bridge.erc20.ERC20TokenStatus;
import org.self.system.bridge.erc20.ERC20AddressStatus;

public class ERC20Metrics {
    private int totalTransactions;
    private int successfulTransactions;
    private SELFNumber totalAmount;
    private SELFNumber totalFees;
    private Map<String, ERC20TokenStatus> tokenStatus;
    private Map<String, ERC20AddressStatus> addressStatus;
    
    public ERC20Metrics() {
        totalTransactions = 0;
        successfulTransactions = 0;
        totalAmount = MiniNumber.ZERO;
        totalFees = MiniNumber.ZERO;
        tokenStatus = new HashMap<>();
        addressStatus = new HashMap<>();
    }
    
    public void incrementTotalTransactions() {
        totalTransactions++;
    }
    
    public void incrementSuccessfulTransactions() {
        successfulTransactions++;
    }
    
    public void addTotalAmount(AICapacityNumber zAmount) {
        totalAmount = totalAmount.add(zAmount);
    }
    
    public void addTotalFees(AICapacityNumber zFee) {
        totalFees = totalFees.add(zFee);
    }
    
    public void updateTokenStatus(String zTokenID, boolean zIsOnline) {
        ERC20TokenStatus status = tokenStatus.getOrDefault(zTokenID, new ERC20TokenStatus());
        status.setOnline(zIsOnline);
        status.incrementUptime(zIsOnline);
        tokenStatus.put(zTokenID, status);
    }
    
    public void updateAddressStatus(String zAddress, boolean zIsValid) {
        ERC20AddressStatus status = addressStatus.getOrDefault(zAddress, new ERC20AddressStatus());
        status.setValid(zIsValid);
        status.incrementValidation(zIsValid);
        addressStatus.put(zAddress, status);
    }
    
    public int getTotalTransactions() {
        return totalTransactions;
    }
    
    public int getSuccessfulTransactions() {
        return successfulTransactions;
    }
    
    public AICapacityNumber getTotalAmount() {
        return totalAmount;
    }
    
    public SELFNumber getTotalFees() {
        return totalFees;
    }
    
    public Map<String, ERC20TokenStatus> getTokenStatus() {
        return new HashMap<>(tokenStatus);
    }
    
    public Map<String, ERC20AddressStatus> getAddressStatus() {
        return new HashMap<>(addressStatus);
    }
    
    public double getSuccessRate() {
        return totalTransactions > 0 ? 
            ((double) successfulTransactions / totalTransactions) * 100 : 0;
    }
    
    public double getAverageTransactionAmount() {
        return totalTransactions > 0 ? 
            totalAmount.divide(new SELFNumber(totalTransactions)).doubleValue() : 0;
    }
    
    public double getAverageFee() {
        return totalTransactions > 0 ? 
            totalFees.divide(new SELFNumber(totalTransactions)).doubleValue() : 0;
    }
    
    public void resetMetrics() {
        totalTransactions = 0;
        successfulTransactions = 0;
        totalAmount = SELFNumber.ZERO;
        totalFees = SELFNumber.ZERO;
        tokenStatus.clear();
        addressStatus.clear();
    }
}
