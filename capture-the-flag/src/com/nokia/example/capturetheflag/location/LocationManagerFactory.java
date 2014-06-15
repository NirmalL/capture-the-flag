/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import java.lang.reflect.Constructor;

import android.app.Activity;

/**
 * Factory class for instantiating either Here or Google specific implementation
 * of the {@link LocationManagerInterface}.
 *
 * @see LocationManagerInterface.ReverseGeocodingResultListener
 */
public class LocationManagerFactory {
    private static final String HERE_POSITIONING_CLASS_NAME = "com.here.android.common.PositioningManager";
    private static final String HERE_LOCATION_CLASS_NAME = "com.nokia.example.capturetheflag.location.here.LocationManagerHere";
    private static final String GOOGLE_LOCATION_CLASS_NAME = "com.nokia.example.capturetheflag.location.google.LocationManagerGoogle";

    private static LocationManagerInterface mInstance;

    /**
     * Checks whether Here Positioning Services are supported on this device.
     *
     * @return <code>true</code> if Here Positioning Services are available, <code>false</code> if not.
     */
    public static boolean isHerePositioningAvailable() {
        boolean available = true;
        try {
            Class.forName(HERE_POSITIONING_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            available = false;
        }
        return available;
    }

    /**
     * Returns the Location Manager singleton instance.
     *
     * @param activity Activity
     * @return {@link LocationManagerInterface}.
     */
    public static LocationManagerInterface getInstance(Activity activity) {
        if (mInstance == null) {
            String className = isHerePositioningAvailable() ? HERE_LOCATION_CLASS_NAME : GOOGLE_LOCATION_CLASS_NAME;
            try {
                Constructor<?> constructor = Class.forName(className).getConstructor(Activity.class);
                mInstance = (LocationManagerInterface) constructor.newInstance(activity);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return mInstance;
    }
}
