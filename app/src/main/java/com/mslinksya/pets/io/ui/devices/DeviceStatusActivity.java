package com.mslinksya.pets.io.ui.devices;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.utils.BLEComm;
import com.mslinksya.pets.io.utils.BLEGATTCallback;
import com.mslinksya.pets.io.utils.Constants;
import com.mslinksya.pets.io.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.mslinksya.pets.io.utils.Constants.STATUS_BLE_CONNECTING;
import static com.mslinksya.pets.io.utils.Constants.STATUS_BLE_ERROR;
import static com.mslinksya.pets.io.utils.Constants.STATUS_BLE_NOT_INITIALIZED;
import static com.mslinksya.pets.io.utils.Constants.STATUS_BLE_REGISTERING;
import static com.mslinksya.pets.io.utils.Constants.STATUS_BLE_SKIPPED;
import static com.mslinksya.pets.io.utils.Constants.STATUS_BLE_SUCCESS;
import static com.mslinksya.pets.io.utils.Constants.STATUS_BLE_WAITING;

public class DeviceStatusActivity extends AppCompatActivity {

    private static final String TAG = DeviceStatusActivity.class.getSimpleName();

    private HashMap<String, BluetoothDevice> deviceHashMap;
    private BLEComm bleComm;
    private String lastDevice;

    private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;

