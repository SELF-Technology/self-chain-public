package org.self.objects.self;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SELFData {
    private byte[] data;
    
    public static final SELFData EMPTY = new SELFData();
    
    public SELFData() {
        this.data = new byte[0];
    }
    
    public SELFData(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }
    
    public SELFData(String hexString) {
        this.data = hexStringToBytes(hexString);
    }
    
    public SELFData(BigInteger value) {
        this.data = value.toByteArray();
    }
    
    private byte[] hexStringToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                               + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }
    
    public byte[] getData() {
        return Arrays.copyOf(this.data, this.data.length);
    }
    
    public SELFData setData(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
        return this;
    }
    
    public SELFData setData(String hexString) {
        this.data = hexStringToBytes(hexString);
        return this;
    }
    
    public SELFData setData(BigInteger value) {
        this.data = value.toByteArray();
        return this;
    }
    
    public SELFData copy() {
        return new SELFData(this.data);
    }
    
    public SELFData concatenate(SELFData other) {
        byte[] newData = new byte[this.data.length + other.data.length];
        System.arraycopy(this.data, 0, newData, 0, this.data.length);
        System.arraycopy(other.data, 0, newData, this.data.length, other.data.length);
        return new SELFData(newData);
    }
    
    public SELFData subArray(int start, int end) {
        byte[] subData = Arrays.copyOfRange(this.data, start, end);
        return new SELFData(subData);
    }
    
    public SELFData subArray(int start) {
        return subArray(start, this.data.length);
    }
    
    public SELFData subArray(int start, int length) {
        return subArray(start, start + length);
    }
    
    public SELFData padLeft(int length) {
        if (this.data.length >= length) return this;
        byte[] newData = new byte[length];
        System.arraycopy(this.data, 0, newData, length - this.data.length, this.data.length);
        return new SELFData(newData);
    }
    
    public SELFData padRight(int length) {
        if (this.data.length >= length) return this;
        byte[] newData = new byte[length];
        System.arraycopy(this.data, 0, newData, 0, this.data.length);
        return new SELFData(newData);
    }
    
    public SELFData trim() {
        int start = 0;
        while (start < this.data.length && this.data[start] == 0) {
            start++;
        }
        int end = this.data.length;
        while (end > 0 && this.data[end - 1] == 0) {
            end--;
        }
        return subArray(start, end);
    }
    
    public SELFData toHex() {
        StringBuilder hex = new StringBuilder(2 * this.data.length);
        for (byte b : this.data) {
            hex.append(String.format("%02x", b));
        }
        return new SELFData(hex.toString());
    }
    
    public SELFData toBase64() {
        // TODO: Implement base64 encoding
        return this;
    }
    
    public SELFData fromBase64() {
        // TODO: Implement base64 decoding
        return this;
    }
    
    public SELFData toBigInteger() {
        return new SELFData(new BigInteger(1, this.data));
    }
    
    public SELFData fromBigInteger(BigInteger value) {
        return new SELFData(value.toByteArray());
    }
    
    public SELFData toUTF8() {
        return new SELFData(new String(this.data, StandardCharsets.UTF_8));
    }
    
    public SELFData fromUTF8() {
        return new SELFData(this.data);
    }
    
    public SELFData toAscii() {
        return new SELFData(new String(this.data, StandardCharsets.US_ASCII));
    }
    
    public SELFData fromAscii() {
        return new SELFData(this.data);
    }
    
    public SELFData toByteArray() {
        return new SELFData(this.data);
    }
    
    public SELFData fromByteArray(byte[] data) {
        return new SELFData(data);
    }
    
    public SELFData toHexString() {
        StringBuilder hex = new StringBuilder(2 * this.data.length);
        for (byte b : this.data) {
            hex.append(String.format("%02x", b));
        }
        return new SELFData(hex.toString());
    }
    
    public SELFData fromHexString(String hexString) {
        return new SELFData(hexStringToBytes(hexString));
    }
    
    public SELFData toBase64String() {
        // TODO: Implement base64 encoding
        return this;
    }
    
    public SELFData fromBase64String(String base64String) {
        // TODO: Implement base64 decoding
        return this;
    }
    
    public SELFData toUTF8String() {
        return new SELFData(new String(this.data, StandardCharsets.UTF_8));
    }
    
    public SELFData fromUTF8String(String utf8String) {
        return new SELFData(utf8String.getBytes(StandardCharsets.UTF_8));
    }
    
    public SELFData toAsciiString() {
        return new SELFData(new String(this.data, StandardCharsets.US_ASCII));
    }
    
    public SELFData fromAsciiString(String asciiString) {
        return new SELFData(asciiString.getBytes(StandardCharsets.US_ASCII));
    }
    
    public SELFData toByteArrayString() {
        return new SELFData(Arrays.toString(this.data));
    }
    
    public SELFData fromByteArrayString(String byteArrayString) {
        // TODO: Implement string to byte array conversion
        return this;
    }
    
    public SELFData toHexStringString() {
        StringBuilder hex = new StringBuilder(2 * this.data.length);
        for (byte b : this.data) {
            hex.append(String.format("%02x", b));
        }
        return new SELFData(hex.toString());
    }
    
    public SELFData fromHexStringString(String hexString) {
        return new SELFData(hexStringToBytes(hexString));
    }
    
    @Override
    public String toString() {
        return new String(this.data, StandardCharsets.UTF_8);
    }
    
    public static SELFData valueOf(byte[] data) {
        return new SELFData(data);
    }
    
    public static SELFData valueOf(String hexString) {
        return new SELFData(hexStringToBytes(hexString));
    }
    
    public static SELFData valueOf(BigInteger value) {
        return new SELFData(value.toByteArray());
    }
}
