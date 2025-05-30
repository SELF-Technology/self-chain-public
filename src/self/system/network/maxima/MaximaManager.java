package org.self.system.network.maxima;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Random;

import org.self.database.SelfDB;
import org.self.database.maxima.MaximaContact;
import org.self.database.maxima.MaximaDB;
import org.self.database.maxima.MaximaHost;
import org.self.database.userprefs.UserDB;
import org.self.objects.Address;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.objects.base.MiniString;
import org.self.system.SELFSystem;
import org.self.system.commands.maxima.maxima;
import org.self.system.network.maxima.message.MaxTxPoW;
import org.self.system.network.maxima.message.MaximaErrorMsg;
import org.self.system.network.maxima.message.MaximaInternal;
import org.self.system.network.maxima.message.MaximaMessage;
import org.self.system.network.maxima.message.MaximaPackage;
import org.self.system.network.maxima.mls.MLSPacketGETReq;
import org.self.system.network.maxima.mls.MLSPacketGETResp;
import org.self.system.network.maxima.mls.MLSPacketSET;
import org.self.system.network.maxima.mls.MLSService;
import org.self.system.network.self.NIOClient;
import org.self.system.network.self.NIOManager;
import org.self.system.network.self.NIOMessage;
import org.self.system.network.p2p.P2PManager;
import org.self.system.params.GeneralParams;
import org.self.utils.Crypto;
import org.self.utils.SelfLogger;
import org.self.utils.encrypt.CryptoPackage;
import org.self.utils.encrypt.GenerateKey;
import org.self.utils.encrypt.SignVerify;
import org.self.utils.json.JSONObject;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageProcessor;
import org.self.utils.messages.TimerMessage;

public class MaximaManager extends MessageProcessor {

	/**
	 * Maxima Messages
	 */
	public static final String MAXIMA_INIT 			= "MAXIMA_INIT";
	
	/**
	 * Network Messages
	 */
	public static final String MAXIMA_CONNECTED 	= "MAXIMA_CONNECTED";
	public static final String MAXIMA_DISCONNECTED 	= "MAXIMA_DISCONNECTED";
	
	/**
	 * Checker loop function - every 20 mins
	 */
	public static final String MAXIMA_LOOP 			= "MAXIMA_LOOP";
	long MAXIMA_LOOP_DELAY = 1000 * 60 * 20;
	
	/**
	 * Messages
	 */
	public static final String MAXIMA_CTRLMESSAGE 		= "MAXIMA_CTRLMESSAGE";
	public static final String MAXIMA_RECMESSAGE 		= "MAXIMA_RECMESSAGE";
	public static final String MAXIMA_SENDMESSAGE 		= "MAXIMA_SENDDMESSAGE";
	public static final String MAXIMA_REFRESH 			= "MAXIMA_REFRESH";
	public static final String MAXIMA_MLSGET_RESP 		= "MAXIMA_GETREQ";
	public static final String MAXIMA_CHECK_CONNECTED 	= "MAXIMA_CHECK_CONNECTED";
	public static final String MAXIMA_CHECK_MLS 		= "MAXIMA_CHECK_MLS";
	public static final String MAXIMA_REFRESH_TIMER 	= "MAXIMA_REFRESH_TIMER";
	
	/**
	 * Send a message to CHECK Maxima is working on connect
	 */
	public static final String MAXIMA_SENDCHKCONNECT 	= "MAXIMA_SENDCHKCONNECT";
	public static final String MAXIMA_CHKCONECT_APP 	= "**maxima_check_connect**";
	
	/**
	 * Maxima Location Service Message
	 */
	public static final String MAXIMA_MLS_SETAPP 	= "**maxima_mls_set**";
	public static final String MAXIMA_MLS_GETAPP 	= "**maxima_mls_get**";
	public static String MLS_RANDOM_UID 			= "";
	
	/**
	 * UserDB data
	 */
	private static final String MAXIMA_PUBKEY 		= "maxima_publickey";
	private static final String MAXIMA_PRIVKEY 		= "maxima_privatekey";
	
	private static final String MAXIMA_MLSPUBKEY 	= "maxima_mlspublickey";
	private static final String MAXIMA_MLSPRIVKEY 	= "maxima_mlsprivatekey";
	private static final String MAXIMA_MLSHOST 		= "maxima_mlshost";
	private static final String MAXIMA_MLSTIME 		= "maxima_mlstime";
	private static final String MAXIMA_OLDMLSHOST 	= "maxima_oldmlshost";
	
	private static final String MAXIMA_ISSTATICMLS 	= "maxima_isstaticmls";
	private static final String MAXIMA_STATICMLS 	= "maxima_staticmls";
	
	/**
	 * The Response message for a Maxima Message
	 */
	public static final MiniData MAXIMA_FAIL 		= new MiniData("0x00");
	public static final MiniData MAXIMA_OK 			= new MiniData("0x01");
	public static final MiniData MAXIMA_UNKNOWN 	= new MiniData("0x02");
	public static final MiniData MAXIMA_TOOBIG 		= new MiniData("0x03");
	public static final MiniData MAXIMA_WRONGHASH 	= new MiniData("0x04");
	
