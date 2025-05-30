package org.self.system.commands.base;

import org.self.database.SelfDB;
import org.self.database.wallet.ScriptRow;
import org.self.database.wallet.Wallet;
import org.self.system.commands.Command;
import org.self.utils.json.JSONObject;

public class newaddress extends Command {

	public newaddress() {
		super("newaddress","Create a new address that will not be not used for anything else (not a default change address)");
	}
	
	@Override
	public String getFullHelp() {
		return "\nnewaddress\n"
				+ "\n"
				+ "Create a new address that will not be not used for anything else (not one of the 64 default change address).\n"
				+ "\n"
				+ "Can be used for a specific use case or for improved privacy.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "newaddress\n";
	}
	
	@Override
	public JSONObject runCommand() throws Exception{
		JSONObject ret = getJSONReply();
		
		//Get the wallet..
		Wallet wallet = SelfDB.getDB().getWallet();
		
		//Create a new address - not a default address!
		ScriptRow srow = wallet.createNewSimpleAddress(false);
			
		//Put the details in the response..
		ret.put("response", srow.toJSON());
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new newaddress();
	}

}
