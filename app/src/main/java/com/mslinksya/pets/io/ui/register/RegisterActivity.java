package com.mslinksya.pets.io.ui.register;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import com.mslinksya.pets.io.utils.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.controller.RegistrationController;
import com.mslinksya.pets.io.data.model.RegistrationModel;

import java.util.concurrent.CompletableFuture;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private static final int QR_SIZE = 700;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void onClickRegister(View v) {
        findViewById(R.id.imageView_register_qrcode).setVisibility(View.GONE);
        findViewById(R.id.textView_register_help).setVisibility(View.GONE);

        findViewById(R.id.button_register_action).setActivated(false);

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

                findViewById(R.id.button_register_action).setActivated(true);
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Não foi possível realizar a solicitação", Toast.LENGTH_LONG).show());
                return;
            }

            QRCodeWriter writer = new QRCodeWriter();

            try {
                BitMatrix bm = writer.encode(registrationModel.getFormattedData(),
                        BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
                Bitmap bmp = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.RGB_565);
                for (int x = 0; x < QR_SIZE; x++){
                    for (int y = 0; y < QR_SIZE; y++){
                        bmp.setPixel(x, y, bm.get(x,y) ? Color.BLACK : Color.WHITE);
                    }
                }

                runOnUiThread(() -> {
                    ((ImageView) findViewById(R.id.imageView_register_qrcode)).setImageBitmap(bmp);

                    findViewById(R.id.imageView_register_qrcode).setVisibility(View.VISIBLE);
                    findViewById(R.id.textView_register_help).setVisibility(View.VISIBLE);
                });

            } catch (WriterException e) {
                e.printStackTrace();
            }
        }).start();
    }
}