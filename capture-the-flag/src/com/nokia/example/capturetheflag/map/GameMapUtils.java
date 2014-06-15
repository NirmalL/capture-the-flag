/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.map;

import android.location.Location;

/**
 * Map-related constants and utility functions.
 */
public class GameMapUtils {

    public static final double DEGREES_TO_RADS = Math.PI / 180;
    public static final double CALC_CONSTANT = 2 * Math.PI * 6378137;
    public static final double DEFAULT_LATITUDE = 61.497885;
    public static final double DEFAULT_LONGITUDE = 23.770037;

    /**
     * Calculates meters per pixel value based on the latitude of the given
     * {@link Location} and map zoom level.
     *
     * @param location  {@link Location} for latitude.
     * @param zoomLevel Map zoom level.
     * @return Meters per pixel.
     */
    public static double calculateMetersPerPixel(Location location, double zoomLevel) {
        return (Math.cos(location.getLatitude() * GameMapUtils.DEGREES_TO_RADS) * GameMapUtils.CALC_CONSTANT) / (256 * Math.pow(2, zoomLevel));
    }
}
