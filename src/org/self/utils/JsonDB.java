package org.self.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.objects.base.MiniString;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;
import org.self.utils.json.parser.JSONParser;
import org.self.utils.json.parser.ParseException;

public class JsonDB implements Streamable{

	/**
	 * Simple parameter JSON
	 */
	protected JSONObject mParams;
	
	public JsonDB() {
		mParams = new JSONObject();
	}
	
	public JSONObject getAllData() {
		return mParams;
	}

	public boolean exists(String zName) {
		return mParams.get(zName) != null;
	}
	
	/**
	 * Boolean functions
	 */
	public boolean getBoolean(String zName, boolean zDefault) {
		if(mParams.get(zName) == null) {
			return zDefault;
		}
		
		return (boolean)mParams.get(zName);
	}
	
	public void setBoolean(String zName, boolean zData) {
		mParams.put(zName, zData);
	}
	
	/**
	 * Number functions
	 */
	public MiniNumber getNumber(String zName, MiniNumber zDefault) {
		if(mParams.get(zName) == null) {
			return zDefault;
		}
		
		String number = (String) mParams.get(zName);
		
		return new MiniNumber(number);
	}
	
	public void setNumber(String zName, MiniNumber zNumber) {
		mParams.put(zName, zNumber.toString());
	}
	
	/**
	 * HEX Data functions
	 */
	public MiniData getData(String zName, MiniData zDefault) {
		if(mParams.get(zName) == null) {
			return zDefault;
		}
		
		String data = (String) mParams.get(zName);
		
		return new MiniData(data);
	}
	
	public void setData(String zName, MiniData zData) {
		mParams.put(zName, zData.toString());
	}
	
	
	/**
	 * String functions
	 */
	public String getString(String zName) {
		if(mParams.get(zName) == null) {
			return null;
		}
		
		return (String)mParams.get(zName);
	}
	
	public String getString(String zName, String zDefault) {
		if(mParams.get(zName) == null) {
			return zDefault;
		}
		
		return (String)mParams.get(zName);
	}
	
	public void setString(String zName, String zData) {
		mParams.put(zName, zData);
	}
	
	/**
	 * JSONObject
	 */
	public void setJSON(String zName, JSONObject zJSON) {
		mParams.put(zName, zJSON);
	}
	
	public JSONObject getJSON(String zName, JSONObject zDefault) {
		if(mParams.get(zName) == null) {
			return zDefault;
		}
		
		return (JSONObject)mParams.get(zName);
	}
	
	/**
	 * JSONArray
	 */
	public void setJSONArray(String zName, JSONArray zJSONArray) {
		mParams.put(zName, zJSONArray);
	}
	
	public JSONArray getJSONArray(String zName ) {
		if(mParams.get(zName) == null) {
			mParams.put(zName, new JSONArray());
		}
		
		return (JSONArray)mParams.get(zName);
	}
	
	/**
	 * Load and Save
	 */
	public void loadDB(File zFile) {
		MiniFile.loadObjectSlow(zFile, this);
	}
	
	public void saveDB(File zFile) {
		MiniFile.saveObjectDirect(zFile, this);
	}
	
//	public void loadEncryptedDB(String zPassword, File zFile) {
//		MiniFile.loadObjectEncrypted(zPassword, zFile, this);
//	}
//	
//	public void saveEncryptedDB(String zPassword, File zFile) {
//		MiniFile.saveObjectEncrypted(zPassword, zFile, this);
//	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		MiniString data = new MiniString(mParams.toString());
		data.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		MiniString data = MiniString.ReadFromStream(zIn);
		try {
			mParams = (JSONObject)(new JSONParser().parse(data.toString()));
		} catch (ParseException e) {
			SelfLogger.log(e);
			mParams = new JSONObject();
		}
	}

}
