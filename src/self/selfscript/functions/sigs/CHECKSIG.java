package org.self.selfscript.functions.sigs;

import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.Value;
import org.self.objects.base.MiniData;
import org.self.objects.keys.Signature;
import org.self.objects.keys.TreeKey;

/**
 * @author spartacusrex
 */
public class CHECKSIG extends SelfFunction {

	public CHECKSIG() {
		super("CHECKSIG");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//This function is special and requires more CPU cycles.. 32 in all (1+31)
		zContract.incrementInstructions(31);
		
		//Get the Pbkey
		HexValue pubkey = zContract.getHexParam(0, this);
		
		//get the data
		HexValue data   = zContract.getHexParam(1, this);
		
		//Get the signature
		HexValue sig    = zContract.getHexParam(2, this);
		
		//Check it..
		MiniData pubk = pubkey.getMiniData();
		
		//Simple checks..
		if(pubk.getLength() == 0 || sig.getMiniData().getLength()==0) {
			throw new ExecutionException("Invalid ZERO length params for CHECKSIG");
		}
		
		//Create a TreeKey to check the signature
		TreeKey checker = new TreeKey();
		checker.setPublicKey(pubk);
		
		//Convert the bytes into a signature Object
		Signature signature = Signature.convertMiniDataVersion(sig.getMiniData());
		
		//Get the signed data
		MiniData sigdata = data.getMiniData();
				
		//Check it..
		boolean ok = checker.verify(sigdata,signature);
		
		return new BooleanValue(ok);
	}
	
	@Override
	public int requiredParams() {
		return 3;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new CHECKSIG();
	}

}
