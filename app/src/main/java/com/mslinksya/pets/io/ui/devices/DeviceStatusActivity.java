package com.mslinksya.pets.io.ui.devices;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.utils.BLEComm;
import com.mslinksya.pets.io.utils.BLEGATTCallback;
import com.mslinksya.pets.io.utils.Constants;
import com.mslinksya.pets.io.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.mslinksya.pets.io.utils.Constants.STATUS_BLE_CONNECTING;
import static com.mslinksya.pets.io.utils.Constants.STATUS_BLE_ERROR;
import static com.mslinksya.pets.io.utils.Constants.STATUS_BLE_NOT_INITIALIZED;
import static com.mslinksya.pets.io.utils.Constants.STATUS_BLE_SUCCESS;

public class DeviceStatusActivity extends AppCompatActivity {

    private static final String TAG = DeviceStatusActivity.class.getSimpleName();

    private HashMap<String, BluetoothDevice> deviceHashMap;
    private BLEComm bleComm;
    private String lastDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_status);
        bleComm = new BLEComm(this);
        hideStatus();
        new Thread(this::requestUpdateDeviceList).start();

        Button updateButton = findViewById(R.id.buttonDeviceStatusUpdate);
        updateButton.setOnClickListener((v) -> {
            updateButton.setActivated(false);
            requestUpdateDeviceList();
            updateButton.setActivated(true);
        });

        Button fetchButton = findViewById(R.id.buttonDeviceStatusFetch);
        fetchButton.setOnClickListener((v) -> forceUpdateStatusIndicators());

        forceUpdateStatusIndicators();
    }

    private void hideStatus() {
        findViewById(R.id.statusLayout).setVisibility(View.GONE);
    }

    private void showStatus() {
        findViewById(R.id.statusLayout).setVisibility(View.VISIBLE);
    }

    private void requestUpdateDeviceList() {
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
                            updateSelectedDevice(device);
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
        }));
        showStatus();
    }

    private void forceUpdateStatusIndicators() {
        if (bleComm.isConnected()) {
            updateStatusIndicator(
                    findViewById(R.id.radioButton_device_status_camera),
                    bleComm.getReadableCharacteristic(Constants.BLE_UUID_CHAR_CAM));
            updateStatusIndicator(
                    findViewById(R.id.radioButton_device_status_connection),
                    bleComm.getReadableCharacteristic(Constants.BLE_UUID_CHAR_CON));
            updateStatusIndicator(
                    findViewById(R.id.radioButton_device_status_register),
                    bleComm.getReadableCharacteristic(Constants.BLE_UUID_CHAR_REG));
        }
    }

    private void updateStatusIndicator(RadioButton statusIndicator, String status) {
        runOnUiThread(() -> {
            statusIndicator.setHighlightColor(getColorForStatus(status));
            statusIndicator.setText(status);
        });
    }

    private int getColorForStatus(String status) {
        switch (status) {
            case STATUS_BLE_NOT_INITIALIZED:
                return getResources().getColor(R.color.status_not_initialized, null);
            case STATUS_BLE_SUCCESS:
                return getResources().getColor(R.color.status_success, null);
            case STATUS_BLE_ERROR:
                return getResources().getColor(R.color.status_error, null);
            case STATUS_BLE_CONNECTING:
                return getResources().getColor(R.color.status_connecting, null);
            default:
                Log.w(TAG, "Unknown status " + status);
                return getResources().getColor(R.color.status_not_initialized, null);
        }
    }
}