package org.self.system.commands.base;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.archive.ArchiveManager;
import org.self.database.txpowtree.TxPowTree;
import org.self.objects.Address;
import org.self.objects.base.MiniData;
import org.self.system.commands.Command;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONObject;

public class test extends Command {

	public test() {
		super("test","test Funxtion");
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"show","action"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
	
		SelfLogger.log("About to close and reopen DBs");
		
		SelfDB.getDB().refreshSQLDB();
		
		SelfLogger.log("DBs reopened..");
				
		
		return ret;
	}
	
	
	// get a file from the resources folder
    // works everywhere, IDEA, unit test and JAR file.
    private InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            SelfLogger.log("file not found! " + fileName);
        }
            
        return inputStream;

    }
	
	@Override
	public Command getFunction() {
		return new test();
	}

	public static void main(String[] zArgs) {
		
		System.out.println("Start test..");
		
		for(int i=0;i<100000;i++) {
			
			MiniData data = MiniData.getRandomData(32);
			
			String add = Address.makeSelfAddress(data);
			int len = add.length(); 
			
			if(len != 63) {
				System.out.println(len+" "+add+" "+data.to0xString());
				//System.out.println("NOT 63! "+);
			}
			
		}
		
		System.out.println("Finish test..");
		
		
		
	}
}