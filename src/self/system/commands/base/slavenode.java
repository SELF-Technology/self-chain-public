package org.self.system.commands.base;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.userprefs.UserDB;
import org.self.system.commands.Command;
import org.self.utils.json.JSONObject;

public class slavenode extends Command {

	public slavenode() {
		super("slavenode","Run in slavenode mode. Requires a master node to connect to.");
	}
	
	@Override
	public String getFullHelp() {
		return "\nslavenode\n"
				+ "\n"
				+ "Connect to a master node and receive txblock messages.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "slavenode enable:true host:87.34.45.56:9001\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"host","enable"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		//Need the UserDB
		UserDB udb 		= SelfDB.getDB().getUserDB();
		JSONObject resp = new JSONObject();
		
		if(existsParam("enable")) {
		
			//A restart is required for changes to take effect
			resp.put("message", "RESTART REQUIRED");
			
			boolean enable = getBooleanParam("enable");
			if(enable) {
				
				//Get the Host
				String host = getParam("host");
					
				//Set these properties..
				udb.setSlaveNode(enable, host);
			}else {
				udb.setSlaveNode(enable, "");
			}
		}
		
		//Save this
		SelfDB.getDB().saveUserDB();
		
		//Get the details..
		boolean slaveenable = udb.isSlaveNode();
		String masternode 	= udb.getSlaveNodeHost();
		
		resp.put("enabled", slaveenable);
		resp.put("master", masternode);
		
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new slavenode();
	}

}
