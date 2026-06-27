package com.device.fingerprint.collector;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import com.device.fingerprint.model.DeviceInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Collector for application-related information
 * Collects: package name, version, signature, install time, etc.
 */
public class ApplicationCollector {
    
    private Context context;
    
    public ApplicationCollector(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Collect all application information
     */
    public void collect(DeviceInfo info) {
        try {
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 
                    PackageManager.GET_SIGNATURES | PackageManager.GET_PERMISSIONS);
            
            // Package name
            info.setPackageName(packageName);
            info.addRawFeature("package_name", packageName);
            
            // Version info
            info.setAppVersionName(packageInfo.versionName);
            info.setAppVersionCode(packageInfo.versionCode);
            info.addRawFeature("app_version", packageInfo.versionName);
            info.addRawFeature("version_code", String.valueOf(packageInfo.versionCode));
            
            // App signature
            String signature = getAppSignature(packageInfo);
            info.setAppSignature(signature);
            info.addRawFeature("app_signature", signature);
            
            // Install times
            info.setFirstInstallTime(packageInfo.firstInstallTime);
            info.setLastUpdateTime(packageInfo.lastUpdateTime);
            info.addRawFeature("install_time", String.valueOf(packageInfo.firstInstallTime));
            
            // Installer package
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                info.setInstallerPackage(pm.getInstallSourceInfo(packageName).getInstallingPackageName());
            } else {
                info.setInstallerPackage(pm.getInstallerPackageName(packageName));
            }
            
            // Process name
            info.setProcessName(android.os.Process.myProcessName());
            
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get application signature hash
     */
    private String getAppSignature(PackageInfo packageInfo) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (packageInfo.signingInfo != null) {
                    Signature[] signatures = packageInfo.signingInfo.getApkContentsSigners();
                    return hashSignatures(signatures);
                }
            } else {
                if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                    return hashSignatures(packageInfo.signatures);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }
    
    /**
     * Hash signatures for comparison
     */
    private String hashSignatures(Signature[] signatures) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (Signature sig : signatures) {
                md.update(sig.toByteArray());
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Arrays.toString(signatures);
        }
    }
}
