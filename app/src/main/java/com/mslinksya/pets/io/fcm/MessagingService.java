package com.mslinksya.pets.io.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import com.mslinksya.pets.io.utils.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.utils.Constants;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = MessagingService.class.getSimpleName();
    private static final String CHANNEL_ID = "Pets.IO";

    private static final Set<Integer> notificationList = new HashSet<>();

    private static String token = null;

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.FCM_DATA, Context.MODE_PRIVATE);
        String previousToken = sharedPref.getString(Constants.FCM_PREVIOUS_TOKEN_KEY, null);
        if (previousToken != null) {
            // TODO send request to server to update token
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.FCM_PREVIOUS_TOKEN_KEY, token).apply();

        MessagingService.token = token;
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: " + remoteMessage.getMessageId());
        Log.d(TAG, "onMessageReceived: " + remoteMessage.getData().toString());

        notifyUser(remoteMessage.getData());

        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onMessageSent(@NonNull String s) {
        Log.d(TAG, "onMessageSent: " + s);
        super.onMessageSent(s);
    }

    public static String getToken() {
        if (token != null) {
            return token;
        }
        CompletableFuture<String> fcmTokenFuture = new CompletableFuture<>();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            fcmTokenFuture.complete("<null>");
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.d(TAG, "got FCM token = [" + token + "]");
                        fcmTokenFuture.complete(token);
                    }
                });

        String fcmToken;
        try {
            fcmToken = fcmTokenFuture.get(Constants.FCM_GET_TOKEN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Log.w(TAG, "FCM token timeout: " + e);
            fcmToken = null;
        }
        token = fcmToken;

        return token;
    }

    public static void getTokenAndDo(FCMRunnable runnable) {
        Log.d(TAG, "getTokenAndDo");
        new Thread(() -> FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, " threaded got FCM token = [" + token + "]");
                    MessagingService.token = token;
                    if (runnable != null) runnable.run(token);
                })).start();
    }

    public abstract static class FCMRunnable implements Runnable {
        @Override
        public void run() {
            Log.e(TAG, "use run(String fcmToken) instead");
        }

        public abstract void run(String fcmToken);
    }

    public static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificações";
            String description = "Canal para notificar usos dos seus devices Pets.IO";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void notifyUser(Map<String, String> message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Pets!")
                .setContentText(generateTextForDevice(message.get("DeviceType"), message.get("EventExtra")))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        int notificationId = new Random().nextInt();
        notificationList.add(notificationId);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
    }

    public static void clearNotifications(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
        notificationList.clear();
    }

    private String generateTextForDevice(String deviceType, String eventExtra) {
        String msg;
        switch (deviceType) {
            case "foodbowl":
                msg = "Seu pet acabou de visitar o pote de comida";
                break;
            case "waterbowl":
                msg = "Seu pet acabou de visitar o pote de água";
                break;
            case "litterbox":
                msg = "Seu pet acabou de visitar a caixa de areia";
                break;
            default:
                msg = "Seu pet acabou de visitar um dispositivo Pets.IO";
        }
        if (!eventExtra.equals("null")) {
            msg += "\n" + eventExtra;
        }
        return msg;
    }
}
