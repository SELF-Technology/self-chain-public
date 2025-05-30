package org.self.system;

import java.io.File;
import java.util.ArrayList;

import org.self.database.SelfDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.database.userprefs.UserDB;
import org.self.database.wallet.ScriptRow;
import org.self.objects.Pulse;
import org.self.objects.TxBlock;
import org.self.objects.TxPoW;
import org.self.objects.self.SELFData;
import org.self.objects.self.SELFNumber;
import org.self.system.brains.AIValidator;
import org.self.system.brains.AIProcessor;
import org.self.system.commands.CommandRunner;
import org.self.system.commands.backup.mysql;
import org.self.system.genesis.GenesisMMR;
import org.self.system.genesis.GenesisTxPoW;
import org.self.system.mds.MDSManager;
import org.self.system.network.NetworkManager;
import org.self.system.network.maxima.MaximaManager;
import org.self.system.network.self.NIOManager;
import org.self.system.network.self.NIOMessage;
import org.self.system.network.p2p.P2PFunctions;
import org.self.system.network.webhooks.NotifyManager;
import org.self.system.params.GeneralParams;
import org.self.system.params.GlobalParams;
import org.self.system.sendpoll.SendPollManager;
import org.self.utils.SELFFile;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageListener;
import org.self.utils.messages.MessageProcessor;
import org.self.utils.messages.TimerMessage;
import org.self.utils.messages.TimerProcessor;
import org.self.utils.mysql.MySQLConnect;
import org.self.utils.ssl.SSLManager;
import org.self.database.poai.ValidatorAIAnalyzer;

public class SELFSystem extends MessageProcessor {
    private ValidatorAIAnalyzer validatorAnalyzer;
    
    public void setValidatorAnalyzer(ValidatorAIAnalyzer analyzer) {
        this.validatorAnalyzer = analyzer;
    }
    
    public ValidatorAIAnalyzer getValidatorAnalyzer() {
        return validatorAnalyzer;
    }

	public static boolean SELF_STARTUP_DEBUG_LOGS = false;
	
	/**
	 * ERROR on Startup
	 */
	public boolean 	STARTUP_ERROR 		= false; 
	public String 	STARTUP_ERROR_MSG 	= "";
	
	/**
	 * Uptime for the node
	 */
	long mUptimeMilli = System.currentTimeMillis();
	
	/**
	 * Static link to the MAIN class
	 */
	private static SELFSystem mSELFInstance = null;
	public static SELFSystem getInstance() {
		return mSELFInstance;
	}
	
	/**
	 * Is there someone listening to Self messages (Android)
	 */
	private static MessageListener SELF_LISTENER = null;
	public static MessageListener getSelfListener() {
		return SELF_LISTENER;
	}
	public static void setSelfListener(MessageListener zListener) {
		SELF_LISTENER = zListener;
	}
	
	/**
	 * Have we told listener to shutdown..
	 */
	private boolean mShutDownSentToListener = false;
	
	/**
	 * Main loop messages
	 */
	public static final String SELF_VALIDATION_COMPLETED	= "MAIN_VALIDATION_COMPLETED";
	public static final String SELF_PULSE 		= "MAIN_PULSE";

	/**
	 * Clean DB - RamDB every 30 mins.. the TxPoW and Archive every 12 hours
	 */
	public static final String SELF_CLEANDB_RAM 	= "MAIN_CLEANDB_RAM";
	long SELF_CLEANDB_RAM_TIMER	= 1000 * 60 * 30;
	
	public static final String SELF_CLEANDB_SQL 	= "MAIN_CLEANDB_SQL";
	long SELF_CLEANDB_SQL_TIMER	= 1000 * 60 * 60 * 12;
	
	public static final String SELF_SYSTEMCLEAN 	= "MAIN_SYSTEMCLEAN";
	long SELF_SYSTEMCLEAN_TIMER	= 1000 * 60 * 5;
	
	public static final String SELF_AUTOBACKUP_MYSQL 	= "MAIN_AUTOBACKUP_MYSQL";
	long SELF_AUTOBACKUP_MYSQL_TIMER					= 1000 * 60 * 60 * 2;
	
	public boolean MYSQL_IMPORTING_NO_ACTION  			= false;
	
	public static final String SELF_AUTOBACKUP_TXPOW 	= "MAIN_AUTOBACKUP_TXPOW";
	
	public static final String SELF_DO_RESCUE 			= "MAIN_DO_RESCUE";
	
	/**
	 * Auto backup every 24 hrs..
	 */
	public static final String MAIN_AUTOBACKUP 	= "MAIN_AUTOBACKUP";
	long SELF_AUTOBACKUP_TIMER = 1000 * 60 * 60 * 24;
	
	/**
	 * Aync Shutdown call
	 */
	public static final String SELF_SHUTDOWN 		= "SELF_SHUTDOWN";
	
	/**
	 * Network Restart
	 */
	public static final String SELF_NETRESTART 	= "SELF_NETRESTART";
	public static final String SELF_NETRESET 	= "SELF_NETRESET";
	long SELF_NETRESET_TIMER = 1000 * 60 * 60 * 24;
	
	
	/**
	 * Debug Function
	 */
	public static final String SELF_CHECKER 	= "SELF_CHECKER";
	SELFData mOldTip 							= SELFData.ZERO_TXPOWID;
	
	/**
	 * Create all the initial Keys
	 */
	public static final String SELF_INIT_KEYS 	= "MAIN_INIT_KEYS";
	long SELF_INIT_KEYS_TIMER = 1000 * 10;
	
