package com.mslinksya.pets.io.ui.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import com.mslinksya.pets.io.utils.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.data.LoginRepository;
import com.mslinksya.pets.io.ui.login.LoggedInUserView;
import com.mslinksya.pets.io.ui.login.LoginActivity;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Resources res = getResources();
        TextView helloTextView = findViewById(R.id.textViewProfileUser);
        TextView emailTextView = findViewById(R.id.textViewProfileEmail);
        TextView petsTextView = findViewById(R.id.textViewProfilePets);
        TextView devicesTextView = findViewById(R.id.textViewProfileDevices);

        LoggedInUserView user = LoggedInUserView.fromUser(LoginRepository.getInstance().getUser());

        helloTextView.setText(res.getString(R.string.profile_hello, user.getDisplayName()));
        emailTextView.setText(res.getString(R.string.profile_email, user.getUserId()));
        petsTextView.setText(res.getQuantityString(R.plurals.profile_pets, user.getNumberOfPets(), user.getNumberOfPets()));
        devicesTextView.setText(res.getQuantityString(R.plurals.profile_devices, user.getNumberOfDevices(), user.getNumberOfDevices()));

        Log.d(TAG, "user has " + user.getNumberOfPets() + " pets");
        Log.d(TAG, "user has " + user.getNumberOfDevices() + " devices");

        Button logoutButton = findViewById(R.id.buttonProfileLogout);
        logoutButton.setOnClickListener(v ->
                new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Sair")
                .setMessage("Você realmente deseja sair?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    LoginRepository.getInstance().logout(ProfileActivity.this);
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(android.R.string.no, null).show());
    }
}