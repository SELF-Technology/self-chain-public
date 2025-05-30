package org.self.selfscript.functions.txn.output;

import java.util.ArrayList;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.Value;
import org.self.objects.Coin;
import org.self.objects.Token;
import org.self.objects.Transaction;

public class GETOUTAMT extends SelfFunction {

	public GETOUTAMT() {
		super("GETOUTAMT");
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
		
		//Is it a Token..
		if(!cc.getTokenID().isEqual(Token.TOKENID_SELF)) {
			//Get the Multiple..
			Token td = cc.getToken();
			if(td == null) {
				throw new ExecutionException("No Token for Output Coin @ "+output+" "+cc.getToken());
			}
			
			//Return the scaled amount
			return new NumberValue(td.getScaledTokenAmount(cc.getAmount()));
		}
		
		//Return the Amount
		return new NumberValue(cc.getAmount());
	}

	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new GETOUTAMT();
	}
}
