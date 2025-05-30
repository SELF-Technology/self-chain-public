package org.self.system.commands.base;

import org.self.database.SelfDB;
import org.self.database.mmr.MMR;
import org.self.system.commands.Command;
import org.self.utils.json.JSONObject;

public class printmmr extends Command {

	public printmmr() {
		super("printmmr", "Print the MMR set of the tip block");
	}
	
	@Override
	public String getFullHelp() {
		return "\nprintmmr\n"
				+ "\n"
				+ "Print the MMR set of the tip block and the total number of entries in the MMR.\n"
				+ "\n"
				+ "Returns the tip block number, latest entrynumber and latest set of MMR entries.\n"
				+ ""
				+ "For each entry, details of its row, entry number, data and value of all new and updated MMR entries for the tip block.\n"
				+ "\n"
				+ "Row 1 represents the leaf nodes, entry 0 represents the first entry on a row.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "printmmr\n";
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		MMR mmr = SelfDB.getDB().getTxPoWTree().getTip().getMMR();
		
		ret.put("response", mmr.toJSON());
		return ret;
	}

	@Override
	public Command getFunction() {
		return new printmmr();
	}

}
