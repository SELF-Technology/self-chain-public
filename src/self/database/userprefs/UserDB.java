package org.self.database.userprefs;

import java.util.ArrayList;

import org.self.objects.Magic;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.params.GeneralParams;
import org.self.system.params.GlobalParams;
import org.self.utils.JsonDB;
import org.self.utils.MiniUtil;
import org.self.utils.json.JSONArray;

public class UserDB extends JsonDB{

	public UserDB() {
		super();
	}
	
	/**
	 * Set your Welcome message
	 */
	public void setWelcome(String zWelcome) {
		setString("welcome", zWelcome);
	}
	
	public String getWelcome() {
		return getString("welcome", "Running Self "+GlobalParams.SELF_VERSION);
	}
	
	/**
	 * The Incentive Cash User
	 */
	public String getIncentiveCashUserID() {
		return getString("uid", "");
	}
	
	public void setIncentiveCashUserID(String zUID) {
		setString("uid", zUID);
	}
	
	/**
	 * Web Hooks
	 */
	public void setWebHooks(ArrayList<String> zWebHooks) {
		
		//Create one string..
		JSONArray arr = new JSONArray();
		for(String hook : zWebHooks) {
			arr.add(hook);
		}
		
		//Add this..
		setJSONArray("webhooks", arr);
	}
	
	public ArrayList<String> getWebHooks() {
		ArrayList<String> ret = new ArrayList<>();
		
		JSONArray arr = getJSONArray("webhooks");
		for(Object hook : arr) {
			String hk = (String)hook;
			
			ret.add(hk);
		}
		
		return ret;
	}
	
	
	/**
	 * Load Custom Transactions
	 */
	public MiniData loadCustomTransactions() {
		return getData("custom_transactions", MiniData.ZERO_TXPOWID);
	}
	
	public void saveCustomTransactions(MiniData zCompleteDB) {
		setData("custom_transactions", zCompleteDB);
	}
	
	/**
	 * Get and Set the user hashrate
	 */
	public void setHashRate(MiniNumber zHashesPerSec) {
		setNumber("hashrate", zHashesPerSec);
	}
	
	public MiniNumber getHashRate() {
		return getNumber("hashrate", MiniNumber.MILLION);
	}
	
	/**
	 * Get set the User Maxima Details..
	 */
	public void setMaximaName(String zName) {
		setString("maximaname", zName);
	}
	
	public String getMaximaName() {
		return getString("maximaname", "noname");
	}
	
	public void setMaximaIcon(String zIcon) {
		setString("maximaicon", zIcon);
	}
	
	public String getMaximaIcon() {
		return getString("maximaicon", "0x00");
	}
	
	/**
	 * Install MDS DAPPs the first time
	 */
	public boolean getMDSINIT() {
		return getBoolean("mdsinitdapps", false);
	}
	
	public void setMDSINIT(boolean zInit) {
		setBoolean("mdsinitdapps", zInit);
	}
	
	/**
	 * Is AUTO backup enabled..
	 */
	public boolean isAutoBackup() {
		return getBoolean("autobackup", false);
	}
	
	public void setAutoBackup(boolean zAuto) {
		setBoolean("autobackup", zAuto);
	}
	
	/**
	 * Desired Magic Numbers
	 */
	public MiniNumber getMagicDesiredSELFScript() {
		return getNumber("magic_selfscript", Magic.DEFAULT_SELFScript_OPERATIONS);
	}
	
	public void setMagicDesiredSELFScript(MiniNumber zSELFScript) {
		setNumber("magic_selfscript", zSELFScript);
	}
	
	public MiniNumber getMagicMaxTxPoWSize() {
		return getNumber("magic_txpowsize", Magic.DEFAULT_TXPOW_SIZE);
	}
	
	public void setMagicMaxTxPoWSize(MiniNumber zMaxSize) {
		setNumber("magic_txpowsize", zMaxSize);
	}
	
	public MiniNumber getMagicMaxTxns() {
		return getNumber("magic_txns", Magic.DEFAULT_TXPOW_TXNS);
	}
	
	public void setMagicMaxTxns(MiniNumber zMaxTxns) {
		setNumber("magic_txns", zMaxTxns);
	}
	
	/**
	 * Encrypted version of Seed phrase
	 */
	public void setEncryptedSeed(MiniData zEncryptedSeed) {
		setData("encrypted_seed", zEncryptedSeed);
	}
	
	public MiniData getEncryptedSeed() {
		return getData("encrypted_seed", MiniData.ZERO_TXPOWID);
	}
	
	/**
	 * MAXIMA - settings
	 */
	public boolean getMaximaAllowContacts() {
		return getBoolean("maxima_allowallcontacts", true);
	}
	
