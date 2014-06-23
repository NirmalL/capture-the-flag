/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.map.here;

import com.here.android.common.GeoCoordinate;
import com.here.android.common.Image;
import com.here.android.mapping.FragmentInitListener;
import com.here.android.mapping.InitError;
import com.here.android.mapping.Map;
import com.here.android.mapping.MapAnimation;
import com.here.android.mapping.MapFactory;
import com.here.android.mapping.MapFragment;
import com.here.android.mapping.MapMarker;
import com.nokia.example.capturetheflag.R;
import com.nokia.example.capturetheflag.location.LocationManagerFactory;
import com.nokia.example.capturetheflag.map.GameMapInterface;
import com.nokia.example.capturetheflag.map.GameMapUtils;
import com.nokia.example.capturetheflag.map.google.MarkerFactoryGoogle;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Here Maps specific {@link Fragment} that extends {@link MapFragment} and is
 * responsible for showing the map and handle map related actions like adding
 * map markers etc.
 *
 * @see GameMapInterface.
 */
public class GameMapHere extends MapFragment implements GameMapInterface {
    private static final double DEFAULT_MAP_ZOOM_LEVEL_IN_GAME = 14;
    private static final String TAG = "CtF/GameMapHere";

    private Map mMap;

    private HashMap<Integer, MapMarker> mPlayerMarkers = new HashMap<Integer, MapMarker>();
    private MapMarker mRedFlag = null;
    private MapMarker mBlueFlag = null;
    private Bitmap mRedFlagBitmap;
    private Bitmap mBlueFlagBitmap;
    private HandlerThread mScaleThread;
    private Handler mScaleHandler;
    private Handler mUIHandler;

    private double mZoomLevel = -1.0;
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
        Log.d(TAG, "onActivityCreated()");

