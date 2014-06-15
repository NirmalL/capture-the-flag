/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.map;

import android.util.Log;

/**
 * Factory class for instantiating either Here or Google Maps UI map fragment.
 *
 * @see GameMapInterface.
 */
public class GameMapFactory {

    private static final String TAG = "CtF/GameMapFactory";
    private static final String HERE_MAP_CLASS_NAME = "com.here.android.mapping.Map";
    private static final String HERE_GAME_MAP_CLASS_NAME = "com.nokia.example.capturetheflag.map.here.GameMapHere";
    private static final String GOOGLE_GAME_MAP_CLASS_NAME = "com.nokia.example.capturetheflag.map.google.GameMapGoogle";

    /**
     * Checks whether or not HERE Maps is supported on this device.
     *
     * @return <code>true</code> if Here Maps are available, <code>false</code> if not.
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
     *
     * @return {@link GameMapInterface}.
     */
    public static GameMapInterface createGameMap() {
        GameMapInterface map = null;
        String className = isHereMapsAvailable() ? HERE_GAME_MAP_CLASS_NAME : GOOGLE_GAME_MAP_CLASS_NAME;

        try {
            map = (GameMapInterface) Class.forName(className).newInstance();
        } catch (Exception e) {
            Log.e(TAG + ".createGameMap()", "Failed to construct a game map instance: " + e.getMessage(), e);
        }

        return map;
    }
}
