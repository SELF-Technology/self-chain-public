package org.self.selfscript.functions.txn.output;

import java.util.ArrayList;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.Value;
import org.self.objects.Coin;
import org.self.objects.Transaction;

public class GETOUTTOK extends SelfFunction {

	public GETOUTTOK() {
		super("GETOUTTOK");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Which Output - must be from 0-255
		int output = zContract.getNumberParam(0, this).getNumber().getAsInt();
		
		//Get the Transaction
		Transaction trans = zContract.getTransaction();
		
		//Check output exists..
		ArrayList<Coin> outs = trans.getAllOutputs();
		if(output<0 || outs.size()<=output) {
			throw new ExecutionException("Output out of range "+output+"/"+outs.size());
		}
		
		//Get it..
		Coin cc = outs.get(output);
		
		//Return the address	
		return new HexValue(cc.getTokenID());
	}

	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new GETOUTTOK();
	}
}
