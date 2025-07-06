# SELF Token Smart Contracts

## Overview
The SELF token is an ERC-20 token with advanced features designed to power the SELF ecosystem. It includes non-custodial staking, subscription tiers, and a deflationary burn mechanism.

## Contract Architecture

### Core Contracts

#### SELFToken.sol
The main ERC-20 token contract with:
- **Non-custodial staking**: Users stake tokens without transferring them
- **Tier system**: Pioneer, Explorer, and Multiverse tiers based on stake amount
- **Subscription management**: Time-based access to premium features
- **Burn mechanism**: Deflationary tokenomics
- **Buyback distribution**: Automated distribution to burn, staking rewards, and rebates

#### SELFOracle.sol
Price oracle for USD-based subscription pricing:
- Chainlink integration for ETH/USD pricing
- Manual SELF/ETH price updates (until Chainlink feed available)
- Price change limits for security

### Key Features

#### 1. Staking System
- **Non-custodial**: Tokens remain in user's wallet
- **Tier benefits**:
  - Pioneer (1,000 SELF): 10% bonus APR
  - Explorer (10,000 SELF): 25% bonus APR
  - Multiverse (100,000 SELF): 50% bonus APR
- **Base APR**: 10% (before tier bonuses)

#### 2. Subscription System
- **Three tiers** with increasing benefits
- **Token burns**: 50% of subscription cost is burned
- **Shine tracking**: Usage percentage monitoring
- **USD-based pricing**: Dynamic token amounts via oracle

#### 3. Security Features
- **Access control**: Role-based permissions
- **Pausable**: Emergency pause functionality
- **Snapshot**: For future migrations
- **Reentrancy protection**: Security against attacks
- **Staking lock**: Cannot transfer staked tokens

## Deployment

### Prerequisites
```bash
npm install --save-dev hardhat @openzeppelin/contracts @chainlink/contracts
```

### Deploy to Ethereum Mainnet
```bash
npx hardhat run scripts/deploy.js --network mainnet
```

### Deploy to Testnet (Goerli)
```bash
npx hardhat run scripts/deploy.js --network goerli
```

## Testing

Run the test suite:
```bash
npx hardhat test
```

## Token Economics

### Initial Supply
- **Total**: 10 billion SELF tokens
- **Distribution**: TBD based on tokenomics plan

### Deflationary Mechanism
- 50% of subscription payments burned
- Buyback program burns 40% of repurchased tokens

### Reward Distribution
- 40% of buybacks to staking rewards
- 20% of buybacks to subscription rebates
- Sustainable circular economy

## Integration

### For DEX Listing
```solidity
// Standard ERC-20 interface
IERC20 self = IERC20(SELF_TOKEN_ADDRESS);
```

### For Applications
```solidity
// Check user tier
ISELFToken self = ISELFToken(SELF_TOKEN_ADDRESS);
uint256 tier = self.getUserTier(userAddress);
```

## Security Considerations

1. **Audit Required**: Professional audit before mainnet deployment
2. **Multi-sig Admin**: Use Gnosis Safe for admin roles
3. **Timelock**: Consider adding timelock for critical functions
4. **Bug Bounty**: Implement bug bounty program

## Future Enhancements

### Phase 2 - Bridge to SELF Chain
- Snapshot capability for migration
- Bridge contract development
- Dual token support

### Phase 3 - Native SELF Coin
- Full migration to SELF Chain
- Enhanced features on native chain
- CEX support for native coin

## License
MIT License - See LICENSE file for details