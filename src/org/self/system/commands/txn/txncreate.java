package org.self.system.commands.txn;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.userprefs.txndb.TxnDB;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONObject;

public class txncreate extends Command {

	public txncreate() {
		super("txncreate","[id:] - Create a transaction");
	}
	
	@Override
	public String getFullHelp() {
		return "\ntxncreate\n"
				+ "\n"
				+ "Create a custom transaction.\n"
				+ "\n"
				+ "The first step before defining the inputs and outputs.\n"
				+ "\n"
				+ "id:\n"
				+ "    Create an id for the transaction.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "txncreate id:multisig\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"id"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		
		TxnDB db = SelfDB.getDB().getCustomTxnDB();
		
		//The transaction
		String id = getParam("id");
		
		if(db.getTransactionRow(id) != null) {
			throw new CommandException("Txn with this ID already exists : "+id);
		}
		
		db.createTransaction(id);
		
		JSONObject ret = getJSONReply();
		ret.put("response", db.getTransactionRow(id).toJSON());
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new txncreate();
	}

}
