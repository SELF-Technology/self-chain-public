package org.self.selfscript.functions.string;

import java.util.regex.Pattern;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;

public class REPLACEFIRST extends SelfFunction {

	public REPLACEFIRST() {
		super("REPLACEFIRST");
	}

	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());

		//Get the the first string
		StringValue strmain   	= zContract.getStringParam(0, this);
		StringValue strsearch 	= zContract.getStringParam(1, this);
		StringValue strrepl 	= zContract.getStringParam(2, this);

		String main 	= strmain.toString();
		
		String search 	= strsearch.toString();
		search 			= Pattern.quote(search);
		
		String repl 	= strrepl.toString();

		//Now replace..
		String newstr = main.replaceFirst(search, repl);
		
		return new StringValue(newstr);	
	}

	@Override
	public int requiredParams() {
		return 3;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new REPLACEFIRST();
	}
}
