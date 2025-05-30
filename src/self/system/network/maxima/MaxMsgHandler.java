package org.self.system.network.maxima;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.self.database.SelfDB;
import org.self.database.txpowtree.TxPoWTreeNode;
import org.self.objects.base.MiniByte;
import org.self.objects.base.MiniData;
import org.self.system.SELFSystem;
import org.self.system.network.maxima.message.MaxTxPoW;
import org.self.system.network.maxima.message.MaximaErrorMsg;
import org.self.system.network.maxima.message.MaximaInternal;
import org.self.system.network.maxima.message.MaximaMessage;
import org.self.system.network.maxima.message.MaximaPackage;
import org.self.system.network.maxima.mls.MLSPacketGETResp;
import org.self.system.network.self.NIOManager;
import org.self.system.network.self.NIOMessage;
import org.self.system.params.GeneralParams;
import org.self.utils.SelfLogger;
import org.self.utils.encrypt.CryptoPackage;
import org.self.utils.encrypt.SignVerify;
import org.self.utils.messages.Message;
import org.self.utils.messages.MessageProcessor;

public class MaxMsgHandler extends MessageProcessor {

	public static final String MAX_SEND_MESSAGE = "MAX_SEND_MESSAGE";
	
	MaximaManager mMaxima;
	
	public MaxMsgHandler(MaximaManager zMaxima) {
		super("MAX_MSG_HANDLER");
		
		mMaxima = zMaxima;
	}
	
	@Override
	protected void processMessage(Message zMessage) throws Exception {
		
		if(zMessage.getMessageType().equals(MAX_SEND_MESSAGE)) {

			//Get the Message 
			Message sendmessage = (Message) zMessage.getObject("msg");
			
			//Who to..
			String host 	= sendmessage.getString("tohost");
			int port		= sendmessage.getInteger("toport");
			
			//Do we have a tip..
			TxPoWTreeNode tip = SelfDB.getDB().getTxPoWTree().getTip();
			if(tip == null) {
				//No TIP - cannot construct a MaxTxPoW..
				return;
			}
			
			try {
				//Create the Maxima Message
				MiniData maxmsg = constructMaximaData(sendmessage);
				if(maxmsg == null) {
					throw new Exception("Could not build Maxima message in time..");
				}
				
				//Send the message
				MiniData validresp = sendMaxPacket(host,port, maxmsg);
				
				//Check the Response..
				if(validresp.isEqual(MaximaManager.MAXIMA_RESPONSE_OK)) {
					//All fine.. 
				}else if(validresp.isEqual(MaximaManager.MAXIMA_RESPONSE_FAIL)) {
					if(GeneralParams.MAXIMA_LOGS) {
						SelfLogger.log("Warning : Maxima message not delivered to.. "+host+":"+port);
					}
				}else if(validresp.isEqual(MaximaManager.MAXIMA_RESPONSE_TOOBIG)) {
					SelfLogger.log("Warning : Maxima message too big not delivered to.. "+host+":"+port);
				}else if(validresp.isEqual(MaximaManager.MAXIMA_RESPONSE_UNKNOWN)) {
					if(GeneralParams.MAXIMA_LOGS) {
						SelfLogger.log("Warning : Maxima message Unknown Address not delivered to.. "+host+":"+port);
					}
				}else if(validresp.isEqual(MaximaManager.MAXIMA_RESPONSE_WRONGHASH)) {
					SelfLogger.log("Warning : Maxima message TxPoW Hash wrong not delivered to.. "+host+":"+port);
				}else {
					SelfLogger.log("Unknown Maxima response message "+validresp.to0xString());
				}
			
			} catch (SocketTimeoutException ce) {
				
			} catch (ConnectException ce) {
				
			} catch (Exception e) {
				SelfLogger.log("MaxMsgHandler "+host+":"+port+" "+e.toString());
			}
		}
	}

	public static MiniData constructMaximaData(Message zMessage) throws Exception {
		//Message details
		String publickey	= zMessage.getString("publickey");
		MiniData topubk 	= new MiniData(publickey);
		
		String tohost 		= zMessage.getString("tohost");
		int toport			= zMessage.getInteger("toport");
		
		//Get the Maxima Message
		MaximaMessage maxima 	= (MaximaMessage) zMessage.getObject("maxima");
		
		//Next Sign the Message and create the MaximaInternal message
		MiniData maxdata		= MiniData.getMiniDataVersion(maxima);
		MiniData privatekey		= (MiniData) zMessage.getObject("myprivatekey");
		byte[] sigBytes  		= SignVerify.sign(privatekey.getBytes(), maxdata.getBytes());
		
		MaximaInternal msign 	= new MaximaInternal();
		msign.mFrom				= (MiniData) zMessage.getObject("mypublickey");
		msign.mData				= maxdata;
		msign.mSignature		= new MiniData(sigBytes);
		
		//And finally create the encrypted MaximaPackage
		MiniData maxpkg			= MiniData.getMiniDataVersion(msign);
		
		//Now Encrypt the Whole Thing..
		CryptoPackage cp = new CryptoPackage();
		cp.encrypt(maxpkg.getBytes(), topubk.getBytes());
		
		//Now Construct a MaximaPackage
		MaximaPackage mp = new MaximaPackage( topubk , cp.getCompleteEncryptedData());
		
		//Time it..
		long timenow = System.currentTimeMillis();
		
		//Now create a MaxTxPow
		MaxTxPoW sxtxpow = MaxTxPoW.createMaxTxPoW(mp);
		if(sxtxpow == null) {
			//Could not create the MaxTxPoW in time..
			return null;
		}
		
		//Message if took a long time
		long timediff = System.currentTimeMillis() - timenow;
		if(timediff>5000) {
			SelfLogger.log("Maxima Construct took more than 5 seconds.. "+timediff);
		}
		
		//Create the Network Message
		return NIOManager.createNIOMessage(NIOMessage.MSG_MAXIMA_TXPOW, sxtxpow);
	}
	
