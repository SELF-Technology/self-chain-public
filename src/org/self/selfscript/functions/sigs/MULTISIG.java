package org.self.selfscript.functions.sigs;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.Value;

public class MULTISIG extends SelfFunction {

	public MULTISIG() {
		super("MULTISIG");
	}

	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkMinParamNumber(requiredParams());
		
		//How many required.. 
		int num = zContract.getNumberParam(0, this).getNumber().getAsInt();
		
		//How many to check from
		int tot= getParameterNum()-1;
		
		//Check valid request..
		if(num<0) {
			throw new ExecutionException("CANNOT check negative sigs in MULTISIG "+num);
		}

		//Cycle..
		int found =0;
		for(int i=0;i<tot;i++) {
			HexValue sig = zContract.getHexParam(1+i, this);
		
			if(zContract.checkSignature(sig)) {
				found++;
			}
			
			if(found >= num) {
				break;
			}
		}
		
		if(found >= num) {
			return BooleanValue.TRUE;
		}else {
			return BooleanValue.FALSE;
		}
	}

	@Override
	public boolean isRequiredMinimumParameterNumber() {
		return true;
	}
	
	@Override
	public int requiredParams() {
		return 2;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new MULTISIG();
	}
}
