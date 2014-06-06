/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.location.google;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.nokia.example.capturetheflag.location.LocationManagerBase;

/**
 * Location Manager implementation that uses Google Play Services and Google-specific APIs, i.e. {@link LocationClient}.
 *
 */
public class LocationManagerGoogle extends LocationManagerBase implements
	GooglePlayServicesClient.ConnectionCallbacks, 
	GooglePlayServicesClient.OnConnectionFailedListener, 
	LocationListener {
	
	private static final String TAG ="CtF/LocationManagerGoogle";

	// Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;	
	
    private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	
	public LocationManagerGoogle(Activity activity) {
		super();
        mLocationClient = new LocationClient(activity.getApplicationContext(), this, this);
        mLocationClient.connect();
	}
	
	@Override
	public boolean isLocationAvailable() {
		return true;
	}

	@Override
	public void start() {
		if (mLocationClient.isConnected()) {
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
		}
	}

	@Override
	public void stop() {
		mLocationClient.removeLocationUpdates(this);
	}

	@Override
	public Location getCurrentLocation() {
		return mLocationClient.getLastLocation();
	}

	@Override
	public void onLocationChanged(Location location) {
		notifyListeners(location);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Connecting to LocationClient failed, notify the listeners.
		Log.d(TAG, "Connection failed!");
        notifyManagerReady(false);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "Connected!");

		// Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        
		// Connecting to LocationClient succeeded, notify the listeners.
        notifyManagerReady(true);
	}

	@Override
	public void onDisconnected() {
		// Not implemented
	}

}
