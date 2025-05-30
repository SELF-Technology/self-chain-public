package org.self.system.commands.txn;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.objects.TxPoW;
import org.self.objects.base.MiniData;
import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.utils.json.JSONObject;
import org.self.utils.messages.Message;

public class txnminepost extends Command {

	public txnminepost() {
		super("txnminepost","[data:] - Post a pre-mined transaction");
	}
	
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"data"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();

		MiniData txdata = getDataParam("data");
		
		//Convert to a TXPOW
		TxPoW txp = TxPoW.convertMiniDataVersion(txdata);
		
		//Now Post it!
		SELFSystem.getInstance().PostMessage(new Message(SELFSystem.SELF_TXPOWMINED).addObject("txpow", txp));
		
		//Return the MINED txn..
		JSONObject resp = new JSONObject();
		resp.put("data", txp.toJSON());
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new txnminepost();
	}

}
