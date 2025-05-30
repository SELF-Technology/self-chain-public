package org.self.objects;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;

import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.utils.Crypto;
import org.self.utils.SelfLogger;
import org.self.utils.Streamable;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;

/**
 * A transaction is a very simple structure. A list of inputs and a list of outputs. 
 * All the Witness data is Segregated..  So that there is no TXN malleability possible.
 * The CoinID of an output is the HASH ( TXN hash + Output Num ), which is ALWAYS Globally Unique.
 * 
 * @author spartacus
 *
 */
public class Transaction implements Streamable {

	/**
	 * The Hash of a prior transaction if this is a burn transaction
	 * MUST Be 0x00 to be a normal transaction.
	 * 
	 * MUST be the Hash of the Transaction if this is a Burn Transaction
	 */
	protected MiniData mLinkHash = MiniData.ZERO_TXPOWID;
	
	/**
	 * The Inputs that make up the Transaction
	 */
	protected ArrayList<Coin> mInputs  = new ArrayList<>();
	
	/**
	 * All the Outputs
	 */
	protected ArrayList<Coin> mOutputs = new ArrayList<>();
	
	/**
	 * The State values of the Transaction
	 */
	protected ArrayList<StateVariable> mState = new ArrayList<>();
	
	/**
	 * The Transaction ID
	 */
	protected MiniData mTransactionID = MiniData.ZERO_TXPOWID;
	
	/**
	 * Used internally..
	 * 
	 * Once you have checked the script you may not need top check it again..
	 */
	public boolean mHaveCheckedMonotonic 	= false;
	public boolean mIsMonotonic 			= false;
	public boolean mIsValid					= false;
	
	/**
	 * Constructor
	 */
	public Transaction() {}
	
	public void addInput(Coin zCoin) {
		mInputs.add(zCoin);
	}
	
	public void addOutput(Coin zCoin) {
		mOutputs.add(zCoin);
	}
	
	public boolean isEmpty() {
		return mInputs.size() == 0 && mOutputs.size() == 0;
	}
	
	public ArrayList<Coin> getAllInputs(){
		return mInputs;
	}
	
	public ArrayList<Coin> getAllOutputs(){
		return mOutputs;
	}
	
	public boolean isCheckedMonotonic() {
		return mHaveCheckedMonotonic && mIsMonotonic;
	}
	
	public void clearIsMonotonic() {
		mHaveCheckedMonotonic = false;
	}
	
	public MiniNumber sumInputs() {
		MiniNumber tot = MiniNumber.ZERO;
		for(Coin cc : mInputs) {
			tot = tot.add(cc.mAmount);
		}
		return tot;
	}
	
	public MiniNumber sumInputs(MiniData zTokenID) {
		MiniNumber tot = MiniNumber.ZERO;
		for(Coin cc : mInputs) {
			if(cc.getTokenID().isEqual(zTokenID)) {
				tot = tot.add(cc.mAmount);	
			}
		}
		return tot;
	}
	
	public MiniNumber sumOutputs() {
		MiniNumber tot = MiniNumber.ZERO;
		for(Coin cc : mOutputs) {
			tot = tot.add(cc.mAmount);
		}
		return tot;
	}
	
	public MiniNumber sumOutputs(MiniData zTokenID) {
		MiniNumber tot = MiniNumber.ZERO;
		for(Coin cc : mOutputs) {
			if(cc.getTokenID().isEqual(zTokenID)) {
				tot = tot.add(cc.mAmount);	
			}
		}
		return tot;
	}
	
