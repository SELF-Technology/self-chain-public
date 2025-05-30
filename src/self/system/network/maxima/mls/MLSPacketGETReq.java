package org.self.system.network.maxima.mls;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.self.objects.base.MiniData;
import org.self.objects.base.MiniString;
import org.self.utils.SelfLogger;
import org.self.utils.Streamable;

public class MLSPacketGETReq implements Streamable {

	String mPublicKey;
	String mRandomUID;

	private MLSPacketGETReq() {}
	
	public MLSPacketGETReq(String zPublicKey, String zRandomUID){
		mPublicKey		= zPublicKey;
		mRandomUID		= zRandomUID;
	}
	
	public String getPublicKey() {
		return mPublicKey;
	}
	
	public String getRandomUID() {
		return mRandomUID;
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		MiniString.WriteToStream(zOut, mPublicKey);
		MiniString.WriteToStream(zOut, mRandomUID);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mPublicKey 	= MiniString.ReadFromStream(zIn).toString();
		mRandomUID 	= MiniString.ReadFromStream(zIn).toString();
	}
	
	public static MLSPacketGETReq ReadFromStream(DataInputStream zIn) throws IOException {
		MLSPacketGETReq mls = new MLSPacketGETReq();
		mls.readDataStream(zIn);
		return mls;
	}
	
	/**
	 * Convert a MiniData version into a MLSPacketGET
	 */
	public static MLSPacketGETReq convertMiniDataVersion(MiniData zTxpData) {
		ByteArrayInputStream bais 	= new ByteArrayInputStream(zTxpData.getBytes());
		DataInputStream dis 		= new DataInputStream(bais);
		
		MLSPacketGETReq mls = null;
		
		try {
			mls = MLSPacketGETReq.ReadFromStream(dis);
		
			dis.close();
			bais.close();
			
		} catch (IOException e) {
			SelfLogger.log(e);
		}
		
		return mls;
	}
}
