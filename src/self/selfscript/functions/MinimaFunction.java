/**
 * 
 */
package org.self.selfscript.functions;

import java.util.ArrayList;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.exceptions.SelfParseException;
import org.self.selfscript.expressions.Expression;
import org.self.selfscript.functions.cast.ASCII;
import org.self.selfscript.functions.cast.BOOL;
import org.self.selfscript.functions.cast.HEX;
import org.self.selfscript.functions.cast.NUMBER;
import org.self.selfscript.functions.cast.STRING;
import org.self.selfscript.functions.cast.UTF8;
import org.self.selfscript.functions.general.ADDRESS;
import org.self.selfscript.functions.general.EXISTS;
import org.self.selfscript.functions.general.FUNCTION;
import org.self.selfscript.functions.general.GET;
import org.self.selfscript.functions.hex.BITCOUNT;
import org.self.selfscript.functions.hex.BITGET;
import org.self.selfscript.functions.hex.BITSET;
import org.self.selfscript.functions.hex.CONCAT;
import org.self.selfscript.functions.hex.LEN;
import org.self.selfscript.functions.hex.OVERWRITE;
import org.self.selfscript.functions.hex.REV;
import org.self.selfscript.functions.hex.SETLEN;
import org.self.selfscript.functions.hex.SUBSET;
import org.self.selfscript.functions.number.ABS;
import org.self.selfscript.functions.number.CEIL;
import org.self.selfscript.functions.number.DEC;
import org.self.selfscript.functions.number.FLOOR;
import org.self.selfscript.functions.number.INC;
import org.self.selfscript.functions.number.MAX;
import org.self.selfscript.functions.number.MIN;
import org.self.selfscript.functions.number.POW;
import org.self.selfscript.functions.number.SIGDIG;
import org.self.selfscript.functions.number.SQRT;
import org.self.selfscript.functions.sha.PROOF;
import org.self.selfscript.functions.sha.SHA2;
import org.self.selfscript.functions.sha.SHA3;
import org.self.selfscript.functions.sigs.CHECKSIG;
import org.self.selfscript.functions.sigs.MULTISIG;
import org.self.selfscript.functions.sigs.SIGNEDBY;
import org.self.selfscript.functions.state.PREVSTATE;
import org.self.selfscript.functions.state.SAMESTATE;
import org.self.selfscript.functions.state.STATE;
import org.self.selfscript.functions.string.REPLACE;
import org.self.selfscript.functions.string.REPLACEFIRST;
import org.self.selfscript.functions.string.SUBSTR;
import org.self.selfscript.functions.txn.input.GETINADDR;
import org.self.selfscript.functions.txn.input.GETINAMT;
import org.self.selfscript.functions.txn.input.GETINID;
import org.self.selfscript.functions.txn.input.GETINTOK;
import org.self.selfscript.functions.txn.input.SUMINPUTS;
import org.self.selfscript.functions.txn.input.VERIFYIN;
import org.self.selfscript.functions.txn.output.GETOUTADDR;
import org.self.selfscript.functions.txn.output.GETOUTAMT;
import org.self.selfscript.functions.txn.output.GETOUTKEEPSTATE;
import org.self.selfscript.functions.txn.output.GETOUTTOK;
import org.self.selfscript.functions.txn.output.SUMOUTPUTS;
import org.self.selfscript.functions.txn.output.VERIFYOUT;
import org.self.selfscript.values.Value;

/**
 * @author Spartacus Rex
 *
 */
public abstract class SelfFunction {
	
	/**
	 * A list of all the available functions
	 */
	public static SelfFunction[] ALL_FUNCTIONS = 
			{ 
				new CONCAT(), new LEN(), new REV(),new SUBSET(), new GET(), new EXISTS(), new ADDRESS(),
				new BOOL(), new HEX(), new NUMBER(), new STRING(),new ASCII(),new UTF8(),
				new ABS(), new CEIL(), new FLOOR(),new MAX(), new MIN(), new DEC(), new INC(), 
				new SIGDIG(), new POW(), new SQRT(), new FUNCTION(),
				new SUMINPUTS(),new SUMOUTPUTS(), new SETLEN(),
				new REPLACE(),new REPLACEFIRST(), new SUBSTR(), new OVERWRITE(), 
				new SHA2(), new SHA3(), new PROOF(),
				new BITSET(), new BITGET(), new BITCOUNT(),
				new SIGNEDBY(), new MULTISIG(), new CHECKSIG(),
				new GETINADDR(), new GETINAMT(), new GETINID(), new GETINTOK(),new VERIFYIN(),
				new GETOUTADDR(), new GETOUTAMT(), new GETOUTTOK(),new GETOUTKEEPSTATE(), new VERIFYOUT(),
				new STATE(), new PREVSTATE(), new SAMESTATE()
			};
	
