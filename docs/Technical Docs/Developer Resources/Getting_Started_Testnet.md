---
title: Getting Started (Testnet)
sidebar_position: 1
---

# Getting Started with SELF Chain (Testnet)

> ⚠️ **TESTNET WARNING**: SELF Chain is currently in testnet phase. Do not use real assets or deploy production applications. Testnet tokens have no value and the network may be reset at any time.

## Welcome Developers!

Welcome to SELF Chain - a revolutionary blockchain powered by Proof-of-AI (PoAI) consensus. This guide will help you start building on our testnet.

## 🚧 Current Testnet Status

- **Phase**: Active Testnet
- **Network**: May be reset periodically
- **Tokens**: Test tokens only (no real value)
- **Stability**: Expect occasional disruptions
- **Features**: Core functionality available, some features in development

## Prerequisites

Before you begin, ensure you have:

- **Rust** 1.70 or higher
- **Node.js** 18 or higher  
- **Git**
- **Docker** (optional, for containerized development)
- Basic understanding of blockchain concepts

## Quick Start (Testnet)

### 1. Clone the Repository

```bash
git clone https://github.com/SELF-Technology/self-chain-public.git
cd self-chain-public
```

### 2. Set Up Testnet Configuration

Create a testnet configuration file:

```bash
cp config/testnet.example.toml config/testnet.toml
```

> ⚠️ **IMPORTANT**: Never use mainnet keys or real funds on testnet!

### 3. Get Testnet Tokens

Testnet tokens are available from our faucet:

```bash
# Coming soon - Discord faucet bot
# Join our Discord: https://discord.gg/WdMdVpA4C8
# Use command: !faucet <your_testnet_address>
```

### 4. Connect to Testnet

```javascript
// Example connection (JavaScript SDK - coming soon)
const { SELFClient } = require('@self-chain/sdk');

const client = new SELFClient({
  network: 'testnet',
  endpoint: 'https://testnet-api.self.app',
  // WARNING: This is a testnet endpoint - do not send real assets!
});
```

## Understanding PoAI Consensus

SELF Chain uses Proof-of-AI (PoAI) consensus, which is fundamentally different from traditional blockchains:

1. **AI-Block Builders** create optimal blocks
2. **Voting Algorithm** coordinates consensus
3. **AI-Validators** determine the winning block

Learn more: [Proof-of-AI Documentation](Technical%20Docs/PoAI/Proof-of-AI.md)

## Testnet Limitations

### What You CAN Do on Testnet:
- ✅ Test smart contracts
- ✅ Experiment with PoAI consensus
- ✅ Build and test dApps
- ✅ Report bugs and issues
- ✅ Learn the SELF Chain architecture

### What You CANNOT Do on Testnet:
- ❌ Use real cryptocurrency
- ❌ Deploy production applications
- ❌ Expect persistent data (resets may occur)
- ❌ Rely on uptime guarantees
- ❌ Transfer testnet tokens to mainnet

## Development Workflow

### 1. Local Development

For local testing without connecting to testnet:

```bash
# Start local development node
cargo run --bin self-chain-node -- --dev

# This runs a local instance with:
# - Instant block production
# - Pre-funded test accounts
# - No real PoAI consensus (simplified for development)
```

### 2. Testnet Deployment

When ready to test on the actual testnet:

```bash
# Build your application
cargo build --release

# Deploy to testnet (example)
self-chain-cli deploy --network testnet --contract ./target/wasm32-unknown-unknown/release/my_contract.wasm
```

### 3. Monitoring Your Application

```bash
# Check transaction status
self-chain-cli tx status <tx_hash> --network testnet

# Monitor blocks
self-chain-cli blocks watch --network testnet
```

## Available Tools & Resources

### Command Line Interface (CLI)
```bash
# Install CLI
cargo install self-chain-cli

# View available commands
self-chain-cli --help
```

### SDKs (Coming Soon)
- **Rust SDK**: Native Rust integration
- **JavaScript/TypeScript SDK**: For web developers
- **Python SDK**: For data scientists and researchers
- **Go SDK**: For backend services

### Documentation
- [Architecture Overview](Architecture/SELF_Chain_Architecture.md)
- [PoAI Consensus](Technical%20Docs/PoAI/Proof-of-AI.md)
- [API Reference](https://testnet-api.self.app/docs) (Testnet)

## Common Testnet Issues

### Connection Problems
```bash
# Check testnet status
curl https://testnet-api.self.app/status

# Expected response:
{
  "network": "testnet",
  "status": "operational",
  "block_height": 12345,
  "warning": "This is a test network - do not use real assets"
}
```

### Faucet Issues
- Faucet limits: 100 TEST tokens per day per address
- If faucet is empty, notify us on Discord
- Test tokens have NO value

### Network Resets
- Announced 48 hours in advance on Discord
- All testnet data will be wiped
- New genesis block created

## Security Considerations for Testnet

Even on testnet, follow security best practices:

1. **Never share private keys** - even testnet keys
2. **Don't reuse passwords** from other services
3. **Report vulnerabilities** to security@self.app
4. **Test edge cases** - help us find bugs!

## Getting Help

### Community Support
- **Discord**: [Join our community](https://discord.gg/WdMdVpA4C8)
  - `#testnet-help` - Technical support
  - `#dev-general` - Development discussion
  - `#bug-reports` - Report issues

### Resources
- [GitHub Issues](https://github.com/SELF-Technology/self-chain-public/issues)
- [Documentation](https://docs.self.app)
- [Testnet Explorer](https://testnet-explorer.self.app) (Coming Soon)

## Contributing

We love contributions! Even in testnet phase, you can:

1. **Report bugs** - Help us improve stability
2. **Suggest features** - Shape the future of SELF Chain
3. **Write documentation** - Help others get started
4. **Build example apps** - Show what's possible

See [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

## Testnet Roadmap

### Current (Q1 2024)
- ✅ Basic PoAI consensus
- ✅ Transaction processing
- ✅ Developer tools
- 🔄 Performance optimization

### Next (Q2 2024)
- Advanced smart contracts
- Enhanced developer SDKs
- Testnet stability improvements
- Public testnet explorer

### Future (Q3-Q4 2024)
- Feature freeze for mainnet
- Security audits
- Load testing
- Mainnet preparation

## ⚠️ Important Reminders

1. **This is a TESTNET** - Not for production use
2. **Test tokens have NO VALUE** - Never buy/sell them
3. **Network may reset** - Don't store important data
4. **Expect bugs** - Report them to help us improve
5. **Have fun experimenting** - That's what testnet is for!

## Next Steps

1. Join our [Discord](https://discord.gg/WdMdVpA4C8)
2. Get testnet tokens from the faucet
3. Try the [Hello World Tutorial](tutorials/hello_world_testnet.md) (Coming Soon)
4. Build something amazing!
5. Share your feedback

Welcome to the future of blockchain - powered by AI! 🚀

---

*Remember: Testnet is for testing. Mainnet is for changing the world.*