package org.self.system.brains;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.self.database.SelfDB;
import org.self.database.mmr.MMRData;
import org.self.database.txpowdb.TxPoWDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.database.userprefs.UserDB;
import org.self.objects.Coin;
import org.self.objects.CoinProof;
import org.self.objects.Magic;
import org.self.objects.Transaction;
import org.self.objects.TxBlock;
import org.self.objects.ValidationMessage;
import org.self.objects.Witness;
import org.self.objects.base.SELFData;
import org.self.objects.base.SELFNumber;
import org.self.system.params.SELFParams;
import org.self.utils.Crypto;
import org.self.utils.SelfLogger;

public class ValidationGenerator {
    
    /**
     * The Bounding range for validation difficulty change
     */
    private final static SELFNumber MAX_DIFFICULTY_CHANGE = new SELFNumber("2.0");
    private final static SELFNumber MIN_DIFFICULTY_CHANGE = new SELFNumber("0.5");
    
    /**
     * The minimum validation score required
     */
    private static SELFNumber MIN_VALIDATION_SCORE = new SELFNumber("0.5");
    
    /**
     * The maximum number of witnesses
     */
    private static final int MAX_WITNESSES = 5;
    
    /**
     * The minimum number of witnesses
     */
    private static final int MIN_WITNESSES = 3;
    
    /**
     * The minimum witness reputation required
     */
    private static final SELFNumber MIN_WITNESS_REPUTATION = new SELFNumber("0.7");
    
    /**
     * Generate a validation task
     */
    public static ValidationMessage generateValidationTask(Transaction zTransaction, Witness zWitness) {
        // Create the validation message
        ValidationMessage validation = new ValidationMessage();
        
        // Current top block
        TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
        
        // Set the block number
        validation.setHeader(new TxHeader());
        validation.getHeader().setBlockNumber(tip.getTxPoW().getBlockNumber().increment());
        
        // Set the current time
        SELFNumber currentTime = SELFNumber.fromLong(SELFSystem.getInstance().getSystemTime());
        validation.getHeader().setTime(currentTime);
        
        // Set the transaction and witness
        validation.setTransaction(zTransaction);
        validation.setWitness(zWitness);
        
        // Generate witnesses
        ArrayList<Witness> witnesses = selectWitnesses();
        validation.getBody().setWitnesses(witnesses);
        
        // Calculate validation difficulty
        SELFNumber difficulty = calculateValidationDifficulty();
        validation.getHeader().setDifficulty(difficulty);
        
        // Calculate validation score
        SELFNumber score = calculateValidationScore(zTransaction, zWitness);
        validation.setValidationScore(score);
        
        // Sign the validation
        signValidation(validation);
        
        return validation;
    }
    
    /**
     * Select witnesses for validation
     */
    private static ArrayList<Witness> selectWitnesses() {
        ArrayList<Witness> witnesses = new ArrayList<>();
        
        // Get all eligible validators
        ArrayList<Witness> validators = SelfDB.getDB().getEligibleValidators();
        
        // Sort by reputation
        validators.sort((v1, v2) -> {
            SELFNumber rep1 = SelfDB.getDB().getValidatorAverageScore(v1.getValidatorID());
            SELFNumber rep2 = SelfDB.getDB().getValidatorAverageScore(v2.getValidatorID());
            return rep2.compareTo(rep1);
        });
        
        // Select witnesses
        int numWitnesses = Math.min(MAX_WITNESSES, Math.max(MIN_WITNESSES, validators.size()));
        for(int i = 0; i < numWitnesses; i++) {
            Witness validator = validators.get(i);
            
            // Check if validator meets reputation requirement
            SELFNumber rep = SelfDB.getDB().getValidatorAverageScore(validator.getValidatorID());
            if(rep.compareTo(MIN_WITNESS_REPUTATION) >= 0) {
                witnesses.add(validator);
            }
        }
        
        // If we don't have enough witnesses, add some random ones
        while(witnesses.size() < MIN_WITNESSES && validators.size() > 0) {
            int randomIndex = (int)(Math.random() * validators.size());
            Witness validator = validators.get(randomIndex);
            if(!witnesses.contains(validator)) {
                witnesses.add(validator);
            }
        }
        
        return witnesses;
    }
    
