package org.self.system.commands.send.wallet;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.system.commands.CommandRunner;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;

public class sendfrom extends Command {
	
	public sendfrom() {
		super("sendfrom","[fromaddress:] [address:] [amount:] (tokenid:) [script:] [privatekey:] [keyuses:] (burn:) (mine:) - Send Self or Tokens from a certain address");
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"fromaddress","address",
				"amount","tokenid","script","privatekey","keyuses","mine","burn"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
	
		//From which address
		String fromaddress 	= getAddressParam("fromaddress");
		String toaddress 	= getAddressParam("address");
		MiniNumber amount 	= getNumberParam("amount");
		String tokenid 		= getAddressParam("tokenid", "0x00");
		
		//Get the BURN
		MiniNumber burn 	= getNumberParam("burn",MiniNumber.ZERO);
		if(burn.isMore(MiniNumber.ZERO) && !tokenid.equals("0x00")) {
			throw new CommandException("Currently BURN on precreated transactions only works for Self.. tokenid:0x00.. not tokens.");
		}
		
		//Thew script of the address
		String script 		= getParam("script");
		
		//The private key we need to sign with
		String privatekey	= getAddressParam("privatekey");
		MiniNumber keyuses  = getNumberParam("keyuses");
		
		//ID of the custom transaction
		String randomid 	= MiniData.getRandomData(32).to0xString();
		
		//Are we mining
		boolean mine 		= getBooleanParam("mine", true);
		
		//Now construct the transaction..
		JSONObject result 	= runCommand("txncreate id:"+randomid);
		
		//Add the mounts..
		String command 		= "txnaddamount id:"+randomid+" burn:"+burn+" fromaddress: "+fromaddress+" address:"+toaddress+" amount:"+amount+" tokenid:"+tokenid;
		result = runCommand(command);
		if(!(boolean)result.get("status")) {
			
			//Delete transaction
			runCommand("txndelete id:"+randomid);
			
			//Not enough funds!
			throw new CommandException(result.getString("error"));
		}
		
		//Add the scripts..
		runCommand("txnscript id:"+randomid+" scripts:{\""+script+"\":\"\"}");
		
		//Sort the MMR
		runCommand("txnmmr id:"+randomid);
		
		//Now SIGN
		runCommand("txnsign id:"+randomid+" publickey:custom privatekey:"+privatekey+" keyuses:"+keyuses);
		
		//And POST!
		result = runCommand("txnpost id:"+randomid+" mine:"+mine);
		
		//And delete..
		runCommand("txndelete id:"+randomid);
		
		//And return..
		ret.put("response", result.get("response"));
		
		return ret;
	}
	
	public JSONObject runCommand(String zCommand) {
		JSONArray res 		= CommandRunner.getRunner().runMultiCommand(zCommand);
		JSONObject result 	= (JSONObject) res.get(0);
		return result;
	}

	@Override
	public Command getFunction() {
		return new sendfrom();
	}	
}