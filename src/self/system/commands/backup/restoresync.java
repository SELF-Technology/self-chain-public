package org.self.system.commands.backup;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import org.self.database.SelfDB;
import org.self.database.txpowdb.sql.TxPoWList;
import org.self.database.txpowdb.sql.TxPoWSqlDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.database.wallet.Wallet;
import org.self.objects.IBD;
import org.self.objects.TxBlock;
import org.self.objects.TxPoW;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.system.commands.network.connect;
import org.self.system.params.GeneralParams;
import org.self.utils.MiniFile;
import org.self.utils.SelfLogger;
import org.self.utils.encrypt.GenerateKey;
import org.self.utils.json.JSONObject;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageListener;
import org.self.utils.ssl.SSLManager;

public class restoresync extends Command {

	public restoresync() {
		super("restoresync","[file:] (password:) (host:) (keyuses:) - Restore the entire system AND perform an archive sync. Use when the backup is old.");
	}
	
	@Override
	public String getFullHelp() {
		return "\nrestoresync\n"
				+ "\n"
				+ "Restore your node from a backup and then sync to the top block using an archive node.\n"
				+ "\n"
				+ "You MUST wait until all the keys for the node are created before this is allowed.\n"
				+ "\n"
				+ "Reverts to a standard restore if backup is not older than 2 days.\n"
				+ "\n"
				+ "file:\n"
				+ "    Specify the filename or local path of the backup to restore\n"
				+ "\n"
				+ "password: (optional)\n"
				+ "    Enter the password of the backup \n"
				+ "\n"
				+ "host: (optional)\n"
				+ "    ip:port of the archive node to sync from.\n"
				+ "\n"
				+ "keyuses: (optional) \n"
				+ "    Increment (not set) the number of key uses per key.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "restoresync file:my-full-backup-01-Jan-22 password:Longsecurepassword456 host:89.98.89.98:9001\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"file","password","host"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		//Can only do this if all keys created..
		vault.checkAllKeysCreated();
				
		String file = getParam("file","");
		if(file.equals("")) {
			throw new Exception("MUST specify a file to restore from");
		}
		
		//Get a password if there is one..
		String password = getParam("password","self");
		if(password.equals("")) {
			throw new CommandException("Cannot have a blank password");
		}
		
		//Does it exist..
		File restorefile = MiniFile.createBaseFile(file);
		if(!restorefile.exists()) {
			throw new Exception("Restore file doesn't exist : "+restorefile.getAbsolutePath());
		}
		
		//Clean up the memory
		System.gc();
		
		///Base folder
		File restorefolder = new File(GeneralParams.DATA_FOLDER, "restore");
		restorefolder.mkdirs();
		
		//Open the file..
		byte[] restoredata = MiniFile.readCompleteFile(restorefile);
		
		//Now start reading in the sections..
		ByteArrayInputStream bais 	= new ByteArrayInputStream(restoredata);
		DataInputStream dis 		= new DataInputStream(bais);
		
		//Read in the SALT and IVParam
		MiniData salt 		= MiniData.ReadFromStream(dis);
		MiniData ivparam 	= MiniData.ReadFromStream(dis);
		
		//Create an AES SecretKey with Password and Salt
		byte[] secret = GenerateKey.secretKey(password,salt.getBytes()).getEncoded();
		
		//Create the cipher..
		Cipher ciph = GenerateKey.getCipherSYM(Cipher.DECRYPT_MODE, ivparam.getBytes(), secret);
		CipherInputStream cis 	= new CipherInputStream(dis, ciph);
		
		GZIPInputStream gzin 	= null;
		try {
			gzin 	= new GZIPInputStream(cis);
		}catch(Exception exc) {
			//Incorrect password ?
			throw new CommandException("Incorrect Password!");
		}
		DataInputStream disciph = new DataInputStream(gzin);
		
		//If it has not stopped - First stop everything.. and get ready to restore the files..
		SELFSystem.getInstance().restoreReady(false);
				
		//The total size of files..
		long total = 1;
		
		//Read in each section..
		total += readNextBackup(new File(restorefolder,"wallet.sql"), disciph);
		
		//Stop saving state
		SelfDB.getDB().setAllowSaveState(false);
		
			SelfLogger.log("Restoring backup files..");
		
			//The rest write directly 
			File basedb = SelfDB.getDB().getBaseDBFolder();
			
			File cascfile = new File(basedb,"cascade.db");
			total += readNextBackup(cascfile, disciph);
			
			File treefile = new File(basedb,"chaintree.db");
			total += readNextBackup(treefile, disciph);
			
			File udb = new File(basedb,"userprefs.db");
			total += readNextBackup(udb, disciph);
			
			File p2pdb = new File(basedb,"p2p.db");
			total += readNextBackup(p2pdb, disciph);
			
			//Now Load these values 
			SelfDB.getDB().getUserDB().loadDB(udb);
			SelfDB.getDB().getUserDB().clearUninstalledMiniDAPP();
			SelfDB.getDB().getP2PDB().loadDB(p2pdb);
			SelfDB.getDB().getCascade().loadDB(cascfile);
			SelfDB.getDB().getTxPoWTree().loadDB(treefile);
			
			//Now load the relevant TxPoW
			TxPoWList txplist = readNextTxPoWList(disciph);
			
			//And add these to the DB
			TxPoWSqlDB txpsqldb = SelfDB.getDB().getTxPoWDB().getSQLDB();
			txpsqldb.wipeDB();
			for(TxPoW txp : txplist.mTxPoWs) {
				txpsqldb.addTxPoW(txp, true);
			}
		
		//Close up shop..
		disciph.close();
		cis.close();
		dis.close();
		gzin.close();
		bais.close();
		
		//Allow saving state
		SelfDB.getDB().setAllowSaveState(true);
		
		//Now load the sql
		SelfDB.getDB().getWallet().restoreFromFile(new File(restorefolder,"wallet.sql"));
		SelfDB.getDB().getWallet().saveDB(false);
		
		//Close
		SelfDB.getDB().getTxPoWDB().getSQLDB().saveDB(false);
		
		//Wipe ArchiveDB	
		SelfDB.getDB().getArchive().saveDB(false);
		SelfDB.getDB().getArchive().getSQLFile().delete();
		
		//And now clean up..
		MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, restorefolder);
		
