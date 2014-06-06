/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.location;

import android.location.Location;

public interface LocationManagerInterface {

	public boolean isLocationAvailable();
	
	public void start();
	
	public void stop();
	
	public void addListener(LocationManagerListener listener);
	
	public void removeListener(LocationManagerListener listener);
	
	public Location getCurrentLocation();

}
