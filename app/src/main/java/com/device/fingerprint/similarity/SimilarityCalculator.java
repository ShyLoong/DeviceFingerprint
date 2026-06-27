package com.device.fingerprint.similarity;

import com.device.fingerprint.model.ComparisonResult;
import com.device.fingerprint.model.DeviceInfo;
import com.device.fingerprint.model.FieldDiff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Calculator for device similarity based on multiple dimensions.
 * Computes per-dimension scores and collects field-level diffs for UI display.
 */
public class SimilarityCalculator {

    private static final int HASH_LENGTH = 64;

    private static final double HARDWARE_WEIGHT = 0.35;
    private static final double SYSTEM_WEIGHT  = 0.30;
    private static final double NETWORK_WEIGHT = 0.15;
    private static final double APP_WEIGHT     = 0.10;
    private static final double RISK_WEIGHT    = 0.10;

    // Dimension feature keys and display names
    private static final String[][] HARDWARE_FIELDS = {
            {"brand","Brand"},{"model","Model"},{"board","Board"},
            {"hardware","Hardware"},{"cpu_abi","CPU ABI"},{"cpu_info","CPU Info"},
            {"total_memory","Total Memory"},{"internal_storage","Internal Storage"},
            {"screen_resolution","Screen Resolution"},{"screen_density","Screen Density"},
            {"sensors","Sensors"},{"sensor_count","Sensor Count"},{"camera","Camera"}
    };
    private static final String[][] SYSTEM_FIELDS = {
            {"os_version","OS Version"},{"sdk_int","SDK Level"},
            {"system_fingerprint","System Fingerprint"},{"language","Language"},
            {"timezone","Timezone"},{"android_id","Android ID"},
            {"pseudo_id","Pseudo ID"},{"is_rooted","Rooted"},
            {"is_emulator","Emulator"},{"app_count","Installed Apps"},
            {"input_method","Input Method"}
    };
    private static final String[][] NETWORK_FIELDS = {
            {"ip_address","IP Address"},{"wifi_ssid","WiFi SSID"},
            {"wifi_bssid","WiFi BSSID"},{"operator","Operator"},
            {"mcc_mnc","MCC/MNC"},{"network_type","Network Type"},
            {"proxy","Proxy"},{"vpn","VPN"}
    };
    private static final String[][] APP_FIELDS = {
            {"package_name","Package Name"},{"app_version","App Version"},
            {"app_signature","App Signature"},{"install_time","Install Time"}
    };

    // Risk field definitions: fieldKey, displayName, riskGetter index
    private static final String[][] RISK_FIELDS = {
            {"is_rooted","Rooted"},{"is_emulator","Emulator"},{"has_xposed","Xposed"},
            {"has_frida","Frida"},{"has_proxy","Proxy"},{"has_vpn","VPN"},
            {"mock_location","Mock Location"}
    };

    public int hammingDistance(String fp1, String fp2) {
        if (fp1 == null || fp2 == null) return -1;
        String b1 = hexToBinary(fp1, HASH_LENGTH);
        String b2 = hexToBinary(fp2, HASH_LENGTH);
        int max = Math.max(b1.length(), b2.length());
        while (b1.length() < max) b1 += "0";
        while (b2.length() < max) b2 += "0";
        int d = 0;
        for (int i = 0; i < max; i++) if (b1.charAt(i) != b2.charAt(i)) d++;
        return d;
    }

    /**
     * Compare two devices and return a result that includes dimension scores
     * and field-level diffs.
     */
    public ComparisonResult compareDevices(DeviceInfo d1, DeviceInfo d2) {
        ComparisonResult result = new ComparisonResult(d1, d2);
        List<FieldDiff> diffs = new ArrayList<>();

        double hw = compareDimension(d1.getRawFeatures(), d2.getRawFeatures(), HARDWARE_FIELDS, "Hardware", diffs);
        double sy = compareDimension(d1.getRawFeatures(), d2.getRawFeatures(), SYSTEM_FIELDS,  "System",  diffs);
        double nw = compareDimension(d1.getRawFeatures(), d2.getRawFeatures(), NETWORK_FIELDS, "Network", diffs);
        double ap = compareDimension(d1.getRawFeatures(), d2.getRawFeatures(), APP_FIELDS,     "App",     diffs);
        double rk = compareRiskDimension(d1, d2, diffs);

        result.setHardwareSimilarity(hw);
        result.setSystemSimilarity(sy);
        result.setNetworkSimilarity(nw);
        result.setAppSimilarity(ap);

        double overall = hw * HARDWARE_WEIGHT + sy * SYSTEM_WEIGHT
                       + nw * NETWORK_WEIGHT + ap * APP_WEIGHT + rk * RISK_WEIGHT;
        result.setOverallSimilarity(overall);

        result.setFieldDiffs(diffs);

        if (d1.getFingerprint() != null && d2.getFingerprint() != null) {
            result.setHammingDistance(hammingDistance(d1.getFingerprint(), d2.getFingerprint()));
        }

        determineResult(result, overall);
        return result;
    }

