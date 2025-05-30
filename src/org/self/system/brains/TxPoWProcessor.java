package org.self.system.brains;

import java.io.IOException;
import java.util.ArrayList;

import org.self.database.SelfDB;
import org.self.database.archive.ArchiveManager;
import org.self.database.cascade.Cascade;
import org.self.database.mmr.MegaMMR;
import org.self.database.txpowdb.TxPoWDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.database.txpowtree.TxPowTree;
import org.self.objects.IBD;
import org.self.objects.TxBlock;
import org.self.objects.TxPoW;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.SELFSystem;
import org.self.system.network.self.NIOManager;
import org.self.system.network.self.NIOMessage;
import org.self.system.params.GeneralParams;
import org.self.system.params.GlobalParams;
import org.self.utils.SelfLogger;
import org.self.utils.Stack;
import org.self.utils.json.JSONObject;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageProcessor;

public class TxPoWProcessor extends MessageProcessor {
	
	private static final String TXPOWPROCESSOR_PROCESSTXPOW 		= "TXP_PROCESSTXPOW";
	private static final String TXPOWPROCESSOR_PROCESSTXBLOCK 		= "TXP_PROCESSTXBLOCK";
	
	private static final String TXPOWPROCESSOR_PROCESS_IBD 			= "TXP_PROCESS_IBD";
	private static final String TXPOWPROCESSOR_PROCESS_SYNCIBD 		= "TXP_PROCESS_SYNCIBD";
	private static final String TXPOWPROCESSOR_PROCESS_ARCHIVEIBD 	= "TXP_PROCESS_ARCHIVEIBD";
	
	/**
	 * A checker Call
	 */
	private static final String TXPOWPROCESSOR_CHECKER_CALL 		= "TXPOWPROCESSOR_CHECKER_CALL";
	
	/**
	 * Ask for Txns in blocks less than this old
	 */
	private static final MiniNumber THREE_HOURS = new MiniNumber(1000 * 60 * 60 * 3);
	
	/**
	 * The IBD you receive on startup
	 */
	private long mFirstIBD 				= System.currentTimeMillis();
	private long MAX_FIRST_IBD_TIME 	= 1000 * 60 * 5; 
	
	/**
	 * When processing IBD for MegaMMR check if finished..
	 */
	private boolean mIBDSyncFinished = false;
	
	public TxPoWProcessor() {
		super("TXPOWPROCESSOR");
	}
	
	public void resetFirstIBDTimer() {
		mFirstIBD = System.currentTimeMillis();
	}
	
	/**
	 * Main entry point for a TxPoW into the system
	 */
	public void postProcessTxPoW(TxPoW zTxPoW) {
		
		//Are we shutting down
		if(SELFSystem.getInstance().isShuttongDownOrRestoring()) {
			return;
		}
		
		//Add / Update last access to the DB
		boolean relevant = SelfDB.getDB().getTxPoWDB().addTxPoW(zTxPoW);
		
		//If Relevant - post a message
		if(relevant) {
			JSONObject data = new JSONObject();
			data.put("relevant", true);
			data.put("txpow", zTxPoW.toJSON());
			
			//And Post it..
			SELFSystem.getInstance().PostNotifyEvent("NEWTXPOW", data);
		
		}else if(GeneralParams.NOTIFY_ALL_TXPOW) {
			
			JSONObject data = new JSONObject();
			data.put("relevant", false);
			data.put("txpow", zTxPoW.toJSON());
			
			//And Post it..
			SELFSystem.getInstance().PostNotifyEvent("NEWTXPOW", data);
		}
		
		//Do NOT process if you are a txblock node
		if(GeneralParams.TXBLOCK_NODE) {
			return;
		}
		
		//Post a message on the single threaded stack
		PostMessage(new Message(TXPOWPROCESSOR_PROCESSTXPOW).addObject("txpow", zTxPoW));
	}
	
	/**
	 * Main entry point for a TxBlock node into the system
	 */
	public void postProcessTxBlock(TxBlock zTxBlock) {
		
		//Are we shutting down
		if(SELFSystem.getInstance().isShuttongDownOrRestoring()) {
			return;
		}
		
		//ONLY txblocknodes do this
		if(!GeneralParams.TXBLOCK_NODE) {
			return;
		}
		
		//Add to the RAM DB
		SelfDB.getDB().getTxBlockDB().addTxBlock(zTxBlock);
		
		//Add / Update last access to the DB
		SelfDB.getDB().getTxPoWDB().addTxPoW(zTxBlock.getTxPoW());
		
		//Post a message on the single threaded stack
		PostMessage(new Message(TXPOWPROCESSOR_PROCESSTXBLOCK).addObject("txblock", zTxBlock));
	}
	
