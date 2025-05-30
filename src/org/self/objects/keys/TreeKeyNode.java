package org.self.objects.keys;

import org.self.database.mmr.MMR;
import org.self.database.mmr.MMRData;
import org.self.database.mmr.MMREntryNumber;
import org.self.database.mmr.MMRProof;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.utils.Crypto;

public class TreeKeyNode {

	int mSize;
	
	MMR mTree;
	
	TreeKeyNode[] mChildren;
	
	Winternitz[] mKeys;
	
	MiniData mChildSeed;
	MiniData mPublicKey;
	
	SignatureProof mParentChildSig = null;
	
	public TreeKeyNode(MiniData zPrivateSeed, int zSize) {
		
		//Need these
		mSize = zSize;
		
		//Hash it again.. so the children have a different base
		mChildSeed = Crypto.getInstance().hashObject(zPrivateSeed);
				
		//This many Children
		mChildren = new TreeKeyNode[zSize];
		
		//Create a new MMR Tree
		mTree = new MMR();
				
		//Create the Keys
		mKeys = new Winternitz[zSize];
		
		//Add 
		for(int i=0;i<zSize;i++) {
			
			//Create a new deterministic private seed for the key..
			MiniData seed = Crypto.getInstance().hashAllObjects(new MiniNumber(i),zPrivateSeed);
			
			//Create  NEW SingleKey
			Winternitz wots = new Winternitz(seed);
			
			//What is the PublicKey
			MiniData pubkey = wots.getPublicKey();
			
			//Create the MMRData entry
			MMRData pubentry = MMRData.CreateMMRDataLeafNode(pubkey, MiniNumber.ZERO);
			
			//Add this to the MMR..
			mTree.addEntry(pubentry);
			
			//Keep this in an array - for quick retrieval
			mKeys[i] = wots;
		}
		
		//What is the Public Key.. root of the MMR..
		mPublicKey = mTree.getRoot().getData();
	}
	
	public TreeKeyNode getChild(int zChild) {
		//Have we created it..
		if(mChildren[zChild] == null) {
			
			//Create a new deterministic private seed for child tree
			MiniData seed = Crypto.getInstance().hashAllObjects(new MiniNumber(zChild),mChildSeed);
			
			//Now create a new Child
			mChildren[zChild] = new TreeKeyNode(seed, mSize);
		}
		
		return mChildren[zChild];
	}
	
	public MiniData getPublicKey() {
		return mPublicKey;
	}
	
	public Winternitz getWOTSKey(int zKeyNum) {
		return mKeys[zKeyNum];
	}
	
	public MMRProof getProof(int zKeyNum) {
		return mTree.getProof(new MMREntryNumber(zKeyNum));
	}
	
	public boolean childSigExists() {
		return mParentChildSig != null;
	}
	
	public void setParentChildSig(SignatureProof zSignature) {
		mParentChildSig = zSignature;
	}
	
	public SignatureProof getParentChildSig() {
		return mParentChildSig;
	}
}
