package com.device.fingerprint.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.device.fingerprint.R;
import com.device.fingerprint.model.ComparisonResult;
import com.device.fingerprint.model.DeviceInfo;
import com.device.fingerprint.similarity.SimilarityCalculator;
import com.device.fingerprint.ui.adapter.ComparisonAdapter;
import com.device.fingerprint.utils.SettingsManager;
import com.device.fingerprint.utils.StorageManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for comparing two devices.
 * Device A is selected from local history.
 * Device B can be selected from history OR imported from JSON (e.g. pasted from another phone).
 */
public class CompareActivity extends AppCompatActivity {

    private Spinner spinnerDevice1;
    private Spinner spinnerDevice2;
    private RadioGroup radioGroupDevice2Source;
    private RadioButton rbHistory;
    private RadioButton rbJson;
    private LinearLayout layoutDevice2History;
    private LinearLayout layoutDevice2Json;
    private TextInputEditText etDeviceJson;
    private MaterialButton btnPasteJson;
    private MaterialButton btnCompare;
    private MaterialCardView cardResult;
    private ProgressBar progressBar;
    private RecyclerView recyclerComparison;

    private MaterialTextView tvOverallSimilarity;
    private MaterialTextView tvResultText;
    private MaterialTextView tvActionText;
    private MaterialTextView tvHammingDistance;
    private View similarityIndicator;

    private StorageManager storageManager;
    private SimilarityCalculator calculator;
    private List<DeviceInfo> deviceList;
    private ComparisonAdapter comparisonAdapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(SettingsManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new SettingsManager(this).applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.compare_devices);
        toolbar.setNavigationOnClickListener(v -> finish());

        storageManager = new StorageManager(this);
        calculator = new SimilarityCalculator();
        deviceList = new ArrayList<>();

