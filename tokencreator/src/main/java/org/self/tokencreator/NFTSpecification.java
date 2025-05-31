package org.self.tokencreator;

/**
 * Specification for creating a new NFT collection.
 */
public class NFTSpecification {
    private String name;
    private String symbol;
    private String description;
    private String website;
    private String logoUrl;
    private String baseURI;
    private boolean isEnumerable;
    private boolean isBurnable;
    private boolean isMintable;
    
    public NFTSpecification(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
        this.isEnumerable = true;
        this.isBurnable = true;
        this.isMintable = true;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    
    public String getBaseURI() { return baseURI; }
    public void setBaseURI(String baseURI) { this.baseURI = baseURI; }
    
    public boolean isEnumerable() { return isEnumerable; }
    public void setEnumerable(boolean enumerable) { isEnumerable = enumerable; }
    
    public boolean isBurnable() { return isBurnable; }
    public void setBurnable(boolean burnable) { isBurnable = burnable; }
    
    public boolean isMintable() { return isMintable; }
    public void setMintable(boolean mintable) { isMintable = mintable; }
}
