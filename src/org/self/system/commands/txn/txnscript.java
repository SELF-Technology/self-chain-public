package org.self.system.commands.txn;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.mmr.MMRProof;
import org.self.database.userprefs.txndb.TxnDB;
import org.self.database.userprefs.txndb.TxnRow;
import org.self.objects.ScriptProof;
import org.self.objects.Witness;
import org.self.objects.base.MiniData;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONObject;

public class txnscript extends Command {

	public txnscript() {
		super("txnscript","[id:] [scripts:{}] - Add scripts to a txn");
	}
	
	@Override
	public String getFullHelp() {
		return "\ntxnscript\n"
				+ "\n"
				+ "Add scripts to a transaction.\n"
				+ "\n"
				+ "id:\n"
				+ "    The id of the transaction.\n"
				+ "\n"
				+ "scripts:\n"
				+ "    JSON holds the script and the proof in the format {script:proof}\n"
				+ "    If it is a single script, and not one created with mmrcreate, leave the proof blank.\n"
				+ "    If it is an mmrcreate script, include the proof.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "txnscript id:txnmast scripts:{\"RETURN TRUE\":\"\"}\n"
				+ "\n"
				+ "txnscript id:txnmast scripts:{\"RETURN TRUE\":\"0x000..\"}\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"id","scripts"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();

		TxnDB db = SelfDB.getDB().getCustomTxnDB();
		
		//The transaction
		String id 			= getParam("id");
		JSONObject scripts  = getJSONObjectParam("scripts");
		
		//Get the Transaction
		TxnRow txnrow 	= db.getTransactionRow(getParam("id"));
		if(txnrow == null) {
			throw new CommandException("Transaction not found : "+id);
		}
		Witness witness = txnrow.getWitness();
		
		//Any extra scripts
		for(Object key : scripts.keySet()) {
			
			//Get the script
			String exscript = (String)key;
			
			//The Key is a String
			String proof 		=  (String) scripts.get(key);
			ScriptProof scprf 	= null;
			if(proof.equals("")) {
				//Create a ScriptProof..
				scprf = new ScriptProof(exscript);
				
			}else {
				MiniData proofdata 	= new MiniData(proof); 
				
				//Make it into an MMRProof..
				MMRProof scproof = MMRProof.convertMiniDataVersion(proofdata);
				
				//Create a ScriptProof..
				scprf = new ScriptProof(exscript, scproof);
			}
			
			//Add to the Witness..
			witness.addScript(scprf);
		}
		
		//Output the current trans..
		ret.put("response", db.getTransactionRow(id).toJSON());
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new txnscript();
	}

}
