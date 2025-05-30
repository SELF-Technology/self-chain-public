package org.self.selfscript.functions.hex;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;

public class LEN extends SelfFunction{

	public LEN() {
		super("LEN");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//The Data
		Value val 	= getParameter(0).getValue(zContract);
		
		if(val.getValueType() == Value.VALUE_HEX) {
			
			HexValue hv = (HexValue)val;
			int len     = hv.getRawData().length;
			
			return new NumberValue(len);
		
		}else if(val.getValueType() == Value.VALUE_SCRIPT) {
			
			StringValue sv 	= (StringValue)val;
			int len     	= sv.toString().length();
			
			return new NumberValue(len);
		}
		
		throw new ExecutionException("LEN requires HEX or STRING param @ "+val.toString());
	}

	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new LEN();
	}
}
