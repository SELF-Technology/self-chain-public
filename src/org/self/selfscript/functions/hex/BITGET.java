package org.self.selfscript.functions.hex;

import java.util.BitSet;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.Value;

public class BITGET extends SelfFunction {

	/**
	 * @param zName
	 */
	public BITGET() {
		super("BITGET");
	}
	
	/* (non-Javadoc)
	 * @see org.ramcash.ramscript.functions.Function#runFunction()
	 */
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//get the Input Data
		byte[] data = zContract.getHexParam(0, this).getRawData();
		int totbits = (data.length * 8) - 1;
		
		//Get the desired Bit
		int bit = zContract.getNumberParam(1, this).getNumber().getAsInt();
		if(bit<0 || bit>totbits) {
			throw new ExecutionException("BitGet too large "+bit+" / "+totbits);
		}
		
		//Create a BitSet object..
		BitSet bits = BitSet.valueOf(data);
		
		//Now check the value..
		boolean isSet = bits.get(bit);
		
		//return..
		return new BooleanValue(isSet);
	}
	
	@Override
	public int requiredParams() {
		return 2;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new BITGET();
	}
}
