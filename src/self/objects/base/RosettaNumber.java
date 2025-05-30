package org.self.objects.base;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RosettaNumber {
    private BigDecimal value;
    
    public RosettaNumber() {
        this.value = BigDecimal.ZERO;
    }
    
    public RosettaNumber(BigDecimal value) {
        this.value = value;
    }
    
    public RosettaNumber(String value) {
        this.value = new BigDecimal(value);
    }
    
    public RosettaNumber(long value) {
        this.value = BigDecimal.valueOf(value);
    }
    
    public BigDecimal getValue() {
        return value;
    }
    
    public void setValue(BigDecimal value) {
        this.value = value;
    }
    
    public RosettaNumber add(RosettaNumber other) {
        return new RosettaNumber(this.value.add(other.value));
    }
    
    public RosettaNumber subtract(RosettaNumber other) {
        return new RosettaNumber(this.value.subtract(other.value));
    }
    
    public RosettaNumber multiply(RosettaNumber other) {
        return new RosettaNumber(this.value.multiply(other.value));
    }
    
    public RosettaNumber divide(RosettaNumber other) {
        return new RosettaNumber(this.value.divide(other.value, RoundingMode.HALF_UP));
    }
    
    public boolean isEqual(RosettaNumber other) {
        return this.value.compareTo(other.value) == 0;
    }
    
    public boolean isMore(RosettaNumber other) {
        return this.value.compareTo(other.value) > 0;
    }
    
    public boolean isLess(RosettaNumber other) {
        return this.value.compareTo(other.value) < 0;
    }
    
    public RosettaNumber abs() {
        return new RosettaNumber(this.value.abs());
    }
    
    public RosettaNumber sqrt() {
        return new RosettaNumber(this.value.sqrt(RoundingMode.HALF_UP));
    }
    
    public RosettaNumber pow(int exponent) {
        return new RosettaNumber(this.value.pow(exponent));
    }
    
    public int getAsInt() {
        return this.value.intValue();
    }
    
    public long getAsLong() {
        return this.value.longValue();
    }
    
    @Override
    public String toString() {
        return value.toPlainString();
    }
}
