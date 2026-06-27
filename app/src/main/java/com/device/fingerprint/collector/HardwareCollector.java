package com.device.fingerprint.collector;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.device.fingerprint.model.DeviceInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Collector for hardware-related device information
 * Collects: brand, model, CPU, memory, screen, sensors, battery, camera, etc.
 */
public class HardwareCollector {
    
    private Context context;
    
    public HardwareCollector(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Collect all hardware information
     */
    public void collect(DeviceInfo info) {
        collectBasicHardware(info);
        collectCpuInfo(info);
        collectMemoryInfo(info);
        collectStorageInfo(info);
        collectScreenInfo(info);
        collectSensorInfo(info);
        collectBatteryInfo(info);
        collectCameraInfo(info);
    }
    
    /**
     * Collect basic hardware properties from Build class
     */
    private void collectBasicHardware(DeviceInfo info) {
        info.setBrand(Build.BRAND);
        info.setModel(Build.MODEL);
        info.setProduct(Build.PRODUCT);
        info.setBoard(Build.BOARD);
        info.setHardware(Build.HARDWARE);
        info.setManufacturer(Build.MANUFACTURER);
        info.setDevice(Build.DEVICE);
        info.setBootloader(Build.BOOTLOADER);
        info.setSerialNumber(Build.SERIAL);
        
        // CPU ABI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            info.setCpuAbi(String.join(", ", Build.SUPPORTED_ABIS));
        } else {
            info.setCpuAbi(Build.CPU_ABI + ", " + Build.CPU_ABI2);
        }
        info.setCpuCores(Runtime.getRuntime().availableProcessors());
        
        // Add to raw features
        info.addRawFeature("brand", Build.BRAND);
        info.addRawFeature("model", Build.MODEL);
        info.addRawFeature("board", Build.BOARD);
        info.addRawFeature("hardware", Build.HARDWARE);
        info.addRawFeature("cpu_abi", info.getCpuAbi());
        info.addRawFeature("cpu_cores", String.valueOf(info.getCpuCores()));
    }
    
    /**
     * Collect CPU detailed information from /proc/cpuinfo
     */
    private void collectCpuInfo(DeviceInfo info) {
        StringBuilder cpuInfo = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/cpuinfo"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Hardware") || line.contains("Processor") || 
                    line.contains("model name") || line.contains("cpu cores")) {
                    cpuInfo.append(line).append("; ");
                }
            }
            reader.close();
        } catch (IOException e) {
            cpuInfo.append("N/A");
        }
        info.setCpuInfo(cpuInfo.toString());
        info.addRawFeature("cpu_info", cpuInfo.toString());
    }
    
    /**
     * Collect memory (RAM) information
     */
    private void collectMemoryInfo(DeviceInfo info) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        info.setTotalMemory(memoryInfo.totalMem);
        info.addRawFeature("total_memory", String.valueOf(memoryInfo.totalMem));
    }
    
    /**
     * Collect internal storage information
     */
    private void collectStorageInfo(DeviceInfo info) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        info.setInternalStorage(blockSize * totalBlocks);
        info.addRawFeature("internal_storage", String.valueOf(blockSize * totalBlocks));
    }
    
    /**
     * Collect screen display information
     */
    private void collectScreenInfo(DeviceInfo info) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        
        info.setScreenResolution(metrics.widthPixels + "x" + metrics.heightPixels);
        info.setScreenDensityDpi(metrics.densityDpi);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For API 30+, we need a different approach
            info.setScreenRefreshRate(60.0f); // Default fallback
        } else {
            float refreshRate = windowManager.getDefaultDisplay().getRefreshRate();
            info.setScreenRefreshRate(refreshRate);
        }
        
        info.addRawFeature("screen_resolution", info.getScreenResolution());
        info.addRawFeature("screen_density", String.valueOf(metrics.densityDpi));
    }
    
    /**
     * Collect sensor information
     */
    private void collectSensorInfo(DeviceInfo info) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        
        StringBuilder sensorBuilder = new StringBuilder();
        for (Sensor sensor : sensorList) {
            sensorBuilder.append(sensor.getName())
                    .append("(")
                    .append(sensor.getType())
                    .append(")")
                    .append(";")
                    .append(" ");
        }
        
        info.setSensorList(sensorBuilder.toString());
        info.setSensorCount(sensorList.size());
        info.addRawFeature("sensors", sensorBuilder.toString());
        info.addRawFeature("sensor_count", String.valueOf(sensorList.size()));
    }
    
    /**
     * Collect battery information
     */
    private void collectBatteryInfo(DeviceInfo info) {
        // Battery info requires a sticky intent, simplified here
        StringBuilder battery = new StringBuilder();
        battery.append("Technology: Li-ion; ");
        battery.append("Status: Unknown; ");
        info.setBatteryInfo(battery.toString());
        info.addRawFeature("battery", battery.toString());
    }
    
    /**
     * Collect camera information
     */
    private void collectCameraInfo(DeviceInfo info) {
        StringBuilder cameraBuilder = new StringBuilder();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                String[] cameraIds = cameraManager.getCameraIdList();
                for (String cameraId : cameraIds) {
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    String facingStr = (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) 
                        ? "Front" : "Back";
                    cameraBuilder.append("Camera").append(cameraId).append("(").append(facingStr).append("); ");
                }
            } catch (CameraAccessException e) {
                cameraBuilder.append("Error accessing camera");
            }
        } else {
            int numCameras = Camera.getNumberOfCameras();
            cameraBuilder.append("Cameras: ").append(numCameras);
        }
        
        info.setCameraInfo(cameraBuilder.toString());
        info.addRawFeature("camera", cameraBuilder.toString());
    }
}
