package com.device.fingerprint.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.device.fingerprint.R;
import com.device.fingerprint.model.DeviceInfo;
import com.device.fingerprint.ui.adapter.DeviceInfoAdapter;
import com.device.fingerprint.utils.SettingsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity showing detailed device information in categorized sections
 */
public class DeviceDetailActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private DeviceInfoAdapter adapter;
    private DeviceInfo deviceInfo;
    private Gson gson;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(SettingsManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        
        gson = new Gson();
        
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Device Details");
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.inflateMenu(R.menu.menu_device_detail);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_copy_json) {
                copyDeviceJson();
                return true;
            }
            return false;
        });
        
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device_info");
        
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        if (deviceInfo != null) {
            adapter = new DeviceInfoAdapter(createSectionData(deviceInfo));
            recyclerView.setAdapter(adapter);
        }
    }
    
    /**
     * Copy device info as JSON to clipboard for sharing/comparing across devices
     */
    private void copyDeviceJson() {
        if (deviceInfo == null) {
            Toast.makeText(this, "No device data", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String json = gson.toJson(deviceInfo);
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("Device Info JSON", json);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Device JSON copied to clipboard! Paste in Compare page.", 
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to copy", Toast.LENGTH_SHORT).show();
        }
    }
    
    private List<DeviceInfoAdapter.Section> createSectionData(DeviceInfo info) {
        List<DeviceInfoAdapter.Section> sections = new ArrayList<>();
        
        // === Hardware Section ===
        DeviceInfoAdapter.Section hardware = new DeviceInfoAdapter.Section("Hardware");
        hardware.addItem("Brand", info.getBrand());
        hardware.addItem("Model", info.getModel());
        hardware.addItem("Product", info.getProduct());
        hardware.addItem("Manufacturer", info.getManufacturer());
        hardware.addItem("Board", info.getBoard());
        hardware.addItem("Hardware", info.getHardware());
        hardware.addItem("Device", info.getDevice());
        hardware.addItem("Bootloader", info.getBootloader());
        hardware.addItem("CPU ABI", info.getCpuAbi());
        hardware.addItem("CPU Cores", String.valueOf(info.getCpuCores()));
        hardware.addItem("CPU Info", info.getCpuInfo());
        hardware.addItem("Total Memory", info.getFormattedMemory());
        hardware.addItem("Internal Storage", info.getFormattedStorage());
        hardware.addItem("Screen Resolution", info.getScreenResolution());
        hardware.addItem("Screen Density DPI", String.valueOf(info.getScreenDensityDpi()));
        hardware.addItem("Screen Refresh Rate", info.getScreenRefreshRate() + " Hz");
        hardware.addItem("Sensor Count", String.valueOf(info.getSensorCount()));
        hardware.addItem("Camera Info", info.getCameraInfo());
        hardware.addItem("Battery", info.getBatteryInfo());
        sections.add(hardware);
        
        // === Device Identifiers Section ===
        DeviceInfoAdapter.Section identifiers = new DeviceInfoAdapter.Section("Identifiers");
        identifiers.addItem("Android ID", info.getAndroidId());
        identifiers.addItem("Pseudo ID", info.getPseudoId());
        identifiers.addItem("OAID", info.getOaid() != null && !info.getOaid().isEmpty() ? 
                info.getOaid() : "Not available");
        identifiers.addItem("IMEI", info.getImei() != null ? info.getImei() : 
                "Restricted (Android 10+)");
        identifiers.addItem("Serial Number", info.getSerialNumber());
        identifiers.addItem("Fingerprint", info.getFingerprint());
        sections.add(identifiers);
        
        // === System Section ===
        DeviceInfoAdapter.Section system = new DeviceInfoAdapter.Section("System");
        system.addItem("OS Version", "Android " + info.getOsVersion());
        system.addItem("SDK Level", String.valueOf(info.getSdkInt()));
        system.addItem("Security Patch", info.getSecurityPatch());
        system.addItem("Language", info.getSystemLanguage());
        system.addItem("Timezone", info.getTimezone());
        system.addItem("System Fingerprint", info.getSystemFingerprint());
        system.addItem("Build Type", info.getBuildType());
        system.addItem("Build Tags", info.getBuildTags());
        system.addItem("Uptime", info.getFormattedUptime());
        system.addItem("Installed Apps", String.valueOf(info.getInstalledAppCount()));
        system.addItem("Input Method", info.getInputMethod());
        system.addItem("Fingerprint Sensor", String.valueOf(info.isHasFingerprintSensor()));
        system.addItem("Enrolled Fingerprints", String.valueOf(info.isHasEnrolledFingerprints()));
        sections.add(system);
        
        // === Security Section ===
        DeviceInfoAdapter.Section security = new DeviceInfoAdapter.Section("Security & Risk");
        security.addItem("Rooted", String.valueOf(info.isRooted()));
        security.addItem("Emulator", String.valueOf(info.isEmulator()));
        security.addItem("USB Debugging", String.valueOf(info.isDebugging()));
        security.addItem("Xposed Detected", String.valueOf(info.isHasXposed()));
        security.addItem("Frida Detected", String.valueOf(info.isHasFrida()));
        security.addItem("Magisk Detected", String.valueOf(info.isHasMagisk()));
        security.addItem("Mock Location", String.valueOf(info.isMockLocationEnabled()));
        security.addItem("Developer Mode", String.valueOf(info.isDeveloperModeEnabled()));
        security.addItem("Proxy Detected", String.valueOf(info.isHasProxy()));
        security.addItem("VPN Detected", String.valueOf(info.isHasVpn()));
        security.addItem("Suspicious Apps", String.valueOf(info.isHasSuspiciousApps()));
        security.addItem("Risk Score", String.valueOf(info.getRiskScore()));
        security.addItem("Risk Level", info.getRiskLevel());
        security.addItem("Risk Tags", info.getRiskTags() != null ? info.getRiskTags() : "None");
        sections.add(security);
        
        // === Network Section ===
        DeviceInfoAdapter.Section network = new DeviceInfoAdapter.Section("Network");
        network.addItem("IP Address", info.getIpAddress());
        network.addItem("WiFi SSID", info.getWifiSsid());
        network.addItem("WiFi BSSID", info.getWifiBssid());
        network.addItem("WiFi RSSI", String.valueOf(info.getWifiRssi()) + " dBm");
        network.addItem("Operator", info.getOperatorName());
        network.addItem("MCC/MNC", info.getMccMnc());
        network.addItem("Network Type", info.getNetworkType());
        network.addItem("Signal Strength", info.getSignalStrength());
        sections.add(network);
        
        // === Application Section ===
        DeviceInfoAdapter.Section app = new DeviceInfoAdapter.Section("Application");
        app.addItem("Package Name", info.getPackageName());
        app.addItem("Version Name", info.getAppVersionName());
        app.addItem("Version Code", String.valueOf(info.getAppVersionCode()));
        app.addItem("Signature Hash", info.getAppSignature());
        app.addItem("First Install", new java.text.SimpleDateFormat("yyyy-MM-dd", 
                java.util.Locale.getDefault()).format(new java.util.Date(info.getFirstInstallTime())));
        app.addItem("Last Update", new java.text.SimpleDateFormat("yyyy-MM-dd", 
                java.util.Locale.getDefault()).format(new java.util.Date(info.getLastUpdateTime())));
        app.addItem("Installer", info.getInstallerPackage());
        app.addItem("Process Name", info.getProcessName());
        sections.add(app);
        
        // === Collection Metadata ===
        DeviceInfoAdapter.Section meta = new DeviceInfoAdapter.Section("Collection Metadata");
        meta.addItem("Collection Time", info.getCollectionTime());
        meta.addItem("Total Features", String.valueOf(info.getRawFeatures().size()));
        sections.add(meta);
        
        return sections;
    }
}
