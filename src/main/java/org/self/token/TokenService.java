package org.self.token;

import org.self.blockchain.BlockchainService;
import org.self.contract.ContractDeployer;
import org.self.crypto.Address;

/**
 * TokenService provides high-level operations for token management.
 */
public interface TokenService {
    /**
     * Create a new ERC20 token.
     * 
     * @param creatorAddress Address of the token creator
     * @param tokenName Name of the token
     * @param tokenSymbol Symbol of the token
     * @param initialSupply Initial supply of tokens
     * @param decimals Number of decimal places
     * @return TokenInfo containing token details
     * @throws TokenCreationException If token creation fails
     */
    TokenInfo createERC20Token(
            Address creatorAddress,
            String tokenName,
            String tokenSymbol,
            long initialSupply,
            int decimals) throws TokenCreationException;
    
    /**
     * Create a new ERC721 token (NFT).
     * 
     * @param creatorAddress Address of the token creator
     * @param tokenName Name of the token
     * @param tokenSymbol Symbol of the token
     * @return TokenInfo containing token details
     * @throws TokenCreationException If token creation fails
     */
    TokenInfo createERC721Token(
            Address creatorAddress,
            String tokenName,
            String tokenSymbol) throws TokenCreationException;
    
    /**
     * Get token information by contract address.
     * 
     * @param contractAddress Address of the token contract
     * @return TokenInfo if found, null otherwise
     */
    TokenInfo getTokenInfo(String contractAddress);
    
    /**
     * Mint new tokens for an ERC20 token.
     * 
     * @param contractAddress Address of the token contract
     * @param recipient Address receiving the new tokens
     * @param amount Amount of tokens to mint
     * @throws TokenOperationFailedException If minting fails
     */
    void mintERC20Tokens(
            String contractAddress,
            Address recipient,
            long amount) throws TokenOperationFailedException;
    
    /**
     * Burn tokens for an ERC20 token.
     * 
     * @param contractAddress Address of the token contract
     * @param owner Address of the token owner
     * @param amount Amount of tokens to burn
     * @throws TokenOperationFailedException If burning fails
     */
    void burnERC20Tokens(
            String contractAddress,
            Address owner,
            long amount) throws TokenOperationFailedException;
    
    /**
     * Mint a new NFT token.
     * 
     * @param contractAddress Address of the token contract
     * @param recipient Address receiving the NFT
     * @param metadataURI URI containing token metadata
     * @return Token ID of the newly minted NFT
     * @throws TokenOperationFailedException If minting fails
     */
    long mintNFT(
            String contractAddress,
            Address recipient,
            String metadataURI) throws TokenOperationFailedException;
    
    /**
     * Transfer tokens between addresses.
     * 
     * @param contractAddress Address of the token contract
     * @param from Sender address
     * @param to Recipient address
     * @param amount Amount of tokens to transfer
     * @throws TokenOperationFailedException If transfer fails
     */
    void transferTokens(
            String contractAddress,
            Address from,
            Address to,
            long amount) throws TokenOperationFailedException;
    
    /**
     * Get token balance for an address.
     * 
     * @param contractAddress Address of the token contract
     * @param address Address to check balance for
     * @return Token balance
     * @throws TokenOperationFailedException If balance check fails
     */
    long getTokenBalance(
            String contractAddress,
            Address address) throws TokenOperationFailedException;
    
    /**
     * Get total supply of a token.
     * 
     * @param contractAddress Address of the token contract
     * @return Total supply of tokens
     * @throws TokenOperationFailedException If supply check fails
     */
    long getTotalSupply(String contractAddress) throws TokenOperationFailedException;
}
