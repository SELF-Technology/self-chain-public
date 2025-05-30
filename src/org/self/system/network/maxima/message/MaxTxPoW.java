package org.self.system.network.maxima.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

import org.self.database.SelfDB;
import org.self.objects.Magic;
import org.self.objects.Transaction;
import org.self.objects.TxPoW;
import org.self.objects.Witness;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniString;
import org.self.system.SELFSystem;
import org.self.system.brains.TxPoWGenerator;
import org.self.utils.Crypto;
import org.self.utils.Streamable;

public class MaxTxPoW implements Streamable {

	/**
	 * The version
	 */
	MiniString mVersion = new MiniString("1.0");
	
	/**
	 * The Maxima Message
	 */
	MaximaPackage mMaxima;
	
	/**
	 * The Payment - A mined TxPoW with the MaximaPackage hash as the PRNG 
	 */
	TxPoW mTxPoW;
	
	/**
	 * Max time to build a Maxima message - 15 seconds
	 */
	static long mMaxTimeMilli = 15000; 
	
	private MaxTxPoW() {}
	
	public MaxTxPoW(MaximaPackage zMaxima, TxPoW zTxPoW) {
		mMaxima = zMaxima;
		mTxPoW 	= zTxPoW;
	}

	public MiniString getVersion() {
		return mVersion;
	}
		
	public MaximaPackage getMaximaPackage() {
		return mMaxima;
	}
	
	public TxPoW getTxPoW() {
		return mTxPoW;
	}
	
	public boolean checkValidTxPoW() {
		
		//What is the hash of the MaximaPackage
		MiniData msghash = Crypto.getInstance().hashObject(mMaxima);
		
		//Check the custom hash of the TxPoW..
		return mTxPoW.getCustomHash().isEqual(msghash);
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		mVersion.writeDataStream(zOut);
		mMaxima.writeDataStream(zOut);
		mTxPoW.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mVersion = MiniString.ReadFromStream(zIn);
		mMaxima  = MaximaPackage.ReadFromStream(zIn);
		mTxPoW	 = TxPoW.ReadFromStream(zIn);
	}
	
	public static MaxTxPoW ReadFromStream(DataInputStream zIn) throws IOException {
		MaxTxPoW mp = new MaxTxPoW();
		mp.readDataStream(zIn);
		return mp;
	}
	
	public static MaxTxPoW createMaxTxPoW(MaximaPackage zMaxima) {		
	
		//What is the hash of this message
		MiniData msghash = Crypto.getInstance().hashObject(zMaxima);
				
		//Create a TXPOW unit around this package
		TxPoW txpow = TxPoWGenerator.generateTxPoW(new Transaction(), new Witness());
		
		//Now set the custom hash
		txpow.setCustomHash(msghash);
		
		//Get the Minimum allowed work..
		Magic magic = SelfDB.getDB().getTxPoWTree().getTip().getTxPoW().getMagic();
		
		//Min Difficulty
		MiniData minwork = magic.getMinTxPowWork();
		
		//Add 10%.. to give yourself some space
		BigDecimal hashes 	= minwork.getDataValueDecimal();
		hashes 				= hashes.divide(new BigDecimal("1.1"), MathContext.DECIMAL64);
		MiniData minhash 	= new MiniData(hashes.toBigInteger());
		txpow.setTxDifficulty(minhash);
		
		//Now Mine it..
		boolean valid = SELFSystem.getInstance().getTxPoWMiner().MineMaxTxPoW(true, txpow, mMaxTimeMilli);
		if(!valid) {
			return null;
		}
		
		//Now create a MaxTxPoW complete unit
		return new MaxTxPoW(zMaxima, txpow);
	}
	
}
