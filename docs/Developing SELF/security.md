---
sidebar_position: 4
---

# Security Policy

## Reporting Security Vulnerabilities

We take the security of SELF seriously. If you believe you have found a security vulnerability in any SELF-owned repository, please report it to us as described below.

**Please do not report security vulnerabilities through public GitHub issues.**

Instead, please report them via email to devs@self.app. You should receive a response within 48 hours. If, for some reason you do not, please follow up via email to ensure we received your original message.

Please include the requested information listed below (as much as you can provide) to help us better understand the nature and scope of the possible issue:

- Type of issue (e.g. buffer overflow, SQL injection, cross-site scripting, etc.)
- Full paths of source file(s) related to the manifestation of the issue
- The location of the affected source code (tag/branch/commit or direct URL)
- Any special configuration required to reproduce the issue
- Step-by-step instructions to reproduce the issue
- Proof-of-concept or exploit code (if possible)
- Impact of the issue, including how an attacker might exploit the issue

This information will help us triage your report more quickly.

## Open Source Model

SELF follows a selective open source model inspired by Signal's approach to security. The SELF Chain project is licensed under the [MIT License](https://opensource.org/licenses/MIT).

### What We Keep Open Source
- **Core Protocol**: The fundamental blockchain and network protocols
- **Cryptographic Implementations**: All encryption and post-quantum cryptography
- **Client Applications**: Super-App interfaces and user-facing code
- **SDKs and APIs**: Developer tools and integration libraries

### What Remains Closed Source
Similar to how Signal keeps their anti-spam system private, we must depart from a totally-open posture for certain security-critical components:

- **AI Validation Rules**: The specific thresholds and patterns used in our Proof-of-AI consensus
- **Pattern Matching Algorithms**: Detection mechanisms that could be gamed if public
- **Security Thresholds**: Specific values that determine consensus and validation

**Why?** Unlike cryptographic protocols which benefit from public scrutiny, AI validation systems are vulnerable to gaming. If malicious actors know the exact patterns and thresholds we use to validate transactions and detect attacks, they can craft exploits to bypass our security. Transparency here would be a major disadvantage to the security of the network.

## Export Control Notice

This distribution includes cryptographic software. The country in which you currently reside may have restrictions on the import, possession, use, and/or re-export to another country, of encryption software. BEFORE using any encryption software, please check your country's laws, regulations and policies concerning the import, possession, or use, and re-export of encryption software, to see if this is permitted. See https://www.wassenaar.org/ for more information.

The U.S. Government Department of Commerce, Bureau of Industry and Security (BIS), has classified this software as Export Commodity Control Number (ECCN) 5D002.C.1, which includes information security software using or performing cryptographic functions with asymmetric algorithms. The form and manner of this distribution makes it eligible for export under the License Exception ENC Technology Software Unrestricted (TSU) exception (see the BIS Export Administration Regulations, Section 740.13) for both object code and source code.

## Security Architecture

### Core Security Components

1. **Blockchain Security**
   - Proof of AI (PoAI) consensus mechanism
   - Immutable blockchain structure
   - Secure transaction validation
   - Timestamp verification

2. **Storage Security**
   - IPFS content addressing
   - OrbitDB P2P synchronization
   - End-to-end encryption
   - Content integrity verification

3. **Network Security**
   - TLS/SSL encryption for all communications
   - Peer authentication
   - Rate limiting
   - DDoS protection
   - Connection timeouts

4. **AI Security**
   - Trusted Execution Environment (TEE) isolation
   - Model validation
   - Input sanitization
   - Output verification

5. **Post-Quantum Cryptography**
   - Kyber key encapsulation mechanism (KEM) implementation
   - SPHINCS+ quantum-resistant digital signatures
   - Hybrid cryptographic approach (classical + post-quantum)
   - Cryptographic agility framework for algorithm transitions

### Security Features

1. **Encryption**
   - AES-256 encryption for data at rest
   - TLS 1.3 for data in transit
   - Perfect Forward Secrecy (PFS)
   - Secure key management
   - Quantum-resistant key exchange protocols

