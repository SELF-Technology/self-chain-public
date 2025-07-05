// Test modules for blockchain operations
// Export the BlockchainArcExt trait so it's available to all test modules
pub mod blockchain_arc_ext;
pub use blockchain_arc_ext::*;

pub mod mock_network;
pub mod blockchain_operations_test;
pub mod synchronization_test;
pub mod poai_consensus_test;
