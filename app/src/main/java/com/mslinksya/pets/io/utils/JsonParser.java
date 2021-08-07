package com.mslinksya.pets.io.utils;

import android.net.Uri;
import com.mslinksya.pets.io.utils.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class JsonParser {

    private static final String TAG = "JsonParser";

    public JSONObject createAccountJson(String name, String email, String password, int numberOfPets){
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(Constants.USER_EMAIL_KEY, email);
            jsonObject.put(Constants.USER_NAME_KEY, name);
            jsonObject.put(Constants.USER_PASS_KEY, password);
            jsonObject.put(Constants.USER_N_PETS_KEY, numberOfPets);
            Log.d(TAG, "JSON: " + jsonObject.toString());
        } catch (JSONException e) {
            Log.d(TAG, "Error trying to create JSON");
            e.printStackTrace();
        }
        Log.d(TAG, "JSON created");
        return jsonObject;
    }

    public JSONObject loginJson(String email, String password, String fcmToken){
        JSONObject jsonObject = new JSONObject();

        try{
            jsonObject.put(Constants.USER_EMAIL_KEY, email);
            jsonObject.put(Constants.USER_PASS_KEY, password);
            jsonObject.put(Constants.FCM_TOKEN_KEY, fcmToken);
        }catch (JSONException e){
            Log.d(TAG, "Error trying to create JSON");
            e.printStackTrace();
        }
        Log.d(TAG, "JSON created");
        return jsonObject;
    }

    public HashMap<String, Object> parseLoginResponse(String response){
        HashMap<String, Object> s = new HashMap<>();
        try{
            JSONObject jsonObject = new JSONObject(response);

            String name = jsonObject.getString(Constants.USER_NAME_KEY);
            String token = jsonObject.getString(Constants.USER_TOKEN_KEY);
            String email = jsonObject.getString(Constants.USER_EMAIL_KEY);
            int numberOfPets = jsonObject.getInt(Constants.USER_N_PETS_KEY);
            int numberOfDevices = jsonObject.getInt(Constants.USER_N_DEVICES_KEY);

            s.put(Constants.USER_NAME_KEY, name);
            s.put(Constants.USER_EMAIL_KEY, email);
            s.put(Constants.USER_TOKEN_KEY, token);
            s.put(Constants.USER_N_PETS_KEY, numberOfPets);
            s.put(Constants.USER_N_DEVICES_KEY, numberOfDevices);

        }catch(JSONException e){
            e.printStackTrace();
        }

        return s;
    }

    public static boolean isResponseSuccessful(String jsonResponse){
       try {
           Log.d(TAG, jsonResponse);
           JSONObject jsonObject = new JSONObject(jsonResponse);
           if(jsonObject.getInt("Code") == Constants.SERVER_RESPONSE_OK){
               return true;
           }
       } catch (JSONException e) {
           e.printStackTrace();
       }
        return false;
    }

    public static String getQueryToken(String dataString) {
        Uri uri = Uri.parse(dataString);
        String args = uri.getQueryParameter(Constants.USER_TOKEN_KEY);
        Log.d(TAG, "User token: " + args);
        return args;
    }

    public static int getErrorResponse(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            return jsonObject.getInt("Code");
        }catch(JSONException e){
            Log.d(TAG, "Json exception why");
        }
        return -1;
    }
}
