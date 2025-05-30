/**
 * 
 */
package org.self.selfscript.functions.number;

import java.util.ArrayList;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.Expression;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.Value;

/**
 * @author Spartacus Rex
 *
 */
public class MAX extends SelfFunction {

	public MAX() {
		super("MAX");
	}
	
	/* (non-Javadoc)
	 * @see org.ramcash.ramscript.functions.Function#runFunction(org.ramcash.ramscript.Contract)
	 */
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkMinParamNumber(requiredParams());
		
		//Run through the function parameters and pick the maximum numeric value..
		ArrayList<Expression> params = getAllParameters();
		
		boolean first 		= true;
		NumberValue max 	= null;
		
		for(Expression exp : params) {
			Value numval = exp.getValue(zContract);
			checkIsOfType(numval, Value.VALUE_NUMBER);
			
			//Get the Value
			NumberValue chk = (NumberValue)numval;
			
			if(first) {
				first 	= false;
				max 	= chk;
			}else {
				if(chk.getNumber().isMore(max.getNumber())) {
					max = chk;
				}
			}
		}
		
		return max;
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
		return new MAX();
	}
}
