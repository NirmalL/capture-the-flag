/**
 * Copyright (c) 2014 Nokia Corporation.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.network;

import org.json.JSONObject;

/**
 * Abstract base class for any type of JSON request.
 */
public abstract class JSONRequest {
	protected String mEventName;
	
	public JSONRequest(String eventName) {
		mEventName = eventName;
	}
	
	public String getEventName() {
		return mEventName;
	}
    public abstract JSONObject getRequestData();
}
