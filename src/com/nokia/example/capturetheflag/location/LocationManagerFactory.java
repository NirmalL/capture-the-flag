/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import com.nokia.example.capturetheflag.location.google.LocationManagerGoogle;
import com.nokia.example.capturetheflag.location.here.LocationManagerHere;

import android.app.Activity;
import android.util.Log;

public class LocationManagerFactory {
	private static final String HERE_POSITIONING_CLASS_NAME = "com.here.android.common.PositioningManager";
		
	private static final String TAG = "CtF/LocationManagerFactory";

	private static LocationManagerInterface mInstance;	
	
	/**
	 * Are Here Positioning Services supported in this device
	 * 
	 * @return	true if Here Positioning Services are available
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

	public static LocationManagerInterface getLocationManagerInterface(Activity activity) {
		if(mInstance == null) {
			if(isHerePositioningAvailable()) {
				Log.d(TAG, "Using Here for positioning");
				mInstance = new LocationManagerHere(activity);
			} else {
				Log.d(TAG, "Using Google for positioning");
				mInstance = new LocationManagerGoogle(activity);
			}
		}
		
		return mInstance;
	}
}
