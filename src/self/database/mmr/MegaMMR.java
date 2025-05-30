package org.self.database.mmr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import org.self.objects.Coin;
import org.self.objects.CoinProof;
import org.self.objects.Token;
import org.self.objects.TxBlock;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.params.GeneralParams;
import org.self.utils.MiniFile;
import org.self.utils.MiniFormat;
import org.self.utils.SelfLogger;
import org.self.utils.Streamable;

public class MegaMMR implements Streamable {

	//The MMR
	MMR mMMR;
	
	//All the UNSPENT Coins
	Hashtable<String,Coin> mAllUnspentCoins;
	
	public MegaMMR() {
			
		//The actual MMR
		mMMR = new MMR();
		
		//The Unspent Coins
		mAllUnspentCoins = new Hashtable<>();
	}
	
	public MMR getMMR() {
		return mMMR;
	}
	
	public Hashtable<String, Coin> getAllCoins(){
		return mAllUnspentCoins;
	}
	
	public boolean isEmpty() {
		return mMMR.getAllEntries().size() == 0;
	}
	
	/**
	 * Convert the TxBlock 
	 */
	public void addBlock(TxBlock zBlock) {
		
		//What Block Time Are we..
		MiniNumber block = zBlock.getTxPoW().getBlockNumber();
		mMMR.setBlockTime(block);
		
		//Add all the peaks..
		ArrayList<MMREntry> peaks = zBlock.getPreviousPeaks();
		for(MMREntry peak : peaks) {
			mMMR.setEntry(peak.getRow(), peak.getEntryNumber(), peak.getMMRData());
		}
		
		//Calculate the Entry Number
		mMMR.calculateEntryNumberFromPeaks();
		
		//Now you have all the previous peaks.. update the spent coins..
		ArrayList<CoinProof> spentcoins = zBlock.getInputCoinProofs();
		for(CoinProof input : spentcoins) {
			
			//Which entry is this in the MMR
			MMREntryNumber entrynumber = input.getCoin().getMMREntryNumber();
			
			//A NEW MMRData of the spent coin
			Coin spentcoin = input.getCoin().deepCopy();
			spentcoin.setSpent(true);

			//Create the MMRData
			MMRData mmrdata = MMRData.CreateMMRDataLeafNode(spentcoin, MiniNumber.ZERO);
						
			//Update the MMR
			mMMR.updateEntry(entrynumber, input.getMMRProof(), mmrdata);
			
			//Remove from all coins..
			mAllUnspentCoins.remove(input.getCoin().getCoinID().to0xString());
		}
		
		//And ADD all the newly created coins
		ArrayList<Coin> outputs = zBlock.getOutputCoins();
		for(Coin output : outputs) {
			
			//Where are we in the MMR
			MMREntryNumber entrynumber = mMMR.getEntryNumber();
			
			//Create a new CoinMMR structure - unspent..
			Coin newcoin = output.deepCopy();
			newcoin.setMMREntryNumber(entrynumber);
			newcoin.setBlockCreated(block);
			newcoin.setSpent(false);
			
			//Create the MMRData
			MMRData mmrdata = MMRData.CreateMMRDataLeafNode(newcoin, newcoin.getAmount());
			
			//Is it Unspendable
			mmrdata.mUnspendable = isPrunable(newcoin);
			
			//And add to the MMR
			mMMR.addEntry(mmrdata);	
			
			//Add to the total List of coins for this block
			mAllUnspentCoins.put(newcoin.getCoinID().to0xString(), newcoin);
		}
		
		//Check values are correct..
		MiniData mroot = mMMR.getRoot().getData();
		MiniData broot = zBlock.getTxPoW().getMMRRoot();
		if(!mroot.isEqual(broot)) {
			SelfLogger.log("[!] MEGAMMR ROOT AND TXBLOCK ROOT DONT MATCH @ "+zBlock.getTxPoW().getBlockNumber());
		}
	}
	
