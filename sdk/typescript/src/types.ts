/**
 * SELF Chain TypeScript SDK Type Definitions
 */

export interface SelfClientConfig {
  apiUrl?: string;
  wsUrl?: string;
  apiKey?: string;
  timeout?: number;
  enableWebSocket?: boolean;
  autoReconnect?: boolean;
}

export interface BlockchainStatus {
  height: number;
  hash: string;
  chainId: string;
  network: 'mainnet' | 'testnet' | 'devnet';
  nodeVersion: string;
  peers: number;
  syncStatus: {
    syncing: boolean;
    currentBlock: number;
    targetBlock: number;
  };
}

export interface Block {
  height: number;
  hash: string;
  previousHash: string;
  timestamp: number;
  transactions: string[];
  validator: string;
  signature: string;
  colorMarker?: ColorMarker;
}

export interface Transaction {
  hash: string;
  from: string;
  to: string;
  value: string;
  data?: string;
  nonce: number;
  timestamp: number;
  blockHeight?: number;
  status: 'pending' | 'confirmed' | 'failed';
  gasUsed?: string;
}

export interface CreateTransactionOptions {
  to: string;
  value: string;
  data?: string;
  privateKey?: string; // For client-side signing
}

export interface ColorMarker {
  red: number;
  green: number;
  blue: number;
  confidence: number;
}

export interface WebSocketEvent {
  type: 'block' | 'transaction' | 'status' | 'error';
  data: any;
}

// MCP Integration Types (Future)
export interface MCPContext {
  contextId: string;
  appId: string;
  permissions: string[];
  metadata: Record<string, any>;
}

export interface MCPRequest {
  context: MCPContext;
  action: string;
  params: Record<string, any>;
}

export interface MCPResponse {
  success: boolean;
  data?: any;
  error?: {
    code: string;
    message: string;
  };
}