	/**
	 * You only need to check that there are enough Inputs for the Outputs.
	 * The rest is BURN..
	 * @return
	 */
	public boolean checkValid(){
		//Basics
		int ins = mInputs.size();
		if(ins<1) {
			return false;
		}
		
		//Starters - Check total inputs is less than total outputs
		MiniNumber totalin 	= MiniNumber.ZERO;
		MiniNumber totalout = MiniNumber.ZERO;
		for(Coin cc : mInputs) {
			totalin = totalin.add(cc.getAmount());
		}
		for(Coin cc : mOutputs) {
			totalout = totalout.add(cc.getAmount());
		}
		if(totalout.isMore(totalin)) {
			SelfLogger.log("Transaction error : Inputs LESS than Outputs "+totalin+"/"+totalout);
			return false;
		}
		
		//Check that all the inputs and outputs are valid Self Values >0 - 1,000,000,000
		for(Coin cc : mInputs) {
			if(!cc.getAmount().isValidSelfValue()) {
				SelfLogger.log("Transaction error : Input is invalid Self Amount "+cc.getAmount().toString());
				return false;
			}
		}
		
		for(Coin cc : mOutputs) {
			if(!cc.getAmount().isValidSelfValue()) {
				SelfLogger.log("Transaction error : Output is invalid Self Amount "+cc.getAmount().toString());
				return false;
			}
		}
		
		//First get a list of all the Ouput tokens..
		ArrayList<String> tokens = new ArrayList<>();
		for(Coin cc : mOutputs) {
			MiniData tokenhash = cc.getTokenID();
			if(tokenhash.isEqual(Token.TOKENID_CREATE)){
				tokenhash = Token.TOKENID_SELF;
			}
			
			String tok = tokenhash.to0xString();
			if(!tokens.contains(tok)) {
				tokens.add(tok);	
			}
		}
		
		//Now get all the Output Amounts...
		Hashtable<String, MiniNumber> outamounts = new Hashtable<>();
		for(String token : tokens) {
			outamounts.put(token, sumOutputs(new MiniData(token)));
		}
		
		//Now cycle through and check there is enough inputs..
		Enumeration<String> keys = outamounts.keys();
		while(keys.hasMoreElements()) {
			//The token
			String tok = keys.nextElement();
			
			//The output total amount
			MiniNumber outamt = outamounts.get(tok);
			
			//The input total amount
			MiniNumber inamt = sumInputs(new MiniData(tok));
			
			//Do the check..
			if(inamt.isLess(outamt)) {
				SelfLogger.log("Transaction error : Inputs LESS than Outputs");
				return false;	
			}
		}
		
		//Check all the inputs have a unique CoinID..
		if(ins>1){
			for(int i=0;i<ins;i++) {
				for(int j=i+1;j<ins;j++) {
					//Get the Input
					Coin input1 = mInputs.get(i);
					Coin input2 = mInputs.get(j);
					if(input1.getCoinID().isEqual(input2.getCoinID())) {
						return false;
					}
				}
			}
		}
			
		return true;
	}
	
	/**
	 * How much Self is burnt..
	 */
	public MiniNumber getBurn() {
		//Starters - Check total inputs is less than total outputs
		MiniNumber totalin 	= MiniNumber.ZERO;
		MiniNumber totalout = MiniNumber.ZERO;
		for(Coin cc : mInputs) {
			totalin = totalin.add(cc.getAmount());
		}
		for(Coin cc : mOutputs) {
			totalout = totalout.add(cc.getAmount());
		}
		
		return totalin.sub(totalout);
	}
	
	/**
	 * Set a state value from 0-255 to a certain value
	 * MAX 255 VAUES
	 * 
	 * @param zStateNum
	 * @param zValue
	 */
	public void addStateVariable(StateVariable zValue) {
		//If it exists remove it
		removeStateVariable(zValue.getPort());
		
		//And now add it..
		mState.add(zValue);
		
		//Order the state
		Collections.sort(mState,new Comparator<StateVariable>() {
			@Override
			public int compare(StateVariable o1, StateVariable o2) {
				int s1 = o1.getPort();
				int s2 = o2.getPort();
				return Integer.compare(s1, s2);
			}
		});
	}
	
	/**
	 * Remove a State Variable
	 * @param zPort
	 */
	public void removeStateVariable(int zPort) {
		//Cycle through and add the remaining
		boolean found = false;
		ArrayList<StateVariable> newvars = new ArrayList<>();
		for(StateVariable sv : mState) {
			if(sv.getPort() != zPort){
				newvars.add(sv);
			}else {
				found = true;
			}
		}
		
		//Did we remove it..
		if(!found) {
			return;
		}
		
		//Set the new state
		mState = newvars;
		
		//Order the state
		Collections.sort(mState,new Comparator<StateVariable>() {
			@Override
			public int compare(StateVariable o1, StateVariable o2) {
				int s1 = o1.getPort();
				int s2 = o2.getPort();
				return Integer.compare(s1, s2);
			}
		});
	}
	
	/**
	 * @param zPort
	 * @return
	 */
	public StateVariable getStateValue(int zPort) {
		for(StateVariable sv : mState) {
			if(sv.getPort() == zPort){
				return sv;
			}
		}
		
		return null;
	}
	
	/**
	 * Check exists
	 * @param zStateNum
	 * @return
	 */
	public boolean stateExists(int zStateNum) {
		return getStateValue(zStateNum) != null;
	}
	
	/**
	 * Clear all the state values
	 */
	public void clearState() {
		mState.clear();
	}
	
