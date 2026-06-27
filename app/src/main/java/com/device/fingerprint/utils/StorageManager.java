package com.device.fingerprint.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.device.fingerprint.model.DeviceInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager for storing and retrieving device information locally
 * Uses SharedPreferences for simple persistence
 */
public class StorageManager {
    
    private static final String PREFS_NAME = "DeviceFingerprintPrefs";
    private static final String KEY_DEVICE_HISTORY = "device_history";
    private static final String KEY_CURRENT_DEVICE = "current_device";
    
    private SharedPreferences preferences;
    private Gson gson;
    
    public StorageManager(Context context) {
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    /**
     * Save current device information
     */
    public void saveCurrentDevice(DeviceInfo deviceInfo) {
        String json = gson.toJson(deviceInfo);
        preferences.edit().putString(KEY_CURRENT_DEVICE, json).apply();
    }
    
    /**
     * Get current device information
     */
    public DeviceInfo getCurrentDevice() {
        String json = preferences.getString(KEY_CURRENT_DEVICE, null);
        if (json != null) {
            return gson.fromJson(json, DeviceInfo.class);
        }
        return null;
    }
    
    /**
     * Save device to history list
     */
    public void saveToHistory(DeviceInfo deviceInfo) {
        List<DeviceInfo> history = getDeviceHistory();
        
        // Check if already exists (by fingerprint)
        boolean exists = false;
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).getFingerprint() != null && 
                history.get(i).getFingerprint().equals(deviceInfo.getFingerprint())) {
                // Update existing entry
                history.set(i, deviceInfo);
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            history.add(0, deviceInfo); // Add to beginning
            
            // Keep only last 20 entries
            if (history.size() > 20) {
                history = history.subList(0, 20);
            }
        }
        
        String json = gson.toJson(history);
        preferences.edit().putString(KEY_DEVICE_HISTORY, json).apply();
    }
    
    /**
     * Get all device history
     */
    public List<DeviceInfo> getDeviceHistory() {
        String json = preferences.getString(KEY_DEVICE_HISTORY, null);
        if (json != null) {
            Type type = new TypeToken<List<DeviceInfo>>(){}.getType();
            List<DeviceInfo> history = gson.fromJson(json, type);
            return history != null ? history : new ArrayList<>();
        }
        return new ArrayList<>();
    }
    
    /**
     * Clear device history
     */
    public void clearHistory() {
        preferences.edit().remove(KEY_DEVICE_HISTORY).apply();
    }
    
    /**
     * Delete a specific device from history
     */
    public void deleteFromHistory(String fingerprint) {
        List<DeviceInfo> history = getDeviceHistory();
        history.removeIf(device -> fingerprint.equals(device.getFingerprint()));
        String json = gson.toJson(history);
        preferences.edit().putString(KEY_DEVICE_HISTORY, json).apply();
    }
    
    /**
     * Get count of stored devices
     */
    public int getHistoryCount() {
        return getDeviceHistory().size();
    }
}
