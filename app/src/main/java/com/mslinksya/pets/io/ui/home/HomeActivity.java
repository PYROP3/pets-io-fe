package com.mslinksya.pets.io.ui.home;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.mslinksya.pets.io.ui.devices.DeviceStatusActivity;
import com.mslinksya.pets.io.ui.events.EventsActivity;
import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.data.LoginRepository;
import com.mslinksya.pets.io.data.model.LoggedInUser;
import com.mslinksya.pets.io.fcm.MessagingService;
import com.mslinksya.pets.io.ui.pendingpets.PendingPetActivity;
import com.mslinksya.pets.io.ui.profile.ProfileActivity;
import com.mslinksya.pets.io.ui.register.RegisterActivity;
import com.mslinksya.pets.io.ui.settings.SettingsActivity;
import com.mslinksya.pets.io.ui.settings.SettingsProvider;
import com.mslinksya.pets.io.ui.stream.StreamActivity;
import com.mslinksya.pets.io.utils.Constants;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mslinksya.pets.io.utils.Log;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        Button profileButton = findViewById(R.id.buttonHomeProfile);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        Button registerButton = findViewById(R.id.buttonHomeRegisterDevice);
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        Button eventsButton = findViewById(R.id.buttonHomeGetData);
        eventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EventsActivity.class);
            startActivity(intent);
        });

        Button streamButton = findViewById(R.id.buttonHomeLivestream);
        streamButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StreamActivity.class);
            startActivity(intent);
        });

        Button settingsButton = findViewById(R.id.buttonHomeSettings);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        Button deviceStatusButton = findViewById(R.id.buttonHomeDeviceStatus);
        deviceStatusButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DeviceStatusActivity.class);
            startActivity(intent);
        });

        MessagingService.createNotificationChannel(this);
        MessagingService.clearNotifications(this);

        LoginRepository.getInstance().loadSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoggedInUser user = LoginRepository.getInstance().getUser();
        if (user != null) {
            int nPendingPets = user.getNumberOfPendingPets();
            Log.d(TAG, "User has " + nPendingPets + " pending pets");
            if (nPendingPets > 0) {
                Intent intent = new Intent(this, PendingPetActivity.class);
                intent.putExtra(Constants.USER_PENDING_PETS_KEY, nPendingPets);
                startActivity(intent);
            }
        }
    }
}