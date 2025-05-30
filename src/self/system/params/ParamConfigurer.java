package org.self.system.params;

import static java.nio.file.Files.lines;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.self.system.params.ParamConfigurer.ParamKeys.toParamKey;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;

import org.self.objects.base.MiniNumber;
import org.self.system.commands.backup.mysql;
import org.self.system.network.p2p.P2PFunctions;
import org.self.system.network.p2p.params.P2PParams;
import org.self.utils.SelfLogger;
import org.self.utils.RPCClient;

public class ParamConfigurer {

    private final Map<ParamKeys, String> paramKeysToArg = new HashMap<>();
    private boolean daemon = false;
    //private boolean rpcenable = false;
    private boolean mShutdownhook = true;
    
    public ParamConfigurer usingConfFile(String[] programArgs) {
        List<String> zArgsList = Arrays.asList(programArgs);
        int confArgKeyIndex = zArgsList.indexOf("-" + ParamKeys.conf);

        if (confArgKeyIndex > -1) {
            final String confFileArgValue = zArgsList.get(confArgKeyIndex + 1);
            File confFile = new File(confFileArgValue);

            try {
                paramKeysToArg.putAll(lines(confFile.toPath())
                        .map(line -> {
                            final String[] split = line.split("=");
                            return new Pair(split[0], split.length > 1 ? split[1] : "");
                        })
                        .map(pair -> toParamKey((String) pair.left)
                                .map(paramKey -> new Pair(paramKey, pair.right)))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(toMap(
                                pair -> (ParamKeys) pair.left,
                                pair -> (String) pair.right)));
            } catch (IOException exception) {
                System.out.println("Unable to read conf file.");
                System.exit(1);
            }
        }
        return this;
    }

