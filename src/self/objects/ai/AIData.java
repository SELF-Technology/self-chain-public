package org.self.objects.ai;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents AI-specific immutable data in the Proof-of-AI system.
 * This class replaces MiniData with AI-capacity focused operations.
 */
public class AIData {
    private final byte[] data;
    
    /**
     * Creates a new AIData with empty data.
     */
    public AIData() {
        this.data = new byte[0];
    }
    
    /**
     * Creates a new AIData with the specified byte array.
     * @param data The byte array
     */
    public AIData(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }
    
    /**
     * Creates a new AIData from another AIData instance.
     * @param other The other AIData to copy
     */
    public AIData(AIData other) {
        this.data = Arrays.copyOf(other.data, other.data.length);
    }
    
    /**
     * Gets the underlying byte array.
     * @return The byte array
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
    
    /**
     * Gets the length of the data.
     * @return The length
     */
    public int length() {
        return data.length;
    }
    
    /**
     * Checks if this AIData is empty.
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return data.length == 0;
    }
    
    /**
     * Gets a byte at the specified index.
     * @param index The index
     * @return The byte
     */
    public byte getByte(int index) {
        return data[index];
    }
    
    /**
     * Gets a sub-array of bytes.
     * @param from The start index
     * @param to The end index
     * @return The sub-array
     */
    public byte[] getBytes(int from, int to) {
        return Arrays.copyOfRange(data, from, to);
    }
    
    /**
     * Gets the data as a hexadecimal string.
     * @return The hexadecimal string
     */
    public String toHexString() {
        StringBuilder hexString = new StringBuilder();
        for (byte b : data) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
    
    /**
     * Gets the data as a string using UTF-8 encoding.
     * @return The string
     */
    public String toString() {
        return new String(data);
    }
    
    /**
     * Gets the data as a string using the specified encoding.
     * @param encoding The encoding
     * @return The string
     */
    public String toString(String encoding) {
        try {
            return new String(data, encoding);
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Creates AIData from a hexadecimal string.
     * @param hexString The hexadecimal string
     * @return A new AIData instance
     */
    public static AIData fromHexString(String hexString) {
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16);
        }
        return new AIData(bytes);
    }
    
    /**
     * Creates AIData from a string using UTF-8 encoding.
     * @param string The string
     * @return A new AIData instance
     */
    public static AIData fromString(String string) {
        return new AIData(string.getBytes());
    }
    
    /**
     * Creates AIData from a string using the specified encoding.
     * @param string The string
     * @param encoding The encoding
     * @return A new AIData instance
     */
    public static AIData fromString(String string, String encoding) {
        try {
            return new AIData(string.getBytes(encoding));
        } catch (Exception e) {
            return new AIData();
        }
    }
    
    /**
     * Checks if this AIData is equal to another.
     * @param other The other AIData to compare with
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AIData aiData = (AIData) other;
        return Arrays.equals(data, aiData.data);
    }
    
    /**
     * Gets the hash code of this AIData.
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(data));
    }
}