	/**
	 * Main Entry point for IBD messages
	 */
	public void postProcessIBD(IBD zIBD, String zClientUID) {
		postProcessIBD(zIBD, zClientUID, false);
	}
	
	public void postProcessIBD(IBD zIBD, String zClientUID, boolean zOverrideRestore) {
		
		//Are we shutting down
		if(SELFSystem.getInstance().isShuttongDownOrRestoring() && !zOverrideRestore) {
			return;
		}
		
		//If we are doing this as a MegaMMR restore.. reset..
		if(zOverrideRestore) {
			mIBDSyncFinished = false;
		}
		
		//Post a message on the single threaded stack
		PostMessage(new Message(TXPOWPROCESSOR_PROCESS_IBD)
				.addObject("ibd", zIBD)
				.addString("uid", zClientUID)
				.addBoolean("notifyfinish", zOverrideRestore));
	}
	
	public boolean isIBDProcessFinished() {
		return mIBDSyncFinished;
	}
	
	/**
	 * Main Entry point for Sync IBD messages
	 */
	public void postProcessSyncIBD(IBD zIBD, String zClientUID) {
		//Post a message on the single threaded stack
		PostMessage(new Message(TXPOWPROCESSOR_PROCESS_SYNCIBD).addObject("ibd", zIBD).addString("uid", zClientUID));
	}
	
	/**
	 * Main Entry point for Archive IBD messages
	 */
	public void postProcessArchiveIBD(IBD zIBD, String zClientUID) {
		//Post a message on the single threaded stack
		PostMessage(new Message(TXPOWPROCESSOR_PROCESS_ARCHIVEIBD).addObject("ibd", zIBD).addString("uid", zClientUID));
	}
	
