package org.self.objects.ai;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Represents AI capacity metrics in the Proof-of-AI system.
 * This class replaces MiniNumber with AI-capacity focused operations.
 */
public class AICapacityNumber {
    private BigDecimal value;
    
    /**
     * Creates a new AICapacityNumber with zero value.
     */
    public AICapacityNumber() {
        this.value = BigDecimal.ZERO;
    }
    
    /**
     * Creates a new AICapacityNumber with the specified value.
     * @param value The BigDecimal value
     */
    public AICapacityNumber(BigDecimal value) {
        this.value = value;
    }
    
    /**
     * Creates a new AICapacityNumber from a string representation.
     * @param value The string value
     */
    public AICapacityNumber(String value) {
        this.value = new BigDecimal(value);
    }
    
    /**
     * Creates a new AICapacityNumber from a long value.
     * @param value The long value
     */
    public AICapacityNumber(long value) {
        this.value = BigDecimal.valueOf(value);
    }
    
    /**
     * Gets the underlying BigDecimal value.
     * @return The BigDecimal value
     */
    public BigDecimal getValue() {
        return value;
    }
    
    /**
     * Sets the value of this AICapacityNumber.
     * @param value The new BigDecimal value
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }
    
    /**
     * Adds another AICapacityNumber to this one.
     * @param other The other AICapacityNumber to add
     * @return A new AICapacityNumber with the sum
     */
    public AICapacityNumber add(AICapacityNumber other) {
        return new AICapacityNumber(this.value.add(other.value));
    }
    
    /**
     * Subtracts another AICapacityNumber from this one.
     * @param other The other AICapacityNumber to subtract
     * @return A new AICapacityNumber with the difference
     */
    public AICapacityNumber subtract(AICapacityNumber other) {
        return new AICapacityNumber(this.value.subtract(other.value));
    }
    
    /**
     * Multiplies this AICapacityNumber by another.
     * @param other The other AICapacityNumber to multiply by
     * @return A new AICapacityNumber with the product
     */
    public AICapacityNumber multiply(AICapacityNumber other) {
        return new AICapacityNumber(this.value.multiply(other.value));
    }
    
    /**
     * Divides this AICapacityNumber by another.
     * @param other The other AICapacityNumber to divide by
     * @return A new AICapacityNumber with the quotient
     */
    public AICapacityNumber divide(AICapacityNumber other) {
        return new AICapacityNumber(this.value.divide(other.value, RoundingMode.HALF_UP));
    }
    
    /**
     * Checks if this AICapacityNumber is equal to another.
     * @param other The other AICapacityNumber to compare with
     * @return true if equal, false otherwise
     */
    public boolean isEqual(AICapacityNumber other) {
        return this.value.compareTo(other.value) == 0;
    }
    
    /**
     * Checks if this AICapacityNumber is greater than another.
     * @param other The other AICapacityNumber to compare with
     * @return true if greater, false otherwise
     */
    public boolean isMore(AICapacityNumber other) {
        return this.value.compareTo(other.value) > 0;
    }
    
    /**
     * Checks if this AICapacityNumber is less than another.
     * @param other The other AICapacityNumber to compare with
     * @return true if less, false otherwise
     */
    public boolean isLess(AICapacityNumber other) {
        return this.value.compareTo(other.value) < 0;
    }
    
    /**
     * Gets the absolute value of this AICapacityNumber.
     * @return A new AICapacityNumber with the absolute value
     */
    public AICapacityNumber abs() {
        return new AICapacityNumber(this.value.abs());
    }
    
    /**
     * Calculates the square root of this AICapacityNumber.
     * @return A new AICapacityNumber with the square root
     */
    public AICapacityNumber sqrt() {
        return new AICapacityNumber(this.value.sqrt(RoundingMode.HALF_UP));
    }
    
    /**
     * Raises this AICapacityNumber to a power.
     * @param exponent The power to raise to
     * @return A new AICapacityNumber with the result
     */
    public AICapacityNumber pow(int exponent) {
        return new AICapacityNumber(this.value.pow(exponent));
    }
    
    /**
     * Gets the integer value of this AICapacityNumber.
     * @return The integer value
     */
    public int getAsInt() {
        return this.value.intValue();
    }
    
    /**
     * Gets the long value of this AICapacityNumber.
     * @return The long value
     */
    public long getAsLong() {
        return this.value.longValue();
    }
    
    /**
     * Gets the double value of this AICapacityNumber.
     * @return The double value
     */
    public double getAsDouble() {
        return this.value.doubleValue();
    }
    
    /**
     * Converts this AICapacityNumber to a string representation.
     * @return The string representation
     */
    @Override
    public String toString() {
        return value.toPlainString();
    }
}
