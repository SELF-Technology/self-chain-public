package org.self.system.commands.backup;

import java.io.File;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.wallet.SeedRow;
import org.self.database.wallet.Wallet;
import org.self.objects.Address;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.objects.base.MiniString;
import org.self.objects.keys.TreeKey;
import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.system.params.GeneralParams;
import org.self.utils.BIP39;
import org.self.utils.Crypto;
import org.self.utils.MiniFile;
import org.self.utils.SelfLogger;
import org.self.utils.encrypt.PasswordCrypto;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;
import org.self.utils.ssl.SSLManager;

public class vault extends Command {

	public vault() {
		super("vault","[action:seed|wipekeys|restorekeys|passwordlock|passwordunlock] (seed:) (phrase:) - BE CAREFUL. Wipe/Restore/Encrypt/Decrypt your private keys");
	}
	
	@Override
	public String getFullHelp() {
		return "\nvault\n"
				+ "\n"
				+ "BE CAREFUL. Wipe / Restore your private keys.\n"
				+ "\n"
				+ "View your passphrase and seed. DO NOT SHARE THESE WITH ANYONE.\n"
				+ "\n"
				+ "ENSURE YOU HAVE A BACKUP AND SECURE RECORD OF YOUR PASSPHRASE BEFORE LOCKING.\n"
				+ "\n"
				+ "You must have your passphrase to unlock your private keys.\n"
				+ "\n"
				+ "action: (optional)\n"
				+ "    seed : Show your seed phrase. The default. \n"
				+ "    wipekeys : Wipe your private keys - keep the public.\n"
				+ "    restorekeys : Restore your private keys.\n"
				+ "    passwordlock : Lock your node by password encrypting private keys.\n"
				+ "    passwordunlock : Unlock your node to reinstating your private keys.\n"
				+ "\n"
				+ "seed: (optional)\n"
				+ "    Enter your seed to lock your node.\n"
				+ "    This will delete your private keys.\n"
				+ "\n"
				+ "phrase: (optional)\n"
				+ "    Enter your passphrase in double quotes to restore your node.\n"
				+ "    This will reinstate your private keys.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "vault\n"
				+ "\n"
				+ "vault action:wipekeys seed:0xDD4E..\n"
				+ "\n"
				+ "vault action:restorekeys phrase:\"SPRAY LAMP..\"\n"
				+ "\n"
				+ "vault action:passwordlock password:your_password\n"
				+ "\n"
				+ "vault action:passwordlock password:your_password confirm:your_password\n"
				+ "\n"
				+ "vault action:passwordunlock password:your_password\n";
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"action","seed","keyuses","phrase","password","confirm","numkeys"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		String action = getParam("action", "seed");
			
		//Display the base seed..
		SeedRow base = SelfDB.getDB().getWallet().getBaseSeed();
		
		if(action.equals("seed")) {
			
			JSONObject json = new JSONObject();
			json.put("phrase", base.getPhrase());
			json.put("seed", base.getSeed());
			json.put("locked", !SelfDB.getDB().getWallet().isBaseSeedAvailable());
			
			ret.put("response", json);
		
		}else if(action.equals("wipekeys")) {
			
			checkAllKeysCreated();
			
			//Are we already locked..
			if(!SelfDB.getDB().getWallet().isBaseSeedAvailable()) {
				throw new CommandException("DB already locked!");
			}
			
			//Get the seed.. TO SHOW THEY KNOW IT..
			MiniData seed = getDataParam("seed");
			if(!seed.to0xString().equals(base.getSeed())) {
				throw new CommandException("Incorrect seed for lock");
			}
			
			//Wipe the private keys (but keep the public keys, modifiers etc)
			SelfDB.getDB().getWallet().wipeBaseSeedRow();
			
			ret.put("response", "All private keys wiped!");
		
		}else if(action.equals("restorekeys")) {
			
			//Check for one or the other..
			String initphrase = getParam("phrase");
			
			//Clean it up..
			String cleanphrase = BIP39.cleanSeedPhrase(initphrase);
			
			//Convert to a data hash
			MiniData seed = BIP39.convertStringToSeed(cleanphrase);
			
			//And now recreate the private keys
			boolean ok = SelfDB.getDB().getWallet().resetBaseSeedPrivKeys(cleanphrase, seed.to0xString());

			if(!ok) {
				throw new CommandException("Error updating Private keys.. please try again");
			}

			JSONObject resp=new JSONObject();
			resp.put("entered", initphrase);
			resp.put("cleaned", cleanphrase);
			resp.put("same", cleanphrase.equalsIgnoreCase(initphrase));
			resp.put("result", "All private keys restored!");
			
			ret.put("response", resp);
		
		}else if(action.equals("passwordlock")) {
			
			checkAllKeysCreated();
			
			//Get the password
			String password = getParam("password");
			if(password.contains(";")) {
				throw new CommandException("Cannot use ; in password");
			}
			
			//Is there a confirm
			if(existsParam("confirm")) {
				String confirm = getParam("confirm");
				if(!password.equals(confirm)) {
					throw new CommandException("Passwords do NOT match!");
				}
			}
			
			passwordLockDB(password);
			
			ret.put("response", "All private keys wiped! Stored encrypted in UserDB");
		
		}else if(action.equals("passwordunlock")) {
			
			//Get the password
			String password = getParam("password");
			
			passowrdUnlockDB(password);
						
			ret.put("response", "All private keys restored!");
			
		}else if(action.equals("testphrase")) {
			
			//Check for one or the other..
			String initphrase = getParam("phrase");
			
			//Clean it up..
			String cleanphrase = BIP39.cleanSeedPhrase(initphrase);
			
			//Convert to a data hash
			MiniData seed = BIP39.convertStringToSeed(cleanphrase);
			
			//Create some keys
			int numkeys = getNumberParam("numkeys",new MiniNumber(4)).getAsInt();
			JSONArray arr = new JSONArray();
			for(int i=0;i<numkeys;i++) {
				
				SelfLogger.log("Creating key : "+i);
				
				//Get the modifier
				MiniData modifier 	= new MiniData(new BigInteger(Integer.toString(i)));

				//Now create a random private seed using the modifier
				MiniData privseed 	= Crypto.getInstance().hashObjects(seed, modifier);
				
				//Make the TreeKey
				TreeKey treekey 	= TreeKey.createDefault(privseed);
				
				//Now create a simple address..
				String script = new String("RETURN SIGNEDBY("+treekey.getPublicKey()+")");
				
				//Get the address
				Address addr = new Address(script);
				
				//Create a JSON
				JSONObject json = new JSONObject();
				json.put("publickey", treekey.getPublicKey().to0xString());
				json.put("address", addr.getSelfAddress());
				
				arr.add(json);
			}
			
			JSONObject json = new JSONObject();
			json.put("phrase", cleanphrase);
			json.put("seed", seed.to0xString());
			json.put("address", arr);
			
			ret.put("response", json);
			
		}else if(action.equals("resetkeys")) {
			
			//Get the seed phrase
			String phrase = getParam("phrase");
			
			//Set the key uses to this..
			int keyuses = getNumberParam("keyuses", new MiniNumber(100)).getAsInt();
			
			//Clean it up..
			String cleanphrase = BIP39.cleanSeedPhrase(phrase);
			
			//First stop everything.. and get ready to restore the files..
			SELFSystem.getInstance().restoreReady();
			
			//Now delete the SQL DBs..
			SelfDB.getDB().getTxPoWDB().getSQLDB().wipeDB();
			SelfDB.getDB().getTxPoWDB().getSQLDB().saveDB(false);
			
			//Wipe ArchiveDB	
			SelfDB.getDB().getArchive().saveDB(false);
			SelfDB.getDB().getArchive().getSQLFile().delete();
			
			File basedb = SelfDB.getDB().getBaseDBFolder();
			MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, new File(basedb,"cascade.db"));
			MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, new File(basedb,"chaintree.db"));
			MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, new File(basedb,"userprefs.db"));
			MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, new File(basedb,"p2p.db"));
			
			//And will need to recreate the SSL
			MiniFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, SSLManager.getSSLFolder());
			
			//Reset all the private and public keys
			Wallet wallet = SelfDB.getDB().getWallet();
			
			//Wipe the whole DB..
			wallet.resetDB(cleanphrase);
			
			//Now create all the keys..
			wallet.initDefaultKeys(Wallet.NUMBER_GETADDRESS_KEYS, true);
			
			//Now Update the USES - since they may have been used before - we don;t know.. 
			wallet.updateAllKeyUses(keyuses);
			
			//Now save the Databases..
			SelfDB.getDB().saveSQL(false);
			
			//Don't do the usual shutdown hook
			SELFSystem.getInstance().setHasShutDown();
			
			//And NOW shut down..
			SELFSystem.getInstance().stopMessageProcessor();
			
			ret.put("response", "All private keys restored!");
			
		}else {
			throw new CommandException("Invalid action : "+action);
		}
		
		return ret;
	}

	/**
	 * Check that all keys are created
	 */
	public static void checkAllKeysCreated() throws CommandException {
		
		//Are they all created..
		if(SELFSystem.getInstance().getAllKeysCreated()) {
			return;
		}
		
		throw new CommandException("Please wait for ALL your keys to be created. "
				+ "This can take 5 mins. "
				+ "Currently ("+SELFSystem.getInstance().getAllDefaultKeysSize()+"/"+Wallet.NUMBER_GETADDRESS_KEYS+")");
	}
	
	/**
	 * Stop All keys created
	 */
	public static void stopAllKeysCreated() throws CommandException {
		
		//Are they all created..
		if(SELFSystem.getInstance().getAllKeysCreated()) {
			return;
		}
		
		//Stop all new keys 
		SelfDB.getDB().getWallet().setStopNewKeys(true);
		
		//Small Pause..
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {}
	}
	
	public static void passwordLockDB(String zPassword) throws CommandException {
		
		//Display the base seed..
		SeedRow base = SelfDB.getDB().getWallet().getBaseSeed();
				
		//Are we already locked..
		if(!SelfDB.getDB().getWallet().isBaseSeedAvailable()) {
			throw new CommandException("DB already locked!");
		}
		
		//Get the Seed phrase
		MiniString phrase = new MiniString(base.getPhrase());
		
		//Convert to data
		MiniData data = new MiniData(phrase.getData());
		
		//Encrypt it
		MiniData encrypted;
		try {
			encrypted = PasswordCrypto.encryptPassword(zPassword, data);
		} catch (Exception e) {
			throw new CommandException(e.toString());
		} 
		
		//Store it in the UserDB
		SelfDB.getDB().getUserDB().setEncryptedSeed(encrypted);
		
		//Save UserDB
		SelfDB.getDB().saveUserDB();
		
		//Wipe the private keys (but keep the public keys, modifiers etc)
		try {
			SelfDB.getDB().getWallet().wipeBaseSeedRow();
		} catch (SQLException e) {
			throw new CommandException(e.toString());
		}
	}
	
	public static void passowrdUnlockDB(String zPassword) throws CommandException {
		
		//Are we already locked..
		if(SelfDB.getDB().getWallet().isBaseSeedAvailable()) {
			throw new CommandException("DB already unlocked!");
		}
		
		//Get the encrypted data
		MiniData encrypted = SelfDB.getDB().getUserDB().getEncryptedSeed();
		
		//Decrypt it..
		MiniData decrypt = null;
		try {
			decrypt = PasswordCrypto.decryptPassword(zPassword, encrypted);
		}catch(Exception exc) {
			throw new CommandException("Incorrect password!");
		}
		
		//Create the string
		String initphrase = new MiniString(decrypt.getBytes()).toString();
		
		//Convert to a data hash
		MiniData seed = BIP39.convertStringToSeed(initphrase);
		
		//And now recreate the private keys
		boolean ok = SelfDB.getDB().getWallet().resetBaseSeedPrivKeys(initphrase, seed.to0xString());

		if(!ok) {
			throw new CommandException("Error updating Private keys.. please try again");
		}

		SelfDB.getDB().getUserDB().setEncryptedSeed(MiniData.ZERO_TXPOWID);
		
		//Save UserDB
		SelfDB.getDB().saveUserDB();
	}
	
	@Override
	public Command getFunction() {
		return new vault();
	}
}
