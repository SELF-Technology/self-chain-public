package org.self.selfscript.functions.general;

import java.util.ArrayList;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.Expression;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.Value;

public class GET extends SelfFunction{

	public GET() {
		super("GET");
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
		
		//MUST be a valid entry
		if(val == null) {
			throw new ExecutionException("GET Variable not found : "+ps);
		}
		
		return val;
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
		return new GET();
	}
}
