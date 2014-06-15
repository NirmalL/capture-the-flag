/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.location.here;

import android.app.Activity;
import android.location.Location;

import com.here.android.common.GeoCoordinate;
import com.here.android.common.GeoPosition;
import com.here.android.common.LocationMethod;
import com.here.android.common.LocationStatus;
import com.here.android.common.PositionListener;
import com.here.android.common.PositioningManager;
import com.here.android.mapping.FactoryInitListener;
import com.here.android.mapping.InitError;
import com.here.android.mapping.MapFactory;
import com.here.android.search.ErrorCode;
import com.here.android.search.ResultListener;
import com.here.android.search.Address;
import com.here.android.search.geocoder.ReverseGeocodeRequest;
import com.nokia.example.capturetheflag.location.LocationManagerBase;
import com.nokia.example.capturetheflag.location.LocationManagerInterface;

/**
 * Location Manager implementation.
 * <p/>
 * Implementation of {@link LocationManagerInterface} that uses Here APIs, i.e {@link PositioningManager}.
 *
 * @see {@link LocationManagerInterface}, {@link LocationManagerBase} and {@link PositionListener}.
 */
public class LocationManagerHere extends LocationManagerBase implements PositionListener {

    private PositioningManager mPosManager;

    public LocationManagerHere(Activity activity) {
        super();
        MapFactory.initFactory(activity.getApplicationContext(), new FactoryInitListener() {

            @Override
            public void onFactoryInitializationCompleted(InitError error) {
                mPosManager = MapFactory.getPositioningManager();
                mPosManager.addPositionListener(LocationManagerHere.this);
                notifyManagerReady(error == InitError.NONE);
            }
        });
    }

    @Override
    public boolean isLocationAvailable() {
        LocationStatus status = mPosManager.getLocationStatus(LocationMethod.GPS);
        return status != LocationStatus.OUT_OF_SERVICE;
    }

    @Override
    public void onPositionFixChanged(LocationMethod arg0, LocationStatus arg1) {
        // Not implemented        
    }

    @Override
    public void onPositionUpdated(LocationMethod arg0, GeoPosition arg1) {
        // Convert GeoPosition to a more generic Location instance
        Location location = new Location("");
        GeoCoordinate coords = arg1.getCoordinate();
        location.setLatitude(coords.getLatitude());
        location.setLongitude(coords.getLongitude());

        notifyListener(location);
    }

    @Override
    public void start() {
        if (mPosManager != null) {
            mPosManager.start(LocationMethod.GPS_NETWORK);
        }
    }

    @Override
    public void stop() {
        if (mPosManager != null) {
            mPosManager.stop();
        }
    }

    @Override
    public Location getCurrentLocation() {
        GeoCoordinate coords = mPosManager.getPosition().getCoordinate();
        Location location = new Location("");
        location.setLatitude(coords.getLatitude());
        location.setLongitude(coords.getLongitude());
        return location;
    }

    @Override
    public void reverseGeocodeLocation(Location location, final ReverseGeocodingResultListener listener) {
        GeoCoordinate coords = MapFactory.createGeoCoordinate(location.getLatitude(), location.getLongitude());
        ReverseGeocodeRequest request = MapFactory.getGeocoder().createReverseGeocodeRequest(coords);
        request.execute(new ResultListener<Address>() {
            public void onCompleted(Address data, com.here.android.search.ErrorCode error) {
                String result = null;
                if (error != ErrorCode.NONE) {
                    result = "Unable to find location";
                } else {
                    result = new String(data.getStreet() + " " + data.getHouseNumber()).trim();
                }
                if (listener != null) {
                    listener.onReverseGeocodingResult(result);
                }
            }

            ;
        });
    }
}
