package com.mslinksya.pets.io.ui.util;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;

import com.mslinksya.pets.io.data.model.RegistrationModel;
import com.mslinksya.pets.io.utils.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mslinksya.pets.io.utils.Constants.BLE_CONNECT_TIMEOUT_MS;
import static com.mslinksya.pets.io.utils.Constants.BLE_UUID_CHAR_PASS;
import static com.mslinksya.pets.io.utils.Constants.BLE_UUID_CHAR_SSID;
import static com.mslinksya.pets.io.utils.Constants.BLE_UUID_CHAR_TOKN;
import static com.mslinksya.pets.io.utils.Constants.BLE_UUID_SERVICE;

public class BLEComm {
    private static final String TAG = BLEComm.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;

    public BLEComm(Context context) {
        mContext = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public boolean isPermissionGranted() {
        return mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean registerDevice(RegistrationModel registrationModel) {
        if (!isBluetoothEnabled()) {
            Log.w(TAG, "BLE not enabled");
            return false;
        }

        if (!isPermissionGranted()) {
            Log.w(TAG, "Permission not granted");
            return false;
        }

        List<ScanFilter> scanFilterList = new ArrayList<>();
        scanFilterList.add(new ScanFilter.Builder().setServiceUuid(
                ParcelUuid.fromString(BLE_UUID_SERVICE)
        ).build());

        ScanSettings settings = new ScanSettings.Builder().setScanMode(
                ScanSettings.SCAN_MODE_LOW_LATENCY
        ).setCallbackType(
                ScanSettings.CALLBACK_TYPE_ALL_MATCHES
        ).build();

        CompletableFuture<BluetoothDevice> deviceCommSuccessful = new CompletableFuture<>();

        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Log.d(TAG, "onScanResult : " + callbackType + ", " + result.toString());
                if (!deviceCommSuccessful.isDone())
                    deviceCommSuccessful.complete(result.getDevice());
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(this);
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.w(TAG, "onScanFailed : " + errorCode);
                deviceCommSuccessful.complete(null);
            }
        };

        mBluetoothAdapter.getBluetoothLeScanner().startScan(
                scanFilterList, settings, scanCallback
        );

        BluetoothDevice device;
        try {
            device = deviceCommSuccessful.get(BLE_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception waiting for device : " + e.toString());
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            return false;
        }

        if (device == null) {
            Log.d(TAG, "Device was null");
            return false;
        }

        BLEGATTCallback gattCallback = new BLEGATTCallback();

        device.connectGatt(mContext, false, gattCallback);

        try {
            Thread.sleep(BLE_CONNECT_TIMEOUT_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!gattCallback.isConnected()) {
            Log.e(TAG, "gatt is not connected");
        }

        BluetoothGattService service = gattCallback.getGatt().getService(java.util.UUID.fromString(BLE_UUID_SERVICE));

        if (service == null) {
            Log.e(TAG, "Service " + BLE_UUID_SERVICE + " not found on " + device.getAddress());
            return false;
        }

        writeCharacteristic(gattCallback.getGatt(), service, BLE_UUID_CHAR_SSID, registrationModel.getSSID());
        writeCharacteristic(gattCallback.getGatt(), service, BLE_UUID_CHAR_PASS, registrationModel.getPass());
        writeCharacteristic(gattCallback.getGatt(), service, BLE_UUID_CHAR_TOKN, registrationModel.getNonce());

        return true;
    }

    private void writeCharacteristic(BluetoothGatt gatt, BluetoothGattService service, String UUID, String value) {
        Log.d(TAG, "writeCharacteristic : " + service.toString() + "; UUID=" + UUID + "; value=" + value);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(java.util.UUID.fromString(UUID));
        characteristic.setValue(value);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        boolean ret;
        int count = 5;
        do {
            ret = gatt.writeCharacteristic(characteristic);
            if (!ret) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                    //ignored
                }
                Log.w(TAG, "Failed to writeCharacteristic : " + count);
            }
        } while (!ret && count-- > 0);
        Log.d(TAG, "writeCharacteristic : " + ret);
    }
}