	/**
	 * Main TxPoW process function
	 *  
	 * @param zTxPoW
	 */
	private void processTxPoW(TxPoW zTxPoW) {
		
		//Has something on tree changed
		boolean recalculate = false;
		
		//Fast access DB
		TxPoWDB txpdb 		= SelfDB.getDB().getTxPoWDB();
		TxPowTree txptree 	= SelfDB.getDB().getTxPoWTree();
		Cascade	cascdb		= SelfDB.getDB().getCascade();
		
		//Process a stack of TxPoW if necessary
		Stack processstack = new Stack();
		processstack.push(zTxPoW);
		
		//Now work through all the required blocks
		while(!processstack.isEmpty()) {
		
			//Get the next txpow
			TxPoW txpow = (TxPoW) processstack.pop();
			
			//Check we are at least enough blocks on from the root of the tree.. for speed and difficulty calcs
			MiniNumber blknum 	= txpow.getBlockNumber();
			MiniNumber tipnum 	= txptree.getTip().getBlockNumber();
			MiniNumber rootnum 	= txptree.getRoot().getBlockNumber();
			
			boolean validrange = false;
			if(GeneralParams.TEST_PARAMS) {
				validrange 	= blknum.isMore(rootnum);
			}else{
				
				//Make sure far enough from root to be able to check block difficulty
				if(tipnum.isLess(MiniNumber.THOUSAND)) {
					validrange = true;
				}else {
				
					//Min block we will check..
					MiniNumber minblock = rootnum.add(GlobalParams.SELF_BLOCKS_SPEED_CALC); 
					
					//Make sure at least Speed Calc away from root..
					if(blknum.isMore(minblock)){
						validrange = true;
					}
				}
			}
			
			if(txpow.isBlock() && !validrange) {
				if(GeneralParams.BLOCK_LOGS) {
					SelfLogger.log("Invalid range for block check @ "
										+blknum+" root:"+rootnum+" tip:"+tipnum
										+" txpowid:"+txpow.getTxPoWID());
				}
			}
			
			//Is it a block.. that is the only time we crunch
			if(txpow.isBlock() && validrange) {
				
				//Check not already added
				TxPoWTreeNode oldnode = txptree.findNode(txpow.getTxPoWID());
				if(oldnode == null) {
					
					//Is there a valid parent block node..
					TxPoWTreeNode parentnode = txptree.findNode(txpow.getParentID().to0xString());
					if(parentnode != null) {
					
						//Do we have all the transactions in the block
						ArrayList<String> txns 		= txpow.getTransactions();
						int numtxns 				= txns.size();
						ArrayList<TxPoW> alltrans 	= txpdb.getAllTxPoW(txns);
						
						//Do we have them all
						if(alltrans.size() == numtxns) {
						
							//Is this a vliad block..
//							boolean validblock = TxPoWChecker.checkTxPoWBlock(parentnode, txpow, alltrans);
							boolean validblock = TxPoWChecker.checkTxPoWBlockTimed(parentnode, txpow, alltrans);
							
							//OK - Lets check this block
							if(validblock) {
								
								//Create a TxBlock..
								TxBlock txblock = new TxBlock(parentnode.getMMR(), txpow, alltrans);
								
								//Add to the RAM DB
								SelfDB.getDB().getTxBlockDB().addTxBlock(txblock);
								
								//Shall we log it..
								if(GeneralParams.BLOCK_LOGS) {
									SelfLogger.log("Added block to tree : "+txblock.getTxPoW().getBlockNumber()+" "+txblock.getTxPoW().getTxPoWID());
								}
								
								//Send a message to everyone..
								try {
									NIOManager.sendNetworkMessageAll(NIOMessage.MSG_TXBLOCKID, txblock.getTxPoW().getTxPoWIDData());
								} catch (Exception e) {
									SelfLogger.log(e);
								}
								
								//Create a new node
								TxPoWTreeNode newblock = new TxPoWTreeNode(txblock);
								
								//Lets add it to the tree
								parentnode.addChildNode(newblock);
								
								//Add fast link in tree - otherwise only reset / added when recalculate tree is called
								txptree.addFastLink(newblock);
								
								//we need to recalculate the Tree
								recalculate = true;
								
								//Do we have children for this block
								ArrayList<TxPoW> children = txpdb.getChildBlocks(txpow.getTxPoWID());
								for(TxPoW child : children) {
									processstack.push(child);
								}
							}else {
								SelfLogger.log("[!] Failed block check @ "
													+txpow.getBlockNumber()+" txpowid:"
													+txpow.getTxPoWID()
													+" root:"+rootnum+" tip:"+tipnum);
							}
						}
						
					}else {
						//Do we have the Parent TxPoW
						TxPoW parent = txpdb.getTxPoW(txpow.getParentID().to0xString());
						if(parent != null) {
							//If Parent not added.. must be missing transactions.. try now ( This block builds on it soo.. )
							processstack.push(parent);
						}
					}
				}
			}
		}
		
		//Did something change..
		if(recalculate) {
			
			//Recalculate the whole tree
			recalculateTree();
		}
	}
	
