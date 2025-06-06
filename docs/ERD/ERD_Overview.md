# User Layer
user_node [icon: computer, color: blue] {
    cloud_node string
    SELF_assistant string
    PoAI_validator string
    SDK string
}

# SELF Assistant Layer
SELF_assistant [icon: robot, color: blue] {
    user_interface string
    AI_context string
    PoAI string
    SDK_integration string
}

# SDK Layer
SDK [icon: code, color: purple] {
    multi_language string
    token_operations string
    storage string
    SELF_assistant string
}

# dApp Layer
dApp [icon: app, color: orange] {
    SDK string
    SELF_assistant string
    token string
    storage string
}

# SDK Components
SDK_multi_language [icon: code, color: purple] {
    java string
    kotlin string
    python string
    javascript string
    go string
    solidity string
}

SDK_token [icon: coin, color: purple] {
    SC20 string
    SC721 string
    token_operations string
    smart_contract string
}

SDK_storage [icon: cloud, color: purple] {
    IPFS string
    OrbitDB string
    cross_chain string
}

SDK_AI [icon: robot, color: purple] {
    Ollama string
    AI_context string
    SELF_assistant string
}

# Smart Contract Layer
smart_contract [icon: contract, color: purple] {
    solidity string
    token_creation string
    token_operations string
    storage_operations string
}

# Smart Contract Components
smart_contract_solidity [icon: code, color: purple] {
    token_factory string
    token_management string
    storage_management string
    cross_chain string
}

# Smart Contract Relationships
smart_contract.solidity > smart_contract_solidity.token_factory
smart_contract.token_creation > SDK_token.SC20
smart_contract.token_creation > SDK_token.SC721
smart_contract.storage_operations > SDK_storage

# SDK Relationships
SDK.multi_language > SDK_multi_language.solidity
SDK.token_operations > smart_contract.token_creation
SDK.storage > smart_contract.storage_operations

# dApp Relationships
dApp.token > smart_contract.token_creation
dApp.storage > smart_contract.storage_operations

# Smart Contract Components
smart_contract {
    solidity string
    token_creation string
    token_operations string
    storage_operations string
}

# Smart Contract Solidity
smart_contract_solidity {
    token_factory string
    token_management string
    storage_management string
    cross_chain string
}

# dApp Components
dApp_token [icon: coin, color: orange] {
    SC20 string
    SC721 string
    token_operations string
}

dApp_storage [icon: cloud, color: orange] {
    IPFS string
    OrbitDB string
    cross_chain string
}

dApp_AI [icon: robot, color: orange] {
    SELF_assistant string
    Ollama string
    AI_context string
}

# Relationships
# SDK Relationships
SDK.multi_language > SDK_multi_language.java
SDK.multi_language > SDK_multi_language.kotlin
SDK.multi_language > SDK_multi_language.python
SDK.multi_language > SDK_multi_language.javascript
SDK.multi_language > SDK_multi_language.go

SDK.token_operations > SDK_token.SC20
SDK.token_operations > SDK_token.SC721

SDK.storage > SDK_storage.IPFS
SDK.storage > SDK_storage.OrbitDB
SDK.storage > SDK_storage.cross_chain

SDK.AI > SDK_AI.Ollama
SDK.AI > SDK_AI.AI_context
SDK.AI > SDK_AI.SELF_assistant

# dApp Relationships
dApp.SDK > SDK_multi_language

# SELF Assistant Integration
dApp.SELF_assistant > SDK_AI.SELF_assistant
dApp.AI > SDK_AI.Ollama
dApp.AI > SDK_AI.AI_context

# Token Integration
dApp.token > SDK_token.SC20
dApp.token > SDK_token.SC721

# Storage Integration
dApp.storage > SDK_storage.IPFS
dApp.storage > SDK_storage.OrbitDB
dApp.storage > SDK_storage.cross_chain

# SDK Integration
SDK_multi_language > SDK_token
SDK_multi_language > SDK_storage
SDK_multi_language > SDK_AI

# dApp Components
dApp {
    SDK string
    SELF_assistant string
    token string
    storage string
}

# SDK Components
SDK {
    multi_language string
    token_operations string
    storage string
    AI string
}

# SDK Multi-Language
SDK_multi_language {
    java string
    kotlin string
    python string
    javascript string
    go string
}

# SDK Token
SDK_token {
    SC20 string
    SC721 string
    token_operations string
}

# SDK Storage
SDK_storage {
    IPFS string
    OrbitDB string
    cross_chain string
}

# SDK AI
SDK_AI {
    Ollama string
    AI_context string
    SELF_assistant string
}

# Blockchain Layer
cloud_node [icon: cloud, color: lightblue] {
    full_node string
    validator string
    block_construct string
    PoAI string
    SELF_Coin string
    TPS string
}

# Grid Compute Layer
grid_compute [icon: grid, color: lightgreen] {
    node_network string
    distributed_compute string
    TPS_optimization string
    resource_pooling string
}

# Node Layer
node_network [icon: network, color: lightgreen] {
    node string
    compute string
    storage string
    bandwidth string
}

