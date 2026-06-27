package com.device.fingerprint.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class for device comparison results
 */
public class ComparisonResult implements Serializable {
    
    public static final int THRESHOLD_SAME_DEVICE = 90;
    public static final int THRESHOLD_LIKELY_SAME = 75;
    public static final int THRESHOLD_UNCERTAIN = 60;
    public static final int THRESHOLD_NEW_DEVICE = 40;
    
    private DeviceInfo device1;
    private DeviceInfo device2;
    private double overallSimilarity;
    private double hardwareSimilarity;
    private double systemSimilarity;
    private double networkSimilarity;
    private double appSimilarity;
    private int hammingDistance;
    private String result;
    private String action;
    private String riskLevel;
    private List<FieldDiff> fieldDiffs = new ArrayList<>();
    
    public ComparisonResult(DeviceInfo device1, DeviceInfo device2) {
        this.device1 = device1;
        this.device2 = device2;
    }
    
    // Getters and Setters
    public DeviceInfo getDevice1() { return device1; }
    public void setDevice1(DeviceInfo device1) { this.device1 = device1; }
    
    public DeviceInfo getDevice2() { return device2; }
    public void setDevice2(DeviceInfo device2) { this.device2 = device2; }
    
    public double getOverallSimilarity() { return overallSimilarity; }
    public void setOverallSimilarity(double overallSimilarity) { this.overallSimilarity = overallSimilarity; }
    
    public double getHardwareSimilarity() { return hardwareSimilarity; }
    public void setHardwareSimilarity(double hardwareSimilarity) { this.hardwareSimilarity = hardwareSimilarity; }
    
    public double getSystemSimilarity() { return systemSimilarity; }
    public void setSystemSimilarity(double systemSimilarity) { this.systemSimilarity = systemSimilarity; }
    
    public double getNetworkSimilarity() { return networkSimilarity; }
    public void setNetworkSimilarity(double networkSimilarity) { this.networkSimilarity = networkSimilarity; }
    
    public double getAppSimilarity() { return appSimilarity; }
    public void setAppSimilarity(double appSimilarity) { this.appSimilarity = appSimilarity; }
    
    public int getHammingDistance() { return hammingDistance; }
    public void setHammingDistance(int hammingDistance) { this.hammingDistance = hammingDistance; }
    
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public List<FieldDiff> getFieldDiffs() { return fieldDiffs; }
    public void setFieldDiffs(List<FieldDiff> fieldDiffs) { this.fieldDiffs = fieldDiffs; }

    /**
     * Get result description based on similarity threshold
     */
    public String getResultDescription() {
        if (overallSimilarity >= THRESHOLD_SAME_DEVICE) {
            return "SAME DEVICE - The two devices appear to be the same device";
        } else if (overallSimilarity >= THRESHOLD_LIKELY_SAME) {
            return "LIKELY SAME - High probability these are the same device";
        } else if (overallSimilarity >= THRESHOLD_UNCERTAIN) {
            return "UNCERTAIN - May be a new device, additional verification recommended";
        } else if (overallSimilarity >= THRESHOLD_NEW_DEVICE) {
            return "NEW DEVICE - This appears to be a different device";
        } else {
            return "HIGH RISK - Significant differences detected, potential fraud";
        }
    }
    
    /**
     * Get recommended action description
     */
    public String getActionDescription() {
        if (overallSimilarity >= THRESHOLD_SAME_DEVICE) {
            return "Allow - No additional verification needed";
        } else if (overallSimilarity >= THRESHOLD_LIKELY_SAME) {
            return "Behavior check - Monitor user behavior patterns";
        } else if (overallSimilarity >= THRESHOLD_UNCERTAIN) {
            return "MFA Required - Trigger multi-factor authentication (SMS/Face)";
        } else if (overallSimilarity >= THRESHOLD_NEW_DEVICE) {
            return "MFA + Monitor - Require verification and increase monitoring";
        } else {
            return "Block/Review - Block access or send for manual review";
        }
    }
    
    /**
     * Get color code for the similarity score
     */
    public int getSimilarityColor() {
        if (overallSimilarity >= THRESHOLD_SAME_DEVICE) {
            return 0xFF4CAF50; // Green
        } else if (overallSimilarity >= THRESHOLD_LIKELY_SAME) {
            return 0xFF8BC34A; // Light Green
        } else if (overallSimilarity >= THRESHOLD_UNCERTAIN) {
            return 0xFFFFC107; // Amber
        } else if (overallSimilarity >= THRESHOLD_NEW_DEVICE) {
            return 0xFFFF9800; // Orange
        } else {
            return 0xFFF44336; // Red
        }
    }
}
