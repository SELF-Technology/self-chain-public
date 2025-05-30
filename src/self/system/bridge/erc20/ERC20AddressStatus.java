package org.self.system.bridge.erc20;

import org.self.objects.self.SELFNumber;

public class ERC20AddressStatus {
    private boolean isValid;
    private int transactionCount;
    private SELFNumber totalAmount;
    private SELFNumber averageAmount;
    
    public ERC20AddressStatus() {
        this.isValid = false;
        this.transactionCount = 0;
        this.totalAmount = SELFNumber.ZERO;
        this.averageAmount = SELFNumber.ZERO;
    }
    
    public void setValid(boolean zIsValid) {
        this.isValid = zIsValid;
    }
    
    public boolean isValid() {
        return this.isValid;
    }
    
    public void incrementTransactionCount() {
        this.transactionCount++;
    }
    
    public int getTransactionCount() {
        return this.transactionCount;
    }
    
    public void addAmount(AICapacityNumber zAmount) {
        this.totalAmount = this.totalAmount.add(zAmount);
        this.averageAmount = this.totalAmount.divide(new AICapacityNumber(this.transactionCount));
    }
    
    public AICapacityNumber getTotalAmount() {
        return this.totalAmount;
    }
    
    public AICapacityNumber getAverageAmount() {
        return this.averageAmount;
    }
}
