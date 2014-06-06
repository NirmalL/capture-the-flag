package com.nokia.example.capturetheflag.location.here;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.here.android.common.GeoCoordinate;
import com.here.android.common.GeoPosition;
import com.here.android.common.LocationMethod;
import com.here.android.common.LocationStatus;
import com.here.android.common.PositionListener;
import com.here.android.common.PositioningManager;
import com.here.android.mapping.FactoryInitListener;
import com.here.android.mapping.InitError;
import com.here.android.mapping.MapFactory;
import com.nokia.example.capturetheflag.location.LocationManagerBase;

/**
 * Location Manager implementation that uses Here-specific APIs, i.e {@link PositioningManager}.
 *
 */
public class LocationManagerHere extends LocationManagerBase implements PositionListener {

	private static final String TAG = "CtF/LocationManagerHere";
	
    private PositioningManager mPosManager;
    
    public LocationManagerHere(Activity activity) {
    	super();
    	MapFactory.initFactory(activity.getApplicationContext(), new FactoryInitListener() {
			
			@Override
			public void onFactoryInitializationCompleted(InitError error) {
		        mPosManager = MapFactory.getPositioningManager();
		        mPosManager.addPositionListener(LocationManagerHere.this);
				notifyManagerReady(error == InitError.NONE);
			}
		});
    }

    @Override
    public boolean isLocationAvailable() {
        LocationStatus status = mPosManager.getLocationStatus(LocationMethod.GPS);
        return status != LocationStatus.OUT_OF_SERVICE;
    }
    
	@Override
	public void onPositionFixChanged(LocationMethod arg0, LocationStatus arg1) {
		// Not implemented		
	}

	@Override
	public void onPositionUpdated(LocationMethod arg0, GeoPosition arg1) {
		Location location = new Location("");
		GeoCoordinate coords = arg1.getCoordinate();
		location.setLatitude(coords.getLatitude());
		location.setLongitude(coords.getLongitude());

		notifyListeners(location);
	}
	
	@Override
	public void start() {
        if (mPosManager != null) {
            Log.d(TAG, "Starting position manager");
            mPosManager.start(LocationMethod.GPS_NETWORK);
        }		
	}
	
	@Override
	public void stop() {
        if (mPosManager != null) {
            Log.d(TAG, "Stopping position manager");
            mPosManager.stop();
        }		
	}

	@Override
	public Location getCurrentLocation() {
		GeoCoordinate coords = mPosManager.getPosition().getCoordinate();
		Location location = new Location("");
		location.setLatitude(coords.getLatitude());
		location.setLongitude(coords.getLongitude());

		return location;
	}	
}
