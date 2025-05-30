package org.self.system.rewards;

public class TrendDataPoint {
    private double value;
    private long timestamp;
    
    public TrendDataPoint(double zValue) {
        this(zValue, System.currentTimeMillis());
    }
    
    public TrendDataPoint(double zValue, long zTimestamp) {
        value = zValue;
        timestamp = zTimestamp;
    }
    
    public double getValue() {
        return value;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setValue(double zValue) {
        value = zValue;
    }
    
    public void setTimestamp(long zTimestamp) {
        timestamp = zTimestamp;
    }
    
    @Override
    public String toString() {
        return String.format(
            "TrendDataPoint[value=%.2f, timestamp=%d]",
            value,
            timestamp
        );
    }
}
