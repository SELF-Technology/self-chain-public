package org.self.system.commands.base;

import org.self.system.commands.Command;
import org.self.utils.json.JSONObject;

public class automine extends Command {

	public automine() {
		super("automine","[enable:true|false|single] - Simulate traffic");
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		String enable = getParam("enable","");
		
//		if(enable.equals("single")) {
//			//Send 1 mine message
//			SELFSystem.getInstance().getTxPoWMiner().PostMessage(TxPoWMiner.TXPOWMINER_MINEPULSE);
//			
//			ret.put("message", "Mining Single PULSE TxPoW");
//		
//		}else if(enable.equals("true")) {
//			GeneralParams.AUTOMINE = true;
//			
//		}else if(enable.equals("false")) {
//			GeneralParams.AUTOMINE = false;
//			
//		}
//		
//		JSONObject mine = new JSONObject();
//		mine.put("enabled", GeneralParams.AUTOMINE);
//		
//		ret.put("response", mine);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new automine();
	}

}
