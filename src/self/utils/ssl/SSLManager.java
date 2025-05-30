package org.self.utils.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;

import org.self.database.SelfDB;
import org.self.objects.base.MiniData;
import org.self.system.params.GeneralParams;
import org.self.utils.SelfLogger;

public class SSLManager {

	public static File getSSLFolder() {
		return new File(GeneralParams.DATA_FOLDER,"ssl"); 
	}
	
	public static File getKeystoreFile() {
		//Where are we storing the key file..
		File sslkeyfolder = getSSLFolder(); 
		sslkeyfolder.mkdirs();
		
		//The actual Key Store..
		File sslkeyfile = new File(sslkeyfolder,"sslkeystore"); 
		
		return sslkeyfile;
	}
	
	public static void makeKeyFile() {
		
		try {
			
			//Check file and password exist
			File sslfile 		 = getKeystoreFile();
			String keystorecheck = SelfDB.getDB().getUserDB().getString("sslkeystorepass", null);
			
			if(!sslfile.exists() || (keystorecheck==null)) {
				
				//Generate the key store..
				generateKeyStore();
						
			}else {
				
				//Try to load keystore..
				SelfLogger.log("Loading SSL Keystore.. ");
				
				KeyStore ks = getSSLKeyStore();
				if(ks == null) {
					//Problem..
					SelfLogger.log("Issue with keystore.. regenerate. Could be issues with UserDB..");
					
					//Some issue.. recreate..
					generateKeyStore();
				}
			}
			
		}catch(Exception exc) {
			SelfLogger.log(exc);
		}
	}
	
	private static void generateKeyStore() throws Exception {
		
		SelfLogger.log("Generating SSL Keystore.. "+KeyStore.getDefaultType());
		
		//Set a Random Key - and save DB
		String keystorepass = MiniData.getRandomData(32).to0xString(); 
		SelfDB.getDB().getUserDB().setString("sslkeystorepass", keystorepass);
		SelfDB.getDB().saveUserDB();
		
		// Create Key
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        KeyPair keyPair 			= keyPairGenerator.generateKeyPair();
        final X509Certificate cert 	= SelfSignedCertGenerator.generate(keyPair, "SHA256withRSA", "localhost", 730);
        KeyStore createkeystore 	= SelfSignedCertGenerator.createKeystore(cert, keyPair.getPrivate());

        // Save the File
        OutputStream fos = new FileOutputStream(getKeystoreFile());
        createkeystore.store(fos, keystorepass.toCharArray());
        fos.flush();
        fos.close();
	}
	
	public static KeyStore getSSLKeyStore() {
		try {
			//Get the keystore pass
			String keystorepass = SelfDB.getDB().getUserDB().getString("sslkeystorepass", null);
			
			// Load the keystore
	        KeyStore loadedKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        InputStream fis = new FileInputStream(getKeystoreFile());
	        loadedKeyStore.load(fis, keystorepass.toCharArray());
	        fis.close();
			
			return loadedKeyStore;
			
		}catch(Exception exc) {
			SelfLogger.log(exc.toString());
		}
		
		return null;
	}
	
	public static KeyManagerFactory getSSLKeyFactory(KeyStore zKeyStore) {
		try {
			//Get the keystore pass
			String keystorepass = SelfDB.getDB().getUserDB().getString("sslkeystorepass", null);
			
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(zKeyStore, keystorepass.toCharArray());

			return keyManagerFactory;
			
		}catch(Exception exc) {
			SelfLogger.log(exc);
		}
		
		return null;
	}
}
