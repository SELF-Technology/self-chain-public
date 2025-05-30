package org.self.system.params;

import org.self.objects.base.MiniNumber;

public class TestParams {

	/**
	 * Which Version
	 */
	public static String SELF_VERSION = GlobalParams.SELF_VERSION+"-TEST"; 
	
	/**
	 * Speed in blocks per second.. 
	 * -  0.02  = 50 second block time
	 * -  0.05  = 20 second block time
	 * -  0.2   =  5 second block time
	 */
	public static MiniNumber SELF_BLOCK_SPEED = new MiniNumber("0.05");
	
	/**
	 * When checking speed and average difficulty only look at this many blocks back
	 */
	public static MiniNumber SELF_BLOCKS_SPEED_CALC = new MiniNumber(16);
	
	/**
	 * How deep before we think confirmed..
	 */
	public static MiniNumber SELF_CONFIRM_DEPTH  = new MiniNumber("3");
	
	/**
	 * How often do we cascade the chain
	 */
	public static MiniNumber SELF_CASCADE_FREQUENCY = new MiniNumber(3);
	
	/**
	 * Depth before we cascade..
	 */
	public static MiniNumber SELF_CASCADE_START_DEPTH = new MiniNumber(32);
	
	
	/**
	 * Number of blocks at each cascade level 
	 */
	public static int SELF_CASCADE_LEVEL_NODES  = 4;
	
	/**
	 * How Many Cascade Levels
	 */
	public static int SELF_CASCADE_LEVELS  = 32;
	
	/**
	 * Max Proof History - how far back to use a proof of coin..
	 * If there is a re-org of more than this the proof will be invalid 
	 */
	public static MiniNumber SELF_MMR_PROOF_HISTORY = new MiniNumber(8);

	/**
	 * The MEDIAN time block is taken from this many blocks back
	 * When calculating the Difficulty of a block ( both from the tip and the previous block )
	 * This smooths out the time fluctuations for different blocks and removes incorrect times.
	 * 
	 * 64 blocks means the block 1/2 hour ago.
	 */
	public static int MEDIAN_BLOCK_CALC = 8;
	
	/**
	 * Set these as the GlobalParams..
	 */
	public static void setTestParams() {
		GlobalParams.SELF_BLOCK_SPEED 			= TestParams.SELF_BLOCK_SPEED;
		GlobalParams.SELF_BLOCKS_SPEED_CALC 		= TestParams.SELF_BLOCKS_SPEED_CALC;
		GlobalParams.SELF_CASCADE_FREQUENCY 		= TestParams.SELF_CASCADE_FREQUENCY;
		GlobalParams.SELF_CASCADE_LEVEL_NODES		= TestParams.SELF_CASCADE_LEVEL_NODES;
		GlobalParams.SELF_CASCADE_LEVELS			= TestParams.SELF_CASCADE_LEVELS;
		GlobalParams.SELF_CASCADE_START_DEPTH		= TestParams.SELF_CASCADE_START_DEPTH;
		GlobalParams.SELF_CONFIRM_DEPTH			= TestParams.SELF_CONFIRM_DEPTH;
		GlobalParams.SELF_MMR_PROOF_HISTORY		= TestParams.SELF_MMR_PROOF_HISTORY;
		GlobalParams.SELF_VERSION					= TestParams.SELF_VERSION;
		GlobalParams.MEDIAN_BLOCK_CALC				= TestParams.MEDIAN_BLOCK_CALC;
	}	
}
