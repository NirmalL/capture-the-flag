/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.DisplayMetrics;

import com.nokia.example.capturetheflag.R;
import com.nokia.example.capturetheflag.Settings;
import com.nokia.example.capturetheflag.network.model.Player;

/**
 * Base class for Here and Google-specific map marker factory classes.
 */
public class MarkerFactoryBase {

    private static final int PLAYER_NAME_MAX_LENGTH = 10;
    private static final int TEXT_MARGIN = 6;
    protected static final int PLAYER_NAME_SIZE = 15; // In dp

    /**
     * Creates and returns a bitmap for player marker.
     *
     * @param player  {@link Player}.
     * @param metrics {@link DisplayMetrics} for marker bitmap size calculation.
     * @param res     {@link Resources} to use.
     * @return {@link Bitmap} {@link Player} marker bitmap.
     */
    static protected Bitmap getBitmapForPlayer(final Player player, DisplayMetrics metrics, Resources res) {
        Bitmap base = null;

        if (player.getTeam().equals(Player.BLUE)) {
            base = BitmapFactory.decodeResource(res, R.drawable.playername);
        } else {
            base = BitmapFactory.decodeResource(res, R.drawable.playername_red);
        }

        int y = dpToPx(5, metrics);
        int textSize = (int) (PLAYER_NAME_SIZE * metrics.density + 0.5f);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Style.FILL);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setTextSize(textSize);

        String name = player.getName();

        if (name.length() > PLAYER_NAME_MAX_LENGTH) {
            name = name.substring(0, PLAYER_NAME_MAX_LENGTH);
            name += "...";
        }

        Bitmap img = Bitmap.createBitmap(base.getWidth(), base.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(img);
        canvas.drawBitmap(base, 0, 0, null);
        paint.setColor(Color.WHITE);
        canvas.drawText(name, TEXT_MARGIN, y + textSize, paint);

        return img;
    }

    /**
     * Calculates marker size for given {@link DisplayMetrics} and meters per pixel.
     *
     * @param displayMetrics         {@link DisplayMetrics}.
     * @param currentMetersPerPixels Meters per pixel.
     * @return Marker size in pixels.
     */
    public static int calculateMarkerSize(DisplayMetrics displayMetrics, double currentMetersPerPixels) {
        int size = (int) (Settings.BASE_SIZE / currentMetersPerPixels);
        int minimumSize = dpToPx(Settings.MINIMUM_MARKER_SIZE, displayMetrics);

        if (size < minimumSize) {
            size = minimumSize;
        }

        return size;
    }

    /**
     * Converts given density-independent pixel value to pixel value value using
     * density from given display metrics.
     *
     * @param dp      The device-independent pixel value to convert.
     * @param metrics {@link DisplayMetrics} to use for density.
     * @return Converted pixel value.
     */
    protected static int dpToPx(double dp, DisplayMetrics metrics) {
        return (int) (dp * metrics.density + 0.5f);
    }

}
