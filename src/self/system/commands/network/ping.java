package org.self.system.commands.network;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.objects.Greeting;
import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONObject;

public class ping extends Command {

	public ping() {
		super("ping","[host:] - Ping a host and get back Self Node info");
	}
	
	@Override
	public String getFullHelp() {
		return "\nping\n"
				+ "\n"
				+ "Ping a host and get back Self Node info.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "ping host:\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"host"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		String host = getParam("host");
		
		int index = host.indexOf(":");
		if(index == -1) {
			return null;
		}
		
		String ip 	 = host.substring(0,index).trim();
		String ports = host.substring(index+1).trim();
		
		int port = 0;
		try {
			port = Integer.parseInt(ports);
		}catch(NumberFormatException exc) {
			throw new CommandException("Invalid port : "+ports);
		}
		
		//Call the ping function..
		Greeting greet = SELFSystem.getInstance().getNIOManager().sendPingMessage(ip, port, false);
		JSONObject resp = new JSONObject();
		resp.put("host", ip);
		resp.put("port", port);
		
		if(greet == null) {
			resp.put("valid", false);
		}else {
			resp.put("valid", true);
			resp.put("version", greet.getVersion().toString());
			resp.put("extradata", greet.getExtraData());
		}
			
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new ping();
	}

}
