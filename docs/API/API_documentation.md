---
layout: page
title: API documentation
---

# SELF Chain API Documentation

## 1. API Overview

The SELF Chain API provides a comprehensive interface for interacting with the SELF blockchain network. It supports reward distribution, governance operations, bridge service interactions, and monitoring functionality.

## 2. Core API Endpoints

### 2.1 Reward System API

#### 2.1.1 Reward Distribution
```java
// Get user rewards
GET /api/rewards/user/{userID}

// Get validator rewards
GET /api/rewards/validator/{validatorID}

// Distribute rewards
POST /api/rewards/distribute
```

#### 2.1.2 Stake Management
```java
// Get user stake
GET /api/stake/user/{userID}

// Get validator stake
GET /api/stake/validator/{validatorID}

// Update stake
POST /api/stake/update
```

### 2.2 Governance API

#### 2.2.1 Proposal Management
```java
// Submit proposal
POST /api/governance/proposal

// Vote on proposal
POST /api/governance/vote

// Get proposal status
GET /api/governance/proposal/{proposalID}
```

#### 2.2.2 Validator Management
```java
// Register validator
POST /api/governance/validator

// Get validator status
GET /api/governance/validator/{validatorID}

// Update validator
PUT /api/governance/validator/{validatorID}
```

### 2.3 Bridge Service API

#### 2.3.1 Cross-Chain Operations
```java
// Process transaction
POST /api/bridge/transaction

// Process reward
POST /api/bridge/reward

// Get transaction status
GET /api/bridge/transaction/{txID}
```

#### 2.3.2 Status Management
```java
// Get bridge status
GET /api/bridge/status

// Get bridge metrics
GET /api/bridge/metrics

// Get bridge configuration
GET /api/bridge/config
```

## 3. Request/Response Formats

### 3.1 Reward Distribution
```json
// Request
{
    "userID": "string",
    "amount": "number",
    "type": "string"
}

// Response
{
    "status": "success|error",
    "message": "string",
    "reward": {
        "amount": "number",
        "type": "string",
        "timestamp": "number"
    }
}
```

### 3.2 Stake Management
```json
// Request
{
    "userID": "string",
    "amount": "number",
    "type": "string"
}

// Response
{
    "status": "success|error",
    "message": "string",
    "stake": {
        "amount": "number",
        "type": "string",
        "timestamp": "number"
    }
}
```

### 3.3 Governance Operations
```json
// Request
{
    "proposal": {
        "id": "string",
        "title": "string",
        "description": "string",
        "type": "string"
    }
}

// Response
{
    "status": "success|error",
    "message": "string",
    "proposal": {
        "id": "string",
        "status": "pending|active|completed",
        "votes": "number"
    }
}
```

## 4. Error Handling

### 4.1 Error Responses
```json
{
    "error": {
        "code": "number",
        "message": "string",
        "details": "string"
    }
}
```

### 4.2 Error Codes
- 400: Bad Request
- 401: Unauthorized
- 403: Forbidden
- 404: Not Found
- 500: Internal Server Error

## 5. Security

### 5.1 Authentication
- JWT token-based authentication
- API key authentication
- Role-based access control

### 5.2 Authorization
- User-level permissions
- Validator-level permissions
- Admin-level permissions

## 6. Rate Limiting

### 6.1 Request Limits
- 100 requests per minute
- 1000 requests per hour
- 10000 requests per day

### 6.2 Response Limits
- 1MB response size limit
- 500ms response time limit

## 7. API Versions

### 7.1 Current Version
- v1.0.0
- Stable release
- Full feature set

### 7.2 Future Versions
- v1.1.0 (planned)
- New features
- Bug fixes
- Performance improvements

## 8. API Examples

### 8.1 Reward Distribution
```javascript
// JavaScript example
fetch('/api/rewards/distribute', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer <token>'
    },
    body: JSON.stringify({
        userID: 'user123',
        amount: 1000,
        type: 'user_reward'
    })
})
```

### 8.2 Stake Management
```javascript
// JavaScript example
fetch('/api/stake/update', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer <token>'
    },
    body: JSON.stringify({
        userID: 'user123',
        amount: 1000,
        type: 'user_stake'
    })
})
```

### 8.3 Governance Operations
```javascript
// JavaScript example
fetch('/api/governance/proposal', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer <token>'
    },
    body: JSON.stringify({
        proposal: {
            id: 'prop123',
            title: 'New Feature Proposal',
            description: 'Description of proposal',
            type: 'feature'
        }
    })
})
```

## 9. API Best Practices

### 9.1 Request Handling
- Use appropriate HTTP methods
- Handle errors gracefully
- Implement proper validation
- Use proper authentication

### 9.2 Response Handling
- Return proper status codes
- Include error details
- Include success messages
- Include response metadata

### 9.3 Security
- Validate all inputs
- Implement proper authentication
- Use secure protocols
- Implement rate limiting

### 9.4 Performance
- Use caching where possible
- Implement proper error handling
- Use batch processing
- Implement proper logging
