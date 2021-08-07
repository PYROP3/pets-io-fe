package com.mslinksya.pets.io.ui.util;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mslinksya.pets.io.R;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import com.mslinksya.pets.io.utils.Log;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static androidx.core.content.FileProvider.getUriForFile;

public class ImagePickerActivity extends AppCompatActivity {
    private static final String TAG = ImagePickerActivity.class.getSimpleName();
    public static final String INTENT_IMAGE_PICKER_OPTION = "image_picker_option";
    public static final String INTENT_ASPECT_RATIO_X = "aspect_ratio_x";
    public static final String INTENT_ASPECT_RATIO_Y = "aspect_ratio_Y";
    public static final String INTENT_LOCK_ASPECT_RATIO = "lock_aspect_ratio";
    public static final String INTENT_IMAGE_COMPRESSION_QUALITY = "compression_quality";
    public static final String INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT = "set_bitmap_max_width_height";
    public static final String INTENT_BITMAP_MAX_WIDTH = "max_width";
    public static final String INTENT_BITMAP_MAX_HEIGHT = "max_height";

    public static final int REQUEST_IMAGE_CAPTURE = 0;
    public static final int REQUEST_GALLERY_IMAGE = 1;

    private boolean lockAspectRatio = false, setBitmapMaxWidthHeight = false;
    private int ASPECT_RATIO_X = 16, ASPECT_RATIO_Y = 9, bitmapMaxWidth = 200, bitmapMaxHeight = 200;
    private int IMAGE_COMPRESSION = 80;
    public static String fileName;

    private CompletableFuture<Boolean> permissionGranted;
    private int requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(getApplicationContext(), "Image picker option is missing", Toast.LENGTH_LONG).show();
            return;
        }

        ASPECT_RATIO_X = intent.getIntExtra(INTENT_ASPECT_RATIO_X, ASPECT_RATIO_X);
        ASPECT_RATIO_Y = intent.getIntExtra(INTENT_ASPECT_RATIO_Y, ASPECT_RATIO_Y);
        IMAGE_COMPRESSION = intent.getIntExtra(INTENT_IMAGE_COMPRESSION_QUALITY, IMAGE_COMPRESSION);
        lockAspectRatio = intent.getBooleanExtra(INTENT_LOCK_ASPECT_RATIO, false);
        setBitmapMaxWidthHeight = intent.getBooleanExtra(INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, false);
        bitmapMaxWidth = intent.getIntExtra(INTENT_BITMAP_MAX_WIDTH, bitmapMaxWidth);
        bitmapMaxHeight = intent.getIntExtra(INTENT_BITMAP_MAX_HEIGHT, bitmapMaxHeight);

        requestCode = intent.getIntExtra(INTENT_IMAGE_PICKER_OPTION, -1);
        new Thread(() -> {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                takeCameraImage();
            } else {
                chooseImageFromGallery();
            }
        }).start();
    }

    private void takeCameraImage() {
        while (!checkPermissions()) {
            Log.w(TAG, "Failed to check permissions");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void chooseImageFromGallery() {
        while (!checkPermissions()) {
            Log.w(TAG, "Failed to check permissions");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_IMAGE);
    }

    private boolean checkPermissions() {
        return resolve(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: " + requestCode + ", " + resultCode + ", " + (data == null ? "null" : data.toString()));

        Intent resultData = new Intent();
        Bitmap bitmap = null;

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                Log.d(TAG, "REQUEST_IMAGE_CAPTURE");
                if (resultCode == RESULT_OK) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                } else {
                    setResultCancelled();
                }
                break;
            case REQUEST_GALLERY_IMAGE:
                Log.d(TAG, "REQUEST_GALLERY_IMAGE");
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d(TAG, "Looking at URI = " + uri.toString());
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    } catch (IOException e) {
                        Log.w(TAG, "Failed due to " + e.toString());
                        e.printStackTrace();
                    }
                } else {
                    setResultCancelled();
                }
                break;
            default:
                setResultCancelled();
                return;
        }
        if (bitmap != null) {
            int scaleFactor = getImageScaleFactor(bitmap);
            resultData.putExtra("data",
                    Bitmap.createScaledBitmap(
                            bitmap,
                            bitmap.getWidth() / scaleFactor,
                            bitmap.getHeight() / scaleFactor,
                            true));
            Log.d(TAG, "Updated data with parcelable : " + resultData.getExtras().get("data").getClass().getCanonicalName());
            setResultOk(resultData);
        } else {
            setResultCancelled();
        }
    }

    private int getImageScaleFactor(Bitmap b) {
        Log.d(TAG, "getImageScaleFactor : " + b.getWidth() + "x" + b.getHeight());
        int scaleFactor = (int) Math.max(b.getWidth()/(float)bitmapMaxWidth, b.getHeight()/(float)bitmapMaxHeight);
        return scaleFactor > 0 ? scaleFactor : 1;
    }

    private void setResultOk(Intent data) {
        Log.d(TAG, "setResultOk");
        setResult(Activity.RESULT_OK, data);
        finish();
    }

//    private void setResultOk(Uri imagePath) {
//        Intent intent = new Intent();
//        intent.putExtra("path", imagePath);
//        setResult(Activity.RESULT_OK, intent);
//        finish();
//    }

    private void setResultCancelled() {
        Log.d(TAG, "setResultCancelled");
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    private Uri getCacheImagePath(String fileName) {
        File path = new File(getExternalCacheDir(), "camera");
        if (!path.exists()) path.mkdirs();
        File image = new File(path, fileName);
        return getUriForFile(ImagePickerActivity.this, getPackageName() + ".provider", image);
    }

    private static String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    /**
     * Calling this will delete the images from cache directory
     * useful to clear some memory
     */
    public static void clearCache(Context context) {
        File path = new File(context.getExternalCacheDir(), "camera");
        if (path.exists() && path.isDirectory()) {
            for (File child : path.listFiles()) {
                child.delete();
            }
        }
    }

    public int resolve(String... permissionsSet) {
        for (String permission : permissionsSet) {
            Log.d(TAG, "resolving permission " + permission);
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                permissionGranted = new CompletableFuture<>();
                if (shouldShowRequestPermissionRationale(permission)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permissão")
                            .setMessage("Precisamos de algumas permissões para poder cadastrar fotos de pets")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("Entendi", null)
                            .show();
                    break;
                }
            }
        }
        permissionGranted = new CompletableFuture<>();
        Log.d(TAG, "requesting permissions");
        new Thread(() -> requestPermissions(
                permissionsSet,
                0
        )).start();
        Log.d(TAG, "waiting for result");
        try {
            boolean permissionResult = permissionGranted.get();
            if (!permissionResult) {
                return PackageManager.PERMISSION_DENIED;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return PackageManager.PERMISSION_DENIED;
        }
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult : " + requestCode + ", " + Arrays.toString(permissions) + ", " + Arrays.toString(grantResults));
        permissionGranted.complete(grantResults[0] == PackageManager.PERMISSION_GRANTED);
    }
}