    /**
     * Calculate validation difficulty
     */
    private static SELFNumber calculateValidationDifficulty() {
        // Get current difficulty from params
        SELFNumber currentDifficulty = SELFParams.CURRENT_VALIDATION_DIFFICULTY;
        
        // Adjust based on network conditions
        SELFNumber networkLoad = SELFParams.CURRENT_NETWORK_LOAD;
        SELFNumber difficultyAdjustment = currentDifficulty.multiply(networkLoad);
        
        // Apply bounds
        difficultyAdjustment = difficultyAdjustment.max(MIN_DIFFICULTY_CHANGE);
        difficultyAdjustment = difficultyAdjustment.min(MAX_DIFFICULTY_CHANGE);
        
        return currentDifficulty.multiply(difficultyAdjustment);
    }
    
    /**
     * Calculate validation score
     */
    private static SELFNumber calculateValidationScore(Transaction task, Witness validator) {
        // Base score based on task complexity
        SELFNumber baseScore = calculateTaskComplexity(task);
        
        // Add reputation bonus
        SELFNumber validatorRep = SelfDB.getDB().getValidatorAverageScore(validator.getValidatorID());
        SELFNumber repBonus = validatorRep.multiply(SELFNumber.fromDouble(0.1));
        
        // Add timeliness bonus
        SELFNumber currentTime = SELFNumber.fromLong(SELFSystem.getInstance().getSystemTime());
        SELFNumber taskTime = task.getTime();
        SELFNumber timeDiff = currentTime.subtract(taskTime);
        SELFNumber timeBonus = SELFNumber.ONE;
        if(timeDiff.compareTo(SELFNumber.fromLong(60000)) <= 0) { // Within 1 minute
            timeBonus = timeBonus.add(timeBonus.multiply(SELFNumber.fromDouble(0.2)));
        }
        
        // Calculate final score
        SELFNumber score = baseScore.add(repBonus).multiply(timeBonus);
        
        // Apply minimum score requirement
        return score.max(MIN_VALIDATION_SCORE);
    }
    
    /**
     * Calculate task complexity
     */
    private static SELFNumber calculateTaskComplexity(Transaction task) {
        // Base complexity based on transaction size
        SELFNumber baseComplexity = SELFNumber.fromLong(task.getData().length);
        
        // Add complexity for each input/output
        SELFNumber inputComplexity = SELFNumber.fromLong(task.getAllInputs().size());
        SELFNumber outputComplexity = SELFNumber.fromLong(task.getAllOutputs().size());
        
        // Calculate total complexity
        SELFNumber totalComplexity = baseComplexity.add(inputComplexity).add(outputComplexity);
        
        // Normalize to a score between 0 and 1
        SELFNumber maxComplexity = new SELFNumber("1000000");
        return totalComplexity.divide(maxComplexity);
    }
    
    /**
     * Sign the validation message
     */
    private static void signValidation(ValidationMessage validation) {
        // Sign with validator's key
        SELFData signature = Crypto.signMessage(
            validation.getValidatorID(),
            validation.getValidationScore().toString()
        );
        validation.getHeader().setSignature(signature);
        
        // Sign with each witness
        for(Witness witness : validation.getBody().getWitnesses()) {
            SELFData witnessSig = Crypto.signMessage(
                witness.getWitnessID(),
                validation.getValidationScore().toString()
            );
            witness.setSignature(witnessSig);
        }
    }
		
		//Set the correct Magic Numbers..
		UserDB udb = SelfDB.getDB().getUserDB();
		
		Magic txpowmagic = tip.getTxPoW().getMagic().calculateNewCurrent();
		txpowmagic.setDesiredSELFScript(udb.getMagicDesiredSELFScript());
		txpowmagic.setDesiredMaxTxPoWSize(udb.getMagicMaxTxPoWSize());
		txpowmagic.setDesiredMaxTxns(udb.getMagicMaxTxns());
		txpow.setMagic(txpowmagic);
		
		//Set the parents..
		for(int i=0;i<GlobalParams.SELF_CASCADE_LEVELS;i++) {
			txpow.setSuperParent(i, tip.getTxPoW().getSuperParent(i));
		}

		//And now set the correct SBL given the last block
		int sbl = tip.getTxPoW().getSuperLevel();
				
