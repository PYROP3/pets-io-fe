package com.mslinksya.pets.io.data.model;

public class AccountCreationRequestModel {
    private final String password;
    private final String userId;
    private final String displayName;
    private final int numberOfPets;

    public AccountCreationRequestModel(String userId, String password, String displayName, int numberOfPets) {
        this.userId = userId;
        this.displayName = displayName;
        this.password = password;
        this.numberOfPets = numberOfPets;
    }

    public String getPassword() {
        return password;
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
}
