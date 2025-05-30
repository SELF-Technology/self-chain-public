package org.self.system.bridge.wire;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.system.bridge.BridgeService;
import org.self.system.bridge.BridgeParameters;
import org.self.system.bridge.BridgeStatus;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;
import org.self.system.bridge.wire.WireRPCClient;
import org.self.system.bridge.wire.WireSecurityManager;
import org.self.system.bridge.wire.WireConnectionManager;

public class WireNetworkService {
    private static WireNetworkService instance;
    private Map<String, WireNode> nodes;
    private Map<String, WireConnection> connections;
    private Map<String, WireTransaction> pendingTransactions;
    private BridgeService bridgeService;
    private WireNetworkMetrics metrics;
    private WireConnectionManager connectionManager;
    private WireTransactionMonitor transactionMonitor;
    private WireErrorHandler errorHandler;
    private WireConnectionStatus connectionStatus;
    
    private WireNetworkService() {
        nodes = new ConcurrentHashMap<>();
        connections = new ConcurrentHashMap<>();
        pendingTransactions = new ConcurrentHashMap<>();
        bridgeService = BridgeService.getInstance();
        metrics = new WireNetworkMetrics();
        
        // Initialize connection manager
        connectionManager = new WireConnectionManager();
        
        // Initialize transaction monitor
        transactionMonitor = new WireTransactionMonitor(connectionManager);
        
        // Initialize error handler
        errorHandler = new WireErrorHandler(connectionManager, transactionMonitor);
        
        // Initialize connection status
        connectionStatus = new WireConnectionStatus(connectionManager, errorHandler);
        
        // Initialize connections
        WireNode mainNode = nodes.get("main_wire_node");
        if (mainNode != null) {
            connectionManager.initializeConnection(mainNode);
        }
        
        WireNode backupNode = nodes.get("backup_wire_node");
        if (backupNode != null) {
            connectionManager.initializeConnection(backupNode);
        }
        
        // Initialize security manager
        WireSecurityManager securityManager = new WireSecurityManager();
        
        // Initialize default nodes
        initializeDefaultNodes();
    }
    
    public static WireNetworkService getInstance() {
        if (instance == null) {
            instance = new WireNetworkService();
        }
        return instance;
    }
    
    private void initializeDefaultNodes() {
        // Initialize main Wire Network node
        WireNode mainNode = new WireNode();
        mainNode.setNodeID("main_wire_node");
        mainNode.setEndpoint("https://wire.self.network");
        mainNode.setNodeType("main");
        mainNode.setBandwidthLimit(SELFParams.WIRE_BANDWIDTH_LIMIT);
        mainNode.setLatencyLimit(SELFParams.WIRE_LATENCY_LIMIT);
        
        nodes.put(mainNode.getNodeID(), mainNode);
        
        // Initialize backup Wire Network node
        WireNode backupNode = new WireNode();
        backupNode.setNodeID("backup_wire_node");
        backupNode.setEndpoint("https://backup.wire.self.network");
        backupNode.setNodeType("backup");
        backupNode.setBandwidthLimit(SELFParams.WIRE_BANDWIDTH_LIMIT);
        backupNode.setLatencyLimit(SELFParams.WIRE_LATENCY_LIMIT);
        
        nodes.put(backupNode.getNodeID(), backupNode);
    }
    
    public boolean createWireTransaction(MiniData zTxID, MiniNumber zAmount, String zDestination) {
        // Check if destination is valid
        if (!isValidWireAddress(zDestination)) {
            // Add validation error
            errorHandler.addError(new WireError(
                WireErrorType.VALIDATION,
                "main_wire_node",
                zTxID.toString(),
                zDestination,
                "Invalid Wire address format"
            ));
            return false;
        }
        
        // Create new transaction
        WireTransaction tx = new WireTransaction(zTxID, zAmount, zDestination);
        pendingTransactions.put(zTxID.toString(), tx);
        
        try {
            // Process transaction through connection manager
            if (connectionManager.processTransaction(zTxID, zAmount, zDestination)) {
                // Add to transaction monitor
                transactionMonitor.addTransaction(tx);
                
                // Process through bridge service
                if (bridgeService.processBridgeTransaction("wire", zTxID, zAmount)) {
                    // Update metrics
                    metrics.incrementTotalTransactions();
                    metrics.addTotalAmount(zAmount);
                    return true;
                }
            }
        } catch (Exception e) {
            SelfLogger.error("Error creating Wire transaction: " + e.getMessage());
            tx.setStatus("error");
            tx.setError(e.getMessage());
            
            // Add transaction error
            errorHandler.addError(new WireError(
                WireErrorType.TRANSACTION,
                "main_wire_node",
                zTxID.toString(),
                zDestination,
                e.getMessage()
            ));
        }
        
        return false;
    }
    
