/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network;

import com.nokia.example.capturetheflag.network.model.Player;

/**
 * Base class for all JSON responses. The class has a static createResponse()
 * method which acts as a factory method for creating different kind of
 * responses based on the given JSON data.
 */
public class JSONResponse {
    public static final int GAMELIST = 0;
    public static final int JOINED = 1;
    public static final int UPDATE_PLAYER = 2;
    public static final int ERROR = 3;
    public static final int FLAG_CAPTURED = 4;

    public static final int ERROR_GAME_FULL = -1;
    public static final int ERROR_EXCEPTION = -2;

    // Special case mainly used by an observer to listen all events
    public static final int ALL = 3;

    private Player mPlayer = null;
    private Exception mException = null;
    private int mType = 0;
    private int mErrorCode = 0;

    public JSONResponse() {
    }

    public Exception getException() {
        return mException;
    }

    public final int getType() {
        return mType;
    }

    public final Player getPlayer() {
        return mPlayer;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public final void setPlayer(Player player) {
        mPlayer = player;
    }

    protected void setException(Exception exception) {
        mException = exception;
    }

    protected void setType(int type) {
        mType = type;
    }

    public void setErrorCode(int code) {
        mErrorCode = code;
    }
}
