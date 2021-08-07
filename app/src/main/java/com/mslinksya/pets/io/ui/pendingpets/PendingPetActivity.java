package com.mslinksya.pets.io.ui.pendingpets;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import com.mslinksya.pets.io.utils.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.controller.ServerController;
import com.mslinksya.pets.io.data.LoginRepository;
import com.mslinksya.pets.io.data.model.Pet;
import com.mslinksya.pets.io.ui.util.ImagePickerActivity;
import com.mslinksya.pets.io.utils.Constants;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PendingPetActivity extends AppCompatActivity {
    private static final String TAG = PendingPetActivity.class.getSimpleName();

    private final int REQUEST_CODE_CAMERA = 0;
    private final int REQUEST_CODE_GALLERY = 1;

    private int pendingImageId;
    private int nPets;

    private final List<ImageView> pendingImageViews = new ArrayList<>();
    private final List<Bitmap> pendingImageBitmaps = new ArrayList<>();
    private final List<EditText> petNameEditTexts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_pet);

        Intent intent = getIntent();

        nPets = intent.getIntExtra(Constants.USER_PENDING_PETS_KEY, 0);

        if (nPets < 1) {
            Log.d(TAG, "not enough pets : " + nPets);
            finish();
            return;
        }

        LinearLayout layout = findViewById(R.id.pendingPetLinearLayout);
        for (int i = 0; i < nPets; i++) {
            layout.addView(createPetLayout(i));
        }

        Button confirmButton = findViewById(R.id.buttonPendingPetsConfirm);
        confirmButton.setOnClickListener(v -> {
            // TODO uncomment after testing !!!!
            for (int i = 0; i < nPets; i++) {
                if (pendingImageBitmaps.get(i) == null) {
                    runOnUiThread(() -> Toast.makeText(PendingPetActivity.this, "Precisamos de uma foto para cada pet!", Toast.LENGTH_LONG).show());
                    return;
                }
                if (petNameEditTexts.get(i).getText().toString().isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(PendingPetActivity.this, "Faltou o nome de um pet!", Toast.LENGTH_LONG).show());
                    return;
                }
            }

            ArrayList<Pet> petList = new ArrayList<>();

            for (int i = 0; i < nPets; i++) {
                Pet pet = new Pet(null, petNameEditTexts.get(i).getText().toString());
                pet.setPicture(pendingImageBitmaps.get(i));
                petList.add(pet);
            }

            new ServerController(this).initializePets(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(() -> Toast.makeText(PendingPetActivity.this,
                            "Erro ao registrar os pets, tente novamente mais tarde!",
                            Toast.LENGTH_LONG).show());
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(() -> Toast.makeText(PendingPetActivity.this,
                            "Pets registrados com sucesso!",
                            Toast.LENGTH_LONG).show());

                    finish();

                    // TODO query server (is it really required?)
                    LoginRepository.getInstance().getUser().removePendingPets();
                }
            }, LoginRepository.getInstance().getUser().getAuthToken(), petList);
        });
    }

    private LinearLayout createPetLayout(int id) {
        LinearLayout parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);

        LinearLayout child = new LinearLayout(this);

        TextView textView = new TextView(this);
        textView.setText("Nome");
        parent.addView(textView);

        EditText editText = new EditText(this);
        parent.addView(editText);
        petNameEditTexts.add(editText);

        final ImageView preview = new ImageView(this);
        preview.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1));
        child.addView(preview);
        pendingImageViews.add(preview);
        pendingImageBitmaps.add(null);
        Button cameraButton = new Button(this);
        cameraButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1));
        cameraButton.setOnClickListener(v -> {
            pendingImageId = id;
            launchCameraIntent();
        });
        cameraButton.setText("Camera");
        Button galleryButton = new Button(this);
        galleryButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1));
        galleryButton.setOnClickListener(v -> {
            pendingImageId = id;
            launchGalleryIntent();
        });
        galleryButton.setText("Galeria");
        child.addView(cameraButton);
        child.addView(galleryButton);
        parent.addView(child);
        return parent;
    }

    private void launchCameraIntent() {
        Intent intent = new Intent(this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;
        Log.d(TAG, "onActivityResult: " + requestCode + ", " + resultCode + ", " + (data == null ? "null" : data.toString()));
        if (resultCode == Activity.RESULT_OK) {
            if (data != null && data.hasExtra("data")) {
                bitmap = (Bitmap) data.getExtras().get("data");
                pendingImageViews.get(pendingImageId).setImageBitmap(bitmap);
                pendingImageBitmaps.set(pendingImageId, bitmap);
            }
        }
        boolean ready = true;
        for (int i = 0; i < nPets; i++) {
            if (pendingImageBitmaps.get(i) == null) {
                ready = false;
                break;
            }
        }
        Button confirmButton = findViewById(R.id.buttonPendingPetsConfirm);
        confirmButton.setActivated(ready);
    }
}