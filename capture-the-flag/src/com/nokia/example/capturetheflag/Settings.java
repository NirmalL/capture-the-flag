/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * A simple settings class that holds some basic data accessed via shared
 * preferences.
 */
public class Settings {
    public static final int BASE_SIZE = 20; // In meters
    public static final int MINIMUM_MARKER_SIZE = 20; // In dp

    private static final String SERVER_URL_KEY = "server_url";
    private static final String SERVER_PORT_KEY = "server_port";
    private static final String PREMIUM_KEY = "premium";
    private static final String USERNAME_KEY = "username";
    private static final String GAME_NAME_KEY = "game_name";

    /* When testing with emulator user 10.0.2.2 and your server is running on
     * the same computer.
     */
    private static final String DEFAULT_SOCKET_URL = "http://capturetheflag-c9-nokiadeveloper.c9.io";
    private static final int DEFAULT_SOCKET_PORT = 80;

    public static String getServerUrl(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SERVER_URL_KEY, DEFAULT_SOCKET_URL);
    }

    public static boolean setServerUrl(String url, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.edit().putString(SERVER_URL_KEY, url).commit();
    }

    public static int getServerPort(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(SERVER_PORT_KEY, DEFAULT_SOCKET_PORT);
    }

    public static boolean setServerPort(int port, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.edit().putInt(SERVER_PORT_KEY, port).commit();
    }

    public static String getUsername(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(USERNAME_KEY, "");
    }

    public static boolean setUsername(String name, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.edit().putString(USERNAME_KEY, name).commit();
    }

    public static String getPremium(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREMIUM_KEY, "");
    }

    public static boolean setPremium(String productId, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.edit().putString(PREMIUM_KEY, productId).commit();
    }

    public static String getGameName(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(GAME_NAME_KEY, "");
    }

    public static boolean setGameName(String name, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.edit().putString(GAME_NAME_KEY, name).commit();
    }
}
