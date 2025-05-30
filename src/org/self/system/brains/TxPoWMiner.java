package org.self.system.brains;

import java.math.BigInteger;
import java.util.ArrayList;

import org.self.database.SelfDB;
import org.self.objects.Coin;
import org.self.objects.Transaction;
import org.self.objects.TxHeader;
import org.self.objects.TxPoW;
import org.self.objects.Witness;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.SELFSystem;
import org.self.system.network.self.NIOManager;
import org.self.system.network.self.NIOMessage;
import org.self.system.params.GeneralParams;
import org.self.utils.Crypto;
import org.self.utils.SelfLogger;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageProcessor;
import org.self.utils.messages.TimerMessage;

public class TxPoWMiner extends MessageProcessor {

	public static final String TXPOWMINER_MINETXPOW 	= "TXPOWMINER_MINETXPOW";
	public static final String TXPOWMINER_MINEPULSE 	= "TXPOWMINER_MINEPULSE";
	
	public static final String TXPOWMINER_TXBLOCKMINER 	= "TXPOWMINER_TXBLOCKMINER";
	
	/**
	 * The Large Byte MiniNumber to set the Header up for hashing
	 */
	private static MiniNumber START_NONCE_BYTES = new MiniNumber("100000000000000000.00000000000000000000000000000000000000001");
	
	/**
	 * A list of coins currently being mined.. to check when creating new transactions 
	 */
	ArrayList<String> mMiningCoins;
	
	public TxPoWMiner() {
		super("MINER");
		
		mMiningCoins = new ArrayList<>();
	}
	
	public void mineTxPoWAsync(TxPoW zTxPoW) {
		
		//Add these coins to our Mining list
		addMiningCoins(zTxPoW);
		
		//Hard set the Header Body hash - now we are mining it can never change
		zTxPoW.setHeaderBodyHash();
		
		//Now post a Mining message
		PostMessage(new Message(TXPOWMINER_MINETXPOW).addObject("txpow", zTxPoW).addBoolean("automine", false));
	}
	
