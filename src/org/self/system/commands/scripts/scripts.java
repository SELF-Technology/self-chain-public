package org.self.system.commands.scripts;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.wallet.ScriptRow;
import org.self.database.wallet.Wallet;
import org.self.objects.Address;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;

public class scripts extends Command {

	public scripts() {
		super("scripts","(address:) - Search scripts / addresses");
	}
	
	@Override
	public String getFullHelp() {
		return "\nscripts\n"
				+ "\n"
				+ "List all scripts or search for a script / basic address your node is tracking.\n"
				+ "\n"
				+ "address: (optional)\n"
				+ "    Script address or basic address to search for. Can be 0x or Sx address.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "scripts\n"
				+ "\n"
				+ "scripts address:0xFED5..\n"
				+ "\n"
				+ "scripts address:SxG087..n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"address"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception{
		JSONObject ret = getJSONReply();
		
		//Get the wallet..
		Wallet wallet = SelfDB.getDB().getWallet();
		
		//Is there an address
		String address = getParam("address","");
		if(address.toLowerCase().startsWith("sx")) {
			//Convert back to normal hex..
			try {
				address = Address.convertSelfAddress(address).to0xString();
			}catch(IllegalArgumentException exc) {
				throw new CommandException(exc.toString());
			}
		}
		
		if(address.equals("")) {
			
			//Get all the custom scripts
			ArrayList<ScriptRow> allscripts = wallet.getAllAddresses();
			
			JSONArray arr = new JSONArray();
			for(ScriptRow kr : allscripts) {
				arr.add(kr.toJSON());
			}
				
			//Put the details in the response..
			ret.put("response", arr);
			
		}else {
			
			//Search for that address
			ScriptRow scrow = wallet.getScriptFromAddress(address);
			if(scrow == null) {
				throw new CommandException("Script with that address not found");
			}
			
			//Put the details in the response..
			ret.put("response", scrow.toJSON());
		}
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new scripts();
	}

}
