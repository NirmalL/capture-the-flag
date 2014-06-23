/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.Player;

/**
 * Socket I/O version of the network client, @see {@link NetworkClient}.
 */
public class SocketIONetworkClient
        extends NetworkClient
        implements ConnectCallback {
    public static final int DISCONNECT_TIMEOUT = 1000 * 60;
    private static final String TAG = "CtF/SocketIONetworkClient";
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    
    private Handler mHandler;
    private SocketIOClient mSocketClient;
    private JSONRequest queuedMessage;
    private Timer mTimer;
    private TimerTask mIdleTask;
    private String mUrl;
    private int mPort;
    private int mReconnectAttempts;

    public SocketIONetworkClient() {
        mHandler = new Handler();
        mTimer = new Timer();
    }

    public void connect(final String url, final int port) {
        if (mState == State.CONNECTED || mState == State.CONNECTING) {
            Log.d(TAG, "connect(): Already connected or trying to connect!");
            return;
        }
        
        mState = State.CONNECTING;
        mUrl = url;
        mPort = port;
        SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(),
                mUrl + ":" + mPort, this);
    }

    @Override
    public void onConnectCompleted(Exception ex, SocketIOClient client) {
        mSocketClient = client;
        
        if (ex != null) {
            mReconnectAttempts++;
            
            if (mReconnectAttempts > MAX_RECONNECT_ATTEMPTS) {
                Log.e(TAG, "Connection failed. Maximum number of reconnect attempts exceeded. Aborting...");
                disconnect();
                return;
            }
            
            Log.e(TAG, "Connection failed, trying to reconnect (attempt "
                    + mReconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS
                    + ")..." /*, ex*/);
            
            mSocketClient.reconnect();
            return;
        }

        mState = State.CONNECTED;
        mReconnectAttempts = 0;
        mListener.onNetworkStateChange(true, SocketIONetworkClient.this);

        if (queuedMessage != null) {
            Log.d(TAG, "Sending message from queue");
            JSONArray arr = new JSONArray();
            arr.put(queuedMessage.getRequestData());
            client.emit(queuedMessage.getEventName(), arr);
            queuedMessage = null;
        }

        client.addListener("gamelist", new EventCallback() {
            @Override
            public void onEvent(final JSONArray argument, Acknowledge acknowledge) {
                Log.d(TAG, "Game list event: " + argument.toString());

                try {
                    JSONObject msg = argument.getJSONObject(0);
                    JSONArray arr = msg.getJSONArray("games");
                    Game[] games = new Game[arr.length()];

                    for (int i = 0; i < arr.length(); i++) {
                        Game g = new Game(arr.getJSONObject(i));
                        games[i] = g;
                    }

                    final GameListResponse resp = new GameListResponse();
                    resp.setGames(games);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mListener != null) {
                                mListener.onGameListMessage(resp);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "JSON error: ", e);
                }
            }
        });

        client.addListener("joined", new EventCallback() {
            @Override
            public void onEvent(JSONArray argument, Acknowledge acknowledge) {
                try {
                    JSONObject obj = argument.getJSONObject(0);
                    final JoinedResponse joined = new JoinedResponse();
                    JSONObject gameObj = obj.getJSONObject("game");
                    joined.setJoinedGame(new Game(gameObj));
                    Log.d(TAG, "Joined response parsed");
                    joined.setPlayer(new Player(obj.getJSONObject("player")));

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mListener != null) {
                                mListener.onJoinedMessage(joined);
                            }
                        }
                    });
                } catch (JSONException je) {
                    Log.e(TAG, "JSON error: ", je);
                }
            }
        });

        client.addListener("update-player", new EventCallback() {
            @Override
            public void onEvent(JSONArray argument, Acknowledge acknowledge) {
                try {
                    JSONObject obj = argument.getJSONObject(0);
                    Player p = new Player(obj.getJSONObject("update-player"));
                    final UpdatePlayerResponse resp = new UpdatePlayerResponse();
                    resp.setUpdatedPlayer(p);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mListener != null) {
                                mListener.onUpdatePlayerMessage(resp);
                            }
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "JSON error: ", e);
                }
            }
        });

        client.addListener("error", new EventCallback() {
            @Override
            public void onEvent(JSONArray argument, Acknowledge acknowledge) {
                try {
                    JSONObject errorObj = argument.getJSONObject(0);
                    final JSONResponse resp = new JSONResponse();
                    resp.setErrorCode(errorObj.optInt("code", -100));

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mListener != null) {
                                mListener.onError(resp);
                            }
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "JSON error: ", e);
                }
            }
        });
    }

    public synchronized void emit(JSONRequest request) {
        if (mSocketClient != null) {
            if (!mSocketClient.isConnected()) {
                Log.d(TAG, "Adding the request to the queue and trying to reconnect...");
                mSocketClient.reconnect();
                queuedMessage = request;
                return;
            }

            Log.d(TAG, "Sending: " + request.getEventName());
            JSONArray args = new JSONArray();

            if (request.getRequestData() != null) {
                args.put(request.getRequestData());
            }

            mSocketClient.emit(request.getEventName(), args);
        } else {
            Log.d(TAG, "Adding the request to the queue");
            queuedMessage = request;
        }
    }

    public void disconnect() {
        if (mSocketClient != null) {
            mSocketClient.disconnect();

            if (mListener != null) {
                mListener.onNetworkStateChange(false, SocketIONetworkClient.this);
            }
        }

        mState = State.IDLE;
        mReconnectAttempts = 0;
    }

    @Override
    public boolean isConnected() {
        if (mSocketClient != null) {
            return mSocketClient.isConnected();
        }

        return false;
    }

    @Override
    public void setConnectionIdle(boolean isIdle) {
        if (isIdle) {
            mIdleTask = new TimerTask() {
                @Override
                public void run() {
                    if (mSocketClient != null) {
                        Log.d(TAG, "Idle, disconnecting...");
                        mSocketClient.disconnect();
                        mState = State.IDLE;
                        mReconnectAttempts = 0;
                    }
                }
            };

            mTimer.schedule(mIdleTask, DISCONNECT_TIMEOUT);
        } else {
            if (mIdleTask != null) {
                boolean isCanceled = mIdleTask.cancel();

                if (!isCanceled) {
                    if (mSocketClient != null) {
                        Log.d(TAG, "Not idle anymore, waking up...");
                        connect(mUrl, mPort);
                    }
                }
            }
        }
    }

    @Override
    public void cleanUp() {
        mListener = null;

        if (isConnected()) {
            disconnect();
        }
    }
}