# TPS Optimization
TPS_optimization [icon: speed, color: lightgreen] {
    transaction_processing string
    block_validation string
    network_sync string
    resource_allocation string
}

# Resource Pooling
resource_pooling [icon: pool, color: lightgreen] {
    compute string
    storage string
    bandwidth string
    node_resources string
}

# Relationships
# Grid Compute Relationships
cloud_node > grid_compute
grid_compute.node_network > node_network.node
grid_compute.TPS_optimization > TPS_optimization.transaction_processing
grid_compute.resource_pooling > resource_pooling.node_resources

# Node Network Relationships
node_network.node > node_network.compute
node_network.node > node_network.storage
node_network.node > node_network.bandwidth

# TPS Optimization Relationships
TPS_optimization.transaction_processing > cloud_node.block_construct
TPS_optimization.block_validation > cloud_node.validator
TPS_optimization.network_sync > wire_network.sync
TPS_optimization.resource_allocation > resource_pooling.node_resources

# Resource Pooling Relationships
resource_pooling.node_resources > node_network.node
resource_pooling.compute > node_network.compute
resource_pooling.storage > node_network.storage
resource_pooling.bandwidth > node_network.bandwidth

# TPS Components
TPS_optimization {
    transaction_processing string
    block_validation string
    network_sync string
    resource_allocation string
}

# Resource Pooling Components
resource_pooling {
    compute string
    storage string
    bandwidth string
    node_resources string
}

# Node Network Components
node_network {
    node string
    compute string
    storage string
    bandwidth string
}

# Integration Layer
minima_integration [icon: cloud, color: gray] {
    backward_compatibility string
    network_bridge string
    transaction_bridge string
}

rosetta_integration [icon: cloud, color: blue] {
    api_compatibility string
    transaction_processing string
    validation string
}

wire_network [icon: cloud, color: green] {
    cross_chain string
    validation string
    sync string
}

# SELFSwap Layer
SELFSwap [icon: exchange, color: yellow] {
    DEX string
    liquidity string
    trading string
    cross_chain string
}

# SELFSwap Components
SELFSwap_DEX [icon: exchange, color: yellow] {
    order_book string
    matching string
    settlement string
    cross_chain string
}

SELFSwap_liquidity [icon: pool, color: yellow] {
    pool string
    staking string
    rewards string
    cross_chain string
}

SELFSwap_trading [icon: chart, color: yellow] {
    spot string
    limit string
    market string
    cross_chain string
}

# SELFSwap Relationships
SELFSwap.DEX > SELFSwap_DEX.order_book
SELFSwap.liquidity > SELFSwap_liquidity.pool
SELFSwap.trading > SELFSwap_trading.spot
SELFSwap.cross_chain > SELFSwap_DEX.cross_chain
SELFSwap.cross_chain > SELFSwap_liquidity.cross_chain
SELFSwap.cross_chain > SELFSwap_trading.cross_chain

# Integration Relationships
cloud_node > SELFSwap
SELFSwap_DEX > SDK_token.SC20
SELFSwap_DEX > SDK_token.SC721
SELFSwap_liquidity > SDK_storage
SELFSwap_trading > SDK_AI
SELFSwap.cross_chain > wire_network

# SELFSwap Components
SELFSwap {
    DEX string
    liquidity string
    trading string
    cross_chain string
}

# SELFSwap DEX
SELFSwap_DEX {
    order_book string
    matching string
    settlement string
    cross_chain string
}

# SELFSwap Liquidity
SELFSwap_liquidity {
    pool string
    staking string
    rewards string
    cross_chain string
}

# SELFSwap Trading
SELFSwap_trading {
    spot string
    limit string
    market string
    cross_chain string
}

# Relationships
# Integration Relationships
cloud_node > minima_integration
cloud_node > rosetta_integration
cloud_node > wire_network

# PoAI and SELF Coin
PoAI.reference_block > cloud_node.SELF_Coin
PoAI.block_builder > cloud_node.SELF_Coin
PoAI.validator > cloud_node.SELF_Coin

# Integration with Minima
minima_integration.backward_compatibility > cloud_node.full_node
minima_integration.network_bridge > cloud_node.validator
minima_integration.transaction_bridge > cloud_node.block_construct

# Integration with Rosetta
rosetta_integration.api_compatibility > cloud_node.full_node
rosetta_integration.transaction_processing > cloud_node.block_construct
rosetta_integration.validation > cloud_node.validator

# Integration with Wire Network
wire_network.cross_chain > cloud_node.block_construct
wire_network.validation > cloud_node.validator
wire_network.sync > cloud_node.full_node

# SELF Coin Relationships
SELF_Coin {
    PoAI_reward string
    transaction_fee string
    point_value string
    point_price string
    point_halving string
}

# Point System
point_system {
    point_value string
    point_price string
    point_halving string
    point_data string
}

# Relationships
# Point System Relationships
point_system.point_value > SELF_Coin.PoAI_reward
point_system.point_price > SELF_Coin.transaction_fee
point_system.point_halving > SELF_Coin.point_halving
point_system.point_data > block_builder.efficiency_calculator

