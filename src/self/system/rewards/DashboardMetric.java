package org.self.system.rewards;

public class DashboardMetric {
    private String name;
    private String category;
    private String type;
    private boolean visible;
    private String description;
    
    public DashboardMetric(String zName, String zCategory, String zType) {
        this(zName, zCategory, zType, true, "");
    }
    
    public DashboardMetric(String zName, String zCategory, String zType, boolean zVisible) {
        this(zName, zCategory, zType, zVisible, "");
    }
    
    public DashboardMetric(String zName, String zCategory, String zType, boolean zVisible, String zDescription) {
        name = zName;
        category = zCategory;
        type = zType;
        visible = zVisible;
        description = zDescription;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getType() {
        return type;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setName(String zName) {
        name = zName;
    }
    
    public void setCategory(String zCategory) {
        category = zCategory;
    }
    
    public void setType(String zType) {
        type = zType;
    }
    
    public void setVisible(boolean zVisible) {
        visible = zVisible;
    }
    
    public void setDescription(String zDescription) {
        description = zDescription;
    }
    
    @Override
    public String toString() {
        return String.format(
            "DashboardMetric[name=%s, category=%s, type=%s, visible=%b, desc=%s]",
            name,
            category,
            type,
            visible,
            description
        );
    }
}
