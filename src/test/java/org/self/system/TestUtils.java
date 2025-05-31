package org.self.system;

import org.self.utils.HashUtils;

public class TestUtils {
    // Test constants
    public static final String TEST_ADDRESS = "test_address_123";
    public static final String TEST_PRIVATE_KEY = "test_private_key_123";
    public static final String TEST_PUBLIC_KEY = "test_public_key_123";
    public static final long TEST_AMOUNT = 1000L;
    public static final String TEST_MESSAGE = "test_message_123";

    // Test data generation
    public static String generateTestHash() {
        return HashUtils.sha256(TEST_MESSAGE);
    }

    public static byte[] generateTestSignature() {
        // TODO: Implement test signature generation
        return new byte[]{0x01, 0x02, 0x03};
    }

    public static String generateTestBlockHash() {
        return HashUtils.sha256(TEST_ADDRESS + TEST_AMOUNT);
    }
}
