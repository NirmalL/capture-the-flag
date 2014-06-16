## Location

__<< TODO: Insert class diagram >>__

### Usage

Location information is used by the game to update the local player's (device's) location on the game map and sending the location information to other players through the game server.

Location information is retrieved through `LocationManagerInterface` which encapsulates the use of platform-specific location information API. The `LocationManagerInterface` can be obtained by calling `LocationManagerFactory.getInstance(Context context)` which returns the interface for using a singleton instance of either `LocationManagerHere` or `LocationManagerGoogle` depending on the platform, **Nokia X** or **Android**, the application is running on.

### LocationManagerInterface

The `LocationManagerInterface` declares the following public methods:

- `boolean isLocationAvailable()`

	Returns `true` of location information is available.
	
- `void start()`

	Starts receiving location updates. The updates are notified to the currently set `LocationManagerListener`.
	
- `void stop()`

	Stops receiving location updates.

- `void setListener(LocationManagerListener listener)`

	Sets a `LocationManagerListener` for receiving location updates.

- `void removeListener()`

	Removes the currently set `LocationManagerListener`.
	
- `Location getCurrentLocation()`

	Returns the current `Location`.

- `void reverseGeocodeLocation(Location location, final ReverseGeocodingResultListener listener)`

	Makes reverse geocoding request for receiving human-readable address for the given `Location`. The result of the reverse geocoding operation is delivered to the given `ReverseGeocodingResultListener`.

and a `ReverseGeocodingResultListener` with the following public method for receiving reverse geocoding results:

- `void onReverseGeocodingResult(String result)`
 
	Address for the `Location` given to `reverseGeocodeLocation(...)`.

### LocationManagerListener interface

Location updates are delivered through the `LocationManagerListener` interface which declares the following public methods:

- `void onLocationManagerReady(boolean success)`

	Called when Location Manager has connected to the underlying location information provider or if the connection failed.

- `void onLocationUpdated(Location location)`

     Called when device's location has changed.

### Implementation

The platform-specific location manager implementations, `LocationManagerHere` for the **Nokia X** platform and `LocationManagerGoogle` for **Android** devices, both implement the `LocationManagerInterface` and extend the `LocationManagerBase` class. The base class contains listener-related functionality shared between the two implementations.

The application's `Controller`, in its `onCreate(Bundle savedInstance)` method,  sets itself as a `LocationManagerListener` and a stores a reference to the `LocationManagerInterface`. When the `onLocationUpdated(Location position)` method is called, the `Controller` updates the local player's new location to the game map and also sends it to the game server to be relayed to the other players in the game.

The `Controller` also starts and stops the receiving of location updates in its `onResume()` and `onPause()` methods, respectively.

The `LocationManagerInterface` is also used from within the `CreateGameFragment` and `JoinGameFragment` for sending the player's initial position to the game server when creating a new or joining an existing game. The platform-specific map implementations (`GameMapHere` and `GameMapGoogle`) also use the current location information from `LocationManager` for calculating the meters-per-pixel used in scaling the flag markers.

### Links
- [HERE Maps documentation on developer.nokia.com](http://developer.nokia.com/resources/library/nokia-x/here-maps.html)
- [Google Play Services Location APIs documentation on developer.android.com](http://developer.android.com/google/play-services/location.html)