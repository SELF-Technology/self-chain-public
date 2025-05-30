package org.self.system.commands.backup;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import org.self.database.SelfDB;
import org.self.database.archive.ArchiveManager;
import org.self.database.archive.RawArchiveInput;
import org.self.database.cascade.Cascade;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.database.wallet.Wallet;
import org.self.objects.Address;
import org.self.objects.Coin;
import org.self.objects.CoinProof;
import org.self.objects.IBD;
import org.self.objects.TxBlock;
import org.self.objects.TxPoW;
import org.self.objects.base.MiniByte;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.SELFSystem;
import org.self.system.brains.TxPoWProcessor;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.system.commands.CommandRunner;
import org.self.system.commands.network.connect;
import org.self.system.network.self.NIOManager;
import org.self.system.network.self.NIOMessage;
import org.self.system.network.webhooks.NotifyManager;
import org.self.system.params.GeneralParams;
import org.self.utils.BIP39;
import org.self.utils.MiniFile;
import org.self.utils.MiniFormat;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageListener;

public class archive extends Command {
	
	//Are we restoring from a local archive..
	private final String LOCAL_ARCHIVE 					= "archiverestore";
	
	//H2 local
	private static boolean H2_TEMPARCHIVE 				= true;
	
	private static ArchiveManager STATIC_TEMPARCHIVE 	= null;
	private static RawArchiveInput STATIC_RAW		 	= null;
	
