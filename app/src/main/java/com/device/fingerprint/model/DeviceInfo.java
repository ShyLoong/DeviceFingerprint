package com.device.fingerprint.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Data model class that holds all collected device information
 * across 5 dimensions: Hardware, System, Network, Application, and Behavior
 */
public class DeviceInfo implements Serializable {
    
    // === Basic Info ===
    private String collectionTime;
    private String fingerprint;
    private double riskScore;
    
    // === Hardware Profile ===
    private String brand;
    private String model;
    private String product;
    private String board;
    private String hardware;
    private String manufacturer;
    private String device;
    private String bootloader;
    private String cpuAbi;
    private int cpuCores;
    private String cpuInfo;
    private long totalMemory;
    private long internalStorage;
    private String screenResolution;
    private int screenDensityDpi;
    private float screenRefreshRate;
    private String sensorList;
    private int sensorCount;
    private String batteryInfo;
    private String cameraInfo;
    private String imei;
    private String oaid;
    private String androidId;
    private String pseudoId;
    private String serialNumber;
    
    // === System Profile ===
    private String osVersion;
    private int sdkInt;
    private String securityPatch;
    private String systemLanguage;
    private String timezone;
    private String systemFingerprint;
    private String buildType;
    private String buildTags;
    private long uptime;
    private boolean isRooted;
    private boolean isEmulator;
    private boolean isDebugging;
    private boolean selinuxEnabled;
    private int installedAppCount;
    private String inputMethod;
    private boolean hasFingerprintSensor;
    private boolean hasEnrolledFingerprints;
    
    // === Network Profile ===
    private String ipAddress;
    private String wifiSsid;
    private String wifiBssid;
    private int wifiRssi;
    private String operatorName;
    private String mccMnc;
    private String networkType;
    private String signalStrength;
    private String cellInfo;
    private String dnsServers;
    private boolean hasProxy;
    private boolean hasVpn;
    
    // === Application Profile ===
    private String packageName;
    private String appVersionName;
    private int appVersionCode;
    private String appSignature;
    private long firstInstallTime;
    private long lastUpdateTime;
    private String installerPackage;
    private String processName;
    
    // === Risk Detection ===
    private boolean hasRootTools;
    private boolean hasMagisk;
    private boolean hasXposed;
    private boolean hasFrida;
    private boolean isMockLocationEnabled;
    private boolean isDeveloperModeEnabled;
    private boolean hasSuspiciousApps;
    private String riskTags;
    
    // === Raw Feature Map for fingerprint generation ===
    private Map<String, String> rawFeatures;
    
    public DeviceInfo() {
        this.rawFeatures = new HashMap<>();
    }
    
    // === Getters and Setters ===
    
