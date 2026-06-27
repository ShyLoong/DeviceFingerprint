package com.device.fingerprint.model;

import java.io.Serializable;

/**
 * Model for field-level comparison between two devices
 */
public class FieldDiff implements Serializable {
    
    private String fieldName;
    private String valueA;
    private String valueB;
    private boolean matched;
    private String dimension;
    
    public FieldDiff(String fieldName, String valueA, String valueB, boolean matched, String dimension) {
        this.fieldName = fieldName;
        this.valueA = valueA;
        this.valueB = valueB;
        this.matched = matched;
        this.dimension = dimension;
    }
    
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    
    public String getValueA() { return valueA; }
    public void setValueA(String valueA) { this.valueA = valueA; }
    
    public String getValueB() { return valueB; }
    public void setValueB(String valueB) { this.valueB = valueB; }
    
    public boolean isMatched() { return matched; }
    public void setMatched(boolean matched) { this.matched = matched; }
    
    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    
    /**
     * Get display-friendly field name
     */
    public String getDisplayName() {
        return formatFieldName(fieldName);
    }
    
    /**
     * Get display value A (truncated if too long)
     */
    public String getDisplayValueA() {
        return truncate(valueA);
    }
    
    /**
     * Get display value B (truncated if too long)
     */
    public String getDisplayValueB() {
        return truncate(valueB);
    }
    
    private String truncate(String value) {
        if (value == null || value.isEmpty()) {
            return "(empty)";
        }
        if (value.length() > 40) {
            return value.substring(0, 37) + "...";
        }
        return value;
    }
    
    private String formatFieldName(String raw) {
        String name = raw.replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
