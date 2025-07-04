---
layout: page
title: ERD Overview v2
---

# SELF Chain Architecture

// Custom Icons
icon:custom:SELF-WHEEL-WHITE {
    url: "https://self.app/images/favicon.png"
}

// User Layer
user_node [icon: computer] {
    cloud_node [icon: cloud]
    SELF_assistant [icon: custom:SELF-WHEEL-WHITE]
    PoAI_validator [icon: robot]
    SDK [icon: code]
}

// SELF Assistant Layer
SELF_assistant [icon: custom:SELF-WHEEL-WHITE] {
    user_interface string
    AI_context string
    PoAI string
    SDK_integration string
}

// SDK Layer
SDK [icon: code] {
    SDK_multi_language [icon: aws-codebuild]
    SDK_token [icon: aws-lambda]
    SDK_storage [icon: aws-s3]
    SDK_SELF_assistant [icon: custom:SELF-WHEEL-WHITE]
}

// dApp Layer
dApp [icon: app] {
    dApp_token [icon: aws-lambda]
    dApp_storage [icon: aws-s3]
    dApp_SELF_assistant [icon: custom:SELF-WHEEL-WHITE]
}

// Block Building Layer
block_builder [icon: robot] {
    ML_model [icon: database]
    transaction_sorter [icon: robot]
    efficiency_calculator [icon: robot]
    point_price [icon: robot]
}

// AI Validator Layer
AI_validator [icon: robot] {
    wallet_color [icon: robot]
    hex_validator [icon: robot]
    voting_power [icon: robot]
}

// Smart Contract Layer
smart_contract [icon: code] {
    solidity [icon: code]
    token_creation [icon: aws-lambda]
    token_operations [icon: aws-lambda]
    storage_operations [icon: aws-s3]
}

// SELFSwap Layer
SELFSwap [icon: exchange] {
    DEX [icon: exchange]
    liquidity [icon: pool]
    trading [icon: chart]
    cross_chain [icon: network]
}

// Grid Compute Layer
grid_compute [icon: grid] {
    node_network [icon: network]
    distributed_compute [icon: cpu]
    TPS_optimization [icon: speed]
    resource_pooling [icon: database]
}

// Relationships between layers
user_node -> SDK
user_node -> SELF_assistant
user_node -> PoAI_validator

SDK -> SDK_multi_language
SDK -> SDK_token
SDK -> SDK_storage
SDK -> SDK_SELF_assistant

SELF_assistant -> SA_user_interface
SELF_assistant -> SA_AI_context
SELF_assistant -> SA_PoAI
SELF_assistant -> SA_SDK_integration

SDK -> dApp
SDK -> SELFSwap
SDK -> smart_contract

SELFSwap -> DEX
SELFSwap -> liquidity
SELFSwap -> trading
SELFSwap -> cross_chain

dApp -> dApp_token
dApp -> dApp_storage
dApp -> dApp_SELF_assistant

dApp -> grid_compute
SELFSwap -> grid_compute
smart_contract -> grid_compute

block_builder -> AI_validator
block_builder -> SDK
block_builder -> SELFSwap

AI_validator -> SDK
AI_validator -> SELFSwap
AI_validator -> smart_contract

smart_contract -> SELFSwap
smart_contract -> grid_compute
smart_contract -> SDK

grid_compute -> node_network
grid_compute -> distributed_compute
grid_compute -> TPS_optimization
grid_compute -> resource_pooling
dApp -> dApp_SELF_assistant

// Legend
legend {
    User Layer: user_node
    SDK Layer: SDK
    dApp Layer: dApp
    Block Building: block_builder
    AI Validator: AI_validator
    Smart Contract: smart_contract
    SELFSwap: SELFSwap
    Grid Compute: grid_compute
    SELF Assistant: SELF_assistant
}

// SDK Layer
SDK [icon: code] {
    multi_language [icon: code]
    token_operations [icon: coin]
    storage [icon: database]
    SELF_assistant [icon: custom:SELF-WHEEL-WHITE]
}

// dApp Layer
dApp [icon: app] {
    SDK [icon: code]
    SELF_assistant [icon: custom:SELF-WHEEL-WHITE]
    token [icon: coin]
    storage [icon: database]
}

// Block Building Layer
block_builder [icon: robot] {
    ML_model [icon: database]
    transaction_sorter [icon: robot]
    efficiency_calculator [icon: robot]
    point_price [icon: robot]
}

// AI Validator Layer
AI_validator [icon: robot] {
    wallet_color [icon: robot]
    hex_validator [icon: robot]
    voting_power [icon: robot]
}

// Smart Contract Layer
smart_contract [icon: contract] {
    solidity [icon: code]
    token_creation [icon: coin]
    token_operations [icon: coin]
    storage_operations [icon: database]
}

// SELFSwap Layer
SELFSwap [icon: exchange] {
    DEX [icon: exchange]
    liquidity [icon: pool]
    trading [icon: chart]
    cross_chain [icon: exchange]
}

// Grid Compute Layer
grid_compute [icon: grid] {
    node_network [icon: network]
    distributed_compute [icon: cpu]
    TPS_optimization [icon: speed]
    resource_pooling [icon: pool]
}

// Relationships
user_node -> SDK
user_node -> dApp
SDK -> dApp
block_builder -> AI_validator
smart_contract -> SDK
SELFSwap -> SDK
grid_compute -> cloud_node

// Integration Layer
cloud_node -> minima_integration
cloud_node -> rosetta_integration
cloud_node -> wire_network

// PoAI Relationships
PoAI -> block_builder
PoAI -> AI_validator
PoAI -> cloud_node

// SDK Relationships
SDK -> SDK_multi_language
SDK -> SDK_token
SDK -> SDK_storage
SDK -> SDK_AI

// dApp Relationships
dApp -> dApp_token
dApp -> dApp_storage
dApp -> dApp_AI

// SELFSwap Relationships
SELFSwap -> SELFSwap_DEX
SELFSwap -> SELFSwap_liquidity
SELFSwap -> SELFSwap_trading
SELFSwap -> SELFSwap_cross_chain

// Grid Compute Relationships
grid_compute -> node_network
grid_compute -> TPS_optimization
grid_compute -> resource_pooling

// Style
style user_node fill:#3498db
style SDK fill:#9b59b6
style dApp fill:#e67e22
style block_builder fill:#2ecc71
style AI_validator fill:#3498db
style smart_contract fill:#95a5a6
style SELFSwap fill:#f1c40f
style grid_compute fill:#27ae60

// Legend
legend {
    User Layer: user_node
    SDK Layer: SDK
    dApp Layer: dApp
    Block Building: block_builder
    AI Validator: AI_validator
    Smart Contract: smart_contract
    SELFSwap: SELFSwap
    Grid Compute: grid_compute
}
