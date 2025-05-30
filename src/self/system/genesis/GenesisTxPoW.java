package org.self.system.genesis;

import java.util.ArrayList;

import org.self.database.mmr.MMRData;
import org.self.database.mmr.MMREntryNumber;
import org.self.database.mmr.MMRProof;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.objects.Address;
import org.self.objects.Coin;
import org.self.objects.CoinProof;
import org.self.objects.ScriptProof;
import org.self.objects.Token;
import org.self.objects.Transaction;
import org.self.objects.TxBlock;
import org.self.objects.TxPoW;
import org.self.objects.Witness;
import org.self.objects.base.SELFData;
import org.self.objects.base.SELFNumber;
import org.self.system.params.GlobalParams;
import org.self.utils.Crypto;
import org.self.utils.SelfLogger;

public class GenesisTxPoW extends TxPoW {

	public GenesisTxPoW(String zGenesisAddress) {
		super();
		
		//The first BASE MMR..
		GenesisMMR genesismmr = new GenesisMMR();
		
		setTxDifficulty(Crypto.MAX_HASH);
		
		setNonce(new SELFNumber(256));
		 
		setTimeMilli(new SELFNumber(System.currentTimeMillis()));
		
		//First Block starts at 1! .. 0 created the genesis coin
		setBlockNumber(SELFNumber.ONE);
		
		setBlockDifficulty(Crypto.MAX_HASH);
		
		//Super Block Levels.. FIRST just copy them all..
		SELFData ultimateparent = new SELFData("0x00");
		for(int i=0;i<GlobalParams.SELF_CASCADE_LEVELS;i++) {
			setSuperParent(i, ultimateparent);
		}
		
		//Set the Genesis transaction
		Transaction transaction = getTransaction();
		
		//The first billion Self
		transaction.addInput(new GenesisCoin());
		
		//Now add 1 output
		Coin self = new Coin(	Coin.COINID_OUTPUT, 
								new MiniData(zGenesisAddress), 
								MiniNumber.BILLION, 
								Token.TOKENID_SELF);
		transaction.addOutput(self);
		
		//Add a coinproof..
		Witness witness = getWitness();
		
		//Get the proof..
		MMRProof proof = genesismmr.getProofToPeak(MMREntryNumber.ZERO);
		
		//Create the CoinProof..
		CoinProof cp = new CoinProof(new GenesisCoin(), proof);
		
		//Add it to the witness data
		witness.addCoinProof(cp);
		
		//And the script is Return True..
		witness.addScript(new ScriptProof(Address.TRUE_ADDRESS.getScript()));
		
		//Set the body hash - no more changes..
		setHeaderBodyHash();
		
		//Calculate the TxPOWID
		calculateTransactionID();
		
		//Create a TxBlock..
		TxBlock txblock 	= new TxBlock(genesismmr, this, new ArrayList<>());
		
		//And the MMR details
		TxPoWTreeNode node 	= new TxPoWTreeNode(txblock, false);
		
		//Get the MMR root data
		MMRData root = node.getMMR().getRoot();
		setMMRRoot(root.getData());
		setMMRTotal(root.getValue());
		
		//Set the TXPOW
		calculateTXPOWID();
		
		//Get the TxPoWID - this is a one time universal value
		String gentxpow = getTxPoWID();
		SelfLogger.log("Genesis block created : "+gentxpow);
		
		//Hard code it..
		_mIsBlockPOW = true;
		_mIsTxnPOW   = true;
	}
	
}
