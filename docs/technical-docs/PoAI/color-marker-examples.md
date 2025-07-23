---
sidebar_label: "Color Marker Examples"
sidebar_position: 7
---

# Color Marker System Examples

This document provides practical examples and visualizations of how the Color Marker System works in SELF Chain.

## Basic Transaction Example

### Initial State
```
Wallet Address: 0x742d35Cc6634C0532925a3b844Bc9e7595f2bD47
Initial Color: #3A7BD5 (Blue shade)
Balance: 1000 SELF
```

### Transaction Details
```
Sender: 0x742d35Cc6634C0532925a3b844Bc9e7595f2bD47
Receiver: 0x8B3f5Af2958438D676E4F7144C5F2B5E7A6c2Fb8
Amount: 100 SELF
Timestamp: 1704123456
Nonce: 42
```

### Color Calculation Process

1. **Generate Transaction Hash**
```
Transaction Data = sender || receiver || amount || timestamp || nonce
Transaction Hash = SHA256(Transaction Data)
                 = 0xA3F2B7C9E4D1A8F6B2C7E9D4A1F8B6C2E7D9A4F1B8C6E2D79AF4B1C8E6D2A97F
```

2. **Divide into 6 Parts**
```
Part 1: A3F2B7C9E4
Part 2: D1A8F6B2C7
Part 3: E9D4A1F8B6
Part 4: C2E7D9A4F1
Part 5: B8C6E2D79A
Part 6: F4B1C8E6D2
```

3. **Reduce Each Part**
```
Part 1: A+3+F+2+B+7+C+9+E+4 = 59 → 5+9 = 14 → 1+4 = 5
Part 2: D+1+A+8+F+6+B+2+C+7 = 54 → 5+4 = 9
Part 3: E+9+D+4+A+1+F+8+B+6 = 56 → 5+6 = 11 → 1+1 = 2
Part 4: C+2+E+7+D+9+A+4+F+1 = 53 → 5+3 = 8
Part 5: B+8+C+6+E+2+D+7+9+A = 52 → 5+2 = 7
Part 6: F+4+B+1+C+8+E+6+D+2 = 51 → 5+1 = 6
```

4. **Form Transaction Color**
```
Transaction Color = #592876
```

5. **Calculate New Wallet Color**
```
Current Color: #3A7BD5 (decimal: 3832789)
Transaction Color: #592876 (decimal: 5843062)
New Color: (3832789 + 5843062) mod 16777216 = #9460FB (Purple shade)
```

## Multi-Transaction Sequence

### Transaction Chain Example
```
Initial State: Wallet #FF5733 (Orange)

Transaction 1: Send 50 SELF
- Transaction Color: #1A2B3C
- New Wallet Color: #19839F (Blue-green)

Transaction 2: Receive 75 SELF
- Transaction Color: #4D5E6F
- New Wallet Color: #66E20E (Light green)

Transaction 3: Send 25 SELF
- Transaction Color: #7890AB
- New Wallet Color: #DF72B9 (Pink)
```

## Validation Example

### Block Builder Process
```json
{
  "block": {
    "height": 123456,
    "transactions": [
      {
        "tx_hash": "0xA3F2B7...",
        "sender": "0x742d35...",
        "receiver": "0x8B3f5A...",
        "amount": 100,
        "sender_color_before": "#3A7BD5",
        "sender_color_after": "#9460FB",
        "transaction_color": "#592876"
      }
    ]
  }
}
```

### AI Validator Verification
```python
# Pseudocode for validation
def validate_color_transitions(block):
    for tx in block.transactions:
        # Recalculate transaction color
        calculated_tx_color = calculate_transaction_color(tx.tx_hash)
        
        # Verify transaction color matches
        if calculated_tx_color != tx.transaction_color:
            return False
        
        # Verify color transition
        expected_new_color = (tx.sender_color_before + calculated_tx_color) % 0xFFFFFF
        if expected_new_color != tx.sender_color_after:
            return False
    
    return True
```

