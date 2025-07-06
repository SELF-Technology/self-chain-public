// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/security/ReentrancyGuard.sol";

/**
 * @title SELF Vesting Contract
 * @dev Manages token vesting for different allocation categories according to tokenomics
 */
contract SELFVesting is Ownable, ReentrancyGuard {
    using SafeERC20 for IERC20;

    IERC20 public immutable selfToken;

    struct VestingSchedule {
        uint256 totalAmount;      // Total tokens to be vested
        uint256 releasedAmount;   // Tokens already released
        uint256 startTime;        // Vesting start time (TGE)
        uint256 cliffDuration;    // Cliff period in seconds
        uint256 vestingDuration;  // Total vesting duration in seconds
        uint256 tgePercent;       // Percentage released at TGE (in basis points, e.g., 2500 = 25%)
        bool revocable;           // Whether vesting can be revoked
        bool revoked;             // Whether vesting has been revoked
    }

    // Allocation categories based on tokenomics
    enum AllocationCategory {
        PreSeed,             // 10% supply, 50% TGE, 10 month vesting
        Seed,                // 2% supply, 25% TGE, 12 month vesting
        Private,             // 2% supply, 25% TGE, 3 month cliff, 15 month vesting
        Public,              // 1% supply, 20% TGE, 1 month cliff, 15 month vesting
        Partners,            // 4% supply, 10% TGE, 3 month cliff, 15 month vesting
        TeamAndAdvisors,     // 10% supply, 0% TGE, 3 month cliff, 36 month vesting
        Liquidity,           // 10% supply, 100% TGE, no vesting
        UserAdoption,        // 15% supply, 10% TGE, 48 month vesting
        DeveloperIncentives, // 15% supply, 10% TGE, 48 month vesting
        Ecosystem,           // 15% supply, 10% TGE, 48 month vesting
        Staking              // 16% supply, 0% TGE, 36 month linear release
    }

    mapping(address => mapping(AllocationCategory => VestingSchedule)) public vestingSchedules;
    
    event VestingScheduleCreated(address indexed beneficiary, AllocationCategory category, uint256 amount);
    event TokensReleased(address indexed beneficiary, AllocationCategory category, uint256 amount);
    event VestingRevoked(address indexed beneficiary, AllocationCategory category);

    constructor(address _selfToken) {
        require(_selfToken != address(0), "Invalid token address");
        selfToken = IERC20(_selfToken);
    }

    /**
     * @dev Create vesting schedule for a beneficiary
     */
    function createVestingSchedule(
        address beneficiary,
        AllocationCategory category,
        uint256 totalAmount
    ) external onlyOwner {
        require(beneficiary != address(0), "Invalid beneficiary");
        require(totalAmount > 0, "Amount must be > 0");
        require(vestingSchedules[beneficiary][category].totalAmount == 0, "Schedule exists");

        VestingSchedule memory schedule;
        schedule.totalAmount = totalAmount;
        schedule.startTime = block.timestamp;
        
        // Set parameters based on category
        if (category == AllocationCategory.PreSeed) {
            schedule.tgePercent = 5000; // 50%
            schedule.cliffDuration = 0;
            schedule.vestingDuration = 10 * 30 days; // 10 months
        } else if (category == AllocationCategory.Seed) {
            schedule.tgePercent = 2500; // 25%
            schedule.cliffDuration = 0;
            schedule.vestingDuration = 12 * 30 days; // 12 months
        } else if (category == AllocationCategory.Private) {
            schedule.tgePercent = 2500; // 25%
            schedule.cliffDuration = 3 * 30 days; // 3 months
            schedule.vestingDuration = 15 * 30 days; // 15 months
        } else if (category == AllocationCategory.Public) {
            schedule.tgePercent = 2000; // 20%
            schedule.cliffDuration = 1 * 30 days; // 1 month
            schedule.vestingDuration = 15 * 30 days; // 15 months
        } else if (category == AllocationCategory.Partners) {
            schedule.tgePercent = 1000; // 10%
            schedule.cliffDuration = 3 * 30 days; // 3 months
            schedule.vestingDuration = 15 * 30 days; // 15 months
        } else if (category == AllocationCategory.TeamAndAdvisors) {
            schedule.tgePercent = 0; // 0%
            schedule.cliffDuration = 3 * 30 days; // 3 months
            schedule.vestingDuration = 36 * 30 days; // 36 months
            schedule.revocable = true; // Team vesting is revocable
        } else if (category == AllocationCategory.Liquidity) {
            schedule.tgePercent = 10000; // 100%
            schedule.cliffDuration = 0;
            schedule.vestingDuration = 0; // No vesting
        } else if (category == AllocationCategory.UserAdoption) {
            schedule.tgePercent = 1000; // 10%
            schedule.cliffDuration = 0;
            schedule.vestingDuration = 48 * 30 days; // 48 months
        } else if (category == AllocationCategory.DeveloperIncentives) {
            schedule.tgePercent = 1000; // 10%
            schedule.cliffDuration = 0;
            schedule.vestingDuration = 48 * 30 days; // 48 months
        } else if (category == AllocationCategory.Ecosystem) {
            schedule.tgePercent = 1000; // 10%
            schedule.cliffDuration = 0;
            schedule.vestingDuration = 48 * 30 days; // 48 months
        } else if (category == AllocationCategory.Staking) {
            schedule.tgePercent = 0; // 0%
            schedule.cliffDuration = 0;
            schedule.vestingDuration = 36 * 30 days; // 36 months
        }

        vestingSchedules[beneficiary][category] = schedule;
        
        // Transfer tokens to this contract
        selfToken.safeTransferFrom(msg.sender, address(this), totalAmount);
        
        emit VestingScheduleCreated(beneficiary, category, totalAmount);
    }

    /**
     * @dev Release vested tokens
     */
    function release(AllocationCategory category) external nonReentrant {
        VestingSchedule storage schedule = vestingSchedules[msg.sender][category];
        require(schedule.totalAmount > 0, "No vesting schedule");
        require(!schedule.revoked, "Vesting revoked");

        uint256 releasable = _computeReleasableAmount(schedule);
        require(releasable > 0, "No tokens to release");

        schedule.releasedAmount += releasable;
        selfToken.safeTransfer(msg.sender, releasable);

        emit TokensReleased(msg.sender, category, releasable);
    }

    /**
     * @dev Compute releasable amount
     */
    function _computeReleasableAmount(VestingSchedule memory schedule) private view returns (uint256) {
        if (block.timestamp < schedule.startTime + schedule.cliffDuration) {
            // Still in cliff period, only TGE amount is available
            uint256 tgeAmount = (schedule.totalAmount * schedule.tgePercent) / 10000;
            if (schedule.releasedAmount < tgeAmount) {
                return tgeAmount - schedule.releasedAmount;
            }
            return 0;
        }

        // Calculate total vested amount
        uint256 timeFromStart = block.timestamp - schedule.startTime;
        uint256 tgeAmount = (schedule.totalAmount * schedule.tgePercent) / 10000;
        uint256 vestingAmount = schedule.totalAmount - tgeAmount;
        
        uint256 vestedAmount;
        if (timeFromStart >= schedule.vestingDuration) {
            vestedAmount = schedule.totalAmount;
        } else {
            // Linear vesting after cliff
            uint256 timeAfterCliff = timeFromStart - schedule.cliffDuration;
            uint256 vestingPeriod = schedule.vestingDuration - schedule.cliffDuration;
            vestedAmount = tgeAmount + (vestingAmount * timeAfterCliff) / vestingPeriod;
        }

        return vestedAmount - schedule.releasedAmount;
    }

    /**
     * @dev Get releasable amount for a beneficiary
     */
    function getReleasableAmount(address beneficiary, AllocationCategory category) external view returns (uint256) {
        VestingSchedule memory schedule = vestingSchedules[beneficiary][category];
        if (schedule.totalAmount == 0 || schedule.revoked) {
            return 0;
        }
        return _computeReleasableAmount(schedule);
    }

    /**
     * @dev Revoke vesting (only for revocable schedules like team)
     */
    function revoke(address beneficiary, AllocationCategory category) external onlyOwner {
        VestingSchedule storage schedule = vestingSchedules[beneficiary][category];
        require(schedule.revocable, "Not revocable");
        require(!schedule.revoked, "Already revoked");

        // Release any vested tokens first
        uint256 releasable = _computeReleasableAmount(schedule);
        if (releasable > 0) {
            schedule.releasedAmount += releasable;
            selfToken.safeTransfer(beneficiary, releasable);
        }

        // Return unvested tokens to owner
        uint256 refund = schedule.totalAmount - schedule.releasedAmount;
        if (refund > 0) {
            selfToken.safeTransfer(owner(), refund);
        }

        schedule.revoked = true;
        emit VestingRevoked(beneficiary, category);
    }

    /**
     * @dev Emergency withdrawal (only by owner)
     */
    function emergencyWithdraw(address token) external onlyOwner {
        if (token == address(0)) {
            payable(owner()).transfer(address(this).balance);
        } else {
            IERC20(token).safeTransfer(owner(), IERC20(token).balanceOf(address(this)));
        }
    }
}