/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network.model;

/**
 * Contains various constants for mainly keys in key-value pairs. Note that
 * in case you want to change the values of the constants, you have to be
 * careful since most of them are in sync with the server implementation.
 */
public class ModelConstants {
    public static final String IS_PREMIUM_KEY = "premium";
    public static final String NAME_KEY = "name";
    public static final String ID_KEY = "id";
    public static final String GAME_ID_KEY = "gameid";
    public static final String GAME_KEY = "game";
    public static final String PLAYER_KEY = "player";
    public static final String PLAYERS_KEY = "players";
    public static final String TEAM_KEY = "team";
    public static final String LATITUDE_KEY = "latitude";
    public static final String LONGITUDE_KEY = "longitude";
    public static final String RED_FLAG_KEY = "redflag";
    public static final String BLUE_FLAG_KEY = "blueflag";
    public static final String REGISTRATION_ID_KEY = "regId";
    public static final String TYPE_KEY = "type";
    public static final String BEGIN_VALUE = "begin";
    public static final String JOIN_VALUE = "join";
    public static final String CAPTURER_KEY = "capturer";
    public static final String CAPTURED_BY_PLAYER_KEY = "captured_by_player";
    public static final String PLAYER_NAME_KEY = "player_name";
    public static final String PLATFORM_TYPE_KEY = "platform";

    // Types for JSON responses
    public static final String GAME_LIST_TYPE = "gamelist";
    public static final String JOINED_TYPE = "joined";
    public static final String UPDATE_PLAYER_TYPE = "update-player";
    public static final String FLAG_CAPTURED_TYPE = "flag-captured";
    public static final String ERROR_TYPE = "error";
}
