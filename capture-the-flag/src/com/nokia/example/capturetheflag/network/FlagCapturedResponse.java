/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network;


import com.nokia.example.capturetheflag.network.model.Player;

/**
 * Response for the flag captured event.
 */
public class FlagCapturedResponse extends JSONResponse {
    private Player mCapturer;

    public FlagCapturedResponse() {
        setType(JSONResponse.FLAG_CAPTURED);
    }

    public void setCapturer(Player player) {
        mCapturer = player;
    }

    public Player getCapturer() {
        return mCapturer;
    }
}
