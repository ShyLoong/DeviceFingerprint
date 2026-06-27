package com.device.fingerprint.collector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import com.device.fingerprint.model.DeviceInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * Detector for risk environment indicators
 * Detects: Xposed, Frida, Magisk, mock location, suspicious apps, etc.
 */
public class RiskDetector {
    
    private Context context;
    
    // Known suspicious/fraud-related package names
    private static final String[] SUSPICIOUS_PACKAGES = {
        // Hook frameworks
        "de.robv.android.xposed.installer",
        "com.saurik.substrate",
        "com.android.vending.billing.InAppBillingService.COIN",
        "com.chelpus.lackypatch",
        "com.charles.lpoqasert",
        // Emulators
        "com.bluestacks.appfinder",
        "com.bignox.app.phone",
        "com.bignox.app.noxservice",
        "com.microvirt.guide",
        // Mock location
        "com.lexa.fakegps",
        "com.incorporateapps.fakegps.fre",
        // VPN/Proxy
        "org.proxydroid",
        "com.github.shadowsocks",
        // Root tools
        "com.koushikdutta.rommanager",
        "com.koushikdutta.rommanager.license",
        "com.dimonvideo.luckypatcher",
        "com.chelpus.luckypatcher"
    };
    
    // Known Xposed-related strings in process maps
    private static final String[] XPOSED_INDICATORS = {
        "XposedBridge",
        "libxposed",
        "edposed",
        "lsposed",
        "libepic"
    };
    
    // Known Frida-related strings
    private static final String[] FRIDA_INDICATORS = {
        "frida",
        "frida-server",
        "libfrida",
        "frida-gadget"
    };
    
    public RiskDetector(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Perform full risk detection
     */
    public void detect(DeviceInfo info) {
        detectXposed(info);
        detectFrida(info);
        detectMockLocation(info);
        detectSuspiciousApps(info);
        
        // Calculate final risk score
        info.calculateRiskScore();
    }
    
    /**
     * Detect Xposed framework
     */
    private void detectXposed(DeviceInfo info) {
        boolean hasXposed = false;
        
        // Check for Xposed classes
        try {
            Class.forName("de.robv.android.xposed.XposedBridge");
            hasXposed = true;
        } catch (ClassNotFoundException e) {
            // Not found
        }
        
        // Check for Xposed-related packages
        if (!hasXposed) {
            hasXposed = checkPackages(new String[]{
                "de.robv.android.xposed.installer",
                "io.github.lsposed.manager",
                "org.meowcat.edxposed.manager"
            });
        }
        
        // Check process maps for Xposed libraries
        if (!hasXposed) {
            hasXposed = checkProcessMaps(XPOSED_INDICATORS);
        }
        
        // Check for native Xposed hooks by examining stack trace
        if (!hasXposed) {
            hasXposed = detectXposedByStackTrace();
        }
        
        info.setHasXposed(hasXposed);
    }
    
    /**
     * Detect Frida framework
     */
    private void detectFrida(DeviceInfo info) {
        boolean hasFrida = false;
        
        // Check process maps for Frida libraries
        hasFrida = checkProcessMaps(FRIDA_INDICATORS);
        
        // Check for Frida-specific ports
        if (!hasFrida) {
            hasFrida = checkFridaPorts();
        }
        
        // Check for Frida named pipes
        if (!hasFrida) {
            hasFrida = checkFridaPipes();
        }
        
        // Check for suspicious thread names
        if (!hasFrida) {
            hasFrida = checkFridaThreads();
        }
        
        info.setHasFrida(hasFrida);
    }
    
    /**
     * Detect mock location settings
     */
    private void detectMockLocation(DeviceInfo info) {
        boolean mockLocation = false;
        try {
            if (Settings.Secure.getInt(context.getContentResolver(), 
                    Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0) {
                mockLocation = true;
            }
        } catch (Exception e) {
            // Fallback
        }
        
        // Check if any app has mock location permission
        if (!mockLocation && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                PackageManager pm = context.getPackageManager();
                List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                for (ApplicationInfo app : apps) {
                    if (pm.checkPermission("android.permission.ACCESS_MOCK_LOCATION", 
                            app.packageName) == PackageManager.PERMISSION_GRANTED) {
                        mockLocation = true;
                        break;
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        
        info.setMockLocationEnabled(mockLocation);
    }
    
    /**
     * Detect suspicious/risky applications
     */
    private void detectSuspiciousApps(DeviceInfo info) {
        boolean hasSuspicious = checkPackages(SUSPICIOUS_PACKAGES);
        info.setHasSuspiciousApps(hasSuspicious);
    }
    
    /**
     * Check if any of the given packages are installed
     */
    private boolean checkPackages(String[] packages) {
        PackageManager pm = context.getPackageManager();
        for (String pkg : packages) {
            try {
                pm.getPackageInfo(pkg, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                // Not installed
            }
        }
        return false;
    }
    
    /**
     * Check process maps for indicator strings
     */
    private boolean checkProcessMaps(String[] indicators) {
        try {
            File mapsFile = new File("/proc/self/maps");
            if (!mapsFile.exists()) return false;
            
            BufferedReader reader = new BufferedReader(new FileReader(mapsFile));
            String line;
            while ((line = reader.readLine()) != null) {
                for (String indicator : indicators) {
                    if (line.toLowerCase().contains(indicator.toLowerCase())) {
                        reader.close();
                        return true;
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }
    
    /**
     * Detect Xposed by examining stack trace for suspicious method names
     */
    private boolean detectXposedByStackTrace() {
        try {
            throw new Exception("Detect Xposed");
        } catch (Exception e) {
            for (StackTraceElement element : e.getStackTrace()) {
                String className = element.getClassName();
                if (className.contains("xposed") || className.contains("Xposed") ||
                    className.contains("lsposed") || className.contains("LSPosed") ||
                    className.contains("edposed")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check for Frida default ports (27042)
     */
    private boolean checkFridaPorts() {
        try {
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress("127.0.0.1", 27042), 100);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check for Frida named pipes
     */
    private boolean checkFridaPipes() {
        String[] pipePaths = {
            "/data/local/tmp/frida-server",
            "/data/local/tmp/frida-gadget",
            "/data/local/tmp/re.frida.server"
        };
        for (String path : pipePaths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }
    
    /**
     * Check for suspicious thread names related to Frida
     */
    private boolean checkFridaThreads() {
        try {
            java.util.Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
            for (Thread thread : stacks.keySet()) {
                String name = thread.getName();
                if (name != null && (name.contains("frida") || name.contains("gum-js") || 
                                     name.contains("gmain"))) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }
}
