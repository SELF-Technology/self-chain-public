/**
 * 
 */
package org.self.selfscript.expressions;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.values.Value;

/**
 * @author Spartacus Rex
 *
 */
public class VariableExpression implements Expression {

	String mVariableName;
	
	public VariableExpression(String zName) {
		super();
		
		//Store the name
		mVariableName = zName;
	}

	@Override
	public Value getValue(Contract zContract) throws ExecutionException {
		
		//This action counts as one instruction
		zContract.incrementInstructions();
				
		//Get the Value.. 
		Value val = zContract.getVariable(mVariableName);
		
		if(val == null) {
			throw new ExecutionException("Variable does not exist : "+mVariableName);
		}
		
		return val;
	}
	
	@Override
	public String toString() {
		return "variable:"+mVariableName;
	}
}


