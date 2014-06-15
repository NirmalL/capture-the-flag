/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Object representing a flag on the map.
 */
public class Flag {
    public static final int RED = 0;
    public static final int BLUE = 1;

    private double mLatitude;
    private double mLongitude;

    public Flag(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put(ModelConstants.LATITUDE_KEY, mLatitude);
            obj.put(ModelConstants.LONGITUDE_KEY, mLongitude);
        } catch (JSONException e) {
            Log.e("Flag", "error in toJSON", e);
        }

        return obj;
    }
}
