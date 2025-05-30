package org.self.system.network.self;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.self.database.SelfDB;
import org.self.database.txpowdb.TxPoWDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.objects.Greeting;
import org.self.objects.IBD;
import org.self.objects.Pulse;
import org.self.objects.TxBlock;
import org.self.objects.TxPoW;
import org.self.objects.base.SELFByte;
import org.self.objects.base.SELFData;
import org.self.objects.base.SELFNumber;
import org.self.objects.base.SELFString;
import org.self.system.SELFSystem;
import org.self.system.brains.TxPoWChecker;
import org.self.system.brains.TxPoWGenerator;
import org.self.system.brains.TxPoWSearcher;
import org.self.system.commands.backup.mmrsync.MegaMMRIBD;
import org.self.system.commands.backup.mmrsync.MegaMMRSyncData;
import org.self.system.commands.backup.mmrsync.megammrsync;
import org.self.system.network.maxima.MaximaCTRLMessage;
import org.self.system.network.maxima.MaximaManager;
import org.self.system.network.maxima.message.MaxTxPoW;
import org.self.system.network.p2p.P2PFunctions;
import org.self.system.network.p2p.P2PManager;
import org.self.system.network.p2p.messages.InetSocketAddressIO;
import org.self.system.params.GeneralParams;
import org.self.system.params.GlobalParams;
import org.self.utils.MiniFormat;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;
import org.self.utils.json.parser.JSONParser;
import org.self.utils.messages.Message;
import org.self.utils.messages.TimerMessage;

public class NIOMessage implements Runnable {

	/**
	 * What was the last sync block requested..
	 */
	public static ConcurrentHashMap<String, SELFNumber> mlastSyncReq = new ConcurrentHashMap<>(); 
	
	/**
	 * When was the last time you tried a chain sync..
	 */
	public static ConcurrentHashMap<String, Long> mLastChainSync = new ConcurrentHashMap<>();
	
	/**
	 * Have we sent an IBD in the last 30 mins..  
	 */
	public static HashSet<String> mHaveSentIBDRecently = new HashSet<>(); 
	
	/**
	 * Base Message types sent over the network
	 */
	public static final SELFByte MSG_GREETING 		= new SELFByte(0);
	public static final SELFByte MSG_IBD 			= new SELFByte(1); // initial blockchain download
	
	// PoAI Message Types
	public static final SELFByte MSG_VALIDATIONID 		= new SELFByte(2);
	public static final SELFByte MSG_VALIDATIONREQ 	= new SELFByte(3);
	public static final SELFByte MSG_VALIDATION 		= new SELFByte(4);
	
	// General Message Types
	public static final SELFByte MSG_GENMESSAGE 	= new SELFByte(5);
	public static final SELFByte MSG_PULSE 			= new SELFByte(6);
	public static final SELFByte MSG_P2P 			= new SELFByte(7);
	public static final SELFByte MSG_PING 			= new SELFByte(8);
	
	// Maxima Message Types
	public static final SELFByte MSG_MAXIMA_CTRL	= new SELFByte(9);
	public static final SELFByte MSG_MAXIMA_TXPOW 	= new SELFByte(10);
	
	// Single Message Types
	public static final SELFByte MSG_SINGLE_PING 	= new SELFByte(11);
	public static final SELFByte MSG_SINGLE_PONG 	= new SELFByte(12);
	
	// IBD Message Types
	public static final SELFByte MSG_IBD_REQ 		= new SELFByte(13);
	public static final SELFByte MSG_IBD_RESP 		= new SELFByte(14);
	
	// Archive Message Types
	public static final SELFByte MSG_ARCHIVE_REQ 		= new SELFByte(15);
	public static final SELFByte MSG_ARCHIVE_DATA 		= new SELFByte(16);
	public static final SELFByte MSG_ARCHIVE_SINGLE_REQ = new SELFByte(17);
	
	// TxBlock Message Types
	public static final SELFByte MSG_TXBLOCKID 			= new SELFByte(18);
	public static final SELFByte MSG_TXBLOCKREQ 		= new SELFByte(19);
	public static final SELFByte MSG_TXBLOCK 			= new SELFByte(20);
	public static final SELFByte MSG_TXBLOCKMINE 		= new SELFByte(21);
	
	// MegaMMR Message Types
	public static final SELFByte MSG_MEGAMMRSYNC_REQ 	= new SELFByte(22);
	public static final SELFByte MSG_MEGAMMRSYNC_RESP 	= new SELFByte(23);
	
	/**
	 * Chain types
	 */
	public static final SELFData CHAIN_SELF = new SELFData("SELF");
	public static final SELFData CHAIN_MINIMA = new SELFData("MINIMA");
	
	/**
	 * Process incoming message
	 */
	public void processMessage(SELFByte msgType, DataInputStream in) throws IOException {
		switch(msgType) {
			case MSG_VALIDATION:
				// Handle PoAI validation message
				processValidationMessage(in);
				break;
				
			case MSG_MAXIMA_TXPOW:
				// Handle TxPOW message for Minima compatibility
				processTxPoWMessage(in);
				break;
				
			default:
				// Handle other message types
				super.processMessage(msgType, in);
				break;
		}
	}
	
	/**
	 * Process PoAI validation message
	 */
	private void processValidationMessage(DataInputStream in) throws IOException {
		// Read validation message
		ValidationMessage validation = new ValidationMessage();
		validation.read(in);
		
		// Check if this is a Minima validation
		if(validation.getHeader().getChainID().isEqual(CHAIN_MINIMA)) {
			// Convert to TxPoW format for Minima compatibility
			TxPoW txpow = convertToTxPoW(validation);
			SelfDB.getDB().getTxPoWDB().addTxPoW(txpow);
		} else {
			// Process as SELF validation
			SelfDB.getDB().addValidation(validation);
		}
	}
	
	/**
	 * Process TxPOW message
	 */
	private void processTxPoWMessage(DataInputStream in) throws IOException {
		// Read TxPoW message
		TxPoW txpow = new TxPoW();
		txpow.read(in);
		
		// Convert to validation format for SELF compatibility
		ValidationMessage validation = convertToValidation(txpow);
		
		// Process as SELF validation
		SelfDB.getDB().addValidation(validation);
	}
	
	/**
	 * Convert ValidationMessage to TxPoW
	 */
	private TxPoW convertToTxPoW(ValidationMessage validation) {
		TxPoW txpow = new TxPoW();
		txpow.setHeader(validation.getHeader());
		txpow.setTransaction(validation.getTransaction());
		txpow.setWitness(validation.getWitness());
		txpow.setValidationScore(validation.getValidationScore());
		return txpow;
	}
	
	/**
	 * Convert TxPoW to ValidationMessage
	 */
	private ValidationMessage convertToValidation(TxPoW txpow) {
		ValidationMessage validation = new ValidationMessage();
		validation.setHeader(txpow.getHeader());
		validation.setTransaction(txpow.getTransaction());
		validation.setWitness(txpow.getWitness());
		validation.setValidationScore(txpow.getValidationScore());
		validation.getHeader().setChainID(CHAIN_SELF);
		return validation;
	}
	
