---
title: Public Interfaces
sidebar_position: 2
---

# SELF Chain Public Interfaces Specification

> 🚧 **BETA NOTICE**: These public interfaces are currently in beta and under active development. The specifications described here represent our target API design, but implementation is ongoing. Check back regularly for updates!

> This document defines the security-safe public interfaces that can be exposed in SDKs and public repositories without compromising SELF Chain's security.

## Overview

SELF Chain maintains strict security boundaries between public and private code. This specification defines what interfaces can be safely made public while protecting our core security mechanisms.

## Core Principles

1. **Read-Only First**: Public interfaces should primarily be read-only
2. **No Validation Logic**: Never expose AI validation algorithms or thresholds
3. **Abstract Consensus**: Hide internal consensus mechanisms behind abstractions
4. **Testnet Safe**: All interfaces must be safe for public testnet use
5. **Security by Design**: Default to restrictive, open carefully

## Public Interfaces

### 1. Blockchain Query Interface

**Purpose**: Allow reading blockchain state without exposing internals

```typescript
interface IBlockchainQuery {
  // Block queries
  getBlock(height: number): Promise<Block>;
  getBlockByHash(hash: string): Promise<Block>;
  getLatestBlock(): Promise<Block>;
  getBlockRange(start: number, end: number): Promise<Block[]>;
  
  // Transaction queries
  getTransaction(hash: string): Promise<Transaction>;
  getTransactionsByBlock(blockHeight: number): Promise<Transaction[]>;
  getTransactionsByAddress(address: string, limit?: number): Promise<Transaction[]>;
  
  // Account queries
  getAccount(address: string): Promise<Account>;
  getBalance(address: string): Promise<Balance>;
  getNonce(address: string): Promise<number>;
  
  // Network queries
  getNetworkInfo(): Promise<NetworkInfo>;
  getValidators(): Promise<ValidatorInfo[]>;
  getPeers(): Promise<PeerInfo[]>;
}
```

**Security Notes**:
- ✅ Read-only operations
- ✅ No exposure of validation logic
- ✅ Public blockchain data only

### 2. Transaction Builder Interface

**Purpose**: Construct transactions without exposing validation rules

```typescript
interface ITransactionBuilder {
  // Basic transactions
  transfer(params: TransferParams): UnsignedTransaction;
  delegate(params: DelegateParams): UnsignedTransaction;
  undelegate(params: UndelegateParams): UnsignedTransaction;
  
  // Transaction helpers
  estimateFee(tx: UnsignedTransaction): Promise<Fee>;
  validateAddress(address: string): boolean;
  generateAddress(): Address;
}

interface TransferParams {
  from: string;
  to: string;
  amount: string;
  memo?: string;
  // Note: No validation thresholds exposed
}
```

**Security Notes**:
- ✅ Transaction construction only
- ✅ Validation happens on-chain
- ✅ No consensus parameters exposed

### 3. Event Subscription Interface

**Purpose**: Subscribe to blockchain events in real-time

```typescript
interface IEventSubscription {
  // Block events
  onNewBlock(callback: (block: Block) => void): Subscription;
  onBlockFinalized(callback: (block: Block) => void): Subscription;
  
  // Transaction events
  onTransaction(filter: TxFilter, callback: (tx: Transaction) => void): Subscription;
  onTransactionConfirmed(txHash: string, callback: (tx: Transaction) => void): Subscription;
  
  // Network events
  onValidatorChange(callback: (validators: ValidatorInfo[]) => void): Subscription;
  onNetworkStatus(callback: (status: NetworkStatus) => void): Subscription;
}

interface Subscription {
  unsubscribe(): void;
  id: string;
}
```

**Security Notes**:
- ✅ Public event data only
- ✅ No internal consensus events
- ✅ Rate-limited subscriptions

### 4. Cryptographic Utilities Interface

**Purpose**: Provide crypto utilities without exposing secure operations

```typescript
interface ICryptoUtils {
  // Key generation (testnet safe)
  generateKeyPair(): KeyPair;
  generateMnemonic(): string;
  keyPairFromMnemonic(mnemonic: string): KeyPair;
  
  // Public key operations only
  publicKeyToAddress(publicKey: string): string;
  isValidPublicKey(publicKey: string): boolean;
  
  // Hashing (public algorithms only)
  sha256(data: Uint8Array): Uint8Array;
  keccak256(data: Uint8Array): Uint8Array;
  
  // Signature verification (not creation)
  verifySignature(message: Uint8Array, signature: Uint8Array, publicKey: string): boolean;
}
```

**Security Notes**:
- ✅ Public crypto operations only
- ✅ No private key operations in SDK
- ✅ Standard algorithms only