    public String getCollectionTime() { return collectionTime; }
    public void setCollectionTime(String collectionTime) { this.collectionTime = collectionTime; }
    
    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }
    
    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
    
    // Hardware getters/setters
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }
    
    public String getBoard() { return board; }
    public void setBoard(String board) { this.board = board; }
    
    public String getHardware() { return hardware; }
    public void setHardware(String hardware) { this.hardware = hardware; }
    
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    
    public String getBootloader() { return bootloader; }
    public void setBootloader(String bootloader) { this.bootloader = bootloader; }
    
    public String getCpuAbi() { return cpuAbi; }
    public void setCpuAbi(String cpuAbi) { this.cpuAbi = cpuAbi; }
    
    public int getCpuCores() { return cpuCores; }
    public void setCpuCores(int cpuCores) { this.cpuCores = cpuCores; }
    
    public String getCpuInfo() { return cpuInfo; }
    public void setCpuInfo(String cpuInfo) { this.cpuInfo = cpuInfo; }
    
    public long getTotalMemory() { return totalMemory; }
    public void setTotalMemory(long totalMemory) { this.totalMemory = totalMemory; }
    
    public long getInternalStorage() { return internalStorage; }
    public void setInternalStorage(long internalStorage) { this.internalStorage = internalStorage; }
    
    public String getScreenResolution() { return screenResolution; }
    public void setScreenResolution(String screenResolution) { this.screenResolution = screenResolution; }
    
    public int getScreenDensityDpi() { return screenDensityDpi; }
    public void setScreenDensityDpi(int screenDensityDpi) { this.screenDensityDpi = screenDensityDpi; }
    
    public float getScreenRefreshRate() { return screenRefreshRate; }
    public void setScreenRefreshRate(float screenRefreshRate) { this.screenRefreshRate = screenRefreshRate; }
    
    public String getSensorList() { return sensorList; }
    public void setSensorList(String sensorList) { this.sensorList = sensorList; }
    
    public int getSensorCount() { return sensorCount; }
    public void setSensorCount(int sensorCount) { this.sensorCount = sensorCount; }
    
    public String getBatteryInfo() { return batteryInfo; }
    public void setBatteryInfo(String batteryInfo) { this.batteryInfo = batteryInfo; }
    
    public String getCameraInfo() { return cameraInfo; }
    public void setCameraInfo(String cameraInfo) { this.cameraInfo = cameraInfo; }
    
    public String getImei() { return imei; }
    public void setImei(String imei) { this.imei = imei; }
    
    public String getOaid() { return oaid; }
    public void setOaid(String oaid) { this.oaid = oaid; }
    
    public String getAndroidId() { return androidId; }
    public void setAndroidId(String androidId) { this.androidId = androidId; }
    
    public String getPseudoId() { return pseudoId; }
    public void setPseudoId(String pseudoId) { this.pseudoId = pseudoId; }
    
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    
    // System getters/setters
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    
    public int getSdkInt() { return sdkInt; }
    public void setSdkInt(int sdkInt) { this.sdkInt = sdkInt; }
    
    public String getSecurityPatch() { return securityPatch; }
    public void setSecurityPatch(String securityPatch) { this.securityPatch = securityPatch; }
    
    public String getSystemLanguage() { return systemLanguage; }
    public void setSystemLanguage(String systemLanguage) { this.systemLanguage = systemLanguage; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public String getSystemFingerprint() { return systemFingerprint; }
    public void setSystemFingerprint(String systemFingerprint) { this.systemFingerprint = systemFingerprint; }
    
    public String getBuildType() { return buildType; }
    public void setBuildType(String buildType) { this.buildType = buildType; }
    
    public String getBuildTags() { return buildTags; }
    public void setBuildTags(String buildTags) { this.buildTags = buildTags; }
    
    public long getUptime() { return uptime; }
    public void setUptime(long uptime) { this.uptime = uptime; }
    
    public boolean isRooted() { return isRooted; }
    public void setRooted(boolean rooted) { isRooted = rooted; }
    
    public boolean isEmulator() { return isEmulator; }
    public void setEmulator(boolean emulator) { isEmulator = emulator; }
    
    public boolean isDebugging() { return isDebugging; }
    public void setDebugging(boolean debugging) { isDebugging = debugging; }
    
    public boolean isSelinuxEnabled() { return selinuxEnabled; }
    public void setSelinuxEnabled(boolean selinuxEnabled) { this.selinuxEnabled = selinuxEnabled; }
    
    public int getInstalledAppCount() { return installedAppCount; }
    public void setInstalledAppCount(int installedAppCount) { this.installedAppCount = installedAppCount; }
    
    public String getInputMethod() { return inputMethod; }
    public void setInputMethod(String inputMethod) { this.inputMethod = inputMethod; }
    
    public boolean isHasFingerprintSensor() { return hasFingerprintSensor; }
    public void setHasFingerprintSensor(boolean hasFingerprintSensor) { this.hasFingerprintSensor = hasFingerprintSensor; }
    
    public boolean isHasEnrolledFingerprints() { return hasEnrolledFingerprints; }
    public void setHasEnrolledFingerprints(boolean hasEnrolledFingerprints) { this.hasEnrolledFingerprints = hasEnrolledFingerprints; }
    
    // Network getters/setters
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getWifiSsid() { return wifiSsid; }
    public void setWifiSsid(String wifiSsid) { this.wifiSsid = wifiSsid; }
    
    public String getWifiBssid() { return wifiBssid; }
    public void setWifiBssid(String wifiBssid) { this.wifiBssid = wifiBssid; }
    
    public int getWifiRssi() { return wifiRssi; }
    public void setWifiRssi(int wifiRssi) { this.wifiRssi = wifiRssi; }
    
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    
    public String getMccMnc() { return mccMnc; }
    public void setMccMnc(String mccMnc) { this.mccMnc = mccMnc; }
    
    public String getNetworkType() { return networkType; }
    public void setNetworkType(String networkType) { this.networkType = networkType; }
    
    public String getSignalStrength() { return signalStrength; }
    public void setSignalStrength(String signalStrength) { this.signalStrength = signalStrength; }
    
    public String getCellInfo() { return cellInfo; }
    public void setCellInfo(String cellInfo) { this.cellInfo = cellInfo; }
    
    public String getDnsServers() { return dnsServers; }
    public void setDnsServers(String dnsServers) { this.dnsServers = dnsServers; }
    
    public boolean isHasProxy() { return hasProxy; }
    public void setHasProxy(boolean hasProxy) { this.hasProxy = hasProxy; }
    
    public boolean isHasVpn() { return hasVpn; }
    public void setHasVpn(boolean hasVpn) { this.hasVpn = hasVpn; }
    
    // Application getters/setters
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    
    public String getAppVersionName() { return appVersionName; }
    public void setAppVersionName(String appVersionName) { this.appVersionName = appVersionName; }
    
    public int getAppVersionCode() { return appVersionCode; }
    public void setAppVersionCode(int appVersionCode) { this.appVersionCode = appVersionCode; }
    
    public String getAppSignature() { return appSignature; }
    public void setAppSignature(String appSignature) { this.appSignature = appSignature; }
    
    public long getFirstInstallTime() { return firstInstallTime; }
    public void setFirstInstallTime(long firstInstallTime) { this.firstInstallTime = firstInstallTime; }
    
    public long getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    
    public String getInstallerPackage() { return installerPackage; }
    public void setInstallerPackage(String installerPackage) { this.installerPackage = installerPackage; }
    
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    
    // Risk detection getters/setters
    public boolean isHasRootTools() { return hasRootTools; }
    public void setHasRootTools(boolean hasRootTools) { this.hasRootTools = hasRootTools; }
    
    public boolean isHasMagisk() { return hasMagisk; }
    public void setHasMagisk(boolean hasMagisk) { this.hasMagisk = hasMagisk; }
    
    public boolean isHasXposed() { return hasXposed; }
    public void setHasXposed(boolean hasXposed) { this.hasXposed = hasXposed; }
    
    public boolean isHasFrida() { return hasFrida; }
    public void setHasFrida(boolean hasFrida) { this.hasFrida = hasFrida; }
    
    public boolean isMockLocationEnabled() { return isMockLocationEnabled; }
    public void setMockLocationEnabled(boolean mockLocationEnabled) { isMockLocationEnabled = mockLocationEnabled; }
    
    public boolean isDeveloperModeEnabled() { return isDeveloperModeEnabled; }
    public void setDeveloperModeEnabled(boolean developerModeEnabled) { isDeveloperModeEnabled = developerModeEnabled; }
    
    public boolean isHasSuspiciousApps() { return hasSuspiciousApps; }
    public void setHasSuspiciousApps(boolean hasSuspiciousApps) { this.hasSuspiciousApps = hasSuspiciousApps; }
    
    public String getRiskTags() { return riskTags; }
    public void setRiskTags(String riskTags) { this.riskTags = riskTags; }
    
    public Map<String, String> getRawFeatures() { return rawFeatures; }
    public void setRawFeatures(Map<String, String> rawFeatures) { this.rawFeatures = rawFeatures; }
    
    public void addRawFeature(String key, String value) {
        if (value != null && !value.isEmpty()) {
            rawFeatures.put(key, value);
        }
    }
    
    /**
     * Calculate risk score based on detected risk factors
     */
    public void calculateRiskScore() {
        int score = 0;
        StringBuilder tags = new StringBuilder();
        
        if (isRooted) { score += 25; tags.append("ROOTED;"); }
        if (isEmulator) { score += 30; tags.append("EMULATOR;"); }
        if (isDebugging) { score += 10; tags.append("USB_DEBUG;"); }
        if (hasXposed) { score += 35; tags.append("XPOSED;"); }
        if (hasFrida) { score += 35; tags.append("FRIDA;"); }
        if (hasMagisk) { score += 30; tags.append("MAGISK;"); }
        if (hasProxy) { score += 15; tags.append("PROXY;"); }
        if (hasVpn) { score += 15; tags.append("VPN;"); }
        if (isMockLocationEnabled) { score += 20; tags.append("MOCK_LOCATION;"); }
        if (isDeveloperModeEnabled) { score += 5; tags.append("DEV_MODE;"); }
        if (hasSuspiciousApps) { score += 10; tags.append("SUSPICIOUS_APP;"); }
        
        this.riskScore = Math.min(score, 100);
        this.riskTags = tags.toString();
    }
    
    /**
     * Get formatted memory string
     */
    public String getFormattedMemory() {
        return formatBytes(totalMemory);
    }
    
    /**
     * Get formatted storage string
     */
    public String getFormattedStorage() {
        return formatBytes(internalStorage);
    }
    
    /**
     * Get formatted uptime string
     */
    public String getFormattedUptime() {
        long seconds = uptime / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        }
        return hours + "h " + minutes + "m";
    }
    
    private String formatBytes(long bytes) {
        if (bytes <= 0) return "N/A";
        String[] units = {"B", "KB", "MB", "GB"};
        int unit = 0;
        double size = bytes;
        while (size >= 1024 && unit < units.length - 1) {
            size /= 1024;
            unit++;
        }
        return String.format("%.2f %s", size, units[unit]);
    }
    
    /**
     * Get risk level description
     */
    public String getRiskLevel() {
        if (riskScore >= 70) return "CRITICAL";
        if (riskScore >= 50) return "HIGH";
        if (riskScore >= 30) return "MEDIUM";
        if (riskScore >= 10) return "LOW";
        return "SAFE";
    }
}
