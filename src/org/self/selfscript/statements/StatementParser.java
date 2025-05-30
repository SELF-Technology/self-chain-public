/**
 * 
 */
package org.self.selfscript.statements;

import java.util.ArrayList;
import java.util.List;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.SelfParseException;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.expressions.Expression;
import org.self.selfscript.expressions.ExpressionParser;
import org.self.selfscript.statements.commands.ASSERTstatement;
import org.self.selfscript.statements.commands.EXECstatement;
import org.self.selfscript.statements.commands.IFstatement;
import org.self.selfscript.statements.commands.LETstatement;
import org.self.selfscript.statements.commands.MASTstatement;
import org.self.selfscript.statements.commands.RETURNstatement;
import org.self.selfscript.statements.commands.WHILEstatement;
import org.self.selfscript.tokens.LexicalTokenizer;
import org.self.selfscript.tokens.ScriptToken;
import org.self.selfscript.values.BooleanValue;

/**
 * IF..THEN ELSEIF.. THEN.. ELSE.. ENDIF
 * 
 * @author Spartacus Rex
 *
 */
public class StatementParser {

	/**
	 * Parse a list of tokens into a list of Statements
	 * @param zTokens
	 * @return the list of Statements
	 */
	public static StatementBlock parseTokens(List<ScriptToken> zTokens, int zStackDepth) throws Exception{
		List<Statement> stats = new ArrayList<>();
		
		//The current stack depth
		int currentStackDepth = zStackDepth+1;
		
		//Check Stack Depth
		if(currentStackDepth > Contract.MAX_STACK_DEPTH) {
			throw new SelfParseException("Stack too deep (MAX "+Contract.MAX_STACK_DEPTH+") "+currentStackDepth);
		}
				
		//Cycle..
		int currentPosition	= 0;
		int totaltokens 	= zTokens.size();
		
		while(currentPosition<totaltokens) {
		
			//Get the current token
			ScriptToken tok = zTokens.get(currentPosition++);
			
			String token 	= tok.getToken();
			int type 		= tok.getTokenType();
			
			if(type != ScriptToken.TOKEN_COMMAND) {
				throw new SelfParseException("Invalid Token where there should be a COMMMAND - "+token); 
			}
			
			//Cycle through commands
			if(token.equalsIgnoreCase("LET")) {
				//The next token is either the variable name or an array position..
				ScriptToken var = zTokens.get(currentPosition++);
				
				//Is it a simple variable LET or an ARRAY set LET
				if(var.getTokenType() == ScriptToken.TOKEN_OPENBRACKET) {
					//Get the tokens to the equals signn
					List<ScriptToken> arraypos = getTokensToNextEquals(zTokens, currentPosition);
					currentPosition += arraypos.size();
					
					//Check the last token is a close bracket
					int arrsize = arraypos.size();
					var = arraypos.get(arrsize-1);
					if(var.getTokenType() != ScriptToken.TOKEN_CLOSEBRACKET) {
						throw new SelfParseException("Incorrect LET statement, missing ) .. "+var.getToken()+")");
					}
					
					//The next token is always =
					var = zTokens.get(currentPosition++);
					if(var.getTokenType() != ScriptToken.TOKEN_OPERATOR && !var.getToken().equals("=")) {
						throw new SelfParseException("Incorrect LET statement, missing = (.."+var.getToken()+")");
					}
	
					//Remove the last token..
					arraypos.remove(arrsize-1);
					
					//Check is a valid non-empty expression
					if(arraypos.size() == 0) {
						throw new SelfParseException("Incorrect LET statement, EMPTY ARRAY POS @ "+currentPosition);
					}
					
					//Create a Lexical Tokenizer.. there may be multiple expressions..
					LexicalTokenizer lt = new LexicalTokenizer(arraypos, currentStackDepth);
					
					ArrayList<Expression> exps = new ArrayList<Expression>();
					while(!lt.checkAllTokensUsed()) {
						//Now get each of the expressions
						Expression letexp = ExpressionParser.getExpression(lt);
						
						//Add it to out list for the LET statement
						exps.add(letexp);
					}
					
					//Now find the next Command, and everything in between is the expression
					List<ScriptToken> lettokens = getTokensToNextCommand(zTokens, currentPosition);
					currentPosition += lettokens.size();
					
					//Now create an expression from those tokens..
					Expression exp = ExpressionParser.getExpression(lettokens, currentStackDepth);
					
					//And finally create the LET statement..
					stats.add(new LETstatement(exps, exp));
					
				}else if(var.getTokenType() == ScriptToken.TOKEN_VARIABLE) {
					//The Variable name
					String varname = var.getToken();
					
					//The next token is always =
					var = zTokens.get(currentPosition++);
					if(!var.getToken().equals("=")) {
						throw new SelfParseException("Incorrect LET statement, missing = (.."+var.getToken()+")");
					}
				
					//Now find the next Command, and everything in between is the expression
					List<ScriptToken> lettokens = getTokensToNextCommand(zTokens, currentPosition);
					currentPosition += lettokens.size();
					
					//Now create an expression from those tokens..
					Expression exp = ExpressionParser.getExpression(lettokens, currentStackDepth);
					
					//And finally create the LET statement..
					stats.add(new LETstatement(varname, exp));
					
				}else {
					throw new SelfParseException("Not a variable or array after LET (.."+var.getToken()+")");
				}
				
			}else if(token.equalsIgnoreCase("EXEC")) {
				//Now find the next Command, and everything in between is the expression
				List<ScriptToken> exectokens = getTokensToNextCommand(zTokens, currentPosition);
				currentPosition += exectokens.size();
				
				//Now create an expression from those tokens..
				Expression exp = ExpressionParser.getExpression(exectokens, currentStackDepth);
				
				//And finally create the LET statement..
				stats.add(new EXECstatement(exp));
					
			}else if(token.equalsIgnoreCase("MAST")) {
				//Now find the next Command, and everything in between is the expression
				List<ScriptToken> masttokens = getTokensToNextCommand(zTokens, currentPosition);
				currentPosition += masttokens.size();
				
				//Now create an expression from those tokens..
				Expression exp = ExpressionParser.getExpression(masttokens, currentStackDepth);
				
				//And finally create the LET statement..
				stats.add(new MASTstatement(exp));
					
			}else if(token.equalsIgnoreCase("IF")) {
				//An IFX
				IFstatement ifsx = new IFstatement();
				
				//Get the IFConditional
				List<ScriptToken> conditiontokens = getTokensToRequiredCommand(zTokens, currentPosition, "THEN");
				
				//Now create an expression from those tokens..
				Expression IFcondition = ExpressionParser.getExpression(conditiontokens, currentStackDepth);
				
				//Increments
				currentPosition += conditiontokens.size() + 1;
				
				//Now get the Expression..
				List<ScriptToken> actiontokens = getElseOrElseIfOrEndIF(zTokens, currentPosition,true);
				
				//Increment
				currentPosition += actiontokens.size();
				
				//Is it the ENDIF or the ELSE
				String nexttok = actiontokens.get(actiontokens.size()-1).getToken();
				
				//Remove the final ENDIF - This is done here as we need all the ENDIFs for the child IF clauses
				actiontokens = actiontokens.subList(0, actiontokens.size()-1);
				
				//And convert that block of Tokens into a block of code..
				StatementBlock IFaction = parseTokens(actiontokens, currentStackDepth);
				
				//Add what we know to the IF statement..
				ifsx.addCondition(IFcondition, IFaction);
				
				//Is it ELSE or END
				while(!nexttok.equals("ENDIF")) {
					//New branch
					Expression     ELSEcondition = null;
					StatementBlock ELSEaction    = null;
					
					if(nexttok.equals("ELSE")) {
						//ELSE is default
						ELSEcondition = new ConstantExpression(BooleanValue.TRUE);
						
					}else if(nexttok.equals("ELSEIF")) {
						//It's ELSEIF
						conditiontokens = getTokensToRequiredCommand(zTokens, currentPosition, "THEN");
						
						//Create an Expression..
						ELSEcondition = ExpressionParser.getExpression(conditiontokens, currentStackDepth);
						
						//Increments
						currentPosition += conditiontokens.size() + 1;
					
					}else {
						
						//Incorrect IF statement
						throw new SelfParseException("MISSING ELSE or ELSEIF in IF Statement");
					}
					
					//Now get the Action..
					actiontokens = getElseOrElseIfOrEndIF(zTokens, currentPosition,true);
					
					//Increment
					currentPosition += actiontokens.size();
					
					//Is it the ENDIF or the ELSE
					nexttok = actiontokens.get(actiontokens.size()-1).getToken();
					
					//Remove the final ENDIF - This is done here as we need all the ENDIFs for the child IF clauses
					actiontokens = actiontokens.subList(0, actiontokens.size()-1);
					
					//And convert that block of Tokens into a block of code..
					ELSEaction = parseTokens(actiontokens, currentStackDepth);
					
					//Add what we know to the IF statement..
					ifsx.addCondition(ELSEcondition, ELSEaction);
				}
				
				//Add..
				stats.add(ifsx);
										
			}else if(token.equalsIgnoreCase("WHILE")) {
				//Get the WHILE Conditional - stop at the next THEN
				List<ScriptToken> conditiontokens = getTokensToRequiredCommand(zTokens, currentPosition, "DO");
				
				//Now create an expression from those tokens..
				Expression WHILEcondition = ExpressionParser.getExpression(conditiontokens, currentStackDepth);
				
				//Increments
				currentPosition += conditiontokens.size() + 1;
				
				//Now get the Expression..
				List<ScriptToken> actiontokens = getEndWHILE(zTokens, currentPosition);
				
				//Increment
				currentPosition += actiontokens.size();
				
				//Remove the final ENDIF - This is done here as we need all the ENDIFs for the child IF clauses
				actiontokens = actiontokens.subList(0, actiontokens.size()-1);
				
				//And convert that block of Tokens into a block of code..
				StatementBlock WHILEaction = parseTokens(actiontokens, currentStackDepth);
				
				//Create an IF statement
				WHILEstatement ws = new WHILEstatement(WHILEcondition, WHILEaction);
				
				//Add..
				stats.add(ws);
				
			}else if(token.equalsIgnoreCase("ASSERT")) {
				//The Next tokens are the Expression..
				List<ScriptToken> returntokens = getTokensToNextCommand(zTokens, currentPosition);
				currentPosition += returntokens.size();
				
				//Now create an expression from those tokens..
				Expression exp = ExpressionParser.getExpression(returntokens, currentStackDepth);
				
				//Create a new RETURN statement
				stats.add(new ASSERTstatement(exp));
			
			}else if(token.equalsIgnoreCase("RETURN")) {
				//The Next tokens are the Expression..
				List<ScriptToken> returntokens = getTokensToNextCommand(zTokens, currentPosition);
				currentPosition += returntokens.size();
				
				//Now create an expression from those tokens..
				Expression exp = ExpressionParser.getExpression(returntokens, currentStackDepth);
				
				//Create a new RETURN statement
				stats.add(new RETURNstatement(exp));
			
			}else {
				throw new SelfParseException("Invalid Token where there should be a Command - "+token); 
			}
		}
		
		return new StatementBlock(stats);
	}
	
