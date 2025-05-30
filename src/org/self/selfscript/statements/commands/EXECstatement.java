package org.self.selfscript.statements.commands;

import java.util.List;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.Expression;
import org.self.selfscript.statements.Statement;
import org.self.selfscript.statements.StatementBlock;
import org.self.selfscript.statements.StatementParser;
import org.self.selfscript.tokens.ScriptToken;
import org.self.selfscript.tokens.ScriptTokenizer;
import org.self.selfscript.values.StringValue;

/**
 * EXEC SCRIPT
 * 
 * @author spartacusrex
 *
 */
public class EXECstatement implements Statement{

	Expression mScript;
	
	public EXECstatement(Expression zScript) {
		mScript = zScript;
	}
	
	@Override
	public void execute(Contract zContract) throws ExecutionException {
		//get the Script..
		StringValue script = (StringValue) mScript.getValue(zContract);
		
		try {
			//Tokenize the script
			ScriptTokenizer tokz = new ScriptTokenizer(script.toString());
			
			//Convert the script to SELFScript!
			List<ScriptToken> tokens = tokz.tokenize();	
		
			//And now convert to a statement block..
			StatementBlock mBlock = StatementParser.parseTokens(tokens, zContract.getStackDepth());
			
			//Now run it..
			mBlock.run(zContract);
		
		}catch(ExecutionException exc) {
			throw exc;
			
		}catch(Exception exc) {
			throw new ExecutionException(exc.toString());			
		}
	}
	
	@Override
	public String toString() {
		return "EXEC "+mScript;
	}
}
