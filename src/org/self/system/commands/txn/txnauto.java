package org.self.system.commands.txn;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.mmr.MMRProof;
import org.self.database.txpowdb.TxPoWDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.database.userprefs.txndb.TxnDB;
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
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.system.commands.CommandRunner;
import org.self.system.params.GlobalParams;
import org.self.utils.json.JSONObject;

public class txnauto extends Command {

	public txnauto() {
		super("txnauto","[id:] [amount:] [address:] (tokenid:) (sign:) (burn:) - Create a transaction automatically");
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"id","amount","address","tokenid","sign","burn"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		
		JSONObject ret = getJSONReply();
		
		TxnDB db  = SelfDB.getDB().getCustomTxnDB();
		
		//Params
		String id 			= getParam("id");
		String address 		= getAddressParam("address");
		MiniNumber amount 	= getNumberParam("amount");
		String tokenid 		= getAddressParam("tokenid", "0x00");
		boolean sign 		= getBooleanParam("sign",false);
		
		//Get the BURN
		MiniNumber burn 	= getNumberParam("burn",MiniNumber.ZERO);
		if(burn.isMore(MiniNumber.ZERO) && !tokenid.equals("0x00")) {
			throw new CommandException("Currently BURN on precreated transactions only works for Self.. tokenid:0x00.. not tokens.");
		}
		
		if(db.getTransactionRow(id) != null) {
			throw new CommandException("Txn with this ID already exists : "+id);
		}
		
		db.createTransaction(id);
		
		//Add the mounts..
		String command = "txnaddamount id:"+id+" burn:"+burn+" address:"+address+" amount:"+amount+" tokenid:"+tokenid;
		JSONObject result = CommandRunner.getRunner().runSingleCommand(command); 
		if(!(boolean)result.get("status")) {
			
			//Delete the txn..
			db.deleteTransaction(id);
			
			//Not enough funds!
			throw new CommandException(result.getString("error"));
		}
		
		//Now sort the scripts and MMR
		TxnRow txnrow = db.getTransactionRow(id); 
		
		//Get the Transaction
		Transaction trans = txnrow.getTransaction();
		Witness wit		  = txnrow.getWitness();
		
		//Set the MMR data and Scripts
		txnutils.setMMRandScripts(trans, wit);
	
		//Do we sign..
		if(sign) {
			command = "txnsign id:"+id+" publickey:auto";
			result = CommandRunner.getRunner().runSingleCommand(command); 
			if(!(boolean)result.get("status")) {
				
				//Delete the txn..
				db.deleteTransaction(id);
				
				//Not enough funds!
				throw new CommandException(result.getString("error"));
			}
		}
		
		//Output the current trans..
		ret.put("response", db.getTransactionRow(id).toJSON());
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new txnauto();
	}

	public static TxnRow createTransaction(String zAddress, MiniNumber zAmount) throws CommandException {
		
		//The Full Txn..
		TxnRow txnrow = new TxnRow("temp", new Transaction(), new Witness());
		
		//Get the DBs
		TxPoWDB txpdb 		= SelfDB.getDB().getTxPoWDB();
		TxPoWMiner txminer 	= SELFSystem.getInstance().getTxPoWMiner();
		Wallet walletdb 	= SelfDB.getDB().getWallet();
		TxPoWTreeNode tip 	= SelfDB.getDB().getTxPoWTree().getTip();
		
		//How much are we sending.. What are we Burning..
		MiniNumber sendamount 	= zAmount;
		
		//Check valid - for Self..
		if(!sendamount.isValidSelfValue()) {
			throw new CommandException("Invalid Self amount to send.. "+sendamount.toString());
		}
		
		//Lets build a transaction..
		ArrayList<Coin> relcoins = TxPoWSearcher.getRelevantUnspentCoins(tip,"0x00",true);
		
		//The current total
		MiniNumber currentamount 	= MiniNumber.ZERO;
		ArrayList<Coin> currentcoins = new ArrayList<>();
		
		//Now cycle through..
		for(Coin coin : relcoins) {
			
			String coinidstr = coin.getCoinID().to0xString();
		
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
		
		//Compute the correct CoinID
		TxPoWGenerator.precomputeTransactionCoinID(transaction);
		
		//Calculate the TransactionID..
		transaction.calculateTransactionID();
		
		//Now that we have constructed the transaction - lets sign it..
		for(String pubk : reqsigs) {

			//Use the wallet..
			Signature signature = walletdb.signData(pubk, transaction.getTransactionID());
			
			//Add it..
			witness.addSignature(signature);
		}
		
		return txnrow;
	}
	
}