		//All levels below this now point to the last block..
		MiniData tiptxid = tip.getTxPoW().getTxPoWIDData();
		for(int i=sbl;i>=0;i--) {
			txpow.setSuperParent(i, tiptxid);
		}
		
		//Set the block difficulty - minimum is the TxPoW diff..
		MiniData blkdiff = getBlockDifficulty(tip);
		txpow.setBlockDifficulty(blkdiff);
				
		//Set the TXN Difficulty.. currently 1 second work..
		MiniNumber userhashrate = SelfDB.getDB().getUserDB().getHashRate();
		MiniData minhash 		= calculateDifficultyData(userhashrate);
		
		//Check is not MORE than the block difficulty - this only happens at genesis..
		if(minhash.isLess(blkdiff)) {
			minhash = blkdiff;
		}
		
		//Check is acceptable.. if not add 10% as may be changing..
		if(minhash.isMore(txpowmagic.getMinTxPowWork())) {
			
			//Warn them..
			//SelfLogger.log("WARNING : Your Hashrate is lower than the current Minimum allowed by the network");
			
			//Add 10%.. to give yourself some space
			BigDecimal hashes 	= txpowmagic.getMinTxPowWork().getDataValueDecimal();
			hashes 				= hashes.divide(new BigDecimal("1.1"), MathContext.DECIMAL64);
			minhash 			= new MiniData(hashes.toBigInteger());
			
			//This could be too low if the Hash value is going up..
//			minhash = txpowmagic.getMinTxPowWork();
		}
		txpow.setTxDifficulty(minhash);
		
		//And add the current mempool txpow..
		ArrayList<TxPoW> mempool = SelfDB.getDB().getTxPoWDB().getAllUnusedTxns();
		
		//Order the mempool txns by BURN..
		Collections.sort(mempool, new Comparator<TxPoW>() {
			@Override
			public int compare(TxPoW o1, TxPoW o2) {
				return o2.getBurn().compareTo(o1.getBurn());
			}
		});
		
		//MAX number of transactions in mempool.. 1 hrs worth of blocks..
		//int max 					= tip.getTxPoW().getMagic().getMaxNumTxns().getAsInt() * 12 * 6;
		int max 					= 5000;
		
		int counter					= 0;
		ArrayList<TxPoW> newmempool = new ArrayList<>();
		TxPoWDB txpdb 				= SelfDB.getDB().getTxPoWDB();
		MEMPOOL_FULL 				= false;
		for(TxPoW memtxp : mempool) {
			if(counter<max) {
				newmempool.add(memtxp);
			}else {
				//Remove from RAMDB..
				SelfLogger.log("MEMPOOL MAX SIZE REACHED : REMOVED id:"+memtxp.getTxPoWID()+" burn:"+memtxp.getBurn());
				txpdb.removeMemPoolTxPoW(memtxp.getTxPoWID());
				
				//No more new Txpow forwarded with a low burn
				if(!MEMPOOL_FULL) {
					MEMPOOL_FULL = true;
					
					//Store this as the min burn
					MIN_MEMPOOL_BURN = memtxp.getBurn();
				}
			}
			counter++;
		}
		
		//Swap lists..
		mempool = newmempool;
		
		//The final TxPoW transactions put in this TxPoW
		ArrayList<TxPoW> chosentxns = new ArrayList<>();
				
		//A list of the added coins
		ArrayList<String> addedcoins = new ArrayList<>();
		
		//Main
		ArrayList<CoinProof> proofs = txpow.getWitness().getAllCoinProofs();
		for(CoinProof proof : proofs) {
			addedcoins.add(proof.getCoin().getCoinID().to0xString());
		}

		//Burn
		proofs = txpow.getBurnWitness().getAllCoinProofs();
		for(CoinProof proof : proofs) {
			addedcoins.add(proof.getCoin().getCoinID().to0xString());
		}
		
