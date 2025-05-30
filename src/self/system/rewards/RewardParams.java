package org.self.system.rewards;

import org.self.system.params.SELFParams;

public class RewardParams {
    // Dashboard Configuration
    public static final SELFParams SELF_REWARD_DASHBOARD_REFRESH = 
        new SELFParams("REWARD_DASHBOARD_REFRESH", "Dashboard refresh period in milliseconds", "60000");
    
    public static final SELFParams SELF_REWARD_DASHBOARD_THRESHOLD_VALIDATOR_DISTRIBUTION = 
        new SELFParams("REWARD_DASHBOARD_THRESHOLD_VALIDATOR_DISTRIBUTION", 
            "Validator distribution threshold percentage", "0.8");
    
    public static final SELFParams SELF_REWARD_DASHBOARD_THRESHOLD_USER_DISTRIBUTION = 
        new SELFParams("REWARD_DASHBOARD_THRESHOLD_USER_DISTRIBUTION", 
            "User distribution threshold percentage", "0.7");
    
    public static final SELFParams SELF_REWARD_DASHBOARD_THRESHOLD_VALIDATION_RATE = 
        new SELFParams("REWARD_DASHBOARD_THRESHOLD_VALIDATION_RATE", 
            "Validation rate threshold percentage", "0.9");
    
    // Performance Metrics
    public static final SELFParams SELF_REWARD_THRESHOLD_VALIDATOR_PERFORMANCE = 
        new SELFParams("REWARD_THRESHOLD_VALIDATOR_PERFORMANCE", 
            "Validator performance threshold percentage", "0.85");
    
    public static final SELFParams SELF_REWARD_THRESHOLD_USER_PERFORMANCE = 
        new SELFParams("REWARD_THRESHOLD_USER_PERFORMANCE", 
            "User performance threshold percentage", "0.8");
    
    // Participation Configuration
    public static final SELFParams PARTICIPATION_WINDOW = 
        new SELFParams("PARTICIPATION_WINDOW", "Participation tracking window in milliseconds", "86400000");
    
    public static final SELFParams PARTICIPATION_CHECK_INTERVAL = 
        new SELFParams("PARTICIPATION_CHECK_INTERVAL", "Participation check interval in milliseconds", "3600000");
    
    public static final SELFParams PARTICIPATION_ACTIVITY_INTERVAL = 
        new SELFParams("PARTICIPATION_ACTIVITY_INTERVAL", "Activity interval for participation calculation", "300000");
    
    public static final SELFParams PARTICIPATION_REPUTATION_MULTIPLIER = 
        new SELFParams("PARTICIPATION_REPUTATION_MULTIPLIER", "Reputation multiplier for participation rate", "0.1");
    
    // Distribution Configuration
    public static final SELFParams REWARD_DISTRIBUTION_MIN_VALIDATOR_STAKE = 
        new SELFParams("REWARD_DISTRIBUTION_MIN_VALIDATOR_STAKE", 
            "Minimum stake required for validator rewards", "1000000");
    
    public static final SELFParams REWARD_DISTRIBUTION_MIN_USER_STAKE = 
        new SELFParams("REWARD_DISTRIBUTION_MIN_USER_STAKE", 
            "Minimum stake required for user rewards", "100000");
    
    // Visualization Configuration
    public static final SELFParams DASHBOARD_CHART_WIDTH = 
        new SELFParams("DASHBOARD_CHART_WIDTH", "Width of dashboard charts in pixels", "800");
    
    public static final SELFParams DASHBOARD_CHART_HEIGHT = 
        new SELFParams("DASHBOARD_CHART_HEIGHT", "Height of dashboard charts in pixels", "400");
    
    private RewardParams() {
        // Private constructor to prevent instantiation
    }
}
