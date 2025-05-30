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
import org.self.selfscript.values.HexValue;
import org.self.objects.ScriptProof;
import org.self.objects.Witness;

public class MASTstatement implements Statement {

	/**
	 * The MAST script is a HEXvalue that is the hash of the script..
	 */
	Expression mMASTScript;
	
	public MASTstatement(Expression zMAST) {
		mMASTScript = zMAST;
	}
	
	@Override
	public void execute(Contract zContract) throws ExecutionException {
		//get the MAST Value..
		HexValue mast = (HexValue) mMASTScript.getValue(zContract);
		
		//Now get that Script from the transaction..
		Witness wit = zContract.getWitness();
		
		//Get the Script Proof
		ScriptProof scrpr = wit.getScript(mast.getMiniData());
		
		if(scrpr == null) {
			throw new ExecutionException("No script found for MAST "+mast.getMiniData());
		}
		
		//get the script of this hash value
		String script = scrpr.getScript().toString();
		
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
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new ExecutionException(e.toString());
		}		
	}

	@Override
	public String toString() {
		return "MAST "+mMASTScript.toString();
	}
}
