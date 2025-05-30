package org.self.utils.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.self.objects.base.MiniData;

public class SelfTrustManager implements X509TrustManager {

	public static TrustManager[] getTrustManagers() {
		return getTrustManagers(null);
	}
	
	public static TrustManager[] getTrustManagers(MiniData zSSLPubKey) {
		
		//Create an array of trust managers
		TrustManager[] trustmanagers = new TrustManager[1];
		
		//Create a manager
		trustmanagers[0] = new SelfTrustManager(zSSLPubKey);
		
		return trustmanagers;
	}

	/**
	 * The allowed SSL key - if any
	 */
	private MiniData mSSLPublicKey = null;
	
	public SelfTrustManager() {}
	
	public SelfTrustManager(MiniData zSSLPublicKey) {
		mSSLPublicKey = zSSLPublicKey;
	}
	
	@Override
	public void checkClientTrusted(X509Certificate[] zCerts, String arg1) throws CertificateException {
		if(mSSLPublicKey != null) {
			boolean found = false;
			int len = zCerts.length;
			for(int i=0;i<len;i++) {
				
				//Get the public key
				MiniData pubk = new MiniData(zCerts[i].getPublicKey().getEncoded());
				
				//Check it..
				if(pubk.isEqual(mSSLPublicKey)) {
					found = true;
					break;
				}
			}
			
			//Did we find it..
			if(!found) {
				throw new CertificateException("Invalid SSL Public Key ( not same as sslpubkey )");
			}
		}
	}

	@Override
	public void checkServerTrusted(X509Certificate[] zCerts, String arg1) throws CertificateException {
		if(mSSLPublicKey != null) {
			boolean found = false;
			int len = zCerts.length;
			for(int i=0;i<len;i++) {
				
				//Get the public key
				MiniData pubk = new MiniData(zCerts[i].getPublicKey().getEncoded());
				
				//Check it..
				if(pubk.isEqual(mSSLPublicKey)) {
					found = true;
					break;
				}
			}
			
			//Did we find it..
			if(!found) {
				throw new CertificateException("Invalid SSL Public Key");
			}
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}
}
