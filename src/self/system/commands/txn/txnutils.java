package org.self.system.commands.txn;

import java.util.ArrayList;

import org.self.database.SelfDB;
import org.self.database.mmr.MMRProof;
import org.self.database.txpowdb.TxPoWDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.database.userprefs.txndb.TxnRow;
import org.self.database.wallet.ScriptRow;
import org.self.database.wallet.Wallet;
import org.self.objects.Coin;
import org.self.objects.CoinProof;
import org.self.objects.ScriptProof;
import org.self.objects.Token;
import org.self.objects.Transaction;
import org.self.objects.Witness;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.objects.keys.Signature;
import org.self.system.SELFSystem;
import org.self.system.brains.TxPoWGenerator;
import org.self.system.brains.TxPoWMiner;
import org.self.system.brains.TxPoWSearcher;
import org.self.system.commands.CommandException;
import org.self.system.params.GlobalParams;

public class txnutils {

	public static void setMMRandScripts(Transaction zTransaction, Witness zWitness) throws Exception {
		setMMRandScripts(zTransaction, zWitness, true);
	}
	
	public static void setMMRandScripts(Transaction zTransaction, Witness zWitness, boolean zExitOnFail) throws Exception {
		//get the tip..
		TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
		
		//Get all the input coins..
		ArrayList<Coin> baseinputs = zTransaction.getAllInputs();
		
		//Are any of the inputs floating
		ArrayList<Coin> inputs = new ArrayList<>();
		for(Coin cc : baseinputs) {
			if(cc.getCoinID().isEqual(Coin.COINID_ELTOO)) {
			
				//Get the MOST recent coin to attach to this transaction..
				Coin floater = TxPoWSearcher.getFloatingCoin(tip, cc.getAmount(), cc.getAddress(), cc.getTokenID());	
				
				if(floater == null) {
					if(zExitOnFail) {
						throw new CommandException("Could not find valid unspent coin for "+cc.toJSON());
					}
				}else {
					inputs.add(floater);
				}
				
			}else {
				
				//Get the complete coin given the CoinID 
				//could be a pre-made coin.. so use correct MMREntry / Created 
				Coin current = TxPoWSearcher.searchCoin(cc.getCoinID());
				if(current == null) {
					if(zExitOnFail) {
						throw new CommandException("Coin with CoinID not found : "+cc.getCoinID().to0xString());
					}
				}else {
					inputs.add(current);
				}
			}
		}
		
		//Min depth of a coin
		MiniNumber minblock = MiniNumber.ZERO;
				
		//Add the inputs..
		for(Coin input : inputs) {
			//How deep
			if(input.getBlockCreated().isMore(minblock)) {
				minblock = input.getBlockCreated();
			}
		}
		
		//Get the block..
		MiniNumber currentblock = tip.getBlockNumber();
		MiniNumber blockdiff 	= currentblock.sub(minblock);
		if(blockdiff.isMore(GlobalParams.SELF_MMR_PROOF_HISTORY)) {
			blockdiff = GlobalParams.SELF_MMR_PROOF_HISTORY;
		}
		
		//Now get that Block
		TxPoWTreeNode mmrnode = tip.getPastNode(tip.getBlockNumber().sub(blockdiff));
		if(mmrnode == null) {
			//Not enough blocks..
			throw new Exception("Not enough blocks in chain to make valid MMR Proofs..");
		}
		
		//Get the main Wallet
		Wallet walletdb = SelfDB.getDB().getWallet();
		
		//Add the MMR proofs for the coins..
		for(Coin input : inputs) {
			
			//Get the proof..
			MMRProof proof = mmrnode.getMMR().getProofToPeak(input.getMMREntryNumber());
			
			//Create the CoinProof..
			CoinProof cp = new CoinProof(input, proof);
			
			//Add it to the witness data
			zWitness.addCoinProof(cp);
			
			//Add the script proofs
			String scraddress 	= input.getAddress().to0xString();
			ScriptRow srow 		= walletdb.getScriptFromAddress(scraddress);
			if(srow == null) {
				if(zExitOnFail) {
					throw new Exception("SERIOUS ERROR script missing for simple address : "+scraddress);
				}
			}else {
				ScriptProof pscr = new ScriptProof(srow.getScript());
				zWitness.addScript(pscr);
			}
		}
	}
	
