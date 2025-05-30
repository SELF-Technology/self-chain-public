package org.self.system.commands.base;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.mmr.MMRData;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.objects.Coin;
import org.self.objects.CoinProof;
import org.self.objects.base.MiniData;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONObject;

public class coincheck extends Command {

	public coincheck() {
		super("coincheck","[data:] - Check a coin exists");
	}
	
	@Override
	public String getFullHelp() {
		return "\ncoincheck\n"
				+ "\n"
				+ "Check a coin exists and is valid. Can only check unspent coins.\n"
				+ "\n"
				+ "Returns the coin details and whether the MMR proof is valid.\n"
				+ "\n"
				+ "data:\n"
				+ "    The data of a coin. Can be found using the 'coinexport' command.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "coincheck data:0x00000..\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"data"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		String data = getParam("data");
		
		//Convert to a coin proof..
		CoinProof newcoinproof 	= CoinProof.convertMiniDataVersion(new MiniData(data));
		
		//The coin and Proof..
		Coin newcoin 			= newcoinproof.getCoin();
		
		//Check is UNSPENT..
		if(newcoin.getSpent()) {
			throw new CommandException("Coin is spent. Can only check UNSPENT coins.");
		}
		
		//Get the tip
		TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
		
		//Get the Coin..
		Coin txcoin = newcoinproof.getCoin();
		
		//Create the MMRData Leaf Node..
		MMRData mmrcoin = MMRData.CreateMMRDataLeafNode(txcoin, txcoin.getAmount());
		
		//Check the MMR
		boolean validmmr = tip.getMMR().checkProofTimeValid(newcoinproof.getCoin().getMMREntryNumber(), 
															mmrcoin, 
															newcoinproof.getMMRProof());
		
		//Add to response
		JSONObject resp = new JSONObject();
		resp.put("proofblock", newcoinproof.getMMRProof().getBlockTime());
		resp.put("coin", newcoin.toJSON());
		resp.put("valid", validmmr);
		
		ret.put("response", resp);
				
		return ret;
	}

	@Override
	public Command getFunction() {
		return new coincheck();
	}

}
