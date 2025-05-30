package org.self.system.commands.base;

import org.self.database.SelfDB;
import org.self.database.cascade.Cascade;
import org.self.database.cascade.CascadeNode;
import org.self.database.maxima.MaximaDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.objects.base.MiniNumber;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONObject;

public class healthcheck extends Command {

	public healthcheck() {
		super("healthcheck","Run a system check to see everything adds up");
	}
	
	@Override
	public String getFullHelp() {
		return "\nhealthcheck\n"
				+ "\n"
				+ "Return information about your chain, cascade and maxima.\n"
				+ "\n"
				+ "Chain - tip:current chain tip block, root:current chain root block, chainlength:number of blocks in the heaviest chain.\n"
				+ "\n"
				+ "Cascade - tip:current cascade tip block, tipcorrect:returns true if the cascade tip meets the root of the txpow tree.\n"
				+ "\n"
				+ "Maxima - hosts:number of maxima hosts, contacts:number of maxima contacts.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "healthcheck\n";
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		JSONObject resp = new JSONObject();
		
		//First get the tip..
		TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
		if(tip == null) {
			throw new CommandException("No TIP block found.. ?");
		}
		
		JSONObject chain = new JSONObject();
		
		chain.put("tip", tip.getTxPoW().getBlockNumber().toString());
		
		TxPoWTreeNode root = SelfDB.getDB().getTxPoWTree().getRoot();
		MiniNumber treeroot = root.getTxPoW().getBlockNumber();
		chain.put("root", treeroot.toString());
		
		MiniNumber len = tip.getTxPoW().getBlockNumber().sub(treeroot);
		chain.put("chainlength", len.toString());
		
		resp.put("chain", chain);
		
		//Now check the cascade
		Cascade casc = SelfDB.getDB().getCascade();
		CascadeNode ctip = casc.getTip();
		if(ctip != null) {
			MiniNumber casctip = casc.getTip().getTxPoW().getBlockNumber();
			
			JSONObject cascade = new JSONObject();
			cascade.put("tip", casctip.toString());
			
			boolean correctstart = casctip.isEqual(treeroot.decrement());
			cascade.put("tipcorrect", correctstart);
			
			cascade.put("cascadelength", casc.getLength());
			
			resp.put("cascade", cascade);
		}else {
			resp.put("cascade", "nocacade");
		}
		
		
		//Now check Maxima..
		MaximaDB maxdb = SelfDB.getDB().getMaximaDB();
		JSONObject maxima = new JSONObject();
		
		int hosts 	 = maxdb.getAllHosts().size();
		int contacts = maxdb.getAllContacts().size();
		
		maxima.put("hosts", hosts);
		maxima.put("contacts", contacts);
		
		resp.put("maxima", maxima);
		
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new healthcheck();
	}

}
