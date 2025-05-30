package org.self.system.commands.send;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.self.database.SelfDB;
import org.self.database.mmr.MMRProof;
import org.self.database.txpowdb.TxPoWDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.database.userprefs.txndb.TxnRow;
import org.self.database.wallet.ScriptRow;
import org.self.database.wallet.Wallet;
import org.self.objects.Address;
import org.self.objects.Coin;
import org.self.objects.CoinProof;
import org.self.objects.ScriptProof;
import org.self.objects.StateVariable;
import org.self.objects.Token;
import org.self.objects.Transaction;
import org.self.objects.TxPoW;
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
import org.self.system.commands.backup.vault;
import org.self.system.commands.search.keys;
import org.self.system.commands.txn.txnutils;
import org.self.system.params.GeneralParams;
import org.self.system.params.GlobalParams;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;

public class send extends Command {

	public class AddressAmount {
		
		MiniData 	mAddress;
		MiniNumber 	mAmount;
		
		public AddressAmount(MiniData zAddress, MiniNumber zAmount) {
			mAddress 	= zAddress;
			mAmount		= zAmount;
		}
		
		public MiniData getAddress(){
			return mAddress;
		}
		
		public MiniNumber getAmount() {
			return mAmount;
		}
	}
	
	public send() {
		super("send","(address:Sx..|0x..) (amount:) (multi:[address:amount,..]) (tokenid:) (state:{}) (password:) (burn:) (split:) (coinage:) (mine:) (debug:) (dryrun:) - Send Self or Tokens to an address");
	}
	
	@Override
	public String getFullHelp() {
		return "\nsend\n"
				+ "\n"
				+ "Send Self or custom tokens to a wallet or custom script address.\n"
				+ "\n"
				+ "Optionally, send to multiple addresses in one transaction; split UTxOs; add state variables or include a burn.\n"
				+ "\n"
				+ "address: (optional)\n"
				+ "    A Self 0x or Sx wallet address or custom script address. Must also specify amount.\n"
				+ "\n"
				+ "amount: (optional)\n"
				+ "    The amount of Self or custom tokens to send to the specified address.\n"
				+ "\n"
				+ "multi: (optional)\n"
				+ "    JSON Array listing addresses and amounts to send in one transaction.\n"
				+ "    Takes the format [address:amount,address2:amount2,..], with each set in double quotes.\n"
				+ "\n"
				+ "tokenid: (optional)\n"
				+ "    If sending a custom token, you must specify its tokenid. Defaults to Self (0x00).\n"
				+ "\n"
				+ "state: (optional)\n"
				+ "    List of state variables, if sending coins to a script. A JSON object in the format {\"port\":\"value\",..}\n"
				+ "\n"
				+ "burn: (optional)\n"
				+ "    The amount of Self to burn with this transaction.\n"
				+ "\n"
				+ "password: (optional)\n"
				+ "    If your Wallet is password locked you can unlock it for this one transaction - then relock it.\n"
				+ "\n"
				+ "split: (optional)\n"
				+ "    You can set the number of coins the recipient will receive, between 1 and 20. Default is 1.\n"
				+ "    The amount being sent will be split into multiple coins of equal value.\n"
				+ "    You can split your own coins by sending to your own address.\n"
				+ "    Useful if you want to send multiple transactions without waiting for change to be confirmed.\n"
				+ "\n"
				+ "coinage: (optional)\n"
				+ "    How old must the coins be in blocks.\n"
				+ "\n"
				+ "debug: (optional)\n"
				+ "    true or false, true will print more detailed logs.\n"
				+ "\n"
				+ "dryrun: (optional)\n"
				+ "    true or false, true will simulate the send transaction but not execute it.\n"
				+ "\n"
				+ "mine: (optional)\n"
				+ "    true or false - should you mine the transaction immediately.\n"
				+ "\n"
				+ "fromaddress: (optional)\n"
				+ "    Only use this address for input coins.\n"
				+ "\n"
				+ "signkey: (optional)\n"
				+ "    Sign the txn with only this key (use with fromaddress).\n"
				+ "\n"
				+ "storestate: (optional)\n"
				+ "    true or false - defaults to true. Should the output coins store the state (will still appear in NOTIFYCOIN messages).\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "send address:0xFF.. amount:10\n"
				+ "\n"
				+ "send address:0xFF.. amount:10 tokenid:0xFED5.. burn:0.1\n"
				+ "\n"
				+ "send address:0xFF.. amount:10 split:5 burn:0.1\n"
				+ "\n"
				+ "send multi:[\"0xFF..:10\",\"0xEE..:10\",\"0xDD..:10\"] split:20\n"
				+ "\n"
				+ "send amount:1 address:0xFF.. state:{\"0\":\"0xEE..\",\"1\":\"0xDD..\"}\n";
					
	}

	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"action","uid",
				"address","amount","multi","tokenid","state","burn","coinage",
				"split","debug","dryrun","mine","password","storestate",
				"fromaddress","signkey"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		//Who are we sending to
		ArrayList<AddressAmount> recipients = new ArrayList<>();
		
