package org.self.system.commands.maxima;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.objects.base.MiniData;
import org.self.objects.base.MiniString;
import org.self.system.commands.Command;
import org.self.utils.encrypt.SignVerify;
import org.self.utils.json.JSONObject;

public class maxverify extends Command {

	public maxverify() {
		super("maxverify","[data:] [publickey:] [signature:] - Verify data with a Maxima Public key");
	}
	
	@Override
	public String getFullHelp() {
		return "\nmaxverify\n"
				+ "\n"
				+ "Verify data with a Maxima public key. Returns valid true or false.\n"
				+ "\n"
				+ "data:\n"
				+ "    The 0x HEX data to verify the signature for.\n"
				+ "\n"
				+ "publickey:\n"
				+ "    The Maxima public key of the signer.\n"
				+ "\n"
				+ "signature:\n"
				+ "    The signature of the data.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "maxverify data:0xCD34.. publickey:0xFED5 signature:0x4827..\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"data","publickey","signature"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		//Is it a JSON
		String datastr = null;
		if(isParamJSONObject("data")) {
			datastr = getJSONObjectParam("data").toString();
		}else if(isParamJSONArray("data")) {
			datastr = getJSONArrayParam("data").toString();
		}else {
			datastr = getParam("data");
		}
		
		MiniData data = null;
		if(datastr.startsWith("0x")) {
			data = new MiniData(datastr);
		}else {
			data = new MiniData(new MiniString(datastr).getData());
		}
		
		MiniData pubk = getDataParam("publickey");
		MiniData sign = getDataParam("signature");
		
		boolean valid = SignVerify.verify(pubk.getBytes(), data.getBytes(), sign.getBytes());
		
		JSONObject resp = new JSONObject();
		
		resp.put("valid", valid);
		
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new maxverify();
	}

}
