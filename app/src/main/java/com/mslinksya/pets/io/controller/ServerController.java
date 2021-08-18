package com.mslinksya.pets.io.controller;

import android.content.Context;
import android.graphics.Bitmap;

import com.mslinksya.pets.io.data.model.AccountCreationRequestModel;
import com.mslinksya.pets.io.data.model.Event;
import com.mslinksya.pets.io.data.model.LoginRequestModel;
import com.mslinksya.pets.io.data.model.Pet;
import com.mslinksya.pets.io.ui.settings.SettingsProvider;
import com.mslinksya.pets.io.utils.Constants;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;

import java.util.List;

import static com.mslinksya.pets.io.utils.Constants.SERVER_HOST_CLOUD;
import static com.mslinksya.pets.io.utils.Constants.SERVER_HOST_LOCAL;
import static com.mslinksya.pets.io.utils.Constants.SERVER_PORT_CLOUD;
import static com.mslinksya.pets.io.utils.Constants.SERVER_PORT_LOCAL;
import static com.mslinksya.pets.io.utils.Constants.SETTING_ENV;

public class ServerController {
    private final HttpClient httpClient;
    private final Context mContext;

    public ServerController(Context mContext) {
        this.mContext = mContext;
        httpClient = new HttpClient();
    }

    public static HttpUrl.Builder getServerUrl() {
        boolean isLocal = SettingsProvider.getInstance().getBooleanSetting(SETTING_ENV);
        return new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(isLocal ? SERVER_HOST_LOCAL : SERVER_HOST_CLOUD)
                .port(isLocal ? SERVER_PORT_LOCAL : SERVER_PORT_CLOUD);
    }

    public boolean createAccount(Callback callback, AccountCreationRequestModel creationRequest) {
        return httpClient.sendCreateAccountRequest(
                mContext,
                callback,
                creationRequest.getUserId(),
                creationRequest.getDisplayName(),
                creationRequest.getPassword(),
                creationRequest.getNumberOfPets()
        );
    }

    public boolean login(Callback callback, LoginRequestModel loginRequest) {
        return httpClient.sendLoginRequest(
                mContext,
                callback,
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );
    }

    public boolean logout(String token) {
        return httpClient.sendEndSession(
                mContext,
                token
        );
    }

    public String registerDevice(String token) {
        return httpClient.sendRegistrationRequest(
                mContext,
                token
        );
    }

    public List<String> requestDeviceList(String token) {
        return httpClient.sendDevicesRequest(
                mContext,
                token
        );
    }

    public List<Event> requestEventList(String token, String deviceID) {
        return httpClient.sendEventsRequest(
                mContext,
                token,
                deviceID
        );
    }

    public Bitmap requestEventPicture(String token, String eventId) {
        return httpClient.sendEventPictureRequest(
                mContext,
                token,
                eventId
        );
    }

    public boolean requestDeleteEvent(String token, String eventId) {
        return httpClient.sendDeleteEventRequest(
                mContext,
                token,
                eventId
        );
    }

    public boolean requestEditEvent(String token, String eventId, String petId) {
        return httpClient.sendEditEventRequest(
                mContext,
                token,
                eventId,
                petId
        );
    }

    public List<Pet> requestPetList(String token) {
        return httpClient.sendPetsRequest(
                mContext,
                token
        );
    }

    public boolean initializePets(Callback callback, String token, List<Pet> pets) {
        return httpClient.registerPetsInitial(
                mContext,
                callback,
                token,
                pets
        );
    }
}
