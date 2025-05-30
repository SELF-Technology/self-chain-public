package org.self.system.bridge.wire;

import java.util.Date;
import org.json.JSONObject;
import org.self.utils.SelfLogger;

public class ConnectionStatus {
    private boolean connected;
    private long bytesSent;
    private long bytesReceived;
    private long transactionsProcessed;
    private long errors;
    private long latency;
    private int health;
    private String error;
    private Date lastUpdate;
    
    public ConnectionStatus() {
        this.connected = false;
        this.bytesSent = 0;
        this.bytesReceived = 0;
        this.transactionsProcessed = 0;
        this.errors = 0;
        this.latency = 0;
        this.health = 0;
        this.error = null;
        this.lastUpdate = new Date();
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    public long getBytesSent() {
        return bytesSent;
    }
    
    public void setBytesSent(long bytesSent) {
        this.bytesSent = bytesSent;
    }
    
    public long getBytesReceived() {
        return bytesReceived;
    }
    
    public void setBytesReceived(long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }
    
    public long getTransactionsProcessed() {
        return transactionsProcessed;
    }
    
    public void setTransactionsProcessed(long transactionsProcessed) {
        this.transactionsProcessed = transactionsProcessed;
    }
    
    public long getErrors() {
        return errors;
    }
    
    public void setErrors(long errors) {
        this.errors = errors;
    }
    
    public long getLatency() {
        return latency;
    }
    
    public void setLatency(long latency) {
        this.latency = latency;
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public Date getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("connected", connected);
        json.put("bytes_sent", bytesSent);
        json.put("bytes_received", bytesReceived);
        json.put("transactions_processed", transactionsProcessed);
        json.put("errors", errors);
        json.put("latency", latency);
        json.put("health", health);
        json.put("error", error);
        json.put("last_update", lastUpdate.getTime());
        return json;
    }
}
