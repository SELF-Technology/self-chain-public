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
public class ConstantExpression implements Expression{

	private Value mValue;
	
	public ConstantExpression(Value zValue) {
		mValue = zValue;
	}
	
	@Override
	public Value getValue(Contract zContract) throws ExecutionException {
		
		//This action counts as one instruction
		zContract.incrementInstructions();
				
		return mValue;
	}
	
	@Override
	public String toString() {
		return mValue.toString();
	}
}
