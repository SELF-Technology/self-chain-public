package org.self.system.bridge.wire;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.utils.SelfLogger;

public class WireErrorHandler {
    private static final int CHECK_INTERVAL = 10; // seconds
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY = 1000; // milliseconds
    
    private final ScheduledExecutorService scheduler;
    private final ConcurrentMap<String, WireError> activeErrors;
    private final ConcurrentMap<String, WireError> resolvedErrors;
    private final AtomicInteger activeErrorsCount;
    private final AtomicLong totalErrors;
    private final AtomicLong totalResolved;
    private final AtomicLong totalRetries;
    private final WireConnectionManager connectionManager;
    private final WireTransactionMonitor transactionMonitor;
    
    public WireErrorHandler(WireConnectionManager connectionManager, WireTransactionMonitor transactionMonitor) {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.activeErrors = new ConcurrentHashMap<>();
        this.resolvedErrors = new ConcurrentHashMap<>();
        this.activeErrorsCount = new AtomicInteger(0);
        this.totalErrors = new AtomicLong(0);
        this.totalResolved = new AtomicLong(0);
        this.totalRetries = new AtomicLong(0);
        this.connectionManager = connectionManager;
        this.transactionMonitor = transactionMonitor;
        
        // Start error handling
        startErrorHandling();
    }
    
    /**
     * Start error handling
     */
    private void startErrorHandling() {
        scheduler.scheduleAtFixedRate(this::handleErrors, 
            CHECK_INTERVAL, CHECK_INTERVAL, TimeUnit.SECONDS);
    }
    
    /**
     * Add error for handling
     */
    public void addError(WireError error) {
        activeErrors.put(error.getErrorId(), error);
        activeErrorsCount.incrementAndGet();
        totalErrors.incrementAndGet();
    }
    
    /**
     * Handle all active errors
     */
    private void handleErrors() {
        for (Map.Entry<String, WireError> entry : activeErrors.entrySet()) {
            WireError error = entry.getValue();
            
            try {
                // Attempt to resolve error
                if (resolveError(error)) {
                    // Error resolved
                    resolvedErrors.put(error.getErrorId(), error);
                    activeErrors.remove(error.getErrorId());
                    activeErrorsCount.decrementAndGet();
                    totalResolved.incrementAndGet();
                }
            } catch (Exception e) {
                SelfLogger.error("Error handling Wire Network error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Resolve single error
     */
    private boolean resolveError(WireError error) throws Exception {
        int retries = 0;
        
        while (retries < MAX_RETRIES) {
            try {
                // Handle connection errors
                if (error.getType() == WireErrorType.CONNECTION) {
                    return handleConnectionError(error);
                }
                
                // Handle transaction errors
                if (error.getType() == WireErrorType.TRANSACTION) {
                    return handleTransactionError(error);
                }
                
                // Handle validation errors
                if (error.getType() == WireErrorType.VALIDATION) {
                    return handleValidationError(error);
                }
                
                return false;
                
            } catch (Exception e) {
                retries++;
                totalRetries.incrementAndGet();
                
                if (retries >= MAX_RETRIES) {
                    throw e;
                }
                Thread.sleep(RETRY_DELAY);
            }
        }
        return false;
    }
    
    /**
     * Handle connection error
     */
    private boolean handleConnectionError(WireError error) throws Exception {
        // Get node ID from error
        String nodeId = error.getNodeId();
        WireNode node = connectionManager.getActiveNodes().get(nodeId);
        
        if (node == null) {
            throw new Exception("Node not found: " + nodeId);
        }
        
        // Try to reconnect
        if (connectionManager.initializeConnection(node)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle transaction error
     */
    private boolean handleTransactionError(WireError error) throws Exception {
        // Get transaction ID from error
        String txId = error.getTransactionId();
        WireTransaction tx = transactionMonitor.getTransaction(MiniData.fromBytes(txId.getBytes()));
        
        if (tx == null) {
            throw new Exception("Transaction not found: " + txId);
        }
        
        // Check transaction status
        if (transactionMonitor.checkTransactionStatus(tx)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle validation error
     */
    private boolean handleValidationError(WireError error) throws Exception {
        // Get address from error
        String address = error.getAddress();
        
        // Validate address
        if (WireNetworkService.getInstance().isValidWireAddress(address)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Get error statistics
     */
    public JSONObject getErrorStats() {
        JSONObject stats = new JSONObject();
        stats.put("active_errors", activeErrorsCount.get());
        stats.put("total_errors", totalErrors.get());
        stats.put("total_resolved", totalResolved.get());
        stats.put("total_retries", totalRetries.get());
        
        // Calculate resolution rate
        long total = totalErrors.get();
        if (total > 0) {
            double resolutionRate = ((double) totalResolved.get() / total) * 100;
            stats.put("resolution_rate", resolutionRate);
        }
        
        return stats;
    }
    
    /**
     * Get error details
     */
    public WireError getError(String errorId) {
        WireError error = resolvedErrors.get(errorId);
        if (error == null) {
            error = activeErrors.get(errorId);
        }
        return error;
    }
    
    /**
     * Shutdown error handling
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
