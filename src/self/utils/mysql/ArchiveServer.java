package org.self.utils.mysql;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import org.self.database.cascade.Cascade;
import org.self.objects.IBD;
import org.self.objects.TxBlock;
import org.self.objects.base.MiniByte;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;
import org.self.system.network.self.NIOManager;
import org.self.system.network.self.NIOMessage;
import org.self.system.network.rpc.HTTPServer;
import org.self.utils.SelfLogger;

public class ArchiveServer extends HTTPServer {

	public MySQLConnect mMySQL = null;
	
	public Cascade mCascade = null;
	
	static long mLastClean = 0 ;
	public static synchronized void SystemClean() {
		long timenow = System.currentTimeMillis();
		if(timenow - mLastClean > 10000) {
			SelfLogger.log("System Clean..");
			System.gc();
			mLastClean = timenow;
		}
	}
	
	public class ArchiveHandler implements Runnable{

		Socket mSocket;
		
		public ArchiveHandler(Socket zSocket) {
			mSocket = zSocket;
		}
		
		public synchronized ArrayList<TxBlock> reconnectLoadTxBlocks(MiniNumber zFirstBlock) {
			//Load the block range..
			try {
				
				return mMySQL.loadBlockRange(zFirstBlock);
				
			}catch(Exception zExc) {
				SelfLogger.log("Connection failed.. reconnecting.. : "+zExc);

				//Wait a sec..
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
				
				//Try again
				return mMySQL.loadBlockRange(zFirstBlock);
			}
		}
		
		@Override
		public void run() {
			try {
				DataInputStream maindis = new DataInputStream(mSocket.getInputStream());
				DataOutputStream dos	= new DataOutputStream(mSocket.getOutputStream());
				
				//First read in the complete MiniData package
				MiniData data = null;
				try {
					data = MiniData.ReadFromStream(maindis);
				}catch(Exception exc) {
					//Not a Self connection - just random internet traffic
					return;
				}
				
				//Convert..
				ByteArrayInputStream bais 	= new ByteArrayInputStream(data.getBytes());
				DataInputStream dis 		= new DataInputStream(bais);
				
				//What Type..
				MiniByte type = MiniByte.ReadFromStream(dis);
				
				//What block are we starting from..
				MiniNumber firstblock = null;
				try {
					firstblock 	= MiniNumber.ReadFromStream(dis);
				}catch(Exception exc) {
					//Not a Self connection - just random internet traffic
					return;
				}
				
				SelfLogger.log("Received request first block : "+firstblock, false);
		
				//Get the IBD
				IBD ibd = new IBD();
				
				//Is this the initial
				if(firstblock.isEqual(MiniNumber.MINUSONE)) {
					//Testing the connection - Reconnect to the DB to make sure it is alive..
					ArrayList<TxBlock> testblocks = reconnectLoadTxBlocks(MiniNumber.ZERO);
					
				}else {
					
					//Do we have a cascade - only check on first call..
					if(firstblock.isEqual(MiniNumber.ZERO) && mCascade!=null) {
						SelfLogger.log("Adding cascade..");
						ibd.setCascade(mCascade);
					}
					
					//Get the blocks
					ArrayList<TxBlock> ibdblocks = ibd.getTxBlocks(); 
					
					//Load the block range..
					ArrayList<TxBlock> blocks = reconnectLoadTxBlocks(firstblock);
					if(blocks != null) {
						for(TxBlock block : blocks) {
							ibdblocks.add(block);
						}
					}else {
						
						//Close the streams
						dis.close();
						dos.close();
						
						return;
					}
				}
				
				//Create the network message
				MiniData netdata = NIOManager.createNIOMessage(NIOMessage.MSG_ARCHIVE_DATA, ibd);
				
				//And send it..
				netdata.writeDataStream(dos);
				
				//Flush
				dos.flush();
				
				//Wait a few seconds for the download to be processed
				Thread.sleep(10000);
				
				//Close the streams
				bais.close();
				dis.close();
				dos.close();
				maindis.close();
				
			} catch (Exception e) {
				SelfLogger.log(e);
			}
			
			//Clean up
			SystemClean();
		}
	}
	
