package org.self.system.bridge.rosetta.coinbase;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.MiniNumber;
import org.self.system.bridge.BridgeParameters;
import org.self.system.params.SELFParams;

public class CoinbaseConfig {
    private static CoinbaseConfig instance;
    private Map<String, Object> config;
    private BridgeParameters bridgeParams;
    
    private CoinbaseConfig() {
        config = new HashMap<>();
        initializeDefaultConfig();
    }
    
    public static CoinbaseConfig getInstance() {
        if (instance == null) {
            instance = new CoinbaseConfig();
        }
        return instance;
    }
    
    private void initializeDefaultConfig() {
        // Coinbase-specific configuration
        config.put("api_version", "2024-01-01");
        config.put("network_id", "self_mainnet");
        config.put("blockchain", "SELF");
        config.put("network", "mainnet");
        
        // Initialize bridge parameters
        bridgeParams = new BridgeParameters();
        bridgeParams.setBridgeType("COINBASE");
        bridgeParams.setFeeRate(new MiniNumber(0.01)); // 1% fee
        bridgeParams.setMinAmount(SELFParams.SELF_MIN_REWARD);
        bridgeParams.setMaxAmount(SELFParams.SELF_MAX_REWARD);
        
        // Coinbase-specific limits
        config.put("max_transaction_size", 1000000L);
        config.put("max_block_size", 2000000L);
        config.put("block_time_seconds", 15);
        
        // Coinbase-specific features
        config.put("supports_mempool", true);
        config.put("supports_delegation", true);
        config.put("supports_staking", true);
    }
    
    public Map<String, Object> getConfig() {
        return new HashMap<>(config);
    }
    
    public BridgeParameters getBridgeParameters() {
        return bridgeParams;
    }
    
    public void updateConfig(Map<String, Object> zUpdates) {
        config.putAll(zUpdates);
    }
    
    public void updateBridgeParameters(BridgeParameters zParams) {
        bridgeParams = zParams;
    }
    
    public boolean isFeatureSupported(String zFeature) {
        return Boolean.TRUE.equals(config.get(zFeature));
    }
    
    public long getMaxTransactionSize() {
        return (long) config.get("max_transaction_size");
    }
    
    public long getMaxBlockSize() {
        return (long) config.get("max_block_size");
    }
    
    public int getBlockTimeSeconds() {
        return (int) config.get("block_time_seconds");
    }
}
