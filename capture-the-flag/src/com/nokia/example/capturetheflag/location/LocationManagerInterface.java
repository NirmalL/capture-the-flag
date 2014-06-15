/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import com.nokia.example.capturetheflag.location.google.LocationManagerGoogle;
import com.nokia.example.capturetheflag.location.here.LocationManagerHere;

import android.location.Location;

/**
 * Interface for accessing platform-specific location services.
 * <p/>
 * Use {@link LocationManagerFactory} to instantiate a class that implements
 * this interface.
 *
 * @see {@link LocationManagerHere} and {@link LocationManagerGoogle} for the
 * platform-specific implementations.
 */
public interface LocationManagerInterface {

    /**
     * Interface for receiving reverse geocoding results.
     */
    public interface ReverseGeocodingResultListener {
        /**
         * Called when reverse geocoding result has been received for a call
         * to @see {@link LocationManagerInterface#reverseGeocodeLocation(Location, ReverseGeocodingResultListener)}.
         *
         * @param result Contains the reverse geocoded address for the given location or <code>null</code> if address could not be resolved.
         */
        public void onReverseGeocodingResult(String result);
    }

    /**
     * Returns true if location information is available.
     *
     * @return <code>true</code> if location information is available, <code>false</code> if not.
     */
    public boolean isLocationAvailable();

    /**
     * Starts requesting location updates.
     */
    public void start();

    /**
     * Stops requesting location updated.
     */
    public void stop();

    /**
     * Sets a listener that will receive location updates through
     * {@link LocationManagerListener}. Any previously set listener will be removed.
     *
     * @param listener to add @See {@link LocationManagerListener}.
     */
    public void setListener(LocationManagerListener listener);

    /**
     * Removes the currently set location update listener.
     */
    public void removeListener();

    /**
     * Returns the current (or last received) location.
     *
     * @return {@link Location}.
     */
    public Location getCurrentLocation();

    /**
     * Tries to reverse geocode a human-readable address for the given location.
     *
     * @param location {@link Location} to be reverse geocoded.
     * @param listener {@link ReverseGeocodingResultListener} to call when reverse geocoding is ready.
     */
    public void reverseGeocodeLocation(Location location, final ReverseGeocodingResultListener listener);

}