	public archive() {
		super("archive","[action:] (host:) (phrase:) (keys:) (keyuses:) - Resync your chain with seed phrase if necessary (otherwise wallet remains the same)");
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"action","host","phrase","anyphrase","keys","keyuses","file","address","statecheck","logs","maxexport"}));
	}
	
	@Override
	public String getFullHelp() {
		return "\narchive\n"
				+ "\n"
				+ "Perform a chain or seed re-sync from an archive node or archive export file.\n"
				+ "\n"
				+ "A chain re-sync will put your node on the correct chain.\n"
				+ "\n"
				+ "Use a chain re-sync if your node has been offline for too long and cannot catchup. Seed Phrase is not required.\n"
				+ "\n"
				+ "A seed re-sync will wipe the wallet, re-generate your private keys and restore your coins.\n"
				+ "\n"
				+ "Only do a seed re-sync if you have lost your node and do not have a backup.\n"
				+ "\n"
				+ "You can also perform checks on your archive db or archive file and check an address.\n"
				+ "\n"
				+ "action:\n"
				+ "    integrity : Check the integrity of your archive db. No host required.\n"
				+ "    inspect : inspect an archive export .gzip file. If 'last:1', the file can re-sync any node from genesis.\n"
				+ "    export : Export your archive db to a .gzip file. \n"
				+ "    exportraw : Export your archive db to a raw .dat file (recommended).\n"
				+ "    resync : Use with 'host' to do a chain or seed re-sync from an archive node. \n"
				+ "    import : Use with 'file' to do a chain or seed re-sync using a .dat or .gzip archive file. \n"
				+ "    addresscheck : check your archive db for spent and unspent coins at a specific address.\n"
				+ "\n"
				+ "host: (optional) \n"
				+ "    ip:port of the archive node to sync from. Use with action:resync.\n"
				+ "\n"
				+ "file: (optional) \n"
				+ "    name or path of the archive export gzip file to export/import/inspect.\n"
				+ "\n"
				+ "phrase: (optional)\n"
				+ "    To seed re-sync, enter your seed phrase in double quotes. Use with action:import or resync.\n"
				+ "    This will replace the current seed phrase of this node. You do NOT have to do this if you still have access to your wallet.\n"
				+ "    In this case, use action:import or resync without 'phrase' to get on the correct chain.\n"
				+ "\n"
				+ "anyphrase: (optional)\n"
				+ "    true or false. If you set a custom seed phrase on startup, you can set this to true. Default is false.\n"
				+ "\n"
				+ "keys: (optional) \n"
				+ "    Number of keys to create if you need to do a seed re-sync. Default is 64.\n"
				+ "\n"
				+ "keyuses: (optional) \n"
				+ "    How many times at most you used your keys..\n"
				+ "    Every time you re-sync with seed phrase this needs to be higher as Self Signatures are stateful.\n"
				+ "    Defaults to 1000 - the max is 262144 for normal keys.\n"
				+ "\n"
				+ "address: (optional) \n"
				+ "    The wallet or script address to search for in the archive. Use with action:addresscheck.\n"
				+ "    If using a script address, also provide an address or public key in the 'statecheck' parameter.\n"
				+ "    Use with action:addresscheck.\n"
				+ "\n"
				+ "statecheck: (optional) \n"
				+ "    Data to search for in a coin's state variables.\n"
				+ "    Combine with a script address in the 'address' parameter to search for coins locked in a contract.\n"
				+ "\n"
				+ "logs: (optional) \n"
				+ "    true or false. Show detailed logs, default false.\n"
				+ "\n"
				+ "maxexport: (optional) \n"
				+ "    How many blocks to export to a raw .dat file. Useful for testing purposes.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "archive action:integrity\n"
				+ "\n"
				+ "archive action:inspect file:archiveexport-ddmmyy.gzip\n"
				+ "\n"
				+ "archive action:exportraw file:archiveexport-ddmmyy.raw.dat\n"
				+ "\n"
				+ "archive action:resync host:89.98.89.98:9001\n"
				+ "\n"
				+ "archive action:import file:archiveexport-ddmmyy.raw.dat\n"
				+ "\n"
				+ "archive action:import file:archiveexport-ddmmyy.raw.dat phrase:\"YOUR 24 WORD SEED PHRASE\" keyuses:2000\n"
				+ "\n"			
				+ "archive action:addresscheck address:0xFED.. statecheck:0xABC..\n";
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
	
		String action = getParam("action");
		
		//Get the ArchiveManager
		ArchiveManager arch = SelfDB.getDB().getArchive();
		
		if(action.equals("integrity")) {
			
			//Scan through the entire DB.. checking.. 
			SelfLogger.log("Checking Archive DB.. this may take some time..");
			
//			JSONObject res = arch.executeGenericSQL("SELECT * FROM cascadedata");
//			SelfLogger.log(MiniFormat.JSONPretty(res));
			
			//What is the first block in the DB
			TxBlock starterblock = arch.loadLastBlock();
			
			//What is the first entry
			boolean startcheck 	= true;
			boolean startatroot = false;
			MiniNumber lastlog 	= MiniNumber.ZERO;
			MiniNumber start 	= MiniNumber.ZERO;
			if(starterblock == null) {
				SelfLogger.log("You have no Archive blocks..");
				startcheck = false;
			}else {
				lastlog = starterblock.getTxPoW().getBlockNumber();
				start 	= lastlog;
				
				//Is it from root..
				MiniNumber startblocknumber = starterblock.getTxPoW().getBlockNumber();
				if(startblocknumber.isEqual(MiniNumber.ONE)) {
					SelfLogger.log("ArchiveDB starts at root");
					startatroot = true;
				}
			}
			
			//Get the cascade details
			MiniNumber cascstart 	= MiniNumber.MINUSONE;
			JSONObject cascjson 	= new JSONObject();
			Cascade dbcasc = arch.loadCascade(); 
			if(dbcasc != null) {
				cascjson.put("exists", true);
				
				//Get the tip..
				cascstart = dbcasc.getTip().getTxPoW().getBlockNumber();
				cascjson.put("tip", cascstart.toString());
				cascjson.put("length", dbcasc.getLength());
				
			}else {
				cascjson.put("exists", false);
			}
			
			//Get t the initial 1000
			MiniData parenthash 	= null;
			MiniNumber parentnum 	= null;
			int errorsfound 		= 0;
			int total 				= 0;
			MiniNumber archstart 	= start;
			MiniNumber archend 		= archstart;
			
			JSONObject archjson = new JSONObject();
			archjson.put("start", archstart.toString());
			
			while(startcheck) {
				
				//Do we log a message
				if(lastlog.isLess(start.sub(new MiniNumber(2000)))) {
					SelfLogger.log("Now checking from  "+start);
					lastlog = start;
				}
				
				//Use batches of 256
				MiniNumber end 	= start.add(MiniNumber.TWOFIVESIX);
				
				//Get some blocks
				ArrayList<TxBlock> blocks = arch.loadBlockRange(start.decrement(),end,false); 
				
				for(TxBlock block : blocks) {
					archend = block.getTxPoW().getBlockNumber();
					total++;
					
					//Start Checking..
					if(parenthash == null) {
						parenthash 	= block.getTxPoW().getTxPoWIDData();
						parentnum  	= block.getTxPoW().getBlockNumber();
						lastlog 	= parentnum;
						
						archstart 	= parentnum;
						
						SelfLogger.log("ArchiveDB blocks resync start at block "+parentnum+" @ "+new Date(block.getTxPoW().getTimeMilli().getAsLong()));
						
					}else {
						
						//Check correct number
						if(!block.getTxPoW().getBlockNumber().isEqual(parentnum.increment())) {
							SelfLogger.log("Incorrect child block @ "+block.getTxPoW().getBlockNumber()+" parent:"+parentnum);
							errorsfound++;
						}else if(!block.getTxPoW().getParentID().isEqual(parenthash)) {
							SelfLogger.log("Parent hash incorrect @ "+block.getTxPoW().getBlockNumber());
							errorsfound++;
						}
						
						parenthash 	= block.getTxPoW().getTxPoWIDData();
						parentnum 	= block.getTxPoW().getBlockNumber();
					}
				}
				
				//Have we checked them all..
				if(blocks.size()==0) {
					break;
				}
				
				//Now recycle..
				start = parentnum.increment();
			}
			
			//Add more details
			archjson.put("end", archend.toString());
			archjson.put("blocks", total);
			
			//Check the archive node starts in the cascade..
			MiniNumber startresync 	= archstart;
			boolean validlist 		= false;
			if(!archstart.isEqual(MiniNumber.ONE)) {
				if(cascstart.isMoreEqual(archstart.sub(MiniNumber.ONE)) && cascstart.isLessEqual(archend)) {
					validlist = true;
				}
				
				startresync = cascstart;
			}else {
				validlist = true;
			}
			
			JSONObject resp = new JSONObject();
			resp.put("message", "Archive integrity check completed");
			resp.put("cascade", cascjson);
			resp.put("archive", archjson);
			resp.put("valid", validlist);
			if(!validlist) {
				resp.put("notvalid", "Your cascade and blocks do not line up.. new cascade required.. pls restart Self");
			}
			resp.put("from", startresync);
			resp.put("errors", errorsfound);
			
			if(errorsfound>0) {
				resp.put("recommend", "There are errors in your Archive DB blocks - you should wipe then resync with a valid host");
			}else {
				resp.put("recommend", "Your ArchiveDB is correct and has no errors.");
			}
			
			ret.put("response", resp);
			
		}else if(action.equals("resync")) {
			
			//Can only do this if all keys created..
			vault.checkAllKeysCreated();
			
			//Get the Self Listener..
			MessageListener selflistener = SELFSystem.getInstance().getSelfListener();
			
			//Get the host
			String fullhost = getParam("host");
			
			//Is it auto
			boolean usinglocal = false;
			if(fullhost.equals(LOCAL_ARCHIVE)) {
				
				//Using the local DB..
				if(H2_TEMPARCHIVE && STATIC_TEMPARCHIVE == null) {
					throw new CommandException("No Local STATIC Archive DB Found..");
				}
				
				usinglocal = true;
			}
			
			Message connectdata = connect.createConnectMessage(fullhost);
			
			String host = null;
			int port 	= 0;
			if(!usinglocal) {
				
				//Check a valid host
				if(connectdata == null) {
					throw new CommandException("Invalid HOST format for resync : "+fullhost);
				}
				
				host = connectdata.getString("host");
				port = connectdata.getInteger("port");
			}
			
			//How many Keys do we need to generate
			int keys = getNumberParam("keys", new MiniNumber(Wallet.NUMBER_GETADDRESS_KEYS)).getAsInt();
			
			//Set the key uses to this..
			int keyuses = getNumberParam("keyuses", new MiniNumber(1000)).getAsInt();
			
			//Before we start deleting - check connection..
			if(!usinglocal) {
				IBD ibdtest = sendArchiveReq(host, port, MiniNumber.MINUSONE);
				if(ibdtest == null) {
					throw new CommandException("Could not connect to Archive host! @ "+host+":"+port);
				}
			}
			
			//Tell the MiniDAPPs..
			SELFSystem.getInstance().PostNotifyEvent("MDS_RESYNC_START",new JSONObject());
			
			//Are we MEGA MMR
			if(GeneralParams.IS_MEGAMMR) {
				SelfDB.getDB().getMegaMMR().clear();
			}
			
			//Are we resetting the wallet too ?
			MiniData seed 		= null;
			String phrase = getParam("phrase","");
			if(!phrase.equals("")) {
			
				//Are we allowing ANY phrase..
				boolean anyphrase = getBooleanParam("anyphrase", false);
				
				//Clean it up..
				String cleanphrase = phrase;
				if(!anyphrase) {
					cleanphrase = BIP39.cleanSeedPhrase(phrase);
				}
				
				//reset ALL the default data
				SELFSystem.getInstance().archiveResetReady(true);
				
				//This can take soem time..
				SelfLogger.log("Resetting all wallet private keys..");
				
				//Convert that into a seed..
				seed = BIP39.convertStringToSeed(cleanphrase);
				
				//Get the Wallet
				Wallet wallet = SelfDB.getDB().getWallet();
				
				//Set it..
				wallet.updateSeedRow(cleanphrase, seed.to0xString());
				
				//Now cycle through all the default wallet keys..
				SelfLogger.log("Creating a total of "+keys+" keys / addresses..");
				for(int i=0;i<keys;i++) {
					NotifyListener(selflistener,"Creating key "+i);
					SelfLogger.log("Creating key "+i);
					
					//Create a new key..
					wallet.createNewSimpleAddress(true);
				}
				SelfLogger.log("All keys created..");
				
				//Now Update the USES - since they may have been used before - we don;t know.. 
				wallet.updateAllKeyUses(keyuses);
				
			}else {
				//reset ALL the default data
				SELFSystem.getInstance().archiveResetReady(false);
			}
			
			//Now cycle through the chain..
			MiniNumber startblock 	= MiniNumber.ZERO;
			long starttime			= 0;
			MiniNumber endblock 	= MiniNumber.ZERO;
			boolean foundsome 		= false;
			boolean firstrun 		= true;
			MiniNumber firstStart   = MiniNumber.ZERO;
			
			long lastlogmessage = 0;
			
			int counter = 0;
			SelfLogger.log("System clean..");
			System.gc();
			IBD ibd = null;
			while(true) {
				
				//We don't need any transactions in RamDB
				SelfDB.getDB().getTxPoWDB().wipeDBRAM();
				
				//Clean system counter
				counter++;
				
				//HACK
//				if(counter > 0) {
//					break;
//				}
				
				//HARD RESET - H2 database doesn't like it if I don't do this
				if(counter % 10 == 0) {
					//SelfLogger.log("Clean up memory..");
					SELFSystem.getInstance().resetMemFull();
				}
				
				//Send him a message..
				if(!usinglocal) {
					ibd = sendArchiveReq(host, port, startblock);
				}else {
					
					if(H2_TEMPARCHIVE) {
						ibd = new IBD();
						ibd.createArchiveIBD(startblock, STATIC_TEMPARCHIVE, true);
					}else {
						ibd = STATIC_RAW.getNextIBD();
					}
				}
				
				//Make sure something returned..
				if(ibd == null) {
					ibd = new IBD();
				}
				
				//Is there a cascade..
				if(startblock.isEqual(MiniNumber.ZERO) && ibd.hasCascade()) {
					SelfLogger.log("Cascade Received.. "+ibd.getCascade().getTip().getTxPoW().getBlockNumber());
					
					//Set it as our cascade
					SelfDB.getDB().setIBDCascade(ibd.getCascade());
					
					//Do we need to save this..
					SelfDB.getDB().getArchive().checkCascadeRequired(ibd.getCascade());
				}
				
				int size = ibd.getTxBlocks().size();
				
				if(size > 0) {
					foundsome 		= true;
					TxBlock start 	= ibd.getTxBlocks().get(0);
					if(firstrun) {
						firstrun 	= false;
						firstStart 	= start.getTxPoW().getBlockNumber();
					}
					
					TxBlock last 	= ibd.getTxBlocks().get(size-1);
					endblock		= last.getTxPoW().getBlockNumber();
					
					startblock 		= endblock.increment();
					starttime		= last.getTxPoW().getTimeMilli().getAsLong();
					
					//SelfLogger.log("Archive IBD received start : "+start.getTxPoW().getBlockNumber()+" end : "+endblock);
				
					//Notify the Android Listener
					NotifyListener(selflistener,"Loading "+start.getTxPoW().getBlockNumber()+" @ "+new Date(start.getTxPoW().getTimeMilli().getAsLong()).toString());
				}else {
					SelfLogger.log("No Archive TxBlocks left..");
				}
			
				//Post it..
				SELFSystem.getInstance().getTxPoWProcessor().postProcessArchiveIBD(ibd, "0x00");
			
				//Now wait for something to happen
				boolean error = false;
				TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
				int attempts = 0;
				while(foundsome && tip == null) {
					Thread.sleep(250);
					tip = SelfDB.getDB().getTxPoWTree().getTip();
					attempts++;
					if(attempts>5000) {
						error = true;
						break;
					}
				}
				
				if(error) {
					SelfLogger.log("ERROR : There was an error processing that FIRST IBD");
					break;
				}
				
				//Now wait to catch up..
				long timenow = System.currentTimeMillis();
				
				//SelfLogger.log("Waiting for chain to catch up.. please wait");
				attempts = 0;
				while(foundsome) {
					if(!tip.getBlockNumber().isEqual(endblock)) {
						Thread.sleep(50);
					}else {
						break;
					}
					
					tip = SelfDB.getDB().getTxPoWTree().getTip();
					
					attempts++;
					if(attempts>20000) {
						error = true;
						break;
					}
				}
				
				//Do we print a log..
				if((System.currentTimeMillis() - lastlogmessage)>5000) {
					SelfLogger.log("IBD Processed.. block:"+startblock+" @ "+SelfLogger.DATEFORMAT.format(new Date(starttime)));
					lastlogmessage = System.currentTimeMillis();
				}
				
				if(error) {
					SelfLogger.log("ERROR : There was an error processing that IBD - took too long");
					break;
				}
				
				//Do we have enough to ask again.. 
				if(size==0) {
					break;
				}
				
				//HACK
				//if(startblock.isMore(new MiniNumber(100000))) {
				//	SelfLogger.log("FORCE ARCHIVE STOP @ 100000");
				//	break;
				//}
			}
			
			//Notify the Android Listener
			NotifyListener(selflistener,"All blocks loaded.. pls wait");
			SelfLogger.log("All Archive data received and processed.. shutting down.."); 
			
			JSONObject resp = new JSONObject();
			resp.put("message", "Archive sync completed.. shutting down now.. please restart after");
			resp.put("start", firstStart.toString());
			resp.put("end", endblock.toString());
			ret.put("response", resp);
			
			//Don't do the usual shutdown hook
			SELFSystem.getInstance().setHasShutDown();
			
			//And NOW shut down..
			SELFSystem.getInstance().shutdownFinalProcs();
			
			//Now shutdown and save everything
			SelfDB.getDB().saveAllDB();
			
			//And NOW shut down..
			SELFSystem.getInstance().stopMessageProcessor();
			
			//Tell listener..
			SELFSystem.getInstance().NotifySELFListenerOfShutDown();
		
		}else if(action.equals("export")) {
			
			//The GZIPPED file 
			String file = getParam("file","archivebackup-"+System.currentTimeMillis()+".gzip");
			
			//Create the file
			File gzoutput = MiniFile.createBaseFile(file);
			if(gzoutput.exists()) {
				gzoutput.delete();
			}
			
			//Write out in GZIP format
			SelfLogger.log("Exporting ArchiveDB to H2 GZIPPED SQL..");
			SelfDB.getDB().getArchive().backupToFile(gzoutput,true);
			
			long gziplen = gzoutput.length();
			
			JSONObject resp = new JSONObject();
			resp.put("message", "Archive DB GZIPPED");
			resp.put("rows", SelfDB.getDB().getArchive().getSize());
			resp.put("file", gzoutput.getAbsolutePath());
			resp.put("size", MiniFormat.formatSize(gziplen));
			ret.put("response", resp);
		
		}else if(action.equals("exportraw")) {
			
			//Are there any records..
			if(arch.getSize()==0) {
				throw new CommandException("No blocks in ArchiveDB");
			}
			
			boolean logs = getBooleanParam("logs", false);
			
			//The GZIPPED file 
			String file = getParam("file","archivebackup-"+System.currentTimeMillis()+".raw.dat");
			
			//Create the file
			File rawoutput = MiniFile.createBaseFile(file);
			if(rawoutput.exists()) {
				rawoutput.delete();
			}
			
			//Make sure the parents exist
			File parent = rawoutput.getAbsoluteFile().getParentFile();
			if(!parent.exists()) {
				parent.mkdirs();
			}
			
			//Create output streams..
			FileOutputStream fix 		= new FileOutputStream(rawoutput);
			BufferedOutputStream bos 	= new BufferedOutputStream(fix, 65536);
			GZIPOutputStream gout 		= new GZIPOutputStream(bos, 65536);
			DataOutputStream dos 		= new DataOutputStream(gout);
			
			//Load first and last blocks..
			int mysqllastblock 	= arch.loadLastBlock().getTxPoW().getBlockNumber().getAsInt();
			int mysqlfirstblock = arch.loadFirstBlock().getTxPoW().getBlockNumber().getAsInt();
			
			SelfLogger.log("Exporting ArchiveDB to RAW block format..");
			
			//If it starts from 1 don't use a cascade
			boolean allowcascade = true;
			if(mysqllastblock == 1) {
				allowcascade = false;
				SelfLogger.log("Archive starts from 1 no need to use a cascade..");
			}
			
			//Load the cascade if it is there
			Cascade casc = arch.loadCascade();
			if(casc!=null && allowcascade) {
				SelfLogger.log("Cascade found in Archive DB..");
				
				//Write cacade out..
				MiniByte.TRUE.writeDataStream(dos);
				casc.writeDataStream(dos);
				
				//Reset the start point to the tip of the cascade
				mysqllastblock = casc.getTip().getTxPoW().getBlockNumber().getAsInt()+1;
				SelfLogger.log("Save blocks from cascade tip onwards.. "+(mysqllastblock-1));
				
			}else {
				MiniByte.FALSE.writeDataStream(dos);
				SelfLogger.log("No cascade added");
			}
			
			//How many entries..
			int total = mysqlfirstblock - mysqllastblock +1;
			SelfLogger.log("Add records from  : "+mysqllastblock);
			SelfLogger.log("Total records to add : "+total);
			
			//Max specified..
			if(existsParam("maxexport")) {
				int max = getNumberParam("maxexport").getAsInt();
				SelfLogger.log("Max export specified.. : "+max);
				if(total>max) {
					total = max;
				}
			}
			
			MiniNumber tot = new MiniNumber(total);
			tot.writeDataStream(dos);
			
			int outcounter = 0;
			
			//Load a range..
			long firstblock = -1;
			long endblock 	= -1;
			TxBlock lastblock = null;
			
			long startload 	= mysqllastblock-1;
			int counter = 0;
			while(true) {
				
				//Small log message
				if(counter % 20 == 0) {
					if(logs) {
						SelfLogger.log("Loading from Archive @ "+startload);
					}
				}
				
				ArrayList<TxBlock> blocks = arch.loadBlockRange(new MiniNumber(startload),new MiniNumber(startload).add(MiniNumber.HUNDRED),false);
				if(blocks.size()==0) {
					//All blocks checked
					break;
				}
				
				//Cycle and add to our DB..
				for(TxBlock block : blocks) {
					
					//Send to data export file..
					block.writeDataStream(dos);
					if(lastblock == null) {
						firstblock = block.getTxPoW().getBlockNumber().getAsLong();
					}
					lastblock = block;
					endblock  = block.getTxPoW().getBlockNumber().getAsLong();
					
					//Increase counter
					outcounter++;
					if(outcounter>=total) {
						break;
					}
				}
				
				if(outcounter>=total) {
					SelfLogger.log("Finished loading blocks..");
					break;
				}
				
				startload = endblock;
				
				//Clean up..
				counter++;
				if(counter % 20 == 0) {
					System.gc();
				}
			}			
			
			//Flush data
			dos.flush();
			
			try {
				dos.close();
				gout.close();
				bos.close();
				fix.close();
			}catch(Exception exc) {
				SelfLogger.log(exc);
			}
			
			JSONObject resp = new JSONObject();
			resp.put("start", firstblock);
			resp.put("end", endblock);
			resp.put("total", outcounter);
			resp.put("file", rawoutput.getName());
			resp.put("path", rawoutput.getAbsolutePath());
			resp.put("size", MiniFormat.formatSize(rawoutput.length()));
			
			ret.put("response", resp);
		
		}else if(action.equals("importold")) {
			
			//Get the file
			String file = getParam("file");
			
			//Does it exist..
			File restorefile = MiniFile.createBaseFile(file);
			if(!restorefile.exists()) {
				throw new Exception("Restore file doesn't exist : "+restorefile.getAbsolutePath());
			}
			
			//And now restore
			ArchiveManager archtemp = new ArchiveManager();
			
			//Create a temp DB file..
			File restorefolder = new File(GeneralParams.DATA_FOLDER,"archiverestore");
			restorefolder.mkdirs();
			
			File tempdb = new File(restorefolder,"archivetemp");
			if(tempdb.exists()) {
				tempdb.delete();
			}
			archtemp.loadDB(tempdb);
			
			//Restore from File..
			SelfLogger.log("Restoring ArchiveDB from file..");
			archtemp.restoreFromFile(restorefile,true);
			
			Cascade casc = archtemp.loadCascade();
			if(casc != null) {
				SelfLogger.log("Archive DB cascade start : "+casc.getTip().getTxPoW().getBlockNumber()+" length:"+casc.getLength());
			}
			
			TxBlock first 	= archtemp.loadFirstBlock();
			if(first!=null) {
				SelfLogger.log("Archive DB first block : "+first.getTxPoW().getBlockNumber());
			}
			TxBlock last 	= archtemp.loadLastBlock();
			if(last!=null) {
				SelfLogger.log("Archive DB last block : "+last.getTxPoW().getBlockNumber());
			}
			
			//Set this statically..
			STATIC_TEMPARCHIVE = archtemp;
			
			//Now run a chain sync.. with correct params
			String command = "archive action:resync host:"+LOCAL_ARCHIVE;
			if(existsParam("phrase")) {
				command = command+" phrase:\""+getParam("phrase")+"\"";
			}
			
			if(existsParam("keys")) {
				command = command+" keys:"+getParam("keys");
			}
			
			if(existsParam("keyuses")) {
				command = command+" keyuses:"+getParam("keyuses");
			}
			
			if(existsParam("anyphrase")) {
				command = command+" anyphrase:"+getParam("anyphrase");
			}
			
			JSONArray res 		= CommandRunner.getRunner().runMultiCommand(command);
			JSONObject result 	= (JSONObject) res.get(0);
			
			//Shutdwon TEMP DB
			archtemp.saveDB(false);
			
			//Delete the restore folder
			MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, restorefolder);
			
			//Reset 
			STATIC_TEMPARCHIVE = null;
			
			JSONObject resp = new JSONObject();
			resp.put("archiveresync", result);
			ret.put("response", resp);
				
		}else if(action.equals("import")) {
			
			//Get the file
			String file = getParam("file");
			
			//Does it exist..
			File restorefile = MiniFile.createBaseFile(file);
			if(!restorefile.exists()) {
				throw new Exception("Restore file doesn't exist : "+restorefile.getAbsolutePath());
			}
			
			//Is it an H2 gzip or a raw dat
			boolean h2import = true; 
			if(file.endsWith(".dat")) {
				h2import = false;
			}
			
			if(h2import) {
				SelfLogger.log("H2 archive imprt started..");
				
				//And now restore
				ArchiveManager archtemp = new ArchiveManager();
				H2_TEMPARCHIVE			= true;
				
				//Create a temp DB file..
				File restorefolder = new File(GeneralParams.DATA_FOLDER,"archiverestore");
				restorefolder.mkdirs();
				
				File tempdb = new File(restorefolder,"archivetemp");
				if(tempdb.exists()) {
					tempdb.delete();
				}
				archtemp.loadDB(tempdb);
				
				//Restore from File..
				SelfLogger.log("Restoring ArchiveDB from file..");
				archtemp.restoreFromFile(restorefile,true);
				
				Cascade casc = archtemp.loadCascade();
				if(casc != null) {
					SelfLogger.log("Archive DB cascade start : "+casc.getTip().getTxPoW().getBlockNumber()+" length:"+casc.getLength());
				}
				
				TxBlock first 	= archtemp.loadFirstBlock();
				if(first!=null) {
					SelfLogger.log("Archive DB first block : "+first.getTxPoW().getBlockNumber());
				}
				TxBlock last 	= archtemp.loadLastBlock();
				if(last!=null) {
					SelfLogger.log("Archive DB last block : "+last.getTxPoW().getBlockNumber());
				}
				
				//Set this statically..
				STATIC_TEMPARCHIVE = archtemp;
			
				//Now run a chain sync.. with correct params
				String command = "archive action:resync host:"+LOCAL_ARCHIVE;
				if(existsParam("phrase")) {
					command = command+" phrase:\""+getParam("phrase")+"\"";
				}
				
				if(existsParam("keys")) {
					command = command+" keys:"+getParam("keys");
				}
				
				if(existsParam("keyuses")) {
					command = command+" keyuses:"+getParam("keyuses");
				}
				
				if(existsParam("anyphrase")) {
					command = command+" anyphrase:"+getParam("anyphrase");
				}
				
				JSONArray res 		= CommandRunner.getRunner().runMultiCommand(command);
				JSONObject result 	= (JSONObject) res.get(0);
				
				//Shutdown TEMP DB
				archtemp.saveDB(false);
				
				//Delete the restore folder
				MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, restorefolder);
				
				//Reset 
				STATIC_TEMPARCHIVE = null;
				
				JSONObject resp = new JSONObject();
				resp.put("archiveresync", result);
				ret.put("response", resp);
			
			}else {
				SelfLogger.log("RAW archive import started..");
				
				//RAW import..
				H2_TEMPARCHIVE	= false;
				
				//Set this statically..
				STATIC_RAW = new RawArchiveInput(restorefile);
				STATIC_RAW.connect();
				
				//Now run a chain sync.. with correct params
				String command = "archive action:resync host:"+LOCAL_ARCHIVE;
				if(existsParam("phrase")) {
					command = command+" phrase:\""+getParam("phrase")+"\"";
				}
				
				if(existsParam("keys")) {
					command = command+" keys:"+getParam("keys");
				}
				
				if(existsParam("keyuses")) {
					command = command+" keyuses:"+getParam("keyuses");
				}
				
				if(existsParam("anyphrase")) {
					command = command+" anyphrase:"+getParam("anyphrase");
				}
				
				JSONArray res 		= CommandRunner.getRunner().runMultiCommand(command);
				JSONObject result 	= (JSONObject) res.get(0);
				
				//Shutdown TEMP DB
				STATIC_RAW.stop();
				
				//Reset 
				STATIC_RAW = null;
				
				JSONObject resp = new JSONObject();
				resp.put("archiveresync", result);
				ret.put("response", resp);
			}
			
		}else if(action.equals("importraw")) {
			
			//Get the file
			String file = getParam("file");
			
			//Does it exist..
			File restorefile = MiniFile.createBaseFile(file);
			if(!restorefile.exists()) {
				throw new Exception("Restore file doesn't exist : "+restorefile.getAbsolutePath());
			}
			
			//reset ALL the default data
			SELFSystem.getInstance().archiveResetReady(false);
			
			RawArchiveInput rawin = new RawArchiveInput(restorefile);
			rawin.connect();
			
			int ibdcount = 0;
			while(true) {
				
				//Get the next IBD
				IBD syncibd = rawin.getNextIBD();
				
				//Are there any blocks..
				int size = syncibd.getTxBlocks().size();
				if(size==0) {
					break;
				}
				
				//Get the main processor
				TxPoWProcessor proc = SELFSystem.getInstance().getTxPoWProcessor();
				proc.postProcessArchiveIBD(syncibd, "0x00");
				ibdcount++;
				
				//Do this every 10 IBD..
				if(ibdcount % 20 == 0) {
					
					//Last block
					TxBlock block = syncibd.getTxBlocks().get(size-1);
					
					//Now wait for something to happen
					TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
					while(tip == null) {
						Thread.sleep(50);
						tip = SelfDB.getDB().getTxPoWTree().getTip();
					}
					
					long starttime = block.getTxPoW().getTimeMilli().getAsLong();
					SelfLogger.log("Processing block:"+block.getTxPoW().getBlockNumber()+" @ "+SelfLogger.DATEFORMAT.format(new Date(starttime)));
					
					while(!tip.getBlockNumber().isEqual(block.getTxPoW().getBlockNumber())) {
						Thread.sleep(50);
						tip = SelfDB.getDB().getTxPoWTree().getTip();
					}
					
					//Clean up..
					SELFSystem.getInstance().resetMemFull();
				}
			}
			
			//Close it down..
			rawin.stop();
			
			SelfLogger.log("All blocks processed..");
			
			JSONObject resp = new JSONObject();
			resp.put("message", "Archive sync completed.. shutting down now.. please restart after");
			ret.put("response", resp);
			
			//Don't do the usual shutdown hook
			SELFSystem.getInstance().setHasShutDown();
			
			//And NOW shut down..
			SELFSystem.getInstance().shutdownFinalProcs();
			
			//Now shutdown and save everything
			SelfDB.getDB().saveAllDB();
			
			//And NOW shut down..
			SELFSystem.getInstance().stopMessageProcessor();
			
			//Tell listener..
			SELFSystem.getInstance().NotifySELFListenerOfShutDown();
			
		}else if(action.equals("inspectraw")) {
			
			long timestart = System.currentTimeMillis();
			
			//Create a temp name
			String infile 	= getParam("file");
			File fileinfile = MiniFile.createBaseFile(infile);
			
			RawArchiveInput rawin = new RawArchiveInput(fileinfile);
			rawin.connect();
			
			//Is there a cascade..
			Cascade casc = rawin.getCascade();
			if(casc != null) {
				SelfLogger.log("Cascade found.. ");
			}
			
			//Load a range..
			long endblock 	= -1;
			TxBlock lastblock = null;
			int counter = 0;
			
			MiniNumber lasttxblock = MiniNumber.ZERO;
			while(true) {
				//Get the next batch of data..
				IBD syncibd 				= rawin.getNextIBD();
				ArrayList<TxBlock> blocks 	= syncibd.getTxBlocks();
				
				if(counter % 10 ==0) {
					if(blocks.size()>0) {
						SelfLogger.log("Loading from RAW Block : "+blocks.get(0).getTxPoW().getBlockNumber(),false);
					}
				}
			
				if(blocks.size()==0) {
					//All blocks checked
					break;
				}
				
				//Cycle and add to our DB..
				boolean exit = false;
				for(TxBlock block : blocks) {
					
					//Which block is this..
					MiniNumber blocknum = block.getTxPoW().getBlockNumber();
					
					//Check it..
					if(!blocknum.isEqual(lasttxblock.increment())){
						SelfLogger.log("INVALID non sequential blocks.. lastblock:"+lasttxblock+" new:"+blocknum);
						exit = true;
						break;
					}
					
					//Keep it..
					lasttxblock = blocknum;
				}
				
				//Clean up..
				counter++;
				if(counter % 10 == 0) {
					System.gc();
				}
				
				if(exit) {
					break;
				}
			}
			
			//Shutdown TEMP DB
			rawin.stop();
			
			long timediff = System.currentTimeMillis() - timestart;
			
			JSONObject resp = new JSONObject();
			resp.put("time", MiniFormat.ConvertMilliToTime(timediff));
			
			ret.put("response", resp);
			
		}else if(action.equals("inspect")) {
			
			//Get the file
			String file = getParam("file","");
			if(file.endsWith(".dat")) {
				throw new CommandException("inspect only works for H2 archive files");
			}
			
			//Where the temp db will go..
			File restorefolder 	= new File(GeneralParams.DATA_FOLDER,"archiverestore");
			
			//Delete the restore folder - just in case is already there
			MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, restorefolder);
			
			//Do we load one..
			ArchiveManager archtemp = null;
			if(!file.equals("")) {
				//Does it exist..
				File restorefile = MiniFile.createBaseFile(file);
				if(!restorefile.exists()) {
					throw new Exception("Restore file doesn't exist : "+restorefile.getAbsolutePath());
				}
				
				//And now restore
				archtemp = new ArchiveManager();
				
				//Create a temp DB file..
				restorefolder.mkdirs();
				
				File tempdb = new File(restorefolder,"archivetemp");
				if(tempdb.exists()) {
					tempdb.delete();
				}
				archtemp.loadDB(tempdb);
				
				SelfLogger.log("Restoring ArchiveDB from file..");
				archtemp.restoreFromFile(restorefile,true);
			}else {
				archtemp = SelfDB.getDB().getArchive();
			}
			
			//Inspect File..
			JSONObject resp 	= new JSONObject();
			JSONObject jcasc 	= new JSONObject();
			JSONObject jarch 	= new JSONObject();
			
			resp.put("cascade", jcasc);
			resp.put("archive", jarch);
			
			jcasc.put("exists", false);
			jcasc.put("start", "-1");
			jcasc.put("length", "-1");
			jarch.put("first", "-1");
			jarch.put("last", "-1");
			jarch.put("size", "-1");
			
			Cascade casc = archtemp.loadCascade();
			if(casc != null) {
				jcasc.put("exists", true);
				jcasc.put("start", casc.getTip().getTxPoW().getBlockNumber().toString());
				jcasc.put("length", casc.getLength());
			}
			
			TxBlock first 	= archtemp.loadFirstBlock();
			if(first!=null) {
				jarch.put("first", first.getTxPoW().getBlockNumber().toString());
			}
			TxBlock last 	= archtemp.loadLastBlock();
			if(last!=null) {
				jarch.put("last", last.getTxPoW().getBlockNumber().toString());
			}
			
			//And finally the size
			jarch.put("size", archtemp.getSize());
			
			//Shutdwon TEMP DB
			if(!file.equals("")) {
				archtemp.saveDB(false);
			}
			
			//Delete the restore folder
			MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, restorefolder);
		
			ret.put("response", resp);
			
		}else if(action.equals("addresscheck")) {
			
			//Which address are we looking for
			String address = getAddressParam("address");
			
			//Is there a state aswell ? 
			String statecheck = getParam("statecheck", "");
			if(	statecheck.toLowerCase().startsWith("sx") && 
				statecheck.indexOf("@")==-1) {
				//Convert to 0x format - as all statevariables are
				statecheck = Address.convertSelfAddress(statecheck).to0xString();
			}
			
			//Cycle through
			JSONObject resp 	= new JSONObject();
			JSONArray inarr 	= new JSONArray();
			JSONArray outarr 	= new JSONArray();
			
			ArchiveManager adb 		= SelfDB.getDB().getArchive();
			TxBlock startblock 		= adb.loadLastBlock();
			
			MiniNumber firstStart = MiniNumber.ZERO;
			boolean canstart = true;
			if(startblock == null) {
				canstart = false;
			}else {
				firstStart   = startblock.getTxPoW().getBlockNumber().decrement();
				SelfLogger.log("Start archive @ "+firstStart);
			}
			
			while(canstart) {
				
				//Create an IBD for the mysql data
				ArrayList<TxBlock> mysqlblocks = adb.loadBlockRange(firstStart, firstStart.add(MiniNumber.HUNDRED), false);
				if(mysqlblocks.size()==0) {
					//No blocks left
					break;
				}
				
				for(TxBlock block : mysqlblocks) {
					
					//For the next loop
					firstStart = block.getTxPoW().getBlockNumber(); 
					
					//Get details
					TxPoW txp 			= block.getTxPoW();
					long blocknumber 	= txp.getBlockNumber().getAsLong();
					
					//Date string
					String date = SelfLogger.DATEFORMAT.format(new Date(txp.getTimeMilli().getAsLong()));
					
					//Created
					ArrayList<Coin> outputs 		= block.getOutputCoins();
					for(Coin cc : outputs) {
						if(cc.getAddress().to0xString().equals(address)) {
							
							boolean found = true;
							if(!statecheck.equals("")) {
								//Check for state aswell..
								found = cc.checkForStateVariable(statecheck);
							}
							
							if(found) {
								SelfLogger.log("BLOCK "+blocknumber+" CREATED COIN : "+cc.toString());
								
								JSONObject created = new JSONObject();
								created.put("block", blocknumber);
								created.put("date", date);
								created.put("datemilli", txp.getTimeMilli().toString());
								created.put("coin", cc.toJSON());
								outarr.add(created);
							}
							
						}
					}
					
					//Spent
					ArrayList<CoinProof> inputs  	= block.getInputCoinProofs();
					for(CoinProof incoin : inputs) {
						if(incoin.getCoin().getAddress().to0xString().equals(address)) {
							
							boolean found = true;
							if(!statecheck.equals("")) {
								//Check for state aswell..
								found = incoin.getCoin().checkForStateVariable(statecheck);
							}
							
							if(found) {
								SelfLogger.log("BLOCK "+blocknumber+" SPENT COIN : "+incoin.getCoin().toString());
								
								JSONObject spent = new JSONObject();
								spent.put("block", blocknumber);
								spent.put("date", date);
								spent.put("datemilli", txp.getTimeMilli().toString());
								spent.put("coin", incoin.getCoin().toJSON());
								inarr.add(spent);
							}
						}
					}
				}
			}
			
			//And Now check the chain..
			if(startblock!=null) {
				SelfLogger.log("End archive @ "+firstStart);
			}
			
			SelfLogger.log("Checking BlockChain.. descending");
			if(SelfDB.getDB().getTxPoWTree() != null) {
				TxPoWTreeNode top = SelfDB.getDB().getTxPoWTree().getTip();
				while(top != null) {
					TxBlock block = top.getTxBlock();
					
					//Get details
					TxPoW txp 			= block.getTxPoW();
					long blocknumber 	= txp.getBlockNumber().getAsLong();
					
					//Date string
					String date = SelfLogger.DATEFORMAT.format(new Date(txp.getTimeMilli().getAsLong()));
					
					//Created
					ArrayList<Coin> outputs 		= block.getOutputCoins();
					for(Coin cc : outputs) {
						if(cc.getAddress().to0xString().equals(address)) {
							
							boolean found = true;
							if(!statecheck.equals("")) {
								//Check for state aswell..
								found = cc.checkForStateVariable(statecheck);
							}
							
							if(found) {
								SelfLogger.log("BLOCK "+blocknumber+" CREATED COIN : "+cc.toString());
								
								JSONObject created = new JSONObject();
								created.put("block", blocknumber);
								created.put("date", date);
								created.put("datemilli", txp.getTimeMilli().toString());
								created.put("coin", cc.toJSON());
								outarr.add(created);
							}
						}
					}
					
					//Spent
					ArrayList<CoinProof> inputs  	= block.getInputCoinProofs();
					for(CoinProof incoin : inputs) {
						if(incoin.getCoin().getAddress().to0xString().equals(address)) {
							
							boolean found = true;
							if(!statecheck.equals("")) {
								//Check for state aswell..
								found = incoin.getCoin().checkForStateVariable(statecheck);
							}
							
							if(found) {
								SelfLogger.log("BLOCK "+blocknumber+" SPENT COIN : "+incoin.getCoin().toString());
								
								JSONObject spent = new JSONObject();
								spent.put("block", blocknumber);
								spent.put("date", date);
								spent.put("datemilli", txp.getTimeMilli().toString());
								spent.put("coin", incoin.getCoin().toJSON());
								inarr.add(spent);
							}
						}
					}
					
					top = top.getParent();
				}
			}
			
			SelfLogger.log("All checks complete..");
			
			resp.put("created", outarr);
			resp.put("spent", inarr);
			ret.put("coins", resp);
			
		}else {
			throw new CommandException("Invalid action : "+action);
		}
		
		return ret;
	}
	
	public static void NotifyListener(MessageListener zListener, String zMessage) throws Exception {
		//Notify
		if(zListener != null) {
			
			//Details..
			JSONObject data = new JSONObject();
			data.put("message", zMessage);
			
			//Create the JSON Message
			JSONObject notify = new JSONObject();
			notify.put("event", "ARCHIVEUPDATE");
			notify.put("data", data);
			
			Message msg = new Message(NotifyManager.NOTIFY_POST);
			msg.addObject("notify", notify);
			
			//Notify them that something is happening..
			zListener.processMessage(msg);
		}
	}
	
	@Override
	public Command getFunction() {
		return new archive();
	}

	/**
	 * A special PING message to  check a valid connection..
	 */
	public static IBD sendArchiveReq(String zHost, int zPort, MiniNumber zStartBlock) {
		return sendArchiveReq(zHost, zPort, zStartBlock, 3);
	}
	
	public static IBD sendArchiveReq(String zHost, int zPort, MiniNumber zStartBlock, int zAttempts) {
			
		IBD ibd= null;
		
		int attempts = 0;
		
		while(attempts<zAttempts) {
			try {
				
				//Create the Network Message
				MiniData msg = NIOManager.createNIOMessage(NIOMessage.MSG_ARCHIVE_REQ, zStartBlock);
				
				//Open the socket..
				Socket sock = new Socket();
	
				//3 seconds to connect
				sock.connect(new InetSocketAddress(zHost, zPort), 10000);
				
				//10 seconds to read
				sock.setSoTimeout(10000);
				
				//Create the streams..
				OutputStream out 		= sock.getOutputStream();
				DataOutputStream dos 	= new DataOutputStream(out);
				
				InputStream in			= sock.getInputStream();
				DataInputStream dis 	= new DataInputStream(in);
				
				//Write the data
				msg.writeDataStream(dos);
				dos.flush();
				
				//Tell the NIO
				SELFSystem.getInstance().getNIOManager().getTrafficListener().addWriteBytes("sendArchiveReq",msg.getLength());
				
				//Load the message
				MiniData resp = MiniData.ReadFromStream(dis);
				
				//Tell the NIO
				SELFSystem.getInstance().getNIOManager().getTrafficListener().addReadBytes("sendArchiveReq",resp.getLength());
				
				//Close the streams..
				dis.close();
				in.close();
				dos.close();
				out.close();
				
				//Convert
				ByteArrayInputStream bais 	= new ByteArrayInputStream(resp.getBytes());
				DataInputStream bdis 		= new DataInputStream(bais);
	
				//What Type..
				MiniByte type = MiniByte.ReadFromStream(bdis);
				
				//Load the IBD
				ibd = IBD.ReadFromStream(bdis);
				
				bdis.close();
				bais.close();
			
				break;
				
			}catch(Exception exc){
				SelfLogger.log("Archive connection : "+exc+" @ "+zHost+":"+zPort);
				
				//Null the IBD
				ibd= null;
				
				//Increase attempts
				attempts++;			
				
				if(attempts<zAttempts) {
					SelfLogger.log(attempts+" Attempts > Wait 10 seconds and re-attempt..");
					
					//Wait 10 seconds
					try {Thread.sleep(10000);} catch (InterruptedException e) {}
					
					SelfLogger.log("Re-attempt started..");
				}
			}
		}
		
		return ibd;
	}
}
