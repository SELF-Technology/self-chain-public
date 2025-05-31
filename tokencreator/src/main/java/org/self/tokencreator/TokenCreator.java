package org.self.tokencreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * TokenCreator converts user-friendly token concepts into deployable smart contracts.
 */
public class TokenCreator {
    private static final Logger logger = LoggerFactory.getLogger(TokenCreator.class);
    
    private static final BigInteger GAS_PRICE = BigInteger.valueOf(2000000000L); // 20 Gwei
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(4300000);
    
    private final Web3j web3j;
    private final Credentials credentials;
    private final ContractGasProvider gasProvider;
    
    public TokenCreator(Web3j web3j, String privateKey) {
        this.web3j = web3j;
        this.credentials = WalletUtils.loadCredentials("", privateKey);
        this.gasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);
    }
    
    /**
     * Create a new ERC20 token based on user input.
     */
    public TokenCreationResult createToken(TokenSpecification spec) {
        try {
            logger.info("Creating token: {}", spec.getName());
            
            // Create and deploy token contract
            CustomERC20 token = CustomERC20.deploy(
                web3j,
                credentials,
                gasProvider,
                spec.getName(),
                spec.getSymbol(),
                spec.getDecimals(),
                spec.getTotalSupply()
            ).send();
            
            // Store token details
            TokenCreationResult result = new TokenCreationResult(
                token.getContractAddress(),
                spec.getName(),
                spec.getSymbol(),
                spec.getDecimals(),
                spec.getTotalSupply()
            );
            
            logger.info("Token created successfully: {}", result);
            return result;
            
        } catch (Exception e) {
            logger.error("Error creating token", e);
            throw new TokenCreationException("Failed to create token: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a new NFT collection based on user input.
     */
    public NFTCreationResult createNFTCollection(NFTSpecification spec) {
        try {
            logger.info("Creating NFT collection: {}", spec.getName());
            
            // Create and deploy NFT contract
            CustomNFT nft = CustomNFT.deploy(
                web3j,
                credentials,
                gasProvider,
                spec.getName(),
                spec.getSymbol()
            ).send();
            
            // Store NFT details
            NFTCreationResult result = new NFTCreationResult(
                nft.getContractAddress(),
                spec.getName(),
                spec.getSymbol()
            );
            
            logger.info("NFT collection created successfully: {}", result);
            return result;
            
        } catch (Exception e) {
            logger.error("Error creating NFT collection", e);
            throw new TokenCreationException("Failed to create NFT collection: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get token details by address.
     */
    public TokenDetails getTokenDetails(String contractAddress) {
        try {
            CustomERC20 token = CustomERC20.load(
                contractAddress,
                web3j,
                credentials,
                gasProvider
            );
            
            String name = token.name().send();
            String symbol = token.symbol().send();
            BigInteger decimals = token.decimals().send();
            BigInteger totalSupply = token.totalSupply().send();
            
            return new TokenDetails(
                contractAddress,
                name,
                symbol,
                decimals.intValue(),
                totalSupply
            );
            
        } catch (Exception e) {
            logger.error("Error getting token details", e);
            throw new TokenCreationException("Failed to get token details: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get NFT collection details by address.
     */
    public NFTDetails getNFTCollectionDetails(String contractAddress) {
        try {
            CustomNFT nft = CustomNFT.load(
                contractAddress,
                web3j,
                credentials,
                gasProvider
            );
            
            String name = nft.name().send();
            String symbol = nft.symbol().send();
            BigInteger totalSupply = nft.totalSupply().send();
            
            return new NFTDetails(
                contractAddress,
                name,
                symbol,
                totalSupply
            );
            
        } catch (Exception e) {
            logger.error("Error getting NFT collection details", e);
            throw new TokenCreationException("Failed to get NFT collection details: " + e.getMessage(), e);
        }
    }
}
