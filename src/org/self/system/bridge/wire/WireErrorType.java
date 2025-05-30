package org.self.system.bridge.wire;

public enum WireErrorType {
    CONNECTION("Connection Error"),
    TRANSACTION("Transaction Error"),
    VALIDATION("Validation Error"),
    RPC("RPC Error"),
    SECURITY("Security Error");
    
    private final String description;
    
    WireErrorType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