        init(new FragmentInitListener() {
            @Override
            public void onFragmentInitializationCompleted(InitError error) {
                if (error == InitError.NONE) {
                    // Retrieve a reference of the map from the map fragment
                    mMap = getMap();

                    if (mIsFirstTime) {
                        mMap.setCenter(
                                MapFactory.createGeoCoordinate(
                                        GameMapUtils.DEFAULT_LATITUDE,
                                        GameMapUtils.DEFAULT_LONGITUDE),
                                MapAnimation.NONE);

                        if (mZoomLevel > 0) {
                            mMap.setZoomLevel(mZoomLevel);
                        } else {
                            mMap.setZoomLevel((mMap.getMinZoomLevel() + mMap.getMaxZoomLevel()) / 2);
                        }
                    }

                    View view = GameMapHere.this.getView();

                    view.setOnTouchListener(new OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            // Regardless of what happens, let's check zoom state
                            final double level = mMap.getZoomLevel();

                            if (level != mZoomLevel) {
                                updateMetersPerPixel();
                                scaleMarkers();
                                mZoomLevel = level;
                            }

                            return false;
                        }
                    });

                    updateMetersPerPixel();
                } else {
                    Log.e(TAG, "Unable to init maps: " + error);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScaleThread.quit();
    }

    @Override
    public void clearMarkers() {
        Log.d(TAG, "Clearing all markers");
        removePlayerMarkers();
        mMap.removeMapObject(mBlueFlag);
        mMap.removeMapObject(mRedFlag);
    }

    @Override
    public void updateMarkerForPlayer(Player updated, Player old) {
        MapMarker marker = getPlayerMarker(old);
        mPlayerMarkers.put(updated.getId(), marker);
        mPlayerMarkers.remove(old);
    }

    @Override
    public void updatePlayerMarkerPosition(Player player) {
        Log.d(TAG, "Updating player with ID " + player.getId());

        if (!playerHasMarker(player)) {
            // New player joined
            Log.d(TAG, "Adding new player with name " + player.getName());
            MapMarker marker = MarkerFactoryHere.createPlayerMarker(
                    player, getResources().getDisplayMetrics(), getResources());
            Log.d(TAG, "New marker to: " + marker.getCoordinate().getLatitude()
                    + "; " + marker.getCoordinate().getLongitude());
            addPlayerMarker(player, marker);
        } else {
            Log.d(TAG, "Updating marker of existing player with name " + player.getName());
            MapMarker marker = getPlayerMarker(player);
            marker.setCoordinate(MapFactory.createGeoCoordinate(player.getLatitude(), player.getLongitude()));
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
            MapMarker marker = MarkerFactoryHere.createPlayerMarker(player, getResources().getDisplayMetrics(), getResources());
            addPlayerMarker(player, marker);
        }

        final int markerSize = MarkerFactoryGoogle.calculateMarkerSize(getActivity().getResources().getDisplayMetrics(), mCurrentMetersPerPixels);
        mRedFlag = MarkerFactoryHere.createFlagMarker(game.getRedFlag(), mRedFlagBitmap, markerSize);
        mBlueFlag = MarkerFactoryHere.createFlagMarker(game.getBlueFlag(), mBlueFlagBitmap, markerSize);
        updateMetersPerPixel();

        mMap.addMapObject(mRedFlag);
        mMap.addMapObject(mBlueFlag);
    }

    @Override
    public void centerMapToPosition(Location location) {
        mZoomLevel = DEFAULT_MAP_ZOOM_LEVEL_IN_GAME;
        mMap.setCenter(locationToGeoCoordinate(location), MapAnimation.LINEAR, mZoomLevel, 0, 0);
    }

    /**
     * Updates the current meters per pixel value based on the current
     * {@link Location} and map zoom level.
     */
    private void updateMetersPerPixel() {
        mCurrentMetersPerPixels = GameMapUtils.calculateMetersPerPixel(
                LocationManagerFactory.getInstance(getActivity()).getCurrentLocation(), mZoomLevel);
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

                    final Image redFlagImage = MapFactory.createImage();
                    redFlagImage.setBitmap(Bitmap.createScaledBitmap(mRedFlagBitmap, markerSize, markerSize, true));

                    final Image blueFlagImage = MapFactory.createImage();
                    blueFlagImage.setBitmap(Bitmap.createScaledBitmap(mBlueFlagBitmap, markerSize, markerSize, true));

                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mRedFlag != null && mBlueFlag != null) {
                                mRedFlag.setIcon(redFlagImage);
                                mBlueFlag.setIcon(blueFlagImage);
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * Converts the given {@link Location} to {@link GeoCoordinate}.
     *
     * @param location {@link Location} to convert.
     * @return {@link GeoCoordinate}.
     */
    private GeoCoordinate locationToGeoCoordinate(Location location) {
        return MapFactory.createGeoCoordinate(location.getLatitude(), location.getLongitude());
    }

    /**
     * Adds {@link Player} {@link MapMarker} to the map and stores references
     * to the {@link Player} and the {@link MapMarker} in a {@link HashMap}
     *
     * @param player
     * @param marker
     */
    private void addPlayerMarker(Player player, MapMarker marker) {
        mMap.addMapObject(marker);
        mPlayerMarkers.put(player.getId(), marker);
    }

    /**
     * Returns a map {@link MapMarker} for the given {@link Player} from the {@link HashMap}.
     *
     * @param player {@link Player} for which to return the {@link MapMarker} for.
     * @return {@link MapMarker} for the given {@link Player}.
     */
    private MapMarker getPlayerMarker(Player player) {
        return mPlayerMarkers.get(player.getId());
    }

    /**
     * Removes all {@link Player} map {@link MapMarker} objects from the map and
     * the {@link HashMap}.
     */
    private void removePlayerMarkers() {
        for (MapMarker marker : mPlayerMarkers.values()) {
            mMap.removeMapObject(marker);
        }
        mPlayerMarkers.clear();
    }
}
