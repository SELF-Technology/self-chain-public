/**
 * 
 */
package org.self.utils.messages;

import org.self.database.SelfDB;
import org.self.utils.SelfLogger;

/**
 * @author Spartacus Rex
 *
 */
public abstract class MessageProcessor extends MessageStack implements Runnable{

	/**
	 * Main Thread loop
	 */
    private Thread mMainThread;
    
    /**
     * Are we running
     */
    private boolean mRunning;
    
    /**
     * Have we finbished shutting down
     */
    private boolean mShutDownComplete;
    
	/**
	 * LOG messages ?
	 */
	static private boolean mTrace 		= false;
	static private String mTraceFilter 	= "";
	
	public static void setTrace(boolean zTrace, String zTraceFilter) {
		mTrace 			= zTrace;
		mTraceFilter 	= zTraceFilter;
	}
	
	/**
	 * Processor Name
	 */
	String mName;
	
	/**
	 * Current Message being processed..
	 */
	Message mLastMessage = null;
	
	/**
	 * Constructor
	 */
    public MessageProcessor(String zName){
    	super();
    	
    	mName 				= zName;
    	mRunning 			= true;
    	mShutDownComplete 	= false;
    	
    	mMainThread = new Thread(this,zName);
        mMainThread.start();
    }
    
    public String getName() {
    	return mName;
    }
    
    public void setFullLogging(boolean zLogON, String zTraceFilter) {
    	mTrace 			= zLogON;
    	mTraceFilter 	= zTraceFilter;
    }
    
    public boolean isTrace() {
    	return mTrace;
    }
    
    public String getTraceFilter() {
    	return mTraceFilter;
    }
    
    public Message getLastMessage() {
    	return mLastMessage;
    }
    
    public boolean checkTraceFilter(String zMsg) {
    	if(mTrace && zMsg.toLowerCase().contains(mTraceFilter.toLowerCase())) {
    		return true;
    	}
    	
    	return false;
    }
    
    public boolean isRunning(){
    	return mRunning;
    }
    
    public boolean isShutdownComplete() {
    	return mShutDownComplete;
    }
    
    public void waitToShutDown() {
    	long timewaited = 0;
    	while(!isShutdownComplete()) {
			try {Thread.sleep(250);} catch (InterruptedException e) {}
			timewaited +=250;
			if(timewaited>15000) {
				SelfLogger.log("Failed to shutdown in 15 secs for "+mName);
				
				//Hard shutdown
				mShutDownComplete 	= true;
				mRunning			= false;
				
				mMainThread.interrupt();
				
				return;
			}
		}
    }
    
    public void stopMessageProcessor(){
        mRunning = false;
        
        //Wake it up if is locked..
        notifyLock();
    }
    
    public void PostTimerMessage(TimerMessage zMessage) {    	
    	//Set this is the processor..
    	zMessage.setProcessor(this);
    	
    	//Post it on the TimerProcessor
    	TimerProcessor.getTimerProcessor().PostMessage(zMessage);
    }
    
    public void run() {
    	
    	if(mTrace) {
        	SelfLogger.log("["+mMainThread.getName()+"] (stack:"+getSize()+") START", false);
        }
    	
    	//Loop while still running
        while(mRunning){
            //Check for valid mnessage
            Message msg = getNextMessage();
            
            //Cycle through available messages
            while(msg != null && mRunning){          
                //Process that message
                try{
                	//Check for trace
                	String tracemsg = msg.toString();
            		
                	//Are we logging  ?
                	long timenow = 0;
                	if(checkTraceFilter(tracemsg)) {
                		//Call can slow things down so only do if needed
                		timenow = System.currentTimeMillis();
                	}
                
                	//Store incase sys check
                	mLastMessage = msg;
                	
                	//Process Message
                    processMessage(msg);
                
                    if(checkTraceFilter(tracemsg)) {
                    	long timediff = System.currentTimeMillis() - timenow;
                    	SelfLogger.log("TRACE > ["+mMainThread.getName()+"] (stack:"+getSize()+") process_time_milli:"+timediff+" \t"+msg, false);
                    }
                    
                }catch(Error noclass){
                	SelfLogger.log("**SERIOUS ERROR "+msg.getMessageType()+" "+noclass.toString());
                	
                	//Now the Stack Trace
            		for(StackTraceElement stack : noclass.getStackTrace()) {
            			//Print it..
            			SelfLogger.log("     "+stack.toString());
            		}
            		
                }catch(Exception exc){
                	SelfLogger.log("MESSAGE PROCESSING ERROR @ "+msg.getMessageType());
                	SelfLogger.log(exc);
                
                }finally {
                	
                	//Make sure the write lock is released..
                	SelfDB.getDB().safeReleaseReadWriteLock();
				} 
                
                //Are there more messages..
                msg = getNextMessage();
            }
            
            //Wait.. for a notify.. 
            try {
            	synchronized (mLock) {
            		//Last check.. inside the LOCK
            		if(!isNextMessage() && mRunning) {
            			//Wait for a message to be posted on the stack
            			mLock.wait();	
            		}
				}
			} catch (InterruptedException e) {
				SelfLogger.log("MESSAGE_PROCESSOR "+mName+" INTERRUPTED");
			}
        }

        if(mTrace) {
        	SelfLogger.log("["+mMainThread.getName()+"] (stack:"+getSize()+") SHUTDOWN", false);
        }
        
        //All done..
        mRunning 			= false;
        mShutDownComplete 	= true;
    }
    
    /**
     * This is the main processing unit, must be overloaded in extends classes
     * @param zMessage The Full Message
     * @throws Exception
     */
    protected abstract void processMessage(Message zMessage) throws Exception;
}

