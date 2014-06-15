/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.notifications.nokia;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Broadcast Receiver for Nokia Notifications.
 * <p/>
 * Receives Nokia Notifications messages and passes them to
 * {@link NokiaNotificationsIntentService}.
 * <p/>
 * This implementation is necessary only for redefining the name of the intent
 * service that will handle the received messages because the intent service
 * used in this example application has a non-default package/name of
 * com.nokia.example.capturetheflag.notifications.nokia.NokiaNotificationsIntentService
 * due to organizing all notifications related implementation under the
 * "notifications" package and its sub-packages.
 * <p/>
 * If the default name of .PushIntentService would be used for the intent
 * service instead, this custom Broadcast Receiver implementation would not be
 * required.
 *
 * @see <a href="http://developer.nokia.com/resources/library/nokia-x/nokia-notifications/nokia-notifications-developer-guide/nokia-notifications-client-api-guide.html">Nokia Notifications Client API Guide</a>.
 */
public class NokiaNotificationsBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that NokiaNotificationsmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), NokiaNotificationsIntentService.class.getName());

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}