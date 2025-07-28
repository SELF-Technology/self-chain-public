# SELF Chain Public Interfaces

This directory contains public type definitions and interfaces for interacting with the SELF blockchain. These interfaces are designed to be used by external developers building on the SELF ecosystem.

## Structure

- `types.rs` - Common types used across the API
- `transaction.rs` - Transaction structures and interfaces
- `block.rs` - Block structures and interfaces
- `account.rs` - Account and validator interfaces

## Usage

These interfaces are used by:
- The public REST API
- Client SDKs
- Third-party integrations
- DApp developers

## Important Note

These are public interfaces only. The actual implementation details, consensus mechanisms, and security-critical code are maintained in the private repository. This separation ensures security while allowing open development on the SELF platform.