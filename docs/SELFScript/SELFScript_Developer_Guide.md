---
layout: page
title: SELFScript Developer Guide
---

# SELFScript Developer Guide

Welcome to the SELFScript Developer Guide! This comprehensive guide will help you understand and develop with SELFScript, the native smart contract language of SELF Chain.

## Table of Contents
1. [Introduction to SELFScript](#introduction)
2. [Getting Started](#getting-started)
3. [Basic Syntax](#basic-syntax)
4. [Data Types](#data-types)
5. [Control Flow](#control-flow)
6. [Functions](#functions)
7. [Smart Contracts](#smart-contracts)
8. [Blockchain Operations](#blockchain-operations)
9. [Token Integration](#token-integration)
10. [Governance](#governance)
11. [Rosetta Integration](#rosetta-integration)
12. [Best Practices](#best-practices)
13. [Security Considerations](#security)
14. [Testing](#testing)
15. [Deployment](#deployment)

## Introduction to SELFScript <a name="introduction"></a>

SELFScript is a modern smart contract language designed specifically for SELF Chain. It builds upon the simplicity of SELFScript while adding powerful features for blockchain development.

### Key Features
- Type-safe language
- Modern control flow
- Built-in blockchain operations
- Token integration
- Governance support
- Rosetta compatibility
- Easy debugging
- Comprehensive tooling

## Getting Started <a name="getting-started"></a>

### Prerequisites
- Java Development Kit (JDK) 17+
- SELF SDK 1.0.0+
- IDE with SELFScript support

### First Contract
```selfscript
// Simple token transfer contract
contract SimpleTransfer {
    function transfer(address to, uint amount) {
        require(amount > 0, "Amount must be positive");
        let balance = token.balanceOf(msg.sender);
        require(balance >= amount, "Insufficient balance");
        token.transfer(to, amount);
    }
}
```

## Basic Syntax <a name="basic-syntax"></a>

### Comments
```selfscript
// Single line comment
/* Multi-line
   comment */
```

### Variables
```selfscript
// Declaration
let x: Number = 42
let name: String = "SELF"
let address: Hex = "0x1234..."

// Constants
const PI: Number = 3.14159
```

### Arrays and Maps
```selfscript
// Arrays
let numbers: Array<Number> = [1, 2, 3]
let mixed: Array<Any> = ["hello", 42, true]

// Maps
let user: Map<String, Any> = {
    "name": "John",
    "age": 30,
    "active": true
}
```

## Data Types <a name="data-types"></a>

### Basic Types
- `Number`: Numeric values
- `String`: Textual data
- `Boolean`: True/false values
- `Hex`: Hexadecimal values
- `Address`: Blockchain addresses

### Complex Types
- `Array<T>`: Ordered collection
- `Map<K,V>`: Key-value pairs
- `Tuple`: Fixed-size collection
- `Struct`: Custom data structures

## Control Flow <a name="control-flow"></a>

### Conditional Statements
```selfscript
if (condition) {
    // code block
} else if (another) {
    // another block
} else {
    // final block
}
```

### Loops
```selfscript
// For loop
for (let i = 0; i < 10; i++) {
    // code
}

// For each
for (item in array) {
    // code
}

// While loop
while (condition) {
    // code
}
```

## Functions <a name="functions"></a>

### Basic Functions
```selfscript
function add(a: Number, b: Number): Number {
    return a + b
}
```

### Async Functions
```selfscript
async function fetchBalance(address: Address): Number {
    return await token.balanceOf(address)
}
```

### Events
```selfscript
// Emit event
emit Transfer(from, to, amount)

// Event definition
contract MyContract {
    event Transfer(address from, address to, uint amount)
}
```

## Smart Contracts <a name="smart-contracts"></a>

### Contract Structure
```selfscript
contract MyContract {
    // State variables
    let owner: Address
    let balance: Number

    // Constructor
    constructor() {
        owner = msg.sender
    }

    // Functions
    function deposit(amount: Number) {
        require(amount > 0, "Amount must be positive")
        balance += amount
    }
}
```

### Inheritance
```selfscript
contract BaseContract {
    function baseFunction() {
        // base implementation
    }
}

contract DerivedContract inherits BaseContract {
    // can override baseFunction
}
```

## Blockchain Operations <a name="blockchain-operations"></a>

### Block Information
```selfscript
let blockNumber = blockchain.blockNumber
let timestamp = blockchain.timestamp
let difficulty = blockchain.difficulty
```

### Transaction Context
```selfscript
let sender = msg.sender
let value = msg.value
let data = msg.data
```

## Token Integration <a name="token-integration"></a>

### Token Operations
```selfscript
// Transfer tokens
function transfer(address to, uint amount) {
    token.transfer(to, amount)
}

// Check balance
let balance = token.balanceOf(address)

// Approve spending
function approve(address spender, uint amount) {
    token.approve(spender, amount)
}
```

## Governance <a name="governance"></a>

### Creating Proposals
```selfscript
function createProposal(title: String, description: String) {
    let proposal = governance.createProposal(title, description)
    return proposal.id
}
```

### Voting
```selfscript
function vote(proposalId: Number, approve: Boolean) {
    governance.vote(proposalId, approve)
}
```

## Rosetta Integration <a name="rosetta-integration"></a>

### Operations
```selfscript
// Create operation
let operation = rosetta.createOperation(
    type: "transfer",
    amount: "100",
    currency: "SELF"
)

// Submit transaction
let txHash = rosetta.submitTransaction(operation)
```

## Best Practices <a name="best-practices"></a>

### Security
1. Always validate inputs
2. Use require statements
3. Implement proper error handling
4. Follow gas optimization guidelines

### Code Organization
1. Keep contracts modular
2. Use meaningful variable names
3. Document functions and contracts
4. Follow style guidelines

## Security Considerations <a name="security"></a>

### Common Pitfalls
1. Reentrancy attacks
2. Integer overflow/underflow
3. Gas limitations
4. Timestamp manipulation

### Security Recommendations
1. Use SafeMath operations
2. Implement access control
3. Use proper error handling
4. Follow security best practices

## Testing <a name="testing"></a>

### Unit Testing
```selfscript
contract TestContract {
    function testTransfer() {
        let initialBalance = token.balanceOf(address)
        token.transfer(to, amount)
        assert(token.balanceOf(address) == initialBalance - amount)
    }
}
```

### Testing Tools
- SELFScript Test Framework
- Mock contracts
- Gas usage analysis
- Coverage reporting

## Deployment <a name="deployment"></a>

### Deployment Process
1. Compile contract
2. Generate deployment script
3. Sign transaction
4. Submit to network
5. Verify deployment

### Deployment Tools
- SELF CLI
- SELF Studio
- SELF Deployer
- Network Explorer

## Migration Guide

### From SELFScript
```selfscript
// SELFScript
let x = 42
// Becomes
let x: Number = 42
```

### From Solidity
```selfscript
// Solidity
contract MyContract {
    function transfer(address to, uint amount) public {
        // ...
    }
}
// Becomes
contract MyContract {
    function transfer(address to, uint amount) {
        // ...
    }
}
```

## Community Resources

### Documentation
- [SELFScript Language Specification](SELFSCRIPT_SPEC.md)
- [SELF SDK Documentation](../sdk/README.md)
- [SELF Chain Documentation](../README.md)

### Support
- Discord: #selfscript
- GitHub: Issues and Discussions
- Developer Forum: SELF Developer Community

### Learning Resources
- SELFScript Tutorial Series
- Example Contracts
- Best Practices Guide
- Security Audits

## Contributing

### Guidelines
1. Follow coding standards
2. Write tests
3. Document changes
4. Review security implications

### Process
1. Fork the repository
2. Create a feature branch
3. Submit a pull request
4. Review and merge

## License

SELFScript is licensed under the MIT License. See LICENSE file for details.

---

*Last updated: [DATE]*
