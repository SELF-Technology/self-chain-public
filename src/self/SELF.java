package org.self;

import org.self.objects.base.MiniString;
import org.self.system.SELFSystem;
import org.self.system.commands.CommandRunner;
import org.self.system.params.GeneralParams;
import org.self.system.params.GlobalParams;
import org.self.system.params.ParamConfigurer;
import org.self.system.params.SELFParams;
import org.self.utils.MiniFormat;
import org.self.utils.SelfLogger;
import org.self.utils.SelfUncaughtException;
import org.self.utils.json.JSONArray;
import org.self.utils.json.JSONObject;
import org.self.objects.base.SELFData;
import org.self.objects.base.SELFNumber;
import org.self.database.nillion.NillionStorage;
import org.self.database.poai.ValidatorAIAnalyzer;

public class SELF {
    public SELF() {}

    public static String runSELF_CMD(String zInput) {
        return runSELF_CMD(zInput, true);
    }

    public static String runSELF_CMD(String zInput, boolean zPrettyJSON) {
        try {
            // Get the Command Runner..
            CommandRunner runner = SELFSystem.getInstance().getCommandRunner();
            
            // Run the command..
            String result = runner.runCommand(zInput);
            
            // Pretty Print if required..
            if (zPrettyJSON) {
                try {
                    // Try to format it as JSON
                    result = MiniFormat.formatJSON(result);
                } catch (Exception ex) {
                    // Don't do anything..
                }
            }
            
            return result;
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    public static void main(String[] args) {
        try {
            // Set the default uncaught exception handler
            Thread.setDefaultUncaughtExceptionHandler(new SelfUncaughtException());
            
            // Check if we have any arguments
            if (args.length > 0) {
                // Get the first argument
                String arg = args[0];
                
                // If it's a number, set the port
                try {
                    int port = Integer.parseInt(arg);
                    GeneralParams.SELF_PORT = port;
                } catch (Exception ex) {
                    // Don't do anything..
                }
            }
            
            // Get the data folder
            File dataFolder = new File(System.getProperty("user.home"),".self");
            
            // Get the SELF folder
            File selffolder = new File(dataFolder,GlobalParams.SELF_BASE_VERSION);
            
            // Set the Data folder
            GeneralParams.DATA_FOLDER = selffolder.getAbsolutePath();
            
            // Set the Ports
            GeneralParams.MDSFILE_PORT = GeneralParams.SELF_PORT + 2;
            GeneralParams.MDSCOMMAND_PORT = GeneralParams.SELF_PORT + 3;
            GeneralParams.RPC_PORT = GeneralParams.SELF_PORT + 4;
            
            // Create the Main
            SELFSystem main = new SELFSystem();
            
            // Set the Main
            SELFSystem.setMain(main);
            
            // Set the Data Folder
            main.setDataFolder(dataFolder);
            
            // Set the SELF folder
            main.setSELFFolder(selffolder);
            
            // Create the SELF folder
            main.createSELFFolder();
            
            // Set the Ports
            main.setPorts();
            
            // Set the Command Runner
            main.setCommandRunner(CommandRunner.getRunner());
            
            // Set the Parameters
            ParamConfigurer.setParameters();
            
            // Initialize validator with PoAI
            try {
                // Get validator seed from system parameters
                SELFData validatorSeed = SELFParams.CURRENT_VALIDATOR_SEED;
                
                // Initialize Nillion storage
                NillionStorage storage = new NillionStorage(validatorSeed);
                
                // Initialize validator analyzer
                ValidatorAIAnalyzer analyzer = new ValidatorAIAnalyzer(storage);
                
                // Store the analyzer in the system
                SELFSystem.getInstance().setValidatorAnalyzer(analyzer);
                
                // Initialize validator reputation
                SELFData validatorId = storage.getValidatorID();
                SELFNumber reputation = analyzer.getAIReputationScore(validatorId);
                
                // Log initialization
                SelfLogger.log("Validator initialized with AI reputation: " + reputation.toString());
                
            } catch (Exception e) {
                SelfLogger.log("Error initializing validator with PoAI: " + e.getMessage());
            }
            
            // Load the Parameters
            main.loadParameters();
            
            // Start the Main
            main.start();
            
            // Start the Command Runner
            main.startCommandRunner();
            
            // Get the Command Runner
            CommandRunner runner = main.getCommandRunner();
            
            // Get the Input Stream
            InputStreamReader inputStream = new InputStreamReader(System.in, MiniString.SELF_CHARSET);
            
            // Create the buffer
            char[] buffer = new char[1024];
            
            // Create the String Buffer
            StringBuffer strbuf = new StringBuffer();
            
            // Create the Shutdown Hook
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    SelfLogger.log("[!] Shutdown Hook..");
                    
                    // Stop the Main
                    main.stop();
                    
                    // Stop the Command Runner
                    main.stopCommandRunner();
                }
            });
            
            // Daemon mode
            if (args.length > 0) {
                SelfLogger.log("Daemon mode started..");
                
                // Wait for the Main to stop
                main.waitForStop();
            } else {
                // Create the Shutdown Hook
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        SelfLogger.log("[!] Shutdown Hook..");
                        
                        // Stop the Main
                        main.stop();
                        
                        // Stop the Command Runner
                        main.stopCommandRunner();
                    }
                });
                
                // Log the ASCII art
                SelfLogger.log("**********************************************");
                SelfLogger.log("*  __  __  ____  _  _  ____  __  __    __    *");
                SelfLogger.log("* (  \\/  )(_  _)( \\( )(_  _)(  \\/  )  /__\\   *");
                SelfLogger.log("*  )    (  _)(_  )  (  _)(_  )    (  /(__)\\  *");
                SelfLogger.log("* (_/\\/\\_)(____)(_)\\_)(____)(_/\\/\\_)(__)(__) *");
                SelfLogger.log("*                                            *");
                SelfLogger.log("*            SELF - A Self-Sovereign Chain   *");
                SelfLogger.log("*                                            *");
                SelfLogger.log("**********************************************");
                SelfLogger.log("Welcome to SELF " + GlobalParams.getFullMicroVersion() + " - for assistance type help. Then press enter.");
                
                // Get the PP size
                long len = Runtime.getRuntime().totalMemory();
                SelfLogger.log("PP allocated : " + MiniFormat.formatSize(len));
                
                // Create input stream reader for command input
                InputStreamReader commandInputStream = new InputStreamReader(System.in, MiniString.SELF_CHARSET);
                
                // Start the Command Runner
                main.startCommandRunner();
                
                // Create the buffer
                char[] inputBuffer = new char[1024];
                
                // Create the String Buffer
                StringBuffer commandBuffer = new StringBuffer();
                
                // Read the input
                while (true) {
                    try {
                        // Read the input
                        int read = commandInputStream.read(inputBuffer);
                        
                        // If we have read something
                        if (read > 0) {
                            // Add to the buffer
                            commandBuffer.append(inputBuffer, 0, read);
                            
                            // Check if we have a newline
                            int newline = strbuf.indexOf("\n");
                            
                            // If we have a newline
                            if (newline > 0) {
                                // Get the command
                                String command = commandBuffer.substring(0, newline);
                                
                                // Clear the buffer
                                commandBuffer.delete(0, newline + 1);
                                
                                // Run the command
                                try {
                                    SelfLogger.log(runSELF_CMD(command));
                                } catch (Exception ex) {
                                    SelfLogger.log("" + ex);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        SelfLogger.log(ex);
                        break;
                    }
                }
                
                // Stop the Command Runner
                main.stopCommandRunner();
                
                // Stop the Main
                main.stop();
                
                // Log the stop
                SelfLogger.log("SELF CLI input stopped..", false);
            }
        } catch (Exception ex) {
            SelfLogger.log(ex);
        }
    }
}
