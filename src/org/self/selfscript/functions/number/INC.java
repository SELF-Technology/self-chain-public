package org.self.selfscript.functions.number;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.Value;

public class INC extends SelfFunction {

	public INC() {
		super("INC");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		NumberValue number = zContract.getNumberParam(0, this);

		return new NumberValue(number.getNumber().increment());
	}
	
	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new INC();
	}
}
