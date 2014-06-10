/**
 * Copyright (c) 2013-2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
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
import com.nokia.example.capturetheflag.notifications.NotificationsManagerInterface.NotificationServiceType;

public class NotificationsManagerGoogle extends NotificationsManagerBase {
    private static final String TAG = "CtF/NotificationsManagerGoogle";

    private static final String PREFS_KEY_REGISTRATION_ID = "registration_id";    
    private static final String GCM_SENDER_ID = "1006294830624";
    
    private GoogleCloudMessaging gcm;
    
    @Override
    public void register() {
        String registrationId = getRegistrationId();
        if(registrationId == null || registrationId.isEmpty()) {
            registerInBackground();
        } else {
            Log.d(TAG, "Registered to Google Cloud Messaging using existing id:" + registrationId);
        }
    };
    
    
    private boolean checkPlayServices() {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        return resultCode == ConnectionResult.SUCCESS;
    }    

    public NotificationsManagerGoogle(Context context) {
        super(context);

        if(checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(context);
        }
    }

    /** 
     */
    private void storeRegistrationId(String registrationId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit().putString(PREFS_KEY_REGISTRATION_ID, registrationId).apply();
    }
    
    /** 
     * @return The notification/registration ID if available, null otherwise.
     */
    public String getRegistrationId() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getString(PREFS_KEY_REGISTRATION_ID, null);
    }

    /**
     * Executes the necessary operations when the application is terminated.
     */
    public void onDestroy() {
        gcm.close();
    }
    
    @Override
    public NotificationServiceType getServiceType() {
        return NotificationServiceType.NOKIA_PUSH_MESSAGING;
    }
    
    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mContext);
                    String registrationId = gcm.register(GCM_SENDER_ID);
                    storeRegistrationId(registrationId);
                    Log.d(TAG, "Registered to Google Cloud Messaging:" + registrationId);
                } catch (IOException ex) {
                    Log.d(TAG, "IO ERROR REGISTERING: " + ex.toString());
                }
                return null;
            }
        }.execute(null, null, null);
    }
}
