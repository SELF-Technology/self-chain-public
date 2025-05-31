# SELF Chain API Documentation

Welcome to the SELF Chain API documentation. This document provides comprehensive information about the public API endpoints available in the SELF Chain system.

## API Overview

The SELF Chain API provides RESTful endpoints for interacting with the blockchain system. All endpoints are versioned and follow REST best practices.

### Base URL
```
https://api.self.app/v1
```

### Authentication
All API endpoints require authentication using a Bearer token in the Authorization header:
```
Authorization: Bearer <your-token>
```

## API Endpoints

### Blockchain Operations

1. **Submit Block**
   - POST `/blocks`
   - Submit a new block to the blockchain
   - Requires: Block data, signature
   - Response: Block hash, status

2. **Get Block**
   - GET `/blocks/{blockHash}`
   - Retrieve a specific block by hash
   - Response: Block data, transactions

3. **Get Block Height**
   - GET `/blocks/height`
   - Get current blockchain height
   - Response: Height, timestamp

### Transaction Operations

1. **Submit Transaction**
   - POST `/transactions`
   - Submit a new transaction
   - Requires: Transaction data, signature
   - Response: Transaction hash, status

2. **Get Transaction**
   - GET `/transactions/{txHash}`
   - Retrieve transaction details
   - Response: Transaction data, status

3. **List Transactions**
   - GET `/transactions`
   - List transactions by address or block
   - Query params: address, blockHash, limit, offset
   - Response: Transaction array

### Smart Contracts

1. **Deploy Contract**
   - POST `/contracts`
   - Deploy a new smart contract
   - Requires: Contract code, parameters
   - Response: Contract address, transaction hash

2. **Call Contract**
   - POST `/contracts/{contractAddress}`
   - Execute contract method
   - Requires: Method name, parameters
   - Response: Execution result

3. **Get Contract State**
   - GET `/contracts/{contractAddress}/state`
   - Retrieve contract state
   - Response: State variables

### Cross-chain Bridges

1. **ERC20 Bridge**
   - POST `/bridge/erc20`
   - Transfer ERC20 tokens
   - Requires: Token address, amount, recipient
   - Response: Transaction hash

2. **Rosetta Bridge**
   - POST `/bridge/rosetta`
   - Cross-chain transfer using Rosetta
   - Requires: Network ID, amount, recipient
   - Response: Transaction hash

3. **Wire Protocol**
   - POST `/bridge/wire`
   - Cross-chain transfer using Wire
   - Requires: Network ID, amount, recipient
   - Response: Transaction hash

## Error Responses

All API endpoints return standardized error responses:

```json
{
    "error": {
        "code": <error_code>,
        "message": "Error message",
        "details": {
            "field": "error details"
        }
    }
}
```

### Common Error Codes

- `400`: Bad Request
- `401`: Unauthorized
- `403`: Forbidden
- `404`: Not Found
- `429`: Too Many Requests
- `500`: Internal Server Error

## Rate Limiting

The API implements rate limiting:
- 100 requests per minute per IP
- 1000 requests per hour per API key
- Rate limits reset at the start of each hour

## Security

### Request Security

1. All requests must use HTTPS
2. Authentication required for all endpoints
3. Input validation enforced
4. Rate limiting applied
5. IP blocking for suspicious activity

### Response Security

1. Sensitive data encrypted
2. Personal data protected
3. API keys never exposed
4. Error messages sanitized
5. Rate limiting enforced

## API Versioning

The API follows semantic versioning:
- Major version: Breaking changes
- Minor version: Backward compatible changes
- Patch version: Bug fixes

### Version Format
```
/v<major>.<minor>.<patch>
```

## Development Guidelines

### Testing

1. Use testnet endpoints for development
2. Test with valid data
3. Handle all error cases
4. Implement retry logic
5. Monitor rate limits

### Best Practices

1. Always validate responses
2. Implement proper error handling
3. Use exponential backoff for retries
4. Cache responses when appropriate
5. Monitor API usage

## Support

For API support, please:
1. Check the documentation
2. Search existing issues
3. Create a new issue
4. Contact support@self.app

## API Status

Check API status at:
```
https://status.self.app
```

## License

The SELF Chain API documentation is licensed under the Apache License 2.0. See LICENSE for details.
