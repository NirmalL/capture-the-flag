/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import com.nokia.example.capturetheflag.location.google.LocationManagerGoogle;
import com.nokia.example.capturetheflag.location.here.LocationManagerHere;

import android.location.Location;

/**
 * Interface for accessing platform-specific location services.
 * 
 * Use {@link LocationManagerFactory} to instantiate a class that implements this interface.
 * @See {@link LocationManagerHere} and {@link LocationManagerGoogle} for the platform-specific implementations.
 */
public interface LocationManagerInterface {

    /**
     * Interface for receiving reverse geocoding results.
     */
    public interface ReverseGeocodingResultListener {
        /**
         * Called when reverse geocoding result has been received for a call to @see {@link LocationManagerInterface#reverseGeocodeLocation(Location, ReverseGeocodingResultListener)}.
         * @param result Contains the reverse geocoded address for the given location or null if address could not be resolved.
         */
        public void onReverseGeocodingResult(String result);
    }

    /**
     * Returns true if location information is available.
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
     * Adds a new listener that will receive location updates through @see {@link LocationManagerListener}.
     * @param listener to add @See {@link LocationManagerListener}.
     */
    public void addListener(LocationManagerListener listener);

    /**
     * Removes a location update listener.
     * @param listener to remove @See {@link LocationManagerListener}.
     */
    public void removeListener(LocationManagerListener listener);
    
    /**
     * Returns the current (or last received) location.
     * @return {@link Location}.
     */
    public Location getCurrentLocation();
    
    /**
     * Tries to reverse geocode a human-readable address for the given location.
     * @param location Location to reverse geocode, @see {@link Location}.
     * @param listener Listener to call when reverse geocoding is ready, @see {@link ReverseGeocodingResultListener}.
     */
    public void reverseGeocodeLocation(Location location, final ReverseGeocodingResultListener listener);

}