package com.mslinksya.pets.io.controller;

import android.content.Context;
import android.graphics.Bitmap;

import com.mslinksya.pets.io.data.model.AccountCreationRequestModel;
import com.mslinksya.pets.io.data.model.Event;
import com.mslinksya.pets.io.data.model.LoginRequestModel;
import com.mslinksya.pets.io.data.model.Pet;
import com.squareup.okhttp.Callback;

import java.util.List;

public class ServerController {
    private final HttpClient httpClient;
    private final Context mContext;

    public ServerController(Context mContext) {
        this.mContext = mContext;
        httpClient = new HttpClient();
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
