package org.self.system.bridge.rosetta;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.system.bridge.BridgeService;
import org.self.system.bridge.BridgeParameters;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class RosettaService {
    private static RosettaService instance;
    private Map<String, RosettaNetwork> networks;
    private BridgeService bridgeService;
    
    private RosettaService() {
        networks = new HashMap<>();
        bridgeService = BridgeService.getInstance();
        initializeNetworks();
    }
    
    public static RosettaService getInstance() {
        if (instance == null) {
            instance = new RosettaService();
        }
        return instance;
    }
    
    private void initializeNetworks() {
        // Initialize ERC-20 network
        RosettaNetwork erc20 = new RosettaNetwork();
        erc20.setNetworkType("ERC20");
        erc20.setNetworkID("erc20_network");
        erc20.setEndpoint("https://api.erc20.network");
        
        // Set bridge parameters
        BridgeParameters params = new BridgeParameters();
        params.setBridgeType("ERC20");
        params.setFeeRate(new MiniNumber(0.01));
        params.setMinAmount(SELFParams.SELF_MIN_REWARD);
        params.setMaxAmount(SELFParams.SELF_MAX_REWARD);
        
        erc20.setBridgeParameters(params);
        networks.put("erc20", erc20);
        
        // Initialize SELF network
        RosettaNetwork self = new RosettaNetwork();
        self.setNetworkType("SELF");
        self.setNetworkID("self_network");
        self.setEndpoint("https://api.self.network");
        
        // Set bridge parameters
        params = new BridgeParameters();
        params.setBridgeType("SELF");
        params.setFeeRate(new MiniNumber(0.005));
        params.setMinAmount(SELFParams.SELF_MIN_REWARD);
        params.setMaxAmount(SELFParams.SELF_MAX_REWARD);
        
        self.setBridgeParameters(params);
        networks.put("self", self);
    }
    
    public boolean createRosettaTransaction(String zNetworkType, MiniData zTxID, MiniNumber zAmount) {
        RosettaNetwork network = networks.get(zNetworkType.toLowerCase());
        if (network == null) {
            return false;
        }
        
        // Process bridge transaction
        return bridgeService.processBridgeTransaction(
            network.getBridgeParameters().getBridgeType(),
            zTxID,
            zAmount
        );
    }
    
    public Map<String, Object> getNetworkInfo(String zNetworkType) {
        RosettaNetwork network = networks.get(zNetworkType.toLowerCase());
        if (network == null) {
            return null;
        }
        
        return network.getNetworkInfo();
    }
    
    public Map<String, Object> getAccountInfo(String zNetworkType, String zAddress) {
        RosettaNetwork network = networks.get(zNetworkType.toLowerCase());
        if (network == null) {
            return null;
        }
        
        return network.getAccountInfo(zAddress);
    }
    
    public Map<String, Object> getTransactionInfo(String zNetworkType, String zTxID) {
        RosettaNetwork network = networks.get(zNetworkType.toLowerCase());
        if (network == null) {
            return null;
        }
        
        return network.getTransactionInfo(zTxID);
    }
    
    public Map<String, RosettaNetwork> getNetworks() {
        return new HashMap<>(networks);
    }
    
    public void addNetwork(RosettaNetwork zNetwork) {
        networks.put(zNetwork.getNetworkType(), zNetwork);
    }
    
    public void removeNetwork(String zNetworkType) {
        networks.remove(zNetworkType.toLowerCase());
    }
    
    public void updateNetworkParameters(String zNetworkType, Map<String, Object> zParams) {
        RosettaNetwork network = networks.get(zNetworkType.toLowerCase());
        if (network != null) {
            network.updateParameters(zParams);
        }
    }
}
