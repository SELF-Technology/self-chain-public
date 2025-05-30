package org.self.selfscript.functions.cast;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;

/**
 * Convert a HEXValue to a NUMBERVALUE
 * 
 * @author spartacusrex
 *
 */
public class NUMBER extends SelfFunction{

	public NUMBER() {
		super("NUMBER");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Get the Value..
		Value val = getParameter(0).getValue(zContract);
		
		int type = val.getValueType();
		if(type == Value.VALUE_BOOLEAN) {
			BooleanValue cval = (BooleanValue)val;
			if(cval.isTrue()) {
				return new NumberValue(1);
			}else{
				return new NumberValue(0);
			}
		
		}else if(type == Value.VALUE_HEX) {
			HexValue cval = (HexValue)val;
			MiniData md1 = cval.getMiniData();
			MiniNumber num = new MiniNumber(md1.getDataValue());
			return new NumberValue(num);
	
		}else if(type == Value.VALUE_SCRIPT) {
			StringValue cval = (StringValue)val;
			return new NumberValue(cval.toString());
		
		}else if(type == Value.VALUE_NUMBER) {
			NumberValue cval = (NumberValue)val;
			return new NumberValue(cval.getNumber());
		}
	
		throw new ExecutionException("Invalid Type in NUMBER cast "+type);
	}

	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new NUMBER();
	}
}
