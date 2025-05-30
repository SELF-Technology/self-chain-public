package org.self.selfscript.functions.sha;

import java.io.IOException;

import org.self.database.mmr.MMRData;
import org.self.database.mmr.MMRProof;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;

public class PROOF extends SelfFunction {

	public PROOF() {
		super("PROOF");
	}
	
	@Override
	public Value runFunction(Contract zContract) throws ExecutionException {
		checkExactParamNumber(requiredParams());
		
		//Get the initial data - can be a string or HEX
		Value vv = getParameter(0).getValue(zContract);
		checkIsOfType(vv, Value.VALUE_HEX | Value.VALUE_SCRIPT);
		
		//Get the Sum value..
		NumberValue sumval = zContract.getNumberParam(1, this);
		
		MMRData mmrdata = null;
		if(vv.getValueType() == Value.VALUE_HEX) {
			//HEX
			HexValue hex 	= (HexValue)vv;
			mmrdata 		= MMRData.CreateMMRDataLeafNode(hex.getMiniData(),sumval.getNumber());

		}else {
			
			//Script..
			StringValue scr = (StringValue)vv;
			mmrdata 		= MMRData.CreateMMRDataLeafNode(scr.getMiniString(),sumval.getNumber());
		}
		
		//The root of the tree
		MMRData mmrroot = new MMRData(zContract.getHexParam(2, this).getMiniData(), 
									  zContract.getNumberParam(3, this).getNumber());
				
		//Get the proof chain 
		HexValue chain = zContract.getHexParam(4, this);
		
		//Create into the MMRProof..
		MMRProof proof = null;
		try {
			proof = MMRProof.convertMiniDataVersion(chain.getMiniData());
		} catch (IOException e) {
			//Invalid Proof..
			throw new ExecutionException("Invalid MMRProof at PROOF "+chain.getMiniData().to0xString());
		}
		
		//And calculate the final chain value..
		MMRData root = proof.calculateProof(mmrdata); 
		
		//Are they the same..
		boolean same = root.isEqual(mmrroot);
		
		//Return..
		return new BooleanValue(same);
	}
	
	@Override
	public int requiredParams() {
		return 5;
	}
	
	@Override
	public SelfFunction getNewFunction() {
		return new PROOF();
	}
}
