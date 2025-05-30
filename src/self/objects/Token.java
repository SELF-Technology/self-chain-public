package org.self.objects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.objects.base.MiniString;
import org.self.utils.Crypto;
import org.self.utils.SelfLogger;
import org.self.utils.Streamable;
import org.self.utils.json.JSONObject;

public class Token implements Streamable{
	
	/**
	 * Token create requires a special tokeni
	 */
	public static final MiniData TOKENID_CREATE = new MiniData("0xFF");
	
	/**
	 * The Self Coin has s special TokenID
	 */
	public static final MiniData TOKENID_SELF = new MiniData("0x00");
	
	/**
	 * The CoinID used when creating the token initially
	 */
	protected MiniData  mCoinID;
	
	/**
	 * The Scale of the Token vs the amount
	 */
	protected MiniNumber mTokenScale;
	
	/**
	 * The total amount of Self Used
	 */
	protected MiniNumber mTokenSelfAmount;
	
	/**
	 * The Token Name / Description.. can be a string / JSON
	 */
	protected MiniString mTokenName;
	
	/**
	 * The Token Script
	 */
	protected MiniString mTokenScript;
	
	/**
	 * The Block this Token was created in
	 */
	protected MiniNumber mTokenCreated;
	
	/**
	 * TokenID created after all the details are set
	 */
	protected MiniData mTokenID;
	
	/**
	 * Blank Constructor for ReadDataStream
	 */
	protected Token() {}
	
	/**
	 * The Only Public Constructor
	 * @param zCoindID
	 * @param zScale
	 * @param zSelfAmount
	 * @param zName
	 */
	public Token(MiniData zCoindID, MiniNumber zScale, MiniNumber zSelfAmount, MiniString zName, MiniString zTokenScript) {
		this(zCoindID, zScale, zSelfAmount, zName, zTokenScript, MiniNumber.ZERO);
	}
	
	public Token(MiniData zCoindID, MiniNumber zScale, MiniNumber zSelfAmount, MiniString zName, MiniString zTokenScript, MiniNumber zCreated) {
				
		mCoinID 			= zCoindID;
		mTokenName 			= zName;
		mTokenScale 		= zScale;
		mTokenSelfAmount 	= zSelfAmount;
		mTokenScript        = new MiniString(zTokenScript.toString()) ;
		mTokenCreated		= zCreated;
		
		calculateTokenID();
	}
	
	public MiniNumber getScaledTokenAmount(MiniNumber zSelfAmount) {
		int scale = mTokenScale.getAsInt();
		MiniNumber current = zSelfAmount;
		for(int i=0;i<scale;i++){
			current = current.mult(MiniNumber.TEN);
		}
		return current;
	}
	
	public MiniNumber getScaledSelfAmount(MiniNumber zTokenAmount) {
		int scale = mTokenScale.getAsInt();
		MiniNumber current = zTokenAmount;
		for(int i=0;i<scale;i++){
			current = current.div(MiniNumber.TEN);
		}
		return current;
	}
	
	public MiniNumber getScale() {
		return mTokenScale;
	}
	
	public MiniNumber getAmount() {
		return mTokenSelfAmount;
	}
	
	public MiniNumber getTotalTokens() {
		return getScaledTokenAmount(mTokenSelfAmount);
	}
	
	public MiniNumber getDecimalPlaces() {
		return new MiniNumber(MiniNumber.MAX_DECIMAL_PLACES - getScale().getAsInt());
	}
	
	public MiniString getName() {
		return mTokenName;
	}
	
	public MiniString getTokenScript() {
		return mTokenScript;
	}
	
	public MiniData getCoinID() {
		return mCoinID;
	}
	
	public MiniNumber getCreated() {
		return mTokenCreated;
	}
	
	public MiniData getTokenID() {
		return mTokenID;
	}
	
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		
		//Is Token Name a JSON
		if(mTokenName.toString().trim().startsWith("{")) {
			obj.put("name", mTokenName);
		}else {
			
			//It's just a String
			obj.put("name", mTokenName.toString());
		}
		
		obj.put("coinid", mCoinID.to0xString());
		obj.put("total", getTotalTokens().toString());
		obj.put("decimals", getDecimalPlaces());
		obj.put("script", mTokenScript.toString());
		obj.put("totalamount", mTokenSelfAmount.toString());
		obj.put("scale", mTokenScale.toString() );
		obj.put("created", mTokenCreated.toString());
		
		if(mTokenID == null) {
			obj.put("tokenid", null);
		}else {
			obj.put("tokenid", mTokenID.to0xString());
		}
		
		
		return obj;
	}
	
	private void calculateTokenID() {
		try {
			//Make it the HASH ( CoinID | Total Amount )
			ByteArrayOutputStream baos 	= new ByteArrayOutputStream();
			DataOutputStream daos 		= new DataOutputStream(baos);
			
			//Write the details to the stream
			writeDataStream(daos);
			
			//Push It
			daos.flush();
			
			//Create a MiniData..
			MiniData tokdat = new MiniData(baos.toByteArray());
			
			//Now Hash it..
			mTokenID = Crypto.getInstance().hashObject(tokdat);
			
			//Clean up
			daos.close();
			baos.close();
			
		} catch (Exception e) {
			SelfLogger.log(e);
		}
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		mCoinID.writeHashToStream(zOut);
		mTokenScript.writeDataStream(zOut);
		mTokenScale.writeDataStream(zOut);
		mTokenSelfAmount.writeDataStream(zOut);
		mTokenName.writeDataStream(zOut);
		mTokenCreated.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mCoinID 			= MiniData.ReadHashFromStream(zIn);
		mTokenScript        = MiniString.ReadFromStream(zIn);
		mTokenScale 		= MiniNumber.ReadFromStream(zIn);
		mTokenSelfAmount	= MiniNumber.ReadFromStream(zIn);
		mTokenName 			= MiniString.ReadFromStream(zIn);
		mTokenCreated		= MiniNumber.ReadFromStream(zIn);
		
		calculateTokenID();
	}
	
	/**
	 * Convert a MiniData version into a Token
	 */
	public static Token convertMiniDataVersion(MiniData zTxpData) {
		ByteArrayInputStream bais 	= new ByteArrayInputStream(zTxpData.getBytes());
		DataInputStream dis 		= new DataInputStream(bais);
		
		Token tok = null;
		
		try {
			//Convert data into a TxPoW
			tok = Token.ReadFromStream(dis);
		
			dis.close();
			bais.close();
			
		} catch (IOException e) {
			SelfLogger.log(e);
		}
		
		return tok;
	}
	
	public static Token ReadFromStream(DataInputStream zIn) throws IOException{
		Token td = new Token();
		td.readDataStream(zIn);
		return td;
	}
}