		//What is the total amount we are sending..
		MiniNumber totalamount = MiniNumber.ZERO;
		
		//Is it a MULTI send..
		if(existsParam("multi")) {
			
			//Convert the list..
			JSONArray allrecips = getJSONArrayParam("multi");
			Iterator<String> it = allrecips.iterator(); 
			while(it.hasNext()) {
				String sendto = it.next();
				
				StringTokenizer strtok = new StringTokenizer(sendto,":");
				
				//Get the address
				String address 	= strtok.nextToken();
				MiniData addr 	= null; 
				if(address.toLowerCase().startsWith("sx")) {
					//Convert back to normal hex..
					try {
						addr = Address.convertSelfAddress(address);
					}catch(IllegalArgumentException exc) {
						throw new CommandException(exc.toString());
					}
				}else {
					addr = new MiniData(address);
				}
				
				//Get the amount
				MiniNumber amount 	= new MiniNumber(strtok.nextToken());
				totalamount 		= totalamount.add(amount);
				
				//Add to our List
				recipients.add(new AddressAmount(addr, amount));
			}
			
		}else {
			//Get the address
			MiniData sendaddress	= new MiniData(getAddressParam("address"));
			
			//How much to send
			MiniNumber sendamount 	= getNumberParam("amount");
			totalamount 			= sendamount;
			
			recipients.add(new AddressAmount(sendaddress, sendamount));
		}
		
		//What is the Token
		String tokenid = getParam("tokenid", "0x00");
		
		//Show extra info..
		boolean debug 	= getBooleanParam("debug", false);
		boolean dryrun 	= getBooleanParam("dryrun", false);
		if(dryrun) {
			debug = true;
		}
		
		//Are the outputs storing the state
		boolean storestate = getBooleanParam("storestate", true);
		
		//Is there a burn..
		MiniNumber burn  = getNumberParam("burn",MiniNumber.ZERO);
		if(burn.isLess(MiniNumber.ZERO)) {
			throw new CommandException("Cannot have negative burn "+burn.toString());
		}
		
		//Are we splitting the outputs
		MiniNumber split = getNumberParam("split", MiniNumber.ONE).floor();
		if(split.isLess(MiniNumber.ONE) || split.isMore(GeneralParams.MAX_SPLIT_COINS)) {
			throw new CommandException("Split must be whole number from 1 to "+GeneralParams.MAX_SPLIT_COINS);
		}
		
		//Are we doing a Self burn
		if(tokenid.equals("0x00")) {
			totalamount = totalamount.add(burn);
		}
		
		//Get the State
		JSONObject state = new JSONObject();
		if(existsParam("state")) {
			state = getJSONObjectParam("state");
		}
		
		//Are we Mining synchronously
		boolean minesync = getBooleanParam("mine", false);
		
		//Get the TxPoWDB
		TxPoWDB txpdb 		= SelfDB.getDB().getTxPoWDB();
		TxPoWMiner txminer 	= SELFSystem.getInstance().getTxPoWMiner();
		
		//Get the tip of the tree
		TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
		
		//How old do the coins need to be.. used by consolidate
		MiniNumber coinage = getNumberParam("coinage", GlobalParams.SELF_CONFIRM_DEPTH);
		if(coinage.isLess(GlobalParams.SELF_CONFIRM_DEPTH)) {
			throw new CommandException("Coinage MUST be >= "+GlobalParams.SELF_CONFIRM_DEPTH);
		}
		
		//Is it from a specific address
		String usepubkey 		= getAddressParam("signkey","");
		boolean usefromaddress 	= false;
		String fromaddress 		= getAddressParam("fromaddress","");
		if(!fromaddress.equals("")) {
			if(debug) {
				SelfLogger.log("Search only coins with address : "+fromaddress);
			}
			usefromaddress 	= true;
		}
		
