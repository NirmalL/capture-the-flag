/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.notifications.google;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.*;
import com.nokia.example.capturetheflag.notifications.NotificationsManagerBase;
import com.nokia.example.capturetheflag.notifications.NotificationsManagerInterface;

/**
 * Google-specific implementation of the {@link NotificationsManagerInterface}.
 * Uses {@link GoogleCloudMessaging} for registering the application for push
 * notifications.
 */
public class NotificationsManagerGoogle extends NotificationsManagerBase {
    private static final String TAG = "CtF/NotificationsManagerGoogle";

    private static final String PREFS_KEY_REGISTRATION_ID = "registration_id";
    private static final String GCM_SENDER_ID = "1006294830624";

    private GoogleCloudMessaging mGcm;

    /**
     * Constructor.
     * <p/>
     * Constructs the Google Cloud Messaging specific Notifications Manager instance.
     *
     * @param context Context.
     */
    public NotificationsManagerGoogle(Context context) {
        super(context);

        if (checkPlayServices()) {
            mGcm = GoogleCloudMessaging.getInstance(context);
        } else {
            Log.d(TAG, "Google Play Services not available!");
        }
    }

    /**
     * Registers the application to the {@link GoogleCloudMessaging} for receiving
     * push notifications.
     */
    @Override
    public void register() {
        if (!hasRegistrationId()) {
            registerInBackground();
        }
    }

    ;

    /**
     * Checks whether Google Play Services are available or not.
     *
     * @return <code>true</code> if Google Play Services are available, <code>false</code> if not.
     */
    private boolean checkPlayServices() {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        return resultCode == ConnectionResult.SUCCESS;
    }

    /**
     * Stores the application's registration to {@link SharedPreferences}.
     *
     * @param registrationId Registration id to store.
     */
    private void storeRegistrationId(String registrationId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit().putString(PREFS_KEY_REGISTRATION_ID, registrationId).apply();
    }

    /**
     * Returns the application's registration id.
     *
     * @return The registration id if available, null otherwise.
     */
    public String getRegistrationId() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getString(PREFS_KEY_REGISTRATION_ID, null);
    }

    /**
     * Closes the Google Cloud Messaging connection when the application is terminated.
     */
    public void onDestroy() {
        mGcm.close();
    }

    @Override
    public NotificationServiceType getServiceType() {
        return NotificationServiceType.GOOGLE_CLOUD_MESSAGING;
    }

    /**
     * Registers the application to Google Cloud Messaging in the background
     * and stores the received registration id for later use.
     * <p/>
     * Registration is blocking so the operation is executed using
     * {@link AsyncTask} so that the UI does not get blocked.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (mGcm != null) {
                    try {
                        String registrationId = mGcm.register(GCM_SENDER_ID);
                        storeRegistrationId(registrationId);
                        Log.d(TAG, "Registered to Google Cloud Messaging:" + registrationId);
                    } catch (IOException ex) {
                        Log.d(TAG, "IO ERROR REGISTERING: " + ex.toString());
                    }
                }
                return null;
            }
        }.execute(null, null, null);
    }
}
