package org.self.objects;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.self.database.mmr.MMR;
import org.self.database.mmr.MMREntry;
import org.self.database.mmr.MMREntryNumber;
import org.self.database.mmr.MMRProof;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.utils.SelfLogger;
import org.self.utils.Streamable;

public class TxBlock implements Streamable {

	/**
	 * The main TxPoW block
	 */
	TxPoW mTxPoW;

	/**
	 * The MMR Peaks from the previous block
	 */
	ArrayList<MMREntry> mPreviousPeaks = new ArrayList<>();
	
	/**
	 * The Proofs of all the input-spent coins - unspent as of the last block
	 */
	ArrayList<CoinProof> mSpentCoins = new ArrayList<>();
	
	/**
	 * A list of all the newly created coins
	 */
	ArrayList<Coin> mNewCoins = new ArrayList<>();
	
	/**
	 * When notifying about coins.. always send the state - even if you don't storte it
	 * Keep a record here - this is not stored / saved for later
	 */
	Hashtable<String, ArrayList<StateVariable>> mRemovedStates = new Hashtable<>();
	
	private TxBlock() {}
	
	//For Tests
	public TxBlock(TxPoW zTxPoW) {
		//Main Block
		mTxPoW = zTxPoW;
	}
	
	public TxBlock(MMR zParentMMR, TxPoW zTxPoW, ArrayList<TxPoW> zAllTrans) {
		//Main Block
		mTxPoW = zTxPoW;
		
		//Get the Previous Peaks..
		mPreviousPeaks = zParentMMR.getPeaks();
		
		//Make a new child MMR that you can play with..
		MMR copymmr = new MMR(zParentMMR);
		
		//Cycle through the Main Block TxPoW
		calculateCoins(copymmr,zTxPoW);
	
		//Now cycle through the txns in the block MUST BE THE CORRECT ORDER for MMR root
		ArrayList<MiniData> txns = zTxPoW.getBlockTransactions();
		for(MiniData txn : txns) {
			
			//Get the correct txpow - the order could be wrong in ther function param
			TxPoW txp = getTxpoWFromList(txn, zAllTrans);
			if(txp == null) {
				throw new IllegalArgumentException("SERIOUS ERROR : TxBlock creation with missing txns.. "+zTxPoW.getTxPoWID()+" missing "+txn); 
			}
			
			//And now process that
			calculateCoins(copymmr,txp);
		}
	}
	
	/**
	 * Get the transactions in the correct order
	 */
	private TxPoW getTxpoWFromList(MiniData zTxPoWID, ArrayList<TxPoW> zAllTrans) {
		for(TxPoW txp : zAllTrans) {
			if(txp.getTxPoWIDData().isEqual(zTxPoWID)) {
				return txp;
			}
		}
		
		return null;
	}
	
	/**
	 * Calculate the MMR for both the main and burn transactions
	 */
	private void calculateCoins(MMR zPreviousMMR, TxPoW zTxPoW) {
		//First check the main transaction
		calculateCoins(zPreviousMMR, zTxPoW.getTransaction(), zTxPoW.getWitness());
		
		//And now the Burn Transaction
		calculateCoins(zPreviousMMR, zTxPoW.getBurnTransaction(), zTxPoW.getBurnWitness());
	}
	
