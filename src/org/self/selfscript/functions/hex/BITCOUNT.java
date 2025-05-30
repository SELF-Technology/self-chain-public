package org.self.selfscript.functions.hex;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.Value;

/**
 * Count the number of bits set in a HEX value..
 * @author spartacusrex
 *
 */
public class BITCOUNT extends SelfFunction {

	public static final int[] BITSPERBYTE = {0,1,1,2,1,2,2,3,1,2,2,3,2,3,3,4,1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,4,5,5,6,5,6,6,7,5,6,6,7,6,7,7,8};
	
	/**
	 * @param zName
	 */
	public BITCOUNT() {
		super("BITCOUNT");
	}
	
	/* (non-Javadoc)
	 * @see org.ramcash.ramscript.functions.Function#runFunction()
	 */
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//get the Input Data
		byte[] data = zContract.getHexParam(0, this).getRawData();
		
		//How many Bits are set..
		int bits = totalBits(data);
		
		//return the New HEXValue
		return new NumberValue(bits);
	}
	
	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new BITCOUNT();
	}
	
	public static int totalBits(byte[] zData) {
		int total = 0;
		for(int i=0;i<zData.length;i++) {
			total+=BITSPERBYTE[zData[i] & 0xFF]; 
		}
		return total;
	}
}
