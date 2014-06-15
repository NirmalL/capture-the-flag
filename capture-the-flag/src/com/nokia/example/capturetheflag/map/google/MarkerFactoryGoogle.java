/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.map.google;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nokia.example.capturetheflag.map.MarkerFactoryBase;
import com.nokia.example.capturetheflag.network.model.Flag;
import com.nokia.example.capturetheflag.network.model.Player;

/**
 * Helper class using Google APIs for creating map markers, e.g. player or flag marker.
 */
public class MarkerFactoryGoogle extends MarkerFactoryBase {

    private static final float ANCHOR_U = 1.0f - (12.0f / (float) PLAYER_NAME_SIZE);
    private static final float ANCHOR_V = 1.0f;

    /**
     * Creates and returns a {@link MarkerOptions} instance for a player marker
     * to be used for creating a new marker on the Google map.
     *
     * @param player  Player data, @see {@link Player}.
     * @param metrics Display metrics to use for size calculations.
     * @param res     Resources to use.
     * @return Map marker options for the player marker, @see {@link MarkerOptions}.
     */
    public static MarkerOptions createPlayerMarker(final Player player, DisplayMetrics metrics, Resources res) {
        Bitmap bitmap = getBitmapForPlayer(player, metrics, res);
        BitmapDescriptor bmap = BitmapDescriptorFactory.fromBitmap(bitmap);
        MarkerOptions marker = new MarkerOptions().position(new LatLng(player.getLatitude(), player.getLongitude())).icon(bmap).anchor(ANCHOR_U, ANCHOR_V);

        return marker;
    }

    /**
     * Creates and returns a {@link MarkerOptions} instance for a flag marker
     * to be used for creating a new marker on the Google map.
     *
     * @param player  Player data, @see {@link Player}.
     * @param metrics Display metrics to use for size calculations.
     * @param res     Resources to use.
     * @return Map marker options for the player marker, @see {@link MarkerOptions}.
     */
    public static MarkerOptions createFlagMarker(Flag flag, Bitmap bitmap, int size) {
        BitmapDescriptor bmap = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, size, size, true));
        MarkerOptions marker = new MarkerOptions().position(new LatLng(flag.getLatitude(), flag.getLongitude())).icon(bmap);
        return marker;
    }
}
