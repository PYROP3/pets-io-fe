package com.mslinksya.pets.io.ui.stream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.utils.BLEComm;
import com.mslinksya.pets.io.utils.BLEGATTCallback;
import com.mslinksya.pets.io.utils.Constants;
import com.mslinksya.pets.io.utils.Log;
import com.squareup.okhttp.HttpUrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class StreamActivity extends AppCompatActivity {

    private static final String TAG = StreamActivity.class.getSimpleName();

    private HashMap<String, BluetoothDevice> deviceHashMap;
    private BLEComm bleComm;
    private String lastDevice;

    private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;

    private CompletableFuture<Boolean> userInteractionLock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        bleComm = new BLEComm(this);

        new Thread(this::requestUpdateDeviceList).start();
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

    private void hideLoading() {
        runOnUiThread(() -> {
            findViewById(R.id.progressBarStreamLoading).setVisibility(View.GONE);
            findViewById(R.id.webview_stream_content).setVisibility(View.VISIBLE);
        });
    }

    private void showLoading() {
        runOnUiThread(() -> {
            findViewById(R.id.progressBarStreamLoading).setVisibility(View.VISIBLE);
            findViewById(R.id.webview_stream_content).setVisibility(View.GONE);
        });
    }

    private void updateDeviceList(HashMap<String, BluetoothDevice> deviceHashMap) {
        Spinner devicesSpinner = findViewById(R.id.spinner_stream_devices);
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
        if (!lastDevice.equals(deviceID)) {
            lastDevice = deviceID;
            bleComm.disconnectDevice();
            bleComm.connectGATT(deviceHashMap.get(deviceID), new BLEGATTCallback());
            String localIP = bleComm.getReadableCharacteristic(Constants.BLE_UUID_CHAR_STRM);
            HttpUrl url = new HttpUrl.Builder()
                    .scheme(Constants.SERVER_SCHEME_HTTP)
                    .host(localIP)
                    .port(Constants.SERVER_PORT_HTTP)
                    .build();
            Log.d(TAG, "reading stream at " + url.toString());

            // TODO fix webview
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
            startActivity(browserIntent);
//        WebView webView = findViewById(R.id.webview_stream_content);
//        runOnUiThread(() -> {
//            WebSettings webSettings = webView.getSettings();
//            webSettings.setUserAgentString("APP");
//            webView.setWebViewClient(new WebViewClient() {
//                @Override
//                public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                    super.onPageStarted(view, url, favicon);
//                }
//
//                @Override
//                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest url) {
//                    view.loadUrl(url.getUrl().toString());
//                    return true;
//                }
//
//                @Override
//                public void onPageFinished(WebView view, String url) {
//                    super.onPageFinished(view, url);
//                    hideLoading();
//                }
//            });
//            webView.loadUrl(url);
//        });
            hideLoading();
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