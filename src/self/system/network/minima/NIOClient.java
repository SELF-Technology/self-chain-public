package org.self.system.network.self;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;

import org.self.objects.base.MiniData;
import org.self.system.SELFSystem;
import org.self.utils.FastByteArrayStream;
import org.self.utils.MiniFormat;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONObject;
import org.self.utils.messages.Message;

public class NIOClient {

	/**
	 * Show debug information
	 */
	public static boolean mTraceON = false;
	
	/**
	 * 8K buffer for send and receive.. 8KB
	 */
	public static final int MAX_NIO_BUFFERS = 8 * 1024;

	/**
	 * The Maximum size of a single message 256MB
	 */
	public static final int MAX_MESSAGE 	= 256 * 1024 * 1024;
	
	String mUID;
	
	SelectionKey mKey;
	
	ByteBuffer mBufferIn;
	int mReadCurrentPosition 	= 0;
    int mReadCurrentLimit 		= 0;
    
    byte[]	mReadByteArrayTemp					= null;
	FastByteArrayStream mReadByteArray 	= null;
	
	ByteBuffer mBufferOut;
	int mWritePosition 			= 0;
	int mWriteLimit				= 0;
	boolean mWriteStart			= false;
	byte[] mWriteData			= null;
	
	SocketChannel 	mSocket;
	
	String 			mHost;
	int 			mPort;
	
	int 			mSelfPort=-1;
	
	boolean mIncoming;
	
	private ArrayList<MiniData> mMessages;
	
	NIOManager mNIOManager;
	
	String mWelcomeMessage = "";
	
	long mTimeConnected = 0;
	
	long mLastMessageRead;
	
	int mConnectAttempts = 1;
	
	boolean mValidGreeting = false;
	
	boolean mSentGreeting = false;
	
	boolean mP2PGreeting = false;
	
	String mMaximaIdent = "";
	String mMaximaMLS 	= "";
	
	boolean mHasMaximaDisconnected = false;
	
	/**
	 * Specify extra info
	 */
	private Object mExtraData = null;
	
	public NIOClient(String zHost, int zPort) {
		mUID 		= MiniFormat.createRandomString(8);
		mHost		= zHost;
		mPort		= zPort;
		mIncoming	= false;
		mNIOManager = SELFSystem.getInstance().getNetworkManager().getNIOManager();
	}
	
	public NIOClient(boolean zIncoming, String zHost, int zPort, SocketChannel zSocket, SelectionKey zKey) {
        mUID 		= MiniFormat.createRandomString(8);
        
        mHost 	= zHost;
        mPort 	= zPort;
        mSocket	= zSocket;
        mKey	= zKey;
        
        mIncoming	= zIncoming;
        
        //Max buffer chunks for read and write
        mBufferIn 	= ByteBuffer.allocate(MAX_NIO_BUFFERS);
        mBufferOut 	= ByteBuffer.allocate(MAX_NIO_BUFFERS);
        
        //Create the Read array
        mReadByteArrayTemp = new byte[MAX_NIO_BUFFERS];
        
        //Writing
        mMessages 				= new ArrayList<>();
        
    	mNIOManager = SELFSystem.getInstance().getNetworkManager().getNIOManager();
    	
    	mTimeConnected 		= System.currentTimeMillis();
    	mLastMessageRead 	= mTimeConnected; 
    }
	
	@Override
	public String toString() {
		return toJSON().toString();
	}
	
	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		
		ret.put("welcome", mWelcomeMessage);
		ret.put("uid", getUID());
		ret.put("incoming", isIncoming());
		ret.put("host", mHost);
		ret.put("port", mPort);
		ret.put("selfport", mSelfPort);
		ret.put("connected", new Date(mTimeConnected).toString());
		ret.put("valid", mValidGreeting);
		ret.put("sentgreeting", mSentGreeting);
		ret.put("maxima", mMaximaIdent);
		ret.put("maximamls", mMaximaMLS);
		
