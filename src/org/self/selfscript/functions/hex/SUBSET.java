package org.self.selfscript.functions.hex;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.Value;

/**
 * Works on Scripts and HEX
 * @author spartacusrex
 *
 */
public class SUBSET extends SelfFunction {

	public SUBSET() {
		super("SUBSET");
	}

	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Get a a subset of a hex value..
		int start = zContract.getNumberParam(0, this).getNumber().getAsInt();
		int end   = zContract.getNumberParam(1, this).getNumber().getAsInt();
		int len   = end - start;
		
		//Check size
		if(len<0) {
			throw new ExecutionException("Negative SUBSET length "+len);
		}else if (len>Contract.MAX_DATA_SIZE) {
			throw new ExecutionException("SUBSET size too large "+len);
		}
		
		//Now pick it out of the 3rd value..
		byte[] orig = zContract.getHexParam(2, this).getRawData();
		int origlen = orig.length;
		
		//Check limits
		if(start<0 || start>origlen) {
			throw new ExecutionException("SUBSET start outside size of data array "+start+"-"+end+" length:"+origlen);
		}
		
		if(end<0 || end>origlen) {
			throw new ExecutionException("SUBSET end outside size of data array "+start+"-"+end+" length:"+origlen);
		}
		
		//Now get the subset
		byte[] subs = new byte[len];
		System.arraycopy(orig, start, subs, 0, len);
		
		return new HexValue(subs);	
	}
	
	@Override
	public int requiredParams() {
		return 3;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new SUBSET();
	}
}
