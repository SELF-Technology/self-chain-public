package org.self.system.commands.network;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.system.network.webhooks.NotifyManager;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;

public class webhooks extends Command {
	
	public webhooks() {
		super("webhooks","(action:list|add|remove|clear) (hook:url) - Add a web hook that is called with Self events as they happen");
	}
	
	@Override
	public String getFullHelp() {
		return "\nwebhooks\n"
				+ "\n"
				+ "Add a web hook that is called with Self events as they happen.\n"
				+ "\n"
				+ "POST requests, so the URL must be a POST endpoint.\n"
				+ "\n"
				+ "action: (optional)\n"
				+ "    list : List your existing webhooks. The default.\n"
				+ "    add : Add a new webhook. \n"
				+ "    remove : Remove an existing webhook.\n"
				+ "    clear : Clear the existing webhooks.\n"
				+ "\n"
				+ "hook: (optional)\n"
				+ "    A URL, must be a POST endpoint.\n"
				+ "\n"
				+ "filter: (optional)\n"
				+ "    Filters which events get posted.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "webhooks action:list\n"
				+ "\n"
				+ "webhooks action:add hook:http://127.0.0.1/myapi.php\n"
				+ "\n"
				+ "webhooks action:remove hook:http://127.0.0.1/myapi.php\n"
				+ "\n"
				+ "webhooks action:add hook:http://127.0.0.1/myapi.php filter:MINING\n"
				+ "\n"
				+ "webhooks action:clear\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"enable","action","hook","filter"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception{
		JSONObject ret = getJSONReply();
		
		//get the data
		String action = getParam("action", "list");
		
		//Get the Notify Manager
		NotifyManager notify = SELFSystem.getInstance().getNotifyManager();
		
		//The response..
		JSONObject resp = new JSONObject();
		ret.put("response", resp);
		
		if(action.equals("add")) {
			
			String filter = getParam("filter","");
			String hook = getParam("hook");
			
			String fullhook=filter+"#"+hook;
			
			notify.addHook(fullhook);
			
		}else if(action.equals("remove")) {
			
			String hook = getParam("hook");
			notify.removeHook(hook);
		
		}else if(action.equals("clear")) {
			notify.clearHooks();
			
		}else if(action.equals("errorlogs")) {
			
			boolean enable = getBooleanParam("enable");
			
			SELFSystem.getInstance().getNotifyManager().WEBHOOKS_ERROR_LOGS = enable;
		}
		
		//List all the current hooks
		ArrayList<String> hooks = notify.getAllWebHooks();
		JSONArray arr = new JSONArray();
		for(String hook : hooks) {
			arr.add(hook);
		}
		
		resp.put("webhooks", arr);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new webhooks();
	}

}
