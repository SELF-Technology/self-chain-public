# SELF Chain SDK Structure (Testnet)

> ⚠️ **TESTNET ONLY**: These SDKs are for testnet use only. Do not use in production or with real assets.

## Repository Structure Plan

This document outlines the planned structure for SELF Chain SDKs. These will be separate repositories when released.

### Planned SDK Repositories

```
self-chain-sdk-js/          # JavaScript/TypeScript SDK
self-chain-sdk-rust/        # Rust SDK
self-chain-sdk-python/      # Python SDK
self-chain-sdk-go/          # Go SDK
```

## JavaScript/TypeScript SDK Structure

```
self-chain-sdk-js/
├── README.md               # ⚠️ TESTNET warnings prominent
├── package.json
├── tsconfig.json
├── src/
│   ├── index.ts
│   ├── client/
│   │   ├── SELFClient.ts   # Main client class
│   │   ├── testnet.ts      # Testnet-specific configuration
│   │   └── types.ts
│   ├── crypto/
│   │   ├── keys.ts         # Key generation (testnet only)
│   │   ├── signing.ts
│   │   └── hashing.ts
│   ├── transactions/
│   │   ├── builder.ts      # Transaction construction
│   │   ├── types.ts
│   │   └── validation.ts
│   ├── poai/               # PoAI interaction (limited in testnet)
│   │   ├── validator.ts
│   │   └── colorMarker.ts
│   └── utils/
│       ├── constants.ts    # TESTNET constants
│       └── errors.ts
├── examples/               # Testnet examples only
│   ├── 01-connect.ts      # Basic connection
│   ├── 02-transfer.ts     # Token transfer
│   ├── 03-query.ts        # Query blockchain
│   └── README.md          # ⚠️ Testnet warning
├── tests/
│   ├── unit/
│   └── integration/
└── docs/
    ├── getting-started.md  # Testnet focus
    └── api-reference.md
```

## Core SDK Features (Testnet Limited)

### 1. Client Connection
```typescript
// Example usage (TESTNET ONLY)
import { SELFClient } from '@self-chain/sdk';

const client = new SELFClient({
  network: 'testnet', // ONLY testnet supported
  endpoint: 'https://testnet-api.self.app',
  warning: true // Shows testnet warning on connection
});

// SDK will display warning:
// ⚠️ WARNING: Connected to SELF Chain TESTNET
// Do not use real assets or production data!
```

### 2. Transaction Building
```typescript
// All transactions include testnet marker
const tx = await client.transaction()
  .transfer({
    to: 'self1234...', // Testnet address
    amount: '100',     // TEST tokens only
    memo: 'Test transfer'
  })
  .sign(privateKey)    // Testnet key only
  .broadcast();

// Response includes testnet warning
console.log(tx.warning); // "This is a testnet transaction"
```

### 3. Safety Features

All SDKs will include:

```typescript
// Automatic testnet detection
if (isMainnetAddress(address)) {
  throw new Error('Mainnet address detected! Use testnet addresses only.');
}

// Transaction limits
const MAX_TESTNET_AMOUNT = 10000; // TEST tokens
if (amount > MAX_TESTNET_AMOUNT) {
  throw new Error('Amount exceeds testnet limits');
}

// Prominent warnings
console.warn('🚧 TESTNET TRANSACTION - NO REAL VALUE 🚧');
```

## Public Interfaces (Security-Safe)

### Exposed APIs

These interfaces can be safely made public:

```typescript
// Read-only blockchain queries
interface BlockchainQuery {
  getBlock(height: number): Promise<Block>;
  getTransaction(hash: string): Promise<Transaction>;
  getAccount(address: string): Promise<Account>;
  getValidators(): Promise<Validator[]>;
}

// Transaction construction (no security secrets)
interface TransactionBuilder {
  transfer(params: TransferParams): Transaction;
  delegate(params: DelegateParams): Transaction;
  undelegate(params: UndelegateParams): Transaction;
}

// Event subscription (read-only)
interface EventSubscription {
  onBlock(callback: BlockCallback): Unsubscribe;
  onTransaction(callback: TxCallback): Unsubscribe;
  onEvent(type: string, callback: EventCallback): Unsubscribe;
}
```

### Hidden/Restricted APIs

These remain in private repositories:

```typescript
// ❌ NOT exposed in public SDKs:
// - AI validation thresholds
// - Pattern matching algorithms
// - Consensus mechanism internals
// - Security-critical parameters
// - Production configurations
```

## SDK Development Guidelines

### 1. Testnet-First Design
- Every function assumes testnet
- Prominent warnings throughout
- Automatic mainnet detection and rejection
- Test token limits enforced

### 2. Security Boundaries
- No exposure of validation logic
- No consensus parameters
- No security thresholds
- Read-only blockchain access

### 3. Developer Experience
- Clear error messages with testnet context
- Comprehensive examples (testnet only)
- TypeScript types for safety
- Intuitive API design

## Example Applications Plan

### Basic Examples (Safe for Testnet)

1. **Hello SELF** - Basic connection and query
2. **Token Transfer** - Send TEST tokens
3. **Block Explorer** - Read blockchain data
4. **Event Watcher** - Subscribe to events

### Advanced Examples (Coming Later)

1. **Simple dApp** - Basic decentralized application
2. **NFT Minting** - Create test NFTs
3. **Governance Voting** - Participate in test governance
4. **Cross-Chain Bridge** - Test bridge functionality

## Release Timeline (Tentative)

### Phase 1: Core SDKs (Q1 2024)
- JavaScript/TypeScript SDK (testnet)
- Basic transaction support
- Read-only queries
- Event subscriptions

### Phase 2: Extended Features (Q2 2024)
- Python SDK
- Rust SDK
- Smart contract interaction
- Advanced queries

### Phase 3: Developer Tools (Q3 2024)
- Go SDK
- CLI improvements
- Testing frameworks
- Documentation expansion

## Contributing to SDKs

When SDKs are released, contributions welcome for:

- 🔧 Bug fixes
- 📚 Documentation improvements
- 🧪 Test coverage
- 🌍 Internationalization
- ♿ Accessibility features

NOT accepted:
- 🚫 Mainnet features (until ready)
- 🚫 Security bypass attempts
- 🚫 Consensus modifications
- 🚫 Production features

## Security Considerations

All SDK development follows:

1. **Principle of Least Privilege** - Only expose what's necessary
2. **Testnet Isolation** - Clear separation from mainnet
3. **Fail-Safe Defaults** - Conservative limits and checks
4. **Transparent Communication** - Clear about limitations

## Questions?

- Discord: [#sdk-development](https://discord.gg/selfcommunity)
- GitHub: Create an issue with [SDK] tag
- Email: sdk@self.app

---

⚠️ **Remember**: These SDKs are for TESTNET only. Never use with real assets!