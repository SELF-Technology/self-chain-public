package org.self.objects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.utils.Crypto;
import org.self.utils.Streamable;
import org.self.utils.json.JSONObject;

/**
 * These Numbers define the capacity of the Self network
 * 
 * @author spartacusrex
 *
 */
public class Magic implements Streamable {

	/**
	 * Used to calculate the weighted averages
	 */
	private static final MiniNumber CALC_WEIGHTED		= new MiniNumber(16383);
	private static final MiniNumber CALC_TOTAL 			= new MiniNumber(16384);
	
	/**
	 * These are HARD limits that can NEVER Change
	 */
	private static final MiniNumber MINMAX_TXPOW_SIZE 			= new MiniNumber(64*1024);
	private static final MiniNumber MINMAX_SELFScript_OPERATIONS 	= new MiniNumber(1024);
	private static final MiniNumber MINMAX_TXPOW_TXNS			= new MiniNumber(256);
	
	/**
	 * Minimum acceptable PoW per TxPoW - Also a HARD limit
	 * 
	 * 10 KHashes is the minimum..also for Maxima messages
	 */
	public static final MiniNumber MIN_HASHES 		= new MiniNumber(10000);
	public static final BigInteger MIN_TXPOW_VAL 	= Crypto.MAX_VAL.divide(MIN_HASHES.getAsBigInteger());
	public static final MiniData MIN_TXPOW_WORK 	= new MiniData(MIN_TXPOW_VAL);
	
	/**
	 * Default Maximum size of a TxPoW unit.. Can change
	 */
	public static final MiniNumber DEFAULT_TXPOW_SIZE 	= new MiniNumber(64*1024);
	
	/**
	 * Default Maximum Number of executed SELFScript Operations
	 */
	public static final MiniNumber DEFAULT_SELFScript_OPERATIONS 	= new MiniNumber(1024);
	
	/**
	 * Default Maximum number of Txns per block
	 */
	public static final MiniNumber DEFAULT_TXPOW_TXNS	= new MiniNumber(256);
	
		
	/**
	 * The Current MAGIC numbers.. based on a weighted average of the chain..
	 * 
	 * This is ( 16383*the last current values + 1*Desired value ) / 16384
	 */
	public MiniNumber mCurrentMaxTxPoWSize;
	public MiniNumber mCurrentMaxSELFScriptOps;
	public MiniNumber mCurrentMaxTxnPerBlock;
	public MiniData   mCurrentMinTxPoWWork;
	
	public MiniNumber mDesiredMaxTxPoWSize;
	public MiniNumber mDesiredMaxSELFScriptOps;
	public MiniNumber mDesiredMaxTxnPerBlock;
	public MiniData   mDesiredMinTxPoWWork;
	
	public Magic() {
		mCurrentMaxTxPoWSize			= DEFAULT_TXPOW_SIZE;
		mCurrentMaxSELFScriptOps			= DEFAULT_SELFScript_OPERATIONS;
		mCurrentMaxTxnPerBlock			= DEFAULT_TXPOW_TXNS;
		mCurrentMinTxPoWWork			= MIN_TXPOW_WORK;
		
		mDesiredMaxTxPoWSize			= DEFAULT_TXPOW_SIZE;
		mDesiredMaxSELFScriptOps			= DEFAULT_SELFScript_OPERATIONS;
		mDesiredMaxTxnPerBlock        	= DEFAULT_TXPOW_TXNS;
		mDesiredMinTxPoWWork			= MIN_TXPOW_WORK;
	}
	
	public JSONObject toJSON() {
		JSONObject magic = new JSONObject();
		
		magic.put("currentmaxtxpowsize", mCurrentMaxTxPoWSize.toString());
		magic.put("currentmaxselfscriptops", mCurrentMaxSELFScriptOps.toString());
		magic.put("currentmaxtxn", mCurrentMaxTxnPerBlock.toString());
		magic.put("currentmintxpowwork", mCurrentMinTxPoWWork.to0xString());
		
		magic.put("desiredmaxtxpowsize", mDesiredMaxTxPoWSize.toString());
		magic.put("desiredmaxselfscriptops", mDesiredMaxSELFScriptOps.toString());
		magic.put("desiredmaxtxn", mDesiredMaxTxnPerBlock.toString());
		magic.put("desiredmintxpowwork", mDesiredMinTxPoWWork.to0xString());
		
		return magic;
	}
	
