package org.self.system.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.self.database.SelfDB;
import org.self.database.minidapps.MiniDAPP;
import org.self.system.SELFSystem;
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
import org.self.system.commands.base.automine;
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
import org.self.system.commands.base.debugflag;
import org.self.system.commands.base.getaddress;
import org.self.system.commands.base.hash;
import org.self.system.commands.base.hashtest;
import org.self.system.commands.base.healthcheck;
import org.self.system.commands.base.incentivecash;
import org.self.system.commands.base.logs;
import org.self.system.commands.base.maths;
import org.self.system.commands.base.mempool;
import org.self.system.commands.base.missingcmd;
import org.self.system.commands.base.mmrcreate;
import org.self.system.commands.base.mmrproof;
import org.self.system.commands.base.newaddress;
import org.self.system.commands.base.printmmr;
import org.self.system.commands.base.printtree;
import org.self.system.commands.base.quit;
import org.self.system.commands.base.random;
import org.self.system.commands.base.scanchain;
import org.self.system.commands.base.seedrandom;
import org.self.system.commands.base.slavenode;
import org.self.system.commands.base.status;
import org.self.system.commands.base.systemcheck;
import org.self.system.commands.base.test;
import org.self.system.commands.base.timemilli;
import org.self.system.commands.base.tokencreate;
import org.self.system.commands.base.tokenvalidate;
import org.self.system.commands.base.trace;
import org.self.system.commands.maxima.maxcontacts;
import org.self.system.commands.maxima.maxcreate;
import org.self.system.commands.maxima.maxdecrypt;
import org.self.system.commands.maxima.maxencrypt;
import org.self.system.commands.maxima.maxextra;
import org.self.system.commands.maxima.maxima;
import org.self.system.commands.maxima.maxmessage;
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
import org.self.system.commands.network.p2pstate;
import org.self.system.commands.network.peers;
import org.self.system.commands.network.ping;
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
import org.self.system.commands.send.multisigread;
import org.self.system.commands.send.send;
import org.self.system.commands.send.sendnosign;
import org.self.system.commands.send.sendpoll;
import org.self.system.commands.send.sendpost;
import org.self.system.commands.send.sendsign;
import org.self.system.commands.send.sendview;
import org.self.system.commands.send.wallet.consolidatefrom;
import org.self.system.commands.send.wallet.constructfrom;
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
import org.self.system.commands.txn.txncoinlock;
import org.self.system.commands.txn.txncreate;
import org.self.system.commands.txn.txndelete;
import org.self.system.commands.txn.txnexport;
import org.self.system.commands.txn.txnimport;
import org.self.system.commands.txn.txninput;
import org.self.system.commands.txn.txnlist;
import org.self.system.commands.txn.txnlock;
import org.self.system.commands.txn.txnmine;
import org.self.system.commands.txn.txnminepost;
import org.self.system.commands.txn.txnmmr;
import org.self.system.commands.txn.txnoutput;
import org.self.system.commands.txn.txnpost;
import org.self.system.commands.txn.txnscript;
import org.self.system.commands.txn.txnsign;
import org.self.system.commands.txn.txnstate;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;
import org.self.utils.json.parser.JSONParser;
import org.self.utils.json.parser.ParseException;

public class CommandRunner {
	
