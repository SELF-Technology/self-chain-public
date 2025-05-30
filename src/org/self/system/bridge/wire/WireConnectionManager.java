package org.self.system.bridge.wire;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.utils.SelfLogger;
import org.json.JSONObject;

public class WireConnectionManager {
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_CONNECTION_TIMEOUT = 5000; // 5 seconds
    private static final int MAX_CONNECTION_TIMEOUT = 30000; // 30 seconds
    
    private final ConcurrentHashMap<String, WireConnection> activeConnections;
    private final ConcurrentHashMap<String, WireNode> activeNodes;
    private final AtomicInteger activeConnectionsCount;
    private final AtomicLong totalErrors;
    private final AtomicLong totalTransactions;
    private final AtomicLong totalBytesSent;
    private final AtomicLong totalBytesReceived;
    
    public WireConnectionManager() {
        this.activeConnections = new ConcurrentHashMap<>();
        this.activeNodes = new ConcurrentHashMap<>();
        this.activeConnectionsCount = new AtomicInteger(0);
        this.totalErrors = new AtomicLong(0);
        this.totalTransactions = new AtomicLong(0);
        this.totalBytesSent = new AtomicLong(0);
        this.totalBytesReceived = new AtomicLong(0);
    }
    
    /**
     * Initialize connection to Wire Network node
     */
    public boolean initializeConnection(WireNode node) {
        try {
            // Check if connection already exists
            if (activeConnections.containsKey(node.getNodeId())) {
                return true;
            }
            
            // Create new connection
            WireConnection connection = new WireConnection(node);
            
            // Add to active connections
            activeConnections.put(node.getNodeId(), connection);
            activeNodes.put(node.getNodeId(), node);
            
            // Increment active connections count
            activeConnectionsCount.incrementAndGet();
            
            // Initialize connection
            if (connection.initialize()) {
                SelfLogger.info("Successfully connected to Wire Network node: " + node.getNodeId());
                return true;
            }
            
            // Remove failed connection
            activeConnections.remove(node.getNodeId());
            activeNodes.remove(node.getNodeId());
            activeConnectionsCount.decrementAndGet();
            
            return false;
            
        } catch (Exception e) {
            SelfLogger.error("Error initializing Wire Network connection: " + e.getMessage());
            totalErrors.incrementAndGet();
            return false;
        }
    }
    
    /**
     * Process Wire Network transaction
     */
    public boolean processTransaction(MiniData zTxID, MiniNumber zAmount, String zDestination) {
        try {
            // Get main node connection
            WireNode mainNode = activeNodes.get("main_wire_node");
            if (mainNode == null) {
                SelfLogger.error("Main Wire Network node not found");
                return false;
            }
            
            WireConnection connection = activeConnections.get(mainNode.getNodeId());
            if (connection == null || !connection.isConnected()) {
                // Try to reconnect
                if (!initializeConnection(mainNode)) {
                    SelfLogger.error("Failed to reconnect to main Wire Network node");
                    return false;
                }
                connection = activeConnections.get(mainNode.getNodeId());
            }
            
            // Create transaction data
            JSONObject transaction = new JSONObject();
            transaction.put("tx_id", zTxID.toString());
            transaction.put("amount", zAmount.toString());
            transaction.put("destination", zDestination);
            
            // Send transaction
            long bytesSent = transaction.toString().getBytes().length;
            totalBytesSent.addAndGet(bytesSent);
            
            JSONObject response = connection.sendTransaction(transaction);
            
            if (response != null && response.has("status") && response.getString("status").equals("success")) {
                totalTransactions.incrementAndGet();
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            SelfLogger.error("Error processing Wire Network transaction: " + e.getMessage());
            totalErrors.incrementAndGet();
            return false;
        }
    }
    
    /**
     * Monitor connection status
     */
    public void monitorConnections() {
        for (Map.Entry<String, WireConnection> entry : activeConnections.entrySet()) {
            WireConnection connection = entry.getValue();
            
            // Check connection status
            if (!connection.isConnected()) {
                // Attempt to reconnect
                WireNode node = activeNodes.get(entry.getKey());
                if (node != null) {
                    initializeConnection(node);
                }
            }
            
            // Update metrics
            totalBytesReceived.addAndGet(connection.getBytesReceived());
            totalBytesSent.addAndGet(connection.getBytesSent());
        }
    }
    
    /**
     * Get connection statistics
     */
    public JSONObject getConnectionStats() {
        JSONObject stats = new JSONObject();
        stats.put("active_connections", activeConnectionsCount.get());
        stats.put("total_transactions", totalTransactions.get());
        stats.put("total_errors", totalErrors.get());
        stats.put("total_bytes_sent", totalBytesSent.get());
        stats.put("total_bytes_received", totalBytesReceived.get());
        return stats;
    }
    
    /**
     * Close all connections
     */
    public void closeConnections() {
        for (WireConnection connection : activeConnections.values()) {
            connection.close();
        }
        activeConnections.clear();
        activeNodes.clear();
        activeConnectionsCount.set(0);
    }
}