	/**
	 * Main loop to check various values every 180 seconds..
	 */
	long SELF_CHECKER_TIMER							= 1000 * 180;
	
	/**
	 * USe to ttest if MAIN thread running correctly..
	 */
	public static final String SELF_CALLCHECKER 	= "MAIN_CALLCHECKER";
	
	/**
	 * Used to check the P2P and MDS systems.. every 20 minutes
	 */
	public static final String SELF_P2PNETMDS_CHECKER 	= "MAIN_P2PNETMDS_CHECKER";
	long SELF_P2PNETMDS_TIMER								= 1000 * 60 * 30;
	
	/**
	 * Notify Users..
	 */
	public static final String SELF_NEWBLOCK 	= "MAIN_NEWBLOCK";
	public static final String SELF_BALANCE 	= "MAIN_BALANCE";
	public static final String SELF_MINING 		= "MAIN_MINING";
	
	public static final String SELF_NEWCOIN 			= "NEWCOIN";
	public static final String SELF_NOTIFYCOIN 			= "NOTIFYCOIN";
	
	//Notify when data hits the cascade
	public static final String SELF_NOTIFYCASCADEBLOCK 	= "NOTIFYCASCADEBLOCK";
	public static final String SELF_NOTIFYCASCADETXN 	= "NOTIFYCASCADETXN";
	public static final String SELF_NOTIFYCASCADECOIN 	= "NOTIFYCASCADECOIN";
	
	/**
	 * Are we on Normal mine mode or LOW
	 */
	boolean mNormalMineMode = true;
	
	/**
	 * Main AI Processor
	 */
	AIProcessor	mAIProcessor;
	AIValidator	mAIValidator;
	
	/**
	 * TxPoW Miner
	 */
	TxPoWMiner 		mTxPoWMiner;
	
	/**
	 * Network Manager
	 */
	NetworkManager mNetwork;
	
	/**
	 * Maxima
	 */
	MaximaManager mMaxima;
	
	/**
	 * MDS
	 */
	MDSManager mMDS;
	
	/**
	 * Send POll Manager
	 */
	SendPollManager mSendPoll;
	
	/**
	 * The Web Hooks for Self messages
	 */
	NotifyManager mNotifyManager;
	
	/**
	 * Are we shutting down..
	 */
	boolean mShuttingdown 				= false;
	boolean mHaveShutDownMDS 			= false;
	/**
	 * Are we restoring..
	 */
	boolean mRestoring = false;
	
	/**
	 * Are we syncing an IBD
	 */
	boolean mSyncIBD = false;
	
	/**
	 * Timer for the automine message
	 */
	public long AUTOMINE_TIMER = 1000 * 50;
	
	/**
	 * Have all the default keys been created..
	 */
	boolean mInitKeysCreated = false;
	
	public SELFSystem() {
		super("MAIN");
	
		if(STARTUP_DEBUG_LOGS) {
			SelfLogger.log("MAIN init.. start");
		}
		
		//Start the Uptime clock..
		mUptimeMilli = System.currentTimeMillis();
		
		//Reset the static values
		mInstance 	= this;
		
		//Create the timer processor
		TimerProcessor.createTimerProcessor();
		
		//Are we running a PRIVATE network..
		if(GeneralParams.PRIVATE) {
			
			//Get the base folder
			File basefolder = new File(GeneralParams.DATA_FOLDER,"databases");
			
			//Is this the first run.. Check if files exist..
			File userdb = new File(basefolder, "userprefs.db");
			if(userdb.exists() && !GeneralParams.CLEAN) {
				//This is not the first run..
				SelfLogger.log("SOLO NETWORK : userdb found : not first run.. no -genesis..");
				
			}else {
				SelfLogger.log("SOLO NETWORK : userdb not found : FIRST RUN.. creating genesis coins..");
				GeneralParams.CLEAN 	= true;
                GeneralParams.GENESIS 	= true;
			}
		}
		
		//Are we deleting previous..
		if(GeneralParams.CLEAN) {
			SelfLogger.log("Wiping previous config files..");
			//Delete the conf folder
			SELFFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, new File(GeneralParams.DATA_FOLDER));
		}
		
		//Create the MinmaDB
		if(STARTUP_DEBUG_LOGS) {
			SelfLogger.log("SelfDB create.. start");
		}
		SelfDB.createDB();
		if(STARTUP_DEBUG_LOGS) {
			SelfLogger.log("SelfDB create.. finish");
		}
		
		//Load the Databases
		if(STARTUP_DEBUG_LOGS) {
			SelfLogger.log("Load all DB.. start");
		}
		SelfDB.getDB().loadAllDB();
		if(STARTUP_DEBUG_LOGS) {
			SelfLogger.log("Load all DB.. finish");
		}
		
		//Are we connecting to a MySQL DB automatically
		if(!GeneralParams.MYSQL_DB_DETAILS.equals("")) {
				
			//Set the details.. and start AUTO backup..
			mysql.convertMySQLParams(GeneralParams.MYSQL_DB_DETAILS);
		}
		
		//Are we in Slave node mode
		boolean slavemode = SelfDB.getDB().getUserDB().isSlaveNode();
		if(slavemode) {
			GeneralParams.CONNECT_LIST 			= SelfDB.getDB().getUserDB().getSlaveNodeHost();
        	GeneralParams.P2P_ENABLED 			= false;
            GeneralParams.TXBLOCK_NODE 			= true;
            GeneralParams.NO_SYNC_IBD 			= true;
            GeneralParams.IS_ACCEPTING_IN_LINKS = false;
            SelfLogger.log("Slave Mode ENABLED master:"+GeneralParams.CONNECT_LIST);
		}
		
