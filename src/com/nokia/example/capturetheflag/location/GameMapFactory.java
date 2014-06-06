/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import com.nokia.example.capturetheflag.location.google.GameMapGoogle;
import com.nokia.example.capturetheflag.location.here.GameMapHere;

import android.util.Log;

/**
 * Factory class for instantiating either Here or Google Maps UI map fragment.
 * @see GameMapInterface.
 */
public class GameMapFactory {
	private static final String HERE_MAP_CLASS_NAME = "com.here.android.mapping.Map";
	
	private static final String TAG = "CtF/MapFactory";

	/**
	 * Are Here Maps supported in this device
	 * @return	<code>true</code> if Here Maps are available, <code>false</code> if not.
	 */
	public static boolean isHereMapsAvailable() {
		boolean available = true;
		try {
			Class.forName(HERE_MAP_CLASS_NAME);
		} catch (ClassNotFoundException e) {
			available = false;
		}
		return available;
	}
	
	/**
	 * Returns a {@link GameMapInterface} implementation instance.
	 * @return {@link GameMapInterface}.
	 */
	public static GameMapInterface createGameMap() {
		GameMapInterface map = null;
        if(isHereMapsAvailable()) {
        	Log.d(TAG, "Using Here Maps");
            map = new GameMapHere();
        } else {
        	Log.d(TAG, "Using Google Maps");
        	map = new GameMapGoogle();
        }
        return map;
	}
}
