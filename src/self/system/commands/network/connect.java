package org.self.system.commands.network;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.system.network.self.NIOManager;
import org.self.utils.json.JSONObject;
import org.self.utils.messages.Message;

public class connect extends Command {

	public connect() {
		super("connect","[host:ip:port] - Connect to a network Self instance");
	}
	
	@Override
	public String getFullHelp() {
		return "\nconnect\n"
				+ "\n"
				+ "Connect to a network Self instance.\n"
				+ "\n"
				+ "Connect to another node to join the main network or to create a private test network.\n"
				+ "\n"
				+ "Set your own host using the -host parameter at start up.\n"
				+ "\n"
				+ "host:\n"
				+ "    The external ip:port of the node to connect to.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "connect host:94.0.239.117:9001\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"host"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		//Get the host:port
		String fullhost = (String)getParams().get("host");
		if(fullhost == null) {
			throw new Exception("No host specified");
		}
		
		//Create the Message
		Message connect = createConnectMessage(fullhost);
		if(connect == null) {
			throw new Exception("Must specify host:port");
		}
		
		SELFSystem.getInstance().getNIOManager().PostMessage(connect);
		
		ret.put("message", "Attempting to connect to "+fullhost);
	
		return ret;
	}

	public static Message createConnectMessage(String zFullHost) {
		//IP and PORT
		int index = zFullHost.indexOf(":");
		if(index == -1) {
			return null;
		}
		
		String ip 	 = zFullHost.substring(0,index).trim();
		String ports = zFullHost.substring(index+1).trim();
		
		int port = 0;
		try {
			port = Integer.parseInt(ports);
		}catch(NumberFormatException exc) {
			return null;
		}
		
		//Post a message
		Message msg = new Message(NIOManager.NIO_CONNECT);
		msg.addString("host", ip);
		msg.addInteger("port", port);
		
		return msg;
	}
	
	@Override
	public Command getFunction() {
		return new connect();
	}

}