	/**
	 * The name used to refer to this function in RamScript. 
	 */
	private String mName;
	
	/**
	 * The Parameters
	 */
	ArrayList<Expression> mParameters;
	
	/**
	 * 
	 */
	public SelfFunction(String zName) {
		//Function names are always Uppercase
		mName = zName.toUpperCase();
		
		//Blank the parameters
		mParameters = new ArrayList<>();
	}
	
	public void addParameter(Expression zParam) {
		mParameters.add(zParam);
	}
	
	public Expression getParameter(int zParamNum) throws ExecutionException {
		if(zParamNum>=getParameterNum()) {
			throw new ExecutionException("Parameter missing for "+getName()+" num:"+zParamNum);
		}
		return mParameters.get(zParamNum);
	}
	
	public int getParameterNum(){
		return mParameters.size();
	}
	
	public ArrayList<Expression> getAllParameters(){
		return mParameters;
	}
	
	public String getName() {
		return mName;
	}
	
	protected void checkIsOfType(Value zValue, int zType) throws ExecutionException {
		if((zValue.getValueType() & zType) == 0) {
			throw new ExecutionException("Parameter is incorrect type in "+getName()
			+" Found:"+Value.getValueTypeString(zValue.getValueType())
			+" @ "+zValue.toString());
		}
	}
	
	protected void checkExactParamNumber(int zNumberOfParams) throws ExecutionException {
		if(getAllParameters().size() != zNumberOfParams) {
			throw new ExecutionException("Function requires "+zNumberOfParams+" parameters");
		}
	}
	
	protected void checkMinParamNumber(int zMinNumberOfParams) throws ExecutionException {
		if(getAllParameters().size() < zMinNumberOfParams) {
			throw new ExecutionException("Function requires minimum of "+zMinNumberOfParams+" parameters");
		}
	}
	
	/**
	 * Run it. And return a Value. 
	 * @return
	 */
	public abstract Value runFunction(Contract zContract) throws ExecutionException;

	/**
	 * Return a new copy of this function
	 * @return
	 */
	public abstract SelfFunction getNewFunction();
	
	/**
	 * How many Parameters do you expect
	 */
	public abstract int requiredParams();
	
	/**
	 * Can be overridden in calsses that set a minimum
	 * @return
	 */
	public boolean isRequiredMinimumParameterNumber() {
		return false;
	}
	
	/**
	 * External function to do a quick check
	 */
	public void checkParamNumberCorrect() throws SelfParseException {
		int paramsize = getAllParameters().size();
		int reqparam  = requiredParams();
		
		if(isRequiredMinimumParameterNumber()) {
			if(paramsize < reqparam) {
				throw new SelfParseException(getName()+" function requires a  minimum of "+reqparam+" parameters not "+paramsize);
			}
		}else {
			if(paramsize != reqparam) {
				throw new SelfParseException(getName()+" function requires exactly "+reqparam+" parameters not "+paramsize);
			}
		}
	}
	
	/**
	 * Get a specific function given it's name
	 * 
	 * @param zFunction
	 * @return the Function
	 * @throws SelfParseException
	 */
	public static SelfFunction getFunction(String zFunction) throws SelfParseException{
		//Cycle through all the functions - find the right one..
		for(SelfFunction func : SelfFunction.ALL_FUNCTIONS) {
			//Check it..
			if(func.getName().equalsIgnoreCase(zFunction)) {
				return func.getNewFunction();
			}
		}
		
		throw new SelfParseException("Invalid Function : "+zFunction);
	}
	
}