	public static final MiniData MAXIMA_RESPONSE_FAIL 		= new MiniData("0x080000000100");
	public static final MiniData MAXIMA_RESPONSE_OK 		= new MiniData("0x080000000101");
	public static final MiniData MAXIMA_RESPONSE_UNKNOWN 	= new MiniData("0x080000000102");
	public static final MiniData MAXIMA_RESPONSE_TOOBIG 	= new MiniData("0x080000000103");
	public static final MiniData MAXIMA_RESPONSE_WRONGHASH 	= new MiniData("0x080000000104");
	
	/**
	 * RSA Keys
	 */
	MiniData mPublic;
	MiniData mPrivate;
	String mMaximaAddress;
	
	/**
	 * MLS Keys
	 */
	MLSService mMLSService = new MLSService();
	MiniData mMLSPublic;
	MiniData mMLSPrivate;
	String mMaximaMLSAddress;
	
	/**
	 * Permanent Maxima address Users..
	 */
	public ArrayList<String> mPermanentMaxima = new ArrayList<>();
	
	/**
	 * Are we Inited
	 */
	private boolean mInited 	= false;

	/**
	 * Are you a static Maxima ID
	 */
	boolean mStaticMaxima  = false;
	String mStaticMaximaID = "";
	
	/**
	 * The Contacts Manager
	 */
	MaximaContactManager mMaxContacts;
	
	/**
	 * Message Sender
	 */
	MaxMsgHandler mMaxSender;
	
	/**
	 * Are we using a STATIC MLS
	 */
	boolean mIsStaticMLS 	= false;
	String mStaticMLS 		= "";
	
	/**
	 * Main Constructor
	 */
	public MaximaManager() {
		super("MAXIMA");
		
		mMaxSender 		= new MaxMsgHandler(this);
		mMaxContacts 	= new MaximaContactManager(this);
		
		PostMessage(MAXIMA_INIT);
	}
	
	public void shutdown() {
		mMaxContacts.stopMessageProcessor();
		
		mMaxSender.stopMessageProcessor();
		
		stopMessageProcessor();
	}
	
	public boolean isInited() {
		return mInited;
	}
	
	public MaxMsgHandler getMaxSender() {
		return mMaxSender;
	}
	
	/**
	 * Sometime the message stack can grow too much
	 */
	public void checkPollMessages() {
		if(getMaxSender().getSize()>256) {
			SelfLogger.log("Maxima POLL stack > 256.. clearing.. ");
			getMaxSender().clear();
		}
	}
	
	public String getMaximaIdentity() {
		return mMaximaAddress;
	}
	
	public String getMaximaMLSIdentity() {
		return mMaximaMLSAddress;
	}
	
	public MiniData getPublicKey() {
		return mPublic;
	}
	
	public MiniData getPrivateKey() {
		return mPrivate;
	}
	
	public MaximaContactManager getContactsManager() {
		return mMaxContacts;
	}
	
	public String getCurrentHostIP() {
		if(GeneralParams.P2P_ENABLED && !GeneralParams.IS_HOST_SET) {
			return ((P2PManager)(SELFSystem.getInstance().getNetworkManager().getP2PManager())).getP2PAddress();
		}
		
		return GeneralParams.SELF_HOST+":"+GeneralParams.SELF_PORT;
	}
	
	public String getLocalMaximaAddress(boolean zP2P) {
		
		///Regular host
		String host = GeneralParams.SELF_HOST+":"+GeneralParams.SELF_PORT;
		
		//What the P2P thinks you are
		if(zP2P && GeneralParams.P2P_ENABLED && !GeneralParams.IS_HOST_SET) {
			host = ((P2PManager)(SELFSystem.getInstance().getNetworkManager().getP2PManager())).getP2PAddress();
		}
		
		return getMaximaMLSIdentity()+"@"+host;
	}
	
	public boolean isStaticMLS() {
		return mIsStaticMLS;
	}
	
	public void setStaticMLS(boolean zStatic, String zStaticAddress) {
		mIsStaticMLS 	= zStatic;
		mStaticMLS 		= zStaticAddress;
		
		//Get the UserDB
		UserDB udb = SelfDB.getDB().getUserDB();
		udb.setBoolean(MAXIMA_ISSTATICMLS, mIsStaticMLS);
		udb.setString(MAXIMA_STATICMLS, mStaticMLS);
		
		//Save this..
		SelfDB.getDB().saveUserDB();
		
		if(zStatic) {
			SelfLogger.log("Enable STATIC MLS to host : "+zStaticAddress);
		}else {
			SelfLogger.log("Disable STATIC MLS");
		}
	}
	
	private void savePermanentUDB() {
		SelfDB.getDB().getUserDB().setMaximaPermanent(mPermanentMaxima);
		SelfDB.getDB().saveUserDB();
	}
	
	public ArrayList<String> getAllPermanent(){
		return mPermanentMaxima;
	}
	
	public void addPermanentMaxima(String zPublicKey) {
		SelfLogger.log("Permanent Maxima Publickey Added! : "+zPublicKey);
		if(!mPermanentMaxima.contains(zPublicKey)) {
			mPermanentMaxima.add(zPublicKey);
			savePermanentUDB();
		}
	}
	
	public void removePermanentMaxima(String zPublicKey) {
		if(mPermanentMaxima.contains(zPublicKey)) {
			mPermanentMaxima.remove(zPublicKey);
			savePermanentUDB();
		}
	}
	
	public void clearPermanentMaxima() {
		mPermanentMaxima.clear();
		savePermanentUDB();
	}
	
	public MLSService getMLSService() {
		return mMLSService;
	}
	
