package org.self.system.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.time.Duration;
import java.util.Date;
import java.util.Enumeration;

import javax.net.ssl.SSLSocket;

import org.self.database.SelfDB;
import org.self.database.userprefs.UserDB;
import org.self.system.network.self.NIOManager;
import org.self.system.network.self.NIOTraffic;
import org.self.system.network.p2p.P2PFunctions;
import org.self.system.network.p2p.P2PManager;
import org.self.system.network.p2p2.P2P2Manager;
import org.self.system.network.rpc.CMDHandler;
import org.self.system.network.rpc.HTTPSServer;
import org.self.system.network.rpc.HTTPServer;
import org.self.system.network.rpc.Server;
import org.self.system.params.GeneralParams;
import org.self.utils.MiniFormat;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONObject;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageProcessor;

public class NetworkManager {

	/**
	 * Have we shut this down..
	 */
	public boolean mShuttingDown = false;
	
	/**
	 * NIO Manager
	 */
	NIOManager mNIOManager;
	
	/**
	 * P2P Manager..
	 */
	MessageProcessor mP2PManager;
	
	/**
	 * P2P2 Manager..
	 */
	MessageProcessor mP2P2Manager;
	
	/**
	 * The RPC server
	 */
	Server mRPCServer = null;
	
	public NetworkManager() {
		
		//Not shutting down
		mShuttingDown = false;
		
		//Calculate the local host
		calculateHostIP();
		
		//Is the P2P Enabled
		if(GeneralParams.P2P_ENABLED) {
			//Create the Manager
			mP2PManager = new P2PManager();
		}else {
			//Create a Dummy listener.. 
			mP2PManager = new MessageProcessor("P2P_DUMMY") {
				@Override
				protected void processMessage(Message zMessage) throws Exception {
					if(zMessage.isMessageType(P2PFunctions.P2P_SHUTDOWN)) {
						stopMessageProcessor();
					}
				}
			};
		}
		
		//The main NIO server manager
		mNIOManager = new NIOManager(this);
				
		//Is the P2P2 enabled..
		if(GeneralParams.P2P2_ENABLED) {
			mP2P2Manager = new P2P2Manager();
		}else {
			//Create a Dummy listener.. 
			mP2P2Manager = new MessageProcessor("P2P2_DUMMY") {
				@Override
				protected void processMessage(Message zMessage) throws Exception {
					if(zMessage.isMessageType(P2PFunctions.P2P_SHUTDOWN)) {
						stopMessageProcessor();
					}
				}
			};
		}
		
		//Do we start the RPC server
		if(GeneralParams.RPC_ENABLED) {
			startRPC();
		}
	}
	
	public void calculateHostIP() {
		
		//Has it been specified at the command line..?
		if(GeneralParams.IS_HOST_SET) {
			return;
		}
		
		//Cycle through all the network interfaces 
		try {
			//Start easy
			GeneralParams.SELF_HOST = "127.0.0.1";
			
			boolean found = false;
		    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
	        while (!found && interfaces.hasMoreElements()) {
	            NetworkInterface iface = interfaces.nextElement();
	            // filters out 127.0.0.1 and inactive interfaces
	            if (iface.isLoopback() || !iface.isUp())
	                continue;

	            Enumeration<InetAddress> addresses = iface.getInetAddresses();
	            while(!found && addresses.hasMoreElements()) {
	                InetAddress addr = addresses.nextElement();
	                String ip   = addr.getHostAddress();
	                String name = iface.getDisplayName();
	                
	                //Only get the IPv4
	                if(!ip.contains(":")) {
						// This breaks P2P
						GeneralParams.SELF_HOST = ip;
	                	
	                	//If you're on WiFi..this is the one
	                	if(name.startsWith("wl")) {
	                		found = true;
	                		break;
	                	}
	                }
	            }
	        }
	    } catch (SocketException e) {
	        SelfLogger.log("ERROR calculating host IP : "+e);
	    }
	}
	
	public JSONObject getStatus() {
		return getStatus(false);
	}
	
