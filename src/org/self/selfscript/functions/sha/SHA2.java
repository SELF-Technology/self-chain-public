package org.self.selfscript.functions.sha;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;
import org.self.utils.Crypto;

public class SHA2 extends SelfFunction {

	/**
	 * @param zName
	 */
	public SHA2() {
		super("SHA2");
	}
	
	/* (non-Javadoc)
	 * @see org.ramcash.ramscript.functions.Function#runFunction()
	 */
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		Value vv = getParameter(0).getValue(zContract);
		checkIsOfType(vv, Value.VALUE_HEX | Value.VALUE_SCRIPT);
		
		byte[] data = null;
		if(vv.getValueType() == Value.VALUE_HEX) {
			//HEX
			HexValue hex = (HexValue)vv;
			data = hex.getRawData();
			
		}else {
			//Script..
			StringValue scr = (StringValue)vv;
			data = scr.getBytes();
			
		}
		
		//Perform the SHA2 Operation
		byte[] ans = Crypto.getInstance().hashSHA2(data);
		
		//return the New HEXValue
		return new HexValue(ans);
	}
	
	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new SHA2();
	}
}
