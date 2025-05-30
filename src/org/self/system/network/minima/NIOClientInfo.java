package org.self.system.network.self;

import java.util.Date;

import org.self.utils.json.JSONObject;

public class NIOClientInfo {

	/**
	 * The MAin NIOClient
	 */
	NIOClient 	mNIOClient;
	
	/**
	 * Are we connected yet
	 */
	boolean 	mConnected;

	String mWelcome = "no welcome set..";
	
	String mUID;

	String mHost;

	int mPort;

	int mSelfPort;
	
	boolean isIncoming;

	boolean mValidGreeting;
	
	public NIOClientInfo(String uid, String zHost, int zPort, boolean zIsIncoming){
		mConnected = true;
		mUID = uid;
		mHost = zHost;
		mPort = zPort;
		isIncoming = zIsIncoming;
	}

	public NIOClientInfo(NIOClient zNIOClient, boolean zConnected) {
		mNIOClient 	= zNIOClient;
		mWelcome	= zNIOClient.getWelcomeMessage();
		mValidGreeting = zNIOClient.isValidGreeting();
		mConnected	= zConnected;
		mHost = zNIOClient.getHost();
		mPort = zNIOClient.getPort();
		mSelfPort = zNIOClient.getSelfPort();
		mUID = zNIOClient.getUID();
		isIncoming = zNIOClient.isIncoming();
	}
	
	public String getUID() {
		return mUID;
	}
	
	public boolean isIncoming() {
		return isIncoming;
	}
	
	public boolean isConnected() {
		return mConnected;
	}
	
	public String getHost() {return mHost;}
	
	public int getPort() {
		return mPort;
	}
	
	public int getSelfPort() {
		return mSelfPort;
	}
	
	public long getTimeConnected() {
		return mNIOClient.getTimeConnected();
	}
	
	public Object getExtraData() {
		return mNIOClient.getExtraData();
	}
	
	public void setExtrasData(Object zExtraData) {
		mNIOClient.setExtraData(zExtraData);
	}
	
	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		
		ret.put("welcome", mWelcome);
		ret.put("uid", getUID());
		ret.put("incoming", isIncoming());
		ret.put("host", getHost());
		ret.put("port", getPort());
		ret.put("selfport", getSelfPort());
		ret.put("isconnected", isConnected());
		ret.put("valid", mValidGreeting);
		if (mNIOClient != null) {
			ret.put("connected", new Date(getTimeConnected()).toString());
		}
		
		return ret;
	}

	public boolean ismValidGreeting() {
		return mValidGreeting;
	}
}
