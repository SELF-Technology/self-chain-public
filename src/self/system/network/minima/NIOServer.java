package org.self.system.network.self;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.self.objects.base.MiniData;
import org.self.utils.SelfLogger;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageProcessor;

public class NIOServer implements Runnable {

	public static boolean mTraceON = false;
	
	MessageProcessor mNIOManager;
	
	int mPort;
	
	Selector mSelector;
	
	boolean mShutDown;
	
	ConcurrentHashMap<String, NIOClient> mClients = new ConcurrentHashMap<>();
	
	ArrayList<SocketChannel> mRegisterChannels;
	
	ArrayList<String> mDisconnectChannels;
	
	boolean mIsRunning = false;
	
	public NIOServer(int zPort, MessageProcessor zNIOManager) {
		mPort 				= zPort;
		mNIOManager 		= zNIOManager;
		mRegisterChannels	= new ArrayList<>();
		mDisconnectChannels	= new ArrayList<>();
	}
	
	public void shutdown() {
		mShutDown = true;
		if(mSelector != null) {
			mSelector.wakeup();
		}
	}
	
	public int getNetClientSize() {
		return mClients.size();
	}
	
	public ArrayList<NIOClient> getAllNIOClients(){
		ArrayList<NIOClient> allclients = new ArrayList<>();
		
		Enumeration<NIOClient> clients = mClients.elements();
		while(clients.hasMoreElements()) {
			NIOClient client = clients.nextElement();
			if(client != null) {
				allclients.add(client);
			}
		}
		
		return allclients;
	}
	
	public void regsiterNewSocket(SocketChannel zChannel) {
		synchronized (mRegisterChannels) {
			mRegisterChannels.add(zChannel);
		}
		
		mSelector.wakeup();
	}
	
	public void disconnect(String zUID) {
		synchronized (mDisconnectChannels) {
			mDisconnectChannels.add(zUID);
		}
		
		mSelector.wakeup();
	}
	
	public NIOClient getClient(String zUID) {
		return mClients.get(zUID);
	}
	
	public void sendMessage(String zUID, MiniData zData) {
		NIOClient client =  mClients.get(zUID);
		if(client != null) {
			try {
				client.sendData(zData);
			}catch(Exception exc) {
				SelfLogger.log(exc);
			}
		}
	}
	
	public void sendMessageAll(MiniData zData) {
		Enumeration<NIOClient> clients = mClients.elements();
		while(clients.hasMoreElements()) {
			NIOClient nioc = clients.nextElement();
			if(nioc != null) {
				if(nioc.isValidGreeting()) {
					try {
						nioc.sendData(zData);
					}catch(Exception exc) {
						SelfLogger.log(exc);
					}
				}
			}
		}
	}
	
	public boolean isRunning() {
		return mIsRunning;
	}
	
