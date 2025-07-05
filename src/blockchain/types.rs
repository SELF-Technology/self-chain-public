use serde::{Serialize, Deserialize};
use std::collections::HashMap;
use crate::blockchain::{Block, Transaction};
use crate::consensus::VotingStatus;

#[derive(Debug, Serialize, Deserialize)]
pub struct BlockProposal {
    pub id: u64,
    pub block: Block,
    pub proposer_id: String,
    pub timestamp: u64,
    pub status: ProposalStatus,
    pub votes: HashMap<String, bool>, // validator_id -> vote
}

#[derive(Debug, Serialize, Deserialize)]
pub enum ProposalStatus {
    Pending,
    Accepted,
    Rejected,
    Failed,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct BlockProposalMetrics {
    pub total_proposals: u64,
    pub successful_proposals: u64,
    pub failed_proposals: u64,
    pub avg_proposal_time: u64,
    pub avg_tx_count: u64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct BlockProposalStats {
    pub proposal_id: u64,
    pub proposer_id: String,
    pub block_height: u64,
    pub tx_count: u64,
    pub status: ProposalStatus,
    pub votes_for: u64,
    pub votes_against: u64,
    pub total_votes: u64,
    pub quorum_reached: bool,
    pub validation_time: u64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct BlockProposalConfig {
    pub proposal_window: u64,         // Duration of proposal window in seconds
    pub min_stake: u64,              // Minimum stake required to propose
    pub max_tx_per_block: u32,       // Maximum transactions per block
    pub block_reward: u64,           // Block reward for proposer
    pub proposal_timeout: u64,       // Timeout for proposal acceptance
}

impl Default for BlockProposalConfig {
    fn default() -> Self {
        Self {
            proposal_window: 300,     // 5 minutes
            min_stake: 1000000,      // 1000 SELF tokens
            max_tx_per_block: 100,
            block_reward: 500000,    // 500 SELF tokens
            proposal_timeout: 30,    // 30 seconds
        }
    }
}

#[derive(Debug, Serialize, Deserialize)]
pub struct BlockProposalResult {
    pub proposal_id: u64,
    pub block: Block,
    pub status: ProposalStatus,
    pub votes: HashMap<String, bool>,
    pub quorum_reached: bool,
    pub validation_time: u64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct BlockProposalSummary {
    pub proposal_id: u64,
    pub proposer_id: String,
    pub block_height: u64,
    pub tx_count: u64,
    pub status: ProposalStatus,
    pub votes_for: u64,
    pub votes_against: u64,
    pub total_votes: u64,
    pub quorum_reached: bool,
    pub validation_time: u64,
    pub timestamp: u64,
}
