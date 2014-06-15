/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network;

import org.json.JSONObject;

/**
 * Request for getting initial data from server when the app is started.
 */
public class GameListRequest extends JSONRequest {
    public GameListRequest() {
        super("gamelist");
    }

    @Override
    public JSONObject getRequestData() {
        return new JSONObject();
    }
}