		//Lets build a transaction..
		ArrayList<Coin> foundcoins = null;
		if(usefromaddress) {
			
			//Only search a specific address
			foundcoins	= TxPoWSearcher.searchCoins(tip, true, 
										false, MiniData.ZERO_TXPOWID, 
										false, MiniNumber.ZERO,
										true, new MiniData(fromaddress),
										true, new MiniData(tokenid),
										false);
			
		}else {
			foundcoins	= TxPoWSearcher.getRelevantUnspentCoins(tip,tokenid,true);
		}
		ArrayList<Coin> relcoins 	= new ArrayList<>();
		
		//Now make sure they are old enough
		MiniNumber mincoinblock = tip.getBlockNumber().sub(coinage);
		for(Coin relc : foundcoins) {
			if(relc.getBlockCreated().isLessEqual(mincoinblock)) {
				relcoins.add(relc);
			}
		}
		
		//Are there any coins at all..
		if(relcoins.size()<1) {
			throw new CommandException("No Coins of tokenid:"+tokenid+" available!");
		}
		
		//Lets select the correct coins..
		MiniNumber findamount = totalamount;
		if(!tokenid.equals("0x00")) {
			findamount 	= relcoins.get(0).getToken().getScaledSelfAmount(totalamount);
		}
		
		//Now search for the best coin selection.. leave for Now!..
		relcoins = selectCoins(relcoins, findamount, debug);
		
		//The current total
		MiniNumber currentamount 		= MiniNumber.ZERO;
		ArrayList<Coin> currentcoins 	= new ArrayList<>();
		
		if(debug) {
			SelfLogger.log("Coins that will be checked for transaction");
			for(Coin coin : relcoins) {
				SelfLogger.log("Coin : "+coin.getAmount()+" "+coin.getCoinID().to0xString());
			}
		}
		
		//Now cycle through..
		Token token = null;
		for(Coin coin : relcoins) {
			
			//Check if we are already using thewm in another Transaction that is being mined
			if(txminer.checkForMiningCoin(coin.getCoinID().to0xString())) {
				if(debug) {
					SelfLogger.log("Coin being mined : "+coin.getAmount()+" "+coin.getCoinID().to0xString());
				}
				continue;
			}
			
			//Check if in mempool..
			if(txpdb.checkMempoolCoins(coin.getCoinID())) {
				if(debug) {
					SelfLogger.log("Coin in mempool : "+coin.getAmount()+" "+coin.getCoinID().to0xString());
				}
				continue;
			}
		
			//Add this coin..
			currentcoins.add(coin);
			
			//Get the actual ammount..
			if(tokenid.equals("0x00")) {
				currentamount = currentamount.add(coin.getAmount());
			}else {
				//Store it..
				if(token == null) {
					token = coin.getToken();
				}
				
				//Calculate the Token Amount..
				MiniNumber amt = coin.getToken().getScaledTokenAmount(coin.getAmount());
				
				//Add that to the total
				currentamount = currentamount.add(amt);
			}
			
			if(debug) {
				SelfLogger.log("Coin added : "+coin.getAmount()+" "+coin.getCoinID().to0xString()+" total:"+currentamount);
			}
			
			//Do we have enough..
			if(currentamount.isMoreEqual(totalamount)) {
				break;
			}
		}
		
		//Check the token script
		if(token != null) {
			String script = token.getTokenScript().toString();
			if(!script.equals("RETURN TRUE")) {
				//Not enough funds..
				ret.put("status", false);
				ret.put("message", "Token script is not simple : "+script);
				return ret;
			}
		}
		
		//Did we add enough
		if(currentamount.isLess(totalamount)) {
			//Not enough funds..
			ret.put("status", false);
			ret.put("message", "Insufficient funds.. you only have "+currentamount+" require:"+totalamount);
			return ret;
		}
		
		if(debug) {
			SelfLogger.log("Total Coins used : "+currentcoins.size());
		}
		
		//What is the change..
		MiniNumber change = currentamount.sub(totalamount); 
		
		//Lets construct a txn..
		Transaction transaction 	= new Transaction();
		Witness witness 			= new Witness();
		
		//Min depth of a coin
		MiniNumber minblock = MiniNumber.ZERO;
				
