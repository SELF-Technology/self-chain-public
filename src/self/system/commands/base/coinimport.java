package org.self.system.commands.base;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.mmr.MMRData;
import org.self.database.mmr.MMRProof;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.objects.Coin;
import org.self.objects.CoinProof;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.brains.TxPoWSearcher;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONObject;

public class coinimport extends Command {

	public coinimport() {
		super("coinimport","[data:] (track:true|false) - Import a coin, and keep tracking it");
	}
	
	@Override
	public String getFullHelp() {
		return "\ncoinimport\n"
				+ "\n"
				+ "Import a coin including its MMR proof.\n"
				+ "\n"
				+ "Optionally you can track the coin to add it to your relevant coins list and know when it becomes spent.\n"
				+ "\n"
				+ "Importing does not allow the spending of a coin - just the knowledge of its existence.\n"
				+ "\n"
				+ "data:\n"
				+ "    The data of a coin. Can be found using the 'coinexport' command.\n"
				+ "\n"
				+ "track: (optional)\n"
				+ "    true or false, true will create an MMR entry for the coin and add it to your relevant coins.\n"			
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "coinimport data:0x00000..\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"data","track"}));
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
			throw new CommandException("Coin is spent. Can only import UNSPENT coins.");
		}
		
		//Get the tip
		TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
		
		//Do we already have it..
		MMRProof checkproof = tip.getMMR().getProofToPeak(newcoin.getMMREntryNumber());
		CoinProof currentproof 	= new CoinProof(newcoin, checkproof);
		
		//Get the Coin..
		Coin txcoin = currentproof.getCoin();
		
		//Create the MMRData Leaf Node..
		MMRData mmrcoin 		= MMRData.CreateMMRDataLeafNode(txcoin, txcoin.getAmount());
		
		boolean currentvalid 	= tip.getMMR().checkProofTimeValid(	newcoin.getMMREntryNumber(), 
																	mmrcoin, 
																	currentproof.getMMRProof());
		if(currentvalid) {
			
			//Get the Tree Node..
			TxPoWTreeNode node = TxPoWSearcher.getTreeNodeForCoin(newcoin.getCoinID());
			if(node!=null) {
			
				//Is it relevant..
				if(node.isRelevantEntry(newcoin.getMMREntryNumber())) {
					throw new CommandException("Attempting to add relevant coin we already have");
				}
				
				//Add to relevant coins..
				node.getRelevantCoinsEntries().add(newcoin.getMMREntryNumber());
				node.calculateRelevantCoins();
			
				//Added
				ret.put("response", newcoinproof.toJSON());
				return ret;
			}
		}
		
		//Get the Coin..
		txcoin = newcoinproof.getCoin();
		
		//Create the MMRData Leaf Node..
		mmrcoin = MMRData.CreateMMRDataLeafNode(txcoin, txcoin.getAmount());
		
		//Now check that newcoinproof is valid..
		boolean valid = tip.getMMR().checkProofTimeValid(newcoin.getMMREntryNumber(), 
														 mmrcoin, 
														 newcoinproof.getMMRProof());
		if(!valid) {
			throw new CommandException("Invalid MMR Proof");
		}
		
		//When is this CoinProof..
		MiniNumber coinblock = newcoinproof.getMMRProof().getBlockTime();
		
		//Ok.. now we have to add this to OUR TreeNode MMR..
		TxPoWTreeNode treenode = tip.getPastNode(coinblock);
		if(treenode==null) {
			throw new CommandException("TreeNode at Blocktime not found (proof too old): "+coinblock);
		}
		
		//Checker..
		MMRData oldroot = treenode.getMMR().getRoot();
		
		//And create a new MMRData with the correct amount
		MMRData mmrdata = newcoinproof.getMMRData();

		//About to change the MMR..
		treenode.getMMR().setFinalized(false);
		
		//And add to the MMR
		treenode.getMMR().updateEntry(newcoin.getMMREntryNumber(), newcoinproof.getMMRProof(), newcoinproof.getMMRData());	

		//Re-finalise
		treenode.getMMR().finalizeSet();
				
		//Add to the total List of coins fro this block
		treenode.getAllCoins().add(newcoin);
		
		//And set to relevant.. track it..
		if(getBooleanParam("track", true)) {
			treenode.getRelevantCoins().add(newcoin);
			treenode.getRelevantCoinsEntries().add(newcoin.getMMREntryNumber());
		}
		
		//New root..
		MMRData newroot = treenode.getMMR().getRoot();
		if(!newroot.isEqual(oldroot)) {
			throw new CommandException("SERIOUS ERROR : MMR root different after adding coin.. "+newcoinproof.toJSON().toString());
		}
		
		ret.put("response", newcoinproof.toJSON());
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new coinimport();
	}

}
