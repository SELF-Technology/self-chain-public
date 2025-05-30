package org.self.system.bridge.wire;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import org.self.utils.SelfLogger;

public class WireConnectionStatus {
    private static final int CHECK_INTERVAL = 5; // seconds
    private static final int MAX_RETRIES = 3;
    private static final int HEALTHY_THRESHOLD = 95; // percentage
    
    private final ScheduledExecutorService scheduler;
    private final ConcurrentMap<String, ConnectionStatus> connectionStatus;
    private final WireConnectionManager connectionManager;
    private final WireErrorHandler errorHandler;
    
    public WireConnectionStatus(WireConnectionManager connectionManager, WireErrorHandler errorHandler) {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.connectionStatus = new ConcurrentHashMap<>();
        this.connectionManager = connectionManager;
        this.errorHandler = errorHandler;
        
        // Start status monitoring
        startMonitoring();
    }
    
    /**
     * Start status monitoring
     */
    private void startMonitoring() {
        scheduler.scheduleAtFixedRate(this::checkStatus, 
            CHECK_INTERVAL, CHECK_INTERVAL, TimeUnit.SECONDS);
    }
    
    /**
     * Check all connection status
     */
    private void checkStatus() {
        for (Map.Entry<String, WireConnection> entry : connectionManager.getActiveConnections().entrySet()) {
            String nodeId = entry.getKey();
            WireConnection connection = entry.getValue();
            
            try {
                // Get current status
                ConnectionStatus status = connectionStatus.get(nodeId);
                if (status == null) {
                    status = new ConnectionStatus();
                    connectionStatus.put(nodeId, status);
                }
                
                // Update status
                updateConnectionStatus(nodeId, connection, status);
                
                // Check health
                checkConnectionHealth(nodeId, status);
                
            } catch (Exception e) {
                SelfLogger.error("Error checking connection status: " + e.getMessage());
                errorHandler.addError(new WireError(
                    WireErrorType.CONNECTION,
                    nodeId,
                    null,
                    null,
                    e.getMessage()
                ));
            }
        }
    }
    
    /**
     * Update connection status
     */
    private void updateConnectionStatus(String nodeId, WireConnection connection, ConnectionStatus status) {
        try {
            // Get connection stats
            JSONObject stats = connection.getConnectionStats();
            if (stats == null) {
                status.setConnected(false);
                status.setError("Failed to get connection stats");
                return;
            }
            
            // Update status
            status.setConnected(connection.isConnected());
            status.setBytesSent(stats.getLong("bytes_sent"));
            status.setBytesReceived(stats.getLong("bytes_received"));
            status.setTransactionsProcessed(stats.getLong("transactions_processed"));
            status.setErrors(stats.getLong("errors"));
            status.setLatency(stats.getLong("latency"));
            status.setLastUpdate(System.currentTimeMillis());
            
            // Calculate health
            long total = stats.getLong("transactions_processed") + stats.getLong("errors");
            if (total > 0) {
                double successRate = ((double) stats.getLong("transactions_processed") / total) * 100;
                status.setHealth((int) successRate);
            }
            
        } catch (Exception e) {
            status.setConnected(false);
            status.setError(e.getMessage());
        }
    }
    
    /**
     * Check connection health
     */
    private void checkConnectionHealth(String nodeId, ConnectionStatus status) {
        // Check if connection is healthy
        if (!status.isConnected() || status.getHealth() < HEALTHY_THRESHOLD) {
            // Add connection error
            errorHandler.addError(new WireError(
                WireErrorType.CONNECTION,
                nodeId,
                null,
                null,
                "Connection unhealthy: " + status.getError()
            ));
        }
    }
    
    /**
     * Get connection status
     */
    public ConnectionStatus getConnectionStatus(String nodeId) {
        return connectionStatus.get(nodeId);
    }
    
    /**
     * Get all connection statuses
     */
    public JSONObject getAllConnectionStatus() {
        JSONObject allStatus = new JSONObject();
        for (Map.Entry<String, ConnectionStatus> entry : connectionStatus.entrySet()) {
            allStatus.put(entry.getKey(), entry.getValue().toJson());
        }
        return allStatus;
    }
    
    /**
     * Shutdown status monitoring
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}
