package org.self.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockchainTest {
    private static final Logger logger = LoggerFactory.getLogger(BlockchainTest.class);
    private static final String GENESIS_BLOCK_HASH = "genesis_block_hash_123";
    private static final long INITIAL_BALANCE = 10000L;

    @BeforeEach
    public void setUp() {
        // Initialize test environment
        logger.info("Starting blockchain tests");
    }

    @Test
    public void testBlockchainInitialization() {
        // Test that blockchain initializes with genesis block
        Blockchain blockchain = new Blockchain();
        
        assertThat(blockchain).isNotNull();
        assertThat(blockchain.getChainSize()).isEqualTo(1);
        assertThat(blockchain.getLatestBlock().getHash()).isEqualTo(GENESIS_BLOCK_HASH);
        assertThat(blockchain.isValid()).isTrue();
    }

    @Test
    public void testTransactionValidation() {
        // Test transaction validation
        Transaction transaction = new Transaction(
            TestUtils.TEST_ADDRESS,
            TestUtils.TEST_ADDRESS,
            TestUtils.TEST_AMOUNT
        );
        
        // Test valid transaction
        boolean isValid = transaction.validate();
        assertThat(isValid).isTrue();
        
        // Test invalid transaction (negative amount)
        transaction.setAmount(-100L);
        isValid = transaction.validate();
        assertThat(isValid).isFalse();
        
        // Test invalid transaction (zero amount)
        transaction.setAmount(0L);
        isValid = transaction.validate();
        assertThat(isValid).isFalse();
    }

    @Test
    public void testBlockCreation() {
        // Test block creation and validation
        Block genesis = new Block(0, TestUtils.generateTestHash(), TestUtils.generateTestBlockHash());
        
        assertThat(genesis).isNotNull();
        assertThat(genesis.getIndex()).isEqualTo(0);
        assertThat(genesis.getPreviousHash()).isEqualTo(TestUtils.generateTestHash());
        assertThat(genesis.getHash()).isEqualTo(TestUtils.generateTestBlockHash());
        
        // Test block validation
        assertThat(genesis.isValid()).isTrue();
        
        // Test invalid block (tampered data)
        genesis.setData("tampered_data");
        assertThat(genesis.isValid()).isFalse();
    }

    @Test
    public void testChainValidation() {
        // Test chain validation
        Blockchain blockchain = new Blockchain();
        
        // Add a valid block
        Block block1 = new Block(1, GENESIS_BLOCK_HASH, TestUtils.generateTestBlockHash());
        blockchain.addBlock(block1);
        
        // Chain should be valid
        assertThat(blockchain.isValid()).isTrue();
        
        // Add an invalid block (wrong previous hash)
        Block invalidBlock = new Block(2, "wrong_previous_hash", TestUtils.generateTestBlockHash());
        blockchain.addBlock(invalidBlock);
        
        // Chain should be invalid
        assertThat(blockchain.isValid()).isFalse();
    }

    @Test
    public void testTransactionProcessing() {
        // Test transaction processing
        Blockchain blockchain = new Blockchain();
        
        // Create a transaction
        Transaction transaction = new Transaction(
            TestUtils.TEST_ADDRESS,
            TestUtils.TEST_ADDRESS,
            TestUtils.TEST_AMOUNT
        );
        
        // Process transaction
        boolean processed = blockchain.processTransaction(transaction);
        assertThat(processed).isTrue();
        
        // Verify balance
        long balance = blockchain.getBalance(TestUtils.TEST_ADDRESS);
        assertThat(balance).isEqualTo(INITIAL_BALANCE - TestUtils.TEST_AMOUNT);
    }
}
