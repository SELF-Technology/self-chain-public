package org.self.system.network.self;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.self.database.SelfDB;
import org.self.database.archive.ArchiveManager;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.objects.Greeting;
import org.self.objects.TxBlock;
import org.self.objects.TxPoW;
import org.self.objects.base.MiniByte;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.SELFSystem;
import org.self.system.commands.network.connect;
import org.self.system.network.NetworkManager;
import org.self.system.network.maxima.MaximaManager;
import org.self.system.network.p2p.P2PFunctions;
import org.self.system.network.p2p.P2PManager;
import org.self.system.params.GeneralParams;
import org.self.utils.MiniFormat;
import org.self.utils.SelfLogger;
import org.self.utils.Streamable;
import org.self.utils.json.JSONObject;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageProcessor;
import org.self.utils.messages.TimerMessage;

public class NIOManager extends MessageProcessor {

	public static final String NIO_SERVERSTARTED 	= "NIO_SERVERSTARTED";
	
	public static final String NIO_SHUTDOWN 		= "NIO_SHUTDOWN";
	
	public static final String NIO_CONNECT 			= "NIO_CONNECT";
	public static final String NIO_CONNECTATTEMPT 	= "NIO_CONNECTATTEMPT";
	public static final String NIO_NEWCONNECTION 	= "NIO_NEWCONNECT";
	
	public static final String NIO_DISCONNECT 		= "NIO_DISCONNECT";
	public static final String NIO_DISCONNECTED		= "NIO_DISCONNECTED";
	public static final String NIO_DISCONNECTALL 	= "NIO_DISCONNECTALL";
	
	public static final String NIO_RECONNECT 		= "NIO_RECONNECT";
	
	public static final String NIO_INCOMINGMSG 		= "NIO_NEWMSG";
	public static final String NIO_TXPOWREQ 		= "NIO_REQTXPOW";

	public static final String NIO_SYNCTXBLOCK 		= "NIO_SYNCTXBLOCK";

	//50MB limit on archive read / write
	public static long MAX_ARCHIVE_WRITE			= 1024 * 1024 * 50;
	
	/**
	 * How many attempts to reconnect
	 */
	public int RECONNECT_ATTEMPTS = 3;
	
	/**
	 * Check every 2 minutes to see if you have had a message in the last 10 mins..
	 */
	public static final String NIO_CHECKLASTMSG 	= "NIO_CHECKLASTMSG";
	long LASTREAD_CHECKER 		= 1000 * 120;
	long MAX_LASTREAD_CHECKER 	= 1000 * 60 * 10;
	
	/**
	 * Nuclear check to see if the networking is behaving itself 
	 */
	public static final String NIO_HEALTHCHECK 	= "NIO_HEALTHCHECK";
	long NIO_HEALTHCHECK_TIMER 	= 1000 * 60 * 20;
	long MAX_TIP_TIME_GAP		= 1000 * 60 * 120;
	
	/**
	 * SYNC Back max time
	 */
	long SYNC_MAX_TIME = 1000 * 60 * 60 * 24 * GeneralParams.NUMBER_DAYS_ARCHIVE;
	
	/**
	 * How long before a reconnect attempt
	 */
	static final long RECONNECT_TIMER = 30000;
	
	/**
	 * Main Network Manager
	 */
	NetworkManager mNetworkManager;
	
	/**
	 * The MAIN Self Server
	 */
	private NIOServer mNIOServer;
	
	/**
	 * Clients we are trying to connect to
	 */
	private ConcurrentHashMap<String, NIOClient> mConnectingClients;
	
	/**
	 * The clients connected awaiting added to NIOServer pool
	 */
	Object mSyncObject;
	HashSet<String> mAwaitingConnect = new HashSet<String>();
	
	/**
	 * How much data is being read / written over Self protocol
	 */
	NIOTraffic mTrafficListener;
	
	/**
	 * Thread pool to manage incoming messages
	 */
	ExecutorService THREAD_POOL = Executors.newFixedThreadPool(4);
	
	public NIOManager(NetworkManager zNetManager) {
		super("NIOMANAGER");
		
		mTrafficListener = new NIOTraffic();
		
		mNetworkManager = zNetManager;
		
		mConnectingClients = new ConcurrentHashMap<>();
		
		//New NIOServer
		mNIOServer = new NIOServer(GeneralParams.SELF_PORT, this);
		Thread nio = new Thread(mNIOServer);
		nio.start();
	}
	
	public NIOServer getNIOServer() {
		return mNIOServer;
	}
	
	public int getNumberOfConnectedClients() {
		return mNIOServer.getNetClientSize();
	}
	
