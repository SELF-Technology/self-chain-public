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
public interface Expression {
	public Value getValue(Contract zContract) throws ExecutionException;
}
