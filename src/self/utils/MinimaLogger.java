/**
 * 
 */
package org.self.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.self.system.SELFSystem;
import org.self.utils.json.JSONObject;

/**
 * @author Spartacus Rex
 *
 */
public class SelfLogger {
	
	public static final String SELF_LOG = "SELFLOG";
	
	public static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH );
	
	public static void log(String zLog){
		log(zLog, true);
	}
	
	public static void log(String zLog, boolean zNotify){
		long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		String full_log = "Self @ "+DATEFORMAT.format(new Date())+" ["+MiniFormat.formatSize(mem)+"] : "+zLog;
		System.out.println(full_log);
		
		//Create a Notify Message for the listeners
		if(zNotify && SELFSystem.getInstance() != null) {
			JSONObject data = new JSONObject();
			data.put("message", full_log);
			SELFSystem.getInstance().PostNotifyEvent(SELF_LOG, data);
		}
	}
	
	public static void log(Exception zException){
		log(zException,true);
	}
	
	public static void log(Exception zException, boolean zNotify){
		//First the Full Exception
		SelfLogger.log(zException.toString(), zNotify);
		
		//Now the Stack Trace
		for(StackTraceElement stack : zException.getStackTrace()) {
			//Print it..
			SelfLogger.log("     "+stack.toString(), zNotify);
		}
	}
	
	public static void logUncaught(Throwable zThrow, boolean zNotify){
		//First the Full Exception
		SelfLogger.log("[!] UNCAUGHT EXCEPTION : "+zThrow.toString(), zNotify);
		
		//Now the Stack Trace
		for(StackTraceElement stack : zThrow.getStackTrace()) {
			//Print it..
			SelfLogger.log("     "+stack.toString(), zNotify);
		}
	}
}
