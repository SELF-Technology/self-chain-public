package org.self.system.mds.runnable.shutter;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

public class SandboxNativeJavaObject extends NativeJavaObject {
	public SandboxNativeJavaObject(Scriptable scope, Object javaObject, Class staticType) {
		super(scope, javaObject, staticType);
	}
 
	@Override
	public Object get(String name, Scriptable start) {
		if (name.equals("getClass")) {
			return NOT_FOUND;
		}
 
		return super.get(name, start);
	}
}
