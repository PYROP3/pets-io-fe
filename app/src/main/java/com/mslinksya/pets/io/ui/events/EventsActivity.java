package com.mslinksya.pets.io.ui.events;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.mslinksya.pets.io.data.EventRepository;
import com.mslinksya.pets.io.ui.util.ClockView;
import com.mslinksya.pets.io.utils.Log;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.controller.ServerController;
import com.mslinksya.pets.io.data.LoginRepository;
import com.mslinksya.pets.io.data.model.Event;
import com.mslinksya.pets.io.ui.home.HomeActivity;
import com.mslinksya.pets.io.ui.register.RegisterActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventsActivity extends AppCompatActivity {
    private static final String TAG = EventsActivity.class.getSimpleName();

//    private static volatile int eventIndex = -1;
//
//    public static void deletedEvent(int eventIndex) {
//        EventsActivity.eventIndex = eventIndex;
//    }

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
        updateEventList();
    }

    private void requestUpdateEventList(String deviceID) {
        new Thread(() -> {
            EventRepository.updateEvents(new ServerController(this)
                    .requestEventList(LoginRepository.getInstance().getUser().getAuthToken(),
                            deviceID));

            updateEventList();
        }).start();
    }

    private void updateEventList() {
        LinearLayout eventListLayout = findViewById(R.id.linearlayout_events_list);

        runOnUiThread(eventListLayout::removeAllViews);

        for (Event event : EventRepository.getEvents()) {
            LinearLayout eventLayout = new LinearLayout(this);
            eventLayout.setOrientation(LinearLayout.HORIZONTAL);
            eventLayout.setPadding(20, 20, 20, 20);

            if (event.getPicture() == null) {
                Bitmap eventPicture = new ServerController(this)
                        .requestEventPicture(LoginRepository.getInstance().getUser().getAuthToken(), event.getID());
                event.setPicture(eventPicture);
            }

            ImageView eventImageView = new ImageView(this);
            eventImageView.setImageBitmap(event.getPicture());

            TextView eventTimestamp = new TextView(this);
            Calendar c = event.getTimestamp();
            eventTimestamp.setText(String.format("%02d/%02d/%d\n%s",
                    c.get(Calendar.DAY_OF_MONTH),
                    c.get(Calendar.MONTH) + 1,
                    c.get(Calendar.YEAR),
                    getWeekday(c.get(Calendar.DAY_OF_WEEK))));
            eventTimestamp.setHeight(event.getPicture().getHeight());
            eventTimestamp.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            eventTimestamp.setGravity(Gravity.CENTER);
            eventTimestamp.setTextSize(22);

            ClockView clockView = new ClockView(this, c);
            clockView.setMinimumWidth(200);
            clockView.setMinimumHeight(event.getPicture().getHeight());
            // event.getTimestamp().toString()

            eventLayout.addView(eventImageView);
            eventLayout.addView(createSpace(50));
            eventLayout.addView(eventTimestamp);
            eventLayout.addView(createSpace(50));
            eventLayout.addView(clockView);

            eventLayout.setOnClickListener(v -> {
                Intent intent = new Intent(EventsActivity.this, EventFocusActivity.class);
                intent.putExtra("EVENT", event.getID());
                startActivity(intent);
            });

            runOnUiThread(() -> {
                eventListLayout.addView(eventLayout);
                eventListLayout.addView(createSpace(10));
                Log.d(TAG, "eventListLayout now has " + eventListLayout.getChildCount() + " children");
            });
        }
    }

    private String getWeekday(int weekday) {
        return new String[]{"DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SAB"}[weekday - 1];
    }

    private Space createSpace(int size) {
        Space sp = new Space(this);
        sp.setMinimumHeight(size);
        sp.setMinimumWidth(size);
        return sp;
    }
}