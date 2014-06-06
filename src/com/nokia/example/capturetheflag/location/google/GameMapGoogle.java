/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.location.google;

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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nokia.example.capturetheflag.R;
import com.nokia.example.capturetheflag.Settings;
import com.nokia.example.capturetheflag.location.GameMapInterface;
import com.nokia.example.capturetheflag.location.GameMapSettings;
import com.nokia.example.capturetheflag.location.LocationManagerFactory;
import com.nokia.example.capturetheflag.location.LocationManagerInterface;
import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.Player;

public class GameMapGoogle extends MapFragment implements GameMapInterface, OnCameraChangeListener {

	private static final String TAG = "CtF/GameMapGoogle";
	
    private LocationManagerInterface mLocationManager;
	private GoogleMap mMap;

	private HashMap<Player, Marker> mPlayerMarkers = new HashMap<Player, Marker>();
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

		mMap = getMap();
        mMap.setOnCameraChangeListener(this);
    
        if (mIsFirstTime) {
        	// TODO: No animation
        	mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(GameMapSettings.DEFAULT_LATITUDE, GameMapSettings.DEFAULT_LONGITUDE)));

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
		mPlayerMarkers.put(updated, marker);
		mPlayerMarkers.remove(old);
	}

    @Override
	public void updatePlayerMarkerPosition(Player player) {
        Log.d(TAG, "Updating player with ID " + player.getId());
        
        if(!playerHasMarker(player)) {
            // New player joined
            Log.d(TAG, "Adding new player with name " + player.getName());
            MarkerOptions marker =  MarkerFactoryGoogle.createPlayerMarker(player, getResources().getDisplayMetrics(), getResources());
            //Log.d(TAG, "New marker to: " + marker.getCoordinate().getLatitude() + "; " + marker.getCoordinate().getLongitude());
            //player.setMarker(marker);
            addPlayerMarker(player, marker);
        } else {
            Log.d(TAG, "Updating marker of existing player with name " + player.getName() + "pos: " + player.getLatitude() + ", " + player.getLongitude());
            getPlayerMarker(player).setPosition(new LatLng(player.getLatitude(), player.getLongitude()));
        }		
	}

	@Override
    public boolean playerHasMarker(Player player) {
		return mPlayerMarkers.containsKey(player);
    }

	@Override
	public void setMarkers(Game game, Player user) {
        ArrayList<Player> players = game.getPlayers();
        //ArrayList<MarkerOptions> markers = new ArrayList<MarkerOptions>();
        
        for (Player player : players) {
        	MarkerOptions marker = MarkerFactoryGoogle.createPlayerMarker(player, getResources().getDisplayMetrics(), getResources());
            Log.d(TAG, "Adding marker to: " + player.getLatitude() + "; " + player.getLongitude() + ", name: " + player.getName() + ", id: " + player.getId());
            // TODO!!
            //player.setMarker(marker);
            
            if (player.equals(user)) {
                Log.d(TAG, "User object marker added");
                // TODO!!
                // user.setMarker(marker);
            }
            addPlayerMarker(player, marker);
            //markers.add(marker);
        }
        
        mRedFlag = mMap.addMarker(MarkerFactoryGoogle.createFlagMarker(game.getRedFlag(), mRedFlagBitmap, calculateMarkerSize()));
        mBlueFlag = mMap.addMarker(MarkerFactoryGoogle.createFlagMarker(game.getBlueFlag(), mBlueFlagBitmap, calculateMarkerSize()));
        updateMetersPerPixel();        
	}

	@Override
	public void centerMapToUserPosition() {
		Location loc = mLocationManager.getCurrentLocation();
		LatLng lat = new LatLng(loc.getLatitude(), loc.getLongitude());
		mMap.animateCamera(CameraUpdateFactory.newLatLng(lat));
		
        updateMetersPerPixel();		
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		// TODO Auto-generated method stub
		
        final float level = position.zoom;
        
        if (level != mZoomLevel) {
            updateMetersPerPixel();
            scaleMarkers();
            mZoomLevel = level;
        }
	}

    private void addPlayerMarker(Player player, MarkerOptions marker) {
    	Marker m = mMap.addMarker(marker);
    	Log.d(TAG, "Added marker:" + m.getPosition().latitude + ", " + m.getPosition().longitude);
    	mPlayerMarkers.put(player, m);
    }

    private Marker getPlayerMarker(Player player) {
    	return mPlayerMarkers.get(player);
    }
    
    private void removePlayerMarkers() {
    	for (Marker marker : mPlayerMarkers.values()) {
			marker.remove();
		}
    	mPlayerMarkers.clear();
    }
    
    private void updateMetersPerPixel() {
        Location coordinate = mLocationManager.getCurrentLocation();
        mCurrentMetersPerPixels = (Math.cos(coordinate.getLatitude() * GameMapSettings.DEGREES_TO_RADS) * GameMapSettings.CALC_CONSTANT) / (256 * Math.pow(2, mZoomLevel));
    }

    /**
     * Scales markers asynchronously.
     */
    private void scaleMarkers() {
        if (mRedFlag != null && mBlueFlag != null) {
            mScaleHandler.post(new Runnable() {
                @Override
                    public void run() {
                	return; /*
                        int size = calculateMarkerSize();
                        
                        final Image redFlagImage = MapFactory.createImage();
                        redFlagImage.setBitmap(Bitmap.createScaledBitmap(mRedFlagBitmap, size, size, true));
                        
                        final Image blueFlagImage = MapFactory.createImage();
                        blueFlagImage.setBitmap(Bitmap.createScaledBitmap(mBlueFlagBitmap, size, size, true));
                        
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mRedFlag != null && mBlueFlag != null) {
                                    mRedFlag.setIcon(redFlagImage);
                                    mBlueFlag.setIcon(blueFlagImage);
                                }
                            }
                        });*/
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
}
