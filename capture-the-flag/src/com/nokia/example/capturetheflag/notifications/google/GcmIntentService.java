/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.notifications.google;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nokia.example.capturetheflag.notifications.NotificationsUtils;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

/**
 * Intent service responsible for handling Google Cloud Messaging (GCM) push messages.
 * <p/>
 * Receives GCM messages from {@link GcmBroadcastReceiver} and processes them.
 */
public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;

    /**
     * Constructor.
     */
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Extract GCM message type
        // Only messages of type GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE are handled.
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty() && (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))) {
            NotificationsUtils.broadcastGameMessage(extras.getString("payload"), this);
        }

        // Release the wake lock provided by the GcmBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
