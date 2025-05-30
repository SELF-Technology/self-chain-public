package org.self.selfscript.functions.number;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.Value;
import org.self.objects.base.MiniNumber;

public class POW extends SelfFunction {

	public POW() {
		super("POW");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		NumberValue exp 	= zContract.getNumberParam(0, this);
		NumberValue number 	= zContract.getNumberParam(1, this);
		
		MiniNumber actnum = exp.getNumber();
		if(!actnum.floor().isEqual(actnum)) {
			throw new ExecutionException("POW must be to a whole Number");
		}
		
		//Check within limits..
		if(actnum.abs().isMoreEqual(MiniNumber.THOUSAND24)) {
			throw new ExecutionException("ABS POW exponent must be less than 1024");
		}
		
		//Only works for WHOLE numbers..
		return new NumberValue(number.getNumber().pow(actnum.getAsInt()));
	}
	
	@Override
	public int requiredParams() {
		return 2;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new POW();
	}
	
	public static void main(String[] zArgs) {
		
		MiniNumber ww 	= new MiniNumber("0.01");
		
		long timenow = System.currentTimeMillis();
		for(int i=0;i<1000;i++) {
			MiniNumber pow 	= ww.pow(1000);
			System.out.println(pow);
		}
		long timediff = System.currentTimeMillis() - timenow;
		
		System.out.println("Time : "+timediff);
		
	}
}