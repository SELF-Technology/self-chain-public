package org.self.system.network.p2p.params;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import org.self.system.network.p2p.P2PFunctions;

public class P2PTestParams {

    /**
     * P2P Log level
     */
    public static P2PFunctions.Level LOG_LEVEL = P2PFunctions.Level.DEBUG;

    /**
     * Desired number of in link and out links to maintain
     */
    public static int TGT_NUM_LINKS = 5;

    /**
     * Desired number of client (nodes that can't accept inbound connections) to maintain
     */
    public static int TGT_NUM_NONE_P2P_LINKS = 2;

    /**
     * Desired number of connections clients should maintain
     * This must be less than TGT_NUM_LINKS or it will break the p2p logic
     */
    public static int MIN_NUM_CONNECTIONS = 2;

    /**
     * Time between P2P system assessing its state in milliseconds
     */
    public static int LOOP_DELAY = 5_000;

    /**
     * Time between updating the device hash_rate in milliseconds
     */
    //                                         S    millis
    public static int HASH_RATE_UPDATE_DELAY = 60 * 1000;

    /**
     * Max additional ms to add to loop delay (mostly useful during testing to ensure all nodes
     * aren't perfectly in sync)
     */
    public static int LOOP_DELAY_VARIABILITY = 3_000;

    /**
     * Time between P2P system assessing if it can receive inbound connections milliseconds
     */
    public static int NODE_NOT_ACCEPTING_CHECK_DELAY = 60_000;


    public static int SAVE_DATA_DELAY = 60_000;

    /**
     * Time in ms before walk link messages expire
     */
    public static int WALK_LINKS_EXPIRE_TIME = 5_000;

    /**
     * Time before auth key to expire - required to accept DoSwap and walk based connections
     */
    public static int AUTH_KEY_EXPIRY = 300_000;

    public static int METRICS_DELAY = 30_000;

    public static String METRICS_URL = "http://metrics:5000/network";

    public static List<InetSocketAddress> DEFAULT_NODE_LIST = Arrays.asList(
            new InetSocketAddress("self_one", 9001)
    );

    public static void setTestParams() {
        P2PParams.DEFAULT_NODE_LIST = DEFAULT_NODE_LIST;
        P2PParams.LOOP_DELAY = LOOP_DELAY;
        P2PParams.LOOP_DELAY_VARIABILITY = LOOP_DELAY_VARIABILITY;
        P2PParams.NODE_NOT_ACCEPTING_CHECK_DELAY = NODE_NOT_ACCEPTING_CHECK_DELAY;
        P2PParams.WALK_LINKS_EXPIRE_TIME = WALK_LINKS_EXPIRE_TIME;
        P2PParams.AUTH_KEY_EXPIRY = AUTH_KEY_EXPIRY;
        P2PParams.TGT_NUM_LINKS = TGT_NUM_LINKS;
        P2PParams.TGT_NUM_NONE_P2P_LINKS = TGT_NUM_NONE_P2P_LINKS;
        P2PParams.MIN_NUM_CONNECTIONS = MIN_NUM_CONNECTIONS;
        P2PParams.METRICS_DELAY = METRICS_DELAY;
        P2PParams.SAVE_DATA_DELAY = SAVE_DATA_DELAY;
        P2PParams.HASH_RATE_UPDATE_DELAY = HASH_RATE_UPDATE_DELAY;
        P2PParams.LOG_LEVEL = LOG_LEVEL;
    }


}
