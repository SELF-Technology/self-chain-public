package org.self.system.network.maxima;

import java.util.ArrayList;

import org.self.database.SelfDB;
import org.self.database.maxima.MaximaContact;
import org.self.database.maxima.MaximaDB;
import org.self.database.maxima.MaximaHost;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.objects.Address;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.objects.base.MiniString;
import org.self.system.SELFSystem;
import org.self.system.commands.maxima.maxima;
import org.self.system.network.self.NIOClient;
import org.self.system.network.self.NIOManager;
import org.self.system.network.self.NIOMessage;
import org.self.system.params.GeneralParams;
import org.self.utils.SelfLogger;
import org.self.utils.json.JSONObject;
import org.self.utils.json.parser.JSONParser;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageProcessor;

public class MaximaContactManager extends MessageProcessor {

	public static final String CONTACT_APPLICATION 		 = "**maxima_contact_ctrl**";
	
	public static final String MAXCONTACTS_RECMESSAGE 	 = "MAXCONTACTS_RECMESSAGE";
	public static final String MAXCONTACTS_UPDATEINFO 	 = "MAXCONTACTS_SENDMESSAGE";
	
	public static final String MAXCONTACTS_DELETECONTACT = "MAXCONTACTS_DELETECONTACT";
	
	MaximaManager mManager;
	
	public boolean mEnableOutsideContactRequest = true;
	public ArrayList<String> mAllowedContacts 	= new ArrayList<>();
	
	public MaximaContactManager(MaximaManager zManager) {
		super("MAXIMA_CONTACTS");
		
		mManager = zManager;
	}
	
	/**
	 * Are Users allowed to add you as a contact without your say so..
	 */
	public boolean isAllowedAll() {
		return mEnableOutsideContactRequest;
	}
	
	public void setAllowContact(boolean zAllow) {
		mEnableOutsideContactRequest = zAllow;
		
		//Save to DB
		SelfDB.getDB().getUserDB().setMaximaAllowContacts(zAllow);
		SelfDB.getDB().saveUserDB();
	}
	
	public void addValidContactRequest(String zPublicKey) {
		SelfLogger.log("Valid Contact Request added : "+zPublicKey);
		if(!mAllowedContacts.contains(zPublicKey)) {
			mAllowedContacts.add(zPublicKey);
		}
	}
	
	public void clearAllowedContactRequest() {
		mAllowedContacts.clear();
	}
	
	public ArrayList<String> getAllowed(){
		return mAllowedContacts;
	}
	
	public JSONObject getMaximaContactInfo(boolean zIntro, boolean zDelete) {
		JSONObject ret = new JSONObject();
		
		ret.put("delete", zDelete);
		
		if(zDelete) {
			ret.put("intro", false);
			ret.put("publickey", mManager.getPublicKey().to0xString());
			ret.put("address", "");
			
			//Extra Data
			ret.put("name", "");
			ret.put("selfaddress", "Sx00");
			ret.put("topblock",MiniNumber.ZERO.toString());
			ret.put("checkblock",MiniNumber.ZERO.toString());
			ret.put("checkhash",MiniData.ZERO_TXPOWID.toString());
			ret.put("mls","");
			
		}else {
			
			//Get some info about the chain
			TxPoWTreeNode tip 	= SelfDB.getDB().getTxPoWTree().getTip();
			
			//Are we on test net - shorter chain
			TxPoWTreeNode tip50 = null;
			if(GeneralParams.TEST_PARAMS) {
				tip50	= tip.getParent(16);
			}else {
				tip50	= tip.getParent(50);
			}
			
			ret.put("intro", zIntro);
			ret.put("publickey", mManager.getPublicKey().to0xString());
			ret.put("address", mManager.getRandomMaximaAddress());
			
			//Extra Data
			ret.put("name", SelfDB.getDB().getUserDB().getMaximaName());
			ret.put("icon", SelfDB.getDB().getUserDB().getMaximaIcon());
			
			String selfaddress = SelfDB.getDB().getWallet().getDefaultAddress().getAddress();
			String sxaddress 	 = Address.makeSelfAddress(new MiniData(selfaddress)); 
			ret.put("selfaddress", sxaddress);
			
			ret.put("topblock",tip.getBlockNumber().toString());
			ret.put("checkblock",tip50.getBlockNumber().toString());
			ret.put("checkhash",tip50.getTxPoW().getTxPoWID());
			ret.put("mls",mManager.getMLSHost());
		}
		
		return ret;
	}
	
