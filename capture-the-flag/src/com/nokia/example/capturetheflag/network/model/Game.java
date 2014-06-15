/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Object representing the game, contains the players and the flags.
 */
public class Game {
    public static final int NEW_GAME = 0;

    private ArrayList<Player> mPlayers;
    private String mName;
    private Flag mRedFlag;
    private Flag mBlueFlag;
    private int mId;
    private boolean mHasEnded = false;
    private boolean mIsPremium = false;

    public Game(int id) {
        mId = id;
        mPlayers = new ArrayList<Player>();
    }

    public Game(final JSONObject jsonObj) throws JSONException {
        this(jsonObj.getInt(ModelConstants.ID_KEY));
        setName(jsonObj.getString(ModelConstants.NAME_KEY));
        JSONArray jsonArray = jsonObj.getJSONArray(ModelConstants.PLAYERS_KEY);

        for (int i = 0; i < jsonArray.length(); i++) {
            Player p = new Player(jsonArray.getJSONObject(i));
            addPlayer(p);
        }

        JSONObject redflag = jsonObj.getJSONObject(ModelConstants.RED_FLAG_KEY);
        JSONObject blueflag = jsonObj.getJSONObject(ModelConstants.BLUE_FLAG_KEY);

        mRedFlag = new Flag(
                redflag.getDouble(ModelConstants.LATITUDE_KEY),
                redflag.getDouble(ModelConstants.LONGITUDE_KEY));
        mBlueFlag = new Flag(
                blueflag.getDouble(ModelConstants.LATITUDE_KEY),
                blueflag.getDouble(ModelConstants.LONGITUDE_KEY));

        mIsPremium = jsonObj.optBoolean(ModelConstants.IS_PREMIUM_KEY, false);
    }

    @Override
    public String toString() {
        return mName;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(ModelConstants.NAME_KEY, getName());
        obj.put(ModelConstants.ID_KEY, getId());
        obj.put(ModelConstants.RED_FLAG_KEY, mRedFlag.toJSON());
        obj.put(ModelConstants.BLUE_FLAG_KEY, mBlueFlag.toJSON());
        obj.put(ModelConstants.PLAYERS_KEY, new JSONArray());
        return obj;
    }

    public void setId(int mId) {
        this.mId = mId;
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

    public void addPlayer(Player p) {
        mPlayers.add(p);
    }

    public ArrayList<Player> getPlayers() {
        return mPlayers;
    }

    public void setRedFlag(Flag flag) {
        mRedFlag = flag;
    }

    public Flag getRedFlag() {
        return mRedFlag;
    }

    public void setBlueFlag(Flag flag) {
        mBlueFlag = flag;
    }

    public Flag getBlueFlag() {
        return mBlueFlag;
    }

    public void setHasEnded(boolean hasEnded) {
        mHasEnded = hasEnded;
    }

    public boolean getHasEnded() {
        return mHasEnded;
    }

    public void setPremium(boolean isPremium) {
        mIsPremium = isPremium;
    }

    public boolean isPremium() {
        return mIsPremium;
    }
}
