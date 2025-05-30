package org.self.system.params;

import org.self.objects.MiniNumber;

public class SELFParams {
    // SELF Coin parameters
    public static final MiniNumber REWARD_BASE = new MiniNumber(1000);
    public static final MiniNumber RESOURCE_WEIGHT = new MiniNumber(0.3);
    public static final MiniNumber UPTIME_WEIGHT = new MiniNumber(0.4);
    public static final MiniNumber REPUTATION_WEIGHT = new MiniNumber(0.3);
    
    // Upgrade parameters
    public static final MiniNumber UPGRADE_VOTE_THRESHOLD = new MiniNumber(51); // 51% majority
    public static final MiniNumber UPGRADE_GRACE_PERIOD = new MiniNumber(7776000); // 90 days in seconds
    
    // Cloud node parameters
    public static final MiniNumber MIN_RESOURCE_ALLOCATION = new MiniNumber(100); // Minimum resource units
    public static final MiniNumber MAX_RESOURCE_ALLOCATION = new MiniNumber(10000); // Maximum resource units
    
    // Reputation parameters
    public static final MiniNumber MIN_REPUTATION = new MiniNumber(0);
    public static final MiniNumber MAX_REPUTATION = new MiniNumber(100);
    
    // SELF-specific parameters
    public static final MiniNumber SELF_PORT = new MiniNumber(9001);
    public static final String SELF_BASE_VERSION = "SELF_1.0.0";
    public static final MiniNumber SELF_MIN_REWARD = new MiniNumber(10);
    public static final MiniNumber SELF_MAX_REWARD = new MiniNumber(10000);
    public static final MiniNumber SELF_MIN_UPTIME = new MiniNumber(86400); // 24 hours
    public static final MiniNumber SELF_REWARD_PERIOD = new MiniNumber(86400); // 24 hours
    
    // Reward monitoring parameters
    public static final MiniNumber SELF_REWARD_MONITOR_PERIOD = new MiniNumber(3600); // 1 hour
    public static final MiniNumber SELF_REWARD_DASHBOARD_REFRESH = new MiniNumber(300); // 5 minutes
    
    // Trend tracking parameters
    public static final MiniNumber SELF_REWARD_TREND_TRACKING_PERIOD = new MiniNumber(3600); // 1 hour
    public static final MiniNumber SELF_REWARD_TREND_MAX_POINTS = new MiniNumber(72); // 3 days of hourly data
    
    // Visualization parameters
    public static final MiniNumber SELF_REWARD_VISUALIZATION_REFRESH = new MiniNumber(60); // 1 minute
    
    // Validator reward thresholds
    public static final double VALIDATOR_REWARD_THRESHOLD_LOW = 0.01;
    public static final double VALIDATOR_REWARD_THRESHOLD_HIGH = 10000.0;
    
    // User reward thresholds
    public static final double USER_REWARD_THRESHOLD_LOW = 0.01;
    public static final double USER_REWARD_THRESHOLD_HIGH = 1000.0;
    
    // Performance thresholds
    public static final double PERFORMANCE_THRESHOLD_LOW = 0.0;
    public static final double PERFORMANCE_THRESHOLD_HIGH = 100.0;
    
    // Validation thresholds
    public static final double VALIDATION_RATE_THRESHOLD_LOW = 0.9;
    public static final double VALIDATION_RATE_THRESHOLD_HIGH = 1.0;
    
    // Governance parameters
    public static final MiniNumber MIN_STAKE_FOR_PROPOSAL = new MiniNumber(1000); // Minimum SELF required to create proposal
    public static final MiniNumber VOTE_GRACE_PERIOD = new MiniNumber(604800); // 7 days in seconds
    public static final MiniNumber PROPOSAL_EXPIRATION = new MiniNumber(1209600); // 14 days in seconds
    public static final MiniNumber MIN_VOTE_THRESHOLD = new MiniNumber(51); // 51% minimum vote threshold
    public static final MiniNumber MAX_PROPOSALS_PER_NODE = new MiniNumber(5); // Maximum concurrent proposals per node
    public static final MiniNumber PROPOSAL_FEE = new MiniNumber(100); // SELF required to create proposal
    public static final MiniNumber STAKE_LOCK_PERIOD = new MiniNumber(2592000); // 30 days in seconds
    public static final MiniNumber MIN_STAKE_FOR_VOTE = new MiniNumber(100); // Minimum SELF required to vote
    
    private SELFParams() {
        // Private constructor to prevent instantiation
    }
}
