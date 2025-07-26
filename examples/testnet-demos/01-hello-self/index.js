#!/usr/bin/env node

/**
 * Hello SELF - Basic SELF Chain Testnet Connection Example
 * 
 * ⚠️ TESTNET ONLY - Do not use with real assets!
 * 
 * This example demonstrates:
 * - Connecting to SELF Chain testnet
 * - Querying basic blockchain information
 * - Handling testnet warnings properly
 */

// Simulated SDK imports (SDK not yet released)
// In real usage: import { SELFClient } from '@self/sdk';

class TestnetWarning {
  static display() {
    console.log('\n' + '='.repeat(60));
    console.log('⚠️  SELF CHAIN TESTNET WARNING ⚠️');
    console.log('='.repeat(60));
    console.log('You are connected to TESTNET. Important reminders:');
    console.log('• Testnet tokens have NO VALUE');
    console.log('• Network may be reset at any time');
    console.log('• Do not use real cryptocurrency');
    console.log('• This is for testing and learning only');
    console.log('='.repeat(60) + '\n');
  }
}

// Simulated client for demonstration
class MockSELFClient {
  constructor(config) {
    this.config = config;
    this.connected = false;
  }

  async connect() {
    console.log(`Connecting to ${this.config.network}...`);
    
    // Simulate connection delay
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // Safety check - reject mainnet
    if (this.config.network === 'mainnet') {
      throw new Error('🚫 Mainnet connection blocked! This example is for testnet only.');
    }
    
    this.connected = true;
    console.log('✅ Connected to SELF Chain Testnet!');
    console.log(`Endpoint: ${this.config.endpoint}`);
  }

  async getNetworkInfo() {
    if (!this.connected) throw new Error('Not connected');
    
    // Simulated network info
    return {
      network: 'testnet',
      chainId: 'self-testnet-001',
      blockHeight: 123456,
      blockTime: 2.1,
      validators: 10,
      consensusType: 'Proof-of-AI (PoAI)',
      warning: 'TESTNET - No real value'
    };
  }

  async getLatestBlock() {
    if (!this.connected) throw new Error('Not connected');
    
    // Simulated block data
    return {
      height: 123456,
      hash: '0xTESTNET7f9a8b7c6d5e4f3a2b1c...',
      timestamp: new Date().toISOString(),
      transactions: 5,
      validator: 'test-validator-001',
      aiScore: 0.98,
      warning: 'This is testnet data'
    };
  }

  async getTestnetFaucetInfo() {
    return {
      endpoint: 'https://faucet.testnet.self.app',
      dailyLimit: '100 TEST',
      cooldown: '24 hours',
      discord: 'Use !faucet command in Discord'
    };
  }
}

// Main example function
async function main() {
  try {
    // Display testnet warning first
    TestnetWarning.display();

    // Configuration (would come from environment in real app)
    const config = {
      network: 'testnet',
      endpoint: 'https://testnet-api.self.app',
      // Never put real keys here!
    };

    // Create client instance
    console.log('🚀 Initializing SELF Chain client...\n');
    const client = new MockSELFClient(config);

    // Connect to testnet
    await client.connect();

    // Get network information
    console.log('\n📊 Network Information:');
    const networkInfo = await client.getNetworkInfo();
    console.log(`• Chain ID: ${networkInfo.chainId}`);
    console.log(`• Consensus: ${networkInfo.consensusType}`);
    console.log(`• Block Height: ${networkInfo.blockHeight.toLocaleString()}`);
    console.log(`• Block Time: ${networkInfo.blockTime}s`);
    console.log(`• Active Validators: ${networkInfo.validators}`);

    // Get latest block
    console.log('\n📦 Latest Block:');
    const latestBlock = await client.getLatestBlock();
    console.log(`• Height: ${latestBlock.height.toLocaleString()}`);
    console.log(`• Hash: ${latestBlock.hash}`);
    console.log(`• Transactions: ${latestBlock.transactions}`);
    console.log(`• AI Validation Score: ${latestBlock.aiScore}`);
    console.log(`• Timestamp: ${latestBlock.timestamp}`);

    // Get faucet information
    console.log('\n💰 Testnet Faucet Information:');
    const faucetInfo = await client.getTestnetFaucetInfo();
    console.log(`• Daily Limit: ${faucetInfo.dailyLimit}`);
    console.log(`• Cooldown: ${faucetInfo.cooldown}`);
    console.log(`• How to get tokens: ${faucetInfo.discord}`);

    // Success message
    console.log('\n✨ Successfully connected to SELF Chain Testnet!');
    console.log('\nNext steps:');
    console.log('1. Join our Discord for testnet tokens');
    console.log('2. Try the token transfer example');
    console.log('3. Build something amazing (on testnet)!');

    // Final warning
    console.log('\n' + '⚠️  Remember: This is TESTNET only! '.repeat(2) + '\n');

  } catch (error) {
    console.error('\n❌ Error:', error.message);
    console.log('\nTroubleshooting:');
    console.log('• Check your internet connection');
    console.log('• Verify you\'re using testnet configuration');
    console.log('• Join Discord for help: https://discord.gg/WdMdVpA4C8');
  }
}

// Run the example
if (require.main === module) {
  main().catch(console.error);
}

module.exports = { MockSELFClient, TestnetWarning };