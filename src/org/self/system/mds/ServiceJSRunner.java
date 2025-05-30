package org.self.system.mds;

import java.io.File;

import org.self.database.minidapps.MiniDAPP;
import org.self.objects.base.MiniString;
import org.self.system.mds.runnable.MDSJS;
import org.self.system.mds.runnable.NullCallable;
import org.self.system.mds.runnable.api.APICallback;
import org.self.utils.MiniFile;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONObject;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageProcessor;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ServiceJSRunner extends MessageProcessor{

	public static String SERVICEJS_INIT = "SERVICEJS_INIT";
	public static String SERVICEJS_STOP = "SERVICEJS_STOP";
	public static String SERVICEJS_POLL = "SERVICEJS_POLL";
	
	MDSManager mMDS;
	
	MiniDAPP mMiniDapp;
	
	MDSJS mMDSJS;
	
	public ServiceJSRunner(MiniDAPP zDapp, MDSManager zMDS) {
		super("SERVICEJS_RUUNNER");
		
		mMiniDapp = zDapp;
		mMDS 	  = zMDS;
		
		PostMessage(SERVICEJS_INIT);
	}
	
	public MiniDAPP getMiniDapp() {
		return mMiniDapp;
	}
	
	public String getMiniDappID() {
		return mMiniDapp.getUID();
	}
	
	public void stopJS() {
		
		//Clear the message stack
		clear();
		
		//Send the shutdown message - must be from the processor thread
		PostMessage(SERVICEJS_STOP);
	}
	
	public void sendPollMessage(JSONObject zPollObject) {
		Message pollmessage = new Message(SERVICEJS_POLL);
		pollmessage.addObject("poll_object", zPollObject);
		PostMessage(pollmessage);
	}
	
	/**
	 * Initialise a MiniDAPP
	 */
	private void setupMiniDAPP() {
		
		//Is there a service.js class
		File service = new File(mMDS.getMiniDAPPWebFolder(mMiniDapp.getUID()),"service.js");
		if(service.exists()) {
			
			try {
				//Load the file..
				byte[] serv = MiniFile.readCompleteFile(service);
				String code = new String(serv,MiniString.SELF_CHARSET);
				
				//Load it into the service runner..
				Context ctx = Context.enter();
				
				ctx.setOptimizationLevel(-1);
				ctx.setLanguageVersion(Context.VERSION_ES6);
				ctx.setMaximumInterpreterStackDepth(1024);
				
				//Stop any JAVA classes from being run..
				ctx.setClassShutter(new ClassShutter() {
					public boolean visibleToScripts(String className) {					
						
						//ONLY MDSJS can be called form JS
						if(className.startsWith("org.self.system.mds.runnable")) {
							return true;
						}
							
						//SelfLogger.log("RHINOJS JAVA CLASS DENIED ACCESS : "+className);
						return false;
					}
				});
				
				//Create the Scope
				Scriptable scope = ctx.initStandardObjects();
				
				//Create an MDSJS object
				mMDSJS = new MDSJS(mMDS, mMiniDapp.getUID(), mMiniDapp.getName(), ctx, scope);
				ScriptableObject.putProperty(scope, "MDS", Context.javaToJS(mMDSJS, scope));
				
				//Add the main code to the Runnable
				ctx.evaluateString(scope, code, "<mds_"+mMiniDapp.getName()+"_"+mMiniDapp.getUID()+">", 1, null);
			
			}catch(Exception exc) {
				SelfLogger.log("ERROR starting service "+mMiniDapp.getName()+" "+exc);
			}
		}else {
			SelfLogger.log("ERROR starting service with no service.js : "+mMiniDapp.getName());
		}
	}
	
	@Override
	protected void processMessage(Message zMessage) throws Exception {
		
		if(zMessage.getMessageType().equals(SERVICEJS_INIT)) {
			
			//Start the service
			setupMiniDAPP();
		
			SelfLogger.log("Started service.js "+mMiniDapp.getName());
			
		}else if(zMessage.getMessageType().equals(SERVICEJS_STOP)) {
		
			//SelfLogger.log("ServiceJS Stopped : "+mMiniDapp.getName());
			
			try {
				mMDSJS.shutdown();
			}catch (Exception e) {
				SelfLogger.log(e);
			}
			
			stopMessageProcessor();
			
		}else if(zMessage.getMessageType().equals(SERVICEJS_POLL)) {
			
			JSONObject pollobj = (JSONObject) zMessage.getObject("poll_object");
			
			boolean processed = false;
			if(pollobj.getString("event").equals("MDSAPI")) {
				
				//SelfLogger.log("JSRUNNER REC MDSAPI "+pollobj.toString());
				
				//Get the data
				JSONObject dataobj = (JSONObject) pollobj.get("data");
				
				//Is it  a response..
				if(!(boolean)dataobj.get("request")) {
					
					processed = true;
					
					//Send to the API Call..
					APICallback api = mMDS.getAPICallback(dataobj.getString("id"));
					if(api != null) {
						
						//Construct a reply..
						JSONObject reply = new JSONObject();
						reply.put("status", dataobj.get("status"));
						reply.put("data", dataobj.get("message"));
						
						//Call it..
						Object[] args = { NativeJSON.parse(api.getContext(), 
									api.getScope(),reply.toString(), new NullCallable()) };
						
						//Call the main MDS Function in JS
						api.getFunction().call(api.getContext(), api.getScope(), api.getScope(), args);
						
					}else {
						//Has already been digested - this is probably the AUTOResponse.. 
						//SelfLogger.log("MDS API callback not found / already digested  : "+dataobj.toString());
					}
				}
			}

			//Forward it.. 
			if(!processed) {
				mMDSJS.callMainCallback(pollobj);
			}
		}
	}
	
}

