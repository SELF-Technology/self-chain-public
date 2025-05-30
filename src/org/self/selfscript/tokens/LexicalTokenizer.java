/**
 * 
 */
package org.self.selfscript.tokens;

import java.util.List;

import org.self.selfscript.exceptions.SelfParseException;

/**
 * @author Spartacus Rex
 *
 */
public class LexicalTokenizer{
	List<ScriptToken> mTokens;
	int 		mPos;
	int 		mSize;
	
	int mStackDepth;
	
	public LexicalTokenizer(List<ScriptToken> zTokens, int zStackDepth) {
		mTokens = zTokens;
		mPos 	= 0;
		mSize   = zTokens.size();
	}
	
	public ScriptToken getNextToken() throws SelfParseException {
		if(mPos >= mSize) {
			throw new SelfParseException("Run out of tokens!..");
		}
		return mTokens.get(mPos++);
	}
	
	public int getCurrentPosition() {
		return mPos;
	}
	
	public void goBackToken() throws SelfParseException {
		if(mPos==0 ) {
			throw new SelfParseException("LexicalTokenizer cannot go back as at 0 position");
		}
		mPos--;
	}
	
	public boolean checkAllTokensUsed() {
		return mPos == mSize;
	}
	
	public boolean hasMoreElements() {
		return mPos<mSize;
	}
	
	public int getStackDepth() {
		return mStackDepth;
	}
	
	public void incrementStackDepth() {
		mStackDepth++;
	}
	
	public void decrementStackDepth() {
		mStackDepth--;
	}
}
