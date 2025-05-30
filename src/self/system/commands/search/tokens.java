package org.self.system.commands.search;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.objects.Token;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.brains.TxPoWSearcher;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;

public class tokens extends Command {

	public tokens() {
		super("tokens","(tokenid:) (action:import|export) (data:) - List, import or export tokens on the chain");
	}
	
	@Override
	public String getFullHelp() {
		return "\ntokens\n"
				+ "\n"
				+ "List all tokens in the unpruned chain.\n"
				+ "\n"
				+ "Optionally import or export tokens to share token data.\n"
				+ "\n"
				+ "tokenid: (optional)\n"
				+ "    The tokenid of the token to search for or export.\n"
				+ "\n"
				+ "action: (optional)\n"
				+ "    import : List your existing public keys.\n"
				+ "    export : Create a new key.\n"
				+ "\n"
				+ "data: (optional)\n"
				+ "    The data of the token to import, generated from the export.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "tokens\n"
				+ "\n"
				+ "tokens tokenid:0xFED5..\n"
				+ "\n"
				+ "tokens action:export tokenid:0xFED5..\n"
				+ "\n"
				+ "tokens action:import data:0x000..\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"tokenid","action","data"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		String tokenid = getParam("tokenid","");
		String action  = getParam("action", "");
		
		if(action.equals("export")) {
			
			//Export a token..
			Token tok = TxPoWSearcher.getToken(new MiniData(tokenid));
			if(tok == null) {
				throw new CommandException("Token not found : "+tokenid);
			}
			
			//Ok  - now convert to MiniData..
			MiniData tokdata = MiniData.getMiniDataVersion(tok);
			
			JSONObject resp = new JSONObject();
			resp.put("tokenid", tokenid);
			resp.put("data", tokdata.to0xString());
			ret.put("response", resp);
		
		}else if(action.equals("import")) {
			
			String data 		= getParam("data");
			MiniData tokendata 	= new MiniData(data);
			Token newtok 		= Token.convertMiniDataVersion(tokendata);
			
			//Add this..
			TxPoWSearcher.importToken(newtok);
			
			JSONObject resp = new JSONObject();
			resp.put("token", newtok.toJSON());
			ret.put("response", resp);
			
		}else {
			
			if(tokenid.equals("")) {
				
				//The return array
				JSONArray toksarr = new JSONArray();
				
				//First add Self..
				JSONObject self = new JSONObject();
				self.put("name", "Self");
				self.put("tokenid", "0x00");
				self.put("total", "1000000000");
				self.put("decimals", MiniNumber.MAX_DECIMAL_PLACES);
				self.put("scale", 1);
				toksarr.add(self);
			
				//Get ALL the tokens in the chain..
				ArrayList<Token> alltokens = TxPoWSearcher.getAllTokens();
				
				for(Token tok : alltokens) {
					//Add to our list
					toksarr.add(tok.toJSON());
				}
				
				ret.put("response", toksarr);
			
			}else {
				
				//Search for one token..
				Token tok = TxPoWSearcher.getToken(new MiniData(tokenid));
				if(tok == null) {
					throw new CommandException("Token not found : "+tokenid);
				}
				ret.put("response", tok.toJSON());
				
			}
		}
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new tokens();
	}

}
