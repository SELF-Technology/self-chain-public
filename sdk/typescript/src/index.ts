export interface ClientConfig {
  endpoint: string;
  apiKey?: string;
  timeout?: number;
}

export interface BlockchainStatus {
  height: number;
  hash: string;
  time: string;
  network: string;
  version: string;
  peers: number;
  syncing: boolean;
}

export interface Block {
  height: number;
  hash: string;
  previousHash: string;
  timestamp: string;
  validator: string;
  transactions: string[];
  size: number;
}

export interface Transaction {
  hash: string;
  from: string;
  to: string;
  value: string;
  fee: string;
  nonce: number;
  data?: string;
  signature: string;
  blockHash?: string;
  blockHeight?: number;
  status: 'pending' | 'confirmed' | 'failed';
}

export interface TransactionRequest {
  from: string;
  to: string;
  value: string;
  fee?: string;
  nonce: number;
  data?: string;
  signature: string;
}

export interface Account {
  address: string;
  balance: string;
  nonce: number;
  isValidator: boolean;
  createdAt: string;
}

export interface Validator {
  address: string;
  stake: string;
  reputation: number;
  uptime: number;
  blocksProposed: number;
  isActive: boolean;
}

export class SelfClient {
  private endpoint: string;
  private apiKey?: string;
  private timeout: number;

  constructor(config: ClientConfig) {
    this.endpoint = config.endpoint.replace(/\/$/, '');
    this.apiKey = config.apiKey;
    this.timeout = config.timeout || 30000;
  }

  async getStatus(): Promise<BlockchainStatus> {
    // Implementation would make HTTP request to /status
    throw new Error('Not implemented');
  }

  async getBlock(hashOrHeight: string | number): Promise<Block> {
    // Implementation would make HTTP request to /blocks/{hashOrHeight}
    throw new Error('Not implemented');
  }

  async getTransaction(hash: string): Promise<Transaction> {
    // Implementation would make HTTP request to /transactions/{hash}
    throw new Error('Not implemented');
  }

  async getAccount(address: string): Promise<Account> {
    // Implementation would make HTTP request to /accounts/{address}
    throw new Error('Not implemented');
  }

  async submitTransaction(tx: TransactionRequest): Promise<{ hash: string; status: string }> {
    // Implementation would make HTTP POST to /transactions
    throw new Error('Not implemented');
  }

  async getValidators(): Promise<{ validators: Validator[]; total: number }> {
    // Implementation would make HTTP request to /validators
    throw new Error('Not implemented');
  }
}