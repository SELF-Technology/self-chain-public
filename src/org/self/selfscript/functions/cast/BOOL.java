package org.self.selfscript.functions.cast;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;
import org.self.objects.base.MiniNumber;

public class BOOL extends SelfFunction {

	public BOOL() {
		super("BOOL");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Get the Value..
		Value val = getParameter(0).getValue(zContract);
		
		//What Type..
		boolean ret = false;
		int type = val.getValueType();
		if(type == Value.VALUE_BOOLEAN) {
			BooleanValue cval = (BooleanValue)val;
			ret = cval.isTrue();
		
		}else if(type == Value.VALUE_HEX) {
			HexValue cval = (HexValue)val;
			
			//Convert to a mininumber - this ensures is not TOO big a data structure
			MiniNumber num = new MiniNumber(cval.getMiniData().getDataValue());
			
			//0 is FALSE
			ret = !num.isEqual(MiniNumber.ZERO);
		
		}else if(type == Value.VALUE_NUMBER) {
			NumberValue cval = (NumberValue)val;
			
			//0 is FALSE
			ret = !cval.getNumber().isEqual(MiniNumber.ZERO);
		
		}else if(type == Value.VALUE_SCRIPT) {
			StringValue cval = (StringValue)val;
			
			//check for FALSE - everything else is TRUE
			ret = !cval.toString().equals("FALSE");
		
		}else {
			throw new ExecutionException("Invalid Type in BOOL cast "+type);
		}
		
		return new BooleanValue(ret);
	}

	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new BOOL();
	}
}
