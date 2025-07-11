# Developer Integration Guide

## Integrating with SELF Chain Cloud Infrastructure

This guide explains how developers can integrate with SELF Chain's automatic cloud provisioning system to create applications that leverage our user-sovereign infrastructure.

## Overview

When a user signs up for SELF through your application, our infrastructure automatically:
1. Provisions isolated cloud resources
2. Deploys a SELF Chain node
3. Sets up a private LLM instance
4. Configures decentralized storage
5. Returns API endpoints for interaction

## Authentication

### API Keys
Request API access by contacting our developer relations team. You'll receive:
- `CLIENT_ID`: Your application identifier
- `CLIENT_SECRET`: Your application secret
- `WEBHOOK_SECRET`: For verifying webhook signatures

### OAuth 2.0 Flow
For user-authorized actions, implement OAuth 2.0:

```javascript
// 1. Redirect user to authorization
const authUrl = `https://auth.self.app/oauth/authorize?
  client_id=${CLIENT_ID}&
  redirect_uri=${REDIRECT_URI}&
  response_type=code&
  scope=provision:create provision:read`;

// 2. Exchange code for token
const token = await fetch('https://auth.self.app/oauth/token', {
  method: 'POST',
  body: JSON.stringify({
    grant_type: 'authorization_code',
    code: authorizationCode,
    client_id: CLIENT_ID,
    client_secret: CLIENT_SECRET
  })
});
```

## Provisioning API

### Create User Instance

```bash
POST https://api.self.app/v1/provision
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "userId": "unique-user-id",
  "email": "user@example.com",
  "tier": "free",
  "preferences": {
    "aiModel": "default",
    "region": "us-east"
  }
}
```

**Response:**
```json
{
  "success": true,
  "instanceId": "inst_abc123xyz",
  "userId": "unique-user-id",
  "status": "provisioning",
  "endpoints": {
    "api": "https://user-abc123.api.self.app",
    "websocket": "wss://user-abc123.ws.self.app",
    "ai": "https://user-abc123.ai.self.app"
  },
  "credentials": {
    "apiKey": "sk_user_...",
    "nodeId": "node_..."
  }
}
```

### Check Instance Status

```bash
GET https://api.self.app/v1/provision/{instanceId}
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Response:**
```json
{
  "instanceId": "inst_abc123xyz",
  "status": "active",
  "health": "healthy",
  "resources": {
    "cpu": "0.5 vCPU",
    "memory": "1GB",
    "storage": "10GB"
  },
  "usage": {
    "shinePercent": 45,
    "cpuPercent": 23,
    "memoryPercent": 67,
    "storageUsed": "3.2GB"
  }
}
```

### Upgrade Instance

```bash
POST https://api.self.app/v1/provision/{instanceId}/upgrade
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "tier": "growth",
  "immediate": true
}
```

## Webhooks

Configure webhook endpoints to receive real-time updates:

### Webhook Events

#### `instance.created`
Fired when provisioning completes successfully.

```json
{
  "event": "instance.created",
  "timestamp": "2025-07-11T10:30:00Z",
  "data": {
    "instanceId": "inst_abc123xyz",
    "userId": "unique-user-id",
    "endpoints": {...}
  }
}
```

#### `instance.upgraded`
Fired when a tier upgrade completes.

```json
{
  "event": "instance.upgraded",
  "timestamp": "2025-07-11T11:00:00Z",
  "data": {
    "instanceId": "inst_abc123xyz",
    "previousTier": "free",
    "newTier": "growth"
  }
}
```

#### `instance.usage_alert`
Fired when usage exceeds thresholds.

```json
{
  "event": "instance.usage_alert",
  "timestamp": "2025-07-11T12:00:00Z",
  "data": {
    "instanceId": "inst_abc123xyz",
    "alert": "high_memory_usage",
    "current": 85,
    "threshold": 80
  }
}
```

### Webhook Security

Verify webhook signatures to ensure authenticity:

```javascript
const crypto = require('crypto');

