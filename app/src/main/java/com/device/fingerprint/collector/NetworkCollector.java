package com.device.fingerprint.collector;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

import com.device.fingerprint.model.DeviceInfo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

/**
 * Collector for network-related device information
 * Collects: IP address, WiFi info, operator, network type, signal strength, etc.
 */
public class NetworkCollector {
    
    private Context context;
    
    public NetworkCollector(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Collect all network information
     */
    public void collect(DeviceInfo info) {
        collectIpAddress(info);
        collectWifiInfo(info);
        collectMobileNetworkInfo(info);
        detectProxy(info);
        detectVpn(info);
    }
    
    /**
     * Collect IP address
     */
    private void collectIpAddress(DeviceInfo info) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.isLoopbackAddress()) continue;
                    String ip = addr.getHostAddress();
                    if (ip != null && ip.indexOf(':') < 0) { // IPv4 only
                        info.setIpAddress(ip);
                        info.addRawFeature("ip_address", ip);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            info.setIpAddress("N/A");
        }
    }
    
    /**
     * Collect WiFi information (SSID, BSSID, RSSI)
     */
    private void collectWifiInfo(DeviceInfo info) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    String ssid = wifiInfo.getSSID();
                    if (ssid != null && !ssid.equals("<unknown ssid>")) {
                        info.setWifiSsid(ssid.replace("\"", ""));
                    }
                    try {
                        info.setWifiBssid(wifiInfo.getBSSID());
                    } catch (Exception e) {
                        info.setWifiBssid("unknown");
                    }
                    info.setWifiRssi(wifiInfo.getRssi());
                    
                    info.addRawFeature("wifi_ssid", info.getWifiSsid());
                    info.addRawFeature("wifi_bssid", info.getWifiBssid());
                }
            }
        } catch (Exception e) {
            // WiFi info may not be available
        }
    }
    
    /**
     * Collect mobile network information
     */
    private void collectMobileNetworkInfo(DeviceInfo info) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) 
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                // Operator name
                String operatorName = telephonyManager.getNetworkOperatorName();
                info.setOperatorName(operatorName);
                info.addRawFeature("operator", operatorName);
                
                // MCC+MNC
                String mccMnc = telephonyManager.getNetworkOperator();
                info.setMccMnc(mccMnc);
                info.addRawFeature("mcc_mnc", mccMnc);
                
                // Network type
                String networkType = getNetworkTypeString(telephonyManager);
                info.setNetworkType(networkType);
                info.addRawFeature("network_type", networkType);
                
                // Signal strength
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    android.telephony.SignalStrength signalStrength = telephonyManager.getSignalStrength();
                    if (signalStrength != null) {
                        info.setSignalStrength(String.valueOf(signalStrength.getCellSignalStrengths()));
                    }
                }
                
                // Cell info
                if (ActivityCompat.checkSelfPermission(context, 
                        Manifest.permission.ACCESS_FINE_LOCATION) == 
                        android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
                    if (cellInfoList != null && !cellInfoList.isEmpty()) {
                        info.setCellInfo(cellInfoList.get(0).toString());
                    }
                }
            }
        } catch (Exception e) {
            // Telephony info may not be available on tablets
        }
    }
    
    /**
     * Convert network type constant to readable string
     */
    private String getNetworkTypeString(TelephonyManager tm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            switch (tm.getDataNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "4G";
                case TelephonyManager.NETWORK_TYPE_NR:
                    return "5G";
                default:
                    return "UNKNOWN";
            }
        }
        return "UNKNOWN";
    }
    
    /**
     * Detect if device is using proxy
     */
    private void detectProxy(DeviceInfo info) {
        String proxyHost = android.net.Proxy.getDefaultHost();
        int proxyPort = android.net.Proxy.getDefaultPort();
        boolean hasProxy = !TextUtils.isEmpty(proxyHost) && proxyPort != -1;
        info.setHasProxy(hasProxy);
        info.addRawFeature("proxy", String.valueOf(hasProxy));
    }
    
    /**
     * Detect if device is using VPN
     */
    private void detectVpn(DeviceInfo info) {
        boolean hasVpn = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ConnectivityManager cm = (ConnectivityManager) 
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    hasVpn = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
                }
            } else {
                // Legacy method
                String iface = "";
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface i = interfaces.nextElement();
                    if (i.isUp() && !i.isLoopback() && 
                        (i.getName().contains("tun") || i.getName().contains("ppp") || 
                         i.getName().contains("pptp"))) {
                        hasVpn = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        info.setHasVpn(hasVpn);
        info.addRawFeature("vpn", String.valueOf(hasVpn));
    }
}
