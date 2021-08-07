package com.mslinksya.pets.io.data;

import android.content.Context;
import com.mslinksya.pets.io.utils.Log;

import com.mslinksya.pets.io.controller.ServerController;
import com.mslinksya.pets.io.data.model.LoggedInUser;
import com.mslinksya.pets.io.data.model.LoginRequestModel;
import com.mslinksya.pets.io.utils.Constants;
import com.mslinksya.pets.io.utils.JsonParser;
import com.mslinksya.pets.io.utils.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private static final String TAG = LoginDataSource.class.getSimpleName();

    public Result<LoggedInUser> login(Context context, String username, String password) {

        try {
            CompletableFuture<Result<LoggedInUser>> future = new CompletableFuture<>();
            if (!new ServerController(context).login(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.d(TAG, "login onFailure : " + e.toString());
                    future.complete(new Result.Error(new IOException("Error logging in", e)));
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    Log.d(TAG, "login onResponse : " + response.toString());
                    if(response.isSuccessful()){
                        Log.d(TAG, "Successful response");

                        String res = response.body().string();
                        Log.d(TAG, "Received: " + res);

                        LoggedInUser user = parseServerResponse(res);
                        if (user != null) {
                            future.complete(new Result.Success<LoggedInUser>(user));
                        } else {
                            future.complete(new Result.Error(new IOException("Error parsing data")));
                        }
                    }else {
                        future.complete(new Result.Error(new IOException("Error logging in (" +
                                response.code() + "/" + response.message() + ")")));
                    }
                }
            },
                    new LoginRequestModel(username, password))) {
                return new Result.Error(new IOException("Error logging in"));
            }

            Result<LoggedInUser> result = future.get(5000, TimeUnit.MILLISECONDS);
            if (result == null) {
                return new Result.Error(new IOException("Error logging in (timeout)"));
            }
            return result;
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    private LoggedInUser parseServerResponse(String response) {
        Log.d(TAG, "parseServerResponse");
        JsonParser jsonParser = new JsonParser();
        HashMap<String, Object> s = jsonParser.parseLoginResponse(response);
        Log.d(TAG, "parsed hashmap : " + s.toString());

        try {
            return new LoggedInUser(
                    s.get(Constants.USER_EMAIL_KEY).toString(),
                    s.get(Constants.USER_NAME_KEY).toString(),
                    s.get(Constants.USER_TOKEN_KEY).toString(),
                    Utils.parseIntOrDefault(s.get(Constants.USER_N_PETS_KEY).toString(), -1),
                    Utils.parseIntOrDefault(s.get(Constants.USER_N_DEVICES_KEY).toString(), -1));
        } catch (Exception e) {
            Log.d(TAG, "Failed parsing due to " + e.toString());
            return null;
        }
    }

    public void logout(Context context, String token) {
        new ServerController(context).logout(token);
    }
}