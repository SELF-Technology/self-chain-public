package org.self.system.rewards;

import org.self.objects.MiniData;
import org.self.objects.MiniNumber;

public class RewardRecord {
    private MiniData recipientID;
    private MiniNumber amount;
    private String rewardType;
    private long timestamp;
    private MiniNumber reputationBonus;
    private MiniNumber stakeBonus;
    private MiniNumber participationBonus;
    private String description;
    
    public RewardRecord(MiniData zRecipientID, MiniNumber zAmount, String zRewardType, long zTimestamp) {
        recipientID = zRecipientID;
        amount = zAmount;
        rewardType = zRewardType;
        timestamp = zTimestamp;
        reputationBonus = MiniNumber.ZERO;
        stakeBonus = MiniNumber.ZERO;
        participationBonus = MiniNumber.ZERO;
        description = "";
    }
    
    public void setReputationBonus(MiniNumber zBonus) {
        reputationBonus = zBonus;
    }
    
    public void setStakeBonus(MiniNumber zBonus) {
        stakeBonus = zBonus;
    }
    
    public void setParticipationBonus(MiniNumber zBonus) {
        participationBonus = zBonus;
    }
    
    public void setDescription(String zDescription) {
        description = zDescription;
    }
    
    public MiniData getRecipientID() {
        return recipientID;
    }
    
    public MiniNumber getAmount() {
        return amount;
    }
    
    public String getRewardType() {
        return rewardType;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public MiniNumber getReputationBonus() {
        return reputationBonus;
    }
    
    public MiniNumber getStakeBonus() {
        return stakeBonus;
    }
    
    public MiniNumber getParticipationBonus() {
        return participationBonus;
    }
    
    public String getDescription() {
        return description;
    }
    
    public MiniNumber getTotalAmount() {
        return amount.add(reputationBonus).add(stakeBonus).add(participationBonus);
    }
    
    public String toString() {
        return String.format(
            "RewardRecord[recipient=%s, amount=%s, type=%s, timestamp=%d, total=%s]",
            recipientID.toString(),
            amount.toString(),
            rewardType,
            timestamp,
            getTotalAmount().toString()
        );
    }

    public boolean isValid() {
        return recipientID != null && 
               !recipientID.isEmpty() &&
               amount != null &&
               amount.compareTo(MiniNumber.ZERO) >= 0 &&
               rewardType != null &&
               !rewardType.isEmpty() &&
               timestamp > 0;
    }

    public void validate() throws IllegalArgumentException {
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid reward record");
        }
        
        if (amount.compareTo(MiniNumber.ZERO) < 0) {
            throw new IllegalArgumentException("Reward amount cannot be negative");
        }
        
        if (rewardType == null || rewardType.isEmpty()) {
            throw new IllegalArgumentException("Reward type is required");
        }
        
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Invalid timestamp");
        }
    }

    public static boolean isValidRewardType(String zType) {
        return zType != null && 
               !zType.isEmpty() &&
               (zType.equals("validator") ||
                zType.equals("user") ||
                zType.equals("hex") ||
                zType.equals("participation") ||
                zType.equals("stake"));
    }
}
