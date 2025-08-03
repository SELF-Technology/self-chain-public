---
sidebar_label: "📚 SDK Reference"
sidebar_position: 6
---

# SDK Reference

This reference covers the planned SELF Chain SDKs for developers. The SDKs are currently in development and will provide access to core blockchain functionality once released.

:::warning Coming Q4 2025
The SDKs are currently in development and not yet available for download. This documentation shows the planned API structure. Release expected Q4 2025.
:::

## Planned SDKs

### TypeScript/JavaScript SDK

The TypeScript SDK will provide full access to SELF Chain functionality for web and Node.js applications.

#### Installation (Coming Q4 2025)

```bash
npm install @self-chain/sdk
```

#### Basic Usage

```typescript
import { SelfClient } from '@self-chain/sdk';

// Initialize client
const client = new SelfClient({
  endpoint: 'https://testnet-api.self.tech/v1',
  apiKey: 'your-api-key' // Optional for public endpoints
});

// Get blockchain status
const status = await client.getStatus();
console.log('Current height:', status.height);

// Get account information
const account = await client.getAccount('self1...');
console.log('Balance:', account.balance);

// Submit a transaction
const tx = await client.submitTransaction({
  from: 'sender-address',
  to: 'recipient-address',
  value: '1000000000000000000', // 1 SELF
  nonce: 1,
  signature: 'transaction-signature'
});
```

#### Available Methods

- `getStatus()` - Get current blockchain status
- `getBlock(hashOrHeight)` - Get block by hash or height
- `getTransaction(hash)` - Get transaction by hash
- `getAccount(address)` - Get account information
- `submitTransaction(tx)` - Submit a new transaction
- `getValidators()` - Get list of active validators

### Python SDK

The Python SDK will enable SELF Chain integration for AI and data science applications.

#### Installation (Coming Q4 2025)

```bash
pip install self-sdk
```

#### Basic Usage

```python
from self_sdk import SelfClient

# Initialize client
client = SelfClient(
    endpoint="https://testnet-api.self.tech/v1",
    api_key="your-api-key"  # Optional
)

# Get blockchain status
status = client.get_status()
print(f"Current height: {status['height']}")

# Get latest block
block = client.get_latest_block()
print(f"Latest block: {block['hash']}")

# Get account info
account = client.get_account("self1...")
print(f"Balance: {account['balance']}")
```

#### Available Methods

- `get_status()` - Get blockchain status
- `get_latest_block()` - Get the most recent block
- `get_block(block_id)` - Get specific block
- `get_account(address)` - Get account details
- `get_transaction(tx_hash)` - Get transaction details

## SDK Architecture

All SELF SDKs follow a consistent architecture:

```
SDK Client
    ├── HTTP/REST API Layer
    ├── WebSocket Support (real-time updates)
    ├── Type Definitions
    └── Error Handling
```

## Common Patterns

### Error Handling

All SDKs use consistent error types:

```typescript
try {
  const result = await client.getAccount(address);
} catch (error) {
  if (error.code === 'ACCOUNT_NOT_FOUND') {
    // Handle missing account
  } else if (error.code === 'NETWORK_ERROR') {
    // Handle network issues
  }
}
```

### Authentication

For testnet access:
- Public endpoints don't require authentication
- Private endpoints require an API key
- Mainnet will require full authentication

### Rate Limiting

Current testnet limits:
- 100 requests per minute for public endpoints
- 1000 requests per minute with API key

## Coming Soon

### Rust SDK
```toml
[dependencies]
self-sdk = "0.1.0"
```

### Go SDK
```go
import "github.com/self-chain/go-sdk"
```

Both SDKs are under development and will provide:
- Full blockchain interaction
- Native performance
- Type safety
- Comprehensive documentation

## Support

- **GitHub Issues**: [SELF-Technology/self-chain-public](https://github.com/SELF-Technology/self-chain-public/issues)
- **Discord**: [Join our developer community](https://discord.gg/WdMdVpA4C8)
- **Email**: devs@self.app

## Next Steps

1. Wait for SDK release (Q4 2025)
2. Get testnet access (coming soon)
3. Review the [Getting Started Guide](/building-on-self/getting-started)
4. Join our Discord for support

---

*Note: This documentation reflects the current state of SDK development. Features and APIs are subject to change as we approach mainnet launch.*