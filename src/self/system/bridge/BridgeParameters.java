package org.self.system.bridge;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.self.SELFNumber;
import org.self.system.params.SELFParams;

public class BridgeParameters {
    private String bridgeType;
    private SELFNumber feeRate;
    private SELFNumber minAmount;
    private SELFNumber maxAmount;
    private Map<String, Object> parameters;
    
    public BridgeParameters() {
        parameters = new HashMap<>();
        feeRate = new SELFNumber(0.01); // Default 1% fee
        minAmount = SELFParams.SELF_MIN_REWARD;
        maxAmount = SELFParams.SELF_MAX_REWARD;
    }
    
    public void setBridgeType(String zType) {
        bridgeType = zType.toLowerCase();
    }
    
    public String getBridgeType() {
        return bridgeType;
    }
    
    public void setFeeRate(SELFNumber zRate) {
        feeRate = zRate;
    }
    
    public SELFNumber getFeeRate() {
        return feeRate;
    }
    
    public void setMinAmount(SELFNumber zAmount) {
        minAmount = zAmount;
    }
    
    public SELFNumber getMinAmount() {
        return minAmount;
    }
    
    public void setMaxAmount(SELFNumber zAmount) {
        maxAmount = zAmount;
    }
    
    public SELFNumber getMaxAmount() {
        return maxAmount;
    }
    
    public void setParameters(Map<String, Object> zParams) {
        parameters.putAll(zParams);
    }
    
    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }
    
    public Object getParameter(String zKey) {
        return parameters.get(zKey);
    }
    
    public void addParameter(String zKey, Object zValue) {
        parameters.put(zKey, zValue);
    }
    
    public void removeParameter(String zKey) {
        parameters.remove(zKey);
    }
}
