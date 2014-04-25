/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.here.android.common.GeoCoordinate;
import com.here.android.common.GeoPosition;
import com.here.android.common.Image;
import com.here.android.common.LocationMethod;
import com.here.android.common.LocationStatus;
import com.here.android.common.PositionListener;
import com.here.android.common.PositioningManager;
import com.here.android.mapping.FragmentInitListener;
import com.here.android.mapping.InitError;
import com.here.android.mapping.Map;
import com.here.android.mapping.MapAnimation;
import com.here.android.mapping.MapFactory;
import com.here.android.mapping.MapFragment;
import com.here.android.mapping.MapMarker;
import com.here.android.mapping.MapObject;
import com.nokia.example.capturetheflag.R;
import com.nokia.example.capturetheflag.Settings;
import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.Player;

/**
 * Fragment that is responsible for showing the map and handle map related
 * actions like adding map markers etc.
 */
public class GameMap extends MapFragment {
    public static final double DEFAULT_MAP_ZOOM_LEVEL_IN_GAME = 14;
    private static final String TAG = "CtF/GameMap";
    private static final double DEGREES_TO_RADS = Math.PI / 180;
    private static final double CALC_CONSTANT = 2 * Math.PI * 6378137;
    private static final double DEFAULT_LATITUDE = 61.497885;
    private static final double DEFAULT_LONGITUDE = 23.770037;

    private Map mMap;
    private PositioningManager mPosManager;
    private ArrayList<MapObject> mMarkers = new ArrayList<MapObject>();
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
    private PositionListener mPositionListener;

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
                                MapFactory.createGeoCoordinate(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
                                MapAnimation.NONE);
                        
