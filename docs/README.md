# SELF Documentation

## Overview

This documentation provides comprehensive technical documentation for the SELF blockchain project, covering all aspects from core architecture to implementation details. The documentation is organized into several key sections to facilitate easy navigation and understanding.

## Core Documentation Structure

### 1. Architecture Documentation
- [architecture/](https://github.com/SELF-Technology/self-chain-public/tree/main/docs/architecture)
  - [SELF_chain_architecture.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/architecture/SELF_chain_architecture.md): Comprehensive system architecture

### 2. API Documentation
- [api/](https://github.com/SELF-Technology/self-chain-public/tree/main/docs/api)
  - [API_DOCUMENTATION.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/api/API_DOCUMENTATION.md): Comprehensive API reference

### 3. Development
- [development/](https://github.com/SELF-Technology/self-chain-public/tree/main/docs/development)
  - [CODE_STYLE_GUIDE.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/development/CODE_STYLE_GUIDE.md): Code style and conventions
  - [TESTING_REQUIREMENTS.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/development/TESTING_REQUIREMENTS.md): Testing framework and requirements

### 4. Governance
- [governance/](https://github.com/SELF-Technology/self-chain-public/tree/main/docs/governance)
  - [AI_TRAINING_SYSTEM.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/governance/AI_TRAINING_SYSTEM.md): AI governance documentation
  - [GOVERNANCE_IMPLEMENTATION.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/governance/GOVERNANCE_IMPLEMENTATION.md): Governance implementation details

### 5. Integration
- [integration/](https://github.com/SELF-Technology/self-chain-public/tree/main/docs/integration)
  - [COINBASE_INTEGRATION.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/integration/COINBASE_INTEGRATION.md): Coinbase integration details
  - [ROSETTA_ERC20_INTEGRATION.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/integration/ROSETTA_ERC20_INTEGRATION.md): Rosetta ERC20 integration

### 6. Monitoring
- [monitoring/](https://github.com/SELF-Technology/self-chain-public/tree/main/docs/monitoring)
  - [monitoring_and_alerting.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/monitoring/monitoring_and_alerting.md): Monitoring system documentation

### 7. Performance
- [performance/](https://github.com/SELF-Technology/self-chain-public/tree/main/docs/performance)
  - [tps_optimization.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/performance/tps_optimization.md): Transaction processing optimization
  - [advanced_tps_optimization.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/performance/advanced_tps_optimization.md): Advanced optimization techniques

### 8. SDK
- [sdk/](https://github.com/SELF-Technology/self-chain-public/tree/main/docs/sdk)
  - [README.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/sdk/README.md): SDK overview and features
  - [token_operations.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/sdk/token_operations.md): Token operations documentation
  - [storage_integration.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/sdk/storage_integration.md): Storage integration documentation

### 9. Security
- [security/](https://github.com/SELF-Technology/self-chain-public/tree/main/docs/security)
  - [AI_CAPACITY_IMPLEMENTATION.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/security/AI_CAPACITY_IMPLEMENTATION.md): AI capacity implementation
  - [overview.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/security/overview.md): Core security documentation

### 10. Storage
- [storage/](https://github.com/SELF-Technology/self-chain-public/tree/main/docs/storage)
  - [HYBRID_ARCHITECTURE.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/storage/HYBRID_ARCHITECTURE.md): Hybrid storage architecture (IPFS + OrbitDB)

### 11. Wire Network
- [Wire/](https://github.com/SELF-Technology/self-chain-public/tree/main/docs/Wire)
  - [WIRE_NETWORK_INTEGRATION.md](https://github.com/SELF-Technology/self-chain-public/blob/main/docs/Wire/WIRE_NETWORK_INTEGRATION.md): Wire Network integration details

## Key Technical Components

### 1. Core Architecture
- Proof-of-AI (PoAI) consensus mechanism
- Hybrid storage architecture (IPFS + OrbitDB)
- Cross-chain capabilities
- AI integration with Ollama Cloud

### 2. Storage Layer
- Decentralized storage using IPFS
- Real-time database with OrbitDB
- Cross-chain data synchronization

### 3. AI Integration
- Ollama Cloud integration
- Context-aware AI assistants
- AI-powered validation
- Validator reputation system

### 4. Security Features
- Decentralized key management
- Transaction signing and verification
- Network security
- AI-powered security validation

## Documentation Tools

This documentation uses several tools and technologies:

1. **Mermaid Diagrams**
   - Interactive diagram generation
   - GitHub rendering support
   - Architecture visualization

2. **Markdown**
   - Structured documentation
   - Easy maintenance
   - GitHub compatibility

3. **Version Control**
   - Git integration
   - Documentation history
   - Branch management

## Getting Started

### Prerequisites
- GitHub account
- Mermaid diagram viewer enabled
- Basic understanding of blockchain concepts

### Navigation Tips
1. Start with the architecture overview
2. Explore specific components in their respective folders
3. Use the SDK documentation for development
4. Reference the API documentation for integration

## Documentation Standards

1. **Consistency**
   - Standard markdown format
   - Clear section headers
   - Consistent terminology

2. **Accuracy**
   - Regular updates
   - Technical verification
   - Code examples

3. **Clarity**
   - Simple explanations
   - Step-by-step guides
   - Visual aids

## Contact

For questions, please email [j@self.app](mailto:j@self.app).

## Directory Structure

```mermaid
graph TD
    A[docs] --> B[architecture]
    A --> C[api]
    A --> D[development]
    A --> E[governance]
    A --> F[integration]
    A --> G[monitoring]
    A --> H[performance]
    A --> I[sdk]
    A --> J[security]
    A --> K[storage]
    A --> L[Wire]
    A --> M[README.md]
    
    B --> B1[architecture_overview.md]
    B --> B2[network_architecture.md]
    
    I --> I1[README.md]
    I --> I2[token_operations.md]
    I --> I3[storage_integration.md]
```

## SELF Chain Architecture

![SELF Chain Architecture](Diagrams/SELF%20Chain%20Architecture.png)

This diagram shows the complete architecture of the SELF Chain system, including all layers and their relationships.

This diagram shows the complete documentation structure, with each component linked to its respective subcomponents. The documentation is designed to be both comprehensive and modular, allowing developers to dive deep into specific areas while maintaining an overview of the entire system.

## Additional Resources

- PoAI Documentation: https://proofofai.com
- Ollama Cloud Documentation: https://ollama.ai/docs
- IPFS Integration Guide: https://docs.ipfs.tech/
- OrbitDB Documentation: https://orbitdb.org/docs/

## Contact

For questions or contributions, please email [j@self.app](mailto:j@self.app).

## Documentation References

- Ollama Cloud: https://ollama.ai/docs
- IPFS: https://docs.ipfs.tech/
- OrbitDB: https://orbitdb.org/docs/
