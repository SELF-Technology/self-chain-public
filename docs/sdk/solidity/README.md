# Solidity Development Guide

Welcome to the SELF Chain Solidity development guide. This document provides comprehensive information about developing smart contracts on the SELF Chain platform.

## Getting Started

### Prerequisites

1. Solidity Compiler (solc)
2. Node.js (for development tools)
3. SELF Chain SDK
4. SELF Chain Testnet Access

### Installation

```bash
# Install Solidity compiler
npm install -g solc

# Install development tools
npm install -g truffle
```

## Contract Development

### Basic Contract Structure

```solidity
pragma solidity ^0.8.0;

contract MyContract {
    uint256 public someValue;
    
    constructor(uint256 initialValue) {
        someValue = initialValue;
    }
    
    function updateValue(uint256 newValue) public {
        someValue = newValue;
    }
}
```

### Best Practices

1. Use SafeMath for arithmetic operations
2. Implement proper access control
3. Use events for logging
4. Follow ERC standards
5. Implement proper error handling

## Token Development

### ERC20 Token

```solidity
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract SelfToken is ERC20, Ownable {
    constructor(uint256 initialSupply) ERC20("SelfToken", "SELF") {
        _mint(msg.sender, initialSupply);
    }

    function mint(address to, uint256 amount) public onlyOwner {
        _mint(to, amount);
    }

    function burn(uint256 amount) public {
        _burn(msg.sender, amount);
    }
}
```

### ERC721 Token (NFT)

```solidity
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract SelfNFT is ERC721, Ownable {
    uint256 private _tokenIds;

    constructor() ERC721("SelfNFT", "SELFNFT") {}

    function mintNFT(address recipient, string memory tokenURI)
        public
        onlyOwner
        returns (uint256)
    {
        _tokenIds++;
        uint256 newItemId = _tokenIds;
        _mint(recipient, newItemId);
        _setTokenURI(newItemId, tokenURI);
        return newItemId;
    }
}
```

## Contract Testing

### Testing Framework

```solidity
// Test contract
contract MyContractTest {
    MyContract private contractUnderTest;
    
    function setUp() public {
        contractUnderTest = new MyContract(100);
    }
    
    function testInitialValue() public {
        assertEq(contractUnderTest.someValue(), 100);
    }
}
```

### Testing Best Practices

1. Test all functions
2. Test edge cases
3. Test error conditions
4. Test gas usage
5. Test security vulnerabilities

## Deployment

### Local Development

```bash
# Compile contracts
solc contracts/MyContract.sol --bin --abi --optimize -o build/

# Deploy to local blockchain
npx truffle migrate --network development
```

### Testnet Deployment

```bash
# Deploy to testnet
npx truffle migrate --network testnet
```

## Security Considerations

### Common Vulnerabilities

1. Reentrancy attacks
2. Arithmetic over/underflows
3. Unchecked external calls
4. Gas limit issues
5. Timestamp manipulation

### Security Best Practices

1. Use SafeMath
2. Implement proper access control
3. Use events for logging
4. Follow ERC standards
5. Implement proper error handling

## Development Tools

### Truffle

```bash
# Create new project
truffle init

# Compile contracts
truffle compile

# Test contracts
truffle test

# Deploy contracts
truffle migrate
```

### Hardhat

```bash
# Create new project
npx hardhat

# Compile contracts
npx hardhat compile

# Test contracts
npx hardhat test

# Deploy contracts
npx hardhat run scripts/deploy.js
```

## Integration with SDK

### Using SDK with Solidity

```java
// Deploy Solidity contract
String contractCode = "pragma solidity ^0.8.0; ...";
ContractDeploymentResult result = sdk.contracts().deploySolidityContract(
    contractCode,
    "0.8.0"
);

// Call contract method
sdk.contracts().callMethod(
    result.getContractAddress(),
    "set",
    new Object[]{123}
);
```

## Token Creation Guide

### Creating a New Token

1. Choose token type (ERC20, ERC721)
2. Define token parameters
3. Implement token contract
4. Test token functionality
5. Deploy token contract
6. Verify deployment
7. Document token details

### Token Parameters

1. Token name
2. Token symbol
3. Initial supply
4. Decimals (for ERC20)
5. Minting permissions
6. Transfer restrictions
7. Burn functionality

## Best Practices

### Development

1. Use version control
2. Write comprehensive tests
3. Document your code
4. Follow coding standards
5. Implement proper error handling

### Security

1. Audit your code
2. Test thoroughly
3. Follow security guidelines
4. Implement proper access control
5. Monitor contract usage

## Support

For Solidity development support:
1. Check the documentation
2. Search existing issues
3. Join our developer community
4. Contact support@self.app

## Resources

### Documentation
- Solidity documentation
- SELF Chain API docs
- ERC standards
- Security guidelines

### Tools
- Truffle
- Hardhat
- Remix IDE
- OpenZeppelin

### Libraries
- OpenZeppelin Contracts
- SafeMath
- ERC standards
- Access Control

## License

The SELF Chain Solidity documentation is licensed under the Apache License 2.0. See LICENSE for details.