        initViews();
        loadDevices();
    }

    private void initViews() {
        spinnerDevice1 = findViewById(R.id.spinner_device_1);
        spinnerDevice2 = findViewById(R.id.spinner_device_2);
        radioGroupDevice2Source = findViewById(R.id.radio_group_device2_source);
        rbHistory = findViewById(R.id.rb_history);
        rbJson = findViewById(R.id.rb_json);
        layoutDevice2History = findViewById(R.id.layout_device2_history);
        layoutDevice2Json = findViewById(R.id.layout_device2_json);
        etDeviceJson = findViewById(R.id.et_device_json);
        btnPasteJson = findViewById(R.id.btn_paste_json);
        btnCompare = findViewById(R.id.btn_compare);
        cardResult = findViewById(R.id.card_result);
        progressBar = findViewById(R.id.progress_bar);
        recyclerComparison = findViewById(R.id.recycler_comparison);

        tvOverallSimilarity = findViewById(R.id.tv_overall_similarity);
        tvResultText = findViewById(R.id.tv_result_text);
        tvActionText = findViewById(R.id.tv_action_text);
        tvHammingDistance = findViewById(R.id.tv_hamming_distance);
        similarityIndicator = findViewById(R.id.similarity_indicator);

        recyclerComparison.setLayoutManager(new LinearLayoutManager(this));

        // Radio group toggle between history and JSON for Device B
        radioGroupDevice2Source.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_history) {
                layoutDevice2History.setVisibility(View.VISIBLE);
                layoutDevice2Json.setVisibility(View.GONE);
            } else {
                layoutDevice2History.setVisibility(View.GONE);
                layoutDevice2Json.setVisibility(View.VISIBLE);
            }
        });

        // Paste JSON button
        btnPasteJson.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                android.content.ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String pasteData = item.getText() != null ? item.getText().toString() : "";
                etDeviceJson.setText(pasteData);
                Toast.makeText(this, "Pasted from clipboard", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
            }
        });

        btnCompare.setOnClickListener(v -> performComparison());
    }

    private void loadDevices() {
        deviceList = storageManager.getDeviceHistory();

        DeviceInfo currentDevice = (DeviceInfo) getIntent().getSerializableExtra("current_device");
        if (currentDevice != null) {
            boolean exists = false;
            for (DeviceInfo d : deviceList) {
                if (d.getFingerprint() != null &&
                        d.getFingerprint().equals(currentDevice.getFingerprint())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                deviceList.add(0, currentDevice);
            }
        }

        List<String> deviceNames = new ArrayList<>();
        for (DeviceInfo device : deviceList) {
            String name = device.getBrand() + " " + device.getModel();
            if (device.getCollectionTime() != null) {
                name += " (" + device.getCollectionTime().substring(0, 10) + ")";
            }
            deviceNames.add(name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, deviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerDevice1.setAdapter(adapter);
        spinnerDevice2.setAdapter(adapter);

        if (deviceList.size() > 1) {
            spinnerDevice2.setSelection(1);
        }

        // Show warning if only 1 device in history
        if (deviceList.size() < 2) {
            Toast.makeText(this,
                    "Only 1 device in history. Switch Device B to 'Import JSON' mode to compare external devices.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void performComparison() {
        int index1 = spinnerDevice1.getSelectedItemPosition();
        if (index1 < 0 || index1 >= deviceList.size()) {
            Toast.makeText(this, "Please select Device A", Toast.LENGTH_SHORT).show();
            return;
        }
        DeviceInfo device1 = deviceList.get(index1);

        DeviceInfo device2;
        if (rbHistory.isChecked()) {
            // Device B from history spinner
            int index2 = spinnerDevice2.getSelectedItemPosition();
            if (index2 < 0 || index2 >= deviceList.size()) {
                Toast.makeText(this, "Please select Device B", Toast.LENGTH_SHORT).show();
                return;
            }
            device2 = deviceList.get(index2);
        } else {
            // Device B from JSON input
            String json = etDeviceJson.getText() != null ? etDeviceJson.getText().toString().trim() : "";
            if (json.isEmpty()) {
                Toast.makeText(this, "Please paste Device B JSON", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                device2 = new Gson().fromJson(json, DeviceInfo.class);
                if (device2 == null) {
                    Toast.makeText(this, "Invalid JSON format", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (JsonSyntaxException e) {
                Toast.makeText(this, "JSON parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (device1.getFingerprint() != null && device2.getFingerprint() != null
                && device1.getFingerprint().equals(device2.getFingerprint())) {
            Toast.makeText(this, "Same device selected, similarity will be 100%", Toast.LENGTH_SHORT).show();
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCompare.setEnabled(false);

        ComparisonResult result = calculator.compareDevices(device1, device2);
        displayResult(result);

        progressBar.setVisibility(View.GONE);
        btnCompare.setEnabled(true);
    }

    private void displayResult(ComparisonResult result) {
        cardResult.setVisibility(View.VISIBLE);

        double similarity = result.getOverallSimilarity();
        tvOverallSimilarity.setText(String.format(java.util.Locale.getDefault(), "%.1f%%", similarity));

        int color = result.getSimilarityColor();
        similarityIndicator.setBackgroundColor(color);
        tvOverallSimilarity.setTextColor(color);

        tvResultText.setText(result.getResultDescription());
        tvResultText.setTextColor(color);

        tvActionText.setText(result.getActionDescription());

        if (result.getHammingDistance() >= 0) {
            tvHammingDistance.setText(String.format(java.util.Locale.getDefault(),
                    "Hamming Distance: %d", result.getHammingDistance()));
        } else {
            tvHammingDistance.setText("Hamming Distance: N/A");
        }

        // Field-level comparison detail
        comparisonAdapter = new ComparisonAdapter(result.getFieldDiffs());
        recyclerComparison.setAdapter(comparisonAdapter);
    }

    private int getSimilarityColor(double similarity) {
        if (similarity >= 90) return 0xFF4CAF50;
        if (similarity >= 75) return 0xFF8BC34A;
        if (similarity >= 60) return 0xFFFFC107;
        if (similarity >= 40) return 0xFFFF9800;
        return 0xFFF44336;
    }
}