	public static void setMMRandScripts(Coin zCoin, Witness zWitness) throws Exception {
		//get the tip..
		TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
		
		//Min depth of a coin
		MiniNumber minblock = MiniNumber.ZERO;
		
		//How deep
		if(zCoin.getBlockCreated().isMore(minblock)) {
			minblock = zCoin.getBlockCreated();
		}		
		
		//Get the block..
		MiniNumber currentblock = tip.getBlockNumber();
		MiniNumber blockdiff 	= currentblock.sub(minblock);
		if(blockdiff.isMore(GlobalParams.SELF_MMR_PROOF_HISTORY)) {
			blockdiff = GlobalParams.SELF_MMR_PROOF_HISTORY;
		}
		
		//Now get that Block
		TxPoWTreeNode mmrnode = tip.getPastNode(tip.getBlockNumber().sub(blockdiff));
		if(mmrnode == null) {
			//Not enough blocks..
			throw new Exception("Not enough blocks in chain to make valid MMR Proofs..");
		}
		
		//Get the main Wallet
		Wallet walletdb = SelfDB.getDB().getWallet();
			
		//Get the proof..
		MMRProof proof = mmrnode.getMMR().getProofToPeak(zCoin.getMMREntryNumber());
		
		//Create the CoinProof..
		CoinProof cp = new CoinProof(zCoin, proof);
		
		//Add it to the witness data
		zWitness.addCoinProof(cp);
		
		//Add the script proofs
		String scraddress 	= zCoin.getAddress().to0xString();
		ScriptRow srow 		= walletdb.getScriptFromAddress(scraddress);
		if(srow == null) {
			throw new CommandException("SERIOUS ERROR script missing for simple address : "+scraddress);
		}
		ScriptProof pscr = new ScriptProof(srow.getScript());
		zWitness.addScript(pscr);
	}
	
	public static TxnRow createBurnTransaction(ArrayList<String> zExcludeCoins, 
			MiniData zLinkTransactionID, MiniNumber zAmount) throws CommandException {
		return createBurnTransaction(zExcludeCoins, zLinkTransactionID, zAmount, true);
	}
		