### 5. Smart Contract Interface (Future)

**Purpose**: Interact with smart contracts when available

```typescript
interface ISmartContract {
  // Deployment (testnet)
  deploy(bytecode: Uint8Array, params?: any[]): Promise<ContractAddress>;
  
  // Interaction
  call(address: string, method: string, params: any[]): Promise<any>;
  query(address: string, method: string, params: any[]): Promise<any>;
  
  // Events
  getContractEvents(address: string, filter?: EventFilter): Promise<Event[]>;
  subscribeToContract(address: string, callback: (event: Event) => void): Subscription;
}
```

**Security Notes**:
- ✅ Standard contract ABI only
- ✅ No privileged operations
- ✅ Testnet deployment only initially

## Restricted Interfaces (NOT Public)

These interfaces must NEVER be exposed in public code:

### ❌ AI Validation Interface
```typescript
// NEVER EXPOSE THIS
interface IAIValidation {
  validateTransaction(tx: Transaction): AIValidationResult;
  getValidationThreshold(): number;
  getPatternRules(): PatternRule[];
  trainModel(data: TrainingData): void;
}
```

### ❌ Consensus Internal Interface
```typescript
// NEVER EXPOSE THIS
interface IConsensusInternal {
  proposeBlock(transactions: Transaction[]): Block;
  voteOnBlock(block: Block): Vote;
  getConsensusParams(): ConsensusParameters;
  adjustDifficulty(): void;
}
```

### ❌ Security Critical Interface
```typescript
// NEVER EXPOSE THIS
interface ISecurityCritical {
  getSecurityThresholds(): SecurityThresholds;
  detectAnomalies(data: any): AnomalyReport;
  getPrivateKeys(): PrivateKey[];
  accessSecureStorage(): SecureStorage;
}
```

## Data Types (Public Safe)

### Block Structure
```typescript
interface Block {
  height: number;
  hash: string;
  previousHash: string;
  timestamp: number;
  transactions: string[]; // Transaction hashes only
  validator: string;
  // Note: No AI scores or validation details
}
```

### Transaction Structure
```typescript
interface Transaction {
  hash: string;
  from: string;
  to: string;
  amount: string;
  fee: string;
  nonce: number;
  timestamp: number;
  status: 'pending' | 'confirmed' | 'failed';
  // Note: No validation scores or AI details
}
```

### Network Information
```typescript
interface NetworkInfo {
  chainId: string;
  networkType: 'testnet' | 'mainnet';
  blockHeight: number;
  blockTime: number;
  activeValidators: number;
  // Note: No consensus parameters
}
```

## Implementation Guidelines

### For SDK Developers

1. **Always Check Network Type**
```typescript
if (network === 'mainnet' && !MAINNET_ENABLED) {
  throw new Error('Mainnet not yet available');
}
```

2. **Include Testnet Warnings**
```typescript
console.warn('⚠️ Connected to SELF Chain TESTNET - tokens have no value');
```

3. **Rate Limit Everything**
```typescript
const rateLimiter = new RateLimiter({
  maxRequests: 100,
  windowMs: 60000 // 1 minute
});
```

4. **Validate But Don't Expose**
```typescript
// Good: Validate on client side for UX
if (!isValidAddress(address)) {
  throw new Error('Invalid address format');
}

// Bad: Don't expose how validation works internally
// if (address.colorMarker !== calculateColorMarker(address)) { ... }
```

### For Core Developers

1. **Review All Public Interfaces**
   - Security review required for new interfaces
   - Document security implications
   - Test with adversarial mindset

2. **Maintain Abstraction Layers**
   - Public interfaces call private implementations
   - Never expose internal state
   - Use dependency injection

3. **Monitor Usage**
   - Log public API usage
   - Detect abnormal patterns
   - Have kill switches ready

## Version Management

### Interface Versioning
```typescript
interface VersionedAPI {
  version: '1.0.0';
  deprecated?: string[];
  experimental?: string[];
}
```

### Breaking Changes
- Announce 3 months in advance
- Provide migration guides
- Support old version for 6 months

## Security Checklist

Before making any interface public:

- [ ] No validation logic exposed
- [ ] No consensus parameters visible
- [ ] No security thresholds included
- [ ] Rate limiting implemented
- [ ] Testnet safety checks added
- [ ] Security review completed
- [ ] Documentation updated
- [ ] Examples provided

## Conclusion

These public interfaces provide a safe, useful API surface for developers while protecting SELF Chain's security-critical components. When in doubt, keep it private and consult the security team.

---

*"Security is not a feature, it's a design principle."* - SELF Chain Security Team