	public static MiniData sendMaxPacket(String zHost, int zPort, MiniData zMaxMessage) throws IOException {
		
		//Open the socket..
		Socket sock = new Socket();

		//20 seconds to connect
		sock.connect(new InetSocketAddress(zHost, zPort), 20000);
		
		//20 seconds to read
		sock.setSoTimeout(20000);
		
		//Create the streams..
		OutputStream out 		= sock.getOutputStream();
		DataOutputStream dos 	= new DataOutputStream(out);
		
		InputStream in			= sock.getInputStream();
		DataInputStream dis 	= new DataInputStream(in);
		
		//Write the data
		zMaxMessage.writeDataStream(dos);
		dos.flush();
		
		//Tell the NIO
		SELFSystem.getInstance().getNIOManager().getTrafficListener().addWriteBytes("sendMaxPacket",zMaxMessage.getLength());
		
		//Now get a response.. should be ONE_ID.. give it 10 second max.. ( might get a block..)
		MiniData valid = MaximaManager.MAXIMA_RESPONSE_FAIL;
		long maxtime = System.currentTimeMillis() + 20000;
		while(System.currentTimeMillis() < maxtime) {
			
			//Read the data
			MiniData resp = MiniData.ReadFromStream(dis);
			
			//Tell the NIO
			SELFSystem.getInstance().getNIOManager().getTrafficListener().addReadBytes("sendMaxPacket",resp.getLength());
			
			if(resp.isEqual(MaximaManager.MAXIMA_RESPONSE_OK)) {
				valid = resp;
				break;
			}else if(resp.isEqual(MaximaManager.MAXIMA_RESPONSE_FAIL)) {
				valid = resp;
				break;
			}else if(resp.isEqual(MaximaManager.MAXIMA_RESPONSE_TOOBIG)) {
				valid = resp;
				break;
			}else if(resp.isEqual(MaximaManager.MAXIMA_RESPONSE_UNKNOWN)) {
				valid = resp;
				break;
			}else if(resp.isEqual(MaximaManager.MAXIMA_RESPONSE_WRONGHASH)) {
				valid = resp;
				break;
			}else{
				
				//Try a couple of different messages..
				boolean found 	= true;
				byte[] msgdata 	= resp.getBytes();	 
				
				//Is it an MLS Get Package..
				try {
					
					ByteArrayInputStream bais 	= new ByteArrayInputStream(msgdata);
					DataInputStream respdis 	= new DataInputStream(bais);
					
					//What Type..
					MiniByte type = MiniByte.ReadFromStream(respdis);
					MiniData data = MiniData.ReadFromStream(respdis);
					
					//Convert
					MLSPacketGETResp mls = MLSPacketGETResp.convertMiniDataVersion(data);
					
					//Ok - it's read
					valid = MaximaManager.MAXIMA_RESPONSE_OK;
					
					//Check ther Random UID - security
					if(!mls.getRandomUID().equals(MaximaManager.MLS_RANDOM_UID)) {
						SelfLogger.log("Invalid MLS GET RandomUID! from "+sock.getInetAddress().toString());
						valid = MaximaManager.MAXIMA_RESPONSE_FAIL;
					
					}else{
						//Post this on Maxima..
						Message max = new Message(MaximaManager.MAXIMA_MLSGET_RESP);
						max.addObject("mlsget", mls);
						SELFSystem.getInstance().getMaxima().PostMessage(max);
					}
					
					respdis.close();
					bais.close();
					
					break;
					
				}catch(Exception exc){
					found = false;
					//SelfLogger.log("Could not convert MLS GET Package : "+exc);
				}
				
				//Is it an error Message
				if(!found) {
					
					ByteArrayInputStream bais 	= new ByteArrayInputStream(msgdata);
					DataInputStream respdis 	= new DataInputStream(bais);
					
					try {
						
						//What Type..
						MiniByte type = MiniByte.ReadFromStream(respdis);
						MiniData data = MiniData.ReadFromStream(respdis);
						
						//Convert
						MaximaErrorMsg error = MaximaErrorMsg.convertMiniDataVersion(data);
						
						//Print this Out!
						SelfLogger.log("MAXIMA SEND ERROR "+error.getError().toString());
						
						//Ok - it's read
						valid = MaximaManager.MAXIMA_RESPONSE_FAIL;
						
						break;
						
					}catch(Exception exc){
						found = false;
						//SelfLogger.log("Could not convert MLS GET Package : "+exc);
					
					}finally {
						
						//Close resources
						respdis.close();
						bais.close();
					}
				}
			}
		}
		
		//Close the streams..
		dis.close();
		in.close();
		dos.close();
		out.close();
		
		return valid;
	}
}
