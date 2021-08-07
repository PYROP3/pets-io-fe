package com.mslinksya.pets.io.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.util.JsonReader;
import com.mslinksya.pets.io.utils.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.data.model.Event;
import com.mslinksya.pets.io.data.model.Pet;
import com.mslinksya.pets.io.fcm.MessagingService;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.mslinksya.pets.io.utils.Constants;
import com.mslinksya.pets.io.utils.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.MappedByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okio.BufferedSink;

public class HttpClient implements Callback{

    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final String TAG = getClass().getSimpleName();

    public boolean sendCreateAccountRequest(Context context, Callback callback,
                                            String email, String name, String password, int numberOfPets){
        Log.d(TAG, "Preparing sendCreateAccountRequest request 1");

        if(!isNetworkAvailable(context))
            return false;

        OkHttpClient okHttpClient = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(Constants.SERVER_HOST).port(Constants.SERVER_PORT)
                .addPathSegment(Constants.SERVER_CREATE_ACCOUNT)
                .build();

        Log.d(TAG, "Sending request to: " + url.toString());

        JsonParser jsonParser = new JsonParser();
        JSONObject jsonObject = jsonParser.createAccountJson(name, email, password, numberOfPets);
        RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Log.d(TAG, "Enqueuing new retrofit callback [CreateAccount]");

        okHttpClient.newCall(request).enqueue(callback);

        return true;
    }

    public boolean sendLoginRequest(Context context, Callback callback, String email, String password){
        Log.d(TAG, "Preparing sendLoginRequest request 1");
        if(!isNetworkAvailable(context))
            return false;

        Log.d(TAG, "Preparing sendLoginRequest request 2");
        OkHttpClient okHttpClient = new OkHttpClient();

        Log.d(TAG, "Preparing sendLoginRequest request 3");
        HttpUrl url = new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(Constants.SERVER_HOST).port(Constants.SERVER_PORT)
                .addPathSegment(Constants.SERVER_LOGIN)
                .build();

        Log.d(TAG, "Preparing sendLoginRequest request 4");

        Log.d(TAG, "Sending request to: " + url.toString());

//        CompletableFuture<String> fcmTokenFuture = new CompletableFuture<>();
//
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
//                            fcmTokenFuture.complete("<null>");
//                            return;
//                        }
//
//                        // Get new FCM registration token
//                        String token = task.getResult();
//                        Log.d(TAG, "got FCM token = [" + token + "]");
//                        fcmTokenFuture.complete(token);
//                    }
//                });
//
//        String fcmToken;
//        try {
//            fcmToken = fcmTokenFuture.get(Constants.FCM_GET_TOKEN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
//        } catch (InterruptedException | ExecutionException | TimeoutException e) {
//            Log.w(TAG, "FCM token timeout: " + e);
//            fcmToken = "<null>";
//        }

        JsonParser jsonParser = new JsonParser();
        JSONObject jsonObject = jsonParser.loginJson(email, password, MessagingService.getToken());
        RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Log.d(TAG, "Enqueuing new retrofit callback [Login]");

        okHttpClient.newCall(request).enqueue(callback);

        return true;
    }

