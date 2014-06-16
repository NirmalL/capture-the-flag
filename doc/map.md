## Map

__<< TODO: Insert class diagram >>__

### Usage

The game map is created by `MainActivity` using `GameMapFactory`'s static `createGameMap(Context context)` method. If the factory function finds **HERE Maps** APIs on the device it creates a new `GameMapHere` instance for a **HERE Maps** specific map implementation and returns it. If **HERE Maps** are not found, an instance of **Google Maps Android API v2** specific implementation class `GameMapsGoogle` is returned instead.

Once the game map is instantiated, `MainActivity` sets it to the UI as a `Fragment` and also passes a reference to the `GameMapInterface` to the game's `Controller` instance. The `Controller` is then responsible for updating the map through the set of methods declared in `GameMapInterface` when the local player's location changes or updates for other player's positions are received from the game server.

### GameMapInterface

The map functionality of the game is used through the `GameMapInterface`. The interface declares the following public methods for controlling the map:

- `void centerMapToPosition(Location location)`

	Centers the game map to the coordinates defined by `location`.

- `void clearMarkers()`

	Clears all existing player and map markers from the map. Effectively prepares the map for a new game.

- `void updateMarkerForPlayer(Player updated, Player old)`

	Refreshes an existing (`old`) player marker with `updated` data.

- `void updatePlayerMarkerPosition(Player player)`

	Updates the position of an existing marker on the map for the given `player` or creates a new marker for the `player` if one doesn't already exist.

- `boolean playerHasMarker(Player player)`

	Returns `true` if `player` already has a marker on the map.

- `void setMarkers(Game game)`

	Sets up markers for the given `game`. Creates map markers for all the players and flags for both teams participating in the `game`.
	
### Implementation

The `GameMapHere` and `GameMapGoogle` classes along with the respective `MarkerFactoryHere` and `MarkerFactoryGoogle` classes contain all the platform-specific map implementations for **HERE Maps** on **Nokia X** devices and **Google Maps** (API v2) on **Android** devices.

The basic functionality and logic of both map implementations is quite identical, the main difference being the names of the platform-specific classes and interfaces used in the implementations and e.g. how the map markers are created and added to and removed from the map on the two platforms.

The `GameMapHere` implementation uses `com.here.android.mapping.Map` as the main map control where `GameMapGoogle` uses `com.google.android.gms.maps.GoogleMaps`. Also, both map implementations extend a different, platform-specific `MapFragment` implementations that extend the `android.app.Fragment` base class.
  
Both game maps use a private `HashMap` for keeping track of the player markers (`com.here.android.mapping.MapMarker` for **HERE Maps** and `com.google.android.gms.maps.model.Marker` and `MarkerOptions` for **Google Maps**) on the map. The HashMap is updated when player markers are added to or removed from the game map.

`MarkerFactoryHere` and `MarkerFactoryGoogle` contain a set of static platform-specific utility methods for creating both player and flag markers.

Flag markers are handled separately from the player markers and their sizes scaled asyncronously based on the current zoom level of the map. The `GameMapUtils` class contains a utility method for calculating a meters-per-pixel value based on the current map zoom level which is used when scaling the flag markers..

### Links

- [HERE Maps documentation on developer.nokia.com](http://developer.nokia.com/resources/library/nokia-x/here-maps.html)
- [Google Maps Android API v2 documention on developers.google.com](https://developers.google.com/maps/documentation/android/)
