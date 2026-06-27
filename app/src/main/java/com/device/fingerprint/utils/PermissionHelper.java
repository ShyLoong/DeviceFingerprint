package com.device.fingerprint.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for managing runtime permissions
 */
public class PermissionHelper {
    
    public static final int REQUEST_CODE_PERMISSIONS = 1001;
    
    // List of dangerous permissions that may be requested
    private static final String[] DANGEROUS_PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    
    /**
     * Check if all required permissions are granted
     */
    public static boolean hasAllPermissions(Context context) {
        for (String permission : DANGEROUS_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get list of permissions that need to be requested
     */
    public static String[] getPermissionsToRequest(Context context) {
        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : DANGEROUS_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }
        return permissionsNeeded.toArray(new String[0]);
    }
    
    /**
     * Request permissions from user
     */
    public static void requestPermissions(Activity activity) {
        String[] permissionsToRequest = getPermissionsToRequest(activity);
        if (permissionsToRequest.length > 0) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest, REQUEST_CODE_PERMISSIONS);
        }
    }
    
    /**
     * Check if specific permission is granted
     */
    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if READ_PHONE_STATE permission is granted (for IMEI)
     */
    public static boolean canReadPhoneState(Context context) {
        return hasPermission(context, Manifest.permission.READ_PHONE_STATE);
    }
    
    /**
     * Check if location permission is granted
     */
    public static boolean canAccessLocation(Context context) {
        return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
               hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }
    
    /**
     * Get permission description for user-friendly display
     */
    public static String getPermissionDescription(String permission) {
        switch (permission) {
            case Manifest.permission.READ_PHONE_STATE:
                return "Phone State (for IMEI/Device ID)";
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return "Precise Location (for cell tower info)";
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "Approximate Location (for network info)";
            default:
                return permission;
        }
    }
    
    /**
     * Check if permission rationale should be shown
     */
    public static boolean shouldShowRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
}
