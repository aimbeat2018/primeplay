package ott.primeplay.firebaseservice;

import static ott.primeplay.firebaseservice.Config.TOPIC_GLOBAL;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.appsflyer.AppsFlyerLib;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.pushnotification.NotificationInfo;
import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ott.primeplay.DetailsActivity;
import ott.primeplay.GoldDetailsActivity;
import ott.primeplay.MainActivity;
import ott.primeplay.R;
import ott.primeplay.firebaseservice.util.NotificationHelper;
import ott.primeplay.firebaseservice.util.NotificationUtils;


/**
 * Firebase-Notification
 * https://github.com/quintuslabs/Firebase-Notification
 * Created on 28/04/19..
 * Created by : Santosh Kumar Dash:- http://santoshdash.epizy.com
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private NotificationUtils notificationUtils;
    String offer_id, issue, location;
    Intent intent;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        //(RK)add CTFcmMessageHandler() for clevertap receive notification
    /* new  CTFcmMessageHandler()
                .createNotification(getApplicationContext(), remoteMessage);*/


        //this if for clevertap notification
        if (remoteMessage.getData().size() > 0) {
            Bundle extras = new Bundle();
            for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                extras.putString(entry.getKey(), entry.getValue());
            }

            NotificationInfo info = CleverTapAPI.getNotificationInfo(extras);
            if (info.fromCleverTap) {
                new CTFcmMessageHandler()
                        .createNotification(getApplicationContext(), remoteMessage);
            }

        }
        else {

            //this is for firebase notification
            if(remoteMessage.getData().containsKey("af-uinstall-tracking")){ // "uinstall" is not a typo
                return;
            } else {
                // handleNotification(remoteMessage);
            }

            if (remoteMessage == null)
                return;

            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
                handleNotification(remoteMessage.getNotification());
//            try {
//                JSONObject json = new JSONObject(remoteMessage.getNotification().getBody().toString());
//                handleDataMessage(json);
//            } catch (Exception e) {
//                Log.e(TAG, "Exception: " + e.getMessage());
//            }
            }


            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

                try {

                    HashMap<String, String> map = new HashMap<>();
                    if (remoteMessage.getData() != null) {
//                    builder = new RemoteMessage.Builder("MessagingService");
                        for (String key : remoteMessage.getData().keySet()) {
                            map.put(key, remoteMessage.getData().get(key));
                        }

                        handleDataMessage(new JSONObject(), map);

                    } else {
//                super.handleIntent(intent);
                    }
//                JSONObject json = new JSONObject(remoteMessage.getData().toString());
//                handleDataMessage(json, map);
                } catch (Exception e) {
                    Log.e(TAG, "Exception: " + e.getMessage());
                }
            }
        }

      /*  if(remoteMessage.getData().containsKey("af-uinstall-tracking")){ // "uinstall" is not a typo
            return;
        } else {
            // handleNotification(remoteMessage);
        }
*/

      /*  if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification());
//            try {
//                JSONObject json = new JSONObject(remoteMessage.getNotification().getBody().toString());
//                handleDataMessage(json);
//            } catch (Exception e) {
//                Log.e(TAG, "Exception: " + e.getMessage());
//            }
        }
*/

       /* // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {

                HashMap<String, String> map = new HashMap<>();
                if (remoteMessage.getData() != null) {
//                    builder = new RemoteMessage.Builder("MessagingService");
                    for (String key : remoteMessage.getData().keySet()) {
                        map.put(key, remoteMessage.getData().get(key));
                    }

                    handleDataMessage(new JSONObject(), map);

                } else {
//                super.handleIntent(intent);
                }
//                JSONObject json = new JSONObject(remoteMessage.getData().toString());
//                handleDataMessage(json, map);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }*/
    }


    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // Sending new token to AppsFlyer
        AppsFlyerLib.getInstance().updateServerUninstallToken(getApplicationContext(), token);
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_GLOBAL);
        sendRegistrationToServer(token);
    }


    private void sendRegistrationToServer(final String token) {
        // sending gcm token to server
        Log.e(TAG, "sendRegistrationToServer: " + token);
    }

    private void handleNotification(RemoteMessage.Notification message) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
//            String title = remoteMessage.getNotification().getTitle();
//            String messageBody = remoteMessage.getNotification().getBody();
//            Uri imageUrl = remoteMessage.getNotification().getImageUrl();

            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message.getBody());
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();

//            NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
//            intent = new Intent(getApplicationContext(), MainActivity.class);
//            Log.e(TAG, "sendRegistrationToServer: " + "Inback");
//
//            if (message.getImageUrl() != null) {
//                notificationHelper.createNotification(message.getTitle(), message.getBody(), R.drawable.ppapplogo, String.valueOf(message.getImageUrl()), "", intent);
//            } else {
//                notificationHelper.createNotification(message.getTitle(), message.getBody(), "", intent);
//            }

//            showNotificationMessageWithBigImage(this, message.getTitle(), message.getBody(), "", pushNotification, String.valueOf(message.getImageUrl()));
        } else {
            // If the app is in background, firebase itself handles the notification
//            Log.e(TAG, "sendRegistrationToServer: " + "Inback22");
            Uri alarmSound = Uri.parse("android.resource://" +
                    getPackageName() + "/" + R.raw.notification_sound);
            Ringtone r = RingtoneManager.getRingtone(this, alarmSound);
            r.play();

//            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
//            pushNotification.putExtra("message", message.getBody());
//            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
        }
    }

    private void handleDataMessage(JSONObject json, HashMap<String, String> map) {
        String imageUrl = "", timestamp = "", id = "", type = "";
        Log.e(TAG, "push json: " + map.toString());
        try {

//            JSONObject data = json.getJSONObject(map);
            JSONObject data = new JSONObject(map);
            Log.e(TAG, "push json: " + data.toString());
            String title = data.getString("nt");
            String message = data.getString("nm");
//            String title = data.getString("title");
//            String message = data.getString("message");
//            boolean isBackground = data.getBoolean("is_background");
            if (data.has("wzrk_bp"))
                imageUrl = data.getString("wzrk_bp");

            if (data.has("wzrk_ttl"))
                timestamp = data.getString("wzrk_ttl");

            if (data.has("id"))
                id = data.getString("id");

            if (data.has("type"))
                type = data.getString("type");
//            String flag = data.getString("flag");
//            String is_gold = data.getString("is_gold");
//            String amt = data.getString("amt");

            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();

            NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());

//            if (flag.equals("Gold")) {
//                intent = new Intent(getApplicationContext(), GoldDetailsActivity.class);
//                intent.putExtra("vType", type);
//                intent.putExtra("id", id);
//                intent.putExtra("is_gold", is_gold);
//                intent.putExtra("amt", amt);
//            } else {
            if (type.equals("")) {
                intent = new Intent(getApplicationContext(), MainActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), DetailsActivity.class);
                intent.putExtra("vType", type);
                intent.putExtra("id", id);
            }
//            }

            if (imageUrl != null) {
              //  notificationHelper.createNotification(title, message, R.drawable.ppapplogo, imageUrl, timestamp, intent);
                notificationHelper.createNotification(title, message, R.drawable.ppapplogo, imageUrl, timestamp, intent);
            } else {
                notificationHelper.createNotification(title, message, timestamp, intent);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        Toast.makeText(this, "in app back1", Toast.LENGTH_SHORT).show();
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }
}
