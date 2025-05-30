package org.self.system.network.self;

import java.util.ArrayList;

import org.self.objects.Coin;
import org.self.objects.TxPoW;
import org.self.system.params.GeneralParams;
import org.self.utils.SelfLogger;

public class RelayPolicy {

	/**
	 * Do we exceed the maximium number of coin outputs
	 */
	public static boolean checkMaxCoinOutputNumber(TxPoW zTxPow) {
		
		//Check Total Outputs..
		int outsize = zTxPow.getTransaction().getAllOutputs().size();
		if(outsize>GeneralParams.MAX_RELAY_OUTPUTCOINS) {
			SelfLogger.log("MAX Coin outpouts in Transaction @ "+zTxPow.getTxPoWID());
			return false;
		}
		
		outsize = zTxPow.getBurnTransaction().getAllOutputs().size();
		if(outsize>GeneralParams.MAX_RELAY_OUTPUTCOINS) {
			SelfLogger.log("MAX Coin outpouts in Burn Transaction @ "+zTxPow.getTxPoWID());
			return false;
		}
		
		return true;
	}

	public static boolean checkMaxStateStoreSize(TxPoW zTxPow, long zMaxSize) {
		
		//How many outputs store the state
		int countertxn=0;
		
		//How many output coins store the state
		ArrayList<Coin> outputs = zTxPow.getTransaction().getAllOutputs();
		for(Coin out : outputs) {
			if(out.storeState()) {
				countertxn++;
			}
		}
		
		//Get the size of the state
		long statesizetxn = zTxPow.getTransaction().calculateStateSize();
		
		//Multiply by number of coins that store the state..
		long totaltxn = statesizetxn * countertxn;
		
		//And for the burn
		int counterburn=0;
		outputs = zTxPow.getBurnTransaction().getAllOutputs();
		for(Coin out : outputs) {
			if(out.storeState()) {
				counterburn++;
			}
		}
		
		//Get the size of the state
		long statesizeburn = zTxPow.getBurnTransaction().calculateStateSize();
		
		//Multiply by number of coins that store the state..
		long totalburn = statesizeburn * counterburn;
		
		//Total state size store
		long total = totaltxn + totalburn;
		
		//Now check..
		if(total > zMaxSize) {
			SelfLogger.log("TXPoW exceeds maximum state store policy"
					+ " coinstxn:"+countertxn+" coinsburn:"+counterburn
					+ " statesizetxn:"+statesizetxn+" statesizeburn:"+statesizeburn
					+" for "+total+" / "+zMaxSize);
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check All the relay policies..
	 */
	public static boolean checkAllPolicies(TxPoW zTxPow, long zMaxSize) {
		
		if(!checkMaxCoinOutputNumber(zTxPow)) {
			return false;
		}
		
		if(!checkMaxStateStoreSize(zTxPow,zMaxSize)) {
			SelfLogger.log("Relay Policy FAIL - not forwarding txn..");
			return false;
		}
		
		return true;
	}

}
