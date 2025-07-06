// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";

interface ISELFToken is IERC20 {
    // Staking functions
    function stake(uint256 amount) external;
    function unstake(uint256 amount) external;
    function claimRewards() external;
    function calculateRewards(address user) external view returns (uint256);
    
    // Subscription functions
    function purchaseSubscription(uint256 tier, uint256 duration) external;
    function isSubscriptionActive(address user) external view returns (bool);
    function getUserTier(address user) external view returns (uint256);
    
    // View functions
    function totalStaked() external view returns (uint256);
    
    // Events
    event Staked(address indexed user, uint256 amount, uint256 tier);
    event Unstaked(address indexed user, uint256 amount);
    event RewardsClaimed(address indexed user, uint256 amount);
    event SubscriptionPurchased(address indexed user, uint256 tier, uint256 duration);
}