		//Add the inputs..
		for(Coin inputs : currentcoins) {
			
			//Add this input to our transaction
			transaction.addInput(inputs);
			
			if(debug) {
				SelfLogger.log("Input : "+inputs.toJSON());
			}
			
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
		
		//Get the main Wallet
		Wallet walletdb = SelfDB.getDB().getWallet();
		
		//Create a list of the required signatures
		ArrayList<String> reqsigs = new ArrayList<>();
		
		//Which Coins are added
		ArrayList<String> addedcoinid = new ArrayList<>();
		
		//Add the MMR proofs for the coins..
		for(Coin input : currentcoins) {
			
			//May need it for BURN
			addedcoinid.add(input.getCoinID().to0xString());
			
			//Get the proof..
			MMRProof proof = mmrnode.getMMR().getProofToPeak(input.getMMREntryNumber());
			
			//Create the CoinProof..
			CoinProof cp = new CoinProof(input, proof);
			
			//Add it to the witness data
			witness.addCoinProof(cp);
			
			//Add the script proofs
			String scraddress 	= input.getAddress().to0xString();
			
			//Get the ScriptRow..
			ScriptRow srow = walletdb.getScriptFromAddress(scraddress);
			if(srow == null) {
				throw new CommandException("SERIOUS ERROR script missing for simple address : "+scraddress);
			}
			ScriptProof pscr = new ScriptProof(srow.getScript());
			witness.addScript(pscr);
			
			//Add this address / public key to the list we need to sign as..
			String pubkey = srow.getPublicKey();
			if(!reqsigs.contains(pubkey)) {
				reqsigs.add(pubkey);
			}
		}
		
		//Are we spcifying the sign key
		if(usepubkey != "") {
			reqsigs.clear();
			reqsigs.add(usepubkey);
		}
		
		//Now make the sendamount correct
		if(!tokenid.equals("0x00")) {
			
			//Convert back and forward to make sure is a valid amount
			MiniNumber tokenamount 	= token.getScaledSelfAmount(totalamount); 
			MiniNumber prectest 	= token.getScaledTokenAmount(tokenamount);
			
			if(!prectest.isEqual(totalamount)) {
				throw new CommandException("Invalid Token amount to send.. "+totalamount);
			}
			
			totalamount = tokenamount;
					
		}else {
			//Check valid - for Self..
			if(!totalamount.isValidSelfValue()) {
				throw new CommandException("Invalid Self amount to send.. "+totalamount);
			}
		}
		
		//Are we splitting the outputs
		int isplit = split.getAsInt();
		
		//Cycle through all the recipients
		for(AddressAmount user : recipients) {
			
			//The user address
			MiniData address 		= user.getAddress();
			
			//How much we send to this user
			MiniNumber usertotal 	= user.getAmount();
			if(!tokenid.equals("0x00")) {
				//Get the correct scaled amount
				usertotal = token.getScaledSelfAmount(usertotal);
			}
			MiniNumber currenttotal = MiniNumber.ZERO;
			
			//The split amount
			MiniNumber splitamount 	= usertotal.div(split);
			if(splitamount.isLessEqual(MiniNumber.ZERO)) {
				//ZERO output not allowed..
				throw new CommandException("Cannot have ZERO output - output is too small for this user.. "+user.getAddress());
			}
			
			//Create all the outputs for this user at this split level
			for(int i=0;i<isplit;i++) {
				//Create the output
				Coin recipient = new Coin(Coin.COINID_OUTPUT, address, splitamount, Token.TOKENID_SELF, storestate);
				
				//Add to the User total
				currenttotal = currenttotal.add(splitamount);
				
				//Do we need to add the Token..
				if(!tokenid.equals("0x00")) {
					recipient.resetTokenID(new MiniData(tokenid));
					recipient.setToken(token);
				}
				
				//Add to the Transaction
				transaction.addOutput(recipient);
				
				if(debug) {
					SelfLogger.log("Output : "+recipient.toJSON());
				}
			}
			
			//Is there any left over..
			MiniNumber currentdiff = usertotal.sub(currenttotal);
			if(currentdiff.isMore(MiniNumber.ZERO)) {
				
				//Send them the remainder..
				Coin remaincoin = new Coin(Coin.COINID_OUTPUT, address, currentdiff, Token.TOKENID_SELF, storestate);
				if(!tokenid.equals("0x00")) {
					remaincoin.resetTokenID(new MiniData(tokenid));
					remaincoin.setToken(token);
				}
				
				//And finally.. add the change output
				transaction.addOutput(remaincoin);
				
				if(debug) {
					SelfLogger.log("Rounding Output from split : "+remaincoin.toJSON());
				}
			}
		}
		
		//Do we need to send change..
		if(debug) {
			SelfLogger.log("Change amount : "+change);
		}
		
		if(change.isMore(MiniNumber.ZERO)) {
			//Create a new address
			ScriptRow newwalletaddress = SelfDB.getDB().getWallet().getDefaultAddress();
			
			//THIS is a fix for an issue where backup saved with wrong seed phrase
			if(SelfDB.getDB().getWallet().isBaseSeedAvailable()) {
				if(!keys.checkKey(newwalletaddress.getPublicKey())) {
					throw new CommandException("[!] SERIOUS ERROR - INCORRECT Public key : "+newwalletaddress.getPublicKey());
				}
			}
			
			//Create the change address..
			MiniData chgaddress = new MiniData(newwalletaddress.getAddress());
			
			//Are we using a specific address..
			if(usefromaddress) {
				chgaddress = new MiniData(fromaddress);
			}
			
			//Get the scaled token ammount..
			MiniNumber changeamount = change;
			if(!tokenid.equals("0x00")) {
				//Use the token object we previously found
				changeamount = token.getScaledSelfAmount(change);
			}
			
			//Change coin does not keep the state
			Coin changecoin = new Coin(Coin.COINID_OUTPUT, chgaddress, changeamount, Token.TOKENID_SELF, false);
			if(!tokenid.equals("0x00")) {
				changecoin.resetTokenID(new MiniData(tokenid));
				changecoin.setToken(token);
			}
			
			//And finally.. add the change output
			transaction.addOutput(changecoin);
			
			if(debug) {
				SelfLogger.log("Change Output : "+changecoin.toJSON());
			}
		}
		
		//Check Total Outputs..
		int outsize = transaction.getAllOutputs().size();
		if(outsize>GeneralParams.MAX_RELAY_OUTPUTCOINS) {
			
			//Will not be relayed..
			throw new CommandException("Too many outputs "+outsize+" - will not be relayed by network");
		}
		
		//Are there any State Variables
		for(Object key : state.keySet()) {
			
			//The Key is a String
			String portstr = (String)key; 
			
			//The port
			int port = Integer.parseInt(portstr);
			
			//Get the state var..
			String var = state.get(key)+"";

			//Create a state variable..
			StateVariable sv = new StateVariable(port, var);
			
			//Add to the transaction..
			transaction.addStateVariable(sv);
		}
		
		//Compute the correct CoinID
		TxPoWGenerator.precomputeTransactionCoinID(transaction);
		
		//Calculate the TransactionID..
		transaction.calculateTransactionID();
		
		//Now that we have constructed the transaction - lets sign it..
		if(debug) {
			if(usepubkey != "") {
				SelfLogger.log("(SIGNKEYS) Total signatures required : "+reqsigs.size());
			}else {
				SelfLogger.log("Total signatures required : "+reqsigs.size());
			}
		}
		
		//Are we password unlocking..
		boolean passwordlock = false;
		if(!dryrun) {
			
			if(existsParam("password")) {
			
				if(SelfDB.getDB().getWallet().isBaseSeedAvailable()) {
					throw new CommandException("WalletDB NOT Locked! Password Invalid");
				}
				
				if(debug) {
					SelfLogger.log("Unlocking password DB");
				}
				
				//Lets unlock the DB
				vault.passowrdUnlockDB(getParam("password"));
				 
				//Lock at the end..
				passwordlock = true;
			}
		}else {
			SelfLogger.log("DRYRUN so NOT Unlocking password DB");
		}
		
		for(String pubkey : reqsigs) {
			if(debug) {
				SelfLogger.log("Signing transction with : "+pubkey);
			}
			
			if(!dryrun) {
				try {
					//Use the wallet..
					Signature signature = walletdb.signData(pubkey, transaction.getTransactionID());
					
					//Add it..
					witness.addSignature(signature);
					
				}catch (Exception e) {
					throw new CommandException(e.toString());
				}
				
			}else {
				SelfLogger.log("DRY RUN - not signing");
			}
		}
		
		//The final TxPoW
		TxPoW txpow = null;
		
		//Is there a BURN..
		if(!tokenid.equals("0x00") && burn.isMore(MiniNumber.ZERO)) {
			
			//Create a Burn Transaction
			TxnRow burntxn = txnutils.createBurnTransaction(addedcoinid,transaction.getTransactionID(),burn);

			//Now create a complete TxPOW
			txpow = TxPoWGenerator.generateTxPoW(transaction, witness, burntxn.getTransaction(), burntxn.getWitness());
		
		}else {
			//Now create a complete TxPOW
			txpow = TxPoWGenerator.generateTxPoW(transaction, witness);
		}
		
		//Calculate the txpowid / size..
		txpow.calculateTXPOWID();
		
		//Check Size is acceptable..
		long size = txpow.getSizeinBytesWithoutBlockTxns();
		long max  = tip.getTxPoW().getMagic().getMaxTxPoWSize().getAsLong();
		if(debug) {
			SelfLogger.log("TxPoW size "+size+" max:"+max);
		}
		
		if(size > max) {
			
			//Are we locking the DB
			if(!dryrun && passwordlock) {
				
				//Lock the Wallet DB
				vault.passwordLockDB(getParam("password"));
			}
			
			throw new CommandException("TxPoW size too large.. "+size+"/"+max);
		}
		
		if(!dryrun) {
		
			//Are we locking the DB
			if(passwordlock) {
				
				//Lock the Wallet DB
				vault.passwordLockDB(getParam("password"));
			}
			
			//Sync or Async mining..
			if(minesync) {
				boolean success = SELFSystem.getInstance().getTxPoWMiner().MineMaxTxPoW(false, txpow, 120000);
				
				if(!success) {
					throw new CommandException("FAILED TO MINE txn in 120 seconds !?");
				}
				
			}else {
				SELFSystem.getInstance().getTxPoWMiner().mineTxPoWAsync(txpow);
			}
			
		}else {
			SelfLogger.log("DRY RUN - not sending");
		}
		
		//All good..
		ret.put("dryrun", dryrun);
		ret.put("response", txpow.toJSON());
		
		//Work out some sizes..
		if(dryrun) {
			
			JSONObject sizes = new JSONObject();
			
			sizes.put("txpow", txpow.getSizeinBytes());
			
			JSONObject inputcoins=new JSONObject();
			ArrayList<Coin> incoins = txpow.getTransaction().getAllInputs();
			for(Coin cc : incoins) {
				MiniData cd = MiniData.getMiniDataVersion(cc);
				inputcoins.put(cc.getCoinID(), cd.getLength());
			}
			
			JSONObject outputcoins=new JSONObject();
			ArrayList<Coin> outcoins = txpow.getTransaction().getAllOutputs();
			for(Coin cc : outcoins) {
				MiniData cd = MiniData.getMiniDataVersion(cc);
				outputcoins.put(cc.getCoinID(), cd.getLength());
			}
			
			sizes.put("inputcoins", inputcoins);
			sizes.put("outputcoins", outputcoins);
			
			//And the Witness data..
			MiniData wd = MiniData.getMiniDataVersion(txpow.getWitness());
			sizes.put("witness", wd.getLength());
			
			ret.put("bytesize", sizes);
		}
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new send();
	}

	
	/**
	 * Coin Selection Algorithm..
	 * 
	 * Which coins to use when sending a transaction
	 * Expects all the coins to be of the same tokenid
	 */
	
