package org.self.system.commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.system.commands.backup.archive;
import org.self.system.commands.backup.backup;
import org.self.system.commands.backup.decryptbackup;
import org.self.system.commands.backup.mysql;
import org.self.system.commands.backup.mysqlcoins;
import org.self.system.commands.backup.reset;
import org.self.system.commands.backup.restore;
import org.self.system.commands.backup.restoresync;
import org.self.system.commands.backup.vault;
import org.self.system.commands.backup.mmrsync.megammr;
import org.self.system.commands.backup.mmrsync.megammrsync;
import org.self.system.commands.base.balance;
import org.self.system.commands.base.block;
import org.self.system.commands.base.burn;
import org.self.system.commands.base.checkaddress;
import org.self.system.commands.base.coincheck;
import org.self.system.commands.base.coinexport;
import org.self.system.commands.base.coinimport;
import org.self.system.commands.base.coinnotify;
import org.self.system.commands.base.cointrack;
import org.self.system.commands.base.consolidate;
import org.self.system.commands.base.convert;
import org.self.system.commands.base.getaddress;
import org.self.system.commands.base.hash;
import org.self.system.commands.base.hashtest;
import org.self.system.commands.base.logs;
import org.self.system.commands.base.maths;
import org.self.system.commands.base.mmrcreate;
import org.self.system.commands.base.mmrproof;
import org.self.system.commands.base.newaddress;
import org.self.system.commands.base.printtree;
import org.self.system.commands.base.quit;
import org.self.system.commands.base.random;
import org.self.system.commands.base.scanchain;
import org.self.system.commands.base.status;
import org.self.system.commands.base.timemilli;
import org.self.system.commands.base.tokencreate;
import org.self.system.commands.base.tokenvalidate;
import org.self.system.commands.base.trace;
import org.self.system.commands.maxima.maxcontacts;
import org.self.system.commands.maxima.maxcreate;
import org.self.system.commands.maxima.maxextra;
import org.self.system.commands.maxima.maxima;
import org.self.system.commands.maxima.maxsign;
import org.self.system.commands.maxima.maxverify;
import org.self.system.commands.mds.checkmode;
import org.self.system.commands.mds.checkpending;
import org.self.system.commands.mds.checkrestore;
import org.self.system.commands.mds.mds;
import org.self.system.commands.network.connect;
import org.self.system.commands.network.disconnect;
import org.self.system.commands.network.message;
import org.self.system.commands.network.network;
import org.self.system.commands.network.rpc;
import org.self.system.commands.network.webhooks;
import org.self.system.commands.scripts.newscript;
import org.self.system.commands.scripts.removescript;
import org.self.system.commands.scripts.runscript;
import org.self.system.commands.scripts.scripts;
import org.self.system.commands.search.coins;
import org.self.system.commands.search.history;
import org.self.system.commands.search.keys;
import org.self.system.commands.search.tokens;
import org.self.system.commands.search.txpow;
import org.self.system.commands.send.multisig;
import org.self.system.commands.send.send;
import org.self.system.commands.send.sendnosign;
import org.self.system.commands.send.sendpoll;
import org.self.system.commands.send.sendpost;
import org.self.system.commands.send.sendsign;
import org.self.system.commands.send.sendview;
import org.self.system.commands.send.wallet.createfrom;
import org.self.system.commands.send.wallet.postfrom;
import org.self.system.commands.send.wallet.sendfrom;
import org.self.system.commands.send.wallet.signfrom;
import org.self.system.commands.signatures.sign;
import org.self.system.commands.signatures.verify;
import org.self.system.commands.txn.txnaddamount;
import org.self.system.commands.txn.txnauto;
import org.self.system.commands.txn.txnbasics;
import org.self.system.commands.txn.txncheck;
import org.self.system.commands.txn.txnclear;
import org.self.system.commands.txn.txncreate;
import org.self.system.commands.txn.txndelete;
import org.self.system.commands.txn.txnexport;
import org.self.system.commands.txn.txnimport;
import org.self.system.commands.txn.txninput;
import org.self.system.commands.txn.txnlist;
import org.self.system.commands.txn.txnmine;
import org.self.system.commands.txn.txnminepost;
import org.self.system.commands.txn.txnoutput;
import org.self.system.commands.txn.txnpost;
import org.self.system.commands.txn.txnscript;
import org.self.system.commands.txn.txnsign;
import org.self.system.commands.txn.txnstate;
import org.self.utils.json.JSONObject;

public class help extends Command {

