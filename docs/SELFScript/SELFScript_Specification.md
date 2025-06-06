# SELFScript Language Specification

## 1. Introduction

SELFScript is the native smart contract language for SELF Chain, designed to be simple, secure, and powerful. This specification defines the language grammar, syntax, and semantics.

## 2. Language Grammar

### 2.1 Lexical Structure

#### 2.1.1 Identifiers
- Case-sensitive
- Start with letter or underscore
- Followed by letters, digits, or underscores
- No reserved keywords allowed

#### 2.1.2 Literals
- Numbers: Integer and floating-point
- Strings: Double-quoted
- Hex: 0x prefix
- Boolean: true/false

### 2.2 Syntax

#### 2.2.1 Comments
```selfscript
// Single line comment
/* Multi-line
   comment */
```

#### 2.2.2 Variables
```selfscript
// Declaration
let variable: Type = value
const constant: Type = value

// Types
Number
String
Boolean
Hex
Address
```

#### 2.2.3 Control Flow
```selfscript
// If statement
if (condition) {
    // code
} else if (condition) {
    // code
} else {
    // code
}

// For loop
for (let i = 0; i < 10; i++) {
    // code
}

// While loop
while (condition) {
    // code
}
```

### 2.3 Functions

#### 2.3.1 Function Definition
```selfscript
function name(parameters): ReturnType {
    // function body
}
```

#### 2.3.2 Async Functions
```selfscript
async function name(parameters): ReturnType {
    // function body
}
```

#### 2.3.3 Events
```selfscript
event EventName(parameters)
emit EventName(value1, value2)
```

## 3. Type System

### 3.1 Basic Types

#### 3.1.1 Number
- Integer and floating-point
- Arithmetic operations
- SafeMath operations

#### 3.1.2 String
- Textual data
- String operations
- Pattern matching

#### 3.1.3 Boolean
- True/false values
- Logical operations

#### 3.1.4 Hex
- Hexadecimal values
- Byte operations
- Encoding/decoding

#### 3.1.5 Address
- Blockchain addresses
- Address validation
- Address operations

### 3.2 Complex Types

#### 3.2.1 Arrays
- Ordered collection
- Fixed-size arrays
- Dynamic arrays

#### 3.2.2 Maps
- Key-value pairs
- Hash maps
- Sorted maps

#### 3.2.3 Structs
- Custom data structures
- Struct inheritance
- Struct operations

## 4. Contract System

### 4.1 Contract Definition
```selfscript
contract Name {
    // state variables
    // functions
}
```

### 4.2 State Variables
- Persistent storage
- Visibility modifiers
- Storage layout

### 4.3 Functions
- Function modifiers
- Return values
- Function visibility

## 5. Blockchain Integration

### 5.1 Block Information
```selfscript
blockchain.blockNumber
blockchain.timestamp
blockchain.difficulty
```

### 5.2 Transaction Context
```selfscript
msg.sender
msg.value
msg.data
```

### 5.3 Events
- Event definition
- Event emission
- Event filtering

## 6. Token Integration

### 6.1 Token Operations
```selfscript
token.transfer(to, amount)
token.balanceOf(address)
token.approve(spender, amount)
```

### 6.2 Token Standards
- ERC-20 compatibility
- ERC-721 compatibility
- Custom token standards

## 7. Governance

### 7.1 Proposal System
```selfscript
governance.createProposal(title, description)
governance.vote(proposalId, approve)
```

### 7.2 Voting Mechanisms
- Weighted voting
- Quorum requirements
- Voting periods

## 8. Rosetta Integration

### 8.1 Operations
```selfscript
rosetta.createOperation(
    type: "transfer",
    amount: "100",
    currency: "SELF"
)
```

### 8.2 Transaction Handling
- Transaction creation
- Transaction signing
- Transaction submission

## 9. Security Features

### 9.1 Access Control
- Role-based access
- Function modifiers
- Permission management

### 9.2 Error Handling
- Require statements
- Error propagation
- Safe operations

### 9.3 Gas Optimization
- Gas-aware operations
- Memory management
- Storage optimization

## 10. Testing Framework

### 10.1 Unit Testing
- Test contracts
- Test functions
- Test assertions

### 10.2 Integration Testing
- Contract interactions
- Blockchain operations
- Token transfers

## 11. Deployment

### 11.1 Deployment Process
1. Contract compilation
2. Bytecode generation
3. Deployment transaction
4. Contract verification

### 11.2 Tools
- SELF CLI
- SELF Studio
- SELF Deployer

## 12. Migration Guide

### 12.1 From SELFScript
- Variable declarations
- Function definitions
- Control flow
- Blockchain operations

### 12.2 From Solidity
- Contract structure
- Function modifiers
- Event system
- Token standards

## 13. Best Practices

### 13.1 Security
1. Input validation
2. Error handling
3. Gas optimization
4. Code review

### 13.2 Code Organization
1. Modular contracts
2. Clear naming
3. Documentation
4. Testing

## 14. Future Extensions

### 14.1 Planned Features
- More complex data types
- Advanced control flow
- Enhanced blockchain operations
- Better tooling

### 14.2 Community Extensions
- Third-party libraries
- Custom token standards
- Development tools

## 15. References

### 15.1 Related Documents
- [SELFScript Developer Guide](SELFSCRIPT_DEVELOPER_GUIDE.md)
- [SELF SDK Documentation](../sdk/README.md)
- [SELF Chain Documentation](../README.md)

### 15.2 External Resources
- SELF Developers Forum
- SELF Discord
- SELF GitHub
- SELF Documentation Portal

---

*Last updated: [DATE]*
