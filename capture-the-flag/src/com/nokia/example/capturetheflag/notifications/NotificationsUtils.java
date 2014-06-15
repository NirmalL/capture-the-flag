/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.notifications;

import com.nokia.example.capturetheflag.network.model.ModelConstants;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Notifications-related utility functions.
 */
public class NotificationsUtils {

    /**
     * Broadcasts the given game message.
     *
     * @param message The message to broadcast.
     * @param context Context.
     */
    public static void broadcastGameMessage(String message, Context context) {
        Intent i = new Intent(NotificationsManagerFactory.PUSH_MESSAGE_ACTION);
        i.putExtra(ModelConstants.CAPTURER_KEY, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

}