	/**
	 * Main TxPoW process function
	 *  
	 * @param zTxPoW
	 */
	private void processTxBlock(TxBlock zTxBlock) {
		
		//Are we running this type of node..
		if(!GeneralParams.TXBLOCK_NODE) {
			return;
		}
		
		//Has something on tree changed
		boolean recalculate = false;
		
		//Fast access DB
		TxPowTree txptree 	= SelfDB.getDB().getTxPoWTree();
		Cascade	cascdb		= SelfDB.getDB().getCascade();
		
		//Process a stack of TxPoW if necessary
		Stack processstack = new Stack();
		processstack.push(zTxBlock);
		
		//Now work through all the required blocks
		while(!processstack.isEmpty()) {
		
			//Get the next txpow
			TxBlock trustedtxblock = (TxBlock) processstack.pop();
			
			//Get the TxPoW
			TxPoW txpow = trustedtxblock.getTxPoW();
			
			//Check we are at least enough blocks on from the root of the tree.. for speed and difficulty calcs
			MiniNumber blknum 	= txpow.getBlockNumber();
			MiniNumber tipnum 	= txptree.getTip().getBlockNumber();
			MiniNumber rootnum 	= txptree.getRoot().getBlockNumber();
			
			boolean validrange = false;
			if(GeneralParams.TEST_PARAMS) {
				validrange 	= blknum.isMore(rootnum);
			}else{
				
				//Make sure far enough from root to be able to check block difficulty
				if(tipnum.isLess(MiniNumber.THOUSAND)) {
					validrange = true;
				}else {
				
					//Min block we will check..
					MiniNumber minblock = rootnum.add(GlobalParams.SELF_BLOCKS_SPEED_CALC); 
					
					//Make sure at least Speed Calc away from root..
					if(blknum.isMore(minblock)){
						validrange = true;
					}
				}
			}
			
			if(txpow.isBlock() && !validrange) {
				if(GeneralParams.BLOCK_LOGS) {
					SelfLogger.log("[!] Invalid range for txblock check slavemode @ "
										+blknum+" root:"+rootnum+" tip:"+tipnum
										+" txpowid:"+txpow.getTxPoWID());
				}
			}
			
			//Is it a block.. that is the only time we crunch
			if(txpow.isBlock() && validrange) {
				
				//Check not already added
				TxPoWTreeNode oldnode = txptree.findNode(txpow.getTxPoWID());
				if(oldnode == null) {
					
					//Is there a valid parent block node..
					TxPoWTreeNode parentnode = txptree.findNode(txpow.getParentID().to0xString());
					if(parentnode != null) {
					
						//Check this TxBlock is Valid..
						boolean validblock = TxPoWChecker.checkTxBlockOnly(parentnode, trustedtxblock);
						
						//OK - Lets check this block
						if(validblock) {
							
							//Shall we log it..
							if(GeneralParams.BLOCK_LOGS) {
								SelfLogger.log("Added TxBlock to tree : "+trustedtxblock.getTxPoW().getBlockNumber()+" "+trustedtxblock.getTxPoW().getTxPoWID());
							}
							
							//Create a new node - using the given TxBlock
							TxPoWTreeNode newblock = new TxPoWTreeNode(trustedtxblock);
							
							//Lets add it to the tree
							parentnode.addChildNode(newblock);
							
							//Add fast link in tree - otherwise only reset / added when recalculate tree is called
							txptree.addFastLink(newblock);
							
							//we need to recalculate the Tree
							recalculate = true;
							
							//Do we have children for this block
							ArrayList<TxBlock> children = SelfDB.getDB().getTxBlockDB().getChildBlocks(txpow.getTxPoWID());
							for(TxBlock child : children) {
								processstack.push(child);
							}
						}else {
							SelfLogger.log("[!] Failed txblock check @ "
												+txpow.getBlockNumber()+" txpowid:"
												+txpow.getTxPoWID()
												+" root:"+rootnum+" tip:"+tipnum);
						}
						
					}else {
						
						//Do we have the Parent TxPoW
						TxBlock parent = SelfDB.getDB().getTxBlockDB().findTxBlock(txpow.getParentID().to0xString());
						if(parent != null) {
							//If Parent not added.. must be missing transactions.. try now ( This block builds on it soo.. )
							processstack.push(parent);
						}
					}
				}
			}
		}
		
		//Did something change..
		if(recalculate) {
			
			//Recalculate the whole tree
			recalculateTree();
		}
	}
	
	private boolean processSyncBlock(TxBlock zTxBlock) throws Exception {
		
		//Get all the required DBs
		Cascade cascdb		= SelfDB.getDB().getDB().getCascade();
		TxPoWDB txpdb 		= SelfDB.getDB().getTxPoWDB();
		TxPowTree txptree 	= SelfDB.getDB().getTxPoWTree();
		
		//Add to the RAM DB
		SelfDB.getDB().getTxBlockDB().addTxBlock(zTxBlock);
				
		//Add the TxPoW to the database - in case we don't have it
		txpdb.addTxPoW(zTxBlock.getTxPoW());
		
		//Do we have ANY TxPoW in the tree at all..
		if(txptree.getTip() == null) {
			
			//Check the cascade ends where this block begins..
			if(cascdb.getTip() != null) {
				
				//The block numbers
				MiniNumber txblknum = zTxBlock.getTxPoW().getBlockNumber();
				MiniNumber cascblk 	= cascdb.getTip().getTxPoW().getBlockNumber();
				
				//Check is 1 less than this block..
				if(!cascblk.isEqual(txblknum.sub(MiniNumber.ONE))) {
					//Error cascade should start where this ends..
					throw new Exception("Invalid SyncBlock Cascade Tip not parent "+txblknum+" casctip:"+cascblk);
				}			
			}
			
			//Create a new node
			TxPoWTreeNode newblock = new TxPoWTreeNode(zTxBlock);
			
			//Set it as root - this recalculates the tree automagically
			txptree.setRoot(newblock);
			
			return true;
		}
		
		//Check not already added
		TxPoWTreeNode oldnode = txptree.findNode(zTxBlock.getTxPoW().getTxPoWID());
		if(oldnode == null) {
		
			//Is there a valid parent block node..
			TxPoWTreeNode parentnode = txptree.findNode(zTxBlock.getTxPoW().getParentID().to0xString());
			if(parentnode != null) {
			
				//Create a new node
				TxPoWTreeNode newblock = new TxPoWTreeNode(zTxBlock);
				
				//Lets add it to the tree
				parentnode.addChildNode(newblock);
				
				//Add fast link in tree - otherwise only reset / added when recalculate tree is called
				txptree.addFastLink(newblock);
				
				return true;
			}else {
				throw new Exception("Invalid SyncBlock as NO PARENT! syncblock:"+zTxBlock.getTxPoW().getBlockNumber()); 
			}
		}
		
		return false;
	}
	