	public void setMaximaAllowContacts(boolean zAllowContacts) {
		setBoolean("maxima_allowallcontacts", zAllowContacts);
	}
	
	public ArrayList<String> getMaximaPermanent() {
		return MiniUtil.convertJSONArray(getJSONArray("maxima_permanent")) ;
	}
	
	public void setMaximaPermanent(ArrayList<String> zPermanentList) {
		setJSONArray("maxima_permanent",MiniUtil.convertArrayList(zPermanentList));
	}
	
	/**
	 * Are we auto backing up the MySQLDB
	 */
	public boolean getAutoLoginDetailsMySQL() {
		return getBoolean("mysql_autologindetails", false);
	}
	
	public void setAutoLoginDetailsMySQL(boolean zLoginDetails) {
		setBoolean("mysql_autologindetails", zLoginDetails);
	}
	
	public boolean getAutoBackupMySQL() {
		return getBoolean("mysql_autobackup", false);
	}
	
	public void setAutoBackupMySQL(boolean zAuto) {
		setBoolean("mysql_autobackup", zAuto);
	}
	
	public boolean getAutoBackupMySQLCoins() {
		return getBoolean("mysqlcoins_autobackup", false);
	}
	
	public void setAutoBackupMySQLCoins(boolean zAuto) {
		setBoolean("mysqlcoins_autobackup", zAuto);
	}
	
	public void setAutoMySQLHost(String zHost) {
		setString("mysql_host", zHost);
	}
	
	public String getAutoMySQLHost() {
		return getString("mysql_host","");
	}
	
	public void setAutoMySQLDB(String zDB) {
		setString("mysql_db", zDB);
	}
	
	public String getAutoMySQLDB() {
		return getString("mysql_db","");
	}
	
	public void setAutoMySQLUser(String zUser) {
		setString("mysql_user", zUser);
	}
	
	public String getAutoMySQLUser() {
		return getString("mysql_user","");
	}
	
	public void setAutoMySQLPassword(String zPassword) {
		setString("mysql_password", zPassword);
	}
	
	public String getAutoMySQLPassword() {
		return getString("mysql_password","");
	}
	
	/**
	 * Slave Node properties..
	 */
	public boolean isSlaveNode() {
		return getBoolean("slavenode_enabled", false);
	}
	
	public String getSlaveNodeHost() {
		return getString("slavenode_host", "");
	}
	
	public void setSlaveNode(boolean zEnabled, String zHost) {
		setBoolean("slavenode_enabled", zEnabled);
		setString("slavenode_host", zHost);
	}
	
	/**
	 * Default MiniHUB
	 */
	public String getDefaultMiniHUB() {
		return getString("minihub_default", "0x00");
	}
	
	public void setDefaultMiniHUB(String zMiniDAPPID) {
		setString("minihub_default", zMiniDAPPID);
	}
	
	/**
	 * If you delete a MIniDAPP do NOT reinstall it..
	 */
	public void clearUninstalledMiniDAPP() {
		setJSONArray("minidapps_uninstalled", new JSONArray());
	}
	
	public JSONArray getUninstalledMiniDAPP() {
		return getJSONArray("minidapps_uninstalled");
	}
	
	public void removeUninstalledMiniDAPP(String zName) {
		JSONArray alluninst = getUninstalledMiniDAPP();
		String name = zName.toLowerCase().replaceAll(" ", "");
		alluninst.remove(name);
		setJSONArray("minidapps_uninstalled", alluninst);
	}
	
	public void addUninstalledMiniDAPP(String zName) {
		JSONArray alluninst = getUninstalledMiniDAPP();
		String name = zName.toLowerCase().replaceAll(" ", "");
		alluninst.remove(name);
		alluninst.add(name);
		setJSONArray("minidapps_uninstalled", alluninst);
	}
	
	public boolean checkUninstalledMiniDAPP(String zName) {
		JSONArray alluninst = getUninstalledMiniDAPP();
		String name = zName.toLowerCase().replaceAll(" ", "");
		return alluninst.contains(name);
	}
	
	/**
	 * Are we running the public site
	 */
	public void setPublicMDS(boolean zEnable) {
		setBoolean("mds_publicsite", zEnable);
	}
	
	public boolean  getPublicMDS() {
		//Check the CLI param as well..
		if(GeneralParams.PUBLICMDS_ENABLE) {
			return true;
		}
		
		//Otherwise just check the normal user setting
		return getBoolean("mds_publicsite", false);
	}
	
	/**
	 * RPC Users
	 */
	public JSONArray getRPCUsers() {
		return getJSONArray("rpcusers_allusers");
	}
	
	public void setRPCUsers(JSONArray zNewUsers) {
		setJSONArray("rpcusers_allusers", zNewUsers);
	}
}
