package org.self.selfscript.functions.txn.input;

import java.util.ArrayList;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.Value;
import org.self.objects.Coin;
import org.self.objects.Token;
import org.self.objects.Transaction;

public class GETINAMT extends SelfFunction {

	public GETINAMT() {
		super("GETINAMT");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Which Output
		int input = zContract.getNumberParam(0, this).getNumber().getAsInt();
		
		//Get the Transaction
		Transaction trans = zContract.getTransaction();
		
		//Check output exists..
		ArrayList<Coin> ins = trans.getAllInputs();
		if(input<0 || ins.size()<=input) {
			throw new ExecutionException("Input number out of range "+input+"/"+ins.size());
		}
		
		//Get it..
		Coin cc = ins.get(input);
		
		//Is it a Token..
		if(!cc.getTokenID().isEqual(Token.TOKENID_SELF)) {
			//Get the Multiple..
			Token td = cc.getToken();
			if(td == null) {
				throw new ExecutionException("No Token for Input Coin @ "+input+" "+cc.getToken());
			}
			
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
		return new GETINAMT();
	}
}