	/**
	 * Can we PRUNE this coin - may even check a custom list.. ?
	 */
	private boolean isPrunable(Coin zCoin) {
		if(GeneralParams.MEGAMMR_MEGAPRUNE) {
			
			//Check for spendable coin address
			if(zCoin.getAddress().getLength() != 32) {
				return true;
			}
			
			//Does it have a state
			if(GeneralParams.MEGAMMR_MEGAPRUNE_STATE) {
				if(zCoin.getState().size() > 0) {
					return true;
				}
			}
			
			//Is it a token other than Self
			if(GeneralParams.MEGAMMR_MEGAPRUNE_TOKENS) {
				if(!zCoin.getTokenID().isEqual(Token.TOKENID_SELF)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Scan the MMR for UN-spendable coins
	 */
	private void scanUnspendable() {
		
		Enumeration<Coin> coins = mAllUnspentCoins.elements();
		while(coins.hasMoreElements()) {
			Coin cc = coins.nextElement();
			
			//get it..
			MMREntry ment = mMMR.getEntry(0, cc.getMMREntryNumber());
				
			//Set the MMRData
			if(!ment.isEmpty()) {
				ment.getMMRData().setUnspendable(isPrunable(cc));
			}else {
				SelfLogger.log("[!] Coin with no MMREntry in MegaMMR! @ "+cc.toString());
			}
		}
	}
	
	/**
	 * Scan the WHOLE tree for unspendable coins..
	 */
	private static boolean PRUNE_LOGS = false;
	private void pruneUnspendable(boolean zScanMMR) {
		
		//Time how long it takes..
		long timestart 	= System.currentTimeMillis();
		if(PRUNE_LOGS) {
			SelfLogger.log("Start Prune MegaMMR Coins:"+mAllUnspentCoins.size()+" MMREntries:"+getMMR().getTotalEntries());
		}
		 
		//Do we need to scan
		if(zScanMMR) {
			scanUnspendable();
		}
		
		//Scan the MMR..
		mMMR.scanUnspendableTree();
		
		//Create a copy with the correct list
		Hashtable<String,Coin> newAllCoins = new Hashtable<>();
				
		//First scan ALL the coins..
		Enumeration<Coin> coins = mAllUnspentCoins.elements();
		while(coins.hasMoreElements()) {
			Coin cc = coins.nextElement();
			
			//What entry is this
			MMREntryNumber entry = cc.getMMREntryNumber();
			
			//Is this PRUNED.. if not add to NEW list
			if(!mMMR.getPrunedUnspendableCoins().contains(entry.toString())) {
				newAllCoins.put(cc.getCoinID().to0xString(), cc);
			}
		}
		
		//And now reset the list..
		mAllUnspentCoins = newAllCoins;
		
		if(PRUNE_LOGS) {
			long timediff = System.currentTimeMillis() - timestart;
			SelfLogger.log("Final Pruned MegaMMR Coins:"+mAllUnspentCoins.size()+" MMREntries:"+getMMR().getTotalEntries()
							+" time:"+timediff+"ms");
		}
	}
	
	
	/**
	 * Wipe the data
	 */
	public void clear() {
		mMMR 				= new MMR();
		mAllUnspentCoins 	= new Hashtable<>();
	}
	
	public void loadMMR(File zFile) {
		SelfLogger.log("Loading MegaMMR size : "+MiniFormat.formatSize(zFile.length()));
		MiniFile.loadObjectSlow(zFile, this);
	}
	
	public void saveMMR(File zFile) {
		MiniFile.saveObjectDirect(zFile, this);
		if(PRUNE_LOGS) {
			SelfLogger.log("Saving MegaMMR size : "+MiniFormat.formatSize(zFile.length()));
		}
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		
		//Are we pruning the unspendable coins
		if(GeneralParams.MEGAMMR_MEGAPRUNE) {
			pruneUnspendable(false);
		}
		
		//First write out the VERSION
		MiniNumber.WriteToStream(zOut, 1);
		
		//Now the MMR
		mMMR.writeDataStream(zOut);
		
		//And now all the coins..
		int size = mAllUnspentCoins.size();
		MiniNumber.WriteToStream(zOut, size);
		
		Enumeration<Coin> coins = mAllUnspentCoins.elements();
		while(coins.hasMoreElements()) {
			Coin cc = coins.nextElement();
			cc.writeDataStream(zOut);
		}
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		int version = MiniNumber.ReadFromStream(zIn).getAsInt();
		
		//Read in the MMR..
		mMMR = new MMR();
		mMMR.readDataStream(zIn);
		mMMR.setFinalized(false);
		
		//And now all the coins
		mAllUnspentCoins = new Hashtable<>();
		int size = MiniNumber.ReadFromStream(zIn).getAsInt();
		for(int i=0;i<size;i++) {
			Coin cc = Coin.ReadFromStream(zIn);
			
			//Do we prune it..
			mAllUnspentCoins.put(cc.getCoinID().to0xString(), cc);
		}
		
		//Are we pruning the unspendable coins
		if(GeneralParams.MEGAMMR_MEGAPRUNE) {
			pruneUnspendable(true);
		}else {
			//Just Scan it..
			scanUnspendable();
		}
	}
	
	
	public static void main(String[] zArgs) {
		
		MegaMMR mega 	= new MegaMMR();
		MMR mmr 		= mega.getMMR();
		
		int coinnum =10;
		
		for(int i=0;i<coinnum;i++) {
			
			//Create a coin..
			MiniData address = MiniData.getRandomData(32);
			//if(i>2 && i<6) {
				address = MiniData.ZERO_TXPOWID;
			//}
			MiniNumber amount 	= MiniNumber.ONE;
			
			Coin cc = new Coin(MiniData.getRandomData(32), address, amount, Token.TOKENID_SELF);
			cc.setMMREntryNumber(mmr.getEntryNumber());
			
			//Create the MMRData
			MMRData mmrdata = MMRData.CreateMMRDataLeafNode(cc, amount);
			
			//HACK Add it 
			mega.getMMR().addEntry(mmrdata);
			
			//And to the COIn List..
			mega.getAllCoins().put(cc.getCoinID().to0xString(), cc);
		}
		
		MMR.printmmrtree(mega.getMMR());
		
		mega.getMMR().pruneTree();
		
		//Now scan
		mega.pruneUnspendable(true);
		
		MMR.printmmrtree(mega.getMMR());
		
		if(true) {
		//	return;
		}
		
		
		//Now scan
		mega.pruneUnspendable(true);
		
		MMR.printmmrtree(mega.getMMR());
		
		if(true) {
		//	return;
		}
		
		for(int i=0;i<coinnum;i++) {
			
			//Create a coin..
			MiniData address = MiniData.getRandomData(32);
			if(i>4 && i<9) {
				address = MiniData.ZERO_TXPOWID;
			}
			MiniNumber amount 	= MiniNumber.ONE;
			if(i<=1) {
				amount = MiniNumber.ZERO;
			}
			
			if(i==9) {
				amount = MiniNumber.ZERO;
			}
			
			Coin cc = new Coin(MiniData.getRandomData(32), address, amount, Token.TOKENID_SELF);
			cc.setMMREntryNumber(mmr.getEntryNumber());
			
			//Create the MMRData
			MMRData mmrdata = MMRData.CreateMMRDataLeafNode(cc, amount);
			
			//HACK Add it 
			mega.getMMR().addEntry(mmrdata);
			
			//And to the COIn List..
			mega.getAllCoins().put(cc.getCoinID().to0xString(), cc);
		}
		
		MMR.printmmrtree(mega.getMMR());
		
		//Now scan
		mega.pruneUnspendable(true);
		
		mega.getMMR().pruneTree();
		
		MMR.printmmrtree(mega.getMMR());
		
		//Now scan
		mega.pruneUnspendable(true);
		
		MMR.printmmrtree(mega.getMMR());
		
		
		/*//Set random values to Zero..
		for(int zz=0;zz<24;zz++) {
			int rand 				= new Random().nextInt(16);
			MMREntryNumber entry 	= new MMREntryNumber(rand);
			MMREntry ent = mmr.getEntry(0, entry);
			if(ent.isEmpty() || ent.getMMRData().getValue().isEqual(MiniNumber.ZERO)) {
				continue;
			}
			
			System.out.println("\nSet entry "+rand+" to 0");
			MMRProof proof 	= mmr.getProofToPeak(entry);
			mmr.updateEntry(entry, proof, zero);
			mmr.pruneTree();
			MMR.printmmrtree(mmr);
		}*/
	}
	
	public static MMRData getCoinData() {
		return getCoinData(MiniNumber.ZERO);
	}
	
	public static MMRData getCoinData(MiniNumber zNumber) {
		
		//Create the coin
		Coin test = new Coin(MiniData.ZERO_TXPOWID,zNumber, MiniData.ZERO_TXPOWID);
		
		//Create the MMRData
		return MMRData.CreateMMRDataLeafNode(test, zNumber); 
	}
}
