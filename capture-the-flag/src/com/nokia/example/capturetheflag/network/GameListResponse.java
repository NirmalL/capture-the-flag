/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network;


import com.nokia.example.capturetheflag.network.model.Game;

/**
 * Response for getting the list of current games on the server.
 */
public class GameListResponse extends JSONResponse {
    private Game[] mGames = new Game[0];

    public GameListResponse() {
        setType(JSONResponse.GAMELIST);
    }

    public void setGames(Game[] games) {
        mGames = games;
    }

    public Game[] getGames() {
        return mGames;
    }

}
