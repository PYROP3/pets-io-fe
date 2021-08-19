package com.mslinksya.pets.io.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;

import java.util.concurrent.CompletableFuture;

public class BLEGATTCallback extends BluetoothGattCallback {
    private static final String TAG = BLEGATTCallback.class.getSimpleName();

    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothGattCallback mExtraCallback = null;
    private CompletableFuture<String> pendingCharacteristic = null;

    public BLEGATTCallback() {}

    public BLEGATTCallback(BluetoothGattCallback extraCallback) {mExtraCallback = extraCallback;}

    public boolean isConnected() {
        return mBluetoothGatt != null;
    }

    public BluetoothGatt getGatt() {
        return mBluetoothGatt;
    }

    public void requestCharacteristicRead(CompletableFuture<String> completableFuture) {
        pendingCharacteristic = completableFuture;
    }

    @Override
    public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        if (mExtraCallback != null) mExtraCallback.onPhyUpdate(gatt, txPhy, rxPhy, status);
    }

    @Override
    public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        super.onPhyRead(gatt, txPhy, rxPhy, status);
        if (mExtraCallback != null) mExtraCallback.onPhyRead(gatt, txPhy, rxPhy, status);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Successfully connected to " + gatt.getDevice().getAddress() + "/" + gatt.getDevice().getName());
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w(TAG, "Successfully disconnected from " + gatt.getDevice().getAddress());
                gatt.close();
                mBluetoothGatt = null;
            }
        } else {
            Log.w(TAG, "Error " + status + " encountered! Disconnecting...");
            gatt.close();
            mBluetoothGatt = null;
        }
        if (mExtraCallback != null) mExtraCallback.onConnectionStateChange(gatt, status, newState);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        Log.d(TAG, "onServicesDiscovered: " + status + "; " + gatt.getServices().toString());
        mBluetoothGatt = gatt;
        if (mExtraCallback != null) mExtraCallback.onServicesDiscovered(gatt, status);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (pendingCharacteristic != null) {
            String value = characteristic.getStringValue(0);
            Log.d(TAG, "Reporting characteristic read: " + value);
            pendingCharacteristic.complete(value);
        }
        if (mExtraCallback != null) mExtraCallback.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (mExtraCallback != null) mExtraCallback.onCharacteristicWrite(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if (mExtraCallback != null) mExtraCallback.onCharacteristicChanged(gatt, characteristic);
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        if (mExtraCallback != null) mExtraCallback.onDescriptorRead(gatt, descriptor, status);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        if (mExtraCallback != null) mExtraCallback.onDescriptorWrite(gatt, descriptor, status);
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
        if (mExtraCallback != null) mExtraCallback.onReliableWriteCompleted(gatt, status);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (mExtraCallback != null) mExtraCallback.onReadRemoteRssi(gatt, rssi, status);
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        if (mExtraCallback != null) mExtraCallback.onMtuChanged(gatt, mtu, status);
    }
}
