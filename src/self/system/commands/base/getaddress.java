package org.self.system.commands.base;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.wallet.ScriptRow;
import org.self.database.wallet.Wallet;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.system.commands.search.keys;
import org.self.utils.json.JSONObject;

public class getaddress extends Command {

	public getaddress() {
		super("getaddress","Get one of your default Self addresses");
	}
	
	@Override
	public String getFullHelp() {
		return "\ngetaddress\n"
				+ "\n"
				+ "Returns an existing default Self address to receive funds, use as a change address etc.\n"
				+ "\n"
				+ "Each address can be used securely 262144 (64^3) times.\n"
				+ "\n"
				+ "Then you can wipe the private keys from your online node using the 'vault' command.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "getaddress\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception{
		JSONObject ret = getJSONReply();
		
		//Get the wallet..
		Wallet wallet = SelfDB.getDB().getWallet();
		
		//Are we creating them all..
		if(existsParam("createall")) {
//			
//			SelfLogger.log("Creating all remaining keys..");
//			
//			//Create all remaining addresses..
//			wallet.initDefaultKeys(Wallet.NUMBER_GETADDRESS_KEYS, true);
//			
//			ret.put("response", "All keys created..");
			
		}else {
		
			String type = getParam("type","single");
			
			//Get an existing address
			ScriptRow scrow = wallet.getDefaultAddress();
			
			//Get the key row.. THIS is a fix for an issue where backup saved with wrong seed phrase
			if(SelfDB.getDB().getWallet().isBaseSeedAvailable()) {
				if(!keys.checkKey(scrow.getPublicKey())) {
					throw new CommandException("[!] SERIOUS ERROR - INCORRECT Public key : "+scrow.getPublicKey());
				}
			}
			
			//Put the details in the response..
			ret.put("response", scrow.toJSON());
		}
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new getaddress();
	}

}
