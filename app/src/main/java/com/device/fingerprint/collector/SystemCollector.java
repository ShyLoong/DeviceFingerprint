package com.device.fingerprint.collector;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.device.fingerprint.model.DeviceInfo;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Collector for system-related device information
 * Collects: OS version, Android ID, language, timezone, root status, emulator status, etc.
 */
public class SystemCollector {
    
    private Context context;
    
    public SystemCollector(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Collect all system information
     */
    public void collect(DeviceInfo info) {
        collectOsInfo(info);
        collectSystemSettings(info);
        collectDeviceIdentifiers(info);
        collectRuntimeInfo(info);
        detectRoot(info);
        detectEmulator(info);
        detectDebugMode(info);
        collectAppList(info);
        collectInputMethod(info);
        checkFingerprintSensor(info);
    }
    
    /**
     * Collect OS version and build information
     */
    private void collectOsInfo(DeviceInfo info) {
        info.setOsVersion(Build.VERSION.RELEASE);
        info.setSdkInt(Build.VERSION.SDK_INT);
        info.setSystemFingerprint(Build.FINGERPRINT);
        info.setBuildType(Build.TYPE);
        info.setBuildTags(Build.TAGS);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            info.setSecurityPatch(Build.VERSION.SECURITY_PATCH);
        }
        
        info.addRawFeature("os_version", Build.VERSION.RELEASE);
        info.addRawFeature("sdk_int", String.valueOf(Build.VERSION.SDK_INT));
        info.addRawFeature("system_fingerprint", Build.FINGERPRINT);
    }
    
    /**
     * Collect system settings (language, timezone)
     */
    private void collectSystemSettings(DeviceInfo info) {
        info.setSystemLanguage(Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
        info.setTimezone(TimeZone.getDefault().getID());
        
        info.addRawFeature("language", info.getSystemLanguage());
        info.addRawFeature("timezone", info.getTimezone());
    }
    
    /**
     * Collect device identifiers (Android ID, Pseudo ID)
     * Note: IMEI requires READ_PHONE_STATE permission and is limited on Android 10+
     */
    private void collectDeviceIdentifiers(DeviceInfo info) {
        // Android ID - most reliable cross-version identifier
        ContentResolver resolver = context.getContentResolver();
        String androidId = Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID);
        info.setAndroidId(androidId != null ? androidId : "unknown");
        info.addRawFeature("android_id", androidId);
        
        // Generate Pseudo ID based on hardware features (no permissions needed)
        String pseudoId = generatePseudoId();
        info.setPseudoId(pseudoId);
        info.addRawFeature("pseudo_id", pseudoId);
        
        // OAID - would require MSA SDK integration in production
        // For this demo, we'll leave it empty or use a placeholder
        info.setOaid("");
    }
    
    /**
     * Generate a Pseudo ID based on hardware characteristics.
     * Combines model-level fields with individual-level features (memory, storage,
     * screen, sensors, CPU) to maximize distinguishability between devices of the same model.
     * No permissions are required for any of these reads.
     */
    private String generatePseudoId() {
        StringBuilder sb = new StringBuilder();

        // --- Model-level fields (same for identical models) ---
        sb.append(Build.BOARD).append("|");
        sb.append(Build.BRAND).append("|");
        sb.append(Build.DEVICE).append("|");
        sb.append(Build.HARDWARE).append("|");
        sb.append(Build.MANUFACTURER).append("|");
        sb.append(Build.MODEL).append("|");
        sb.append(Build.PRODUCT).append("|");

        // --- Individual-level fields (distinguish same-model devices) ---

        // 1. Total RAM (different SKUs: 8GB / 12GB / 16GB)
        try {
            android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
            android.app.ActivityManager am = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            am.getMemoryInfo(mi);
            sb.append(mi.totalMem).append("|");
        } catch (Exception e) {
            sb.append("0").append("|");
        }

        // 2. Internal storage capacity (128GB / 256GB / 512GB variants)
        try {
            android.os.StatFs statFs = new android.os.StatFs(android.os.Environment.getDataDirectory().getPath());
            long storage = statFs.getBlockCountLong() * statFs.getBlockSizeLong();
            sb.append(storage).append("|");
        } catch (Exception e) {
            sb.append("0").append("|");
        }

        // 3. Screen resolution & density (different panel suppliers / variants)
        try {
            android.util.DisplayMetrics dm = context.getResources().getDisplayMetrics();
            sb.append(dm.widthPixels).append("x").append(dm.heightPixels).append("|");
            sb.append(dm.densityDpi).append("|");
        } catch (Exception e) {
            sb.append("0x0").append("|").append("0").append("|");
        }

        // 4. Sensor set summary (count + list; varies by batch / revision)
        try {
            android.hardware.SensorManager sm = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            java.util.List<android.hardware.Sensor> sensors = sm.getSensorList(android.hardware.Sensor.TYPE_ALL);
            sb.append(sensors.size()).append("|");
            // Append first 5 sensor names to capture sensor configuration differences
            int count = Math.min(sensors.size(), 5);
            for (int i = 0; i < count; i++) {
                sb.append(sensors.get(i).getName()).append(";");
            }
            sb.append("|");
        } catch (Exception e) {
            sb.append("0|");
        }

        // 5. CPU info (stepping / revision differences)
        try {
            java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.FileReader("/proc/cpuinfo"));
            String line;
            int linesRead = 0;
            while ((line = br.readLine()) != null && linesRead < 8) {
                if (line.contains("Hardware") || line.contains("Processor") ||
                    line.contains("model name") || line.contains("cpu cores") ||
                    line.contains("CPU architecture")) {
                    sb.append(line.trim()).append(";");
                }
                linesRead++;
            }
            br.close();
        } catch (Exception e) {
            sb.append(Build.SUPPORTED_ABIS != null ? java.util.Arrays.toString(Build.SUPPORTED_ABIS) : "unknown");
        }