	private static List<ScriptToken> getElseOrElseIfOrEndIF(List<ScriptToken> zTokens, int zCurrentPosition, boolean zElseAlso){
		List<ScriptToken> rettokens = new ArrayList<>();
		
		int currentpos  = zCurrentPosition;
		int total 		= zTokens.size();
		
		//Cycle through the tokens..
		while(currentpos<total) {
			
			//Get the next token
			ScriptToken tok = zTokens.get(currentpos);
			
			if(tok.getTokenType() == ScriptToken.TOKEN_COMMAND && tok.getToken().equals("ENDIF")) {
				//We've found the end to the current depth IF
				rettokens.add(tok);
				break;
			
			}else if(zElseAlso && (tok.getTokenType() == ScriptToken.TOKEN_COMMAND && tok.getToken().equals("ELSEIF")) ) {
				//We've found the end to the current depth IF
				rettokens.add(tok);
				break;
			
			}else if(zElseAlso && (tok.getTokenType() == ScriptToken.TOKEN_COMMAND && tok.getToken().equals("ELSE")) ) {
				//We've found the end to the current depth IF
				rettokens.add(tok);
				break;
				
			}else if(tok.getTokenType() == ScriptToken.TOKEN_COMMAND && tok.getToken().equals("IF")) {
				//Add it..
				rettokens.add(tok);
				currentpos++;
				
				//Go down One Level
				List<ScriptToken> toks = getElseOrElseIfOrEndIF(zTokens, currentpos, false);
			
				rettokens.addAll(toks);
				currentpos += toks.size();
			
			}else{
				//Just add it to the list
				rettokens.add(tok);
				currentpos++;
			}
		}
		
		return rettokens;
	}
	
