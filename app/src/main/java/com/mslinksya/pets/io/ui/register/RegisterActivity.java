package com.mslinksya.pets.io.ui.register;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.mslinksya.pets.io.utils.BLEComm;
import com.mslinksya.pets.io.utils.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.controller.RegistrationController;
import com.mslinksya.pets.io.data.model.RegistrationModel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;

    private CompletableFuture<Boolean> userInteractionLock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    private void enableRegister() {
        runOnUiThread(() -> {
            findViewById(R.id.button_register_action).setEnabled(true);
            findViewById(R.id.progressBar_register).setVisibility(View.GONE);
        });
    }

    public void onClickRegister(View v) {
        findViewById(R.id.button_register_action).setEnabled(false);
        findViewById(R.id.progressBar_register).setVisibility(View.VISIBLE);

        String wifi_ssid = ((EditText)findViewById(R.id.editText_register_wifi_ssid))
                .getText().toString();
        String wifi_pass = ((EditText)findViewById(R.id.editText_register_wifi_pass))
                .getText().toString();

        new Thread(() -> {
            RegistrationModel registrationModel = RegistrationController.requestRegistration(
                    RegisterActivity.this,
                    wifi_ssid,
                    wifi_pass
            );

            if (registrationModel == null) {
                Log.w(TAG, "Could not request registration");

                enableRegister();
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Não foi possível realizar a solicitação", Toast.LENGTH_LONG).show());
                return;
            }

            BLEComm comm = new BLEComm(this);

            if (!comm.isPermissionGranted()) {
                userInteractionLock = new CompletableFuture<>();
                askPermission();
                try {
                    if (!userInteractionLock.get()) {
                        Log.w(TAG, "Failed waiting for permission");
                        enableRegister();
                        return;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    Log.w(TAG, "Failed waiting for permission");
                    enableRegister();
                    return;
                }
            }

            if (!comm.isBluetoothEnabled()) {
                userInteractionLock = new CompletableFuture<>();
                promptEnableBluetooth();
                try {
                    if (!userInteractionLock.get()) {
                        Log.w(TAG, "Failed waiting for bluetooth");
                        enableRegister();
                        return;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    Log.w(TAG, "Failed waiting for bluetooth");
                    enableRegister();
                    return;
                }
            }

            if (!comm.registerDevice(registrationModel)) {
                Log.w(TAG, "Could not communicate with device");

                enableRegister();
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this,
                        "Não foi possível comunicar com o seu dispositivo",
                        Toast.LENGTH_LONG).show());
            } else {
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this,
                        "Seu dispositivo está sendo registrado com o nosso servidor",
                        Toast.LENGTH_LONG).show());
                finish();
            }
        }).start();
    }

    private void askPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Precisamos da sua permissão")
                    .setMessage("Precisamos da permissão de localização para poder conversar com " +
                            "o seu dispositivo Pets.IO")
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> reqPermission())
                    .setNegativeButton(android.R.string.cancel, (dialog, which) ->
                            userInteractionLock.complete(false))
                    .create().show();
        } else {
            reqPermission();
        }
    }

    private void reqPermission() {
        String[] permissions = new String[1];
        permissions[0] = Manifest.permission.ACCESS_FINE_LOCATION;
        requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void promptEnableBluetooth() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, ENABLE_BLUETOOTH_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            userInteractionLock.complete(grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BLUETOOTH_REQUEST_CODE) {
            userInteractionLock.complete(resultCode == Activity.RESULT_OK);
        }
    }
}