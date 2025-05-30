package org.self.system.commands.txn;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.userprefs.txndb.TxnDB;
import org.self.database.userprefs.txndb.TxnRow;
import org.self.objects.Transaction;
import org.self.objects.TxPoW;
import org.self.objects.Witness;
import org.self.objects.base.MiniData;
import org.self.system.SELFSystem;
import org.self.system.brains.TxPoWGenerator;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.json.JSONObject;

public class txnmine extends Command {

	public txnmine() {
		super("txnmine","(id:) (data:) - Mine a txn but don't post it from either ID or txnexport Data");
	}
	
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"id","data"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();

		TxnDB db 		= SelfDB.getDB().getCustomTxnDB();
		TxnRow txnrow 	= null;
		
		//Are we loading from ID or from data..
		if(existsParam("id")) {
			//The transaction
			String id 			= getParam("id");
			
			//Get the Transaction
			txnrow 	= db.getTransactionRow(getParam("id"));
			if(txnrow == null) {
				throw new CommandException("Transaction not found : "+id);
			}
		
		}else {
			
			//Get the HEX data
			MiniData dv = getDataParam("data");
			
			//Convert to a TxnRow
			txnrow = TxnRow.convertMiniDataVersion(dv);
		}
		
		//Clear any previous checks..
		txnrow.getTransaction().clearIsMonotonic();
		
		//Get the txn..
		Transaction trans 	= txnrow.getTransaction();
		Witness wit		  	= txnrow.getWitness();
		
		//Compute the correct CoinID
		TxPoWGenerator.precomputeTransactionCoinID(trans);
		
		//Calculate the TransactionID..
		trans.calculateTransactionID();
		
		//Now create the TxPoW
		TxPoW txpow = TxPoWGenerator.generateTxPoW(trans, wit);
		
		//Calculate the size..
		txpow.calculateTXPOWID();
		
		//Now Mine it BUT dont POST it..
		boolean success = SELFSystem.getInstance().getTxPoWMiner().MineMaxTxPoW(false, txpow, 120000, false);
		
		if(!success) {
			throw new CommandException("FAILED TO MINE txn in 120 seconds !?");
		}
		
		//Now convert to DATA
		MiniData txdata = MiniData.getMiniDataVersion(txpow);
		
		//Return the MINED txn..
		JSONObject resp = new JSONObject();
		resp.put("txpowid", txpow.getTxPoWID());
		resp.put("data", txdata.to0xString());
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new txnmine();
	}

}