	@Override
	public void run() {
	
		try {
			//We are running
			mIsRunning = true;
			
			// Bind to 0.0.0.0 address which is the local network stack
	        InetAddress addr = InetAddress.getByName("0.0.0.0");
	
	        // Open a new ServerSocketChannel so we can listen for connections
	        ServerSocketChannel serversocket = ServerSocketChannel.open();
	
	        // Configure the socket to be non-blocking as part of the new-IO library (NIO)
	        serversocket.configureBlocking(false);
	
	        try {
	        	
	            // Bind our socket to the local port
	            serversocket.socket().bind(new InetSocketAddress(addr.getHostName(), mPort));
            
	        }catch(IOException exc) {
                
	        	//Serious enough to shut down..
	        	SelfLogger.log("[!] NIO ERROR - MAIN PORT " + mPort + " ALREADY IN USE. SHUTTING DOWN");
                
                //Shut down..
	        	Runtime.getRuntime().halt(0);
                //System.exit(1);
	        	
                return;
            }
        
	        // Bind our socket to the local port
//	        serversocket.socket().bind(new InetSocketAddress(addr.getHostName(), mPort));
	
	        // Reuse the address so more than one connection can come in
	        serversocket.socket().setReuseAddress(true);
	
	        // Open our selector channel
	        mSelector = Selector.open(); // selector is open here
	
	        // Register an "Accept" event on our selector service which will let us know when sockets connect to our channel
	        SelectionKey acceptKey = serversocket.register(mSelector, SelectionKey.OP_ACCEPT);
	
	        // Set our key's interest OPs to "Accept"
	        acceptKey.interestOps(SelectionKey.OP_ACCEPT);
	
	        //Ok - we are up and running..
	        mNIOManager.PostMessage(NIOManager.NIO_SERVERSTARTED);
	        
	        // This is the main loop
	        while (!mShutDown) {
	        	
	        	//Select something.. 
	        	mSelector.select(30000);
	        	
	        	//Are there any Channels to add..
	        	synchronized (mRegisterChannels) {
        			if(mRegisterChannels.size()>0) {
		        		for(SocketChannel chann : mRegisterChannels) {
		        			addChannel(false,chann);
		        		}
		        		
	        			//And clear..
	        			mRegisterChannels.clear();
        			}
	        	}
	        	
	        	//Are there any Channels to disconnect..
	        	synchronized (mDisconnectChannels) {
        			if(mDisconnectChannels.size()>0) {
		        		for(String uid: mDisconnectChannels) {
		        			NIOClient client = mClients.get(uid);
		        			if(client != null) {
		        				//Close client and invalidate key
		        				client.disconnect();
		        				
		        				//Remove from the list..
			                    mClients.remove(uid);
			                    
			                    //Tell the Network Manager
			                    Message newclient = new Message(NIOManager.NIO_DISCONNECTED)
			                    		.addObject("client", client)
			                    		.addBoolean("reconnect", false);
			                    
			                    mNIOManager.PostMessage(newclient);
		        			}
		        		}
		        		
	        			//And clear..
		        		mDisconnectChannels.clear();
        			}
	        	}
	        	
	        	//Loop through the current keys
	            Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator();
	            while (iterator.hasNext()) {
	            	
	                //Get  and remove the next key
	            	SelectionKey key = (SelectionKey) iterator.next();
	                iterator.remove();
	
	                // Get a reference to one of our custom objects
	                NIOClient client = (NIOClient) key.attachment();
	                
	                // skip any invalid / cancelled keys
	                if (!key.isValid()) {
	                	continue;
	                }
	                
	                try {
	                	if (key.isAcceptable()) {
	                		// Accept the socket's connection
	                        SocketChannel socket = serversocket.accept();
	                		
	                        //And add to our selector..
	                        addChannel(true, socket);
	                    }
	
	                    if (key.isReadable()) {
	                    	client.handleRead();
	                    }
	                    
	                    if (key.isWritable()) {
	                    	client.handleWrite();
	                    }
	                    
	                } catch (Exception e) {
	                	
	                    // Disconnect the user
	                    if(client != null) {
	                    	
	                    	//Disconnect..
		                	client.disconnect();
		                    
		                    //Remove from the list..
		                    mClients.remove(client.getUID());
		
		                    //Small Log..
		                    if(mTraceON) {
		                    	SelfLogger.log("[NIOSERVER] NIOClient:"+client.getUID()+" "+e+" total:"+mClients.size(), false);
		                    }
		                    
		                    //Tell the Network Manager
		                    Message dissclient = new Message(NIOManager.NIO_DISCONNECTED)
		                    		.addObject("client", client)
		                    		.addBoolean("reconnect", !client.isIncoming());
		                    
		                    //Tell the manager
		                    mNIOManager.PostMessage(dissclient);
		                    
	                    }else {
	                    	SelfLogger.log("[NIOSERVER] NULL Client disconnect in NIOServer.. ignoring..");
	                    }
	                }
	            }
	        }
	        
			//Shut down the socket..
			serversocket.close();
	        
			//Disconnect all clients..
			Enumeration<NIOClient> clients = mClients.elements();
			while(clients.hasMoreElements()) {
				NIOClient client = clients.nextElement();
				if(client !=null) {
					client.disconnect();
				}
			}
			
			//Need to call this to shut down properly
			mSelector.selectNow();
			
		}catch(Exception exc) {
			SelfLogger.log(exc);
		}
		
		//Not running anymore..
		//SelfLogger.log("[NIOServer] SHUTDOWN");
		mIsRunning = false;
	}

	private void addChannel(boolean zIncoming, SocketChannel zSocketChannel) throws IOException {
		// You can get the IPV6  Address (if available) of the connected user like so:
        String ipAddress = zSocketChannel.socket().getInetAddress().getHostAddress();
        
        // We also want this socket to be non-blocking so we don't need to follow the thread-per-socket model
        zSocketChannel.configureBlocking(false);
        zSocketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        zSocketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

        // Let's also register this socket to our selector:
        SelectionKey selectionkey = zSocketChannel.register(mSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        
        // Initially - We are only interested in events for reads for our selector.
        selectionkey.interestOps(SelectionKey.OP_READ);

        //What Port..
        InetSocketAddress remote = (InetSocketAddress)zSocketChannel.getRemoteAddress();
//        InetSocketAddress local  = (InetSocketAddress )zSocketChannel.getLocalAddress();
        
        int port = remote.getPort();
        
        //Create a new NIOCLient
        NIOClient  nioc = new NIOClient(zIncoming, ipAddress, port, zSocketChannel, selectionkey);
        
        // register with key
        selectionkey.attach(nioc);
        
        //Add to the total list..
        mClients.put(nioc.getUID(), nioc);
        
        //log..
        if(mTraceON) {
        	SelfLogger.log("[NIOSERVER] NEW NIOClient:"+nioc.getUID()+" total:"+mClients.size(), false);
        }
        
        //Post about it..
        Message newclient = new Message(NIOManager.NIO_NEWCONNECTION).addObject("client", nioc);
        mNIOManager.PostMessage(newclient);
	}
	
	public static void main(String[] zArgs) throws Exception {
		
		NIOServer nio = new NIOServer(9002, new MessageProcessor("hello") {
			@Override
			protected void processMessage(Message zMessage) throws Exception {
				SelfLogger.log(zMessage.toString());
			}
		});
		
		Thread tt = new Thread(nio);
		tt.start();
		
		SelfLogger.log("Started.. waiting..");
		Thread.sleep(2000);
		
		SelfLogger.log("Stopping..");
		nio.shutdown();
		Thread.sleep(2000);
		
		//Now start again..
		nio = new NIOServer(9002, new MessageProcessor("hello") {
			@Override
			protected void processMessage(Message zMessage) throws Exception {
				SelfLogger.log(zMessage.toString());
			}
		});
		
		tt = new Thread(nio);
		tt.start();
		
		SelfLogger.log("Started again.. waiting..");
		Thread.sleep(2000);
		
		SelfLogger.log("Stopping..");
		nio.shutdown();
		Thread.sleep(2000);
		
		
		System.exit(0);
	}
	
}
