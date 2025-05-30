/**
 * 
 */
package org.self.selfscript.expressions;

import java.util.List;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.SelfParseException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.tokens.LexicalTokenizer;
import org.self.selfscript.tokens.ScriptToken;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.Value;
import org.self.objects.base.MiniNumber;

/**
 * @author Spartacus Rex
 */
public class ExpressionParser {
	
	/**
	 * The main entry point that converts a sequence of tokens 
	 * into a valid computable Expression
	 * 
	 * @param zTokens
	 * @return
	 */
	public static Expression getExpression(List<ScriptToken> zTokens, int zStackDepth) throws SelfParseException{
		//Must have some tokens!
		if(zTokens.size() == 0) {
			throw new SelfParseException("Cannot have EMPTY expression");
		}
		
		//Create a Lexical Tokenizer..
		LexicalTokenizer lt = new LexicalTokenizer(zTokens, zStackDepth);
		
		//get the complete expression..
		Expression exp = getExpression(lt);
		
		//Did we use all the tokens..
		if(!lt.checkAllTokensUsed()) {
			throw new SelfParseException("Incorrect token number in expression @ "
						+lt.getNextToken().getToken());
		}
		
		//return the final expression
		return exp;
	}
	
	/**
	 * Classes to hierarchically break down the script into Valid Expressions with
	 * correct precedence.
	 */
	public static Expression getExpression(LexicalTokenizer zTokens) throws SelfParseException{
		
		//Check Stack Depth
		if(zTokens.getStackDepth() > Contract.MAX_STACK_DEPTH) {
			throw new SelfParseException("Stack too deep (MAX "+Contract.MAX_STACK_DEPTH+") "+zTokens.getStackDepth());
		}
		
		//Top level..
		Expression exp = getRelation(zTokens);
		
		while(zTokens.hasMoreElements()) {
			ScriptToken tok = zTokens.getNextToken();
			
			if(tok.getToken().equals("AND")) {
				exp = new BooleanExpression(exp, getRelation(zTokens), BooleanExpression.BOOLEAN_AND);
			}else if(tok.getToken().equals("OR")) {
				exp = new BooleanExpression(exp, getRelation(zTokens), BooleanExpression.BOOLEAN_OR);
			}else if(tok.getToken().equals("XOR")) {
				exp = new BooleanExpression(exp, getRelation(zTokens), BooleanExpression.BOOLEAN_XOR);
			}else if(tok.getToken().equals("NAND")) {
				exp = new BooleanExpression(exp, getRelation(zTokens), BooleanExpression.BOOLEAN_NAND);
			}else if(tok.getToken().equals("NOR")) {
				exp = new BooleanExpression(exp, getRelation(zTokens), BooleanExpression.BOOLEAN_NOR);
			}else if(tok.getToken().equals("NXOR")) {
				exp = new BooleanExpression(exp, getRelation(zTokens), BooleanExpression.BOOLEAN_NXOR);
			}else{
				zTokens.goBackToken();
				break;
			}
		}
		
		return exp;
	}
		
	private static Expression getRelation(LexicalTokenizer zTokens) throws SelfParseException{
		//Keep Drilling Down..
		Expression exp = getLogic(zTokens);
		
		while(zTokens.hasMoreElements()) {
			ScriptToken tok = zTokens.getNextToken();
			
			if(tok.getToken().equals("EQ")) {
				exp = new BooleanExpression(exp, getLogic(zTokens), BooleanExpression.BOOLEAN_EQ);
			}else if(tok.getToken().equals("NEQ")) {
				exp = new BooleanExpression(exp, getLogic(zTokens), BooleanExpression.BOOLEAN_NEQ);
			}else if(tok.getToken().equals("GT")) {
				exp = new BooleanExpression(exp, getLogic(zTokens), BooleanExpression.BOOLEAN_GT);
			}else if(tok.getToken().equals("GTE")) {
				exp = new BooleanExpression(exp, getLogic(zTokens), BooleanExpression.BOOLEAN_GTE);
			}else if(tok.getToken().equals("LT")) {
				exp = new BooleanExpression(exp, getLogic(zTokens), BooleanExpression.BOOLEAN_LT);
			}else if(tok.getToken().equals("LTE")) {
				exp = new BooleanExpression(exp, getLogic(zTokens), BooleanExpression.BOOLEAN_LTE);
			}else{
				zTokens.goBackToken();
				break;
			}			
		}
		
		return exp;
	}
	