## Edge Cases

### Color Overflow Example
```
Current Color: #FEDCBA (16702650)
Transaction Color: #123456 (1193046)
Sum: 17895696
New Color: 17895696 mod 16777216 = #1118480 → #111111 (Dark gray)
```

### Zero Transaction Example
```
Transaction Amount: 0 SELF
Transaction Hash: Still unique due to timestamp and nonce
Color Transition: Still occurs, preventing spam
```

## Visual Representation

### Color Progression Over Time
```
Block 1: #FF0000 (Red)    ━━━━━━━━━━━━━━━
Block 2: #FF7F00 (Orange) ━━━━━━━━━━━━━━━
Block 3: #FFFF00 (Yellow) ━━━━━━━━━━━━━━━
Block 4: #7FFF00 (Green)  ━━━━━━━━━━━━━━━
Block 5: #00FF00 (Green)  ━━━━━━━━━━━━━━━
Block 6: #00FF7F (Cyan)   ━━━━━━━━━━━━━━━
Block 7: #00FFFF (Cyan)   ━━━━━━━━━━━━━━━
Block 8: #007FFF (Blue)   ━━━━━━━━━━━━━━━
```

### Attack Detection Pattern
```
Normal Pattern:
Wallet A: #FF0000 → #FF5500 → #FFAA00 → #FFFF00 (Gradual progression)

Attack Pattern:
Wallet B: #FF0000 → #0000FF → #FF0000 → #0000FF (Suspicious oscillation)
         ↑ Potential double-spend attempt detected
```

## Performance Metrics

### Computation Time
```
Single Color Calculation: ~0.001ms
Block with 1000 transactions: ~1ms
Parallel validation (8 cores): ~0.125ms
```

### Storage Requirements
```
Per Wallet Color State: 3 bytes
Per Transaction Color: 3 bytes
Block Color Merkle Root: 32 bytes
Total for 1M wallets: ~3MB
```

## Integration Examples

### Smart Contract Integration
```solidity
// Solidity example
contract ColorAwareContract {
    mapping(address => uint24) public walletColors;
    
    function updateColor(address wallet, uint24 newColor) external {
        require(msg.sender == validator, "Only validator can update");
        require(isValidColorTransition(walletColors[wallet], newColor));
        walletColors[wallet] = newColor;
    }
}
```

### API Response Example
```json
{
  "wallet": "0x742d35Cc6634C0532925a3b844Bc9e7595f2bD47",
  "current_color": "#9460FB",
  "color_history": [
    {"block": 123456, "color": "#3A7BD5"},
    {"block": 123457, "color": "#9460FB"}
  ],
  "pending_transactions": [
    {
      "tx_hash": "0xB4E9F2...",
      "expected_color": "#A123BC"
    }
  ]
}
```

## Troubleshooting

### Common Issues

1. **Color Mismatch Error**
   - Cause: Transaction ordering issue
   - Solution: Verify transaction sequence in mempool

2. **Validator Disagreement**
   - Cause: Network partition during color update
   - Solution: Resync with majority color state

3. **Performance Degradation**
   - Cause: Large block size
   - Solution: Implement parallel color validation

### Debug Output Example
```
[DEBUG] Wallet 0x742d35... color transition:
  Current: #3A7BD5 (3832789)
  Transaction: #592876 (5843062)
  Expected: #9460FB (9675851)
  Calculated: #9460FB (9675851)
  Status: VALID ✓
```

## Best Practices

1. **Always Verify Color History**: Check last N color transitions for consistency
2. **Cache Color States**: Maintain recent color states in memory for fast validation
3. **Batch Validations**: Process multiple color transitions in parallel
4. **Monitor Patterns**: Use AI to detect unusual color progression patterns
5. **Regular Checkpoints**: Store color state snapshots for fast synchronization

This comprehensive example set demonstrates the practical application of the Color Marker System in various scenarios, helping developers and validators understand its implementation and behavior.