/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network;

import com.nokia.example.capturetheflag.network.model.Player;

/**
 * Response for when a player's position in the game has changed.
 */
public class UpdatePlayerResponse extends JSONResponse {
    private Player mUpdatePlayer;

    public UpdatePlayerResponse() {
        setType(JSONResponse.UPDATE_PLAYER);
    }

    public void setUpdatedPlayer(Player player) {
        mUpdatePlayer = player;
    }

    public Player getUpdatedPlayer() {
        return mUpdatePlayer;
    }
}