	/**
	 * Required to cycle..
	 * @return
	 */
	public ArrayList<StateVariable> getCompleteState(){
		return mState;
	}
	
	/**
	 * The Link Hash - for the Burn Transaction
	 */
	public MiniData getLinkHash() {
		return mLinkHash;
	}
	
	public void setLinkHash(MiniData zLinkHash) {
		mLinkHash = zLinkHash;
	}
	
	/**
	 * Calculate the TransactionID
	 */
	public void calculateTransactionID() {
		mTransactionID = Crypto.getInstance().hashObject(this);
		if(mTransactionID==null) {
			throw new IllegalArgumentException("NULL mTransactionID!");
		}
	}
	
	public MiniData getTransactionID() {
		return mTransactionID;
	}
	
	/**
	 * Calculate the CoinID of an Output
	 * 
	 * CoinID is calculated for output coins as the hash of the firstcoin + output num
	 * 
	 * Always use the MMR value as may be ELTOO 0x01 in some transactions..
	 * 
	 */
	public MiniData calculateCoinID(MiniData zBaseCoinID, int zOutput) {
		return Crypto.getInstance().hashObjects(zBaseCoinID, new MiniNumber(zOutput));
	}
	
	@Override
	public String toString() {
		return toJSON().toString();
	}
	
	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		
		//Inputs
		JSONArray ins = new JSONArray();
		for(Coin in : mInputs) {
			ins.add(in.toJSON());
		}
		ret.put("inputs", ins);
		
		//Outputs
		JSONArray outs = new JSONArray();
		for(Coin out : mOutputs) {
			outs.add(out.toJSON());
		}
		ret.put("outputs", outs);
		
		//State
		outs = new JSONArray();
		for(StateVariable sv : mState) {
			outs.add(sv.toJSON());
		}
		ret.put("state", outs);
		
		ret.put("linkhash", mLinkHash.to0xString());
	
		calculateTransactionID();
		if(mTransactionID==null) {
			ret.put("transactionid", "null");
		}else {
			ret.put("transactionid", mTransactionID.to0xString());
		}
		
		return ret;
	}
	
	public long calculateStateSize() {
		ByteArrayOutputStream baos 	= new ByteArrayOutputStream();
		DataOutputStream dos 		= new DataOutputStream(baos);
		
		long ret = 1000000;
		try {
			//How many state variables..
			MiniNumber statelen = new MiniNumber(mState.size());
			statelen.writeDataStream(dos);
			for(StateVariable sv : mState) {
				sv.writeDataStream(dos);
			}
			
			dos.flush();
			
			ret = baos.toByteArray().length;
			
			dos.close();
			baos.close();
			
		}catch(Exception exc){
			SelfLogger.log("Calcualte state size error!");
			SelfLogger.log(exc);
		}
		
		return ret;
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		MiniNumber ins = new MiniNumber(mInputs.size());
		ins.writeDataStream(zOut);
		for(Coin coin : mInputs) {
			coin.writeDataStream(zOut);
		}
		
		MiniNumber outs = new MiniNumber(mOutputs.size());
		outs.writeDataStream(zOut);
		for(Coin coin : mOutputs) {
			coin.writeDataStream(zOut);
		}
		
		//How many state variables..
		MiniNumber statelen = new MiniNumber(mState.size());
		statelen.writeDataStream(zOut);
		for(StateVariable sv : mState) {
			sv.writeDataStream(zOut);
		}
		
		//The Link Hash
		mLinkHash.writeHashToStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mInputs  = new ArrayList<>();
		mOutputs = new ArrayList<>();
		mState 	 = new  ArrayList<>();
		
		//Inputs
		MiniNumber ins = MiniNumber.ReadFromStream(zIn);
		int len = ins.getAsInt();
		for(int i=0;i<len;i++) {
			Coin coin = Coin.ReadFromStream(zIn);
			mInputs.add(coin);
		}
		
		//Outputs
		MiniNumber outs = MiniNumber.ReadFromStream(zIn);
		len = outs.getAsInt();
		for(int i=0;i<len;i++) {
			Coin coin = Coin.ReadFromStream(zIn);
			mOutputs.add(coin);
		}
		
		//State Variables
		MiniNumber states = MiniNumber.ReadFromStream(zIn);
		len = states.getAsInt();
		for(int i=0;i<len;i++){
			StateVariable sv = StateVariable.ReadFromStream(zIn);
			mState.add(sv);
		}
		
		mLinkHash = MiniData.ReadHashFromStream(zIn);
		
		calculateTransactionID();
	}
}
