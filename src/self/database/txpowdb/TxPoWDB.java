package org.self.database.txpowdb;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.self.database.SelfDB;
import org.self.database.txpowdb.onchain.TxPoWOnChainDB;
import org.self.database.txpowdb.ram.RamDB;
import org.self.database.txpowdb.ram.RamData;
import org.self.database.txpowdb.sql.TxPoWSqlDB;
import org.self.objects.TxPoW;
import org.self.objects.base.MiniData;
import org.self.system.SELFSystem;
import org.self.system.brains.TxPoWSearcher;
import org.self.utils.SelfLogger;
import org.self.utils.messages.Message;

/**
 * The Main TxPoW store for the whole app
 * 
 * TxPoW are added and kept for a certain period. 
 * 
 * That period is up to the Node runner. The node won't need the txpow 
 * after a day but may choose to keep them all for longer periods.
 * 
 * Helpful for resyncing clients as they come to the network.
 * 
 * Store long term in SQL but kept in RAM for short term fast access..
 * 
 */
public class TxPoWDB {

	/**
	 * The RAM DB
	 */
	RamDB mRamDB;
	
	/**
	 * The SQL DB cache of all the txpow
	 */
	TxPoWSqlDB mSqlDB;
	
	/**
	 * The store of which transactions are ONCHAIN
	 */
	TxPoWOnChainDB mOnChainDB;
	
	public TxPoWDB() {
		mRamDB 		= new RamDB();
		
		//The SQL DBs
		mSqlDB 		= new TxPoWSqlDB();
		mOnChainDB	= new TxPoWOnChainDB();
	}
	
	public void loadSQLDB(File zFile) throws SQLException {
		//Set the SQL DB base file
		mSqlDB.loadDB(zFile);
		
		//Create a subfolder for the onchain data
		File onchainfile = new File(zFile.getParentFile(),"onchain");
		mOnChainDB.loadDB(onchainfile);
	}
	
	public void hardCloseSQLDB() {
		mSqlDB.hardCloseDB();
		mOnChainDB.hardCloseDB();
	}
	
	public void saveDB(boolean zCompact) {
		//Shut down the SQL DB cleanly
		mSqlDB.saveDB(zCompact);
		mOnChainDB.saveDB(zCompact);
	}
	
	/**
	 * Add a TxPoW to the Database - both RAM and SQL
	 */
	public boolean addTxPoW(TxPoW zTxPoW) {
		//Get the ID
		String txpid = zTxPoW.getTxPoWID();
		
		//Post a message to MAIN to store in MySQL..
		try {
			SELFSystem.getInstance().PostMessage(new Message(SELFSystem.SELF_AUTOBACKUP_TXPOW).addObject("txpow", zTxPoW));
		}catch(Exception exc) {
			SelfLogger.log("STORESQl TxPoW : "+exc.toString());
		}
		
		//Do we have it already..
		if(!mRamDB.exists(txpid)) {

			//Add it to the RAM
			mRamDB.addTxPoW(zTxPoW);
		}
		
		//Is it in the SQL
		if(!mSqlDB.exists(txpid)) {
			
			//Is this TxPoW relevant
			boolean relevant = TxPoWSearcher.checkTxPoWRelevant(zTxPoW, SelfDB.getDB().getWallet());
			
			//Add it to the SQL..
			mSqlDB.addTxPoW(zTxPoW, relevant);
			
			return relevant;
		}
		
		//Check if relevant
		return TxPoWSearcher.checkTxPoWRelevant(zTxPoW, SelfDB.getDB().getWallet());
	}
	
	public void addSQLTxPoW(TxPoW zTxPoW) {
		//Is it in the SQL
		if(!mSqlDB.exists(zTxPoW.getTxPoWID())) {
			
			//Is this TxPoW relevant
			boolean relevant = TxPoWSearcher.checkTxPoWRelevant(zTxPoW, SelfDB.getDB().getWallet());
			
			//Add it to the SQL..
			mSqlDB.addTxPoW(zTxPoW,relevant);
		}
	}
	
