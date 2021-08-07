package com.mslinksya.pets.io.controller;

import android.content.Context;
import com.mslinksya.pets.io.utils.Log;

import com.mslinksya.pets.io.data.LoginRepository;
import com.mslinksya.pets.io.data.model.RegistrationModel;

public class RegistrationController {
    private static final String TAG = RegistrationController.class.getSimpleName();

    public static RegistrationModel requestRegistration(Context context, String wifi_ssid, String wifi_pass) {
        String register_id = new ServerController(context).registerDevice(
                LoginRepository.getInstance().getUser().getAuthToken());

        if (register_id == null) {
            Log.w(TAG, "Failed to request registration");
            return null;
        }

        return new RegistrationModel(wifi_ssid, wifi_pass, register_id);
    }
}
