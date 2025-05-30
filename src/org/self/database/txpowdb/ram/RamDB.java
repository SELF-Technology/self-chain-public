package org.self.database.txpowdb.ram;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.self.objects.CoinProof;
import org.self.objects.TxPoW;
import org.self.objects.base.MiniData;
import org.self.system.params.GeneralParams;

public class RamDB {

	/**
	 * How long does data remain in RAM DB in milli seconds
	 */
	public long MAX_TIME = 1000 * 60 * 60 * GeneralParams.NUMBER_HOURS_RAMTXPOWDB;
	
	ConcurrentHashMap<String, RamData> mTxPoWDB;
	
	public RamDB() {
		mTxPoWDB = new ConcurrentHashMap<>();
	}
	
	public boolean addTxPoW(TxPoW zTxPoW) {
		String txpid = zTxPoW.getTxPoWID();
		
		//Do we already have it..
		RamData curr = mTxPoWDB.get(txpid);
		if(curr!=null) {
			//Reset last access..
			curr.updateLastAccess();
			return false;
		}else{
			mTxPoWDB.put(txpid, new RamData(zTxPoW));
		}
		
		return true;
	}
	
	public ConcurrentHashMap<String, RamData> getCompleteMemPool(){
		return mTxPoWDB;
	}
	
	public boolean exists(String zTxPoWID) {
		return mTxPoWDB.containsKey(zTxPoWID);
	}
	
	public TxPoW getTxPoW(String zTxPoWID) {
		RamData curr = mTxPoWDB.get(zTxPoWID);
		if(curr!=null) {
			curr.updateLastAccess();
			return curr.getTxPoW();
		}
		return null;
	}
	
	public void remove(String zTxPoWID) {
		mTxPoWDB.remove(zTxPoWID);
	}
	
	public void cleanDB() {
		//Cut off point
		long timecut = System.currentTimeMillis() - MAX_TIME;
		
		//Routine maintenance - remove old entries..
		ConcurrentHashMap<String, RamData> newmap = new ConcurrentHashMap<>();
		
		Enumeration<RamData> alldata = mTxPoWDB.elements();
		while(alldata.hasMoreElements()) {
			RamData ram = alldata.nextElement();
			
			//Do we make it!
			if(ram.getLastAccess()>timecut) {
				TxPoW txp = ram.getTxPoW();
				
				//Reuse the RamData..
				newmap.put(ram.getTxPoW().getTxPoWID(), ram);
			}
		}
		
		//Switcheroo..
		mTxPoWDB = newmap;
	}

	public int getSize() {
		return mTxPoWDB.size();
	}

	public void wipeRamDB() {
		mTxPoWDB.clear();
	}
	
	/**
	 * MEMPOOL specific functions
	 */
	public void clearSelfChainTxns() {
		Enumeration<RamData> alldata = mTxPoWDB.elements();
		while(alldata.hasMoreElements()) {
			RamData ram = alldata.nextElement();
			ram.setOnMainChain(false);
		}
	}
	
	public void setOnMainChain(String zTxPoWID) {
		RamData curr = mTxPoWDB.get(zTxPoWID);
		if(curr!=null) {
			curr.setOnMainChain(true);
		}
	}
	
	public ArrayList<TxPoW> getAllUnusedTxns(){
		ArrayList<TxPoW> ret = new ArrayList<>();
		Enumeration<RamData> alldata = mTxPoWDB.elements();
		while(alldata.hasMoreElements()) {
			RamData ram = alldata.nextElement();
			if(!ram.isOnMainChain() && ram.getTxPoW().isTransaction() && !ram.isInCascade()) {
				ret.add(ram.getTxPoW());
			}
		}
		
		return ret;
	}

	/**
	 * Once a TxPoW goes past the root of the tree into the cascade - it cannot be added again
	 */
	public void setInCascade(String zTxPoWID) {
		RamData curr = mTxPoWDB.get(zTxPoWID);
		if(curr!=null) {
			curr.setInCascade(true);
		}
	}
	
	/**
	 * Look for double spend coins..
	 */
	public boolean checkForCoinID(MiniData zCoinID) {
		
		Enumeration<RamData> alldata = mTxPoWDB.elements();
		while(alldata.hasMoreElements()) {
			RamData ram = alldata.nextElement();
			if(ram.getTxPoW().isTransaction() && !ram.isInCascade()) {
	
				//Get the this TxPoW
				TxPoW txp = ram.getTxPoW();
				
				//Get all the input coins..
				ArrayList<CoinProof> proofs = txp.getWitness().getAllCoinProofs();
				for(CoinProof cp : proofs) {
					if(cp.getCoin().getCoinID().isEqual(zCoinID)) {
						return true;
					}
				}
				
				//And BURN Coins
				proofs = txp.getBurnWitness().getAllCoinProofs();
				for(CoinProof cp : proofs) {
					if(cp.getCoin().getCoinID().isEqual(zCoinID)) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
