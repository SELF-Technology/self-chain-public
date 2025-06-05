# SELF Chain Governance Implementation

## Overview
This document outlines the implementation of the governance system for SELF Chain, focusing on the AI-driven voting and proposal evaluation system.

## Core Components

### 1. AI-Capacity Voting System
- **AIValidator**: Manages validator identity, stake, and hex-based color system using AICapacityNumber
- **AIVotingSystem**: Handles validator registration, proposal voting, and point-based scoring with AICapacityNumber
- **GovernanceContract**: Manages stake and proposal operations using AICapacityNumber

### 2. AI-Capacity Proposal Evaluation
- **ProposalEvaluator**: Evaluates proposals using AI-capacity metrics
- **MLModel**: Predicts proposal success using AICapacityNumber
- **RewardMetrics**: Tracks proposal performance using AICapacityNumber

## Key Features

### AI Validator Features
- Hex-based color validation system
- Stake-weighted voting
- Random validator selection
- Efficiency-based bonus points

### ML Evaluation Features
- Feature-based scoring:
  - Stake-based scoring
  - Reputation scoring
  - Resource efficiency
  - Network impact
  - Consensus score
- Self-learning capabilities
- Resource efficiency tracking

## Implementation Details

### AIValidator
```java
// Key methods:
public boolean canVote(GovernanceProposal zProposal)
public boolean validateProposal(GovernanceProposal zProposal)
private String calculateNewColor(String zHexProposal)
```

### AIVotingSystem
```java
// Key methods:
public void registerValidator(AIData zValidatorID, AICapacityNumber zStake)
public List<AIValidator> selectValidators(GovernanceProposal zProposal)
public boolean processVote(AIData zValidatorID, GovernanceProposal zProposal, boolean zVote)
```

### ProposalEvaluator
```java
// Key methods:
public double evaluateProposal(GovernanceProposal zProposal)
private Map<String, Double> calculateFeatures(GovernanceProposal zProposal)
```

### MLModel
```java
// Key methods:
public double predict(GovernanceProposal zProposal)
public void train(GovernanceProposal zProposal, double zActualScore)
```

## Next Steps

1. Point-based system integration
2. Hex-validator selection mechanism
3. Monitoring and reporting capabilities
4. Integration with existing governance system

## Governance Parameters

### From SELFParams.java
```java
// Governance parameters
public static final AICapacityNumber MIN_STAKE_FOR_PROPOSAL = new AICapacityNumber(1000);
public static final AICapacityNumber VOTE_GRACE_PERIOD = new AICapacityNumber(604800);
public static final AICapacityNumber PROPOSAL_EXPIRATION = new AICapacityNumber(1209600);
public static final AICapacityNumber MIN_VOTE_THRESHOLD = new AICapacityNumber(51);
public static final AICapacityNumber MAX_PROPOSALS_PER_NODE = new AICapacityNumber(5);
public static final AICapacityNumber PROPOSAL_FEE = new AICapacityNumber(100);
public static final AICapacityNumber STAKE_LOCK_PERIOD = new AICapacityNumber(2592000);
public static final AICapacityNumber MIN_STAKE_FOR_VOTE = new AICapacityNumber(100);
```

## References
- Proof of AI concept: https://proofofai.com/
- SELF Chain implementation: https://github.com/SELFHQDev/chain