	@Override
	protected void processMessage(Message zMessage) throws Exception {
		
		if(zMessage.isMessageType(TXPOWMINER_MINETXPOW)) {
			
			//Start time
			long timenow = System.currentTimeMillis();
			
			//Is this an automine..
			boolean automine = false;
			if(zMessage.exists("automine")) {
				automine = zMessage.getBoolean("automine");
			}
			
			//Are we logging..
			if(GeneralParams.MINING_LOGS) {
				SelfLogger.log("MINING TXPOW START auto:"+automine);
			}
			
			//Get the TxPoW
			TxPoW txpow = (TxPoW) zMessage.getObject("txpow");
			
			//Hard set the Header Body hash - now we are mining it can never change
			txpow.setHeaderBodyHash();
			
			//Set the nonce.. we make it a large size in bytes then edit those - no reserialisation
			txpow.setNonce(START_NONCE_BYTES);
			
			//Post a message.. Mining Started
			Message mining = new Message(SELFSystem.SELF_MINING);
			mining.addBoolean("starting", true);
			mining.addBoolean("automine", automine);
			mining.addObject("txpow", txpow);
			SELFSystem.getInstance().PostMessage(mining);
			
			//Get the byte data
			byte[] data = MiniData.getMiniDataVersion(txpow.getTxHeader()).getBytes();
			
			//Cycle until done..
			MiniNumber finalnonce 	= MiniNumber.ZERO;
			BigInteger newnonce 	= BigInteger.ZERO;
			while(isRunning()) {
				
				//Get a nonce to write over the data
				byte[] noncebytes = newnonce.toByteArray();
				newnonce 		  = newnonce.add(BigInteger.ONE);
				
				//Copy these into the byte array of the TxHeader 
				//start 4 numbers in so leading zero is not changed
				System.arraycopy(noncebytes, 0, data, 4, noncebytes.length);
				
				//Hash the data array
				byte[] hashedbytes = Crypto.getInstance().hashData(data);
				
				//Make into a MiniData structure
				MiniData hash = new MiniData(hashedbytes);
				
				//Have we found a valid txpow
				if(hash.isLess(txpow.getTxnDifficulty())) {
					
					//Ok read in the final data..
					MiniData finaldata = new MiniData(data);
					
					//Now convert to a TxHeader
					TxHeader txh = TxHeader.convertMiniDataVersion(finaldata);
					
					//What was the nonce..
					finalnonce = txh.mNonce;
					
					break;
				}
			}
			
			//Now set the final nonce..
			txpow.setNonce(finalnonce);
			
			//Calculate TxPoWID
			txpow.calculateTXPOWID();
			
			//Make a log..
			if(txpow.isTransaction()) {
				SelfLogger.log("ASYNC Transaction Mined : "+txpow.getTxPoWID());
			}
			
			//Post a message.. Mining Finished
			Message miningend = new Message(SELFSystem.SELF_MINING);
			miningend.addBoolean("starting", false);
			mining.addBoolean("automine", automine);
			miningend.addObject("txpow", txpow);
			SELFSystem.getInstance().PostMessage(miningend);
			
			//Post it on..
			SELFSystem.getInstance().PostMessage(new Message(SELFSystem.SELF_TXPOWMINED).addObject("txpow", txpow));
		
			//Remove the coins from our mining list
			removeMiningCoins(txpow);
			
			//Was this an AUTOMINE message
			if(automine) {
				//Post another mine message
				PostTimerMessage(new TimerMessage(SELFSystem.getInstance().AUTOMINE_TIMER, TXPOWMINER_MINEPULSE));
				
				//Post a message for the txblock miners - give this message time to be processed
				long timerdelay = SELFSystem.getInstance().AUTOMINE_TIMER / 2;
				PostTimerMessage(new TimerMessage(timerdelay, TXPOWMINER_TXBLOCKMINER));
			}
			
			//Are we logging..
			if(GeneralParams.MINING_LOGS) {
				//Time diff..
				long timediff = System.currentTimeMillis() - timenow;
				
				SelfLogger.log("MINING TXPOW FINISHED time:"+timediff);
			}
			
		}else if(zMessage.isMessageType(TXPOWMINER_MINEPULSE)) {
			
			//This is not where you get your mining block from..
			if(GeneralParams.TXBLOCK_NODE) {
				return;
			}
			
			//Do we have any blocks yet
			if(SelfDB.getDB().getTxPoWTree().getTip() != null) {
				
				//Get the current TxPoW
				TxPoW txpow = TxPoWGenerator.generateTxPoW(new Transaction(), new Witness());
				
				//Mine a TxPow..
				PostMessage(new Message(TXPOWMINER_MINETXPOW).addObject("txpow", txpow).addBoolean("automine", true));
			}else {
				
				//Check again in 30 secs
				PostTimerMessage(new TimerMessage(30000, TXPOWMINER_MINEPULSE));
			}
			
		}else if(zMessage.isMessageType(TXPOWMINER_TXBLOCKMINER)) {
			
			//This is not where you get your mining block from..
			if(GeneralParams.TXBLOCK_NODE) {
				return;
			}
			
			//This message is sent for those who are txblock only..
			TxPoW txpow = TxPoWGenerator.generateTxPoW(new Transaction(), new Witness());
			
			//Post to the TxBlock crew..
			NIOManager.sendNetworkMessageAll(NIOMessage.MSG_TXBLOCKMINE, txpow);
		}
	}

	/**
	 * Add these coins to a list so we know we can;t use them when creating a transaction..
	 */
	private void addMiningCoins(TxPoW zTxPoW) {
		
		//Get all the coins..
		if(!zTxPoW.getTransaction().isEmpty()) {
			ArrayList<Coin> inputs = zTxPoW.getTransaction().getAllInputs();
			for(Coin cc : inputs) {
				String coinid = cc.getCoinID().to0xString();
				if(!mMiningCoins.contains(coinid)) {
					mMiningCoins.add(coinid);
				}
			}
		}
		
		if(!zTxPoW.getBurnTransaction().isEmpty()) {
			ArrayList<Coin> inputs = zTxPoW.getBurnTransaction().getAllInputs();
			for(Coin cc : inputs) {
				String coinid = cc.getCoinID().to0xString();
				if(!mMiningCoins.contains(coinid)) {
					mMiningCoins.add(coinid);
				}
			}
		}
	}
	