	/**
	 * Helper function that converts to String 
	 */
	public static String convertMessageType(SELFByte zType) {
		if(zType.isEqual(MSG_GREETING)) {
			return "GREETING";
		}else if(zType.isEqual(MSG_IBD)) {
			return "IBD";
		}else if(zType.isEqual(MSG_VALIDATIONID)) {
			return "VALIDATIONID";
		}else if(zType.isEqual(MSG_VALIDATIONREQ)) {
			return "VALIDATIONREQ";
		}else if(zType.isEqual(MSG_VALIDATION)) {
			return "VALIDATION";
		}else if(zType.isEqual(MSG_GENMESSAGE)) {
			return "GENMESSAGE";
		}else if(zType.isEqual(MSG_PULSE)) {
			return "PULSE";
		}else if(zType.isEqual(MSG_P2P)) {
			return "P2P";
		}else if(zType.isEqual(MSG_PING)) {
			return "PING";
		}else if(zType.isEqual(MSG_SINGLE_PONG)) {
			return "MSG_SINGLE_PONG";
		}else if(zType.isEqual(MSG_MAXIMA_CTRL)) {
			return "MAXIMA_CTRL";
		}else if(zType.isEqual(MSG_MAXIMA_TXPOW)) {
			return "MAXIMA";
		
		}else if(zType.isEqual(MSG_IBD_REQ)) {
			return "MSG_IBD_REQ";
		}else if(zType.isEqual(MSG_IBD_RESP)) {
			return "MSG_IBD_RESP";
		
		}else if(zType.isEqual(MSG_ARCHIVE_DATA)) {
			return "MSG_ARCHIVE_DATA";
		}else if(zType.isEqual(MSG_ARCHIVE_REQ)) {
			return "MSG_ARCHIVE_REQ";
		}else if(zType.isEqual(MSG_ARCHIVE_SINGLE_REQ)) {
			return "MSG_ARCHIVE_SINGLE_REQ";
		
		}else if(zType.isEqual(MSG_MEGAMMRSYNC_REQ)) {
			return "MSG_MEGAMMRSYNC_REQ";
		}else if(zType.isEqual(MSG_MEGAMMRSYNC_RESP)) {
			return "MSG_MEGAMMRSYNC_RESP";
		
		}else if(zType.isEqual(MSG_TXBLOCKID)) {
			return "TXBLOCKID";
		}else if(zType.isEqual(MSG_TXBLOCKREQ)) {
			return "TXBLOCKREQ";
		}else if(zType.isEqual(MSG_TXBLOCK)) {
			return "TXBLOCK";
		}else if(zType.isEqual(MSG_TXBLOCKMINE)) {
			return "TXBLOCKMINE";
		}
		
		return "UNKNOWN_"+zType.toString();
	}
	
	/**
	 * Who sent the message
	 */
	String mClientUID;
	
	/**
	 * The Data packet sent
	 */
	MiniData mData;
	
	/**
	 * Is Trace on 
	 */
	boolean mTrace = false;
	String mFilter  = "";
 	
	public static long LAST_TXBLOCKMINE_MSG = 0;
	
	public String mFullAdrress = "";
	
	public int HEAVIER_CHAIN_FOUND = 0;
	
	public NIOMessage(String zClientUID, MiniData zData) {
		mClientUID 	= zClientUID;
		mData 		= zData;
	}
	
	public void setFullAddress(String zAddress) {
		mFullAdrress = zAddress;
	}
	
	public void setTrace(boolean zTrace, String zFilter) {
		mTrace = zTrace;
		mFilter = zFilter;
	}
	