	/**
	 * All the Valid Commands
	 */
	public static final Command[] ALL_COMMANDS = 
		{   new quit(), new status(), new coins(), new txpow(), new connect(), new disconnect(), new network(),
			new message(), new trace(), new help(), new printtree(), new automine(), new printmmr(), new rpc(),
			new send(), new balance(), new tokencreate(), new tokenvalidate(), new tokens(),new getaddress(), new newaddress(), new debugflag(),
			new incentivecash(), new webhooks(), new peers(), new p2pstate(),

			new mds(), new sendpoll(), new healthcheck(), new mempool(), new block(), new reset(),
			
			new whitepaper(), new sendnosign(), new sendsign(), new sendpost(), new sendview(),
			new sendfrom(), new createfrom(), new signfrom(), new postfrom(), new constructfrom(), new consolidatefrom(),
			
			new archive(), new logs(), new history(), new convert(),new maths(),
			new checkpending(), new checkmode(), new restoresync(), new timemilli(),
			
			new decryptbackup(), new megammrsync(), new systemcheck(), new scanchain(),
			
			new multisig(), new multisigread(), new checkaddress(),
			new maxsign(), new maxverify(), new maxextra(), new maxcreate(),
			new maxencrypt(), new maxdecrypt(),
			
			new ping(), new random(), new seedrandom(), new mysql(), new mysqlcoins(), new slavenode(), new checkrestore(),
			//new file(),
			new megammr(), new maxmessage(),
			
			new vault(), new consolidate(), new coinnotify(),
			new backup(), new restore(), new test(), 
			new runscript(), new tutorial(),new keys(),
			new scripts(), new newscript(), new removescript(),
			new burn(),
			
			new txnbasics(),new txncreate(), new txninput(),new txnlist(), new txnclear(),
			new txnoutput(),new txnstate(),new txnsign(),new txnpost(),new txndelete(),
			new txnexport(),new txnimport(),new txncheck(), new txnscript(), new txnauto(),
			new txnaddamount(),new txnlock(), new txnmmr(), new txnmine(), new txnminepost(), new txncoinlock(),
			
			new coinimport(), new coinexport(),new cointrack(), new coincheck(),
			
			new hash(), new hashtest(), new sign(), new verify(),
			
			new maxima(), new maxcontacts(),
			
			new mmrcreate(), new mmrproof()
		};
	
	
	/**
	 * Get a Command RUnner to run / check commands
	 */
	public static CommandRunner getRunner() {
		return new CommandRunner();
	}
	
	private CommandRunner() {}
	
	public JSONArray runMultiCommand(String zCommand) {
		return runMultiCommand("0x00", zCommand);
	}
	
	public JSONObject runSingleCommand(String zCommand) {
		JSONArray res 		= runMultiCommand(zCommand);
		JSONObject result 	= (JSONObject) res.get(0);
		return result;
	}
	
	public JSONArray runMultiCommand(String zMiniDAPPID, String zCommand) {
		
		JSONArray finalresult = new JSONArray();
		
		//First break it up..
		StringTokenizer strtok = new StringTokenizer(zCommand, ";");
		while(strtok.hasMoreTokens()) {
			String command = strtok.nextToken().trim();
			
			//Get this command..
			Command cmd = getCommand(command);
			
			//Set who called it
			cmd.setMiniDAPPID(zMiniDAPPID);
			
			//The final result
			JSONObject result = null;
			
			//Check the Parameters
			ArrayList<String> validparams 	= cmd.getValidParams();
			
			JSONObject allparams 			=  cmd.getParams();
			Set<String> keys 				= allparams.keySet(); 
			
			boolean validp=true;
			for(String key : keys) {
				if(!validparams.contains(key)) {
					
					//Invalid Param
					result=  new JSONObject();
					result.put("command", command);
					result.put("params", allparams);
					result.put("status", false);
					result.put("pending", false);
					result.put("error", "Invalid parameter : "+key);
					
					//Add to the List..
					finalresult.add(result);
					
					//And that's all folks..
					validp=false;
					break;
				}
			}
			
			//Are we valid..
			if(!validp) {
				break;
			}
			
			//What is the command
			String comname = cmd.getName();
			
			//Check this MiniDAPP can make this call..
			boolean allowed = isCommandAllowed(comname);
			
			//Is this a MiniDAPP..
			if(zMiniDAPPID.equals(SELFSystem.getInstance().getMDSManager().getPublicMiniDAPPID())) {
				
				//Public MiniDAPPs cannot add to pending..
				if(!allowed) {
					result=  new JSONObject();
					result.put("command", command);
					result.put("status", false);
					result.put("pending", false);
					result.put("error", "Public MDS cannot run WRITE commands");
					
					//Add to the List..
					finalresult.add(result);
					
					//And that's all folks..
					break;
				}
				
			}else if(!zMiniDAPPID.equals("0x00")) {
				
				if(!allowed) {
					
					//Get the MiniDAPP
					MiniDAPP md = SELFSystem.getInstance().getMDSManager().getMiniDAPP(zMiniDAPPID);
					
					//Does it have WRITE permission..
					if(!md.getPermission().equalsIgnoreCase("write")) {
					
						//Add to pending..
						String puid = SELFSystem.getInstance().getMDSManager().addPendingCommand(md, command);
						
						//And return..
						result=  new JSONObject();
						result.put("command", command);
						result.put("status", false);
						result.put("pending", true);
						result.put("pendinguid", puid);
						result.put("error", "This command needs to be confirmed and is now pending..");
						
						//Add to the List..
						finalresult.add(result);
						
						//And that's all folks..
						break;
					}
				}
			}
			
			try {
				result = cmd.runCommand();
				
			}catch(CommandException cexc) {
				result = cmd.getJSONReply();
				result.put("status", false);
				result.put("error", cexc.getMessage());
				
			}catch(Exception exc) {
				//Print the full error
				SelfLogger.log(exc);
				
				result = cmd.getJSONReply();
				result.put("status", false);
				result.put("error", exc.toString());
			}
			
			//Add it..
			finalresult.add(result);
			
			//Stop at a false..
			if((boolean)result.get("status") == false) {
				break;
			}
		}
		
		return finalresult;
	}
	