	@Override
	protected void processMessage(Message zMessage) throws Exception {
		
		//Get the DB
		MaximaDB maxdb = SelfDB.getDB().getMaximaDB();
		
		if(zMessage.getMessageType().equals(MAXCONTACTS_RECMESSAGE)) {
			
			//get the max json
			JSONObject maxjson = (JSONObject) zMessage.getObject("maxmessage");
			
			//Get the public key
			String publickey = maxjson.getString("from");
			
			//Get the data
			String data 	= maxjson.getString("data");
			MiniData dat 	= new MiniData(data);
			
			//Convert to a JSON
			MiniString datastr 		= new MiniString(dat.getBytes());
			JSONObject contactjson 	= (JSONObject) new JSONParser().parse(datastr.toString());
			
			//Process this special contacts message..
			String contactkey = (String) contactjson.get("publickey"); 
			if(!contactkey.equals(publickey)) {
				SelfLogger.log("Received contact message with mismatch public keys..");
				return;
			}
			
			//OK - lets get his current address
			boolean intro		= (boolean)contactjson.get("intro");
			boolean delete		= (boolean)contactjson.get("delete");
			
			//Their Address
			String address 		= (String) contactjson.get("address");
			
			//Few checks on name
			String name = (String) contactjson.getString("name","");
			name = name.replace("\"", "").replace("'", "").replace(";", "");
			
			String icon = (String) contactjson.getString("icon","");
			icon = icon.replace("\"", "").replace("'", "").replace(";", "");
			
			//Create a Contact - if not there already
			MaximaContact checkcontact = maxdb.loadContactFromPublicKey(publickey);
			
			//Are we being deleted..
			if(delete) {
				SelfLogger.log("DELETED contact request from : "+publickey);
				if(checkcontact != null) {
					maxdb.deleteContact(checkcontact.getUID());
				
					//Contacts have changed
					mManager.NotifyMaximaContactsChanged();
				}
				
				return;
			}
			
			//The ExtraData
			String sxaddress		= (String) contactjson.get("selfaddress");
			MiniNumber topblock 	= new MiniNumber((String) contactjson.get("topblock"));
			MiniNumber checkblock 	= new MiniNumber((String) contactjson.get("checkblock"));
			MiniData checkhash 		= new MiniData((String) contactjson.get("checkhash"));
			String mls				= contactjson.getString("mls");
			
			MaximaContact sxcontact = new MaximaContact(publickey);
			sxcontact.setCurrentAddress(address);
			sxcontact.setLastSeen(System.currentTimeMillis());
			
			if(checkcontact == null) {
				
				//Are we allowing all contact requests
				if(!mEnableOutsideContactRequest) {
					
					//make sure we have ok'ed this
					if(!mAllowedContacts.contains(publickey)) {
						SelfLogger.log("[!] NOT ALLOWED CONTACT REQUEST FROM : "+contactjson.toString());
						return;
					}
				}
				
				SelfLogger.log("ADDED NEW MAXIMA CONTACT : "+name);
				
				//New Contact
				sxcontact.setName(name);
				sxcontact.setIcon(icon);
				
				sxcontact.setSelfAddress(sxaddress);
				sxcontact.setBlockDetails(topblock, checkblock, checkhash);
				sxcontact.setMLS(mls);
				
				maxdb.newContact(sxcontact);
				
			}else{
				//Set this FIRST
				sxcontact.setExtraData(checkcontact.getExtraData());
				sxcontact.setMyAddress(checkcontact.getMyAddress());
				
				//Overwrite with the new details
				sxcontact.setName(name);
				sxcontact.setIcon(icon);
				
				sxcontact.setSelfAddress(sxaddress);
				sxcontact.setBlockDetails(topblock, checkblock, checkhash);
				sxcontact.setMLS(mls);
				
				maxdb.updateContact(sxcontact);
			}
			
			//Send a message that Tells there has been an update..
			mManager.NotifyMaximaContactsChanged();
			
			//Send them a contact message aswell..
			if(intro || checkcontact == null) {
				Message msg = new Message(MAXCONTACTS_UPDATEINFO);
				msg.addString("publickey", publickey);
				msg.addString("address", address);
				PostMessage(msg);
			}
			
		}else if(zMessage.getMessageType().equals(MAXCONTACTS_UPDATEINFO)) {
			
			//Are we deleting..
			boolean delete = false;
			if(zMessage.exists("delete")) {
				delete = zMessage.getBoolean("delete");
			}
			
			//Who To..
			String publickey = zMessage.getString("publickey");
			String address 	 = zMessage.getString("address");
			
			//Send a Contact info message to a user
			JSONObject mycontactinfo	= getMaximaContactInfo(false,delete);
			
			//Now Update Our DB..
			if(!delete) {
				MaximaContact sxcontact = maxdb.loadContactFromPublicKey(publickey);
				sxcontact.setMyAddress((String)mycontactinfo.get("address"));
				maxdb.updateContact(sxcontact);
			}
			
			//There has been an Update
			mManager.NotifyMaximaContactsChanged();
			
			MiniString str			= new MiniString(mycontactinfo.toString());
			MiniData mdata 			= new MiniData(mycontactinfo.toString().getBytes(MiniString.SELF_CHARSET));
			
			//Now convert into the correct message..
			Message sender = maxima.createSendMessage(address, CONTACT_APPLICATION , mdata);
			
			//Post it on the stack
			mManager.PostMessage(sender);	
		
		}else if(zMessage.getMessageType().equals(MAXCONTACTS_DELETECONTACT)) {
			
			//Few steps here..
			int id = zMessage.getInteger("id");
			
			//Get that contact
			MaximaContact mcontact = maxdb.loadContactFromID(id);
			if(mcontact == null) {
				SelfLogger.log("Trying to remove unknown Contact ID : "+id);
				return;
			}
			
			//Where are WE 
			String myaddress = mcontact.getMyAddress();
			
			//Get the host..
			int index 	= myaddress.indexOf("@");
			String host = myaddress.substring(index+1);
			
			//Get the client
			MaximaHost sxhost = maxdb.loadHost(host);
			
			//Reset that host PubKey.. and Update DB
			if(sxhost != null) {
				sxhost.createKeys();
				sxhost.updateLastSeen();
				maxdb.updateHost(sxhost);
			}
			
			//Delete the contact
			SelfLogger.log("DELETED MAXIMA CONTACT : "+mcontact.getName());
			maxdb.deleteContact(id);
			
			//Contacts have changed
			mManager.NotifyMaximaContactsChanged();
			
			//Tell them
			NIOClient nioc = SELFSystem.getInstance().getNIOManager().getNIOClient(host); 
			if(nioc != null) {
				//So we know the details.. Post them to him.. so he knows who we are..
				MaximaCTRLMessage maxmess = new MaximaCTRLMessage(MaximaCTRLMessage.MAXIMACTRL_TYPE_ID);
				maxmess.setData(sxhost.getPublicKey());
				NIOManager.sendNetworkMessage(nioc.getUID(), NIOMessage.MSG_MAXIMA_CTRL, maxmess);
			}
			
			//Refresh ALL users..
			mManager.PostMessage(MaximaManager.MAXIMA_REFRESH);
			
			//Send him a message saying we have deleted him..
			Message msg = new Message(MAXCONTACTS_UPDATEINFO);
			msg.addBoolean("delete", true);
			msg.addString("publickey", mcontact.getPublicKey());
			msg.addString("address", mcontact.getCurrentAddress());
			PostMessage(msg);
		}
		
	}

}