	/**
	 * Find a specific TxPoW
	 */
	public TxPoW getTxPoW(String zTxPoWID) {
		//First check the fast RAM DB
		TxPoW txp = mRamDB.getTxPoW(zTxPoWID);
		
		//An Old TxPoW?
		if(txp == null) {
			//Check the SQL..
			txp = mSqlDB.getTxPoW(zTxPoWID);
		}
		
		//Could still be null
		return txp;
	}
	
	public ArrayList<TxPoW> getAllTxPoW(ArrayList<String> zTxPoWID) {
		ArrayList<TxPoW> ret = new ArrayList<>();
		
		//Cycle through the list
		for(String child : zTxPoWID) {
			
			//Could be in RAM already
			TxPoW txp = getTxPoW(child);
			
			//only add if valid..
			if(txp != null) {
				ret.add(txp);
			}
		}
		
		return ret;
	}

	/**
	 * Do we have this TxPoW - no need to load it. just check if we have it
	 * 
	 * @param zTxPoWID
	 * @return
	 */
	public boolean exists(String zTxPoWID) {
		//Is it in RAM
		boolean exists = mRamDB.exists(zTxPoWID);
		
		//If not check the SQL
		if(!exists) {
			exists = mSqlDB.exists(zTxPoWID);
		}
		
		return exists;
	}
	
	/**
	 * Find the children of a TxPoW ( ONLY TxPoW BLocks)
	 */
	public ArrayList<TxPoW> getChildBlocks(String zParentTxPoWID){
		//Ask the SQL for all the children first..
		ArrayList<String> children = mSqlDB.getChildBlocks(zParentTxPoWID);
		
		//Now get all of those..
		return getAllTxPoW(children);
	}
	
	/**
	 * How big is the DB
	 */
	public int getRamSize() {
		return mRamDB.getSize();
	}
	
	public int getSqlSize() {
		return mSqlDB.getSize();
	}
	
	public File getSqlFile() {
		return mSqlDB.getSQLFile();
	}
	
	public TxPoWSqlDB getSQLDB() {
		return mSqlDB;
	}
	
	public TxPoWOnChainDB getOnChainDB() {
		return mOnChainDB;
	}
	
	/**
	 * Remove OLD TxPoWs from the DB - no longer needed..
	 * 
	 * When you access a txpow it's record is updated 
	 * and will not be deleted for another time period 
	 */
	public void cleanDBRAM() {
		mRamDB.cleanDB();
	}
	
	public void wipeDBRAM() {
		mRamDB.wipeRamDB();
	}
	
	public void cleanDBSQL() {
		mSqlDB.cleanDB();
		mOnChainDB.cleanDB();
	}
	
	/**
	 * MEMPOOL specific functions
	 */
	public void clearMainChainTxns() {
		mRamDB.clearMainChainTxns();
	}
	
	public void setOnMainChain(String zTxPoWID) {
		mRamDB.setOnMainChain(zTxPoWID);
	}
	
	public void setInCascade(String zTxPoWID) {
		mRamDB.setInCascade(zTxPoWID);
	}
	
	public ArrayList<TxPoW> getAllUnusedTxns(){
		return mRamDB.getAllUnusedTxns();
	}
	
	/**
	 * Remove a TxPoW from the RamDB (Mempool)
	 */
	public void removeMemPoolTxPoW(String zTxPoWID) {
		mRamDB.remove(zTxPoWID);
	}
	
	/**
	 * Check for a certain CoinID - double spend
	 */
	public boolean checkMempoolCoins(MiniData zCoinID) {
		return mRamDB.checkForCoinID(zCoinID);
	}
	
	public ConcurrentHashMap<String, RamData> getCompleteMemPool(){
		return mRamDB.getCompleteMemPool();
	}
}