	private void calculateCoins(MMR zPreviousMMR, Transaction zTransaction, Witness zWitness) {
		
		//Could be an empty BURN transaction
		if(zTransaction.isEmpty()) {
			return;
		}
		
		//Get all the input coins
		ArrayList<CoinProof> coinspent = zWitness.getAllCoinProofs();
		
		//And now get all the proofs pointing to the previous block
		for(CoinProof csp : coinspent) {
			//Get the Coin
			Coin coin = csp.getCoin();
			
			//Get the ENTRY NUmber..
			MMREntryNumber entry = coin.getMMREntryNumber();
		
			//Add this to the MMR - so we can get a proof..
			zPreviousMMR.updateEntry(entry, csp.getMMRProof(), csp.getMMRData());
			
			//The Proof - from the previous block
			MMRProof proof = zPreviousMMR.getProofToPeak(entry);
		
			//Construct the CoinProof
			CoinProof cp = new CoinProof(coin, proof);
			
			//Add to the list..
			mSpentCoins.add(cp);
		}
		
		//The state of this Txn
		ArrayList<StateVariable> txnstate = zTransaction.getCompleteState();
		
		//All the Outputs..
		ArrayList<Coin> outputs = zTransaction.getAllOutputs();
		if(coinspent.size()>0) {
			//Get the First Coin in the Txn CoinID.. Genesis Transaction is Different
			MiniData basecoinid = null; 
			if(zPreviousMMR.getBlockTime().isEqual(MiniNumber.ONE)) {
				
				//Because the first address is different this is always unique
				basecoinid = zTransaction.getTransactionID();
			}else {
				
				//First coinid
				basecoinid = coinspent.get(0).getCoin().getCoinID();
			}
		
			//All the new coins
			int num=0;
			for(Coin newoutput : outputs) {
				
				//Calculate the Correct CoinID for this coin.. 
				MiniData coinid = zTransaction.calculateCoinID(basecoinid,num);
				
				//Create a new coin with correct coinid
				Coin correctcoin = newoutput.getSameCoinWithCoinID(coinid);
				
				//Set the correct state variables
				if(correctcoin.storeState()) {
					correctcoin.setState(txnstate);
				}else {
					//Keep a copy for notify events..
					mRemovedStates.put(coinid.to0xString(), txnstate);
				}
				
				//Is this a create token output..
				if(newoutput.getTokenID().isEqual(Token.TOKENID_CREATE)) {
					
					//Get the Create token details..
					Token creator = newoutput.getToken();
					
					//Get the details..
					Token newtoken = new Token(	coinid, 
												creator.getScale(), 
												newoutput.getAmount(), 
												creator.getName(),
												creator.getTokenScript(),
												mTxPoW.getBlockNumber()); 
					
					//Set it..
					correctcoin.resetTokenID(newtoken.getTokenID());
					
					//And set that as the token..
					correctcoin.setToken(newtoken);
				}
				
				//Add to our list
				mNewCoins.add(correctcoin);
				
				//Next coin down
				num++;
			}
		}
	}
	
	public TxPoW getTxPoW() {
		return mTxPoW;
	}
	
	public ArrayList<MMREntry> getPreviousPeaks(){
		return mPreviousPeaks;
	}
	
	public ArrayList<CoinProof> getInputCoinProofs(){
		return mSpentCoins;
	}
	
	public ArrayList<Coin> getOutputCoins(){
		return mNewCoins;
	}
	
	public ArrayList<StateVariable> removedState(String zCoinID) {
		if(mRemovedStates.containsKey(zCoinID)) {
			return mRemovedStates.get(zCoinID);
		}
		
		return null;
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		mTxPoW.writeDataStream(zOut);
		
		MiniNumber.WriteToStream(zOut, mPreviousPeaks.size());
		for(MMREntry entry : mPreviousPeaks) {
			entry.writeDataStream(zOut);
		}
		
		MiniNumber.WriteToStream(zOut, mSpentCoins.size());
		for(CoinProof cp : mSpentCoins) {
			cp.writeDataStream(zOut);
		}
		
		MiniNumber.WriteToStream(zOut, mNewCoins.size());
		for(Coin cc : mNewCoins) {
			cc.writeDataStream(zOut);
		}
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mPreviousPeaks 	= new ArrayList<>();
		mSpentCoins		= new ArrayList<>();
		mNewCoins		= new ArrayList<>();
		
		mTxPoW 			= TxPoW.ReadFromStream(zIn);
		
		int len = MiniNumber.ReadFromStream(zIn).getAsInt();
		for(int i=0;i<len;i++) {
			mPreviousPeaks.add(MMREntry.ReadFromStream(zIn));
		}
		
		len = MiniNumber.ReadFromStream(zIn).getAsInt();
		for(int i=0;i<len;i++) {
			mSpentCoins.add(CoinProof.ReadFromStream(zIn));
		}
		
		len = MiniNumber.ReadFromStream(zIn).getAsInt();
		for(int i=0;i<len;i++) {
			mNewCoins.add(Coin.ReadFromStream(zIn));
		}
	}
	
	public static TxBlock ReadFromStream(DataInputStream zIn) throws IOException {
		TxBlock sb = new TxBlock();
		sb.readDataStream(zIn);
		return sb;
	}
	
	/**
	 * Convert a MiniData version into a TxBlock
	 */
	public static TxBlock convertMiniDataVersion(MiniData zTxpData) {
		ByteArrayInputStream bais 	= new ByteArrayInputStream(zTxpData.getBytes());
		DataInputStream dis 		= new DataInputStream(bais);
		
		TxBlock sync = null;
		
		try {
			//Convert data into a TxPoW
			sync = TxBlock.ReadFromStream(dis);
		
			dis.close();
			bais.close();
			
		} catch (IOException e) {
			SelfLogger.log(e);
		}
		
		return sync;
	}
}