	public void onStartUpRecalc() {
		recalculateTree();
	}
	
	private void recalculateTree() {
		
		//Are we shutting down..
//		if(SELFSystem.getInstance().isShuttingDown()) {
//			return;
//		}
		
		//Required DBs
		TxPoWDB txpdb		= SelfDB.getDB().getTxPoWDB();
		TxPowTree txptree 	= SelfDB.getDB().getTxPoWTree();
		Cascade	cascdb		= SelfDB.getDB().getCascade();
		ArchiveManager arch = SelfDB.getDB().getArchive();
		
		//Get the Mega MMR
		MegaMMR megammr = SelfDB.getDB().getMegaMMR();
		
		//Need to LOCK DB
		SelfDB.getDB().writeLock(true);
		
		try {
		
			//What is the current tip..
			TxPoWTreeNode currenttip = txptree.getTip();
			
			//Recalculate the tree and tip
			txptree.recalculateTree();
			
			//How big..
			int maxlen = GlobalParams.SELF_CASCADE_START_DEPTH.add(GlobalParams.SELF_CASCADE_FREQUENCY).getAsInt();
			if(txptree.getHeaviestBranchLength() >= maxlen) {
				
				//Current Tip
				TxPoWTreeNode tip = txptree.getTip();
				
				//get the new root..
				TxPoWTreeNode newroot = tip.getPastNode(tip.getTxPoW().getBlockNumber().sub(GlobalParams.SELF_CASCADE_START_DEPTH).increment()); 
				
				//Now copy all the MMR Coins.. 
				newroot.copyParentRelevantCoins();
				
				//NOW - Shrink it..
				ArrayList<TxPoWTreeNode> cascade = txptree.setLength(GlobalParams.SELF_CASCADE_START_DEPTH.getAsInt());
				
				//Add these node to the cascade;
				for(TxPoWTreeNode txpnode : cascade) {
					
					//Get the TxPoW
					TxPoW txpow = txpnode.getTxPoW();
					
					//These TxPoW are now in the cascade - that can NEVER change..
					txpdb.setInCascade(txpow.getTxPoWID());
					for(String txpid : txpow.getTransactions()) {
						txpdb.setInCascade(txpid);
					}
					
					//Store in the ArchiveManager
					arch.saveBlock(txpnode.getTxBlock());
					
					//And add to the cascade
					cascdb.addToTip(txpnode.getTxPoW());
					
					//Send out Notify Messages for coins added
					try {
						TxPoWTreeNode.CheckTxBlockForNotifyCoins(txpnode.getTxBlock());
					}catch(Exception exc) {
						SelfLogger.log(exc);
					}
					
					//Are we ruinning in MEGA MMR
					if(GeneralParams.IS_MEGAMMR) {
						//SelfLogger.log("MEGAMMR : Add block "+txpnode.getTxBlock().getTxPoW().getBlockNumber());
						//Add this to the MEGA MMR
						megammr.addBlock(txpnode.getTxBlock());
					}
				}
				
				//Are we running in MEGA MMR
				if(GeneralParams.IS_MEGAMMR) {
					
					//SelfLogger.log("MEGAMMR : Prune tree start");
					
					//Prune the tree
					megammr.getMMR().pruneTree();
					
					//SelfLogger.log("MEGAMMR : Prune tree end");
				}
				
				//And finally..
				cascdb.cascadeChain();
				
				//Clear the TxBlockDB
				SelfDB.getDB().getTxBlockDB().clearOld(newroot.getBlockNumber().sub(MiniNumber.HUNDRED));
			}
		
			//And now set all the onchain txns so not used again in a new TxPoW
			txpdb.clearSelfChainTxns();
			
			//Current Tip
			TxPoWTreeNode tip = txptree.getTip();
			while(tip != null) {
				
				//Get the TxPoW
				TxPoW txpow = tip.getTxPoW();
				
				//Set as onchain..
				txpdb.setOnMainChain(txpow.getTxPoWID());
				
				//Set all the txns in the block
				ArrayList<String> txns = txpow.getTransactions();
				for(String txn : txns) {
					txpdb.setOnMainChain(txn);
				}
				
				//And move back up the chain
				tip = tip.getParent();
			}
		
			//Has the Tip changed..
			TxPoWTreeNode newtipnode 	= txptree.getTip();
			
			//Has the tip changed..
			if(currenttip!=null && newtipnode!=null) {
				if(!currenttip.getTxPoW().getTxPoWIDData().isEqual(newtipnode.getTxPoW().getTxPoWIDData())) {
					SELFSystem.getInstance().PostMessage(new Message(SELFSystem.SELF_NEWBLOCK).addObject("txpow", newtipnode.getTxPoW()));
				}
			}
			
		}catch(Exception exc) {
			SelfLogger.log(exc);
		
		}finally {
			//Unlock..
			SelfDB.getDB().writeLock(false);	
		}
		
	}
	
