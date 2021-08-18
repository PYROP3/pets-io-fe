package com.mslinksya.pets.io.data.model;

import android.content.Context;
import android.os.Bundle;
import com.mslinksya.pets.io.utils.Log;

import com.mslinksya.pets.io.controller.ServerController;
import com.mslinksya.pets.io.utils.Constants;

import java.util.ArrayList;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {
    private static final String TAG = LoggedInUser.class.getSimpleName();

    private final String userId;
    private final String displayName;
    private final String authToken;
    private int numberOfPets;
    private int numberOfPendingPets;
    private final int numberOfDevices;
    private final ArrayList<Pet> pets = new ArrayList<>();

    public LoggedInUser(String userId, String displayName, String authToken, int numberOfPets, int numberOfDevices) {
        this.userId = userId;
        this.displayName = displayName;
        this.authToken = authToken;
        this.numberOfPets = Math.max(numberOfPets, 0);
        this.numberOfPendingPets = -1 * Math.min(numberOfPets, 0);
        this.numberOfDevices = numberOfDevices;
        Log.d(TAG, "new LoggedInUser: " + toBundle().toString());
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getNumberOfPets() {
        return numberOfPets;
    }

    public int getNumberOfPendingPets() {
        return numberOfPendingPets;
    }

    public void removePendingPets() {
        numberOfPets = numberOfPendingPets;
        numberOfPendingPets = 0;
    }

    public int getNumberOfDevices() {
        return numberOfDevices;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.USER_EMAIL_KEY, this.userId);
        bundle.putString(Constants.USER_NAME_KEY, this.displayName);
        bundle.putString(Constants.USER_TOKEN_KEY, this.authToken);
        bundle.putInt(Constants.USER_N_PETS_KEY, this.numberOfPets);
        bundle.putInt(Constants.USER_PENDING_PETS_KEY, this.numberOfPendingPets);
        bundle.putInt(Constants.USER_N_DEVICES_KEY, this.numberOfDevices);
        return bundle;
    }

    public void addPet(Pet pet) {
        this.pets.add(pet);
    }

    public ArrayList<Pet> getPets() {
        return this.pets;
    }

    public Pet getPet(String petID) {
        for (Pet pet :
                this.pets) {
            if (pet.getID().equals(petID)) {
                return pet;
            }
        }
        return null;
    }

    public ArrayList<Pet> requestPets(Context context) {
        if (this.pets.isEmpty()) {
            Log.d(TAG, "Pet list empty, fetching from server");
            this.pets.addAll(new ServerController(context).requestPetList(authToken));
        }
        Log.d(TAG, "New pet list: " + this.pets.toString() + " (" + this.pets.size() + " pets)");
        return this.pets;
    }
}