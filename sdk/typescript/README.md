# SELF Chain TypeScript SDK

Official TypeScript SDK for interacting with the SELF blockchain.

## Installation

```bash
npm install @self-chain/sdk
```

## Quick Start

```typescript
import { SelfClient } from '@self-chain/sdk';

// Initialize client
const client = new SelfClient({
  endpoint: 'https://api.self.tech/v1',
  apiKey: 'your-api-key'
});

// Get blockchain status
const status = await client.getStatus();
console.log('Current height:', status.height);

// Get account balance
const account = await client.getAccount('your-address');
console.log('Balance:', account.balance);

// Submit transaction
const tx = await client.submitTransaction({
  from: 'sender-address',
  to: 'recipient-address',
  value: '1000000000000000000', // 1 SELF
  nonce: 1,
  signature: 'transaction-signature'
});
console.log('Transaction hash:', tx.hash);
```

## API Reference

### Client Initialization

```typescript
const client = new SelfClient({
  endpoint: string,    // API endpoint
  apiKey?: string,     // Optional API key
  timeout?: number     // Request timeout in ms
});
```

### Methods

#### getStatus()
Returns current blockchain status.

#### getBlock(hashOrHeight: string | number)
Get block by hash or height.

#### getTransaction(hash: string)
Get transaction by hash.

#### getAccount(address: string)
Get account information.

#### submitTransaction(tx: TransactionRequest)
Submit a new transaction.

#### getValidators()
Get list of active validators.

## Development

```bash
# Install dependencies
npm install

# Build
npm run build

# Run tests
npm test

# Lint
npm run lint
```

## License

MIT License - see LICENSE file for details.