	/**
	 * Get the last ENDWHILE of a while statement - could recurse
	 * @param zTokens
	 * @param zCurrentPosition
	 * @return
	 */
	private static List<ScriptToken> getEndWHILE(List<ScriptToken> zTokens, int zCurrentPosition){
		List<ScriptToken> rettokens = new ArrayList<>();
		
		int currentpos  = zCurrentPosition;
		int total 		= zTokens.size();
		
		//Cycle through the tokens..
		while(currentpos<total) {
			
			//Get the next token
			ScriptToken tok = zTokens.get(currentpos);
			
			if(tok.getTokenType() == ScriptToken.TOKEN_COMMAND && tok.getToken().equals("ENDWHILE")) {
				//We've found the end to the current depth IF
				rettokens.add(tok);
				break;
				
			}else if(tok.getTokenType() == ScriptToken.TOKEN_COMMAND && tok.getToken().equals("WHILE")) {
				//Add it..
				rettokens.add(tok);
				currentpos++;
				
				//Go down One Level
				List<ScriptToken> toks = getEndWHILE(zTokens, currentpos);
			
				rettokens.addAll(toks);
				currentpos += toks.size();
			
			}else {
				//Just add it to the list
				rettokens.add(tok);
				currentpos++;
			}
		}
		
		return rettokens;
	}
	
	
	/**
	 * Simple function that returns all the tokens up to the very next COMMAND TOKEN.
	 * 
	 * @param zTokens
	 * @param zCurrentPosition
	 * @return The list of tokens
	 */
	private static List<ScriptToken> getTokensToNextCommand(List<ScriptToken> zTokens, int zCurrentPosition){
		List<ScriptToken> rettokens = new ArrayList<>();
		
		int ret   = zCurrentPosition;
		int total = zTokens.size();
		while(ret<total) {
			ScriptToken tok = zTokens.get(ret);
			if(tok.getTokenType() == ScriptToken.TOKEN_COMMAND) {
				break;
			}else {
				//Add it to the list
				rettokens.add(tok);
			}
			ret++;
		}
	
		
		return rettokens;
	}
	
