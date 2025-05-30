package org.self.selfscript.functions.general;

import java.util.ArrayList;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.Expression;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.Value;

public class EXISTS extends SelfFunction{

	public EXISTS() {
		super("EXISTS");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkMinParamNumber(requiredParams());
				
		//The full parameter String to search for
		String ps = "";
		
		//Get all the parameters
		ArrayList<Expression> params = getAllParameters();
		for(Expression exp : params) {
			Value numval = exp.getValue(zContract);
			checkIsOfType(numval, Value.VALUE_NUMBER);
			
			ps += numval.toString().trim()+",";		
		}
		
		//Get the Value.. 
		Value val = zContract.getVariable(ps);
		
		//Does it exist
		if(val == null) {
			return BooleanValue.FALSE;
		}
		
		return BooleanValue.TRUE;
	}

	@Override
	public boolean isRequiredMinimumParameterNumber() {
		return true;
	}
	
	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new EXISTS();
	}
}
