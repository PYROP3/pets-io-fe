package com.mslinksya.pets.io.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.mslinksya.pets.io.utils.Constants;
import com.mslinksya.pets.io.utils.Log;

import java.util.HashMap;

public class SettingsProvider {
    private static final String TAG = SettingsProvider.class.getSimpleName();

    private static SettingsProvider instance = null;

    private final HashMap<String, Boolean> booleanSettings = new HashMap<>();

//    private final HashMap<Class<?>, HashMap<String, ?>> settings = new HashMap<>();

    private SettingsProvider() {
//        settings.put(Boolean.class, booleanSettings);
    }

    public static SettingsProvider getInstance() {
        if (instance == null) {
            instance = new SettingsProvider();
        }
        return instance;
    }

//    public Object getSetting(String setting, Class<?> type) {
//        return settings.get(type).get(setting);
//    }
//
//    public <T> void setSetting(String setting, Class<T> type, T value) {
//        HashMap<String, T> map = (HashMap<String, T>) settings.get(type);
//        map.put(setting, value);
//    }

    public void setBooleanSetting(String setting, boolean value) {
        booleanSettings.put(setting, value);
    }

    public boolean getBooleanSetting(String setting) {
        return booleanSettings.getOrDefault(setting, false);
    }

    public void writeSettings(Context context) {
        Log.d(TAG, "writeSettings");
        SharedPreferences sharedPref = context.getSharedPreferences(
                Constants.SHARED_PREFERENCES_BOOLEAN_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        for (String setting : booleanSettings.keySet()) {
            editor.putBoolean(setting, booleanSettings.get(setting));
        }
        editor.apply();
    }

    public SettingsProvider loadSettings(Context context) {
        Log.d(TAG, "loadSettings");
        booleanSettings.clear();
        SharedPreferences sharedPref = context.getSharedPreferences(
                Constants.SHARED_PREFERENCES_BOOLEAN_SETTINGS, Context.MODE_PRIVATE);
        for (String setting : sharedPref.getAll().keySet()) {
            booleanSettings.put(setting, sharedPref.getBoolean(setting, false));
        }
        return this;
    }
}