		//Check them all..
		int totaladded = 0;
		for(TxPoW memtxp : mempool) {
			
			//Is it a transaction
			if(!memtxp.isTransaction()) {
				continue;
			}
			
			//Start off assuming it's valid
			boolean valid = true;
			
			try {
				
				//Check how many times we have checked this TxPoW - and rejected it
				if(memtxp.getCheckRejectNumber()>3) {
					//No good..
					//SelfLogger.log("TxPoW checked too many times.. "+memtxp.getTxPoWID());
					valid = false;
				}
				
				//Are we still valid and checkabnle
				if(valid) {
					
					//Input coin checkers - For the CoinProofs as CoinID may be ELTOO
					ArrayList<CoinProof> inputs;
					
					//Did we find this coin..
					boolean found = false;
					
					//Check CoinIDs not added already.. for Transaction
					inputs = memtxp.getWitness().getAllCoinProofs();
					for(CoinProof proof : inputs) {
						if(addedcoins.contains(proof.getCoin().getCoinID().to0xString())) {
							//Coin already added in previous TxPoW
							found = true;
							break;
						}
					}
					
					//Check CoinIDs not added already.. for Burn Transaction
					if(!found) {
						inputs = memtxp.getBurnWitness().getAllCoinProofs();
						for(CoinProof proof : inputs) {
							if(addedcoins.contains(proof.getCoin().getCoinID().to0xString())) {
								//Coin already added in previous TxPoW
								found = true;
								break;
							}
						}
					}
					
					//Did we find it..
					if(found) {
						//Checked and already added
						memtxp.incrementCheckRejectNumber();
						continue;
					}
				
					//Check against the Magic Numbers
					if(memtxp.getSizeinBytesWithoutBlockTxns() > txpowmagic.getMaxTxPoWSize().getAsLong()) {
						SelfLogger.log("Mempool txn too big.. "+memtxp.getTxPoWID()+" size:"+memtxp.getSizeinBytesWithoutBlockTxns()+" max:"+txpowmagic.getMaxTxPoWSize().getAsLong());
						valid = false;
					}else if(memtxp.getTxnDifficulty().isMore(txpowmagic.getMinTxPowWork())) {
						SelfLogger.log("Mempool txn TxPoW too low.. "+memtxp.getTxPoWID());
						valid = false;
					}
				}
			
				//Check if Valid!
				if(valid && TxPoWChecker.checkTxPoWSimple(tip.getMMR(), memtxp, txpow, false)) {
					
					//Add to our list
					chosentxns.add(memtxp);
					
					//Add to this TxPoW
					txpow.addBlockTxPOW(memtxp.getTxPoWIDData());
					
					//One more to the total..
					totaladded++;
					
					//Add all the input coins - from transaction
					ArrayList<CoinProof> memtxpinputcoins = memtxp.getWitness().getAllCoinProofs();
					for(CoinProof cc : memtxpinputcoins) {
						addedcoins.add(cc.getCoin().getCoinID().to0xString());
					}
					
					//Add all the input coins - from burn transaction
					memtxpinputcoins = memtxp.getBurnWitness().getAllCoinProofs();
					for(CoinProof cc : memtxpinputcoins) {
						addedcoins.add(cc.getCoin().getCoinID().to0xString());
					}	
					
				}else {
					//Checked and something wrong..
					memtxp.incrementCheckRejectNumber();
				}
						
			}catch(Exception exc) {
				SelfLogger.log("ERROR Checking TxPoW "+memtxp.getTxPoWID()+" "+exc.toString());
				valid = false;
			}
			
			//Was it valid
			if(!valid) {
				//Invalid TxPoW - remove from mempool
				//SelfLogger.log("Invalid TxPoW in mempool.. removing.. "+memtxp.getTxPoWID());
				SelfDB.getDB().getTxPoWDB().removeMemPoolTxPoW(memtxp.getTxPoWID());
			}
			
			//Max allowed..
			if(totaladded >= txpowmagic.getMaxNumTxns().getAsInt()) {
				break;
			}
		}
		
		//Calculate the TransactionID - needed for CoinID and MMR..
		txpow.calculateTransactionID();
		
		//Construct the MMR
		TxBlock txblock 	= new TxBlock(tip.getMMR(), txpow, chosentxns);
		TxPoWTreeNode node 	= new TxPoWTreeNode(txblock, false);
		
		//Get the MMR root data
		MMRData root = node.getMMR().getRoot();
		txpow.setMMRRoot(root.getData());
		txpow.setMMRTotal(root.getValue());
		
