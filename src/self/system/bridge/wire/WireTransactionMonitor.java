package org.self.system.bridge.wire;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONObject;
import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.utils.SelfLogger;

public class WireTransactionMonitor {
    private static final int CHECK_INTERVAL = 5; // seconds
    private static final int MAX_RETRIES = 3;
    private static final int CONFIRMATION_THRESHOLD = 12; // blocks
    
    private final ScheduledExecutorService scheduler;
    private final ConcurrentMap<String, WireTransaction> pendingTransactions;
    private final ConcurrentMap<String, WireTransaction> confirmedTransactions;
    private final AtomicInteger activeTransactions;
    private final AtomicLong totalTransactions;
    private final AtomicLong totalConfirmed;
    private final AtomicLong totalFailed;
    private final AtomicLong totalLatency;
    private final WireConnectionManager connectionManager;
    
    public WireTransactionMonitor(WireConnectionManager connectionManager) {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.pendingTransactions = new ConcurrentHashMap<>();
        this.confirmedTransactions = new ConcurrentHashMap<>();
        this.activeTransactions = new AtomicInteger(0);
        this.totalTransactions = new AtomicLong(0);
        this.totalConfirmed = new AtomicLong(0);
        this.totalFailed = new AtomicLong(0);
        this.totalLatency = new AtomicLong(0);
        this.connectionManager = connectionManager;
        
        // Start monitoring
        startMonitoring();
    }
    
    /**
     * Start monitoring transactions
     */
    private void startMonitoring() {
        scheduler.scheduleAtFixedRate(this::checkTransactions, 
            CHECK_INTERVAL, CHECK_INTERVAL, TimeUnit.SECONDS);
    }
    
    /**
     * Add transaction to monitoring
     */
    public void addTransaction(WireTransaction transaction) {
        pendingTransactions.put(transaction.getTxID().toString(), transaction);
        activeTransactions.incrementAndGet();
        totalTransactions.incrementAndGet();
    }
    
    /**
     * Check transaction status
     */
    private void checkTransactions() {
        for (Map.Entry<String, WireTransaction> entry : pendingTransactions.entrySet()) {
            WireTransaction transaction = entry.getValue();
            
            try {
                // Check transaction status
                if (checkTransactionStatus(transaction)) {
                    // Transaction confirmed
                    confirmedTransactions.put(transaction.getTxID().toString(), transaction);
                    pendingTransactions.remove(transaction.getTxID().toString());
                    activeTransactions.decrementAndGet();
                    totalConfirmed.incrementAndGet();
                    
                    // Update latency metrics
                    long latency = System.currentTimeMillis() - transaction.getCreationTime();
                    totalLatency.addAndGet(latency);
                }
            } catch (Exception e) {
                SelfLogger.error("Error checking transaction status: " + e.getMessage());
                totalFailed.incrementAndGet();
            }
        }
    }
    
    /**
     * Check single transaction status
     */
    private boolean checkTransactionStatus(WireTransaction transaction) throws Exception {
        int retries = 0;
        
        while (retries < MAX_RETRIES) {
            try {
                // Get transaction status
                JSONObject status = connectionManager.getConnectionStats();
                if (status == null) {
                    throw new Exception("Failed to get transaction status");
                }
                
                // Check confirmations
                if (status.has("confirmations") && 
                    status.getInt("confirmations") >= CONFIRMATION_THRESHOLD) {
                    return true;
                }
                
                // Check block number
                if (status.has("block_num") && 
                    status.getInt("block_num") > 0) {
                    return true;
                }
                
                // Check status
                if (status.has("status") && 
                    status.getString("status").equals("confirmed")) {
                    return true;
                }
                
                // Check error
                if (status.has("error") && 
                    status.getString("error") != null) {
                    throw new Exception(status.getString("error"));
                }
                
                return false;
                
            } catch (Exception e) {
                retries++;
                if (retries >= MAX_RETRIES) {
                    throw e;
                }
                Thread.sleep(1000); // Wait 1 second between retries
            }
        }
        return false;
    }
    
    /**
     * Get transaction statistics
     */
    public JSONObject getTransactionStats() {
        JSONObject stats = new JSONObject();
        stats.put("active_transactions", activeTransactions.get());
        stats.put("total_transactions", totalTransactions.get());
        stats.put("total_confirmed", totalConfirmed.get());
        stats.put("total_failed", totalFailed.get());
        
        // Calculate average latency
        long total = totalTransactions.get();
        if (total > 0) {
            stats.put("average_latency", totalLatency.get() / total);
        }
        
        return stats;
    }
    
    /**
     * Get transaction details
     */
    public WireTransaction getTransaction(MiniData zTxID) {
        String txID = zTxID.toString();
        WireTransaction tx = confirmedTransactions.get(txID);
        if (tx == null) {
            tx = pendingTransactions.get(txID);
        }
        return tx;
    }
    
    /**
     * Shutdown monitoring
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
