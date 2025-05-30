package org.self.system.commands.send;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.objects.TxPoW;
import org.self.objects.base.MiniData;
import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.utils.MiniFile;
import org.self.utils.json.JSONObject;

public class sendpost extends Command {

	public sendpost() {
		super("sendpost","[file:] - Post a signed txn");
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"file"}));
	}
	
	@Override
	public String getFullHelp() {
		return "\nsendpost\n"
				+ "\n"
				+ "Post a transaction previously created and signed using the 'sendnosign' and 'sendsign' commands.\n"
				+ "\n"
				+ "Must be posted from an online node within approximately 24 hours of creating to ensure MMR proofs are valid.\n"
				+ "\n"
				+ "file:\n"
				+ "    Name of the signed transaction (.txn) file to post, located in the node's base folder.\n"
				+ "    If not in the base folder, specify the full file path.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "sendpost file:signedtransaction-1674907380057.txn\n"
				+ "\n"
				+ "sendpost file:C:\\Users\\signedtransaction-1674907380057.txn\n"
				+ "\n";
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
	
		String txnfile = getParam("file");
		
		//Load the txn
		byte[] data = MiniFile.readCompleteFile(MiniFile.createBaseFile(txnfile));
		
		//Create the MininData
		MiniData txndata = new MiniData(data);
		
		//Now convert back into a TxPoW
		TxPoW txp = TxPoW.convertMiniDataVersion(txndata);
		
		//Calculate the TxPOWID
		txp.calculateTXPOWID();
				
		JSONObject sigtran = new JSONObject();
		sigtran.put("txpow", txp.toJSON());
		
		JSONObject resp = new JSONObject();
		ret.put("response", sigtran);
		
		//Post It..!
		SELFSystem.getInstance().getTxPoWMiner().mineTxPoWAsync(txp);
		
		return ret;
	}
	
	@Override
	public Command getFunction() {
		return new sendpost();
	}
}