package com.device.fingerprint.generator;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Device fingerprint generator using SimHash algorithm
 * Generates a unique fingerprint based on multiple device features
 */
public class FingerprintGenerator {
    
    private static final String TAG = "FingerprintGenerator";
    private static final int HASH_LENGTH = 64; // 64-bit fingerprint
    
    // Feature weights based on stability and uniqueness
    // Higher weight = more important for fingerprint uniqueness
    private static final Map<String, Double> FEATURE_WEIGHTS = new HashMap<>();
    
    static {
        // Hardware features - high stability, high weight
        FEATURE_WEIGHTS.put("brand", 0.5);
        FEATURE_WEIGHTS.put("model", 1.2);
        FEATURE_WEIGHTS.put("board", 1.5);
        FEATURE_WEIGHTS.put("hardware", 1.0);
        FEATURE_WEIGHTS.put("cpu_abi", 0.8);
        FEATURE_WEIGHTS.put("cpu_cores", 0.5);
        FEATURE_WEIGHTS.put("cpu_info", 1.0);
        FEATURE_WEIGHTS.put("total_memory", 0.6);
        FEATURE_WEIGHTS.put("internal_storage", 0.6);
        FEATURE_WEIGHTS.put("screen_resolution", 1.0);
        FEATURE_WEIGHTS.put("screen_density", 0.8);
        FEATURE_WEIGHTS.put("sensors", 1.5);
        FEATURE_WEIGHTS.put("sensor_count", 0.8);
        FEATURE_WEIGHTS.put("camera", 0.6);
        
        // System features - medium stability
        FEATURE_WEIGHTS.put("os_version", 0.5);
        FEATURE_WEIGHTS.put("sdk_int", 0.5);
        FEATURE_WEIGHTS.put("system_fingerprint", 1.5);
        FEATURE_WEIGHTS.put("language", 0.4);
        FEATURE_WEIGHTS.put("timezone", 0.4);
        FEATURE_WEIGHTS.put("android_id", 2.5);  // High uniqueness
        FEATURE_WEIGHTS.put("pseudo_id", 2.0);   // Hardware-based fallback
        FEATURE_WEIGHTS.put("is_rooted", 0.5);
        FEATURE_WEIGHTS.put("is_emulator", 0.5);
        FEATURE_WEIGHTS.put("usb_debug", 0.3);
        FEATURE_WEIGHTS.put("app_count", 0.6);
        FEATURE_WEIGHTS.put("input_method", 0.8);
        
        // Network features - low stability but good for uniqueness
        FEATURE_WEIGHTS.put("wifi_bssid", 1.5);
        FEATURE_WEIGHTS.put("operator", 0.4);
        FEATURE_WEIGHTS.put("mcc_mnc", 0.5);
        FEATURE_WEIGHTS.put("network_type", 0.3);
        FEATURE_WEIGHTS.put("proxy", 0.5);
        FEATURE_WEIGHTS.put("vpn", 0.5);
        
        // App features
        FEATURE_WEIGHTS.put("package_name", 1.0);
        FEATURE_WEIGHTS.put("app_version", 0.5);
        FEATURE_WEIGHTS.put("app_signature", 1.0);
        FEATURE_WEIGHTS.put("install_time", 0.8);
    }
    
    /**
     * Generate device fingerprint using SimHash algorithm
     * @param features Map of feature name to feature value
     * @return Hex string representing the device fingerprint
     */
    public String generateFingerprint(Map<String, String> features) {
        if (features == null || features.isEmpty()) {
            return "";
        }
        
        // Step 1: Initialize fingerprint vector
        double[] fingerprintVector = new double[HASH_LENGTH];
        
        // Step 2: Process each feature
        int processedFeatures = 0;
        for (Map.Entry<String, String> entry : features.entrySet()) {
            String featureName = entry.getKey();
            String featureValue = entry.getValue();
            
            if (featureValue == null || featureValue.isEmpty() || 
                featureValue.equals("N/A") || featureValue.equals("unknown")) {
                continue;
            }
            
            // Get weight for this feature
            double weight = FEATURE_WEIGHTS.getOrDefault(featureName, 0.5);
            
            // Step 3: Hash the feature value to binary string
            String hash = sha256(featureValue);
            String binaryHash = hexToBinary(hash, HASH_LENGTH);
            
            // Step 4: Weighted sum
            for (int i = 0; i < HASH_LENGTH; i++) {
                if (i < binaryHash.length()) {
                    if (binaryHash.charAt(i) == '1') {
                        fingerprintVector[i] += weight;
                    } else {
                        fingerprintVector[i] -= weight;
                    }
                }
            }
            processedFeatures++;
        }
        
        Log.d(TAG, "Processed " + processedFeatures + " features for fingerprint generation");
        
        // Step 5: Binarize - convert to 0/1 based on sign
        StringBuilder binaryFingerprint = new StringBuilder();
        for (int i = 0; i < HASH_LENGTH; i++) {
            binaryFingerprint.append(fingerprintVector[i] > 0 ? '1' : '0');
        }
        
        // Step 6: Convert binary to hex
        return binaryToHex(binaryFingerprint.toString());
    }
    
    /**
     * Generate a human-readable short fingerprint (first 16 chars of hex)
     */
    public String generateShortFingerprint(Map<String, String> features) {
        String fullFingerprint = generateFingerprint(features);
        if (fullFingerprint.length() >= 16) {
            return fullFingerprint.substring(0, 16).toUpperCase();
        }
        return fullFingerprint.toUpperCase();
    }
    
    /**
     * Calculate SHA-256 hash of input string
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash
            return String.valueOf(input.hashCode());
        }
    }
    
    /**
     * Convert hex string to binary string of specified length
     */
    private String hexToBinary(String hex, int length) {
        StringBuilder binary = new StringBuilder();
        for (char c : hex.toCharArray()) {
            String bin = Integer.toBinaryString(Integer.parseInt(String.valueOf(c), 16));
            // Pad to 4 bits
            while (bin.length() < 4) {
                bin = "0" + bin;
            }
            binary.append(bin);
        }
        // Truncate or pad to required length
        if (binary.length() > length) {
            return binary.substring(0, length);
        } else {
            while (binary.length() < length) {
                binary.append("0");
            }
            return binary.toString();
        }
    }
    
    /**
     * Convert binary string to hex string
     */
    private String binaryToHex(String binary) {
        StringBuilder hex = new StringBuilder();
        // Process 4 bits at a time
        for (int i = 0; i < binary.length(); i += 4) {
            int end = Math.min(i + 4, binary.length());
            String chunk = binary.substring(i, end);
            // Pad last chunk if needed
            while (chunk.length() < 4) {
                chunk += "0";
            }
            int decimal = Integer.parseInt(chunk, 2);
            hex.append(Integer.toHexString(decimal));
        }
        return hex.toString();
    }
}
