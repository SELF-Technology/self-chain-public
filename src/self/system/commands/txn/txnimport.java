package org.self.system.commands.txn;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.userprefs.txndb.TxnDB;
import org.self.database.userprefs.txndb.TxnRow;
import org.self.objects.base.MiniData;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.MiniFile;
import org.self.utils.json.JSONObject;

public class txnimport extends Command {

	public txnimport() {
		super("txnimport","(id:) (file:) (data:) - Import a transaction as a file or HEX data. Optionally specify the ID");
	}
	
	@Override
	public String getFullHelp() {
		return "\ntxnimport\n"
				+ "\n"
				+ "Import a transaction from previously exported HEX data or a .txn file.\n"
				+ "\n"
				+ "Optionally specify an ID to use for the new transaction.\n"
				+ "\n"
				+ "id: (optional)\n"
				+ "    Choose an ID for the transaction you are importing.\n"
				+ "\n"
				+ "file: (optional)\n"
				+ "    File name/path to the previously exported .txn file.\n"
				+ "\n"
				+ "data: (optional)\n"
				+ "    HEX data of the previously exported transaction.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "txnimport data:0x0000..\n"
				+ "\n"
				+ "txnimport id:simpletxn data:0x0000..\n"
				+ "\n"
				+ "txnimport id:multisig file:multisig.txn\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"id","file","data"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();

		TxnDB db = SelfDB.getDB().getCustomTxnDB();
		
		if(existsParam("file")) {
			String file = getParam("file");
			File ff = MiniFile.createBaseFile(file);
			if(!ff.exists()) {
				throw new CommandException("File does not exist : "+ff.getAbsolutePath());
			}
			
			//Load it in..
			byte[] txndata = MiniFile.readCompleteFile(ff);
			
			//Convert to MiniData
			MiniData minitxn = new MiniData(txndata);
			
			//Convert this..
			TxnRow txnrow = TxnRow.convertMiniDataVersion(minitxn);
			if(existsParam("id")) {
				txnrow.setID(getParam("id"));
			}
			
			db.addCompleteTransaction(txnrow);
			
			JSONObject resp = new JSONObject();
			ret.put("response", txnrow.toJSON());
			
		}else if(existsParam("data")){
			
			//Get the HEX data
			MiniData dv = getDataParam("data");
			
			//Convert to a TxnRow
			TxnRow tx 	= TxnRow.convertMiniDataVersion(dv);
			if(existsParam("id")) {
				tx.setID(getParam("id"));
			}
			
			//Add to the DB
			db.addCompleteTransaction(tx);
			
			JSONObject resp = new JSONObject();
			ret.put("response", tx.toJSON());
			
		}else {
			throw new CommandException("Must specify file or data");
		}
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new txnimport();
	}

}
