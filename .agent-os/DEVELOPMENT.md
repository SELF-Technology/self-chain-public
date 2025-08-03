# SELF Chain Public - Development Workflow

> Agent OS development workflows for SELF Chain documentation and SDK development
> Generated: 2025-08-03

## Development Environment Setup

### Prerequisites
- **Node.js**: v18+ with npm/yarn
- **Python**: 3.8+ with pip
- **Rust**: Latest stable with Cargo
- **Go**: 1.19+ with modules
- **Git**: For version control

### Local Development Commands
```bash
# Install dependencies
npm install

# Start documentation development server
npm start

# Build documentation
npm run build

# Deploy to production
npm run deploy

# SDK Development
cd sdk/typescript && npm install
cd sdk/python && pip install -e .
cd sdk/rust && cargo build
cd sdk/go && go mod tidy
```

## Agent OS Development Workflows

### 1. Documentation Development Workflow

**Step 1: Create Specification**
```bash
/create-spec "Interactive code examples for SDK"
```
- Generates detailed documentation specification
- Plans interactive examples and tutorials
- Defines content structure and navigation
- Sets up testing requirements

**Step 2: Execute Implementation**
```bash
/execute-task "Implement TypeScript SDK documentation"
```
- Creates Docusaurus content pages
- Implements React components for examples
- Adds search and navigation features
- Includes comprehensive testing

**Step 3: Content Validation**
```bash
/test-integration "Documentation accuracy testing"
```
- Tests code examples for accuracy
- Validates navigation and search
- Checks performance and accessibility
- Verifies cross-browser compatibility

### 2. SDK Development Workflow

**TypeScript SDK Development**
```bash
/create-spec "TypeScript SDK client implementation"
/execute-task "Implement API client with TypeScript"
/test-integration "SDK functionality testing"
```

**Python SDK Development**
```bash
/create-spec "Python SDK async implementation"
/execute-task "Implement async client with Python"
/test-integration "Python SDK compatibility testing"
```

**Rust SDK Development**
```bash
/create-spec "Rust SDK safe implementation"
/execute-task "Implement safe Rust client"
/test-integration "Rust SDK performance testing"
```

**Go SDK Development**
```bash
/create-spec "Go SDK idiomatic implementation"
/execute-task "Implement idiomatic Go client"
/test-integration "Go SDK integration testing"
```

### 3. Cross-Component Coordination

**API Documentation Sync**
```bash
/coordinate "self-chain-public ↔ self-chain-private"
```
- Synchronizes API documentation
- Updates SDK interfaces
- Plans release coordination
- Creates testing strategies

**Developer Experience Alignment**
```bash
/sync-features "Developer onboarding workflow"
```
- Coordinates across all components
- Updates documentation
- Plans SDK releases
- Sets up monitoring

## Component-Specific Commands

### Documentation Development
```bash
/create-spec "Getting started tutorial"
/execute-task "Implement interactive examples"
/test-integration "Documentation accuracy validation"
```

### SDK Development
```bash
/create-spec "Multi-language SDK architecture"
/execute-task "Implement cross-language compatibility"
/test-integration "SDK integration testing"
```

### Smart Contract Development
```bash
/create-spec "Smart contract documentation"
/execute-task "Implement contract examples"
/test-integration "Contract security validation"
```

### Developer Resources
```bash
/create-spec "Developer onboarding workflow"
/execute-task "Implement tutorial system"
/test-integration "Developer experience testing"
```

## Quality Assurance Workflows

### Documentation Review Process
1. **Content Review**: Accuracy and completeness validation
2. **Technical Review**: Code example verification
3. **User Experience Review**: Navigation and search testing
4. **Performance Review**: Page load and search optimization

### SDK Testing Strategies
- **Unit Tests**: Individual function testing
- **Integration Tests**: Cross-language compatibility
- **Performance Tests**: Load and stress testing
- **Security Tests**: Vulnerability assessment

### Developer Experience Validation
- **Onboarding Testing**: Time to first success
- **Example Testing**: Code example execution
- **Search Testing**: Documentation search accuracy
- **Community Testing**: Developer feedback integration

## Deployment Workflows

### Documentation Deployment
```bash
/plan-deployment "Documentation website"
```
- Sets up Cloudflare Pages deployment
- Configures search and analytics
- Establishes monitoring and alerting
- Creates backup procedures

### SDK Deployment
```bash
/plan-deployment "Multi-language SDK release"
```
- Configures package distribution
- Sets up CI/CD pipelines
- Plans version management
- Establishes release procedures

### Cross-Component Deployment
```bash
/coordinate-deployment "Ecosystem documentation"
```
- Synchronizes documentation updates
- Plans SDK release coordination
- Coordinates marketing updates
- Sets up rollback procedures

## Monitoring and Maintenance

### Documentation Monitoring
- **Page Views**: Content popularity tracking
- **Search Analytics**: Query performance monitoring
- **User Feedback**: Developer satisfaction tracking
- **Performance Metrics**: Page load time optimization

### SDK Monitoring
- **Download Rates**: Package download tracking
- **Usage Analytics**: SDK adoption monitoring
- **Error Tracking**: Runtime error collection
- **Performance Metrics**: API response time tracking

### Developer Experience Monitoring
- **Onboarding Success**: Time to first integration
- **Community Growth**: Developer community metrics
- **Support Volume**: Help request tracking
- **Feature Adoption**: New feature usage tracking

## Development Standards

### Code Quality
- **TypeScript**: Strict type checking and documentation
- **Python**: PEP 8 compliance and type hints
- **Rust**: Memory safety and performance optimization
- **Go**: Idiomatic Go patterns and testing

### Documentation Standards
- **Comprehensive Coverage**: All features documented
- **Interactive Examples**: Live code examples
- **Performance Guidelines**: Best practices included
- **Security Considerations**: Security best practices

### Developer Experience
- **Easy Onboarding**: Quick start guides
- **Comprehensive Examples**: Real-world use cases
- **Error Handling**: Clear error messages
- **Performance Monitoring**: Built-in metrics

---

*This development workflow ensures world-class developer experience for the SELF ecosystem while maintaining comprehensive documentation and multi-language SDK support.* 