    /**
     * Compare features in one dimension, append FieldDiffs, return dimension similarity.
     */
    private double compareDimension(Map<String, String> f1, Map<String, String> f2,
                                    String[][] fields, String dimension, List<FieldDiff> diffs) {
        int match = 0, count = 0;
        for (String[] entry : fields) {
            String key = entry[0];
            String name = entry[1];
            String v1 = f1 != null ? f1.get(key) : null;
            String v2 = f2 != null ? f2.get(key) : null;
            String sv1 = safe(v1);
            String sv2 = safe(v2);
            boolean bothEmpty = sv1.isEmpty() && sv2.isEmpty();
            boolean matched = bothEmpty || (!sv1.isEmpty() && !sv2.isEmpty() && sv1.equalsIgnoreCase(sv2));
            if (!sv1.isEmpty() || !sv2.isEmpty()) {
                count++;
                if (matched) match++;
            }
            diffs.add(new FieldDiff(name, sv1, sv2, matched, dimension));
        }
        return count > 0 ? (match * 100.0 / count) : 0.0;
    }

    private double compareRiskDimension(DeviceInfo d1, DeviceInfo d2, List<FieldDiff> diffs) {
        boolean[] r1 = { d1.isRooted(), d1.isEmulator(), d1.isHasXposed(),
                         d1.isHasFrida(), d1.isHasProxy(), d1.isHasVpn(), d1.isMockLocationEnabled() };
        boolean[] r2 = { d2.isRooted(), d2.isEmulator(), d2.isHasXposed(),
                         d2.isHasFrida(), d2.isHasProxy(), d2.isHasVpn(), d2.isMockLocationEnabled() };
        int match = 0;
        for (int i = 0; i < RISK_FIELDS.length; i++) {
            boolean m = r1[i] == r2[i];
            if (m) match++;
            diffs.add(new FieldDiff(RISK_FIELDS[i][1],
                    String.valueOf(r1[i]), String.valueOf(r2[i]), m, "Risk"));
        }
        return RISK_FIELDS.length > 0 ? (match * 100.0 / RISK_FIELDS.length) : 0.0;
    }

    private String safe(String v) {
        return (v != null && !v.isEmpty()) ? v : "";
    }

    private void determineResult(ComparisonResult r, double s) {
        if (s >= 90)       { r.setResult("SAME_DEVICE");  r.setAction("ALLOW");         r.setRiskLevel("LOW");      }
        else if (s >= 75)  { r.setResult("LIKELY_SAME");  r.setAction("BEHAVIOR_CHECK");r.setRiskLevel("LOW");      }
        else if (s >= 60)  { r.setResult("UNCERTAIN");    r.setAction("MFA_REQUIRED");  r.setRiskLevel("MEDIUM");   }
        else if (s >= 40)  { r.setResult("NEW_DEVICE");   r.setAction("MFA_MONITOR");   r.setRiskLevel("HIGH");     }
        else               { r.setResult("HIGH_RISK");    r.setAction("BLOCK_REVIEW");  r.setRiskLevel("CRITICAL"); }
    }

    private String hexToBinary(String hex, int length) {
        StringBuilder bin = new StringBuilder();
        for (char c : hex.toCharArray()) {
            String b = Integer.toBinaryString(Integer.parseInt(String.valueOf(c), 16));
            while (b.length() < 4) b = "0" + b;
            bin.append(b);
        }
        return bin.length() > length ? bin.substring(0, length) : bin.toString();
    }
}