	public boolean checkSame(Magic zMagic) {
		boolean w = mCurrentMaxTxPoWSize.isEqual(zMagic.mCurrentMaxTxPoWSize);
		boolean x = mCurrentMaxSELFScriptOps.isEqual(zMagic.mCurrentMaxSELFScriptOps);
		boolean y = mCurrentMaxTxnPerBlock.isEqual(zMagic.mCurrentMaxTxnPerBlock);
		boolean z = mCurrentMinTxPoWWork.isEqual(zMagic.mCurrentMinTxPoWWork);
		
		return w && x && y && z;
	}
	
	/**
	 * Get the Magic Parameters
	 */
	public MiniNumber getMaxTxPoWSize() {
		return mCurrentMaxTxPoWSize;
	}
	
	public MiniNumber getMaxSELFScriptOps() {
		return mCurrentMaxSELFScriptOps;
	}
	
	public MiniNumber getMaxNumTxns() {
		return mCurrentMaxTxnPerBlock;
	}
	
	public MiniData getMinTxPowWork() {
		return mCurrentMinTxPoWWork;
	}
	
	/**
	 * Set the desired values..
	 */
	public void setDesiredSELFScript(MiniNumber zSELFScript) {
		mDesiredMaxSELFScriptOps = zSELFScript;
	}
	
	public void setDesiredMaxTxPoWSize(MiniNumber zMaxSize) {
		mDesiredMaxTxPoWSize = zMaxSize;
	}
	
	public void setDesiredMaxTxns(MiniNumber zMaxTxn) {
		mDesiredMaxTxnPerBlock = zMaxTxn;
	}
	
	
	/**
	 * Calculate the current MAX values by taking a heavily weighted average
	 * 
	 *  Desired calculated as >= x0.5 and <= x2
	 *  
	 */
	public Magic calculateNewCurrent() {
		
		//The New Magic Numbers
		Magic ret = new Magic();
		
		//TxPoWSize
		MiniNumber desired 	= mDesiredMaxTxPoWSize;
		MiniNumber min	 	= mCurrentMaxTxPoWSize.div(MiniNumber.TWO);
		MiniNumber max	 	= mCurrentMaxTxPoWSize.mult(MiniNumber.TWO);
		if(desired.isLess(min)) {
			desired = min;
		}else if(desired.isMore(max)) {
			desired = max;
		
		}
		
		//And finally - this is the minimum limit
		if(desired.isLess(MINMAX_TXPOW_SIZE)) {
			desired = MINMAX_TXPOW_SIZE;
		}
		
		ret.mCurrentMaxTxPoWSize 	= mCurrentMaxTxPoWSize.mult(CALC_WEIGHTED).add(desired).div(CALC_TOTAL);
		
		//SELFScriptOpS
		desired 	= mDesiredMaxSELFScriptOps;
		min	 		= mCurrentMaxSELFScriptOps.div(MiniNumber.TWO);
		max	 		= mCurrentMaxSELFScriptOps.mult(MiniNumber.TWO);
		if(desired.isLess(min)) {
			desired = min;
		}else if(desired.isMore(max)) {
			desired = max;
		}
		
		//And finally - this is the minimum limit
		if(desired.isLess(MINMAX_SELFScript_OPERATIONS)) {
			desired = MINMAX_SELFScript_OPERATIONS;
		}
		
		ret.mCurrentMaxSELFScriptOps	= mCurrentMaxSELFScriptOps.mult(CALC_WEIGHTED).add(desired).div(CALC_TOTAL);
		
		//Txns per block
		desired 	= mDesiredMaxTxnPerBlock;
		min	 		= mCurrentMaxTxnPerBlock.div(MiniNumber.TWO);
		max	 		= mCurrentMaxTxnPerBlock.mult(MiniNumber.TWO);
		if(desired.isLess(min)) {
			desired = min;
		}else if(desired.isMore(max)) {
			desired = max;
		}
		
		//And finally - this is the minimum limit
		if(desired.isLess(MINMAX_TXPOW_TXNS)) {
			desired = MINMAX_TXPOW_TXNS;
		}
		
		ret.mCurrentMaxTxnPerBlock	= mCurrentMaxTxnPerBlock.mult(CALC_WEIGHTED).add(desired).div(CALC_TOTAL);
		
		//Work is slightly different as is MiniData
		BigInteger two 	  = new BigInteger("2");
		BigInteger oldval = mCurrentMinTxPoWWork.getDataValue();
		BigInteger minval = oldval.divide(two);
		BigInteger maxval = oldval.multiply(two);
		
		BigInteger newval = mDesiredMinTxPoWWork.getDataValue();
		if(newval.compareTo(minval)<0) {
			newval = minval;
		}else if(newval.compareTo(maxval)>0) {
			newval = maxval;
		}
		
		//And finally - this is the minimum limit
		if(newval.compareTo(MIN_TXPOW_VAL) > 0) {
			newval = MIN_TXPOW_VAL;
		}
		
		//Now do the same calculation..
		BigInteger calc = oldval.multiply(CALC_WEIGHTED.getAsBigInteger()).add(newval).divide(CALC_TOTAL.getAsBigInteger()); 
		ret.mCurrentMinTxPoWWork = new MiniData(calc);	
		
		return ret;
	}
	
