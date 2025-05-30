package org.self.system.commands.base;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.utils.json.JSONObject;

public class quit extends Command {

	public quit() {
		super("quit","(compact:) - Shutdown Self. Compact the Databases if you want");
	}
	
	@Override
	public String getFullHelp() {
		return "\nquit\n"
				+ "\n"
				+ "Shutdown Self safely.\n"
				+ "\n"
				+ "Ensure you have a backup before shutting down.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "quit\n"
				+ "\n"
				+ "quit compact:true\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"compact"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		boolean compact = getBooleanParam("compact", false);
		
		if(SELFSystem.getInstance()!=null) {
			SELFSystem.getInstance().shutdown(compact);
		}
		
		ret.put("message", "Shutdown complete");
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new quit();
	}

}
