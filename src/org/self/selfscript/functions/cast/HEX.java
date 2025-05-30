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

public class HEX extends SelfFunction{

	public HEX() {
		super("HEX");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Get the Value..
		Value val = getParameter(0).getValue(zContract);
		
		//What Type..
		MiniData ret = null;
		int type = val.getValueType();
		if(type == Value.VALUE_BOOLEAN) {
			BooleanValue cval = (BooleanValue)val;
			if(cval.isTrue()) {
				ret = new MiniData("0x01");
			}else {
				ret = new MiniData("0x00");
			}
		
		}else if(type == Value.VALUE_HEX) {
			HexValue cval = (HexValue)val;
			ret = cval.getMiniData();
		
		}else if(type == Value.VALUE_NUMBER) {
			NumberValue cval = (NumberValue)val;
			
			//Check no decimal places..
			MiniNumber num = cval.getNumber();
			if(!num.floor().isEqual(num) || num.isLess(MiniNumber.ZERO)) {
				throw new ExecutionException("Can ONLY convert positive whole NUMBERs to HEX : "+num);
			}
			
			ret = new MiniData(num.getAsBigInteger());
		
		}else if(type == Value.VALUE_SCRIPT) {
			StringValue cval = (StringValue)val;
			ret = new MiniData(cval.getBytes());
		
		}else {
			throw new ExecutionException("Invalid Type in HEX cast "+type);
		}
		
		return new HexValue(ret);
	}

	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new HEX();
	}
}