		return ret;
	}
	
	public void setExtraData(Object zExtraData) {
		mExtraData = zExtraData;
	}
	
	public Object getExtraData() {
		return mExtraData;
	}
	
	public String getUID() {
		return mUID;
	}
	
	public boolean isIncoming() {
		return mIncoming;
	}
	
	public boolean isOutgoing() {
		return !mIncoming;
	}
	
	public boolean isMaximaClient() {
		return !mMaximaIdent.equals("");
	}
	
	public void setMaximaIdent(String zMaxima) {
		mMaximaIdent = zMaxima;
	}
	
	public String getMaximaIdent() {
		return mMaximaIdent;
	}
	
	public boolean isMaximaMLS() {
		return !mMaximaMLS.equals("");
	}
	
	public void setMaximaMLS(String zMaximaMLS) {
		mMaximaMLS = zMaximaMLS;
	}
	
	public String getMaximaMLS() {
		return mMaximaMLS;	
	}
	
	public boolean hasMaximaDiscxonnected() {
		return mHasMaximaDisconnected;
	}
	
	public void setMaximaDisconnected() {
		mHasMaximaDisconnected = true;
	}
	
	public void overrideHost(String zHost) {
		mHost = zHost;
	}
	
	public String getHost() {
		return mHost;
	}
	
	public void setPort(int zPort) {
		mPort = zPort;
	}
	
	public int getPort() {
		return mPort;
	}
	
	public void setSelfPort(int zPort) {
		mSelfPort = zPort;
	}
	
	public int getSelfPort() {
		return mSelfPort;
	}
	
	public String getFullAddress() {
		if(mSelfPort != -1) {
			return mHost+":"+mSelfPort;
		}
		
		return mHost+":"+mPort;
	}
	
	public String getFullSelfAddress() {
		return getFullAddress();
	}
	
	public boolean isValidGreeting() {
		return mValidGreeting;
	}
	
	public void setValidGreeting(boolean zValid) {
		mValidGreeting = zValid;
	}
	
	public boolean haveSentGreeting() {
		return mSentGreeting;
	}
	
	public void setReceivedP2PGreeting() {
		mP2PGreeting = true;
	}
	
	public boolean hasReceivedP2PGreeting() {
		return mP2PGreeting;
	}
	
	public void setSentGreeting(boolean zSent) {
		mSentGreeting = zSent;
	}
	
	public String getWelcomeMessage() {
		return mWelcomeMessage;
	}
	
	public void setWelcomeMessage(String zWelcome) {
		mWelcomeMessage = zWelcome;
	}
	
	public long getTimeConnected() {
		return  mTimeConnected;
	}
	
	public long getLastReadTime() {
		return mLastMessageRead;
	}
	
	public int getConnectAttempts() {
		return mConnectAttempts;
	}
	
	public void incrementConnectAttempts() {
		mConnectAttempts++;
	}
	
	public void setConnectAttempts(int zConnectAttempts) {
		mConnectAttempts = zConnectAttempts;
	}
	
	public void sendData(MiniData zData) {
		synchronized (mMessages) {
			if(mKey.isValid()) {
				mMessages.add(zData);
			
				//And now say we want to write..
				mKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				mKey.selector().wakeup();
			}
		}
	}
	
	private boolean isNextData() {
		synchronized (mMessages) {
			return mMessages.size()>0;
		}
	}
	
	private MiniData getNextData() {
		synchronized (mMessages) {
			if(mMessages.size()>0) {
				return mMessages.remove(0);
			}
		}
		
		return  null;
	}
	
	public void handleRead() throws IOException {
		
		//read in..
 	   	int readbytes = mSocket.read(mBufferIn);
 	   	if(readbytes == -1) {
 	   		throw new IOException("Socket Closed!");
 	   	}
 	   
 	   	//Debug
 		if(mTraceON) {
 			SelfLogger.log("[NIOCLIENT] "+mUID+" read "+readbytes, false);
 		}
 		
 		//Add to the Traffic Listener..
 	   	mNIOManager.getTrafficListener().addToTotalRead(readbytes);
		
 	   	//Nothing..
 	   	if(readbytes == 0) {
 	   		return;
 	   	}
 	   	
 	   	//Ready to read
// 	   	mBufferIn.flip();
 	   ((Buffer) mBufferIn).flip();
 	   	
 	   	while(mBufferIn.hasRemaining()) {
 	   	
 	   		//What are we reading
 	   		if(mReadByteArray == null) {
 	   			
 	   			//Do we have enough for the size..
 	   			if(mBufferIn.remaining() >= 4) {
 	   				mReadCurrentLimit 		= mBufferIn.getInt();
 	   				mReadCurrentPosition 	= 0;
 	   				
 	   				//Check MAX size..
 	   				if(mReadCurrentLimit > MAX_MESSAGE) {
 	   					//Message too big..
 	   					throw new IOException("Message too big for read! "+mReadCurrentLimit);
 	   				}
 	   				
 	   				//Create a new array - with initial capacity of 64k
 	   				mReadByteArray 	= new FastByteArrayStream(mReadCurrentLimit);
 	   				
 	   			}else {
 	   				//Not enough for the size..
 	   				break;
 	   			}
 	   		}
 	   		
 	   		//We have something..
 	   		if(mReadByteArray != null) {
 	   			//How much left to read for this object
				int readremaining = mReadCurrentLimit - mReadCurrentPosition;
				   
				//How much is there still to read
				int buffread = mBufferIn.remaining();
				if(buffread > readremaining) {
					buffread = readremaining;
				}
				
				//Copy into the temp array
				mBufferIn.get(mReadByteArrayTemp, 0, buffread);
				
				//Now write to the ByteArrayOutputStream
				mReadByteArray.writeData(mReadByteArrayTemp, 0, buffread);
				mReadCurrentPosition += buffread;
				
				//Are we done..
				if(mReadCurrentPosition == mReadCurrentLimit) {
					
					//Get  all the data
					byte[] allreaddata = mReadByteArray.toByteArray(); 
					
					//Post it !
					Message msg = new Message(NIOManager.NIO_INCOMINGMSG);
					msg.addString("uid", mUID);
					msg.addString("fullhost", getFullAddress());
					msg.addObject("data", new MiniData(allreaddata));
					mNIOManager.PostMessage(msg);
					
					//New array required..
					mReadByteArray = null;
					
					//Last message we have received from this client
					mLastMessageRead = System.currentTimeMillis();
				}
 	   		}
 	   	}
 	   	
		//ready to read more..
		mBufferIn.compact();
	}
	
	public void handleWrite() throws IOException {
		
		//First fill the buffer if it has the space
		while(mBufferOut.hasRemaining()) {
			
			//Do we have a packet we are working on
			if(mWriteData == null) {
				
				//Get the next packet
				if(isNextData()) {
					mWriteData 		= getNextData().getBytes();
					
					//Check MAX
					if(mWriteData.length > MAX_MESSAGE) {
						//Error Message too Big!
						SelfLogger.log("ERROR : Trying to write a message that is too big! "+mWriteData.length);
					
						//Hmm.. don't write it..
						mWriteData = null;
						break;
					}
					
					mWritePosition 	= 0;
					mWriteLimit 	= mWriteData.length; 
					mWriteStart		= false;
				}else {
					//Nothing to add
					break;
				}
			}
			
			//We have data to write
			if(mWriteData != null) {
				
				//Have we written the size yet
				if(!mWriteStart) {
					if(mBufferOut.remaining() >= 4) {
						mBufferOut.putInt(mWriteLimit);
						mWriteStart = true;
					}else {
						//Not enough space to write the size
						break;
					}
				}
				
				if(mWriteStart) {
					//How much left in the buffer
					int remaining = mBufferOut.remaining();
					
					//How much left to write
					int writeremain = mWriteLimit - mWritePosition;
					if(writeremain > remaining) {
						writeremain = remaining; 
					}
					   
					//Copy that to the buffer..   
					mBufferOut.put(mWriteData, mWritePosition, writeremain);
					mWritePosition += writeremain;
					
					//Have we finished
					if(mWritePosition == mWriteLimit) {
						mWriteData = null;
					}
				}
			}
		}
		
		//Ready to write
//		mBufferOut.flip();
		((Buffer) mBufferOut).flip();
		
		//Write
		int write = mSocket.write(mBufferOut);
		if(mTraceON) {
			SelfLogger.log("[NIOCLIENT] "+mUID+" wrote : "+write, false);
		}
		
		//Add to the Traffic Listener..
 	   	mNIOManager.getTrafficListener().addToTotalWrite(write);
		
		//Any left
		synchronized (mMessages) {
			if(!mBufferOut.hasRemaining() && mMessages.size()==0 && mWriteData == null) {
				if(mKey.isValid()) {
					//Only interested in READ
					mKey.interestOps(SelectionKey.OP_READ);
				}
			}
		}
		
		//Compact..
		mBufferOut.compact();
	}
	
	public void disconnect() {
        try {
     	   mKey.cancel();
     	   mSocket.close();
     	} catch (Exception ioe) {
     		
     	}
    }
}
