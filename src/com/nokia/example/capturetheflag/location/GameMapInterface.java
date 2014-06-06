/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.Player;

public interface GameMapInterface {

	public void clearMarkers();

	public void updateMarkerForPlayer(Player updated, Player old);
	
	public void updatePlayerMarkerPosition(Player updated);

	public boolean playerHasMarker(Player player);

	public void setMarkers(Game game, Player player);

	public void centerMapToUserPosition();

}