	public int getNumberOfConnnectingClients() {
		return mConnectingClients.size();
	}
	
	public ArrayList<NIOClientInfo> getAllConnectionInfo() {
		//A list of all the connections
		ArrayList<NIOClientInfo> connections = new ArrayList<>();
		
		//Who are we trying to connect to
		Enumeration<NIOClient> clients = mConnectingClients.elements();
		while(clients.hasMoreElements()) {
			NIOClient nc 		= clients.nextElement();
			NIOClientInfo ninfo = new NIOClientInfo(nc, false);
			connections.add(ninfo);
		}
		
		//Who are we connected to..
		ArrayList<NIOClient> conns = mNIOServer.getAllNIOClients();
		for(NIOClient conn : conns) {
			NIOClientInfo ninfo = new NIOClientInfo(conn, true);
			connections.add(ninfo);
		}
		
		return connections;
	}
	
	public ArrayList<NIOClient> getAllValidConnectedClients() {
		//A list of all the connections
		ArrayList<NIOClient> connections = new ArrayList<>();
		
		//Who are we connected to..
		ArrayList<NIOClient> conns = mNIOServer.getAllNIOClients();
		for(NIOClient conn : conns) {
			if(conn.isValidGreeting()) {
				connections.add(conn);
			}
		}
		
		return connections;
	}
	
	public ArrayList<NIOClient> getAllValidOutGoingConnectedClients() {
		//A list of all the connections
		ArrayList<NIOClient> connections = new ArrayList<>();
		
		//Who are we connected to..
		ArrayList<NIOClient> conns = mNIOServer.getAllNIOClients();
		for(NIOClient conn : conns) {
			if(conn.isValidGreeting() && conn.isOutgoing()) {
				connections.add(conn);
			}
		}
		
		return connections;
	} 
	
	public JSONObject getAllConnectedDetails() {
		//A list of all the connections
		ArrayList<NIOClient> connections = new ArrayList<>();
		
		int incoming = 0;
		int outgoing = 0;
		int total 	 = 0;
		
		//Who are we connected to..
		ArrayList<NIOClient> conns = mNIOServer.getAllNIOClients();
		for(NIOClient conn : conns) {
			if(conn.isValidGreeting()) {
				if(conn.isOutgoing()) {
					outgoing++;
				}else {
					incoming++;
				}
				total++;
			}
		}
		
		JSONObject ret = new JSONObject();
		ret.put("total", total);
		ret.put("incoming", incoming);
		ret.put("outgoing", outgoing);
		
		return ret;
	} 
	
	public NIOClient checkConnected(String zHost, boolean zOnlyConnected) {
		
		if(!zOnlyConnected) {
			//Who are we trying to connect to
			Enumeration<NIOClient> clients = mConnectingClients.elements();
			while(clients.hasMoreElements()) {
				NIOClient nc = clients.nextElement();
				if(zHost.equals(nc.getFullAddress())) {
					return nc;
				}
			}
		}
		
		//Who are we connected to..
		ArrayList<NIOClient> conns = mNIOServer.getAllNIOClients();
		for(NIOClient conn : conns) {
			if(zHost.equals(conn.getFullAddress())) {
				return conn;
			}
		}
		
		return null;
	}
	
	public NIOClient getMaximaUID(String zMaximaPubKey) {
		ArrayList<NIOClient> conns = mNIOServer.getAllNIOClients();
		for(NIOClient conn : conns) {
			if(conn.getMaximaIdent().equals(zMaximaPubKey)) {
				return conn;
			}
		}
		
		return null;
	}
	
	public NIOClient getNIOClient(String zHost) {
		ArrayList<NIOClient> conns = mNIOServer.getAllNIOClients();
		for(NIOClient conn : conns) {
			if(conn.getFullAddress().equals(zHost)) {
				return conn;
			}
		}
		
		return null;
	}
	
	public NIOClient getNIOClientFromUID(String zUID) {
		
		return SELFSystem.getInstance().getNIOManager().getNIOServer().getClient(zUID);
		
//		ArrayList<NIOClient> conns = mNIOServer.getAllNIOClients();
//		for(NIOClient conn : conns) {
//			if(conn.getUID().equals(zUID)) {
//				return conn;
//			}
//		}
//		
//		return null;
	}
	
	public void hardShutDown() throws InterruptedException {
		//Stop the Thread pool
		THREAD_POOL.shutdown();
		THREAD_POOL.awaitTermination(8000, TimeUnit.MILLISECONDS);
		
		//Shut down the NIO
		mNIOServer.shutdown();
		
		//Stop this..
		stopMessageProcessor();
	}
	
