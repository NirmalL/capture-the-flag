/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.location.google;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.nokia.example.capturetheflag.location.LocationManagerBase;
import com.nokia.example.capturetheflag.location.LocationManagerInterface;
import com.nokia.example.capturetheflag.location.LocationManagerListener;

import java.io.IOException;
import java.util.List;

/**
 * Location Manager implementation.
 * <p/>
 * Implementation of {@link LocationManagerInterface} that uses Google and
 * Google Play Services APIs, i.e. {@link LocationClient} and {@link Geocoder}.
 *
 * @see {@link LocationManagerInterface}, {@link LocationManagerListener}, {@link LocationClient} and {@link LocationListener}.
 */
public class LocationManagerGoogle extends LocationManagerBase implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "CtF/LocationManagerGoogle";

    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    private Geocoder mGeoCoder;

    /**
     * Constructor.
     *
     * @param activity Activity.
     */
    public LocationManagerGoogle(Activity activity) {
        super();
        mLocationClient = new LocationClient(activity.getApplicationContext(), this, this);
        mGeoCoder = new Geocoder(activity.getApplicationContext());

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
        notifyListener(location);
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

        // Create the LocationRequest object with desired accuracy and interval for updates.
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

    @Override
    public void reverseGeocodeLocation(Location location, final ReverseGeocodingResultListener listener) {

        List<Address> matches = null;
        try {
            matches = mGeoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            Log.d(TAG, "IOException - GeoCoder not available!");
        }

        String result = null;

        // If any matches found, use the address from the first one.
        if (matches != null) {
            Address bestMatch = (matches.isEmpty() ? null : matches.get(0));
            if (bestMatch != null) {
                result = bestMatch.getAddressLine(0);
            }
        } else {
            result = "Unable to find location";
        }

        listener.onReverseGeocodingResult(result);
    }
}
