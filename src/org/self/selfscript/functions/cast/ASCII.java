package org.self.selfscript.functions.cast;

import java.nio.charset.Charset;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;

/**
 * Replace ALL occurrences of str with replacemnet
 * 
 * @author spartacusrex
 */
public class ASCII extends SelfFunction {

	public ASCII() {
		super("ASCII");
	}

	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Get the HEX value
		HexValue hex = zContract.getHexParam(0, this);
		
		//Now create a ASCII String
		String newstr = new String(hex.getRawData(), Charset.forName("ASCII"));
		
		return new StringValue(newstr);	
	}
	
	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new ASCII();
	}
}