	public NIOTraffic getTrafficListener() {
		return mTrafficListener;
	}
	
	@Override
	protected void processMessage(Message zMessage) throws Exception {
		
		//Don't process messages after shutdown
		if((SELFSystem.getInstance().isShuttingDown() ||  SELFSystem.getInstance().isRestoring()) && !zMessage.getMessageType().equals(NIO_SHUTDOWN)) {
			return;
		}
		
		if(zMessage.getMessageType().equals(NIO_SERVERSTARTED)) {
			
			//The NIOServer has started you can now start up the P2P and pre-connect list
			mNetworkManager.getP2PManager().PostMessage(P2PFunctions.P2P_INIT);
			
			//Any nodes to auto connect to.. comma separated list
			if(!GeneralParams.CONNECT_LIST.equals("")) {
				
				StringTokenizer strtok = new StringTokenizer(GeneralParams.CONNECT_LIST,",");
				while(strtok.hasMoreTokens()) {
					String host = strtok.nextToken().trim();
					
					//Create the connect message
					Message msg = connect.createConnectMessage(host);
					if(msg == null) {
						SelfLogger.log("ERROR connect host specified incorrectly : "+host);
					}else {
						SelfLogger.log("Attempt to connect to specified host in 10 seconds : "+host);
						
						//Wait 10 secs and then connect
						PostTimerMessage(new TimerMessage(10000, msg));
					}
				}
			}
			
			//Check how long since last connect for each client..
			PostTimerMessage(new TimerMessage(LASTREAD_CHECKER, NIO_CHECKLASTMSG));
			
			//DO a health check on the state of the networking - Now done in Main
			//PostTimerMessage(new TimerMessage(NIO_HEALTHCHECK_TIMER, NIO_HEALTHCHECK));
			
		}else if(zMessage.getMessageType().equals(NIO_SHUTDOWN)) {
			
			//Stop the Thread pool
			THREAD_POOL.shutdown();
			THREAD_POOL.awaitTermination(8000, TimeUnit.MILLISECONDS);
			
			//Shut down the NIO
			mNIOServer.shutdown();
			
			SelfLogger.log("Shutdown Networking..");
			
			//Wait for it to stop..
			while(mNIOServer.isRunning()) {
				Thread.sleep(50);
			}
			
			//Stop this..
			stopMessageProcessor();
		
		}else if(zMessage.getMessageType().equals(NIO_CONNECT)) {
			//Start a new connection - Outgoing connection
			String host = zMessage.getString("host");
			int port 	= zMessage.getInteger("port");
			
			//Double check if this peer on the naughty list
			if(P2PFunctions.isInvalidPeer(host+":"+port)) {
	        	SelfLogger.log("NIO_CONNECT : Trying to connect to Invalid Peer - disallowed @ "+host+":"+port);
	        	return;
	        }
			
			//Create a new NetworkClient
			NIOClient nc = new NIOClient(host, port);
			
			//Add to our list
			mConnectingClients.put(nc.getUID(), nc);
			
			//Now try and connect..
			PostMessage(new Message(NIO_CONNECTATTEMPT).addObject("client", nc));
		
		}else if(zMessage.getMessageType().equals(NIO_CONNECTATTEMPT)) {
			//Get the client..
			NIOClient nc = (NIOClient) zMessage.getObject("client");
			
			//Is it still in the list..
			if(!mConnectingClients.containsKey(nc.getUID())) {
				//Has been removed.. stop trying to connect
				SelfLogger.log("Connect attempt to "+nc.getFullAddress()+" cancelled.. (removed from connecting clients)");
				return;
			}
			
			//Logs..
			if(GeneralParams.TXBLOCK_NODE) {
				SelfLogger.log("Slave Node Connect attempt to "+nc.getFullAddress());
			}
			
//			//How many connections - if too many stop.. 
//			if(getNumberOfConnnectingClients() > 10) {
//				SelfLogger.log("Too many 'connecting' attempts - not connecting to "+nc.getFullAddress());
//				mConnectingClients.remove(nc.getUID());
//				
//				//Tell Maxima just in case..
//				Message maxconn = new Message(MaximaManager.MAXIMA_DISCONNECTED);
//				maxconn.addObject("nioclient", nc);
//				maxconn.addBoolean("reconnect", false);
//				SELFSystem.getInstance().getMaxima().PostMessage(maxconn);
//				
//				return;
//			}
			
			//Connect in separate thread..
			connectAttempt(nc);
			
		}else if(zMessage.getMessageType().equals(NIO_RECONNECT)) {
			//Get the client..
			NIOClient nc = (NIOClient) zMessage.getObject("client");
			
			//Increase the connect attempts
			nc.incrementConnectAttempts();
			
			//Do we try to reconnect
			boolean reconnect = true;
			
			//Are we in slave mode..
			boolean p2pmessagesent = false;
			if(GeneralParams.TXBLOCK_NODE) {
				
				if(nc.getConnectAttempts() > RECONNECT_ATTEMPTS) {
					
					//Always attempts to reconnect
					SelfLogger.log("INFO : Slave node attempt reconnect.. "+nc.getFullAddress());
					
					//We definitely have to reconnect..
					nc.setConnectAttempts(1);
				}
			
			}else if(!GeneralParams.P2P_ENABLED){
				
				//No P2P - keep trying to connect
				if(nc.getConnectAttempts() > RECONNECT_ATTEMPTS) {
					
					//Always attempts to reconnect
					SelfLogger.log("INFO : P2P disabled.. attempt reconnect.. "+nc.getFullAddress()+" use disconnect to stop.");
					
					//We definitely have to reconnect..
					nc.setConnectAttempts(1);
				}
				
			}else{
				
				//Do we attempt a reconnect..
				if(nc.getConnectAttempts() > RECONNECT_ATTEMPTS) {
					
					//Do we have ANY connections at all..
					int connected = getNumberOfConnectedClients();
					int connecting = getNumberOfConnnectingClients();
					
//					ArrayList<NIOClient> conns = mNIOServer.getAllNIOClients();
//					int tot = conns.size();
					
					if(connected>0 || connecting>1) {
						
						//No reconnect
						reconnect = false;
						
						//Tell the P2P..
						p2pmessagesent = true;
						Message newconn = new Message(P2PFunctions.P2P_NOCONNECT);
						newconn.addObject("client", nc);
						newconn.addString("uid", nc.getUID());
						mNetworkManager.getP2PManager().PostMessage(newconn);
						
						SelfLogger.log("INFO : "+nc.getUID()+"@"+nc.getFullAddress()+" connection failed - no more reconnect attempts ");
						
						//Add to our Invalid Peers list
						P2PFunctions.addInvalidPeer(nc.getFullAddress());
						
					}else {
						SelfLogger.log("INFO : "+nc.getUID()+"@"+nc.getFullAddress()+" Resetting reconnect attempts (no other connections) for "+nc.getFullAddress());
						
						//reset connect attempts..
						nc.setConnectAttempts(1);
					}
				}
			}
			
			//Is it on the Invalid List..
			if(reconnect && P2PFunctions.isInvalidPeer(nc.getFullAddress())) {
	        	SelfLogger.log("NIO_RECONNECT : Trying to connect to Invalid Peer - disallowed @ "+nc.getFullAddress());
	        	reconnect = false;
	        	
	        	if(!p2pmessagesent) {
	        		Message newconn = new Message(P2PFunctions.P2P_NOCONNECT);
					newconn.addObject("client", nc);
					newconn.addString("uid", nc.getUID());
					mNetworkManager.getP2PManager().PostMessage(newconn);
	        	}
	        }
			
			//Try and reconnect
			if(reconnect) {
				
				//Try again..
				TimerMessage tmsg = new TimerMessage(RECONNECT_TIMER, NIO_CONNECTATTEMPT);
				tmsg.addObject("client", nc);
				NIOManager.this.PostTimerMessage(tmsg);
			
			}else{
				
				//We are no  longer attempting to connect
				mConnectingClients.remove(nc.getUID());
				
				//Tell MAXIMA
				Message maxconn = new Message(MaximaManager.MAXIMA_DISCONNECTED);
				maxconn.addObject("nioclient", nc);
				maxconn.addBoolean("reconnect", false);
				SELFSystem.getInstance().getMaxima().PostMessage(maxconn);
			}
		
		}else if(zMessage.getMessageType().equals(NIO_DISCONNECTALL)) {
			
			//Disconnect from all the clients..!
			Enumeration<NIOClient> clients = mConnectingClients.elements();
			while(clients.hasMoreElements()) {
				NIOClient nc = clients.nextElement();
				disconnect(nc.getUID());
			}
			
			ArrayList<NIOClient> conns = mNIOServer.getAllNIOClients();
			for(NIOClient conn : conns) {
				disconnect(conn.getUID());
			}
			
		}else if(zMessage.getMessageType().equals(NIO_DISCONNECT)) {
			//Get the UID
			String uid = zMessage.getString("uid");
			
			//Remove from connecting..
			mConnectingClients.remove(uid);
			
			//And the connected as well..
			mNIOServer.disconnect(uid);
			
		}else if(zMessage.getMessageType().equals(NIO_DISCONNECTED)) {
			
			//Which nioclient
			NIOClient nioc = (NIOClient)zMessage.getObject("client");
			
			//Remove from the last sync list
			String clientid = nioc.getUID();
			NIOMessage.mlastSyncReq.remove(clientid);
			NIOMessage.mLastChainSync.remove(clientid);
			
			//Do we reconnect
			boolean reconnect = false;
			if(zMessage.exists("reconnect")) {
				reconnect = zMessage.getBoolean("reconnect");
			}
			
			//Is it a vaid client..
			if(!nioc.isValidGreeting()) {
				reconnect = false;
			}
			
			//Is it incoming..
			if(nioc.isIncoming()) {
				reconnect = false;
			}
			
			//Is it on the Invalid List..
			if(reconnect && P2PFunctions.isInvalidPeer(nioc.getFullAddress())) {
	        	SelfLogger.log("NIO_DISCONNECT : Trying to connect to Invalid Peer - disallowed @ "+nioc.getFullAddress());
	        	reconnect = false;
	        }
			
			//Slave node logs
			if(GeneralParams.TXBLOCK_NODE) {
				if(zMessage.exists("reconnect")) {
					SelfLogger.log("SLAVE NODE disconneced.. from:"+nioc.getUID()+" req:"+zMessage.getBoolean("reconnect")+" reconnect:"+reconnect+" validgreeting:"+nioc.isValidGreeting()+" host:"+nioc.getFullAddress()+" incoming:"+nioc.isIncoming());
				}else {
					SelfLogger.log("SLAVE NODE disconneced.. from:"+nioc.getUID()+" reconnect:"+reconnect+" validgreeting:"+nioc.isValidGreeting()+" host:"+nioc.getFullAddress()+" incoming:"+nioc.isIncoming());
				}
				
				//Are we still connected..
				if(nioc.isOutgoing() && checkConnected(nioc.getFullAddress(), true) == null) {
					if(reconnect == false) {
						SelfLogger.log("FORCE Slave reconnect "+nioc.getUID()+" "+nioc.getFullAddress());
						reconnect = true;
					}
				}
			}
			
			//Lost a connection
			if(reconnect && nioc.isOutgoing()) {
				String host = nioc.getHost();
				int port 	= nioc.getPort();
				
				//Create a new NetworkClient
				NIOClient nc = new NIOClient(host, port);
				
				//Add to our list
				mConnectingClients.put(nc.getUID(), nc);
				
				//Create a connect message
				TimerMessage tmsg = new TimerMessage(RECONNECT_TIMER, NIO_CONNECTATTEMPT);
				tmsg.addObject("client", nc);
				PostTimerMessage(tmsg);
			}
			
			//Tell the P2P..
			Message newconn = new Message(P2PFunctions.P2P_DISCONNECTED);
			newconn.addObject("nioclient", nioc);
			newconn.addString("uid", nioc.getUID());
			newconn.addBoolean("incoming", nioc.isIncoming());
			newconn.addBoolean("reconnect", reconnect);
			mNetworkManager.getP2PManager().PostMessage(newconn);

			//Tell MAXIMA
			Message maxconn = new Message(MaximaManager.MAXIMA_DISCONNECTED);
			maxconn.addObject("nioclient", nioc);
			maxconn.addBoolean("reconnect", reconnect);
			SELFSystem.getInstance().getMaxima().PostMessage(maxconn);
			
		}else if(zMessage.getMessageType().equals(NIO_NEWCONNECTION)) {
			//New connection.. 
			NIOClient nioc = (NIOClient)zMessage.getObject("client");
		
			//We are no  longer attempting to connect
			mConnectingClients.remove(nioc.getUID());
			
			//IS THIS AN INVALID PEER..
			if(P2PFunctions.isInvalidPeer(nioc.getFullAddress())) {
				SelfLogger.log("Disconnecting invalid peer before sending or recieving ANY data..");
				disconnect(nioc.getUID());
				return;
			}
			
			//Is this an outgoing connection..
			if(!nioc.isIncoming()) {
				
				//Only Send this ONCE!
				if(!nioc.haveSentGreeting()) {
					nioc.setSentGreeting(true);	
				
					//Create the Greeting..
					Greeting greet = new Greeting().createGreeting();
					
					//And send it..
					NIOManager.sendNetworkMessage(nioc.getUID(), NIOMessage.MSG_GREETING, greet);
				}
			}
			
		}else if(zMessage.getMessageType().equals(NIO_INCOMINGMSG)) {
			//Who is it from
			String uid = zMessage.getString("uid");
			
			//What was the message
			MiniData data = (MiniData) zMessage.getObject("data");
			
			//Create a handler task
			NIOMessage niomsg = new NIOMessage(uid, data);
			niomsg.setTrace(isTrace(), getTraceFilter());
			
			//Is there a full address..
			if(zMessage.exists("fullhost")) {
				niomsg.setFullAddress(zMessage.getString("fullhost"));
			}
			
			//Process it.. in a thread pool..
			THREAD_POOL.execute(niomsg);
		
		}else if(zMessage.getMessageType().equals(NIO_TXPOWREQ)) {
			
			//Get the TxPoWID
			String txpowid = zMessage.getString("txpowid");
			
			//Which client..
			String clientid = zMessage.getString("client");

			//Why
			String reason = zMessage.getString("reason"); 
			
			//Check if we have it..
			if(!SelfDB.getDB().getTxPoWDB().exists(txpowid)) {
		
				//Notify..
				SelfLogger.log("INFO : Requesting TxPoW "+txpowid+" from "+clientid+" : "+reason);
				
				//Now get it..
				sendNetworkMessage(clientid, NIOMessage.MSG_TXPOWREQ, new MiniData(txpowid));
			}
		
		}else if(zMessage.getMessageType().equals(NIO_SYNCTXBLOCK)) {
			
			//Check we have a chain..
			if(SelfDB.getDB().getTxPoWTree().getRoot() == null) {
				SelfLogger.log("No TxPoWTree yet.. required for NIO_SYNCTXBLOCK");
				return;
			}
			
//			//Are we limiting this..
//			if(GeneralParams.ARCHIVESYNC_LIMIT_BANDWIDTH) {
//				
//				//How much have we used..
//				long total 		= SELFSystem.getInstance().getNIOManager().getTrafficListener().getTotalRead();
//				String current 	= MiniFormat.formatSize(total);
//				
//				if(total > NIOManager.MAX_ARCHIVE_WRITE) {
//					SelfLogger.log("MAX Bandwith used already ("+current+") - not asking for archive sync for 24hours..");
//					return;
//				}
//			}
			
			//Which client..
			String clientid = zMessage.getString("client");

			//Get the archive db
			ArchiveManager arch = SelfDB.getDB().getArchive();
			
			//What is my last block
			TxBlock lastblock 	= arch.loadLastBlock();
			TxPoW lastpow 		= null;
			if(lastblock == null) {
				//SelfLogger.log("NIO_SYNCTXBLOCK : No data in archive setting root of tree :"+arch.getSize());
				lastpow = SelfDB.getDB().getTxPoWTree().getRoot().getTxPoW();
			}else {
				lastpow = lastblock.getTxPoW();
			}
			
			//Do we have them all
			if(lastpow.getBlockNumber().isEqual(MiniNumber.ONE)) {
				//we have them all
				return;
			}
			
			//Check is within acceptable time..
			if(!GeneralParams.ARCHIVE) {
				long timenow = System.currentTimeMillis();
				long maxtime = timenow - SYNC_MAX_TIME;
				if(lastpow.getTimeMilli().getAsLong() < maxtime) {
					//we have enough..
					if(GeneralParams.IBDSYNC_LOGS) {
						SelfLogger.log("We have enough archive blocks.. lastblock "+new Date(lastpow.getTimeMilli().getAsLong()));
					}
					return;
				}
			}
			
			//Send a message asking for a sync
			if(GeneralParams.IBDSYNC_LOGS) {
				SelfLogger.log("[+] Request Sync IBD @ "+lastpow.getBlockNumber());
			}
			
			sendNetworkMessage(clientid, NIOMessage.MSG_IBD_REQ, lastpow);
			
		}else if(zMessage.getMessageType().equals(NIO_CHECKLASTMSG)) {
			
			//Check how long since last connect
			long timenow = System.currentTimeMillis();
			
			//Cycle and see..
			ArrayList<NIOClient> conns = mNIOServer.getAllNIOClients();
			for(NIOClient conn : conns) {
			
				long diff = timenow - conn.getLastReadTime();
				if(diff > MAX_LASTREAD_CHECKER) {
					
					//Too long a delay..
					SelfLogger.log("INFO : No recent message (10 mins) from "
							+conn.getUID()+" disconnect/reconnect incoming:"
							+conn.isIncoming()+" valid:"+conn.isValidGreeting()+" host:"+conn.getFullAddress());
					
					//Disconnect
					disconnect(conn.getUID());
					
					//And reconnect in 5 secs if outgoing.. incoming will reconnect anyway
					if(!conn.isIncoming() && conn.isValidGreeting()) {
						TimerMessage timedconnect = new TimerMessage(5000, NIO_CONNECT);
						timedconnect.addString("host", conn.getHost());
						timedconnect.addInteger("port", conn.getPort());
						PostTimerMessage(timedconnect);
					}
				}
			}
			
			//And Again..
			PostTimerMessage(new TimerMessage(LASTREAD_CHECKER, NIO_CHECKLASTMSG));
		
		}else if(zMessage.getMessageType().equals(NIO_HEALTHCHECK)) {
			
			//Recheck every 20 minutes
			/*PostTimerMessage(new TimerMessage(NIO_HEALTHCHECK_TIMER, NIO_HEALTHCHECK));
			
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
				
				//Is the gap too great
				if(diff > MAX_TIP_TIME_GAP) {
					
					SelfLogger.log("[!] Chain Tip too far behind.. restart Networking!");
					
					//Something wrong.. restart the Networking..
					restartsent = true;
					SELFSystem.getInstance().PostMessage(Main.MAIN_NETRESTART);
				}
			}
			
			//Check the P2P message count - can explode..
			if(GeneralParams.P2P_ENABLED) {
				
				//How many messages are in the stack..
	        	int count = SELFSystem.getInstance().getNetworkManager().getP2PManager().getSize();
	        	
	        	//If too many restart networking..
	        	if(count > 50) {
	        		
	        		SelfLogger.log("[!] P2P Message Overload - Restart");
	        		
	        		//Wipe the List
	        		if(!restartsent) {
	        			restartsent = true;
	        			SELFSystem.getInstance().PostMessage(Main.MAIN_NETRESTART);
	        		}
	        	}
			}
			*/
			
			//Check the number of Connecting Clients.. if too great.. restart the networking..
//			if(getNumberOfConnnectingClients() > 20 ) {
//				
//				//Log..
//				SelfLogger.log("Too Many connecting clients "+getNumberOfConnectedClients()+".. restarting networking");
//				
//				//Something not right..
//				Message netstart = new Message(SELFSystem.SELF_NETRESTART);
//				netstart.addBoolean("repeat", false);
//				SELFSystem.getInstance().PostMessage(netstart);
//				
//			}else {
//			
//				
//			}
		}
	}
	
