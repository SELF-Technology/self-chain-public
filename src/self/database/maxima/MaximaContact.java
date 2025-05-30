package org.self.database.maxima;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.utils.SelfLogger;
import org.self.utils.SqlDB;
import org.self.utils.json.JSONObject;
import org.self.utils.json.parser.ParseException;

public class MaximaContact {

	/**
	 * Unique database ID
	 */
	public int 		mUID = 0;
	
	/**
	 * Extra data can be stored with the contact as a aJSON
	 */
	public JSONObject mExtraData = new JSONObject();
	
	/**
	 * The actual MAIN public Key of the Contact
	 */
	public String mPublicKey = "0x00";
	
	/**
	 * Where you contact them
	 */
	public String 	mCurrentAddress 	= "Sx00";
	
	/**
	 * Where they contact you
	 */
	public String 	mMyCurrentAddress	= "Sx00";
	
	/**
	 * Last Seen
	 */
	long mLastSeen = System.currentTimeMillis();
	
	public MaximaContact(String zPublicKey) {
		mPublicKey	= zPublicKey;
		
		setName("noname");
		setIcon("");
		
		setSelfAddress("Sx00");
		setBlockDetails(MiniNumber.ZERO, MiniNumber.ZERO, MiniData.ZERO_TXPOWID);
		setMLS("");
	}
	
	public MaximaContact(ResultSet zSQLResult) throws SQLException {
		mUID			= zSQLResult.getInt("id");
		mPublicKey		= zSQLResult.getString("publickey");
		mCurrentAddress	= zSQLResult.getString("currentaddress");
		mMyCurrentAddress	= zSQLResult.getString("myaddress");
		mLastSeen		= zSQLResult.getLong("lastseen");
		
		//Extra Data is a JSONOBject stored as bytes
		MiniData extrabytes = new MiniData(zSQLResult.getBytes("extradata")); 
		try {
			mExtraData	= SqlDB.convertDataToJSONObject(extrabytes);
		} catch (ParseException e) {
			SelfLogger.log(e);
			
			//Create a default
			mExtraData = new JSONObject();
			
			setName("noname");
			setIcon("");
			
			setSelfAddress("Sx00");
			setBlockDetails(MiniNumber.ZERO, MiniNumber.ZERO, MiniData.ZERO_TXPOWID);
			setMLS("");
		} 
	}
	
	public void setExtraData(JSONObject zExtra){
		mExtraData = zExtra;
	}
	
	public void setCurrentAddress(String zAddress) {
		mCurrentAddress = zAddress;
	}
	
	public void setMyAddress(String zMyAddress) {
		mMyCurrentAddress = zMyAddress;
	}
	
	public int getUID() {
		return mUID;
	}
	
	public void setSelfAddress(String zSxAddress) {
		mExtraData.put("selfaddress", zSxAddress);
	}
	
	public String getSelfAddress() {
		return mExtraData.getString("selfaddress","");
	}
	
	public void setName(String zName) {
		mExtraData.put("name", zName);
	}
	
	public String getName() {
		return mExtraData.getString("name","");
	}
	
	public void setIcon(String zIcon) {
		mExtraData.put("icon", zIcon);
	}
	
	public String getIcon() {
		return mExtraData.getString("icon","");
	}
	
	public JSONObject getExtraData() {
		return mExtraData;
	}
	
	public String getPublicKey() {
		return mPublicKey;
	}
	
	public String getCurrentAddress() {
		return mCurrentAddress;
	}
	
	public String getMyAddress() {
		return mMyCurrentAddress;
	}
	
	public void setLastSeen(long zLastSeen) {
		mLastSeen = zLastSeen;
	}
	
	public long getLastSeen() {
		return mLastSeen;
	}
	
	public void setBlockDetails(MiniNumber zTipBlock, MiniNumber zTipBlock50, MiniData zT50Hash) {
		mExtraData.put("topblock", zTipBlock.toString());
		mExtraData.put("checkblock", zTipBlock50.toString());
		mExtraData.put("checkhash", zT50Hash.to0xString());
	}
	
	public void setMLS(String zMLS) {
		mExtraData.put("mls",zMLS);
	}
	
	public String getMLS() {
		return mExtraData.getString("mls");
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		json.put("id", mUID);
		json.put("publickey", mPublicKey);
		json.put("currentaddress", mCurrentAddress);
		json.put("myaddress", mMyCurrentAddress);
		json.put("lastseen", mLastSeen);
		json.put("date", new Date(mLastSeen).toString());
		json.put("extradata", mExtraData);
		
		return json;
	}
}
