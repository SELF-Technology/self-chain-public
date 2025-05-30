package org.self.system.commands.network;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.system.network.NetworkManager;
import org.self.system.network.self.NIOClientInfo;
import org.self.system.params.GeneralParams;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;

public class network extends Command {

	public network() {
		super("network","(action:list|reset|recalculateip) - Show network status or reset traffic counter");
	}
	
	@Override
	public String getFullHelp() {
		return "\nnetwork\n"
				+ "\n"
				+ "Show network status or reset traffic counter.\n"
				+ "\n"
				+ "action: (optional)\n"
				+ "    list : List the direct peers you are connected to. The default.\n"
				+ "    reset : Restart the traffic counter from 0.\n"
				+ "    recalculateip : Reset your IP - when you move to a different WiFi.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "network\n"
				+ "\n"
				+ "network action:list\n"
				+ "\n"
				+ "network action:reset\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"action"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		String action = getParam("action", "list");
		
		if(action.equals("list")) {
			
			String uid = getParam("uid", "");
			
			//Get the NIO Details
			ArrayList<NIOClientInfo> clients = SELFSystem.getInstance().getNetworkManager().getNIOManager().getAllConnectionInfo();
			
			//Create a JSONArray
			JSONArray clarr = new JSONArray();
			for(NIOClientInfo info : clients) {
				if(uid.equals("")) {
					clarr.add(info.toJSON());
				}else if(uid.equals(info.getUID())){
					clarr.add(info.toJSON());
					break;
				}
			}
			
			JSONObject resp = new JSONObject();
			resp.put("connections", clarr);
			
			//Network..
			NetworkManager netmanager = SELFSystem.getInstance().getNetworkManager();
			if(netmanager!=null) {
				resp.put("details", netmanager.getStatus(true));
			}
			
			//Add to the response
			ret.put("response", resp);
			
		}else if(action.equals("reset")) {
			
			SELFSystem.getInstance().getNIOManager().getTrafficListener().reset();
			ret.put("response", "Traffic counter restarted..");
		
		}else if(action.equals("recalculateip")) {
			
			SELFSystem.getInstance().getNetworkManager().calculateHostIP();
			
			JSONObject ip = new JSONObject();
			ip.put("ip", GeneralParams.SELF_HOST);
			
			ret.put("response", ip);
			
		}else if(action.equals("restart")) {
			
			//Send the message to restart the network
			SELFSystem.getInstance().PostMessage(SELFSystem.SELF_NETRESTART);
			
			//Add to the response
			ret.put("response", "Restarting..");
		
		}else if(action.equals("loggingon")) {
		
			SELFSystem.getInstance().getNetworkManager().getNIOManager().setFullLogging(true, "");
			SELFSystem.getInstance().getNetworkManager().getP2PManager().setFullLogging(true, "");
			
			//Add to the response
			ret.put("response", "Full Network logging ON");
		
		}else if(action.equals("loggingoff")) {
			
			SELFSystem.getInstance().getNetworkManager().getNIOManager().setFullLogging(false, "");
			SELFSystem.getInstance().getNetworkManager().getP2PManager().setFullLogging(false, "");
			
			//Add to the response
			ret.put("response", "Full Network logging OFF");
		
		}else {
			throw new CommandException("Invalid action");
		}
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new network();
	}

}
