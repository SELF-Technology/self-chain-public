package org.self.system.commands.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.self.database.SelfDB;
import org.self.database.archive.ArchiveManager;
import org.self.database.txpowdb.onchain.TxPoWOnChainDB;
import org.self.database.txpowdb.sql.TxPoWSqlDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.objects.TxBlock;
import org.self.objects.TxPoW;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.brains.TxPoWSearcher;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.system.commands.CommandRunner;
import org.self.system.params.GeneralParams;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;

public class txpow extends Command {

	public txpow() {
		super("txpow","(txpowid:) (onchain:) (block:) (address:) (relevant:) (max:) - Search for a specific TxPoW or check for onchain");
	}
	
	@Override
	public String getFullHelp() {
		return "\ntxpow\n"
				+ "\n"
				+ "Search for a specific TxPoW in the unpruned chain or your mempool.\n"
				+ "\n"
				+ "Search by txpowid, block or 0x / Sx address.\n"
				+ "\n"
				+ "txpowid: (optional)\n"
				+ "    TxPoW id of the TxPoW to search for.\n"
				 + "    Returns the txpow details.\n"
				+ "\n"
				+ "onchain: (optional)\n"
				+ "    TxPoW id to search for on chain. Must be in the unpruned chain.\n"
				+ "    Returns block info and number of confirmations.\n"
				+ "\n"
				+ "block: (optional)\n"
				+ "    Block number to search in. Must be in the unpruned chain.\n"
				+ "\n"
				+ "address: (optional)\n"
				+ "    0x or Sx address. Search for TxPoWs containing this specific address.\n"
				+ "\n"
				+ "relevant: (optional)\n"
				+ "    true or false. Only list TxPoWs relevant to this node.\n"
				+ "\n"
				+ "max: (optional)\n"
				+ "    Max relevant TxPoW to retrieve. Default 100.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "txpow txpowid:0x000..\n"
				+ "\n"
				+ "txpow block:200\n"
				+ "\n"
				+ "txpow address:0xCEF6..\n"
				+ "\n"
				+ "txpow onchain:0x000..\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"txpowid","block",
				"address","onchain","relevant","max","action","inblock"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception{
		JSONObject ret = getJSONReply();
		
		
		//Are we getting info..
		if(existsParam("action")) {
			
			String action = getParam("action");
			if(action.equals("info")) {
				
				JSONObject txdb = new JSONObject();
				int txpowdbsize = SelfDB.getDB().getTxPoWDB().getSqlSize();
				txdb.put("size",txpowdbsize);
				
				//Get some details..
				TxPoWOnChainDB ocdb = SelfDB.getDB().getTxPoWDB().getOnChainDB();
				JSONObject onchaindb = new JSONObject();
				onchaindb.put("size", ocdb.getSize());
				onchaindb.put("first", ocdb.getFirstTxPoW());
				onchaindb.put("last", ocdb.getLastTxPoW());
				
				JSONObject infojson = new JSONObject();
				infojson.put("storedays", GeneralParams.NUMBER_DAYS_SQLTXPOWDB);
				infojson.put("txpowdb", txdb);
				infojson.put("onchaindb", onchaindb);
				
				ret.put("response", infojson);
			
			}else {
				throw new CommandException("Invalid action : "+action);
			}
		
		}else if(existsParam("txpowid")) {
			String txpowid = getAddressParam("txpowid");
			
			//Search for a given txpow
			TxPoW txpow = SelfDB.getDB().getTxPoWDB().getTxPoW(txpowid);
			if(txpow == null) {
				
				//Lets check the archive..
				TxBlock block = SelfDB.getDB().getArchive().loadBlock(txpowid);
				if(block!=null) {
					txpow = block.getTxPoW();
				}else {
					
					//Lets check the Mysql..
					boolean autologindetail = SelfDB.getDB().getUserDB().getAutoLoginDetailsMySQL();
					if(autologindetail) {
					
						//Run a search of the MySQL
						String command 		= "mysql action:findtxpow txpowid:"+txpowid;
						JSONArray res 		= CommandRunner.getRunner().runMultiCommand(command);
						JSONObject result 	= (JSONObject) res.get(0);
						
						//FOR NOW..
						//SelfLogger.log("MYSQL TXPOW SEARCH : "+result.toJSONString());
						
						//Get the response..
						JSONObject response = (JSONObject)result.get("response");
						
						if((boolean)response.get("found")) {
							ret.put("response", response.get("txpow"));
							return ret;
						
						}else {
							throw new CommandException("TxPoW not found : "+txpowid);
						}
						
					}else {
						throw new CommandException("TxPoW not found : "+txpowid);
					}
				}
			}
		
			ret.put("response", txpow.toJSON());
			
		}else if(existsParam("relevant")) {
			
			int max = getNumberParam("max",TxPoWSqlDB.MAX_RELEVANT_TXPOW).getAsInt();
			
			ArrayList<TxPoW> txps = SelfDB.getDB().getTxPoWDB().getSQLDB().getAllRelevant(max);
			
			//Only add them once..
			JSONArray txns = new JSONArray();
			HashSet<String> allreadyadded = new HashSet<>();
			for(TxPoW txp : txps) {
				String txpowid = txp.getTxPoWID();
				if(!allreadyadded.contains(txpowid)) {
					allreadyadded.add(txpowid);
					txns.add(txp.toJSON());
				}
			}
			
//			JSONArray txns = new JSONArray();
//			for(TxPoW txp : txps) {
//				txns.add(txp.toJSON());
//			}
			
			ret.put("response", txns);
			
		}else if(existsParam("inblock")) {
			TxPoWOnChainDB chaindb 	= SelfDB.getDB().getTxPoWDB().getOnChainDB();
			JSONObject resp 		= new JSONObject();
			JSONArray onchaintxpow 	= null;
			
			String inb = getParam("inblock");
			if(inb.startsWith("0x")) {
				MiniData txpowid = getDataParam("inblock");
				
				//Now search the DB
				onchaintxpow  = chaindb.getInBlockTxPoW(txpowid.to0xString());
			}else {
				
				//Its a number
				MiniNumber block = getNumberParam("inblock");
				
				//Now search the DB
				onchaintxpow  = chaindb.getInBlockTxPoW(block.getAsLong());
			}
				
			resp.put("txns", onchaintxpow);
			
			ret.put("response", resp);
			
			
		}else if(existsParam("onchain")) {
			MiniData txpowid = getDataParam("onchain");
		
			JSONObject resp = new JSONObject();
			
			TxPoW block = TxPoWSearcher.searchChainForTxPoW(txpowid);
			if(block == null) {
				
				//Now search the DB
				TxPoWOnChainDB chaindb 	= SelfDB.getDB().getTxPoWDB().getOnChainDB();
				JSONObject onchaintxpow = chaindb.getOnChainTxPoW(txpowid.to0xString());
				if((boolean)onchaintxpow.get("found")) {
					
					MiniNumber fblock = new MiniNumber(onchaintxpow.get("block").toString());
					
					//Found in the DB
					TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
					
					resp.put("found", true);
					resp.put("block", fblock.toString());
					resp.put("blockid", onchaintxpow.get("blockid").toString());
					resp.put("tip", tip.getBlockNumber().toString());
					
					MiniNumber depth  = tip.getBlockNumber().sub(fblock);
					resp.put("confirmations", depth.toString());
					
				}else {
					resp.put("found", false);
				}
				
			}else {
				
				TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
				
				resp.put("found", true);
				resp.put("block", block.getBlockNumber().toString());
				resp.put("blockid", block.getTxPoWID());
				resp.put("tip", tip.getBlockNumber().toString());
				
				MiniNumber depth  = tip.getBlockNumber().sub(block.getBlockNumber());
				resp.put("confirmations", depth.toString());
			}
			
			ret.put("response", resp);
		
		}else if(existsParam("block")) {
			
			MiniNumber block = getNumberParam("block");
			
			TxPoW txpow = TxPoWSearcher.getTxPoWBlock(block);
			if(txpow == null) {
				
				//Search the Archive..
				TxBlock txblock = SelfDB.getDB().getArchive().loadBlockFromNumber(block);
				if(txblock == null) {
					throw new CommandException("TxPoW not found @ height "+block);
				}
				
				txpow = txblock.getTxPoW();
			}
			
			ret.put("response", txpow.toJSON());
			
		}else if(existsParam("address")) {
			
			String address = getAddressParam("address");
			
			ArrayList<TxPoW> txps = TxPoWSearcher.searchTxPoWviaAddress(new MiniData(address));
			
			//Only add them once..
			JSONArray txns = new JSONArray();
			HashSet<String> allreadyadded = new HashSet<>();
			for(TxPoW txp : txps) {
				String txpowid = txp.getTxPoWID();
				if(!allreadyadded.contains(txpowid)) {
					allreadyadded.add(txpowid);
					txns.add(txp.toJSON());
				}
			}
			
			ret.put("response", txns);
			
		}else {
			throw new CommandException("Must Specify search params");
		}
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new txpow();
	}

}
