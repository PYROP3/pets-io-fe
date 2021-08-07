package com.mslinksya.pets.io.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.mslinksya.pets.io.utils.Log;

import com.mslinksya.pets.io.data.model.LoggedInUser;
import com.mslinksya.pets.io.utils.Constants;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static final String TAG = LoginRepository.class.getSimpleName();

    private static volatile LoginRepository instance;

    private final LoginDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance() {
        if (instance == null) {
            instance = new LoginRepository(new LoginDataSource());
        }
        return instance;
    }

    public boolean isLoggedIn() {
        Log.d(TAG, "isLoggedIn " + (user != null));
        return user != null;
    }

    public LoggedInUser getUser() {
        return user;
    }

    public void logout(Context context) {
        Log.d(TAG, "logout");
        dataSource.logout(context, user.getAuthToken());
        user = null;
        clearSharedPreferences(context);
    }

    private void setLoggedInUser(LoggedInUser user) {
        Log.d(TAG, "setLoggedInUser : " + user.toBundle().toString());
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public void clearSharedPreferences(Context context) {
        Log.d(TAG, "clearSharedPreferences");
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.USER_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear().apply();
    }

    public void updateSharedPreferences(Context context) {
        Log.d(TAG, "updateSharedPreferences");
        if (!isLoggedIn()) {
            Log.w(TAG, "updateSharedPreferences() : not logged in");
            return;
        }
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.USER_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.USER_TOKEN_KEY, user.getAuthToken())
                .putString(Constants.USER_NAME_KEY, user.getDisplayName())
                .putString(Constants.USER_EMAIL_KEY, user.getUserId())
                .putInt(Constants.USER_N_PETS_KEY, user.getNumberOfPets())
                .putInt(Constants.USER_N_DEVICES_KEY, user.getNumberOfDevices())
                .apply();
    }

    public boolean loadSharedPreferences(Context context) {
        if (isLoggedIn()) {
            Log.w(TAG, "loadSharedPreferences() : already logged in");
            return false;
        }
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.USER_DATA, Context.MODE_PRIVATE);
        if (sharedPref.getAll().isEmpty()) {
            Log.d(TAG, "not logged in : " + sharedPref.getString(Constants.USER_EMAIL_KEY, "null"));
            return false;
        }
        LoggedInUser user = new LoggedInUser(
                sharedPref.getString(Constants.USER_EMAIL_KEY, null),
                sharedPref.getString(Constants.USER_NAME_KEY, null),
                sharedPref.getString(Constants.USER_TOKEN_KEY, null),
                sharedPref.getInt(Constants.USER_N_PETS_KEY, 0),
                sharedPref.getInt(Constants.USER_N_DEVICES_KEY, 0));
        new Thread(() -> user.requestPets(context)).start();
        setLoggedInUser(user);
        return true;
    }

    public Result<LoggedInUser> login(Context context, String username, String password) {
        Log.d(TAG, "login");
        // handle login
        Result<LoggedInUser> result = dataSource.login(context, username, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
            updateSharedPreferences(context);
        }
        return result;
    }
}