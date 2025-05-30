package org.self.system.network.rpc;

import java.util.Base64;

import org.self.database.SelfDB;
import org.self.database.userprefs.UserDB;
import org.self.objects.base.MiniString;
import org.self.system.params.GeneralParams;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;

public class Authorizer {

	public static JSONObject checkAuchCredentials(String zAuthHeader) {
		
		JSONObject falseret = new JSONObject();
		falseret.put("valid",false);
		
		JSONObject ret = new JSONObject();
		ret.put("valid",false);
				
		UserDB userdb = SelfDB.getDB().getUserDB();
		int rpcusers  = userdb.getRPCUsers().size();		
		
		//Are we BASIC checking
		if(GeneralParams.RPC_AUTHSTYLE.equals("basic")) {
			
			try {
				//Is it basic Auth
				int pos = zAuthHeader.indexOf("Basic ");
				if(pos!=-1) {
					String userpass = zAuthHeader.substring(pos+6);
					
					byte[] dec 		= Base64.getDecoder().decode(userpass);
					String decstr 	= new String(dec, MiniString.SELF_CHARSET).trim();
					
					//Get the 2 bits..
					int col 		= decstr.indexOf(":");
					String user 	= decstr.substring(0,col);
					String password = decstr.substring(col+1, decstr.length());
					
					ret.put("username",user);
					
					//Now check
					if(user.equals("self")) {
						if(!GeneralParams.RPC_AUTHENTICATE || password.equals(GeneralParams.RPC_PASSWORD)) {
							
							ret.put("valid",true);
							ret.put("mode","write");
							
							return ret;
						}
					
					}else {
						
						if(GeneralParams.RPC_AUTHENTICATE) {
						
							JSONArray users = userdb.getRPCUsers();
							for(Object userobj : users) {
								JSONObject rpcuser = (JSONObject)userobj;
								
								//Is it the one to be removed..
								if( rpcuser.getString("username").equals(user) && 
									rpcuser.getString("password").equals(password)) {
									
									ret.put("valid",true);
									ret.put("mode",rpcuser.getString("mode"));
								}
							}
							
							return ret;
						
						}else {
							//Cannot access extra users if no Auth for main self user
							SelfLogger.log("[!] Cannot access rpc user ("+user+") as no default password (for user self) set via -rpcpassword..");
						}
					}
				}
				
			}catch(Exception exc) {
				SelfLogger.log(exc);
				return falseret;
			}
		}
				
		return falseret;
	}
}
