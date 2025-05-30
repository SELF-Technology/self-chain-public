package org.self.system.commands.txn;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.userprefs.txndb.TxnDB;
import org.self.database.userprefs.txndb.TxnRow;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;

public class txnlist extends Command {

	public txnlist() {
		super("txnlist","(id:) (transactiononly:) - List current custom transactions");
	}
	
	@Override
	public String getFullHelp() {
		return "\ntxnlist\n"
				+ "\n"
				+ "List your custom transactions. Includes previously posted transactions.\n"
				+ "\n"
				+ "Returns the full details of transactions.\n"
				+ "\n"
				+ "id: (optional)\n"
				+ "    The id of a single transaction to list.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "txnlist\n"
				+ "\n"
				+ "txnlist id:multisig\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"id","transactiononly"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();

		TxnDB db = SelfDB.getDB().getCustomTxnDB();
		
		//The transaction
		String id = getParam("id","");
		
		boolean transonly = getBooleanParam("transactiononly",false);
		
		if(id.equals("")) {
			//The transaction
			ArrayList<TxnRow> txns = db.listTxns();
			
			JSONArray arr = new JSONArray();
			for(TxnRow txnrow : txns) {
				arr.add(txnrow.toJSON(!transonly));
			}
			
			ret.put("response", arr);
		}else {
			TxnRow txnrow 	= db.getTransactionRow(getParam("id"));
			if(txnrow == null) {
				throw new CommandException("Transaction not found : "+id);
			}
			
			ret.put("response", txnrow.toJSON(!transonly));
		}
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new txnlist();
	}

}
