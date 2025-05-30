package org.self.system.commands.scripts;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.wallet.Wallet;
import org.self.system.commands.Command;
import org.self.utils.json.JSONObject;

public class removescript extends Command {

	public removescript() {
		super("removescript","[address:] - Remove a script from your DB");
	}
	
	@Override
	public String getFullHelp() {
		return "\removescript\n"
				+ "\n"
				+ "Remove a custom script. BE CAREFUL not to remove a script you need.\n"
				+ "\n"
				+ "address:\n"
				+ "    The address of the script.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "removescript address:0xFFE678768CDE.."
				+ "\n"
				+ "removescript address:SxFFE678768CDE..";
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
		
		//Get the script
		String address = getAddressParam("address");
		
		//Remove it..
		wallet.removeScript(address);
		
		//Put the details in the response..
		ret.put("response", "Script removed");
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new removescript();
	}

}