    public boolean verifyAccount(Context context, String token){
        Log.d(TAG, "Preparing verifyAccount request 1");
        if(!isNetworkAvailable(context))
            return false;

        OkHttpClient okHttpClient = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(Constants.SERVER_HOST).port(Constants.SERVER_PORT)
                .addPathSegment(Constants.SERVER_VERIFY_ACCOUNT)
                .addQueryParameter(Constants.USER_TOKEN_KEY, token)
                .build();

        Log.d(TAG, "Sending request to: " + url.toString());

        Request request = new Request.Builder()
                .url(url)
                .build();

        Log.d(TAG, "Enqueuing new retrofit callback [VerifyAccount]");

        try {
            Response r = okHttpClient.newCall(request).execute();
            if(r.isSuccessful()){
                Log.d(TAG, "ResponseSuccessful!");
                return true;
            }else{
                Log.d(TAG, "ResponseFailure: " + r.body().string());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePetPictures(Context context, Callback callback, String token){
        Log.d(TAG, "Preparing updatePetPictures request 1");
        if(!isNetworkAvailable(context))
            return false;

        Log.d(TAG, "Preparing pet pictures request : " + token);

        OkHttpClient okHttpClient = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(Constants.SERVER_HOST).port(Constants.SERVER_PORT)
                .addPathSegment(Constants.SERVER_UPDATE_PET_PICS)
                .build();

        Log.d(TAG, "Sending request to: " + url.toString());

        Request request = new Request.Builder()
                .header("Authorization", Constants.KEY_AUTH_TOKEN + token)
                .url(url)
                .build();

        Log.d(TAG, "Enqueuing new retrofit callback [" + Constants.SERVER_UPDATE_PET_PICS + "]");

        okHttpClient.newCall(request).enqueue(callback);

        Log.d(TAG, "Pet pictures request sent");

        return true;
    }

    public boolean registerPetsInitial(Context context, Callback callback, String token, List<Pet> pets){
        if(!isNetworkAvailable(context))
            return false;

        Log.d(TAG, "Preparing pet register request : " + token);

        OkHttpClient okHttpClient = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(Constants.SERVER_HOST).port(Constants.SERVER_PORT)
                .addPathSegment(Constants.SERVER_REQUEST_INIT_PENDING_PETS)
                .build();

        Log.d(TAG, "Sending request to: " + url.toString());

        JSONArray jsonArray = new JSONArray();
        try {
            for (Pet pet : pets) {
                JSONObject jsonPet = new JSONObject();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                pet.getPicture().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                jsonPet.put(Constants.PET_NAME_KEY, pet.getName());
                jsonPet.put(Constants.PET_PICTURE_KEY, Base64.getEncoder().encodeToString(byteArray));
                jsonArray.put(jsonPet);
            }
        } catch (JSONException e) {
            return false;
        }

        Request request = new Request.Builder()
                .header("Authorization", Constants.KEY_AUTH_TOKEN + token)
                .url(url)
                .post(RequestBody.create(JSON, jsonArray.toString()))
                .build();

        Log.d(TAG, "Enqueuing new retrofit callback [" + Constants.SERVER_REQUEST_INIT_PENDING_PETS + "]");

        okHttpClient.newCall(request).enqueue(callback);

        Log.d(TAG, "Pet pictures request sent");

        return true;
    }

    public synchronized boolean sendEndSession(Context context, String token){
        Log.d(TAG, "Preparing sendEndSession request 1");
        if(!isNetworkAvailable(context))
            return false;

        Log.d(TAG, "Preparing end session request");

        OkHttpClient okHttpClient = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(Constants.SERVER_HOST).port(Constants.SERVER_PORT)
                .addPathSegment(Constants.SERVER_LOG_OUT)
                .build();

        Log.d(TAG, "Sending request to: " + url.toString());

        Request request = new Request.Builder()
                .header("Authorization", Constants.KEY_AUTH_TOKEN + token)
                .url(url)
                .build();

        Log.d(TAG, "Enqueuing new retrofit callback [SendEndSession]");

        okHttpClient.newCall(request).enqueue(this);

        Log.d(TAG, "Logout request sent");

        return true;
    }

    public String sendRegistrationRequest(Context context, String token){
        Log.d(TAG, "Preparing registration request 1");
        if(!isNetworkAvailable(context))
            return null;

        Log.d(TAG, "Preparing registration request");

        OkHttpClient okHttpClient = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(Constants.SERVER_HOST).port(Constants.SERVER_PORT)
                .addPathSegment(Constants.SERVER_REGISTER_DEVICE)
                .build();

        Log.d(TAG, "Sending request to: " + url.toString());

        Request request = new Request.Builder()
                .header("Authorization", Constants.KEY_AUTH_TOKEN + token)
                .url(url)
                .post(new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return null;
                    }

                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {

                    }
                })
                .build();

        Log.d(TAG, "Enqueuing new retrofit callback [" + Constants.SERVER_REGISTER_DEVICE + "]");

        try {
            Response r = okHttpClient.newCall(request).execute();
            if(r.isSuccessful()){
                String register_token = r.body().string();
                Log.d(TAG, "ResponseSuccessful! \"" + register_token + "\"");
                return register_token;
            }else{
                Log.d(TAG, "ResponseFailure: " + r.body().string());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> sendDevicesRequest(Context context, String token){
        if(!isNetworkAvailable(context))
            return null;

        Log.d(TAG, "Preparing devices request");

        OkHttpClient okHttpClient = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(Constants.SERVER_HOST).port(Constants.SERVER_PORT)
                .addPathSegment(Constants.SERVER_REQUEST_DEVICES)
                .build();

        Log.d(TAG, "Sending request to: " + url.toString());

        Request request = new Request.Builder()
                .header("Authorization", Constants.KEY_AUTH_TOKEN + token)
                .url(url)
                .get()
                .build();

        Log.d(TAG, "Enqueuing new retrofit callback [" + Constants.SERVER_REQUEST_DEVICES + "]");

        try {
            Response r = okHttpClient.newCall(request).execute();
            if(r.isSuccessful()){
                ArrayList<String> devList = new ArrayList<>();
                String devListString = r.body().string();
                Log.d(TAG, "ResponseSuccessful! \"" + devListString + "\"");
                JSONArray jsonArray = new JSONArray(devListString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    devList.add(jsonArray.getString(i));
                }
                return devList;
            }else{
                Log.d(TAG, "ResponseFailure: " + r.body().string());
                return new ArrayList<>();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Event> sendEventsRequest(Context context, String token, String deviceID){
        if(!isNetworkAvailable(context))
            return null;

        Log.d(TAG, "Preparing events request : " + deviceID);

        OkHttpClient okHttpClient = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(Constants.SERVER_HOST).port(Constants.SERVER_PORT)
                .addPathSegment(Constants.SERVER_REQUEST_EVENTS)
                .addQueryParameter(Constants.DEVICE_ID_KEY, deviceID)
                .build();

        Log.d(TAG, "Sending request to: " + url.toString());

        Request request = new Request.Builder()
                .header("Authorization", Constants.KEY_AUTH_TOKEN + token)
                .url(url)
                .get()
                .build();

        Log.d(TAG, "Enqueuing new retrofit callback [" + Constants.SERVER_REQUEST_EVENTS + "]");

        try {
            Response r = okHttpClient.newCall(request).execute();
            if(r.isSuccessful()){
                ArrayList<Event> devList = new ArrayList<>();
                String devListString = r.body().string();
                Log.d(TAG, "ResponseSuccessful! \"" + devListString + "\"");
                JSONArray jsonArray = new JSONArray(devListString);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
                // 2021-07-28 00:47:25.553000
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    Date eventDate = null;
                    try {
                        eventDate = dateFormat.parse(object.getString(Constants.EVENT_TIMESTAMP_KEY));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    devList.add(new Event(
                            object.getString(Constants.EVENT_ID_KEY),
                            deviceID,
                            !object.getString(Constants.EVENT_EXTRA_KEY).equals("null") ? object.getString(Constants.EVENT_EXTRA_KEY) : null,
                            !object.getString(Constants.EVENT_PET_KEY).equals("null") ? object.getString(Constants.EVENT_EXTRA_KEY) : null,
                            eventDate));
                }
                Log.d(TAG, "Found " + devList.size() + " events");
                return devList;
            }else{
                Log.d(TAG, "ResponseFailure: " + r.body().string());
                return new ArrayList<>();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Bitmap sendEventPictureRequest(Context context, String token, String eventID){
        if(!isNetworkAvailable(context))
            return null;

        Log.d(TAG, "Preparing pic request : " + eventID);

        OkHttpClient okHttpClient = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(Constants.SERVER_HOST).port(Constants.SERVER_PORT)
                .addPathSegment(Constants.SERVER_EVENT_PIC)
                .addQueryParameter(Constants.EVENT_ID_KEY, eventID)
                .build();

        Log.d(TAG, "Sending request to: " + url.toString());

        Request request = new Request.Builder()
                .header("Authorization", Constants.KEY_AUTH_TOKEN + token)
                .url(url)
                .get()
                .build();

        Log.d(TAG, "Enqueuing new retrofit callback [" + Constants.SERVER_EVENT_PIC + "]");

        try {
            Response r = okHttpClient.newCall(request).execute();
            if(r.isSuccessful()){
                return BitmapFactory.decodeStream(r.body().byteStream());
            }else{
                Log.d(TAG, "ResponseFailure: " + r.body().string());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean sendDeleteEventRequest(Context context, String token, String eventID){
        if(!isNetworkAvailable(context))
            return false;

        Log.d(TAG, "Preparing delete event request : " + eventID);

        OkHttpClient okHttpClient = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(Constants.SERVER_HOST).port(Constants.SERVER_PORT)
                .addPathSegment(Constants.SERVER_REQUEST_DELETE_EVENT)
                .addQueryParameter(Constants.EVENT_ID_KEY, eventID)
                .build();

        Log.d(TAG, "Sending request to: " + url.toString());

        Request request = new Request.Builder()
                .header("Authorization", Constants.KEY_AUTH_TOKEN + token)
                .url(url)
                .get()
                .build();

        Log.d(TAG, "Enqueuing new retrofit callback [" + Constants.SERVER_REQUEST_DELETE_EVENT + "]");

        try {
            Response r = okHttpClient.newCall(request).execute();
            if(r.isSuccessful()){
                return true;
            }else{
                Log.d(TAG, "ResponseFailure: " + r.body().string());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendEditEventRequest(Context context, String token, String eventID){
        if(!isNetworkAvailable(context))
            return false;

        Log.d(TAG, "Preparing pic request : " + eventID);

        OkHttpClient okHttpClient = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(Constants.SERVER_HOST).port(Constants.SERVER_PORT)
                .addPathSegment(Constants.SERVER_REQUEST_DELETE_EVENT)
                .addQueryParameter(Constants.EVENT_ID_KEY, eventID)
                .build();

        Log.d(TAG, "Sending request to: " + url.toString());

        Request request = new Request.Builder()
                .header("Authorization", Constants.KEY_AUTH_TOKEN + token)
                .url(url)
                .get()
                .build();

        Log.d(TAG, "Enqueuing new retrofit callback [" + Constants.SERVER_EVENT_PIC + "]");

        try {
            Response r = okHttpClient.newCall(request).execute();
            if(r.isSuccessful()){
                return true;
            }else{
                Log.d(TAG, "ResponseFailure: " + r.body().string());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Pet> sendPetsRequest(Context context, String token){
        if(!isNetworkAvailable(context))
            return null;

        Log.d(TAG, "Preparing pets request");

        OkHttpClient okHttpClient = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme(Constants.SERVER_SCHEME_HTTPS)
                .host(Constants.SERVER_HOST).port(Constants.SERVER_PORT)
                .addPathSegment(Constants.SERVER_REQUEST_GET_PETS)
                .build();

        Log.d(TAG, "Sending request to: " + url.toString());

        Request request = new Request.Builder()
                .header("Authorization", Constants.KEY_AUTH_TOKEN + token)
                .url(url)
                .get()
                .build();

        Log.d(TAG, "Enqueuing new retrofit callback [" + Constants.SERVER_REQUEST_GET_PETS + "]");

        try {
            Response r = okHttpClient.newCall(request).execute();
            if(r.isSuccessful()){
                ArrayList<Pet> devList = new ArrayList<>();
                String devListString = r.body().string();
                Log.d(TAG, "ResponseSuccessful! \"" + devListString + "\"");
                JSONArray jsonArray = new JSONArray(devListString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                    devList.add(new Pet(
                            jsonObject.getString(Constants.PET_ID_KEY),
                            jsonObject.getString(Constants.PET_NAME_KEY)
                    ));
                }
                Log.d(TAG, "Received " + devList.size() + " pets");
                return devList;
            }else{
                Log.d(TAG, "ResponseFailure: " + r.body().string());
                return new ArrayList<>();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private boolean isNetworkAvailable(Context context){
        Log.d(TAG, "isNetworkAvailable");
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager == null) {
                Log.w(TAG, "null connectivityManager");
                return false;
            }

            Log.d(TAG, "connectivityManager.getActiveNetworkInfo().isConnected() = " +
                    connectivityManager.getActiveNetworkInfo().isConnected());
            return connectivityManager.getActiveNetworkInfo() != null
                    && connectivityManager.getActiveNetworkInfo().isConnected();
        } catch(Exception e) {
            Log.d(TAG, "Android returned null connectivity manager");
            return true;
        }
    }

    @Override
    public void onResponse(Response response) throws IOException {
        if(response.isSuccessful()){
            Log.d(TAG, "Response successfully");
        }else{
            Log.d(TAG, "Response is not successfully");
        }
        Log.d(TAG, "Return: " + response.body().string());
    }

    @Override
    public void onFailure(Request request, IOException e) {
        Log.d(TAG, "Fail onFailure");
    }
}