	public Command getCommandOnly(String zCommandName) {
		int commandlen = ALL_COMMANDS.length;
		
		Command comms = null;
		for(int i=0;i<commandlen;i++) {
			if(ALL_COMMANDS[i].getName().equals(zCommandName)) {
				comms = ALL_COMMANDS[i].getFunction();
				break;
			}
		}
	
		return comms;
	}
	
	public Command getCommand(String zCommand) {
		int commandlen = ALL_COMMANDS.length;
		
		//Get the first word..
		String[] split = splitStringJSON(false,zCommand);
		
		//The first is the command..
		String command = split[0].toLowerCase();
		
		Command comms = null;
		for(int i=0;i<commandlen;i++) {
			if(ALL_COMMANDS[i].getName().equals(command)) {
				comms = ALL_COMMANDS[i].getFunction();
				break;
			}
		}
		
		//If not found return error
		if(comms == null) {
			return new missingcmd(command,"Command not found");
		}
		
		//Set the Complete Command - for reference..
		comms.setCompleteCommand(zCommand);
		
		//get the parameters if any
		int len = split.length;
		for(int i=1;i<len;i++) {
			String token = split[i];
			
			//Find the :
			int index 	 = token.indexOf(":");
			if(index == -1) {
				return new missingcmd(command,"Invalid parameters for "+command+" @ "+token);
			}
			
			String name  = token.substring(0, index).trim();
			String value = token.substring(index+1).trim();
			
			//Is the value a JSON..or JSONArray..
			if(value.startsWith("{") && value.endsWith("}")) {
				
				//It's a JSON..!
				JSONObject json = null;
				try {
					json = (JSONObject) new JSONParser().parse(value);
				} catch (ParseException e) {
					return new missingcmd(command,"Invalid JSON parameter for "+command+" @ "+token+" "+e.toString());
				}
				
				//Store this parameter..
				comms.getParams().put(name, json);
			
			}else if(value.startsWith("[") && value.endsWith("]")) {
				
				//Is this a state variable
				if(command.equals("txnstate")) {

					//Could be a String variable.. add normal String parameter to..
					comms.getParams().put(name, value);

					continue;
				}
				
				//It's a JSONArray..!
				JSONArray json = null;
				try {
					json = (JSONArray) new JSONParser().parse(value);
				} catch (ParseException e) {
					
					//Otherwise is just a broken JSONArray
					return new missingcmd(command,"Invalid JSON parameter for "+command+" @ "+token+" "+e.toString());
				}
				
				//Store this parameter..
				comms.getParams().put(name, json);
				
			}else {
				
				//Should not have any "..
				if(value.startsWith("\"")) {
					value = value.substring(1);
				}
				
				if(value.endsWith("\"")) {
					value = value.substring(0,value.length()-1);
				}
				
				//Add normal String parameter to..
				comms.getParams().put(name, value);
			}
		}
		
		return comms;
	}
	
