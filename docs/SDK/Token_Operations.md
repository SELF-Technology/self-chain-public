---
layout: page
title: Token Operations
---

# Token Operations Documentation

## Overview
The SELF SDK provides comprehensive token operations for SELF coins, SC20 tokens, and SC721 NFTs. This documentation covers the available operations and their usage.

## SELF Coins

### Key Features
- Wallet creation and management
- Balance tracking
- Coin transfers
- Transaction handling

### Usage Example
```rust
// Create a new wallet
let wallet = SELFCoinManager::new();

// Get balance
let balance = wallet.get_balance();

// Transfer coins
wallet.transfer_to("recipient_address", 100_000_000); // 100 SELF coins
```

## SC20 Tokens

### Key Features
- Token creation
- Token deployment
- Token transfers
- Balance tracking

### Usage Example
```rust
// Create a new SC20 token
let token = SELFTokenManager::new("MyToken", "MTK", 18);

// Deploy token
let contract_address = token.deploy();

// Transfer tokens
token.transfer_to("recipient_address", 1000_000_000_000_000_000); // 1 SC20 token
```

## SC721 NFTs

### Key Features
- NFT creation
- NFT transfers
- Metadata management
- Ownership verification

### Usage Example
```rust
// Create a new NFT
let nft = SELFTokenManager::create_nft("MyNFT", "MNFT", "https://ipfs.example.com/token-uri");

// Transfer NFT
nft.transfer_to("recipient_address");
```

## Best Practices

### Security
- Always validate recipient addresses
- Use proper error handling
- Implement rate limiting
- Secure private keys

### Performance
- Batch operations when possible
- Use proper caching
- Implement retry mechanisms
- Monitor gas usage

## Error Handling

### Common Errors
- Insufficient balance
- Invalid recipient address
- Network errors
- Contract errors

### Error Handling Example
```rust
match wallet.transfer_to("recipient_address", 100_000_000) {
    Ok(_) => println!("Transfer successful"),
    Err(e) => match e {
        SELFCoinError::InsufficientBalance => println!("Insufficient balance"),
        SELFCoinError::InvalidAddress => println!("Invalid recipient address"),
        SELFCoinError::NetworkError => println!("Network error occurred"),
        _ => println!("Unknown error: {}", e),
    }
}
```
