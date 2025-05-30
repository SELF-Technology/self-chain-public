package org.self.system.commands.base;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.database.userprefs.UserDB;
import org.self.system.commands.Command;
import org.self.utils.json.JSONObject;

public class magic extends Command {

	public magic() {
		super("magic","(selfscript:) (txpowsize:) (txnsperblock:) - Set the Magic numbers that define the Self network overall capacity");
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"selfscript","txpowsize","txnsperblock"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception{
		JSONObject ret = getJSONReply();

		JSONObject resp = new JSONObject();
		
		UserDB udb = SelfDB.getDB().getUserDB();
		
		if(existsParam("selfscript")) {
			//Set this as your SELFScript opcodes..
			udb.setMagicDesiredSELFScript(getNumberParam("selfscript"));
		}
		
		if(existsParam("txpowsize")) {
			//Set this as your SELFScript opcodes..
			udb.setMagicMaxTxPoWSize(getNumberParam("txpowsize"));
		}
		
		if(existsParam("txnsperblock")) {
			//Set this as your SELFScript opcodes..
			udb.setMagicMaxTxns(getNumberParam("txnsperblock"));
		}
		
		//Get the Tip..
		TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
		resp.put("lastblock", tip.getTxPoW().getMagic().toJSON());
		
		JSONObject desired = new JSONObject();
		desired.put("selfscript", udb.getMagicDesiredSELFScript());
		desired.put("txpowsize", udb.getMagicMaxTxPoWSize());
		desired.put("txnsperblock", udb.getMagicMaxTxns());
		resp.put("desired", desired);
		
		//Add balance..
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new magic();
	}

}
