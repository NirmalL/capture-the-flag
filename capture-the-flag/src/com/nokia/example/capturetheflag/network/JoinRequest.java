/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network;

import org.json.JSONException;
import org.json.JSONObject;

import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.ModelConstants;
import com.nokia.example.capturetheflag.network.model.Player;

/**
 * Request for joining a game.
 */
public class JoinRequest extends JSONRequest {
    public static final String EVENT_NAME = "join";
    private Game mGame;
    private Player mPlayer;

    public JoinRequest(Game game, Player player) {
        super(EVENT_NAME);
        mGame = game;
        mPlayer = player;
    }

    public Game getGame() {
        return mGame;
    }

    public Player getPlayer() {
        return mPlayer;
    }

    @Override
    public JSONObject getRequestData() {
        JSONObject obj = new JSONObject();
        try {
            obj.put(ModelConstants.TYPE_KEY, ModelConstants.JOIN_VALUE);
            obj.put(ModelConstants.GAME_ID_KEY, mGame.getId());
            obj.put(ModelConstants.GAME_KEY, mGame.toJSON());
            obj.put(ModelConstants.PLAYER_KEY, mPlayer.toJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
