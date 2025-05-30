package org.self.system.network.maxima.message;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.self.objects.base.MiniData;
import org.self.utils.SelfLogger;
import org.self.utils.Streamable;
import org.self.utils.json.JSONObject;

public class MaximaInternal implements Streamable {

	/**
	 * Who is this message from - Public Key
	 */
	public MiniData mFrom;
	
	/**
	 * The Complete Data - A MaximaMessage
	 */
	public MiniData mData;
	
	/**
	 * The Signature
	 */
	public MiniData mSignature;
	
	public MaximaInternal() {}

	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		
		ret.put("from", mFrom.to0xString());
		ret.put("data", mData.to0xString());
		ret.put("signature", mSignature.to0xString());
		
		return ret;
	}
	
	public static MaximaInternal ConvertMiniDataVersion(MiniData zData) {
		ByteArrayInputStream bais 	= new ByteArrayInputStream(zData.getBytes());
		DataInputStream dis 		= new DataInputStream(bais);
		
		MaximaInternal mm = new MaximaInternal();
		
		try {
			mm.readDataStream(dis);
			
			dis.close();
			bais.close();
			
		} catch (IOException e) {
			SelfLogger.log(e);
		}
		
		return mm;
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		mFrom.writeDataStream(zOut);
		mData.writeDataStream(zOut);
		mSignature.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mFrom 			= MiniData.ReadFromStream(zIn);
		mData 			= MiniData.ReadFromStream(zIn);
		mSignature 		= MiniData.ReadFromStream(zIn);
	}
	
	public static MaximaInternal ReadFromStream(DataInputStream zIn) throws IOException {
		MaximaInternal mm = new MaximaInternal();
		mm.readDataStream(zIn);
		return mm;
	}
}
