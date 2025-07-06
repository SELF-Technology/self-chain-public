---
sidebar_label: "🔗 Smart Contract Architecture"
sidebar_position: 3
---

# SELF Token Smart Contract Architecture

## Overview

The SELF token smart contract system is designed with modularity, security, and future migration in mind. Built on Ethereum as an ERC-20 token, it includes advanced features for staking, subscriptions, and governance while preparing for eventual migration to SELF Chain.

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

```solidity
// Tokens stay in user wallet
mapping(address => StakeInfo) public stakes;

struct StakeInfo {
    uint256 amount;        // Staked amount
    uint256 timestamp;     // Stake time
    uint256 tier;         // User tier level
    uint256 rewards;      // Accumulated rewards
}
```

**Benefits**:
- No smart contract risk for staked tokens
- Instant liquidity if needed
- Simplified tax implications
- Better user experience

### 2. Tier System

Three tiers provide increasing benefits:

| Tier | Required SELF | APR Bonus | Benefits |
|------|---------------|-----------|----------|
| Pioneer | 1,000 | +10% | Basic features, priority support |
| Explorer | 10,000 | +25% | Advanced features, governance voting |
| Multiverse | 100,000 | +50% | Full access, cross-chain privileges |

### 3. Dynamic Subscription Pricing

Oracle integration enables USD-based pricing:

```solidity
// Price subscriptions in USD, pay in SELF
function purchaseSubscription(uint256 tierUSD, uint256 duration) {
    uint256 selfAmount = oracle.usdToSelf(tierUSD);
    // Process payment in SELF tokens
}
```

### 4. Deflationary Mechanics

Multiple burn mechanisms create deflationary pressure:
- 50% of subscription payments burned
- Buyback program burns 40% of repurchased tokens
- Optional user burns for special features

### 5. Security Features

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
Standard ERC-20 interface ensures compatibility:

```javascript
// Uniswap V3 pool creation
const pool = await factory.createPool(
    SELF_TOKEN_ADDRESS,
    WETH_ADDRESS,
    3000 // 0.3% fee tier
);
```

### For Applications
Check user tier and subscription status:

```javascript
const self = new ethers.Contract(SELF_ADDRESS, SELF_ABI, provider);
const userTier = await self.getUserTier(userAddress);
const isActive = await self.isSubscriptionActive(userAddress);
```

### For Wallets
Display staking information:

```javascript
const stakeInfo = await self.stakes(userAddress);
const rewards = await self.calculateRewards(userAddress);
```

## Gas Optimization

The contract employs several gas-saving techniques:
- Packed struct storage
- Minimal external calls
- Efficient reward calculations
- Batch operations where possible

**Estimated Gas Costs**:
- Token Transfer: ~65,000 gas
- Stake/Unstake: ~85,000 gas
- Claim Rewards: ~75,000 gas
- Purchase Subscription: ~120,000 gas

## Development Setup

### Prerequisites
```bash
npm install --save-dev hardhat @openzeppelin/contracts
```

### Compile Contracts
```bash
npx hardhat compile
```

### Run Tests
```bash
npx hardhat test
```

### Deploy to Mainnet
```bash
npx hardhat run scripts/deploy.js --network mainnet
```

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