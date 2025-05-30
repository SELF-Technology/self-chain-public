package org.self.system.commands.txn;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.userprefs.txndb.TxnDB;
import org.self.database.userprefs.txndb.TxnRow;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONObject;

public class txnclear extends Command {

	public txnclear() {
		super("txnclear","[id:] - Clear ALL the Witness data");
	}
	
	@Override
	public String getFullHelp() {
		return "\ntxnclear\n"
				+ "\n"
				+ "Clear ALL the Witness data - signatures, mmr proofs and script proofs.\n"
				+ "\n"
				+ "id:\n"
				+ "    The id of the transaction to clear.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "txnclear id:multisig\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"id"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();

		TxnDB db = SelfDB.getDB().getCustomTxnDB();
		
		String id 	= getParam("id");
		
		//Get the Transaction..
		TxnRow txnrow 	= db.getTransactionRow(getParam("id"));
		if(txnrow == null) {
			throw new CommandException("Transaction not found : "+id);
		}
		
		txnrow.getTransaction().clearIsMonotonic();
		txnrow.clearWitness();
		
		JSONObject resp = new JSONObject();
		ret.put("response", txnrow.toJSON());
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new txnclear();
	}

}
