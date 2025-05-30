package org.self.system.commands.base;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.self.database.SelfDB;
import org.self.database.txpowdb.TxPoWDB;
import org.self.database.txpowdb.ram.RamData;
import org.self.objects.TxPoW;
import org.self.system.commands.Command;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;

public class mempool extends Command {

	public mempool() {
		super("mempool","Check the mempool");
	}
	
	@Override
	public JSONObject runCommand() throws Exception{
		JSONObject ret = getJSONReply();

		TxPoWDB txpdb = SelfDB.getDB().getTxPoWDB();
		
		JSONArray txpows = new JSONArray();
		
		ConcurrentHashMap<String, RamData> mempool = txpdb.getCompleteMemPool();
		int size 		= 0;
		int cascade 	= 0;
		int txnnum 		= 0;
		int blknum 		= 0;
		
		Enumeration<RamData> alldata = mempool.elements();
		while(alldata.hasMoreElements()) {
			RamData ram = alldata.nextElement();
			
			if(!ram.isInCascade()) {
				TxPoW txp = ram.getTxPoW();
				
				size++;
				
				JSONObject txpow = new JSONObject();
				txpow.put("txpowid", txp.getTxPoWID());
				txpow.put("transaction", txp.isTransaction());
				txpow.put("block", txp.isBlock());
				
				txpows.add(txpow);
				
				if(txp.isTransaction()) {
					txnnum++;
				}
				if(txp.isBlock()) {
					blknum++;
				}
			}else {
				cascade++;
			}
		}
		
		JSONObject resp = new JSONObject();
		resp.put("txpow", txpows);
		resp.put("total", mempool.size());
		resp.put("onchain", size);
		resp.put("cascade", cascade);
		resp.put("transactions", txnnum);
		resp.put("blocks", blknum);
		
		//Add balance..
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new mempool();
	}

}