	public String getMLSHost() {
		if(mIsStaticMLS) {
			return mStaticMLS;
		}
		
		String mls = mMLSService.getMLSServer();
		if(mls.equals("")) {
			return mMaximaMLSAddress+"@"+GeneralParams.SELF_HOST+":"+GeneralParams.SELF_PORT;
		}
		
		return mls;
	}
	
	public String getOldMLSHost() {
		if(mIsStaticMLS) {
			return mStaticMLS;
		}
		
		String mls = mMLSService.getOldMLSServer();
		if(mls.equals("")) {
			return mMaximaMLSAddress+"@"+GeneralParams.SELF_HOST+":"+GeneralParams.SELF_PORT;
		}
		
		return mls;
	}
	
	public ArrayList<MaximaHost> getAllConnectedHosts(){
		
		//Get all the current hosts
		ArrayList<MaximaHost> hosts = SelfDB.getDB().getMaximaDB().getAllHosts();
		
		//Only the connected ones..
		ArrayList<MaximaHost> connctedhosts = new ArrayList<>();
		for(MaximaHost host : hosts) {
			if(host.isConnected()) {
				connctedhosts.add(host);
			}
		}
		
		return connctedhosts;
	}
	
	public String getRandomMaximaAddress() {

		//Are we a static address..
		if(mStaticMaxima) {
			return mMaximaMLSAddress+"@"+mStaticMaximaID;
		}
		
		//Who are we connected to
		ArrayList<MaximaHost> connctedhosts = getAllConnectedHosts();
		
		//Are there any..
		if(connctedhosts.size() == 0) {
			return getLocalMaximaAddress(true);
		}
		
		return connctedhosts.get(new Random().nextInt(connctedhosts.size())).getMaximaAddress();
	}

	public void setStaticAddress(boolean zStatic, String zAddress) {
		mStaticMaxima 	= zStatic;
		mStaticMaximaID = zAddress;
	}
	
	public MaximaMessage createMaximaMessage(String zTo, String zApplication, MiniData zData) {
		MaximaMessage maxima 	= new MaximaMessage();
		
		maxima.mFrom 			= getPublicKey();
		maxima.mTo 				= new MiniData(zTo);
		maxima.mApplication 	= new MiniString(zApplication);
		maxima.mData 			= zData;
		
		return maxima;
	}
	
