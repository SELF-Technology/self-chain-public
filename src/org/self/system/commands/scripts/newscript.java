package org.self.system.commands.scripts;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.wallet.ScriptRow;
import org.self.database.wallet.Wallet;
import org.self.selfscript.Contract;
import org.self.objects.Address;
import org.self.system.commands.Command;
import org.self.utils.json.JSONObject;

public class newscript extends Command {

	public newscript() {
		super("newscript","[script:] [trackall:false|true] (clean:false|true) - Add a new custom script. Track ALL addresses or just ones with relevant state variables.");
	}
	
	@Override
	public String getFullHelp() {
		return "\nnewscript\n"
				+ "\n"
				+ "Add a new custom script.\n"
				+ "\n"
				+ "Track ALL coins with this script address or just ones with state variables relevant to you.\n"
				+ "\n"
				+ "script:\n"
				+ "    The script to add to your node.\n"
				+ "    Your node will then know about coins with this script address.\n"
				+ "\n"
				+ "trackall:\n"
				+ "    true or false, true will track all coins with this script address.\n"
				+ "    false will only track coins with this script address that are relevant to you.\n"
				+ "\n"
				+ "clean: (optional)\n"
				+ "    true or false, true will clean the script to its selfl correct representation.\n"
				+ "    Default is false.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "newscript trackall:true script:\"RETURN SIGNEDBY(0x1539..) AND SIGNEDBY(0xAD25..)\"\n"
				+ "\n"
				+ "newscript trackall:false script:\"RETURN (@BLOCK GTE PREVSTATE(1) OR @COINAGE GTE PREVSTATE(4)) AND VERIFYOUT(@INPUT PREVSTATE(2) @AMOUNT @TOKENID FALSE)\"\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"script","trackall","clean"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception{
		JSONObject ret = getJSONReply();
		
		//Get the wallet..
		Wallet wallet = SelfDB.getDB().getWallet();
		
		//Get the script
		String script = getParam("script");
		boolean track = getBooleanParam("trackall");
		boolean clean = getBooleanParam("clean",false);
		
		//Clean the script
		if(clean) {
			script = Contract.cleanScript(script);
		}
		
		//Do we have this script already..
		Address addr = new Address(script);
		
		//Load it...
		ScriptRow scr = wallet.getScriptFromAddress(addr.getAddressData().to0xString());
		
		//If we have it remove it first
		if(scr!=null) {
			wallet.removeScript(addr.getAddressData().to0xString());
		}
		
		//Now add it to the DB
		ScriptRow krow = wallet.addScript(script, false, false, "0x00", track);
		
		//Put the details in the response..
		ret.put("response", krow.toJSON());
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new newscript();
	}

}
