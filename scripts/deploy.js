const { ethers } = require("hardhat");

async function main() {
  console.log("Deploying SELF Token contracts...");

  // Get deployer account
  const [deployer] = await ethers.getSigners();
  console.log("Deploying contracts with account:", deployer.address);
  console.log("Account balance:", (await deployer.getBalance()).toString());

  // Deploy Oracle first (if mainnet, use Chainlink ETH/USD feed)
  const chainlinkEthUsd = {
    mainnet: "0x5f4eC3Df9cbd43714FE2740f5E3616155c5b8419",
    goerli: "0xD4a33860578De61DBAbDc8BFdb98FD742fA7028e",
    sepolia: "0x694AA1769357215DE4FAC081bf1f309aDC325306"
  };

  const network = await ethers.provider.getNetwork();
  const priceFeed = chainlinkEthUsd[network.name] || chainlinkEthUsd.goerli;

  console.log("\nDeploying SELFOracle...");
  const SELFOracle = await ethers.getContractFactory("SELFOracle");
  const oracle = await SELFOracle.deploy(priceFeed);
  await oracle.deployed();
  console.log("SELFOracle deployed to:", oracle.address);

  // Deploy SELF Token
  console.log("\nDeploying SELFToken...");
  const SELFToken = await ethers.getContractFactory("SELFToken");
  const token = await SELFToken.deploy();
  await token.deployed();
  console.log("SELFToken deployed to:", token.address);

  // Get deployment info
  const totalSupply = await token.totalSupply();
  console.log("\nToken Information:");
  console.log("Name:", await token.name());
  console.log("Symbol:", await token.symbol());
  console.log("Decimals:", await token.decimals());
  console.log("Total Supply:", ethers.utils.formatEther(totalSupply), "SELF");

  // Save deployment addresses
  const fs = require("fs");
  const deploymentInfo = {
    network: network.name,
    chainId: network.chainId,
    deploymentTime: new Date().toISOString(),
    contracts: {
      SELFToken: token.address,
      SELFOracle: oracle.address
    },
    deployer: deployer.address
  };

  fs.writeFileSync(
    `deployment-${network.name}.json`,
    JSON.stringify(deploymentInfo, null, 2)
  );

  console.log("\nDeployment complete! Contract addresses saved to deployment-" + network.name + ".json");

  // Verify on Etherscan (if API key is set)
  if (process.env.ETHERSCAN_API_KEY) {
    console.log("\nWaiting for block confirmations before verification...");
    await token.deployTransaction.wait(5);
    
    console.log("Verifying contracts on Etherscan...");
    try {
      await hre.run("verify:verify", {
        address: token.address,
        constructorArguments: []
      });
      
      await hre.run("verify:verify", {
        address: oracle.address,
        constructorArguments: [priceFeed]
      });
      
      console.log("Contracts verified successfully!");
    } catch (error) {
      console.log("Verification failed:", error.message);
    }
  }
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });