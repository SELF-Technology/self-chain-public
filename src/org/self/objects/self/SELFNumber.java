package org.self.objects.self;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.MathContext;

public class SELFNumber {
    private BigDecimal value;
    
    public static final SELFNumber ZERO = new SELFNumber(0);
    public static final SELFNumber ONE = new SELFNumber(1);
    
    public SELFNumber() {
        this.value = BigDecimal.ZERO;
    }
    
    public SELFNumber(double value) {
        this.value = BigDecimal.valueOf(value);
    }
    
    public SELFNumber(BigDecimal value) {
        this.value = value;
    }
    
    public SELFNumber add(SELFNumber other) {
        return new SELFNumber(this.value.add(other.value));
    }
    
    public SELFNumber subtract(SELFNumber other) {
        return new SELFNumber(this.value.subtract(other.value));
    }
    
    public SELFNumber multiply(SELFNumber other) {
        return new SELFNumber(this.value.multiply(other.value));
    }
    
    public SELFNumber divide(SELFNumber other) {
        return new SELFNumber(this.value.divide(other.value, 8, RoundingMode.HALF_UP));
    }
    
    public SELFNumber divide(BigDecimal divisor) {
        return new SELFNumber(this.value.divide(divisor, 8, RoundingMode.HALF_UP));
    }
    
    public SELFNumber scaleByPowerOfTen(int n) {
        return new SELFNumber(this.value.scaleByPowerOfTen(n));
    }
    
    public SELFNumber setScale(int scale) {
        return new SELFNumber(this.value.setScale(scale, RoundingMode.HALF_UP));
    }
    
    public SELFNumber abs() {
        return new SELFNumber(this.value.abs());
    }
    
    public SELFNumber negate() {
        return new SELFNumber(this.value.negate());
    }
    
    public SELFNumber max(SELFNumber other) {
        return new SELFNumber(this.value.max(other.value));
    }
    
    public SELFNumber min(SELFNumber other) {
        return new SELFNumber(this.value.min(other.value));
    }
    
    public SELFNumber round(int scale) {
        return new SELFNumber(this.value.round(new MathContext(scale, RoundingMode.HALF_UP)));
    }
    
    public SELFNumber movePointLeft(int n) {
        return new SELFNumber(this.value.movePointLeft(n));
    }
    
    public SELFNumber movePointRight(int n) {
        return new SELFNumber(this.value.movePointRight(n));
    }
    
    public SELFNumber stripTrailingZeros() {
        return new SELFNumber(this.value.stripTrailingZeros());
    }
    
    public SELFNumber toPlainString() {
        return new SELFNumber(this.value.toPlainString());
    }
    
    public SELFNumber toBigInteger() {
        return new SELFNumber(this.value.toBigInteger());
    }
    
    public SELFNumber toBigIntegerExact() {
        return new SELFNumber(this.value.toBigIntegerExact());
    }
    
    public SELFNumber toEngineeringString() {
        return new SELFNumber(this.value.toEngineeringString());
    }
    
    public SELFNumber toScientificString() {
        return new SELFNumber(this.value.toScientificString());
    }
    
    public SELFNumber unscaledValue() {
        return new SELFNumber(this.value.unscaledValue());
    }
    
    public SELFNumber precision() {
        return new SELFNumber(this.value.precision());
    }
    
    public SELFNumber scale() {
        return new SELFNumber(this.value.scale());
    }
    
    public SELFNumber signum() {
        return new SELFNumber(this.value.signum());
    }
    
    public SELFNumber ulp() {
        return new SELFNumber(this.value.ulp());
    }
    
    public SELFNumber intValue() {
        return new SELFNumber(this.value.intValue());
    }
    
    public SELFNumber intValueExact() {
        return new SELFNumber(this.value.intValueExact());
    }
    
    public SELFNumber longValue() {
        return new SELFNumber(this.value.longValue());
    }
    
    public SELFNumber longValueExact() {
        return new SELFNumber(this.value.longValueExact());
    }
    
    public SELFNumber floatValue() {
        return new SELFNumber(this.value.floatValue());
    }
    
    public SELFNumber doubleValue() {
        return new SELFNumber(this.value.doubleValue());
    }
    
    public int compareTo(SELFNumber other) {
        return this.value.compareTo(other.value);
    }
    
    public boolean equals(SELFNumber other) {
        return this.value.equals(other.value);
    }
    
    public boolean equals(BigDecimal other) {
        return this.value.equals(other);
    }
    
    public boolean equals(Object other) {
        if (other instanceof SELFNumber) {
            return this.value.equals(((SELFNumber) other).value);
        }
        return false;
    }
    
    public int hashCode() {
        return this.value.hashCode();
    }
    
    @Override
    public String toString() {
        return this.value.toString();
    }
    
    public BigDecimal toBigDecimal() {
        return this.value;
    }
    
    public static SELFNumber valueOf(double value) {
        return new SELFNumber(value);
    }
    
    public static SELFNumber valueOf(BigDecimal value) {
        return new SELFNumber(value);
    }
}
