package com.device.fingerprint.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.device.fingerprint.R;
import com.device.fingerprint.collector.ApplicationCollector;
import com.device.fingerprint.collector.HardwareCollector;
import com.device.fingerprint.collector.NetworkCollector;
import com.device.fingerprint.collector.RiskDetector;
import com.device.fingerprint.collector.SystemCollector;
import com.device.fingerprint.generator.FingerprintGenerator;
import com.device.fingerprint.model.DeviceInfo;
import com.device.fingerprint.utils.PermissionHelper;
import com.device.fingerprint.utils.SettingsManager;
import com.device.fingerprint.utils.StorageManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main activity - Dashboard showing device overview and navigation
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(SettingsManager.applyLanguage(newBase));
    }
    
    private StorageManager storageManager;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    // UI Components
    private CardView cardOverview;
    private CardView cardRisk;
    private CardView cardFingerprint;
    private ProgressBar progressBar;
    private MaterialTextView tvDeviceName;
    private MaterialTextView tvAndroidVersion;
    private MaterialTextView tvFingerprint;
    private MaterialTextView tvRiskScore;
    private MaterialTextView tvFeatureCount;
    private MaterialTextView tvCollectionTime;
    private MaterialTextView tvRiskLevel;
    private MaterialButton btnCollect;
    private MaterialButton btnViewDetails;
    private MaterialButton btnCompare;
    private MaterialButton btnHistory;
    private View riskIndicator;
    
    private DeviceInfo currentDevice;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new SettingsManager(this).applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storageManager = new StorageManager(this);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupListeners();

        // Request permissions on first launch
        if (!PermissionHelper.hasAllPermissions(this)) {
            showPermissionRationale();
        }

        // Load previously saved device info
        currentDevice = storageManager.getCurrentDevice();
        if (currentDevice != null) {
            displayDeviceInfo(currentDevice);
        }
    }
    
    private void initViews() {
        cardOverview = findViewById(R.id.card_overview);
        cardRisk = findViewById(R.id.card_risk);
        cardFingerprint = findViewById(R.id.card_fingerprint);
        progressBar = findViewById(R.id.progress_bar);
        tvDeviceName = findViewById(R.id.tv_device_name);
        tvAndroidVersion = findViewById(R.id.tv_android_version);
        tvFingerprint = findViewById(R.id.tv_fingerprint);
        tvRiskScore = findViewById(R.id.tv_risk_score);
        tvFeatureCount = findViewById(R.id.tv_feature_count);
        tvCollectionTime = findViewById(R.id.tv_collection_time);
        tvRiskLevel = findViewById(R.id.tv_risk_level);
        btnCollect = findViewById(R.id.btn_collect);
        btnViewDetails = findViewById(R.id.btn_view_details);
        btnCompare = findViewById(R.id.btn_compare);
        btnHistory = findViewById(R.id.btn_history);
        MaterialButton btnSettings = findViewById(R.id.btn_settings);
        riskIndicator = findViewById(R.id.risk_indicator);
    }
    
    private void setupListeners() {
        btnCollect.setOnClickListener(v -> startCollection());
        
        btnViewDetails.setOnClickListener(v -> {
            if (currentDevice != null) {
                Intent intent = new Intent(this, DeviceDetailActivity.class);
                intent.putExtra("device_info", currentDevice);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please collect device info first", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnCompare.setOnClickListener(v -> {
            if (currentDevice != null) {
                Intent intent = new Intent(this, CompareActivity.class);
                intent.putExtra("current_device", currentDevice);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please collect device info first", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        });

        // Settings button
        MaterialButton btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }
    
    private void showPermissionRationale() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Permissions Required")
                .setMessage("This app needs certain permissions to collect device information for fingerprinting:\n\n" +
                        "- Phone State: For device identifier (IMEI)\n" +
                        "- Location: For cell tower and WiFi information\n\n" +
                        "These are used solely for device fingerprinting and risk assessment.")
                .setPositiveButton("Grant", (dialog, which) -> {
                    PermissionHelper.requestPermissions(this);
                })
                .setNegativeButton("Skip", (dialog, which) -> {
                    Toast.makeText(this, "Some features may be limited without permissions", 
                            Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }
    
    private void startCollection() {
        progressBar.setVisibility(View.VISIBLE);
        btnCollect.setEnabled(false);
        
        executorService.execute(() -> {
            try {
                DeviceInfo deviceInfo = collectDeviceInfo();
                
                // Generate fingerprint
                FingerprintGenerator generator = new FingerprintGenerator();
                String fingerprint = generator.generateFingerprint(deviceInfo.getRawFeatures());
                deviceInfo.setFingerprint(fingerprint);
                
                // Save to storage
                storageManager.saveCurrentDevice(deviceInfo);
                storageManager.saveToHistory(deviceInfo);
                
                currentDevice = deviceInfo;
                
                mainHandler.post(() -> {
                    displayDeviceInfo(deviceInfo);
                    progressBar.setVisibility(View.GONE);
                    btnCollect.setEnabled(true);
                    Toast.makeText(MainActivity.this, "Collection complete!", Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnCollect.setEnabled(true);
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private DeviceInfo collectDeviceInfo() {
        DeviceInfo info = new DeviceInfo();
        
        // Set collection time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        info.setCollectionTime(sdf.format(new Date()));
        
        // Collect from all collectors
        new HardwareCollector(this).collect(info);
        new SystemCollector(this).collect(info);
        new NetworkCollector(this).collect(info);
        new ApplicationCollector(this).collect(info);
        new RiskDetector(this).detect(info);
        
        return info;
    }
    
    private void displayDeviceInfo(DeviceInfo info) {
        cardOverview.setVisibility(View.VISIBLE);
        cardRisk.setVisibility(View.VISIBLE);
        cardFingerprint.setVisibility(View.VISIBLE);
        
        tvDeviceName.setText(String.format("%s %s", info.getBrand(), info.getModel()));
        tvAndroidVersion.setText(String.format("Android %s (API %d)", 
                info.getOsVersion(), info.getSdkInt()));
        tvFingerprint.setText(info.getFingerprint() != null ? 
                info.getFingerprint().toUpperCase() : "N/A");
        tvRiskScore.setText(String.format(Locale.getDefault(), "%.0f/100", info.getRiskScore()));
        tvFeatureCount.setText(String.valueOf(info.getRawFeatures().size()));
        tvCollectionTime.setText(info.getCollectionTime());
        
        // Risk level
        String riskLevel = info.getRiskLevel();
        tvRiskLevel.setText(riskLevel);
        
        // Risk indicator color
        int riskColor = getRiskColor(info.getRiskScore());
        riskIndicator.setBackgroundColor(riskColor);
        tvRiskLevel.setTextColor(riskColor);
        tvRiskScore.setTextColor(riskColor);
        
        btnViewDetails.setEnabled(true);
        btnCompare.setEnabled(true);
    }
    
    private int getRiskColor(double riskScore) {
        if (riskScore >= 70) return 0xFFF44336; // Red
        if (riskScore >= 50) return 0xFFFF9800; // Orange
        if (riskScore >= 30) return 0xFFFFC107; // Amber
        if (riskScore >= 10) return 0xFF4CAF50; // Green
        return 0xFF2E7D32; // Dark Green
    }
    
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.REQUEST_CODE_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Toast.makeText(this, "Permissions granted! You can now collect device info.", 
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Some permissions denied. Collection will use limited features.", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