	/**
	 * Connect to a client.. in a separate thread so returns immediately
	 */
	private void connectAttempt(final NIOClient zNIOClient) {
		
		Runnable connector = new Runnable() {
			
			@Override
			public void run() {
			
				try {
					//Create the socket channel if possible..
					InetSocketAddress addr 	= new InetSocketAddress(zNIOClient.getHost(), zNIOClient.getPort());
					SocketChannel sc 		= SocketChannel.open(addr);
					
					//Remove from the connecting..
					NIOManager.this.mConnectingClients.remove(zNIOClient.getUID());
					
					//we connected.. 
					NIOManager.this.mNIOServer.regsiterNewSocket(sc);
					
					SelfLogger.log("Connected attempt success to "+zNIOClient.getFullAddress());
					
				}catch(Exception exc) {
					//Try again in a minute..
					SelfLogger.log(zNIOClient.getUID()+" INFO : connecting attempt "+zNIOClient.getConnectAttempts()+" to "+zNIOClient.getHost()+":"+zNIOClient.getPort()+" "+exc.toString());
					
					//Do we try to reconnect
					Message reconn = new Message(NIO_RECONNECT);
					reconn.addObject("client", zNIOClient);
					NIOManager.this.PostMessage(reconn);
				}
			}
		};
		
		Thread tt = new Thread(connector);
		tt.start();
	}
	
