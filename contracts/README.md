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
- **Lock-up periods**: Flexible options from no lock-up to 24 months
- **Base APR**: 
  - No lock-up: 8%
  - 3 months: 12%
  - 6 months: 16%
  - 9 months: 20%
  - 12 months: 22%
  - 18 months: 25%
  - 24 months: 30%
- **Additional benefits**: For lock-ups of 12+ months
- **Bonus staking program**: For early investors with 10-30% extra tokens

#### 2. Subscription System
- **Three tiers** with increasing benefits
- **Token burns**: 50% of subscription cost is burned
- **AI Power monitoring**: Usage tracking and resource management
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
- **Total**: 500 million SELF tokens
- **Distribution**: See tokenomics documentation

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

- Enhanced governance features
- Additional DeFi integrations
- Cross-chain compatibility
- Advanced staking mechanisms

## License
MIT License - See LICENSE file for details