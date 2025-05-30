package org.self.system.bridge.wire;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import org.self.utils.SelfLogger;

public class WireConnection {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_TIMEOUT = 5000; // 5 seconds
    
    private final WireNode node;
    private final OkHttpClient client;
    private final AtomicBoolean isConnected;
    private final AtomicLong bytesSent;
    private final AtomicLong bytesReceived;
    private long lastConnectionTime;
    private int currentTimeout;
    
    public WireConnection(WireNode node) {
        this.node = node;
        this.client = new OkHttpClient();
        this.isConnected = new AtomicBoolean(false);
        this.bytesSent = new AtomicLong(0);
        this.bytesReceived = new AtomicLong(0);
        this.lastConnectionTime = System.currentTimeMillis();
        this.currentTimeout = INITIAL_TIMEOUT;
    }
    
    /**
     * Initialize connection to Wire Network node
     */
    public boolean initialize() {
        try {
            // Check node status
            JSONObject status = getNodeStatus();
            if (status == null || !status.has("status") || !status.getString("status").equals("active")) {
                return false;
            }
            
            // Check peers
            JSONObject peers = getPeers();
            if (peers == null || !peers.has("active_peers") || peers.getJSONArray("active_peers").length() == 0) {
                return false;
            }
            
            isConnected.set(true);
            lastConnectionTime = System.currentTimeMillis();
            currentTimeout = INITIAL_TIMEOUT;
            return true;
            
        } catch (Exception e) {
            SelfLogger.error("Error initializing Wire Network connection: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send transaction to Wire Network
     */
    public JSONObject sendTransaction(JSONObject transaction) {
        try {
            // Create request
            Request request = new Request.Builder()
                .url(node.getEndpoint() + "/v1/chain/push_transaction")
                .post(RequestBody.create(JSON, transaction.toString()))
                .build();
            
            // Execute request
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response.code());
                }
                
                // Update metrics
                bytesSent.addAndGet(transaction.toString().getBytes().length);
                bytesReceived.addAndGet(response.body().string().getBytes().length);
                
                return new JSONObject(response.body().string());
            }
            
        } catch (Exception e) {
            // Handle connection errors
            if (isConnected.get()) {
                isConnected.set(false);
                lastConnectionTime = System.currentTimeMillis();
                
                // Increase timeout exponentially
                currentTimeout = Math.min(currentTimeout * 2, MAX_RETRIES * INITIAL_TIMEOUT);
            }
            
            SelfLogger.error("Error sending transaction to Wire Network: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get node status
     */
    public JSONObject getNodeStatus() {
        try {
            Request request = new Request.Builder()
                .url(node.getEndpoint() + "/v1/chain/get_info")
                .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response.code());
                }
                return new JSONObject(response.body().string());
            }
            
        } catch (Exception e) {
            SelfLogger.error("Error getting Wire Network node status: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get network peers
     */
    public JSONObject getPeers() {
        try {
            Request request = new Request.Builder()
                .url(node.getEndpoint() + "/v1/net/get_info")
                .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response.code());
                }
                return new JSONObject(response.body().string());
            }
            
        } catch (Exception e) {
            SelfLogger.error("Error getting Wire Network peers: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if connection is active
     */
    public boolean isConnected() {
        // Check if connection has timed out
        if (System.currentTimeMillis() - lastConnectionTime > currentTimeout) {
            return false;
        }
        return isConnected.get();
    }
    
    /**
     * Get bytes sent
     */
    public long getBytesSent() {
        return bytesSent.get();
    }
    
    /**
     * Get bytes received
     */
    public long getBytesReceived() {
        return bytesReceived.get();
    }
    
    /**
     * Close connection
     */
    public void close() {
        isConnected.set(false);
        client.dispatcher().executorService().shutdown();
    }
}