	@Override
	protected void processMessage(Message zMessage) throws Exception {
		
		//Get the MaximaDB
		MaximaDB maxdb = SelfDB.getDB().getMaximaDB();
		
		if(zMessage.getMessageType().equals(MAXIMA_INIT)) {
			
			//Get the UserDB
			UserDB udb = SelfDB.getDB().getUserDB();
			
			//Get the Permanent List
			mPermanentMaxima = udb.getMaximaPermanent();
			
			//Are we allowed contact requests
			mMaxContacts.setAllowContact(udb.getMaximaAllowContacts());
			
			//Do we have an account already..
			if(!udb.exists(MAXIMA_PUBKEY)) {
				createMaximaKeys();
			
			}else {
				mPublic  = udb.getData(MAXIMA_PUBKEY, MiniData.ZERO_TXPOWID);
				mPrivate = udb.getData(MAXIMA_PRIVKEY, MiniData.ZERO_TXPOWID);
			
				//Convert to a Maxima Address
				mMaximaAddress = Address.makeSelfAddress(mPublic);
			}
			
			//Check MLS Keys
			if(!udb.exists(MAXIMA_MLSPUBKEY)) {
				createMaximaMLSKeys();
			
			}else {
				mMLSPublic  = udb.getData(MAXIMA_MLSPUBKEY, MiniData.ZERO_TXPOWID);
				mMLSPrivate = udb.getData(MAXIMA_MLSPRIVKEY, MiniData.ZERO_TXPOWID);
			
				//Convert to a Maxima Address
				mMaximaMLSAddress = Address.makeSelfAddress(mMLSPublic);
			}
			
			mIsStaticMLS 	= udb.getBoolean(MAXIMA_ISSTATICMLS, false);
			mStaticMLS		= udb.getString(MAXIMA_STATICMLS, "");
			if(mIsStaticMLS) {
				SelfLogger.log("Static MLS found : "+mStaticMLS);
			}
			
			//Hard set the MLSService
			String oldserver 	= udb.getString(MAXIMA_OLDMLSHOST, "");
			String server 		= udb.getString(MAXIMA_MLSHOST, "");
			long mlstime		= udb.getNumber(MAXIMA_MLSTIME, MiniNumber.ZERO).getAsLong();
			mMLSService.hardSetMLSNode(oldserver, server, mlstime);
			
			//New Random UID for MLS GET Messages
			MLS_RANDOM_UID = MiniData.getRandomData(32).to0xString();
			
			//Load the permanent address stuff if required
//			udb.
			
			//We are inited
			mInited = true;
			
			//Save the DB
			SelfDB.getDB().saveUserDB();
			
			//Post a LOOP message that updates all my contacts just in case..
			PostTimerMessage(new TimerMessage(1000 * 60 * 3, MAXIMA_LOOP));
			
		}else if(zMessage.getMessageType().equals(MAXIMA_LOOP)) {
			
			//Check the MLS servers - for Users we have not seen in a while..
			PostMessage(MAXIMA_CHECK_MLS);
			
			//Resend all your details to your contacts
			PostMessage(MAXIMA_REFRESH_TIMER);
			
			//Delete really old MaxHosts - not seen for 7 days
			maxdb.deleteOldHosts();
			
			//Post a LOOP message that updates all my contacts just in case..
			PostTimerMessage(new TimerMessage(MAXIMA_LOOP_DELAY, MAXIMA_LOOP));
		
		}else if(zMessage.getMessageType().equals(MAXIMA_REFRESH_TIMER)) {
			
			//Wait 60 seconds.. then refresh Users
			PostTimerMessage(new TimerMessage(60000, MAXIMA_REFRESH));
			
		}else if(zMessage.getMessageType().equals(MAXIMA_CHECK_MLS)) {
			
			//Are we forcing
			boolean force = zMessage.getBoolean("force",false);
			
			//Flush the MLS
			mMLSService.flushList();
			
			//The Min Time before we do an MLS lookup - 30 mins.. 1 loop
			long mintime = System.currentTimeMillis() - (1000 * 60 * 30);
			
			//Get all your contacts
			ArrayList<MaximaContact> allcontacts = maxdb.getAllContacts();
			for(MaximaContact contact : allcontacts) {
				
				//Have we heard from them lately
				if(force || contact.getLastSeen() < mintime) {
					
					//Send an MLS GET req..
					String mls = contact.getMLS();
					if(!mls.equals("")) {
						
						//Log it.. 
						//SelfLogger.log("MLS check "+contact.getName()+" @ "+contact.getCurrentAddress()+" mls:"+mls);	
						
						//Create a Get req
						MLSPacketGETReq req = new MLSPacketGETReq(contact.getPublicKey(), MLS_RANDOM_UID);
						
						//Get the data version
						MiniData reqdata = MiniData.getMiniDataVersion(req);
						
						Message getreq = maxima.createSendMessage(mls,MAXIMA_MLS_GETAPP,reqdata);
						PostMessage(getreq);
					}
				}
			}
		
		}else if(zMessage.getMessageType().equals(MAXIMA_REFRESH)) {
			
			//Update the MLS Servers
			updateMLSServers();
			
			//Get all your contacts
			ArrayList<MaximaContact> allcontacts = maxdb.getAllContacts();
			for(MaximaContact contact : allcontacts) {
				
				//Now send a message updating them
				Message update = new Message(MaximaContactManager.MAXCONTACTS_UPDATEINFO);
				update.addString("publickey", contact.getPublicKey());
				update.addString("address", contact.getCurrentAddress());
				getContactsManager().PostMessage(update);
			}
			
		}else if(zMessage.getMessageType().equals(MAXIMA_CONNECTED)) {
		
			//Get the client
			NIOClient nioc = (NIOClient) zMessage.getObject("nioclient");
			
			//Is this an internal IP
			String fullhost 	= nioc.getFullAddress(); 
			boolean invalidip	 = false;
			
			if(!GeneralParams.ALLOW_ALL_IP) {
				invalidip 	= 	fullhost.startsWith("127.") || 
								fullhost.startsWith("10.")  || 
								fullhost.startsWith("100.") ||
								fullhost.startsWith("0.") 	||
								fullhost.startsWith("169.") ||
								fullhost.startsWith("172.") ||
								fullhost.startsWith("198.") ||
								fullhost.startsWith("192.");
			}
			
			//Warn..
			if(invalidip && nioc.isOutgoing()) {
				SelfLogger.log("Invalid IP for MAXIMA host ( is internal ) "+nioc.getFullAddress()+" ..re-enable with -allowallip");
				return;
			}
			
			//Send him our MLS details..
			if(nioc.isIncoming()) {
				MaximaCTRLMessage maxmls = new MaximaCTRLMessage(MaximaCTRLMessage.MAXIMACTRL_TYPE_MLS);
				maxmls.setData(new MiniData(mMaximaMLSAddress.getBytes()));
				NIOManager.sendNetworkMessage(nioc.getUID(), NIOMessage.MSG_MAXIMA_CTRL, maxmls);
			}
			
			//is it an outgoing.. ONLY outgoing can be used for MAXIMA
			if(!invalidip && nioc.isOutgoing()) {
				
				//OK.. Do we have this node in our list..
				MaximaHost sxhost = maxdb.loadHost(nioc.getFullAddress());
				
				//Do we have something..
				if(sxhost == null) {
					if(GeneralParams.MAXIMA_LOGS) {
						SelfLogger.log("MAXIMA NEW connection : "+nioc.getFullAddress());
					}
					
					//Create a new Host
					sxhost = new MaximaHost(nioc.getFullAddress());
					sxhost.createKeys();
					
					//Now insert this into the DB
					maxdb.newHost(sxhost);
				}else {
					if(GeneralParams.MAXIMA_LOGS) {
						SelfLogger.log("MAXIMA EXISTING connection : "+nioc.getFullAddress());
					}
					
					sxhost.updateLastSeen();
					maxdb.updateHost(sxhost);
				}
				
				//So we know the details.. Post them to him.. so he knows who we are..
				MaximaCTRLMessage maxmess = new MaximaCTRLMessage(MaximaCTRLMessage.MAXIMACTRL_TYPE_ID);
				maxmess.setData(sxhost.getPublicKey());
				NIOManager.sendNetworkMessage(nioc.getUID(), NIOMessage.MSG_MAXIMA_CTRL, maxmess);
				
				//And now post a check message..
				String to 			= sxhost.getMaximaAddress();
				String uid			= nioc.getUID();
				
				//Are we ready to mine a message
				if(SelfDB.getDB().getTxPoWTree().getTip() != null){
					//Send Immediately..
					Message check = new Message(MAXIMA_SENDCHKCONNECT);
					check.addString("to", to);
					check.addString("uid", uid);
					PostMessage(check);
					
				}else {
					SelfLogger.log("TIMED Maxima connect as no chain yet.. : "+nioc.getFullAddress());
					//With Delay
					TimerMessage check = new TimerMessage(10000,MAXIMA_SENDCHKCONNECT);
					check.addString("to", to);
					check.addString("uid", uid);
					PostTimerMessage(check);
				}
			}
			
		}else if(zMessage.getMessageType().equals(MAXIMA_SENDCHKCONNECT)) {
			
			//Get the NIO Client uid
			String uid = zMessage.getString("uid");
			
			//Send a check Connect message
			String to 			= zMessage.getString("to");
			String application 	= MAXIMA_CHKCONECT_APP;
			MiniData data 		= new MiniData(uid.getBytes());
			
			//Create a HELLO message
			Message chkconnect 	= maxima.createSendMessage(to,application,data);
			
			//Send it..
			PostMessage(chkconnect);
			
			//AND - send a message that 
			TimerMessage checkconnected = new TimerMessage(30000, MAXIMA_CHECK_CONNECTED);
			checkconnected.addString("uid", uid);
			PostTimerMessage(checkconnected);
			
		}else if(zMessage.getMessageType().equals(MAXIMA_CHECK_CONNECTED)) {
			
			//Don't do this if in SLAVE mode
			if(GeneralParams.TXBLOCK_NODE) {
				return;
			}
			
			//Check that IF this host is connected to us - it is a valid Maxima host
			String uid = zMessage.getString("uid");
			
			//Get the NIO Client..
			NIOClient nioc =  SELFSystem.getInstance().getNIOManager().getNIOClientFromUID(uid);
			
			//Is it valid..
			if(nioc != null) {
				
				SelfLogger.log("MAXIMA HOST CONNECTED "+nioc.getUID()+" "+nioc.getFullAddress());
				
				//Ok - we should be connected..
				MaximaHost sxhost = maxdb.loadHost(nioc.getFullAddress());
				if(sxhost == null) {
					SelfLogger.log("MaximaHost NOT Found on CHECK_CONNECTED "+nioc.getFullAddress()+" incoming:"+nioc.isIncoming());
					return;
				}
				
				//If not connected..
				if(sxhost.getConnected() == 0) {
				
					//Are we connected..
					SelfLogger.log("MAXIMA Check if connected : "+nioc.getFullAddress()+" "+sxhost.getConnected());
					
					//How many valid hosts are we connected to.. if enough leave it..
					int conns = getAllConnectedHosts().size();
					if(conns < 2) {
					
						SelfLogger.log("MAXIMA disconnecting from "+nioc.getFullAddress()+" reconnecting to random host");
						
						//Disconnect
						SELFSystem.getInstance().getNIOManager().disconnect(uid);
						
						//Disconnect this and reconnect to a random peer..
						SELFSystem.getInstance().getNetworkManager().getP2PManager().PostMessage(P2PManager.P2P_RANDOM_CONNECT);
					
					}else {
						SelfLogger.log("MAXIMA Connected to "+conns+" Hosts.. not disconnecting..");
					}
				}
				
			}else {
				SelfLogger.log("MAXIMA check if connected : HOST NOT AVAILABLE.. "+uid);
			}
			
		}else if(zMessage.getMessageType().equals(MAXIMA_DISCONNECTED)) {
			
			//Get the client
			NIOClient nioc = (NIOClient) zMessage.getObject("nioclient");
			
			//Is there a reconnect
			boolean reconnect = zMessage.getBoolean("reconnect");
			
			//OK.. Do we have this node in our list..
			MaximaHost sxhost = maxdb.loadHost(nioc.getFullAddress());
			if(sxhost != null) {
				sxhost.setConnected(0);
				maxdb.updateHost(sxhost);
			}
			
			//is it an outgoing.. ONLY outgoing can be used for MAXIMA
			if(nioc.isOutgoing()) {
				
				if(sxhost != null) {
					if(GeneralParams.MAXIMA_LOGS) {
						SelfLogger.log("MAXIMA outgoing disconnection : "+nioc.getFullAddress()+" "+reconnect);
					}
					nioc.setMaximaDisconnected();
				}
				
				//Update the MLS Servers
				updateMLSServers();
				
				//Ok - lets reset contacts that use this host
				String host = nioc.getFullAddress();
			
				//Which contacts used that host - reassign them
				ArrayList<MaximaContact> allcontacts = maxdb.getAllContacts();
				for(MaximaContact contact : allcontacts) {
					
					//Only reset those that use this address
					if(contact.getMyAddress().contains(host)) {
					
						SelfLogger.log("MAXIMA Updating contact on disconnected host : "+contact.getName());
						
						//Update them with a new address..
						String publickey = contact.getPublicKey();
						String address	 = contact.getCurrentAddress();
						
						//Now send a message updating them
						Message update = new Message(MaximaContactManager.MAXCONTACTS_UPDATEINFO);
						update.addString("publickey", publickey);
						update.addString("address", address);
						
						getContactsManager().PostMessage(update);
					}
				}
				
				//There has been a change..
				NotifyMaximaHostsChanged(nioc.getFullAddress(), false);
			}
			
		}else if(zMessage.getMessageType().equals(MAXIMA_CTRLMESSAGE)) {
			
			//Received a control message from a client
			MaximaCTRLMessage msg = (MaximaCTRLMessage) zMessage.getObject("maximactrl");
			
			//Get the NIOClient
			NIOClient nioc = (NIOClient) zMessage.getObject("nioclient");
			
			if(msg.getType().isEqual(MaximaCTRLMessage.MAXIMACTRL_TYPE_ID)) {
				
				//Set the ID for this Connection
				MiniData pubkey = msg.getData();

				//And Set..
				nioc.setMaximaIdent(pubkey.to0xString());
			
			}else if(msg.getType().isEqual(MaximaCTRLMessage.MAXIMACTRL_TYPE_MLS)) {
				
				//Set the ID for this Connection
				String mlspubkey = new String(msg.getData().getBytes());
				
				//Set this as his MLS address
				nioc.setMaximaMLS(mlspubkey+"@"+nioc.getFullAddress());
			}
			
		}else if(zMessage.getMessageType().equals(MAXIMA_SENDMESSAGE)) {
		
			Message msg = new Message(MaxMsgHandler.MAX_SEND_MESSAGE);
			msg.addObject("msg", zMessage);
			mMaxSender.PostMessage(msg);
			
		}else if(zMessage.getMessageType().equals(MAXIMA_MLSGET_RESP)) {
			//Get the MLS Packet
			MLSPacketGETResp mls = (MLSPacketGETResp) zMessage.getObject("mlsget");
			
			//Set these details..
			MaximaContact contact = maxdb.loadContactFromPublicKey(mls.getPublicKey());
			
			//Set the new Maxima Address
			contact.setCurrentAddress(mls.getAddress());
			
			//And update the DB
			maxdb.updateContact(contact);
			
			//Now send a message updating them
			Message update = new Message(MaximaContactManager.MAXCONTACTS_UPDATEINFO);
			update.addString("publickey", contact.getPublicKey());
			update.addString("address", contact.getCurrentAddress());
			getContactsManager().PostMessage(update);
			
			//Log it..
			if(GeneralParams.MAXIMA_LOGS) {
				SelfLogger.log("MLSGET address updated for "+contact.getName()+" "+contact.getCurrentAddress());
			}
			
		}else if(zMessage.getMessageType().equals(MAXIMA_RECMESSAGE)) {
			
			//Get the MaxTxPoW
			MaxTxPoW sxtxpow 	= (MaxTxPoW) zMessage.getObject("maxtxpow");
			
			//Get the NIOClient
			NIOClient nioc 		= (NIOClient) zMessage.getObject("nioclient");
			
			//received a Message!
			MaximaPackage mpkg 	= sxtxpow.getMaximaPackage();
		
			//Private key tpo decode the message
			MiniData privatekey = null;
			
			//The pubkey it is encrypted with
			String tomaxima = mpkg.mTo.to0xString();
			
			//Is it straight to us..
			if(mpkg.mTo.equals(mPublic)) {
				//It's directly sent to us..
				privatekey = mPrivate;
			}else if(mpkg.mTo.equals(mMLSPublic)) {
				//It's an MLS message
				privatekey = mMLSPrivate;
			} 
			
			//Is it for us - check the Maxhosts..
			if(privatekey == null) {
				//Get the maxima Host
				MaximaHost host = maxdb.loadHostFromPublicKey(tomaxima);
				if(host != null) {
					privatekey = host.getPrivateKey();
				}
			}
			
			//If we don't find it..
			if(privatekey == null) {
				
				//Forward it to them
				NIOClient client =  SELFSystem.getInstance().getNIOManager().getMaximaUID(tomaxima);
				
				//Do we have it
				if(client != null) {
					if(GeneralParams.MAXIMA_LOGS) {
						SelfLogger.log("MAXIMA message forwarded to client : "+tomaxima);
					}
					
					//Send to the client we are connected to..
					NIOManager.sendNetworkMessage(client.getUID(), NIOMessage.MSG_MAXIMA_TXPOW, sxtxpow);
					
					//Notify that Client that we received the message.. this makes external client disconnect ( internal just a ping )
					maximaMessageStatus(nioc,MAXIMA_OK);
					
				}else{
					
					if(GeneralParams.MAXIMA_LOGS) {
						SelfLogger.log("MAXIMA message received for Client we are not connected to : "+tomaxima);
					}
				
					//Notify that Client of the fail.. this makes external client disconnect ( internal just a ping )
					maximaMessageStatus(nioc,MAXIMA_UNKNOWN);
				}
				
				return;
			}
			
			//Decrypt the data
			CryptoPackage cp = new CryptoPackage();
			cp.ConvertMiniDataVersion(mpkg.mData);
			byte[] data = cp.decrypt(privatekey.getBytes());
			
			//Now get the Decrypted data..
			MaximaInternal mm = MaximaInternal.ConvertMiniDataVersion(new MiniData(data));
			
			//Check the Signature..
			boolean valid = SignVerify.verify(mm.mFrom.getBytes(), mm.mData.getBytes(), mm.mSignature.getBytes());
			if(!valid) {
				SelfLogger.log("MAXIMA Invalid Signature on message : "+mpkg.mTo.to0xString());
				
				//Notify that Client of the fail.. this makes external client disconnect ( internal just a ping )
				maximaMessageStatus(nioc,MAXIMA_FAIL);
				
				return;
			}
			
			//Now convert the data to a Maxima Message
			MaximaMessage maxmsg 	= MaximaMessage.ConvertMiniDataVersion(mm.mData);
			
			//Check the message is from the person who signed it!
			if(!maxmsg.mFrom.isEqual(mm.mFrom)) {
				SelfLogger.log("MAXIMA Message From field signed by incorrect pubkey  from:"+maxmsg.mFrom.to0xString()+" signed:"+mm.mFrom.to0xString());
				
				//Notify that Client of the fail.. this makes external client disconnect ( internal just a ping )
				maximaMessageStatus(nioc,MAXIMA_FAIL);
				
				return;
			}
			
			//Hash the complete message..
			MiniData hash = Crypto.getInstance().hashObject(mm.mData);
			
			//Now create the final JSON..
			JSONObject maxjson = maxmsg.toJSON();
			maxjson.put("msgid", hash.to0xString());
			
			//Do we log
			if(GeneralParams.MAXIMA_LOGS) {
				SelfLogger.log("MAXIMA : "+maxjson.toString());
			}
			
			//Where is it headed
			String application = (String) maxjson.get("application");
			
			//Notify that Client that we received the message.. this makes external client disconnect ( internal just a ping )
			if(!application.equals(MAXIMA_MLS_GETAPP)) {
				maximaMessageStatus(nioc,MAXIMA_OK);
			}
			
			//Is it a special contact message
			if(application.equals(MaximaContactManager.CONTACT_APPLICATION)) {
				
				//Process this internally..
				Message contactmessage = new Message(MaximaContactManager.MAXCONTACTS_RECMESSAGE);
				contactmessage.addObject("maxmessage", maxjson);
				getContactsManager().PostMessage(contactmessage);
				
				//Update DB - this host is being used..
				MaximaHost host = maxdb.loadHost(nioc.getFullAddress());
				if(host != null) {
					host.updateLastSeen();
					maxdb.updateHost(host);
				}
				
			}else if(application.equals(MAXIMA_CHKCONECT_APP)) {
				
				//Get the Data
				MiniData maxdata = new MiniData(maxjson.getString("data"));
				String uid = new String(maxdata.getBytes());
				
				//Check Valid..
				if(!uid.equals(nioc.getUID())) {
					SelfLogger.log("INVALID MAXCHECK REC:"+uid+" FROM:"+nioc.getUID()+" Could be multiple connections to the same Host..? @ "+nioc.getFullAddress());
					//Multiple connections to the same host cause this error ?
					//return;
				}
				
				//Get the HOST
				MaximaHost sxhost = maxdb.loadHost(nioc.getFullAddress());
				if(sxhost == null) {
					SelfLogger.log("MaximaHost NOT Found on CHKCONNECT_APP "+nioc.getFullAddress()+" incoming:"+nioc.isIncoming());
					return;
				}
				
				//DOUBLE check - Are we connected..
				if(nioc.hasMaximaDiscxonnected()) {
					//Already disconnected
					SelfLogger.log("MaximaHost already disconnected at check connect "+nioc.getFullAddress()+" incoming:"+nioc.isIncoming());
					return;
				}
				
				if(GeneralParams.MAXIMA_LOGS) {
					SelfLogger.log("MAXIMA HOST accepted : "+nioc.getFullAddress());
				}
				
				//Now we can use this as one of Our Addresses
				sxhost.setConnected(1);
				maxdb.updateHost(sxhost);
				
				NotifyMaximaHostsChanged(nioc.getFullAddress(), true);
				
				//OK.. add to our list
				if(nioc.isMaximaMLS()) {
					
					String niocmls = nioc.getMaximaMLS();
					
					if(mMLSService.newMLSNode(niocmls)) {
						//Changed.. set new in DB
						UserDB udb = SelfDB.getDB().getUserDB();
						udb.setString(MAXIMA_OLDMLSHOST, mMLSService.getOldMLSServer());
						udb.setString(MAXIMA_MLSHOST, mMLSService.getMLSServer());
						udb.setNumber(MAXIMA_MLSTIME, new MiniNumber(mMLSService.getMLSTime()));	
								
						//Save this..
						SelfDB.getDB().saveUserDB();
					}
					
					//Are we in slave mode..
					if(GeneralParams.TXBLOCK_NODE) {
						SelfLogger.log("Slave Node force set STATIC MLS");
						
						//Set this as static MLS
						setStaticMLS(true, niocmls);
					}
				}
				
				//Update the MLS
				updateMLSServers();
				
			}else if(application.equals(MAXIMA_MLS_SETAPP)) {
				
				//Get the package
				MiniData mlssetdata = new MiniData(maxjson.getString("data"));
				
				//Convert it..
				MLSPacketSET mls = MLSPacketSET.convertMiniDataVersion(mlssetdata);
				
				//Add to the MLSService
				mMLSService.addMLSData(maxmsg.mFrom.to0xString(), mls);
				
			}else if(application.equals(MAXIMA_MLS_GETAPP)) {
				
				//Get the data
				MiniData reqdata 	= new MiniData(maxjson.getString("data"));
				MLSPacketGETReq req = MLSPacketGETReq.convertMiniDataVersion(reqdata);
				
				//Check the MLS service for this 
				MLSPacketSET mlspack = mMLSService.getData(req.getPublicKey());
				
				//Do we have data
				if(mlspack == null) {
					SelfLogger.log("Unknown publickey in MLSService "+req.getPublicKey());
					maximaMessageStatus(nioc,MAXIMA_UNKNOWN);
					return;
				}
				
				//Is THIS user allowed to see this data
				boolean allowed 	= mlspack.isValidPublicKey(maxmsg.mFrom.to0xString());
				boolean ispermanent = mPermanentMaxima.contains(req.getPublicKey());
				
				if(!allowed && !ispermanent) {
					SelfLogger.log("Invalid MLS request for "+req.getPublicKey()+" by "+maxmsg.mFrom.to0xString());
					maximaMessageStatus(nioc,MAXIMA_UNKNOWN);
					return;
				}
				
				//Create a response..
				MLSPacketGETResp mlsget = new MLSPacketGETResp(req.getPublicKey(),mlspack.getMaximaAddress(),req.getRandomUID());
				
				//Convert to a MiniData structure
				MiniData mlsdata = MiniData.getMiniDataVersion(mlsget);
				
				//Send that
				maximaMessageStatus(nioc,mlsdata);
				
			}else {
				//Notify The Listeners
				SELFSystem.getInstance().PostNotifyEvent("MAXIMA",maxjson);
			}
		}
	}
	
