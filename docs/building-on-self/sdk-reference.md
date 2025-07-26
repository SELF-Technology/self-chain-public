---
sidebar_position: 6
---

# SDK Reference

:::danger NOT YET RELEASED
The SDKs described in this documentation are planned but not yet available. All code examples and features shown are specifications only and will not work until the SDKs are released.

**Expected Release**: Q4 2025 for JavaScript/TypeScript SDK (Watch [GitHub Releases](https://github.com/SELF-Technology/self-chain-public/releases) for updates)
:::

:::warning TESTNET ONLY
When released, these SDKs will be for testnet use only:
- Do not use in production or with real assets
- Testnet tokens have ZERO monetary value
- Network may reset at any time without notice
:::

:::info Current Status
- ✅ SDK architecture designed
- ✅ API specifications complete
- ✅ Documentation ready
- ❌ JavaScript/TypeScript SDK pending (Q4 2025)
- ❌ Rust SDK pending (Q1 2026)
- ❌ Python SDK pending (Q2 2026)
- ❌ Go SDK pending (Q2 2026)
:::

## AI-Native SDK Overview

SELF Chain SDKs are designed for AI-native development through Model Context Protocol (MCP) integration, providing built-in AI capabilities alongside traditional blockchain functionality.

### Available SDKs (Planned)

| Language | Package | Status | Features |
|----------|---------|--------|----------|
| **JavaScript/TypeScript** | `@self/sdk` | In Development | Full MCP integration, SELF AI |
| **Rust** | `self-sdk` | Planned Q1 2026 | Native MCP bindings |
| **Python** | `self-sdk` | Planned Q2 2026 | MCP + AI/ML optimized |
| **Go** | `self-go-sdk` | Planned Q2 2026 | Enterprise MCP support |

### MCP Integration

The SELF SDK includes native Model Context Protocol (MCP) support, enabling:
- **Automatic Tool Orchestration**: AI selects and invokes appropriate development tools
- **Security Analysis**: Real-time vulnerability detection through MCP servers
- **Documentation Access**: Context-aware API references and examples
- **Cross-App Coordination**: Seamless MCP-powered app integration

For detailed MCP documentation, see [MCP Integration Guide](mcp-integration.md).

## Quick Start (When Available)

### Installation
```bash
# JavaScript/TypeScript (future)
npm install @self/sdk

# Rust (future)
cargo add self-sdk

# Python (future)
pip install self-sdk
```

### Basic Usage
```javascript
// PLANNED API - Shows how SDK will work
import { SELF } from '@self/sdk';

// Initialize with AI-native features
const app = new SELF.App({
  network: 'testnet',
  aiNative: true, // Enable built-in AI capabilities
  environment: 'testnet'
});

// Connect to user instance
const session = await app.authenticate(userToken);

// Use AI-enhanced blockchain operations
const result = await session.transaction()
  .transfer({ to: 'self1234...', amount: '100' })
  .withAIValidation() // AI checks transaction optimality
  .sign()
  .broadcast();
```

## Core Features

### AI-Native Capabilities
- **Built-in SELF AI Integration**: Automatic AI tool coordination
- **Intelligent Transaction Building**: AI-optimized transaction construction
- **Cross-App Coordination**: Seamless integration with other SELF apps
- **Privacy-Preserving AI**: AI that respects user sovereignty

### Traditional Blockchain Features
- Transaction construction and broadcasting
- Blockchain state queries
- Event subscriptions
- Cryptographic operations

### Security Features
- Automatic testnet detection and warnings
- Built-in security analysis via AI
- Privacy-preserving operations
- Secure key management

## SDK Architecture

```
SELF SDK
├── Core Blockchain
│   ├── Transactions
│   ├── Queries
│   └── Events
├── AI-Native Layer
│   ├── MCP Integration
│   ├── SELF AI Services
│   ├── Rule-Based Automation
│   └── Cross-App Coordination
└── Developer Experience
    ├── TypeScript Types
    ├── Error Handling
    └── Testing Utilities
```

## API Categories

### Blockchain Operations
```javascript
// Core blockchain functionality
session.blockchain.getBalance(address)
session.blockchain.getTransactionHistory()
session.blockchain.subscribeToEvents()
```

### AI-Enhanced Features
```javascript
// AI-native capabilities
session.ai.chat(message, context)
session.ai.analyzeCode(code)
session.ai.optimizeTransaction(txParams)
```

### Cross-App Integration
```javascript
// Coordinate with other SELF apps
session.shareWithApp('self-messenger', data)
session.subscribeToApp('self-calendar')
session.ai.coordinateApps(['app1', 'app2'])
```

## Development Tools

### Testing Framework
```javascript
// Built-in testing utilities
import { SELFTest } from '@self/sdk/testing';

const testEnv = new SELFTest({
  network: 'local',
  aiMocked: true // Use AI simulation for testing
});
```

### CLI Tools
```bash
# SELF Chain CLI (future)
self-chain init my-app --ai-native
self-chain deploy --network testnet
self-chain test --with-ai
```

## Error Handling

```javascript
try {
  const result = await session.ai.analyze(code);
} catch (error) {
  switch (error.code) {
    case 'AI_UNAVAILABLE':
      // Fallback to manual analysis
      break;
    case 'PERMISSION_DENIED':
      // Request additional permissions
      break;
    case 'RATE_LIMITED':
      // Implement retry logic
      break;
  }
}
```

## Best Practices

### AI Resource Management
- Batch AI operations when possible
- Use appropriate AI models for tasks
- Implement fallback for AI failures

### Security Guidelines
- Never expose private keys
- Validate all inputs
- Use SDK's built-in security features

### Performance Optimization
- Cache frequently accessed data
- Use event subscriptions for real-time updates
- Leverage AI for transaction optimization

## Migration Guide

### From Traditional Blockchain SDKs
1. **Enable AI features**: Add `aiNative: true` to configuration
2. **Replace manual validations**: Use AI-enhanced alternatives
3. **Leverage cross-app features**: Integrate with SELF ecosystem

### Version Compatibility
- SDK versions will maintain backward compatibility
- AI features are opt-in and non-breaking
- Deprecation notices provided 6 months in advance

## Support & Resources

### Documentation
- [Getting Started](/building-on-self/getting-started)
- [Developer Integration](/building-on-self/developer-integration)
- [Project Status](/project-status) - Current development updates

### Community
- **Email**: devs@self.app
- **GitHub**: [Issues & Discussions](https://github.com/SELF-Technology/self-chain-public)
- **Discord**: [SELF Community](https://discord.gg/WdMdVpA4C8)

### Early Access
Want to be first to try the SDK? Join our community:
- Watch [GitHub Releases](https://github.com/SELF-Technology/self-chain-public/releases) for announcements
- Join [Discord](https://discord.gg/WdMdVpA4C8) for early access notifications
- Email devs@self.app with your development interests

---

**Ready to build?** The SDK will be available for testnet development in Q4 2025. Join our community to get early access notifications. 