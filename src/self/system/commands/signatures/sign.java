package org.self.system.commands.signatures;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.wallet.Wallet;
import org.self.objects.base.MiniData;
import org.self.objects.keys.Signature;
import org.self.system.commands.Command;
import org.self.utils.json.JSONObject;

public class sign extends Command {

	public sign() {
		super("sign","[publickey:] [data:] - Sign the data with the publickey");
	}
	
	@Override
	public String getFullHelp() {
		return "\nsign\n"
				+ "\n"
				+ "Sign the data with the publickey.\n"
				+ "\n"
				+ "Returns the signature of the data, signed with the corresponding private key.\n"
				+ "\n"
				+ "data:\n"
				+ "    The 0x HEX data to sign.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "sign data:0xCD34..\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"publickey","data"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
	
		MiniData data = getDataParam("data");
		MiniData pubk = getDataParam("publickey");
		
		//Get the Key row..
		Wallet wallet = SelfDB.getDB().getWallet();
		
		//Use the wallet..
		Signature signature = wallet.signData(pubk.to0xString(), data);
		
		ret.put("response", MiniData.getMiniDataVersion(signature).to0xString());
	
		return ret;
	}

	@Override
	public Command getFunction() {
		return new sign();
	}

}
