package org.self.selfscript.values;

import org.self.selfscript.Contract;
import org.self.objects.base.MiniString;

public class StringValue extends Value {
	
	/**
	 * The String
	 */
	MiniString mScript;
	
	public StringValue(String zScript) {
		mScript = new MiniString( zScript );
		
		int len = getBytes().length;
		if(len > Contract.MAX_DATA_SIZE) {
			throw new IllegalArgumentException("MAX String length reached : "+len+"/"+Contract.MAX_DATA_SIZE);
		}
	}
	
	@Override
	public String toString() {
		return mScript.toString();
	}
	
	public byte[] getBytes(){
		return mScript.getData();
	}
	
	public MiniString getMiniString() {
		return mScript;
	}
	
	@Override
	public int getValueType() {
		return VALUE_SCRIPT;
	}
	
	public boolean isEqual(StringValue zValue) {
		return mScript.toString().equals(zValue.toString());
	}
	
	public StringValue add(StringValue zSCValue) {
		return new StringValue(mScript.toString()+zSCValue.toString());
	}
}
