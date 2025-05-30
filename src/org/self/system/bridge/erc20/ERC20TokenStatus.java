package org.self.system.bridge.erc20;

import org.self.objects.self.SELFNumber;

public class ERC20TokenStatus {
    private boolean isOnline;
    private int uptime;
    private SELFNumber totalVolume;
    private SELFNumber averageVolume;
    
    public ERC20TokenStatus() {
        this.isOnline = false;
        this.uptime = 0;
        this.totalVolume = SELFNumber.ZERO;
        this.averageVolume = SELFNumber.ZERO;
    }
    
    public void setOnline(boolean zIsOnline) {
        this.isOnline = zIsOnline;
    }
    
    public boolean isOnline() {
        return this.isOnline;
    }
    
    public void incrementUptime(boolean zIsOnline) {
        if (zIsOnline) {
            this.uptime++;
        }
    }
    
    public int getUptime() {
        return this.uptime;
    }
    
    public void addVolume(AICapacityNumber zVolume) {
        this.totalVolume = this.totalVolume.add(zVolume);
        this.averageVolume = this.totalVolume.divide(new AICapacityNumber(this.uptime));
    }
    
    public AICapacityNumber getTotalVolume() {
        return this.totalVolume;
    }
    
    public AICapacityNumber getAverageVolume() {
        return this.averageVolume;
    }
}
