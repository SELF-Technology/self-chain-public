# SELF Chain Testnet Example Applications

:::warning TESTNET ONLY
These examples are for testnet use only:
- Do not use with real assets or in production environments
- Testnet tokens have ZERO monetary value
- Network may reset at any time without notice
- All data is temporary and will be lost
:::

## Overview

This directory contains example applications demonstrating how to build on SELF Chain testnet. Each example includes prominent warnings about testnet usage and safe development practices.

## Available Examples

### 1. Hello SELF (Basic Connection)
Simple example showing how to connect to SELF Chain testnet and query basic information.

**What you'll learn**:
- Connecting to testnet
- Reading blockchain state
- Handling testnet warnings

### 2. Token Transfer Demo
Demonstrates sending TEST tokens between accounts.

:::caution
Testnet tokens have ZERO monetary value and cannot be exchanged for real assets.
:::

**What you'll learn**:
- Creating transactions
- Signing with testnet keys
- Broadcasting to network
- Checking transaction status

### 3. Block Explorer Lite
A minimal block explorer showing recent blocks and transactions.

**What you'll learn**:
- Subscribing to new blocks
- Querying historical data
- Displaying blockchain information
- Real-time updates

### 4. Simple Wallet
Basic wallet interface for testnet operations.

**What you'll learn**:
- Key generation (testnet only - NEVER use these keys for real assets)
- Balance checking
- Transaction history
- QR code generation

## Important Testnet Guidelines

### DO:
- ✅ Use these examples to learn SELF Chain concepts
- ✅ Experiment freely with testnet tokens
- ✅ Report bugs you find
- ✅ Modify examples for your own learning
- ✅ Share your testnet creations with the community

### DON'T:
- ❌ Use these examples with real cryptocurrency
- ❌ Deploy to production
- ❌ Share private keys (even testnet ones)
- ❌ Expect persistent data (testnet may reset)
- ❌ Try to sell or trade testnet tokens

## Running the Examples

### Prerequisites
```bash
# Install dependencies
npm install

# or with yarn
yarn install
```

### Environment Setup
```bash
# Copy example environment file
cp .env.example .env.testnet

# Edit .env.testnet with your testnet configuration
# WARNING: NEVER use mainnet endpoints or keys!
```

### Running Examples

Each example can be run independently:

```bash
# Example 1: Hello SELF
npm run example:hello

# Example 2: Token Transfer
npm run example:transfer

# Example 3: Block Explorer
npm run example:explorer

# Example 4: Simple Wallet
npm run example:wallet
```

## Code Structure

Each example follows this structure:
```
example-name/
├── index.js          # Main entry point
├── README.md         # Specific instructions
├── config.js         # Testnet configuration
└── utils/
    └── warnings.js   # Testnet warning displays
```

## Security Considerations

Even in testnet examples, we follow security best practices:

1. **No Hardcoded Keys**: All examples use environment variables
2. **Testnet Detection**: Automatic checks prevent mainnet usage
3. **Warning Messages**: Clear indicators of testnet status
4. **Safe Defaults**: Conservative limits and timeouts
5. **Error Handling**: Graceful failure with helpful messages

## Contributing Examples

We welcome new example contributions! Please ensure:

1. **Testnet Only**: Must work exclusively on testnet
2. **Educational Value**: Should teach specific concepts
3. **Clear Documentation**: README with learning objectives
4. **Warning Messages**: Prominent testnet warnings
5. **Code Quality**: Follow our style guide
6. **No Security Risks**: Don't expose sensitive patterns

## Example Ideas for Contributors

- [ ] NFT Minting Demo (testnet NFTs)
- [ ] Multi-Signature Wallet
- [ ] Simple DAO Voting
- [ ] Token Swap Interface
- [ ] Event Notification System
- [ ] Transaction Batching Demo

## Getting Help

- **Discord**: [#testnet-examples](https://discord.gg/WdMdVpA4C8)
- **Issues**: Use `example` label
- **Discussions**: Share your creations!

## License

These examples are MIT licensed - use them freely for learning!

---

🎓 **Learning Tip**: Start with "Hello SELF" and work your way through each example. By the end, you'll understand the basics of building on SELF Chain!