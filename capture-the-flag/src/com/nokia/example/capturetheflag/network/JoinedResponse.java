/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network;


import com.nokia.example.capturetheflag.network.model.Game;

/**
 * Response for when a user joins a game.
 */
public class JoinedResponse extends JSONResponse {
    private Game mGameJoined;

    public JoinedResponse() {
        setType(JOINED);
    }

    public void setJoinedGame(Game game) {
        mGameJoined = game;
    }

    public Game getJoinedGame() {
        return mGameJoined;
    }
}
