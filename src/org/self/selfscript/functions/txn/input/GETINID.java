package org.self.selfscript.functions.txn.input;

import java.util.ArrayList;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.Value;
import org.self.objects.Coin;
import org.self.objects.Transaction;

public class GETINID extends SelfFunction {

	public GETINID() {
		super("GETINID");
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
		
		//Use the witness data
		cc = zContract.getWitness().getAllCoinProofs().get(input).getCoin();
		
		//Return the address	
		return new HexValue(cc.getCoinID());
	}

	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new GETINID();
	}
}
