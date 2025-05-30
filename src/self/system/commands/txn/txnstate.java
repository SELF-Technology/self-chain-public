package org.self.system.commands.txn;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.userprefs.txndb.TxnDB;
import org.self.database.userprefs.txndb.TxnRow;
import org.self.objects.StateVariable;
import org.self.objects.Transaction;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONObject;

public class txnstate extends Command {

	public txnstate() {
		super("txnstate","[id:] [port:] [value:] - Add a state variable");
	}
	
	@Override
	public String getFullHelp() {
		return "\ntxnstate\n"
				+ "\n"
				+ "Add a state variable to a transaction.\n"
				+ "\n"
				+ "id:\n"
				+ "    The id of the transaction.\n"
				+ "\n"
				+ "port:\n"
				+ "    Port number of the state variable, from 0-255.\n"
				+ "\n"
				+ "value:\n"
				+ "    Value for the state variable.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "txnstate id:multisig port:0 value:0xFED5..\n"
				+ "\n"
				+ "txnstate id:multisig port:1 value:100 \n"
				+ "\n"
				+ "txnstate id:multisig port:1 value:\"string\" \n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"id","port","value"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();

		TxnDB db = SelfDB.getDB().getCustomTxnDB();
		
		//The transaction
		String id 			= getParam("id");
		String port			= getParam("port");
		String value		= getParam("value");
		
		//Get the Transaction
		TxnRow txnrow 	= db.getTransactionRow(getParam("id"));
		if(txnrow == null) {
			throw new CommandException("Transaction not found : "+id);
		}
		Transaction trans = txnrow.getTransaction();
		
		//Create a state variable..
		StateVariable sv = new StateVariable(Integer.parseInt(port),value);
		
		//Add it to the transaction
		trans.addStateVariable(sv);
		
		//Calculate transid
		trans.calculateTransactionID();
				
		//Output the current trans..
		ret.put("response", db.getTransactionRow(id).toJSON());
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new txnstate();
	}

}
