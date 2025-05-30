package org.self.selfscript.functions.txn.input;

import java.util.ArrayList;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.Value;
import org.self.objects.Coin;
import org.self.objects.Token;
import org.self.objects.Transaction;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;

/**
 * Verify that the specified output exists in the transaction.
 * @author spartacusrex
 *
 */
public class VERIFYIN extends SelfFunction{

	public VERIFYIN() {
		super("VERIFYIN");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Which Output
		int input = zContract.getNumberParam(0, this).getNumber().getAsInt();
		
		//Get the details
		MiniData address  = new MiniData(zContract.getHexParam(1, this).getRawData());
		MiniNumber amount = zContract.getNumberParam(2, this).getNumber();
		MiniData tokenid  = new MiniData(zContract.getHexParam(3, this).getRawData());
		
		//Check an output exists..
		Transaction trans = zContract.getTransaction();
	
		//Check input exists..
		ArrayList<Coin> ins = trans.getAllInputs();
		if(input<0 || ins.size()<=input) {
			throw new ExecutionException("Input number out of range "+input+"/"+ins.size());
		}
		
		//Get it..
		Coin cc = ins.get(input);
		
		//Now Check
		boolean addr = address.isEqual(cc.getAddress());  
		boolean tok  = tokenid.isEqual(cc.getTokenID());  
		
		//Amount
		MiniNumber inamt = cc.getAmount();
		
		//Could be a token Amount!
		if(!cc.getTokenID().isEqual(Token.TOKENID_SELF)) {
			//Get the Token
			Token td = cc.getToken();
			if(td == null) {
				throw new ExecutionException("No token specified @ Input coin "+input+" "+cc.getTokenID());
			}
			
			inamt = td.getScaledTokenAmount(cc.getAmount());
		}
		
		//Check Amount
		boolean amt  = inamt.isEqual(amount);
		
		//Return if all true
		return new BooleanValue( addr && amt && tok );
	}
	
	@Override
	public int requiredParams() {
		return 4;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new VERIFYIN();
	}

}
