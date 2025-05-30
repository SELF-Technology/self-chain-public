package org.self.system.commands.mds;

import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.utils.json.JSONObject;

public class checkrestore extends Command {

	public checkrestore() {
		super("checkrestore","Check if the system is restoring");
	}
	
	@Override
	public String getFullHelp() {
		return  "checkrestore\n"
				+ "\n"
				+ "Check if Self is restoring\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "checkrestore\n";
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		//Who called it
		String minidappid = getMiniDAPPID();
		
		JSONObject resp = new JSONObject();
		resp.put("restoring", SELFSystem.getInstance().isRestoring());
		resp.put("shuttingdown", SELFSystem.getInstance().isShuttingDown());
		resp.put("complete", SELFSystem.getInstance().isShutdownComplete());
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new checkrestore();
	}

}
