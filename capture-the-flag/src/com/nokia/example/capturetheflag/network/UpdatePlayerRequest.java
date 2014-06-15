/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network;

import org.json.JSONObject;

import com.nokia.example.capturetheflag.network.model.ModelConstants;
import com.nokia.example.capturetheflag.network.model.Player;

import android.util.Log;

/**
 * Request for updating the player position, takes a player object and game ID.
 */
public class UpdatePlayerRequest extends JSONRequest {
    public static final String EVENT_NAME = "update-player";
    private static final String TAG = "CtF/UpdatePlayerRequest";
    private Player mPlayer;
    private int mGameId;

    public UpdatePlayerRequest(Player player, int gameId) {
        super(EVENT_NAME);
        mPlayer = player;
        mGameId = gameId;
    }

    public Player getUpdatedPlayer() {
        return mPlayer;
    }

    @Override
    public JSONObject getRequestData() {
        JSONObject obj = new JSONObject();
        try {
            obj.put(ModelConstants.TYPE_KEY, ModelConstants.UPDATE_PLAYER_TYPE);
            obj.put(ModelConstants.GAME_ID_KEY, mGameId);
            obj.put(ModelConstants.PLAYER_KEY, mPlayer.toJSON());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return obj;
    }
}
