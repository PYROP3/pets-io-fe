package com.mslinksya.pets.io.data;

import android.content.Context;

import com.mslinksya.pets.io.data.model.LoggedInUser;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class AccountRepository {

    private static volatile AccountRepository instance;

    private AccountDataSource dataSource;

    // private constructor : singleton access
    private AccountRepository(AccountDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static AccountRepository getInstance() {
        if (instance == null) {
            instance = new AccountRepository(new AccountDataSource());
        }
        return instance;
    }

    public Result<Void> createAccount(Context context, String username, String password, String useralias, int numberOfPets) {
        // handle account creation
        return dataSource.createAccount(context, username, password, useralias, numberOfPets);
    }
}