2. **Access Control**
   - Role-based access control (RBAC)
   - Token-based authentication
   - Session management
   - Rate limiting

3. **Data Integrity**
   - SHA-256 hashing
   - SHA3-256 quantum-resistant hashing
   - Digital signatures (classical and post-quantum)
   - Merkle tree validation
   - Content addressable storage

4. **Audit Logging**
   - Immutable audit trails
   - Event logging
   - Access logging
   - Error logging

## Quantum Security Approach

The SELF Chain implements a forward-looking hybrid cryptographic approach that combines classical cryptographic algorithms with post-quantum resistant algorithms. This provides the security and performance benefits of well-established cryptographic methods while ensuring protection against future quantum computing threats.

### Key Quantum Security Components

1. **Hybrid Cryptography Implementation**
   - All sensitive data is protected using both classical (e.g., X25519, Ed25519) and post-quantum (e.g., Kyber, SPHINCS+) algorithms
   - Fallback mechanisms ensure continued operation if vulnerabilities are discovered in any single algorithm

2. **Algorithm Agility**
   - Modular design allows for rapid replacement of cryptographic primitives
   - Support for cryptographic algorithm negotiation during handshake
   - Regular security audits to assess the need for algorithm updates

3. **Post-Quantum Key Exchange**
   - Implementation of Kyber key encapsulation mechanism
   - Quantum-resistant shared secret establishment
   - Hybrid key exchange combining classical and post-quantum methods

4. **Quantum-Resistant Digital Signatures**
   - SPHINCS+ hash-based signature scheme implementation
   - Secure against both classical and quantum adversaries
   - Used for critical verification operations in the blockchain

## Reporting a Vulnerability

Please report security vulnerabilities by emailing devs@self.app as described at the top of this document.

### Responsible Disclosure

1. **Initial Contact**
   - Send a detailed vulnerability report
   - Include proof of concept if possible
   - Specify affected versions
   - Provide contact information

2. **Response Timeline**
   - Initial acknowledgment within 24 hours
   - Patch development within 7 days
   - Public disclosure after patch release

3. **Coordination**
   - Keep communication private
   - Provide updates on progress
   - Coordinate patch release timing
   - Acknowledge responsible disclosure

## Security Best Practices

### Development

1. **Code Review**
   - Mandatory security review for all code changes
   - Static code analysis
   - Dependency scanning
   - Regular security audits

2. **Testing**
   - Security-focused unit tests
   - Integration testing
   - Penetration testing
   - Fuzz testing

3. **Dependencies**
   - Regular dependency updates
   - Security vulnerability scanning
   - Minimal required permissions
   - Regular security audits

### Deployment

1. **Environment**
   - Secure infrastructure
   - Regular security updates
   - Monitoring and alerting
   - Backup procedures

2. **Configuration**
   - Secure defaults
   - Minimal permissions
   - Regular security reviews
   - Configuration validation

### Operations

1. **Monitoring**
   - Continuous monitoring
   - Alerting system
   - Log analysis
   - Regular security testing

2. **Maintenance**
   - Regular security updates
   - Backup procedures
   - Disaster recovery
   - Incident response procedures

3. **Compliance**
   - OWASP Top 10 compliance
   - Regular security audits
   - Security policy updates
   - Access control reviews

## Security Response

### Response Times
- Critical vulnerabilities: < 24 hours
- High severity: < 48 hours
- Medium severity: < 7 days
- Low severity: < 14 days

### Response Process
1. Initial triage and verification
2. Root cause analysis
3. Fix development
4. Testing and verification
5. Deployment
6. Public disclosure (if applicable)

## Security Contact Information

- Security Team: devs@self.app

## Legal Notice

By submitting a security vulnerability, you agree to:
1. Not publicly disclose the vulnerability until a patch is released
2. Not exploit the vulnerability for malicious purposes
3. Follow responsible disclosure guidelines
4. Provide accurate and complete information
5. Cooperate with our security team
