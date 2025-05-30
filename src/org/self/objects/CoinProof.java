package org.self.objects;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.self.database.mmr.MMRData;
import org.self.database.mmr.MMRProof;
import org.self.objects.base.MiniData;
import org.self.utils.SelfLogger;
import org.self.utils.Streamable;
import org.self.utils.json.JSONObject;

public class CoinProof implements Streamable {

	/**
	 * The Coin record in the MMR
	 */
	Coin mCoin;
	
	/**
	 * The proof of this record
	 */
	MMRProof mProof;
	
	private CoinProof() {}
	
	public CoinProof(Coin zCoin, MMRProof zProof) {
		mCoin 		= zCoin;
		mProof 		= zProof;
	}
	
	public Coin	getCoin() {
		return mCoin;
	}

	public MMRProof getMMRProof(){
		return mProof;
	}
	
	public MMRData getMMRData() {
		
//		//Get the Hash of this 
//		MiniData hash 		= Crypto.getInstance().hashObject(getCoin());
//		
//		//The Value
//		MiniNumber value 	= getCoin().getAmount();
		
//		return new MMRData(hash, value);
		return MMRData.CreateMMRDataLeafNode(getCoin(), getCoin().getAmount());
	}
	
	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		ret.put("coin", mCoin.toJSON());
		ret.put("proof", mProof.toJSON());
		return ret;
	}

	/**
	 * Convert a MiniData version into a CoinProof
	 */
	public static CoinProof convertMiniDataVersion(MiniData zTxpData) {
		ByteArrayInputStream bais 	= new ByteArrayInputStream(zTxpData.getBytes());
		DataInputStream dis 		= new DataInputStream(bais);
		
		CoinProof txnrow = null;
		
		try {
			//Convert data
			txnrow = CoinProof.ReadFromStream(dis);
		
			dis.close();
			bais.close();
			
		} catch (IOException e) {
			SelfLogger.log(e);
		}
		
		return txnrow;
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		mCoin.writeDataStream(zOut);
		mProof.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mCoin	= Coin.ReadFromStream(zIn);
		mProof 	= MMRProof.ReadFromStream(zIn);
	}
	
	public static CoinProof ReadFromStream(DataInputStream zIn) throws IOException {
		CoinProof cp = new CoinProof();
		cp.readDataStream(zIn);
		return cp;
				
	}
}