                        if (mZoomLevel > 0) {
                            mMap.setZoomLevel(mZoomLevel);
                        }
                        else {
                            mMap.setZoomLevel((mMap.getMinZoomLevel() + mMap.getMaxZoomLevel()) / 2);
                        }
                    }
                    
                    initLocation();
                    
                    View view = GameMap.this.getView();
                    
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
    public void onPause() {
        super.onPause();
        
        if (mPosManager != null) {
            Log.d(TAG, "Stopping position manager");
            mPosManager.stop();
            if(mPositionListener != null) {
            	mPosManager.removePositionListener(mPositionListener);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if (mPosManager != null) {
            Log.d(TAG, "Starting position manager");
            
            mPosManager.start(LocationMethod.GPS_NETWORK);
            if(mPositionListener != null) {
                mPosManager.addPositionListener(mPositionListener);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScaleThread.quit();
    }

    public void setMarkers(Game game, Player user) {
        ArrayList<Player> players = game.getPlayers();
        ArrayList<MapObject> markers = new ArrayList<MapObject>();
        
        for (Player player : players) {
            MapMarker marker = MarkerFactory.createPlayerMarker(
                    player, getResources().getDisplayMetrics(), getResources());
            Log.d(TAG, "Adding marker to: " + player.getLatitude() + "; "
                    + player.getLongitude() + ", name: " + player.getName()
                    + ", id: " + player.getId());
            
            player.setMarker(marker);
            
            if (player.equals(user)) {
                Log.d(TAG, "User object marker added");
                user.setMarker(marker);
            }
            
            markers.add(marker);
        }
        
        mRedFlag = MarkerFactory.createFlagMarker(
                game.getRedFlag(), mRedFlagBitmap, calculateMarkerSize());
        mBlueFlag = MarkerFactory.createFlagMarker(
                game.getBlueFlag(), mBlueFlagBitmap, calculateMarkerSize());
        updateMetersPerPixel();
        
        markers.add(mRedFlag);
        markers.add(mBlueFlag);
        addMarkers(markers);
    }

    public void updatePlayerMarker(Player player) {
        Log.d(TAG, "Updating player with ID " + player.getId());
        
        if (player.getMarker() == null) {
            // New player joined
            Log.d(TAG, "Adding new player with name " + player.getName());
            MapMarker marker =  MarkerFactory.createPlayerMarker(
                    player, getResources().getDisplayMetrics(), getResources());
            Log.d(TAG, "New marker to: " + marker.getCoordinate().getLatitude()
                    + "; " + marker.getCoordinate().getLongitude());
            player.setMarker(marker);
            addMarker(marker);
            showPlayerJoinedToast(player);
        }
        else {
            Log.d(TAG, "Updating marker of existing player with name " + player.getName());
            removeMarker(player.getMarker());
            player.getMarker().setCoordinate(
                    MapFactory.createGeoCoordinate(
                            player.getLatitude(), player.getLongitude()));
            addMarker(player.getMarker());
        }
    }
    private void showPlayerJoinedToast(Player p) {
        Log.d(TAG, "show playerjoined toast");
        String text = getString(R.string.player) + " " + p.getName() + " " +
                        getString(R.string.joined);
        Toast toast = Toast.makeText(getActivity(),
                text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public void clearMarkers() {
        Log.d(TAG, "Clearing all (" + mMarkers.size() + ") markers");
        mMap.removeMapObjects(mMarkers);
        mMarkers.clear();
    }

    public GeoPosition getCurrentPosition() {
        return mPosManager.getPosition();
    }

    /**
     * For convenience.
     */
    public void centerMapToUserPosition() {
        setMapPosition(mPosManager.getPosition().getCoordinate(), mZoomLevel, MapAnimation.LINEAR);
    }

    public void centerMapToUserPosition(double zoomLevel, MapAnimation animation) {
        setMapPosition(mPosManager.getPosition().getCoordinate(), zoomLevel, animation);
    }

    public void setMapPosition(GeoCoordinate coordinate, double zoomLevel, MapAnimation animation) {
        mMap.setCenter(coordinate, animation, zoomLevel, 0, 0);
        
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

    private void initLocation() {
        mPosManager = MapFactory.getPositioningManager();
        Log.d(TAG, "Position manager set");
        LocationStatus status = mPosManager.getLocationStatus(LocationMethod.GPS);
        
        if (status == LocationStatus.OUT_OF_SERVICE) {
            showEnableGPSDialog();
        }
        else {
            mPosManager.start(LocationMethod.GPS_NETWORK);
            if (mPositionListener != null) {
                mPosManager.addPositionListener(mPositionListener);
            }
            GeoCoordinate currentloc = mPosManager.getPosition().getCoordinate();
            mMap.setCenter(currentloc, MapAnimation.LINEAR);
        }
    }

    private void showEnableGPSDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getText(R.string.gps_not_enabled));
        
        builder.setPositiveButton(
                getResources().getText(R.string.action_settings),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        getActivity().startActivity(i);
                    }
        });
        
        builder.setNegativeButton(
                getResources().getText(R.string.cancel),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
        });
        
        builder.create().show();
    }

    private void addMarker(MapObject marker) {
        mMap.addMapObject(marker);
        mMarkers.add(marker);
    }

    private void removeMarker(MapObject marker) {
        mMap.removeMapObject(marker);
        mMarkers.remove(marker);
    }

    private void addMarkers(List<MapObject> markers) {
        mMarkers.addAll(markers);
        mMap.addMapObjects(markers);
    }

    private void updateMetersPerPixel() {
        GeoCoordinate coordinate = this.getCurrentPosition().getCoordinate();
        mCurrentMetersPerPixels =
                (Math.cos(coordinate.getLatitude() * DEGREES_TO_RADS) * CALC_CONSTANT)
                / (256 * Math.pow(2, mMap.getZoomLevel()));
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

	public void setPositionListener(PositionListener listener) {
		if (mPositionListener != listener) {
			mPositionListener = listener;
			if (mPosManager != null) {

				mPosManager.addPositionListener(listener);
			}
		}
	}
}
