package org.self.selfscript.statements;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;

public interface Statement {
	/**
	 * Execute the Statement in the given Contract Environment
	 * 
	 * @param zContract
	 * @return The block of code to execute or null
	 */
	public void execute(Contract zContract) throws ExecutionException;
}
