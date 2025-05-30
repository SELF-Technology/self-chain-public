package org.self.system.commands.maxima;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniString;
import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.system.network.maxima.MaximaManager;
import org.self.utils.encrypt.CryptoPackage;
import org.self.utils.encrypt.SignVerify;
import org.self.utils.json.JSONObject;

public class maxdecrypt extends Command {

	public maxdecrypt() {
		super("maxdecrypt","[data:] [privatekey:] - Decrypt data using a specific RSA Private key");
	}
	
	/*@Override
	public String getFullHelp() {
		return "\nmaxsign\n"
				+ "\n"
				+ "Sign a piece of data with your Maxima ID.\n"
				+ "\n"
				+ "Returns the signature of the data, signed with your Maxima private key or the specified key.\n"
				+ "\n"
				+ "data:\n"
				+ "    The 0x HEX data to sign.\n"
				+ "\n"
				+ "privatekey: (optional)\n"
				+ "    The 0x HEX data of the private key from maxcreate. Uses your Maxima ID otherwise.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "maxsign data:0xCD34..\n"
				+ "\n"
				+ "maxsign data:0xCD34.. privatekey:0x30819..\n";
	}*/
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"data","privatekey"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		//Is it a JSON
		MiniData data 		= getDataParam("data");
		
		//Is there a private key..
		MiniData privatekey = null;
		if(existsParam("privatekey")) {
			privatekey = getDataParam("privatekey");
		}else {
			privatekey = SELFSystem.getInstance().getMaxima().getPrivateKey();
		}
		
		//Encrypt the data..
		CryptoPackage cp = new CryptoPackage();
		cp.ConvertMiniDataVersion(data);
		byte[] dec = cp.decrypt(privatekey.getBytes());
		
		JSONObject resp = new JSONObject();
		
		resp.put("decrypted", new MiniData(dec).to0xString());
		
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new maxdecrypt();
	}

}
