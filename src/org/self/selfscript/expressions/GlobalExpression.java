package org.self.selfscript.expressions;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.values.Value;

public class GlobalExpression implements Expression {

	/**
	 * What type of Global id this
	 */
	String mGlobalType;

	/**
	 * Main Constructor
	 * 
	 * @param zType
	 */
	public GlobalExpression(String zType) {
		mGlobalType = zType;
	}
	
	@Override
	public Value getValue(Contract zContract) throws ExecutionException {
		
		//This action counts as one instruction
		zContract.incrementInstructions();
				
		return zContract.getGlobal(mGlobalType);
	}

	@Override
	public String toString() {
		return "global:"+mGlobalType;
	}
}
