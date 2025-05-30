package org.self.objects.keys;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.self.database.mmr.MMRData;
import org.self.database.mmr.MMRProof;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.utils.Streamable;
import org.self.utils.json.JSONObject;

public class SignatureProof implements Streamable {

	private MiniData mPublicKey;
	
	private MiniData mSignature;
	
	private MMRProof mProof;
	
	private SignatureProof() {}

	public SignatureProof(MiniData zPublicKey, MiniData zSignature, MMRProof zProof) {
		mPublicKey 	= zPublicKey;
		mSignature 	= zSignature;
		mProof 		= zProof;
	}
	
	public MiniData getPublicKey() {
		return mPublicKey;
	}
	
	public MiniData getSignature() {
		return mSignature;
	}
	
	public MMRProof getProof() {
		return mProof;
	}
	
	public MiniData getRootPublicKey(){
		//Create the MMR data aentry
		MMRData pubentry = MMRData.CreateMMRDataLeafNode(mPublicKey, MiniNumber.ZERO);
		
//		return mProof.calculateProof(new MMRData(mPublicKey, MiniNumber.ZERO)).getData();
		return mProof.calculateProof(pubentry).getData();
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		json.put("publickey", mPublicKey.to0xString());
		json.put("rootkey", getRootPublicKey().to0xString());
		json.put("proof", mProof.toJSON());
		json.put("signature", mSignature.to0xString());

		return json;
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		mPublicKey.writeDataStream(zOut);
		mSignature.writeDataStream(zOut);
		mProof.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mPublicKey	= MiniData.ReadFromStream(zIn);
		mSignature 	= MiniData.ReadFromStream(zIn);
		mProof		= MMRProof.ReadFromStream(zIn);
	}
	
	public static SignatureProof ReadFromStream(DataInputStream zIn) throws IOException {
		SignatureProof sig = new SignatureProof();
		sig.readDataStream(zIn);
		return sig;
	}
}
