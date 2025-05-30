package org.self.system.mds.runnable;

import org.self.system.mds.handler.NETcommand;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.Scriptable;

public class NETService {

	/**
	 * Which MiniDAPP does this Contect apply to
	 */
	String 		mMiniDAPPID;
	String 		mMiniDAPPName;
	
	/**
	 * JS Context and Scope
	 */
	Context mContext;
	Scriptable 	mScope;
	
	public NETService(String zMiniDAPPID, String zMiniName,  Context zContext, Scriptable zScope) {
		mMiniDAPPID		= zMiniDAPPID;
		mMiniDAPPName	= zMiniName;
		mContext 		= zContext;
		mScope 			= zScope;
	}
	
	/**
	 * Make a GET request
	 */
	public void GET(String zURL) {
		GET(zURL,null);
	}
	
	public void GET(String zURL, Function zCallback) {
		
		//Create a Command and run it..
		NETcommand net 	= new NETcommand(mMiniDAPPID, zURL);
		String result 	= net.runCommand();
		
		//Send Info Back
		if(zCallback == null) {
			return;
		}
		
		//The arguments
		Object[] args = { NativeJSON.parse(mContext, mScope, result, new NullCallable()) };
		
		//Call the main MDS Function in JS
		zCallback.call(mContext, mScope, mScope, args);
	}
	
	/**
	 * Make a GET request WITH a Basic Auth token
	 */
	public void GETAUTH(String zURL, String zAuthToken) {
		GETAUTH(zURL,zAuthToken,null);
	}
	
	public void GETAUTH(String zURL, String zAuthToken, Function zCallback) {
		
		//Create a Command and run it..
		NETcommand net 	= new NETcommand(mMiniDAPPID, zURL, "", zAuthToken);
		String result 	= net.runCommand();
		
		//Send Info Back
		if(zCallback == null) {
			return;
		}
		
		//The arguments
		Object[] args = { NativeJSON.parse(mContext, mScope, result, new NullCallable()) };
		
		//Call the main MDS Function in JS
		zCallback.call(mContext, mScope, mScope, args);
	}
	
	/**
	 * Make a POST request
	 */
	public void POST(String zURL, String zData) {
		POST(zURL,zData,null);
	}
	
	public void POST(String zURL, String zData, Function zCallback) {
		
		//Create a Command and run it..
		NETcommand net 	= new NETcommand(mMiniDAPPID, zURL, zData);
		String result 	= net.runCommand();
		
		//Send Info Back
		if(zCallback == null) {
			return;
		}
		
		//The arguments
		Object[] args = { NativeJSON.parse(mContext, mScope, result, new NullCallable()) };
		
		//Call the main MDS Function in JS
		zCallback.call(mContext, mScope, mScope, args);
	}
}
