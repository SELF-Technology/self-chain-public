---
sidebar_label: "Proof-of-AI"
sidebar_position: 1
---

# Proof-of-AI

We have developed a novel consensus mechanism known as [Proof-of-AI ](https://proofofai.com)(PoAI), enabling SELF Chain's decentralized validation and security. This section details the PoAI methodology.

At its core, PoAI is all about efficiency. It removes human interference and focuses the AI on completing tasks in the most useful way—unlike PoW, which wastes energy, or PoS, which becomes increasingly centralized and less efficient over time.

In contrast, PoAI is based on the work of the following three algorithms that carry out their work independently, enabling blockchain consensus to be achieved.

**The AI-Block Builder Algorithm:** this forms effective blocks of transactions.

**The Voting Algorithm:** this is implemented by the PoAI mechanism, the task of which will be to organize voting for blocks of block builders and communicate between the two associated AI algorithms to achieve final consensus.

**The AI-Validator Algorithm:** this votes for the choice of block builder and determines the node with the permission to enter a block into the chain.

A critical component of PoAI is the **[Color Marker System](color-marker-system)**, which provides cryptographic validation of wallet states and prevents double-spending through deterministic color transitions.

An overview of the PoAI architecture is shown below:

<div>
<img src="/img/PoAI-Overview.png" alt="PoAI Architecture"/>
</div>


## How PoAI Works

The Proof-of-AI mechanism operates through the coordinated action of three algorithms:

### 1. Block Formation Process
- **AI-Block Builders** generate potential blocks from the transaction mempool
- **The PoAI mechanism** generates a reference block for comparison
- Multiple block builders compete to create the most efficient block

### 2. Voting Process
- The **Voting Algorithm** organizes the selection process among block builders
- **AI-Validators** vote to choose the most efficient block
- The voting algorithm facilitates communication between the AI algorithms

### 3. Validation and Finalization
- The winning block builder is determined through AI validator votes
- An AI validator (who did not vote for the winning block) performs color marker validation
- The validated block is added to the blockchain

### Reward Distribution

The PoAI system distributes rewards as follows:
- **90%** to the winning block builder
- **8%** to voting AI validators
- **1%** to the block-checking validator
- **1%** to the PoAI mechanism reserve

### Performance Target

SELF Chain has a long-term target of 50,000 TPS through planned optimizations to AI consensus algorithms and transaction processing. Current testnet performance is ~1,000 TPS.

### Key Advantages

PoAI provides:
- **Objective Consensus**: Removes human interference from validation
- **Maximum Efficiency**: AI optimizes for the most useful block composition
- **Energy Efficiency**: No computational waste like proof-of-work
- **Decentralization**: Prevents centralization issues of proof-of-stake

## Future Evolution

While SELF Chain is focused on implementing the pure PoAI invention as described above, research continues into potential enhancements for future versions:

### PoAI 2.0 Considerations (Future Research)
- **Enhanced Resilience**: Multiple AI models for redundancy
- **Advanced Security**: Proactive threat detection capabilities
- **Performance Optimization**: Predictive caching and optimization
- **Extended Capabilities**: Handling complex edge cases

These potential enhancements would be additive to the core PoAI mechanism, maintaining the elegance of the three-algorithm design while providing additional capabilities for enterprise deployments.

The current implementation focus remains on delivering PoAI 1.0 - the pure, patent-pending consensus mechanism that revolutionizes blockchain validation through artificial intelligence.