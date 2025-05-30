package org.self.system.bridge.wire;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.utils.SelfLogger;

public class WireSecurityManager {
    private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";
    private static final String CURVE_NAME = "secp256k1";
    private static final String WIRE_PUBLIC_KEY_PREFIX = "WIRE_PUB_";
    private static final String WIRE_PRIVATE_KEY_PREFIX = "WIRE_PRIV_";
    
    private KeyPair keyPair;
    
    public WireSecurityManager() {
        try {
            // Generate ECDSA key pair
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec(CURVE_NAME));
            keyPair = generator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Wire security manager", e);
        }
    }
    
    /**
     * Generate Wire Network compatible public key
     */
    public String generatePublicKey() {
        try {
            PublicKey publicKey = keyPair.getPublic();
            byte[] encoded = publicKey.getEncoded();
            String base64 = Base64.getEncoder().encodeToString(encoded);
            return WIRE_PUBLIC_KEY_PREFIX + base64;
        } catch (Exception e) {
            SelfLogger.error("Error generating Wire public key: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Generate Wire Network compatible private key
     */
    public String generatePrivateKey() {
        try {
            PrivateKey privateKey = keyPair.getPrivate();
            byte[] encoded = privateKey.getEncoded();
            String base64 = Base64.getEncoder().encodeToString(encoded);
            return WIRE_PRIVATE_KEY_PREFIX + base64;
        } catch (Exception e) {
            SelfLogger.error("Error generating Wire private key: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Sign Wire transaction
     */
    public String signTransaction(MiniData zTxID, MiniNumber zAmount, String zDestination) {
        try {
            // Create transaction data to sign
            StringBuilder data = new StringBuilder();
            data.append(zTxID.toString());
            data.append(zAmount.toString());
            data.append(zDestination);
            
            // Create and initialize signature
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(keyPair.getPrivate());
            
            // Sign the transaction data
            signature.update(data.toString().getBytes());
            byte[] signed = signature.sign();
            
            // Convert to Wire Network compatible format
            String base64 = Base64.getEncoder().encodeToString(signed);
            return "WIRE_SIG_" + base64;
        } catch (Exception e) {
            SelfLogger.error("Error signing Wire transaction: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Verify Wire transaction signature
     */
    public boolean verifyTransaction(MiniData zTxID, MiniNumber zAmount, String zDestination, String signature, String publicKey) {
        try {
            // Extract base64 signature
            String base64Sig = signature.replace("WIRE_SIG_", "");
            byte[] decodedSig = Base64.getDecoder().decode(base64Sig);
            
            // Extract base64 public key
            String base64Pub = publicKey.replace("WIRE_PUB_", "");
            byte[] decodedPub = Base64.getDecoder().decode(base64Pub);
            
            // Create transaction data to verify
            StringBuilder data = new StringBuilder();
            data.append(zTxID.toString());
            data.append(zAmount.toString());
            data.append(zDestination);
            
            // Create and initialize signature
            Signature signatureVerifier = Signature.getInstance(SIGNATURE_ALGORITHM);
            signatureVerifier.initVerify(keyPair.getPublic());
            
            // Verify the signature
            signatureVerifier.update(data.toString().getBytes());
            return signatureVerifier.verify(decodedSig);
        } catch (Exception e) {
            SelfLogger.error("Error verifying Wire transaction: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate Wire Network compatible address
     */
    public String generateAddress() {
        try {
            PublicKey publicKey = keyPair.getPublic();
            byte[] encoded = publicKey.getEncoded();
            String base64 = Base64.getEncoder().encodeToString(encoded);
            return "wire_" + base64.substring(0, 40); // Truncate to 42 characters including prefix
        } catch (Exception e) {
            SelfLogger.error("Error generating Wire address: " + e.getMessage());
            return null;
        }
    }
}
