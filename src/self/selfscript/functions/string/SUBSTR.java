package org.self.selfscript.functions.string;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;

/**
 * Works on Scripts and HEX
 * @author spartacusrex
 *
 */
public class SUBSTR extends SelfFunction {

	public SUBSTR() {
		super("SUBSTR");
	}

	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Get a a subset of a hex value..
		int start = zContract.getNumberParam(0, this).getNumber().getAsInt();
		int end   = zContract.getNumberParam(1, this).getNumber().getAsInt();
		int len   = end - start;
		if(len<0) {
			throw new ExecutionException("Negative SUBSTR length "+len);
		}
		
		//Now pick it out of the 3rd value..
		StringValue str = zContract.getStringParam(2, this);
		String main 	= str.toString();
		
		//Check limits
		if(start<0 || end>main.length()) {
			throw new ExecutionException("SUBSTR range outside size of String "+start+"-"+end+" length:"+main.length());
		}
		
		//Now get the substr
		String substr = main.substring(start, end);  
		
		return new StringValue(substr);	
	}
	
	@Override
	public int requiredParams() {
		return 3;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new SUBSTR();
	}
}
