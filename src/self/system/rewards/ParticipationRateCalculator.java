/**
 * Calculates user participation rates based on their activity history and reputation.
 * Uses a sliding window approach to track user activities and applies reputation-based modifiers.
 * 
 * @author SELFHQ Development Team
 * @version 1.0
 */
package org.self.system.rewards;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.self.objects.ai.AIData;
import org.self.objects.ai.AICapacityNumber;
import org.self.system.governance.points.PointSystem;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

/**
 * Singleton class for calculating user participation rates.
 * Tracks user activities over time and calculates participation rates with reputation modifiers.
 */
public class ParticipationRateCalculator {
    private static ParticipationRateCalculator instance;
    private Map<AIData, ParticipationStats> participationStats;
    private long participationWindow;
    private long participationCheckInterval;
    private Timer participationTimer;
    
    private ParticipationRateCalculator() {
        participationStats = new HashMap<>();
        participationWindow = SELFParams.PARTICIPATION_WINDOW.toLong();
        participationCheckInterval = SELFParams.PARTICIPATION_CHECK_INTERVAL.toLong();
        initializeParticipationTracking();
    }
    
    public static ParticipationRateCalculator getInstance() {
        if (instance == null) {
            instance = new ParticipationRateCalculator();
        }
        return instance;
    }
    
    private void initializeParticipationTracking() {
        participationTimer = new Timer();
        participationTimer.schedule(new ParticipationTask(), 0, participationCheckInterval);
    }
    
    public AICapacityNumber calculateParticipationRate(AIData zUserID) {
        ParticipationStats stats = participationStats.getOrDefault(zUserID, new ParticipationStats());
        
        // Calculate participation rate
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - participationWindow;
        
        // Filter activities within the window
        AICapacityNumber totalActivities = stats.filterActivities(windowStart);
        
        // Calculate rate based on activity frequency
        double rate = totalActivities.getAsDouble() / 
                     (participationWindow / SELFParams.PARTICIPATION_ACTIVITY_INTERVAL.toLong());
        
        // Apply rate modifiers
        double modifiedRate = applyRateModifiers(zUserID, rate);
        
        return new AICapacityNumber(modifiedRate);
    }
    
    private double applyRateModifiers(AIData zUserID, double baseRate) {
        // Get user's points for reputation bonus
        PointSystem pointSystem = PointSystem.getInstance();
        AICapacityNumber points = pointSystem.getUserPoints(zUserID);
        
        // Calculate reputation bonus
        double reputationBonus = points.getAsDouble() * SELFParams.PARTICIPATION_REPUTATION_MULTIPLIER.toDouble();
        
        // Calculate final rate with modifiers
        return Math.min(1.0, baseRate * (1 + reputationBonus));
    }
    
    public void recordActivity(AIData zUserID) {
        ParticipationStats stats = participationStats.computeIfAbsent(zUserID, k -> new ParticipationStats());
        stats.addActivity(System.currentTimeMillis());
    }
    
    private class ParticipationTask extends TimerTask {
        @Override
        public void run() {
            try {
                updateParticipationRates();
            } catch (Exception e) {
                SelfLogger.log("Error updating participation rates: " + e.getMessage());
            }
        }
    }
    
    private void updateParticipationRates() {
        // Update all user participation rates
        for (Map.Entry<MiniData, ParticipationStats> entry : participationStats.entrySet()) {
            MiniData userID = entry.getKey();
            ParticipationStats stats = entry.getValue();
            
            // Calculate new rate
            MiniNumber newRate = calculateParticipationRate(userID);
            
            // Update user's participation stats
            stats.setCurrentRate(newRate);
        }
    }
    
    public void resetParticipationStats() {
        participationStats.clear();
    }
    
    private static class ParticipationStats {
        private final Map<Long, MiniNumber> activityTimes;
        private MiniNumber currentRate;
        
        public ParticipationStats() {
            activityTimes = new HashMap<>();
            currentRate = MiniNumber.ZERO;
        }
        
        public void addActivity(long zTime) {
            activityTimes.put(zTime, MiniNumber.ONE);
        }
        
        public MiniNumber filterActivities(long zWindowStart) {
            MiniNumber total = MiniNumber.ZERO;
            
            // Remove old activities
            activityTimes.keySet().removeIf(time -> time < zWindowStart);
            
            // Sum remaining activities
            for (MiniNumber count : activityTimes.values()) {
                total = total.add(count);
            }
            
            return total;
        }
        
        public void setCurrentRate(MiniNumber zRate) {
            currentRate = zRate;
        }
        
        public MiniNumber getCurrentRate() {
            return currentRate;
        }
    }
}