	private void removeMiningCoins(TxPoW zTxPoW) {
		
		//Get all the coins..
		if(!zTxPoW.getTransaction().isEmpty()) {
			ArrayList<Coin> inputs = zTxPoW.getTransaction().getAllInputs();
			for(Coin cc : inputs) {
				mMiningCoins.remove(cc.getCoinID().to0xString());
			}
		}
		
		if(!zTxPoW.getBurnTransaction().isEmpty()) {
			ArrayList<Coin> inputs = zTxPoW.getBurnTransaction().getAllInputs();
			for(Coin cc : inputs) {
				mMiningCoins.remove(cc.getCoinID().to0xString());
			}
		}
	}
	
	public boolean checkForMiningCoin(String zCoinID) {
		return mMiningCoins.contains(zCoinID);
	}
	
	/**
	 * Mine a TxPoW - Used to Mine Maxima Messages
	 */
	public boolean MineMaxTxPoW(boolean zMaxima, TxPoW zTxPoW, long zTimeLimit) {
		return MineMaxTxPoW(zMaxima, zTxPoW, zTimeLimit, true);
	}
	
	public boolean MineMaxTxPoW(boolean zMaxima, TxPoW zTxPoW, long zTimeLimit, boolean zPost) {
		
		//What is the time..
		long timenow = System.currentTimeMillis();
		
		//Add these coins to our Mining list
		addMiningCoins(zTxPoW);
		
		//Are we logging..
		if(GeneralParams.MINING_LOGS && zMaxima) {
			SelfLogger.log("MINING MAXIMA START");
		}
		
		//Hard set the Header Body hash - now we are mining it can never change
		zTxPoW.setHeaderBodyHash();
		
		//Set the nonce.. we make it a large size in bytes then edit those - no reserialisation
		zTxPoW.setNonce(START_NONCE_BYTES);
		
		//Get the byte data
		byte[] data = MiniData.getMiniDataVersion(zTxPoW.getTxHeader()).getBytes();
		
		//Cycle until done..
		MiniNumber finalnonce 	= MiniNumber.ZERO;
		BigInteger newnonce 	= BigInteger.ZERO;
		int counter				= 0;
		while(true) {
			
			//Get a nonce to write over the data
			byte[] noncebytes = newnonce.toByteArray();
			newnonce 		  = newnonce.add(BigInteger.ONE);
			
			//Copy these into the byte array of the TxHeader 
			//start 4 numbers in so leading zero is not changed
			System.arraycopy(noncebytes, 0, data, 4, noncebytes.length);
			
			//Hash the data array
			byte[] hashedbytes = Crypto.getInstance().hashData(data);
			
			//Make into a MiniData structure
			MiniData hash = new MiniData(hashedbytes);
			
			//Have we found a valid txpow
			if(hash.isLess(zTxPoW.getTxnDifficulty())) {
				
				//Ok read in the final data..
				MiniData finaldata = new MiniData(data);
				
				//Now convert to a TxHeader
				TxHeader txh = TxHeader.convertMiniDataVersion(finaldata);
				
				//What was the nonce..
				finalnonce = txh.mNonce;
				
				break;
			}
			
			//Check time yet..
			counter++;
			if(counter>10000) {
				long timediff = System.currentTimeMillis() - timenow;
				if(timediff > zTimeLimit) {
						
					//No good - took too long..
					return false;
				}
				
				//Reset
				counter = 0;
			}
		}
		
		//Now set the final nonce..
		zTxPoW.setNonce(finalnonce);
		
		//Calculate TxPoWID
		zTxPoW.calculateTXPOWID();
		
		//Make a log..
		if(zTxPoW.isTransaction()) {
			SelfLogger.log("SYNC Transaction Mined : "+zTxPoW.getTxPoWID());
		}
		
		//Post it on..
		if(zPost) {
			SELFSystem.getInstance().PostMessage(new Message(Main.MAIN_TXPOWMINED_SELF).addObject("txpow", zTxPoW));
		}
		
		//Remove the coins from our mining list
		removeMiningCoins(zTxPoW);
		
		//Are we logging..
		if(GeneralParams.MINING_LOGS && zMaxima) {
			//Time diff..
			long timediff = System.currentTimeMillis() - timenow;
			
			SelfLogger.log("MINING MAXIMA FINISHED : "+timediff);
		}
		
		//Found it..
		return true;
	}
	
