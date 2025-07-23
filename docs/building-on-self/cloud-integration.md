---
sidebar_position: 5
---

# Developer Integration Guide

> 🚧 **NOT YET AVAILABLE**: This guide describes the PLANNED developer integration for SELF Chain. The features, APIs, and SDKs described here do not exist yet. This is a design specification for future implementation.

> ⚠️ **DO NOT ATTEMPT**: The code examples and endpoints shown are conceptual. They will not work as the infrastructure is not yet deployed.

## CRITICAL: What This Guide Is About

**This guide shows the planned design for developers who will be able to:**
- ✅ Build apps that interact with SELF user instances (future capability)
- ✅ Access user data and AI with permission (planned feature)
- ✅ Create experiences on top of SELF infrastructure (coming soon)

**This guide is NOT for:**
- ❌ Provisioning cloud infrastructure (SELF will handle this)
- ❌ Managing servers or containers (SELF will handle this)
- ❌ Creating user instances (will happen automatically)

---

## Understanding Your Role as a Developer

### The SELF Ecosystem

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│ SELF Super-App  │────▶│ SELF Cloud Infra │────▶│ User Instance   │
│ (User signs up) │     │ (Auto-provision) │     │ (Ready to use)  │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                                                           │
                                                           ▼
                                                   ┌───────────────┐
                                                   │  YOUR APP     │
                                                   │ (Interacts    │
                                                   │  with users)  │
                                                   └───────────────┘
```

### What You Build

Your applications:
1. **Enhance** user's AI capabilities
2. **Utilize** user's private storage
3. **Respect** user's privacy
4. **Add value** to user's digital life

---

## Core Concepts

### User Instances

Every SELF user automatically has:
- **Private LLM**: Their own AI that never shares data
- **Blockchain Node**: For network participation
- **Sovereign Storage**: Encrypted, user-controlled data
- **API Gateway**: Secure access point for apps

### Permission Model

Your app must request permission to:
- Chat with user's AI
- Read/write user's storage
- Access user's blockchain data
- Use user's compute resources

---

## Getting Started (Future Process)

> **REMINDER**: The following steps show how the developer onboarding will work once the platform is launched. None of these endpoints or packages exist yet.

### 1. Register as a Developer (Planned)

```bash
# FUTURE ENDPOINT - NOT ACTIVE
# This shows how registration will work when available
curl -X POST https://developers.self.app/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Your Name",
    "email": "dev@example.com",
    "project": "My Awesome App",
    "description": "What your app does"
  }'
```

Once available, you'll receive:
- `APP_ID`: Your application identifier
- `APP_SECRET`: Your application secret
- Testnet access credentials

### 2. Install the SDK (Future)

```bash
# THESE PACKAGES DO NOT EXIST YET
# Showing planned package names for future release

# JavaScript/TypeScript (planned)
npm install @selfchain/sdk  # NOT AVAILABLE

# Python (planned)
pip install selfchain-sdk   # NOT AVAILABLE

# Go (planned)
go get github.com/self-chain/sdk-go  # NOT AVAILABLE
```

### 3. Initialize Your App (Conceptual)

```javascript
// CONCEPTUAL CODE - Shows planned API design
// This will not work as the SDK doesn't exist yet
import { SELF } from '@selfchain/sdk';

const app = new SELF.App({
  appId: process.env.APP_ID,
  appSecret: process.env.APP_SECRET,
  environment: 'testnet'
});
```

---

## User Authentication Flow (Planned Design)

> **CONCEPTUAL**: The following shows how user authentication will work when the platform is launched.

### How Users Will Connect to Your App

1. **User Initiates Connection (Future)**
```javascript
// PLANNED API - Not yet implemented
// Shows how connection requests will work
const connectionUrl = app.createConnectionUrl({
  permissions: ['ai:chat', 'storage:read', 'storage:write'],
  redirectUrl: 'https://yourapp.com/callback'
});

// Redirect user to authorize
window.location.href = connectionUrl;
```

2. **User Grants Permissions**
- User sees what permissions your app requests
- User approves/denies in SELF app
- User is redirected back to your app

3. **Your App Receives Access Token**
```javascript
// In your callback handler
app.handleCallback(async (err, auth) => {
  if (err) {
    console.error('User denied access');
    return;
  }
  
  // Store the user session
  const session = auth.session;
  
  // Now you can interact with user's instance
  const ai = auth.ai;
  const storage = auth.storage;
});
```

---

## Interacting with User Instances (Conceptual Examples)

> ⚠️ **ALL CODE BELOW IS CONCEPTUAL**: These examples show the planned API design. None of this functionality exists yet.

### Chat with User's Private AI (Planned Feature)

```javascript
// FUTURE API - This shows how AI chat will work
// Not currently functional
const response = await session.ai.chat({
  message: "What's on my calendar today?"
});

// Chat with context
const response = await session.ai.chat({
  message: "Summarize this document",
  context: {
    document: documentContent,
    style: "brief"
  }
});

// Streaming response
const stream = await session.ai.chatStream({
  message: "Write a story about space"
});

stream.on('data', (chunk) => {
  console.log(chunk.text);
});
```

### Use User's Storage

```javascript
// Save data to user's storage
await session.storage.set({
  key: 'app:preferences',
  value: {
    theme: 'dark',
    notifications: true
  },
  encrypted: true // Optional encryption
});

// Read data
const prefs = await session.storage.get('app:preferences');