	private static List<ScriptToken> getTokensToRequiredCommand(List<ScriptToken> zTokens, 
															int zCurrentPosition, 
															String zRequiredToken ) throws SelfParseException{
		List<ScriptToken> rettokens = new ArrayList<>();
		
		int ret   = zCurrentPosition;
		int total = zTokens.size();
		boolean found=false;
		while(ret<total) {
			ScriptToken tok = zTokens.get(ret);
			if(tok.getToken().equals(zRequiredToken)) {
				found=true;
				break;
			}else {
				//Add it to the list
				rettokens.add(tok);
			}
			ret++;
		}
	
		//Did we find it..
		if(!found) {
			throw new SelfParseException("Could not find required token : "+zRequiredToken);
		}
		
		return rettokens;
	}
	
	
	/**
	 * Get all the tokens to the next = sign.. 
	 * @param zTokens
	 * @param zCurrentPosition
	 * @return
	 */
	private static List<ScriptToken> getTokensToNextEquals(List<ScriptToken> zTokens, int zCurrentPosition){
		List<ScriptToken> rettokens = new ArrayList<>();
		
		int ret   = zCurrentPosition;
		int total = zTokens.size();
		while(ret<total) {
			ScriptToken tok = zTokens.get(ret);
			if(tok.getTokenType() == ScriptToken.TOKEN_OPERATOR && tok.getToken().equals("=")) {
				break;
			}else {
				//Add it to the list
				rettokens.add(tok);
			}
			ret++;
		}
	
		return rettokens;
	}
}