	/**
	 * Disconnect a client
	 */
	public void disconnect(String zClientUID) {
		disconnect(zClientUID, false);
	}
	
	public void disconnect(String zClientUID, boolean zRemoveP2P) {
		
		//Logs
		if(GeneralParams.TXBLOCK_NODE) {
			try {
				if(true) {
					throw new Exception("Show Disconnect Stack Trace");
				}
			}catch(Exception exc) {
				SelfLogger.log(exc);
			}
		}
		
		//Do we remove from p2p as well..
		if(zRemoveP2P && GeneralParams.P2P_ENABLED) {
			
			NIOClient nioc =  getNIOClientFromUID(zClientUID);
			if(nioc!=null) {
				
				SelfLogger.log("Disconnecting and Removing PEER from P2P "+nioc.getFullAddress());
				
				//Make it invalid.
				P2PFunctions.addInvalidPeer(nioc.getFullAddress());
				
				//Create an address
				InetSocketAddress addr = new InetSocketAddress(nioc.getHost(), nioc.getPort());
				
				//Post it..
				Message msg = new Message(P2PManager.P2P_REMOVE_PEER).addObject("address", addr);
                SELFSystem.getInstance().getNetworkManager().getP2PManager().PostMessage(msg);
			}
		}
		
		Message msg = new Message(NIOManager.NIO_DISCONNECT).addString("uid", zClientUID);
		PostMessage(msg);
	}
	
