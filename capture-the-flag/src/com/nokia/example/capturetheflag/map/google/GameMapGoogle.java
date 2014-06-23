/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.map.google;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nokia.example.capturetheflag.R;
import com.nokia.example.capturetheflag.location.LocationManagerFactory;
import com.nokia.example.capturetheflag.map.GameMapInterface;
import com.nokia.example.capturetheflag.map.GameMapUtils;
import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.Player;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Google Maps specific {@link Fragment} that extends {@link MapFragment} and
 * is responsible for showing the map and handle map related actions like adding
 * map markers etc.
 *
 * @see GameMapInterface.
 */
public class GameMapGoogle extends MapFragment implements GameMapInterface, OnCameraChangeListener {
    private static final float DEFAULT_MAP_ZOOM_LEVEL_IN_GAME = 14;
    private static final String TAG = "CtF/GameMapGoogle";

    private GoogleMap mMap;

    private HashMap<Integer, Marker> mPlayerMarkers = new HashMap<Integer, Marker>();
    private Marker mRedFlag;
    private Marker mBlueFlag;
    private Bitmap mRedFlagBitmap;
    private Bitmap mBlueFlagBitmap;
    private HandlerThread mScaleThread;
    private Handler mScaleHandler;
    private Handler mUIHandler;

    private float mZoomLevel = -1.0f;
    private double mCurrentMetersPerPixels = 1;
    private boolean mIsFirstTime = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mIsFirstTime = true;
        }

        mRedFlagBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.base_red);
        mBlueFlagBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.base_blue);
        mScaleThread = new HandlerThread("ScaleThread");
        mScaleThread.start();
        mScaleHandler = new Handler(mScaleThread.getLooper());
        mUIHandler = new Handler();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMap = getMap();
        mMap.setOnCameraChangeListener(this);

        if (mIsFirstTime) {
            // Set up defaults

            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(GameMapUtils.DEFAULT_LATITUDE, GameMapUtils.DEFAULT_LONGITUDE)));

            if (mZoomLevel > 0) {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(mZoomLevel));
            } else {
                mMap.animateCamera(CameraUpdateFactory.zoomTo((mMap.getMinZoomLevel() + mMap.getMaxZoomLevel()) / 2));
            }
        }
    }

    @Override
    public void clearMarkers() {
        removePlayerMarkers();
        mRedFlag.remove();
        mBlueFlag.remove();
    }

    @Override
    public void updateMarkerForPlayer(Player updated, Player old) {
        Marker marker = getPlayerMarker(old);
        mPlayerMarkers.put(updated.getId(), marker);
        mPlayerMarkers.remove(old);
    }

    @Override
    public void updatePlayerMarkerPosition(Player player) {
        Log.d(TAG, "Updating player with ID " + player.getId());

        if (!playerHasMarker(player)) {
            // New player joined
            Log.d(TAG, "Adding new player with name " + player.getName());
            MarkerOptions marker = MarkerFactoryGoogle.createPlayerMarker(player, getResources().getDisplayMetrics(), getResources());
            addPlayerMarker(player, marker);
        } else {
            Log.d(TAG, "Updating marker of existing player with name " + player.getName() + "pos: " + player.getLatitude() + ", " + player.getLongitude());
            getPlayerMarker(player).setPosition(new LatLng(player.getLatitude(), player.getLongitude()));
        }
    }

    @Override
    public boolean playerHasMarker(Player player) {
        return mPlayerMarkers.containsKey(player.getId());
    }

    @Override
    public void setMarkers(Game game) {
        ArrayList<Player> players = game.getPlayers();

        for (Player player : players) {
            Log.d(TAG, "Adding marker to: " + player.getLatitude() + "; " + player.getLongitude() + ", name: " + player.getName() + ", id: " + player.getId());
            MarkerOptions marker = MarkerFactoryGoogle.createPlayerMarker(player, getResources().getDisplayMetrics(), getResources());
            addPlayerMarker(player, marker);
        }

        // Flag markers
        final int markerSize = MarkerFactoryGoogle.calculateMarkerSize(getActivity().getResources().getDisplayMetrics(), mCurrentMetersPerPixels);
        mRedFlag = mMap.addMarker(MarkerFactoryGoogle.createFlagMarker(game.getRedFlag(), mRedFlagBitmap, markerSize));
        mBlueFlag = mMap.addMarker(MarkerFactoryGoogle.createFlagMarker(game.getBlueFlag(), mBlueFlagBitmap, markerSize));

        updateMetersPerPixel();
    }

    @Override
    public void centerMapToPosition(Location location) {
        LatLng lat = new LatLng(location.getLatitude(), location.getLongitude());
        mZoomLevel = DEFAULT_MAP_ZOOM_LEVEL_IN_GAME;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lat, mZoomLevel));
        updateMetersPerPixel();
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        final float level = position.zoom;

        if (level != mZoomLevel) {
            updateMetersPerPixel();
            scaleMarkers();
            mZoomLevel = level;
        }
    }

    /**
     * Adds {@link Player} {@link Marker} to the map and stores references to
     * the {@link Player} and the created {@link Marker} in a {@link HashMap}.
     *
     * @param player {@link Player} for which to add the created {@link Marker}.
     * @param marker {@link MarkerOptions} to be used for the {@link Marker}.
     */
    private void addPlayerMarker(Player player, MarkerOptions marker) {
        Marker m = mMap.addMarker(marker);
        Log.d(TAG, "Added marker:" + m.getPosition().latitude + ", " + m.getPosition().longitude);
        mPlayerMarkers.put(player.getId(), m);
    }

    /**
     * Returns a map {@link Marker} for the given {@link Player} from the {@link HashMap}.
     *
     * @param player {@link Player} for which to return the {@link Marker} for.
     * @return {@link Marker} for the given {@link Player}.
     */
    private Marker getPlayerMarker(Player player) {
        return mPlayerMarkers.get(player.getId());
    }

    /**
     * Removes all {@link Player} map {@link Marker} objects from the map and
     * the {@link HashMap}.
     */
    private void removePlayerMarkers() {
        for (Marker marker : mPlayerMarkers.values()) {
            marker.remove();
        }
        mPlayerMarkers.clear();
    }

    /**
     * Updates the current meters per pixel value based on the current
     * {@link Location} and map zoom level.
     */
    private void updateMetersPerPixel() {
        Location location = LocationManagerFactory.getInstance(getActivity()).getCurrentLocation();
        
        if (location != null) {
            mCurrentMetersPerPixels = GameMapUtils.calculateMetersPerPixel(location, mZoomLevel);
        }
        else {
            Log.d(TAG, "No location!");
        }
    }

    /**
     * Scales the map markers asynchronously.
     */
    private void scaleMarkers() {
        if (mRedFlag != null && mBlueFlag != null) {
            mScaleHandler.post(new Runnable() {
                @Override
                public void run() {
                    final int markerSize = MarkerFactoryGoogle.calculateMarkerSize(getActivity().getResources().getDisplayMetrics(), mCurrentMetersPerPixels);
                    final BitmapDescriptor redFlagBitmapDesc = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(mRedFlagBitmap, markerSize, markerSize, true));
                    final BitmapDescriptor blueFlagBitmapDesc = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(mBlueFlagBitmap, markerSize, markerSize, true));

                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mRedFlag != null && mBlueFlag != null) {
                                mRedFlag.setIcon(redFlagBitmapDesc);
                                mBlueFlag.setIcon(blueFlagBitmapDesc);
                            }
                        }
                    });
                }
            });
        }
    }
}