		//Create the SSL Keystore..
		if(STARTUP_DEBUG_LOGS) {
			SelfLogger.log("SSL Key.. start");
		}
		SSLManager.makeKeyFile();
		if(STARTUP_DEBUG_LOGS) {
			SelfLogger.log("SSL Key.. finish");
		}
		//Calculate the User hashrate.. start her up as seems to make a difference.. initialises..
		TxPoWMiner.calculateHashRateOld(new SELFNumber(10000));
		
		//Delete the Archive restore folder - if it exists..
		File restorefolder = new File(GeneralParams.DATA_FOLDER,"archiverestore");
		SELFFile.deleteFileOrFolder(GeneralParams.DATA_FOLDER, restorefolder);
		
		//Now do the actual check..
		SELFNumber hashcheck 	= new SELFNumber("250000");
		SELFNumber hashrate 	= TxPoWMiner.calculateHashSpeed(hashcheck);
		SelfDB.getDB().getUserDB().setHashRate(hashrate);
		SelfLogger.log("Calculate device hash rate : "+hashrate.div(SELFNumber.MILLION).setSignificantDigits(4)+" MHs");
		
		//Create the Initial Key Set
		try {
			mInitKeysCreated = SelfDB.getDB().getWallet().initDefaultKeys(3);
		}catch(Exception exc) {
			SelfLogger.log(exc.toString());
		}
		
		//Notification of Events
		mNotifyManager = new NotifyManager();
				
		//Start the engine..
		mAIProcessor = new AIProcessor();
		
		//Create the AI Validator
		mAIValidator	= new AIValidator();
				
//		//Recalc Tree if too large
//		try {
//			if(SelfDB.getDB().getTxPoWTree().getHeaviestBranchLength() > 1200) {
//				SelfLogger.log("Large tree.. recalculating..");
//				mTxPoWProcessor.onStartUpRecalc();
//				
//				//For now..
//				SelfDB.getDB().saveState();
//				
//				//Clean..
//				System.gc();
//			}	
//		}catch(Exception exc) {
//			SelfLogger.log(exc);
//		}
		
		//Are we running a private network
		if(GeneralParams.GENESIS) {
			//Create a genesis node
			doGenesis();
		}
		
		//Clear the Peer Invalid list
		P2PFunctions.clearInvalidPeers();
		
		//Start the networking..
		mNetwork = new NetworkManager();
				
		//Start up Maxima
		mMaxima = new MaximaManager();
				
		//Start MDS
		mMDS = new MDSManager();
		
		//New Send POll Manager
		mSendPoll = new SendPollManager();
		
		//Simulate traffic message
		AUTOMINE_TIMER = SELFNumber.THOUSAND.div(GlobalParams.SELF_BLOCK_SPEED).getAsLong();
		mTxPoWMiner.PostTimerMessage(new TimerMessage(AUTOMINE_TIMER, TxPoWMiner.TXPOWMINER_MINEPULSE));
		
		//Set the PULSE message timer.
		PostTimerMessage(new TimerMessage(GeneralParams.USER_PULSE_FREQ, MAIN_PULSE));
		
		//Clean the DB (delete old records)
		if(GeneralParams.GENESIS) {
			//Do sooner as stores the genesis Txn..
			PostTimerMessage(new TimerMessage(10 * 1000, MAIN_CLEANDB_RAM));
		}else {
			PostTimerMessage(new TimerMessage(3 * 60 * 1000, MAIN_CLEANDB_RAM));
		}
		PostTimerMessage(new TimerMessage(10 * 60 * 1000, MAIN_CLEANDB_SQL));
		
		//System Clean..
		PostTimerMessage(new TimerMessage(SYSTEMCLEAN_TIMER, MAIN_SYSTEMCLEAN));
		
		//Debug Checker
		PostTimerMessage(new TimerMessage(CHECKER_TIMER, SELF_CHECKER));
		
		//Init Keys
		PostTimerMessage(new TimerMessage(1000 * 30, SELF_INIT_KEYS));
				
		//Reset Network stats every 24 hours
		PostTimerMessage(new TimerMessage(NETRESET_TIMER, SELF_NETRESTART));
		
		//AutoBackup - do one in 5 minutes then every 24 hours
		PostTimerMessage(new TimerMessage(1000 * 60 * 5, SELF_AUTOBACKUP));
		
		//MYSQL AutoBackup - do one every 2 hours
		PostTimerMessage(new TimerMessage(SELF_AUTOBACKUP_MYSQL_TIMER, SELF_AUTOBACKUP_MYSQL));
		PostTimerMessage(new TimerMessage(MAIN_AUTOBACKUP_MYSQL_TIMER, MAIN_AUTOBACKUP_MYSQL));
		
		//P2P MDS NET checker
		PostTimerMessage(new TimerMessage(P2PNETMDS_TIMER, MAIN_P2PNETMDS_CHECKER));
				
		//Quick Clean up..
		System.gc();
		
