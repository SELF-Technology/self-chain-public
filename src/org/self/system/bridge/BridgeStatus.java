package org.self.system.bridge;

import org.self.objects.self.SELFNumber;

public class BridgeStatus {
    private int totalTransactions;
    private SELFNumber totalAmount;
    private SELFNumber totalFee;
    private SELFNumber averageFee;
    private SELFNumber maxFee;
    private SELFNumber minFee;
    
    public BridgeStatus() {
        totalTransactions = 0;
        totalAmount = SELFNumber.ZERO;
        totalFee = SELFNumber.ZERO;
        averageFee = SELFNumber.ZERO;
        maxFee = SELFNumber.ZERO;
        minFee = SELFNumber.ZERO;
    }
    
    public void incrementTransactions() {
        totalTransactions++;
    }
    
    public void addTotalAmount(SELFNumber zAmount) {
        totalAmount = totalAmount.add(zAmount);
    }
    
    public void addTotalFee(SELFNumber zFee) {
        totalFee = totalFee.add(zFee);
        
        // Update min/max fees
        // Update max fee
        if (zFee.compareTo(maxFee) > 0) {
            maxFee = zFee;
        }
        
        // Update min fee
        if (zFee.compareTo(minFee) < 0 || minFee.equals(SELFNumber.ZERO)) {
            minFee = zFee;
        }
        
        // Update average fee
        SELFNumber newTotal = totalFee.add(zFee);
        averageFee = newTotal.divide(new SELFNumber(totalTransactions));
    }
    
    public int getTotalTransactions() {
        return totalTransactions;
    }
    
    public SELFNumber getTotalAmount() {
        return totalAmount;
    }
    
    public SELFNumber getTotalFee() {
        return totalFee;
    }
    
    public SELFNumber getAverageFee() {
        return averageFee;
    }
    
    public SELFNumber getMaxFee() {
        return maxFee;
    }
    
    public SELFNumber getMinFee() {
        return minFee;
    }
    
    public void reset() {
        totalTransactions = 0;
        totalAmount = SELFNumber.ZERO;
        totalFee = SELFNumber.ZERO;
        averageFee = SELFNumber.ZERO;
        maxFee = SELFNumber.ZERO;
        minFee = SELFNumber.ZERO;
    }
}
