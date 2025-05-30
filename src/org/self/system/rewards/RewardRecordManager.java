package org.self.system.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.self.objects.MiniData;
import org.self.objects.MiniNumber;
import org.self.system.governance.monitor.GovernanceMonitor;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;

public class RewardRecordManager {
    private static RewardRecordManager instance;
    private Map<MiniData, List<RewardRecord>> rewardRecords;
    private GovernanceMonitor governanceMonitor;
    private MiniNumber maxRewardAmount;
    private MiniNumber minRewardAmount;
    
    private RewardRecordManager() {
        rewardRecords = new HashMap<>();
        governanceMonitor = GovernanceMonitor.getInstance();
        maxRewardAmount = SELFParams.VALIDATOR_REWARD_THRESHOLD_HIGH;
        minRewardAmount = SELFParams.VALIDATOR_REWARD_THRESHOLD_LOW;
    }
    
    public static RewardRecordManager getInstance() {
        if (instance == null) {
            instance = new RewardRecordManager();
        }
        return instance;
    }
    
    public synchronized void addRewardRecord(RewardRecord record) {
        try {
            record.validate();
            
            MiniData recipientID = record.getRecipientID();
            
            // Check if recipient exists
            if (!rewardRecords.containsKey(recipientID)) {
                rewardRecords.put(recipientID, new ArrayList<>());
            }
            
            // Add record
            rewardRecords.get(recipientID).add(record);
            
            // Log and notify
            SelfLogger.log(String.format("Added reward record: %s", record.toString()));
            governanceMonitor.updateRewardRecord(record);
            
        } catch (IllegalArgumentException e) {
            SelfLogger.error(String.format("Failed to add reward record: %s", e.getMessage()));
            governanceMonitor.reportError("INVALID_REWARD_RECORD", e.getMessage());
        }
    }
    
    public synchronized List<RewardRecord> getRewardRecords(MiniData zRecipientID) {
        return rewardRecords.getOrDefault(zRecipientID, new ArrayList<>());
    }
    
    public synchronized List<RewardRecord> getRewardRecordsByType(String zType) {
        if (!RewardRecord.isValidRewardType(zType)) {
            return new ArrayList<>();
        }
        
        return rewardRecords.values().stream()
            .flatMap(List::stream)
            .filter(record -> record.getRewardType().equals(zType))
            .collect(Collectors.toList());
    }
    
    public synchronized MiniNumber getTotalRewards(MiniData zRecipientID) {
        List<RewardRecord> records = getRewardRecords(zRecipientID);
        return records.stream()
            .map(RewardRecord::getTotalAmount)
            .reduce(MiniNumber.ZERO, MiniNumber::add);
    }
    
    public synchronized MiniNumber getTotalRewardsByType(String zType) {
        List<RewardRecord> records = getRewardRecordsByType(zType);
        return records.stream()
            .map(RewardRecord::getTotalAmount)
            .reduce(MiniNumber.ZERO, MiniNumber::add);
    }
    
    public synchronized void clearRewardRecords(MiniData zRecipientID) {
        rewardRecords.remove(zRecipientID);
    }
    
    public synchronized void clearAllRewardRecords() {
        rewardRecords.clear();
    }
    
    public synchronized void generateRewardReport() {
        StringBuilder report = new StringBuilder("\n=== REWARD RECORD REPORT ===\n");
        
        // Total rewards by type
        report.append("\nTotal Rewards by Type:\n");
        for (String type : RewardRecord.getValidRewardTypes()) {
            MiniNumber total = getTotalRewardsByType(type);
            report.append(String.format("%s: %s\n", type, total.toString()));
        }
        
        // Top recipients
        report.append("\nTop Recipients:\n");
        rewardRecords.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().stream()
                    .map(RewardRecord::getTotalAmount)
                    .reduce(MiniNumber.ZERO, MiniNumber::add)
                    .compareTo(e1.getValue().stream()
                             .map(RewardRecord::getTotalAmount)
                             .reduce(MiniNumber.ZERO, MiniNumber::add)))
            .limit(5)
            .forEach(entry -> {
                MiniNumber total = entry.getValue().stream()
                    .map(RewardRecord::getTotalAmount)
                    .reduce(MiniNumber.ZERO, MiniNumber::add);
                report.append(String.format("%s: %s\n", entry.getKey().toString(), total.toString()));
            });
        
        SelfLogger.log(report.toString());
        governanceMonitor.updateReport(report.toString());
    }
    
    private static List<String> getValidRewardTypes() {
        return List.of("validator", "user", "hex", "participation", "stake");
    }
}
