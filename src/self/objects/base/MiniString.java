package org.self.objects.base;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.self.utils.Streamable;

public class MiniString implements Streamable {

	/**
	 *  Self Charset
	 */
	public static Charset SELF_CHARSET = Charset.forName("UTF-8");
	
	/**
	 * The UTF-8 String Data
	 */
	String mString;
	
	public MiniString(String zString) {
		this(zString.getBytes(SELF_CHARSET));
	}
	
	public MiniString(byte[] zBytesData) {
		mString = new String(zBytesData,SELF_CHARSET);
	}
	
	public MiniString(MiniString zString) {
		mString = zString.toString();
	}
	
	public boolean isEqual(String zString) {
		return toString().equals(zString);
	}
	
	@Override
	public String toString() {
		return mString;
	}
	
	public byte[] getData() {
		return mString.getBytes(SELF_CHARSET);
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		MiniData strdata = new MiniData(getData());
		strdata.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		MiniData strdata = MiniData.ReadFromStream(zIn);
		mString = new String(strdata.getBytes(),SELF_CHARSET);
	}
	
	public static MiniString ReadFromStream(DataInputStream zIn) throws IOException{
		MiniString data = new MiniString("");
		data.readDataStream(zIn);
		return data;
	}
	
	public static void WriteToStream(DataOutputStream zOut, String zString) throws IOException{
		new MiniString(zString).writeDataStream(zOut);
	}
}