	public static ArrayList<Coin> selectCoins(ArrayList<Coin> zAllCoins, MiniNumber zAmountRequired){
		return selectCoins(zAllCoins, zAmountRequired, false);
	}
	
	public static ArrayList<Coin> selectCoins(ArrayList<Coin> zAllCoins, MiniNumber zAmountRequired, boolean zDebug){
		ArrayList<Coin> ret = new ArrayList<>();
		
		//Get the TxPoWDB
		TxPoWDB txpdb 		= SelfDB.getDB().getTxPoWDB();
		TxPoWMiner txminer 	= SELFSystem.getInstance().getTxPoWMiner();
		
		//First sort the coins by size and address..
		ArrayList<Coin> coinlist = orderCoins(zAllCoins);

		//Are we debugging..
		if(zDebug) {
			SelfLogger.log("All Selection coins");
			for(Coin coin : coinlist) {
				SelfLogger.log("Coin found : "+coin.getAmount()+" "+coin.getCoinID().to0xString()+" @ "+coin.getAddress().to0xString());
			}
			
			SelfLogger.log("Now checking coins");
		}
		
		//Now go through and pick a coin big enough.. but keep looking for smaller coins  
		boolean found    = false;
		Coin currentcoin = null;
		for(Coin coin : coinlist) {
			
			//Check if we are already using thewm in another Transaction that is being mined
			if(txminer.checkForMiningCoin(coin.getCoinID().to0xString())) {
				if(zDebug) {
					SelfLogger.log("Coin being mined : "+coin.getAmount()+" "+coin.getCoinID().to0xString());
				}
				continue;
			}
			
			//Check if in mempool..
			if(txpdb.checkMempoolCoins(coin.getCoinID())) {
				if(zDebug) {
					SelfLogger.log("Coin in mempool : "+coin.getAmount()+" "+coin.getCoinID().to0xString());
				}
				continue;
			}
			
			if(coin.getAmount().isMoreEqual(zAmountRequired)) {
			
				if(zDebug) {
					SelfLogger.log("Valid Coin found : "+coin.getAmount()+" "+coin.getCoinID().to0xString());
				}
				found 		= true;
				currentcoin = coin;
			}else {
				
				//Not big enough - all others will be smaller..
				if(zDebug) {
					SelfLogger.log("Coin too small - no more checking : "+coin.getAmount()+" "+coin.getCoinID().to0xString());
				}
				
				break;
			}
		}
		
		//Did we find one..
		if(found) {
			//Add the single coin to the list
			ret.add(currentcoin);
		
			if(zDebug) {
				SelfLogger.log("Single coin returned : "+currentcoin.getAmount()+" "+currentcoin.getCoinID().to0xString());
			}
			
		}else {

			//Did not find a single coin that satisfies the amount..
			if(zDebug) {
				SelfLogger.log("Returning all coins..");
			}
			
			//Return them all..
			return coinlist;
		}
		
		//Return what we have..
		return ret;
	}
	