	public ArchiveServer(int zPort, String zServer, String zDB, String zUser, String zPassowrd) throws SQLException {
		super(zPort,false);
		
		mMySQL = new MySQLConnect(zServer,zDB,zUser,zPassowrd);
		mMySQL.init();
		
		//Do some checks
		mCascade = mMySQL.loadCascade();
		if(mCascade == null) {
			SelfLogger.log("No Cascade found..");
		}else {
			SelfLogger.log("Cascade found.. tip:"+mCascade.getTip().getTxPoW().getBlockNumber());
		}
		
		start();
	}

	@Override
	public Runnable getSocketHandler(Socket zSocket) {
		return new ArchiveHandler(zSocket);
	}
	
	public static void main(String[] zArgs) throws SQLException {
		
		SelfLogger.log("Starting Archive Server v1.3");
		
		//Load the MySQL driver
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		
		String mysqlhost 	 = null;
		String mysqldb	 	 = null;
		String mysqluser 	 = null;
		String mysqlpassword = null;
		int port 			 = 8888;
		
		//Check the Environment variables..
		Map<String,String> env = System.getenv();
		if(env.get("MYSQL_HOST")!=null) {
			mysqlhost = env.get("MYSQL_HOST");
		}
		if(env.get("MYSQL_DB")!=null) {
			mysqldb = env.get("MYSQL_DB");
		}
		if(env.get("MYSQL_USER")!=null) {
			mysqluser = env.get("MYSQL_USER");
		}
		if(env.get("MYSQL_PASSWORD")!=null) {
			mysqlpassword = env.get("MYSQL_PASSWORD");
		}
		if(env.get("ARCHIVE_PORT")!=null) {
			port = Integer.parseInt(env.get("ARCHIVE_PORT"));
		}
		
		//Now check the command line..
		int arglen 	= zArgs.length;
		if(arglen > 0) {
			int counter	=	0;
			while(counter<arglen) {
				String arg 	= zArgs[counter];
				counter++;
				
				if(arg.equals("-mysqlhost")) {
					mysqlhost = zArgs[counter++];
					
				}else if(arg.equals("-port")) {
					port = Integer.parseInt(zArgs[counter++]);
				
				}else if(arg.equals("-mysqldb")) {
					mysqldb   = zArgs[counter++];
				
				}else if(arg.equals("-mysqluser")) {
					mysqluser   = zArgs[counter++];
				
				}else if(arg.equals("-mysqlpassword")) {
					mysqlpassword  = zArgs[counter++];
				
				}else if(arg.equals("-help")) {
					
					System.out.println("Self Archive Server v0.8 Help");
					System.out.println(" -mysqlhost      : The MySQL Host server");
					System.out.println(" -mysqldb        : The MySQL Database");
					System.out.println(" -mysqluser      : The MySQL User");
					System.out.println(" -mysqlpassword  : The MySQL password");
					System.out.println(" -help           : Print this help");
					
					System.exit(1);
					
				}else {
					System.out.println("Unknown parameter : "+arg);
					System.exit(1);
				}
			}
		}
		
		
		ArchiveServer server 	= new ArchiveServer(port, mysqlhost, mysqldb, mysqluser, mysqlpassword);
		
		//Listen for input
		InputStreamReader is    = new InputStreamReader(System.in, Charset.forName("UTF-8"));
	    BufferedReader bis      = new BufferedReader(is);
	    
	    //Loop until finished..
	    while(true){
	        try {
	            //Get a line of input
	            String input = bis.readLine();
	            
	            //Check valid..
	            if(input!=null && !input.equals("")) {
	            	//trim it..
	            	input = input.trim();
	            	
	            	//Check the Command
	            	if(input.equals("quit")) {
	            		break;
	            	}else {
	            		SelfLogger.log("Unknown command : "+input, false);
	            	}
	            }
	            
	        } catch (Exception ex) {
	            SelfLogger.log(ex);
	        }
	    }
	    
	    //Cross the streams..
	    try {
	        bis.close();
	        is.close();
	    } catch (IOException ex) {
	    	SelfLogger.log(""+ex, false);
	    }
		
	    //Stop the Server
	    server.shutdown();

	    SelfLogger.log("Archive Server stopped", false);
	}
}