	private void maximaMessageErrorStatus(NIOClient zClient, String zMessage) throws IOException {
		//And send this..
		MaximaErrorMsg error 	= new MaximaErrorMsg(zMessage);
		MiniData errdata 		= MiniData.getMiniDataVersion(error);
		maximaMessageStatus(zClient,errdata);
	}
	
	private void maximaMessageStatus(NIOClient zClient, MiniData zStatus) throws IOException {
		//Send this Maxima response
		NIOManager.sendNetworkMessage(zClient.getUID(), NIOMessage.MSG_PING, zStatus);
	}
	
	private void createMaximaKeys() throws Exception {
		
		//Get the UserDB
		UserDB udb = SelfDB.getDB().getUserDB();
		
		//Create a new new maxima ident..
		KeyPair generateKeyPair = GenerateKey.generateKeyPair();
		
		byte[] publicKey 		= generateKeyPair.getPublic().getEncoded();
		mPublic 				= new MiniData(publicKey);
		
		byte[] privateKey	 	= generateKeyPair.getPrivate().getEncoded();
		mPrivate 				= new MiniData(privateKey);
	
		//Convert to a Maxima Address
		mMaximaAddress = Address.makeSelfAddress(mPublic);
		
		//Put in the DB..
		udb.setData(MAXIMA_PUBKEY, mPublic);
		udb.setData(MAXIMA_PRIVKEY, mPrivate);
	}
	
