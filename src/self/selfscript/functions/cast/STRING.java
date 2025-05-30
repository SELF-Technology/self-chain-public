package org.self.selfscript.functions.cast;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;

public class STRING extends SelfFunction {

	public STRING() {
		super("STRING");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		Value val = getParameter(0).getValue(zContract);
		
		return new StringValue(val.toString());
	}

	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new STRING();
	}
}
