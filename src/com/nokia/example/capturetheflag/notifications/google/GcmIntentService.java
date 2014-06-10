/**
 * Copyright (c) 2013-2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.notifications.google;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nokia.example.capturetheflag.network.model.ModelConstants;
import com.nokia.example.capturetheflag.notifications.NotificationsManagerFactory;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class GcmIntentService extends IntentService {
    private static final String TAG = "CtF/GcmIntentService";
    public static final int NOTIFICATION_ID = 1;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "GCM onHandleIntent");
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty() && (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))) {
            sendNotification(extras.getString("payload"));
        }

        // Release the wake lock provided by the GcmBroadcastReceiver.
       GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        Intent i = new Intent(NotificationsManagerFactory.PUSH_MESSAGE_ACTION);
        i.putExtra(ModelConstants.CAPTURER_KEY, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}
