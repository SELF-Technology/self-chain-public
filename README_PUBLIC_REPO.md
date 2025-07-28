# SELF Chain Public Repository

## Overview

This is the public repository for SELF Chain, containing:
- 📚 Documentation and guides
- 🔧 Public APIs and SDKs
- 📋 Smart contract interfaces
- 🌐 Developer resources
- 📖 Technical specifications

## What's Here

### `/docs` - Documentation
Comprehensive documentation for the SELF ecosystem, including:
- Getting started guides
- Architecture overviews
- API references
- Integration tutorials

### `/api` - Public API Specifications
OpenAPI specifications for interacting with SELF Chain:
- REST API endpoints
- WebSocket interfaces
- Event schemas

### `/sdk` - Software Development Kits
Client libraries for various languages:
- TypeScript/JavaScript
- Python
- Rust
- Go (coming soon)

### `/contracts` - Smart Contracts
Public smart contract interfaces and implementations:
- SELF Token (ERC-20)
- Staking contracts
- Governance contracts

### `/interfaces` - Public Type Definitions
Shared type definitions and interfaces used across the ecosystem.

### `/examples` - Example Applications
Sample applications demonstrating SELF Chain integration:
- Simple transactions
- Wallet integration
- DApp templates

## Getting Started

### For Developers

1. **Read the Documentation**
   ```bash
   cd docs
   npm install
   npm run start
   ```

2. **Install SDK**
   ```bash
   # TypeScript
   npm install @self-chain/sdk
   
   # Python
   pip install self-chain-sdk
   ```

3. **Run Examples**
   ```bash
   cd examples/simple-transaction
   npm install
   npm run start
   ```

### For Contributors

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md).

## Architecture

SELF Chain uses a unique Proof-of-AI (PoAI) consensus mechanism. While the core implementation is maintained privately for security, this repository provides all the interfaces and tools needed to build on SELF.

## Resources

- 🌐 [Website](https://self.tech)
- 📖 [Documentation](https://docs.self.tech)
- 💬 [Discord](https://discord.gg/self)
- 🐦 [Twitter](https://twitter.com/self_tech)

## License

This repository is licensed under the MIT License. See [LICENSE](LICENSE) for details.

## Security

For security concerns, please email security@self.tech. Do not open public issues for security vulnerabilities.

---

**Note**: The core blockchain implementation, consensus mechanisms, and security-critical code are maintained in a separate private repository. This separation ensures security while allowing open development of tools and integrations.