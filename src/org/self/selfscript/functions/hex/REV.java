package org.self.selfscript.functions.hex;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.Value;

public class REV extends SelfFunction {

	public REV() {
		super("REV");
	}	
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//The Data
		HexValue hex = zContract.getHexParam(0, this);
		
		//get the bytes..
		byte[] array  = hex.getRawData();
		
		//Lengths
		int datalen   = array.length;
		
		//Create the reverse buffer
		byte[] revdata = new byte[datalen];
		
		//Reverse..
		for(int i=0;i<datalen; i++){
			revdata[i] = array[array.length -i -1];
		}
		
		// Return reversed value
		return new HexValue(revdata);
	}

	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new REV();
	}
}
