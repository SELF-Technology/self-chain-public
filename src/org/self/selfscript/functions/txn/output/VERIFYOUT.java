package org.self.selfscript.functions.txn.output;

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
public class VERIFYOUT extends SelfFunction{

	public VERIFYOUT() {
		super("VERIFYOUT");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		
		//Check parameters..
		int paramnum = getAllParameters().size();
		if(paramnum<4 || paramnum>5) {
			throw new ExecutionException("VERIFYOUT requires 4 or 5 parameters");
		}
		
		//Which Output
		int output = zContract.getNumberParam(0, this).getNumber().getAsInt();
		
		//Get the details
		MiniData address  = new MiniData(zContract.getHexParam(1, this).getRawData());
		MiniNumber amount = zContract.getNumberParam(2, this).getNumber();
		MiniData tokenid  = new MiniData(zContract.getHexParam(3, this).getRawData());
		
		//Are we checking KEEPSTATE
		boolean checkkeepstate 	= false;
		boolean keepstate 		= false;
		if(paramnum == 5) {
			checkkeepstate 	= true;
			keepstate = zContract.getBoolParam(4, this).isTrue();
		}
		
		//Check an output exists..
		Transaction trans = zContract.getTransaction();
		
		//Check output exists..
		ArrayList<Coin> outs = trans.getAllOutputs();
		if(output<0 || outs.size()<=output) {
			throw new ExecutionException("Output out of range "+output+"/"+outs.size());
		}
		
		//Get it..
		Coin cc = outs.get(output);
		
		//Check Keep State
		boolean samestate = true;
		if(checkkeepstate) {
			samestate = cc.storeState() == keepstate;
		}
		
		//Now Check
		boolean addr = address.isEqual(cc.getAddress());  
		boolean tok  = tokenid.isEqual(cc.getTokenID());  
		
		//The amount may need to be scaled
		MiniNumber outamt = cc.getAmount();
		
		//Could be a token Amount!
		if(!cc.getTokenID().isEqual(Token.TOKENID_SELF)) {
			//Get the token details
			Token cctok = cc.getToken();
			if(cctok == null) {
				throw new ExecutionException("No token specified @ Output coin "+output+" "+cc.getTokenID());
			}
			
			//Scale the amount
			outamt = cctok.getScaledTokenAmount(cc.getAmount());
		}
		
		//Are they equal
		boolean amt  = outamt.isEqual(amount);
		
		//If all equal..
		boolean ver = addr && amt && tok && samestate;
		
		//Log the error
		if(!ver) {
			zContract.traceLog("VERIFYOUT failed @ ouptut "+output+" found (address:"+cc.getAddress().to0xString()+" amount:"+outamt+" tokenid:"+cc.getTokenID().to0xString()+" keepstate:"+cc.storeState()+" ) "
								+"expected (address:"+address.to0xString()+" amount:"+amount+" tokenid:"+tokenid.to0xString()+" keepstate:"+keepstate+") CheckKeepState:"+checkkeepstate);
		}
		
		//Return if all true
		return new BooleanValue( ver );
	}
	
	@Override
	public boolean isRequiredMinimumParameterNumber() {
		return true;
	}
	
	@Override
	public int requiredParams() {
		return 4;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new VERIFYOUT();
	}

}
