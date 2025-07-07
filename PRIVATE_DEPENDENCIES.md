# Private Repository Dependencies

This document outlines the components and functionality that depend on the private `self-chain-private` repository. The public repository provides the framework and interfaces, while the private repository contains proprietary implementations.

## Overview

The SELF Chain architecture is designed with a clear separation between public infrastructure and proprietary AI algorithms. This ensures:
- Open-source transparency for blockchain mechanics
- Protection of intellectual property for AI innovations
- Clear interfaces for community contributions
- Security through obscurity for critical validation logic

## Private Components

### 1. AI Validation Algorithms

**Location**: `self-chain-private/src/ai/validation/`

The core AI algorithms that power the PoAI consensus mechanism:
- **Pattern Recognition**: Advanced hex color pattern analysis
- **Anomaly Detection**: Identifies unusual transaction patterns
- **Efficiency Scoring**: Proprietary scoring algorithms beyond basic Input-Output calculation
- **Predictive Analysis**: Forecasts network behavior and optimization opportunities

**Public Interface**: `src/ai/service.rs::AIService`
```rust
// Public interface in self-chain-public
pub trait AIService {
    async fn validate_block(&self, block: &Block) -> Result<f64>;
    async fn validate_transaction(&self, tx: &Transaction) -> Result<bool>;
    async fn generate_reference_block(&self, current: &Block) -> Result<Block>;
}
```

### 2. Constellation API Integration

**Location**: `self-chain-private/src/integrations/constellation/`

Integration with external AI validation services:
- Industry-specific validation rules
- External oracle connections
- Third-party AI model interfaces
- Proprietary data sources

**Public Placeholder**: `src/ai/constellation.rs` (references only)

### 3. Advanced Voting Algorithms

**Location**: `self-chain-private/src/consensus/voting/`

Sophisticated voting weight calculations:
- Dynamic weight adjustment based on validator history
- Reputation-based influence scoring
- Network topology optimization
- Anti-gaming mechanisms

**Public Interface**: Uses standard `VotingSystem` but with enhanced scoring

### 4. Security Implementations

**Location**: `self-chain-private/src/security/`

Critical security features:
- Advanced Sybil attack detection
- Pattern-based fraud detection
- Validator behavior analysis
- Network partition detection algorithms

### 5. Optimization Engine

**Location**: `self-chain-private/src/optimization/`

Network optimization algorithms:
- Transaction routing optimization
- Block size dynamic adjustment
- Fee market algorithms
- Network congestion prediction

## Integration Points

### Environment Variables

The following environment variables control private component integration:

```bash
# Enable private AI service (defaults to placeholder in public repo)
ENABLE_PRIVATE_AI=true

# Private repository path for development
PRIVATE_REPO_PATH=/path/to/self-chain-private

# Constellation API endpoint
CONSTELLATION_API_URL=https://api.constellation.self.xyz

# Private component configuration
AI_MODEL_PATH=/path/to/models
```

### Build Configuration

When building with private components:

```toml
# Cargo.toml with private dependencies
[dependencies]
self-chain-private = { path = "../self-chain-private", optional = true }

[features]
private = ["self-chain-private"]
```

### Runtime Detection

The system automatically detects available components:

```rust
// Runtime detection in public code
if cfg!(feature = "private") {
    // Use private implementation
    let ai_service = self_chain_private::ai::AdvancedAIService::new();
} else {
    // Use public placeholder
    let ai_service = crate::ai::service::BasicAIService::new();
}
```

## Development Guidelines

### For Public Repository Contributors

1. **Use Interfaces**: Always work against trait definitions, not concrete implementations
2. **Add Placeholders**: Provide basic implementations that return reasonable defaults
3. **Document Behavior**: Clearly specify expected behavior for private implementations
4. **Test with Mocks**: Use mock implementations that simulate private component behavior

### For Private Repository Access

Private repository access is limited to:
- Core development team members
- Strategic partners under NDA
- Security auditors under contract

To request access:
1. Contact the development team
2. Sign necessary legal agreements
3. Undergo security clearance
4. Receive access credentials

## Component Status

| Component | Public Status | Private Required | Notes |
|-----------|--------------|------------------|-------|
| Block Validation | Framework ✓ | AI Logic | Basic validation works without private |
| Voting System | Complete ✓ | Enhanced Scoring | Functional with basic scoring |
| Reward Distribution | Complete ✓ | No | Fully implemented in public |
| Efficiency Calculation | Complete ✓ | Advanced Metrics | Basic Input-Output works |
| Builder Rotation | Complete ✓ | No | Fully implemented in public |
| Color Validation | Complete ✓ | Pattern Analysis | Basic hex validation works |
| Network Communication | Complete ✓ | No | Fully implemented in public |

## Migration Path

For transitioning from public to private components:

1. **Development**: Use public placeholders
2. **Testing**: Integrate private components in test environment
3. **Staging**: Full private component testing
4. **Production**: Deploy with private components enabled

## Security Considerations

- Private components undergo separate security audits
- Public interfaces are designed to prevent information leakage
- Private algorithms are obfuscated and encrypted
- Access logs are maintained for all private component usage

## Support

For questions about private components:
- Public discussions: GitHub Issues (without revealing private details)
- Private inquiries: dev@self.technology
- Security concerns: security@self.technology