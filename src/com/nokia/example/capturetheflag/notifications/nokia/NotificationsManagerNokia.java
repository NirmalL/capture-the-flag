package com.nokia.example.capturetheflag.notifications.nokia;

import android.content.Context;
import android.util.Log;

import com.nokia.example.capturetheflag.notifications.NotificationsManagerBase;
import com.nokia.example.capturetheflag.notifications.NotificationsManagerInterface;
import com.nokia.push.PushRegistrar;

/**
 * Nokia-specific implementation of the {@link NotificationsManagerInterface}.
 * Uses {@link PushRegistrar} for registering the application for push notifications.
 */
public class NotificationsManagerNokia extends NotificationsManagerBase {

    private static final String TAG = "CtF/NotificationsManagerNokia";

    /**
     * Constructor.
     * 
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
        final String registrationId = getRegistrationId();
        if (registrationId == null || registrationId.isEmpty()) {
            Log.d(TAG, "Registering to Nokia Notifications...");
            PushRegistrar.register(mContext, NokiaNotificationsIntentService.SENDER_ID);
        } else {
            Log.d(TAG, "Registered to Nokia Notifications using existing id: " + registrationId);            
        }
    }

    /** 
     * Returns the application's registration id.
     * @return The registration id if available, null otherwise.
     */
    @Override
    public String getRegistrationId() {
        Log.d(TAG, "registration id: " + PushRegistrar.getRegistrationId(mContext));
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
