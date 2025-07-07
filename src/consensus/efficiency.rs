use crate::blockchain::{Block, Transaction};
use anyhow::Result;
use serde::{Deserialize, Serialize};

/// Efficiency coefficient for block evaluation in PoAI
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct EfficiencyCoefficient {
    /// Raw efficiency score (0.0 to 1.0)
    pub score: f64,
    /// Total input value in the block
    pub total_input: u64,
    /// Total output value in the block
    pub total_output: u64,
    /// Efficiency percentage
    pub efficiency_percentage: f64,
    /// Block utilization percentage
    pub utilization_percentage: f64,
}

/// Calculates efficiency coefficients for blocks according to PoAI specification
pub struct EfficiencyCalculator {
    /// Maximum block size in bytes
    max_block_size: usize,
    /// Minimum efficiency threshold
    min_efficiency_threshold: f64,
}

impl EfficiencyCalculator {
    pub fn new(max_block_size: usize, min_efficiency_threshold: f64) -> Self {
        Self {
            max_block_size,
            min_efficiency_threshold,
        }
    }

    /// Calculate the efficiency coefficient for a block
    /// According to PoAI: Efficiency = (Input - Output) / Input
    pub fn calculate_efficiency(&self, block: &Block) -> Result<EfficiencyCoefficient> {
        let mut total_input = 0u64;
        let mut total_output = 0u64;
        let mut useful_data_size = 0usize;

        // Calculate total inputs and outputs from all transactions
        for tx in &block.transactions {
            let (input, output) = self.calculate_transaction_io(tx)?;
            total_input += input;
            total_output += output;
            
            // Calculate useful data size (transaction data minus overhead)
            useful_data_size += self.calculate_useful_data_size(tx);
        }

        // Calculate efficiency as (Input - Output) / Input
        // This represents the value consumed/burned in the block
        let efficiency_percentage = if total_input > 0 {
            ((total_input - total_output) as f64 / total_input as f64) * 100.0
        } else {
            0.0
        };

        // Calculate block utilization
        let block_size = self.calculate_block_size(block);
        let utilization_percentage = (useful_data_size as f64 / self.max_block_size as f64) * 100.0;

        // Combined efficiency score (0.0 to 1.0)
        // Weighs both value efficiency and space utilization
        let score = (efficiency_percentage / 100.0) * 0.7 + (utilization_percentage / 100.0) * 0.3;

        Ok(EfficiencyCoefficient {
            score,
            total_input,
            total_output,
            efficiency_percentage,
            utilization_percentage,
        })
    }

    /// Calculate input and output for a transaction
    fn calculate_transaction_io(&self, tx: &Transaction) -> Result<(u64, u64)> {
        // In a real implementation, this would calculate from UTXOs
        // For now, we use amount as output and fee as the consumed input
        let output = tx.amount;
        let fee = tx.fee.unwrap_or(0);
        let input = output + fee;
        
        Ok((input, output))
    }

    /// Calculate useful data size for a transaction
    fn calculate_useful_data_size(&self, tx: &Transaction) -> usize {
        // Useful data includes:
        // - Transaction data
        // - Signature
        // - Essential metadata
        // But excludes protocol overhead
        
        let data_size = tx.data.as_ref().map(|d| d.len()).unwrap_or(0);
        let signature_size = tx.signature.as_ref().map(|s| s.len()).unwrap_or(0);
        
        // Base transaction size (from, to, amount, nonce)
        let base_size = 32 + 32 + 8 + 8; // addresses + amount + nonce
        
        base_size + data_size + signature_size
    }

    /// Calculate total block size
    fn calculate_block_size(&self, block: &Block) -> usize {
        // This is a simplified calculation
        // In reality, would serialize the block and measure bytes
        let header_size = 200; // Approximate header size
        let tx_size: usize = block.transactions.iter()
            .map(|tx| self.calculate_useful_data_size(tx) + 50) // +50 for tx overhead
            .sum();
        
        header_size + tx_size
    }

    /// Check if a block meets the minimum efficiency threshold
    pub fn meets_efficiency_threshold(&self, coefficient: &EfficiencyCoefficient) -> bool {
        coefficient.score >= self.min_efficiency_threshold
    }