	@Override
	public String toString() {
		return toJSON().toString();
	}
	
	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		mCurrentMaxTxPoWSize.writeDataStream(zOut);
		mCurrentMaxSELFScriptOps.writeDataStream(zOut);
		mCurrentMaxTxnPerBlock.writeDataStream(zOut);
		mCurrentMinTxPoWWork.writeDataStream(zOut);
		
		mDesiredMaxTxPoWSize.writeDataStream(zOut);
		mDesiredMaxSELFScriptOps.writeDataStream(zOut);
		mDesiredMaxTxnPerBlock.writeDataStream(zOut);
		mDesiredMinTxPoWWork.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mCurrentMaxTxPoWSize	= MiniNumber.ReadFromStream(zIn);
		mCurrentMaxSELFScriptOps	= MiniNumber.ReadFromStream(zIn);
		mCurrentMaxTxnPerBlock 	= MiniNumber.ReadFromStream(zIn);
		mCurrentMinTxPoWWork 	= MiniData.ReadFromStream(zIn);
		
		mDesiredMaxTxPoWSize	= MiniNumber.ReadFromStream(zIn);
		mDesiredMaxSELFScriptOps	= MiniNumber.ReadFromStream(zIn);
		mDesiredMaxTxnPerBlock 	= MiniNumber.ReadFromStream(zIn);
		mDesiredMinTxPoWWork	= MiniData.ReadFromStream(zIn);
	}
	
	public static Magic ReadFromStream(DataInputStream zIn) throws IOException {
		Magic mag = new Magic();
		mag.readDataStream(zIn);
		return mag;
	}
	
	public static void main(String[] zArgs) {

		MiniNumber desired 	= new MiniNumber(3000);
	
		System.out.println("Start:1024 Desired:"+desired);
		
		int days=0;
		Magic current 		= new Magic();
//		current.mCurrentMaxSELFScriptOps = new MiniNumber(2000);
		for(int i=0;i<1728*50;i++) {
			if(i%1000==0) {
				current.mDesiredMaxSELFScriptOps = desired;
			}
			
			current = current.calculateNewCurrent();
			
			if(i%50 == 0) {
				days++;
				System.out.println(days+") "+current.mCurrentMaxSELFScriptOps);
			}
		}	
	}
}
