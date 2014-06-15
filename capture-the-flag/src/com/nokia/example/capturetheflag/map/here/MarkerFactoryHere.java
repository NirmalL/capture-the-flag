/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.map.here;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.DisplayMetrics;

import com.here.android.common.GeoCoordinate;
import com.here.android.common.Image;
import com.here.android.mapping.MapFactory;
import com.here.android.mapping.MapMarker;
import com.nokia.example.capturetheflag.map.MarkerFactoryBase;
import com.nokia.example.capturetheflag.network.model.Flag;
import com.nokia.example.capturetheflag.network.model.Player;

/**
 * Helper class using Here APIs for creating map markers, e.g. player or flag marker.
 */
public class MarkerFactoryHere extends MarkerFactoryBase {

    /**
     * Creates a player marker.
     *
     * @param player  Player data, @see {@link Player}.
     * @param metrics Display metrics to use for size calculations.
     * @param res     Resources to use.
     * @return Map marker for the given player, @see {@link MapMarker}.
     */
    public static MapMarker createPlayerMarker(final Player player, DisplayMetrics metrics, Resources res) {

        Bitmap bitmap = getBitmapForPlayer(player, metrics, res);
        Image icon = MapFactory.createImage();
        icon.setBitmap(bitmap);

        GeoCoordinate coords = MapFactory.createGeoCoordinate(
                player.getLatitude(), player.getLongitude());
        MapMarker marker = MapFactory.createMapMarker(coords, icon);
        PointF anchor = new PointF(dpToPx(12, metrics), bitmap.getHeight());
        marker.setAnchorPoint(anchor);

        return marker;
    }

    /**
     * Creates a flag marker.
     *
     * @param flag   Flag data, @see {@link Flag}.
     * @param Bitmap Bitmap to use, @see {@link Bitmap}.
     * @param size   Marker size.
     * @return Flag map marker, @see {@link MapMarker}.
     */
    public static MapMarker createFlagMarker(Flag flag, Bitmap bitmap, int size) {
        GeoCoordinate coords = MapFactory.createGeoCoordinate(flag.getLatitude(), flag.getLongitude());
        Image image = MapFactory.createImage();
        image.setBitmap(Bitmap.createScaledBitmap(bitmap, size, size, true));
        MapMarker marker = MapFactory.createMapMarker(coords, image);
        return marker;
    }
}
