package org.self.system.selfscript;

import java.util.HashMap;
import java.util.Map;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.exceptions.SelfParseException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.statements.StatementBlock;
import org.self.selfscript.statements.StatementParser;
import org.self.selfscript.tokens.ScriptToken;
import org.self.selfscript.tokens.ScriptTokenizer;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;
import org.self.objects.Transaction;
import org.self.objects.Witness;
import org.self.utils.SelfLogger;

public class SelfScriptVM extends Contract {
    private static SelfScriptVM instance;
    private Map<String, SelfFunction> builtInFunctions;
    private Map<String, Value> globalVariables;
    
    private SelfScriptVM() {
        super();
        initializeBuiltInFunctions();
        initializeGlobalVariables();
    }
    
    public static SelfScriptVM getInstance() {
        if (instance == null) {
            instance = new SelfScriptVM();
        }
        return instance;
    }
    
    private void initializeBuiltInFunctions() {
        builtInFunctions = new HashMap<>();
        
        // Add SELF-specific functions
        builtInFunctions.put("blockchain.blockNumber", new BlockchainBlockNumberFunction());
        builtInFunctions.put("blockchain.timestamp", new BlockchainTimestampFunction());
        builtInFunctions.put("token.balanceOf", new TokenBalanceFunction());
        builtInFunctions.put("governance.createProposal", new CreateProposalFunction());
        
        // Add Rosetta compatibility functions
        builtInFunctions.put("rosetta.createOperation", new CreateOperationFunction());
    }
    
    private void initializeGlobalVariables() {
        globalVariables = new HashMap<>();
        globalVariables.put("blockNumber", new NumberValue(blockNumber()));
        globalVariables.put("timestamp", new NumberValue(timestamp()));
        globalVariables.put("txHash", new HexValue(txHash()));
    }
    
    @Override
    public void execute() throws ExecutionException {
        try {
            // Parse the script
            ScriptTokenizer tokenizer = new ScriptTokenizer(mRamScript);
            StatementParser parser = new StatementParser(tokenizer);
            mBlock = parser.parseBlock();
            
            // Execute the block
            mBlock.execute(this);
            
        } catch (SelfParseException e) {
            throw new ExecutionException("Script parsing error: " + e.getMessage());
        }
    }
    
    public Value executeFunction(String zFunctionName, Value... zArgs) throws ExecutionException {
        SelfFunction function = builtInFunctions.get(zFunctionName);
        if (function == null) {
            throw new ExecutionException("Unknown function: " + zFunctionName);
        }
        return function.execute(this, zArgs);
    }
    
    public Value getGlobalVariable(String zName) {
        return globalVariables.get(zName);
    }
    
    public void setGlobalVariable(String zName, Value zValue) {
        globalVariables.put(zName, zValue);
    }
    
    private long blockNumber() {
        return mTransaction.getBlockNumber().toLong();
    }
    
    private long timestamp() {
        return mTransaction.getTimestamp().toLong();
    }
    
    private String txHash() {
        return mTransaction.getHash().toString();
    }
    
    public void addBuiltInFunction(String zName, SelfFunction zFunction) {
        builtInFunctions.put(zName, zFunction);
    }
    
    public void resetVM() {
        mVariables.clear();
        mSignatures.clear();
        initializeGlobalVariables();
    }
}
