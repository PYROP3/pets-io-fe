package com.mslinksya.pets.io.ui.events;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import com.mslinksya.pets.io.utils.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.controller.ServerController;
import com.mslinksya.pets.io.data.LoginRepository;
import com.mslinksya.pets.io.data.model.Event;
import com.mslinksya.pets.io.ui.home.HomeActivity;
import com.mslinksya.pets.io.ui.register.RegisterActivity;

import java.util.List;

public class EventsActivity extends AppCompatActivity {
    private static final String TAG = EventsActivity.class.getSimpleName();

    private static volatile int eventIndex = -1;

    public static void deletedEvent(int eventIndex) {
        EventsActivity.eventIndex = eventIndex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Spinner devicesSpinner = findViewById(R.id.spinner_events_devices);

        new Thread(() -> {
            List<String> devicesList = new ServerController(this)
                    .requestDeviceList(LoginRepository.getInstance().getUser().getAuthToken());

            if (devicesList.size() > 1) {
                devicesList.add(0, getString(R.string.events_choose_device));
            } else if (devicesList.size() == 0) {
                Log.w(TAG, "no devices retrieved");
                devicesList.add(getString(R.string.events_no_devices));
            }

            runOnUiThread(() -> {
                ArrayAdapter arrayAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, devicesList);
                devicesSpinner.setAdapter(arrayAdapter);
                arrayAdapter.setNotifyOnChange(true);
                devicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String device = (String)devicesSpinner.getAdapter().getItem(position);
                        if (!device.equals(getString(R.string.events_no_devices)) &&
                                !device.equals(getString(R.string.events_choose_device))) {
                            requestUpdateEventList(device);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Do nothing
                    }
                });
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (eventIndex != -1) {
            LinearLayout eventListLayout = findViewById(R.id.linearlayout_events_list);
            eventListLayout.removeViewAt(eventIndex);
            eventIndex = -1;
        }
    }

    private void requestUpdateEventList(String deviceID) {
        new Thread(() -> {
            LinearLayout eventListLayout = findViewById(R.id.linearlayout_events_list);

            runOnUiThread(eventListLayout::removeAllViews);

            List<Event> eventList = new ServerController(this)
                    .requestEventList(LoginRepository.getInstance().getUser().getAuthToken(), deviceID);

            final int[] i = {0};
            for (Event event : eventList) {
                LinearLayout eventLayout = new LinearLayout(this);
                eventLayout.setOrientation(LinearLayout.HORIZONTAL);

                if (event.getPicture() == null) {
                    Bitmap eventPicture = new ServerController(this)
                            .requestEventPicture(LoginRepository.getInstance().getUser().getAuthToken(), event.getID());
                    event.setPicture(eventPicture);
                }

                ImageView eventImageView = new ImageView(this);
                eventImageView.setImageBitmap(event.getPicture());

                TextView eventTimestamp = new TextView(this);
                eventTimestamp.setText(event.getTimestamp().toString());

                eventLayout.addView(eventImageView);
                eventLayout.addView(eventTimestamp);

                eventLayout.setOnClickListener(v -> {
                    Intent intent = new Intent(EventsActivity.this, EventFocusActivity.class);
                    intent.putExtra("EVENT", event);
                    intent.putExtra("EVENT_IDX", i[0]++);
                    startActivity(intent);
                });

                runOnUiThread(() -> {
                    eventListLayout.addView(eventLayout);
                    Log.d(TAG, "eventListLayout now has " + eventListLayout.getChildCount() + " children");
                });
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            if (eventIndex != -1) {
                LinearLayout eventListLayout = findViewById(R.id.linearlayout_events_list);
                runOnUiThread(() -> eventListLayout.removeViewAt(eventIndex));
                eventIndex = -1;
            }
        }
    }
}