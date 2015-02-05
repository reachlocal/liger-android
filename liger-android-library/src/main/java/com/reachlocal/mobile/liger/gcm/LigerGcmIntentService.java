package com.reachlocal.mobile.liger.gcm;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.reachlocal.mobile.liger.LIGER;
import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.ui.DefaultMainActivity;

public class LigerGcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public LigerGcmIntentService() {
        super("LigerGcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "LigerGcmIntentService onHandleIntent");
        }
        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            Log.d(LIGER.TAG, "Received: " + messageType + " - " + extras.toString());
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString(), intent);
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString(), intent);
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
            } else {
                // Post notification of received message.
                sendNotification(extras.getString("message", "No message attribute found"), intent);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        LigerGcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    protected void sendNotification(String msg, Intent cloudIntent) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        String activityName = getResources().getString(R.string.liger_main_activity_class);
        Class<? extends DefaultMainActivity> activityClass = null;

        try {
            activityClass = (Class<? extends DefaultMainActivity>) Class.forName(activityName);
        } catch (ClassNotFoundException e) {
            Log.e(LIGER.TAG, "Failed to find class for main activity named " + activityName, e);
        } catch (ClassCastException e) {
            Log.e(LIGER.TAG, "Main activity class named " + activityName + " is not an Activity!", e);
        }

        Intent appIntent = new Intent(this, activityClass);
        Bundle cloudExtras = cloudIntent.getExtras();

        Log.d(LIGER.TAG, "Recieved cloudExtras: " + cloudExtras.toString());
        appIntent.putExtra("notification", cloudExtras);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);


        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, appIntent, 0);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(this.getString(this.getApplicationInfo().labelRes))
                        .setLights(Color.YELLOW, 1, 2)
                        .setAutoCancel(true)
                        .setSound(defaultSound)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);
        //TODO: When we know what the contents of the cloud packet are, replace the contents of the notification

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
