/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

/**
 * Represents a single player in a game. Player object also contains the marker
 * object that is shown in the map.
 * <p/>
 * Note: The equals and hashCode have been overwritten. Thus, only use the ID
 * to differentiate player objects.
 */
public class Player {
    public static final String BLUE = "blue";
    public static final String RED = "red";

    public static final String PLATFORM_NOKIA = "nokia";
    public static final String PLATFORM_GOOGLE = "google";

    private String mName;
    private String mTeam = "none";
    private String mRegId;
    private double mLatitude;
    private double mLongitude;
    private int mId;
    private String mPlatformType;

    public Player(int id, String name) {
        mId = id;
        mName = name;
    }

    public Player(JSONObject jsonObject) throws JSONException {
        this(jsonObject.getInt(ModelConstants.ID_KEY),
                jsonObject.getString(ModelConstants.NAME_KEY));
        mLatitude = jsonObject.getDouble(ModelConstants.LATITUDE_KEY);
        mLongitude = jsonObject.getDouble(ModelConstants.LONGITUDE_KEY);
        mTeam = jsonObject.getString(ModelConstants.TEAM_KEY);
        mRegId = jsonObject.getString(ModelConstants.REGISTRATION_ID_KEY);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof Player) {
            return ((Player) o).getId() == this.getId();
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 13;
        result = 31 * result + mId;
        return result;
    }

    public void setLocation(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setTeam(String team) {
        mTeam = team;
    }

    public String getTeam() {
        return mTeam;
    }

    public void setRegistrationId(String id) {
        mRegId = id;
    }

    public String getRegistrationId() {
        return mRegId;
    }

    public void setPlatformType(String platformType) {
        mPlatformType = platformType;
    }

    public String getPlatformType() {
        return mPlatformType;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(ModelConstants.NAME_KEY, getName());
        obj.put(ModelConstants.ID_KEY, getId());
        obj.put(ModelConstants.LATITUDE_KEY, getLatitude());
        obj.put(ModelConstants.LONGITUDE_KEY, getLongitude());
        obj.put(ModelConstants.TEAM_KEY, getTeam());
        obj.put(ModelConstants.REGISTRATION_ID_KEY, getRegistrationId());
        obj.put(ModelConstants.PLATFORM_TYPE_KEY, getPlatformType());
        return obj;
    }
}