# PoAI and Points
PoAI.reference_block > point_system.point_value
PoAI.block_builder > point_system.point_price
PoAI.validator > point_system.point_data

# Point Halving
point_halving {
    milestone_1 string
    milestone_2 string
    point_value string
    point_price string
}

# Point Halving Milestones
milestone_1 {
    total_points string
    point_value string
    point_price string
}

milestone_2 {
    total_points string
    point_value string
    point_price string
}

# Integration Entities
minima_integration {
    backward_compatibility string
    network_bridge string
    transaction_bridge string
}

rosetta_integration {
    api_compatibility string
    transaction_processing string
    validation string
}

wire_network {
    cross_chain string
    validation string
    sync string
}

# AI Layer
PoAI [icon: robot, color: purple] {
    ML_model string
    block_builder string
    validator string
    reference_block string
}

block_builder [icon: robot, color: blue] {
    ML_model string
    transaction_sorter string
    efficiency_calculator string
    point_price string
}

AI_validator [icon: robot, color: green] {
    wallet_color string
    hex_validator string
    voting_power string
}

# PoAI Components
ML_model [icon: database, color: purple] {
    training string
    efficiency string
    validation string
}

voting [icon: users, color: orange] {
    block_efficiency string
    point_price string
    hex_validation string
}

# PoAI Relationships
PoAI.ML_model > ML_model.training
PoAI.block_builder > block_builder.ML_model
PoAI.validator > AI_validator.wallet_color
PoAI.reference_block > block_builder.efficiency_calculator

block_builder.transaction_sorter > ML_model.efficiency
block_builder.point_price > voting.block_efficiency

AI_validator.hex_validator > voting.hex_validation
AI_validator.voting_power > voting.block_efficiency

ML_model.validation > voting.block_efficiency
ML_model.efficiency > block_builder.transaction_sorter

# PoAI Voting Process
voting {
    block_efficiency string
    point_price string
    hex_validation string
    final_score string
}

# PoAI Block Building
block_builder {
    transaction_sorting string
    efficiency_calculation string
    point_price_calculation string
    reference_block string
}

# PoAI Validation
AI_validator {
    hex_validation string
    color_update string
    voting string
    block_validation string
}

# Storage Layer
cloud_storage [icon: cloud, color: green] {
    IPFS string
    OrbitDB string
    cross_chain string
}

# Relationships
# User Node Relationships
user_node.cloud_node > cloud_node.full_node
user_node.SELF_assistant > SELF_assistant.Ollama
user_node.PoAI_validator > PoAI.Ollama_Cloud

# Cloud Node Relationships
cloud_node.full_node > cloud_node.validator
cloud_node.validator > PoAI.consensus
cloud_node.block_construct > blockchain.block

# AI Relationships
PoAI.Ollama_Cloud > SELF_assistant.Ollama
PoAI.validation > blockchain.validator_signatures

# Storage Relationships
cloud_storage.IPFS > cloud_storage.cross_chain
cloud_storage.OrbitDB > cloud_storage.cross_chain

# Cross-layer relationships
user_node > cloud_node
user_node > SELF_assistant
user_node > PoAI
user_node > cloud_storage

# Cloud-first Architecture
cloud_node > cloud_storage
cloud_node > PoAI
cloud_storage > PoAI

# Entity relationships
block { 
    transactions string
    validator_signatures string
    cross_chain_data string
}

transaction {
    token_operations string
    storage_operations string
    cross_chain_transfers string
}

file {
    ipfs_content string
    cross_chain_references string
    version_history string
}

database {
    validator_data string
    transaction_metadata string
    cross_chain_sync string
}

validator {
    training_status string
    voting_power string
    reputation_score string
    PoAI_score string
}

proposal {
    evaluation_metrics string
    voting_results string
    ai_validation string
    PoAI_validation string
}

vote {
    validator_participation string
    weight_calculation string
    result_aggregation string
    PoAI_weight string
}

# Integration entities
PoAI_integration {
    consensus string
    validation string
    scoring string
}

cloud_storage {
    IPFS string
    OrbitDB string
    cross_chain string
}

security {
    PoAI string
    access_control string
    data_integrity string
}

# Entity relationships
block { 
    transactions string
    validator_signatures string
    cross_chain_data string
}

transaction {
    token_operations string
    storage_operations string
    cross_chain_transfers string
}

file {
    ipfs_content string
    cross_chain_references string
    version_history string
}

database {
    validator_data string
    transaction_metadata string
    cross_chain_sync string
}

validator {
    training_status string
    voting_power string
    reputation_score string
    PoAI_score string
}

proposal {
    evaluation_metrics string
    voting_results string
    ai_validation string
    PoAI_validation string
}

vote {
    validator_participation string
    weight_calculation string
    result_aggregation string
    PoAI_weight string
}

# Integration entities
PoAI_integration {
    consensus string
    validation string
    scoring string
}

storage_sync {
    IPFS string
    OrbitDB string
    cross_chain string
}

security {
    PoAI string
    access_control string
    data_integrity string
}

This ERD overview provides a high-level view of the SELF Chain architecture, focusing on entity relationships and integration points without delving into specific attributes or detailed data structures.
