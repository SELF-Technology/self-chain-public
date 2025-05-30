package org.self.system.bridge.wire;

import java.util.Date;
import org.json.JSONObject;
import org.self.objects.MiniData;
import org.self.utils.SelfLogger;

public class WireError {
    private final String errorId;
    private final WireErrorType type;
    private final String nodeId;
    private final String transactionId;
    private final String address;
    private final String message;
    private final Date timestamp;
    private final int retryCount;
    private final boolean resolved;
    
    public WireError(WireErrorType type, String nodeId, String transactionId, String address, String message) {
        this.errorId = generateErrorId();
        this.type = type;
        this.nodeId = nodeId;
        this.transactionId = transactionId;
        this.address = address;
        this.message = message;
        this.timestamp = new Date();
        this.retryCount = 0;
        this.resolved = false;
        
        // Log error
        SelfLogger.error("Wire Network Error: " + message);
    }
    
    private String generateErrorId() {
        return "ERROR_" + System.currentTimeMillis() + "_" + Math.abs(hashCode());
    }
    
    public String getErrorId() {
        return errorId;
    }
    
    public WireErrorType getType() {
        return type;
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public boolean isResolved() {
        return resolved;
    }
    
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("error_id", errorId);
        json.put("type", type.name());
        json.put("node_id", nodeId);
        json.put("transaction_id", transactionId);
        json.put("address", address);
        json.put("message", message);
        json.put("timestamp", timestamp.getTime());
        json.put("retry_count", retryCount);
        json.put("resolved", resolved);
        return json;
    }
}
