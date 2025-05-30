package org.self.system.rewards;

import org.self.objects.ai.AICapacityNumber;

public class AlertThreshold {
    private double lowThreshold;
    private double highThreshold;
    private String description;
    private boolean enabled;
    
    public AlertThreshold(double zLowThreshold, double zHighThreshold) {
        this(zLowThreshold, zHighThreshold, "", true);
    }
    
    public AlertThreshold(double zLowThreshold, double zHighThreshold, String zDescription) {
        this(zLowThreshold, zHighThreshold, zDescription, true);
    }
    
    public AlertThreshold(double zLowThreshold, double zHighThreshold, String zDescription, boolean zEnabled) {
        lowThreshold = zLowThreshold;
        highThreshold = zHighThreshold;
        description = zDescription;
        enabled = zEnabled;
    }
    
    public double getLowThreshold() {
        return lowThreshold;
    }
    
    public double getHighThreshold() {
        return highThreshold;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setLowThreshold(double zLowThreshold) {
        lowThreshold = zLowThreshold;
    }
    
    public void setHighThreshold(double zHighThreshold) {
        highThreshold = zHighThreshold;
    }
    
    public void setDescription(String zDescription) {
        description = zDescription;
    }
    
    public void setEnabled(boolean zEnabled) {
        enabled = zEnabled;
    }
    
    public boolean isValueWithinThreshold(double zValue) {
        return enabled && zValue >= lowThreshold && zValue <= highThreshold;
    }
    
    @Override
    public String toString() {
        return String.format(
            "AlertThreshold[low=%.2f, high=%.2f, desc=%s, enabled=%b]",
            lowThreshold,
            highThreshold,
            description,
            enabled
        );
    }
}
