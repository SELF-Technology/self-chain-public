package org.self.system.commands.base;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import org.self.database.SelfDB;
import org.self.objects.base.MiniData;
import org.self.system.SELFSystem;
import org.self.system.commands.Command;
import org.self.system.mds.ServiceJSRunner;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageProcessor;
import org.self.utils.messages.TimerMessage;
import org.self.utils.messages.TimerProcessor;

public class systemcheck extends Command {

	public systemcheck() {
		super("systemcheck","Check system processors..");
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"processor","action"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
	
		
		JSONObject resp = new JSONObject();
		
		String action=getParam("action","list");
		
		if(action.equals("list")) {
			//Get info about each Process Manager
			resp.put("Main", getInfo(SELFSystem.getInstance()));
			resp.put("TxPowProcesssor", getInfo(SELFSystem.getInstance().getTxPoWProcessor()));
			resp.put("TxPowMiner", getInfo(SELFSystem.getInstance().getTxPoWMiner()));
			resp.put("NIOManager", getInfo(SELFSystem.getInstance().getNIOManager()));
			resp.put("P2PManager", getInfo(SELFSystem.getInstance().getNetworkManager().getP2PManager()));
			
			resp.put("MDSManager", getInfo(SELFSystem.getInstance().getMDSManager()));
			
			//And Print out the servcies details..
			ArrayList<ServiceJSRunner> services = SELFSystem.getInstance().getMDSManager().getAllServices();
			JSONArray servs = new JSONArray();
			for(ServiceJSRunner runner : services) {
				JSONObject dets = new JSONObject();
				dets.put("service", runner.getMiniDapp().getName());
				dets.put("processor", getInfo(runner));
				servs.add(dets);
			}
			resp.put("MDSServices", servs);
			
			resp.put("SendPollManager", getInfo(SELFSystem.getInstance().getSendPoll()));
			resp.put("NotifyManager", getInfo(SELFSystem.getInstance().getNotifyManager()));
			
			//The Timer Processor..
			TimerProcessor tp = TimerProcessor.getTimerProcessor();
			resp.put("TimerProcessor", tp.getSize());
			
			resp.put("RWLockInfo", SelfDB.getDB().getRWLockInfo());
			
			resp.put("writelockthread", SelfDB.getDB().mCurrentWriteLockThread);
			resp.put("writelockthreadstate", SelfDB.getDB().mCurrentWriteLockState);
			
			resp.put("Shutting Down", SELFSystem.getInstance().isShuttingDown());
			
			//Check the Read Write Lock..
			SelfLogger.log("Posting Checker Call in TxPoWProcessor..");
			SELFSystem.getInstance().getTxPoWProcessor().postCheckCall();

			//Post a message to the Mina thread also..
			SelfLogger.log("Posting Checker Call in Main..");
			SELFSystem.getInstance().PostMessage(SELFSystem.SELF_CALLCHECKER);
			
			//And a timer message
			Message msg = new Message(SELFSystem.SELF_CALLCHECKER);
			msg.addBoolean("timer", true);
			TimerMessage timed = new TimerMessage(1000, msg);
			SelfLogger.log("Posting TIMED Checker Call in Main..");
			SELFSystem.getInstance().PostTimerMessage(timed);
		
		}else if(action.equals("details")) {
			
			String proc = getParam("processor");
			
			if(proc.equalsIgnoreCase("p2pmanager")) {
				printDetails(SELFSystem.getInstance().getNetworkManager().getP2PManager());
				
			}else if(proc.equalsIgnoreCase("niomanager")) {
				printDetails(SELFSystem.getInstance().getNIOManager());
			
			}else if(proc.equalsIgnoreCase("main")) {
				printDetails(SELFSystem.getInstance());
			
			}else if(proc.equalsIgnoreCase("txpowprocessor")) {
				printDetails(SELFSystem.getInstance().getTxPoWProcessor());
			
			}else if(proc.equalsIgnoreCase("txpowminer")) {
				printDetails(SELFSystem.getInstance().getTxPoWMiner());
			
			}else if(proc.equalsIgnoreCase("mdsmanager")) {
				printDetails(SELFSystem.getInstance().getMDSManager());
			
			}else if(proc.equalsIgnoreCase("notifymanager")) {
				printDetails(SELFSystem.getInstance().getNotifyManager());
			
			}else if(proc.equalsIgnoreCase("senpollmanager")) {
				printDetails(SELFSystem.getInstance().getSendPoll());
			
			}else if(proc.equalsIgnoreCase("timerprocessor")) {
				SelfLogger.log("Processor Details  : TimerProcessor",false);
				TimerProcessor.getTimerProcessor().printAllMessages();
			}
			
			resp.put("details", "Sent to Self Log");
		}
		
		ret.put("response", resp);

		return ret;
	}
	
	public void printDetails(MessageProcessor zProc) {
		SelfLogger.log("Processor Details : "+zProc.getName(),false);
		zProc.printAllMessages();
	}
	
	public JSONObject getInfo(MessageProcessor zProc) {
		JSONObject ret = new JSONObject();
		ret.put("stack", zProc.getSize());
		
		Message lst = zProc.getLastMessage();
		if(lst == null) {
			ret.put("lastmessage", null);
		}else {
			ret.put("lastmessage", lst.toString());
		}
		return ret;
	}
	
		
	@Override
	public Command getFunction() {
		return new systemcheck();
	}
}