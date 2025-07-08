# Contributing to SELF Chain

Welcome to SELF Chain! We're excited that you're interested in contributing to our open-source blockchain ecosystem. This document provides guidelines and instructions for contributing to SELF Chain.

## 🌟 Our Open-Source Philosophy

SELF Chain embraces the Linux/open-source ethos. We believe in:
- **Transparency**: Open development process and clear communication
- **Meritocracy**: Contributions speak louder than titles
- **Collaboration**: Building together is stronger than building alone
- **Security**: Protecting users while enabling innovation
- **Accessibility**: Making blockchain technology available to everyone

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Contribution Areas](#contribution-areas)
- [Security Considerations](#security-considerations)
- [Pull Request Process](#pull-request-process)
- [Community](#community)

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md). We are committed to providing a welcoming and inclusive environment for all contributors.

## Getting Started

1. **Join our Community**
   - Discord: [Join SELF Community](https://discord.gg/WdMdVpA4C8)
   - Follow us on [Twitter/X](https://x.com/self_hq)
   - Star and watch this repository

2. **Understand the Project**
   - Read our [documentation](https://docs.self.app)
   - Review the [architecture overview](docs/Architecture/SELF_Chain_Architecture.md)
   - Understand [Proof-of-AI consensus](docs/Technical%20Docs/PoAI/Proof-of-AI.md)

3. **Find Your First Issue**
   - Look for issues labeled `good-first-issue`
   - Check `help-wanted` labels for areas needing assistance
   - Join Discord to discuss ideas before starting major work

## How to Contribute

### 🐛 Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates.

**To report a bug:**
1. Use the [Bug Report template](.github/ISSUE_TEMPLATE/bug_report.md)
2. Include detailed steps to reproduce
3. Provide system information
4. Add relevant logs or screenshots

### 💡 Suggesting Features

We love new ideas! To suggest a feature:
1. Use the [Feature Request template](.github/ISSUE_TEMPLATE/feature_request.md)
2. Explain the problem it solves
3. Describe your proposed solution
4. Consider alternatives you've thought about

### 📝 Improving Documentation

Documentation contributions are highly valued! You can:
- Fix typos or clarify existing docs
- Add examples and tutorials
- Translate documentation
- Create diagrams or visualizations

### 🔧 Contributing Code

1. **Fork the Repository**
   ```bash
   git clone https://github.com/SELF-Technology/self-chain-public.git
   cd self-chain-public
   git remote add upstream https://github.com/SELF-Technology/self-chain-public.git
   ```

2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make Your Changes**
   - Follow our [Code Style Guide](docs/Development/Code_Style_Guide.md)
   - Add tests for new functionality
   - Update documentation as needed

4. **Commit Your Changes**
   ```bash
   git add .
   git commit -m "feat: add awesome feature"
   ```
   We use [Conventional Commits](https://www.conventionalcommits.org/).

5. **Push and Create PR**
   ```bash
   git push origin feature/your-feature-name
   ```
   Then create a Pull Request on GitHub.

## Development Setup

### Prerequisites

- Rust 1.70+ (for core blockchain)
- Node.js 18+ (for SDK and tools)
- Docker (for local development)
- Git

### Local Development

1. **Clone and Install**
   ```bash
   git clone https://github.com/SELF-Technology/self-chain-public.git
   cd self-chain-public
   # Follow specific setup instructions in each component's README
   ```

2. **Run Tests**
   ```bash
   # For Rust components
   cargo test
   
   # For JavaScript/TypeScript
   npm test
   ```

3. **Build Documentation**
   ```bash
   cd docs
   npm install
   npm run build
   ```

## Contribution Areas

### ✅ Open for Contributions

These areas welcome community contributions:

- **Documentation**
  - Tutorials and guides
  - API documentation
  - Example applications
  - Translations

- **Developer Tools**
  - SDKs in various languages
  - CLI tools
  - IDE integrations
  - Testing frameworks

- **UI/UX Components**
  - Dashboard improvements
  - Visualization tools
  - Mobile interfaces
  - Accessibility enhancements

- **Integrations**
  - Exchange integrations
  - Wallet support
  - Third-party service connectors
  - Blockchain bridges (non-security critical)

- **Performance**
  - Benchmarking tools
  - Optimization suggestions
  - Load testing scenarios
  - Monitoring solutions

### 🔒 Restricted Areas

For security reasons, these areas require special approval:

- Core consensus mechanisms
- Cryptographic implementations
- Security-critical validation logic
- Production deployment configurations
- Private key management
- AI model parameters

If you want to contribute to these areas, please discuss with the core team first via Discord or a GitHub issue.

## Security Considerations

### Responsible Disclosure

- **DO NOT** open public issues for security vulnerabilities
- Email security@self.app with details
- Use PGP encryption if possible (key available on our website)
- We aim to respond within 48 hours

### Security in Contributions

When contributing:
- Never commit secrets, keys, or credentials
- Be cautious with dependencies
- Consider security implications of changes
- Add security tests where appropriate

## Pull Request Process

1. **Before Submitting**
   - [ ] Code follows style guidelines
   - [ ] Tests pass locally
   - [ ] Documentation is updated
   - [ ] Commit messages follow conventions
   - [ ] Branch is up-to-date with main

2. **PR Description**
   - Clearly describe what changes you made
   - Link related issues
   - Include screenshots for UI changes
   - List any breaking changes

3. **Review Process**
   - PRs require at least one maintainer approval
   - CI checks must pass
   - Security scan must complete
   - Documentation must be updated

4. **After Merge**
   - Delete your feature branch
   - Update your local repository
   - Celebrate your contribution! 🎉

## Community

### Communication Channels

- **Discord**: [SELF Community](https://discord.gg/WdMdVpA4C8) - Real-time chat
- **GitHub Discussions**: Long-form technical discussions
- **Twitter/X**: [@self_hq](https://x.com/self_hq) - Updates and announcements

### Recognition

We value all contributions! Contributors are:
- Listed in our [Contributors page](CONTRIBUTORS.md)
- Eligible for special Discord roles
- Invited to contributor-only events
- Considered for bounty programs

### Getting Help

- **Discord #dev-help**: Quick questions
- **GitHub Discussions**: Technical deep-dives
- **Documentation**: [docs.self.app](https://docs.self.app)
- **Office Hours**: Weekly developer calls (Thursdays 3PM UTC)

## License

By contributing to SELF Chain, you agree that your contributions will be licensed under the [MIT License](LICENSE).

## Thank You!

Your contributions make SELF Chain better for everyone. Whether you're fixing a typo, adding a feature, or helping others in the community, every contribution matters.

Welcome to the SELF Chain family! 🚀

---

*"The best way to predict the future is to invent it."* - Alan Kay