package org.self.system.mds.handler;

import org.self.system.SELFSystem;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONObject;

public class NOTIFYcommand {

	String mMiniDAPPID;
	String mName;
	String mText;
	boolean mShow;
	
	public NOTIFYcommand(String zMiniDAPPID, String zMiniDappName, String zText, boolean zShow) {
		mMiniDAPPID 	= zMiniDAPPID;
		mName			= zMiniDappName;
		mText			= zText;
		mShow			= zShow;
	}
	
	public String runCommand() {
		
		//Create a notification
		JSONObject notification = new JSONObject();
		notification.put("uid", mMiniDAPPID);
		notification.put("title", mName);
		notification.put("text", mText);
		notification.put("show", mShow);
		
		//Log it in the console
		SelfLogger.log("Notification : "+notification.toString());
		
		//Post it
		SELFSystem.getInstance().PostNotifyEvent("NOTIFICATION", notification);
		
		JSONObject stattrue = new JSONObject();
		stattrue.put("command", "Notification");
		stattrue.put("status", true);
		stattrue.put("pending", false);
		stattrue.put("response", notification);
		String result = stattrue.toJSONString();
		
		return result;
	}
	

}
