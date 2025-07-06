// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/token/ERC20/extensions/ERC20Burnable.sol";
import "@openzeppelin/contracts/token/ERC20/extensions/ERC20Snapshot.sol";
import "@openzeppelin/contracts/access/AccessControl.sol";
import "@openzeppelin/contracts/security/Pausable.sol";
import "@openzeppelin/contracts/security/ReentrancyGuard.sol";

/**
 * @title SELF Token
 * @dev ERC20 Token with staking, burn mechanics, and tier system for SELF ecosystem
 * @notice This token serves as the foundation for the SELF ecosystem with built-in
 * staking mechanics that don't require token transfers (non-custodial staking)
 */
contract SELFToken is ERC20, ERC20Burnable, ERC20Snapshot, AccessControl, Pausable, ReentrancyGuard {
    // Role definitions
    bytes32 public constant SNAPSHOT_ROLE = keccak256("SNAPSHOT_ROLE");
    bytes32 public constant PAUSER_ROLE = keccak256("PAUSER_ROLE");
    bytes32 public constant TREASURY_ROLE = keccak256("TREASURY_ROLE");

    // Token configuration
    uint256 public constant INITIAL_SUPPLY = 500_000_000 * 10**18; // 500 million tokens
    uint256 public constant MAX_SUPPLY = 500_000_000 * 10**18; // Maximum supply cap
    uint256 public constant TGE_SUPPLY = 78_000_000 * 10**18; // 78M at TGE (16%)

    // Staking tiers (in tokens)
    uint256 public constant PIONEER_TIER = 1_000 * 10**18;        // 1,000 SELF
    uint256 public constant EXPLORER_TIER = 10_000 * 10**18;      // 10,000 SELF
    uint256 public constant MULTIVERSE_TIER = 100_000 * 10**18;   // 100,000 SELF

    // Staking information
    struct StakeInfo {
        uint256 amount;
        uint256 timestamp;
        uint256 tier;
        uint256 accumulatedRewards;
    }

    mapping(address => StakeInfo) public stakes;
    uint256 public totalStaked;

    // Subscription information
    struct Subscription {
        uint256 tier;
        uint256 expiresAt;
        uint256 shinePercentUsed; // Usage tracking (0-10000 = 0-100%)
    }

    mapping(address => Subscription) public subscriptions;

    // Reward pools
    uint256 public stakingRewardPool;
    uint256 public subscriptionRebatePool;

    // Events
    event Staked(address indexed user, uint256 amount, uint256 tier);
    event Unstaked(address indexed user, uint256 amount);
    event RewardsClaimed(address indexed user, uint256 amount);
    event SubscriptionPurchased(address indexed user, uint256 tier, uint256 duration);
    event BuybackExecuted(uint256 burned, uint256 toStaking, uint256 toRebates);
    event ShineUsageUpdated(address indexed user, uint256 newUsage);

    constructor() ERC20("SELF", "SELF") {
        _grantRole(DEFAULT_ADMIN_ROLE, msg.sender);
        _grantRole(SNAPSHOT_ROLE, msg.sender);
        _grantRole(PAUSER_ROLE, msg.sender);
        _grantRole(TREASURY_ROLE, msg.sender);

        // Mint initial supply to deployer
        _mint(msg.sender, INITIAL_SUPPLY);
    }

    // Staking functions (non-custodial - tokens stay in user wallet)
    function stake(uint256 amount) external whenNotPaused nonReentrant {
        require(amount > 0, "Cannot stake 0");
        require(balanceOf(msg.sender) >= amount, "Insufficient balance");

        StakeInfo storage userStake = stakes[msg.sender];
        
        // Claim any pending rewards first
        if (userStake.amount > 0) {
            _claimRewards(msg.sender);
        }

        userStake.amount += amount;
        userStake.timestamp = block.timestamp;
        totalStaked += amount;

        // Update tier
        uint256 newTier = _calculateTier(userStake.amount);
        userStake.tier = newTier;

        emit Staked(msg.sender, amount, newTier);
    }

    function unstake(uint256 amount) external nonReentrant {
        StakeInfo storage userStake = stakes[msg.sender];
        require(amount > 0, "Cannot unstake 0");
        require(userStake.amount >= amount, "Insufficient staked amount");

        // Claim rewards first
        _claimRewards(msg.sender);

        userStake.amount -= amount;
        totalStaked -= amount;

        // Update tier
        uint256 newTier = _calculateTier(userStake.amount);
        userStake.tier = newTier;

        emit Unstaked(msg.sender, amount);
    }

    function claimRewards() external nonReentrant {
        _claimRewards(msg.sender);
    }

    function _claimRewards(address user) internal {
        StakeInfo storage userStake = stakes[user];
        uint256 rewards = calculateRewards(user);
        
        if (rewards > 0 && stakingRewardPool >= rewards) {
            stakingRewardPool -= rewards;
            _mint(user, rewards);
            userStake.timestamp = block.timestamp;
            userStake.accumulatedRewards += rewards;
            
            emit RewardsClaimed(user, rewards);
        }
    }

    function calculateRewards(address user) public view returns (uint256) {
        StakeInfo memory userStake = stakes[user];
        if (userStake.amount == 0) return 0;

        uint256 timeStaked = block.timestamp - userStake.timestamp;
        
        // Base APR is 8% (no lockup rate)
        uint256 baseAPR = 8;
        
        // For simplicity, using tier-based approximation of lockup periods
        // In production, would track actual lockup periods
        if (userStake.tier >= 3) baseAPR = 22; // Multiverse tier ~12 month rate
        else if (userStake.tier >= 2) baseAPR = 16; // Explorer tier ~6 month rate  
        else if (userStake.tier >= 1) baseAPR = 12; // Pioneer tier ~3 month rate
        
        uint256 reward = (userStake.amount * timeStaked * baseAPR) / (365 days * 100);
        return reward;
    }

    function _calculateTier(uint256 amount) internal pure returns (uint256) {
        if (amount >= MULTIVERSE_TIER) return 3;
        if (amount >= EXPLORER_TIER) return 2;
        if (amount >= PIONEER_TIER) return 1;
        return 0;
    }

    // Subscription functions
    function purchaseSubscription(uint256 tier, uint256 duration) external whenNotPaused {
        require(tier > 0 && tier <= 3, "Invalid tier");
        require(duration >= 30 days, "Minimum 30 days");

        uint256 cost = _calculateSubscriptionCost(tier, duration);
        require(balanceOf(msg.sender) >= cost, "Insufficient balance");

        // Burn 50% of subscription cost, 50% to treasury
        uint256 burnAmount = cost / 2;
        _burn(msg.sender, burnAmount);
        _transfer(msg.sender, address(this), cost - burnAmount);

        subscriptions[msg.sender] = Subscription({
            tier: tier,
            expiresAt: block.timestamp + duration,
            shinePercentUsed: 0
        });

        emit SubscriptionPurchased(msg.sender, tier, duration);
    }

    function _calculateSubscriptionCost(uint256 tier, uint256 duration) internal pure returns (uint256) {
        uint256 baseCost = 100 * 10**18; // 100 SELF base
        uint256 tierMultiplier = tier; // 1x, 2x, 3x
        uint256 durationDays = duration / 1 days;
        
        return (baseCost * tierMultiplier * durationDays) / 30; // Price per 30 days
    }

    // Buyback and distribution
    function executeBuyback() external onlyRole(TREASURY_ROLE) {
        uint256 contractBalance = balanceOf(address(this));
        require(contractBalance > 0, "No funds for buyback");

        // Distribution: 40% burn, 40% staking rewards, 20% subscription rebates
        uint256 burnAmount = (contractBalance * 40) / 100;
        uint256 stakingAmount = (contractBalance * 40) / 100;
        uint256 rebateAmount = contractBalance - burnAmount - stakingAmount;

        _burn(address(this), burnAmount);
        stakingRewardPool += stakingAmount;
        subscriptionRebatePool += rebateAmount;

        emit BuybackExecuted(burnAmount, stakingAmount, rebateAmount);
    }

    // Usage tracking
    function updateShineUsage(address user, uint256 usage) external onlyRole(TREASURY_ROLE) {
        require(usage <= 10000, "Usage cannot exceed 100%");
        subscriptions[user].shinePercentUsed = usage;
        emit ShineUsageUpdated(user, usage);
    }

    // View functions
    function getUserTier(address user) external view returns (uint256) {
        // Check subscription tier first, then staking tier
        if (subscriptions[user].expiresAt > block.timestamp) {
            return subscriptions[user].tier;
        }
        return stakes[user].tier;
    }

    function isSubscriptionActive(address user) external view returns (bool) {
        return subscriptions[user].expiresAt > block.timestamp;
    }

    // Admin functions
    function snapshot() external onlyRole(SNAPSHOT_ROLE) {
        _snapshot();
    }

    function pause() external onlyRole(PAUSER_ROLE) {
        _pause();
    }

    function unpause() external onlyRole(PAUSER_ROLE) {
        _unpause();
    }

    // Required overrides
    function _beforeTokenTransfer(
        address from,
        address to,
        uint256 amount
    ) internal override(ERC20, ERC20Snapshot) whenNotPaused {
        // Prevent transfer of staked tokens
        if (from != address(0)) { // Not minting
            require(balanceOf(from) - stakes[from].amount >= amount, "Cannot transfer staked tokens");
        }
        super._beforeTokenTransfer(from, to, amount);
    }

    // Emergency functions
    function emergencyWithdraw(address token) external onlyRole(DEFAULT_ADMIN_ROLE) {
        if (token == address(0)) {
            payable(msg.sender).transfer(address(this).balance);
        } else {
            IERC20(token).transfer(msg.sender, IERC20(token).balanceOf(address(this)));
        }
    }

    // Receive ETH for buybacks
    receive() external payable {}
}