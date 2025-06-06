# SELFScript Language Specification

## Overview
SELFScript is the native smart contract language for SELF Chain, designed to be simple, secure, and powerful. It builds upon the foundation of SELFScript while adding modern features and SELF-specific capabilities.

## Core Features

### 1. Enhanced Data Types
```selfscript
// Basic types
let number: Number = 42
let hex: Hex = "0x1234"
let string: String = "Hello SELF"
let boolean: Boolean = true

// Complex types
let array: Array<Number> = [1, 2, 3]
let map: Map<String, Number> = { "one": 1, "two": 2 }
let struct: { name: String, age: Number } = { "name": "SELF", "age": 1 }
```

### 2. Modern Control Flow
```selfscript
// Enhanced if statements
if (condition) {
    // ...
} else if (another) {
    // ...
} else {
    // ...
}

// For loops with range
for (let i = 0; i < 10; i++) {
    // ...
}

// For each
for (item in array) {
    // ...
}

// While loops
while (condition) {
    // ...
}
```

### 3. SELF-Specific Features
```selfscript
// Blockchain operations
let blockNumber = blockchain.blockNumber
let timestamp = blockchain.timestamp
let txHash = transaction.hash

// Token operations
let balance = token.balanceOf(address)
let transfer = token.transfer(to, amount)

// Governance integration
let proposal = governance.createProposal(title, description)
let vote = governance.vote(proposalId, true)
```

### 4. Error Handling
```selfscript
try {
    // Potentially failing operation
} catch (error) {
    // Handle error
} finally {
    // Cleanup
}
```

### 5. Async Operations
```selfscript
// Async/await support
async function fetchBalance(address) {
    return await token.balanceOf(address)
}

// Promise handling
let promise = new Promise((resolve, reject) => {
    // ...
})
```

## Compatibility Layer

### 1. Solidity Compatibility
```selfscript
// Solidity-style contract definition
contract MyContract {
    function transfer(address to, uint amount) {
        // ...
    }
}

// Solidity-style events
emit Transfer(from, to, amount)
```

### 2. Rosetta Integration
```selfscript
// Rosetta-style operations
let operation = rosetta.createOperation(
    type: "transfer",
    amount: "100",
    currency: "SELF"
)
```

## Security Features

### 1. Type Safety
```selfscript
// Strong typing
let num: Number = 42 // Valid
let str: String = 42 // Error

// Type inference
let inferred = "hello" // Automatically typed as String
```

### 2. Memory Safety
```selfscript
// Automatic memory management
let array = [1, 2, 3] // Memory is managed automatically
```

### 3. Gas Optimization
```selfscript
// Gas-aware operations
let optimized = gasOptimize(operation) // Returns optimized version
```

## Development Tools

### 1. IDE Support
- Syntax highlighting
- Code completion
- Error detection
- Smart contract debugging

### 2. Testing Framework
```selfscript
// Built-in testing
contract TestContract {
    function testTransfer() {
        assert(balanceOf(address) == 100)
    }
}
```

### 3. Documentation Generation
```selfscript
/// @title My Contract
/// @author SELF Developer
contract MyContract {
    /// @notice Transfer tokens
    function transfer(address to, uint amount) {
        // ...
    }
}
```

## Migration Guide

### 1. From SELFScript
```selfscript
// SELFScript
let x = 42
// Becomes
let x: Number = 42
```

### 2. From Solidity
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

## Best Practices

### 1. Writing Secure Contracts
- Always validate inputs
- Use type safety
- Implement proper error handling
- Follow gas optimization guidelines

### 2. Testing
- Write comprehensive tests
- Test edge cases
- Use gas optimization tools
- Follow security best practices

## Future Roadmap

1. Enhanced security features
2. More language optimizations
3. Additional compatibility layers
4. Advanced development tools
5. Better documentation
6. Community extensions
