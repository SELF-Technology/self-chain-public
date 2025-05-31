# SELF Chain SDK Documentation

Welcome to the SELF Chain SDK documentation. This SDK provides a comprehensive set of tools and libraries to help developers build applications on top of the SELF Chain blockchain.

## SDK Overview

The SELF Chain SDK is available in multiple languages:
- Java (Primary)
- JavaScript
- Python
- Go
- Rust

### Key Features

1. **Blockchain Interaction**
   - Block submission and retrieval
   - Transaction management
   - Smart contract deployment and execution
   - Event monitoring

2. **Cross-chain Integration**
   - ERC20 bridge support
   - Rosetta protocol integration
   - Wire protocol support
   - Multi-chain operations

3. **Development Tools**
   - Local blockchain simulation
   - Testnet deployment
   - Contract testing
   - Debugging tools

## SDK Setup

### Java SDK

1. Add dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>com.selftech</groupId>
    <artifactId>self-chain-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. Initialize the SDK:
```java
// Create configuration
SelfChainConfig config = new SelfChainConfig()
    .setNetwork("testnet")
    .setApiKey("your-api-key");

// Initialize SDK
SelfChainSDK sdk = new SelfChainSDK(config);
```

### JavaScript SDK

1. Install via npm:
```bash
npm install @selftech/sdk
```

2. Initialize:
```javascript
const { SelfChainSDK } = require('@selftech/sdk');

const sdk = new SelfChainSDK({
    network: 'testnet',
    apiKey: 'your-api-key'
});
```

## Key Components

### Blockchain Operations

```java
// Submit a transaction
TransactionResult result = sdk.blockchain().submitTransaction(transactionData);

// Get block by hash
Block block = sdk.blockchain().getBlock(blockHash);

// Get transaction status
TransactionStatus status = sdk.blockchain().getTransactionStatus(txHash);
```

### Smart Contracts

```java
// Deploy contract
ContractDeploymentResult result = sdk.contracts().deploy(contractCode);

// Call contract method
ContractResult result = sdk.contracts().callMethod(
    contractAddress,
    methodName,
    parameters
);
```

### Cross-chain Operations

```java
// ERC20 transfer
ERC20TransferResult result = sdk.bridges().erc20().transfer(
    tokenAddress,
    amount,
    recipientAddress
);

// Rosetta transfer
RosettaTransferResult result = sdk.bridges().rosetta().transfer(
    networkId,
    amount,
    recipientAddress
);
```

## Development Tools

### Local Development

```java
// Create local blockchain instance
LocalBlockchain localChain = sdk.development().createLocalBlockchain();

// Deploy contract locally
ContractAddress localAddress = localChain.deployContract(contractCode);
```

### Testing

```java
// Create test environment
TestEnvironment testEnv = sdk.development().createTestEnvironment();

// Run contract tests
TestResult result = testEnv.runContractTests(contractCode);
```

## Security Features

### Key Management

```java
// Generate key pair
KeyPair keys = sdk.security().generateKeyPair();

// Sign transaction
Signature signature = sdk.security().signTransaction(
    transactionData,
    privateKey
);
```

### Authentication

```java
// Get authentication token
String token = sdk.security().getAuthToken(apiKey);

// Verify token
boolean isValid = sdk.security().verifyToken(token);
```

## Best Practices

### Error Handling

```java
try {
    sdk.blockchain().submitTransaction(transactionData);
} catch (SelfChainException e) {
    // Handle specific error types
    if (e instanceof RateLimitException) {
        // Handle rate limiting
    } else if (e instanceof AuthenticationException) {
        // Handle authentication issues
    }
}
```

### Performance Optimization

1. Use batch operations where possible
2. Implement proper caching
3. Use connection pooling
4. Monitor API usage
5. Implement retry logic with exponential backoff

## SDK Examples

### Basic Transaction

```java
// Create transaction
Transaction transaction = new Transaction()
    .setFromAddress(fromAddress)
    .setToAddress(toAddress)
    .setAmount(amount);

// Sign and submit
TransactionResult result = sdk.blockchain().submitTransaction(
    transaction.sign(privateKey)
);
```

### Smart Contract Deployment

```java
// Deploy contract
ContractDeploymentResult result = sdk.contracts().deploy(
    contractCode,
    initialParameters
);

// Call method
ContractResult methodResult = sdk.contracts().callMethod(
    result.getContractAddress(),
    methodName,
    methodParameters
);
```

## Support

For SDK support, please:
1. Check the documentation
2. Search existing issues
3. Create a new issue
4. Join our developer community
5. Contact support@self.app

## Version Compatibility

The SDK follows semantic versioning:
- Major version: Breaking changes
- Minor version: Backward compatible changes
- Patch version: Bug fixes

### Version Format
```
<major>.<minor>.<patch>
```

## License

The SELF Chain SDK is licensed under the Apache License 2.0. See LICENSE for details.
