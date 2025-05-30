package org.self.database.userprefs.txndb;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.self.objects.Coin;
import org.self.objects.Transaction;
import org.self.objects.Witness;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniString;
import org.self.system.brains.TxPoWGenerator;
import org.self.utils.SelfLogger;
import org.self.utils.Streamable;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;

public class TxnRow implements Streamable {

	public String mID;
	public Transaction 	mTransaction;
	public Witness 		mWitness;
	
	private TxnRow() {}
	
	public TxnRow(String zID, Transaction zTransaction, Witness zWitness) {
		mID 			= zID;
		mTransaction 	= zTransaction;	
		mWitness		= zWitness;	
	}
	
	public void setID(String zID) {
		mID = zID;
	}
	
	public String getID() {
		return mID;
	}
	
	public Transaction getTransaction() {
		return mTransaction;
	}
	
	public Witness getWitness() {
		return mWitness;
	}
	
	public void clearWitness() {
		mWitness = new Witness();
	}
	
	public JSONObject toJSON() {
		return toJSON(true);
	}
	
	public JSONObject toJSON(boolean zShowWitness) {
		JSONObject ret = new JSONObject();
		ret.put("id", mID);
		
		//Calculate the correct CoinID - if possible..
		TxPoWGenerator.precomputeTransactionCoinID(mTransaction);
		
		//Now output
		ret.put("transaction", mTransaction.toJSON());
		
		if(zShowWitness) {
			ret.put("witness", mWitness.toJSON());
			
			//Now output the full output coins with correct coinid
			ArrayList<Coin> coinidcoins = mTransaction.getAllOutputs();
			JSONArray coinarr 		= new JSONArray();
			for(Coin cc : coinidcoins) {
				coinarr.add(MiniData.getMiniDataVersion(cc).to0xString());
			}
			ret.put("outputcoindata", coinarr);
		}
		
		return ret;		
	}

	/**
	 * Convert a MiniData version into a TxnRow
	 */
	public static TxnRow convertMiniDataVersion(MiniData zTxpData) {
		ByteArrayInputStream bais 	= new ByteArrayInputStream(zTxpData.getBytes());
		DataInputStream dis 		= new DataInputStream(bais);
		
		TxnRow txnrow = null;
		
		try {
			//Convert data
			txnrow = TxnRow.ReadFromStream(dis);
		
			dis.close();
			bais.close();
			
		} catch (IOException e) {
			SelfLogger.log(e);
		}
		
		return txnrow;
	}
	
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		MiniString.WriteToStream(zOut, mID);
		mTransaction.writeDataStream(zOut);
		mWitness.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mID = MiniString.ReadFromStream(zIn).toString();
		
		mTransaction = new Transaction();
		mTransaction.readDataStream(zIn);
		
		mWitness = new Witness();
		mWitness.readDataStream(zIn);
	}
	
	public static TxnRow ReadFromStream(DataInputStream zIn) throws IOException {
		TxnRow txp = new TxnRow();
		txp.readDataStream(zIn);
		return txp;
	}
}
