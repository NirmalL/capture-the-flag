/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.notifications;

import android.content.Context;

/**
 * Abstract base class for the Notifications Managers.
 */
public abstract class NotificationsManagerBase implements NotificationsManagerInterface {

    protected Context mContext;

    /**
     * Constructor
     * @param context Context.
     */
    public NotificationsManagerBase(Context context) {
        super();
        mContext = context;
    }
    
    protected boolean hasRegistrationId() {
        final String registrationId = getRegistrationId();
        return (registrationId != null) && !registrationId.isEmpty();
    }
}
