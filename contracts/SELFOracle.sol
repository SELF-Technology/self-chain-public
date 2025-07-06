// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

import "@chainlink/contracts/src/v0.8/interfaces/AggregatorV3Interface.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title SELF Oracle
 * @dev Price oracle for SELF token using Chainlink price feeds
 * @notice This contract provides USD pricing for dynamic subscription costs
 */
contract SELFOracle is Ownable {
    AggregatorV3Interface internal ethUsdPriceFeed;
    
    // Manual SELF/ETH price until we have Chainlink feed
    uint256 public selfPerEth = 100000; // 100,000 SELF per ETH initial price
    uint256 public lastPriceUpdate;
    
    // Price update constraints
    uint256 public constant MAX_PRICE_CHANGE = 1000; // 10% max change per update
    uint256 public constant MIN_UPDATE_DELAY = 1 hours;
    
    event PriceUpdated(uint256 oldPrice, uint256 newPrice);
    
    constructor(address _ethUsdPriceFeed) {
        // Mainnet ETH/USD: 0x5f4eC3Df9cbd43714FE2740f5E3616155c5b8419
        ethUsdPriceFeed = AggregatorV3Interface(_ethUsdPriceFeed);
        lastPriceUpdate = block.timestamp;
    }
    
    /**
     * @dev Get latest ETH price in USD
     */
    function getEthUsdPrice() public view returns (uint256) {
        (, int256 price, , , ) = ethUsdPriceFeed.latestRoundData();
        require(price > 0, "Invalid price");
        return uint256(price); // Price has 8 decimals
    }
    
    /**
     * @dev Calculate SELF price in USD (18 decimals)
     */
    function getSelfUsdPrice() public view returns (uint256) {
        uint256 ethPrice = getEthUsdPrice(); // 8 decimals
        // SELF/USD = (SELF/ETH) * (ETH/USD)
        // Convert to 18 decimals: ethPrice * 10^10 / selfPerEth
        return (ethPrice * 10**10 * 10**18) / selfPerEth;
    }
    
    /**
     * @dev Calculate how many SELF tokens equal a USD amount
     */
    function usdToSelf(uint256 usdAmount) external view returns (uint256) {
        uint256 selfPrice = getSelfUsdPrice(); // 18 decimals
        return (usdAmount * 10**18) / selfPrice;
    }
    
    /**
     * @dev Update SELF/ETH price manually (temporary until Chainlink feed)
     */
    function updateSelfPrice(uint256 newSelfPerEth) external onlyOwner {
        require(block.timestamp >= lastPriceUpdate + MIN_UPDATE_DELAY, "Too soon");
        require(newSelfPerEth > 0, "Invalid price");
        
        // Check price change is within bounds
        uint256 priceDiff = newSelfPerEth > selfPerEth 
            ? newSelfPerEth - selfPerEth 
            : selfPerEth - newSelfPerEth;
        require(priceDiff <= (selfPerEth * MAX_PRICE_CHANGE) / 10000, "Price change too large");
        
        uint256 oldPrice = selfPerEth;
        selfPerEth = newSelfPerEth;
        lastPriceUpdate = block.timestamp;
        
        emit PriceUpdated(oldPrice, newSelfPerEth);
    }
    
    /**
     * @dev Emergency function to update price feed address
     */
    function updatePriceFeed(address newFeed) external onlyOwner {
        ethUsdPriceFeed = AggregatorV3Interface(newFeed);
    }
}