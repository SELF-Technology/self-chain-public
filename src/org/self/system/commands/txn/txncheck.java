package org.self.system.commands.txn;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.database.userprefs.txndb.TxnDB;
import org.self.database.userprefs.txndb.TxnRow;
import org.self.objects.Coin;
import org.self.objects.Token;
import org.self.objects.Transaction;
import org.self.objects.TxPoW;
import org.self.objects.Witness;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.brains.TxPoWChecker;
import org.self.system.brains.TxPoWGenerator;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;

public class txncheck extends Command {

	public txncheck() {
		super("txncheck","[id:] - Show details about the transaction");
	}
	
	@Override
	public String getFullHelp() {
		return "\ntxncheck\n"
				+ "\n"
				+ "Show details about the transaction.\n"
				+ "\n"
				+ "Verify whether the inputs, outputs, signatures, proofs and scripts are valid.\n"
				+ "\n"
				+ "id: (optional)\n"
				+ "    The id of the transaction to check.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "txncheck id:multisig\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"id"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();

		TxnDB db  = SelfDB.getDB().getCustomTxnDB();
		
		String id = getParam("id");
		
		//Get the Transaction..
		TxnRow txnrow 	= db.getTransactionRow(getParam("id"));
		if(txnrow == null) {
			throw new CommandException("Transaction not found : "+id);
		}
		
		Transaction txn = txnrow.getTransaction();
		Witness wit 	= txnrow.getWitness();
		
		//Inputs and outputs
		ArrayList<Coin> inputs = txnrow.getTransaction().getAllInputs();
		ArrayList<Coin> outputs = txnrow.getTransaction().getAllOutputs();
				
		//The results
		JSONObject details = new JSONObject();
		
		//First get a list of all the Output tokens..
		ArrayList<String> tokens = new ArrayList<>();
		for(Coin cc : inputs) {
			MiniData tokenhash = cc.getTokenID();
			if(tokenhash.isEqual(Token.TOKENID_CREATE)){
				tokenhash = Token.TOKENID_SELF;
			}
			
			String tok = tokenhash.to0xString();
			if(!tokens.contains(tok)) {
				tokens.add(tok);	
			}
		}
		
		for(Coin cc : outputs) {
			MiniData tokenhash = cc.getTokenID();
			if(tokenhash.isEqual(Token.TOKENID_CREATE)){
				tokenhash = Token.TOKENID_SELF;
			}
			
			String tok = tokenhash.to0xString();
			if(!tokens.contains(tok)) {
				tokens.add(tok);	
			}
		}
		
		//Now cycle through and check there is enough inputs..
		JSONArray alltokens = new JSONArray();
		for(String token : tokens) {
			MiniData tok = new MiniData(token);
			
			//The output total amount
			MiniNumber outamt = txn.sumOutputs(tok);
			
			//The input total amount
			MiniNumber inamt = txn.sumInputs(tok);
			
			//Add to thew details..
			JSONObject tokcoin = new JSONObject();
			tokcoin.put("tokenid", token);
			tokcoin.put("input", inamt.toString());
			tokcoin.put("output", outamt.toString());
			tokcoin.put("difference", inamt.sub(outamt).toString());
			alltokens.add(tokcoin);
		}
		details.put("coins", alltokens);
		
		//Get some details
		details.put("tokens", tokens.size());
		details.put("inputs", inputs.size());
		MiniNumber totselfin = MiniNumber.ZERO;
		for(Coin cc: inputs) {
			totselfin = totselfin.add(cc.getAmount());
		}
		
		details.put("outputs", outputs.size());
		MiniNumber totselfout = MiniNumber.ZERO;
		for(Coin cc: outputs) {
			totselfout = totselfout.add(cc.getAmount());
		}
		
		MiniNumber diff = totselfin.sub(totselfout);
		
		details.put("burn", diff.toString());
		details.put("validamounts", txnrow.getTransaction().checkValid());
		
		int sigs = txnrow.getWitness().getAllSignatures().size();
		details.put("signatures", sigs);
		
		//Now some low level checks..
		TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
		
		//Create a TxPoW..
		TxPoW temp = TxPoWGenerator.generateTxPoW(txn, wit);
		
		//Redo any checks..
		txn.clearIsMonotonic();
		
		boolean validbasic 		= TxPoWChecker.checkTxPoWBasic(temp); 
		boolean validsig 		= TxPoWChecker.checkSignatures(temp); 
		boolean validmmr 		= TxPoWChecker.checkMMR(tip.getMMR(), temp);
		boolean validscripts 	= TxPoWChecker.checkTxPoWScripts(tip.getMMR(), temp, tip.getTxPoW());

		JSONObject valid = new JSONObject();
		valid.put("basic", validbasic);
		valid.put("signatures", validsig);
		valid.put("mmrproofs", validmmr);
		valid.put("scripts", validscripts);
		
		details.put("valid", valid);
		
		
		JSONObject resp = new JSONObject();
		ret.put("response", details);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new txncheck();
	}

}