	public void postCheckCall() {
		PostMessage(TXPOWPROCESSOR_CHECKER_CALL);
	}
	
	@Override
	protected void processMessage(Message zMessage) throws Exception {
		
		//Are we shutting down..
		if(SELFSystem.getInstance().isShuttingDown()) {
			return;
		}
		
		if(zMessage.isMessageType(TXPOWPROCESSOR_PROCESSTXPOW)) {
			
			//Do NOT process if you are a txblock node
			if(GeneralParams.TXBLOCK_NODE) {
				return;
			}
			
			//Get the TxPoW
			TxPoW txp = (TxPoW) zMessage.getObject("txpow");
			
			//Process it..
			processTxPoW(txp);
		
		}else if(zMessage.isMessageType(TXPOWPROCESSOR_PROCESSTXBLOCK)) {
			
			//ONLY process if you are a txblock node
			if(!GeneralParams.TXBLOCK_NODE) {
				return;
			}
			
			//Get the TxBlock
			TxBlock txblock = (TxBlock)zMessage.getObject("txblock");
			
			//process it
			processTxBlock(txblock);
			
		}else if(zMessage.isMessageType(TXPOWPROCESSOR_PROCESS_IBD)) {
			
			//Get the client - in case is invalid..
			String uid = zMessage.getString("uid");
			
			//Get the IBD
			IBD ibd = (IBD) zMessage.getObject("ibd");
			
			//Are we notifying on Finish
			boolean notifyfinish = zMessage.getBoolean("notifyfinish");
			
			//Does it seem Valid..
			if(!ibd.checkValidData()) {
				return;
			}
			
			//Put in a BIG try catch to make sure you disable setSyncIBD
			try {
				
				//we are syncing..
				SELFSystem.getInstance().setSyncIBD(true);
				
				//How big is it..
				if(GeneralParams.IBDSYNC_LOGS) {
					SelfLogger.log("Processing main IBD length : "+ibd.getTxBlocks().size());
				}
				long timestart = System.currentTimeMillis();
				
				//Does it have a cascade
				if(ibd.hasCascade()) {
					
					//Do we.. ?
					if(SelfDB.getDB().getCascade().getTip() == null) {
						
						//Check is valid for out chain..
						boolean ignore = false;
						TxPoWTreeNode root = SelfDB.getDB().getTxPoWTree().getRoot();
						if(root != null) {
							
							MiniNumber rootblock = root.getBlockNumber();
							MiniNumber casctip 	 = ibd.getCascade().getTip().getTxPoW().getBlockNumber();
							
							if(!casctip.isEqual(rootblock.decrement())) {
								
								ignore = true;
								SelfLogger.log("[!] IGNORE IBD - My Tree Root : "+rootblock+" IBD TIP : "+casctip);
							
							}else{
								
								MiniData rootparent = root.getTxPoW().getParentID();
								MiniData casctipid	= ibd.getCascade().getTip().getTxPoW().getTxPoWIDData();
								
								if(!rootparent.isEqual(casctipid)) {
									ignore = true;
									SelfLogger.log("[!] IGNORE IBD - Invalid Hash for parents");
								}
							}
						}
						
						if(!ignore){
							
							//Process.. Need to LOCK DB
							SelfDB.getDB().writeLock(true);
							
							//Set this cascade
							try {
								
								//Set this for us
								SelfDB.getDB().setIBDCascade(ibd.getCascade());
								
								//Do we need to store the cascade in the ArchiveDB
								SelfDB.getDB().getArchive().checkCascadeRequired(ibd.getCascade());
								
							}catch(Exception exc) {
								SelfLogger.log(exc);
							
							}finally {
								//Unlock..
								SelfDB.getDB().writeLock(false);
							}
							
						}
						
					}else {
						//Received a cascade when we already have one.. ignore..
						//SelfLogger.log("WARNING Received cascade when already have one from "+uid);
					}
				}
				
				//Now process the SyncBlocks
				TxPowTree txptree 		= SelfDB.getDB().getTxPoWTree();
				MiniNumber timenow 		= new MiniNumber(System.currentTimeMillis());
				
				//First run accept the IBD - still follow heaviest chain
				long diff = System.currentTimeMillis() - mFirstIBD;
				if((diff > MAX_FIRST_IBD_TIME) && !GeneralParams.TXBLOCK_NODE) {
					
					//If our chain is up to date (within 3 hrs) we don't accept TxBlock at all.. only full blocks
					if(txptree.getTip() != null && ibd.getTxBlocks().size()>0) {
						MiniNumber notxblocktimediff = new MiniNumber(1000 * 60 * 180);
						if(GeneralParams.TEST_PARAMS) {
							notxblocktimediff = new MiniNumber(1000 * 60 * 5);
						}
						if(txptree.getTip().getTxPoW().getTimeMilli().sub(timenow).abs().isLess(notxblocktimediff)) {
							SelfLogger.log("Your chain tip is up to date - no TxBlocks accepted - only FULL TxPoW");
							
							//we are not syncing..
							SELFSystem.getInstance().setSyncIBD(false);
							
							//Ask to sync the TxBlocks
							askToSyncTxBlocks(uid);
							
							return;
						}
					}
				}
				
				//How many blocks have we added
				int additions = 0;
				
				//Cycle and add..
				ArrayList<TxBlock> blocks = ibd.getTxBlocks();
				for(TxBlock block : blocks) {
					
					try {
							
						//Process it..
						processSyncBlock(block);	
						additions++;
					
						//Request any missing..
						requestMissingTxns(uid,block);
						
						//If we've added a lot of blocks..
						if(additions > 256) {
							
							//recalculate the Tree..
							recalculateTree();
							
							//Clean memory
							System.gc();
							
							//Reset these
							additions = 0;
							
							if(GeneralParams.IBDSYNC_LOGS) {
								SelfLogger.log("[!] Processed IBD block @ "+block.getTxPoW().getBlockNumber().toString());
							}
						}
						
					}catch(Exception exc) {
						SelfLogger.log(exc.toString());
						
						//Something funny going on.. disconnect and remove from list
						SELFSystem.getInstance().getNIOManager().disconnect(uid,true);
						
						break;
					}
				}
				
				//And now recalculate tree
				recalculateTree();
				
				//How big is it..
				long timediff = System.currentTimeMillis() - timestart;
				if(GeneralParams.IBDSYNC_LOGS) {
					SelfLogger.log("Processing main IBD finished "+timediff+"ms");
				}
				
				//we are not syncing..
				SELFSystem.getInstance().setSyncIBD(false);
				
				//Wipe old Archive Blocks..
				SelfDB.getDB().getArchive().checkForCleanDB();
				
				//Ask to sync the TxBlocks
				askToSyncTxBlocks(uid);
				
				//Notify
				if(notifyfinish) {
					mIBDSyncFinished = true;
				}
				
			}catch(Exception exc) {
				SelfLogger.log("[!] TXPOWPROCESSOR_PROCESS_IBD ERROR "+exc);
				SelfLogger.log(exc);
			
			}finally {
				//we are NOT syncing anymore..
				SELFSystem.getInstance().setSyncIBD(false);
			}
			
		}else if(zMessage.isMessageType(TXPOWPROCESSOR_PROCESS_SYNCIBD)) {
			
			//Get the client - in case is invalid..
			String uid = zMessage.getString("uid");
			
			//Get the IBD
			IBD ibd = (IBD) zMessage.getObject("ibd");
			
			//Get the sync blocks
			ArrayList<TxBlock> blocks = ibd.getTxBlocks();
			if(blocks.size() == 0) {
				//No sync blocks
				return;
			}
			
			//Get the ArchiveDB
			ArchiveManager arch = SelfDB.getDB().getArchive();
			
			//Get the last block I have..
			TxBlock lastblock 	= arch.loadLastBlock();
			TxPoW lastpow 		= null;
			if(lastblock == null) {
				//Use the TxPoWTree
				lastpow = SelfDB.getDB().getTxPoWTree().getRoot().getTxPoW();
			}else {
				lastpow = lastblock.getTxPoW();
			}
			
			//Cycle through and add..
			for(TxBlock block : blocks) {
				
				//What is the last current block number we know of..
				MiniNumber lastnum = lastpow.getBlockNumber();
				
				//Check the block number is correct.. could be an asynchronous miss-alignment
				if(block.getTxPoW().getBlockNumber().isEqual(lastnum.decrement())) {
				
					//Check the parent hash is correct
					if(block.getTxPoW().getTxPoWIDData().isEqual(lastpow.getParentID())) {
						//We can store it..
						arch.saveBlock(block);
					}else {
						SelfLogger.log("[-] Invalid block parent in TxBlock sync.. @ "+block.getTxPoW().getBlockNumber()+" from "+uid);
						return;
					}
				}
				
				//we have a new last pow..
				lastpow = block.getTxPoW();
			}
		
			//Wipe old Archive Blocks..
			SelfDB.getDB().getArchive().checkForCleanDB();
			
			//Ask to sync the TxBlocks
			askToSyncTxBlocks(uid);
		
		}else if(zMessage.isMessageType(TXPOWPROCESSOR_PROCESS_ARCHIVEIBD)) {
			
			//Get the IBD archive data
			IBD arch 			= (IBD) zMessage.getObject("ibd");
			String uid 			= zMessage.getString("uid");
			
			//How many blocks have we added
			int additions = 0;
			
			//Cycle and add..
			ArrayList<TxBlock> blocks = arch.getTxBlocks();
			if(blocks.size() > 0) {
				if(GeneralParams.IBDSYNC_LOGS) {
					SelfLogger.log("Processing Archive IBD length:"+blocks.size()+" start:"+blocks.get(0).getTxPoW().getBlockNumber());	
				}
			}
			
			for(TxBlock block : blocks) {
				
				try {
					
					//Process it..
					processSyncBlock(block);	
					additions++;
				
					//If we've added a lot of blocks..
					if(additions > 1000) {
						
						//recalculate the Tree..
						recalculateTree();
						
						//Reset these
						additions = 0;
					}
					
				}catch(Exception exc) {
					SelfLogger.log(exc.toString());
					
					//Something funny going on.. disconnect
					SELFSystem.getInstance().getNIOManager().disconnect(uid);
					
					break;
				}
			}
			
			//And now recalculate tree
			recalculateTree();
		
		
		}else if(zMessage.isMessageType(TXPOWPROCESSOR_CHECKER_CALL)) {
				
				//Sent to check this is running..
				SelfLogger.log("TXPOWPROCESSOR Checker Call Recieved..");
		}
		
	}
	
