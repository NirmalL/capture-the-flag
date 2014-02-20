/**
 * Copyright (c) 2014 Nokia Corporation.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.util.DisplayMetrics;

import com.here.android.common.GeoCoordinate;
import com.here.android.common.Image;
import com.here.android.mapping.MapFactory;
import com.here.android.mapping.MapMarker;
import com.nokia.example.capturetheflag.R;
import com.nokia.example.capturetheflag.network.model.Flag;
import com.nokia.example.capturetheflag.network.model.Player;

/**
 * Helper class to create different kind of map markers like player marker or
 * flag marker.
 */
public class MarkerFactory {
    private static final int PLAYER_NAME_MAX_LENGTH = 10;
    private static final int TEXT_MARGIN = 6;
    private static final int PLAYER_NAME_SIZE = 15; // In dp

    public static MapMarker createPlayerMarker(final Player player,
                                               DisplayMetrics metrics,
                                               Resources res)
    {
        Bitmap base = null;
        
        if (player.getTeam().equals(Player.BLUE)) {
            base = BitmapFactory.decodeResource(res, R.drawable.playername);
        }
        else {
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
        
        Bitmap img = Bitmap.createBitmap(
                base.getWidth(),base.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(img);
        canvas.drawBitmap(base, 0, 0, null);
        paint.setColor(Color.WHITE);
        canvas.drawText(name, TEXT_MARGIN, y + textSize, paint);
        
        Image icon = MapFactory.createImage();
        icon.setBitmap(img);
        
        GeoCoordinate coords = MapFactory.createGeoCoordinate(
                player.getLatitude(), player.getLongitude());
        MapMarker marker = MapFactory.createMapMarker(coords, icon);
        PointF anchor = new PointF(dpToPx(12, metrics), base.getHeight());
        marker.setAnchorPoint(anchor);
        
        return marker;
    }

    public static MapMarker createFlagMarker(Flag flag, Bitmap bitmap, int size) {
        GeoCoordinate coords = MapFactory.createGeoCoordinate(flag.getLatitude(), flag.getLongitude());
        Image image = MapFactory.createImage();
        image.setBitmap(Bitmap.createScaledBitmap(bitmap, size, size, true));
        MapMarker marker = MapFactory.createMapMarker(coords, image);
        return marker;
    }

    private static int dpToPx(double dp, DisplayMetrics metrics) {
        return (int) (dp * metrics.density + 0.5f);
    }
}