		//And will need to recreate the SSL
		MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, SSLManager.getSSLFolder());
		
		//Now reopen the required SQL Dbs..
		SELFSystem.getInstance().restoreReadyForSync();
				
		//Shall we do a sync..
		TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();

		long timemilli  = tip.getTxPoW().getTimeMilli().getAsLong();
		long timediff   = System.currentTimeMillis() - timemilli;
		long maxtime 	= 1000 * 60 * 60 * 24 * 2;
		
		//How many keyuses
		int keyuses = getNumberParam("keyuses", new MiniNumber(256)).getAsInt();
				
		//Do we even need to do a sync..
		if(timediff < maxtime || !existsParam("host")) {
			
			if(existsParam("host")) {
				SelfLogger.log("No Sync required as new backup");
			}
			
			//Update key uses
			SelfDB.getDB().getWallet().updateIncrementAllKeyUses(keyuses);
			
			//Don't do the usual shutdown hook
			SELFSystem.getInstance().setHasShutDown();
			
			//And NOW shut down..
			SELFSystem.getInstance().shutdownFinalProcs();
			
			//Now save the Databases..
			SelfDB.getDB().saveAllDB();
			
			//And NOW shut down..
			SELFSystem.getInstance().stopMessageProcessor();
			
			//Tell listener..
			SELFSystem.getInstance().NotifySELFListenerOfShutDown();
			
			//And send data
			JSONObject resp2 = new JSONObject();
			resp2.put("file", restorefile.getAbsolutePath());
			ret.put("restore", resp2);
			ret.put("message", "Restart Self for restore to take effect!");
			
			return ret;
		}
		
		//Is there a host
		if(existsParam("host")) {
				
			//Get the TxPowTree
			TxPoWTreeNode nottip = tip.getParent(128);
			
			//What block
			MiniNumber startblock = nottip.getBlockNumber(); 
			SelfLogger.log("Start sync from "+startblock);
		
			String host = getParam("host");
			
			//Now do a resync..
			performResync(	host, keyuses, startblock, true);
			
			//Get the TxPowTree
			tip = SelfDB.getDB().getTxPoWTree().getTip();
			
			//What block
			SelfLogger.log("End sync on "+tip.getBlockNumber());
		}
		
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
		
		//And send data
		JSONObject resp = new JSONObject();
		resp.put("file", restorefile.getAbsolutePath());
		ret.put("restore", resp);
		ret.put("message", "Restart Self for restore to take effect!");
		
		return ret;
	}
	
	private long readNextBackup(File zOutput, DataInputStream zDis) throws IOException {
		MiniData data = MiniData.ReadFromStream(zDis);
		MiniFile.writeDataToFile(zOutput, data.getBytes());
		return zOutput.length();
	}
	
	private TxPoWList readNextTxPoWList(DataInputStream zDis) throws IOException {
		MiniData data 		= MiniData.ReadFromStream(zDis);
		TxPoWList txplist 	= TxPoWList.convertMiniDataVersion(data);
		return txplist;
	}

	@Override
	public Command getFunction() {
		return new restoresync();
	}

	/**
	 * Perform a resync
	 * @throws Exception 
	 */
	public JSONObject performResync(String zHost, int zKeyUses, MiniNumber zStartBlock, boolean zIncrementKeys) throws Exception {
		
		//Get the Self Listener..
		MessageListener selflistener = SELFSystem.getInstance().getSelfListener();
		
		//Notify the Android Listener
		archive.NotifyListener(selflistener,"Loading sync blocks from "+zStartBlock);
		
		//Get the host
		String fullhost = zHost;
		
		//Is it auto
//		if(fullhost.equals("auto")) {
//			
//			//Choose one from our default list
//			int size  	= P2PParams.DEFAULT_ARCHIVENODE_LIST.size();
//			int rand  	= new Random().nextInt(size);
//			
//			InetSocketAddress archaddr = P2PParams.DEFAULT_ARCHIVENODE_LIST.get(rand);
//			String ip 	= archaddr.getHostString();
//			int port    = archaddr.getPort();
//			fullhost	= ip+":"+port;
//			
//			SelfLogger.log("RANDOM ARCHIVE HOST : "+rand+" host:"+fullhost);
//		}
		
		Message connectdata = connect.createConnectMessage(fullhost);
		
		String host = connectdata.getString("host");
		int port 	= connectdata.getInteger("port");
		
		//Before we start deleting - check connection..
		IBD ibdtest = archive.sendArchiveReq(host, port, MiniNumber.MINUSONE);
		if(ibdtest == null) {
			SelfLogger.log("Could not connect to Archive host! @ "+host+":"+port);
			return new JSONObject();
			//throw new CommandException("Could not connect to Archive host! @ "+host+":"+port);
		}
		
		//reset ALL the default data
		SELFSystem.getInstance().archiveResetReady(false,false);
	
		//Get the Wallet
		Wallet wallet = SelfDB.getDB().getWallet();
		
		//Now Update the USES - since they may have been used before - we don;t know..
		if(zIncrementKeys) {
			wallet.updateIncrementAllKeyUses(zKeyUses);
		}else {
			wallet.updateAllKeyUses(zKeyUses);
		}
		
		//Now cycle through the chain..
		MiniNumber startblock 	= zStartBlock;
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
			if(counter % 5 == 0) {
				long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				if(mem > 250 * 1024 * 1024) {
					SELFSystem.getInstance().resetMemFull();
				}else {
					//SelfLogger.log("RAM memory usage still low.. wait for cleanup");
				}
			}
			
			//Send him a message..
			ibd = archive.sendArchiveReq(host, port, startblock);
			if(ibd == null) {
				SelfLogger.log("No blocks returned..");
				ibd = new IBD();
				//throw new CommandException("Connection error @ "+host+":"+port);
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
				archive.NotifyListener(selflistener,"Loading "+start.getTxPoW().getBlockNumber()+" @ "+new Date(start.getTxPoW().getTimeMilli().getAsLong()).toString());
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
			
//			long timediff = System.currentTimeMillis() - timenow;
//			SelfLogger.log("IBD Processed.. block:"+startblock+" time:"+timediff+"ms");
			
			if(error) {
				SelfLogger.log("ERROR : There was an error processing that IBD - took too long");
				break;
			}
			
			//Do we have enough to ask again.. 
			if(size==0) {
				break;
			}
		}
		
		//Notify the Android Listener
		archive.NotifyListener(selflistener,"All blocks loaded.. pls wait");
		SelfLogger.log("All Archive data received and processed.. shutting down.."); 
		
		JSONObject resp = new JSONObject();
		resp.put("message", "Archive sync completed.. shutting down now.. please restart after");
		resp.put("start", firstStart.toString());
		resp.put("end", endblock.toString());
		
		return resp;
	}
}