	private static Expression getLogic(LexicalTokenizer zTokens) throws SelfParseException{
		//Keep Drilling Down..
		Expression exp = getAddSub(zTokens);
		
		while(zTokens.hasMoreElements()) {
			ScriptToken tok = zTokens.getNextToken();
			
			if(tok.getToken().equals("&")) {
				exp = new OperatorExpression(exp,getAddSub(zTokens),OperatorExpression.OPERATOR_AND);
			}else if(tok.getToken().equals("|")) {
				exp = new OperatorExpression(exp,getAddSub(zTokens),OperatorExpression.OPERATOR_OR);
			}else if(tok.getToken().equals("^")) {
				exp = new OperatorExpression(exp,getAddSub(zTokens),OperatorExpression.OPERATOR_XOR);
			}else{
				zTokens.goBackToken();
				break;
			}			
		}
		
		return exp;
	}
	
	private static Expression getAddSub(LexicalTokenizer zTokens) throws SelfParseException{
		Expression exp = getMulDiv(zTokens);
		
		while(zTokens.hasMoreElements()) {
			ScriptToken tok = zTokens.getNextToken();
			
			if(tok.getToken().equals("+")) {
				exp = new OperatorExpression(exp,getMulDiv(zTokens),OperatorExpression.OPERATOR_ADD);
			}else if(tok.getToken().equals("-")) {
				exp = new OperatorExpression(exp,getMulDiv(zTokens),OperatorExpression.OPERATOR_SUB);
			}else if(tok.getToken().equals("%")) {
				exp = new OperatorExpression(exp,getMulDiv(zTokens),OperatorExpression.OPERATOR_MODULO);
			}else if(tok.getToken().equals("<<")) {
				exp = new OperatorExpression(exp,getMulDiv(zTokens),OperatorExpression.OPERATOR_SHIFTL);
			}else if(tok.getToken().equals(">>")) {
				exp = new OperatorExpression(exp,getMulDiv(zTokens),OperatorExpression.OPERATOR_SHIFTR);
			}else {
				zTokens.goBackToken();
				break;
			}
		}
		
		return exp;
	}

	private static Expression getMulDiv(LexicalTokenizer zTokens) throws SelfParseException{
		Expression exp = getPrimary(zTokens);
		
		while(zTokens.hasMoreElements()) {
			ScriptToken tok = zTokens.getNextToken();
			
			if(tok.getToken().equals("*")) {
				exp = new OperatorExpression(exp,getPrimary(zTokens),OperatorExpression.OPERATOR_MUL);
			}else if(tok.getToken().equals("/")) {
				exp = new OperatorExpression(exp,getPrimary(zTokens),OperatorExpression.OPERATOR_DIV);
			}else {
				zTokens.goBackToken();
				break;
			}
		}
		
		return exp;
	}
	
	private static Expression getPrimary(LexicalTokenizer zTokens) throws SelfParseException{
		//The final result
		Expression exp = null; 
		
		//get the Token
		ScriptToken tok = zTokens.getNextToken();
		
		if(tok.getToken().equals("NOT")) {
			exp = new BooleanExpression(getPrimary(zTokens), BooleanExpression.BOOLEAN_NOT);
			
		}else if(tok.getToken().equals("NEG")) {
			exp = new OperatorExpression(getPrimary(zTokens), OperatorExpression.OPERATOR_NEG);
		
		}else if(tok.getToken().equals("~")) {
			exp = new OperatorExpression(getPrimary(zTokens), OperatorExpression.OPERATOR_NOT);
		
		}else {
			zTokens.goBackToken();
			exp = getBaseUnit(zTokens);
		}
		
		return exp;
	}
	
