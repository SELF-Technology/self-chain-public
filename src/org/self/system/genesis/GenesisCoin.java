package org.self.system.genesis;

import org.self.objects.Address;
import org.self.objects.Coin;
import org.self.objects.Token;
import org.self.objects.self.SELFData;
import org.self.objects.self.SELFNumber;

public class GenesisCoin extends Coin {

	public static final SELFData GENESIS_COINID = new SELFData("0x5350415254414355534C4F5645534D494E494D41");
	
	public GenesisCoin() {
		super(GENESIS_COINID, Address.TRUE_ADDRESS.getAddressData(), SELFNumber.BILLION, Token.TOKENID_SELF);
	}
	
}
