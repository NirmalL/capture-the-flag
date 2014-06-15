/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.notifications;

import android.content.Context;
import android.util.Log;

import com.nokia.example.capturetheflag.notifications.google.NotificationsManagerGoogle;
import com.nokia.example.capturetheflag.notifications.nokia.NotificationsManagerNokia;
import com.nokia.push.PushRegistrar;

/**
 * Factory class instantiating the platform specific {@link NotificationsManagerInterface}
 * instance.
 */
public class NotificationsManagerFactory {

    public static final String PUSH_MESSAGE_ACTION = "com.nokia.example.capturetheflag.PUSH_MESSAGE_ACTION";
    private static final String TAG = "CtF/NotificationsManagerFactory";

    private static NotificationsManagerInterface mInstance;

    /**
     * Provides the Notifications Manager singleton instance.
     *
     * @param context The application context.
     * @return The singleton instance of this class.
     */
    public static NotificationsManagerInterface getInstance(Context context) {
        if (mInstance == null) {

            // Check the device for supported notifications service
            try {
                PushRegistrar.checkDevice(context);
                mInstance = new NotificationsManagerNokia(context);
                Log.i(TAG, "Using Nokia Notifications");
            } catch (UnsupportedOperationException e1) {
                try {
                    mInstance = new NotificationsManagerGoogle(context);
                    Log.i(TAG, "Using Google Cloud Messaging");
                } catch (UnsupportedOperationException e2) {
                    Log.w(TAG, "No supported notifications services!");
                }
            }
        }

        return mInstance;
    }
}