		//Check slavenode status
		if(GeneralParams.TXBLOCK_NODE) {
			
			if(GeneralParams.CONNECT_LIST.indexOf(",")!=-1) {
				//Can only connect to 1 host
				SelfLogger.log("[!] Can ONLY connect to 1 host in slave mode.. stopping");
				Runtime.getRuntime().exit(1);
			}
			
			SelfLogger.log("Running in slave mode. Will Connect to "+GeneralParams.CONNECT_LIST);
		}
	}
	
	/**
	 * Are we syncing an IBD
	 */
	public void setSyncIBD(boolean zSync) {
		if(GeneralParams.IBDSYNC_LOGS) {
			SelfLogger.log("SYNC IBD LOCK : "+zSync);
		}
		mSyncIBD = zSync;
	}
	
	public boolean isSyncIBD() {
		return mSyncIBD;
	}
	
	/**
	 * Used after a Restore
	 */
	public void setHasShutDown() {
		mShuttingdown = true;
	}
	
	public boolean isShuttingDown() {
		return mShuttingdown;
	}
	
	public boolean isRestoring() {
		return mRestoring;
	}
	
	public boolean isShuttongDownOrRestoring() {
		return mShuttingdown || mRestoring;
	}
	
	public void shutdown() {
		shutdown(false);
	}
	
	public void shutdown(boolean zCompact) {
		//Are we already shutting down..
		if(mShuttingdown) {
			SelfLogger.log("Shutdown called when already shutting down..");
			return;
		}
		
		if(zCompact) {
			SelfLogger.log("Shut down started.. Compacting All Databases");
		}else {
			SelfLogger.log("Shut down started..");
		}
		
		//we are shutting down
		mShuttingdown = true;
		
		try {
			
			//Tell the wallet - in case we are creating default keys
			SelfDB.getDB().getWallet().shuttingDown();
			
			//Shut down the network
			shutdownGenProcs();
			
			//Stop the main TxPoW processor
			shutdownFinalProcs();
			
			//Now backup the  databases
			SelfLogger.log("Saving all db");
			SelfDB.getDB().saveAllDB(zCompact);
					
			//Stop this..
			stopMessageProcessor();
			
			//Wait for it..
			SelfLogger.log("SELF thread shutdown");
			waitToShutDown();
			
			SelfLogger.log("Shut down completed OK..");
			
			//Tell listener..
			NotifySELFListenerOfShutDown();
			
		}catch(Exception exc) {
			SelfLogger.log("ERROR Shutting down..");
			SelfLogger.log(exc);
		}
	}
	
	public static void ClearSelfInstance() {
		//NULL main instance..
		if(mInstance != null) {
			mInstance = null;
			SelfLogger.log("SELF Instance Cleared..");
		}
	}
	
	public void NotifySELFListenerOfShutDown() {
		
		//Called from various functions
		ClearSELFInstance();
		
		//Have we done this already
		if(mShutDownSentToListener) {
			return;
		}
		mShutDownSentToListener = true;
		
		//Send them a message
		try {
			NotifySELFListenerOnly("SHUTDOWN");
		} catch (Exception e) {
			SelfLogger.log(e);
		}
	}
	
	public void setStartUpError(boolean zStartError, String zMessage) {
		STARTUP_ERROR 		= zStartError;
		STARTUP_ERROR_MSG 	= zMessage;
	}
	
	public boolean isStartupError() {
		return STARTUP_ERROR;
	}
	
	public String getStartupErrorMsg() {
		return STARTUP_ERROR_MSG;
	}
	
	public void restoreReady() {
		restoreReady(true);
	}
	
	public void restoreReady(boolean zShutdownMDS) {
		//we are about to restore..
		mRestoring = true;
		
		//Shut down the network
		shutdownGenProcs();
		
		//Stop the main TxPoW processor
		shutdownFinalProcs(zShutdownMDS);
	}
	
	public void restoreReadyForSync() {
		
		//Restart the Processor
		mTxPoWProcessor = new TxPoWProcessor();
		
		//Reload the DBs..
		SelfDB.getDB().loadDBsForRestoreSync();
	}
	
	public void archiveResetReady(boolean zResetWallet) {
		archiveResetReady(zResetWallet, true);
	}
	
	public void archiveResetReady(boolean zResetWallet, boolean zResetCascadeTree) {
		//we are about to restore..
		mRestoring = true;
				
		//Shut most of the processors down
		shutdownGenProcs();
		
		//Delete old files.. and reset to new
		SelfDB.getDB().getTxPoWDB().getSQLDB().saveDB(false);
		if(zResetCascadeTree) {
			SelfDB.getDB().getTxPoWDB().getSQLDB().getSQLFile().delete();
		}
		
		SelfDB.getDB().getArchive().saveDB(false);
		SelfDB.getDB().getArchive().getSQLFile().delete();
		
		//Are we deleting the wallet..
		if(zResetWallet) {
			SelfDB.getDB().getWallet().saveDB(false);
			SelfDB.getDB().getWallet().getSQLFile().delete();
		}
		
		//Reload the SQL dbs
		SelfDB.getDB().loadArchiveAndTxPoWDB(zResetWallet);
		
		if(zResetCascadeTree) {
			//Reset these 
			SelfDB.getDB().resetCascadeAndTxPoWTree();
			
			//Delete the cascade..
			SelfLogger.log("Deleting cascade..");
			File cdb = SelfDB.getDB().getCascadeFile();
			if(cdb.exists()) {
				cdb.delete();
			}
		}
	}
	
	private void shutdownGenProcs() {
		
		//No More timer Messages
		TimerProcessor.stopTimerProcessor();
				
		//Shut down the network
		mNetwork.shutdownNetwork();
		
		//Shut down Maxima
		mMaxima.shutdown();
				
		//Stop the Miner
		mTxPoWMiner.stopMessageProcessor();
		
		//Stop sendPoll
		mSendPoll.stopMessageProcessor();
		
		//Wait for the networking to finish
		long timewaited=0;
		while(!mNetwork.isShutDownComplete()) {
			try {Thread.sleep(250);} catch (InterruptedException e) {}
			timewaited+=250;
			if(timewaited>10000) {
				SelfLogger.log("Network shutdown took too long..");
				mNetwork.hardShutDown();
				break;
			}
		}
	}
	
	public void shutdownFinalProcs() {
		shutdownFinalProcs(true);
	}
	
	public void shutdownFinalProcs(boolean zShutDownMDS) {
				
		if(zShutDownMDS) {
			if(!mHaveShutDownMDS) {
				mHaveShutDownMDS = true;
				shutdownMDS();
			}
		}
				
		//Stop the main TxPoW processor
		SelfLogger.log("Shutdown TxPoWProcessor..");
		mTxPoWProcessor.stopMessageProcessor();
		mTxPoWProcessor.waitToShutDown();
	}
	
	public void shutdownMDS() {
		//ShutDown MDS
		SelfLogger.log("Shutdown MDS..");
		mMDS.shutdown();
		
		//Shut down the Notify Manager
		mNotifyManager.shutDown();
	}
	
	/**
	 * USed when Syncing to clear memory
	 */
	public void resetMemFull() {
		//SelfLogger.log("System full memory clean..");
		
		//Reset all the DBs..
		SelfDB.getDB().fullDBRestartMemFree();
		
		//Stop the main TxPoW processor
		mTxPoWProcessor.stopMessageProcessor();
		mTxPoWProcessor.waitToShutDown();
		
		//Now reset the main processor..
		mTxPoWProcessor = new TxPoWProcessor();
		
		//And system clean 
		System.gc();
	}
	
	public void restartNIO() {
		
		//Not now..
		if(mShuttingdown) {
			return;
		}
		
		//Lock the DB
		SelfDB.getDB().readLock(true);
		
		try {
			//Log 
			SelfLogger.log("Network Shutdown started..");
			
			//Shut down the NIO..
			mNetwork.shutdownNetwork();
				
			//Wait for the networking to finish
			long timewaited = 0;
			while(!mNetwork.isShutDownComplete()) {
				try {Thread.sleep(250);} catch (InterruptedException e) {}
				timewaited += 250;
				
				//If we have waited 10 secs.. something not right..
				if(timewaited > 10000) {
					//Hard shutdown..
					mNetwork.hardShutDown();
					break;
				}
			}
					
			//Wait a second..
			SelfLogger.log("Network Shutdown complete.. restart in 5 seconds");
			try {Thread.sleep(5000);} catch (InterruptedException e) {}
			
			//Now restart it..
			mNetwork = new NetworkManager();
			
			SelfLogger.log("Network restarted..");
			
		}catch(Exception exc) {
			
			//Uh oh..
			SelfLogger.log("[!] Error restarting Network.. Restart Self!");
			
		}finally {
			
			//UNLock the DB
			SelfDB.getDB().readLock(false);
		}
	}
	
	//Every 50 seconds - the normal blockspeed
	public void setNormalAutoMineSpeed() {
		mNormalMineMode = true;
		AUTOMINE_TIMER = 1000 * 50;
	}
	
	//Every 500 seconds - for Android when not plugged in
	public void setLowPowAutoMineSpeed() {
		mNormalMineMode = false;
		AUTOMINE_TIMER = 1000 * 500;
	}
	
	public boolean isNormalMineMode() {
		return mNormalMineMode;
	}
	
	public long getUptimeMilli() {
		return System.currentTimeMillis() - mUptimeMilli;
	}
	
	public NetworkManager getNetworkManager() {
		return mNetwork;
	}
	
	public NIOManager getNIOManager() {
		return mNetwork.getNIOManager();
	}
	
	public NotifyManager getNotifyManager() {
		return mNotifyManager;
	}
	
	public AIProcessor getAIProcessor() {
		return mAIProcessor;
	}
	
	public AIValidator getAIValidator() {
		return mAIValidator;
	}
	
	public MaximaManager getMaxima() {
		return mMaxima;
	}
	
	public MDSManager getMDSManager() {
		return mMDS;
	}
	
	public SendPollManager getSendPoll() {
		return mSendPoll;
	}
		
	private void doGenesis() {
		
		//Create a new address - to receive the genesis funds..
		ScriptRow scrow = SelfDB.getDB().getWallet().createNewSimpleAddress(true);
		
		//Create the Genesis TxPoW..
		GenesisTxPoW genesis = new GenesisTxPoW(scrow.getAddress());
		
		//Hard add to the DB
		SelfDB.getDB().getTxPoWDB().addTxPoW(genesis);
		
		//Create the Genesis TxBlock
		TxBlock txgenesisblock = new TxBlock(new GenesisMMR(), genesis, new ArrayList<>());
		
		//The first root node
		TxPoWTreeNode gensisnode = new TxPoWTreeNode(txgenesisblock);
		
		//Set it
		SelfDB.getDB().getTxPoWTree().setRoot(gensisnode);
		
		//And set this txpow as main chain..
		SelfDB.getDB().getTxPoWDB().setOnSelfChain(genesis.getTxPoWID());
	}
	
	public boolean getAllKeysCreated() {
		return mInitKeysCreated;
	}
	
	public int getAllDefaultKeysSize() {
		return SelfDB.getDB().getWallet().getDefaultKeysNumber();
	}
	
	@Override
	protected void processMessage(Message zMessage) throws Exception {
		//Are we shutting down
		if(mShuttingdown || mRestoring) {
			return;
		}
		
		//Process messages
		if(zMessage.getMessageType().equals(SELF_TXPOWMINED)) {
			//Get it..
			TxPoW txpow = (TxPoW) zMessage.getObject("txpow");
			
			//We have mined a TxPoW.. is it atleast a transaction
			if(!txpow.isTransaction() && !txpow.isBlock()) {
				return;
			}
			
			//Did we find a block.. only tell me on the main net.. too easy on Test
			if(!GeneralParams.TEST_PARAMS && txpow.isBlock()) {
				SelfLogger.log("You found a block! "+txpow.getBlockNumber()+" "+txpow.getTxPoWID());
			}
			
			//Create an NIO Message - so the message goes through the same checks as any other message
			SELFData niodata = NIOManager.createNIOMessage(NIOMessage.MSG_TXPOW, txpow);

			//And send
			Message newniomsg = new Message(NIOManager.NIO_INCOMINGMSG);
			newniomsg.addString("uid", "0x00");
			newniomsg.addObject("data", niodata);

			//Post to the NIOManager - which will check it and forward if correct
			getNetworkManager().getNIOManager().PostMessage(newniomsg);
			
		}else if(zMessage.getMessageType().equals(SELF_SYSTEMCLEAN)) {
			
			//Do it again..
			PostTimerMessage(new TimerMessage(SYSTEMCLEAN_TIMER, SELF_SYSTEMCLEAN));
			
			//Clean up the RAM Memory
			System.gc();
			
		}else if(zMessage.getMessageType().equals(SELF_CLEANDB_RAM)) {
			
			//Do it again..
			PostTimerMessage(new TimerMessage(CLEANDB_RAM_TIMER, SELF_CLEANDB_RAM));
			
			//Do some house keeping on the DB
			SelfDB.getDB().getTxPoWDB().cleanDBRAM();
			
			//Now save the state - in case system crashed..
			SelfDB.getDB().saveState();
			
			//Now close and Re-open the SQL db..
			SelfDB.getDB().refreshSQLDB();
			
			//Clear the Maxima Poll Stack
			getMaxima().checkPollMessages();
			
		}else if(zMessage.getMessageType().equals(SELF_AUTOBACKUP_MYSQL)) {
		
			UserDB udb = SelfDB.getDB().getUserDB();
			
			//Are we importing somewhere else..
			if(MYSQL_IMPORTING_NO_ACTION) {
				
				if(udb.getAutoBackupMySQL()) {
					SelfLogger.log("Skipping MySQL Backup as importing data already..");
				}
				
				//Don't do this until we are finished importing..
				PostTimerMessage(new TimerMessage(SELF_AUTOBACKUP_MYSQL_TIMER, SELF_AUTOBACKUP_MYSQL));
			
				return;
			}
			
			
			//Lets see if we need to import data
			try {
				//Are we enabled to back up MySQL..
				if(udb.getAutoBackupMySQL()) {
					
					String backupcommand = "mysql host:"+udb.getAutoMySQLHost()
									+" database:"+udb.getAutoMySQLDB()
									+" user:"+udb.getAutoMySQLUser()
									+" password:"+udb.getAutoMySQLPassword()
									+" action:update";
					
					//Run a mysql Backup of the archive data..
					JSONArray res 	= CommandRunner.getRunner().runMultiCommand(backupcommand);
					JSONObject json = (JSONObject) res.get(0); 
					boolean status  = (boolean) json.get("status");
					
					//Output
					if(!status) {
						SelfLogger.log("[ERROR] MYSQL AUTOBACKUP "+json.getString("error"));
					}else {
						JSONObject response = (JSONObject) json.get("response");
						SelfLogger.log("MYSQL AUTOBACKUP OK "+response.toString());
					}
				}
				
				//Are we enabled to back up MySQL Coins..
				if(udb.getAutoBackupMySQLCoins()) {
					
					String backupcommand = "mysqlcoins host:"+udb.getAutoMySQLHost()
									+" database:"+udb.getAutoMySQLDB()
									+" user:"+udb.getAutoMySQLUser()
									+" password:"+udb.getAutoMySQLPassword()
									+" action:update";
					
					//Run a mysql Backup of the archive data..
					JSONArray res 	= CommandRunner.getRunner().runMultiCommand(backupcommand);
					JSONObject json = (JSONObject) res.get(0); 
					boolean status  = (boolean) json.get("status");
					
					//Output
					if(!status) {
						SelfLogger.log("[ERROR] MYSQLCOINS AUTOBACKUP "+json.getString("error"));
					}else {
						JSONObject response = (JSONObject) json.get("response");
						SelfLogger.log("MYSQLCOINS AUTOBACKUP OK "+response.toString());
					}
				}
				
			}catch(Exception exc) {
				SelfLogger.log(exc);
			}
			
			//Do it again..
			PostTimerMessage(new TimerMessage(SELF_AUTOBACKUP_MYSQL_TIMER, SELF_AUTOBACKUP_MYSQL));
			
		}else if(zMessage.getMessageType().equals(SELF_AUTOBACKUP_TXPOW)) {
			
			//Are we importing somewhere else..
			if(MYSQL_IMPORTING_NO_ACTION) {
				return;
			}
			
			//Are we storing all the TxPoW
			if(GeneralParams.MYSQL_STORE_ALLTXPOW) {
			
				UserDB udb = SelfDB.getDB().getUserDB();
				
				//Are we enabled..
				if(udb.getAutoBackupMySQL()) {
					
					//Get the TxPoW
					TxPoW txp = (TxPoW) zMessage.getObject("txpow");
					
					MySQLConnect mysql = new MySQLConnect(
							udb.getAutoMySQLHost(), 
							udb.getAutoMySQLDB(), 
							udb.getAutoMySQLUser(), 
							udb.getAutoMySQLPassword());
					mysql.init();
					
					//Now save..
					boolean status = mysql.saveTxPoW(txp);
					
					//Shutdown..
					mysql.shutdown();
					
					//Output
					if(!status) {
						SelfLogger.log("[ERROR] MYSQL TXPOW AUTOBACKUP "
								+ " host:"+udb.getAutoMySQLHost()
								+ " user:"+udb.getAutoMySQLUser()
								+ " db:"+udb.getAutoMySQLDB()
								);
					}
				}
			}
		
		}else if(zMessage.getMessageType().equals(SELF_CLEANDB_SQL)) {
			
			//Do it again..
			PostTimerMessage(new TimerMessage(CLEANDB_SQL_TIMER, SELF_CLEANDB_SQL));
			
			//Do some house keeping on the DB
			SelfDB.getDB().getTxPoWDB().cleanDBSQL();
			
			//Same with the ArchiveDB - if not running an archive node
			SelfDB.getDB().getArchive().checkForCleanDB();
			
		}else if(zMessage.getMessageType().equals(SELF_DO_RESCUE)) {
			
			if(!GeneralParams.RESCUE_MEGAMMR_NODE.equals("")) {
				
				SelfLogger.log("Running MegaMMR Sync from Rescuse Node "+GeneralParams.RESCUE_MEGAMMR_NODE);
				
				//Make sure all keys created..
				mInitKeysCreated = true;
				
				//Run a rescue command..
				String command = "megammrsync action:resync host:"+GeneralParams.RESCUE_MEGAMMR_NODE;
				
				//And run it..
				JSONObject res = CommandRunner.getRunner().runSingleCommand(command);
				
				//Output the result
				SelfLogger.log(res.toString());
				
				//At this point.. STOP..
				Runtime.getRuntime().halt(0);
			}
		
		}else if(zMessage.getMessageType().equals(SELF_PULSE)) {
			
			//And then wait again..
			PostTimerMessage(new TimerMessage(GeneralParams.USER_PULSE_FREQ, SELF_PULSE));
			
			//Are we a Slavenode - have no transactions.. use TXBLOCKMINE msg instead
			if(GeneralParams.TXBLOCK_NODE) {
				return;
			}
			
			//Create Pulse Message
			Pulse pulse = Pulse.createPulse();
		
			//And send it to all your peers..
			NIOManager.sendNetworkMessageAll(NIOMessage.MSG_PULSE, pulse);
			
		}else if(zMessage.getMessageType().equals(SELF_NEWBLOCK)) {
			
			//Get the TxPoW
			TxPoW txpow = (TxPoW) zMessage.getObject("txpow");
			
			//Notify The Web Hook Listeners
			JSONObject data = new JSONObject();
			data.put("txpow", txpow.toJSON());
			
			//And Post it..
			PostNotifyEvent("NEWBLOCK", data);
			
		}else if(zMessage.getMessageType().equals(SELF_BALANCE)) {
			
			//And Post it..
			PostNotifyEvent("NEWBALANCE", new JSONObject());
				
		}else if(zMessage.getMessageType().equals(SELF_MINING)) {
			
			//Get the TxPoW
			TxPoW txpow = (TxPoW) zMessage.getObject("txpow");
					
			//Are we starting or stopping..
			boolean starting = zMessage.getBoolean("starting");
			
			//Notify The Web Hook Listeners
			JSONObject data = new JSONObject();
			data.put("txpow", txpow.toJSON());
			data.put("mining", starting);
			
			//And Post it..
			PostNotifyEvent("MINING", data);
			
		}else if(zMessage.getMessageType().equals(SELF_NETRESTART)) {
			
			SelfLogger.log("[!] SELF restart networking..");
			
			//First disconnect everyone..
			SelfLogger.log("Disconnect all peers");
			SELFSystem.getInstance().getNetworkManager().getNIOManager().PostMessage(NIOManager.NIO_DISCONNECTALL);
			
			//Now wait..
			SelfLogger.log("Wait 10 seconds..");
			Thread.sleep(10000);
			
			//Stop and restart the MDS..
			SelfLogger.log("Clear MDS");
			mMDS.clearExceptString("MDS_TIMER");
			
			//Reset the IBD timer
			getTxPoWProcessor().resetFirstIBDTimer();
			
			//Clear the Peer Invalid list
			P2PFunctions.clearInvalidPeers();
			
			//Clear the IBD sent list
			NIOMessage.mHaveSentIBDRecently.clear();
			
			//Restart the Networking..
			restartNIO();

		}else if(zMessage.getMessageType().equals(SELF_AUTOBACKUP)) {
			
			//And Again..
			PostTimerMessage(new TimerMessage(AUTOBACKUP_TIMER, SELF_AUTOBACKUP));
			
			//Are we backing up..
			if(SelfDB.getDB().getUserDB().isAutoBackup()) {
			
				//Create a backup command..
				JSONArray res = CommandRunner.getRunner().runMultiCommand("backup");
				
				//Output
				SelfLogger.log("AUTOBACKUP : "+res.toString());
			}
			
			//Recalculate the hash speed..
			SELFNumber hashcheck 	= new SELFNumber("250000");
			SELFNumber hashrate 	= TxPoWMiner.calculateHashSpeed(hashcheck);
			SelfDB.getDB().getUserDB().setHashRate(hashrate);
			SelfLogger.log("Re-Calculate device hash rate : "+hashrate.div(SELFNumber.MILLION).setSignificantDigits(4)+" MHs");
			
		}else if(zMessage.getMessageType().equals(SELF_NETRESET)) {
			
			//Reset the networking stats
			SELFSystem.getInstance().getNIOManager().getTrafficListener().reset();
			
			//Reset Network stats every 24 hours
			PostTimerMessage(new TimerMessage(NETRESET_TIMER, SELF_NETRESET));
			
		}else if(zMessage.getMessageType().equals(SELF_SHUTDOWN)) {
			
			shutdown();
		
		}else if(zMessage.getMessageType().equals(SELF_INIT_KEYS)) {
			
			//Check the Default keys
			if(!mInitKeysCreated) {
				try {
					mInitKeysCreated = SelfDB.getDB().getWallet().initDefaultKeys(8);
					if(mInitKeysCreated) {
						SelfLogger.log("All default getaddress keys created..");
					}
				}catch(Exception exc) {
					SelfLogger.log(exc);
				}
			}
			
			//Check again..
			if(!mInitKeysCreated) {
				PostTimerMessage(new TimerMessage(INIT_KEYS_TIMER, SELF_INIT_KEYS));
			}
			
		}else if(zMessage.getMessageType().equals(SELF_CHECKER)) {
			
			//Check again..
			PostTimerMessage(new TimerMessage(CHECKER_TIMER, SELF_CHECKER));
			
			//Get the Current Tip
			TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
			if(tip == null) {
				SelfLogger.log("No tip found in SELF Checker..");
				return;
			}
			
			//Has it changed
			if(tip.getTxPoW().getTxPoWIDData().isEqual(mOldTip)) {
				SelfLogger.log("Warning : Chain tip hasn't changed in 180 seconds "+tip.getTxPoW().getTxPoWID()+" "+tip.getTxPoW().getBlockNumber().toString());
			}
			
			//Keep for the next round
			mOldTip = tip.getTxPoW().getTxPoWIDData();
			
			//A Ping Message.. The top TxPoWID
			NIOManager.sendNetworkMessageAll(NIOMessage.MSG_PING, tip.getTxPoW().getTxPoWIDData());
		
		}else if(zMessage.getMessageType().equals(SELF_P2PNETMDS_CHECKER)) {
			
			//Repost
			PostTimerMessage(new TimerMessage(P2PNETMDS_TIMER, SELF_P2PNETMDS_CHECKER));
			
			//Are we connected to the internet
			boolean restartsent = false;
			if(GeneralParams.P2P_ENABLED && P2PFunctions.isNetAvailable()) {
			
				//Current time
				long timenow = System.currentTimeMillis();
				
				//Get the tip.. 
				TxPoWTreeNode tip 	= SelfDB.getDB().getTxPoWTree().getTip();
				
				//Do we have a tip
				if(tip == null) {
					return;
				}
				
				long tiptime 		= tip.getTxPoW().getTimeMilli().getAsLong();
				
				//Difference..
				long diff 			= timenow - tiptime;
				//How many messages are in the stack..
	        	int count = SELFSystem.getInstance().getNetworkManager().getP2PManager().getSize();
	        	
	        	//If too many restart networking..
	        	if(count > 50) {
	        		
	        		SelfLogger.log("[!] P2P Message Overload - Restart");
	        		
	        		//Wipe the List
	        		if(!restartsent) {
	        			restartsent = true;
	        			SELFSystem.getInstance().PostMessage(SELFSystem.SELF_NETRESTART);
	        		}
	        	}
			}
			
			//Clear the Invalid peers..
			P2PFunctions.clearInvalidPeers();
			
			//Clear the IBD sent list
			NIOMessage.mHaveSentIBDRecently.clear();
			
		}else if(zMessage.getMessageType().equals(SELF_CALLCHECKER)) {
			
			boolean timed = zMessage.getBoolean("timer", false);
			
			//Sent to check this is running..
			SelfLogger.log("SELF Checker Call Recieved.. timer:"+timed);
		}
	}
	
	/**
	 * Post a network message to the webhook / MDS / Android listeners
	 */
	public void PostNotifyEvent(String zEvent, JSONObject zData) {
		PostNotifyEvent(zEvent, zData, "*");
	}
	
	public void PostNotifyEvent(String zEvent, JSONObject zData, String zTo) {
		
		//Create the JSON Message
		JSONObject notify = new JSONObject();
		notify.put("event", zEvent);
		notify.put("data", zData);
		
		//Post to everyone ?
		if(zTo.equals("*")) {
			if(getNotifyManager() != null) {
				
				//And post
				getNotifyManager().PostEvent(notify);
			}
		}
		
		//Tell the MDS..
		if(getMDSManager() != null) {
			Message poll = new Message(MDSManager.MDS_POLLMESSAGE);
			poll.addObject("poll", notify);
			poll.addObject("to", zTo);
			getMDSManager().PostMessage(poll);
		}
	}
	
	/**
	 * Send a message ONLY to the LIstener..
	 */
	public static void NotifySELFListenerOnly(String zMessage) throws Exception {
		//Notify
		if(SELFSystem.getSelfListener() != null) {
			
			//Create the JSON Message
			JSONObject notify = new JSONObject();
			notify.put("message", zMessage);
			
			//Send it
			getSelfListener().PostMessage(notify);
		}
	}
}
