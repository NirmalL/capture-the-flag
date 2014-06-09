/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import java.util.ArrayList;

import android.location.Location;

/**
 * Base class for {@link LocationManagerInterface} implementations.
 * 
 * Contains implementation shared between Here and Google-specific implementations of {@link LocationManagerInterface}
 */
public abstract class LocationManagerBase implements LocationManagerInterface {

    protected ArrayList<LocationManagerListener> mListeners;

    @Override
    public void addListener(LocationManagerListener listener) {
        if(!mListeners.contains(listener)) {
            mListeners.add(listener);
        }        
    }

    @Override
    public void removeListener(LocationManagerListener listener) {
        // Return value ignored
        mListeners.remove(listener);
    }
    
    /**
     * Constructor.
     */
    protected LocationManagerBase() {
        mListeners = new ArrayList<LocationManagerListener>();
    }

    /**
     * Notifies all the currently registered listeners of a change in location.
     * @param location New location, @see {@link Location}.
     */
    protected void notifyListeners(Location location) {
        for (LocationManagerListener listener : mListeners) {
            listener.onLocationUpdated(location);
        }
    }
    
    /**
     * Notifies all the currently registered users that the LocationManager implementation is ready to provide location information or that the initialization failed.
     * @param success <code>true</code> if connection was successful, <code>false</code> if not.
     */
    protected void notifyManagerReady(boolean success) {
        for (LocationManagerListener listener : mListeners) {
            listener.onLocationManagerReady(success);
        }
    }
}
