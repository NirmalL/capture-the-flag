/**
 * Copyright (c) 2013-2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.notifications;

import android.content.Context;

public abstract class NotificationsManagerBase implements NotificationsManagerInterface {

    protected Context mContext;

    /**
     * 
     */
    public NotificationsManagerBase(Context context) {
        super();
        mContext = context;
    }
}