	public static TxnRow createBurnTransaction(ArrayList<String> zExcludeCoins, 
			MiniData zLinkTransactionID, MiniNumber zAmount, boolean zSign) throws CommandException {
		
		//The Full Txn..
		TxnRow txnrow = new TxnRow("temp", new Transaction(), new Witness());
		
		//Get the DBs
		TxPoWDB txpdb 		= SelfDB.getDB().getTxPoWDB();
		TxPoWMiner txminer 	= SELFSystem.getInstance().getTxPoWMiner();
		Wallet walletdb 	= SelfDB.getDB().getWallet();
		TxPoWTreeNode tip 	= SelfDB.getDB().getTxPoWTree().getTip();
		
		//How much are we sending.. What are we Burning..
		MiniNumber sendamount 	= zAmount;
		
		//Lets build a transaction..
		ArrayList<Coin> relcoins = TxPoWSearcher.getRelevantUnspentCoins(tip,"0x00",true);
		
		//The current total
		MiniNumber currentamount 	= MiniNumber.ZERO;
		ArrayList<Coin> currentcoins = new ArrayList<>();
		
		//Now cycle through..
		for(Coin coin : relcoins) {
			
			String coinidstr = coin.getCoinID().to0xString();
		
			//Is it to be excluded..
			if(zExcludeCoins.contains(coinidstr)) {
				continue;
			}
			
			//Check if we are already using thewm in another Transaction that is being mined
			if(txminer.checkForMiningCoin(coinidstr)) {
				continue;
			}
			
			//Check if in mempool..
			if(txpdb.checkMempoolCoins(coin.getCoinID())) {
				continue;
			}
			
			//Add this coin..
			currentcoins.add(coin);
			
			//Get the actual ammount..
			currentamount = currentamount.add(coin.getAmount());
			
			//Do we have enough..
			if(currentamount.isMoreEqual(sendamount)) {
				break;
			}
		}
		
		//Did we add enough
		if(currentamount.isLess(sendamount)) {
			//Not enough funds..
			throw new CommandException("Not enough funds / coins for the burn..");
		}
		
		//What is the change..
		MiniNumber change = currentamount.sub(sendamount); 
		
		//Lets construct a txn..
		Transaction transaction 	= txnrow.getTransaction();
		Witness witness 			= txnrow.getWitness();
		
		//Min depth of a coin
		MiniNumber minblock = MiniNumber.ZERO;
				
		//Add the inputs..
		for(Coin inputs : currentcoins) {
			
			//Add this input to our transaction
			transaction.addInput(inputs);
			
			//How deep
			if(inputs.getBlockCreated().isMore(minblock)) {
				minblock = inputs.getBlockCreated();
			}
		}
		
		//Get the block..
		MiniNumber currentblock = tip.getBlockNumber();
		MiniNumber blockdiff 	= currentblock.sub(minblock);
		if(blockdiff.isMore(GlobalParams.SELF_MMR_PROOF_HISTORY)) {
			blockdiff = GlobalParams.SELF_MMR_PROOF_HISTORY;
		}
		
		//Now get that Block
		TxPoWTreeNode mmrnode = tip.getPastNode(tip.getBlockNumber().sub(blockdiff));
		if(mmrnode == null) {
			//Not enough blocks..
			throw new CommandException("Not enough blocks in chain to make valid MMR Proofs..");
		}
		
		//Create a list of the required signatures
		ArrayList<String> reqsigs = new ArrayList<>();
		
		//Add the MMR proofs for the coins..
		for(Coin input : currentcoins) {
			
			//Get the proof..
			MMRProof proof = mmrnode.getMMR().getProofToPeak(input.getMMREntryNumber());
			
			//Create the CoinProof..
			CoinProof cp = new CoinProof(input, proof);
			
			//Add it to the witness data
			witness.addCoinProof(cp);
			
			//Add the script proofs
			String scraddress 	= input.getAddress().to0xString();
			ScriptRow srow 		= walletdb.getScriptFromAddress(scraddress);
			if(srow == null) {
				throw new CommandException("SERIOUS ERROR script missing for simple address : "+scraddress);
			}
			ScriptProof pscr = new ScriptProof(srow.getScript());
			witness.addScript(pscr);
			
			//Add this address to the list we need to sign as..
			String pubkey = srow.getPublicKey();
			if(!reqsigs.contains(pubkey)) {
				reqsigs.add(pubkey);
			}
		}
		
		//Check valid - for Self..
		if(!sendamount.isValidSelfValue()) {
			throw new CommandException("Invalid Self amount to send.. "+sendamount.toString());
		}
		
		//Do we need to send change..
		if(change.isMore(MiniNumber.ZERO)) {
			//Create a new address
			ScriptRow newwalletaddress = SelfDB.getDB().getWallet().getDefaultAddress();
			MiniData chgaddress = new MiniData(newwalletaddress.getAddress());
			
			//Get the scaled token ammount..
			MiniNumber changeamount = change;
			
			//Change coin does not keep the state
			Coin changecoin = new Coin(Coin.COINID_OUTPUT, chgaddress, changeamount, Token.TOKENID_SELF, false);
			
			//And finally.. add the change output
			transaction.addOutput(changecoin);
		}
		
		//Set the Link Hash! - as this is a BURN transaction
		transaction.setLinkHash(zLinkTransactionID);
		
		//Compute the correct CoinID
		TxPoWGenerator.precomputeTransactionCoinID(transaction);
		
		//Calculate the TransactionID..
		transaction.calculateTransactionID();
		
		//Now that we have constructed the transaction - lets sign it..
		if(zSign) {
			
			//Run through the sigs
			for(String pubk : reqsigs) {
	
				//Use the wallet..
				Signature signature = walletdb.signData(pubk, transaction.getTransactionID());
				
				//Add it..
				witness.addSignature(signature);
			}
		}
		
		return txnrow;
	}
}