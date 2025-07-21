---
sidebar_label: "Proof-of-AI"
sidebar_position: 1
---

# Proof-of-AI

We have invented a novel and patent-pending consensus mechanism known as [Proof-of-AI ](https://proofofai.com)(PoAI), enabling SELF Chain's decentralised validation and security. This section details the PoAI methodology.

At its core, every part of PoAI drives efficiency, from the removal of human interference to, critically, the objective of the AI to fulfill its task in the most useful way, quite opposite to PoW (which is inefficient due to the mechanical compute involved) and PoS (which is increasingly centralised and therefore increasingly inefficient).

In contrast, PoAI is based on the work of the following three algorithms that carry out their work independently, enabling blockchain consensus to be achieved.

**The AI-Block Builder Algorithm:** this forms effective blocks of transactions.

**The Voting Algorithm:** this is implemented by the PoAI mechanism, the task of which will be to organise voting for blocks of block builders and communicate between the two associated AI algorithms to achieve final consensus.

**The AI-Validator Algorithm:** this votes for the choice of block builder and determines the node with the permission to enter a block into the chain.

A critical component of PoAI is the **[Color Marker System](Color%20Marker%20System)**, which provides cryptographic validation of wallet states and prevents double-spending through deterministic color transitions.

An overview of the PoAI architecture is shown below:

<div>
<img src="/img/Screenshot 2024-05-01 at 8.59.57 AM.png" alt="PoAI Architecture"/>
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

SELF Chain targets 50,000 TPS through optimized AI consensus algorithms and efficient transaction processing.

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