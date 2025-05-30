package org.self.system.commands.backup.mmrsync;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import org.self.database.SelfDB;
import org.self.database.mmr.MMR;
import org.self.database.mmr.MMRData;
import org.self.database.mmr.MMRProof;
import org.self.database.mmr.MegaMMR;
import org.self.database.txpowtree.TxPowTree;
import org.self.objects.Coin;
import org.self.objects.CoinProof;
import org.self.objects.IBD;
import org.self.objects.TxBlock;
import org.self.objects.TxPoW;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.system.commands.CommandRunner;
import org.self.system.params.GeneralParams;
import org.self.utils.MiniFile;
import org.self.utils.MiniFormat;
import org.self.utils.MiniUtil;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONObject;

public class megammr extends Command {

	public megammr() {
		super("megammr","(action:) (file:) - Get Info on or Import / Export the MegaMMR data");
	}
	
	@Override
	public String getFullHelp() {
		return "\nmegammr\n"
				+ "\n"
				+ "View information about your MegaMMR. Export and Import complete MegaMMR data.\n"
				+ "\n"
				+ "You must be running -megammr.\n"
				+ "\n"
				+ "action: (optional)\n"
				+ "    info   : Shows info about your MegaMMR.\n"
				+ "    export : Export a MegaMMR data file.\n"
				+ "    import : Import a MegaMMR data file.\n"
				+ "\n"
				+ "file: (optional)\n"
				+ "    Use with export and import.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "megammr\n"
				+ "\n"
				+ "megammr action:export\n"
				+ "\n"
				+ "megammr action:export file:thefile\n"
				+ "\n"
				+ "megammr action:import file:thefile\n"
				;
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"action","file"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception{
		JSONObject ret = getJSONReply();

		String action = getParam("action","info");
		
		MegaMMR megammr = SelfDB.getDB().getMegaMMR();
		
		if(action.equals("info")) {
		
			JSONObject resp = new JSONObject();
			resp.put("enabled", GeneralParams.IS_MEGAMMR);
			resp.put("mmr", megammr.getMMR().toJSON(false));
			resp.put("coins", megammr.getAllCoins().size());
			
			//Put the details in the response..
			ret.put("response", resp);
		
		}else if(action.equals("export")) {
			
			if(!GeneralParams.IS_MEGAMMR) {
				throw new CommandException("MegaMMR not enabled");
			}
			
			//Get the file
			String file = getParam("file","");
			if(file.equals("")) {
				//file = "megammr-backup-"+System.currentTimeMillis()+".bak";
				file = "megammr_"+MiniUtil.DATEFORMAT.format(new Date())+".megammr";
			}
			
			//Create the file
			File backupfile = MiniFile.createBaseFile(file);
			
			//get the MMR and IBD..
			if(backupfile.exists()) {
				backupfile.delete();
			}
			backupfile.createNewFile();
			
			IBD ibd = new IBD();
			
			//Lock the DB for read access..
			SelfDB.getDB().readLock(true);
			
			try {
				
				//Create an IBD
				ibd.createCompleteIBD();
				
				MegaMMRBackup mmrbackup = new MegaMMRBackup(megammr, ibd);
				
				//Now write to it..
				FileOutputStream fos 		= new FileOutputStream(backupfile);
				BufferedOutputStream bos 	= new BufferedOutputStream(fos, 65536);
				DataOutputStream fdos 		= new DataOutputStream(bos);
				
				//And write it..
				mmrbackup.writeDataStream(fdos);
				
				//flush
				fdos.flush();
				bos.flush();
				fos.flush();
				
				fdos.close();
				bos.close();
				fos.close();
				
			}catch(Exception exc) {
				
				//Unlock DB
				SelfDB.getDB().readLock(false);
				
				throw new CommandException(exc.toString());
			}
			
			//Unlock DB
			SelfDB.getDB().readLock(false);
			
			JSONObject resp = new JSONObject();
			resp.put("megammrtip", megammr.getMMR().getBlockTime());
			resp.put("ibdtip", ibd.getTreeTip());
			resp.put("backup", backupfile.getAbsolutePath());
			resp.put("size", MiniFormat.formatSize(backupfile.length()));
			
			//Put the details in the response..
			ret.put("response", resp);
		
		}else if(action.equals("import")) {
			
			if(!GeneralParams.IS_MEGAMMR) {
				throw new CommandException("MegaMMR not enabled");
			}
			
			String file = getParam("file","");
			if(file.equals("")) {
				throw new CommandException("MUST specify a file to restore from");
			}
			
			//Does it exist..
			File restorefile = MiniFile.createBaseFile(file);
			if(!restorefile.exists()) {
				throw new CommandException("Restore file doesn't exist : "+restorefile.getAbsolutePath());
			}
			
			//Load it in..
			MegaMMRBackup mmrback = new MegaMMRBackup();
			
			try {
				SelfLogger.log("Loading MegaMMR.. size:"+MiniFormat.formatSize(restorefile.length()));
				MiniFile.loadObjectSlow(restorefile, mmrback);
			}catch(Exception exc) {
				throw new CommandException(exc.toString());
			}
			
			//Now we have the file.. lets set it..
			SELFSystem.getInstance().archiveResetReady(false);
			
			//Get ready..
			SelfDB.getDB().getMegaMMR().clear();
			
			//Now load the Mega MMR so is the current one..
			SelfDB.getDB().hardSetMegaMMR(mmrback.getMegaMMR());
			
			//Now process the IBD.. Override the restore setting
			SelfLogger.log("Process new IBD");
			SELFSystem.getInstance().getTxPoWProcessor().postProcessIBD(mmrback.getIBD(), "0x00", true);
			
			//Small Pause..
			while(true) {
				Thread.sleep(250);
				
				//Check
				if(SELFSystem.getInstance().getTxPoWProcessor().isIBDProcessFinished()) {
					break;
				}
			}
			
			//Quick clean
			SelfLogger.log("System memory clean..");
			System.gc();
			
			//Get the tree
			TxPowTree tree = SelfDB.getDB().getTxPoWTree();
			TxPoW topblock = tree.getTip().getTxPoW();
			SelfLogger.log("Current Top Block : "+topblock.getBlockNumber());
			
			//Now check..
			TxPoW rootblock = tree.getRoot().getTxPoW();
			SelfLogger.log("Current Tree Root : "+rootblock.getBlockNumber());
			
			//And the Mega MMR
			MegaMMR currentmega = SelfDB.getDB().getMegaMMR();
			SelfLogger.log("Current MegaMMR Tip : "+currentmega.getMMR().getBlockTime());
			
			//Get all your coin proofs..
			SelfLogger.log("Get all your CoinProofs");
			MegaMMRSyncData mydata 		 = megammrsync.getMyDetails();
			ArrayList<CoinProof> cproofs = megammrsync.getAllCoinProofs(mydata);
			
			//Import all YOUR coin proofs..
			SelfLogger.log("Transfer your CoinProofs.. "+cproofs.size());
			for(CoinProof cp : cproofs) {
				
				//Convert to MiniData..
				MiniData cpdata = MiniData.getMiniDataVersion(cp);
				
				//Coin Import..
				JSONObject coinproofresp = CommandRunner.getRunner().runSingleCommand("coinimport track:true data:"+cpdata.to0xString());
				
//				if(!(boolean)coinproofresp.get("status")) {
//					SelfLogger.log("Fail Import : "+coinproofresp.getString("error")+" @ "+cp.toJSON());
//				}
			}
			
			JSONObject resp = new JSONObject();
			resp.put("message", "MegaMMR import finished.. please restart");
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
		
		}else if(action.equals("integrity")) {
			
			String file = getParam("file");
			
			//Does it exist..
			File restorefile = MiniFile.createBaseFile(file);
			if(!restorefile.exists()) {
				throw new CommandException("MegaMMR file doesn't exist : "+restorefile.getAbsolutePath());
			}
			
			//Load it in..
			MegaMMRBackup mmrback = new MegaMMRBackup();
			
			SelfLogger.log("Load MegaMMR.. "+MiniFormat.formatSize(restorefile.length()));
			MiniFile.loadObjectSlow(restorefile, mmrback);
			
			BigInteger weight = checkMegaMMR(mmrback);
			
			IBD ibd 			= mmrback.getIBD();
			TxPoW cascade 		= ibd.getCascade().getTip().getTxPoW();
			MiniNumber casctip 	= cascade.getBlockNumber();
			int casclen 		= ibd.getTxBlocks().size();
			MiniNumber chaintip	= casctip.add(new MiniNumber(casclen));
			
			JSONObject resp = new JSONObject();
			resp.put("cascadetip", casctip);
			resp.put("cascadedate", new Date(cascade.getTimeMilli().getAsLong()).toString());
			resp.put("chaintip", chaintip);
			resp.put("weight", weight.toString());
			ret.put("response", resp);
		}
		
		return ret;
	}
	
	@Override
	public Command getFunction() {
		return new megammr();
	}

	public static BigInteger checkMegaMMR(File zMegaMMR) throws CommandException{
		//Load it in..
		MegaMMRBackup mmrback = new MegaMMRBackup();
		
		SelfLogger.log("Load MegaMMR.. "+MiniFormat.formatSize(zMegaMMR.length()));
		MiniFile.loadObjectSlow(zMegaMMR, mmrback);
	
		return checkMegaMMR(mmrback);
	}
	
	public static BigInteger checkMegaMMR(MegaMMRBackup mmrback) throws CommandException{
		
		//Get the mmr
		MegaMMR mega 	= mmrback.getMegaMMR();
		MMR mmr 		= mmrback.getMegaMMR().getMMR();
		
		//Check the IBD
		IBD ibd = mmrback.getIBD();
		SelfLogger.log("Check IBD..");
		boolean validibd = ibd.checkValidData();
		if(!validibd) {
			throw new CommandException("Invalid IBD");
		}
		
		//Check start and end.. This is where the MEGA MMR finishes..
		MiniNumber lastblock = mmr.getBlockTime();
		
		//Load the IBD into the MMR..
		ArrayList<TxBlock> blocks = mmrback.getIBD().getTxBlocks();
		for(TxBlock block : blocks) {
			
			//Check is the next in line.. 
			MiniNumber blknum = block.getTxPoW().getBlockNumber(); 
			if(!blknum.isEqual(lastblock.increment())) {
				throw new CommandException("Invalid block number.. not incremental.. last_in_mega:"+lastblock+" new_block:"+blknum);
			}
			
			//Store for later
			lastblock = blknum;
			
			//Add to the MegaMMR..
			mega.addBlock(block);
		}
		
		//You can finalize as no more being added
		mmr.finalizeSet();
		
		SelfLogger.log("Now check all coin proofs..");
		
		//Now check integrity
		Hashtable<String,Coin> allcoins = mmrback.getMegaMMR().getAllCoins();
		int size = allcoins.size();
		
		Collection<Coin> coincollection = allcoins.values();
		Iterator<Coin> coiniterator = coincollection.iterator();
		
		int maxcheck = 0;
		while(coiniterator.hasNext()) {
			Coin coin = coiniterator.next();
			
			//Create the MMRData Leaf Node..
			MMRData mmrdata 	= MMRData.CreateMMRDataLeafNode(coin, coin.getAmount());
			MMRProof mmrproof 	= null;
			try {
				
				//Get the proof..
				mmrproof = mmr.getProof(coin.getMMREntryNumber());
			
			}catch(Exception exc) {
				throw new CommandException("Error chcking coin @ "+coin.toJSON()+" "+exc);
			}
			
			//Now check the proof..
			boolean valid = mmr.checkProofTimeValid(coin.getMMREntryNumber(), mmrdata, mmrproof);
			
			if(!valid) {
				throw new CommandException("INVALID Coin proof! @ "+coin.toJSON().toString());
			}
			
			maxcheck++;
			if(maxcheck % 5000 == 0) {
				SelfLogger.log("Checking coins @ "+maxcheck+" / "+size);
			}
		}
		
		SelfLogger.log("All coins checked "+maxcheck+" / "+size);
		
		return ibd.getTotalWeight();
	}
	
	public static void main(String[] zArgs) throws Exception {
		checkMegaMMR(new File("./bin/self_megammr.mmr"));
	}
}
