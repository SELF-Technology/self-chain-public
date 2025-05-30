package org.self.selfscript.functions.hex;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.Value;
import org.self.objects.base.MiniData;

/**
 * Works on HEX
 * @author spartacusrex
 *
 */
public class OVERWRITE extends SelfFunction {

	public OVERWRITE() {
		super("OVERWRITE");
	}

	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Get a a subset of a hex value..
		MiniData src 	= zContract.getHexParam(0, this).getMiniData();
		int srcpos 		= zContract.getNumberParam(1, this).getNumber().getAsInt();
		
		MiniData destorig 	= zContract.getHexParam(2, this).getMiniData();
		MiniData dest 		= new MiniData(destorig.to0xString());
		int destpos 		= zContract.getNumberParam(3, this).getNumber().getAsInt();
		
		int len   			= zContract.getNumberParam(4, this).getNumber().getAsInt();
		
		//Do some checks..
		if(destpos+len > dest.getLength()) {
			throw new ExecutionException("OVERWRITE destination array too short");
		
		}else if( srcpos+len > src.getLength()) {
			throw new ExecutionException("OVERWRITE src array too short");
		
		}else if( len < 0) {
			throw new ExecutionException("Cannot have negative length "+len);
			
		}
			
		
		//Now overwrite the bytes..
		System.arraycopy(src.getBytes(), srcpos, dest.getBytes(), destpos, len);
		
		return new HexValue(dest);	
	}
	
	@Override
	public int requiredParams() {
		return 5;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new OVERWRITE();
	}
}