	/**
	 * Send network messages
	 */
	public static void sendNetworkMessageAll(MiniByte zType, Streamable zObject) throws IOException {
		sendNetworkMessage("", zType, zObject);
	}
	
	public static void sendNetworkMessage(String zUID, MiniByte zType, Streamable zObject) throws IOException {
		//Make sure not null
		if(zObject == null) {
			throw new IOException("Cannot sendNetworkMessage with a NULL Object");
		}
		
		//Create the network message
		MiniData niodata = createNIOMessage(zType, zObject);
		
		//Are we logging..
		if(GeneralParams.NETWORKING_LOGS) {
			SelfLogger.log("[NETLOGS SEND] to:"+zUID+" type:"+NIOMessage.convertMessageType(zType)+" size:"+MiniFormat.formatSize(niodata.getLength()));
		}
		
		//For ALL or for ONE
		String howmany = "single";
		if(!zUID.equals("")) {
			//Send it..
			SELFSystem.getInstance().getNIOManager().getNIOServer().sendMessage(zUID,niodata);
		}else {
			//Send it..
			SELFSystem.getInstance().getNIOManager().getNIOServer().sendMessageAll(niodata);
			howmany = "all";
		}
		
		//Log to TRAFFIC monitor
		try {
			String strtype 		= NIOMessage.convertMessageType(zType);
			int size 			= niodata.getLength();
			NIOTraffic traffic 	= SELFSystem.getInstance().getNIOManager().getTrafficListener();
			traffic.addWriteBytes(strtype+"_"+howmany, size);
		}catch(Exception exc) {}
	}
	