	private static Expression getBaseUnit(LexicalTokenizer zTokens) throws SelfParseException{
		//The final result
		Expression exp = null; 
		
		//get the Token
		ScriptToken tok = zTokens.getNextToken();
		
		if(tok.getTokenType() == ScriptToken.TOKEN_VALUE) {
			exp = new ConstantExpression( Value.getValue(tok.getToken()) ); 
		
			//Negative Numbers handled here..
		}else if(tok.getToken().equals("-")) {
			//The next token MUST be a number
			ScriptToken num = zTokens.getNextToken();
			
			//Create a Negative Number 
			MiniNumber numv = new MiniNumber(num.getToken()).mult(MiniNumber.MINUSONE);
			exp = new ConstantExpression(new NumberValue(numv));
			
		}else if(tok.getTokenType() == ScriptToken.TOKEN_GLOBAL) {
			exp = new GlobalExpression(tok.getToken());
		
		}else if(tok.getTokenType() == ScriptToken.TOKEN_VARIABLE) {
			exp = new VariableExpression(tok.getToken());
		
		}else if(tok.getTokenType() == ScriptToken.TOKEN_TRUE) {
			exp = new ConstantExpression(BooleanValue.TRUE);
		
		}else if(tok.getTokenType() == ScriptToken.TOKEN_FALSE) {
			exp = new ConstantExpression(BooleanValue.FALSE);
		
		}else if(tok.getTokenType() == ScriptToken.TOKEN_FUNCTIION) {
			
			//Which Function
			SelfFunction func = SelfFunction.getFunction(tok.getToken());
			
			//Remove the Front bracket.
			ScriptToken bracket = zTokens.getNextToken();
			if(bracket.getTokenType() != ScriptToken.TOKEN_OPENBRACKET) {
				throw new SelfParseException("Missing opening bracket at start of function "+func.getName());
			}
			
			//Now accept variables until you find the closing bracket
			while(true) {
				//Have we reached the close bracket
				ScriptToken isclosebracket = zTokens.getNextToken();
				
				//It must be a close bracket
				if(isclosebracket.getTokenType() == ScriptToken.TOKEN_CLOSEBRACKET) {
					//That' it then..
					break;
					
				}else {
					//Go Back
					zTokens.goBackToken();
			
					//Increment Stack Depth
					zTokens.incrementStackDepth();
					
					//And get the next expression..
					func.addParameter(getExpression(zTokens));
					if(func.getAllParameters().size() > Contract.MAX_FUNCTION_PARAMS) {
						throw new SelfParseException("Too many function params, max "+Contract.MAX_FUNCTION_PARAMS);
					}
					
					//Decrement Stack
					zTokens.decrementStackDepth();
				}
			}
			
			//Check the correct number of Parameters 
			func.checkParamNumberCorrect();
			
			//Now create the Complete Expression
			exp = new FunctionExpression(func);
			
		}else if(tok.getTokenType() == ScriptToken.TOKEN_OPENBRACKET) {
			
			//Increment Stack Depth
			zTokens.incrementStackDepth();
			
			//It's a new complete expression
			exp = getExpression(zTokens);
			
			//Decrement Stack
			zTokens.decrementStackDepth();
			
			//Next token MUST be a close bracket..
			ScriptToken closebracket = zTokens.getNextToken();
			
			if(closebracket.getTokenType() != ScriptToken.TOKEN_CLOSEBRACKET) {
				throw new SelfParseException("Missing close bracket. Found : "+closebracket.getToken());
			}
			
		}else{
			throw new SelfParseException("Incorrect Token in script "+tok.getToken()+" @ "+zTokens.getCurrentPosition());
		}
		
		return exp;
	}	
}
