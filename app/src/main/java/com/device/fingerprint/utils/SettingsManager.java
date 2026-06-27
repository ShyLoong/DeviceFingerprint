package com.device.fingerprint.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import com.device.fingerprint.R;

import java.util.Locale;

/**
 * Manages app settings: language and theme color.
 * All settings are persisted via SharedPreferences.
 */
public class SettingsManager {

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_THEME_COLOR = "theme_color";

    public static final String LANG_ENGLISH = "en";
    public static final String LANG_CHINESE = "zh";

    public static final String THEME_BLUE = "blue";
    public static final String THEME_PURPLE = "purple";
    public static final String THEME_GREEN = "green";
    public static final String THEME_RED = "red";
    public static final String THEME_ORANGE = "orange";

    private static final String DEFAULT_LANGUAGE = LANG_ENGLISH;
    private static final String DEFAULT_THEME = THEME_BLUE;

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ===== Language =====

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    public void setLanguage(String lang) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply();
    }

    public boolean isChinese() {
        return LANG_CHINESE.equals(getLanguage());
    }

    /**
     * Apply language to context resources. Call in attachBaseContext() of each Activity.
     */
    public static Context applyLanguage(Context context) {
        SettingsManager sm = new SettingsManager(context);
        String lang = sm.getLanguage();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(locale));
        } else {
            config.locale = locale;
        }
        return context.createConfigurationContext(config);
    }

    // ===== Theme Color =====

    public String getThemeColor() {
        return prefs.getString(KEY_THEME_COLOR, DEFAULT_THEME);
    }

    public void setThemeColor(String color) {
        prefs.edit().putString(KEY_THEME_COLOR, color).apply();
    }

    /**
     * Get the theme resource ID for the current color setting.
     */
    public int getThemeResId() {
        switch (getThemeColor()) {
            case THEME_PURPLE:
                return R.style.Theme_Purple;
            case THEME_GREEN:
                return R.style.Theme_Green;
            case THEME_RED:
                return R.style.Theme_Red;
            case THEME_ORANGE:
                return R.style.Theme_Orange;
            case THEME_BLUE:
            default:
                return R.style.Theme_Blue;
        }
    }

    /**
     * Apply theme to activity. Must be called BEFORE setContentView().
     */
    public void applyTheme(Context context) {
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).setTheme(getThemeResId());
        }
    }

    // ===== Reset =====

    public void resetToDefaults() {
        prefs.edit()
                .putString(KEY_LANGUAGE, DEFAULT_LANGUAGE)
                .putString(KEY_THEME_COLOR, DEFAULT_THEME)
                .apply();
    }
}
