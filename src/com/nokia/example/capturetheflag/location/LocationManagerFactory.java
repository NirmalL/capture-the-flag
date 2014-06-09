/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import java.lang.reflect.Constructor;

import android.app.Activity;

/**
 * Factory class for instantiating either Here or Google specific implementation
 * of the {@link LocationManagerInterface}.
 * 
 * @see LocationManagerInterface.ReverseGeocodingResultListener
 * 
 */
public class LocationManagerFactory {
    private static final String HERE_POSITIONING_CLASS_NAME = "com.here.android.common.PositioningManager";
	private static final String HERE_LOCATION_CLASS_NAME = "com.nokia.example.capturetheflag.location.here.LocationManagerHere";
	private static final String GOOGLE_LOCATION_CLASS_NAME = "com.nokia.example.capturetheflag.location.google.LocationManagerGoogle";

    private static final String TAG = "CtF/LocationManagerFactory";

    private static LocationManagerInterface mInstance;

    /**
     * Are Here Positioning Services supported in this device
     * 
     * @return true if Here Positioning Services are available
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

    public static LocationManagerInterface getLocationManagerInterface(
            Activity activity) {
        if (mInstance == null) {
        	
            String className = isHerePositioningAvailable() ? HERE_LOCATION_CLASS_NAME : GOOGLE_LOCATION_CLASS_NAME;
            
            try {
                Constructor<?> constructor = Class.forName(className).getConstructor(Activity.class);
                mInstance = (LocationManagerInterface)constructor.newInstance(activity);
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }

        return mInstance;
    }
}
