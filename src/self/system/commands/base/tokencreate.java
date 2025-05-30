package org.self.system.commands.base;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.mmr.MMRProof;
import org.self.database.txpowdb.TxPoWDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.database.wallet.ScriptRow;
import org.self.database.wallet.Wallet;
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
import org.self.objects.base.MiniString;
import org.self.objects.keys.Signature;
import org.self.system.SELFSystem;
import org.self.system.brains.TxPoWGenerator;
import org.self.system.brains.TxPoWMiner;
import org.self.system.brains.TxPoWSearcher;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.system.params.GlobalParams;
import org.self.utils.json.JSONObject;

public class tokencreate extends Command {

	public tokencreate() {
		super("tokencreate","[name:] [amount:] (decimals:) (script:) (state:{}) (signtoken:) (webvalidate:) (burn:) - Create a token. 'name' can be a JSON Object");
	}
	
	@Override
	public String getFullHelp() {
		return "\ntokencreate\n"
				+ "\n"
				+ "Create (mint) custom tokens or NFTs.\n"
				+ "\n"
				+ "You must have some sendable Self in your wallet as tokens are 'colored coins', a fraction of 1 Self.\n"
				+ "\n"
				+ "name:\n"
				+ "    The name of the token. Can be a string or JSON Object.\n"
				+ "\n"
				+ "amount: \n"
				+ "    The amount of total supply to create for the token. Between 1 and 1 Trillion.\n"
				+ "\n"
				+ "decimals: (optional)\n"
				+ "    The number of decimal places for the token. Default is 8, maximum 16.\n"
				+ "    To create NFTs, use 0.\n"
				+ "\n"
				+ "script: (optional)\n"
				+ "    Add a custom script that must return 'TRUE' when spending any coin of this token.\n"
				+ "    Both the token script and coin script must return 'TRUE' for a coin to be sendable.\n"
				+ "\n"
				+ "state: (optional)\n"
				+ "    List of state variables, if adding a script. A JSON object in the format {\"port\":\"value\",..}\n"
				+ "\n"
				+ "signtoken: (optional)\n"
				+ "    Provide a public key to sign the token with.\n"
				+ "    Useful for proving you are the creator of the token/NFT.\n"
				+ "\n"
				+ "webvalidate: (optional)\n"
				+ "    Provide a URL to a publicly viewable .txt file you are hosting which stores the tokenid for validation purposes.\n"
				+ "    Create the file in advance and get the tokenid after the token has been minted.\n"
				+ "\n"
				+ "burn: (optional)\n"
				+ "    Amount to burn with the tokencreate minting transaction.\n"
				+ "\n"
				+ "mine: (optional)\n"
				+ "    Mine the TxPoW synchronously.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "tokencreate name:newtoken amount:1000000\n"
				+ "\n"
				+ "tokencreate amount:10 name:{\"name\":\"newcoin\",\"link\":\"http:mysite.com\",\"description\":\"A very cool token\"}\n"
				+ "\n"
				+ "tokencreate name:mynft amount:10 decimals:0 webvalidate:https://www.mysite.com/nftvalidation.txt signtoken:0xFF.. burn:0.1\n"
				+ "\n"
				+ "tokencreate name:charitycoin amount:1000 script:\"ASSERT VERIFYOUT(@TOTOUT-1 0xMyAddress 1 0x00 TRUE)\"\n";				
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"name","amount","decimals","script",
				"state","signtoken","webvalidate","burn","mine","uselimits"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
	
		//Check the basics..
		if(!existsParam("name") || !existsParam("amount")) {
			throw new CommandException("MUST specify name and amount");
		}
		
		//Are we Mining synchronously
		boolean minesync = getBooleanParam("mine", false);
		
		//Are we adding limits to the NUmber of Tokens allowed..
		boolean uselimits = getBooleanParam("uselimits", true);
		
		//Is there a state JSON
		JSONObject state = new JSONObject();
		if(existsParam("state")) {
			state = getJSONObjectParam("state");
		}
		
		//Is name a JSON
		JSONObject jsonname = null;
		if(isParamJSONObject("name")) {
			
			//Get the JSON
			jsonname = getJSONObjectParam("name");
			
			//make sure there is a name object
			if(!jsonname.containsKey("name")) {
				throw new CommandException("MUST specify a 'name' for the token in the JSON");
			}
			
		}else {
			
			//It's a String.. create a JSON
			jsonname = new JSONObject();
			jsonname.put("name", getParam("name"));
		}
		
		//The amount is always a MiniNumber
		String amount   = (String)getParams().get("amount");
		
		//The burn
		MiniNumber burn = getNumberParam("burn", MiniNumber.ZERO);
		
		//How many decimals - can be 0.. for an NFT
		int decimals = 8;
		if(getParams().containsKey("decimals")) {
			decimals = Integer.parseInt((String)getParams().get("decimals"));
			
			//Safety check.. not consensus set - could be more.
			if(uselimits && decimals>16) {
				throw new Exception("MAX 16 decimal places");
			}
		}
		
		String script = "RETURN TRUE";
		if(getParams().containsKey("script")) {
			script	= (String)getParams().get("script");
		}
		
		//Now construct the txn..
		if(jsonname==null || amount==null) {
			throw new CommandException("MUST specify name and amount");
		}
		
		//The actual amount of tokens..
		MiniNumber totaltoks = new MiniNumber(amount).floor(); 
		
		//Safety check Amount is within tolerant levels.. could use ALL their Self otherwise..
		if(uselimits && totaltoks.isMore(MiniNumber.TRILLION)) {
			throw new CommandException("MAX 1 Trillion coins for a token");
		}
		
		if(totaltoks.isLessEqual(MiniNumber.ZERO)) {
			throw new CommandException("Cannot create less than 1 token");
		}
		
		//Decimals as a number
		MiniNumber totaldecs = MiniNumber.TEN.pow(decimals); 
		
		//How much Self will it take to colour.. 
		MiniNumber colorself = MiniNumber.MINI_UNIT.mult(totaldecs).mult(totaltoks);
		
		//What is the scale..
		int scale = MiniNumber.MAX_DECIMAL_PLACES - decimals;
		
		//The actual amount of Self that needs to be sent - add the burn if any
		MiniNumber sendamount 	= colorself.add(burn);
		
		//Send it to ourselves
		ScriptRow sendkey 		= SelfDB.getDB().getWallet().getDefaultAddress();
		MiniData sendaddress 	= new MiniData(sendkey.getAddress());
		
		//get the tip..
		TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
				
		//Lets build a transaction..
		ArrayList<Coin> foundcoins	= TxPoWSearcher.getRelevantUnspentCoins(tip,"0x00",true);
		ArrayList<Coin> relcoins 	= new ArrayList<>();
		
		//Now make sure they are old enough
		MiniNumber mincoinblock = tip.getBlockNumber().sub(GlobalParams.SELF_CONFIRM_DEPTH);
		for(Coin relc : foundcoins) {
			if(relc.getBlockCreated().isLessEqual(mincoinblock)) {
				relcoins.add(relc);
			}
		}
		
		//Are there any coins at all..
		if(relcoins.size()<1) {
			throw new CommandException("No Self Coins available!");
		}
		
		//The current total
		MiniNumber currentamount 	= MiniNumber.ZERO;
		ArrayList<Coin> currentcoins = new ArrayList<>();
		
		//Get the TxPoWDB
		TxPoWDB txpdb 		= SelfDB.getDB().getTxPoWDB();
		TxPoWMiner txminer 	= SELFSystem.getInstance().getTxPoWMiner();
		
		//Now cycle through..
		for(Coin coin : relcoins) {
			
			//Check if we are already using thewm in another Transaction that is being mined
			if(txminer.checkForMiningCoin(coin.getCoinID().to0xString())) {
				continue;
			}
			
			//Check if in mempool..
			if(txpdb.checkMempoolCoins(coin.getCoinID())) {
				continue;
			}
			
			//Add this coin..
			currentcoins.add(coin);
			currentamount = currentamount.add(coin.getAmount());
			
			//Do we have enough..
			if(currentamount.isMoreEqual(sendamount)) {
				break;
			}
		}
		
		//Did we add enough
		if(currentamount.isLess(sendamount)) {
			//Not enough funds..
			//Insufficient blocks
			ret.put("status", false);
			ret.put("message", "Insufficient funds.. you only have "+currentamount);
			return ret;
		}
		
		//What is the change..
		MiniNumber change = currentamount.sub(sendamount); 
		
		//Lets construct a txn..
		Transaction transaction 	= new Transaction();
		Witness witness 			= new Witness();
		
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
		
		//Get the main Wallet
		Wallet walletdb = SelfDB.getDB().getWallet();
		
		//Create a list of the required signatures
		ArrayList<String> reqsigs = new ArrayList<>();
		
		//Which Coins are added
		ArrayList<String> addedcoinid = new ArrayList<>();
				
		//Add the MMR proofs for the coins..
		for(Coin input : currentcoins) {
			
			//Keep for burn calc
			addedcoinid.add(input.getCoinID().to0xString());
			
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
			
			//Add this address / public key to the list we need to sign as..
			String pubkey = srow.getPublicKey();
			if(!reqsigs.contains(pubkey)) {
				reqsigs.add(pubkey);
			}
		}
		
		//Now add the output..
		Coin recipient = new Coin(Coin.COINID_OUTPUT, sendaddress, colorself, Token.TOKENID_CREATE, true);
		
		//Is there a Web Validation URL
		if(existsParam("webvalidate")) {
			
			//Add to the description
			jsonname.put("webvalidate", getParam("webvalidate"));
		}
		
		//Are we signing the token..
		if(existsParam("signtoken")) {
		
			//What is the coinid of the first input..
			MiniData firstcoinid = transaction.getAllInputs().get(0).getCoinID();
			
			//Calculate the CoinID.. It's the first output
			MiniData tokencoinid = transaction.calculateCoinID(firstcoinid, 0);
			
			//Get the Public Key
			String sigpubkey = getParam("signtoken");
			
			//Now sign the coinid..
			Signature sig = walletdb.signData(sigpubkey, tokencoinid);
			
			//Get the MiniData version..
			MiniData sigdata = MiniData.getMiniDataVersion(sig);
			
			//Get the Pubkey.. add it to the JSON
			jsonname.put("signtype", "self");
			jsonname.put("signedby", sigpubkey);
			jsonname.put("signature", sigdata.to0xString());
		}
		
		//Let's create the token..
		Token createtoken = new Token(Coin.COINID_OUTPUT, 
										new MiniNumber(scale), 
										colorself,
										new MiniString(jsonname.toString()),
										new MiniString(script));
		
		//Set the Create Token Details..
		recipient.setToken(createtoken);
		
		//Add to the transaction..
		transaction.addOutput(recipient);
		
		//Do we need to send change..
		if(change.isMore(MiniNumber.ZERO)) {
			//Create a new address
			ScriptRow newwalletaddress = SelfDB.getDB().getWallet().getDefaultAddress();
			MiniData chgaddress = new MiniData(newwalletaddress.getAddress());
			
			Coin changecoin = new Coin(Coin.COINID_OUTPUT, chgaddress, change, Token.TOKENID_SELF, false);
			transaction.addOutput(changecoin);
		}
		
		//Are there any State Variables
		for(Object key : state.keySet()) {
			
			//The Key is a String
			String portstr = (String)key; 
			
			//The port
			int port = Integer.parseInt(portstr);
			
			//Get the state var..
			String var = (String) state.get(key);

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
		for(String pubkey : reqsigs) {

			//Use the wallet..
			Signature signature = walletdb.signData(pubkey, transaction.getTransactionID());
			
			//Add it..
			witness.addSignature(signature);
		}
		
		//The final TxPoW
		TxPoW txpow = TxPoWGenerator.generateTxPoW(transaction, witness);
		
		//Calculate the size..
		txpow.calculateTXPOWID();
		
		//Sync or Async mining..
		if(minesync) {
			boolean success = SELFSystem.getInstance().getTxPoWMiner().MineMaxTxPoW(false, txpow, 120000);
			
			if(!success) {
				throw new CommandException("FAILED TO MINE txn in 120 seconds !?");
			}
			
		}else {
			SELFSystem.getInstance().getTxPoWMiner().mineTxPoWAsync(txpow);
		}
	
		//All good..
		ret.put("response", txpow.toJSON());
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new tokencreate();
	}
}