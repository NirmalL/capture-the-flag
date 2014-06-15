/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.map;

import android.location.Location;

import com.nokia.example.capturetheflag.map.google.GameMapGoogle;
import com.nokia.example.capturetheflag.map.here.GameMapHere;
import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.Player;

/**
 * Interface for accessing platform-specific Map User Interface.
 * <p/>
 * Use {@link GameMapFactory} to instantiate a class that implements this interface.
 *
 * @See {@link GameMapHere} and {@link GameMapGoogle} for the platform-specific implementations.
 */
public interface GameMapInterface {

    /**
     * Clears all the markers from the game map.
     */
    public void clearMarkers();

    /**
     * Updates existing player marker from new data.
     *
     * @param updated Updated {@link Player} data.
     * @param old     Old {@link Player} data.
     */
    public void updateMarkerForPlayer(Player updated, Player old);

    /**
     * Updates position of the given player's map marker.
     *
     * @param updated, {@link Player} to update.
     */
    public void updatePlayerMarkerPosition(Player updated);

    /**
     * Checks whether a map marker already exists for the given player.
     *
     * @param player {@link Player} to check.
     * @return <code>true</code> if a map marker exists, <code>false</code> if not.
     */
    public boolean playerHasMarker(Player player);

    /**
     * Sets up map markers for the given {@link Game}.
     *
     * @param game The {@link Game} to set up markers for.
     */
    public void setMarkers(Game game);

    /**
     * Centers the game map to the given {@link Location} and resets the map
     * zoom level to default.
     *
     * @param location {@link Location} to center the map to.
     */
    public void centerMapToPosition(Location location);

}