	/**
	 * Calculate the Hash rate of this node...
	 */
	public static MiniNumber calculateHashRateOld(MiniNumber zHashes) {
		
		int ihashes = zHashes.getAsInt();
		
		long timestart = System.currentTimeMillis();
		MiniData data = MiniData.getRandomData(512);
		for(int i=0;i<ihashes;i++) {
			data = Crypto.getInstance().hashObject(data);
		}
		long timediff = System.currentTimeMillis() - timestart;
		if(timediff<=0) {
			timediff = 1;
		}
		
		MiniNumber timesecs = new MiniNumber(timediff).div(MiniNumber.THOUSAND);
		
		MiniNumber spd 		= zHashes.div(timesecs);
		
//		SelfLogger.log("OLD Method) Did "+ihashes+" in "+timesecs+ " speed:"+spd);
		
		return spd;
	}
	
	/**
	 * How fast can you hash a TxPoW
	 * @param zHashes
	 * @return
	 */
	public static MiniNumber calculateHashSpeed(MiniNumber zHashes) {
		
		//How many hashes to attempt
		int ihashes = zHashes.getAsInt();
		
		//What is the time..
		long timenow = System.currentTimeMillis();
		
		TxPoW txp = new TxPoW();
		
		//Hard set the Header Body hash - now we are mining it can never change
		txp.setHeaderBodyHash();
		
		//Set the nonce.. we make it a large size in bytes then edit those - no reserialisation
		txp.setNonce(START_NONCE_BYTES);
		
		//Get the byte data
		byte[] data = MiniData.getMiniDataVersion(txp.getTxHeader()).getBytes();
		
		//Cycle until done..
		MiniNumber finalnonce 	= MiniNumber.ZERO;
		BigInteger newnonce 	= BigInteger.ZERO;
		int counter				= 0;
		for(int i=0;i<ihashes;i++) {
			
			//Get a nonce to write over the data
			byte[] noncebytes = newnonce.toByteArray();
			newnonce 		  = newnonce.add(BigInteger.ONE);
			
			//Copy these into the byte array of the TxHeader 
			//start 2 numbers in so leading zero is not changed
			System.arraycopy(noncebytes, 0, data, 4, noncebytes.length);
			
			//Hash the data array
			byte[] hashedbytes = Crypto.getInstance().hashData(data);
			
			//Make into a MiniData structure
			MiniData hash = new MiniData(hashedbytes);
		}
		
		//Time diff..
		long timediff = System.currentTimeMillis() - timenow;
		if(timediff<=0) {
			timediff = 1;
		}
		
		MiniNumber timesecs = new MiniNumber(timediff).div(MiniNumber.THOUSAND);
		
		MiniNumber spd 		= zHashes.div(timesecs);
		
//		SelfLogger.log("NEW Method) Did "+ihashes+" in "+timesecs+ " speed:"+spd);
		
		return spd;
	}
	
	public static void main(String[] zArgs) {
		
		calculateHashRateOld(new MiniNumber(10000));
		
		//First method..
		calculateHashRateOld(MiniNumber.MILLION);
		
		calculateHashSpeed(MiniNumber.MILLION);
		
	}
}
