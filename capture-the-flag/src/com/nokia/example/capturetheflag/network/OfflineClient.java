/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.network;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import com.nokia.example.capturetheflag.network.model.Flag;
import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.Player;
import com.nokia.example.capturetheflag.Settings;
import com.nokia.example.capturetheflag.location.LocationUtils;
import com.nokia.example.capturetheflag.network.FlagCapturedResponse;
import com.nokia.example.capturetheflag.network.UpdatePlayerResponse;
import com.nokia.example.capturetheflag.network.JoinedResponse;

/**
 * Offline version of the network client, @see {@link NetworkClient}.
 */
public class OfflineClient extends NetworkClient {
    private static final String TAG = "CaptureFlag/OfflineClient";
    private static final String SUB_TAG = "CaptureFlag/OpponentAdvance";

    private static final String OPPONENT_NAME = "WIMP";
    private static final long POSITION_UPDATE_INTERVAL = 5000; // Milliseconds
    private static final double SECONDS_PER_UPDATE_INTERVAL = POSITION_UPDATE_INTERVAL / 1000;
    private static final double ADVANCE_SPEED = 1 * SECONDS_PER_UPDATE_INTERVAL; // Meters per second
    private static final int OPPONENT_ID = 42;

    private TimerTask mOpponentAdvanceTask;
    private Timer mTimer;
    private Game mGame = null;
    private Flag mTargetFlag = null;
    private Player mOpponent = null;

    public OfflineClient() {
    }

    @Override
    public void connect(final String url, final int port) {
        mOpponentAdvanceTask = new TimerTask() {

            @Override
            public void run() {
                Log.d(SUB_TAG, "opponent moving");
                
                if (mOpponent == null || mTargetFlag == null) {
                    Log.e(SUB_TAG, "Objects are null!");
                    stop();
                    return;
                }

                double toBearing = Math.toDegrees(LocationUtils
                        .calculateBearingInputInDegrees(
                                mOpponent.getLatitude(),
                                mOpponent.getLongitude(),
                                mTargetFlag.getLatitude(),
                                mTargetFlag.getLongitude()));

                double[] newPosition = LocationUtils
                        .calculateTargetCoordinateInputInDegrees(
                                mOpponent.getLatitude(),
                                mOpponent.getLongitude(), toBearing,
                                ADVANCE_SPEED);

                Log.d(SUB_TAG, "The bearing of the virtual opponent is "
                        + toBearing
                        + " degrees and the next coordinates will be ("
                        + newPosition[0] + ", " + newPosition[1] + ")");

                mOpponent.setLatitude(newPosition[0]);
                mOpponent.setLongitude(newPosition[1]);

                UpdatePlayerResponse updatePlayerResponse = new UpdatePlayerResponse();
                updatePlayerResponse.setUpdatedPlayer(mOpponent);
                mListener.onUpdatePlayerMessage(updatePlayerResponse);

                if (opponentIsInTheBase()) {
                    // The virtual opponent has capture the flag
                    FlagCapturedResponse flagCapturedResponse = new FlagCapturedResponse();
                    flagCapturedResponse.setCapturer(mOpponent);
                    mListener.onFlagCapturedMessage(flagCapturedResponse);
                    stop();
                }
            }
        };
        
        mState = State.CONNECTED ;
    }

    @Override
    public void emit(JSONRequest request) {
        if (request.getEventName() == JoinRequest.EVENT_NAME) {
            JoinRequest join = (JoinRequest) request;
            startGame(join.getGame(), join.getPlayer());
        } else if (request.getEventName() == UpdatePlayerRequest.EVENT_NAME) {
            UpdatePlayerRequest update = (UpdatePlayerRequest) request;
            Player p = update.getUpdatedPlayer();
            final Flag targetFlag = p.getTeam() == Player.BLUE ? mGame
                    .getRedFlag() : mGame.getBlueFlag();

            // Check if we are capturing the flag
            final double distanceToFlag = LocationUtils
                    .calculateDistanceInMetersInputInDegrees(p.getLatitude(),
                            p.getLongitude(), targetFlag.getLatitude(),
                            targetFlag.getLongitude());

            if (distanceToFlag <= Settings.BASE_SIZE) {
                stop();
                FlagCapturedResponse resp = new FlagCapturedResponse();
                resp.setCapturer(p);
                mListener.onFlagCapturedMessage(resp);
                stop();
            }
        }
    }

    private void startGame(Game game, Player player) {
        clearGame();
        mGame = game;
        mGame.addPlayer(player);
        mOpponent = new Player(OPPONENT_ID, OPPONENT_NAME);
        mOpponent.setTeam(player.getTeam() == Player.BLUE ? Player.RED
                : Player.BLUE);
        mOpponent.setLatitude(player.getLatitude());
        mOpponent.setLongitude(player.getLongitude());
        mGame.addPlayer(mOpponent);

        JoinedResponse joinedResponse = new JoinedResponse();
        joinedResponse.setJoinedGame(mGame);
        joinedResponse.setPlayer(player);

        mTargetFlag = mOpponent.getTeam() == Player.BLUE ? game.getRedFlag()
                : game.getBlueFlag();

        mTimer = new Timer();
        mTimer.schedule(mOpponentAdvanceTask, POSITION_UPDATE_INTERVAL,
                POSITION_UPDATE_INTERVAL);
        mListener.onJoinedMessage(joinedResponse);
    }

    @Override
    public void disconnect() {
        stop();
        mState = State.IDLE;
    }

    /**
     * Checks whether the opponent has reached the target base or not.
     *
     * @return True if the virtual opponent is in the base, false otherwise.
     */
    private boolean opponentIsInTheBase() {
        double distanceToFlag = LocationUtils
                .calculateDistanceInMetersInputInDegrees(
                        mOpponent.getLatitude(), mOpponent.getLongitude(),
                        mTargetFlag.getLatitude(), mTargetFlag.getLongitude());
        Log.d(TAG, "The distance between the virtual opponent and the flag is "
                + distanceToFlag + " meters");

        if (distanceToFlag <= Settings.BASE_SIZE) {
            return true;
        }

        return false;
    }

    /**
     * Stops the timer running the virtual opponent.
     */
    private void stop() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mOpponentAdvanceTask = null;
        }
    }

    private void clearGame() {
        Log.d(TAG, "clearGame()");
        stop();
        mGame = null;
        mOpponent = null;
    }

    @Override
    public boolean isConnected() {
        return (mState == State.CONNECTED);
    }

    @Override
    public void setConnectionIdle(boolean isIdle) {
        // Do nothing since offline client doesn't use any network resources
    }

    @Override
    public void cleanUp() {
        mListener = null;
    }
}
