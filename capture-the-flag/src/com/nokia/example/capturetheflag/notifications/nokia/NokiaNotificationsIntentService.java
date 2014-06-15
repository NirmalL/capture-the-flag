/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.notifications.nokia;

import com.nokia.example.capturetheflag.notifications.NotificationsUtils;
import com.nokia.push.PushBaseIntentService;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Intent service responsible for handling Nokia Notifications push messages.
 *
 * @see {@link PushBaseIntentService}.
 */
public class NokiaNotificationsIntentService extends PushBaseIntentService {
    public static final String SENDER_ID = "capture-the-flag"; // Sender ID for Nokia Notifications
    private static final String TAG = "CtF/PushIntentService";

    /**
     * Constructor.
     */
    public NokiaNotificationsIntentService() {
    }

    @Override
    protected String[] getSenderIds(Context context) {
        return new String[]{SENDER_ID};
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered with ID \"" + registrationId + "\"");
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message. Extras: " + intent.getExtras());

        Bundle extras = intent.getExtras();
        NotificationsUtils.broadcastGameMessage(extras.getString("payload"), this);

        NokiaNotificationsBroadcastReceiver.completeWakefulIntent(intent);
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error:  " + errorId);
        // TODO: Show error message
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        Log.e(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }
}
