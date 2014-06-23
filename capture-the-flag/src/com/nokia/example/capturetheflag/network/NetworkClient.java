/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network;

/**
 * Abstract base class for network clients.
 */
public abstract class NetworkClient {

    protected enum State {
        IDLE,
        CONNECTING,
        CONNECTED
    };

    protected NetworkListener mListener;
    protected State mState = State.IDLE;

    /**
     * Connects to the server.
     *
     * @param url  The server URL.
     * @param port The server port.
     */
    public abstract void connect(final String url, final int port);

    /**
     * Sets the callback listener that will be called when different kind of
     * messages are received.
     *
     * @param listener
     */
    public void setListener(NetworkListener listener) {
        mListener = listener;
    }

    /**
     * Send request to the server
     *
     * @param request
     */
    public abstract void emit(JSONRequest request);

    /**
     * Check if connection to server is open
     *
     * @return
     */
    public abstract boolean isConnected();

    /**
     * Disconnect from server
     */
    public abstract void disconnect();

    /**
     * Sets the connection idle, if the app has gone to a state where continuous
     * connection is not required.
     *
     * @param isIdle true for setting it idle, false for waking it up
     */
    public abstract void setConnectionIdle(boolean isIdle);

    public abstract void cleanUp();

    /**
     * Listener interface for network clients.
     */
    public static interface NetworkListener {
        public void onError(JSONResponse resp);

        public void onUpdatePlayerMessage(UpdatePlayerResponse resp);

        public void onJoinedMessage(JoinedResponse resp);

        public void onGameListMessage(GameListResponse resp);

        public void onFlagCapturedMessage(FlagCapturedResponse resp);

        public void onNetworkStateChange(boolean isConnected, NetworkClient client);
    }
}
