package org.self.system.commands.base;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.mmr.MMRProof;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.objects.Coin;
import org.self.objects.CoinProof;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.brains.TxPoWSearcher;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.system.params.GeneralParams;
import org.self.utils.json.JSONObject;

public class coinexport extends Command {

	public coinexport() {
		super("coinexport","[coinid:] - Export a coin");
	}
	
	@Override
	public String getFullHelp() {
		return "\ncoinexport\n"
				+ "\n"
				+ "Export a coin including its MMR proof.\n"
				+ "\n"
				+ "A coin can then be imported and tracked on another node using the 'coinimport' command.\n"
				+ "\n"
				+ "This does not allow the spending of a coin - just the knowledge of its existence.\n"
				+ "\n"
				+ "coinid:\n"
				+ "    The id of a coin. Can be found using the 'coins' command.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "coinexport coinid:0xCD34..\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"coinid"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		String id = getParam("coinid");
		
		Coin coin = TxPoWSearcher.searchCoin(new MiniData(id));
		if(coin == null) {
			throw new CommandException("Coin not found coinid : "+id);
		}
		
		//When was it made.. 
		MiniNumber created = coin.getBlockCreated();
		
		//Now get the MMR proof.
		TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
		
		//How far back shall we go..
		MiniNumber history = new MiniNumber(256);
		if(GeneralParams.TEST_PARAMS) {
			history = new MiniNumber(8);
		}
		
		MiniNumber back = tip.getBlockNumber().sub(history);
		if(back.isLess(created)) {
			back = created;
		}
		
		//Get that Node..
		TxPoWTreeNode mmrnode = tip.getPastNode(back);
		
		//Now get the MMR PRoof of this coin..
		MMRProof proof = mmrnode.getMMR().getProofToPeak(coin.getMMREntryNumber());
		
		//Create the CoinProof..
		CoinProof cp = new CoinProof(coin, proof);
		
		//And create the Data version
		MiniData dataproof = MiniData.getMiniDataVersion(cp);
		
		JSONObject resp = new JSONObject();
		resp.put("coinproof", cp.toJSON());
		resp.put("data", dataproof.to0xString());
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new coinexport();
	}

}