	@Override
	public void run() {
		//Convert the MiniData into a valid net message
		byte[] data = mData.getBytes();
		
		//The streams..
		ByteArrayInputStream bais 	= null;
		DataInputStream dis			= null;
		
		//Are we shutting down
		if(SELFSystem.getInstance().isShuttongDownOrRestoring()) {
			mData = null;
			return;
		}
		
		//Is this message from an invalid peer
		/*if(!mFullAdrress.equals("")) {
			if(P2PFunctions.isInvalidPeer(mFullAdrress)) {
				//Just disconnect
				mData = null;
				SELFSystem.getInstance().getNIOManager().disconnect(mClientUID);
				return;
			}
		}*/
		
		//Convert..
		bais 	= new ByteArrayInputStream(data);
		dis 	= new DataInputStream(bais);
		
		//What type of message is it..
		try {
			//What Type..
			SELFByte type = SELFByte.ReadFromStream(dis);
		
			//Are we syncing an IBD
			if(SELFSystem.getInstance().isSyncIBD()) {
				if(type.isEqual(MSG_VALIDATIONID) || type.isEqual(MSG_TXBLOCKID) || type.isEqual(MSG_PULSE)) {
					//Ignore until finished..
					SelfLogger.log("Ignoring NIOmessage during IBD Sync.. type:"+convertMessageType(type));
					return;
				}
			}
			
			//Are we a TxBlock node..
			if(GeneralParams.TXBLOCK_NODE) {
				if( type.isEqual(MSG_VALIDATIONID)) {
					//Ignore these..
					return;
				}
			}		
//				//Random message lost
//				if( type.isEqual(MSG_TXBLOCKID)) {
//					if(new Random().nextInt(100) < 80) {
//						SelfLogger.log("RANDOM LOSE TXBLOCK ID MESSAGE");
//						return;
			try {
				if (dis != null) {
					dis.close();
				}
			} catch (IOException e) {
				SelfLogger.log("Error closing streams: " + e.getMessage());
			}
		}
		
		// Process the message
		switch(type.toInteger()) {
			case 0: // GREETING
				// Handle greeting
				break;
			case 1: // IBD
				// Handle IBD
				break;
			case 2: // VALIDATIONID
				// Handle validation ID
				try {
					// Read validation ID
					SELFData validationID = SELFData.ReadFromStream(dis);
					testcheck = false;
				}else if(!GeneralParams.TEST_PARAMS && greetstr.contains("TEST")) {
					testcheck = false;
				} 
				
				if(!testcheck || !greetstr.startsWith(GlobalParams.SELF_BASE_VERSION)) {
					
					SelfLogger.log("Greeting with Incompatible Version! "+greet.getVersion().toString()+" .. we are "+GlobalParams.SELF_VERSION+" from "+nioclient.getFullAddress()+" incoming:"+nioclient.isIncoming());
					
					//Add to our Invalid Peers list
					P2PFunctions.addInvalidPeer(nioclient.getFullAddress());
					
					//Tell the P2P..
					Message newconn = new Message(P2PFunctions.P2P_NOCONNECT);
					newconn.addObject("client", nioclient);
					newconn.addString("uid", nioclient.getUID());
					SELFSystem.getInstance().getNetworkManager().getP2PManager().PostMessage(newconn);
					
					//Are we incoming..
					if(nioclient.isIncoming() && !nioclient.haveSentGreeting()) {
						nioclient.setSentGreeting(true);
						
						//Send them a greeting so THEY disconnect
						Greeting greetout = new Greeting().createGreeting();
						
						//And send it..
						NIOManager.sendNetworkMessage(nioclient.getUID(), NIOMessage.MSG_GREETING, greetout);
						
						//Disconnect in with a small pause
						TimerMessage msg = new TimerMessage(2000,NIOManager.NIO_DISCONNECT);
						msg.addString("uid", mClientUID);
						SELFSystem.getInstance().getNIOManager().PostTimerMessage(msg);
					
					}else{
					
						//Just disconnect, you've already sent the Greeting - permanently ( no reconnect )
						SELFSystem.getInstance().getNIOManager().disconnect(mClientUID);
					}
					
					return;
				}
				
				//Is this a port foprwrad address
				if(nioclient.getHost().equals("127.0.0.1")) {
					if(greet.getExtraData().containsKey("host")) {
						SelfLogger.log("Greeting from SSH Port with HOST set : "+greet.getExtraDataValue("host"));
					}
				}
				
				//Get the Host / Port..
				if(greet.getExtraData().containsKey("host")) {
					nioclient.overrideHost(greet.getExtraDataValue("host"));
				}
				if(greet.getExtraData().containsKey("port")) {
					nioclient.setSelfPort(Integer.parseInt(greet.getExtraDataValue("port")));
				}
				
//				//Is there a maxima mls..
//				if(greet.getExtraData().containsKey("maximamls")) {
//					String mls = greet.getExtraData().getString("maximamls"); 
//					SelfLogger.log("MLS rec : "+mls);
//					nioclient.setMaximaMLS(mls+"@"+nioclient.getFullAddress());
//				}
				
				//Get the welcome message..
				nioclient.setWelcomeMessage("Self v"+greet.getVersion());
				nioclient.setValidGreeting(true);
				
				//Tell the P2P..
				Message newconn = new Message(P2PFunctions.P2P_CONNECTED);
				newconn.addString("uid", nioclient.getUID());
				newconn.addBoolean("incoming", nioclient.isIncoming());
				newconn.addObject("client", nioclient);
				SELFSystem.getInstance().getNetworkManager().getP2PManager().PostMessage(newconn);
				
				//Tell MAXIMA
				Message maxconn = new Message(MaximaManager.MAXIMA_CONNECTED);
				maxconn.addObject("nioclient", nioclient);
				maxconn.addString("uid", nioclient.getUID());
				maxconn.addBoolean("incoming", nioclient.isIncoming());
				SELFSystem.getInstance().getMaxima().PostMessage(maxconn);
				
				//Is this an incoming connection.. send a greeting!
				if(nioclient.isIncoming()) {
					
					//Only Send this ONCE!
					if(!nioclient.haveSentGreeting()) {
						nioclient.setSentGreeting(true);	
						
						//Send a greeting..
						Greeting greetout = new Greeting().createGreeting();
						
						//And send it..
						NIOManager.sendNetworkMessage(nioclient.getUID(), NIOMessage.MSG_GREETING, greetout);
					}
				}
				
//				String welcome = (String) greet.getExtraData().get("welcome");
//				if(welcome != null) {
//					//Tell the NIOServer
//					SELFSystem.getInstance().getNIOManager().getNIOServer().setWelcome(mClientUID, welcome);
//				}
				
				//Have we sent an IBD message already..
				String miniaddress = nioclient.getFullSelfAddress();
				if(mHaveSentIBDRecently.contains(miniaddress)) {
					SelfLogger.log("Already sent an IBD to "+miniaddress+" in last 30 mins..");
					
				}else {
					
					//Add to our list
					mHaveSentIBDRecently.add(miniaddress);
					
					//Create an IBD response to that Greeting..
					IBD ibd = new IBD();
					boolean isvalid = ibd.createIBD(greet);
					
					//Was it a vaild IBD - with a crossover..
					if(!isvalid) {
						 //Add him to the invalid peers list
						if(!mFullAdrress.equals("")) {
							P2PFunctions.addInvalidPeer(mFullAdrress);
						}
						
						//Still send him OUR IBD so they know they are on the wrong chain  aswell.
						//..
					}
					
					//Send it
					NIOManager.sendNetworkMessage(mClientUID, MSG_IBD, ibd);
				}
				
			}else if(type.isEqual(MSG_IBD)) {
				
				//Log it..
				if(GeneralParams.IBDSYNC_LOGS) {
					SelfLogger.log("Received IBD size:"+MiniFormat.formatSize(data.length));
				}
				
				//IBD received..
				IBD ibd = IBD.ReadFromStream(dis);
				
				//Log it..
				if(GeneralParams.IBDSYNC_LOGS) {
					SelfLogger.log("Received IBD blocks:"+ibd.getTxBlocks().size());
				}
				
				//Check Seems Valid..
				if(!ibd.checkValidData()) {
					
					SelfLogger.log("Received INVALID IBD from "+mClientUID);
					
					//Disconnect
					if(!mFullAdrress.equals("")) {
						P2PFunctions.addInvalidPeer(mFullAdrress);
					}
					
					SELFSystem.getInstance().getNIOManager().disconnect(mClientUID,true);
					
					return;
				}
				
				//Is it a complete IBD even though we have a cascade
				if(SelfDB.getDB().getCascade().getLength()>0 && ibd.hasCascadeWithBlocks()) {
					
					boolean heavier = IBD.checkOurChainHeavier(ibd);
					
					if(!heavier) {
						//Post a message
						SELFSystem.getInstance().PostNotifyEvent("MDS_HEAVIER_CHAIN", new JSONObject());
						
						SelfLogger.log("[!] CONNECTED TO HEAVIER CHAIN.. from "+mClientUID+" ..disconnecting");
						
						//Disconnect
						SELFSystem.getInstance().getNIOManager().disconnect(mClientUID,true);
						
						//Do we have a rescue NODE
						if(!GeneralParams.RESCUE_MEGAMMR_NODE.equals("")) {
							
							//For now first time..
							HEAVIER_CHAIN_FOUND++;
							if(HEAVIER_CHAIN_FOUND > 0) {
								
								SelfLogger.log("RESCUE NODE FOUND.. attempting rescue @ "+GeneralParams.RESCUE_MEGAMMR_NODE);
								
								//Post a message that does a RESCUE..
								SELFSystem.getInstance().PostTimerMessage(new TimerMessage(1000, SELFSystem.SELF_DO_RESCUE));
							}
						}
						
						return;
						
					}else {
						SelfLogger.log("[!] Received IBD with cascade even though we have one.. from "+mClientUID);
					}
				}
								
				//A small message..
				SelfLogger.log("[+] Connected to the blockchain Initial Block Download received. size:"+MiniFormat.formatSize(data.length)+" blocks:"+ibd.getTxBlocks().size());
				
				//Send to the Processor
				SELFSystem.getInstance().getTxPoWProcessor().postProcessIBD(ibd, mClientUID);
				
			}else if(type.isEqual(MSG_TXPOWID)) {
				
				//Read in the txpowid
				MiniData txpowid = MiniData.ReadFromStream(dis);
				
				//Do we have it..
				boolean exists = SelfDB.getDB().getTxPoWDB().exists(txpowid.to0xString());
				
				//If not request it..
				if(!exists) {
					//request it..
					NIOManager.sendNetworkMessage(mClientUID, MSG_TXPOWREQ, txpowid);
				}
				
			}else if(type.isEqual(MSG_TXPOWREQ)) {
				
				//Read in the txpowid
				MiniData txpowid = MiniData.ReadFromStream(dis);
				
				//Load it.. (but don't keep in RAM)
				TxPoW txpow = SelfDB.getDB().getTxPoWDB().getTxPoW(txpowid.to0xString());
				 
				//And send it on..
				if(txpow != null) {
					//request it..
					NIOManager.sendNetworkMessage(mClientUID, MSG_TXPOW, txpow);
				}else {
					//SelfLogger.log("TxPoW requested from "+mClientUID+" that we don't have.. "+txpowid.to0xString());
				}
			
			}else if(type.isEqual(MSG_TXPOW)) {
				//Read the TxPoW
				TxPoW txpow = TxPoW.ReadFromStream(dis);
				
				//Do we have it..
				boolean exists = SelfDB.getDB().getTxPoWDB().exists(txpow.getTxPoWID());
				if(exists) {
					return;
				}
				
				//Now get the current tip details
				TxPoWTreeNode tip 		= SelfDB.getDB().getTxPoWTree().getTip();
				TxPoWTreeNode cascade 	= SelfDB.getDB().getTxPoWTree().getRoot();
				
				//Have we got any blocks at all yet..
				if(tip == null) {
					return;
				}
				
				//The block and cascade block
				MiniNumber cascadeblock = cascade.getBlockNumber();
				MiniNumber block 		= txpow.getBlockNumber();
				
				//Check if is a block and within range of our current tip
				BigDecimal tipdec 		= new BigDecimal(tip.getTxPoW().getBlockDifficulty().getDataValue());
				BigDecimal blockdec 	= new BigDecimal(txpow.getBlockDifficulty().getDataValue());
				double blockdiffratio 	= tipdec.divide(blockdec, MathContext.DECIMAL32).doubleValue();
				
				//Start a timer..
				long timestart = System.currentTimeMillis();
				
				//Some BASIC checks that MUST pass..
				boolean disconnectpeer = false;
				
				//NONE of these should fail
				if(!txpow.getChainID().isEqual(TxPoWChecker.CURRENT_NETWORK)) {
					//Check ChainID
					SelfLogger.log("Wrong Block ChainID! from "+mClientUID+" "+txpow.getChainID()+" "+txpow.getTxPoWID());
					disconnectpeer = true;
				
				}else if(!TxPoWChecker.checkTxPoWBasic(txpow)) {
					//Basic checks for valid TxPoW
					SelfLogger.log("TxPoW FAILS Basic checks from "+mClientUID+" "+txpow.getTxPoWID());
					disconnectpeer = true;
				
				}else if(!TxPoWChecker.checkSignatures(txpow)) {
					//Check the Signatures
					SelfLogger.log("Invalid signatures on txpow from "+mClientUID+" "+txpow.getTxPoWID());
					disconnectpeer = true;
				
				}else if(!RelayPolicy.checkMaxStateStoreSize(txpow,tip.getTxPoW().getMagic().getMaxTxPoWSize().getAsLong())) {
					//Check state store size..
					SelfLogger.log("TxPoW state store too large..");
					disconnectpeer = true;
				}
				
				//Do we disconnect yet.. 
				if(disconnectpeer) {
					SELFSystem.getInstance().getNIOManager().disconnect(mClientUID);
					return;
				}
				
				long timestart1 = System.currentTimeMillis();
				
				//More CHECKS.. if ALL these pass will forward otherwise may be a branch txpow that we requested
				boolean fullyvalid = true;
				
				//Interesting info.. check this.. probably a timing issue
				if(txpow.isBlock() && blockdiffratio < 0.01) {
					//Block difficulty too low..
					//SelfLogger.log("Received txpow block with low block difficulty.. "+blockdiffratio+" "+txpow.getBlockNumber()+" "+txpow.getTxPoWID());
					fullyvalid = false;
				}
				
				boolean beforecascade = false;
				if(block.isLess(cascadeblock)) {
					//Block before cascade
					//SelfLogger.log("Received block before cascade.. "+block+" / "+cascadeblock+" difficulty:"+blockdiffratio+" from "+mClientUID);
					fullyvalid 		= false;
					beforecascade 	= true;
				}
				
				//Check RELAY POLICY
				if(!RelayPolicy.checkAllPolicies(txpow,GeneralParams.MAX_RELAY_STORESTATESIZE)) {
					fullyvalid = false;
				}
				
				long timestart2 = System.currentTimeMillis();
				
				//Check the Scripts - could fail.. 
				if(!TxPoWChecker.checkTxPoWScripts(tip.getMMR(), txpow, tip.getTxPoW())) {
					//Monotonic txn MUST pass the script check or is INVALID - since will never pass..
					if(txpow.isMonotonic()) {
						SelfLogger.log("Error Monotonic TxPoW failed script check from Client:"+mClientUID+" "+txpow.getTxPoWID());
						return;
					}else {
						SelfLogger.log("NON-Monotonic TxPoW failed script check from Client:"+mClientUID+" "+txpow.getTxPoWID());
					}
					
					//Could be block related
					fullyvalid = false;
				}
				
				long timestart3 = System.currentTimeMillis();
				
				//Max time in the future.. 2 hours.. could be OUR clock..
				if(txpow.isBlock()) {
					MiniNumber maxtime = new MiniNumber(System.currentTimeMillis() + (1000 * 60 * 120));
					if(txpow.getTimeMilli().isMore(maxtime)) {
						SelfLogger.log("TxPoW block received with millitime MORE than 2 hours in future "+new Date(txpow.getTimeMilli().getAsLong())+" "+txpow.getTxPoWID());
						fullyvalid = false;
					}
				}
				
				//Check size
				long size = txpow.getSizeinBytesWithoutBlockTxns();
				if(size > tip.getTxPoW().getMagic().getMaxTxPoWSize().getAsLong()) {
					SelfLogger.log("TxPoW received size too large.. "+size+" "+txpow.getTxPoWID());
					fullyvalid = false;
				}
				
				long timestart4 = System.currentTimeMillis();
				
				//Check for mempool coins..
				if(TxPoWChecker.checkMemPoolCoins(txpow)) {
					//Same coins in different transaction - could have been requested by us from branch
					//SelfLogger.log("TxPoW with existing mempoolcoins from client : "+mClientUID+" "+txpow.getTxPoWID());
					fullyvalid = false;
				}
	
				long timestart5 = System.currentTimeMillis();
				
				//Check the MMR - could be in a separate branch / or a future txn..
				if(!TxPoWChecker.checkMMR(tip.getMMR(), txpow, false)) {
					fullyvalid = false;
				}
				
				long timestart6 = System.currentTimeMillis();
				
				//Is the MEMPOOL Full
				if(TxPoWGenerator.isMempoolFull()) {
					
					//What is the Burn
					MiniNumber burn = txpow.getBurn();
					
					//Check the Burn
					if(burn.isLessEqual(TxPoWGenerator.getMinMempoolBurn())) {
						SelfLogger.log("Received TxPoW with low burn when MEMPOOL full "+burn);
						fullyvalid=false;
					}
				}
				
				//How long did all that take..
				long timefinish = System.currentTimeMillis();
				long timediff 	= timefinish - timestart;
				if(timediff > 20000) {
					SelfLogger.log("Message took a long time ("+timediff+"ms) to process @ txpowid:"+txpow.getTxPoWID());
//					SelfLogger.log("timerstart1:"+(timestart1-timestart));
//					SelfLogger.log("timerstart2:"+(timestart2-timestart1));
//					SelfLogger.log("timerstart3:"+(timestart3-timestart2));
//					SelfLogger.log("timerstart4:"+(timestart4-timestart3));
//					SelfLogger.log("timerstart5:"+(timestart5-timestart4));
//					SelfLogger.log("timerstart6:"+(timestart6-timestart5));
					
					fullyvalid = false;
				}
				
				//Ok - let's add to our database and process..
				SELFSystem.getInstance().getTxPoWProcessor().postProcessTxPoW(txpow);
				
				//ONLY if it's FULLY OK.. forward the TxPoWID to the rest of the network..
				if(fullyvalid) {
					//Forward to the network
					NIOManager.sendNetworkMessageAll(MSG_TXPOWID, txpow.getTxPoWIDData());
				}
				
				//Check all the Transactions.. if it's a block
				if(!GeneralParams.TXBLOCK_NODE && txpow.isBlock() && !beforecascade) {
					ArrayList<MiniData> txns = txpow.getBlockTransactions();
					for(MiniData txn : txns) {
						exists = SelfDB.getDB().getTxPoWDB().exists(txn.to0xString());
						if(!exists) {
							//request it.. with a slight delay - as may be in process stack
							NIOManager.sendNetworkMessage(mClientUID, MSG_TXPOWREQ, txn);
						}
					}
					
					//Scan through all the blocks to see if we have everything..
					TxPoWDB txpdb 				= SelfDB.getDB().getTxPoWDB();
					TxPoW current 				= txpow;
					
					int counter = 0;
					while(counter<512) {
						
						//What height are we at
						if(current.getBlockNumber().isLessEqual(cascadeblock)) {
							//Far enough
							break;
						}
						
						//What is the parent
						MiniData parentid = current.getParentID();
						
						//Is this onchain already
						TxPoWTreeNode node = TxPoWSearcher.searchChainForTxPoWBlock(parentid);
						if(node!=null) {
							//we'll search the tree next
							break;
						}
						
						//Get the parent
						TxPoW parent = txpdb.getTxPoW(current.getParentID().to0xString());
						if(parent == null) {
							//Send a message for it and break..
							NIOManager.sendNetworkMessage(mClientUID, MSG_TXPOWREQ, current.getParentID());
							break;
						}
						
						//Check all the transactions in the block..
						ArrayList<MiniData> ptxns = parent.getBlockTransactions();
						for(MiniData txn : ptxns) {
							exists = SelfDB.getDB().getTxPoWDB().exists(txn.to0xString());
							if(!exists) {
								//request it.. 
								NIOManager.sendNetworkMessage(mClientUID, MSG_TXPOWREQ, txn);
							}
						}
						
						//And make the parent current
						current = parent;
						counter++;
					}
					
					if(mClientUID.equals("0x00") || mClientUID.equals("0x01")) {
						//Internal message.. no chain sync..
						return;
					}
					
					//Now scan the whole tree - unless you already have per block
					Long lastreq = mLastChainSync.get(mClientUID);
					if(lastreq == null) {
						lastreq = Long.valueOf(0);
					}
					long lasttime 		= lastreq.longValue();
					long reqtimenow  	= System.currentTimeMillis();
					long reqtimediff 	= reqtimenow - lasttime;
					if(reqtimediff < 1000 * 60 * 10) {
						return;
					}
					mLastChainSync.put(mClientUID, Long.valueOf(reqtimenow));
					
					counter = 0;
					TxPoWTreeNode tipblock = SelfDB.getDB().getTxPoWTree().getTip();
					while(tipblock != null && counter<256) {
						
						//Only scan 256 blocks..
						counter++;
						
						//Do we have all the txns in this block
						boolean haveall = tipblock.checkFullTxns(txpdb);
						
						if(!haveall) {
							ArrayList<MiniData> ptxns = tipblock.getTxPoW().getBlockTransactions();
							for(MiniData txn : ptxns) {
								exists = SelfDB.getDB().getTxPoWDB().exists(txn.to0xString());
								if(!exists) {
									//request it.. 
									NIOManager.sendNetworkMessage(mClientUID, MSG_TXPOWREQ, txn);
								}
							}
						}
						
						//Get the parent
						tipblock = tipblock.getParent();
					}
				}
				
			}else if(type.isEqual(MSG_GENMESSAGE)) {
				//Read in the message
				MiniString msg = MiniString.ReadFromStream(dis);
				
				//For now..
				SelfLogger.log(mClientUID+":"+msg.toString());
			
			}else if(type.isEqual(MSG_PING)) {
				//Read in a txpow unit.. currently does nothing.. could be 1000's of connections..
				MiniData txpowid = MiniData.ReadFromStream(dis);
			
			}else if(type.isEqual(MSG_P2P)) {
				
				//P2P message..
				MiniString msg = MiniString.ReadFromStream(dis);
				
				//Should not be receiving these..
				if(!GeneralParams.P2P_ENABLED) {
					return;
				}
				
				//Get the Client
				NIOClient nioclient = SELFSystem.getInstance().getNIOManager().getNIOServer().getClient(mClientUID);
				if(nioclient == null) {
					SelfLogger.log(mClientUID+" Error null client on P2P NIOMessage..");
					return;
				}
				NIOClientInfo clientInfo = new NIOClientInfo(nioclient, true);
				
				//Convert to JSON
				JSONObject json = (JSONObject) new JSONParser().parse(msg.toString());
				
				//Have we received a p2p greeting..?
				P2PManager p2pmanager = (P2PManager)SELFSystem.getInstance().getNetworkManager().getP2PManager();
				
				if(!nioclient.hasReceivedP2PGreeting()) {
//					SelfLogger.log("RECEIVED P2P MSG BEFORE GREETING.. DELAYING BY 10s.. "+json.toJSONString());
					
					//Post with delay
					TimerMessage p2p = new TimerMessage(10000, P2PFunctions.P2P_MESSAGE);
					p2p.addString("uid", mClientUID);
					p2p.addObject("message", json);
					p2pmanager.PostTimerMessage(p2p);
					
				}else {
					//Post directly
					Message p2p = new Message(P2PFunctions.P2P_MESSAGE);
					p2p.addString("uid", mClientUID);
					p2p.addObject("message", json);
					p2pmanager.PostMessage(p2p);
					
				}
				
			}else if(type.isEqual(MSG_PULSE)) {
				
				//Read in the Pulse..
				Pulse pulse 	= Pulse.ReadFromStream(dis);
				TxPoWDB txpdb 	= SelfDB.getDB().getTxPoWDB();
				
				//Now check this list against your ownn..
				ArrayList<MiniData> mylist 		= SelfDB.getDB().getTxPoWTree().getPulseList();
				ArrayList<MiniData> requestlist = new ArrayList<>();
				
				HashSet<String> fullist = new HashSet<>();
				for(MiniData block : mylist) {
					fullist.add(block.to0xString());
				}
				
				long timestart = System.currentTimeMillis();
				
				//Now check for intersection
				boolean found = false;
				ArrayList<MiniData> pulsemsg = pulse.getBlockList();
				
				//Check within limits..
				if(pulsemsg.size()>1000) {
					//Too many..!
					SelfLogger.log("Too many messages in PULSE "+pulsemsg.size()+" max:1000");
					return;
				}
				
				int counter=0;
				for(MiniData block : pulsemsg) {
					
					//Is it one of ours already
					TxPoWTreeNode node = TxPoWSearcher.searchChainForTxPoWBlock(block);
					if(node!=null) {
						found = true;
						//We search this every block
						break;
					}
					
					counter++;
					String blockstr = block.to0xString();
					
					//Do we have the block
					TxPoW check = txpdb.getTxPoW(blockstr);
					if(check == null) {
						//Ask for it
						requestlist.add(0, block);
					}else {
						if(!GeneralParams.TXBLOCK_NODE) {
							//Check all the transactions..
							ArrayList<MiniData> txns = check.getBlockTransactions();
							for(MiniData txn : txns) {
								if(!txpdb.exists(txn.to0xString())) {
									requestlist.add(0, txn);
								}
							}
						}
					}
					
					//Is there a crossover
					if(fullist.contains(blockstr)) {
						found = true;
						//break;
					}
				}
				
				long timediff = System.currentTimeMillis() - timestart;
				if(counter>0) {
					SelfLogger.log("PULSE("+counter+"/"+pulsemsg.size()+") from:"+mClientUID+" TIME:"+timediff+"ms req:"+requestlist.size()+" crossover:"+found);
				}
				
				//Did we find a crossover..
				if(found) {
					if(!GeneralParams.TXBLOCK_NODE) {
					
						//Request all the blocks.. in the correct order
						for(MiniData block : requestlist) {
							NIOManager.sendNetworkMessage(mClientUID, MSG_TXPOWREQ, block);
						}
					}else {
						
						//Request all the blocks.. in the correct order
						for(MiniData block : requestlist) {
							NIOManager.sendNetworkMessage(mClientUID, MSG_TXBLOCKREQ, block);
						}
					}
					
				}else{
					//Remove from our list
					if(!mFullAdrress.equals("")) {
						P2PFunctions.addInvalidPeer(mFullAdrress);
					}else {
						
					}
					
					NIOClient nioclient = SELFSystem.getInstance().getNIOManager().getNIOServer().getClient(mClientUID);
					if(nioclient == null) {
						//No client - already disconnected..
						SELFSystem.getInstance().getNIOManager().disconnect(mClientUID, true);
						return;
					}
					
					int port = nioclient.getPort();
					if (nioclient.getSelfPort() == -1){
						port = nioclient.getSelfPort();
					}
					
					//Hmm something funny..
					SelfLogger.log("[!] No Crossover found whilst syncing with new node. They are on a different chain. Please check you are on the correct chain.. disconnecting from "+ nioclient.getHost() + ":" + port);
					
					//Make it invalid.
					P2PFunctions.addInvalidPeer(nioclient.getFullAddress());
					
					SELFSystem.getInstance().getNIOManager().disconnect(mClientUID, true);
				}
				
			}else if(type.isEqual(MSG_MAXIMA_CTRL)) {
				
				//Make sure acceptable length
				if(mData.getLength() > 65535) {
					SelfLogger.log("Maxima CTRL message too Large! from "+mClientUID);
					return;
				}
				
				//Get the message
				MaximaCTRLMessage msg = MaximaCTRLMessage.ReadFromStream(dis);
				
				//Get the client
				NIOClient nioclient = SELFSystem.getInstance().getNIOManager().getNIOServer().getClient(mClientUID);
				
				//Check not null
				if(nioclient != null) {
					
					//And post it to the Maxima Manager..
					Message maxmsg = new Message(MaximaManager.MAXIMA_CTRLMESSAGE);
					maxmsg.addObject("nioclient", nioclient);
					maxmsg.addObject("maximactrl", msg);
					
					SELFSystem.getInstance().getMaxima().PostMessage(maxmsg);
				}
				
			}else if(type.isEqual(MSG_MAXIMA_TXPOW)) {
				
				//Convert to a MaxTxPOW
				MaxTxPoW sxtxpow = MaxTxPoW.ReadFromStream(dis);
				
				//Check the TxPoW unit is correct
				if(!sxtxpow.checkValidTxPoW()) {
					SelfLogger.log("Invalid Maxima message : Incorrect TxPoW Hash from "+mClientUID);

					//Tell them it's a fail!
					NIOManager.sendNetworkMessage(mClientUID, MSG_PING, MaximaManager.MAXIMA_WRONGHASH);
					
					return;
				}
								
				//How large is the Maxima Package
				MiniData mp = MiniData.getMiniDataVersion(sxtxpow.getMaximaPackage());
				
				//Make sure acceptable length - 256K
				if(mp.getLength() > 262144) {
					SelfLogger.log("Maxima message too Large! from "+mClientUID+" "+MiniFormat.formatSize(mp.getLength()));
					
					//Tell them it's a fail!
					NIOManager.sendNetworkMessage(mClientUID, MSG_PING, MaximaManager.MAXIMA_TOOBIG);
					
					return;
				}
				
				//Get the client
				NIOClient nioclient = SELFSystem.getInstance().getNIOManager().getNIOServer().getClient(mClientUID);
				
				//Are we still connected..
				if(nioclient == null) {
					//Already disconnected
					return;
				}
				
				//And send it on to Maxima..
				Message maxmsg = new Message(MaximaManager.MAXIMA_RECMESSAGE);
				maxmsg.addObject("nioclient", nioclient);
				maxmsg.addObject("maxtxpow", sxtxpow);
				
				//Send to the Maxima Manager
				SELFSystem.getInstance().getMaxima().PostMessage(maxmsg);
				
				//Is it a block or a transaction..
				TxPoW txpow = sxtxpow.getTxPoW();
				if(txpow.isTransaction() || txpow.isBlock()) {
					
					//And Now post the TxPoW on the stack..
					MiniData niodata = NIOManager.createNIOMessage(NIOMessage.MSG_TXPOW, txpow);

					//And post on out stack
					Message newniomsg = new Message(NIOManager.NIO_INCOMINGMSG);
					newniomsg.addString("uid", "0x01");
					newniomsg.addObject("data", niodata);

					//Post to the NIOManager - which will check it and forward if correct
					SELFSystem.getInstance().getNetworkManager().getNIOManager().PostMessage(newniomsg);
				}
				
			}else if(type.isEqual(MSG_SINGLE_PING)) {
				
				//Get the Data Object..
				MiniData datapacket = MiniData.ReadFromStream(dis);
				
				//Now send some useful info back 
				Greeting pinggreet = new Greeting();
				pinggreet.getExtraData().put("welcome", "hi there!");
				
				TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
				
				if(tip != null) {
					pinggreet.getExtraData().put("topblock", tip.getBlockNumber().toString());
					pinggreet.getExtraData().put("tophash", tip.getTxPoW().getTxPoWID());
					
					TxPoWTreeNode tip50 = tip.getParent(100);
					pinggreet.getExtraData().put("50block", tip50.getBlockNumber().toString());
					pinggreet.getExtraData().put("50hash", tip50.getTxPoW().getTxPoWID());
				}else {
					pinggreet.getExtraData().put("topblock", "0");
					pinggreet.getExtraData().put("tophash", "0x00");
					pinggreet.getExtraData().put("50block", "0");
					pinggreet.getExtraData().put("50hash", "0x00");
				}
				
				//Add the NUMBER of connections..
				pinggreet.getExtraData().put("connections",SELFSystem.getInstance().getNIOManager().getAllConnectedDetails());
				
				//Add the Peers List! of P2P2..
				if(GeneralParams.P2P2_ENABLED) {
					
					//Add the Peers List
					pinggreet.getExtraData().put("peers-list", SelfDB.getDB().getP2P2DB().getAllKnownPeers());
					
				}else if(GeneralParams.P2P_ENABLED) {
					
					//Get the peers list
					P2PManager p2PManager 	= (P2PManager) SELFSystem.getInstance().getNetworkManager().getP2PManager();
					JSONArray peers 		= InetSocketAddressIO.addressesListToJSONArray(p2PManager.getPeersCopy());
					pinggreet.getExtraData().put("peers-list", peers);
					pinggreet.getExtraData().put("clients", p2PManager.getClients());
					
				}else {
					//No peers..
					pinggreet.getExtraData().put("peers-list", new JSONArray());
					pinggreet.getExtraData().put("clients", 0);
				}
				
				//Send this back to them.. 
				NIOManager.sendNetworkMessage(mClientUID, MSG_SINGLE_PONG, pinggreet);
			
			}else if(type.isEqual(MSG_IBD_REQ)) {
				
				//Get the Hash of the Block
				TxPoW lastblock = TxPoW.ReadFromStream(dis);
				
				//And post this on..
				//SelfLogger.log("[+] Received Sync IBD Request from "+mClientUID+" @ "+lastblock.getBlockNumber());
				
				//Are we limiting this..
				if(GeneralParams.ARCHIVESYNC_LIMIT_BANDWIDTH) {
					
					//How much have we used..
					long total 		= SELFSystem.getInstance().getNIOManager().getTrafficListener().getTotalWrite();
					String current 	= MiniFormat.formatSize(total);
					
					if(total > NIOManager.MAX_ARCHIVE_WRITE) {
						SelfLogger.log("MAX Bandwith used already ("+current+") - no more archive sync for 24hours..");
						return;
					}
				}
				
				//What was the last request from this user
				MiniNumber lastreq = mlastSyncReq.get(mClientUID);
				if(lastreq != null) {
					if(lastreq.isEqual(lastblock.getBlockNumber())) {
						SelfLogger.log("[+] Received SAME Sync IBD Request from "+mClientUID+" @ "+lastblock.getBlockNumber()+" IGNORING");
						return;
					}
				}
				mlastSyncReq.put(mClientUID, lastblock.getBlockNumber());
				
				//Create an IBD of the blocks we hjave before this one..
				IBD syncibd = new IBD();
				syncibd.createSyncIBD(lastblock);
				
				//And send it..
				NIOManager.sendNetworkMessage(mClientUID, MSG_IBD_RESP, syncibd);
				
			}else if(type.isEqual(MSG_IBD_RESP)) {
				
				//Load the IBD..
				IBD syncibd = IBD.ReadFromStream(dis);
				
				//Are there any blocks..
				if(syncibd.getTxBlocks().size() > 0) {
				
					//Top block
					MiniNumber top = syncibd.getTxBlocks().get(0).getTxPoW().getBlockNumber(); 
					
					//And post this on..
					if(GeneralParams.IBDSYNC_LOGS) {
						long timemilli 		= syncibd.getTxBlocks().get(0).getTxPoW().getTimeMilli().getAsLong();
						String synctoptime 	= new Date(timemilli).toString();
						SelfLogger.log("[+] Received Sync IBD. size:"+MiniFormat.formatSize(data.length)+" blocks:"+syncibd.getTxBlocks().size()+" top:"+top+" @ "+synctoptime);
					}
					
					//Send to the Processor
					SELFSystem.getInstance().getTxPoWProcessor().postProcessSyncIBD(syncibd, mClientUID);
				}
			
			}else if(type.isEqual(MSG_ARCHIVE_REQ)) {
				
				//What block are we starting from..
				MiniNumber firstblock 	= MiniNumber.ReadFromStream(dis);
				
				IBD ibd = new IBD();
				
				//Is this a test connect
				if(firstblock.isEqual(MiniNumber.MINUSONE)) {
					SelfLogger.log("Archive IBD connection test..");
					
					//Send it.. empty just testing the connection
					NIOManager.sendNetworkMessage(mClientUID, MSG_ARCHIVE_DATA, ibd);
					
				}else {
					SelfLogger.log("Archive IBD request start @ "+firstblock);
					ibd.createArchiveIBD(firstblock);
					
					//Send it..
					NIOManager.sendNetworkMessage(mClientUID, MSG_ARCHIVE_DATA, ibd);
				}
			
			}else if(type.isEqual(MSG_ARCHIVE_SINGLE_REQ)) {
				
//				//Do we support archive data
//				if(!SelfDB.getDB().getArchive().isStoreMySQL()) {
//					SelfLogger.log("Archive single request we do not saupport.. from "+mClientUID);
//					return;
//				}
//				
//				//What block do they want
//				MiniNumber blocknum 	= MiniNumber.ReadFromStream(dis);
//				
//				//Get that block
//				TxBlock block = SelfDB.getDB().getArchive().getMySQLCOnnect().loadBlockFromNum(blocknum.getAsLong());
//				if(block != null) {
//					//Send it to them..
//					NIOManager.sendNetworkMessage(mClientUID, MSG_ARCHIVE_DATA, block);
//				}
				
			}else if(type.isEqual(MSG_ARCHIVE_DATA)) {
			
				//Messages are handled in the archive command - not here yet
				SelfLogger.log("Received MSG_ARCHIVE_DATA msg.. ignoring.. from "+mClientUID);
				
//				//It's an IBD structure
//				IBD archibd = IBD.ReadFromStream(dis);
//				
//				//Send this to the main processor
//				SELFSystem.getInstance().getTxPoWProcessor().postProcessArchiveIBD(archibd, mClientUID);
								
			
			}else if(type.isEqual(MSG_TXBLOCKID)) {
				
				//Are we running this type of node..
				if(!GeneralParams.TXBLOCK_NODE) {
					return;
				}
				
				//Read in the txpowid
				MiniData txpowid = MiniData.ReadFromStream(dis);
				String txid 	 = txpowid.to0xString();
				
				//Do we have it..
				TxBlock txb = SelfDB.getDB().getTxBlockDB().findTxBlock(txid);
				
				//Do we have it in TxPoWTree
				if(txb == null) {
					TxPoWTreeNode node = SelfDB.getDB().getTxPoWTree().findNode(txid);
					if(node!=null) {
						txb = node.getTxBlock();
					}
				}
				
				//If not request it..
				if(txb==null) {
					//request it..
					NIOManager.sendNetworkMessage(mClientUID, MSG_TXBLOCKREQ, txpowid);
				} 
			
			}else if(type.isEqual(MSG_TXBLOCKREQ)) {
				
				//Read in the txpowid
				MiniData txpowid 	= MiniData.ReadFromStream(dis);
				String txid 		= txpowid.to0xString();
				
				//Do we have it.. in RAM DB
				TxBlock txb = SelfDB.getDB().getTxBlockDB().findTxBlock(txid);
				
				//Do we have it in TxPoWTree
				if(txb == null) {
					TxPoWTreeNode node = SelfDB.getDB().getTxPoWTree().findNode(txid);
					if(node!=null) {
						txb = node.getTxBlock();
					}
				}

				//Do we have it in Archive..
				if(txb == null) {
					txb = SelfDB.getDB().getArchive().loadBlock(txid);
				}
				
				//Send it to them
				if(txb!=null) {
					NIOManager.sendNetworkMessage(mClientUID, MSG_TXBLOCK, txb);
				}else {
					SelfLogger.log("Request for TxBlock we don't have : "+txpowid.to0xString());
				}
			
			}else if(type.isEqual(MSG_TXBLOCK)) {
				
				//Are we running this type of node..
				if(!GeneralParams.TXBLOCK_NODE) {
					return;
				}
				
				//Get the TxBlock
				TxBlock txblock = TxBlock.ReadFromStream(dis);
				
				//And process..
				SELFSystem.getInstance().getTxPoWProcessor().postProcessTxBlock(txblock);
				
				//Is the parent above the cascade.
				TxPoWTreeNode cascade = SelfDB.getDB().getTxPoWTree().getRoot();
				if(txblock.getTxPoW().getBlockNumber().isMoreEqual(cascade.getBlockNumber())) {
				
					//Do we have the parent..
					MiniData parent = txblock.getTxPoW().getParentID();
					String txid 	= parent.to0xString();
				
					//Do we have it.. in RAM DB
					TxBlock txb = SelfDB.getDB().getTxBlockDB().findTxBlock(txid);
					
					//Do we have it in TxPoWTree
					if(txb == null) {
						TxPoWTreeNode node = SelfDB.getDB().getTxPoWTree().findNode(txid);
						if(node!=null) {
							txb = node.getTxBlock();
						}
					}
					
					//If not request it..
					if(txb==null) {
						//request it..
						SelfLogger.log("Request Parent TxBlock.. @ "+txblock.getTxPoW().getBlockNumber());
						NIOManager.sendNetworkMessage(mClientUID, MSG_TXBLOCKREQ, parent);
					}
				}
				
			}else if(type.isEqual(MSG_TXBLOCKMINE)) {
				
				//Are we running this type of node..
				if(!GeneralParams.TXBLOCK_NODE) {
					return;
				}
				
				//Get the unmined txpow
				TxPoW txp = TxPoW.ReadFromStream(dis);
				
				//Reset the RandomID - so everyone mines a different block
				txp.getTxBody().resetRandomPRNG();
				
				//When was the last mine message rec 
				long timenow 	= System.currentTimeMillis();
				long timediff 	= timenow - LAST_TXBLOCKMINE_MSG;
				if(timediff < SELFSystem.getInstance().AUTOMINE_TIMER) {
					//Not enough time has passed..
					//SelfLogger.log("DON'T MINE - too soon");
					return;
				}
				
				//Set the last Mine Time..
				LAST_TXBLOCKMINE_MSG = timenow;
				
				//Mine it..
				SELFSystem.getInstance().getTxPoWMiner().mineTxPoWAsync(txp);
				
			}else if(type.isEqual(MSG_MEGAMMRSYNC_REQ)) {
				
				if(!GeneralParams.IS_MEGAMMR) {
					SelfLogger.log("[!] Attempt to MegaMMR Sync when -megammr not enabled");
					SELFSystem.getInstance().getNIOManager().disconnect(mClientUID);
					return;
				}
				
				MegaMMRSyncData msyncdata = new MegaMMRSyncData();
				msyncdata.readDataStream(dis);
				
				//Now use that sync data to get all the coinproofs
				SelfLogger.log("Received MegaMMR SYNC request.. addresses:"
									+msyncdata.getAllAddresses().size()
									+" pubkeys:"+msyncdata.getAllPublicKeys().size());
				
				//Create the IBD complete sync package
				MegaMMRIBD mibd = megammrsync.getCurrentMegaMMRIBD(msyncdata);
				
//				SelfLogger.log("LONG DELAY NOW..");
//				Thread.sleep(20000);
				
				//And send it back
				NIOManager.sendNetworkMessage(mClientUID, MSG_MEGAMMRSYNC_RESP, mibd);
				
			}else {
				
				//UNKNOWN MESSAGE..
				SelfLogger.log("Unknown Message type received from "+mClientUID+" type:"+type+" size:"+data.length);
			}
			
		} catch (Exception e) {
			SelfLogger.log(e);
			
		} finally {
			
			//Close the streams..
			if(dis!=null) {
				try {dis.close();} catch (IOException e) {}
			}
			
			if(bais!=null) {
				try {bais.close();} catch (IOException e) {}
			}
			
			//And blank this..
			mData = null;
		}
	}
}