        // Hash the combined string
        return hashString(sb.toString());
    }
    
    /**
     * Collect runtime information
     */
    private void collectRuntimeInfo(DeviceInfo info) {
        long uptime = SystemClock.elapsedRealtime();
        info.setUptime(uptime);
        info.addRawFeature("uptime", String.valueOf(uptime));
    }
    
    /**
     * Detect if device is rooted
     */
    private void detectRoot(DeviceInfo info) {
        boolean isRooted = checkRootFiles() || checkRootPackages() || checkTestKeys();
        info.setRooted(isRooted);
        info.setHasRootTools(checkRootFiles());
        info.setHasMagisk(checkMagisk());
        info.addRawFeature("is_rooted", String.valueOf(isRooted));
    }
    
    private boolean checkRootFiles() {
        String[] paths = {
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }
    
    private boolean checkRootPackages() {
        PackageManager pm = context.getPackageManager();
        String[] rootApps = {"com.koushikdutta.superuser", "com.thirdparty.superuser", 
                            "com.kingroot.kinguser", "com.kingo.root", "com.saurik.substrate"};
        for (String pkg : rootApps) {
            try {
                pm.getPackageInfo(pkg, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                // App not installed
            }
        }
        return false;
    }
    
    private boolean checkTestKeys() {
        String buildTags = Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }
    
    private boolean checkMagisk() {
        return new File("/sbin/.magisk").exists() || 
               new File("/dev/.magisk.unblock").exists() ||
               new File("/system/bin/magisk").exists();
    }
    
    /**
     * Detect if running on emulator
     */
    private void detectEmulator(DeviceInfo info) {
        boolean isEmulator = checkEmulatorIndicators();
        info.setEmulator(isEmulator);
        info.addRawFeature("is_emulator", String.valueOf(isEmulator));
    }
    
    private boolean checkEmulatorIndicators() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.toLowerCase().contains("emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.HOST.startsWith("Build")
            || (Build.BRAND.startsWith("generic") && Build.SERIAL.equals("unknown"))
            || (Build.PRODUCT.equals("sdk") || Build.PRODUCT.equals("google_sdk"))
            || Build.BOARD.toLowerCase().contains("nox")
            || Build.BOOTLOADER.toLowerCase().contains("nox")
            || Build.HARDWARE.toLowerCase().contains("nox")
            || Build.PRODUCT.toLowerCase().contains("nox");
    }
    
    /**
     * Detect if USB debugging is enabled
     */
    private void detectDebugMode(DeviceInfo info) {
        int adbEnabled = 0;
        try {
            adbEnabled = Settings.Global.getInt(context.getContentResolver(), 
                Settings.Global.ADB_ENABLED, 0);
        } catch (Exception e) {
            // Fallback
        }
        info.setDebugging(adbEnabled == 1);
        info.setDeveloperModeEnabled(adbEnabled == 1);
        info.addRawFeature("usb_debug", String.valueOf(adbEnabled == 1));
    }
    
    /**
     * Count installed applications (excluding system apps)
     */
    private void collectAppList(DeviceInfo info) {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        info.setInstalledAppCount(apps.size());
        info.addRawFeature("app_count", String.valueOf(apps.size()));
    }
    
    /**
     * Get current input method
     */
    private void collectInputMethod(DeviceInfo info) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> inputMethods = imm.getEnabledInputMethodList();
        StringBuilder ime = new StringBuilder();
        for (InputMethodInfo method : inputMethods) {
            ime.append(method.getId()).append("; ");
        }
        info.setInputMethod(ime.toString());
        info.addRawFeature("input_method", ime.toString());
    }
    
    /**
     * Check if device has fingerprint sensor and enrolled fingerprints
     */
    private void checkFingerprintSensor(DeviceInfo info) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                FingerprintManager fingerprintManager = 
                    (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
                if (fingerprintManager != null) {
                    info.setHasFingerprintSensor(fingerprintManager.isHardwareDetected());
                    info.setHasEnrolledFingerprints(fingerprintManager.hasEnrolledFingerprints());
                }
            } catch (SecurityException e) {
                // Missing USE_FINGERPRINT permission, skip fingerprint check
                info.setHasFingerprintSensor(false);
                info.setHasEnrolledFingerprints(false);
            }
        }
    }
    
    /**
     * Simple hash function for generating Pseudo ID
     */
    private String hashString(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }
}
