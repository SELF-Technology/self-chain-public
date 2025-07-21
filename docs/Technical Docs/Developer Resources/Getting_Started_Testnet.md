---
title: Getting Started (Testnet)
sidebar_position: 1
---

# Getting Started with SELF Chain (Testnet)

> 🚧 **BETA NOTICE**: These developer resources are currently in beta and under active development. Many features described here are coming soon. We're working hard to bring you a complete development experience!

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

## Testnet Setup Guide

> **Important**: This guide requires technical expertise. Easy-access developer tools (API, SDK, faucet) are currently in development. See [Project Status](/Getting%20Started) for current limitations.

### 1. Clone the Repository

```bash
git clone https://github.com/SELF-Technology/self-chain-public.git
cd self-chain-public
```

### 2. Set Up Testnet Configuration

Create a testnet configuration file:

```bash
# ⚠️ CONFIG TEMPLATE - Testnet not yet deployed
# This example shows the planned configuration format
cp config/testnet.example.toml config/testnet.toml
```

> ⚠️ **IMPORTANT**: Never use mainnet keys or real funds on testnet!

### 3. Get Testnet Tokens

Testnet tokens are available from our faucet:

```bash
# ⚠️ NOT YET AVAILABLE
# The testnet currently operates without a token economy
# Token faucet will be available when the token system is implemented
# See Getting Started section for updates
```

### 4. Connect to Testnet

```javascript
// ⚠️ CODE EXAMPLE - NOT YET FUNCTIONAL
// JavaScript SDK is currently in development
// This shows the planned API interface:

const { SELFClient } = require('@self-chain/sdk'); // Package not yet published

const client = new SELFClient({
  network: 'testnet',
  endpoint: 'https://testnet-api.self.app', // API Gateway not yet deployed
  // WARNING: This is a testnet endpoint - do not send real assets!
});
```

## Developer Expectations

### What You Can Do Today

✅ **Available Now:**
- Clone and build the SELF Chain node from source
- Run a local development node for testing
- Connect to the testnet by running your own node
- Explore the open-source codebase
- Test core blockchain functionality
- Participate in consensus as a validator (requires AI setup)

### What's Coming Soon

🔄 **In Active Development:**
- Public API Gateway for easy testnet access
- JavaScript/TypeScript SDK
- Token faucet for getting test tokens
- Configuration templates and Docker images
- Comprehensive developer documentation

### What's Not Ready Yet

❌ **Not Available:**
- Production deployment (testnet only)
- Direct API access without running a node
- SDK packages (npm, crates.io, pip)
- GUI tools or block explorer
- Smart contract deployment
- Stable network (expect resets)

### Required Technical Skills

To work with SELF Chain today, you should be comfortable with:
- Building Rust projects from source
- Running command-line blockchain nodes
- Understanding P2P networking basics
- Debugging configuration issues
- Working without extensive documentation

If this seems daunting, consider waiting for our developer tools release later this year, which will provide a much easier onboarding experience.

## Understanding PoAI Consensus

SELF Chain uses Proof-of-AI (PoAI) consensus, which is fundamentally different from traditional blockchains:

1. **AI-Block Builders** create optimal blocks
2. **Voting Algorithm** coordinates consensus
3. **AI-Validators** determine the winning block

Learn more: [Proof-of-AI Documentation](/Technical%20Docs/PoAI/Proof-of-AI)

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
- [Architecture Overview](/Technical%20Docs/SELF%20Chain/SELF_Chain_Architecture)
- [PoAI Consensus](/Technical%20Docs/PoAI/Proof-of-AI)
- API Reference (Coming soon)

## Common Testnet Issues

### Connection Problems
```bash
# Check testnet status (Coming soon)
# curl https://testnet-api.self.app/status

# Expected response:
# {
#   "network": "testnet",
#   "status": "operational",
#   "block_height": 12345,
#   "warning": "This is a test network - do not use real assets"
# }
```

### Faucet Issues (When Available)
- Planned faucet limits: 100 TEST tokens per day per address
- Support channels: GitHub Issues, Discord, Email (devs@self.app)
- Test tokens have NO value

### Network Resets
- Will be announced via:
  - GitHub Releases
  - Discord community
  - Social media channels
- 48 hours advance notice
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
- **GitHub**: [Issues & Discussions](https://github.com/SELF-Technology/self-chain-public)
  - Report bugs and request features
  - Technical discussions
  - Community contributions

- **Discord**: [Join our community](https://discord.gg/WdMdVpA4C8)
  - `#testnet-help` - Technical support
  - `#dev-general` - Development discussion
  - `#bug-reports` - Report issues

- **Email**: devs@self.app for developer support

### Resources
- [Documentation](https://docs.self.app)
- [Project Status](https://docs.self.app/Getting%20Started)
- Testnet Explorer (Coming Soon)

## Contributing

We love contributions! Even in testnet phase, you can:

1. **Report bugs** - Help us improve stability
2. **Suggest features** - Shape the future of SELF Chain
3. **Write documentation** - Help others get started
4. **Build example apps** - Show what's possible

See [CONTRIBUTING.md](https://github.com/SELF-Technology/self-chain-public/blob/main/CONTRIBUTING.md) for guidelines.

## Testnet Roadmap

### Completed (Q1-Q2 2025)
- ✅ Basic PoAI consensus
- ✅ Transaction processing
- ✅ Core blockchain implementation
- ✅ Documentation foundation

### Current (Q3 2025) 
- 🔄 Public testnet deployment preparation
- 🔄 API Gateway development
- 🔄 Developer SDK creation
- 🔄 Infrastructure setup

### Next (Q4 2025)
- Public testnet launch
- Token faucet activation
- Block explorer deployment
- Developer onboarding tools

## ⚠️ Important Reminders

1. **This is a TESTNET** - Not for production use
2. **Test tokens have NO VALUE** - Never buy/sell them
3. **Network may reset** - Don't store important data
4. **Expect bugs** - Report them to help us improve
5. **Have fun experimenting** - That's what testnet is for!

## Next Steps

1. Join our community:
   - [GitHub Discussions](https://github.com/SELF-Technology/self-chain-public/discussions)
   - [Discord](https://discord.gg/WdMdVpA4C8)
2. Review the [Project Status](https://docs.self.app/Getting%20Started)
3. Explore the codebase
4. Build something amazing!
5. Share your feedback via GitHub Issues or devs@self.app

Welcome to the future of blockchain - powered by AI! 🚀

---

*Remember: Testnet is for testing. Mainnet is for changing the world.*