# SELF Nillion Storage Integration Documentation

## Overview
This document outlines how SELF integrates with Nillion for decentralized storage of validator data and cross-chain validation records.

## Architecture

### Core Components

1. **NillionStorage Class**
   - Handles all Nillion API interactions
   - Manages app registration and user ID generation
   - Implements secret storage and retrieval
   - Manages permissions for cross-chain validation

2. **Data Structure**
   - Each validator node has its own Nillion app_id
   - Secrets are stored with appropriate permissions
   - Data is synchronized through blockchain consensus

### Data Model

#### Validator Data
```json
{
  "validator_id": "string",
  "reputation": {
    "current": "number",
    "total_validations": "number",
    "total_score": "number",
    "last_validation": "timestamp",
    "last_update": "timestamp"
  },
  "cross_chain_validations": [
    {
      "validation_id": "string",
      "source_chain": "string",
      "target_chain": "string",
      "score": "number",
      "timestamp": "timestamp"
    }
  ],
  "chain_state": {
    "latest_block": "number",
    "difficulty": "number",
    "network_load": "number",
    "last_update": "timestamp"
  }
}
```

### Security Model

1. **Access Control**
   - Each validator has its own app_id
   - Permissions are managed through Nillion's permission system
   - Cross-chain validation requires specific permissions

2. **Data Integrity**
   - All data is stored on the Nillion Network Testnet
   - Blockchain consensus maintains data consistency
   - Validator signatures ensure data authenticity

## Implementation Details

### Nillion API Usage

1. **App Registration**
```java
// Register a new app for the validator
POST /api/apps/register
```

2. **User ID Generation**
```java
// Generate deterministic user ID from validator seed
POST /api/user
```

3. **Secret Storage**
```java
// Store validator data
POST /api/apps/{app_id}/secrets
```

4. **Secret Retrieval**
```java
// Retrieve validator data
GET /api/apps/{app_id}/secrets
```

### Error Handling
- All API calls include retry logic
- Rate limiting is handled gracefully
- Network errors are logged and retried
- Permission errors trigger appropriate alerts

## Development Notes

### Current Limitations
- Limited to Nillion Network Testnet
- Storage capacity constraints
- Network latency considerations

### Future Improvements
- Add bulk operations
- Implement caching layer
- Add more sophisticated permission management
- Add data encryption at rest

## Usage Examples

### Storing Validator Data
```java
// Store validator reputation
NillionStorage.storeValidatorData(
    validatorId,
    reputationScore,
    totalValidations,
    totalScore
);

// Store cross-chain validation
NillionStorage.storeCrossChainValidation(
    validationId,
    sourceChain,
    targetChain,
    score
);
```

### Retrieving Data
```java
// Get validator reputation
ValidatorData data = NillionStorage.getValidatorData(validatorId);

// Get cross-chain validation history
List<ValidationRecord> history = NillionStorage.getValidationHistory(validatorId);
```

## Troubleshooting

### Common Issues
1. **Permission Errors**
   - Verify correct permissions are set
   - Check validator seed is correct
   - Ensure app_id is registered

2. **Network Issues**
   - Check Nillion network status
   - Verify internet connection
   - Check rate limits

3. **Data Consistency**
   - Verify blockchain consensus
   - Check validator signatures
   - Verify timestamp accuracy

## Version History

### v1.0.0 (Initial Implementation)
- Basic Nillion integration
- Validator data storage
- Cross-chain validation support
- Permission management
- Error handling

---

This document will be updated as the implementation progresses.
