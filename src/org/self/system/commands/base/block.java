package org.self.system.commands.base;

import java.util.Date;

import org.self.database.SelfDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.database.txpowtree.TxPowTree;
import org.self.objects.TxPoW;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONObject;

public class block extends Command {

	public block() {
		super("block","Simply return the current top block");
	}
	
	@Override
	public String getFullHelp() {
		return "\nblock\n"
				+ "\n"
				+ "Return the top block\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "block\n";
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		//Get the top block..
		TxPowTree tree 		= SelfDB.getDB().getTxPoWTree();
		TxPoWTreeNode tip 	= tree.getTip();
		if(tip == null) {
			throw new CommandException("NO Blocks yet..");
		}
		
		//Get the top block
		TxPoW topblock 		= tip.getTxPoW();
		
		JSONObject resp = new JSONObject();
		resp.put("block", topblock.getBlockNumber().toString());
		resp.put("hash", topblock.getTxPoWID());
		resp.put("timemilli", topblock.getTimeMilli().toString());
		resp.put("date", new Date(topblock.getTimeMilli().getAsLong()).toString());
		
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new block();
	}

}