    private CompletableFuture<Boolean> userInteractionLock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_status);
        bleComm = new BLEComm(this);
        hideStatus();
        new Thread(this::requestUpdateDeviceList).start();

        Button updateButton = findViewById(R.id.buttonDeviceStatusUpdate);
        updateButton.setOnClickListener((v) -> new Thread(this::requestUpdateDeviceList).start());

        Button fetchButton = findViewById(R.id.buttonDeviceStatusFetch);
        fetchButton.setOnClickListener((v) -> {
            forceUpdateStatusIndicators();
            runOnUiThread(() -> Toast.makeText(DeviceStatusActivity.this,
                    "Dados atualizados!", Toast.LENGTH_SHORT).show());
        });

        forceUpdateStatusIndicators();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bleComm.disconnectDevice();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(this::requestUpdateDeviceList).start();
    }

    private void hideStatus() {
        runOnUiThread(() -> findViewById(R.id.statusLayout).setVisibility(View.GONE));
    }

    private void showStatus() {
        runOnUiThread(() -> findViewById(R.id.statusLayout).setVisibility(View.VISIBLE));
    }

    private void hideLoading() {
        runOnUiThread(() -> {
            findViewById(R.id.progressBarDeviceStatusLoading).setVisibility(View.GONE);
            findViewById(R.id.buttonDeviceStatusUpdate).setEnabled(true);
        });
    }

    private void showLoading() {
        runOnUiThread(() -> {
            findViewById(R.id.progressBarDeviceStatusLoading).setVisibility(View.VISIBLE);
            findViewById(R.id.buttonDeviceStatusUpdate).setEnabled(false);
        });
    }

    private void requestUpdateDeviceList() {
        showLoading();
        if (!bleComm.isPermissionGranted()) {
            userInteractionLock = new CompletableFuture<>();
            askPermission();
            try {
                if (!userInteractionLock.get()) {
                    Log.w(TAG, "Failed waiting for permission");
                    return;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Log.w(TAG, "Failed waiting for permission");
                return;
            }
        }

        if (!bleComm.isBluetoothEnabled()) {
            userInteractionLock = new CompletableFuture<>();
            promptEnableBluetooth();
            try {
                if (!userInteractionLock.get()) {
                    Log.w(TAG, "Failed waiting for bluetooth");
                    return;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Log.w(TAG, "Failed waiting for bluetooth");
                return;
            }
        }
        updateDeviceList(bleComm.getPetsIODevicesInRange());
    }

    private void updateDeviceList(HashMap<String, BluetoothDevice> deviceHashMap) {
        Spinner devicesSpinner = findViewById(R.id.spinnerDeviceStatusDevices);
        List<String> devicesList = new ArrayList<>(deviceHashMap.keySet());
        this.deviceHashMap = deviceHashMap;

        new Thread(() -> {
            if (devicesList.size() > 1) {
                devicesList.add(0, getString(R.string.events_choose_device));
            } else if (devicesList.size() == 0) {
                Log.w(TAG, "no devices retrieved");
                devicesList.add(getString(R.string.events_no_devices_found));
            }
            hideLoading();

            runOnUiThread(() -> {
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, devicesList);
                devicesSpinner.setAdapter(arrayAdapter);
                arrayAdapter.setNotifyOnChange(true);
                devicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String device = (String)devicesSpinner.getAdapter().getItem(position);
                        if (!device.equals(getString(R.string.events_no_devices_found)) &&
                                !device.equals(getString(R.string.events_choose_device)) &&
                                !device.equals(lastDevice)) {
                            new Thread(() -> updateSelectedDevice(device)).start();
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

    private void updateSelectedDevice(String deviceID) {
        showLoading();
        lastDevice = deviceID;
        bleComm.disconnectDevice();
        bleComm.connectGATT(deviceHashMap.get(deviceID), new BLEGATTCallback(new BluetoothGattCallback() {
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);

                Log.d(TAG, "onCharacteristicChanged : " + characteristic.toString());

                switch (characteristic.getUuid().toString()) {
                    case Constants.BLE_UUID_CHAR_CAM:
                        updateStatusIndicator(
                                findViewById(R.id.radioButton_device_status_camera),
                                characteristic.getStringValue(0));
                        break;
                    case Constants.BLE_UUID_CHAR_CON:
                        updateStatusIndicator(
                                findViewById(R.id.radioButton_device_status_connection),
                                characteristic.getStringValue(0));
                        break;
                    case Constants.BLE_UUID_CHAR_REG:
                        updateStatusIndicator(
                                findViewById(R.id.radioButton_device_status_register),
                                characteristic.getStringValue(0));
                        break;
                    default:
                        Log.d(TAG, "Ignoring updated characteristic with UUID=" + characteristic.getUuid().toString());
                }
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    hideStatus();
                }
            }
        }));
        forceUpdateStatusIndicators();
        hideLoading();
        showStatus();
    }

    private void forceUpdateStatusIndicators() {
        if (bleComm.isConnected()) {
            showLoading();
            updateStatusIndicator(
                    findViewById(R.id.radioButton_device_status_camera),
                    bleComm.getReadableCharacteristic(Constants.BLE_UUID_CHAR_CAM));
            updateStatusIndicator(
                    findViewById(R.id.radioButton_device_status_connection),
                    bleComm.getReadableCharacteristic(Constants.BLE_UUID_CHAR_CON));
            updateStatusIndicator(
                    findViewById(R.id.radioButton_device_status_register),
                    bleComm.getReadableCharacteristic(Constants.BLE_UUID_CHAR_REG));
            hideLoading();
        }
    }

    private void updateStatusIndicator(RadioButton statusIndicator, String status) {
        Log.d(TAG, "updateStatusIndicator : " + status);
        runOnUiThread(() -> {
            statusIndicator.setButtonTintList(ColorStateList.valueOf(getColorForStatus(status)));
            statusIndicator.setText(status);
        });
    }

    private int getColorForStatus(String status) {
        if (status == null) {
            Log.w(TAG, "status is null");
            return getResources().getColor(R.color.status_grey, null);
        }
        switch (status) {
            case STATUS_BLE_NOT_INITIALIZED:
                return getResources().getColor(R.color.status_grey, null);
            case STATUS_BLE_SUCCESS:
            case STATUS_BLE_SKIPPED:
                return getResources().getColor(R.color.status_green, null);
            case STATUS_BLE_ERROR:
                return getResources().getColor(R.color.status_red, null);
            case STATUS_BLE_CONNECTING:
            case STATUS_BLE_REGISTERING:
                return getResources().getColor(R.color.status_orange, null);
            case STATUS_BLE_WAITING:
                return getResources().getColor(R.color.status_yellow, null);
            default:
                Log.w(TAG, "Unknown status " + status);
                return getResources().getColor(R.color.status_grey, null);
        }
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
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && permissions.length > 0 &&
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