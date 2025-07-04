---
layout: page
title: Implementation History
---

# SELF Chain Implementation History

## Project Overview
SELF Chain is a self-sovereignty-based layer 1 blockchain that incorporates features such as upgradeability, cloud-based nodes, user rewards, and compatibility with other networks (particularly ERC-20).

## Implementation Timeline

### Initial Setup
- Renamed project from Self to SELF
- Created main entry point class `SELF.java`
- Updated `SELFParams` with SELF-specific parameters
- Updated Docker configuration for SELF branding
- Set up validator, worker, client, and monitor nodes
- Configured resource allocation and restart policies

### Core Components

#### 1. Docker Configuration
- Updated `Dockerfile.self` for SELF branding
- Created `docker-compose.self.yml` with:
  * 3 validator nodes
  * 5 worker nodes
  * 10 client nodes
  * 1 monitor node
  * SELF_PORT environment variable support
  * Resource allocation and restart policies

#### 2. Cloud Node System
- Created `CloudNodeManager`
- Created `CloudNodeRegistry`
- Implemented node participation tracking
- Added resource allocation and reputation system

#### 3. Upgrade System
- Created `UpgradeManager`
- Created `UpgradeProposal`
- Implemented upgrade voting system

#### 4. Governance System
- Created `GovernanceContract`
- Created `GovernanceProposal`
- Created `GovernanceManager`
- Implemented AI-based voting system
- Created ML-based proposal evaluation

## Current Focus: Governance Implementation

### AI-Based Voting System
- **AIValidator**: Manages validator identity, stake, and hex-based color system
- **AIVotingSystem**: Handles validator registration, proposal voting, and point-based scoring

### ML-Based Proposal Evaluation
- **ProposalEvaluator**: Evaluates proposals based on multiple features
- **MLModel**: Predicts proposal success probabilities and learns from historical data

### Point-Based System
- **PointSystem**: Manages point allocation and distribution
- **PointBasedVoting**: Handles point-based voting and approval
- Integrates ML predictions with voting power
- Reputation-weighted voting
- Stake-based point allocation

### Hex-Validator System
- **HexValidator**: Manages validator colors and selection
- **HexValidationManager**: Handles hex-based validation and scoring
- Color similarity scoring
- Validator selection algorithm
- Points-based rewards

### Monitoring and Reporting
- **GovernanceMonitor**: Tracks system metrics
- **GovernanceMetrics**: Proposal performance tracking
- **ValidatorMetrics**: Validator performance tracking
- Periodic reporting
- System health monitoring

### Bridge Service
- **BridgeService**: Manages cross-chain transactions
- **BridgeParameters**: Bridge configuration management
- **BridgeStatus**: Transaction tracking and statistics
- Supports ERC-20 and Wire Network
- Fee management
- Transaction limits
- Performance monitoring

## Next Steps

1. Integration with existing governance system
2. ERC-20 compatibility
3. Rosetta integration
4. Wire Network integration
5. Additional bridge types
6. Advanced monitoring features
7. Performance optimization

## Recent Implementation: AI-Capacity Class Migration

### Core Changes
1. **AI-Capacity Class Migration**
   - Replaced Mini* classes with AI-capacity focused classes
   - Updated all numeric calculations to use AICapacityNumber
   - Updated all data identifiers to use AIData
   - Improved type safety and precision

### Components Updated
1. **Governance System**
   - Updated GovernanceContract to use AICapacityNumber
   - Updated ProposalEvaluator to use AI-capacity metrics
   - Updated MLModel to use AICapacityNumber
   - Updated stake management operations

2. **Reward System**
   - Updated RewardMetrics to use AICapacityNumber
   - Updated RewardDistribution to use AI-capacity calculations
   - Updated validation scoring
   - Improved reward precision

3. **Node Participation**
   - Updated NodeParticipation to use AICapacityNumber
   - Updated resource tracking
   - Updated reputation scoring
   - Improved node metrics precision

### Benefits
- Enhanced type safety across the system
- Better reflection of AI-capacity focused functionality
- Improved precision in calculations
- Better integration with AI validator system
- Reduced risk of type-related errors

## Recent Implementation: Reward System

### Core Components
1. **Reward Manager**
   - Created `RewardManager` for reward distribution
   - Implemented stake-based rewards
   - Added participation tracking
   - Integrated with bridge service

2. **Stake System**
   - Created `StakeCalculator` for stake calculations
   - Implemented validator stake calculation
   - Added user stake calculation
   - Integrated with point system

3. **Metrics System**
   - Created `RewardMetrics` for performance tracking
   - Implemented distribution metrics
   - Added validation metrics
   - Created performance monitoring

4. **Monitoring System**
   - Created `RewardDistributionMonitor` for real-time monitoring
   - Implemented distribution tracking
   - Added validation monitoring
   - Created detailed reporting

### Features Implemented
- Stake-based rewards
- Participation bonuses
- Reputation bonuses
- Hex validation bonuses
- Real-time monitoring
- Performance tracking
- Success rate calculations

### Documentation
- Created comprehensive reward system documentation
- Added integration with governance system
- Documented monitoring and metrics
- Added security features

### Next Focus Areas
1. Integration with governance system
2. Advanced validation features
3. Performance optimization
4. Additional monitoring features

## Parameters

### From SELFParams.java
```java
// Core parameters
public static final MiniNumber REWARD_BASE = new MiniNumber(1000);
public static final MiniNumber RESOURCE_WEIGHT = new MiniNumber(0.3);
public static final MiniNumber UPTIME_WEIGHT = new MiniNumber(0.4);
public static final MiniNumber REPUTATION_WEIGHT = new MiniNumber(0.3);

// Governance parameters
public static final MiniNumber MIN_STAKE_FOR_PROPOSAL = new MiniNumber(1000);
public static final MiniNumber VOTE_GRACE_PERIOD = new MiniNumber(604800);
public static final MiniNumber PROPOSAL_EXPIRATION = new MiniNumber(1209600);
public static final MiniNumber MIN_VOTE_THRESHOLD = new MiniNumber(51);
public static final MiniNumber MAX_PROPOSALS_PER_NODE = new MiniNumber(5);
public static final MiniNumber PROPOSAL_FEE = new MiniNumber(100);
public static final MiniNumber STAKE_LOCK_PERIOD = new MiniNumber(2592000);
public static final MiniNumber MIN_STAKE_FOR_VOTE = new MiniNumber(100);
```

## References
- Proof of AI concept: https://proofofai.com/
- SELF Chain implementation: https://github.com/SELFHQDev/chain
- Rosetta SDK: https://www.rosetta-api.org/
- Wire Network: https://wire.network/
