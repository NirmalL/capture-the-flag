/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import android.util.Log;

import com.here.android.common.GeoCoordinate;

/**
 * See http://www.movable-type.co.uk/scripts/latlong.html for the basis of the
 * algorithms implemented by this class.
 */
public class LocationUtils {
    private static final String TAG = "CtF/LocationUtils";
    private static final double EARTH_RADIUS_IN_METERS = 6371000.0;

    /**
     * Calculates the target coordinate from the given coordinate with the given
     * bearing (in radians) and the distance (in meters).
     *
     * @param fromLatitudeInRads  Source latitude in radians.
     * @param fromLongitudeInRads Source longitude in radians.
     * @param toBearingInRads     The bearing in radians.
     * @param distanceInMeters
     * @return The target coordinates in degrees.
     */
    public static double[] calculateTargetCoordinate(
            final double fromLatitudeInRads, final double fromLongitudeInRads,
            final double toBearingInRads, final double distanceInMeters) {
        Log.d(TAG,
                "From (" + Math.toDegrees(fromLatitudeInRads) + ", "
                        + Math.toDegrees(fromLongitudeInRads)
                        + ") with bearing " + Math.toDegrees(toBearingInRads)
                        + " (degrees) distance of " + distanceInMeters
                        + " meters");

        final double distance = distanceInMeters / EARTH_RADIUS_IN_METERS;

        double toLatitude = Math.asin(Math.sin(fromLatitudeInRads)
                * Math.cos(distance) + Math.cos(fromLatitudeInRads)
                * Math.sin(distance) * Math.cos(toBearingInRads));

        final double a = Math.atan2(
                Math.sin(toBearingInRads) * Math.sin(distance)
                        * Math.cos(fromLatitudeInRads), Math.cos(distance)
                        - Math.sin(fromLatitudeInRads) * Math.sin(toLatitude));
        double toLongitude = fromLongitudeInRads + a;
        toLongitude = (toLongitude + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        toLatitude = Math.toDegrees(toLatitude);
        toLongitude = Math.toDegrees(toLongitude);

        return new double[]{toLatitude, toLongitude};
    }

    /**
     * For convenience.
     */
    public static double[] calculateTargetCoordinate(
            GeoCoordinate fromCoordinate, final double toBearingInRads,
            final double distanceInMeters) {
        final double fromLatitudeInRads = Math.toRadians(fromCoordinate
                .getLatitude());
        final double fromLongitudeInRads = Math.toRadians(fromCoordinate
                .getLongitude());
        return calculateTargetCoordinate(fromLatitudeInRads,
                fromLongitudeInRads, toBearingInRads, distanceInMeters);
    }

    /**
     * For convenience.
     */
    public static double[] calculateTargetCoordinateInputInDegrees(
            final double fromLatitude, final double fromLongitude,
            final double toBearingInDegrees, final double distanceInMeters) {
        return calculateTargetCoordinate(Math.toRadians(fromLatitude),
                Math.toRadians(fromLongitude),
                Math.toRadians(toBearingInDegrees), distanceInMeters);
    }

    /**
     * Calculates the distance (in meters) between the two given coordinates.
     *
     * @param latitude1InRads
     * @param longitude1InRads
     * @param latitude2InRads
     * @param longitude2InRads
     * @return The distance between the two given points in meters.
     */
    public static double calculateDistanceInMeters(
            final double latitude1InRads, final double longitude1InRads,
            final double latitude2InRads, final double longitude2InRads) {
        double latitudeDelta = latitude2InRads - latitude1InRads;
        double longitudeDelta = longitude2InRads - longitude1InRads;

        double a = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
                + Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2)
                * Math.cos(latitude1InRads) * Math.cos(latitude2InRads);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_IN_METERS * c;
    }

    /**
     * For convenience.
     */
    public static double calculateDistanceInMetersInputInDegrees(
            final double latitude1, final double longitude1,
            final double latitude2, final double longitude2) {
        return calculateDistanceInMeters(Math.toRadians(latitude1),
                Math.toRadians(longitude1), Math.toRadians(latitude2),
                Math.toRadians(longitude2));
    }

    /**
     * Calculates the bearing (in radians) from the source coordinate to the
     * destination coordinate.
     *
     * @param fromLatitudeInRads
     * @param fromLongitudeInRads
     * @param toLatitudeInRads
     * @param toLongitudeInRads
     * @return The bearing in radians.
     */
    public static double calculateBearing(final double fromLatitudeInRads,
                                          final double fromLongitudeInRads, final double toLatitudeInRads,
                                          final double toLongitudeInRads) {
        final double latitudeDelta = toLatitudeInRads - fromLatitudeInRads;
        final double longitudeDelta = toLongitudeInRads - fromLongitudeInRads;

        final double y = Math.sin(longitudeDelta) * Math.cos(latitudeDelta);
        final double x = Math.cos(fromLatitudeInRads)
                * Math.sin(toLatitudeInRads) - Math.sin(fromLatitudeInRads)
                * Math.cos(toLatitudeInRads) * Math.cos(longitudeDelta);

        return Math.atan2(y, x);
    }

    /**
     * For convenience.
     */
    public static double calculateBearing(GeoCoordinate from, GeoCoordinate to) {
        final double fromLatitudeInRads = Math.toRadians(from.getLatitude());
        final double fromLongitudeInRads = Math.toRadians(from.getLongitude());
        final double toLatitudeInRads = Math.toRadians(to.getLatitude());
        final double toLongitudeInRads = Math.toRadians(to.getLongitude());
        return calculateBearing(fromLatitudeInRads, fromLongitudeInRads,
                toLatitudeInRads, toLongitudeInRads);
    }

    /**
     * For convenience. The input is expected in degrees, but note that the
     * output (bearing) is in radians.
     */
    public static double calculateBearingInputInDegrees(
            final double fromLatitudeInDegs, final double fromLongitudeInDegs,
            final double toLatitudeInDegs, final double toLongitudeInDegs) {
        return calculateBearing(Math.toRadians(fromLatitudeInDegs),
                Math.toRadians(fromLongitudeInDegs),
                Math.toRadians(toLatitudeInDegs),
                Math.toRadians(toLongitudeInDegs));
    }
}
