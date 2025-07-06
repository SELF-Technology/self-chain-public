// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol";
import "@openzeppelin/contracts/access/AccessControl.sol";
import "@openzeppelin/contracts/security/ReentrancyGuard.sol";
import "@openzeppelin/contracts/security/Pausable.sol";

/**
 * @title SELF Bonus Staking Program
 * @dev Special staking program for early investors with bonus tokens and enhanced APR
 * @notice Only available for investors who lock tokens before exchange listing
 */
contract SELFBonusStaking is AccessControl, ReentrancyGuard, Pausable {
    using SafeERC20 for IERC20;

    IERC20 public immutable selfToken;
    
    // Roles
    bytes32 public constant ADMIN_ROLE = keccak256("ADMIN_ROLE");
    bytes32 public constant WHITELIST_ROLE = keccak256("WHITELIST_ROLE");

    // Bonus staking tiers
    enum BonusTier {
        ONE_MONTH,    // 10% bonus tokens, 120% APR
        THREE_MONTHS, // 20% bonus tokens, 80% APR
        SIX_MONTHS    // 30% bonus tokens, 60% APR
    }

    struct BonusStake {
        uint256 principal;       // Original staked amount
        uint256 bonusTokens;     // Bonus tokens added (10/20/30%)
        uint256 totalStaked;     // Principal + bonus
        uint256 startTime;       // Stake timestamp
        uint256 lockupEnd;       // When tokens can be withdrawn
        BonusTier tier;          // Staking tier
        uint256 lastRewardClaim; // Last time rewards were claimed
        uint256 accumulatedRewards; // Total rewards earned
        bool withdrawn;          // Whether stake has been withdrawn
    }

    // User stakes
    mapping(address => BonusStake[]) public userStakes;
    
    // Whitelist for bonus program
    mapping(address => bool) public whitelist;
    
    // Total staked in bonus program
    uint256 public totalBonusStaked;
    
    // Program end time (e.g., 25% of seed allocation at TGE)
    uint256 public programEndTime;
    
    // Bonus rates (in basis points)
    uint256 public constant ONE_MONTH_BONUS = 1000;    // 10%
    uint256 public constant THREE_MONTH_BONUS = 2000;  // 20%
    uint256 public constant SIX_MONTH_BONUS = 3000;    // 30%
    
    // APR rates (in basis points)
    uint256 public constant ONE_MONTH_APR = 12000;     // 120%
    uint256 public constant THREE_MONTH_APR = 8000;    // 80%
    uint256 public constant SIX_MONTH_APR = 6000;      // 60%
    
    // Events
    event Staked(address indexed user, uint256 principal, uint256 bonus, BonusTier tier);
    event Withdrawn(address indexed user, uint256 amount, uint256 stakeIndex);
    event RewardsClaimed(address indexed user, uint256 amount);
    event UserWhitelisted(address indexed user);
    event ProgramEndTimeSet(uint256 endTime);

    constructor(address _selfToken) {
        require(_selfToken != address(0), "Invalid token address");
        selfToken = IERC20(_selfToken);
        
        _grantRole(DEFAULT_ADMIN_ROLE, msg.sender);
        _grantRole(ADMIN_ROLE, msg.sender);
        _grantRole(WHITELIST_ROLE, msg.sender);
    }

    /**
     * @dev Set program end time (when bonus staking closes)
     */
    function setProgramEndTime(uint256 _endTime) external onlyRole(ADMIN_ROLE) {
        require(_endTime > block.timestamp, "End time must be future");
        require(programEndTime == 0, "Already set");
        programEndTime = _endTime;
        emit ProgramEndTimeSet(_endTime);
    }

    /**
     * @dev Add users to whitelist
     */
    function addToWhitelist(address[] calldata users) external onlyRole(WHITELIST_ROLE) {
        for (uint256 i = 0; i < users.length; i++) {
            whitelist[users[i]] = true;
            emit UserWhitelisted(users[i]);
        }
    }

    /**
     * @dev Stake tokens with bonus
     */
    function stake(uint256 amount, BonusTier tier) external nonReentrant whenNotPaused {
        require(whitelist[msg.sender], "Not whitelisted");
        require(programEndTime == 0 || block.timestamp < programEndTime, "Program ended");
        require(amount > 0, "Amount must be > 0");

        // Calculate bonus tokens
        uint256 bonusPercent;
        uint256 lockupDuration;
        
        if (tier == BonusTier.ONE_MONTH) {
            bonusPercent = ONE_MONTH_BONUS;
            lockupDuration = 30 days;
        } else if (tier == BonusTier.THREE_MONTHS) {
            bonusPercent = THREE_MONTH_BONUS;
            lockupDuration = 90 days;
        } else {
            bonusPercent = SIX_MONTH_BONUS;
            lockupDuration = 180 days;
        }

        uint256 bonusTokens = (amount * bonusPercent) / 10000;
        uint256 totalStake = amount + bonusTokens;

        // Create stake record
        BonusStake memory newStake = BonusStake({
            principal: amount,
            bonusTokens: bonusTokens,
            totalStaked: totalStake,
            startTime: block.timestamp,
            lockupEnd: block.timestamp + lockupDuration,
            tier: tier,
            lastRewardClaim: block.timestamp,
            accumulatedRewards: 0,
            withdrawn: false
        });

        userStakes[msg.sender].push(newStake);
        totalBonusStaked += totalStake;

        // Transfer principal from user
        selfToken.safeTransferFrom(msg.sender, address(this), amount);
        
        // Note: Bonus tokens should be minted or transferred from treasury
        // This is a simplified version - in production, implement proper bonus token sourcing
        
        emit Staked(msg.sender, amount, bonusTokens, tier);
    }

    /**
     * @dev Calculate pending rewards for a stake
     */
    function calculateRewards(address user, uint256 stakeIndex) public view returns (uint256) {
        require(stakeIndex < userStakes[user].length, "Invalid index");
        BonusStake memory userStake = userStakes[user][stakeIndex];
        
        if (userStake.withdrawn) return 0;

        uint256 timeElapsed = block.timestamp - userStake.lastRewardClaim;
        uint256 apr;

        if (userStake.tier == BonusTier.ONE_MONTH) {
            apr = ONE_MONTH_APR;
        } else if (userStake.tier == BonusTier.THREE_MONTHS) {
            apr = THREE_MONTH_APR;
        } else {
            apr = SIX_MONTH_APR;
        }

        // Calculate rewards on total staked (principal + bonus)
        uint256 rewards = (userStake.totalStaked * timeElapsed * apr) / (365 days * 10000);
        return rewards;
    }

    /**
     * @dev Claim rewards for a specific stake
     */
    function claimRewards(uint256 stakeIndex) external nonReentrant {
        require(stakeIndex < userStakes[msg.sender].length, "Invalid index");
        BonusStake storage userStake = userStakes[msg.sender][stakeIndex];
        require(!userStake.withdrawn, "Stake withdrawn");

        uint256 rewards = calculateRewards(msg.sender, stakeIndex);
        require(rewards > 0, "No rewards");

        userStake.lastRewardClaim = block.timestamp;
        userStake.accumulatedRewards += rewards;

        // Transfer rewards
        selfToken.safeTransfer(msg.sender, rewards);
        
        emit RewardsClaimed(msg.sender, rewards);
    }

    /**
     * @dev Withdraw stake after lockup period
     */
    function withdraw(uint256 stakeIndex) external nonReentrant {
        require(stakeIndex < userStakes[msg.sender].length, "Invalid index");
        BonusStake storage userStake = userStakes[msg.sender][stakeIndex];
        
        require(!userStake.withdrawn, "Already withdrawn");
        require(block.timestamp >= userStake.lockupEnd, "Still locked");

        // Claim any pending rewards first
        uint256 pendingRewards = calculateRewards(msg.sender, stakeIndex);
        if (pendingRewards > 0) {
            userStake.accumulatedRewards += pendingRewards;
            selfToken.safeTransfer(msg.sender, pendingRewards);
        }

        // Mark as withdrawn
        userStake.withdrawn = true;
        totalBonusStaked -= userStake.totalStaked;

        // Return principal + bonus tokens
        selfToken.safeTransfer(msg.sender, userStake.totalStaked);
        
        emit Withdrawn(msg.sender, userStake.totalStaked, stakeIndex);
    }

    /**
     * @dev Get all stakes for a user
     */
    function getUserStakes(address user) external view returns (BonusStake[] memory) {
        return userStakes[user];
    }

    /**
     * @dev Get active (non-withdrawn) stake count for a user
     */
    function getActiveStakeCount(address user) external view returns (uint256) {
        uint256 count = 0;
        for (uint256 i = 0; i < userStakes[user].length; i++) {
            if (!userStakes[user][i].withdrawn) {
                count++;
            }
        }
        return count;
    }

    /**
     * @dev Emergency functions
     */
    function pause() external onlyRole(ADMIN_ROLE) {
        _pause();
    }

    function unpause() external onlyRole(ADMIN_ROLE) {
        _unpause();
    }

    function emergencyWithdraw(address token) external onlyRole(DEFAULT_ADMIN_ROLE) {
        if (token == address(0)) {
            payable(msg.sender).transfer(address(this).balance);
        } else {
            IERC20(token).safeTransfer(msg.sender, IERC20(token).balanceOf(address(this)));
        }
    }
}