	public static MiniData createNIOMessage(MiniByte zType, Streamable zObject) throws IOException {
		
		try {
			//Create a stream to write to
			ByteArrayOutputStream baos 	= new ByteArrayOutputStream();
			DataOutputStream dos 		= new DataOutputStream(baos);
			
			//write the type
			zType.writeDataStream(dos);
			
			//Write the Object
			zObject.writeDataStream(dos);
			
			//Flush it..
			dos.flush();
			
			//Convert to byte array
			byte[] bb = baos.toByteArray();
			
			//Close all..
			dos.close();
			baos.close();
			
			//request it..
			MiniData data = new MiniData(bb);
			
			return data;
			
		}catch(OutOfMemoryError oom ) {
			oom.printStackTrace();
			SelfLogger.log("OUT OF MEMORY.. on create NIOMsssage:"+NIOMessage.convertMessageType(zType));
		}
		
		throw new IOException("Out Of Memory..");
		
//		//Create a stream to write to
//		ByteArrayOutputStream baos 	= new ByteArrayOutputStream();
//		DataOutputStream dos 		= new DataOutputStream(baos);
//		
//		//write the type
//		zType.writeDataStream(dos);
//		
//		//Write the Object
//		zObject.writeDataStream(dos);
//		
//		//Flush it..
//		dos.flush();
//		
//		//Convert to byte array
//		byte[] bb = baos.toByteArray();
//		
//		//Close all..
//		dos.close();
//		baos.close();
//		
//		//request it..
//		MiniData data = new MiniData(bb);
//		
//		return data;
	}
	
	/**
	 * A special PING message to  check a valid connection..
	 */
	public static Greeting sendPingMessage(String zHost, int zPort, boolean suppressErrorMessage) {
		
		Greeting greet = null;
		
		try {
			//Create the Network Message
			MiniData msg = NIOManager.createNIOMessage(NIOMessage.MSG_SINGLE_PING, MiniData.ZERO_TXPOWID);
			
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
			SELFSystem.getInstance().getNIOManager().getTrafficListener().addWriteBytes("sendPingMessage",msg.getLength());
			
			//Load the message
			MiniData resp = MiniData.ReadFromStream(dis);
			
			//Tell the NIO
			SELFSystem.getInstance().getNIOManager().getTrafficListener().addReadBytes("sendPingMessage",resp.getLength());
			
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
			
			//Load the greeting
			greet = Greeting.ReadFromStream(bdis);
			
			bdis.close();
			bais.close();
		
		}catch(Exception exc){
			greet = null;
			if (!suppressErrorMessage) {
				SelfLogger.log("Error sending Single Ping message : " + exc.toString());
			}
		}
		
		return greet;
	}
}
