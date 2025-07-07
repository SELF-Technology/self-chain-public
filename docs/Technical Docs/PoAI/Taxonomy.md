---
title: Taxonomy
sidebar_position: 3
---

# Proof-of-AI (PoAI) Taxonomy

This document outlines the core data types and their meanings in the Proof-of-AI system. These types represent AI-capacity focused data structures that reflect their purpose in the PoAI system.

## Core Types

### AICapacityNumber
Core numeric type for AI capacity measurements

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
Core data type for AI-specific structures

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
Core string type for AI-specific text data

Represents AI-specific string data:
- Hex colors for pattern recognition
- Validation patterns
- AI model identifiers

Key features:
- Pattern matching operations
- Color space operations
- String validation methods

### AIFile
Core file type for AI-specific operations

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

## Type Purpose

The PoAI taxonomy types are designed to:
1. AICapacityNumber - Handle numeric calculations for AI capacity metrics
2. AIData - Store and manage AI-specific data structures
3. AIString - Process and validate AI-related text patterns
4. AIFile - Manage file operations for AI models and data

Each type provides enhanced AI-specific features that reflect its purpose in the PoAI system.
