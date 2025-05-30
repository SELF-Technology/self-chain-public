package org.self.utils;

import java.lang.Thread.UncaughtExceptionHandler;

public class SelfUncaughtException implements UncaughtExceptionHandler {
	
	//The default Uncaught Exception.. 
	private static UncaughtExceptionHandler mDefaultUnCaught;

	public SelfUncaughtException() {
		//Get the current default
		mDefaultUnCaught = Thread.getDefaultUncaughtExceptionHandler();
	}
	
	@Override
	public void uncaughtException(Thread zThread, Throwable zThrowable) {
		SelfLogger.log("[!] UNCAUGHT EXCEPTION at THREAD "+zThread.getName());
		SelfLogger.logUncaught(zThrowable,true);
		
		//What type of error is it!
		if(zThrowable instanceof java.lang.OutOfMemoryError) {
			SelfLogger.log("[!] MEMORY ERROR.. SHUTTING DOWN");
			
			Runtime.getRuntime().halt(0);
			//System.exit(0);
			
			return;
			
		}else if(zThrowable instanceof org.h2.mvstore.MVStoreException) {
			SelfLogger.log("[!] H2 DATABASE MVStoreException.. SHUTTING DOWN");
			
			Runtime.getRuntime().halt(0);
			//System.exit(0);
			
			return;
		
		}else if(zThrowable instanceof java.util.concurrent.TimeoutException) {
			SelfLogger.log("[!] Concurrent Timeout Exception.. SHUTTING DOWN");
			
			Runtime.getRuntime().halt(0);
			//System.exit(0);
			
			return;
		}
		
		//Otherwise pass to the default..
		SelfLogger.log("[!] PASSING UNCAUGHT ERROR TO DEFAULT HANDLER");
		mDefaultUnCaught.uncaughtException(zThread, zThrowable);
	}
}
