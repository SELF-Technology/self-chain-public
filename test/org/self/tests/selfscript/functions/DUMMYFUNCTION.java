package org.self.tests.selfscript.functions;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.Value;

public class DUMMYFUNCTION extends SelfFunction {

    public DUMMYFUNCTION() {
        super("DUMMYFUNCTION");
    }

    @Override
    public Value runFunction(Contract zContract) throws ExecutionException {
        return new BooleanValue(true);
    }

    @Override
    public SelfFunction getNewFunction() {
        return new DUMMYFUNCTION();
    }

    @Override
	public boolean isRequiredMinimumParameterNumber() {
		return true;
	}
    
	@Override
	public int requiredParams() {
		return 1;
	}
}