	/**
	 * Send a SYNC TxBlock message
	 */
	public void askToSyncTxBlocks(String zClientID) {
		
		//Check not an internal
		if(zClientID.equals("0x00")) {
			return;
		}
		
		//Only ask if not in no sync ibd mode
		if(!GeneralParams.NO_SYNC_IBD) {
			Message synctxblock = new Message(NIOManager.NIO_SYNCTXBLOCK);
			synctxblock.addString("client", zClientID);
			SELFSystem.getInstance().getNetworkManager().getNIOManager().PostMessage(synctxblock);
		}
	}
	
	private void requestMissingTxns(String zClientID, TxBlock zBlock) {
		
		//Are we in TXBLOCK mode.. no txns pls
		if(GeneralParams.TXBLOCK_NODE) {
			return;
		}
		
		//Get the TxPoW
		TxPoW txp = zBlock.getTxPoW();
		
		//Is this a recent block ? 
		MiniNumber timenow = new MiniNumber(System.currentTimeMillis());
		MiniNumber mintime = timenow.sub(THREE_HOURS);
		if(txp.getTimeMilli().isLess(mintime)) {
			return;
		}
		
		//Get all the missing txns in the block
		try {
			ArrayList<MiniData> txns = txp.getBlockTransactions();
			for(MiniData txn : txns) {
				boolean exists = SelfDB.getDB().getTxPoWDB().exists(txn.to0xString());
				if(!exists) {
					NIOManager.sendNetworkMessage(zClientID, NIOMessage.MSG_TXPOWREQ, txn);
				}
			}
		} catch (IOException e) {
			SelfLogger.log(e);
		}
	}
}
