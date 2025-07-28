---
title: SDK Preparation Guide
sidebar_label: "📚 SDK Preparation Guide"
sidebar_position: 2
---

# SDK Preparation Guide

:::danger SDKs NOT YET AVAILABLE
The SELF SDK and MCP integration described in this documentation are planned but not yet released. Code examples shown are specifications that will work when the SDK is deployed.

**Expected SDK Release**: Q4 2025 (Watch [GitHub Releases](https://github.com/SELF-Technology/self-chain-public/releases) for updates)
:::

:::warning TESTNET ONLY
**SELF Chain is currently in testnet phase.** 
- Do not use real assets or deploy production applications
- Testnet tokens have ZERO monetary value
- Network may reset at any time without notice
- All data is temporary and will be lost
:::

:::info Development Status
These developer resources are in beta. Many features are still being developed. See [Project Status](/project-status) for current capabilities.
:::

## Prepare for AI-Native Development

While SELF's SDKs are in final development, use this guide to prepare for the most advanced blockchain development experience available. SELF Chain's AI-native architecture will revolutionize how you build applications.

### 🤖 Why SELF is Different

**AI-Native by Design:**
- **Proof-of-AI Consensus**: AI validators and block builders ensure optimal network performance
- **Built-in Intelligence**: Every transaction benefits from AI validation and optimization
- **Model Context Protocol (MCP)**: Native integration of AI tools and services directly into the blockchain platform

**For Developers, This Means:**
- Focus on building great applications, not managing blockchain complexity
- Native AI capabilities through MCP - no third-party integrations needed
- Privacy-preserving AI that works across your entire application ecosystem
- Automatic tool orchestration that understands your development context

### 🚀 MCP: AI-Native Development

SELF Chain's Model Context Protocol integration revolutionizes blockchain development:

**Automated Intelligence:**
- **Smart Tool Selection**: AI automatically invokes the right tools based on your code and context
- **Security-First**: Real-time vulnerability detection and security analysis built into the development flow
- **Living Documentation**: Context-aware API references and code examples that update as you work
- **Privacy by Design**: All AI operations respect user sovereignty and data privacy

**Example MCP Workflow:**
```typescript
// Coming Q4 2025 - Example of MCP in action
import { SELFMCP } from '@self/mcp';

const mcp = new SELFMCP({ appId: 'your-app-id' });

// Write naturally - MCP handles complexity
const result = await mcp.develop({
  task: "Create a secure token transfer function",
  // MCP automatically:
  // - Generates secure code patterns
  // - Runs security analysis
  // - Provides optimization suggestions
  // - Ensures best practices
});
```

## 🚧 Current Testnet Status

- **Phase**: Active Testnet (Internal Testing)
- **Network**: Resets frequently for testing
- **Tokens**: Test tokens only - ZERO monetary value
- **Stability**: Expect disruptions and downtime
- **Features**: Limited functionality - see [Project Status](/project-status)

## Prerequisites

Before you begin, ensure you have:

- **Node.js** 18 or higher (for future SDK usage)
- **Git** (for version control)
- Basic understanding of blockchain concepts

## Ready to Build with AI-Native Blockchain?

**SELF Chain's Developer Tools Coming Q4 2025**

SELF Chain's AI-native SDKs and APIs are currently in final development. While we prepare the most advanced blockchain development platform, here's how to get ready:

### **What You Can Do Now:**
- ✅ **Learn**: Explore SELF's [AI-native architecture](/technical-docs/PoAI/Proof-of-AI)
- ✅ **Plan**: Design your application architecture  
- ✅ **Connect**: Join our developer community for early access
- ✅ **Prepare**: Get ready for radically simplified blockchain development

### **Coming Soon (Q4 2025):**
- 🔄 **MCP Integration SDK**: `npm install @self/mcp` - Full Model Context Protocol support
- 🔄 **JavaScript/TypeScript SDK**: `npm install @self/sdk` - Core blockchain SDK
- 🔄 **Public API Gateway**: Direct testnet access without running nodes
- 🔄 **AI-Native Tools**: Built-in MCP-powered development assistance
- 🔄 **Token Faucet**: Get test tokens instantly
- 🔄 **Block Explorer**: Visual blockchain exploration with AI insights

### **Want Early Access?**
Join our developer community to be first in line:
- **Discord**: [SELF Community](https://discord.gg/WdMdVpA4C8) - Get early access notifications
- **GitHub**: [Follow releases](https://github.com/SELF-Technology/self-chain-public/releases)
- **Email**: devs@self.app for direct developer updates

## Why SELF's AI-Native Development Will Be Revolutionary

While you wait, understand what makes SELF different from every other blockchain platform:

### **Traditional Blockchain Development Pain Points:**
- Complex infrastructure setup
- Manual security auditing
- Fragmented tooling
- Steep learning curves
- No built-in intelligence

### **SELF's AI-Native Solution:**
- **Zero Infrastructure Setup**: AI handles complexity
- **Built-in Security**: Real-time vulnerability detection
- **Integrated Tooling**: Everything works together seamlessly  
- **Intelligent Assistance**: AI guides development process
- **Cross-App Coordination**: Apps work together intelligently

### **Revolutionary Use Cases You'll Build:**

**🤖 Intelligent Personal Assistants**
Your users' AI learns their preferences and helps across all their apps - calendar, messaging, finance, health - while keeping data completely private.

**🔒 Privacy-Preserving Collaboration**
Teams can collaborate using AI without anyone's personal data leaving their control. Revolutionary for enterprise and creative projects.

**🌐 Self-Sovereign Web Services**
Build applications where users truly own their data and AI, creating new business models based on user empowerment rather than data extraction.

**💡 AI-Enhanced Everything**
Every blockchain interaction becomes intelligent - transactions optimize themselves, security scans happen automatically, and user experiences adapt in real-time.

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

## Development Workflow (Coming Q4 2025)

### 1. Local Development

When SDKs are released, you'll develop locally using:

```typescript
// Initialize SELF SDK
import { SelfClient } from '@self/sdk';

const client = new SelfClient({ 
  network: 'testnet',
  apiKey: 'your-api-key' 
});

// Deploy your application
const deployment = await client.deploy({
  name: 'MyApp',
  code: appCode,
  config: appConfig
});
```

### 2. Testnet Deployment

Deploy to testnet through the SDK:

```typescript
// Deploy to testnet
const tx = await client.createTransaction({
  to: contractAddress,
  data: deploymentData
});

console.log(`Deployed at: ${tx.hash}`);
```

### 3. Monitoring Your Application

```typescript
// Monitor transactions
const subscription = client.subscribeToTransactions((tx) => {
  console.log(`New transaction: ${tx.hash}`);
});

// Check blockchain status
const status = await client.getStatus();
console.log(`Current height: ${status.height}`);
```

## Available Tools & Resources

### Command Line Interface (CLI)
```bash
# Coming Q4 2025
# npm install -g @self/cli

# View available commands (when released)
# self-cli --help
```

### SDKs (Coming Soon)
- **Rust SDK**: Native Rust integration
- **JavaScript/TypeScript SDK**: For web developers
- **Python SDK**: For data scientists and researchers
- **Go SDK**: For backend services

### Documentation
- [Architecture Overview](/technical-docs/self-chain/SELF_Chain_Architecture)
- [PoAI Consensus](/technical-docs/PoAI/Proof-of-AI)
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
- [Project Status](https://docs.self.app/project-status)
- Testnet Explorer (Coming Soon)

## Contributing

We love contributions! Even in testnet phase, you can:

1. **Report bugs** - Help us improve stability
2. **Suggest features** - Shape the future of SELF Chain
3. **Write documentation** - Help others get started
4. **Build example apps** - Show what's possible

Contact us at devs@self.app for contribution guidelines.

## Testnet Roadmap

### Completed
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
2. Review the [Project Status](https://docs.self.app/project-status)
3. Explore the codebase
4. Build something amazing!
5. Share your feedback via GitHub Issues or devs@self.app

## 🎯 SELF SDK Design Principles

When released, the SELF SDK will follow these core principles:

**🔧 Simplicity** - Easy to understand and use, with clear documentation and examples that enable developers to get started quickly and efficiently.

**🔐 Security** - Robust security features to protect user data and ensure the integrity of applications.

**⚡ Reliability** - Stable and reliable infrastructure with minimal downtime and consistent performance.

**👥 Usability** - Designed with the end-user in mind, ensuring applications are intuitive and provide seamless user experiences.

**♻️ Code Reusability** - Write code once and use it across multiple parts of your application or different projects.

**📦 Modularity** - Include only the components you need, reducing complexity and application size.

**🧪 Continuous Validation** - Built-in tools for continuous testing and validation to catch issues early and maintain high-quality code.

Welcome to the future of blockchain - powered by AI! 🚀

---

*Remember: Testnet is for testing. Mainnet is for changing the world.*