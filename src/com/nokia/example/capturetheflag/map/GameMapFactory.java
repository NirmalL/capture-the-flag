/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.map;


/**
 * Factory class for instantiating either Here or Google Maps UI map fragment.
 * @see GameMapInterface.
 */
public class GameMapFactory {
	
    private static final String HERE_MAP_CLASS_NAME = "com.here.android.mapping.Map";
    private static final String HERE_GAME_MAP_CLASS_NAME = "com.nokia.example.capturetheflag.location.here.GameMapHere";
    private static final String GOOGLE_GAME_MAP_CLASS_NAME = "com.nokia.example.capturetheflag.location.google.GameMapGoogle";    
    
    /**
     * Are Here Maps supported in this device
     * @return    <code>true</code> if Here Maps are available, <code>false</code> if not.
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
        String className = isHereMapsAvailable() ? HERE_GAME_MAP_CLASS_NAME : GOOGLE_GAME_MAP_CLASS_NAME;

        try {
			map = (GameMapInterface)Class.forName(className).newInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return map;
    }
}