	private String[] splitStringJSON(boolean zForceNormal, String zInput) {
		
		//Are there any JSON in this.. if not use super fast method..
		if(!zForceNormal) {
			
			//Check for windows files.. as : screws up
			boolean iswindowsfile = (zInput.indexOf(":\\")!=-1);
			
			//If it's big use the fast one.. but can have issues if : used weirdly.. :( 
			if(!iswindowsfile && zInput.indexOf("{") == -1 && zInput.indexOf("[") == -1) {
				String[] fastres = splitterQuotedPattern(zInput);
				if(fastres != null) {
					return fastres;
				}
			}
		}
		
		//Normal method..
		ArrayList<String> token = new ArrayList<>();
		String ss = zInput.trim();
		
		//Cycle through looking for spaces or quotes..
		String current = new String();
		
		int jsoned 		= 0;
		boolean quoted 	= false;
		
		int len = ss.length();
		for(int i=0;i<len;i++) {
			char cc = ss.charAt(i);
			
			if(cc == ' ') {
				//End of the line..
				if(!quoted && jsoned==0) {
					
					//Add current
					if(!current.equals("")) {
						token.add(current.trim());
					}
						
					//New Current
					current = new String();
					
				}else {
					current += cc;
				}
			
			}else if(cc == '{') {
				jsoned++;
				current += cc;
				
			}else if(cc == '}') {
				jsoned--;
				current += cc;
			
			}else if(cc == '[') {
				jsoned++;
				current += cc;
				
			}else if(cc == ']') {
				jsoned--;
				current += cc;
			
			
			}else if(cc == '\"') {
				if(jsoned>0) {
					
					//It's in a JSON.. so keep it..
					current += cc;
					
				}else {
					//it's a quote!
					if(quoted) {
						//It's finished..
						quoted=false;
					}else {
						quoted=true;
					}
				}
				
			}else {
				current += cc;
			}
		}
		
		//Add the last bit..
		if(!current.equals("")) {
			token.add(current.trim());
		}
		
		return token.toArray(new String[0]);
	}
	
	/**
	 * Very Fast splitter that doesn't work for JSON inputs.. just normal name value pairs..
	 * 
	 * If something weird happens return null .. and other method used..
	 */
	public String[] splitterQuotedPattern(String zInput) {
		ArrayList<String> token = new ArrayList<>();
		
		//Split the : name value pairs aswell..
		String ss = zInput.trim().replaceAll(":", " : ");
		
		//Search for quoted strings
		String regex 	= "\"([^\"]*)\"|(\\S+)";
		Matcher m 		= Pattern.compile(regex).matcher(ss);
	    while (m.find()) {
	        if (m.group(1) != null) {
	        	token.add(m.group(1));
	        } else {
	        	token.add(m.group(2));
	        }
	    }
		
	    //Now cycle and add up the name:value pairs
	    ArrayList<String> finaltokens = new ArrayList<>();
	    boolean first 		= true; 
	    boolean namefound	= false;
	    String nvpair 		= new String("");
	    
	    for(String tok : token) {
	    	
	    	if(first){
	    		//First one is just the command
	    		first = false;
	    		finaltokens.add(tok);
	    		
	    	}else {
		    	//Have we found the name yet
		    	if(!namefound) {
		    		
		    		//CHECK ..
		    		if(tok.trim().equals(":")) {
		    			//Hmm. should not HAPPEN!.. use slow method
		    			return null;
		    		}
		    		
		    		namefound 	 = true;
		    		nvpair 		= new String(tok);
		    		
		    	}else {
		    		if(tok.trim().equals(":")) {
		    			nvpair+=":";
		    		}else {
		    			namefound 	 = false;
		    			nvpair		+=tok;
		    			
		    			//Add to the final list
		    			finaltokens.add(nvpair);
		    		}
		    	}
	    	}
	    }
	    
		return finaltokens.toArray(new String[0]);
	}
	
	/**
	 * Which Commands are WRITE commands..
	 */
	private final String[] ALL_WRITE_COMMANDS = 
		{"send","sendpoll","sendsign","multisig","tokencreate","consolidate",
		 "cointrack","sign","txnsign","mds","backup","removescript",
		 "restore","restoresync","vault","archive","mysql","mysqlcoins",
		 "rpc","magic","quit","seedrandom","megammrsync"};
	
	private final ArrayList<String> ALL_WRITE_COMMANDS_ARRAY = new ArrayList<String>(Arrays.asList(ALL_WRITE_COMMANDS));
	
	public boolean isCommandAllowed(String zCommand) {
		
		//Is it a simple READ command
		if(ALL_WRITE_COMMANDS_ARRAY.contains(zCommand.trim())) {
			return false;
		}
		
		return true;
	}
	
}