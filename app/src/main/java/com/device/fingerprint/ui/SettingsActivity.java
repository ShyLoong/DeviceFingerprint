package com.device.fingerprint.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.device.fingerprint.R;
import com.device.fingerprint.utils.SettingsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Settings page for language and theme color switching.
 */
public class SettingsActivity extends AppCompatActivity {

    private SettingsManager settingsManager;
    private androidx.appcompat.widget.AppCompatTextView tvLanguageValue;
    private androidx.appcompat.widget.AppCompatTextView tvThemeValue;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(SettingsManager.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsManager = new SettingsManager(this);
        settingsManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_settings);
        toolbar.setNavigationOnClickListener(v -> finish());

        settingsManager = new SettingsManager(this);

        tvLanguageValue = findViewById(R.id.tv_language_value);
        tvThemeValue = findViewById(R.id.tv_theme_value);

        findViewById(R.id.row_language).setOnClickListener(v -> showLanguageDialog());
        findViewById(R.id.row_theme).setOnClickListener(v -> showThemeDialog());

        updateValueLabels();
    }

    private void updateValueLabels() {
        String lang = settingsManager.getLanguage();
        if (SettingsManager.LANG_CHINESE.equals(lang)) {
            tvLanguageValue.setText(R.string.language_cn);
        } else {
            tvLanguageValue.setText(R.string.language_en);
        }

        String theme = settingsManager.getThemeColor();
        switch (theme) {
            case SettingsManager.THEME_PURPLE:
                tvThemeValue.setText(R.string.purple);
                break;
            case SettingsManager.THEME_GREEN:
                tvThemeValue.setText(R.string.green);
                break;
            case SettingsManager.THEME_RED:
                tvThemeValue.setText(R.string.red);
                break;
            case SettingsManager.THEME_ORANGE:
                tvThemeValue.setText(R.string.orange);
                break;
            default:
                tvThemeValue.setText(R.string.blue);
                break;
        }
    }

    private void showLanguageDialog() {
        String[] items = {getString(R.string.language_en), getString(R.string.language_cn)};
        String[] values = {SettingsManager.LANG_ENGLISH, SettingsManager.LANG_CHINESE};
        int current = settingsManager.isChinese() ? 1 : 0;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.language)
                .setSingleChoiceItems(items, current, (dialog, which) -> {
                    String newLang = values[which];
                    if (!newLang.equals(settingsManager.getLanguage())) {
                        settingsManager.setLanguage(newLang);
                        Toast.makeText(this, R.string.restart_required, Toast.LENGTH_SHORT).show();
                        restartApp();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showThemeDialog() {
        String[] items = {
                getString(R.string.blue),
                getString(R.string.purple),
                getString(R.string.green),
                getString(R.string.red),
                getString(R.string.orange)
        };
        String[] values = {
                SettingsManager.THEME_BLUE,
                SettingsManager.THEME_PURPLE,
                SettingsManager.THEME_GREEN,
                SettingsManager.THEME_RED,
                SettingsManager.THEME_ORANGE
        };

        String currentTheme = settingsManager.getThemeColor();
        int current = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(currentTheme)) {
                current = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.theme_color)
                .setSingleChoiceItems(items, current, (dialog, which) -> {
                    String newTheme = values[which];
                    if (!newTheme.equals(settingsManager.getThemeColor())) {
                        settingsManager.setThemeColor(newTheme);
                        Toast.makeText(this, R.string.restart_required, Toast.LENGTH_SHORT).show();
                        restartApp();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
    }
}
