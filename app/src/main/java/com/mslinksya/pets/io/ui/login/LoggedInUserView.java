package com.mslinksya.pets.io.ui.login;

import com.mslinksya.pets.io.data.model.LoggedInUser;

/**
 * Class exposing authenticated user details to the UI.
 */
public class LoggedInUserView {

    private final String userId;
    private final String displayName;
    private final int numberOfPets;
    private final int numberOfPendingPets;
    private final int numberOfDevices;
    //... other data fields that may be accessible to the UI

    LoggedInUserView(String displayName, String userId, int numberOfPets, int numberOfPendingPets, int numberOfDevices) {
        this.displayName = displayName;
        this.userId = userId;
        this.numberOfPets = numberOfPets;
        this.numberOfPendingPets = numberOfPendingPets;
        this.numberOfDevices = numberOfDevices;
    }

    public static LoggedInUserView fromUser(LoggedInUser user) {
        return new LoggedInUserView(
                user.getDisplayName(),
                user.getUserId(),
                user.getNumberOfPets(),
                user.getNumberOfPendingPets(),
                user.getNumberOfDevices());
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getNumberOfPets() {
        return numberOfPets;
    }

    public String getUserId() {
        return userId;
    }

    public int getNumberOfDevices() {
        return numberOfDevices;
    }

    public int getNumberOfPendingPets() {
        return numberOfPendingPets;
    }
}