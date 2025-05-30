package org.self.system.commands.backup;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import org.self.database.SelfDB;
import org.self.database.txpowdb.sql.TxPoWList;
import org.self.database.txpowdb.sql.TxPoWSqlDB;
import org.self.objects.TxPoW;
import org.self.objects.base.MiniData;
import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.system.params.GeneralParams;
import org.self.utils.MiniFile;
import org.self.utils.SelfLogger;
import org.self.utils.encrypt.GenerateKey;
import org.self.utils.json.JSONObject;
import org.self.utils.ssl.SSLManager;

public class restore extends Command {

	public restore() {
		super("restore","[file:] (password:) - Restore the entire system.");
	}
	
	@Override
	public String getFullHelp() {
		return "\nrestore\n"
				+ "\n"
				+ "Restore your node from a backup. You MUST wait until all your original keys are created before this is allowed.\n"
				+ "\n"
				+ "file:\n"
				+ "    Specify the filename or local path of the backup to restore\n"
				+ "\n"
				+ "password: (optional)\n"
				+ "    Enter the password of the backup \n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "restore file:my-full-backup-01-Jan-22 password:Longsecurepassword456\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"file","password","shutdown"}));
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
		
		//Are we shutting down - could be a reset
		boolean doshutdown = getBooleanParam("shutdown", true);
		
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
		SELFSystem.getInstance().restoreReady(doshutdown);
		
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
			
			//Load these values 
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
	
		//Increment Key Uses
		SelfDB.getDB().getWallet().updateIncrementAllKeyUses(256);
		
		//Close
		SelfDB.getDB().getTxPoWDB().getSQLDB().saveDB(false);
		
		//Wipe ArchiveDB	
		SelfDB.getDB().getArchive().saveDB(false);
		SelfDB.getDB().getArchive().getSQLFile().delete();
	
		//And now clean up..
		MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, restorefolder);
		
		//And will need to recreate the SSL
		MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, SSLManager.getSSLFolder());
		
		//And send data
		JSONObject resp = new JSONObject();
		resp.put("file", restorefile.getAbsolutePath());
		ret.put("restore", resp);
		ret.put("message", "Restart Self for restore to take effect!");
		
		//Now save the Databases..
		//SelfDB.getDB().saveSQL(false);
		
		//Now shutdown and save everything
		SelfDB.getDB().saveAllDB();
				
		//Normally yes
		if(doshutdown) {
			
			//Don't do the usual shutdown hook
			SELFSystem.getInstance().setHasShutDown();
			
			//And NOW shut down..
			SELFSystem.getInstance().stopMessageProcessor();
			
			//Tell listener..
			SELFSystem.getInstance().NotifySELFListenerOfShutDown();
		}
		
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
		return new restore();
	}

}
