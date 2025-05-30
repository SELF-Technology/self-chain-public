/**
 * 
 */
package org.self.selfscript.expressions;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.Value;

/**
 * @author Spartacus Rex
 *
 */
public class FunctionExpression implements Expression {
	
	SelfFunction mFunction;
	
	/**
	 * Create the Function Expression 
	 */
	public FunctionExpression(SelfFunction zFunction) {
		//Store for later
		mFunction = zFunction;
	}
	
	@Override
	public Value getValue(Contract zContract) throws ExecutionException {
		
		//This action counts as one instruction
		zContract.incrementInstructions();

		//Increment Stack Depth
		zContract.incrementStackDepth();
				
		//Get the Value
		Value val = mFunction.runFunction(zContract);
		
		//Decrement Stack Depth
		zContract.decrementStackDepth();
		
		//And trace it..
		zContract.traceLog(toString()+" returns:"+val.toString());
		
		return val;
	}
	
	@Override
	public String toString() {
		return "function:"+mFunction.getName()+", params:"+mFunction.getAllParameters();
	}
}
