package org.self.selfscript.functions.number;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.Value;
import org.self.objects.base.MiniNumber;

public class SIGDIG extends SelfFunction {

	public SIGDIG() {
		super("SIGDIG");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		NumberValue significantdigits 	= zContract.getNumberParam(0, this);
		NumberValue number 				= zContract.getNumberParam(1, this);
		
		MiniNumber actnum = significantdigits.getNumber();
		if(!actnum.floor().isEqual(actnum)) {
			throw new ExecutionException("SIGDIG precision must be to a whole Number");
		}
		
		if(significantdigits.getNumber().isLess(MiniNumber.ZERO)) {
			throw new ExecutionException("SIGDIG precision must be a positive whole number : "+significantdigits);
		}
		
		return new NumberValue(number.getNumber().setSignificantDigits(significantdigits.getNumber().getAsInt()));
	}
	
	@Override
	public int requiredParams() {
		return 2;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new SIGDIG();
	}
}