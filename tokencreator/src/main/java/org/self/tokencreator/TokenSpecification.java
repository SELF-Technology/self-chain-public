package org.self.tokencreator;

import java.math.BigInteger;

/**
 * Specification for creating a new ERC20 token.
 */
public class TokenSpecification {
    private String name;
    private String symbol;
    private int decimals;
    private BigInteger totalSupply;
    private String description;
    private String website;
    private String logoUrl;
    private String[] features;
    
    public TokenSpecification(String name, String symbol, int decimals, BigInteger totalSupply) {
        this.name = name;
        this.symbol = symbol;
        this.decimals = decimals;
        this.totalSupply = totalSupply;
        this.features = new String[]{};
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public int getDecimals() { return decimals; }
    public void setDecimals(int decimals) { this.decimals = decimals; }
    
    public BigInteger getTotalSupply() { return totalSupply; }
    public void setTotalSupply(BigInteger totalSupply) { this.totalSupply = totalSupply; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    
    public String[] getFeatures() { return features; }
    public void setFeatures(String[] features) { this.features = features; }
}