    /// Compare two blocks by efficiency
    pub fn compare_blocks(&self, block_a: &Block, block_b: &Block) -> Result<std::cmp::Ordering> {
        let coeff_a = self.calculate_efficiency(block_a)?;
        let coeff_b = self.calculate_efficiency(block_b)?;
        
        Ok(coeff_a.score.partial_cmp(&coeff_b.score).unwrap_or(std::cmp::Ordering::Equal))
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::blockchain::Address;

    fn create_test_transaction(amount: u64, fee: u64) -> Transaction {
        Transaction {
            from: Address::from([1u8; 32]),
            to: Some(Address::from([2u8; 32])),
            amount,
            fee: Some(fee),
            nonce: 1,
            signature: Some(vec![0u8; 64]),
            data: None,
            timestamp: 0,
        }
    }

    fn create_test_block(transactions: Vec<Transaction>) -> Block {
        Block {
            height: 100,
            hash: vec![0u8; 32],
            previous_hash: vec![0u8; 32],
            merkle_root: vec![0u8; 32],
            timestamp: 0,
            transactions,
            validator: Address::from([3u8; 32]),
            signature: vec![0u8; 64],
            difficulty: None,
            cumulative_difficulty: None,
            nonce: None,
            storage_root: None,
            state_root: None,
            receipts_root: None,
            logs_bloom: None,
            extra_data: None,
            gas_limit: None,
            gas_used: None,
            base_fee: None,
            next_validators: None,
            vote_attestations: None,
        }
    }

    #[test]
    fn test_efficiency_calculation() {
        let calculator = EfficiencyCalculator::new(1_000_000, 0.1);
        
        // Create a block with transactions that consume fees
        let transactions = vec![
            create_test_transaction(1000, 50),  // Input: 1050, Output: 1000
            create_test_transaction(2000, 100), // Input: 2100, Output: 2000
            create_test_transaction(500, 25),   // Input: 525, Output: 500
        ];
        
        let block = create_test_block(transactions);
        let coefficient = calculator.calculate_efficiency(&block).unwrap();
        
        // Total Input: 1050 + 2100 + 525 = 3675
        // Total Output: 1000 + 2000 + 500 = 3500
        // Efficiency: (3675 - 3500) / 3675 = 175 / 3675 ≈ 4.76%
        assert_eq!(coefficient.total_input, 3675);
        assert_eq!(coefficient.total_output, 3500);
        assert!((coefficient.efficiency_percentage - 4.76).abs() < 0.1);
    }

    #[test]
    fn test_efficiency_threshold() {
        let calculator = EfficiencyCalculator::new(1_000_000, 0.5);
        
        let coefficient = EfficiencyCoefficient {
            score: 0.6,
            total_input: 1000,
            total_output: 900,
            efficiency_percentage: 10.0,
            utilization_percentage: 80.0,
        };
        
        assert!(calculator.meets_efficiency_threshold(&coefficient));
        
        let low_coefficient = EfficiencyCoefficient {
            score: 0.3,
            total_input: 1000,
            total_output: 990,
            efficiency_percentage: 1.0,
            utilization_percentage: 20.0,
        };
        
        assert!(!calculator.meets_efficiency_threshold(&low_coefficient));
    }

    #[test]
    fn test_block_comparison() {
        let calculator = EfficiencyCalculator::new(1_000_000, 0.1);
        
        // High efficiency block (more fees consumed)
        let high_eff_txs = vec![
            create_test_transaction(1000, 100),
            create_test_transaction(2000, 200),
        ];
        let high_eff_block = create_test_block(high_eff_txs);
        
        // Low efficiency block (less fees consumed)
        let low_eff_txs = vec![
            create_test_transaction(1000, 10),
            create_test_transaction(2000, 20),
        ];
        let low_eff_block = create_test_block(low_eff_txs);
        
        let ordering = calculator.compare_blocks(&high_eff_block, &low_eff_block).unwrap();
        assert_eq!(ordering, std::cmp::Ordering::Greater);
    }
}