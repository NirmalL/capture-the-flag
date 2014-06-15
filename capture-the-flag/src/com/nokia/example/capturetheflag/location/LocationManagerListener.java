/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import android.location.Location;

/**
 * Listener interface for receiving location updates from
 * {@link LocationManagerInterface}.
 */
public interface LocationManagerListener {

    /**
     * Called when Location Manager has connected to the underlying location
     * information provider or if the connection failed.
     *
     * @param success <code>true</code> if connection was successful, <code>false</code> if not.
     */
    public void onLocationManagerReady(boolean success);

    /**
     * Called when device's location has changed.
     *
     * @param location New location, @see {@link Location}.
     */
    public void onLocationUpdated(Location location);

}
