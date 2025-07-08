# Hello SELF - Basic Connection Example

> ⚠️ **TESTNET ONLY**: This example only works with testnet. Never use real assets!

## Overview

This is the simplest example showing how to connect to SELF Chain testnet and query basic information. It's the perfect starting point for developers new to SELF Chain.

## What You'll Learn

- How to connect to SELF Chain testnet
- Querying network information
- Understanding block structure
- Getting testnet tokens from faucet
- Proper testnet safety practices

## Running the Example

```bash
# From the examples/testnet-demos directory
cd 01-hello-self

# Run the example
node index.js
```

## Expected Output

```
==============================================================
⚠️  SELF CHAIN TESTNET WARNING ⚠️
==============================================================
You are connected to TESTNET. Important reminders:
• Testnet tokens have NO VALUE
• Network may be reset at any time
• Do not use real cryptocurrency
• This is for testing and learning only
==============================================================

🚀 Initializing SELF Chain client...

Connecting to testnet...
✅ Connected to SELF Chain Testnet!
Endpoint: https://testnet-api.self.app

📊 Network Information:
• Chain ID: self-testnet-001
• Consensus: Proof-of-AI (PoAI)
• Block Height: 123,456
• Block Time: 2.1s
• Active Validators: 10

📦 Latest Block:
• Height: 123,456
• Hash: 0xTESTNET7f9a8b7c6d5e4f3a2b1c...
• Transactions: 5
• AI Validation Score: 0.98
• Timestamp: 2024-01-20T10:30:45.123Z

💰 Testnet Faucet Information:
• Daily Limit: 100 TEST
• Cooldown: 24 hours
• How to get tokens: Use !faucet command in Discord

✨ Successfully connected to SELF Chain Testnet!

Next steps:
1. Join our Discord for testnet tokens
2. Try the token transfer example
3. Build something amazing (on testnet)!

⚠️  Remember: This is TESTNET only! ⚠️  Remember: This is TESTNET only!
```

## Code Structure

```javascript
// 1. Safety First - Always show testnet warning
TestnetWarning.display();

// 2. Configuration - Testnet only
const config = {
  network: 'testnet',
  endpoint: 'https://testnet-api.self.app'
};

// 3. Connection - With safety checks
const client = new SELFClient(config);
await client.connect();

// 4. Query Data - Read-only operations
const networkInfo = await client.getNetworkInfo();
const latestBlock = await client.getLatestBlock();
```

## Key Concepts

### Proof-of-AI (PoAI)
SELF Chain uses AI-powered consensus instead of traditional mining. The `aiScore` in blocks represents the AI validation confidence.

### Testnet vs Mainnet
- **Testnet**: For testing and development (this example)
- **Mainnet**: For real transactions (not yet launched)

### Network Information
- `chainId`: Unique identifier for the network
- `blockHeight`: Number of blocks in the chain
- `blockTime`: Average time between blocks
- `validators`: Number of active validators

## Troubleshooting

### Connection Failed
- Check your internet connection
- Verify the testnet endpoint is correct
- Ensure you're not accidentally using mainnet config

### Need Testnet Tokens?
1. Join our [Discord](https://discord.gg/WdMdVpA4C8)
2. Go to #testnet-faucet channel
3. Use command: `!faucet your_testnet_address`

## Next Steps

Once you've successfully run this example:

1. **Get Testnet Tokens**: Follow the faucet instructions
2. **Try Example 2**: Learn to transfer tokens
3. **Explore the SDK**: Check SDK documentation (when released)
4. **Build Something**: Create your own testnet application

## Security Notes

Even though this is testnet:
- Never share private keys
- Don't reuse passwords from other services
- Report any security issues to security@self.app

## Questions?

- Discord: [#testnet-help](https://discord.gg/WdMdVpA4C8)
- GitHub: Create an issue with `[example]` tag

---

Happy coding on SELF Chain testnet! 🚀