package org.self.system.commands.mds;

import org.self.database.SelfDB;
import org.self.database.minidapps.MiniDAPP;
import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.utils.json.JSONObject;

public class checkmode extends Command {

	public checkmode() {
		super("checkmode","Show if this MiniDAPP is READ or WRITE mode");
	}
	
	@Override
	public String getFullHelp() {
		return  "checkmode\n"
				+ "\n"
				+ "Show if a MiniDAPP is READ or WRITE mode\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "checkmode\n";
	}
	
//	@Override
//	public ArrayList<String> getValidParams(){
//		return new ArrayList<>(Arrays.asList(new String[]{"uid"}));
//	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		//Who called it
		String minidappid = getMiniDAPPID();
		
		JSONObject resp = new JSONObject();
		if(minidappid.equals("0x00")) {
			
			resp.put("name", "SELF");
			resp.put("mode", "WRITE");
			resp.put("public", false);
			resp.put("untrustedmdsuid", SELFSystem.getInstance().getMDSManager().getUntrustedMiniDAPPSessionID());
			resp.put("writemode", true);
		
		}else if(minidappid.equals(SELFSystem.getInstance().getMDSManager().getPublicMiniDAPPID())) {
			
			resp.put("name", "PUBLICMDS");
			resp.put("mode", "READ");
			resp.put("public", true);
			resp.put("untrustedmdsuid", SELFSystem.getInstance().getMDSManager().getUntrustedMiniDAPPSessionID());
			resp.put("writemode", false);
		
		}else if(minidappid.equals(SELFSystem.getInstance().getMDSManager().getUntrustedMiniDAPPID())) {
			
			resp.put("name", "RESTRICTEDMDS");
			resp.put("mode", "READ");
			resp.put("public", false);
			resp.put("untrustedmdsuid", SELFSystem.getInstance().getMDSManager().getUntrustedMiniDAPPSessionID());
			resp.put("writemode", false);
		
		}else{
			//Get that MiniDAPP..
			MiniDAPP md = SelfDB.getDB().getMDSDB().getMiniDAPP(minidappid);
			
			//Return the result
			resp.put("name", md.getName());
			resp.put("mode", md.getPermission().toUpperCase());
			resp.put("public", false);
			resp.put("untrustedmdsuid", SELFSystem.getInstance().getMDSManager().getUntrustedMiniDAPPSessionID());
			resp.put("writemode", md.getPermission().equalsIgnoreCase("write"));
		}
		
		resp.put("dblocked",!SelfDB.getDB().getWallet().isBaseSeedAvailable());
		
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new checkmode();
	}

}