function verifyWebhook(payload, signature, secret) {
  const expectedSignature = crypto
    .createHmac('sha256', secret)
    .update(payload)
    .digest('hex');
  
  return crypto.timingSafeEqual(
    Buffer.from(signature),
    Buffer.from(expectedSignature)
  );
}
```

## User Instance API

Once provisioned, interact with user instances directly:

### AI Chat API

```bash
POST https://user-abc123.ai.self.app/v1/chat
Authorization: Bearer USER_API_KEY
Content-Type: application/json

{
  "messages": [
    {"role": "user", "content": "Hello, how are you?"}
  ],
  "temperature": 0.7,
  "max_tokens": 150
}
```

### Node API

```bash
GET https://user-abc123.api.self.app/v1/node/info
Authorization: Bearer USER_API_KEY
```

### Storage API

```bash
POST https://user-abc123.api.self.app/v1/storage/upload
Authorization: Bearer USER_API_KEY
Content-Type: multipart/form-data

[Binary data]
```

## SDKs

### JavaScript/TypeScript

```bash
npm install @selfchain/cloud-sdk
```

```javascript
import { SelfChainCloud } from '@selfchain/cloud-sdk';

const client = new SelfChainCloud({
  clientId: CLIENT_ID,
  clientSecret: CLIENT_SECRET
});

// Provision new instance
const instance = await client.provision({
  userId: 'user123',
  tier: 'free'
});

// Check status
const status = await client.getInstanceStatus(instance.instanceId);
```

### Python

```bash
pip install selfchain-cloud
```

```python
from selfchain_cloud import Client

client = Client(
    client_id=CLIENT_ID,
    client_secret=CLIENT_SECRET
)

# Provision new instance
instance = client.provision(
    user_id='user123',
    tier='free'
)

# Check status
status = client.get_instance_status(instance.instance_id)
```

## Rate Limits

| Endpoint | Rate Limit |
|----------|------------|
| Provisioning | 100 requests/hour |
| Status checks | 1000 requests/hour |
| Upgrades | 10 requests/hour |
| User APIs | Based on tier |

## Error Handling

### Error Response Format

```json
{
  "error": {
    "code": "insufficient_resources",
    "message": "Unable to provision instance due to resource constraints",
    "details": {
      "region": "us-east",
      "tier": "free"
    }
  }
}
```

### Common Error Codes

| Code | Description | Action |
|------|-------------|--------|
| `invalid_tier` | Requested tier doesn't exist | Check available tiers |
| `insufficient_resources` | No resources available | Try different region |
| `payment_required` | Payment method needed | Add payment method |
| `rate_limited` | Too many requests | Implement backoff |
| `invalid_credentials` | Auth failed | Check API keys |

## Best Practices

1. **Implement Exponential Backoff**: For retries on failures
2. **Cache Instance Status**: Don't poll excessively
3. **Handle Webhooks Asynchronously**: Process events in background
4. **Monitor Usage**: Set up alerts for high usage
5. **Implement Graceful Degradation**: Handle provisioning delays

## Testing

### Sandbox Environment

Test your integration without provisioning real resources:

- Base URL: `https://sandbox.api.self.app`
- Test credentials provided upon request
- Simulated delays and failures for testing
- No charges for sandbox usage

### Test Credit Cards

For testing paid tiers in sandbox:
- Success: `4242 4242 4242 4242`
- Decline: `4000 0000 0000 0002`
- Insufficient funds: `4000 0000 0000 9995`

## Support

- **Documentation**: https://docs.self.app
- **API Status**: https://status.self.app
- **Developer Discord**: https://discord.gg/selfchain
- **Email**: developers@self.app

---

*Note: This guide covers the public API for integrating with SELF Chain's cloud infrastructure. Implementation details and internal APIs are subject to change. Always refer to the latest documentation.*