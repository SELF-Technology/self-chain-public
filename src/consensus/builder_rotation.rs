use anyhow::Result;
use serde::{Deserialize, Serialize};
use std::collections::{HashMap, VecDeque};
use std::time::{SystemTime, UNIX_EPOCH};
use crate::blockchain::Address;

/// Tracks builder participation and enforces rotation
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct BuilderRotation {
    /// Timeout period in blocks (N-block timeout)
    timeout_blocks: u64,
    /// Map of builder address to their last successful block height
    last_success: HashMap<Address, u64>,
    /// Queue of builders in timeout
    timeout_queue: VecDeque<TimeoutEntry>,
    /// Current block height
    current_height: u64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
struct TimeoutEntry {
    builder: Address,
    timeout_until: u64,
}

impl BuilderRotation {
    /// Create a new builder rotation manager
    pub fn new(timeout_blocks: u64) -> Self {
        Self {
            timeout_blocks,
            last_success: HashMap::new(),
            timeout_queue: VecDeque::new(),
            current_height: 0,
        }
    }

    /// Update the current block height and process timeouts
    pub fn update_height(&mut self, height: u64) {
        self.current_height = height;
        self.process_timeouts();
    }

    /// Check if a builder is eligible to propose a block
    pub fn is_eligible(&self, builder: &Address) -> bool {
        // Check if builder is in timeout
        if let Some(last_height) = self.last_success.get(builder) {
            let timeout_until = last_height + self.timeout_blocks;
            timeout_until <= self.current_height
        } else {
            // New builder is always eligible
            true
        }
    }

    /// Record a successful block by a builder
    pub fn record_success(&mut self, builder: Address, block_height: u64) -> Result<()> {
        if !self.is_eligible(&builder) {
            return Err(anyhow::anyhow!(
                "Builder {} is in timeout until block {}",
                hex::encode(&builder),
                self.last_success.get(&builder).unwrap_or(&0) + self.timeout_blocks
            ));
        }

        // Update last success height
        self.last_success.insert(builder.clone(), block_height);
        
        // Add to timeout queue
        self.timeout_queue.push_back(TimeoutEntry {
            builder,
            timeout_until: block_height + self.timeout_blocks,
        });

        Ok(())
    }

    /// Process timeout queue and remove expired entries
    fn process_timeouts(&mut self) {
        while let Some(entry) = self.timeout_queue.front() {
            if entry.timeout_until <= self.current_height {
                // Timeout has expired, remove from queue
                self.timeout_queue.pop_front();
            } else {
                // Queue is ordered, so we can stop here
                break;
            }
        }
    }

    /// Get the number of blocks until a builder is eligible again
    pub fn blocks_until_eligible(&self, builder: &Address) -> Option<u64> {
        self.last_success.get(builder).map(|last_height| {
            let timeout_until = last_height + self.timeout_blocks;
            if timeout_until > self.current_height {
                timeout_until - self.current_height
            } else {
                0
            }
        })
    }

    /// Get all currently eligible builders from a list
    pub fn filter_eligible_builders(&self, builders: &[Address]) -> Vec<Address> {
        builders
            .iter()
            .filter(|builder| self.is_eligible(builder))
            .cloned()
            .collect()
    }

    /// Get statistics about builder rotation
    pub fn get_stats(&self) -> RotationStats {
        let total_builders = self.last_success.len();
        let builders_in_timeout = self.timeout_queue.len();
        let eligible_builders = self.last_success
            .keys()
            .filter(|builder| self.is_eligible(builder))
            .count();

        RotationStats {
            total_builders,
            builders_in_timeout,
            eligible_builders,
            timeout_blocks: self.timeout_blocks,
            current_height: self.current_height,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct RotationStats {
    pub total_builders: usize,
    pub builders_in_timeout: usize,
    pub eligible_builders: usize,
    pub timeout_blocks: u64,
    pub current_height: u64,
}

/// Manages builder selection with fair rotation
pub struct BuilderSelector {
    rotation: BuilderRotation,
    /// Seed for deterministic random selection
    seed: u64,
}

impl BuilderSelector {
    pub fn new(timeout_blocks: u64) -> Self {
        let seed = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_secs();

        Self {
            rotation: BuilderRotation::new(timeout_blocks),
            seed,
        }
    }

    /// Select a builder from eligible candidates
    pub fn select_builder(
        &self,
        candidates: &[Address],
        block_height: u64,
    ) -> Option<Address> {
        let eligible = self.rotation.filter_eligible_builders(candidates);
        
        if eligible.is_empty() {
            return None;
        }

        // Deterministic selection based on block height and seed
        let index = ((block_height.wrapping_mul(self.seed)) as usize) % eligible.len();
        Some(eligible[index].clone())
    }

    /// Update rotation state
    pub fn update(&mut self, height: u64) {
        self.rotation.update_height(height);
    }

    /// Record a successful block
    pub fn record_success(&mut self, builder: Address, height: u64) -> Result<()> {
        self.rotation.record_success(builder, height)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_builder_rotation() {
        let mut rotation = BuilderRotation::new(10); // 10 block timeout
        let builder1 = Address::from([1u8; 32]);
        let builder2 = Address::from([2u8; 32]);

        // Initially both builders are eligible
        assert!(rotation.is_eligible(&builder1));
        assert!(rotation.is_eligible(&builder2));

        // Builder 1 succeeds at block 100
        rotation.update_height(100);
        rotation.record_success(builder1.clone(), 100).unwrap();
        
        // Builder 1 should not be eligible until block 110
        assert!(!rotation.is_eligible(&builder1));
        assert_eq!(rotation.blocks_until_eligible(&builder1), Some(10));

        // Builder 2 is still eligible
        assert!(rotation.is_eligible(&builder2));

        // Move to block 105
        rotation.update_height(105);
        assert!(!rotation.is_eligible(&builder1));
        assert_eq!(rotation.blocks_until_eligible(&builder1), Some(5));

        // Move to block 110
        rotation.update_height(110);
        assert!(rotation.is_eligible(&builder1));
        assert_eq!(rotation.blocks_until_eligible(&builder1), Some(0));
    }

    #[test]
    fn test_filter_eligible_builders() {
        let mut rotation = BuilderRotation::new(5);
        let builders = vec![
            Address::from([1u8; 32]),
            Address::from([2u8; 32]),
            Address::from([3u8; 32]),
        ];

        rotation.update_height(100);
        
        // All builders initially eligible
        let eligible = rotation.filter_eligible_builders(&builders);
        assert_eq!(eligible.len(), 3);

        // Put builder 1 in timeout
        rotation.record_success(builders[0].clone(), 100).unwrap();
        let eligible = rotation.filter_eligible_builders(&builders);
        assert_eq!(eligible.len(), 2);
        assert!(!eligible.contains(&builders[0]));

        // Put builder 2 in timeout
        rotation.record_success(builders[1].clone(), 101).unwrap();
        let eligible = rotation.filter_eligible_builders(&builders);
        assert_eq!(eligible.len(), 1);
        assert_eq!(eligible[0], builders[2]);
    }

    #[test]
    fn test_builder_selector() {
        let mut selector = BuilderSelector::new(10);
        let builders = vec![
            Address::from([1u8; 32]),
            Address::from([2u8; 32]),
            Address::from([3u8; 32]),
        ];

        selector.update(100);
        
        // Should select from all builders
        let selected = selector.select_builder(&builders, 100);
        assert!(selected.is_some());
        assert!(builders.contains(&selected.unwrap()));

        // Record success for selected builder
        if let Some(builder) = selected {
            selector.record_success(builder.clone(), 100).unwrap();
            
            // That builder should not be selected again
            let next_selected = selector.select_builder(&builders, 101);
            assert!(next_selected.is_some());
            assert_ne!(next_selected.unwrap(), builder);
        }
    }
}