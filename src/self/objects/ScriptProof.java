package org.self.objects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.self.database.mmr.MMR;
import org.self.database.mmr.MMRData;
import org.self.database.mmr.MMREntry;
import org.self.database.mmr.MMRProof;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.objects.base.MiniString;
import org.self.utils.Streamable;
import org.self.utils.json.JSONObject;

public class ScriptProof implements Streamable {

	/**
	 * The Script
	 */
	MiniString mScript;
	
	/**
	 * The merkle proof chain -the root is equal to the Address
	 */
	MMRProof mProof;
	
	/**
	 * The Address
	 */
	Address mAddress;
	
	private ScriptProof() {}
	
	public ScriptProof(String zScript) {
	
		//DON'T NEED TO DO THIS FOR 1 SCRIPT BUT SHOWS HOW MULTIPLE SCRIPTS COULD BE ADDED
		
		//Store the script
		mScript = new MiniString(zScript);
		
		//Create an MMR proof..
		MMR mmr = new MMR();
		
		//Create a new piece of data to add
		MMRData scriptdata = MMRData.CreateMMRDataLeafNode(mScript, MiniNumber.ZERO);
		
		//Add to the MMR
		MMREntry entry = mmr.addEntry(scriptdata);
		
		//Get the MMRProof
		mProof = mmr.getProof(entry.getEntryNumber());
		
		//Calculate the root address
		calculateAddress();
	}
	
	public ScriptProof(String zScript, MMRProof zProof) {
	
		//Store the script
		mScript = new MiniString(zScript);
		
		//The proof..
		mProof = zProof;
		
		//Calculate the root address
		calculateAddress();
	}
	
	/**
	 * The final address this represents
	 */
	private void calculateAddress() {
		
		//Hash it..
		MMRData scriptdata = MMRData.CreateMMRDataLeafNode(mScript, MiniNumber.ZERO);
		
		//And calulate the finsl root..
		MMRData root = mProof.calculateProof(scriptdata);
				
		//The address is the final hash
		mAddress = new Address(root.getData()); 
	}
	
	public MiniString getScript() {
		return mScript;
	}
	
	public MMRProof getProof() {
		return mProof;
	}
	
	public Address getAddress() {
		return mAddress;
	}
	
	public MiniData getAddressData() {
		return mAddress.getAddressData();
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("script", mScript.toString());
		json.put("address", mAddress.getAddressData().to0xString());
		json.put("proof", mProof.toJSON());
		return json;
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		mScript.writeDataStream(zOut);
		mProof.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mScript = MiniString.ReadFromStream(zIn);
		mProof	= MMRProof.ReadFromStream(zIn);
		
		calculateAddress();
	}
	
	public static ScriptProof ReadFromStream(DataInputStream zIn) throws IOException {
		ScriptProof pscr = new ScriptProof();
		pscr.readDataStream(zIn);
		return pscr;
	}
	
}
