/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import android.location.Location;

/**
 * Base class for {@link LocationManagerInterface} implementations.
 * <p/>
 * Contains implementation shared between Here and Google-specific
 * implementations of {@link LocationManagerInterface}.
 */
public abstract class LocationManagerBase implements LocationManagerInterface {

    protected LocationManagerListener mListener;

    @Override
    public void setListener(LocationManagerListener listener) {
        if (listener != null) {
            mListener = listener;
        }
    }

    @Override
    public void removeListener() {
        mListener = null;
    }

    /**
     * Constructor.
     */
    protected LocationManagerBase() {
        super();
    }

    /**
     * Notifies all the currently registered listeners of a change in location.
     *
     * @param location New location, @see {@link Location}.
     */
    protected void notifyListener(Location location) {
        if (mListener != null) {
            mListener.onLocationUpdated(location);
        }
    }

    /**
     * Notifies all the currently registered users that the Location Manager
     * implementation is ready to provide location information or if the
     * initialization failed.
     *
     * @param success <code>true</code> if connection was successful, <code>false</code> if not.
     */
    protected void notifyManagerReady(boolean success) {
        if (mListener != null) {
            mListener.onLocationManagerReady(success);
        }
    }
}
