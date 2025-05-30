package org.self.selfscript.functions.general;

import java.util.List;
import java.util.StringTokenizer;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.functions.string.REPLACE;
import org.self.selfscript.statements.StatementBlock;
import org.self.selfscript.statements.StatementParser;
import org.self.selfscript.tokens.ScriptToken;
import org.self.selfscript.tokens.ScriptTokenizer;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;

public class FUNCTION extends SelfFunction{

	public static final String FUNCTION_RETURN = "returnvalue";
	
	public FUNCTION() {
		super("FUNCTION");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkMinParamNumber(requiredParams());
		
		//get the Script..
		StringValue script 		= zContract.getStringParam(0, this);
		String finalfunction 	= script.toString();
		
		//Check number of replacements
		StringTokenizer strtok 	= new StringTokenizer(finalfunction,"$");
		if(strtok.countTokens()-1>64) {
			throw new ExecutionException("Too many replacements in FUNCTION, max 64");
		}
		
		//Replace all the $ variables..
		int params = getAllParameters().size();
		for(int i=1;i<params;i++) {
			
			//Get the param..
			Value paramval = getParameter(i).getValue(zContract);
			
			//What type is it..
			if(paramval.getValueType() == Value.VALUE_SCRIPT) {
				finalfunction = REPLACE.safeReplaceAll(finalfunction, "$"+i, "["+paramval.toString()+"]");
			}else {
				finalfunction = REPLACE.safeReplaceAll(finalfunction, "$"+i, paramval.toString());
			}
			
			//Check number of replacements
			StringTokenizer strtokdollar = new StringTokenizer(finalfunction,"$");
			if(strtokdollar.countTokens()-1>64) {
				throw new ExecutionException("Too many replacements in FUNCTION, max 64");
			}
		}
		
		//Remove any previous return vars..
		zContract.removeVariable(FUNCTION_RETURN);
		
		try {
			//Tokenize the script
			ScriptTokenizer tokz = new ScriptTokenizer(finalfunction);
			
			//Convert the script to SELFScript!
			List<ScriptToken> tokens = tokz.tokenize();	
		
			//And now convert to a statement block..
			StatementBlock mBlock = StatementParser.parseTokens(tokens, zContract.getStackDepth());
			
			//Now run it..
			mBlock.run(zContract);
		
		}catch(ExecutionException exc) {
			throw exc;
		
		}catch(Exception exc) {
			throw new ExecutionException(exc.toString());			
		}
		
		//Is there a return variable..
		if(zContract.existsVariable(FUNCTION_RETURN)) {
			//Get the return vale..
			return zContract.getVariable(FUNCTION_RETURN);
		}
		
		return new BooleanValue(true);
	}

	@Override
	public boolean isRequiredMinimumParameterNumber() {
		return true;
	}
	
	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new FUNCTION();
	}
}