    public ParamConfigurer usingEnvVariables(Map<String, String> envVariableMap) {
        paramKeysToArg.putAll(envVariableMap.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().startsWith("self_"))
                .collect(toMap(
                        entry -> entry.getKey().toLowerCase().replaceFirst("self_", ""),
                        entry -> ofNullable(entry.getValue())
                                //.map(String::toLowerCase)
                                .orElse("")))
                .entrySet().stream()
                .filter(e -> toParamKey(e.getKey()).isPresent())
                .collect(toMap(entry -> toParamKey(entry.getKey()).get(), Map.Entry::getValue)));
        return this;
    }

	public ParamConfigurer usingProgramArgs(String[] programArgs) {

        int arglen = programArgs.length;
        int index = 0;
            while (index < arglen) {
                String arg = programArgs[index];
                final int imuCounter = index;
                progArgsToParamKey(arg)
                        .ifPresent(paramKey -> paramKeysToArg.put(paramKey,
                                lookAheadToNonParamKeyArg(programArgs, imuCounter).orElse("true")));
                index++;
            }
        
        return this;
    }

    public ParamConfigurer configure() {
        paramKeysToArg.forEach((key, value) -> key.consumer.accept(value, this));
       
        //Display the params
		  if(GeneralParams.SHOW_PARAMS) {
		        SelfLogger.log("Config Parameters");
		        for (Map.Entry<ParamKeys, String> entry : paramKeysToArg.entrySet()) {
		        	SelfLogger.log(entry.getKey() + ":" + entry.getValue());
		        }
		  }

        return this;
    }

    private static Optional<String> lookAheadToNonParamKeyArg(String[] programArgs, int currentIndex) {
        if (currentIndex + 1 <= programArgs.length - 1 ) {
            if (!progArgsToParamKey(programArgs[currentIndex + 1]).isPresent()) {
                return ofNullable(programArgs[currentIndex + 1]);
            }
        }
        return empty();
    }

    private static Optional<ParamKeys> progArgsToParamKey(String str) {
        if (str.startsWith("-")) {
            return of(ParamKeys.toParamKey(str.replaceFirst("-", ""))
                    .orElseThrow(() -> new UnknownArgumentException(str)));
        }
        return empty();
    }

    public boolean isDaemon() {
        return daemon;
    }

    public static boolean checkParams(String zFullParams) {
    	
    	//Break it up
    	StringTokenizer strtok = new StringTokenizer(zFullParams," ");
    	ArrayList<String> args = new ArrayList<>();
    	while(strtok.hasMoreTokens()) {
    		String tok = strtok.nextToken().trim();
    		if(!tok.equals("")) {
    			args.add(tok);
    		}
    	}
    	
    	//Convert to an array..
    	String[] allargs = args.toArray(new String[0]);
    	
    	return checkParams(allargs);
    }
    
    public static boolean checkParams(String[] zParams) {
    	
    	//Run through Params
		ParamConfigurer configurer = null;
		try {
			configurer = new ParamConfigurer().usingProgramArgs(zParams).configure();
		} catch (Exception ex) {
			return false;
		}
    	
    	return true;
    }
    
    public boolean isShutDownHook() {
        return mShutdownhook;
    }
    
    enum ParamKeys {
    	data("data", "Specify the data folder ( defaults to ~/.self )", (args, configurer) -> {
    		
    		//Get that folder
    		File dataFolder = new File(args);
    		
    		//Depends on the Base Self Version
    		File selffolder 	= new File(dataFolder,GlobalParams.SELF_BASE_VERSION);
    		selffolder.mkdirs();
    		
    		//Set this globally
    		GeneralParams.DATA_FOLDER 	= selffolder.getAbsolutePath();
        }),
    	dbpassword("dbpassword", "SELF Wallet / SQL AES password - MUST be specified on first launch. CANNOT be changed later.", (args, configurer) -> {
    		GeneralParams.IS_MAIN_DBPASSWORD_SET = true;
    		GeneralParams.MAIN_DBPASSWORD 		 = args;
        }),
    	basefolder("basefolder", "Specify a default file creation / backup / restore folder", (args, configurer) -> {
    		//Get that folder
    		File backupfolder 	= new File(args);
    		backupfolder.mkdirs();
    		
    		//Set this globally
    		GeneralParams.BASE_FILE_FOLDER = backupfolder.getAbsolutePath();
        }),
    	host("host", "Specify the host IP", (arg, configurer) -> {
            GeneralParams.SELF_HOST = arg;
            GeneralParams.IS_HOST_SET = true;
        }),
        port("port", "Specify the Self port", (arg, configurer) -> {
            GeneralParams.SELF_PORT = Integer.parseInt(arg);
        }),
        rpc("rpc", "Specify the RPC port", (arg, configurer) -> {
//            GeneralParams.RPC_PORT = Integer.parseInt(arg);
            SelfLogger.log("-rpc is no longer in use. Your RPC port is: " + GeneralParams.RPC_PORT);

        }),
        rpcenable("rpcenable", "Enable rpc", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.RPC_ENABLED = true;
            }else {
            	GeneralParams.RPC_ENABLED = false;
            }
        }),
        rpcpassword("rpcpassword", "Set Basic Auth password for RPC calls ( Use with SSL / stununel )", (args, configurer) -> {
        	GeneralParams.RPC_PASSWORD 		= args;
        	GeneralParams.RPC_AUTHENTICATE 	= true;
        }),
        rpcssl("rpcssl", "Use Self Signed SSL cert to run RPC", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.RPC_SSL = true;
            }else {
            	GeneralParams.RPC_SSL = false;
            }
        }),
        rpccrlf("rpccrlf", "Use CRLF at the end of the RPC headers (NodeJS)", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.RPC_CRLF = true;
            }else {
            	GeneralParams.RPC_CRLF = false;
            }
        }),
        shownetcalls("shownetcalls", "Show all the network calls", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.SHOW_NETWORK_CALLS = true;
                GeneralParams.SHOW_NETWORK_POLLS = true;
            }else {
            	GeneralParams.SHOW_NETWORK_CALLS = false;
            }
        }),
        shownetcallsnopoll("shownetcallsnopoll", "Show all the network calls except poll messages", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.SHOW_NETWORK_CALLS = true;
                GeneralParams.SHOW_NETWORK_POLLS = false;
            }else {
            	GeneralParams.SHOW_NETWORK_CALLS = false;
            }
        }),
        allowallip("allowallip", "Allow all IP for Maxima / Networking", (args, configurer) -> {
            if ("true".equals(args)) {
            	GeneralParams.ALLOW_ALL_IP = true;
            }else {
            	GeneralParams.ALLOW_ALL_IP = false;
            }
        }),
        archive("archive", "Run an Archive node - store all data / cascade for resync", (args, configurer) -> {
            if ("true".equals(args)) {
            	GeneralParams.ARCHIVE = true;
            }else {
            	GeneralParams.ARCHIVE = false;
            }
        }),
        mdsenable("mdsenable", "Enable MDS", (args, configurer) -> {
            if ("true".equals(args)) {
            	GeneralParams.MDS_ENABLED = true;
            }else {
            	GeneralParams.MDS_ENABLED = false;
            }
        }),
        mdspassword("mdspassword", "Specify the Self MDS password", (arg, configurer) -> {
            GeneralParams.MDS_PASSWORD = arg.trim();
        }),
        mdsinit("mdsinit", "Specify a folder of MiniDAPPs", (arg, configurer) -> {
        	//Get that folder
    		File initFolder 	= new File(arg);
    		initFolder.mkdirs();
    		
        	GeneralParams.MDS_INITFOLDER= initFolder.getAbsolutePath();
        }),
        mdswrite("mdswrite", "Make an initial MiniDAPP WRITE access", (arg, configurer) -> {
        	GeneralParams.MDS_WRITE= arg;
        }),
        conf("conf", "Specify a configuration file (absolute)", (args, configurer) -> {
            // do nothing
        }),
        daemon("daemon", "Run in daemon mode with no stdin input ( services )", (args, configurer) -> {
            if ("true".equals(args)) {
                configurer.daemon = true;
            }
        }),
        isclient("isclient", "Tells the P2P System that this node can't accept incoming connections", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.IS_ACCEPTING_IN_LINKS = false;
            }
        }),
        desktop("desktop", "Use Desktop settings - this node can't accept incoming connections", (args, configurer) -> {
            if ("true".equals(args)) {
            	GeneralParams.IS_DESKTOP 			= true;
            	GeneralParams.IS_ACCEPTING_IN_LINKS = false;
            }
        }),
        server("server", "Use Server settings - this node can accept incoming connections", (args, configurer) -> {
        	if ("true".equals(args)) {
                GeneralParams.IS_ACCEPTING_IN_LINKS = true;
            }
        }),
        mobile("mobile", "Sets this device to a mobile device - used for metrics only", (args, configurer) -> {
            if ("true".equals(args)) {
            	GeneralParams.IS_ACCEPTING_IN_LINKS = false;
                GeneralParams.IS_MOBILE 			= true;
            }
        }),
        jnlp("jnlp", "Are we running from the JNLP", (args, configurer) -> {
            if ("true".equals(args)) {
            	GeneralParams.IS_JNLP = true;
            }
        }),
        showparams("showparams", "Show startup params on launch", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.SHOW_PARAMS = true;
            }
        }),
        nop2p("nop2p", "Disable the automatic P2P system", (args, configurer) -> {
        	if ("true".equals(args)) {
        		GeneralParams.P2P_ENABLED = false;
            }
        }),
        noshutdownhook("noshutdownhook", "Do not use the shutdown hook (Android)", (args, configurer) -> {
        	configurer.mShutdownhook = false;
        }),
        noconnect("noconnect", "Stops the P2P system from connecting to other nodes until it's been connected to", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.NOCONNECT = true;
            }
        }),
        p2prootnode("p2prootnode", "Specify the initial P2P host:port to connect to", (args, configurer) -> {
            GeneralParams.P2P_ROOTNODE = args;
        }),
        p2pnodes("p2pnodes", "Specify a list of nodes (or an URL to a file with the list) to use IF your peers list is empty", (args, configurer) -> {
            
        	//Is it an URL
        	if(args.startsWith("http")) {
        		
        		//Load the list..
        		try {
					GeneralParams.P2P_ADDNODES = RPCClient.sendGET(args);
				} catch (IOException e) {
					SelfLogger.log("Error trying -p2pnodes URL:"+args+" "+e.toString());
				}
				
        	}else {
        		GeneralParams.P2P_ADDNODES = args;
        	}
        	
        }),
        p2ploglevelinfo("p2p-log-level-info", "Set the P2P log level to info", (args, configurer) -> {
            P2PParams.LOG_LEVEL = P2PFunctions.Level.INFO;
        }),
        p2plogleveldebug("p2p-log-level-debug", "Set the P2P log level to debug", (args, configurer) -> {
            P2PParams.LOG_LEVEL = P2PFunctions.Level.DEBUG;
        }),
        p2p2("p2p2", "Enable the new P2P2 system", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.P2P2_ENABLED = true;
                GeneralParams.P2P_ENABLED  = false;
            }
        }),
        connect("connect", "Disable the p2p and manually connect to this list of host:port", (args, configurer) -> {
            GeneralParams.P2P_ENABLED = false;
            GeneralParams.CONNECT_LIST = args;
        }),
        clean("clean", "Wipe data folder at startup", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.CLEAN = true;
            }
        }),
        nodefaultminidapps("nodefaultminidapps", "Do NOT install the default MiniDAPPs", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.DEFAULT_MINIDAPPS = false;
            }
        }),
        nosyncibd("nosyncibd", "Do not sync IBD (for testing)", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.NO_SYNC_IBD = true;
            }
        }),
        syncibdlogs("syncibdlogs", "Show detailed SYNC_IBD logs", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.IBDSYNC_LOGS = true;
            }
        }),
        megammr("megammr", "Are we running in MEGA MMR mode", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.IS_MEGAMMR = true;
            }
        }),
        notifyalltxpow("notifyalltxpow", "Send notification messages for ALL TxPoW (not just relevant)", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.NOTIFY_ALL_TXPOW = true;
            }
        }),
        slavenode("slavenode", "Connect to this node only and only accept TxBlock messages.", (args, configurer) -> {
        	GeneralParams.CONNECT_LIST 			= args;
        	GeneralParams.P2P_ENABLED 			= false;
            GeneralParams.TXBLOCK_NODE 			= true;
            GeneralParams.NO_SYNC_IBD 			= true;
            GeneralParams.IS_ACCEPTING_IN_LINKS = false;
        }),
        limitbandwidth("limitbandwidth", "Limit the amount sent for archive sync", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.ARCHIVESYNC_LIMIT_BANDWIDTH = true;
            }
        }),
        genesis("genesis", "Create a genesis block, -clean and -automine", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.CLEAN = true;
                GeneralParams.GENESIS = true;
            }
        }),
        test("test", "Use test params on a private network", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.TEST_PARAMS 		= true;
                TestParams.setTestParams();
            }
        }),
        solo("solo", "Run a solo/private network (-test -nop2p) and will run -genesis ONLY the first time", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.PRIVATE 		= true;
                GeneralParams.TEST_PARAMS 	= true;
                TestParams.setTestParams();
                GeneralParams.P2P_ENABLED 	= false;
            }
        }),
        testchainlength("testchainlength", "Specify length of tree to keep in -test mode (default is 32)", (arg, configurer) -> {
            TestParams.SELF_CASCADE_START_DEPTH 	= new MiniNumber(arg.trim());
            if(GeneralParams.TEST_PARAMS) {
            	TestParams.setTestParams();
            }
        }),
        nosslmds("nosslmds", "Disable the self signed SSL cert for MDS. Use STunnel or your own SSL proxy instead.", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.MDS_NOSSL = true;
            }
        }),
        mysqldb("mysqldb", "Set the full MySQL DB details as username:password@host:port", (args, configurer) -> {
        	GeneralParams.MYSQL_DB_DETAILS = args;
        }),
        mysqldbcoins("mysqldbcoins", "Enable the MySQL coins db backup from CLI", (args, configurer) -> {
        	if ("true".equals(args)) {
                GeneralParams.MYSQL_DB_COINS = true;
            }
        }),
        mysqldbdelay("mysqldbdelay", "When running in Docker.. Delay in milli-seconds before attempting first MySQL Connection", (args, configurer) -> {
        	GeneralParams.MYSQL_DB_DELAY = Integer.parseInt(args);
        }),
        mysqlalltxpow("mysqlalltxpow", "Store all TxPoW in MySQL when autobackup enabled.", (args, configurer) -> {
            if ("true".equals(args)) {
                GeneralParams.MYSQL_STORE_ALLTXPOW = true;
            }
        }),
        txpowdbstore("txpowdbstore", "How many days to store TxPoW in the internal H2 Database (default 3)", (args, configurer) -> {
        	GeneralParams.NUMBER_DAYS_SQLTXPOWDB = Long.parseLong(args.trim());
        	if(GeneralParams.NUMBER_DAYS_SQLTXPOWDB<3) {
        		GeneralParams.NUMBER_DAYS_SQLTXPOWDB=3;
        		SelfLogger.log("Invalid txpowdbstore.. MUST be >= 3.. setting to 3");
        	}
        }),
        publicmds("publicmds", "Enable the Public MDS system from the CLI", (arg, configurer) -> {
        	GeneralParams.PUBLICMDS_ENABLE = true;	
        }),
        publicmdsuid("publicmdsuid", "Set your own Session ID for the Public MDS and enable Public MDS", (arg, configurer) -> {
        	GeneralParams.PUBLICMDS_ENABLE 		= true;
        	GeneralParams.PUBLICMDS_SESSION_UID = arg.trim();	
        }),
        rescuenode("rescuenode", "If you connect to a heavier chain use this MegaMMR node to resync", (arg, configurer) -> {
        	GeneralParams.RESCUE_MEGAMMR_NODE 	= arg.trim();	
        }),
        help("help", "Print this help", (args, configurer) -> {
            System.out.println("Self Help");
            stream(values())
                    .forEach(pk -> System.out.format("%-20s%-15s%n", new Object[] {"-" + pk.key,pk.helpMsg}));
            System.exit(1);
        }),
    	seed("seed", "Use this seed phrase if starting a new node", (args, configurer) -> {
            GeneralParams.SEED_PHRASE = args;
        }),
    	megaprune("megaprune", "Prune unspendable addresses from the megammr", (args, configurer) -> {
            if ("true".equals(args)) {
            	GeneralParams.MEGAMMR_MEGAPRUNE = true;
            }
        }),
    	megaprunestate("megaprunestate", "Prune all coins with a state (useful for exchanges)", (args, configurer) -> {
            if ("true".equals(args)) {
            	GeneralParams.MEGAMMR_MEGAPRUNE 		= true;
            	GeneralParams.MEGAMMR_MEGAPRUNE_STATE 	= true;
            }
        }),
    	megaprunetokens("megaprunetokens", "Prune all tokens - only keep Self coins", (args, configurer) -> {
            if ("true".equals(args)) {
            	GeneralParams.MEGAMMR_MEGAPRUNE 		= true;
            	GeneralParams.MEGAMMR_MEGAPRUNE_TOKENS  = true;
            }
        });
        
        private final String key;
        private String helpMsg;
        private final BiConsumer<String, ParamConfigurer> consumer;

        ParamKeys(String key, String helpMsg, BiConsumer<String, ParamConfigurer> consumer) {
            this.key = key;
            this.helpMsg = helpMsg;
            this.consumer = consumer;
        }

        static Optional<ParamKeys> toParamKey(String str) {
           return stream(ParamKeys.values())
                   .filter(pk -> pk.key.equals(str))
                   .findFirst();
        }
    }

    public static class UnknownArgumentException extends RuntimeException {
        public UnknownArgumentException(String arg) {
            super("Unknown argument : " + arg);
        }
    }

    static class Pair<L, R> {
        L left;
        R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }
    }
}