		//Calculate the txpowid / size..
		txpow.calculateTXPOWID();
				
		return txpow;
	}
	
	/**
	 * Get the next Block Difficulty - using bounds..
	 */
	public static MiniData getBlockDifficulty(TxPoWTreeNode zParent) {
		
		//Are we just starting out.. first 8 blocks are minimum difficulty
		if(zParent.getBlockNumber().isLess(MiniNumber.EIGHT)) {
			return Magic.MIN_TXPOW_WORK;
		}
		
		//Start from the parent..
		TxPoWTreeNode startblock 	= zParent;
		MiniNumber origstart 		= startblock.getBlockNumber();
		
		//Where to..
		TxPoWTreeNode endblock 		= zParent.getParent(GlobalParams.SELF_BLOCKS_SPEED_CALC.getAsInt());
		MiniNumber origend 			= endblock.getBlockNumber();
		
		//Now use the Median Times..
		startblock 				= getMedianTimeBlock(startblock);
		endblock 				= getMedianTimeBlock(endblock);
		MiniNumber blockdiff 	= startblock.getBlockNumber().sub(endblock.getBlockNumber()); 
		
		//If the start and end are the same..
		if(startblock.getBlockNumber().isEqual(endblock.getBlockNumber())) {
			//Must be the root of the tree.. Return the LATEST value..
			return zParent.getTxBlock().getTxPoW().getBlockDifficulty();
		}
		
		//In case of serious time error
		MiniNumber timediff = startblock.getTxPoW().getTimeMilli().sub(endblock.getTxPoW().getTimeMilli());
		if(timediff.isLessEqual(MiniNumber.ZERO)) {
			//This should not happen..
			SelfLogger.log("SERIOUS NEGATIVE TIME ERROR @ "+zParent.getBlockNumber()+" Using latest block diff..");
			SelfLogger.log("StartBlock @ "+origstart+"/"+startblock.getBlockNumber()+" "+new Date(startblock.getTxPoW().getTimeMilli().getAsLong()));
			SelfLogger.log("EndBlock   @ "+origend+"/"+endblock.getBlockNumber()+" "+new Date(endblock.getTxPoW().getTimeMilli().getAsLong()));
			SelfLogger.log("Root node : "+SelfDB.getDB().getTxPoWTree().getRoot().getBlockNumber());
			
			//Return the LATEST value..
			return zParent.getTxBlock().getTxPoW().getBlockDifficulty();
		}
		
		//Get current speed
		MiniNumber speed 		= getChainSpeed(startblock, blockdiff);
		
		//What is the speed ratio.. what we use to decide the NEW difficulty
		MiniNumber speedratio 	= GlobalParams.SELF_BLOCK_SPEED.div(speed);
		
		//Re-target Boundary
		if(speedratio.isMore(MAX_SPBOUND_DIFFICULTY)) {
			speedratio = MAX_SPBOUND_DIFFICULTY;
		}else if(speedratio.isLess(MIN_SPBOUND_DIFFICULTY)) {
			speedratio = MIN_SPBOUND_DIFFICULTY;
		}
		
		//Get average difficulty over that period
		BigInteger averagedifficulty 	= getAverageDifficulty(startblock, blockdiff);
		BigDecimal averagedifficultydec	= new BigDecimal(averagedifficulty);
		
		//Recalculate..
		BigDecimal newdifficultydec = averagedifficultydec.multiply(speedratio.getAsBigDecimal());  
		MiniData newdiff 			= new MiniData(newdifficultydec.toBigInteger());
		
		//Check harder than the absolute minimum
		if(newdiff.isMore(Magic.MIN_TXPOW_WORK)) {
			newdiff = Magic.MIN_TXPOW_WORK;
		}
		
		return newdiff;
	}
	
	public static MiniNumber getChainSpeed(TxPoWTreeNode zStartBlock, MiniNumber zBlocksBack) {
		
		//Get the past block
		TxPoWTreeNode pastblock = zStartBlock.getParent(zBlocksBack.getAsInt());
		
		MiniNumber blockpast	= pastblock.getTxPoW().getBlockNumber();
		MiniNumber timepast 	= pastblock.getTxPoW().getTimeMilli();
		
		MiniNumber blocknow		= zStartBlock.getTxPoW().getBlockNumber();
		MiniNumber timenow 		= zStartBlock.getTxPoW().getTimeMilli();
		
		MiniNumber blockdiff 	= blocknow.sub(blockpast);
		MiniNumber timediff 	= timenow.sub(timepast);
		
		MiniNumber speedmilli 	= blockdiff.div(timediff);
		MiniNumber speedsecs 	= speedmilli.mult(MiniNumber.THOUSAND);
		
		return speedsecs;
	}
	
	private static BigInteger getAverageDifficulty(TxPoWTreeNode zTopBlock, MiniNumber zBlocksBack) {
		BigInteger total 	= BigInteger.ZERO;
		int totalblock 		= zBlocksBack.getAsInt();
		
		TxPoWTreeNode current 	= zTopBlock;
		int counter 			= 0;
		while(counter<totalblock) {
			MiniData difficulty = current.getTxPoW().getBlockDifficulty();
			BigInteger diffval 	= difficulty.getDataValue();
			
			//Add to the total..
			total = total.add(diffval);
			
			//get the parent..
			current = current.getParent();
			counter++;
		}
		
		//Now do the div..
		BigInteger avg = total.divide(new BigInteger(Integer.toString(counter)));
		
		return avg;
	}
	
	/**
	 * Get the Median Block based on milli time..
	 */
	public static TxPoWTreeNode getMedianTimeBlock(TxPoWTreeNode zStartBlock) {
		return getMedianTimeBlock(zStartBlock, GlobalParams.MEDIAN_BLOCK_CALC);
	}
	
	public static TxPoWTreeNode getMedianTimeBlock(TxPoWTreeNode zStartBlock, int zBlocksBack) {
		
		//The block we start checking from
		TxPoWTreeNode current = zStartBlock;
		
		//Create a list of blocks..
		ArrayList<TxPoWTreeNode> allblocks = new ArrayList<>();
		
		int counter=0;
		while(counter<zBlocksBack && current!=null) {
			
			//Add to our list
			allblocks.add(current);
			
			//Move back up the tree
			current = current.getParent();
			counter++;
		}
		
		//Now sort them.. by time milli
		Collections.sort(allblocks, new Comparator<TxPoWTreeNode>() {
			@Override
			public int compare(TxPoWTreeNode o1, TxPoWTreeNode o2) {
				return o1.getTxPoW().getTimeMilli().compareTo(o2.getTxPoW().getTimeMilli());
			}
		});
		
		//Now pick the middle one
		int middle 	= allblocks.size()/2;
		
		//Return the middle one!
		return allblocks.get(middle);
	}
	
	public static void precomputeTransactionCoinID(Transaction zTransaction) {
		
		//Get the inputs.. 
		ArrayList<Coin> inputs = zTransaction.getAllInputs();
		
		//Are there any..
		if(inputs.size() == 0) {
			return;
		}
		
		//Get the first coin..
		Coin firstcoin = inputs.get(0);
		
		//Is it an ELTOO input
		boolean eltoo = false; 
		if(firstcoin.getCoinID().isEqual(Coin.COINID_ELTOO)) {
			eltoo = true;
		}
		
		//The base modifier
		MiniData basecoinid = firstcoin.getCoinID();
		
		//Now cycle..
		ArrayList<Coin> outputs = zTransaction.getAllOutputs();
		int num=0;
		for(Coin output : outputs) {
			
			//Calculate the CoinID..
			if(eltoo) {
				
				//Normal
				output.resetCoinID(Coin.COINID_OUTPUT);
				
			}else {
				
				//The CoinID
				MiniData coinid = zTransaction.calculateCoinID(basecoinid, num);
				output.resetCoinID(coinid);
			}
			
			num++;
		}
	}
	
	public static void main(String[] zArgs) {
		
		ArrayList<MiniNumber> nums = new ArrayList<>();
		nums.add(MiniNumber.ZERO);
		nums.add(MiniNumber.ONE);
		nums.add(MiniNumber.TWO);
		
		Collections.sort(nums, new Comparator<MiniNumber>() {
			@Override
			public int compare(MiniNumber o1, MiniNumber o2) {
				return o2.compareTo(o1);
			}
		});
		
		System.out.println(nums.toString());
	}
}
