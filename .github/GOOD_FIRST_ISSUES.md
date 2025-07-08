# Good First Issues for New Contributors

This document contains templates for beginner-friendly issues that can be created on GitHub. These issues are designed to help new contributors get started with SELF Chain while respecting our testnet status and security boundaries.

## Documentation Issues (Easiest Start)

### Issue 1: Improve Installation Instructions
**Title**: [DOCS] Add troubleshooting section to installation guide
**Labels**: `good-first-issue`, `documentation`, `help-wanted`
**Description**:
```
We need to add a troubleshooting section to our installation documentation to help new developers resolve common issues.

**Task**:
- Add a "Common Installation Issues" section to docs/Getting_Started_Testnet.md
- Include solutions for:
  - Rust version conflicts
  - Node.js compatibility issues
  - Missing dependencies on different OS
  - Permission errors

**Skills needed**: Technical writing, basic development experience
**Testnet safe**: ✅ Yes - documentation only
```

### Issue 2: Add Code Examples
**Title**: [DOCS] Add more code examples to PoAI explanation
**Labels**: `good-first-issue`, `documentation`
**Description**:
```
Our Proof-of-AI documentation could benefit from more concrete examples to help developers understand the concept better.

**Task**:
- Add 2-3 code examples showing:
  - How transactions are validated (conceptually)
  - Color marker state transitions
  - Basic PoAI flow diagram

**Note**: Use pseudocode or high-level examples only. Do not expose actual validation logic.
**Testnet safe**: ✅ Yes - conceptual examples only
```

### Issue 3: Create Glossary
**Title**: [DOCS] Create terminology glossary for SELF Chain
**Labels**: `good-first-issue`, `documentation`, `help-wanted`
**Description**:
```
New developers often struggle with blockchain-specific terms. Create a glossary to help them.

**Task**:
- Create docs/GLOSSARY.md
- Define 20-30 common terms like:
  - PoAI (Proof-of-AI)
  - Color Marker
  - Testnet vs Mainnet
  - Consensus
  - Validator
  
**Testnet safe**: ✅ Yes - educational content
```

## Testing Issues (Moderate Difficulty)

### Issue 4: Add Unit Tests
**Title**: [TEST] Add unit tests for utility functions
**Labels**: `good-first-issue`, `testing`, `rust`
**Description**:
```
Several utility functions in our public codebase lack unit tests.

**Task**:
- Add tests for functions in src/utils/formatting.rs
- Achieve 90%+ coverage for this module
- Follow existing test patterns

**Note**: Only test public utility functions, not security-critical code
**Testnet safe**: ✅ Yes - testing public utilities only
```

### Issue 5: Improve Error Messages
**Title**: [UX] Improve error messages for common failures
**Labels**: `good-first-issue`, `enhancement`, `developer-experience`
**Description**:
```
Our error messages could be more helpful for developers, especially on testnet.

**Task**:
- Review error messages in public API responses
- Make them more descriptive and actionable
- Always remind users they're on testnet when relevant
- Add error codes for easier debugging

**Example**:
Before: "Transaction failed"
After: "Transaction failed: Insufficient TEST tokens (testnet). Current balance: 50 TEST, Required: 100 TEST"

**Testnet safe**: ✅ Yes - improving developer experience
```

## Tool Development (Good Learning Projects)

### Issue 6: Create Testnet Faucet CLI
**Title**: [TOOL] Create simple CLI for testnet faucet
**Labels**: `good-first-issue`, `tooling`, `rust`
**Description**:
```
Build a simple command-line tool to request testnet tokens.

**Requirements**:
- Command: `self-faucet request <address>`
- Show clear testnet warnings
- Rate limit awareness (display remaining quota)
- Nice error messages

**Note**: This is for TESTNET tokens only which have no value
**Testnet safe**: ✅ Yes - testnet tools
```

### Issue 7: Block Explorer Improvements
**Title**: [UI] Add testnet warning banner to block explorer
**Labels**: `good-first-issue`, `frontend`, `ui`
**Description**:
```
Our testnet block explorer needs a prominent warning banner.

**Task**:
- Add a dismissible warning banner that says:
  "⚠️ TESTNET - Tokens have no value. Network may reset at any time."
- Banner should be visible on all pages
- Use our brand colors (see style guide)
- Store dismissal in localStorage

**Testnet safe**: ✅ Yes - UI improvement
```

## Community Issues

### Issue 8: Create Tutorial
**Title**: [TUTORIAL] Create "Your First SELF Chain Transaction" tutorial
**Labels**: `good-first-issue`, `documentation`, `tutorial`
**Description**:
```
Create a beginner-friendly tutorial for sending a first testnet transaction.

**Include**:
- Setting up environment
- Getting testnet tokens
- Sending a transaction
- Checking transaction status
- Common mistakes to avoid

**Important**: 
- Multiple ⚠️ TESTNET warnings throughout
- Use only testnet endpoints
- Emphasize tokens have no value

**Testnet safe**: ✅ Yes - testnet tutorial
```

### Issue 9: Internationalization
**Title**: [i18n] Translate error messages to Spanish
**Labels**: `good-first-issue`, `internationalization`, `help-wanted`
**Description**:
```
Help make SELF Chain accessible to Spanish speakers.

**Task**:
- Translate error messages in src/errors/messages.rs
- Maintain technical accuracy
- Keep testnet warnings prominent
- Follow existing i18n patterns

**Testnet safe**: ✅ Yes - translation only
```

## Guidelines for Creating More Issues

### ✅ Good First Issues Should:
- Be clearly scoped (completable in <1 week)
- Not require deep blockchain knowledge
- Not touch security-critical code
- Include clear acceptance criteria
- Mention testnet status when relevant
- Be genuinely helpful to the project

### ❌ Avoid Issues That:
- Require access to private repositories
- Involve consensus mechanisms
- Touch validation logic
- Need production credentials
- Could compromise security
- Are just "busy work"

## Issue Templates

### Documentation Issue Template
```markdown
**Description**: [Clear description of what needs to be documented]

**Current State**: [What exists now]

**Desired State**: [What we want to achieve]

**Requirements**:
- [ ] Requirement 1
- [ ] Requirement 2

**Resources**:
- [Link to relevant docs]
- [Link to examples]

**Testnet Considerations**: [Any testnet-specific notes]

**Questions?** Ask in #contributing on Discord!
```

### Code Issue Template
```markdown
**Description**: [What needs to be built/fixed]

**Technical Details**:
- Language: [Rust/JS/etc]
- Affected files: [List files]
- Dependencies: [Any new deps needed]

**Acceptance Criteria**:
- [ ] Tests pass
- [ ] Documentation updated
- [ ] Follows code style guide
- [ ] Testnet warnings included (if applicable)

**Getting Started**:
1. Fork the repository
2. Set up development environment (see Getting_Started_Testnet.md)
3. Find the relevant files
4. Make your changes
5. Submit PR

**Security Note**: [Any security considerations]

**Testnet Safe**: ✅ Yes - [reason]
```

## Mentor Program

For each good first issue, consider assigning a mentor who can:
- Answer questions
- Review PRs
- Provide guidance
- Ensure security boundaries are respected

Mentors should be tagged in issues with `@mentor-name`.

---

Remember: Good first issues are the gateway to building our community. Make them welcoming, educational, and genuinely useful!