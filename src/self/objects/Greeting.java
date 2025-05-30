package org.self.objects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.self.database.SelfDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.objects.base.MiniString;
import org.self.system.params.GeneralParams;
import org.self.system.params.GlobalParams;
import org.self.utils.SelfLogger;
import org.self.utils.Streamable;
import org.self.utils.json.JSONObject;
import org.self.utils.json.parser.JSONParser;
import org.self.utils.json.parser.ParseException;

public class Greeting implements Streamable {

	/**
	 * What version of Self
	 */
	MiniString mVersion = new MiniString(GlobalParams.SELF_VERSION);
	
	/**
	 * Extra information sent in the greeting
	 */
	JSONObject mExtraData = new JSONObject();
	
	/**
	 * The block number of the top block
	 */
	MiniNumber mTopBlock = MiniNumber.ZERO;
	
	/**
	 * The hash chain of the txpow in the current chain - from top down to root of Tree
	 */
	ArrayList<MiniData> mChain = new ArrayList<>();
	
	public Greeting() {}
	
	/**
	 * Create the complete greeting message
	 */
	public Greeting createGreeting() {
		//Lock the DB
		SelfDB.getDB().readLock(true);
		
		try {
			//Add some extra info
			getExtraData().put("welcome", SelfDB.getDB().getUserDB().getWelcome());
			
			//What is my Host / Port
			if(GeneralParams.IS_HOST_SET) {
				getExtraData().put("host",GeneralParams.SELF_HOST);
			}
			getExtraData().put("port",""+GeneralParams.SELF_PORT);
			
			//Add My Maxima MLS identity
//			getExtraData().put("maximamls",SELFSystem.getInstance().getMaxima().getMaximaMLSIdentity());
			
			//Add the chain..
			TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
			if(tip == null) {
				//First time user
				setTopBlock(MiniNumber.MINUSONE);
			}else {
				setTopBlock(tip.getTxPoW().getBlockNumber());
			}
			
			//Add all the chain
			while(tip != null) {
				mChain.add(tip.getTxPoW().getTxPoWIDData());
				tip = tip.getParent();
			}
			
		}catch(Exception exc) {
			SelfLogger.log(exc);
		}
		
		//Unlock..
		SelfDB.getDB().readLock(false);
		
		return this;
	}
	
	public JSONObject getExtraData() {
		return mExtraData;
	}
	
	public String getExtraDataValue(String zKey) {
		return (String) mExtraData.get(zKey);
	}
	
	public void setTopBlock(MiniNumber zTopBlock) {
		mTopBlock = zTopBlock;
	}
	
	public MiniNumber getTopBlock() {
		return mTopBlock;
	}
	
	public MiniString getVersion() {
		return mVersion;
	}
	
	public MiniNumber getRootBlock() {
		if(mTopBlock.isEqual(MiniNumber.MINUSONE)) {
			return MiniNumber.MINUSONE;
		}
		
		//Check Upper Limit..
		if(mTopBlock.isMore(MiniNumber.TRILLION)) {
			//Something wrong here..
			SelfLogger.log("[!] Greeting TopBlock error topblock:"+mTopBlock+" ChainSize:"+mChain.size());
			return MiniNumber.MINUSONE;
		}
		
		MiniNumber rootblock = null;
		try {
			rootblock = mTopBlock.sub(new MiniNumber(mChain.size()-1));
		}catch(NumberFormatException nfe) {
			SelfLogger.log(nfe);
			SelfLogger.log("Greeting calc root error.. topblock:"+mTopBlock+" ChainSize:"+mChain.size());
		
			throw nfe;
		}
		 
		return rootblock;
	}
	
	public ArrayList<MiniData> getChain(){
		return mChain;
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		mVersion.writeDataStream(zOut);
		
		MiniString json = new MiniString(mExtraData.toString());
		json.writeDataStream(zOut);
		
		mTopBlock.writeDataStream(zOut);
		
		int len = mChain.size();
		MiniNumber.WriteToStream(zOut, len);
		for(MiniData txpowid : mChain) {
			txpowid.writeDataStream(zOut);
		}
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mVersion = MiniString.ReadFromStream(zIn);
		
		MiniString json = MiniString.ReadFromStream(zIn);
		try {
			mExtraData = (JSONObject)new JSONParser().parse(json.toString());
		} catch (ParseException e) {
			SelfLogger.log(e);
			mExtraData = new JSONObject();
		}  		
		
		mTopBlock = MiniNumber.ReadFromStream(zIn);
		
		mChain = new ArrayList<>();
		int len = MiniNumber.ReadFromStream(zIn).getAsInt();
		for(int i=0;i<len;i++) {
			mChain.add(MiniData.ReadFromStream(zIn));
		}
	}

	public static Greeting ReadFromStream(DataInputStream zIn) throws IOException{
		Greeting greet = new Greeting();
		greet.readDataStream(zIn);
		return greet;
	}
	
}
