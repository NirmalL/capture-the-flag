/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.map;

import android.location.Location;

import com.nokia.example.capturetheflag.map.google.GameMapGoogle;
import com.nokia.example.capturetheflag.map.here.GameMapHere;
import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.Player;

/**
 * Interface for accessing platform-specific Map User Interface.
 * 
 * Use {@link GameMapFactory} to instantiate a class that implements this interface.
 * @See {@link GameMapHere} and {@link GameMapGoogle} for the platform-specific implementations.
 *
 */
public interface GameMapInterface {

	/**
	 * Clears all markers from map.
	 */
    public void clearMarkers();

    /**
     * Updates existing player marker from new data.
     * @param updated Updated player data, @see {@link Player}.
     * @param old Old player data, @see {@link Player}.
     */
    public void updateMarkerForPlayer(Player updated, Player old);
    
    /**
     * Updates position of the given player's map marker.
     * @param updated, Player to update, @see {@link Player}.
     */
    public void updatePlayerMarkerPosition(Player updated);

    /**
     * Checks whether a map marker already exists for the given player.
     * @param player Player to check, @see {@link Player}.
     * @return <code>true</code> if a map marker exists, <code>false</code> if not.
     */
    public boolean playerHasMarker(Player player);

    /**
     * Sets up map markers for the given game, @see {@link Game}.
     * @param game The game to set up markers for, @see {@link Game}.
     */
    public void setMarkers(Game game);

    /**
     * Centers the game map to the given position and resets the map zoom level to default.
     * @param location Location to center the map to, @see {@link Location}.
     */
    public void centerMapToPosition(Location location);

}
