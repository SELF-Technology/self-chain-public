---
sidebar_label: "🔗 Smart Contract Architecture"
sidebar_position: 3
---

# SELF Token Smart Contract Architecture

:::info Roadmap Item
This document describes planned smart contract architecture. Implementation details are subject to change and should not be used for development until official release.
:::

## Overview

The SELF token smart contract system will be designed with modularity, security, and future migration in mind. Built on Ethereum as an ERC-20 token, it will include advanced features for staking, subscriptions, and governance while preparing for eventual migration to SELF Chain.

## Contract Structure

### Core Components

```
contracts/
├── SELFToken.sol           # Main ERC-20 token contract
├── SELFOracle.sol          # Price oracle for USD conversions
├── interfaces/
│   └── ISELFToken.sol      # Interface for integrations
└── future/
    ├── SELFBridge.sol      # Future bridge to SELF Chain
    └── SELFGovernance.sol  # Future governance module
```

## Key Features

### 1. Non-Custodial Staking
Unlike traditional staking, users retain full custody of their tokens:

**Technical Approach:**
- Tokens remain in user wallets during staking
- Stake information tracked on-chain
- User tier levels determine benefits
- Reward accumulation over time

**Benefits**:
- No smart contract risk for staked tokens
- Instant liquidity if needed
- Simplified tax implications
- Better user experience

### 2. Dynamic Subscription Pricing

Oracle integration enables USD-based pricing:

**Oracle Integration:**
- USD-based pricing for stability
- Real-time SELF token conversion
- Transparent pricing mechanism

### 3. Deflationary Mechanics

Multiple burn mechanisms create deflationary pressure:
- 50% of subscription payments burned
- Buyback program burns 40% of repurchased tokens
- Optional user burns for special features

### 4. Security Features

**Access Control**:
- Role-based permissions (DEFAULT_ADMIN, PAUSER, TREASURY)
- Multi-signature requirement for critical functions
- Time-locked admin actions

**Safety Mechanisms**:
- Pausable in emergencies
- Reentrancy guards
- Snapshot capability for migrations
- Prevention of staked token transfers

## Integration Guide

### For DEXs
Standard ERC-20 interface will ensure compatibility with existing DeFi protocols.

### For Applications
APIs will be provided to check user tiers and subscription status.

### For Wallets
Staking information will be easily accessible for wallet integrations.

## Efficiency Focus

The contract will be optimized for:
- Low gas costs for users
- Efficient storage patterns
- Minimal transaction complexity
- Batch operations where beneficial

## Development Timeline

**Planned Development Phases:**
1. Architecture design and specification
2. Smart contract development
3. Security auditing
4. Testnet deployment
5. Mainnet launch

## Security Considerations

### Auditing Strategy
1. Internal review and testing
2. Community bug bounty program
3. Professional audit before mainnet
4. Continuous monitoring post-launch

### Best Practices
- Immutable core functions
- Upgradeable auxiliary features
- Clear documentation
- Comprehensive event logging

## Contract Addresses

### Testnet (Goerli)
- Token: `[To be deployed]`
- Oracle: `[To be deployed]`

### Mainnet
- Token: `[To be deployed]`
- Oracle: `[To be deployed]`

## Resources

- [GitHub Repository](https://github.com/SELF-Technology/self-chain-public/tree/main/contracts)
- [Technical Documentation](https://github.com/SELF-Technology/self-chain-public/tree/main/contracts)
- [Integration Examples](https://github.com/SELF-Technology/self-chain-public/tree/main/contracts)
- [Security Audits](https://github.com/SELF-Technology/self-chain-public/tree/main/contracts)

---

The SELF token smart contract architecture provides robust features for staking, subscriptions, and ecosystem participation while maintaining security and efficiency.