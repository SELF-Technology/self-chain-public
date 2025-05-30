package org.self.system.commands.base;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.system.commands.Command;
import org.self.system.params.GeneralParams;
import org.self.utils.json.JSONObject;

public class debugflag extends Command {

	public debugflag() {
		super("debugflag","(activate:true|false) (var:) - Set DEBUG flag for testing code..");
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"activate","var"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception{
		JSONObject ret = getJSONReply();

		String txpowon = getParam("activate", "false");
		
		//For now - stop accepting 
		if(txpowon.equals("true")) {
			//Accept txpow messages
			GeneralParams.DEBUGFLAG = true;
		}else {
			//Don't accept txpow messages - pulse does that
			GeneralParams.DEBUGFLAG = false;
		}
		
		//Is there a variable as well..
		if(existsParam("var")) {
			GeneralParams.DEBUGVAR = getParam("var");
		}
		
		JSONObject resp = new JSONObject();
		resp.put("debug", GeneralParams.DEBUGFLAG);
		resp.put("var", GeneralParams.DEBUGVAR);
		
		//Add balance..
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new debugflag();
	}

}
