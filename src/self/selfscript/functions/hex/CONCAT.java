package org.self.selfscript.functions.hex;

import java.util.ArrayList;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.Expression;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.Value;

public class CONCAT extends SelfFunction{
	
	public CONCAT() {
		super("CONCAT");
	}

	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		
		//Check parameters..
		checkMinParamNumber(requiredParams());
		
		//Run through the function parameters and concatenate..
		ArrayList<Expression> params = getAllParameters();
		
		//Sum them
		byte[][] parambytes = new byte[getAllParameters().size()][];
		int totlen  = 0;
		int counter = 0;
		for(Expression exp : params) {
			Value vv = exp.getValue(zContract);
			checkIsOfType(vv, Value.VALUE_HEX);
			
			//This is a HEXValue
			HexValue hex = (HexValue)vv;
			
			//Get the bytes
			parambytes[counter] = hex.getRawData();
			totlen += parambytes[counter].length;
		
			//1MB max size..
			if(totlen > Contract.MAX_DATA_SIZE) {
				throw new ExecutionException("MAX HEX value size reached : "+totlen+"/"+Contract.MAX_DATA_SIZE);
			}
			
			counter++;
		}
		
		//The result is placed in here
		byte[] result     = new byte[totlen];
		//And sum
		int pos=0;
		for(int i=0;i<counter;i++) {
			//Is it RAW data
			System.arraycopy(parambytes[i], 0, result, pos, parambytes[i].length);
			pos += parambytes[i].length;
		}
		
		return new HexValue(result);
	}
	
	@Override
	public boolean isRequiredMinimumParameterNumber() {
		return true;
	}
	
	@Override
	public int requiredParams() {
		return 2;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new CONCAT();
	}
}
