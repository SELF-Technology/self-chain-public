import axios, { AxiosInstance } from 'axios';
import { EventEmitter } from 'events';
import WebSocket from 'ws';
import { 
  BlockchainStatus, 
  Transaction, 
  Block, 
  CreateTransactionOptions,
  SelfClientConfig,
  WebSocketEvent 
} from './types';

/**
 * SELF Chain SDK Client
 * Provides access to SELF Chain API endpoints and real-time updates
 */
export class SelfClient extends EventEmitter {
  private http: AxiosInstance;
  private ws?: WebSocket;
  private config: SelfClientConfig;

  constructor(config: SelfClientConfig) {
    super();
    this.config = config;
    
    // Initialize HTTP client
    this.http = axios.create({
      baseURL: config.apiUrl || 'https://api.self.app/v1',
      headers: {
        'Content-Type': 'application/json',
        ...(config.apiKey && { 'X-API-Key': config.apiKey })
      },
      timeout: config.timeout || 30000
    });

    // Initialize WebSocket if enabled
    if (config.enableWebSocket) {
      this.initWebSocket();
    }
  }

  /**
   * Initialize WebSocket connection for real-time updates
   */
  private initWebSocket(): void {
    const wsUrl = this.config.wsUrl || 'wss://ws.self.app/v1';
    this.ws = new WebSocket(wsUrl);

    this.ws.on('open', () => {
      this.emit('connected');
      if (this.config.apiKey) {
        this.ws?.send(JSON.stringify({
          type: 'auth',
          apiKey: this.config.apiKey
        }));
      }
    });

    this.ws.on('message', (data: WebSocket.Data) => {
      try {
        const event = JSON.parse(data.toString()) as WebSocketEvent;
        this.emit(event.type, event.data);
      } catch (error) {
        this.emit('error', error);
      }
    });

    this.ws.on('error', (error) => {
      this.emit('error', error);
    });

    this.ws.on('close', () => {
      this.emit('disconnected');
      // Attempt to reconnect after 5 seconds
      if (this.config.autoReconnect !== false) {
        setTimeout(() => this.initWebSocket(), 5000);
      }
    });
  }

  /**
   * Get current blockchain status
   */
  async getStatus(): Promise<BlockchainStatus> {
    const response = await this.http.get<BlockchainStatus>('/status');
    return response.data;
  }

  /**
   * Get a specific block by height or hash
   */
  async getBlock(heightOrHash: number | string): Promise<Block> {
    const response = await this.http.get<Block>(`/blocks/${heightOrHash}`);
    return response.data;
  }

  /**
   * Get the latest block
   */
  async getLatestBlock(): Promise<Block> {
    const response = await this.http.get<Block>('/blocks/latest');
    return response.data;
  }

  /**
   * Create a new transaction
   */
  async createTransaction(options: CreateTransactionOptions): Promise<Transaction> {
    const response = await this.http.post<Transaction>('/transactions', options);
    return response.data;
  }

  /**
   * Get a transaction by hash
   */
  async getTransaction(hash: string): Promise<Transaction> {
    const response = await this.http.get<Transaction>(`/transactions/${hash}`);
    return response.data;
  }

  /**
   * Subscribe to real-time block updates
   */
  subscribeToBlocks(callback: (block: Block) => void): () => void {
    this.on('block', callback);
    this.ws?.send(JSON.stringify({ type: 'subscribe', channel: 'blocks' }));
    
    return () => {
      this.removeListener('block', callback);
      this.ws?.send(JSON.stringify({ type: 'unsubscribe', channel: 'blocks' }));
    };
  }

  /**
   * Subscribe to real-time transaction updates
   */
  subscribeToTransactions(callback: (transaction: Transaction) => void): () => void {
    this.on('transaction', callback);
    this.ws?.send(JSON.stringify({ type: 'subscribe', channel: 'transactions' }));
    
    return () => {
      this.removeListener('transaction', callback);
      this.ws?.send(JSON.stringify({ type: 'unsubscribe', channel: 'transactions' }));
    };
  }

  /**
   * Close all connections
   */
  close(): void {
    if (this.ws) {
      this.ws.close();
      this.ws = undefined;
    }
    this.removeAllListeners();
  }
}