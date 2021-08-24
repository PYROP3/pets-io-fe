package com.mslinksya.pets.io.ui.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.utils.Log;

import static com.mslinksya.pets.io.utils.Constants.SETTING_ENV;
import static com.mslinksya.pets.io.utils.Constants.SETTING_REGISTER;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        updateSettings();

        SettingsProvider settingsProvider = SettingsProvider.getInstance();
        Switch envSwitch = findViewById(R.id.switch_settings_env);

        envSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "envSwitch->" + isChecked);
            settingsProvider.setBooleanSetting(SETTING_ENV, isChecked);
        });
        Switch regSwitch = findViewById(R.id.switch_settings_register);

        regSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "regSwitch->" + isChecked);
            settingsProvider.setBooleanSetting(SETTING_REGISTER, isChecked);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        SettingsProvider.getInstance().writeSettings(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateSettings();
    }

    private void updateSettings() {
        SettingsProvider settingsProvider = SettingsProvider.getInstance();
        settingsProvider.loadSettings(this);

        Switch envSwitch = findViewById(R.id.switch_settings_env);
        Switch regSwitch = findViewById(R.id.switch_settings_register);

        envSwitch.setChecked(settingsProvider.getBooleanSetting(SETTING_ENV));
        regSwitch.setChecked(settingsProvider.getBooleanSetting(SETTING_REGISTER));
    }
}