	/**
	 * MLS Functions
	 */
	private void createMaximaMLSKeys() throws Exception {
		
		//Get the UserDB
		UserDB udb = SelfDB.getDB().getUserDB();
		
		//Create a new new maxima ident..
		KeyPair generateKeyPair = GenerateKey.generateKeyPair();
		
		byte[] publicKey 		= generateKeyPair.getPublic().getEncoded();
		mMLSPublic 				= new MiniData(publicKey);
		
		byte[] privateKey	 	= generateKeyPair.getPrivate().getEncoded();
		mMLSPrivate 			= new MiniData(privateKey);
	
		//Convert to a Maxima Address
		mMaximaMLSAddress 		= Address.makeSelfAddress(mMLSPublic);
		
		//Put in the DB..
		udb.setData(MAXIMA_MLSPUBKEY, mMLSPublic);
		udb.setData(MAXIMA_MLSPRIVKEY, mMLSPrivate);
	}
	
	/**
	 * Update the MLS servers
	 */
	public void updateMLSServers(){
		
		//A list of all your contacts public keys
		ArrayList<String> validpubkeys = new ArrayList<>();
		
		//Which contacts used that host - reassign them
		ArrayList<MaximaContact> allcontacts = SelfDB.getDB().getMaximaDB().getAllContacts();
		for(MaximaContact contact : allcontacts) {
			//Store for the MLS
			validpubkeys.add(contact.getPublicKey());
		}
		
		//Create an MLSPacket
		MLSPacketSET mlspack = new MLSPacketSET(getRandomMaximaAddress());
		for(String pubkey : validpubkeys) {
			mlspack.addValidPublicKey(pubkey);
		}
		
		//Get the MiniData version
		MiniData mlspackdata = MiniData.getMiniDataVersion(mlspack);
		
		//Refresh My MLS hosts.. both old and new
		PostMessage(maxima.createSendMessage(getMLSHost(),MAXIMA_MLS_SETAPP,mlspackdata));
		PostMessage(maxima.createSendMessage(getOldMLSHost(),MAXIMA_MLS_SETAPP,mlspackdata));
	}
	
	/**
	 * When your hosts change send a notify message
	 */
	public void NotifyMaximaHostsChanged(String zFullAddress, boolean zConnected) {
		//Post a Notify Message
		JSONObject data = new JSONObject();
		data.put("host", zFullAddress);
		data.put("connected", zConnected);
		SELFSystem.getInstance().PostNotifyEvent("MAXIMAHOSTS", data);
	}
	
	public void NotifyMaximaContactsChanged() {
		//Post a Notify Message
		JSONObject data = new JSONObject();
		SELFSystem.getInstance().PostNotifyEvent("MAXIMACONTACTS", data);
	}
}