	public JSONObject getStatus(boolean zAll) {
		JSONObject stats = new JSONObject();
		
		UserDB udb 				= SelfDB.getDB().getUserDB();
		
		stats.put("host", GeneralParams.SELF_HOST);
		stats.put("hostset", GeneralParams.IS_HOST_SET);
		stats.put("port", GeneralParams.SELF_PORT);
		
		stats.put("connecting", mNIOManager.getNumberOfConnnectingClients());
		stats.put("connected", mNIOManager.getNumberOfConnectedClients());
		
		//RPC Stats
		JSONObject rpcjson = new JSONObject();
		rpcjson.put("enabled", GeneralParams.RPC_ENABLED);
		rpcjson.put("port", GeneralParams.RPC_PORT);
		stats.put("rpc", rpcjson);
		
		if(!zAll) {
			return stats;
		}
		
		//P2P stats
		if(GeneralParams.P2P_ENABLED) {
			stats.put("p2p", ((P2PManager)mP2PManager).getStatus(false));
		}else {
			stats.put("p2p", "disabled");
		}
		
		//P2P2 stats
		if(GeneralParams.P2P2_ENABLED) {
			stats.put("p2p2", "enabled");
		}else {
			stats.put("p2p2", "disabled");
		}
		
		//Read / Write stats..
		NIOTraffic traffic = mNIOManager.getTrafficListener();
		
		JSONObject readwrite = new JSONObject();
		
		Duration dur = Duration.ofMillis(System.currentTimeMillis() - traffic.getStartTime());
		long mins 	 = dur.toMinutes(); 
		if(mins==0) {
			mins = 1;
		}
		
		Date starter = new Date(traffic.getStartTime());
		readwrite.put("from", starter.toString());
		readwrite.put("totalread", MiniFormat.formatSize(traffic.getTotalRead()));
		readwrite.put("totalwrite", MiniFormat.formatSize(traffic.getTotalWrite()));
		readwrite.put("breakdown",traffic.getBreakdown());
		
		long speedread 	= traffic.getTotalRead() / mins;
		long speedwrite = traffic.getTotalWrite() / mins;
		readwrite.put("read",MiniFormat.formatSize(speedread)+"/min");
		readwrite.put("write",MiniFormat.formatSize(speedwrite)+"/min");
		
		stats.put("traffic", readwrite);
		
		return stats;
	}
	
	public void startRPC() {
		if(mRPCServer == null) {
			
			//Are we SSL
			if(GeneralParams.RPC_SSL) {
				
				//Start The RPC server
				mRPCServer = new HTTPSServer(GeneralParams.RPC_PORT) {
					
					@Override
					public Runnable getSocketHandler(SSLSocket zSocket) {
						return new CMDHandler(zSocket);
					}
				};
				
			}else {
				
				//Start The RPC server
				mRPCServer = new HTTPServer(GeneralParams.RPC_PORT) {
					
					@Override
					public Runnable getSocketHandler(Socket zSocket) {
						return new CMDHandler(zSocket);
					}
				};				
			}
		}
	}
	
	public void stopRPC() {
		if(mRPCServer != null) {
			//Stop the RPC
			mRPCServer.shutdown();
			mRPCServer = null;
		}
	}
	
	public void shutdownNetwork() {
		//We are trying toi shutdown
		mShuttingDown = true;
		
		//And the RPC
		stopRPC();
		
		//Stop the NIO Manager
		mNIOManager.PostMessage(NIOManager.NIO_SHUTDOWN);
		
		//Send a message to the P2P
		if(GeneralParams.P2P_ENABLED) {
			((P2PManager)mP2PManager).shutdown();
		}else {
			mP2PManager.stopMessageProcessor();
		}
		
		//Send a message to the P2P
		if(GeneralParams.P2P2_ENABLED) {
			((P2P2Manager)mP2P2Manager).shutdown();
		}else {
			mP2P2Manager.stopMessageProcessor();
		}
	}
	
	public boolean isShutDownComplete() {
		return 	mNIOManager.isShutdownComplete() &&  
				mP2PManager.isShutdownComplete() && 
				mP2P2Manager.isShutdownComplete();
	}
	
	public void hardShutDown() {
		try {
			mNIOManager.hardShutDown();
		} catch (Exception e) {
			SelfLogger.log(e);
		} 
		mP2PManager.stopMessageProcessor();
		mP2P2Manager.stopMessageProcessor();
	}
	
	public MessageProcessor getP2PManager() {
		return mP2PManager;
	}
	
	public MessageProcessor getP2P2Manager() {
		return mP2P2Manager;
	}
	
	public NIOManager getNIOManager() {
		return mNIOManager;
	}
}
