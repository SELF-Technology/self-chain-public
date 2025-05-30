package org.self.system.commands.backup;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import org.self.database.SelfDB;
import org.self.database.txpowdb.sql.TxPoWList;
import org.self.database.txpowdb.sql.TxPoWSqlDB;
import org.self.objects.TxPoW;
import org.self.objects.base.MiniData;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.system.params.GeneralParams;
import org.self.utils.MiniFile;
import org.self.utils.MiniFormat;
import org.self.utils.SelfLogger;
import org.self.utils.encrypt.GenerateKey;
import org.self.utils.json.JSONObject;

public class backup extends Command {

	public static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("dd_MM_yyyy_HHmmss", Locale.ENGLISH );
	
	public backup() {
		super("backup","(password:) (file:) (auto:) (maxhistory:) - Backup the system. Uses a timestamped name by default");
	}
	
	@Override
	public String getFullHelp() {
		return "\nbackup\n"
				+ "\n"
				+ "Backup your node. Uses a timestamped name by default.\n"
				+ "\n"
				+ "password: (optional)\n"
				+ "    Set a password using letters and numbers only.\n"
				+ "\n"
				+ "file: (optional)\n"
				+ "    Specify a filename ending in .bak, optionally include a local path for the backup.\n"
				+ "    Default location for a backup is the Self data folder.\n"
				+ "\n"
				+ "auto: (optional)\n"
				+ "    true or false, true will schedule a non-password protected backup every 24 hours.\n"
				+ "\n"
				+ "maxhistory: (optional)\n"
				+ "    Max relevant TxPoW to add - your history.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "backup password:Longsecurepassword456\n"
				+ "\n"
				+ "backup password:Longsecurepassword456 confirm:Longsecurepassword456\n"
				+ "\n"
				+ "backup password:Longsecurepassword456 file:my-backup-01-Jan-22.bak\n"
				+ "\n"
				+ "backup auto:true\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"debug","password","file","auto","confirm","maxhistory"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		//Check all keys are created..
		vault.checkAllKeysCreated();
		
		//Is this an AUTO backup initiate..
		if(existsParam("auto")) {
			boolean setauto = getBooleanParam("auto");
			if(setauto) {
				//Start an auto backup feature
				SelfDB.getDB().getUserDB().setAutoBackup(true);
				
			}else {
				//Stop the auto feature
				SelfDB.getDB().getUserDB().setAutoBackup(false);
			}
			
			if(!setauto) {
				JSONObject resp = new JSONObject();
				resp.put("autobackup", setauto);
				ret.put("backup", resp);
				
				return ret;
			}
		}
		
		//Get the file
		String file = getParam("file","");
		if(file.equals("")) {
			file = "self-backup-"+System.currentTimeMillis()+".bak";
			//file = "self_backup_"+DATEFORMAT.format(new Date())+".bak";
		}

		//Get a password if there is one..
		String password = getParam("password","self"); 
		if(password.equals("")) {
			throw new CommandException("Cannot have a blank password");
		}
		
		//Is there a confirm
		if(existsParam("confirm")) {
			String confirm = getParam("confirm");
			if(!password.equals(confirm)) {
				throw new CommandException("Passwords do NOT match!");
			}
		}
		
		boolean complete = getBooleanParam("complete", false);

		boolean debug = getBooleanParam("debug", false);
		
		//Create the file
		File backupfile = MiniFile.createBaseFile(file);
		
		if(debug) {
			SelfLogger.log("Backup file : "+backupfile.getAbsolutePath());
		}
		
		//Wipe if exists..
		if(backupfile.exists()) {
			backupfile.delete();
		}
		
		///Base folder
		File backupfolder = new File(GeneralParams.DATA_FOLDER,"backup");
		backupfolder.mkdirs();
		
		if(debug) {
			SelfLogger.log("Backup folder : "+backupfolder.getAbsolutePath());
		}
		
		//Lock the DB
		SelfDB.getDB().readLock(true);
		
		try {
		
			//Save the current state..
			SelfDB.getDB().saveState();
			
			//Write the SQL Dbs
			File walletfile = new File(backupfolder,"wallet.sql");
			SelfDB.getDB().getWallet().backupToFile(walletfile);
			MiniData walletata 	= new MiniData(MiniFile.readCompleteFile(walletfile));
			
			File cascade = new File(backupfolder,"cascade.bak");
			SelfDB.getDB().getCascade().saveDB(cascade);
			MiniData cascadedata = new MiniData(MiniFile.readCompleteFile(cascade));
			
			File chain = new File(backupfolder,"chaintree.bak");
			SelfDB.getDB().getTxPoWTree().saveDB(chain);
			MiniData chaindata = new MiniData(MiniFile.readCompleteFile(chain));
			
			File userdb = new File(backupfolder,"userdb.bak");
			SelfDB.getDB().getUserDB().saveDB(userdb);
			MiniData userdata = new MiniData(MiniFile.readCompleteFile(userdb));
			
			File p2pdb = new File(backupfolder,"p2p.bak");
			SelfDB.getDB().getP2PDB().saveDB(p2pdb);
			MiniData p2pdata = new MiniData(MiniFile.readCompleteFile(p2pdb));
			
			//Store the relevant TxPoWs..
			int max = getNumberParam("maxhistory",TxPoWSqlDB.MAX_RELEVANT_TXPOW).getAsInt();
			ArrayList<TxPoW> txps 	= SelfDB.getDB().getTxPoWDB().getSQLDB().getAllRelevant(max);
			TxPoWList txplist 	 	= new TxPoWList(txps);
			MiniData txplistdata 	= MiniData.getMiniDataVersion(txplist);
			
			//Now create the streams to save these
			FileOutputStream fos 	= new FileOutputStream(backupfile);
			DataOutputStream dos 	= new DataOutputStream(fos);
			
			//Now create a CipherStream.. first need an IVParam
			MiniData ivparam = new MiniData(GenerateKey.IvParam());
			
			//The SALT - for the password
	    	byte[] bsalt 	= new byte[8];
	    	new SecureRandom().nextBytes(bsalt);
	    	MiniData salt = new MiniData(bsalt);
	    	
			//Now write these 2 bits of info to the stream..
			salt.writeDataStream(dos);
			ivparam.writeDataStream(dos);
			
			//Create an AES SecretKey with Password and Salt
			byte[] secret = GenerateKey.secretKey(password,bsalt).getEncoded();
			
			//Create the cipher..
			Cipher ciph = GenerateKey.getCipherSYM(Cipher.ENCRYPT_MODE, ivparam.getBytes(), secret);
			CipherOutputStream cos 		= new CipherOutputStream(dos, ciph);
			GZIPOutputStream gzos		= new GZIPOutputStream(cos);
			DataOutputStream ciphdos 	= new DataOutputStream(gzos);
			
			//And now put ALL of those files into a single file..
			walletata.writeDataStream(ciphdos);
			cascadedata.writeDataStream(ciphdos);
			chaindata.writeDataStream(ciphdos);
			userdata.writeDataStream(ciphdos);
			p2pdata.writeDataStream(ciphdos);
			txplistdata.writeDataStream(ciphdos);
			
			//All done..
			ciphdos.close();
			cos.close();
			dos.close();
			gzos.close();
			fos.close();
			
			//The total uncompressed size..
			long total = 	walletfile.length()+
							cascade.length()+
							chain.length()+
							userdb.length()+
							p2pdb.length()+
							txplistdata.getLength();
			
			//Get all the individual File sizes..
			JSONObject files = new JSONObject();
			files.put("wallet", MiniFormat.formatSize(walletfile.length()));
			
			files.put("cascade", MiniFormat.formatSize(cascade.length()));
			files.put("chain", MiniFormat.formatSize(chain.length()));
			files.put("user", MiniFormat.formatSize(userdb.length()));
			files.put("p2p", MiniFormat.formatSize(p2pdb.length()));
			files.put("txpow", MiniFormat.formatSize(txplistdata.getLength()));
			
			//And send data
			JSONObject resp = new JSONObject();
			if(SelfDB.getDB().getTxPoWTree().getTip() != null) {
				resp.put("block", SelfDB.getDB().getTxPoWTree().getTip().getTxPoW().getBlockNumber());
			}else {
				resp.put("block", "-1");
			}
			resp.put("files", files);
			resp.put("uncompressed", MiniFormat.formatSize(total));
			resp.put("file", backupfile.getAbsolutePath());
			resp.put("size", MiniFormat.formatSize(backupfile.length()));
			resp.put("auto", SelfDB.getDB().getUserDB().isAutoBackup());
			ret.put("backup", resp);
			
			//And now clean up..
			MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, backupfolder);
			
		}catch(Exception exc) {
			
			//Unlock DB..
			SelfDB.getDB().readLock(false);
			
			//Delete backup folder
			MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, backupfolder);
			
			//Throw an error to notify user..
			throw new CommandException(exc.toString());
		}
		
		//Unlock..
		SelfDB.getDB().readLock(false);
				
		//Delete backup folder
		MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, backupfolder);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new backup();
	}

}
