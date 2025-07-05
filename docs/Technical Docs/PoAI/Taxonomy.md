---
title: Taxonomy
sidebar_position: 3
---

# Proof-of-AI (PoAI) Taxonomy

This document outlines the core data types and their meanings in the Proof-of-AI system. These types replace the legacy Mini* classes with AI-capacity focused equivalents that better reflect their purpose in the PoAI system.

## Core Types

### AICapacityNumber
Replaces: `MiniNumber`

Represents various AI capacities and metrics:
- Stake capacity (computational resources)
- Reputation capacity (validation reliability)
- Points capacity (participation level)
- Validation metrics (pattern recognition strength)

Key operations:
- Arithmetic operations for capacity calculations
- Comparison operations for capacity assessments
- Conversion methods for different capacity scales

### AIData
Replaces: `MiniData`

Represents AI-specific data structures:
- Validator IDs
- Proposal IDs
- Pattern data
- Training data

Key features:
- Immutable data representation
- Versioning support
- Data integrity verification

### AIString
Replaces: `MiniString`

Represents AI-specific string data:
- Hex colors for pattern recognition
- Validation patterns
- AI model identifiers

Key features:
- Pattern matching operations
- Color space operations
- String validation methods

### AIFile
Replaces: `MiniFile`

Represents AI-specific file operations:
- Model storage
- Training data storage
- Validation logs
- Reputation history

Key features:
- Secure file operations
- Version control
- Data persistence

## Type Usage in PoAI Components

### AIValidator
- Uses `AICapacityNumber` for:
  - Stake tracking
  - Reputation scores
  - Validation metrics
  - Points calculation
- Uses `AIData` for:
  - Validator ID
  - Proposal tracking
  - Pattern storage
- Uses `AIString` for:
  - Hex color patterns
  - Validation patterns

### AIVotingSystem
- Uses `AICapacityNumber` for:
  - Points calculation
  - Stake management
  - Voting weight calculations
- Uses `AIData` for:
  - Validator registration
  - Proposal tracking
  - Vote recording

### AITrainingSystem
- Uses `AICapacityNumber` for:
  - Model accuracy tracking
  - Training progress
  - Validation scores
- Uses `AIData` for:
  - Training data storage
  - Model versioning
  - Training history

### ReputationSystem
- Uses `AICapacityNumber` for:
  - Reputation calculations
  - Uptime tracking
  - Participation metrics
- Uses `AIData` for:
  - Reputation history
  - Validator tracking
  - Update recording

## Migration Notes

When migrating from Mini* classes to their AI-capacity equivalents:
1. Replace all MiniNumber instances with AICapacityNumber
2. Replace all MiniData instances with AIData
3. Replace all MiniString instances with AIString
4. Replace all MiniFile instances with AIFile

Each new type maintains the same core functionality as its Mini* predecessor but with enhanced AI-specific features and naming that better reflects its purpose in the PoAI system.
