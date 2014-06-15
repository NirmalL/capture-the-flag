/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.notifications.nokia;

import com.nokia.example.capturetheflag.notifications.NotificationsManagerBase;
import com.nokia.example.capturetheflag.notifications.NotificationsManagerInterface;
import com.nokia.push.PushRegistrar;

import android.content.Context;

/**
 * Nokia specific implementation of the {@link NotificationsManagerInterface}.
 * Uses {@link PushRegistrar} for registering the application for push notifications.
 */
public class NotificationsManagerNokia extends NotificationsManagerBase {

    /**
     * Constructor.
     * <p/>
     * Constructs the Nokia Notifications specific Notifications Manager instance.
     *
     * @param context Context.
     */
    public NotificationsManagerNokia(Context context) {
        super(context);
    }

    /**
     * Registers the application to the {@link PushRegistrar} for receiving
     * push notifications. The {@link PushRegistrar} stores the registration id
     * internally.
     */
    @Override
    public void register() {
        PushRegistrar.checkDevice(mContext);
        PushRegistrar.checkManifest(mContext);

        if (!hasRegistrationId()) {
            PushRegistrar.register(mContext, NokiaNotificationsIntentService.SENDER_ID);
        }
    }

    /**
     * Returns the application's registration id.
     *
     * @return The registration id if available, null otherwise.
     */
    @Override
    public String getRegistrationId() {
        return PushRegistrar.getRegistrationId(mContext);
    }

    /**
     * Releases the allocated resources when the application is terminated.
     */
    @Override
    public void onDestroy() {
        PushRegistrar.onDestroy(mContext);
    }

    @Override
    public NotificationServiceType getServiceType() {
        return NotificationServiceType.NOKIA_NOTIFICATIONS;
    }
}
