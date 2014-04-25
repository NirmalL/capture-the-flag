/**
 * Copyright (c) 2013-2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.nokia.example.capturetheflag.network.model.ModelConstants;
import com.nokia.push.PushBaseIntentService;

/**
 * IntentService responsible for handling push notification messages.
 */
public class PushIntentService extends PushBaseIntentService {
    public static final String SENDER_ID = "capture-the-flag"; // Sender ID for Nokia Notifications
    public static final String PUSH_MESSAGE_ACTION = "com.nokia.example.capturetheflag.PUSH_MESSAGE_ACTION";
    private static final String TAG = "CtF/PushIntentService";

    /**
     * Constructor.
     */
    public PushIntentService() {
    }

    @Override
    protected String[] getSenderIds(Context context) {
        return new String[] { SENDER_ID };
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
        Intent i = new Intent(PUSH_MESSAGE_ACTION);
        i.putExtra(ModelConstants.CAPTURER_KEY, extras.getString("payload"));
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
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
