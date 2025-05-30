package org.self.selfscript.functions.general;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;
import org.self.objects.Address;

public class ADDRESS extends SelfFunction{

	public ADDRESS() {
		super("ADDRESS");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Get the Value..
		StringValue str = zContract.getStringParam(0, this);
		
		//Convert  to an address
		Address addr = new Address(str.toString());
		
		return new HexValue(addr.getAddressData());
	}

	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new ADDRESS();
	}
}