// List user's data (that your app created)
const items = await session.storage.list({
  prefix: 'app:',
  limit: 100
});

// Delete data
await session.storage.delete('app:preferences');
```

### Query Blockchain Data

```javascript
// Get user's blockchain info
const info = await session.blockchain.getInfo();
console.log(info.address, info.balance);

// Get user's transaction history
const transactions = await session.blockchain.getTransactions({
  limit: 50,
  offset: 0
});
```

---

## Building Different Types of Apps

### AI-Enhanced Apps

```javascript
// Example: Smart Note-Taking App
class SmartNotes {
  async createNote(session, content) {
    // Use AI to enhance the note
    const enhanced = await session.ai.chat({
      message: "Enhance these notes with key points and action items",
      context: { notes: content }
    });
    
    // Save to user's storage
    await session.storage.set({
      key: `notes:${Date.now()}`,
      value: {
        original: content,
        enhanced: enhanced.response,
        created: new Date()
      }
    });
  }
}
```

### Privacy-First Apps

```javascript
// Example: Encrypted Messaging
class SecureChat {
  async sendMessage(session, recipientId, message) {
    // Encrypt using user's keys
    const encrypted = await session.crypto.encrypt({
      data: message,
      recipientId: recipientId
    });
    
    // Store in user's outbox
    await session.storage.set({
      key: `messages:sent:${Date.now()}`,
      value: encrypted
    });
    
    // Notify recipient (through SELF network)
    await session.network.notify({
      recipientId: recipientId,
      type: 'new_message'
    });
  }
}
```

### Data Analysis Apps

```javascript
// Example: Personal Analytics
class PersonalAnalytics {
  async analyzeData(session) {
    // Get user's data (with permission)
    const data = await session.storage.list({
      prefix: 'health:',
      limit: 1000
    });
    
    // Use AI for analysis
    const insights = await session.ai.chat({
      message: "Analyze this health data and provide insights",
      context: { data: data }
    });
    
    return insights.response;
  }
}
```

---

## Testnet Development

### Getting Test Users

```javascript
// Create test users for development
const testUser = await app.createTestUser({
  tier: 'free' // or 'growth', 'pro'
});

console.log(testUser.credentials);
// Use these credentials to simulate user interactions
```

### Simulating User Flows

```javascript
// Simulate user authentication
const testSession = await app.authenticateTestUser(testUser);

// Now develop as if real user
await testSession.ai.chat({ message: "Hello, test!" });
```

---

## Best Practices

### 1. Respect User Privacy
```javascript
// ❌ BAD: Trying to access without permission
const data = await session.storage.get('private:key'); // Will fail

// ✅ GOOD: Only access what you have permission for
const data = await session.storage.get('app:your-data');
```

### 2. Handle Permissions Properly
```javascript
// Always check permissions before operations
if (session.hasPermission('ai:chat')) {
  const response = await session.ai.chat({...});
} else {
  // Request additional permissions
  const newPerms = await session.requestPermissions(['ai:chat']);
}
```

### 3. Optimize for User Resources
```javascript
// ❌ BAD: Excessive AI calls
for (let i = 0; i < 1000; i++) {
  await session.ai.chat({...}); // User pays for compute!
}

// ✅ GOOD: Batch operations
const batchResponse = await session.ai.analyze({
  documents: allDocuments,
  operation: 'summarize'
});
```

---

## Common Mistakes to Avoid

### ❌ Trying to Provision Infrastructure
```javascript
// THIS WILL NOT WORK - SELF handles all provisioning
const instance = await createUserInstance(); // NO!
```

### ❌ Accessing Other Users' Data
```javascript
// THIS WILL FAIL - Strong isolation between users
const otherUserData = await session.storage.get('user:123:data'); // NO!
```

### ❌ Storing Sensitive Data Unencrypted
```javascript
// BAD: Storing passwords in plain text
await session.storage.set({ key: 'password', value: '123456' });

// GOOD: Always encrypt sensitive data
await session.storage.set({ 
  key: 'credentials', 
  value: hashedPassword,
  encrypted: true 
});
```

---

## SDK Reference

### JavaScript/TypeScript
- Full API Documentation (Coming Soon)
- Example Apps (Coming Soon)
- TypeScript Types (Coming Soon)

### Error Handling
```javascript
try {
  const response = await session.ai.chat({...});
} catch (error) {
  if (error.code === 'PERMISSION_DENIED') {
    // Handle permission issues
  } else if (error.code === 'RATE_LIMITED') {
    // Handle rate limiting
  }
}
```

---

## Support & Resources

### Getting Help
- **Discord**: [Join our community](https://discord.gg/WdMdVpA4C8)
- **GitHub**: [Issues & Discussions](https://github.com/SELF-Technology/self-chain-public)
- **Email**: devs@self.app

### Resources
- [Documentation](https://docs.self.app)
- [Project Status](https://docs.self.app/project-status)
- Example Apps (Coming Soon)
- Video Tutorials (Coming Soon)
- API Status (Coming Soon)

---

## Summary

**Remember:**
1. You build apps, SELF manages infrastructure
2. Always respect user privacy and permissions
3. Users own their data, you just interact with it
4. Focus on adding value to users' digital lives

**Next Steps:**
1. Register as developer
2. Get testnet access
3. Build something amazing
4. Share with the community

---

*This guide is for building on SELF, not building SELF itself. For contribution to core infrastructure, see our [GitHub](https://github.com/SELF-Technology/self-chain-public).*