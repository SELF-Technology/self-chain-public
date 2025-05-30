package org.self.system.commands.base;

import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.database.wallet.SeedRow;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniString;
import org.self.system.commands.Command;
import org.self.system.commands.CommandException;
import org.self.utils.Crypto;
import org.self.utils.json.JSONObject;

public class seedrandom extends Command {

	public seedrandom() {
		super("seedrandom","[modifier:] - Generate a random value, based on your SEED and a modifier");
	}
	
	@Override
	public String getFullHelp() {
		return "\nseedrandom\n"
				+ "\n"
				+ "Generate a random value, based on your SEED and a modifier.\n"
				+ "\n"
				+ "modifier: \n"
				+ "    The modifier - added to seed before hash.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "seedrandom modifier:\"Hello you\"\n"
				+ "\n";	
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"modifier"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		//Check not locked..
		if(!SelfDB.getDB().getWallet().isBaseSeedAvailable()) {
			throw new CommandException("DB locked!");
		}
		
		//Get the modifier..
		String modifier = getParam("modifier");
		
		//Get the minidata version.. 
		MiniData moddata = MiniData.getMiniDataVersion(new MiniString(modifier));
		
		//Make it different from the default - so can't reproduce normal private keys
		MiniData hashmod = Crypto.getInstance().hashObjects(moddata, new MiniData("0xDEADDEAD"));
				
		//Now get the base seed..
		SeedRow sr = SelfDB.getDB().getWallet().getBaseSeed();
		
		//Hash them together..
		MiniData hash = Crypto.getInstance().hashObjects(hashmod, new MiniData(sr.getSeed()));
		
		JSONObject resp = new JSONObject();
		resp.put("modifier", modifier);
		resp.put("seedrandom", hash.to0xString());
			
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new seedrandom();
	}

}
