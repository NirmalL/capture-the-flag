/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
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
     *
     * @param context Context.
     */
    public NotificationsManagerBase(Context context) {
        super();
        mContext = context;
    }

    /**
     * Checks whether a stored registration id exists and is valid.
     *
     * @return <code>true</code> if registration is found and is valid, <code>false</code> if not.
     */
    protected boolean hasRegistrationId() {
        final String registrationId = getRegistrationId();
        return (registrationId != null) && !registrationId.isEmpty();
    }
}