	/**
	 * Order coins by amount and address
	 */
	public static ArrayList<Coin> orderCoins(ArrayList<Coin> zCoins){
		
		ArrayList<Coin> ret = new ArrayList<>();
		
		//First Sort by amount
		Collections.sort(zCoins, new Comparator<Coin>() {
			@Override
			public int compare(Coin zCoin1, Coin zCoin2) {
				MiniNumber amt1 = zCoin1.getAmount();
				MiniNumber amt2 = zCoin2.getAmount();
				return amt2.compareTo(amt1);
			}
		});
		
		//Now cycle through and get the different addresses
		ArrayList<String> addresses = new ArrayList<>();
		String currentaddress = "";
		for(Coin cc : zCoins) {
			String addr = cc.getAddress().to0xString();
			if(!addresses.contains(addr)) {
				addresses.add(addr);
			}
		}
		
		//And now order by address..
		for(String address : addresses) {
			for(Coin cc : zCoins) {
				String caddress = cc.getAddress().to0xString();
				if(caddress.equals(address)) {
					ret.add(cc);
				}
			}
		}
		
		return ret;
	}
	
	public static void main(String[] zArgs) {
		
		ArrayList<Coin> allcoins = new ArrayList<>();
		allcoins.add(new Coin(new MiniData("0xFF"), MiniNumber.ONE, MiniData.ZERO_TXPOWID));
		allcoins.add(new Coin(new MiniData("0xEE"), MiniNumber.TWO, MiniData.ZERO_TXPOWID));
		allcoins.add(new Coin(new MiniData("0xDD"), MiniNumber.TEN, MiniData.ZERO_TXPOWID));
		allcoins.add(new Coin(new MiniData("0xDD"), MiniNumber.ONE, MiniData.ZERO_TXPOWID));
		allcoins.add(new Coin(new MiniData("0xEE"), new MiniNumber("0.5"), MiniData.ZERO_TXPOWID));
		allcoins.add(new Coin(new MiniData("0xEE"), new MiniNumber("0.2"), MiniData.ZERO_TXPOWID));

		ArrayList<Coin> sortedcoins = orderCoins(allcoins);
				
		for(Coin cc : sortedcoins) {
			System.out.println(cc.toJSON().toString());
		}
	}
}