# SELF Chain Post-Quantum Cryptography

## Introduction

SELF Chain is implementing a comprehensive post-quantum cryptography (PQC) strategy to ensure long-term security against threats posed by quantum computing advancements. This document provides an overview of our approach to quantum-resistant security and current implementation status.

## Multi-Layered Defense Strategy

Our post-quantum security implementation leverages multiple complementary cryptographic approaches:

### 1. Quantum-Resistant Key Exchange (Kyber)

SELF Chain implements Kyber, a lattice-based key encapsulation mechanism (KEM) selected by NIST as the first standardized post-quantum cryptographic algorithm. Kyber provides:

- Quantum-resistant secure key exchange
- Strong security guarantees based on module learning with errors (MLWE) problem
- Excellent performance characteristics compared to other PQC candidates
- Well-analyzed security properties with conservative parameter selection

**Implementation Status:** Module structure created with interfaces for both Kyber-768 and Kyber-1024 variants, with Kyber-1024 as the default for maximum security margin.

### 2. Quantum-Resistant Signatures (SPHINCS+)

To complement Kyber's key exchange capabilities, SELF Chain implements SPHINCS+, a stateless hash-based signature scheme built upon:

- Winternitz One-Time Signatures (WOTS)
- Merkle tree authentication paths
- Purely hash-based security (no number-theoretic assumptions)
- Stateless design for practical blockchain implementation

**Implementation Status:** Module structure created with interfaces for SPHINCS+-SHA3-256 in both fast (larger signatures) and small (slower generation) parameter sets.

### 3. Hybrid Cryptographic Approach

During the transition period, SELF Chain employs a hybrid approach that combines:

- Classical cryptography (ECDSA with secp256k1, X25519) for backward compatibility and immediate security
- Post-quantum algorithms (Kyber + SPHINCS+) for forward security against quantum threats
- Versioned cryptographic operations for smooth transition

#### Hybrid Key Exchange

SELF Chain implements a hybrid key exchange mechanism combining X25519 (classical) with Kyber-1024 (post-quantum):

- Follows NIST recommendations for post-quantum transition
- Combines strengths of well-established classical and quantum-resistant algorithms
- Ensures security against both conventional and quantum adversaries
- Provides cryptographic agility through modular design

**Implementation Status:** 
- Hybrid X25519+Kyber key exchange fully implemented with proper encapsulation/decapsulation
- Hybrid signature scheme designed that combines ECDSA and SPHINCS+ signatures with unified verification protocol

## Implementation Architecture

SELF Chain's cryptographic implementation follows a modular architecture:

```
src/crypto/
├── classic/     # Classical cryptography (ECDSA, etc.)
├── quantum/     # Post-quantum algorithms (Kyber, SPHINCS+)
├── hybrid/      # Combined classical+quantum approaches
└── common/      # Shared traits and utilities
```

This architecture provides:

- Clean separation between cryptographic approaches
- Unified interfaces for all signature and key exchange operations
- Versioned algorithms for seamless upgrades
- Backward compatibility with existing blockchain transactions

## Implementation Timeline

The post-quantum security roadmap follows a phased approach:

1. **Phase 1** (Q3 2025): Module structure and Kyber integration ✓
2. **Phase 2** (Q4 2025): X25519+Kyber hybrid key exchange implementation ✓
3. **Phase 3** (Q4 2025): SPHINCS+ integration and hybrid signatures ◐
4. **Phase 4** (Q1 2026): Blockchain integration and performance optimizations ○
5. **Phase 5** (Q2 2026): Full network deployment and security hardening ○

_Legend: ✓ Complete, ◐ In Progress, ○ Planned_

### X25519 Implementation Enhancement Timeline

In addition to the main roadmap, we have a specific timeline to enhance the X25519 implementation:

1. **Q3 2025**: Interim solution for improved X25519 key exchange functionality ○
2. **Q4 2025**: Upstream contributions to X25519 libraries or custom implementation ○
3. **Q1 2026**: Final implementation of enhanced X25519 key exchange with proper deterministic behavior ○

## Security Benefits

This comprehensive post-quantum approach provides several key benefits:

1. **Long-term Security**: Protection against future quantum computing threats
2. **Defense in Depth**: Multiple cryptographic approaches with different security foundations
3. **Standardization Alignment**: Implementation of NIST-approved algorithms
4. **Future-proof Design**: Cryptographic agility for algorithm upgrades

## User Impact

The transition to post-quantum cryptography will be designed to minimize disruption:

1. **Phased Rollout**: Gradual introduction of post-quantum features
2. **Backward Compatibility**: Support for existing applications during transition
3. **Performance Considerations**: Optimizations to manage larger key and signature sizes

## Technical Considerations

While detailed implementation details remain in the private repository for security purposes, the approach includes:

1. **Cryptographic Agility**: Algorithm-agnostic interfaces for future upgrades
2. **Performance Optimization**: Techniques to minimize blockchain bloat from larger signatures and key material
3. **Secure Implementation**: Following best practices for cryptographic code and proper key material handling
4. **Integration Testing**: Comprehensive test suite for all cryptographic primitives
5. **Secure Key Management**: Proper zeroization of sensitive private key material
6. **Hybrid Design**: Careful composition of classical and post-quantum algorithms

## References

- NIST Post-Quantum Cryptography Standardization: https://csrc.nist.gov/Projects/post-quantum-cryptography/post-quantum-cryptography-standardization
- Kyber Algorithm Specification: https://pq-crystals.org/kyber/
- SPHINCS+ Algorithm Specification: https://sphincs.org/
- OpenQuantumSafe liboqs: https://openquantumsafe.org/liboqs/
