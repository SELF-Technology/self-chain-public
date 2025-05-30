package org.self.objects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.self.objects.base.SELFByte;
import org.self.objects.base.SELFData;
import org.self.objects.base.SELFNumber;
import org.self.system.params.GlobalParams;
import org.self.utils.Streamable;
import org.self.utils.SelfLogger;

public class ValidationMessage extends TxPoW implements Streamable {
    
    /**
     * Validation-specific fields
     */
    private SELFNumber mValidationScore;
    private SELFNumber mDifficulty;
    private SELFData mValidatorID;
    private SELFNumber mValidationTime;
    private SELFData mSourceChainID;
    private SELFData mTargetChainID;
    
    /**
     * Constructor for validation message
     */
    public ValidationMessage() {
        super();
        mValidationScore = SELFNumber.ZERO;
        mDifficulty = SELFNumber.ZERO;
        mValidatorID = SELFData.ZERO_TXPOWID;
        mValidationTime = SELFNumber.ZERO;
    }
    
    /**
     * Setters and Getters
     */
    public SELFNumber getValidationScore() {
        return mValidationScore;
    }
    
    public void setValidationScore(SELFNumber score) {
        mValidationScore = score;
    }
    
    public SELFNumber getDifficulty() {
        return mDifficulty;
    }
    
    public void setDifficulty(SELFNumber difficulty) {
        mDifficulty = difficulty;
    }
    
    public SELFData getValidatorID() {
        return mValidatorID;
    }
    
    public void setValidatorID(SELFData validatorID) {
        mValidatorID = validatorID;
    }
    
    public SELFNumber getValidationTime() {
        return mValidationTime;
    }
    
    public void setValidationTime(SELFNumber time) {
        mValidationTime = time;
    }
    
    public SELFData getSourceChainID() {
        return mSourceChainID;
    }
    
    public void setSourceChainID(SELFData chainID) {
        mSourceChainID = chainID;
    }
    
    public SELFData getTargetChainID() {
        return mTargetChainID;
    }
    
    public void setTargetChainID(SELFData chainID) {
        mTargetChainID = chainID;
    }
    
    /**
     * Streamable Implementation
     */
    @Override
    public byte[] ToByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // Write base TxPoW data
        super.ToByteArray(dos);
        
        // Write validation-specific data
        dos.writeBytes(mValidationScore.toByteArray());
        dos.writeBytes(mDifficulty.toByteArray());
        dos.writeBytes(mValidatorID.toByteArray());
        dos.writeBytes(mValidationTime.toByteArray());
        dos.writeBytes(mSourceChainID.toByteArray());
        dos.writeBytes(mTargetChainID.toByteArray());
        
        return baos.toByteArray();
    }
    
    public static ValidationMessage ReadFromByteArray(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        
        // Read base TxPoW data
        TxPoW txpow = TxPoW.ReadFromStream(dis);
        
        // Create validation message
        ValidationMessage validation = new ValidationMessage();
        
        // Read validation-specific data
        validation.setValidationScore(SELFNumber.ReadFromStream(dis));
        validation.setDifficulty(SELFNumber.ReadFromStream(dis));
        validation.setValidatorID(SELFData.ReadFromStream(dis));
        validation.setValidationTime(SELFNumber.ReadFromStream(dis));
        validation.setSourceChainID(SELFData.ReadFromStream(dis));
        validation.setTargetChainID(SELFData.ReadFromStream(dis));
        
        validation.setHeader(txpow.getHeader());
        validation.setBody(txpow.getBody());
        
        // Read validation-specific data
        validation.setValidationScore(SELFNumber.ReadFromStream(dis));
        validation.setDifficulty(SELFNumber.ReadFromStream(dis));
        validation.setValidatorID(SELFData.ReadFromStream(dis));
        validation.setValidationTime(SELFNumber.ReadFromStream(dis));
        
        return validation;
    }
    
    /**
     * Helper methods
     */
    public SELFNumber calculateValidationScore() {
        // TODO: Implement AI validation score calculation
        return SELFNumber.ONE;
    }
    
    public boolean isValid() {
        // TODO: Implement validation verification
        return true;
    }
    
    public String toString() {
        return "ValidationMessage{" +
                "txpowid=" + getTxPoWID() +
                ", score=" + mValidationScore +
                ", difficulty=" + mDifficulty +
                ", validator=" + mValidatorID +
                ", time=" + mValidationTime +
                '}';
    }
}