	public help() {
		super("help","Show Help. [] are required. () are optional. Use 'help command:' for full help. Chain multiple commands with ;");
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"command"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception{
		JSONObject ret = getJSONReply();
		
		JSONObject details = new JSONObject();
		
		String command = getParam("command","");
		
		if(!command.equals("")) {
		
			//Get the command..
			Command cmd = CommandRunner.getRunner().getCommandOnly(command);
			if(cmd == null) {
				throw new CommandException("Command not found : "+command);
			}
			
			//Otherwise get the Full help..
			details.put("command", command);
			details.put("help", cmd.getHelp());
			details.put("fullhelp", cmd.getFullHelp());
			
		}else{

			addCommand(details, new help());
			
			addCommand(details, new whitepaper());
			
			addCommand(details, new status());
			addCommand(details, new block());
			addCommand(details, new scanchain());
			addCommand(details, new printtree());
			addCommand(details, new burn());
			addCommand(details, new trace());
			addCommand(details, new logs());
			addCommand(details, new hashtest());
			addCommand(details, new timemilli());
			addCommand(details, new checkaddress());
			
			addCommand(details, new history());
			addCommand(details, new txpow());
			addCommand(details, new coins());
			addCommand(details, new tokens());
			addCommand(details, new keys());
	
			addCommand(details, new getaddress());
			addCommand(details, new newaddress());
			addCommand(details, new send());
			addCommand(details, new sendpoll());
			
			addCommand(details, new sendnosign());
			addCommand(details, new sendview());
			addCommand(details, new sendsign());
			addCommand(details, new sendpost());
			addCommand(details, new multisig());
			
			addCommand(details, new sendfrom());
			addCommand(details, new createfrom());
			addCommand(details, new signfrom());
			addCommand(details, new postfrom());
			
			addCommand(details, new balance());
			addCommand(details, new tokencreate());
			addCommand(details, new tokenvalidate());
			addCommand(details, new consolidate());
			
			addCommand(details, new hash());
			addCommand(details, new random());
			addCommand(details, new convert());
			addCommand(details, new maths());
			
			addCommand(details, new scripts());
			addCommand(details, new newscript());
			addCommand(details, new runscript());
			addCommand(details, new removescript());
			addCommand(details, new tutorial());
			
			addCommand(details, new mmrcreate());
			addCommand(details, new mmrproof());
			
			addCommand(details, new coincheck());
			addCommand(details, new coinimport());
			addCommand(details, new coinexport());
			addCommand(details, new cointrack());
			addCommand(details, new coinnotify());
			
			addCommand(details, new sign());
			addCommand(details, new verify());
			
			addCommand(details, new txnlist());
			addCommand(details, new txncreate());
			addCommand(details, new txnauto());
			addCommand(details, new txnaddamount());
			addCommand(details, new txnbasics());
			addCommand(details, new txndelete());
			addCommand(details, new txncheck());
			addCommand(details, new txninput());
			addCommand(details, new txnoutput());
			addCommand(details, new txnstate());
			addCommand(details, new txnscript());
			addCommand(details, new txnsign());
			addCommand(details, new txnclear());
			addCommand(details, new txnpost());
			addCommand(details, new txnimport());
			addCommand(details, new txnexport());
			addCommand(details, new txnmine());
			addCommand(details, new txnminepost());
			
			addCommand(details, new network());
			addCommand(details, new maxima());
			addCommand(details, new maxcontacts());
			addCommand(details, new maxextra());
			
			addCommand(details, new maxcreate());
			addCommand(details, new maxsign());
			addCommand(details, new maxverify());
			
			addCommand(details, new message());
			addCommand(details, new connect());
			addCommand(details, new disconnect());
			addCommand(details, new rpc());
			addCommand(details, new webhooks());
			
			addCommand(details, new mds());
			addCommand(details, new checkpending());
			addCommand(details, new checkmode());
			addCommand(details, new checkrestore());
			
			addCommand(details, new backup());
			addCommand(details, new restore());
			addCommand(details, new restoresync());
			addCommand(details, new decryptbackup());
			addCommand(details, new reset());
			addCommand(details, new archive());
			addCommand(details, new vault());
			
			addCommand(details, new megammr());
			addCommand(details, new megammrsync());
			
			addCommand(details, new mysql());
			addCommand(details, new mysqlcoins());
			
			//addCommand(details, new nodecount());
			addCommand(details, new quit());
		}
		
		ret.put("response", details);
		
		return ret;
	}

	
	private void addCommand(JSONObject zDetails, Command zCommand) {
		zDetails.put(getStrOfLength(15,zCommand.getName()), zCommand.getHelp());
	}
	
	public String getStrOfLength(int zDesiredLen, String zString) {
		String ret = new String(zString);
		int len    = ret.length();
		
		//The same or longer
		if(len >= zDesiredLen) {
			return ret.substring(0, zDesiredLen);
		}
		
		//If Shorter add zeros
		for(int i=0;i< zDesiredLen-len;i++) {
			ret = ret.concat(" ");
		}
		
		return ret;
	}
	
	@Override
	public Command getFunction() {
		return new help();
	}

}