    public boolean confirmWireTransaction(MiniData zTxID) {
        WireTransaction tx = pendingTransactions.get(zTxID.toString());
        if (tx == null) {
            return false;
        }
        
        try {
            // Get main node's RPC client
            WireNode mainNode = nodes.get("main_wire_node");
            WireRPCClient rpcClient = mainNode.getRpcClient();
            
            // Get transaction status
            JSONObject response = rpcClient.getTransactionStatus(zTxID);
            
            // Check if transaction is confirmed
            if (response.has("processed") && response.getJSONObject("processed").has("status") && 
                response.getJSONObject("processed").getString("status").equals("executed")) {
                tx.setConfirmed(true);
                tx.setStatus("confirmed");
                
                // Update metrics
                metrics.incrementSuccessfulTransactions();
                return true;
            }
        } catch (IOException e) {
            SelfLogger.error("Error confirming Wire transaction: " + e.getMessage());
            tx.setStatus("error");
            tx.setError(e.getMessage());
        }
        
        return false;
    }
    
    public void addWireNode(WireNode zNode) {
        nodes.put(zNode.getNodeID(), zNode);
    }
    
    public void removeWireNode(String zNodeID) {
        nodes.remove(zNodeID);
    }
    
    public void addWireConnection(WireConnection zConnection) {
        connections.put(zConnection.getConnectionID(), zConnection);
    }
    
    public void removeWireConnection(String zConnectionID) {
        connections.remove(zConnectionID);
    }
    
    public Map<String, WireNode> getNodes() {
        return new HashMap<>(nodes);
    }
    
    public Map<String, WireConnection> getConnections() {
        return new HashMap<>(connections);
    }
    
    public Map<String, WireTransaction> getPendingTransactions() {
        return new HashMap<>(pendingTransactions);
    }
    
    public WireNetworkMetrics getMetrics() {
        // Get connection metrics
        JSONObject connectionStats = connectionManager.getConnectionStats();
        
        // Get connection status
        JSONObject connectionStatus = this.connectionStatus.getAllConnectionStatus();
        
        // Get transaction metrics
        JSONObject transactionStats = transactionMonitor.getTransactionStats();
        
        // Get error metrics
        JSONObject errorStats = errorHandler.getErrorStats();
        
        // Update metrics with all stats
        metrics.setConnectionStats(connectionStats);
        metrics.setConnectionStatus(connectionStatus);
        metrics.setTransactionStats(transactionStats);
        metrics.setErrorStats(errorStats);
        
        return metrics;
    }
    
    private WireSecurityManager securityManager;
    
    private boolean isValidWireAddress(String zAddress) {
        try {
            // First validate address format
            if (!zAddress.startsWith("wire_")) {
                return false;
            }
            
            // Get main node's RPC client
            WireNode mainNode = nodes.get("main_wire_node");
            WireRPCClient rpcClient = mainNode.getRpcClient();
            
            // Validate address with Wire Network
            return rpcClient.validateAddress(zAddress);
        } catch (IOException e) {
            SelfLogger.error("Error validating Wire address: " + e.getMessage());
            return false;
        }
    }
    
    public void updateNodeStatus(String zNodeID, boolean zIsOnline) {
        WireNode node = nodes.get(zNodeID);
        if (node != null) {
            node.setOnlineStatus(zIsOnline);
            metrics.updateNodeStatus(zNodeID, zIsOnline);
        }
    }
    
    public void updateConnectionStatus(String zConnectionID, boolean zIsActive) {
        WireConnection connection = connections.get(zConnectionID);
        if (connection != null) {
            connection.setActiveStatus(zIsActive);
            metrics.updateConnectionStatus(zConnectionID, zIsActive);
        }
    }
}
