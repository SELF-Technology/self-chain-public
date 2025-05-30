/**
 * 
 */
package org.self.selfscript.statements.commands;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.Expression;
import org.self.selfscript.statements.Statement;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.Value;

/**
 * @author Spartacus Rex
 *
 */
public class ASSERTstatement implements Statement{

	Expression mAssertValue;
	
	/**
	 * 
	 */
	public ASSERTstatement(Expression zAssertValue) {
		mAssertValue = zAssertValue;
	}
	
	@Override
	public void execute(Contract zContract) throws ExecutionException {
		//Get the expression
		Value val = mAssertValue.getValue(zContract);
		
		//MUST be a boolean
		if(val.getValueType() != Value.VALUE_BOOLEAN) {
			throw new ExecutionException("ASSERT MUST use a BOOLEAN expression : "+toString());
		}
		
		//Does it pass
		boolean success = ((BooleanValue)val).isTrue();
		
		//Tell the Contract to FAIL if FALSE
		if(!success) {
			zContract.setRETURNValue(false);
		}
	}
	
	@Override
	public String toString() {
		return "ASSERT "+mAssertValue.toString();
	}
}
