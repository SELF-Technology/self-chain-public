# User Instance Architecture

## Overview

Every SELF user gets their own private instance that includes a blockchain node, private AI, and sovereign storage. This document describes the technical architecture of these user instances.

**Key Principle**: SELF automatically provisions and manages all infrastructure when users sign up through the Super-App. Developers build applications that interact with these user instances through APIs.



## Architecture Details

### User Instance Components

Each user's cloud instance includes:

```
User's Private Cloud Instance
├── SELF Chain Node (Blockchain participation)
├── Private LLM (AI assistant)
├── OrbitDB (Decentralized database)
├── IPFS Node (Distributed storage)
└── API Gateway (Secure access point)
```

### Security Boundaries

```
┌─────────────────────────────────┐
│   User A's Instance (Isolated)  │
│  ┌─────────────┐ ┌────────────┐ │
│  │ Private LLM │ │   Storage  │ │
│  └─────────────┘ └────────────┘ │
└─────────────────────────────────┘
         ⚡ No Connection ⚡
┌─────────────────────────────────┐
│   User B's Instance (Isolated)  │
│  ┌─────────────┐ ┌────────────┐ │
│  │ Private LLM │ │   Storage  │ │
│  └─────────────┘ └────────────┘ │
└─────────────────────────────────┘
```

### Developer Access Model

```
Your App → SELF SDK → User Authorization → User's Instance API
                           ↓
                    Permission Granted
                           ↓
                    Scoped Access Only
```

---

## Summary

SELF Chain provides a unique architecture where every user has their own private instance with dedicated resources. This approach ensures complete data sovereignty while enabling developers to build powerful applications that respect user privacy.

**Key Takeaways:**
- Each user instance is completely isolated
- All components work together to provide a sovereign digital experience
- Security boundaries prevent any cross-user data access
- Developers interact only through permission-based APIs

---

*For information on building applications that interact with user instances, see the [Building on SELF](/Building-on-SELF/getting-started) guide.*