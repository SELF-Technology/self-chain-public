package org.self.system.bridge.erc20;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.system.bridge.BridgeService;
import org.self.system.bridge.BridgeParameters;
import org.self.system.governance.ai.AIValidator;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class ERC20Service {
    private static ERC20Service instance;
    private Map<String, ERC20Token> tokens;
    private Map<String, ERC20Address> addresses;
    private Map<String, ERC20Transaction> pendingTransactions;
    private BridgeService bridgeService;
    private ERC20Metrics metrics;
    
    private ERC20Service() {
        tokens = new HashMap<>();
        addresses = new HashMap<>();
        pendingTransactions = new HashMap<>();
        bridgeService = BridgeService.getInstance();
        metrics = new ERC20Metrics();
        initializeDefaultTokens();
    }
    
    public static ERC20Service getInstance() {
        if (instance == null) {
            instance = new ERC20Service();
        }
        return instance;
    }
    
    private void initializeDefaultTokens() {
        // Initialize SELF token
        ERC20Token selfToken = new ERC20Token();
        selfToken.setTokenID("SELF");
        selfToken.setContractAddress("0xSELF_TOKEN");
        selfToken.setDecimals(18);
        selfToken.setTotalSupply(SELFParams.SELF_TOTAL_SUPPLY);
        
        tokens.put(selfToken.getTokenID(), selfToken);
        
        // Initialize bridge parameters
        BridgeParameters params = new BridgeParameters();
        params.setBridgeType("ERC20");
        params.setFeeRate(new MiniNumber(0.01));
        params.setMinAmount(SELFParams.SELF_MIN_REWARD);
        params.setMaxAmount(SELFParams.SELF_MAX_REWARD);
        
        selfToken.setBridgeParameters(params);
    }
    
    public boolean createERC20Transaction(MiniData zTxID, String zTokenID, String zFrom, String zTo, MiniNumber zAmount) {
        // Validate addresses
        if (!isValidERC20Address(zFrom) || !isValidERC20Address(zTo)) {
            return false;
        }
        
        // Validate token
        ERC20Token token = tokens.get(zTokenID);
        if (token == null) {
            return false;
        }
        
        // Create transaction
        ERC20Transaction tx = new ERC20Transaction(
            zTxID,
            zTokenID,
            zFrom,
            zTo,
            zAmount
        );
        
        pendingTransactions.put(zTxID.toString(), tx);
        
        // Process through bridge service
        if (bridgeService.processBridgeTransaction("erc20", zTxID, zAmount)) {
            // Update metrics
            metrics.incrementTotalTransactions();
            metrics.addTotalAmount(zAmount);
            return true;
        }
        
        return false;
    }
    
    public boolean confirmERC20Transaction(MiniData zTxID) {
        ERC20Transaction tx = pendingTransactions.get(zTxID.toString());
        if (tx == null) {
            return false;
        }
        
        // Mark transaction as confirmed
        tx.setConfirmed(true);
        
        // Update metrics
        metrics.incrementSuccessfulTransactions();
        
        return true;
    }
    
    public void addERC20Token(ERC20Token zToken) {
        tokens.put(zToken.getTokenID(), zToken);
    }
    
    public void removeERC20Token(String zTokenID) {
        tokens.remove(zTokenID);
    }
    
    public void addERC20Address(ERC20Address zAddress) {
        addresses.put(zAddress.getAddress(), zAddress);
    }
    
    public void removeERC20Address(String zAddress) {
        addresses.remove(zAddress);
    }
    
    public Map<String, ERC20Token> getTokens() {
        return new HashMap<>(tokens);
    }
    
    public Map<String, ERC20Address> getAddresses() {
        return new HashMap<>(addresses);
    }
    
    public Map<String, ERC20Transaction> getPendingTransactions() {
        return new HashMap<>(pendingTransactions);
    }
    
    public ERC20Metrics getMetrics() {
        return metrics;
    }
    
    private boolean isValidERC20Address(String zAddress) {
        // Check if address is valid ERC20 format
        return zAddress.startsWith("0x") && zAddress.length() == 42;
    }
    
    public void updateTokenStatus(String zTokenID, boolean zIsOnline) {
        ERC20Token token = tokens.get(zTokenID);
        if (token != null) {
            token.setOnline(zIsOnline);
            metrics.updateTokenStatus(zTokenID, zIsOnline);
        }
    }
    
    public void updateAddressStatus(String zAddress, boolean zIsValid) {
        ERC20Address addr = addresses.get(zAddress);
        if (addr != null) {
            addr.setValid(zIsValid);
            metrics.updateAddressStatus(zAddress, zIsValid);
        }
    }
}
