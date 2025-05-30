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
public class UTF8 extends SelfFunction {

	public UTF8() {
		super("UTF8");
	}

	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Get the HEX value
		HexValue hex = zContract.getHexParam(0, this);
		
		//Now create a UTF8 String
		String newstr = new String(hex.getRawData(), Charset.forName("UTF-8"));
		
		return new StringValue(newstr);	
	}
	
	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new UTF8();
	}
}
