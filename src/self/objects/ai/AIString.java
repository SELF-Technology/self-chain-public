package org.self.objects.ai;

/**
 * Represents AI-specific string data in the Proof-of-AI system.
 * This class replaces MiniString with AI-capacity focused operations.
 */
public class AIString {
    private final String value;
    
    /**
     * Creates a new AIString with an empty string.
     */
    public AIString() {
        this.value = "";
    }
    
    /**
     * Creates a new AIString with the specified string.
     * @param value The string value
     */
    public AIString(String value) {
        this.value = value;
    }
    
    /**
     * Creates a new AIString from another AIString.
     * @param other The other AIString to copy
     */
    public AIString(AIString other) {
        this.value = other.value;
    }
    
    /**
     * Gets the underlying string value.
     * @return The string value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Gets the length of the string.
     * @return The length
     */
    public int length() {
        return value.length();
    }
    
    /**
     * Checks if this AIString is empty.
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return value.isEmpty();
    }
    
    /**
     * Gets a substring.
     * @param beginIndex The start index
     * @param endIndex The end index
     * @return The substring
     */
    public AIString substring(int beginIndex, int endIndex) {
        return new AIString(value.substring(beginIndex, endIndex));
    }
    
    /**
     * Converts this AIString to uppercase.
     * @return A new AIString in uppercase
     */
    public AIString toUpperCase() {
        return new AIString(value.toUpperCase());
    }
    
    /**
     * Converts this AIString to lowercase.
     * @return A new AIString in lowercase
     */
    public AIString toLowerCase() {
        return new AIString(value.toLowerCase());
    }
    
    /**
     * Checks if this AIString equals another.
     * @param other The other AIString to compare with
     * @return true if equal, false otherwise
     */
    public boolean equals(AIString other) {
        return value.equals(other.value);
    }
    
    /**
     * Checks if this AIString contains another string.
     * @param other The string to search for
     * @return true if contains, false otherwise
     */
    public boolean contains(String other) {
        return value.contains(other);
    }
    
    /**
     * Checks if this AIString starts with another string.
     * @param prefix The prefix to check
     * @return true if starts with, false otherwise
     */
    public boolean startsWith(String prefix) {
        return value.startsWith(prefix);
    }
    
    /**
     * Checks if this AIString ends with another string.
     * @param suffix The suffix to check
     * @return true if ends with, false otherwise
     */
    public boolean endsWith(String suffix) {
        return value.endsWith(suffix);
    }
    
    /**
     * Gets the hash code of this AIString.
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    /**
     * Gets the string representation of this AIString.
     * @return The string representation
     */
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * Creates an AIString from a regular string.
     * @param string The string to convert
     * @return A new AIString instance
     */
    public static AIString fromString(String string) {
        return new AIString(string);
    }
    
    /**
     * Creates an AIString from a hexadecimal string.
     * @param hexString The hexadecimal string
     * @return A new AIString instance
     */
    public static AIString fromHexString(String hexString) {
        return new AIString(hexString);
    }
    
    /**
     * Creates an AIString from bytes using UTF-8 encoding.
     * @param bytes The bytes to convert
     * @return A new AIString instance
     */
    public static AIString fromBytes(byte[] bytes) {
        return new AIString(new String(bytes));
    }
    
    /**
     * Creates an AIString from bytes using the specified encoding.
     * @param bytes The bytes to convert
     * @param encoding The encoding
     * @return A new AIString instance
     */
    public static AIString fromBytes(byte[] bytes, String encoding) {
        try {
            return new AIString(new String(bytes, encoding));
        } catch (Exception e) {
            return new AIString();
        }
    }
}
