package org.self.selfscript.functions.state;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.Value;

public class PREVSTATE extends SelfFunction {

	public PREVSTATE() {
		super("PREVSTATE");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Which Output
		int statenum = zContract.getNumberParam(0, this).getNumber().getAsInt();
				
		//Work it out
		return zContract.getPrevState( statenum );
	}

	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new PREVSTATE();
	}
}
