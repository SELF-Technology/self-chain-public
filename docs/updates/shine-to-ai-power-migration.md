---
sidebar_label: "🔄 Shine → AI Power Migration"
sidebar_position: 1
---

# Migration from "Shine %" to "AI Power"

This document explains the conceptual evolution from "Shine %" to "AI Power" in the SELF ecosystem and provides guidance for developers and users adapting to the new terminology.

## Background

SELF has evolved its user experience metaphor from a percentage-based "Shine %" system to the more intuitive "AI Power" concept. This change reflects our commitment to making blockchain and AI technology more accessible and understandable.

## What Changed

### Terminology Evolution

| Previous (Shine %) | Current (AI Power) | Reasoning |
|--------------------|--------------------|-----------|
| Shine % | AI Power | More intuitive representation of computational resources |
| Shine depletion | Power consumption | Clearer connection to actual resource usage |
| 100% Shine | 100% AI Power | Maintains familiar full-capacity concept |
| Shine tracking | Power monitoring | Better describes system functionality |
| Shine economy | Power sharing economy | More descriptive of collaborative features |

### Visual Changes

**Before: Shine % Progress Bar**
```
Shine: [████████░░] 80%
```

**After: AI Neural Network Power Orb**
```
    ⚡
   ╭─╮ 80%
  ╱   ╲ AI Power
 ╱  ●  ╲ Remaining
╱  SELF ╲
╲       ╱
 ╲     ╱
  ╲___╱
```

## Benefits of AI Power

### User Experience Improvements

1. **Intuitive Understanding**
   - "Power" is universally understood as capability/energy
   - Direct connection to AI processing strength
   - Removes abstract percentage thinking

2. **Visual Appeal**
   - Neural network orb is more engaging than progress bars
   - Dynamic animations show active AI processing
   - Sci-fi aesthetic aligns with AI technology

3. **Conceptual Clarity**
   - Directly represents computational resources
   - Users understand they're "powering" AI operations
   - Clear connection between usage and resource consumption

### Technical Advantages

1. **Scalable Mapping**
   - AI Power units can represent varying computational complexity
   - Easy to adjust rates based on model sophistication
   - Direct correlation with actual cloud infrastructure costs

2. **Flexible Pricing**
   - Power units can be dynamically priced based on compute costs
   - Different AI models can consume different amounts of power
   - Enables sophisticated resource allocation strategies

## Implementation Changes

### Smart Contract Updates

The migration requires updating smart contract interfaces to use new terminology:

```solidity
// Previous Implementation
struct Subscription {
    uint256 shinePercentUsed;  // 0-10000 (0-100%)
    // other fields...
}

event ShineUsageUpdated(address indexed user, uint256 newUsage);

function updateShineUsage(address user, uint256 usage) external;

// New Implementation
struct Subscription {
    uint256 aiPowerUsed;  // 0-10000 (0-100% of allocated power)
    // other fields...
}

event AIPowerUsageUpdated(address indexed user, uint256 newUsage);

function updateAIPowerUsage(address user, uint256 usage) external;
```

### API Changes

SDK methods have been updated to reflect the new terminology:

```javascript
// Previous API
const shineStatus = await selfSDK.shine.getStatus();
await selfSDK.shine.updateUsage(userId, newUsage);

// New API  
const powerStatus = await selfSDK.aiPower.getStatus();
await selfSDK.aiPower.updateUsage(userId, newUsage);
```

### Configuration Updates

Configuration parameters have been renamed:

```yaml
# Previous configuration
shine:
  daily_limit: 100
  depletion_rate: 1.5
  regeneration_cycle: monthly

# New configuration
ai_power:
  daily_limit: 100
  consumption_rate: 1.5
  regeneration_cycle: monthly
```

## Migration Guide

### For Users

**No Action Required**: The transition is purely cosmetic from a user perspective. Your account balances, tier benefits, and usage patterns remain identical.

**Visual Changes**: 
- Progress bars become neural network power orbs
- "Shine %" labels become "AI Power"
- Same functionality with improved presentation

### For Developers

#### Immediate Changes Needed

1. **Update Documentation References**
   ```bash
   # Find and replace in your documentation
   find . -name "*.md" -exec sed -i 's/Shine %/AI Power/g' {} \;
   find . -name "*.md" -exec sed -i 's/shine-percentage/ai-power-percentage/g' {} \;
   ```

2. **Update API Calls**
   ```javascript
   // Update SDK method calls
   - selfSDK.shine.getStatus()
   + selfSDK.aiPower.getStatus()
   
   - type: 'shine-percentage'
   + type: 'ai-power-percentage'
   ```

3. **Update UI Components**
   ```jsx
   // Update component labels
   - <ShineIndicator percentage={userShine} />
   + <AIPowerOrb power={userPower} />
   ```

#### Future Considerations

1. **Smart Contract Migration**
   - Plan for contract upgrades with new terminology
   - Maintain backwards compatibility during transition
   - Update event listeners and interfaces

2. **Database Schema Updates**
   - Rename columns from `shine_*` to `ai_power_*`
   - Update indexes and constraints
   - Plan for data migration scripts

## Backwards Compatibility

### Transition Period

During the migration period, both terminologies are supported:

```javascript
// Both work during transition
const status1 = await selfSDK.shine.getStatus();      // Deprecated
const status2 = await selfSDK.aiPower.getStatus();    // Preferred
```

### Deprecation Timeline

- **Phase 1 (Current)**: Both terminologies supported
- **Phase 2 (Q4 2025)**: "Shine %" marked as deprecated
- **Phase 3 (Q1 2026)**: "Shine %" support removed

## Frequently Asked Questions

### Q: Will my current subscription tier change?
**A**: No, all tier benefits remain identical. Only the visual representation and terminology change.

### Q: Do I need to update my app integrations?
**A**: Existing integrations continue to work during the transition period. Update to new API methods when convenient.

### Q: Why make this change?
**A**: "AI Power" is more intuitive for users and better represents the actual computational resources being consumed.

### Q: Will there be more changes like this?
**A**: We're committed to continuous UX improvement, but major terminology changes will be rare and well-communicated.

## Support

If you encounter issues during the migration:

- **Developers**: Check the [Developer Integration](/building-on-self/developer-integration) for updated API documentation
- **Users**: No action needed - changes are automatic
- **Questions**: Contact support at devs@self.app

---

*This migration represents SELF's commitment to user-centric design and intuitive technology experiences. The "AI Power" metaphor better communicates the sophisticated computational resources available to every SELF user.*