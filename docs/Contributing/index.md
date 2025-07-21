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

By participating in this project, you agree to abide by our [Code of Conduct](code-of-conduct.md). We are committed to providing a welcoming and inclusive environment for all contributors.

## Getting Started

> ⚠️ **Important**: SELF Chain is in active development. Please review our [Project Status](https://docs.self.app/project-status) to understand what's currently available.

1. **Join our Community**
   - Discord: [Join SELF Community](https://discord.gg/WdMdVpA4C8)
   - Follow us on [Twitter/X](https://x.com/self_hq)
   - Star and watch this repository
   - GitHub Discussions for technical questions

2. **Understand the Project**
   - Read our [documentation](https://docs.self.app)
   - Review the [Project Status](https://docs.self.app/project-status) for current state
   - Review the [architecture overview](https://docs.self.app/Technical%20Docs/SELF%20Chain/SELF_Chain_Architecture)
   - Understand [Proof-of-AI consensus](https://docs.self.app/Technical%20Docs/PoAI/Proof-of-AI)

3. **Find Your First Issue**
   - Look for issues labeled `good-first-issue`
   - Check `help-wanted` labels for areas needing assistance
   - Join Discord or start a GitHub Discussion before starting major work
   - Note: Many components are still being developed - check feasibility first

## How to Contribute

### 🐛 Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates.

**To report a bug:**
1. Create a new issue with a clear title
2. Include detailed steps to reproduce
3. Provide system information (OS, Rust version, etc.)
4. Add relevant logs or screenshots
5. Tag the issue with `bug` label

### 💡 Suggesting Features

We love new ideas! To suggest a feature:
1. Create a new issue with "[Feature Request]" prefix
2. Explain the problem it solves
3. Describe your proposed solution
4. Consider alternatives you've thought about
5. Tag the issue with `enhancement` label

> **Note**: Check our [Project Status](https://docs.self.app/project-status) first to see if the feature is already planned

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
   - Follow Rust best practices and use `cargo fmt` and `cargo clippy`
   - Add tests for new functionality
   - Update documentation as needed
   - Ensure your code builds without warnings

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
   Then create a Pull Request to the **Stage** branch on GitHub.

## Development Setup

### Prerequisites

- Rust 1.70+ (required for core blockchain)
- Git
- Optional: Docker (for containerized development)
- Optional: Node.js 18+ (for future SDK development)

> **Note**: The project is primarily Rust-based. SDKs in other languages are planned but not yet available.

### Local Development

1. **Clone and Build**
   ```bash
   git clone https://github.com/SELF-Technology/self-chain-public.git
   cd self-chain-public
   cargo build
   ```

2. **Run Tests**
   ```bash
   cargo test
   ```

3. **Run a Local Node** (Advanced)
   ```bash
   # Note: Requires additional setup - see documentation
   cargo run --bin self-chain-node -- --dev
   ```

> **Important**: Many features shown in documentation are planned but not yet implemented. Check [Project Status](https://docs.self.app/project-status) for current capabilities.

## Contribution Areas

### ✅ Open for Contributions

These areas welcome community contributions:

- **Documentation**
  - Improving existing documentation clarity
  - Adding code examples
  - Creating architecture diagrams
  - Fixing typos and errors
  - Translations (once base docs are stable)

- **Core Blockchain** (with guidance)
  - Bug fixes in non-security-critical areas
  - Performance improvements
  - Test coverage improvements
  - Code refactoring for clarity

- **Future Development** (Planning Phase)
  - SDK design proposals
  - API specification feedback
  - Tool requirements gathering
  - Use case documentation

- **Testing & Quality**
  - Writing additional tests
  - Benchmarking scenarios
  - Bug reports with reproductions
  - Code review assistance

- **Community**
  - Answering questions on Discord/GitHub
  - Creating educational content
  - Helping new contributors
  - Improving onboarding docs

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

### 🔄 Branch Workflow

We use a **Stage branch workflow** to ensure code quality:

1. **Stage Branch** (`Stage`)
   - All community PRs should target the `Stage` branch
   - This is our testing and integration branch
   - Requires 1 approval to merge
   - All CI checks must pass

2. **Main Branch** (`main`)
   - Production-ready code only
   - PRs from `Stage` to `main` after thorough testing
   - Requires 1 approval to merge
   - Stricter review process

### 📋 PR Checklist

1. **Before Submitting**
   - [ ] Target branch is set to `Stage` (not `main`)
   - [ ] Code follows style guidelines
   - [ ] Tests pass locally
   - [ ] Documentation is updated
   - [ ] Commit messages follow conventions
   - [ ] Branch is up-to-date with Stage

2. **PR Description**
   - Clearly describe what changes you made
   - Link related issues
   - Include screenshots for UI changes
   - List any breaking changes

3. **Review Process**
   - PRs to `Stage` require at least 1 maintainer approval
   - PRs to `main` require at least 1 maintainer approval
   - CI checks must pass
   - Security scan must complete
   - Documentation must be updated

4. **After Merge**
   - Delete your feature branch
   - Update your local repository
   - Your changes will be tested in Stage before going to main
   - Celebrate your contribution! 🎉

## Community

### Communication Channels

- **Discord**: [SELF Community](https://discord.gg/WdMdVpA4C8) - Real-time chat
- **GitHub Discussions**: Long-form technical discussions
- **GitHub Issues**: Bug reports and feature requests
- **Twitter/X**: [@self_hq](https://x.com/self_hq) - Updates and announcements
- **Email**: devs@self.app for developer questions

### Recognition

We value all contributions! Contributors are:
- Recognized in pull request comments and releases
- Eligible for special Discord roles
- Part of building the future of blockchain

### Getting Help

- **Discord**: General questions and community chat
- **GitHub Discussions**: Technical deep-dives
- **GitHub Issues**: Specific bugs or feature discussions
- **Documentation**: [docs.self.app](https://docs.self.app)
- **Project Status**: [Current development state](https://docs.self.app/project-status)

## License

By contributing to SELF Chain, you agree that your contributions will be licensed under the [MIT License](LICENSE).

## Thank You!

Your contributions make SELF Chain better for everyone. Whether you're fixing a typo, adding a feature, or helping others in the community, every contribution matters.

Welcome to the SELF Chain family! 🚀

---

*"The best way to predict the future is to invent it."* - Alan Kay