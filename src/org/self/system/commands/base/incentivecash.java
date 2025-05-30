package org.self.system.commands.base;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.system.commands.Command;
import org.self.system.params.GlobalParams;
import org.self.utils.RPCClient;
import org.self.utils.json.JSONObject;
import org.self.utils.json.parser.JSONParser;

public class incentivecash extends Command {

	public incentivecash() {
		super("incentivecash","(uid:) - Show your rewards or specify your UserID for the Incentive Cash program");
	}
	
	@Override
	public String getFullHelp() {
		return "\nincentivecash\n"
				+ "\n"
				+ "Returns your Incentive Program Rewards balance and full breakdown of daily, invite and community Rewards.\n"
				+ "\n"
				+ "Set your Incentive ID with the 'uid' parameter to start receiving daily Rewards.\n"
				+ "\n"
				+ "uid: (optional)\n"
				+ "    Your Incentive Program ID, can be found by logging into the incentive.self.global website.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "incentivecash\n"
				+ "\n"
				+ "incentivecash uid:00d11b34-7b47-45f3-775c-a37cbe4c9ff3\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"uid"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		if(existsParam("uid")) {
			String uid = getParam("uid");
			
			//Set this in the UserDB
			SelfDB.getDB().getUserDB().setIncentiveCashUserID(uid);
			
			//Save this..
			SelfDB.getDB().saveUserDB();
		}

		//Get the User
		String user = SelfDB.getDB().getUserDB().getIncentiveCashUserID();
		
		//Show the details..
		JSONObject ic = new JSONObject();
		ic.put("uid", user);
				
		//Make sure there is a User specified
		if(!user.equals("")) {
			//Call the RPC End point..
			String reply = RPCClient.sendPUT("https://incentivecash.self.global/api/ping/" + user + "?version="+ GlobalParams.SELF_VERSION);
			
			//Convert response..
			JSONObject json = (JSONObject) new JSONParser().parse(reply);
			
			ic.put("details", json);
		}
		
		ret.put("response", ic);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new incentivecash();
	}

}
