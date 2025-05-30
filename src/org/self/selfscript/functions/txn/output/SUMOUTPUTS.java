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
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;

public class SUMOUTPUTS extends SelfFunction {

	public SUMOUTPUTS() {
		super("SUMOUTPUTS");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Which Token
		MiniData tokenid = zContract.getHexParam(0, this).getMiniData();
		
		//Get the Transaction
		Transaction trans = zContract.getTransaction();
		
		//The Total
		MiniNumber total = MiniNumber.ZERO;
		
		//Cycle through the inputs..
		ArrayList<Coin> outputs = trans.getAllOutputs();
		for(Coin cc : outputs) {
			
			if(cc.getTokenID().isEqual(tokenid)) {
				
				if(tokenid.isEqual(Token.TOKENID_SELF)) {
					
					//Plain Self
					total = total.add(cc.getAmount());
					
				}else {
					
					//Get the token..
					Token td = cc.getToken();
					
					//Add the scaled amount..
					total = total.add(td.getScaledTokenAmount(cc.getAmount()));
				}
			}
		}
		
		//Return the Amount
		return new NumberValue(total);
	}

	@Override
	public int requiredParams() {
		return 1;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new SUMOUTPUTS();
	}
}
