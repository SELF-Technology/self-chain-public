package org.self.system.bridge;

import java.util.HashMap;
import java.util.Map;

import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.system.governance.GovernanceProposal;
import org.self.system.governance.ai.AIVotingSystem;
import org.self.system.governance.hex.HexValidator;
import org.self.system.governance.monitor.GovernanceMonitor;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class BridgeService {
    private static BridgeService instance;
    private Map<String, BridgeParameters> bridgeParams;
    private Map<String, BridgeStatus> bridgeStatus;
    private GovernanceMonitor monitor;
    
    private BridgeService() {
        bridgeParams = new HashMap<>();
        bridgeStatus = new HashMap<>();
        monitor = GovernanceMonitor.getInstance();
        initializeDefaultBridges();
    }
    
    public static BridgeService getInstance() {
        if (instance == null) {
            instance = new BridgeService();
        }
        return instance;
    }
    
    private void initializeDefaultBridges() {
        // Initialize ERC-20 bridge parameters
        BridgeParameters erc20Params = new BridgeParameters();
        erc20Params.setBridgeType("ERC20");
        erc20Params.setFeeRate(new MiniNumber(0.01)); // 1% fee
        erc20Params.setMinAmount(SELFParams.SELF_MIN_REWARD);
        erc20Params.setMaxAmount(SELFParams.SELF_MAX_REWARD);
        
        bridgeParams.put("erc20", erc20Params);
        
        // Initialize Wire Network bridge parameters
        BridgeParameters wireParams = new BridgeParameters();
        wireParams.setBridgeType("WIRE");
        wireParams.setFeeRate(new MiniNumber(0.005)); // 0.5% fee
        wireParams.setMinAmount(SELFParams.SELF_MIN_REWARD);
        wireParams.setMaxAmount(SELFParams.SELF_MAX_REWARD);
        
        bridgeParams.put("wire", wireParams);
    }
    
    public boolean createBridgeProposal(String zBridgeType, Map<String, Object> zParams) {
        // Create governance proposal
        MiniData proposalID = MiniData.getRandomData();
        String description = "Bridge configuration proposal for " + zBridgeType;
        
        // Create proposal
        GovernanceProposal proposal = new GovernanceProposal(
            proposalID,
            description,
            AIVotingSystem.getInstance().getValidators().get(0).getValidatorID()
        );
        
        // Add bridge parameters to proposal
        BridgeParameters params = new BridgeParameters();
        params.setBridgeType(zBridgeType);
        params.setParameters(zParams);
        proposal.setParameters(params);
        
        // Track proposal
        monitor.trackProposal(proposal);
        
        return true;
    }
    
    public boolean processBridgeTransaction(String zBridgeType, MiniData zTxID, MiniNumber zAmount) {
        // Check bridge parameters
        BridgeParameters params = bridgeParams.get(zBridgeType.toLowerCase());
        if (params == null) {
            return false;
        }
        
        // Check amount limits
        if (zAmount.compareTo(params.getMinAmount()) < 0 || 
            zAmount.compareTo(params.getMaxAmount()) > 0) {
            return false;
        }
        
        // Calculate fee
        MiniNumber fee = zAmount.multiply(params.getFeeRate());
        MiniNumber netAmount = zAmount.subtract(fee);
        
        // Update bridge status
        BridgeStatus status = bridgeStatus.getOrDefault(zBridgeType, new BridgeStatus());
        status.incrementTransactions();
        status.addTotalAmount(zAmount);
        status.addTotalFee(fee);
        
        bridgeStatus.put(zBridgeType, status);
        
        // Log transaction
        SelfLogger.log("Bridge transaction processed: " + 
            "Type=" + zBridgeType + 
            ", ID=" + zTxID + 
            ", Amount=" + zAmount + 
            ", Fee=" + fee);
        
        return true;
    }
    
    public Map<String, BridgeParameters> getBridgeParameters() {
        return new HashMap<>(bridgeParams);
    }
    
    public Map<String, BridgeStatus> getBridgeStatus() {
        return new HashMap<>(bridgeStatus);
    }
    
    public void updateBridgeParameters(String zBridgeType, Map<String, Object> zParams) {
        BridgeParameters params = bridgeParams.get(zBridgeType.toLowerCase());
        if (params != null) {
            params.setParameters(zParams);
        }
    }
    
    public void resetBridge(String zBridgeType) {
        bridgeStatus.remove(zBridgeType.toLowerCase());
    }
}
