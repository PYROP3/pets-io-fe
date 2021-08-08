package com.mslinksya.pets.io.ui.events;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import com.mslinksya.pets.io.utils.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.controller.ServerController;
import com.mslinksya.pets.io.data.LoginRepository;
import com.mslinksya.pets.io.data.model.Event;
import com.mslinksya.pets.io.data.model.Pet;

import java.util.ArrayList;

public class EventFocusActivity extends AppCompatActivity {
    private static final String TAG = EventFocusActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_focus);

        Event event = getIntent().getParcelableExtra("EVENT");

        TextView deviceID = findViewById(R.id.textView_eventFocus_deviceID);
        deviceID.setText(event.getDevice());

        new Thread(() -> {
            Spinner petSpinner = findViewById(R.id.spinner_eventFocus_pet);
            ArrayList<String> petNames = new ArrayList<>();
            petNames.add("?");
            for (Pet pet : LoginRepository.getInstance().getUser().requestPets(this)) {
                petNames.add(pet.getName());
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, petNames);
            runOnUiThread(() -> petSpinner.setAdapter(arrayAdapter));
            arrayAdapter.setNotifyOnChange(true);
            petSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String petName = (String) petSpinner.getAdapter().getItem(position);
                    for (Pet pet : LoginRepository.getInstance().getUser().getPets()) {
                        if (pet.getName().equals(petName) && !petName.equals(event.getDetectedPet())) {
                            Log.d(TAG, "Updating event " + event.getID() + " for pet " + pet.getID() + " (" + pet.getName() + ")");
                            new Thread(() -> new ServerController(EventFocusActivity.this).requestEditEvent(
                                    LoginRepository.getInstance().getUser().getAuthToken(),
                                    event.getID(),
                                    pet.getID()
                            )).start();
                            event.setDetectedPet(pet.getID());
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });

            if (event.getDetectedPet() == null) {
                runOnUiThread(() -> petSpinner.setSelection(0));
            } else {
                String detectedPet = event.getDetectedPet();
                int i = 1;
                boolean found = false;
                for (Pet pet : LoginRepository.getInstance().getUser().getPets()) {
                    if (detectedPet.equals(pet.getID())) {
                        int finalI = i;
                        runOnUiThread(() -> petSpinner.setSelection(finalI));
                        found = true;
                        break;
                    }
                    i++;
                }
                if (!found) {
                    runOnUiThread(() -> petSpinner.setSelection(0));
                }
            }
        }).start();

        TextView datetime = findViewById(R.id.textView_eventFocus_datetime);
        datetime.setText(event.getTimestamp().toString());

        TextView eventID = findViewById(R.id.textView_eventFocus_eventID);
        eventID.setText("ID: " + event.getID());

        if (event.getExtra() != null) {
            TextView extras = findViewById(R.id.textView_eventFocus_extra);
            extras.setText(event.getExtra());
            extras.setVisibility(View.VISIBLE);
            findViewById(R.id.textView_eventFocus_extraTitle).setVisibility(View.VISIBLE);
        }

        CalendarView calendarView = findViewById(R.id.calendarView_eventFocus);
        calendarView.setDate(event.getTimestamp().getTime());
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) ->
                view.setDate(event.getTimestamp().getTime()));

        ImageView imageView = findViewById(R.id.imageView_eventFocus);
        imageView.setImageBitmap(event.getPicture());

        Button deleteButton = findViewById(R.id.button_eventFocus_delete);
        deleteButton.setOnClickListener((v) -> new AlertDialog.Builder(EventFocusActivity.this)
                .setMessage("Deseja mesmo excluir este evento?\nEsta ação não pode ser desfeita")
                .setPositiveButton("Sim", (dialog, which) -> new Thread(() -> {
                    Log.d(TAG, "Deleting event");
                    if (new ServerController(EventFocusActivity.this).requestDeleteEvent(
                            LoginRepository.getInstance().getUser().getAuthToken(),
                            event.getID()
                    )) {
                        EventsActivity.deletedEvent(getIntent().getIntExtra("EVENT_IDX", -1));
                        setResult(1);
                        Toast.makeText(EventFocusActivity.this, "Evento deletado!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).start())
                .setNegativeButton("Não", (dialog, which) -> {}).create().show());
    }
}