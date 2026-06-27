package com.device.fingerprint.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.device.fingerprint.R;
import com.device.fingerprint.model.DeviceInfo;
import com.device.fingerprint.utils.SettingsManager;
import com.device.fingerprint.ui.adapter.HistoryAdapter;
import com.device.fingerprint.utils.StorageManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Activity showing device collection history
 */
public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.OnItemClickListener {
    
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private StorageManager storageManager;
    private View emptyView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Collection History");
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.inflateMenu(R.menu.menu_history);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear) {
                clearAllHistory();
                return true;
            }
            return false;
        });
        
        storageManager = new StorageManager(this);
        
        recyclerView = findViewById(R.id.recycler_history);
        emptyView = findViewById(R.id.empty_view);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        loadHistory();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }
    
    private void loadHistory() {
        List<DeviceInfo> history = storageManager.getDeviceHistory();
        
        if (history.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            
            adapter = new HistoryAdapter(history, this);
            recyclerView.setAdapter(adapter);
        }
    }
    
    @Override
    public void onItemClick(DeviceInfo device) {
        Intent intent = new Intent(this, DeviceDetailActivity.class);
        intent.putExtra("device_info", device);
        startActivity(intent);
    }
    
    @Override
    public void onItemLongClick(DeviceInfo device) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Entry")
                .setMessage("Remove this device from history?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    storageManager.deleteFromHistory(device.getFingerprint());
                    loadHistory();
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void clearAllHistory() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Clear All History")
                .setMessage("Are you sure you want to delete all history?")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    storageManager.clearHistory();
                    loadHistory();
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
