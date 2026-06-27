package com.device.fingerprint.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import com.device.fingerprint.R;
import com.device.fingerprint.utils.SettingsManager;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * About activity showing app information
 */
public class AboutActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("About");
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
