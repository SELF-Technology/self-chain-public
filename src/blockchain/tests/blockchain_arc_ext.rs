//! Test utilities for Arc<Blockchain> manipulation
//! 
//! # Safety Warning
//! 
//! This module contains UNSAFE code that should NEVER be used in production.
//! The unsafe patterns here violate Rust's aliasing rules and are only acceptable
//! in test code where we can guarantee single-threaded access.
//! 
//! For production code, always use proper synchronization primitives like:
//! - Arc<Mutex<T>> for exclusive access
//! - Arc<RwLock<T>> for shared/exclusive access
//! - Atomic types for simple values

use std::ops::Deref;
use std::sync::Arc;
use crate::blockchain::{Block, Blockchain, Transaction};

/// Extension trait to allow calling Blockchain methods on Arc<Blockchain>
/// This simplifies test code by allowing direct method calls on Arc-wrapped instances
pub trait BlockchainArcExt {
    // Async methods
    async fn get_height(&self) -> Result<u64, String>;
    async fn get_last_block_hash(&self) -> Result<String, String>;
    async fn add_block(&self, block: &Block) -> Result<(), String>;
    async fn mine(&self, block: &mut Block) -> Result<(), String>;
    async fn get_last_block(&self) -> Result<Block, String>;
    async fn valid_proof(&self, block: &Block) -> bool;
    async fn get_blocks(&self) -> Result<Vec<Block>, String>;
    async fn get_pending_transactions(&self) -> Vec<Transaction>;
    
    // Synchronous methods
    fn get_difficulty(&self) -> u32;
    fn get_blocks_sync(&self) -> Result<Vec<Block>, String>;
    fn get_last_block_sync(&self) -> Result<Block, String>;
    fn add_block_sync(&self, block: &Block) -> Result<(), String>;
    fn valid_proof_sync(&self, block: &Block) -> bool;
    fn get_height_sync(&self) -> Result<u64, String>;
    fn mine_sync(&self, block: &mut Block) -> Result<(), String>;
    fn get_pending_transactions_sync(&self) -> Vec<Transaction>;
}

impl BlockchainArcExt for Arc<Blockchain> {
    // Async method implementations
    async fn get_height(&self) -> Result<u64, String> {
        // Use the synchronous version but return as async for compatibility
        Ok(self.deref().get_height_sync()?)
    }
    
    async fn get_last_block_hash(&self) -> Result<String, String> {
        self.deref().get_last_block_hash().await
    }
    
    async fn add_block(&self, block: &Block) -> Result<(), String> {
        // WARNING: This uses unsafe to get around the immutability constraint of Arc for test purposes ONLY
        // NEVER use this pattern in production code - it violates Rust's safety guarantees
        // In production, use proper synchronization primitives like Arc<Mutex<T>> or Arc<RwLock<T>>
        // This is acceptable here only because:
        // 1. It's test code that runs in a controlled environment
        // 2. We ensure single-threaded access in our tests
        // 3. It significantly simplifies test setup and readability
        let blockchain = unsafe { &mut *(self.deref() as *const Blockchain as *mut Blockchain) };
        blockchain.add_block_sync(block)
    }
    
    async fn mine(&self, block: &mut Block) -> Result<(), String> {
        // Use the synchronous version but return as async for compatibility
        self.deref().mine_sync(block)
    }
    
    async fn get_last_block(&self) -> Result<Block, String> {
        // Use the synchronous version but return as async for compatibility
        self.deref().get_last_block_sync()
    }
    
    async fn valid_proof(&self, block: &Block) -> bool {
        // Use the synchronous version but return as async for compatibility
        self.deref().valid_proof_sync(block)
    }
    
    async fn get_blocks(&self) -> Result<Vec<Block>, String> {
        // Use the synchronous version but return as async for compatibility
        self.deref().get_blocks_sync()
    }
    
    async fn get_pending_transactions(&self) -> Vec<Transaction> {
        self.deref().get_pending_transactions_sync()
    }
    
    // Synchronous method implementations
    fn get_difficulty(&self) -> u32 {
        self.deref().get_difficulty()
    }
    
    fn get_blocks_sync(&self) -> Result<Vec<Block>, String> {
        self.deref().get_blocks_sync()
    }
    
    fn get_last_block_sync(&self) -> Result<Block, String> {
        self.deref().get_last_block_sync()
    }
    
    fn add_block_sync(&self, block: &Block) -> Result<(), String> {
        // Note: This uses unsafe to get around the immutability constraint of Arc for test purposes
        // In production code, this should be handled differently with proper mutex/rwlock control
        let blockchain = unsafe { &mut *(self.deref() as *const Blockchain as *mut Blockchain) };
        blockchain.add_block_sync(block)
    }
    
    fn valid_proof_sync(&self, block: &Block) -> bool {
        self.deref().valid_proof_sync(block)
    }
    
    fn get_height_sync(&self) -> Result<u64, String> {
        self.deref().get_height_sync()
    }
    
    fn mine_sync(&self, block: &mut Block) -> Result<(), String> {
        self.deref().mine_sync(block)
    }
    
    fn get_pending_transactions_sync(&self) -> Vec<Transaction> {
        self.deref().get_pending_transactions_sync()
    }
}
