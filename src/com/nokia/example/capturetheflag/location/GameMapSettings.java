/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import android.location.Location;

public class GameMapSettings {

    public static final double DEGREES_TO_RADS = Math.PI / 180;
    public static final double CALC_CONSTANT = 2 * Math.PI * 6378137;
    public static final double DEFAULT_LATITUDE = 61.497885;
    public static final double DEFAULT_LONGITUDE = 23.770037;

    public static double calculateMetersPerPixel(Location location, double zoomLevel) {
        return (Math.cos(location.getLatitude() * GameMapSettings.DEGREES_TO_RADS) * GameMapSettings.CALC_CONSTANT) / (256 * Math.pow(2, zoomLevel));
    }

}
