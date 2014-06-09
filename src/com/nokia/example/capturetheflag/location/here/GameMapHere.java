/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.location.here;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

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
import com.nokia.example.capturetheflag.Settings;
import com.nokia.example.capturetheflag.location.GameMapInterface;
import com.nokia.example.capturetheflag.location.GameMapSettings;
import com.nokia.example.capturetheflag.location.LocationManagerFactory;
import com.nokia.example.capturetheflag.location.LocationManagerInterface;
import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.Player;

/**
 * Fragment that is responsible for showing the map and handle map related
 * actions like adding map markers etc.
 */
public class GameMapHere extends MapFragment implements GameMapInterface {
    public static final double DEFAULT_MAP_ZOOM_LEVEL_IN_GAME = 14;
    private static final String TAG = "CtF/GameMap";

    private LocationManagerInterface mLocationManager;
    private Map mMap;
    
    private HashMap<Player, MapMarker> mPlayerMarkers = new HashMap<Player, MapMarker>();
    //private ArrayList<MapObject> mMarkers = new ArrayList<MapObject>();
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
        mLocationManager = LocationManagerFactory.getLocationManagerInterface(getActivity());
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
                                MapFactory.createGeoCoordinate(GameMapSettings.DEFAULT_LATITUDE, GameMapSettings.DEFAULT_LONGITUDE),
                                MapAnimation.NONE);
                        
                        if (mZoomLevel > 0) {
                            mMap.setZoomLevel(mZoomLevel);
                        }
                        else {
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
                }
                else {
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
        mPlayerMarkers.put(updated, marker);
        mPlayerMarkers.remove(old);
    }

    @Override
    public void updatePlayerMarkerPosition(Player player) {
        Log.d(TAG, "Updating player with ID " + player.getId());
        
        if (!playerHasMarker(player)) {
            // New player joined
            Log.d(TAG, "Adding new player with name " + player.getName());
            MapMarker marker =  MarkerFactoryHere.createPlayerMarker(
                    player, getResources().getDisplayMetrics(), getResources());
            Log.d(TAG, "New marker to: " + marker.getCoordinate().getLatitude()
                    + "; " + marker.getCoordinate().getLongitude());
            //player.setMarker(marker);
            addPlayerMarker(player, marker);
        }
        else {
            Log.d(TAG, "Updating marker of existing player with name " + player.getName());
            MapMarker marker = getPlayerMarker(player);
            marker.setCoordinate(MapFactory.createGeoCoordinate(player.getLatitude(), player.getLongitude()));
        }
    }

    @Override
    public boolean playerHasMarker(Player player) {
        return mPlayerMarkers.containsKey(player);
    }

    @Override
    public void setMarkers(Game game, Player user) {
        ArrayList<Player> players = game.getPlayers();
        
        for (Player player : players) {
            Log.d(TAG, "Adding marker to: " + player.getLatitude() + "; " + player.getLongitude() + ", name: " + player.getName() + ", id: " + player.getId());
            MapMarker marker = MarkerFactoryHere.createPlayerMarker(player, getResources().getDisplayMetrics(), getResources());
            addPlayerMarker(player, marker);
        }
        
        mRedFlag = MarkerFactoryHere.createFlagMarker(
                game.getRedFlag(), mRedFlagBitmap, calculateMarkerSize());
        mBlueFlag = MarkerFactoryHere.createFlagMarker(
                game.getBlueFlag(), mBlueFlagBitmap, calculateMarkerSize());
        updateMetersPerPixel();
        
        mMap.addMapObject(mRedFlag);
        mMap.addMapObject(mBlueFlag);
    }

    @Override
    public void centerMapToPosition(Location location) {
        setMapPosition(mLocationManager.getCurrentLocation(), mZoomLevel, MapAnimation.LINEAR);
    }

    public void setMapPosition(Location location, double zoomLevel, MapAnimation animation) {
        mMap.setCenter(locationToGeoCoordinate(location), animation, zoomLevel, 0, 0);
        
        if (zoomLevel > 0 && mZoomLevel != zoomLevel) {
            mZoomLevel = zoomLevel;
        }
    }

    /**
     * Sets the map zoom level. The method verifies the validity of the given
     * zoom level.
     * 
     * @param zoomLevel The zoom level to set.
     * @param animation The animation to use for zooming.
     */
    public void setZoomLevel(double zoomLevel, MapAnimation animation) {
        if (mZoomLevel != zoomLevel
                && zoomLevel >= mMap.getMinZoomLevel()
                && zoomLevel <= mMap.getMaxZoomLevel())
        {
            try {
                mMap.setZoomLevel(zoomLevel, animation);
            }
            catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            
            updateMetersPerPixel();
            scaleMarkers();
            mZoomLevel = zoomLevel;
        }
        else {
            Log.d(TAG, "setMapZoomLevel(): " + zoomLevel
                + " is invalid zoom level or matches the currently set zoom level. Valid zoom level is ["
                + mMap.getMinZoomLevel() + ", " + mMap.getMaxZoomLevel() + "].");
        }
    }

    public double getZoomLevel() {
        return mZoomLevel;
    }

    private void updateMetersPerPixel() {
        mCurrentMetersPerPixels = GameMapSettings.calculateMetersPerPixel(mLocationManager.getCurrentLocation(), mZoomLevel);
    }

    /**
     * Scales markers asynchronously.
     */
    private void scaleMarkers() {
        if (mRedFlag != null && mBlueFlag != null) {
            mScaleHandler.post(new Runnable() {
                @Override
                    public void run() {
                        int size = calculateMarkerSize();
                        
                        final Image redFlagImage = MapFactory.createImage();
                        redFlagImage.setBitmap(
                                Bitmap.createScaledBitmap(mRedFlagBitmap, size, size, true));
                        
                        final Image blueFlagImage = MapFactory.createImage();
                        blueFlagImage.setBitmap(
                                Bitmap.createScaledBitmap(mBlueFlagBitmap, size, size, true));
                        
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

    private int calculateMarkerSize() {
        int size = (int) (Settings.BASE_SIZE / mCurrentMetersPerPixels);
        int minimumSize = dpToPx(Settings.MINIMUM_MARKER_SIZE);
        
        if (size < minimumSize) {
            size = minimumSize;
        }
        
        return size;
    }

    private int dpToPx(double dp) {
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        int px = (int)((dp * displayMetrics.density) + 0.5);
        return px;
    }
    
    private GeoCoordinate locationToGeoCoordinate(Location location) {
        return MapFactory.createGeoCoordinate(location.getLatitude(), location.getLongitude());
    }

    private void addPlayerMarker(Player player, MapMarker marker) {
        mMap.addMapObject(marker);
        mPlayerMarkers.put(player, marker);
    }

    private MapMarker getPlayerMarker(Player player) {
        return mPlayerMarkers.get(player);
    }
    
    private void removePlayerMarkers() {
        for (MapMarker marker : mPlayerMarkers.values()) {
            mMap.removeMapObject(marker);
        }
        mPlayerMarkers.clear();
    }
}
