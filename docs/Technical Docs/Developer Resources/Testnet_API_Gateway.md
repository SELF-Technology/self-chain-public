---
title: Testnet API Gateway (Coming Soon)
sidebar_position: 3
---

# SELF Chain Testnet API Gateway

> 🚧 **NOT YET DEPLOYED**: This API gateway is planned but not yet active. This documentation describes the intended implementation. The endpoints shown below are specifications and will not work until deployment is complete.

> 📅 **EXPECTED DEPLOYMENT**: Q1 2024 (Check [Discord](https://discord.gg/WdMdVpA4C8) for updates)

> ⚠️ **TESTNET WARNING**: Once deployed, this API gateway will provide access to SELF Chain's testnet only. Do not send real assets or deploy production applications. All testnet tokens have no value.

## Status: Planning Phase

**Current State**: 
- ✅ Specification complete
- ✅ Documentation ready
- ⏳ Infrastructure setup pending
- ⏳ Deployment pending
- ⏳ Testing pending

## When Will This Be Available?

The API Gateway deployment is tracked as **Task #27** in our development roadmap. Once deployed:

1. This documentation will be updated to remove all "NOT ACTIVE" warnings
2. The endpoints will become functional
3. An announcement will be made on [Discord](https://discord.gg/WdMdVpA4C8)
4. The testnet connection guide will be updated with working examples

**In the meantime**:
- Review this specification to understand the planned API
- Prepare your applications for future integration
- Join our Discord for deployment updates
- Consider running a local node for immediate testing needs

## Overview

The SELF Chain Testnet API Gateway provides secure, reliable access to our testnet blockchain network. Built on enterprise-grade infrastructure, it offers developers a production-like environment for testing and development.

### Key Features

- 🔒 **HTTPS/TLS** - All connections secured with SSL certificates
- ⚡ **Rate Limiting** - Fair usage policies to ensure availability
- 🌐 **CORS Support** - Enable web application development
- 📊 **Health Monitoring** - Real-time status and availability checks
- 🚀 **High Performance** - Optimized for developer productivity

## Endpoint Information

### Primary Endpoint (Planned)

```
https://testnet-api.self.app (NOT ACTIVE YET)
```

### Backup Endpoints (Planned)

During the beta phase, we may provide additional endpoints for redundancy:

```
https://testnet-api-us.self.app  (US Region - NOT ACTIVE)
https://testnet-api-eu.self.app  (EU Region - NOT ACTIVE)
https://testnet-api-ap.self.app  (Asia Pacific - NOT ACTIVE)
```

> **IMPORTANT**: These endpoints are not yet deployed. Attempting to connect will result in connection errors.

## Rate Limiting

To ensure fair access for all developers, the following rate limits apply:

| Tier | Requests/Minute | Burst Capacity | Daily Limit |
|------|----------------|----------------|-------------|
| **Default** | 100 | 200 | 100,000 |
| **Authenticated** | 500 | 1,000 | 500,000 |
| **Partner** | 2,000 | 4,000 | Unlimited |

### Rate Limit Headers

All responses include rate limit information:

```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1640995200
X-RateLimit-Burst-Capacity: 200
```

### Handling Rate Limits

When rate limited, you'll receive:

```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json
Retry-After: 60

{
  "error": "rate_limit_exceeded",
  "message": "Too many requests. Please retry after 60 seconds.",
  "retry_after": 60
}
```

## CORS Configuration

The API gateway supports Cross-Origin Resource Sharing (CORS) for web applications:

### Allowed Origins

- `http://localhost:*` (Development)
- `https://*.self.app` (SELF domains)
- `https://*.vercel.app` (Vercel deployments)
- `https://*.netlify.app` (Netlify deployments)

### Allowed Methods

```
GET, POST, OPTIONS, PUT, DELETE
```

### Allowed Headers

```
Origin, X-Requested-With, Content-Type, Accept, Authorization
```

### Preflight Requests

The gateway automatically handles CORS preflight requests:

```bash
curl -X OPTIONS https://testnet-api.self.app/v1/status \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST"
```

## API Endpoints (Specification)

> **Note**: The following endpoints are planned specifications. They are not yet operational.

### Health & Status

#### Gateway Health Check

```http
GET /health (NOT ACTIVE)
```

Response:
```json
{
  "status": "healthy",
  "timestamp": "2024-01-15T10:30:00Z",
  "version": "1.0.0"
}
```

#### Network Status

```http
GET /v1/status
```

Response:
```json
{
  "network": "testnet",
  "status": "operational",
  "chain_id": "self-testnet-1",
  "latest_block": {
    "height": 123456,
    "hash": "0x...",
    "timestamp": "2024-01-15T10:29:30Z"
  },
  "node_info": {
    "version": "0.1.0",
    "peer_count": 8,
    "syncing": false
  },
  "warning": "This is a test network - do not use real assets"
}
```

### Blockchain Queries

#### Get Block

```http
GET /v1/blocks/{height}
GET /v1/blocks/hash/{hash}
GET /v1/blocks/latest
```

#### Get Transaction

```http
GET /v1/transactions/{hash}
```

#### Get Account

```http
GET /v1/accounts/{address}
GET /v1/accounts/{address}/balance
GET /v1/accounts/{address}/transactions
```

### Transaction Submission

#### Submit Transaction

```http
POST /v1/transactions

Content-Type: application/json
{
  "tx": "base64_encoded_signed_transaction"
}
```

Response:
```json
{
  "hash": "0x...",
  "status": "pending",
  "estimated_confirmation": 5
}
```

## WebSocket Support

For real-time updates, connect to our WebSocket endpoint:

```javascript
const ws = new WebSocket('wss://testnet-api.self.app/v1/ws');

ws.on('open', () => {
  // Subscribe to new blocks
  ws.send(JSON.stringify({
    type: 'subscribe',
    channel: 'blocks'
  }));
});

ws.on('message', (data) => {
  const event = JSON.parse(data);
  console.log('New block:', event);
});
```

### Available Channels

- `blocks` - New block events
- `transactions` - Transaction confirmations
- `validators` - Validator set changes

## Authentication (Optional)

While the testnet is open for public use, authenticated requests receive higher rate limits:

```http
Authorization: Bearer YOUR_API_KEY
```

To obtain an API key:
1. Join our [Discord](https://discord.gg/WdMdVpA4C8)
2. Use the `/api-key` command in #testnet-help
3. Follow the bot's instructions

## Error Handling

### Error Response Format

```json
{
  "error": "error_code",
  "message": "Human readable error message",
  "details": {
    "field": "additional_context"
  }
}
```

### Common Error Codes

| Code | Description |
|------|-------------|
| `invalid_request` | Malformed request |
| `not_found` | Resource not found |
| `rate_limit_exceeded` | Too many requests |
| `internal_error` | Server error |
| `network_error` | Blockchain network issue |

## SDK Integration Examples

> **IMPORTANT**: These are example implementations for when the API gateway is deployed. They will not work until the service is active.

### JavaScript/TypeScript (Future Implementation)

```javascript
// This code will work once the API is deployed
// Currently it will throw a connection error

// Using fetch
const response = await fetch('https://testnet-api.self.app/v1/status');
const status = await response.json();

// Using axios with interceptors
const client = axios.create({
  baseURL: 'https://testnet-api.self.app/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add retry logic for rate limits
client.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 429) {
      const retryAfter = error.response.headers['retry-after'] || 60;
      await new Promise(resolve => setTimeout(resolve, retryAfter * 1000));
      return client.request(error.config);
    }
    return Promise.reject(error);
  }
);
```

### Python

```python
import requests
from time import sleep

class SELFTestnetClient:
    def __init__(self):
        self.base_url = "https://testnet-api.self.app/v1"
        self.session = requests.Session()
    
    def get_status(self):
        response = self.session.get(f"{self.base_url}/status")
        if response.status_code == 429:
            retry_after = int(response.headers.get('Retry-After', 60))
            sleep(retry_after)
            return self.get_status()
        response.raise_for_status()
        return response.json()
```

### cURL

```bash
# Get network status
curl -X GET https://testnet-api.self.app/v1/status \
  -H "Accept: application/json"

# Submit transaction
curl -X POST https://testnet-api.self.app/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{"tx": "base64_encoded_transaction"}'
```

## Monitoring & Observability

### Service Status Page

Check real-time API gateway status:
```
https://status.self.app
```

### Metrics Endpoint

For developers building monitoring dashboards:

```http
GET /metrics
```

Provides Prometheus-compatible metrics including:
- Request counts and latencies
- Error rates
- Active connections
- Rate limit statistics

## Best Practices

### 1. Implement Retry Logic

Always implement exponential backoff for failed requests:

```javascript
async function retryRequest(fn, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn();
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      const delay = Math.pow(2, i) * 1000;
      await new Promise(resolve => setTimeout(resolve, delay));
    }
  }
}
```

### 2. Cache Responses

Cache immutable data to reduce API calls:

```javascript
const blockCache = new Map();

async function getBlock(height) {
  if (blockCache.has(height)) {
    return blockCache.get(height);
  }
  const block = await fetchBlock(height);
  blockCache.set(height, block);
  return block;
}
```

### 3. Use Compression

Enable gzip compression for responses:

```javascript
fetch('https://testnet-api.self.app/v1/blocks/latest', {
  headers: {
    'Accept-Encoding': 'gzip, deflate'
  }
});
```

### 4. Handle Network Errors

Always handle network failures gracefully:

```javascript
try {
  const data = await client.get('/status');
  // Process data
} catch (error) {
  if (error.code === 'ECONNREFUSED') {
    console.error('API gateway is unreachable');
    // Fallback logic
  }
}
```

## Security Considerations

### TLS/SSL

- All connections must use HTTPS
- Minimum TLS version: 1.2
- Certificate pinning recommended for production apps

### API Keys

- Store API keys securely (never in code)
- Rotate keys regularly
- Use environment variables

### Request Validation

- Validate all inputs client-side
- Never trust data from the testnet
- Implement request signing for sensitive operations

## Troubleshooting

### Common Issues

#### Connection Timeout

```
Error: ETIMEDOUT
```

**Solution**: Increase timeout settings or check network connectivity

#### Certificate Error

```
Error: unable to verify the first certificate
```

**Solution**: Update your system's CA certificates

#### CORS Error

```
Access to fetch at 'https://testnet-api.self.app' from origin 'http://localhost:3000' has been blocked by CORS policy
```

**Solution**: Ensure your origin is allowed or use a proxy during development

## Migration Guide

### From Direct Node Connection

If you're currently connecting directly to testnet nodes:

```javascript
// Old way
const node = new SELFNode('http://testnet-node.self.app:26657');

// New way (via API Gateway)
const client = new SELFClient('https://testnet-api.self.app');
```

### Benefits of Migration

- ✅ Automatic load balancing
- ✅ Built-in rate limiting
- ✅ HTTPS encryption
- ✅ Better reliability
- ✅ Monitoring and metrics

## Support & Feedback

### Getting Help

- **Discord**: [#testnet-help](https://discord.gg/WdMdVpA4C8)
- **GitHub Issues**: [Report bugs](https://github.com/SELF-Technology/self-chain-public/issues)
- **Email**: testnet-support@self.app

### Providing Feedback

We value your feedback! Please report:
- Performance issues
- API inconsistencies
- Documentation improvements
- Feature requests

## Roadmap

### Current (Q1 2024)
- ✅ Basic API gateway deployment
- ✅ Rate limiting implementation
- 🔄 WebSocket support
- 🔄 Monitoring dashboard

### Next (Q2 2024)
- GraphQL endpoint
- Advanced query capabilities
- Batch transaction support
- Enhanced caching

### Future
- Multi-region deployment
- Custom rate limit tiers
- Advanced analytics
- SLA guarantees

---

*Remember: This is a testnet. Expect changes, improvements, and occasional downtime as we prepare for mainnet.*