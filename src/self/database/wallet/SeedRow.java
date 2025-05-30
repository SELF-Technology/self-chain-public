package org.self.database.wallet;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.self.utils.json.JSONObject;

public class SeedRow {

	public String mPhrase;
	public String mSeed;
	
	public SeedRow(ResultSet zResults) throws SQLException {
		mPhrase 	= zResults.getString("phrase");
		mSeed 		= zResults.getString("seed");
	}
	
	public SeedRow(String zPhrase, String zSeed) throws SQLException {
		mPhrase 	= zPhrase;
		mSeed 		= zSeed;
	}
	
	public String getPhrase() {
		return mPhrase;
	}
	
	public String getSeed() {
		return mSeed;
	}
	
	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		ret.put("phrase", mPhrase);
		ret.put("seed", mSeed);
		return ret;
	}
}
