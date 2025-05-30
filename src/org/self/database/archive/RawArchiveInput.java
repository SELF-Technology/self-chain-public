package org.self.database.archive;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.self.database.cascade.Cascade;
import org.self.objects.IBD;
import org.self.objects.TxBlock;
import org.self.objects.base.MiniByte;
import org.self.objects.base.MiniNumber;
import org.self.utils.SelfLogger;

public class RawArchiveInput {

	File mFile;
	
	FileInputStream mFileIn;
	BufferedInputStream mBuffIn;
	GZIPInputStream mGzin;
	DataInputStream mDataIn;
	
	int mTotalFound=0;
	int mTotalAdded=0;
	
	Cascade mCascade = null;
	
	public RawArchiveInput(File zFile) {
		mFile = zFile;
	}
	
	public void connect() throws IOException {
		mFileIn = new FileInputStream(mFile);
		mBuffIn	= new BufferedInputStream(mFileIn,65536);
		mGzin	= new GZIPInputStream(mBuffIn, 65536);
		mDataIn	= new DataInputStream(mGzin);
		
		//is there a cascade
		if(MiniByte.ReadFromStream(mDataIn).isTrue()) {
			SelfLogger.log("Cascade found in RAW archive..");
			mCascade = Cascade.ReadFromStream(mDataIn);
		}
		
		//Now How many blocks in total..
		mTotalFound = MiniNumber.ReadFromStream(mDataIn).getAsInt();
		mTotalAdded = 0;
		
		SelfLogger.log("Blocks found in RAW Archive : "+mTotalFound);
	}
	
	public void stop() throws IOException {
		mDataIn.close();
		mGzin.close();
		mBuffIn.close();
		mFileIn.close();
	}
	
	public Cascade getCascade() {
		return mCascade;
	}
	
	public IBD getNextIBD() throws IOException {
		
		//Create new IBD
		IBD ret = new IBD();
		
		if(mTotalAdded<mTotalFound) {
		
			//Add the cascade..
			if(mTotalAdded == 0 && mCascade != null) {
				ret.setCascade(mCascade);
			}
			
			//Try and load 256 blocks
			TxBlock block;
			for(int i=0;i<256;i++) {
				
				//Load a block
				block = TxBlock.ReadFromStream(mDataIn);
				
				//Add to IBD..
				ret.getTxBlocks().add(block);
				
				mTotalAdded++;
				if(mTotalAdded>=mTotalFound) {
					//We are done..
					break;
				}
			}
		}
		
		return ret;
	}
}
